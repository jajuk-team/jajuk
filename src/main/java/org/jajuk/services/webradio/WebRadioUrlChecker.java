/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package org.jajuk.services.webradio;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to check the validity of webradio URLs from the preset XML file.
 * <p>
 * This tool downloads the preset_radios.xml file from GitHub, parses all radio
 * entries, and tests each URL's connectivity. It outputs detailed status information
 * including redirects, timeouts, and errors.
 * </p>
 * <p>
 * Can be run as a standalone main method or integrated into the Jajuk application.
 * </p>
 *
 * @since 2.0
 */
public class WebRadioUrlChecker {

  /** The XML URL source from GitHub. */
  private static final String XML_SOURCE_URL = "https://raw.githubusercontent.com/jajuk-team/resources/master/preset_radios.xml";

  /** Default connection timeout in milliseconds. */
  private static final int CONNECTION_TIMEOUT_MS = 10000;

  /** Maximum number of concurrent connections. */
  private static final int MAX_CONCURRENT_CHECKS = 5;

  /** Regex pattern to extract name tag from XML line. */
  private static final Pattern NAME_PATTERN = Pattern.compile("<name>([^<]+)</name>");

  /** Regex pattern to extract url tag from XML line. */
  private static final Pattern URL_PATTERN = Pattern.compile("<url>([^<]+)</url>");

  /** Regex pattern to extract label tag from XML line. */
  private static final Pattern LABEL_PATTERN = Pattern.compile("<label>([^<]+)</label>");

  /** Regex pattern to extract isRemoved tag from XML line. */
  private static final Pattern REMOVED_PATTERN = Pattern.compile("<isRemoved>([^<]+)</isRemoved>");

  /** HttpClient for making HTTP requests. */
  private final HttpClient httpClient;

  /** Executor service for parallel URL checking. */
  private final ExecutorService executor;

  /** Counter for checked URLs. */
  private int checkedCount = 0;

  /** Counter for successful URLs. */
  private int successCount = 0;

  /** Counter for redirected URLs. */
  private int redirectCount = 0;

  /** Counter for failed URLs. */
  private int failedCount = 0;

  /** Counter for removed URLs. */
  private int removedCount = 0;

  /**
   * Instantiates a new WebRadioUrlChecker with default settings.
   */
  public WebRadioUrlChecker() {
    this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(CONNECTION_TIMEOUT_MS))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    this.executor = Executors.newFixedThreadPool(MAX_CONCURRENT_CHECKS);
  }

  /**
   * Main entry point for standalone execution.
   * <p>
   * Usage: java org.jajuk.services.webradio.WebRadioUrlChecker
   * </p>
   *
   * @param args command line arguments (unused)
   * @throws Exception if an error occurs during execution
   */
  public static void main(String[] args) throws Exception {
    System.out.println("=========================================");
    System.out.println("  Jajuk WebRadio URL Checker Tool");
    System.out.println("=========================================\n");

    WebRadioUrlChecker checker = new WebRadioUrlChecker();
    checker.run();
    checker.shutdown();
  }

  /**
   * Runs the complete URL checking process.
   * <p>
   * Downloads the XML file, parses radio entries, and tests each URL.
   * </p>
   *
   * @throws IOException if the XML file cannot be downloaded
   */
  public void run() throws IOException {
    System.out.println("[STEP 1] Downloading preset_radios.xml from GitHub...");
    String xmlContent = downloadXmlFile();

    if (xmlContent == null || xmlContent.isEmpty()) {
      System.err.println("ERROR: Failed to download XML file or content is empty");
      return;
    }

    System.out.println("[STEP 2] Parsing XML content...\n");
    List<WebRadioEntry> radios = parseXmlContent(xmlContent);
    System.out.println("Found " + radios.size() + " radio entries.\n");

    System.out.println("[STEP 3] Testing URLs...\n");
    System.out.println(formatHeader());

    List<CompletableFuture<Void>> futures = new ArrayList<>();
    for (WebRadioEntry radio : radios) {
      CompletableFuture<Void> future = CompletableFuture.runAsync(() -> checkUrl(radio), executor);
      futures.add(future);
    }

    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

    System.out.println("\n" + formatFooter());
  }

  /**
   * Downloads the XML file from the remote source.
   *
   * @return the XML content as a string, or null if download fails
   * @throws IOException if an I/O error occurs
   */
  private String downloadXmlFile() throws IOException {
    try {
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(XML_SOURCE_URL))
              .timeout(Duration.ofSeconds(30))
              .header("User-Agent", "Jajuk/WebRadioUrlChecker")
              .GET()
              .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != 200) {
        System.err.println("ERROR: HTTP " + response.statusCode() + " when downloading XML");
        return null;
      }

      return response.body();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Download interrupted", e);
    }
  }

  /**
   * Parses the XML content and extracts radio entries.
   *
   * @param xmlContent the XML string to parse
   * @return list of parsed WebRadioEntry objects
   */
  private List<WebRadioEntry> parseXmlContent(String xmlContent) {
    List<WebRadioEntry> entries = new ArrayList<>();
    String[] lines = xmlContent.split("\n");

    WebRadioEntry currentEntry = null;

    for (String line : lines) {
      line = line.trim();

      if (line.startsWith("<Radio>")) {
        currentEntry = new WebRadioEntry();
      } else if (line.startsWith("</Radio>") && currentEntry != null) {
        entries.add(currentEntry);
        currentEntry = null;
      } else if (currentEntry != null) {
        Matcher nameMatcher = NAME_PATTERN.matcher(line);
        if (nameMatcher.find()) {
          currentEntry.setName(nameMatcher.group(1).trim());
          continue;
        }

        Matcher urlMatcher = URL_PATTERN.matcher(line);
        if (urlMatcher.find()) {
          currentEntry.setUrl(urlMatcher.group(1).trim());
          continue;
        }

        Matcher labelMatcher = LABEL_PATTERN.matcher(line);
        if (labelMatcher.find()) {
          currentEntry.setLabel(labelMatcher.group(1).trim());
          continue;
        }

        Matcher removedMatcher = REMOVED_PATTERN.matcher(line);
        if (removedMatcher.find()) {
          currentEntry.setRemoved(Boolean.parseBoolean(removedMatcher.group(1).trim()));
          continue;
        }
      }
    }

    return entries;
  }

  /**
   * Tests a single radio URL and outputs the result.
   *
   * @param entry the radio entry to check
   */
  private void checkUrl(WebRadioEntry entry) {
    checkedCount++;

    String status;
    String finalUrl;
    String errorMsg = null;

    if (entry.isRemoved()) {
      removedCount++;
      status = "REMOVED";
      System.out.println(formatRow(entry, status, null, errorMsg));
      return;
    }

    if (entry.getUrl() == null || entry.getUrl().trim().isEmpty()) {
      failedCount++;
      status = "NO_URL";
      System.out.println(formatRow(entry, status, null, "No URL defined"));
      return;
    }

    // Skip non-HTTP protocols (MMS, RTSP, etc.)
    if (!entry.getUrl().matches("https?://.*")) {
      failedCount++;
      status = "NON_HTTP";
      System.out.println(formatRow(entry, status, null, "Protocol not supported: " +
              entry.getUrl().substring(0, Math.min(entry.getUrl().indexOf(':'), 10))));
      return;
    }

    try {
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(entry.getUrl()))
              .timeout(Duration.ofMillis(CONNECTION_TIMEOUT_MS))
              .header("User-Agent", "Jajuk/WebRadioUrlChecker/1.0")
              .header("Accept", "audio/*, */*")
              .method("HEAD", HttpRequest.BodyPublishers.noBody())
              .build();

      HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());

      finalUrl = response.uri().toString();

      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        successCount++;

        if (!finalUrl.equals(entry.getUrl())) {
          redirectCount++;
          status = "REDIRECT (" + response.statusCode() + ")";
          System.out.println(formatRow(entry, status, finalUrl, null));
        } else {
          status = "OK (" + response.statusCode() + ")";
          System.out.println(formatRow(entry, status, null, null));
        }
      } else {
        failedCount++;
        status = "ERROR (" + response.statusCode() + ")";
        System.out.println(formatRow(entry, status, null, "HTTP " + response.statusCode()));
      }

    } catch (java.net.http.HttpConnectTimeoutException e) {
      failedCount++;
      status = "TIMEOUT";
      System.out.println(formatRow(entry, status, null, "Connection timed out"));
    } catch (java.net.http.HttpTimeoutException e) {
      failedCount++;
      status = "TIMEOUT";
      System.out.println(formatRow(entry, status, null, "Request timed out"));
    } catch (IOException e) {
      failedCount++;
      status = "IO_ERROR";
      System.out.println(formatRow(entry, status, null, e.getMessage()));
    } catch (Exception e) {
      failedCount++;
      status = "ERROR";
      System.out.println(formatRow(entry, status, null, e.getClass().getSimpleName()));
    }
  }

  /**
   * Formats a row for console output.
   *
   * @param entry the radio entry
   * @param status the connection status
   * @param finalUrl the final URL after redirects (can be null)
   * @param errorMsg any error message (can be null)
   * @return formatted row string
   */
  private String formatRow(WebRadioEntry entry, String status, String finalUrl, String errorMsg) {
    StringBuilder sb = new StringBuilder();

    sb.append(String.format("%-3d | ", checkedCount));
    sb.append(String.format("%-30s | ", truncate(entry.getName(), 30)));
    sb.append(String.format("%-15s | ", truncate(entry.getLabel(), 15)));
    sb.append(String.format("%-12s | ", status));

    if (errorMsg != null) {
      sb.append(String.format("ERROR: %s", errorMsg));
    } else if (finalUrl != null) {
      sb.append(String.format("→ %s", truncate(finalUrl, 40)));
    } else {
      sb.append(truncate(entry.getUrl(), 40));
    }

    return sb.toString();
  }

  /**
   * Formats the table header.
   *
   * @return formatted header string
   */
  private String formatHeader() {
    return "#    | Name                         | Label           | Status       | URL/Target/Info";
  }

  /**
   * Formats the table footer with summary statistics.
   *
   * @return formatted footer string
   */
  private String formatFooter() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n=========================================\n");
    sb.append("SUMMARY\n");
    sb.append("=========================================\n");
    sb.append("Total checked: ").append(checkedCount).append("\n");
    sb.append("  ├─ Success:      ").append(successCount).append("\n");
    sb.append("  ├─ Redirected:   ").append(redirectCount).append("\n");
    sb.append("  ├─ Removed:      ").append(removedCount).append("\n");
    sb.append("  └─ Failed:       ").append(failedCount).append("\n");
    sb.append("-----------------------------------------\n");

    double successRate = checkedCount > 0 ?
            (successCount * 100.0 / checkedCount) : 0.0;
    sb.append(String.format("Success rate: %.1f%%\n", successRate));

    return sb.toString();
  }

  /**
   * Truncates a string to maximum length, adding ellipsis if needed.
   *
   * @param s the string to truncate
   * @param maxLength the maximum length
   * @return truncated string
   */
  private String truncate(String s, int maxLength) {
    if (s == null) {
      return "";
    }
    if (s.length() <= maxLength) {
      return s;
    }
    return s.substring(0, maxLength - 3) + "...";
  }

  /**
   * Shuts down the executor service gracefully.
   */
  public void shutdown() {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Inner class representing a web radio entry from the XML file.
   */
  private static class WebRadioEntry {
    private String name;
    private String url;
    private String label;
    private boolean removed;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public boolean isRemoved() {
      return removed;
    }

    public void setRemoved(boolean removed) {
      this.removed = removed;
    }
  }
}