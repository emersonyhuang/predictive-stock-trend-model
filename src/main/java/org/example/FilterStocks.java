package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * FilterStocks reads a CSV file, filters unique stock names, 
 * tracks the last date reached, and writes a summary to a NEW CSV file.
 */
public class FilterStocks {
    public static void main(String[] args) {
        String inputFileName = "data/scraped_stocks.csv";
        String outputFileName = "data/stock_summary.csv";

        Set<String> uniqueStocks = new HashSet<>();
        String lastDate = "N/A";

        // 1. READ FROM ORIGINAL FILE
        try (BufferedReader br = new BufferedReader(new FileReader(inputFileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    lastDate = parts[0].trim();
                    uniqueStocks.add(parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading input file: " + e.getMessage());
            return;
        }

        // 2. WRITE TO NEW FILE
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFileName))) {
            // Header Row: reached [date]
            bw.write("reached [" + lastDate + "]");
            bw.newLine();

            // Header Row: amount of stocks [count]
            bw.write("amount of stocks [" + uniqueStocks.size() + "]");
            bw.newLine();

            // Separator/Label
            bw.write("Stock List");
            bw.newLine();

            // Write each unique stock on a new line
            for (String stock : uniqueStocks) {
                bw.write(stock);
                bw.newLine();
            }

            System.out.println("Success! New file created: " + outputFileName);

        } catch (IOException e) {
            System.err.println("Error writing to new file: " + e.getMessage());
        }
    }
}