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
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult;
import org.jajuk.util.Conf;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
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
public final class WebRadioManager extends DefaultHandler implements Const {

  private static Set<WebRadio> webradios = new TreeSet<WebRadio>();

  // Self instance
  private static WebRadioManager self;

  private StringBuilder buffer;

  private boolean inRadio;
  private String radioName;
  private String radioUrl;

  // Webradio file XML tags static strings
  private static final String XML_RADIO = "Radio";
  private static final String XML_URL = "url";
  private static final String XML_NAME = "name";

  File fwebradios;

  private WebRadioManager() {
    // check for webradio repository file
    fwebradios = UtilSystem.getConfFileByPath(FILE_WEB_RADIOS_REPOS);
    if (!fwebradios.exists()) {
      // download the stream list and load it asynchronously to avoid
      // freezing unconnected people
      new Thread() {
        @Override
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
      if(!fwebradios.delete()) {
        Log.warn("Could not delete file " + fwebradios.toString());
      }
    }
  }

  /**
   * Download asynchronously the default streams list
   * 
   * @return the download thread
   */
  private void downloadRepository() {
    // try to download the default directory (from jajuk SVN trunk
    // directly)
    try {
      DownloadManager.download(new URL(URL_DEFAULT_WEBRADIOS), UtilSystem
          .getConfFileByPath(FILE_WEB_RADIOS_REPOS));
    } catch (Exception e) {
      Log.error(e);
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
    File out = UtilSystem.getConfFileByPath(FILE_WEB_RADIOS_REPOS);
    String sCharset = Conf.getString(CONF_COLLECTION_CHARSET);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out),
        sCharset), 1000000);
    bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n");
    bw.write("<" + XML_STREAMS + " " + XML_VERSION + "='" + JAJUK_VERSION + "'>\n");
    // Manage each stream
    for (WebRadio radio : webradios) {
      bw.write("\t<" + XML_STREAM + " " + XML_NAME + "='" + UtilString.formatXML(radio.getName()) + "' "
          + XML_URL + "='" + UtilString.formatXML(radio.getUrl()) + "'/>\n");
    }
    // close
    bw.write("</" + XML_STREAMS + ">\n");
    bw.flush();
    bw.close();
  }

  /**
   * Copy the default radio file to the current repository file
   */
  public void restore() throws IOException {
    // Clear existing radios
    webradios.clear();
    // Download repository
    downloadRepository();
    // Check file now exists and not void
    File out = UtilSystem.getConfFileByPath(FILE_WEB_RADIOS_REPOS);
    if (!out.exists() || out.length() == 0) {
      // show an "operation failed' message to users
      throw new IOException("Cannot download or parse webradio repository");
    }
  }

  /**
   * Called when we start an element
   */
  @Override
  public void startElement(String sUri, String s, String sQName, Attributes attributes)
      throws SAXException {

    try {
      if (XML_RADIO.equals(sQName)) {
        inRadio = true;
      } else if (XML_STREAM.equals(sQName)) {
        String name = attributes.getValue(attributes.getIndex(XML_NAME));
        String url = attributes.getValue(attributes.getIndex(XML_URL));
        WebRadio radio = new WebRadio(name, url);
        webradios.add(radio);
      } else if (inRadio) {        
        if (XML_NAME.equals(sQName)) {
          buffer = new StringBuilder();
        } else if (XML_URL.equals(sQName)) {
          buffer = new StringBuilder();
        }
      }

    } catch (Exception e) {
      Log.error(e);
    }
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    String s  = new String(ch, start, length);
    if (buffer != null) { 
      buffer.append(s);
    }
  }

  /**
   * End element in order to read from aTunes radio list
   * 
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (XML_RADIO.equals(qName)) {
      // End of radio element, add to list
      WebRadio radio = new WebRadio(radioName, radioUrl);
      webradios.add(radio);
      inRadio = false;
    } else if (XML_NAME.equals(qName)) {
      radioName = buffer.toString();
    } else if (XML_URL.equals(qName)) {
      radioUrl = buffer.toString();
    }
  }

  /**
   * Perform a search in all files names with given criteria
   * 
   * @param sCriteria
   * @return
   */
  public Set<SearchResult> search(String sCriteria) {
    synchronized (FileManager.getInstance().getLock()) {
      Set<SearchResult> tsResu = new TreeSet<SearchResult>();
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
  public Set<WebRadio> getWebRadios() {
    return (Set<WebRadio>) ((TreeSet<WebRadio>)webradios).clone();
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
    String defaultRadio = Conf.getString(CONF_DEFAULT_WEB_RADIO);
    if (WebRadioManager.getInstance().getWebRadioByName(defaultRadio) != null) {
      tooltipWebRadio = "<html>" + tooltipWebRadio + "<p><b>" + defaultRadio + "</b></html>";
    }
    return tooltipWebRadio;
  }

}
