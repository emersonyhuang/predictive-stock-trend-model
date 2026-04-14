package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;

public class StockScreener {
    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        Scanner scanner = new Scanner(System.in);

        // Name of the file where data will be saved
        String csvFileName = "scraped_stocks.csv";

        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFileName, true))) {
            // Write CSV Header if file is new (optional)
            // writer.println("Date,Symbol");

            driver.get("https://www.stockfetcher.com/create?edit=1");

            System.out.println("1. Log in and load your stocks in the browser.");
            System.out.println("2. Press ENTER in this console to start saving to CSV...");
            scanner.nextLine();

            int daysToGoBack = 1000;

            for (int i = 0; i < daysToGoBack; i++) {
                // Wait for the date element
                WebElement dateElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#result-info b")));
                String currentDate = dateElement.getText();

                // Clean the date string (remove commas to avoid breaking CSV format)
                String cleanDate = currentDate.replace(",", "");

                System.out.println("Scraping: " + cleanDate);

                // Find all stock rows
                List<WebElement> rows = driver.findElements(By.cssSelector("#filter-results tbody tr[sym]"));

                for (WebElement row : rows) {
                    String symbol = row.getAttribute("sym");

                    // SAVE TO FILE: Format is "Date,Symbol"
                    writer.println(cleanDate + "," + symbol);
                }

                // Flush ensures data is written to the disk immediately
                writer.flush();

                // Click 'Back' arrow
                WebElement backButton = driver.findElement(By.id("btn-past"));
                backButton.click();

                // Wait for date to change
                wait.until(d -> !d.findElement(By.cssSelector("#result-info b")).getText().equals(currentDate));
                Thread.sleep(1500);
            }

            System.out.println("Done! Data saved to " + csvFileName);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            driver.quit();
        }
    }
}