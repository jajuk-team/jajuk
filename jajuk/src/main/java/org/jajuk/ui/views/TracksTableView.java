/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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

package org.jajuk.ui.views;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.StackItem;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.TableTransferHandler;
import org.jajuk.ui.helpers.TracksTableModel;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Logical table view
 */
public class TracksTableView extends AbstractTableView {

  private static final long serialVersionUID = 1L;

  JPopupMenu jmenuTrack;

  JMenuItem jmiTrackPlay;

  JMenuItem jmiTrackPush;

  JMenuItem jmiTrackDelete;

  JMenuItem jmiTrackPlayShuffle;

  JMenuItem jmiTrackPlayRepeat;

  JMenuItem jmiTrackPlayAlbum;

  JMenuItem jmiTrackPlayAuthor;

  JMenuItem jmiTrackAddFavorite;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("TracksTableView.0");
  }

  public void initUI() {
    // Perform common table view initializations
    super.initUI();
    // Track menu
    jmenuTrack = new JPopupMenu();
    jmiTrackPlay = new JMenuItem(Messages.getString("TracksTableView.7"),
        IconLoader.ICON_PLAY_16x16);
    jmiTrackPlay.addActionListener(this);
    jmiTrackPush = new JMenuItem(Messages.getString("TracksTableView.8"), IconLoader.ICON_PUSH);
    jmiTrackPush.addActionListener(this);
    Action actionDeleteFile = ActionManager.getAction(JajukAction.DELETE);
    jmiTrackDelete = new JMenuItem(actionDeleteFile);
    jmiTrackDelete.addActionListener(this);
    jmiTrackPlayShuffle = new JMenuItem(Messages.getString("TracksTableView.9"),
        IconLoader.ICON_SHUFFLE);
    jmiTrackPlayShuffle.addActionListener(this);
    jmiTrackPlayRepeat = new JMenuItem(Messages.getString("TracksTableView.10"),
        IconLoader.ICON_REPEAT);
    jmiTrackPlayRepeat.addActionListener(this);
    jmiTrackPlayAlbum = new JMenuItem(Messages.getString("TracksTableView.11"),
        IconLoader.ICON_ALBUM);
    jmiTrackPlayAlbum.addActionListener(this);
    jmiTrackPlayAuthor = new JMenuItem(Messages.getString("TracksTableView.12"),
        IconLoader.ICON_AUTHOR);
    jmiTrackPlayAuthor.addActionListener(this);
    jmiTrackAddFavorite = new JMenuItem(Messages.getString("TracksTableView.15"),
        IconLoader.ICON_BOOKMARK_FOLDERS);
    jmiTrackAddFavorite.addActionListener(this);
    jmiProperties = new JMenuItem(Messages.getString("TracksTableView.14"),
        IconLoader.ICON_PROPERTIES);
    jmiProperties.addActionListener(this);
    jmenuTrack.add(jmiTrackPlay);
    jmenuTrack.add(jmiTrackPush);
    jmenuTrack.add(jmiTrackDelete);
    jmenuTrack.add(jmiTrackPlayShuffle);
    jmenuTrack.add(jmiTrackPlayRepeat);
    jmenuTrack.add(jmiTrackPlayAlbum);
    jmenuTrack.add(jmiTrackPlayAuthor);
    jmenuTrack.add(jmiTrackAddFavorite);
    jmenuTrack.add(jmiProperties);
  }

  /** Fill the table */
  public JajukTableModel populateTable() {
    // model creation
    TracksTableModel model = new TracksTableModel();
    return model;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  public void mouseEntered(MouseEvent e) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    if (e.isPopupTrigger()) {
      handlePopup(e);
      // Left click
    } else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
      int iSelectedCol = jtable.getSelectedColumn();
      // selected column in view Test click on play icon launch track only
      // if only first column is selected (fixes issue with
      // Ctrl-A)
      if (jtable.getSelectedColumnCount() == 1
      // click on play icon
          && (jtable.convertColumnIndexToModel(iSelectedCol) == 0)
          // double click on any column and edition state false
          || (e.getClickCount() == 2 && !jtbEditable.isSelected())) {
        // selected row in view
        int iSelectedRow = jtable.getSelectedRow();
        Track track = (Track) model.getItemAt(jtable.convertRowIndexToModel(iSelectedRow));
        File file = track.getPlayeableFile(false);
        if (file != null) {
          try {
            // launch it
            FIFO.getInstance().push(
                new StackItem(file, ConfigurationManager.getBoolean(CONF_STATE_REPEAT)),
                ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));

          } catch (JajukException je) {
            Log.error(je);
          }
        } else {
          Messages.showErrorMessage(10, track.getName());
        }
      } else if (e.getClickCount() == 1) {
        int iSelectedRow = jtable.rowAtPoint(e.getPoint());
        // Store real row index
        TableTransferHandler.iSelectedRow = iSelectedRow;
      }
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (e.isPopupTrigger()) {
      handlePopup(e);
    }
  }

  public void handlePopup(final MouseEvent e) {
    int iSelectedRow = jtable.rowAtPoint(e.getPoint());
    // Store real row index
    TableTransferHandler.iSelectedRow = iSelectedRow;
    // right click on a selected node set if none or 1 node is
    // selected, a right click on another node
    // select it if more than 1, we keep selection and display a
    // popup for them
    if (jtable.getSelectedRowCount() < 2) {
      jtable.getSelectionModel().setSelectionInterval(iSelectedRow, iSelectedRow);
    }
    jmenuTrack.show(jtable, e.getX(), e.getY());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    new Thread() {
      public void run() {
        // Editable state
        if (e.getSource() == jtbEditable) {
          ConfigurationManager.setProperty(CONF_TRACKS_TABLE_EDITION, Boolean.toString(jtbEditable
              .isSelected()));
          model.setEditable(jtbEditable.isSelected());
          return;
        }
        // computes selected tracks
        ArrayList<File> alFilesToPlay = new ArrayList<File>(10);
        int[] indexes = jtable.getSelectedRows();
        ArrayList<Item> alSelectedTracks = new ArrayList<Item>(indexes.length);
        for (int i = 0; i < indexes.length; i++) { // each track in
          // selection
          Track track = (Track) model.getItemAt(jtable.convertRowIndexToModel(indexes[i]));
          alSelectedTracks.add(track);
          ArrayList<Track> alTracks = new ArrayList<Track>(indexes.length);
          if (e.getSource() == jmiTrackPlayAlbum) {
            Album album = track.getAlbum();
            alTracks.addAll(TrackManager.getInstance().getAssociatedTracks(album));
            // add all tracks from the same album
          }
          if (e.getSource() == jmiTrackPlayAuthor) {
            Author author = track.getAuthor();
            // add all tracks from the same author
            alTracks.addAll(TrackManager.getInstance().getAssociatedTracks(author));
          } else {
            alTracks.add(track);
          }
          for (Track track2 : alTracks) {
            // each selected track and tracks from same album author
            // if required
            File file = track2.getPlayeableFile(false);
            if (file != null && !alFilesToPlay.contains(file)) {
              alFilesToPlay.add(file);
            }
          }
        }
        if (alFilesToPlay.size() == 0) {
          Messages.showErrorMessage(18);
          return;
        }
        // simple play
        if (e.getSource() == jmiTrackPlay || e.getSource() == jmiTrackPlayAlbum
            || e.getSource() == jmiTrackPlayAuthor) {
          FIFO.getInstance().push(
              Util.createStackItems(Util.applyPlayOption(alFilesToPlay), ConfigurationManager
                  .getBoolean(CONF_STATE_REPEAT), true), false);
        }
        // push
        else if (e.getSource() == jmiTrackPush) {
          FIFO.getInstance().push(
              Util.createStackItems(Util.applyPlayOption(alFilesToPlay), ConfigurationManager
                  .getBoolean(CONF_STATE_REPEAT), true), true);
        }
        // delete
        else if (e.getSource() == jmiTrackDelete) {
          jmiTrackDelete.putClientProperty(DETAIL_SELECTION, alSelectedTracks);
        }
        // shuffle play
        else if (e.getSource() == jmiTrackPlayShuffle) {
          Collections.shuffle(alFilesToPlay, new Random());
          FIFO.getInstance().push(
              Util.createStackItems(alFilesToPlay, ConfigurationManager
                  .getBoolean(CONF_STATE_REPEAT), true), false);
        }
        // repeat play
        else if (e.getSource() == jmiTrackPlayRepeat) {
          FIFO.getInstance().push(
              Util.createStackItems(Util.applyPlayOption(alFilesToPlay), true, true), false);
        }
        // bookmark
        else if (e.getSource() == jmiTrackAddFavorite) {
          Bookmarks.getInstance().addFiles(alFilesToPlay);
        }
        // properties
        else if (e.getSource() == jmiProperties) {
          if (jtable.getSelectedRowCount() == 1) {
            // mono selection
            Track track = (Track) model.getItemAt(jtable.convertRowIndexToModel(jtable
                .getSelectedRow()));
            ArrayList<Item> alItems = new ArrayList<Item>(1);
            alItems.add(track);
            new PropertiesWizard(alItems);
          } else {// multi selection
            ArrayList<Item> alTracks = new ArrayList<Item>(10);
            for (int i = 0; i <= jtable.getRowCount(); i++) {
              if (jtable.getSelectionModel().isSelectedIndex(i)) {
                Track track = (Track) model.getItemAt(jtable.convertRowIndexToModel(i));
                alTracks.add(track);
              }
            }
            new PropertiesWizard(alTracks);
          }
        }
      }
    }.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTableView#initTable()
   */
  @Override
  void initTable() {
    boolean bEditable = ConfigurationManager.getBoolean(CONF_TRACKS_TABLE_EDITION);
    jtbEditable.setSelected(bEditable);
  }

}
