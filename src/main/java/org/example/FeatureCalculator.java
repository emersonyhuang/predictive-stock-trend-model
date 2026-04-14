package org.example;

import java.util.*;

public class FeatureCalculator {

    public static double movingAverage(List<StockData> data, int end, int period) {
        double sum = 0;
        for (int i = end - period + 1; i <= end; i++) {
            sum += data.get(i).close;
        }
        return sum / period;
    }

    public static double computeReturn(List<StockData> data, int i, int pastDays) {
        return (data.get(i).close - data.get(i - pastDays).close)
                / data.get(i - pastDays).close;
    }

    public static double volatility(List<StockData> data, int end, int period) {
        double mean = 0;
        for (int i = end - period + 1; i <= end; i++) {
            mean += data.get(i).close;
        }
        mean /= period;

        double var = 0;
        for (int i = end - period + 1; i <= end; i++) {
            double diff = data.get(i).close - mean;
            var += diff * diff;
        }

        return Math.sqrt(var / period);
    }

    public static double maxDrawdown(List<StockData> data, int end, int period) {
        double peak = data.get(end - period).close;
        double maxDD = 0;

        for (int i = end - period; i <= end; i++) {
            if (data.get(i).close > peak) peak = data.get(i).close;

            double dd = (peak - data.get(i).close) / peak;
            if (dd > maxDD) maxDD = dd;
        }

        return maxDD;
    }

    public static double returnOver(List<StockData> data, int i, int period) {
        return (data.get(i).close - data.get(i - period).close)
                / data.get(i - period).close;
    }

    public static double distanceFromMA(double price, double ma) {
        return (price - ma) / ma;
    }

    public static double avgVolume(List<StockData> data, int end, int period) {
        double sum = 0;
        for (int i = end - period + 1; i <= end; i++) {
            sum += data.get(i).volume;
        }
        return sum / period;
    }

    public static double volumeSpike(List<StockData> data, int end) {
        double current = data.get(end).volume;
        double avg = avgVolume(data, end, 5);
        return current / avg;
    }

    public static double rsi(List<StockData> data, int index, int period) {
        if (index < period) return 50;

        double gain = 0;
        double loss = 0;

        for (int i = index - period + 1; i <= index; i++) {
            double change = data.get(i).close - data.get(i - 1).close;

            if (change > 0) gain += change;
            else loss -= change;
        }

        if (loss == 0) return 100;

        double rs = gain / loss;
        return 100 - (100 / (1 + rs));
    }

    public static double pctAboveMA50(List<StockData> data, int end, int period) {
        int count = 0;

        for (int i = end - period + 1; i <= end; i++) {
            double ma50 = movingAverage(data, i, 50);
            if (data.get(i).close > ma50) count++;
        }

        return (double) count / period;
    }


}