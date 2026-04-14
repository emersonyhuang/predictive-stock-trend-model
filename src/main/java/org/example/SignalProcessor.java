package org.example;

import java.util.*;

public class SignalProcessor {

    // =========================
    // FIND INDEX
    // =========================
    public static int findIndex(List<StockData> data, String date) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).date.equals(date)) return i;
        }
        return -1;
    }

    // =========================
    // MAIN FEATURE ENGINE
    // =========================
    public static FeatureSet buildFeatures(List<StockData> data, int index) {

        FeatureSet f = new FeatureSet();

        double close = data.get(index).close;

        // =========================
        // 🧩 SHORT TERM
        // =========================

        f.return1d = FeatureCalculator.returnOver(data, index, 1);
        f.return5d = FeatureCalculator.returnOver(data, index, 5);

        // RSI change (simplified placeholder → upgrade later)
        double rsiNow = FeatureCalculator.rsi(data, index, 14);
        double rsiPast = FeatureCalculator.rsi(data, index - 5, 14);
        f.rsi14 = rsiNow;
        f.rsiChange5d = rsiNow - rsiPast;

        // Volume spike
        double avgVol5 = FeatureCalculator.avgVolume(data, index, 5);
        f.volumeSpike = data.get(index).volume / avgVol5;

        // =========================
        // 🧩 MEDIUM TERM
        // =========================

        f.ma20 = FeatureCalculator.movingAverage(data, index, 20);
        f.ma50 = FeatureCalculator.movingAverage(data, index, 50);

        f.distMA20 = (close - f.ma20) / f.ma20;
        f.distMA50 = (close - f.ma50) / f.ma50;

        double ma20Prev = FeatureCalculator.movingAverage(data, index - 5, 20);
        f.ma20Slope = (f.ma20 - ma20Prev) / ma20Prev;

        f.volatility20 = FeatureCalculator.volatility(data, index, 20);

        // =========================
        // 🧩 LONG TERM
        // =========================

        f.return100d = FeatureCalculator.returnOver(data, index, 100);
        f.maxDrawdown100d = FeatureCalculator.maxDrawdown(data, index, 100);

        f.volatility100 = FeatureCalculator.volatility(data, index, 100);

        f.pctAboveMA50 = FeatureCalculator.pctAboveMA50(data, index, 100);

        return f;
    }

    // =========================
    // FINAL ENTRY POINT
    // =========================
    public static DataPoint getDataPoint(String ticker, String date) throws Exception {

        List<StockData> data = CSVLoader.loadCSV(ticker);

        int index = findIndex(data, date);

        if (index == -1) {
            throw new Exception("Date not found: " + date);
        }

        if (index < 100 || index + 5 >= data.size()) {
            throw new Exception("Not enough data around index");
        }

        FeatureSet features = buildFeatures(data, index);

// =========================
// 🌍 GLOBAL FEATURES (ADD THIS BLOCK)
// =========================
        Map<String, Double> market = MarketDataEngine.getMarketFeatures(date);

        if (market == null) {
            throw new Exception("Not enough market data");
        }

        features.spyAboveMA50 = market.get("spyAboveMA50");
        features.spyReturn5d  = market.get("spyReturn5d");
        features.vixLevel     = market.get("vixLevel");
        features.vixReturn5d  = market.get("vixReturn5d");
        features.vixRegime    = market.get("vixRegime");

// =========================
// 🎯 LABEL (UNCHANGED)
// =========================
        double label =
                (data.get(index + 5).close - data.get(index).close)
                        / data.get(index).close;

        return new DataPoint(features, label);
    }
}