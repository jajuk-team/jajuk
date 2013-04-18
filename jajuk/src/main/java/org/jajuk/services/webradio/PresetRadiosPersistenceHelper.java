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
 *  $Revision: 3132 $
 */
package org.jajuk.services.webradio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
   * Preset radios parser
   */
public class PresetRadiosPersistenceHelper extends DefaultHandler {
  /** Temporary buffer. */
  private StringBuilder buffer;
  /** Radio name */
  private String radioName;
  /** Radio url */
  private String radioUrl;
  /** Radio label. */
  private String radioLabel;
  /** Radio genre  */
  private String genre;
  /** Radio keywords */
  private String keywords;
  /** Radio bitrate */
  private String radioBitrate;
  /** Radio frequency. */
  private String radioFrequency;
  /** Is the radio removed and should be ignored ? */
  private String isRemoved;
  // Preset file format XML tags (different tag set from jajuk webradio format)
  private static final String TAG_NAME = "name";
  private static final String TAG_URL = "url";
  private static final String TAG_LABEL = "label";
  private static final String TAG_BITRATE = "bitrate";
  private static final String TAG_FREQUENCY = "frequency";
  private static final String TAG_RADIO = "Radio";
  private static final String TAG_LIST = "list";
  private static final String TAG_REMOVED = "isRemoved";
  private static final String TAG_GENRE = "genre";

  /**
   * Called when we start an element.
   * 
   * @param sUri 
   * @param s 
   * @param sQName 
   * @param attributes 
   * 
   * @throws SAXException the SAX exception
   */
  @Override
  public void startElement(String sUri, String s, String sQName, Attributes attributes)
      throws SAXException {
    buffer = new StringBuilder();
    if (TAG_RADIO.equals(sQName)) {
      keywords = attributes.getValue(attributes.getIndex(Const.XML_KEYWORDS));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
   */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    String s = new String(ch, start, length);
    if (buffer != null) {
      buffer.append(s);
    }
  }

  /**
   * End element in order to read from aTunes radio list.
   * 
   * @param uri 
   * @param localName 
   * @param qName 
   * 
   * @throws SAXException the SAX exception
   */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (Const.XML_RADIO.equals(qName)) {
      // Ignore preset radio whose isRemoved tag is set
      if (!UtilString.isEmpty(isRemoved) && "true".equals(isRemoved.toLowerCase())) {
        return;
      }
      // End of radio element, add to list
      WebRadio radio = WebRadioManager.getInstance().registerWebRadio(radioName);
      radio.setProperty(Const.XML_URL, radioUrl);
      if (!UtilString.isEmpty(radioLabel)) {
        radio.setProperty(Const.XML_DESC, radioLabel);
      }
      if (!UtilString.isEmpty(radioBitrate)) {
        radio.setProperty(Const.XML_BITRATE, Long.parseLong(radioBitrate));
      }
      if (!UtilString.isEmpty(radioFrequency)) {
        radio.setProperty(Const.XML_FREQUENCY, Long.parseLong(radioFrequency));
      }
      if (!UtilString.isEmpty(keywords)) {
        radio.setProperty(Const.XML_KEYWORDS, keywords);
      }
      if (!UtilString.isEmpty(genre)) {
        radio.setProperty(Const.XML_GENRE, genre);
      }
      // It is a preset webradio as we are in this class
      radio.setProperty(Const.XML_ORIGIN, WebRadioOrigin.PRESET);
    } else if (TAG_NAME.equals(qName)) {
      radioName = buffer.toString();
    } else if (TAG_URL.equals(qName)) {
      radioUrl = buffer.toString();
    } else if (TAG_LABEL.equals(qName)) {
      radioLabel = buffer.toString();
    } else if (Const.XML_KEYWORDS.equals(qName)) {
      keywords = buffer.toString();
    } else if (TAG_BITRATE.equals(qName)) {
      radioBitrate = buffer.toString();
    } else if (TAG_FREQUENCY.equals(qName)) {
      radioFrequency = buffer.toString();
    } else if (TAG_REMOVED.equals(qName)) {
      isRemoved = buffer.toString();
    } else if (TAG_GENRE.equals(qName)) {
      genre = buffer.toString();
    }
  }

  /**
  * Write down presets webradios for persistence between sessions.
  * Note that final file is almost identical to the one downloaded from
  * Assembla website but is augmented with Jajuk-specific concepts like keywords.
  * 
  * @throws IOException Signals that an I/O exception has occurred.
  */
  public static void commit() throws IOException {
    WebRadioManager manager = WebRadioManager.getInstance();
    // Write first to a temporary file, override previous file only if everything seems fine
    File out = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_PRESET + "~");
    String sCharset = Conf.getString(Const.CONF_COLLECTION_CHARSET);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out),
        sCharset), 1000000);
    try {
      // If optional attribute is void, don't write it down
      bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n");
      bw.write("<" + TAG_LIST + " version='1.0'>\n");
      // Manage each stream
      /**Sample radio : 
       * <Radio keywords='foo;bar'>
          <name>Buureradio</name>
          <url>http://live-three.dmd2.ch/listen.pls</url>
          <label>Switzerland</label>
          <keywords>cool;fine</keywords>
          <isRemoved>false</isRemoved>
          <bitrate>128</bitrate>
          <frequency>44100</frequency>
          <genre>Pop</genre>
        </Radio>
       */
      for (WebRadio radio : manager.getWebRadiosByOrigin(WebRadioOrigin.PRESET)) {
        // Note that we don't write down the isRemoved tag, not used by jajuk
        bw.write("\t<" + TAG_RADIO);
        if (UtilString.isNotEmpty(radio.getKeywords())) {
          bw.write(" " + Const.XML_KEYWORDS + "='" + UtilString.formatXML(radio.getKeywords())
              + "'");
        }
        bw.write(">\n");
        bw.write("\t\t<" + TAG_NAME + ">" + UtilString.formatXML(radio.getName()) + "</" + TAG_NAME
            + ">\n");
        bw.write("\t\t<" + TAG_URL + ">" + UtilString.formatXML(radio.getUrl()) + "</" + TAG_URL
            + ">\n");
        bw.write("\t\t<" + TAG_LABEL + ">" + UtilString.formatXML(radio.getDescription()) + "</"
            + TAG_LABEL + ">\n");
        bw.write("\t\t<" + Const.XML_KEYWORDS + ">" + UtilString.formatXML(radio.getKeywords())
            + "</" + Const.XML_KEYWORDS + ">\n");
        bw.write("\t\t<" + TAG_GENRE + ">" + UtilString.formatXML(radio.getGenre()) + "</"
            + TAG_GENRE + ">\n");
        bw.write("\t\t<" + TAG_BITRATE + ">" + radio.getLongValue(Const.XML_BITRATE) + "</"
            + TAG_BITRATE + ">\n");
        bw.write("\t\t<" + TAG_FREQUENCY + ">" + radio.getLongValue(Const.XML_FREQUENCY) + "</"
            + TAG_FREQUENCY + ">\n");
        bw.write("\t</" + TAG_RADIO + ">\n");
      }
      // close
      bw.write("</" + TAG_LIST + ">\n");
      bw.flush();
    } finally {
      bw.close();
    }
    // Override initial file
    if (out.length() > 0) {
      File finalFile = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_PRESET);
      try {
        UtilSystem.move(out, finalFile);
        Log.debug("Preset webradios list commited to : " + out.getAbsolutePath());
      } catch (JajukException e) {
        Log.error(e);
        throw new IOException(e);
      }
    }
  }
}
