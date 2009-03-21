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
 *  $$Revision$$
 */
package org.jajuk.ui.wizard;

import entagged.freedb.Freedb;
import entagged.freedb.FreedbAlbum;
import entagged.freedb.FreedbException;
import entagged.freedb.FreedbQueryResult;
import entagged.freedb.FreedbReadResult;
import ext.SwingWorker;
import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.cddb.CDDBTrack;
import org.jajuk.ui.helpers.CDDBTableModel;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.ui.widgets.SteppedComboBox;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

public class CDDBWizard extends JajukJDialog implements ActionListener {

  private static final long serialVersionUID = 1L;

  /** Main panel */
  JPanel jpMain;

  SteppedComboBox jcbAlbum;

  NavigationPanel jpNav;

  JajukTable jtable;

  CDDBTableModel model;

  JDialog dial;

  /** OK/Cancel panel */
  OKCancelPanel okc;

  double p = TableLayout.PREFERRED;

  /** Items to be retagged */
  List<CDDBTrack> alCddbTracks;

  /** Freedb Items */
  Freedb fdb;

  FreedbAlbum fdbAlbum;

  FreedbQueryResult[] foundAlbums;

  FreedbReadResult fdbReader;

  List<String> comboAlbums;

  /**
   * CDDB wizard
   * 
   * @param dir
   *          directory to retag
   */
  public CDDBWizard(final List<Track> tracks) {
    super();
    
    UtilGUI.waiting();

    // windows title: absolute path name of the given directory
    setTitle(Messages.getString("CDDBWizard.19"));
    SwingWorker sw = new SwingWorker() {

      @Override
      public Object construct() {
        try {
          // Put an error message if no tracks were found
          if (tracks.size() == 0) {
            return null;
          } else {
            // Convert given tracks into CDDBTracks
            alCddbTracks = new ArrayList<CDDBTrack>(tracks.size());
            for (Track t : tracks) {
              CDDBTrack track = new CDDBTrack(t);
              if (!alCddbTracks.contains(track)) {
                alCddbTracks.add(track);
              }
            }
            // Perform CDDB Query and display an error message if CDDB query
            // don't found any match
            CDDBTrack[] cddbtracks = alCddbTracks.toArray(new CDDBTrack[alCddbTracks.size()]);
            int results = performQuery(cddbtracks);
            if (results == 0) {
              Messages.showInfoMessage(Messages.getString("CDDBWizard.12"));
              return null;
            }
          }
        } catch (Exception e) {
          Log.error(e);
        } finally {
          UtilGUI.stopWaiting();
        }
        return null;
      }

      @Override
      public void finished() {
        if (foundAlbums != null && foundAlbums.length > 0) {
          // create Main panel
          jpMain = new JPanel();
          jpNav = new NavigationPanel();
          jtable = populateTable();
          okc = new OKCancelPanel(CDDBWizard.this, Messages.getString("Apply"), Messages
              .getString("Close"));

          // Display main panel
          display();
        }
      }
    };
    sw.start();
  }

  /** Fill the table */
  public JajukTable populateTable() {
    model = populateModel();
    jtable = new JajukTable(model, true, null);
    jtable.selectAll();
    jtable.packAll();
    return jtable;
  }

  public CDDBTableModel populateModel() {
    try {
      fdbReader = fdb.read(foundAlbums[jcbAlbum.getSelectedIndex()]);
    } catch (FreedbException e) {
      Log.debug("CDDB error ! " + e.getLocalizedMessage());
      dispose();
    }
    // Re-populate model
    model = new CDDBTableModel();
    model.populateModel(alCddbTracks, fdbReader);
    model.fireTableDataChanged();
    return model;
  }

  public void display() {
    // Create UI
    double[][] dSize = { { 10, TableLayout.FILL, 10 }, { 10, p, p, p, 10 } };
    TableLayout layout = new TableLayout(dSize);
    layout.setVGap(10);
    jpMain.setLayout(layout);
    jpMain.add(jpNav, "1,1");
    jpMain.add(new JScrollPane(jtable), "1,2");
    jpMain.add(okc, "1,3");

    getRootPane().setDefaultButton(okc.getOKButton());
    getContentPane().add(jpMain);
    pack();
    setLocationRelativeTo(JajukWindow.getInstance());
    setVisible(true);
  }

  /**
   * Perform the actual freedb query
   * 
   * @param cddbtracks
   * @return number of results (0 if no result). -1 if a technical problem
   *         occurred.
   */
  public int performQuery(CDDBTrack[] cddbtracks) {
    fdb = new Freedb();
    fdbAlbum = new FreedbAlbum(cddbtracks);
    try {
      foundAlbums = fdb.query(fdbAlbum);
      comboAlbums = new ArrayList<String>(foundAlbums.length);
      for (FreedbQueryResult foundAlbum : foundAlbums) {
        comboAlbums.add("["
            + foundAlbum.getDiscId()
            + "] "
            + UtilString.getLimitedString((foundAlbum.getArtist() + " / " + foundAlbum
                .getAlbum()), 40));
        if (foundAlbum.isExactMatch()) {
          InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.17"), 0);
        }
      }
      return foundAlbums.length;
    } catch (FreedbException e) {
      // freedb throws a Freedb exception for network problem or no match found
      // we want to display an error message only in the first case
      if (e.getMessage().toLowerCase().indexOf("no match") == -1) {
        Messages.showErrorMessage(174);
        return -1;
      }
      Log.debug(e.getLocalizedMessage());
      return 0;
    } catch (Exception e) {
      Log.debug(e.getLocalizedMessage());
      Messages.showErrorMessage(174);
      return -1;
    }
  }

  public void retagFiles() {
    int[] aIdxToTag = jtable.getSelectedRows();
    if (aIdxToTag.length == 0) {
      dispose();
    } else {
      for (int iRow : aIdxToTag) {
        // Unset autocommit to tags so we write to file only once for all
        // changed tags for a single file
        TrackManager.getInstance().setAutocommit(false);
        Track track = alCddbTracks.get(iRow).getTrack();
        try {
          String sValue = fdbReader.getAlbum();
          if (sValue != null && sValue.trim().length() > 0) {
            track = TrackManager.getInstance().changeTrackAlbum(track, sValue, null);
          }
          sValue = fdbReader.getArtist();
          if (sValue != null && sValue.trim().length() > 0) {
            track = TrackManager.getInstance().changeTrackAuthor(track, sValue, null);
          }
          sValue = fdbReader.getTrackTitle(iRow);
          if (sValue != null && sValue.trim().length() > 0) {
            track = TrackManager.getInstance().changeTrackName(track, sValue, null);
          }
          sValue = fdbReader.getGenre();
          if (sValue != null && sValue.trim().length() > 0) {
            track = TrackManager.getInstance().changeTrackStyle(track, sValue, null);
          }
          // Track# can be absent from CDDB database, ignore if not provided
          try {
            long lValue = fdbReader.getTrackNumber(iRow);
            if (lValue > 0) {
              track = TrackManager.getInstance().changeTrackOrder(track, lValue, null);
            }
          } catch (NumberFormatException e) {
            Log.debug(e);
          }
          // Same for year
          try {
            long lValue = Long.parseLong(fdbReader.getYear());
            if (lValue > 0 && lValue < 3000) { // Review this after year 3000
                                                // Fry ;-)
              track = TrackManager.getInstance().changeTrackYear(track, fdbReader.getYear(), null);
            }
          } catch (NumberFormatException e) {
            Log.debug(e);
          }
          // Commit all tags for a single file (we prefer this to make sure some
          // tracks will be changed, so we don't commit all tags for all files)
          TrackManager.getInstance().commit();
        } catch (Exception e) {
          Log.error(e);
          Messages.showErrorMessage(155, track.getName());
          dispose();
          // Stop retagging if one track cannot be written to avoid displaying
          // several error messages
          // TODO a better handling could be to finish all tag writes and
          // displaying a grouped list of failed items like in properties panel
          break;
        } finally {
          TrackManager.getInstance().setAutocommit(true);
        }
      }
      InformationJPanel.getInstance().setMessage(Messages.getString("Success"),
          InformationJPanel.INFORMATIVE);
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == okc.getCancelButton()) {
      dispose();
    }
    if (e.getSource() == okc.getOKButton()) {
      dispose();
      new Thread() {
        @Override
        public void run() {
          retagFiles();
        }
      }.start();
    }
  }

  /**
   * Navigation panel class
   */
  private class NavigationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    JLabel label;

    JLabel jlCurrent;

    JPanel jpButtons;

    /**
     * Navigation panel
     * 
     */
    NavigationPanel() {
      super();
      
      // Albums List
      label = new JLabel(Messages.getString("CDDBWizard.5"));
      jcbAlbum = new SteppedComboBox();

      // add all matches
      jcbAlbum.setModel(new DefaultComboBoxModel(new Vector<String>(comboAlbums)));
      jcbAlbum.setSelectedIndex(jcbAlbum.getSelectedIndex());
      jcbAlbum.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          // Update table
          model = populateModel();
          jtable.setModel(model);
          jtable.selectAll();
        }
      });

      // Show the number of matches found
      jlCurrent = new JLabel(foundAlbums.length + " " + Messages.getString("CDDBWizard.18"));

      double sizeControl[][] = {
          { TableLayout.FILL, p, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL },
          { 10, p, 10 } };

      TableLayout layout = new TableLayout(sizeControl);
      layout.setHGap(10);

      setLayout(layout);

      add(label, "1,1");
      add(jcbAlbum, "3,1");
      add(jlCurrent, "5,1");
    }
  }

}
