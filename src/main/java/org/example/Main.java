package org.example;

public class Main {
    public static void main(String[] args) throws Exception {

        MarketDataEngine.init();

        String inputPath = "data/test.csv";
        String outputPath = "data/test_output.csv";

        DatasetBuilder.buildDataset(inputPath, outputPath);

    }
}
