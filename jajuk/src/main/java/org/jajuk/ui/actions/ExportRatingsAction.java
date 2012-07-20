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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.filters.XMLFilter;
import org.jajuk.util.log.Log;

/**
 * Export ratings
 */
public class ExportRatingsAction extends SelectionAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** The Constant TAG_CLOSE_NEWLINE.  */
  private static final String TAG_CLOSE_NEWLINE = ">\n";
  /** The Constant TAB_CLOSE_TAG_START.  */
  private static final String TAB_CLOSE_TAG_START = "</";
  private java.io.File file;

  ExportRatingsAction() {
    super(Messages.getString("ParameterView.293"), IconLoader.getIcon(JajukIcons.SAVE_AS), true);
    setShortDescription(Messages.getString("ParameterView.294"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.SelectionAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws Exception {
    final JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(XMLFilter.getInstance()));
    jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    jfc.setDialogTitle(Messages.getString("ParameterView.297"));
    jfc.setMultiSelectionEnabled(false);
    jfc.setDialogType(JFileChooser.SAVE_DIALOG);
    String date = UtilString.getAdditionDateFormatter().format(new Date());
    jfc.setSelectedFile(new java.io.File(UtilSystem.getUserHome() + "/ratings_" + date + ".xml"));
    final int returnVal = jfc.showSaveDialog(JajukMainWindow.getInstance());
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      // retrieve selected directory and update it in all necessary places
      file = jfc.getSelectedFile();
      if (file.exists()) {
        int iResu = Messages.getChoice(Messages.getString("Confirmation_file_overwrite")
            + " : \n\n" + file.getName(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (iResu == JOptionPane.NO_OPTION || iResu == JOptionPane.CANCEL_OPTION) {
          return;
        }
      }
    }
    // Perform this asynchronously as it may be long
    new Thread("ExportRatings") {
      @Override
      public void run() {
        // start Export
        try {
          exportRatings(file);
          Messages.showInfoMessage(Messages.getString("ParameterView.299"));
        } catch (Exception ex1) {
          Messages.showWarningMessage(Messages.getString("Error.000") + "-" + ex1.getMessage());
          Log.warn(0, "IOException while exporting current ratings", ex1);
        }
      }
    }.start();
  }

  public void exportRatings(final File file) throws IOException {
    Log.info("Exporting current track ratings to file {{" + file + "}}");
    long time = System.currentTimeMillis();
    String sCharset = Conf.getString(Const.CONF_COLLECTION_CHARSET);
    final BufferedWriter bw;
    if (file.getAbsolutePath().endsWith(".zip")) {
      bw = new BufferedWriter(new OutputStreamWriter(
          new ZipOutputStream(new FileOutputStream(file)), sCharset), 1000000);
    } else {
      bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), sCharset), 1000000);
    }
    try {
      bw.write("<?xml version='1.0' encoding='" + sCharset + "'?>\n");
      bw.write("<!-- Jajuk Ratings export -->\n");
      bw.write("<" + Const.XML_TRACKS + " " + Const.XML_VERSION + "='" + Const.JAJUK_VERSION
          + "'>\n");
      for (Track track : TrackManager.getInstance().getTracks()) {
        bw.write(toRatingsXml(track));
      }
      // end of collection
      bw.write("</" + Const.XML_TRACKS + TAG_CLOSE_NEWLINE);
      bw.flush();
    } finally {
      bw.close();
    }
    Log.debug("Ratings exported in " + (System.currentTimeMillis() - time) + " ms");
  }

  private String toRatingsXml(Track track) {
    StringBuilder sb = new StringBuilder("  <").append(track.getXMLTag()).append(' ');
    sb.append(Const.XML_ID).append("=\'").append(track.getID()).append("\' ");
    sb.append(Const.XML_TRACK_HITS).append("=\'").append(track.getHits()).append("\' ");
    sb.append(Const.XML_TRACK_PREFERENCE).append("=\'")
        .append(track.getLongValue(Const.XML_TRACK_PREFERENCE)).append("\' ");
    sb.append(Const.XML_TRACK_BANNED).append("='")
        .append(track.getBooleanValue(Const.XML_TRACK_BANNED));
    sb.append("'/>\n");
    return sb.toString();
  }
}
