package org.example;

import java.io.*;
import java.util.*;
import java.time.*;
import java.time.format.*;

public class CSVLoader {

    public static List<StockData> loadCSV(String ticker) throws Exception {
        List<StockData> data = new ArrayList<>();

        String path = "C:\\NasdaqDownloads\\" + ticker + ".csv";
        BufferedReader br = new BufferedReader(new FileReader(path));

        String line = br.readLine();

        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        while ((line = br.readLine()) != null) {
            String[] p = line.split(","); // ✅ FIXED

            String rawDate = p[0].trim();
            LocalDate parsedDate = LocalDate.parse(rawDate, inputFormat);
            String date = parsedDate.format(outputFormat);

            double close = parseMoney(p[1]);
            double volume = Double.parseDouble(p[2].trim());
            double open = parseMoney(p[3]);
            double high = parseMoney(p[4]);
            double low = parseMoney(p[5]);

            data.add(new StockData(date, open, high, low, close, volume));
        }

        br.close();

        Collections.reverse(data); // oldest → newest

        return data;
    }

    private static double parseMoney(String s) {
        return Double.parseDouble(s.replace("$", "").trim());
    }
}