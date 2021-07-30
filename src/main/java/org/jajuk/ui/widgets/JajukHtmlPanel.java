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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Cobra browser HTML panel.
 */
public class JajukHtmlPanel extends HtmlPanel {
  /** The Constant COLON.   */
  private static final String COLON = " : ";
  /** The Constant URL_COLON.   */
  private static final String URL_COLON = "URL: ";
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -4033441908072591661L;
  private final SimpleHtmlRendererContext rcontext;
  private final DocumentBuilderImpl dbi;

  /**
   * A HTML renderer based on Cobra.
   */
  public JajukHtmlPanel() {
    super();
    // Disable Cobra traces
    Logger.getLogger("").setLevel(Level.OFF);
    UserAgentContext ucontext = new SimpleUserAgentContext();
    rcontext = new SimpleHtmlRendererContext(this, ucontext);
    dbi = new DocumentBuilderImpl(ucontext, rcontext);
  }

  /**
   * Display a wikipedia url given a cache file.
   * <p>Download the url to cache if not already in cache</p>
   */
  public void setURL(final URL url, final String lang) {
    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
      @Override
      public Void doInBackground() {
    	 File page = new File(SessionService.getConfFileByPath(Const.FILE_CACHE).getAbsolutePath()
    	            + '/' + UtilSystem.getOnlyFile(url.toString() + ".html"));
        File expectedCacheFile =  SessionService.getCachePath(url);
        try {
          setCursor(UtilGUI.WAIT_CURSOR);
          if (!expectedCacheFile.exists()) {
            DownloadManager.downloadToCache(url);
          }
          // first indicate that we are loading a new page
          setLoading(url);
          String sPage = DownloadManager.getTextFromCachedFile(url,"UTF-8");
          // Leave if no result
          if (sPage == null) {
            return null;
          }
          // Remove scripting
          StringBuilder sb = new StringBuilder(sPage);
          // only the part between <!-- start content --> and <!-- end content
          // --> is
          // important to us
          int index = sb.indexOf("<!-- start content -->");
          int lastindex = sb.indexOf("</body></html>");
          if (index > 0) {
            sb.delete(0, index);
            sb.delete(sb.indexOf("<!-- end content -->") + 20, lastindex);
          }
          sPage = sb.toString();
          // fix internal links
          sPage = sPage.replaceAll("href=\"/", "href=\"http://" + lang + ".wikipedia.org/");
          // Display the page
          showPage(sPage, page);
          // Set current url as a tooltip
          setToolTipText(url.toString());
        } catch (FileNotFoundException e) {
          // This happens whenever the Artist is not listed on Wikipedia, so we should report this more user-friendly
          Log.debug("Could not read page: {{" + url.toString() + " Cache: " + page + "}}");
          try {
            setFailedToLoad(Messages.getString("WikipediaView.9") + ": " + url.toString());
          } catch (IOException | SAXException e1) {
            Log.error(e1);
          }
        } catch (IOException e) {
          // report IOException only as warning here as we can expect this to
          // happen frequently with images on the net
          Log.warn("Could not read page: {{" + url.toString() + " Cache: " + page + "}}",
              e.getMessage());
          try {
            setFailedToLoad(URL_COLON + url + COLON + e.getClass().getSimpleName() + COLON
                + e.getMessage());
          } catch (IOException | SAXException e1) {
            Log.error(e1);
          }
        } catch (Exception e) {
          Log.error(e);
          try {
            setFailedToLoad(URL_COLON + url + COLON + e.getClass().getSimpleName() + COLON
                + e.getMessage());
          } catch (IOException | SAXException e1) {
            Log.error(e1);
          }
        } finally {
          // Disable waiting cursor
          setCursor(UtilGUI.DEFAULT_CURSOR);
        }
        return null;
      }
    };
    sw.execute();
  }

  /**
   * Display a "nothing found" page.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  public void setUnknown() throws IOException, SAXException {
    File page = new File(SessionService.getConfFileByPath(Const.FILE_CACHE).getAbsolutePath() + '/'
        + "noresult.html");
    String sPage = "<html><body><h1>" + Messages.getString("WikipediaView.10")
        + "</h1></body></html>";
    showPage(sPage, page);
  }

  /**
   * Sets the loading.
   *
   * @param url the new loading
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  private void setLoading(final URL url) throws IOException, SAXException {
    File page = new File(SessionService.getConfFileByPath(Const.FILE_CACHE).getAbsolutePath() + '/'
        + "loading.html");
    String sPage = "<html><body><h1>" + Messages.getString("WikipediaView.8") + " "
        + url.toString() + "</h1></body></html>";
    showPage(sPage, page);
  }

  /**
   * Sets the failed to load.
   *
   * @param msg the new failed to load
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  private void setFailedToLoad(String msg) throws IOException, SAXException {
    File page = new File(SessionService.getConfFileByPath(Const.FILE_CACHE).getAbsolutePath() + '/'
        + "failed.html");
    String sPage = "<html><body><h1>" + Messages.getString("WikipediaView.9") + "</h1><br>" + msg
        + "</body></html>";
    showPage(sPage, page);
  }

  /**
   * Make the internal operations.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws SAXException the SAX exception
   */
  private void showPage(String sPage, File page) throws IOException, SAXException {
    // Write the page itself
    try (Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(page), StandardCharsets.UTF_8))) {
      bw.write(sPage);
      bw.flush();
    }
    // A Reader should be created with the correct charset,
    // which may be obtained from the Content-Type header
    // of an HTTP response.
    try (Reader reader = new InputStreamReader(new FileInputStream(page), StandardCharsets.UTF_8)) {
      // InputSourceImpl constructor with URI recommended
      // so the renderer can resolve page component URLs.
      InputSource is = new InputSourceImpl(reader, "file://" + page.getAbsolutePath());
      // A documentURI should be provided to resolve relative
      // URIs.
      Document document = dbi.parse(is);
      // Now set document in panel. This is what causes the
      // document to render.
      setDocument(document, rcontext);
    }
  }

  /**
   * Back.
   *
   */
  public void back() {
    rcontext.back();
  }
}
