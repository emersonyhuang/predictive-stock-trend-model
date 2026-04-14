package org.example;

public class FeatureSet {

    // =========================
    // 🧩 SHORT TERM
    // =========================
    public double return1d;
    public double return5d;
    public double rsiChange5d;
    public double volumeSpike;

    // =========================
    // 🧩 MEDIUM TERM
    // =========================
    public double rsi14;
    public double ma20;
    public double ma50;
    public double distMA20;
    public double distMA50;
    public double ma20Slope;
    public double volatility20;

    // =========================
    // 🧩 LONG TERM
    // =========================
    public double return100d;
    public double maxDrawdown100d;
    public double pctAboveMA50;
    public double volatility100;


    public double vixRegime;
    public double spyAboveMA50;
    public double spyReturn5d;
    public double vixLevel;
    public double vixReturn5d;


    @Override
    public String toString() {
        return return1d + "," +
                return5d + "," +
                rsiChange5d + "," +
                volumeSpike + "," +

                rsi14 + "," +
                ma20 + "," +
                ma50 + "," +
                distMA20 + "," +
                distMA50 + "," +
                ma20Slope + "," +
                volatility20 + "," +

                return100d + "," +
                maxDrawdown100d + "," +
                pctAboveMA50 + "," +

                spyAboveMA50
                + "," + spyReturn5d
                + "," + vixLevel
                + "," + vixReturn5d
                + "," + vixRegime;
    }
}