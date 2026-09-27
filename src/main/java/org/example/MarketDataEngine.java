package org.example;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class MarketDataEngine {

    private static final String RAW_DATA_DIR = "data/raw/";

    private static List<StockData> spyData;
    private static List<StockData> vixData;

    // =========================
    // INIT
    // =========================
    public static void init() throws Exception {
        spyData = loadMarketCSV("spy");
        vixData = loadMarketCSV("vix");
    }

    // =========================
    // FLEXIBLE LOADER
    // =========================
    private static List<StockData> loadMarketCSV(String name) throws Exception {

        List<StockData> data = new ArrayList<>();

        String path = RAW_DATA_DIR + name + ".csv";
        BufferedReader br = new BufferedReader(new FileReader(path));

        String header = br.readLine();

        boolean isStockFormat = header.contains("Close/Last"); // SPY
        boolean isVixFormat = header.contains("OPEN");         // VIX

        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String line;

        while ((line = br.readLine()) != null) {

            String[] p = line.split(",");

            // ===== DATE =====
            String rawDate = p[0].trim();
            LocalDate parsedDate = LocalDate.parse(rawDate, inputFormat);
            String date = parsedDate.format(outputFormat);

            double open, high, low, close;

            if (isStockFormat) {
                // SPY format
                close = Double.parseDouble(p[1].trim());
                open  = Double.parseDouble(p[3].trim());
                high  = Double.parseDouble(p[4].trim());
                low   = Double.parseDouble(p[5].trim());

            } else if (isVixFormat) {
                // VIX format
                open  = Double.parseDouble(p[1].trim());
                high  = Double.parseDouble(p[2].trim());
                low   = Double.parseDouble(p[3].trim());
                close = Double.parseDouble(p[4].trim());

            } else {
                throw new RuntimeException("Unknown CSV format: " + name);
            }

            data.add(new StockData(date, open, high, low, close, 0));
        }

        br.close();

        // Ensure oldest -> newest
        Collections.reverse(data);

        return data;
    }

    // =========================
    // FIND INDEX
    // =========================
    private static int findIndex(List<StockData> data, String date) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).date.equals(date)) return i;
        }
        return -1;
    }

    // =========================
    // GLOBAL FEATURES
    // =========================
    public static Map<String, Double> getMarketFeatures(String date) {

        Map<String, Double> f = new HashMap<>();

        int spyIndex = findIndex(spyData, date);
        int vixIndex = findIndex(vixData, date);

        if (spyIndex < 50 || vixIndex < 5) return null;

        // ===== SPY =====
        double spyClose = spyData.get(spyIndex).close;
        double spyMA50 = FeatureCalculator.movingAverage(spyData, spyIndex, 50);

        f.put("spyAboveMA50", spyClose > spyMA50 ? 1.0 : 0.0);
        f.put("spyReturn5d", FeatureCalculator.returnOver(spyData, spyIndex, 5));

        // ===== VIX =====
        double vixClose = vixData.get(vixIndex).close;

        f.put("vixLevel", vixClose);
        f.put("vixReturn5d", FeatureCalculator.returnOver(vixData, vixIndex, 5));

        // Bucket the VIX level into a coarse volatility regime
        double regime;
        if (vixClose < 15) regime = 0;
        else if (vixClose < 25) regime = 1;
        else regime = 2;

        f.put("vixRegime", regime);

        return f;
    }
}
