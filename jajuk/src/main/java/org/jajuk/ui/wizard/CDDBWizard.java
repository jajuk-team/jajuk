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

import com.sun.java.help.impl.SwingWorker;

import entagged.freedb.Freedb;
import entagged.freedb.FreedbAlbum;
import entagged.freedb.FreedbException;
import entagged.freedb.FreedbQueryResult;
import entagged.freedb.FreedbReadResult;
import info.clearthought.layout.TableLayout;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.Event;
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
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

public class CDDBWizard extends JajukJDialog implements Const, ActionListener,
    TableColumnModelListener, TableModelListener, MouseListener {

  private static final long serialVersionUID = 1L;

  /** Main panel */
  JPanel jpMain;

  NavigationPanel jpNav;

  JajukTable jtable;

  CDDBTableModel model;

  JDialog dial;

  /** OK/Cancel panel */
  OKCancelPanel okc;

  /** File filter */
  Set<File> filter;

  /** Layout dimensions */
  double[][] dSize = { { 0, TableLayout.FILL }, { 0, 22, TableLayout.PREFERRED, 22 } };

  /** Items */
  List<CDDBTrack> alTracks;

  /** Freedb Items */
  Freedb fdb;

  FreedbAlbum fdbAlbum;

  FreedbQueryResult[] aResult;

  FreedbReadResult fdbReader;

  Vector<String> vAlbums;

  int[] aIdxToTag;

  int idx;

  
  class NavigationPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    SteppedComboBox jcbAlbum;

    JLabel jlCurrent;

    JPanel jpButtons;

    JLabel jlGenre;

    JTextField jtGenre;

    JLabel jlAlbum;

    /**
     * Navigation panel
     * 
     */
    NavigationPanel() {
      // Albums List
      jlAlbum = new JLabel(Messages.getString("CDDBWizard.5"));
      jcbAlbum = new SteppedComboBox();

      // add all matches
      jcbAlbum.setModel(new DefaultComboBoxModel(vAlbums));
      int iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
      jcbAlbum.setPopupWidth(iWidth);
      jcbAlbum.setSelectedIndex(idx);
      jcbAlbum.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent arg0) {
          idx = jcbAlbum.getSelectedIndex();
          Log.debug("Select index " + jcbAlbum.getSelectedIndex());

          // change the table model
          model = populateModel();
          jtable.setModel(model);
          Log.debug(model.getRowCount() + " rows in model.");
          jtable.selectAll();

          jtGenre.setText(fdbReader.getGenre());
        }
      });

      // Genre Text label
      jlGenre = new JLabel(Messages.getString("CDDBWizard.16"));
      jtGenre = new JTextField(fdbReader.getGenre());
      jtGenre.setEditable(false);

      // Show the number of matches found

      jlCurrent = new JLabel(aResult.length + " " + Messages.getString("CDDBWizard.18"));

      int iXspace = 5;
      double sizeControl[][] = {
          { iXspace, TableLayout.FILL, iXspace, 350, iXspace, TableLayout.FILL, iXspace,
              TableLayout.FILL, iXspace, TableLayout.FILL, iXspace }, { 22 } };

      setLayout(new TableLayout(sizeControl));

      add(jlAlbum, "1,0");
      add(jcbAlbum, "3,0");
      add(jlGenre, "5,0");
      add(jtGenre, "7,0");
      add(jlCurrent, "9,0");
    }
  }

  /**
   * CDDB wizard
   * 
   * @param dir
   *          directory to retag
   */
  public CDDBWizard(final List<Track> alGivenTracks) {
    // windows title: absolute path name of the given directory
    setTitle(Messages.getString("CDDBWizard.19"));
    SwingWorker sw = new SwingWorker() {

      @Override
      public Object construct() {
        try {

          // Put an error message if no tracks were found
          if (alGivenTracks.size() == 0) {
            Messages.showWarningMessage(Messages.getString("CDDBWizard.14"));
            return null;
          } else {
            // Convert given tracks into CDDBTracks
            alTracks = new ArrayList<CDDBTrack>(alGivenTracks.size());
            filter = null;
            for (Track t : alGivenTracks) {
              CDDBTrack track = new CDDBTrack(t);
              if (!alTracks.contains(track)) {
                // filter.add(track);
                alTracks.add(track);
              }
            }
            // Put a message that show the query is running
            InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.11"), 0);
            // Perform CDDB Query
            idx = performQuery();
            // Put an error message if CDDB query don't found any
            // matches
            if (idx < 0) {
              Messages.showInfoMessage(Messages.getString("CDDBWizard.12"));
              return null;
            }
            // Put a message that show possible matches are found
            else {
              InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.13"), 0);
            }
          }
        } catch (Exception e) {
          Log.error(e);
        }
        return null;
      }

      @Override
      public void finished() {
        if (idx >= 0 && aResult != null) {
          // create Main panel
          jpMain = new JPanel();
          jpMain.setBorder(BorderFactory.createEtchedBorder());
          jpMain.setLayout(new TableLayout(dSize));
          jtable = populateTable();
          jpNav = new NavigationPanel();
          okc = new OKCancelPanel(CDDBWizard.this, Messages.getString("Apply"), Messages
              .getString("Close"));

          // Display main panel
          display();
        }
        UtilGUI.stopWaiting();
      }
    };
    sw.start();
  }

  /** Fill the table */
  public JajukTable populateTable() {
    model = populateModel();
    jtable = new JajukTable(model, true, null);
    jtable.selectAll();
    jtable.getColumnModel().addColumnModelListener(this);
    jtable.packAll();
    return jtable;
  }

  public CDDBTableModel populateModel() {
    try {
      fdbReader = fdb.read(aResult[idx]);
    } catch (FreedbException e) {
      Log.debug("CDDB error ! " + e.getLocalizedMessage());
      dispose();
    }
    // Repopulate model
    model = new CDDBTableModel(alTracks);
    model.populateModel(fdbReader);
    model.fireTableDataChanged();
    model.addTableModelListener(CDDBWizard.this);
    return model;
  }

  public void display() {
    // Create UI
    jpMain.add(jpNav, "1,1");
    jpMain.add(new JScrollPane(jtable), "1,2");
    jpMain.add(okc, "1,3");

    getRootPane().setDefaultButton(okc.getOKButton());
    getContentPane().add(jpMain);
    pack();
    setLocationRelativeTo(JajukWindow.getInstance());
    setVisible(true);
  }

  public int performQuery() {
    fdb = new Freedb();
    fdbAlbum = new FreedbAlbum(this.alTracks.toArray(new CDDBTrack[this.alTracks.size()]));
    try {
      aResult = fdb.query(fdbAlbum);
      vAlbums = new Vector<String>(aResult.length);
      Log.debug("CDDB Query return " + aResult.length + " match(es).");
      int index = 0;
      for (int i = 0; i < aResult.length; i++) {
        vAlbums.add("[" + aResult[i].getDiscId() + "] " + aResult[i].getAlbum());
        if (aResult[i].isExactMatch()) {
          index = i;
          InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.17"), 0);
        }
      }
      return index;
    } catch (FreedbException e) {
      Log.debug(e.getLocalizedMessage());
    }
    return -1;
  }

  public void retagFiles() {
    aIdxToTag = jtable.getSelectedRows();
    boolean b = false;
    if (aIdxToTag.length == 0) {
      dispose();
    } else {
      for (int iRow : aIdxToTag) {
        Track track = alTracks.get(iRow).getTrack();
        try {
          String sValue = fdbReader.getAlbum();

          if (sValue != null && sValue.trim().length() > 0) {
            track = TrackManager.getInstance().changeTrackAlbum(track, sValue, filter);
          }
          sValue = fdbReader.getArtist();
          if (sValue != null && sValue.trim().length() > 0) {
            track = TrackManager.getInstance().changeTrackAuthor(track, sValue, filter);
          }
          sValue = fdbReader.getTrackTitle(iRow);
          if (sValue != null && sValue.trim().length() > 0) {
            track = TrackManager.getInstance().changeTrackName(track, sValue, filter);
          }
          sValue = fdbReader.getGenre();
          if (sValue != null && sValue.trim().length() > 0) {
            track = TrackManager.getInstance().changeTrackStyle(track, sValue, filter);
          }
          try {
            int iValue = fdbReader.getTrackNumber(iRow);
            if (iValue > 0) {
              track = TrackManager.getInstance().changeTrackOrder(track, iValue, filter);
            }
            b = true;
          } catch (Exception e) {
            Log.error(e);
          }
          try {
            long lValue = Long.parseLong(fdbReader.getYear());
            if (lValue > 0 && lValue < 10000) {
              track = TrackManager.getInstance()
                  .changeTrackYear(track, fdbReader.getYear(), filter);
            }
          } catch (Exception e) {
            Log.error(e);
          }
        } catch (JajukException e) {
          Log.error(e);
          // dispose();
        }
      }
      if (b) {
        InformationJPanel.getInstance().setMessage(Messages.getString("Success"),
            InformationJPanel.INFORMATIVE);
        ObservationManager.notify(new Event(JajukEvents.EVENT_DEVICE_REFRESH));
      } else {
        InformationJPanel.getInstance().setMessage(Messages.getString("Error.155"),
            InformationJPanel.ERROR);
      }

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

  public void columnMoved(TableColumnModelEvent arg0) {
  }

  public void columnAdded(TableColumnModelEvent e) {
  }

  public void columnMarginChanged(ChangeEvent e) {
  }

  public void columnRemoved(TableColumnModelEvent e) {
  }

  public void columnSelectionChanged(ListSelectionEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
  }

  public void mouseReleased(MouseEvent e) {
  }

  public void tableChanged(TableModelEvent e) {
  }
}
