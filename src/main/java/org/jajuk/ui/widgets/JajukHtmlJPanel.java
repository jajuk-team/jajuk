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
package org.jajuk.ui.widgets;

import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.net.URL;

/**
 * Lightweight Html viewer using built-in JEditorPane (no external dependencies).
 * Replaces Cobra HtmlPanel with native Swing HTML renderer.
 */
public class JajukHtmlJPanel extends JPanel {

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** HTML editor kit with CSS styling. */
  private final HTMLEditorKit editorKit;

  /** The HTML pane for rendering. */
  private final JEditorPane htmlPane;

  /** The scroll pane wrapper. */
  private final JScrollPane scrollPane;

  /**
   * Instantiates a new jajuk html panel.
   * Initializes JEditorPane with custom styling and hyperlink handling.
   */
  public JajukHtmlJPanel() {
    super(new BorderLayout());

    // Create styled editor kit
    editorKit = createStyledEditorKit();

    // Create HTML pane
    htmlPane = new JEditorPane("text/html", "");
    htmlPane.setEditable(false);
    htmlPane.setEditorKit(editorKit);
    htmlPane.setContentType("text/html");
    htmlPane.setBackground(Color.WHITE);
    htmlPane.setCaretPosition(0);

    // Wrap in scroll pane
    scrollPane = new JScrollPane(htmlPane);
    scrollPane.setBorder(null);
    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

    // Add components
    add(scrollPane, BorderLayout.CENTER);
  }

  /**
   * Creates the styled editor kit with custom CSS for Wikipedia rendering.
   * Mimics modern Wikipedia appearance.
   *
   * @return the configured HTMLEditorKit
   */
  private HTMLEditorKit createStyledEditorKit() {
    HTMLEditorKit kit = new HTMLEditorKit();

    // Add basic CSS styling
    StyleSheet stylesheet = kit.getStyleSheet();
    stylesheet.addRule(
            "body { font-family: sans-serif; font-size: 14px; line-height: 1.6; color: #202122; margin: 8px; }" +
                    "h1, h2, h3, h4, h5, h6 { font-weight: bold; margin: 0.5em 0; border-bottom: 1px solid #a2a9b1; }" +
                    "h1 { font-size: 188%; border-bottom: 1px solid #a2a9b1; }" +
                    "h2 { font-size: 150%; }" +
                    "h3 { font-size: 128%; border-bottom: none; }" +
                    "a { color: #3366cc; text-decoration: none; }" +
                    "a:hover { text-decoration: underline; }" +
                    "p { margin: 0.5em 0; }" +
                    "ul, ol { margin: 0.3em 0; padding-left: 2em; }" +
                    "li { margin: 0.15em 0; }" +
                    "table { border-collapse: collapse; margin: 0.5em 0; }" +
                    "td, th { border: 1px solid #a2a9b1; padding: 0.2em; }" +
                    ".infobox { border: 1px solid #a2a9b1; background: #f8f9fa; float: right; margin: 0 0 1em 1em; padding: 0.2em; width: 300px; }" +
                    ".reference { font-size: 0.8em; }"
    );

    return kit;
  }

  /**
   * Displays the processed HTML content in the pane.
   * WITH ROBUST ERROR HANDLING FOR SWING TEXT LAYOUT BUGS
   *
   * @param html the HTML content to display
   * @param url  the URL being displayed
   */
  private void displayHtml(String html, String url) {
    SwingUtilities.invokeLater(() -> {
      try {
        // CRITICAL: Validate and sanitize HTML before setText()
        if (html == null || html.trim().isEmpty()) {
          Log.warn("Empty HTML for: " + url);
          htmlPane.setText("<html><body>No content</body></html>");
          return;
        }

        // Sanitize: remove control characters that break TextMeasurer
        String sanitized = html.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]", "");

        // Safety limit: prevent extremely large HTML from crashing EDT
        if (sanitized.length() > 500000) {  // 500KB limit
          Log.warn("HTML too large (" + sanitized.length() + "), truncating for: " + url);
          sanitized = sanitized.substring(0, 500000);
        }

        // Final validation
        if (sanitized.trim().isEmpty()) {
          Log.warn("Sanitized HTML empty for: " + url);
          htmlPane.setText("<html><body>No content</body></html>");
          return;
        }

        htmlPane.setText(sanitized);
        htmlPane.setCaretPosition(0);
        Log.debug("Wikipedia page displayed: " + url);

      } catch (ArrayIndexOutOfBoundsException e) {
        // Known Swing bug with malformed HTML
        Log.error("HTML layout crashed (index out of bounds): " + url, e);
        htmlPane.setText("<html><body><p style='color:red'>Failed to render content</p></body></html>");

      } catch (IllegalArgumentException e) {
        // Another variant of text layout corruption
        Log.error("HTML layout crashed (offset error): " + url, e);
        htmlPane.setText("<html><body><p style='color:red'>Failed to render content</p></body></html>");

      } catch (Exception e) {
        Log.error("Failed to display HTML", e);
        htmlPane.setText("<html><body><p style='color:red'>Error: " + e.getMessage() + "</p></body></html>");
      }
    });
  }

  /**
   * Display a "nothing found" page.
   * Shows a friendly message when no Wikipedia article exists.
   *
   */
  public void setUnknown() {
    String sPage = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
            "<style>body { font-family: sans-serif; margin: 40px; color: #666; }</style></head>" +
            "<body><h1>" + Messages.getString("WikipediaView.10") + "</h1>" +
            "<p>No Wikipedia article found for this artist.</p></body></html>";

    displayHtml(sPage, "about:blank");
  }

  /**
   * Sets the loading state.
   *
   * @param url the URL being loaded
   */
  public void setLoading(final URL url) {
    String sPage = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
            "<style>body { font-family: sans-serif; margin: 40px; }</style></head>" +
            "<body><h1>" + Messages.getString("WikipediaView.8") + " " + url + "</h1>" +
            "<div style='margin-top: 20px; text-align: center;'>" +
            "<svg width='50' height='50' viewBox='0 0 50 50'>" +
            "<circle cx='25' cy='25' r='20' stroke='#3366cc' stroke-width='4' fill='none' stroke-dasharray='80' stroke-dashoffset='20'>" +
            "</circle></svg></div></body></html>";

    displayHtml(sPage, "about:blank");
  }

  /**
   * Displays raw HTML content directly in the panel.
   * Use this method instead of setURL() when you already have HTML content.
   *
   * @param html the HTML content to display
   */
  public void setText(String html) {
    if (html == null || html.isEmpty()) {
      clearDocument();
      return;
    }

    SwingUtilities.invokeLater(() -> {
      try {
        // Wrap in basic HTML structure if needed
        String processedHtml = html;
        if (!html.trim().startsWith("<!DOCTYPE") && !html.trim().startsWith("<html")) {
          processedHtml = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body>"
                  + html + "</body></html>";
        }

        htmlPane.setText(processedHtml);
        htmlPane.setCaretPosition(0);
        Log.debug("HTML content displayed");
      } catch (Exception e) {
        Log.error("Failed to set HTML text", e);
      }
    });
  }

  /**
   * Sets the failed to load message.
   *
   * @param msg the error message to display
   */
  public void setFailedToLoad(String msg) {
    String sPage = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
            "<style>body { font-family: sans-serif; margin: 40px; color: #d33; }</style></head>" +
            "<body><h1>" + Messages.getString("WikipediaView.9") + "</h1><br>" +
            "<p style='color: #666;'>Details: " + msg + "</p></body></html>";

    displayHtml(sPage, "about:blank");
  }

  /**
   * Clears the current content of the HTML panel.
   * Use this to empty the page without loading another URL.
   */
  public void clearDocument() {
    SwingUtilities.invokeLater(() -> {
      htmlPane.setToolTipText("");
      htmlPane.setText("");
      htmlPane.setCaretPosition(0);
      Log.debug("HTML panel cleared");
    });
  }

}