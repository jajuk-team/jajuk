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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.XMLFilter;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Import ratings
 */
public class ImportRatingsAction extends SelectionAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  private java.io.File file;

  private class Parser extends DefaultHandler {
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
        throws SAXException {
      // <track id='11f89yuelwwdmzg1357yaw87j' rate='0' ban='true'/>
      if (qName.equals(Const.XML_TRACK)) {
        String id = attributes.getValue(Const.XML_ID);
        String preference = attributes.getValue(Const.XML_TRACK_PREFERENCE);
        String hits = attributes.getValue(Const.XML_TRACK_HITS);
        String banned = attributes.getValue(Const.XML_TRACK_BANNED);
        Track track = TrackManager.getInstance().getTrackByID(id);
        if (track == null) {
          Log.debug("Track no more exists : " + track.getID());
        }
        track.setHits(UtilString.fastLongParser(hits));
        track.setPreference(Long.parseLong(preference));
        track.setProperty(Const.XML_TRACK_BANNED, Boolean.parseBoolean(banned));
        //force final rating re-computation
        track.updateRate();
      }
    }
  }

  ImportRatingsAction() {
    super(Messages.getString("ImportRatingsAction.1"), IconLoader.getIcon(JajukIcons.LAUNCH), true);
    setShortDescription(Messages.getString("ImportRatingsAction.2"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.SelectionAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws Exception {
    final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(XMLFilter.getInstance())/*, fDir*/);
    jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    jfc.setDialogTitle(Messages.getString("ImportRatingsAction.3"));
    jfc.setMultiSelectionEnabled(false);
    jfc.setAcceptDirectories(true);
    final int returnVal = jfc.showOpenDialog(JajukMainWindow.getInstance());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      // retrieve selected directory and update it in all necessary places
      file = jfc.getSelectedFile();
      if (!file.exists()) {
        Messages.showWarningMessage(Messages.getString("Error.181"));
        return;
      }
      int iResu = Messages.getChoice(Messages.getString("Confirmation_reset_ratings_overwrite")
          + " : \n\n" + file.getName(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        return;
      }
      // Perform this asynchronously as it may be long
      new Thread("Import ratings") {
        @Override
        public void run() {
          try {
            importRatings(file);
            Messages.showInfoMessage(Messages.getString("Success"));
          } catch (Exception ex1) {
            Messages.showWarningMessage(Messages.getString("Error.000") + "-" + ex1.getMessage());
            Log.warn(0, "IOException while exporting current ratings", ex1);
          }
        }
      }.start();
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    }
  }

  public void importRatings(final File file) throws IOException, SAXException, JajukException,
      ParserConfigurationException {
    Log.info("Importing current track ratings from file {{" + file + "}}");
    final long time = System.currentTimeMillis();
    SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(false);
    spf.setNamespaceAware(false);
    // See http://xerces.apache.org/xerces-j/features.html for details
    spf.setFeature("http://xml.org/sax/features/external-general-entities", false);
    spf.setFeature("http://xml.org/sax/features/string-interning", true);
    SAXParser saxParser = spf.newSAXParser();
    if (!file.exists()) {
      throw new JajukException(5, file.toString());
    }
    final InputSource input;
    if (file.getAbsolutePath().endsWith(".zip")) {
      input = new InputSource(new ZipInputStream(new FileInputStream(file)));
    } else {
      input = new InputSource(new FileInputStream(file));
    }
    saxParser.parse(input, new Parser());
    Log.debug("Ratings exported in " + (System.currentTimeMillis() - time) + " ms");
  }
}
