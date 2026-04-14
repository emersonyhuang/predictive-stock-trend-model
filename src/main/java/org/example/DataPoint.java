package org.example;

public class DataPoint {
    public FeatureSet features;
    public double label;

    public DataPoint(FeatureSet features, double label) {
        this.features = features;
        this.label = label;
    }
}