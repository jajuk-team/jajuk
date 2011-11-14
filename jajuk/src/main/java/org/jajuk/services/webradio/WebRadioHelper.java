/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.services.webradio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.xml.sax.SAXException;

/**
 * Persistence and various helper methods for webradios.
 */
public class WebRadioHelper {

  /** Custom webradios  file */
  private static File fCustom = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_CUSTOM);

  /** Presets webradios file */
  private static File fPresets = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_PRESET);

  /** Reference to the WebRadiomanager */
  private static WebRadioManager manager = WebRadioManager.getInstance();

  /** utility class, no instantiation */
  private WebRadioHelper() {
  }
  

  /**
   * Gets the current web radio tooltip.
   * 
   * @return Current webradio tooltip text
   */
  public static String getCurrentWebRadioTooltip() {
    String tooltipWebRadio = Messages.getString("CommandJPanel.25");
    String defaultRadio = Conf.getString(Const.CONF_DEFAULT_WEB_RADIO);
    if (manager.getWebRadioByName(defaultRadio) != null) {
      tooltipWebRadio = "<html>" + tooltipWebRadio + "<p><b>" + defaultRadio + "</b></html>";
    }
    return tooltipWebRadio;
  }

  /**
   * Perform a search in all files names with given criteria.
   * 
   * @param sCriteria DOCUMENT_ME
   * 
   * @return the set< search result>
   */
  public static Set<SearchResult> search(String sCriteria) {
    synchronized (FileManager.getInstance()) {
      Set<SearchResult> tsResu = new TreeSet<SearchResult>();
      for (WebRadio radio : manager.getWebRadios()) {
        if (radio.getName().toLowerCase(Locale.getDefault())
            .indexOf(sCriteria.toLowerCase(Locale.getDefault())) != -1) {
          String desc = radio.getName();
          if (UtilString.isNotEmpty(radio.getLabel())) {
            desc += " (" + radio.getUrl() + ")";
          }
          tsResu.add(new SearchResult(radio, desc));
        }
      }
      return tsResu;
    }
  }

  /**
   * Load an existing repository.
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  public static void loadCustomRadios() throws SAXException, IOException,
      ParserConfigurationException {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(false);
    spf.setNamespaceAware(false);
    SAXParser saxParser = spf.newSAXParser();
    saxParser.parse(fCustom, new CustomRadiosPersistenceHelper());
  }

  /**
   * Load a presets list.
   * @param preset file to load
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  public static void loadPresetsRadios(File presetToLoad) throws SAXException, IOException,
      ParserConfigurationException {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(false);
    spf.setNamespaceAware(false);
    SAXParser saxParser = spf.newSAXParser();
    saxParser.parse(presetToLoad, new PresetRadiosPersistenceHelper());
  }

  /**
  * Copy the default radio file to the current repository file.
  * 
  * @throws IOException Signals that an I/O exception has occurred.
  */
  public static void forcePresetsRefresh() throws IOException {
    // Clear existing preset radios and store user keywords if any
    HashMap<WebRadio, String> radioKeywords = new HashMap<WebRadio, String>();
    for (WebRadio radio : manager.getWebRadiosByOrigin(WebRadioOrigin.PRESET)) {
      // Note that we iterate over a shallow copy of the webradios, no problem to remove items here.
      manager.removeItem(radio);
      if (UtilString.isNotEmpty(radio.getKeywords())) {
        radioKeywords.put(radio, radio.getKeywords());
      }
    }
    // Download presets and store them in the cache to avoid loosing user keywords
    File cachedPreset = DownloadManager.downloadToCache(new URL(Const.URL_WEBRADIO_PRESETS));
    try {
      loadPresetsRadios(cachedPreset);
    } catch (Exception e) {
      handleFileCorrupted(fPresets, e);
    }
    // Restore user keywords
    for (WebRadio radio : manager.getWebRadiosByOrigin(WebRadioOrigin.PRESET)) {
      String keywords = radioKeywords.get(radio);
      if (UtilString.isNotEmpty(keywords)) {
        radio.setProperty(Const.XML_KEYWORDS, keywords);
      }
    }
  }

  private static void handleFileCorrupted(File file, Exception e) {
    Log.error(137, "Webradio file corrupted: " + file.getAbsolutePath(), e);
    // Remove file if it is corrupted so it will be created again the
    // next time
    if (!fCustom.delete()) {
      Log.warn("Could not delete file " + file.getAbsolutePath());
    }

  }

  /**
   * Download and load webradio files.
   */
  public static void loadWebRadios() {
    // Try to load custom radios first, then presets
    fCustom = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_CUSTOM);
    fPresets = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_PRESET);

    if (fCustom.exists()) {
      try {
        loadCustomRadios();
      } catch (Exception e) {
        handleFileCorrupted(fPresets, e);
      }
    } else {
      Log.info("No custom webradio file found.");
    }

    if (!fPresets.exists()) {
      // download the stream list and load it asynchronously to avoid
      // freezing offline sessions.
      new Thread("WebRadio Download Thread") {
        @Override
        public void run() {
          try {
            DownloadManager.download(new URL(Const.URL_WEBRADIO_PRESETS),
                SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_PRESET));
            loadPresetsRadios(fPresets);
          } catch (Exception e) {
            Log.error(e);
          }
        }
      }.start();
    } else {
      try {
        loadPresetsRadios(fPresets);
      } catch (Exception e) {
        handleFileCorrupted(fPresets, e);
      }
    }
  }
}
