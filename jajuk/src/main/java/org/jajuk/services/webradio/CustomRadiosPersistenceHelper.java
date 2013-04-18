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
public class CustomRadiosPersistenceHelper extends DefaultHandler {
  /** In radio tag. */
  private boolean inRadio;

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
    try {
      if (Const.XML_STREAM.equals(sQName)) {
        String name = attributes.getValue(attributes.getIndex(Const.XML_NAME));
        String url = attributes.getValue(attributes.getIndex(Const.XML_URL));
        String keywords = attributes.getValue(attributes.getIndex(Const.XML_KEYWORDS));
        String genre = attributes.getValue(attributes.getIndex(Const.XML_GENRE));
        String bitrate = attributes.getValue(attributes.getIndex(Const.XML_BITRATE));
        String frequency = attributes.getValue(attributes.getIndex(Const.XML_FREQUENCY));
        String label = attributes.getValue(attributes.getIndex(Const.XML_DESC));
        WebRadio radio = WebRadioManager.getInstance().registerWebRadio(name);
        radio.setProperty(Const.XML_URL, url);
        if (!UtilString.isEmpty(label)) {
          radio.setProperty(Const.XML_DESC, label);
        }
        if (!UtilString.isEmpty(bitrate)) {
          radio.setProperty(Const.XML_BITRATE, Long.parseLong(bitrate));
        }
        if (!UtilString.isEmpty(frequency)) {
          radio.setProperty(Const.XML_FREQUENCY, Long.parseLong(frequency));
        }
        if (!UtilString.isEmpty(keywords)) {
          radio.setProperty(Const.XML_KEYWORDS, keywords);
        }
        if (!UtilString.isEmpty(genre)) {
          radio.setProperty(Const.XML_GENRE, genre);
        }
        // It is a custom webradio as we are in this class
        radio.setProperty(Const.XML_ORIGIN, WebRadioOrigin.CUSTOM);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
  * Write down custom webradios for persistence between sessions.
  * 
  * @throws IOException Signals that an I/O exception has occurred.
  */
  public static void commit() throws IOException {
    WebRadioManager manager = WebRadioManager.getInstance();
    // Write first to a temporary file, override previous file only if everything seems fine
    File out = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_CUSTOM + "~");
    String sCharset = Conf.getString(Const.CONF_COLLECTION_CHARSET);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out),
        sCharset), 1000000);
    try {
      // If optional attribute is void, don't write it down
      bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n");
      bw.write("<" + Const.XML_STREAMS + " " + Const.XML_VERSION + "='" + Const.JAJUK_VERSION
          + "'>\n");
      // Manage each stream
      for (WebRadio radio : manager.getWebRadiosByOrigin(WebRadioOrigin.CUSTOM)) {
        bw.write("\t<" + Const.XML_STREAM + " "
        // Name
            + Const.XML_NAME + "='" + UtilString.formatXML(radio.getName()) + "' "
            //URL
            + Const.XML_URL + "='" + UtilString.formatXML(radio.getUrl()) + "' "
            //Label
            + Const.XML_DESC + "='" + UtilString.formatXML(radio.getDescription()) + "' "
            //keywords
            + Const.XML_KEYWORDS + "='" + UtilString.formatXML(radio.getKeywords()) + "' "
            //genre
            + Const.XML_GENRE + "='" + UtilString.formatXML(radio.getGenre()) + "' "
            //Bitrate
            + Const.XML_BITRATE + "='" + radio.getValue(Const.XML_BITRATE) + "' "
            //Frequency
            + Const.XML_FREQUENCY + "='" + radio.getValue(Const.XML_FREQUENCY) + "' "
            //End of line
            + "/>\n");
      }
      // close
      bw.write("</" + Const.XML_STREAMS + ">\n");
      bw.flush();
    } finally {
      bw.close();
    }
    // Override initial file
    if (out.length() > 0) {
      File finalFile = SessionService.getConfFileByPath(Const.FILE_WEB_RADIOS_CUSTOM);
      try {
        UtilSystem.move(out, finalFile);
        Log.debug("Custom webradios list commited to : " + out.getAbsolutePath());
      } catch (JajukException e) {
        Log.error(e);
        throw new IOException(e);
      }
    }
  }
}
