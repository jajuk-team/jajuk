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

package org.jajuk.services.webradio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult;
import org.jajuk.base.WebRadio;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Stores webradios configurated by user
 * <p>
 * Singleton
 * </p>
 */
public class WebRadioManager extends DefaultHandler implements ITechnicalStrings {

  private static TreeSet<WebRadio> webradios = new TreeSet<WebRadio>();

  // Self instance
  private static WebRadioManager self;

  File fwebradios;

  private WebRadioManager() {
    // check for webradio repository file
    fwebradios = Util.getConfFileByPath(FILE_WEB_RADIOS_REPOS);
    if (!fwebradios.exists()) {
      // download the stream list and load it asynchonously to avoid
      // freezing unconnected people
      new Thread() {
        public void run() {
          downloadRepository();
        }
      }.start();
    }
    // Load repository if it exists
    else {
      loadRepository();
    }

  }

  private void loadRepository() {
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(false);
      spf.setNamespaceAware(false);
      SAXParser saxParser = spf.newSAXParser();
      saxParser.parse(fwebradios.toURI().toURL().toString(), this);
    } catch (Exception e) {
      Log.error(e);
      // Remove file if it is corrupted so it will be downloaded again
      // next time
      fwebradios.delete();
    }
  }

  /**
   * Download asynchronously the default streams list
   * 
   * @return the downnlod thread
   */
  private void downloadRepository() {
    // try to download the default directory (from jajuk SVN trunk
    // directly)
    try {
      DownloadManager.download(new URL(URL_DEFAULT_WEBRADIOS_1), fwebradios);
    } catch (Exception e) {
      Log.error(e);
      // Fail ? try the file from jajuk.info (older)
      Log.debug("Try download default radio stations from jajuk.info");
      try {
        DownloadManager.download(new URL(URL_DEFAULT_WEBRADIOS_2), fwebradios);
      } catch (Exception e2) {
        Log.error(e2);
      }
    }
    // Load repository if any
    if (fwebradios.exists()) {
      loadRepository();
    }

  }

  public static WebRadioManager getInstance() {
    if (self == null) {
      self = new WebRadioManager();
    }
    return self;
  }

  public void addWebRadio(WebRadio radio) {
    webradios.add(radio);
  }

  public void removeWebRadio(WebRadio radio) {
    webradios.remove(radio);
  }

  /**
   * Write current repository for persistence between sessions
   */
  public void commit() throws IOException {
    // If none radio recorded, do not commit to allow next session
    // to download the default covers again
    if (webradios.size() == 0) {
      return;
    }
    File out = Util.getConfFileByPath(FILE_WEB_RADIOS_REPOS);
    String sCharset = ConfigurationManager.getProperty(CONF_COLLECTION_CHARSET);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out),
        sCharset), 1000000);
    bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n");
    bw.write("<" + XML_STREAMS + " " + XML_VERSION + "='" + JAJUK_VERSION + "'>\n");
    // Manage each stream
    for (WebRadio radio : webradios) {
      bw.write("\t<" + XML_STREAM + " " + XML_NAME + "='" + radio.getName() + "' " + XML_URL + "='"
          + radio.getUrl() + "'/>\n");
    }
    // close
    bw.write("</" + XML_STREAMS + ">\n");
    bw.flush();
    bw.close();
  }

  /**
   * Copy the default radio file to the current repository file
   */
  public void restore() throws Exception {
    // Clear existing radios
    webradios.clear();
    // Download repository
    downloadRepository();
    // Check file now exists and not void
    File out = Util.getConfFileByPath(FILE_WEB_RADIOS_REPOS);
    if (!out.exists() || out.length() == 0) {
      // show an "operation failed' message to users
      throw new Exception("Cannot download webradio repository");
    }
  }

  /**
   * Called when we start an element
   */
  public void startElement(String sUri, String s, String sQName, Attributes attributes)
      throws SAXException {
    try {
      if (XML_STREAM.equals(sQName)) {
        String name = attributes.getValue(attributes.getIndex(XML_NAME));
        String url = attributes.getValue(attributes.getIndex(XML_URL));
        WebRadio radio = new WebRadio(name, url);
        webradios.add(radio);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Perform a search in all files names with given criteria
   * 
   * @param sCriteria
   * @return
   */
  public TreeSet<SearchResult> search(String sCriteria) {
    synchronized (FileManager.getInstance().getLock()) {
      TreeSet<SearchResult> tsResu = new TreeSet<SearchResult>();
      for (WebRadio radio : webradios) {
        if (radio.getName().toLowerCase().indexOf(sCriteria.toLowerCase()) != -1) {
          tsResu.add(new SearchResult(radio, radio.toString()));
        }
      }
      return tsResu;
    }
  }

  /**
   * 
   * @return All webradios filled (copy)
   */
  @SuppressWarnings("unchecked")
  public TreeSet<WebRadio> getWebRadios() {
    return (TreeSet<WebRadio>) webradios.clone();
  }

  /**
   * 
   * @param name
   * @return WebRadio for a given name or null if no match
   */
  public WebRadio getWebRadioByName(String name) {
    for (WebRadio radio : webradios) {
      if (radio.getName().equals(name)) {
        return radio;
      }
    }
    return null;
  }

  /**
   * 
   * @param name
   * @return WebRadio for a given url (first match) or null if no match
   */
  public WebRadio getWebRadioByURL(String url) {
    for (WebRadio radio : webradios) {
      if (radio.getUrl().equals(url)) {
        return radio;
      }
    }
    return null;
  }

  /**
   * 
   * @return Current webradio tooltip text
   */
  public static String getCurrentWebRadioTooltip() {
    String tooltipWebRadio = Messages.getString("CommandJPanel.25");
    String defaultRadio = ConfigurationManager.getProperty(CONF_DEFAULT_WEB_RADIO);
    if (WebRadioManager.getInstance().getWebRadioByName(defaultRadio) != null) {
      tooltipWebRadio = "<html>" + tooltipWebRadio + "<p><b>" + defaultRadio + "</b></html>";
    }
    return tooltipWebRadio;
  }

}
