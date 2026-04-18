package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class NasdaqSmartDownloader {

    public static void main(String[] args) {
        String csvInput = "skipped_stocks.csv";
        String downloadDir = "C:\\NasdaqDownloads\\";
        String masterFile = "C:\\NasdaqDownloads\\FINAL_COMBINED_DATA.csv";

        new File(downloadDir).mkdirs();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        // Anti-Bot: Set a real User-Agent
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.setExperimentalOption("useAutomationExtension", false);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", downloadDir);
        options.setExperimentalOption("prefs", prefs);

        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize(); // Act like a human user

        try (BufferedReader br = new BufferedReader(new FileReader(csvInput))) {
            String line;
            int lineCount = 0;

            while ((line = br.readLine()) != null) {
                lineCount++;
                if (lineCount <= 3 || line.trim().isEmpty()) continue;

                String ticker = line.trim();
                System.out.println("Targeting: " + ticker);

                // Navigate to the page
                driver.get("https://www.nasdaq.com/market-activity/stocks/" + ticker.toLowerCase() + "/historical?timeline=y5");

                // Logic to handle the "Doesn't load data" issue
                boolean success = attemptDownload(driver, ticker, downloadDir);

                if (!success) {
                    System.out.println("Data didn't load. Refreshing page for " + ticker + "...");
                    driver.navigate().refresh();
                    attemptDownload(driver, ticker, downloadDir);
                }

                // Random "human" pause between 3 to 7 seconds
                Thread.sleep(ThreadLocalRandom.current().nextInt(3000, 7000));
            }

            mergeFiles(downloadDir, masterFile);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }

    private static boolean attemptDownload(WebDriver driver, String ticker, String dir) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        try {
            // Wait for the button. If it doesn't appear, the data likely didn't load.
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.className("historical-download")));
            btn.click();
            waitForDownloadAndRename(dir, ticker);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void waitForDownloadAndRename(String dir, String ticker) throws InterruptedException {
        File folder = new File(dir);
        for (int i = 0; i < 15; i++) {
            File[] files = folder.listFiles((d, name) -> name.contains("HistoricalData") && name.endsWith(".csv"));
            if (files != null && files.length > 0) {
                File downloadedFile = files[0];
                File newFile = new File(dir + ticker + ".csv");
                downloadedFile.renameTo(newFile);
                System.out.println("Saved: " + ticker + ".csv");
                return;
            }
            Thread.sleep(1000);
        }
    }

    private static void mergeFiles(String dir, String output) throws IOException {
        Path path = Paths.get(dir);
        List<Path> csvFiles = Files.list(path)
                .filter(p -> p.toString().endsWith(".csv") && !p.getFileName().toString().startsWith("FINAL"))
                .toList();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            boolean headerAdded = false;
            for (Path file : csvFiles) {
                String tickerName = file.getFileName().toString().replace(".csv", "");
                List<String> lines = Files.readAllLines(file);
                if (lines.isEmpty()) continue;

                if (!headerAdded) {
                    writer.write("Ticker," + lines.get(0));
                    writer.newLine();
                    headerAdded = true;
                }
                for (int i = 1; i < lines.size(); i++) {
                    writer.write(tickerName + "," + lines.get(i));
                    writer.newLine();
                }
            }
        }
        System.out.println("Processing complete. Master file created.");
    }
}