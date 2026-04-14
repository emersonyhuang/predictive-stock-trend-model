package org.example;

import java.io.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class DatasetBuilder {

    public static LocalDate convertDate(String rawDate) {
        DateTimeFormatter inputFormat =
                DateTimeFormatter.ofPattern("EEEE MMMM d yyyy", Locale.ENGLISH);
        return LocalDate.parse(rawDate, inputFormat);
    }

    public static void buildDataset(String inputPath, String outputPath) throws Exception {

        int success = 0;
        int skipped = 0;
        int duplicatesSkipped = 0;

        // Tracks the most recent date we processed for each ticker
        Map<String, LocalDate> lastSeen = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputPath));
             BufferedWriter bw = new BufferedWriter(new FileWriter(outputPath))) {

            String line;

            while ((line = br.readLine()) != null) {
                try {
                    String[] parts = line.split(",");

                    if (parts.length < 2) {
                        skipped++;
                        continue;
                    }

                    LocalDate currentDate = convertDate(parts[0].trim());
                    String ticker = parts[1].trim();

                    LocalDate lastDate = lastSeen.get(ticker);

                    if (lastDate != null) {
                        // Math.abs handles cases where data is sorted newest-to-oldest
                        long daysBetween = Math.abs(ChronoUnit.DAYS.between(lastDate, currentDate));

                        if (daysBetween < 5) {
                            duplicatesSkipped++;
                            continue;
                        }
                    }

                    DataPoint dp = SignalProcessor.getDataPoint(
                            ticker,
                            currentDate.toString()
                    );

                    bw.write(dp.features.toString() + "," + dp.label);
                    bw.newLine();

                    // Update the map with the date of this successful entry
                    lastSeen.put(ticker, currentDate);
                    success++;

                    if (success % 100 == 0) {
                        System.out.println("Processed: " + success);
                    }

                } catch (Exception e) {
                    skipped++;
                    // Optional: print stack trace if you're getting unexpected errors
                    // e.printStackTrace();
                }
            }
        }

        System.out.println("\n=== DONE ===");
        System.out.println("Success: " + success);
        System.out.println("Skipped (errors): " + skipped);
        System.out.println("Skipped (<5 day spacing): " + duplicatesSkipped);
    }
}