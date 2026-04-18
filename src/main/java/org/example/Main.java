package org.example;

public class Main {
    public static void main(String[] args) throws Exception {

        MarketDataEngine.init();

        String inputPath = "C:\\Users\\BC-Tech\\IdeaProjects\\Data\\test.csv";
        String outputPath = "C:\\Users\\BC-Tech\\IdeaProjects\\Data\\test_output.csv";

        DatasetBuilder.buildDataset(inputPath, outputPath);

    }
}