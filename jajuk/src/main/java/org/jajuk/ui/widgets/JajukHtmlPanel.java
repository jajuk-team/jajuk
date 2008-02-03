/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $Revision$
 */

package org.jajuk.ui.widgets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jajuk.util.DownloadManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.w3c.dom.Document;
import org.xamjwg.html.HtmlParserContext;
import org.xamjwg.html.gui.HtmlPanel;
import org.xamjwg.html.parser.DocumentBuilderImpl;
import org.xamjwg.html.parser.InputSourceImpl;
import org.xamjwg.html.test.SimpleHtmlParserContext;
import org.xamjwg.html.test.SimpleHtmlRendererContext;

/**
 * Type description
 */
public class JajukHtmlPanel extends HtmlPanel implements ITechnicalStrings {

  private static final long serialVersionUID = -4033441908072591661L;

  private SimpleHtmlRendererContext rcontext;

  private HtmlParserContext context;

  private DocumentBuilderImpl dbi;

  /**
   * A HTML renderer based on Cobra
   */
  public JajukHtmlPanel() {
    super();
    // Disable Cobra traces
    Logger.getLogger("").setLevel(Level.OFF);
    rcontext = new SimpleHtmlRendererContext(this);
    context = new SimpleHtmlParserContext();
    dbi = new DocumentBuilderImpl(context, rcontext);
  }

  /**
   * Display a wikipedia url
   */
  public void setURL(URL url) throws Exception {
    setCursor(Util.WAIT_CURSOR);
    File page = new File(Util.getConfFileByPath(FILE_CACHE).getAbsolutePath() + '/'
        + Util.getOnlyFile(url.toString() + ".html"));
    String sPage = DownloadManager.downloadHtml(url);
    // Remove scripting
    int index = -1;
    StringBuilder sb = new StringBuilder(sPage);
    do {
      index = sb.indexOf("<script");
      if (index > 0) {
        sb.delete(index, sb.indexOf("</script>") + 9);
      }
    } while (index > 0);
    sPage = sb.toString();
    // cleanup useless stuff
    sPage = sPage.replaceAll("img src=\"/", "img src=\"http://www.mediawiki.org/");
    sPage = sPage.replaceAll("href=\"/", "href=\"http://www.mediawiki.org/");
    sPage = sPage.replaceAll("<link.*/>", "");
    sPage = sPage.replaceAll("@import.*;", "");
    // Display the page
    showPage(sPage, page);
    // Set current url as a tooltip
    setToolTipText(url.toString());
    // Disable waiting cursor
    setCursor(Util.DEFAULT_CURSOR);
  }

  /**
   * Display a "nothing found" page
   * 
   * @throws Exception
   */
  public void setUnknow() throws Exception {
    File page = new File(Util.getConfFileByPath(FILE_CACHE).getAbsolutePath() + '/'
        + "noresult.html");
    String sPage = "<html><body><h1>" + Messages.getString("WikipediaView.3")
        + "</h1></body></html>";
    showPage(sPage, page);
  }

  /**
   * Make the internal operations
   * 
   * @param sPage
   * @param page
   * @throws IOException
   */
  private void showPage(String sPage, File page) throws Exception {
    // Write the page itself
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(page),
        "UTF-8"));
    bw.write(sPage);
    bw.flush();
    bw.close();
    // A Reader should be created with the correct charset,
    // which may be obtained from the Content-Type header
    // of an HTTP response.
    Reader reader = new InputStreamReader(new FileInputStream(page), "UTF-8");
    // InputSourceImpl constructor with URI recommended
    // so the renderer can resolve page component URLs.
    InputSourceImpl is = new InputSourceImpl(reader, "file://" + page.getAbsolutePath());
    // A documentURI should be provided to resolve relative
    // URIs.
    Document document = dbi.parse(is);
    // Now set document in panel. This is what causes the
    // document to render.
    setDocument(document, rcontext);
  }

  public void back() {
    rcontext.back();
  }

}
