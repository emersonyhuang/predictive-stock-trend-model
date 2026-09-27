# Predictive Stock Trend Model

This started with a pretty basic question: what actually *is* an AI, and how do you train one? Turns out a model is only as good as the data you feed it — it needs clean, structured input before it can recognize any pattern. So before I could train anything, I had to build the pipeline that gets there. This repo is that pipeline: scraping historical prices, engineering features, and assembling a labeled dataset a model could actually read. The target was volatile stocks with breakout potential — could an AI catch the signal before the move happened?

## The problems I ran into

**Finding clean "point-in-time" data.** A normal historical chart hides what a stock actually looked like *before* it moved, which made it hard to filter out junk/noise tickers. After digging through developer forums and a specific Reddit community, I found an old, mostly-forgotten free tool that still worked, and wrote a custom filter on top of it (`StockScreener`) to pull stocks that matched specific health conditions at that exact moment in time.

**Manual logging was too slow.** Once I had criteria, checking candidates one by one was brutal. So I automated it — `StockScreener` drives a real browser session, clicks back through the site's date history, and logs every stock that passes as it goes.

**The 100-day API wall.** For features I needed ~10 years of price history per ticker. Every free API I tried, Alpha Vantage included, caps out around 100 days — useless if a stock's signal happened five years ago. NASDAQ's own site has the full history, just no API for it, so I wrote `NasdaqSmartDownloader` to drive Chrome directly: search the ticker, hit download, grab the CSV, repeat down the whole list.

**Feature engineering + market context.** `FeatureCalculator` builds the technical indicators (moving averages, RSI, volatility, drawdown), and `MarketDataEngine` layers in SPY/VIX context so the model sees broader market conditions, not just one stock in isolation. `DatasetBuilder` and `SignalProcessor` tie it together into labeled (features, 5-day-forward-return) rows.

## The pivot

I built the whole thing in Java first. Once it actually came time to train a model, I hit a wall — Java's ML ecosystem isn't close to Python's. So I pivoted, learned enough Python to get moving, and trained a neural network on the dataset this pipeline produces. Backtested on unseen data, the strategy came out to a consistent net loss — not the result I wanted, but the part of this project I actually learned the most from. It told me the features needed more work, not that the approach was worthless.

## Project structure

```
src/main/java/org/example/
  StockData.java             price bar (OHLCV) for one day
  CSVLoader.java              loads a ticker's raw price history
  MarketDataEngine.java       loads SPY/VIX history, exposes market-wide features
  FeatureCalculator.java      moving averages, RSI, volatility, drawdown, etc.
  FeatureSet.java              container for one row of engineered features
  SignalProcessor.java        builds a FeatureSet + label for one (ticker, date)
  DataPoint.java                a finished (features, label) training example
  DatasetBuilder.java          drives the pipeline end-to-end, writes the dataset CSV
  Main.java                        entry point
  StockScreener.java            point-in-time scraper: pulls a ticker watchlist
  NasdaqSmartDownloader.java  Selenium scraper: pulls per-ticker 10-year price history
  FilterStocks.java              dedupes the scraped ticker list

data/
  test.csv, test_output.csv       small sample run of the pipeline
  scraped_stocks.csv, stock_summary.csv   output of the scraping step
  dataset_output.csv                 a full labeled dataset produced by the pipeline
  raw/                                    per-ticker OHLCV downloads (not checked in — see below)
```

## Running it

Requires Java (compiler target is set to 25 in `pom.xml` — lower it if you're on an older JDK) and Maven.

```bash
mvn compile
mvn exec:java -Dexec.mainClass="org.example.Main"
```

The scrapers (`StockScreener`, `NasdaqSmartDownloader`) drive a real Chrome session via Selenium/WebDriverManager and expect a `skipped_stocks.csv` / ticker watchlist as input; run them separately to populate `data/raw/` before pointing `CSVLoader`/`MarketDataEngine` at real tickers.

## Note on the data

The per-ticker raw price histories (`data/raw/`) aren't checked in — hundreds of tickers x years of daily data adds up fast, and it's fully reproducible by re-running the scrapers. `data/dataset_output.csv` is a real labeled dataset from an actual run of the full pipeline, included so the output format is easy to inspect without re-scraping everything.

This repo is the Java data-engineering half of the project — the model-training pass happened afterward in Python, once the dataset above was built.
