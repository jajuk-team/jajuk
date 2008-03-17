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

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.PlayHighlighterPredicate;
import org.jajuk.ui.helpers.PlaylistEditorTransferHandler;
import org.jajuk.ui.helpers.PlaylistTableModel;
import org.jajuk.ui.perspectives.TracksPerspective;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;

/**
 * Adapter for playlists editors *
 */
public class PlaylistView extends ViewAdapter implements Observer, ActionListener,
    ListSelectionListener {

  private static final long serialVersionUID = -2851288035506442507L;

  JPanel jpControl;

  /*
   * Some widgets are private to make sure QueueView that extends this class
   * will use them
   */
  private JajukButton jbRun;

  JajukButton jbSave;

  JajukButton jbRemove;

  JajukButton jbUp;

  JajukButton jbDown;

  JajukButton jbAddShuffle;

  private JajukButton jbClear;

  private JajukButton jbPrepParty;

  JLabel jlTitle;

  JajukTable jtable;

  JMenuItem jmiFilePlay;

  JMenuItem jmiFilePush;

  JMenuItem jmiFileAddFavorites;

  JMenuItem jmiFileProperties;

  /** playlist editor title : playlist file or playlist name */
  String sTitle;

  /** Current playlist file */
  PlaylistFile plf;

  /** Selection set flag */
  boolean bSettingSelection = false;

  /** Last selected directory using add button */
  java.io.File fileLast;

  /** Model */
  protected PlaylistTableModel model;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    // Control panel
    jpControl = new JPanel();
    jpControl.setBorder(BorderFactory.createEtchedBorder());
    // Note : we don't use toolbar because it's buggy in Metal look and feel
    // : icon get bigger
    double sizeControl[][] = { { 5, TableLayout.PREFERRED, 15, TableLayout.FILL, 5 }, { 5, 25, 5 } };
    TableLayout layout = new TableLayout(sizeControl);
    layout.setHGap(2);
    jpControl.setLayout(layout);
    jbRun = new JajukButton(IconLoader.ICON_RUN);
    jbRun.setToolTipText(Messages.getString("AbstractPlaylistEditorView.2"));
    jbRun.addActionListener(this);
    jbSave = new JajukButton(IconLoader.ICON_SAVE);
    jbSave.setToolTipText(Messages.getString("AbstractPlaylistEditorView.3"));
    jbSave.addActionListener(this);
    jbRemove = new JajukButton(IconLoader.ICON_REMOVE);
    jbRemove.setToolTipText(Messages.getString("AbstractPlaylistEditorView.5"));
    jbRemove.addActionListener(this);
    jbUp = new JajukButton(IconLoader.ICON_UP);
    jbUp.setToolTipText(Messages.getString("AbstractPlaylistEditorView.6"));
    jbUp.addActionListener(this);
    jbDown = new JajukButton(IconLoader.ICON_DOWN);
    jbDown.setToolTipText(Messages.getString("AbstractPlaylistEditorView.7"));
    jbDown.addActionListener(this);
    jbAddShuffle = new JajukButton(IconLoader.ICON_ADD_SHUFFLE);
    jbAddShuffle.setToolTipText(Messages.getString("AbstractPlaylistEditorView.10"));
    jbAddShuffle.addActionListener(this);
    jbClear = new JajukButton(IconLoader.ICON_CLEAR);
    jbClear.setToolTipText(Messages.getString("AbstractPlaylistEditorView.9"));
    jbClear.addActionListener(this);
    jbPrepParty = new JajukButton(IconLoader.ICON_EXT_DRIVE);
    jbPrepParty.setToolTipText(Messages.getString("AbstractPlaylistEditorView.27"));
    jbPrepParty.addActionListener(this);
    jlTitle = new JLabel("");
    JToolBar jtb = new JToolBar();
    jtb.setRollover(true);
    jtb.setBorder(null);

    jtb.add(jbRun);
    jtb.add(jbSave);
    jtb.add(jbRemove);
    jtb.add(jbAddShuffle);
    jtb.add(jbUp);
    jtb.add(jbDown);
    jtb.add(jbClear);
    jtb.add(jbPrepParty);

    jpControl.add(jtb, "1,1");
    jpControl.add(jlTitle, "3,1,c,c");
    model = new PlaylistTableModel(false);
    model.populateModel(jtable.getColumnsConf());
    jtable = new JajukTable(model, CONF_PLAYLIST_EDITOR_COLUMNS);
    jtable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // multi-row
    // selection
    jtable.setSortable(false);
    jtable.setDragEnabled(true);
    jtable.setTransferHandler(new PlaylistEditorTransferHandler(jtable));
    setRenderers();
    // just an icon
    jtable.getColumnModel().getColumn(0).setPreferredWidth(20);
    jtable.getColumnModel().getColumn(0).setMaxWidth(20);
    jtable.getTableHeader().setPreferredSize(new Dimension(0, 20));
    jtable.showColumns(jtable.getColumnsConf());
    ListSelectionModel lsm = jtable.getSelectionModel();
    lsm.addListSelectionListener(this);
    double size[][] = { { 0.99 }, { TableLayout.PREFERRED, 0.99 } };
    setLayout(new TableLayout(size));
    add(jpControl, "0,0");
    add(new JScrollPane(jtable), "0,1");
    // menu items
    jmiFilePlay = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_SELECTION));
    jmiFilePlay.putClientProperty(DETAIL_SELECTION, jtable.getSelection());
    jmiFilePush = new JMenuItem(ActionManager.getAction(JajukAction.PUSH_SELECTION));
    jmiFilePush.putClientProperty(DETAIL_SELECTION, jtable.getSelection());
    jmiFileAddFavorites = new JMenuItem(ActionManager.getAction(JajukAction.BOOKMARK_SELECTION));
    jmiFileAddFavorites.putClientProperty(DETAIL_SELECTION, jtable.getSelection());
    jmiFileProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiFileProperties.putClientProperty(DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiFilePlay);
    jtable.getMenu().add(jmiFilePush);
    jtable.getMenu().add(jmiFileAddFavorites);
    jtable.getMenu().add(jmiFileProperties);

    ColorHighlighter colorHighlighter = new ColorHighlighter(Color.ORANGE, null,
        new PlayHighlighterPredicate(model));
    Highlighter alternate =  Util.getAlternateHighlighter();
    jtable.setHighlighters(alternate, colorHighlighter);
    // register events
    ObservationManager.register(this);
    // -- force a refresh --
    // Add key listener to enable row suppression using SUPR key
    jtable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        // The fact that a selection can be removed or not is
        // in the jbRemove state
        if (e.getKeyCode() == KeyEvent.VK_DELETE && jbRemove.isEnabled()) {
          removeSelection();
          // Refresh table
          refreshCurrentPlaylist();
        }
      }
    });
    // Add specific behavior on left click
    jtable.setCommand(new ILaunchCommand() {
      public void launch(int nbClicks) {
        if (nbClicks == 2) {
          // double click, launches selected track and all after
          StackItem item = model.getStackItem(jtable.getSelectedRow());
          if (item != null) {
            // We launch all tracks from this
            // position
            // to the end of playlist
            FIFO.getInstance().push(model.getItemsFrom(jtable.getSelectedRow()),
                ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
          }
        }
      }
    });
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_ADD);
    eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE);
    eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("AbstractPlaylistEditorView.15");
  }

  private void setRenderers() {
    // set right cell renderer for play and rate icons
    // Play icon
    TableColumn col = jtable.getColumnModel().getColumn(0);
    col.setMinWidth(PLAY_COLUMN_SIZE);
    col.setMaxWidth(PLAY_COLUMN_SIZE);
    // rate
    col = jtable.getColumnModel().getColumn(5);
    col.setMinWidth(RATE_COLUMN_SIZE);
    col.setMaxWidth(RATE_COLUMN_SIZE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final Event event) {
    SwingUtilities.invokeLater(new Runnable() {
      public synchronized void run() { // NEED TO SYNC to avoid out of
        // bound exceptions
        try {
          EventSubject subject = event.getSubject();
          jtable.acceptColumnsEvents = false; // flag reloading to avoid wrong
          // column changed of playlist
          // current playlist has changed
          if (EventSubject.EVENT_DEVICE_REFRESH.equals(subject)) {
            refreshCurrentPlaylist();
          } else if (EventSubject.EVENT_CUSTOM_PROPERTIES_ADD.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) {
              // can be null at view populate
              return;
            }
            // create a new model
            model = new PlaylistTableModel(false);
            model.populateModel(jtable.getColumnsConf());
            jtable.setModel(model);
            setRenderers();
            jtable.addColumnIntoConf((String) properties.get(DETAIL_CONTENT));
            jtable.showColumns(jtable.getColumnsConf());
          } else if (EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) { // can be null at view
              // populate
              return;
            }
            model = new PlaylistTableModel(false);
            model.populateModel(jtable.getColumnsConf());
            jtable.setModel(model);
            setRenderers();
            // remove item from configuration cols
            jtable.removeColumnFromConf((String) properties.get(DETAIL_CONTENT));
            jtable.showColumns(jtable.getColumnsConf());
          }
        } catch (Exception e) {
          Log.error(e);
        } finally {
          jtable.acceptColumnsEvents = true; 
        }
      }
    });
  }

  private void refreshCurrentPlaylist() {
    if (plf == null) { // nothing ? leave
      return;
    }
    // when nothing is selected, set default button state
    if (jtable.getSelectionModel().getMinSelectionIndex() == -1) {
      setDefaultButtonState();
    }
    try {
      model.alItems = Util.createStackItems(plf.getFiles(), ConfigurationManager
          .getBoolean(CONF_STATE_REPEAT), true); // PERF
      ((JajukTableModel) jtable.getModel()).populateModel(jtable.getColumnsConf());
    } catch (JajukException je) { // don't trace because
      // it is called in a loop
    }
    int[] rows = jtable.getSelectedRows();
    // save selection
    model.fireTableDataChanged();// refresh
    bSettingSelection = true;
    for (int i = 0; i < rows.length; i++) {
      // set saved selection after a refresh
      jtable.getSelectionModel().addSelectionInterval(rows[i], rows[i]);
    }
    bSettingSelection = false;
  }

  private void selectPlayist(PlaylistFile plf) {
    // remove selection
    jtable.getSelectionModel().clearSelection();
    PlaylistView.this.plf = plf;
    // set title label
    jlTitle.setText(plf.getName());
    switch (plf.getType()) {
    case PlaylistFile.PLAYLIST_TYPE_BESTOF:
      jlTitle.setIcon(IconLoader.ICON_BESTOF_16x16);
      break;
    case PlaylistFile.PLAYLIST_TYPE_BOOKMARK:
      jlTitle.setIcon(IconLoader.ICON_PLAYLIST_BOOKMARK_SMALL);
      break;
    case PlaylistFile.PLAYLIST_TYPE_NEW:
      jlTitle.setIcon(IconLoader.ICON_PLAYLIST_NEW_SMALL);
      break;
    case PlaylistFile.PLAYLIST_TYPE_NOVELTIES:
      jlTitle.setIcon(IconLoader.ICON_NOVELTIES_16x16);
      break;
    default:
      jlTitle.setIcon(IconLoader.ICON_PLAYLIST_FILE);
      break;
    }
    jlTitle.setToolTipText(plf.getName());
    setDefaultButtonState();
    Util.stopWaiting(); // stop waiting
  }

  /**
   * Set default button state
   * 
   */
  private void setDefaultButtonState() {
    if (plf.getType() == PlaylistFile.PLAYLIST_TYPE_BESTOF
        || plf.getType() == PlaylistFile.PLAYLIST_TYPE_NOVELTIES) {
      jbClear.setEnabled(false);
      jbDown.setEnabled(false);
      jbAddShuffle.setEnabled(false);
      jbRemove.setEnabled(false);
      jbRun.setEnabled(true);
      jbUp.setEnabled(false);
      jbPrepParty.setEnabled(true);
    } else {
      jbClear.setEnabled(true);
      // set it to false just for startup because nothing is selected
      jbDown.setEnabled(false);
      // set it to false just for startup because nothing is selected
      jbUp.setEnabled(false);
      // add at the FIFO end by default even with no selection
      jbAddShuffle.setEnabled(true);
      // set it to false just for startup because nothing is selected
      jbRemove.setEnabled(false);
      jbRun.setEnabled(true);
      jbPrepParty.setEnabled(true);
    }
  }

  public void actionPerformed(ActionEvent ae) {
    try {
      if (ae.getSource() == jbRun) {
        plf.play();
      } else if (ae.getSource() == jbSave) {
        // normal playlist
        if (plf.getType() == PlaylistFile.PLAYLIST_TYPE_NORMAL) {
          // if logical editor, warning message
          if (getPerspective() instanceof TracksPerspective) {
            StringBuilder sbOut = new StringBuilder(Messages
                .getString("AbstractPlaylistEditorView.17"));
            Playlist pl = PlaylistManager.getInstance().getPlaylistByID(
                plf.getHashcode());
            if (pl != null) {
              for (PlaylistFile plf : pl.getPlaylistFiles()) {
                sbOut.append('\n').append(plf.getAbsolutePath());
              }
              int i = Messages.getChoice(sbOut.toString(), JOptionPane.OK_CANCEL_OPTION,
                  JOptionPane.WARNING_MESSAGE);
              if (i == JOptionPane.OK_OPTION) {
                for (PlaylistFile plf : pl.getPlaylistFiles()) {
                  plf.setModified(true);
                  try {
                    // set same files for all playlist files
                    plf.setFiles(plf.getFiles());
                    plf.commit();
                    InformationJPanel.getInstance().setMessage(
                        Messages.getString("AbstractPlaylistEditorView.22"),
                        InformationJPanel.INFORMATIVE);
                  } catch (JajukException je) {
                    Log.error(je);
                  }
                }
              }
            }
          } else {
            // in physical perspective
            try {
              plf.commit();
              InformationJPanel.getInstance().setMessage(
                  Messages.getString("AbstractPlaylistEditorView.22"),
                  InformationJPanel.INFORMATIVE);
            } catch (JajukException je) {
              Log.error(je);
              Messages.showErrorMessage(je.getCode(), je.getMessage());
            }
          }
        } else {
          // special playlist, same behavior than a save as
          plf.saveAs();
        }
        // notify playlist repository to refresh
        ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));

      } else if (ae.getSource() == jbClear) {
        // if it is the queue playlist, stop the selection
        plf.clear();
      } else if (ae.getSource() == jbDown || ae.getSource() == jbUp) {
        int iRow = jtable.getSelectedRow();
        if (iRow != -1) { // -1 means nothing is selected
          if (ae.getSource() == jbDown) {
            plf.down(iRow);
            if (iRow < jtable.getModel().getRowCount() - 1) {
              // force immediate table refresh
              refreshCurrentPlaylist();
              jtable.getSelectionModel().setSelectionInterval(iRow + 1, iRow + 1);
            }
          } else if (ae.getSource() == jbUp) {
            plf.up(iRow);
            if (iRow > 0) {
              // force immediate table refresh
              refreshCurrentPlaylist();
              jtable.getSelectionModel().setSelectionInterval(iRow - 1, iRow - 1);
            }
          }
        }
      } else if (ae.getSource() == jbRemove) {
        removeSelection();
      } else if (ae.getSource() == jbAddShuffle) {
        int iRow = jtable.getSelectedRow();
        if (iRow < 0) {
          // no row is selected, add to the end
          iRow = jtable.getRowCount();
        }
        File file = FileManager.getInstance().getShuffleFile();
        try {
          plf.addFile(iRow, file);
          jbRemove.setEnabled(true);
        } catch (JajukException je) {
          Messages.showErrorMessage(je.getCode());
          Log.error(je);
        }
      } else if (ae.getSource() == jbPrepParty) {
        plf.storePlaylist();
      }
    } catch (Exception e2) {
      Log.error(e2);
    }
  }

  private void removeSelection() {
    int[] iRows = jtable.getSelectedRows();
    if (iRows.length > 1) {// if multiple selection, remove
      // selection
      jtable.getSelectionModel().removeIndexInterval(0, jtable.getRowCount() - 1);
    }
    for (int i = 0; i < iRows.length; i++) {
      // don't forget that index changes when removing
      plf.remove(iRows[i] - i);
    }
    // set selection to last line if end reached
    int iLastRow = jtable.getRowCount() - 1;
    if (iRows[0] == jtable.getRowCount()) {
      jtable.getSelectionModel().setSelectionInterval(iLastRow, iLastRow);
    }
  }

  /**
   * @return Returns current playlist file
   */
  public PlaylistFile getCurrentPlaylistFile() {
    return plf;
  }

  /**
   * Called when table selection changed
   */
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting() || bSettingSelection) {
      // leave during normal refresh
      return;
    }
    ListSelectionModel selection = (ListSelectionModel) e.getSource();
    if (!selection.isSelectionEmpty()) {
      int selectedRow = selection.getMaxSelectionIndex();
      // true if selected line is a planned track
      boolean bPlanned = false;
      if (selectedRow > model.alItems.size() - 1) {
        // means it is a planned track
        bPlanned = true;
      }
      // -- now analyze each button --
      // Remove button
      if (bPlanned) {// not for planned track
        jbRemove.setEnabled(true);
      } else {
        // check for first row remove case : we can't remove currently
        // played track
        if (plf.getType() == PlaylistFile.PLAYLIST_TYPE_BESTOF
            || plf.getType() == PlaylistFile.PLAYLIST_TYPE_NOVELTIES) {
          // neither for bestof nor novelties playlist
          jbRemove.setEnabled(false);
        } else {
          jbRemove.setEnabled(true);
        }
      }
      // Add shuffle button
      if (plf.getType() == PlaylistFile.PLAYLIST_TYPE_BESTOF
          // neither for bestof playlist
          || plf.getType() == PlaylistFile.PLAYLIST_TYPE_NOVELTIES
          || selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()
      // multiple selection not supported
      ) {
        jbAddShuffle.setEnabled(false);
      } else {
        jbAddShuffle.setEnabled(true);
      }
      // Up button
      if (selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()
          // check if several rows have been selected :
          // doesn't supported yet
          || plf.getType() == PlaylistFile.PLAYLIST_TYPE_BESTOF
          || plf.getType() == PlaylistFile.PLAYLIST_TYPE_NOVELTIES) {
        // neither for bestof nor novelties playlist
        jbUp.setEnabled(false);
      } else {
        // still here ?
        if (bPlanned) {
          // No up/down buttons for planned tracks
          jbUp.setEnabled(false);
        } else { // normal item
          if (selection.getMinSelectionIndex() == 0) {
            // check if we selected second track just after current
            // tracks
            jbUp.setEnabled(false); // already at the top
          } else {
            jbUp.setEnabled(true);
          }
        }
      }
      // Down button
      if (selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()
          // check if several rows have been selected :
          // doesn't supported yet
          || plf.getType() == PlaylistFile.PLAYLIST_TYPE_BESTOF
          || plf.getType() == PlaylistFile.PLAYLIST_TYPE_NOVELTIES) {
        jbDown.setEnabled(false);
      } else { // yet here ?
        if (bPlanned) {
          // No up/down buttons for planned tracks
          jbDown.setEnabled(false);
        } else { // normal item
          if (selection.getMaxSelectionIndex() < model.alItems.size() - 1) {
            // a normal item can't go in the planned items
            jbDown.setEnabled(true);
          } else {
            jbDown.setEnabled(false);
          }
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  public void mouseClicked(MouseEvent e) {
  }
}