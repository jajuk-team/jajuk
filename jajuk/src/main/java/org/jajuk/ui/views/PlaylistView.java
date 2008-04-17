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

import ext.SwingWorker;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.PlaylistFile;
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
import org.jajuk.ui.helpers.PlaylistRepositoryTableModel;
import org.jajuk.ui.helpers.PlaylistTableModel;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukJSplitPane;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.ui.widgets.SmartPlaylist;
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

  /*
   * Some widgets are private to make sure QueueView that extends this class
   * will use them
   */

  JajukJSplitPane split;

  // --Editor--
  JPanel jpEditor;
  JPanel jpEditorControl;
  private JajukButton jbRun;
  JajukButton jbSave;
  JajukButton jbRemove;
  JajukButton jbUp;
  JajukButton jbDown;
  JajukButton jbAddShuffle;
  JajukButton jbClear;
  private JajukButton jbPrepParty;
  JLabel jlTitle;
  JajukTable editorTable;
  JMenuItem jmiFilePlay;
  JMenuItem jmiFilePush;
  JMenuItem jmiFileAddFavorites;
  JMenuItem jmiFileProperties;
  /** Current playlist file */
  PlaylistFile plf;
  /** Selection set flag */
  boolean bSettingSelection = false;
  /** Last selected directory using add button */
  java.io.File fileLast;
  /** Editor Model */
  protected PlaylistTableModel editorModel;

  // --- Repository ---
  PlaylistRepository repositoryPanel;
  SmartPlaylist spNew;
  SmartPlaylist spNovelties;
  SmartPlaylist spBookmark;
  SmartPlaylist spBestof;
  

  public void initEditorPanel() {
    jpEditor = new JPanel();

    // Control panel
    jpEditorControl = new JPanel();
    jpEditorControl.setBorder(BorderFactory.createEtchedBorder());
    double sizeControl[][] = { { 5, TableLayout.PREFERRED, 15, TableLayout.FILL, 5 }, { 5, 25, 5 } };
    TableLayout layout = new TableLayout(sizeControl);
    layout.setHGap(2);
    jpEditorControl.setLayout(layout);
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

    jpEditorControl.add(jtb, "1,1");
    jpEditorControl.add(jlTitle, "3,1,c,c");
    editorModel = new PlaylistTableModel(false);
    editorTable = new JajukTable(editorModel, CONF_PLAYLIST_EDITOR_COLUMNS);
    editorModel.populateModel(editorTable.getColumnsConf());
    editorTable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // multi-row
    // selection
    editorTable.setSortable(false);
    editorTable.setDragEnabled(true);
    editorTable.setTransferHandler(new PlaylistEditorTransferHandler(editorTable));
    setRenderers();
    // just an icon
    editorTable.getColumnModel().getColumn(0).setPreferredWidth(20);
    editorTable.getColumnModel().getColumn(0).setMaxWidth(20);
    editorTable.getTableHeader().setPreferredSize(new Dimension(0, 20));
    editorTable.showColumns(editorTable.getColumnsConf());
    ListSelectionModel lsm = editorTable.getSelectionModel();
    lsm.addListSelectionListener(this);
    double size[][] = { { 0.99 }, { TableLayout.PREFERRED, 0.99 } };
    jpEditor.setLayout(new TableLayout(size));
    jpEditor.add(jpEditorControl, "0,0");
    jpEditor.add(new JScrollPane(editorTable), "0,1");
    // menu items
    jmiFilePlay = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_SELECTION));
    jmiFilePlay.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFilePush = new JMenuItem(ActionManager.getAction(JajukAction.PUSH_SELECTION));
    jmiFilePush.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFileAddFavorites = new JMenuItem(ActionManager.getAction(JajukAction.BOOKMARK_SELECTION));
    jmiFileAddFavorites.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFileProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiFileProperties.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    editorTable.getMenu().add(jmiFilePlay);
    editorTable.getMenu().add(jmiFilePush);
    editorTable.getMenu().add(jmiFileAddFavorites);
    editorTable.getMenu().add(jmiFileProperties);

    ColorHighlighter colorHighlighter = new ColorHighlighter(Color.ORANGE, null,
        new PlayHighlighterPredicate(editorModel));
    Highlighter alternate = Util.getAlternateHighlighter();
    editorTable.setHighlighters(alternate, colorHighlighter);
    // register events
    ObservationManager.register(this);
    // -- force a refresh --
    // Add key listener to enable row suppression using SUPR key
    editorTable.addKeyListener(new KeyAdapter() {
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
    editorTable.setCommand(new ILaunchCommand() {
      public void launch(int nbClicks) {
        if (nbClicks == 2) {
          // double click, launches selected track and all after
          StackItem item = editorModel.getStackItem(editorTable.getSelectedRow());
          if (item != null) {
            // We launch all tracks from this
            // position
            // to the end of playlist
            FIFO.getInstance().push(editorModel.getItemsFrom(editorTable.getSelectedRow()),
                ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
          }
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    initEditorPanel();

    spNew = new SmartPlaylist(SmartPlaylist.Type.NEW);
   // spNew.addMouseListener(ma);
    spBestof = new SmartPlaylist(SmartPlaylist.Type.BESTOF);
    spNovelties = new SmartPlaylist(SmartPlaylist.Type.NOVELTIES);
    spBookmark = new SmartPlaylist(SmartPlaylist.Type.BOOKMARK);
    JPanel jpSmartPlaylists = new JPanel();
    jpSmartPlaylists.setLayout(new FlowLayout());
    jpSmartPlaylists.add(spNew);
    jpSmartPlaylists.add(spBestof);
    jpSmartPlaylists.add(spNovelties);
    jpSmartPlaylists.add(spBookmark);
    double size[][] = { { TableLayout.FILL }, { TableLayout.PREFERRED, TableLayout.FILL } };
    JPanel jpRepository = new JPanel(new TableLayout(size));
    repositoryPanel = new PlaylistRepository();
    repositoryPanel.initUI();
    jpRepository.add(jpSmartPlaylists, "0,0");
    jpRepository.add(repositoryPanel, "0,1");

    split = new JajukJSplitPane(JSplitPane.VERTICAL_SPLIT);
    split.setDividerLocation(0.5d);
    split.add(jpRepository);
    split.add(jpEditor);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(split);
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

  void setRenderers() {
    // set right cell renderer for play and rate icons
    // Play icon
    TableColumn col = editorTable.getColumnModel().getColumn(0);
    col.setMinWidth(PLAY_COLUMN_SIZE);
    col.setMaxWidth(PLAY_COLUMN_SIZE);
    // rate
    col = editorTable.getColumnModel().getColumn(5);
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
          editorTable.acceptColumnsEvents = false; // flag reloading to avoid
          // wrong
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
            editorModel = new PlaylistTableModel(false);
            editorModel.populateModel(editorTable.getColumnsConf());
            editorTable.setModel(editorModel);
            setRenderers();
            editorTable.addColumnIntoConf((String) properties.get(DETAIL_CONTENT));
            editorTable.showColumns(editorTable.getColumnsConf());
          } else if (EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) { // can be null at view
              // populate
              return;
            }
            editorModel = new PlaylistTableModel(false);
            editorModel.populateModel(editorTable.getColumnsConf());
            editorTable.setModel(editorModel);
            setRenderers();
            // remove item from configuration cols
            editorTable.removeColumnFromConf((String) properties.get(DETAIL_CONTENT));
            editorTable.showColumns(editorTable.getColumnsConf());
          }
        } catch (Exception e) {
          Log.error(e);
        } finally {
          editorTable.acceptColumnsEvents = true;
        }
      }
    });
  }

  private void refreshCurrentPlaylist() {
    if (plf == null) { // nothing ? leave
      return;
    }
    // when nothing is selected, set default button state
    if (editorTable.getSelectionModel().getMinSelectionIndex() == -1) {
      setDefaultButtonState();
    }
    try {
      editorModel.alItems = Util.createStackItems(plf.getFiles(), ConfigurationManager
          .getBoolean(CONF_STATE_REPEAT), true); // PERF
      ((JajukTableModel) editorTable.getModel()).populateModel(editorTable.getColumnsConf());
    } catch (JajukException je) { // don't trace because
      // it is called in a loop
    }
    int[] rows = editorTable.getSelectedRows();
    // save selection
    editorModel.fireTableDataChanged();// refresh
    bSettingSelection = true;
    for (int i = 0; i < rows.length; i++) {
      // set saved selection after a refresh
      editorTable.getSelectionModel().addSelectionInterval(rows[i], rows[i]);
    }
    bSettingSelection = false;
  }

  private void selectPlaylist(PlaylistFile plf) {
    // remove selection
    editorTable.getSelectionModel().clearSelection();
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
    refreshCurrentPlaylist();
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
          try {
            plf.commit();
            InformationJPanel.getInstance().setMessage(
                Messages.getString("AbstractPlaylistEditorView.22"), InformationJPanel.INFORMATIVE);
          } catch (JajukException je) {
            Log.error(je);
            Messages.showErrorMessage(je.getCode(), je.getMessage());
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
        int iRow = editorTable.getSelectedRow();
        if (iRow != -1) { // -1 means nothing is selected
          if (ae.getSource() == jbDown) {
            plf.down(iRow);
            if (iRow < editorTable.getModel().getRowCount() - 1) {
              // force immediate table refresh
              refreshCurrentPlaylist();
              editorTable.getSelectionModel().setSelectionInterval(iRow + 1, iRow + 1);
            }
          } else if (ae.getSource() == jbUp) {
            plf.up(iRow);
            if (iRow > 0) {
              // force immediate table refresh
              refreshCurrentPlaylist();
              editorTable.getSelectionModel().setSelectionInterval(iRow - 1, iRow - 1);
            }
          }
        }
      } else if (ae.getSource() == jbRemove) {
        removeSelection();
      } else if (ae.getSource() == jbAddShuffle) {
        int iRow = editorTable.getSelectedRow();
        if (iRow < 0) {
          // no row is selected, add to the end
          iRow = editorTable.getRowCount();
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
    int[] iRows = editorTable.getSelectedRows();
    if (iRows.length > 1) {// if multiple selection, remove
      // selection
      editorTable.getSelectionModel().removeIndexInterval(0, editorTable.getRowCount() - 1);
    }
    for (int i = 0; i < iRows.length; i++) {
      // don't forget that index changes when removing
      plf.remove(iRows[i] - i);
    }
    // set selection to last line if end reached
    int iLastRow = editorTable.getRowCount() - 1;
    if (iRows[0] == editorTable.getRowCount()) {
      editorTable.getSelectionModel().setSelectionInterval(iLastRow, iLastRow);
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
      if (selectedRow > editorModel.alItems.size() - 1) {
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
          if (selection.getMaxSelectionIndex() < editorModel.alItems.size() - 1) {
            // a normal item can't go in the planned items
            jbDown.setEnabled(true);
          } else {
            jbDown.setEnabled(false);
          }
        }
      }
    }
  }

  /**
   * This class is not a view but the playlist upper panel of the PlaylistView
   * It leverages the Abstract Playlist code (filters...s)
   */
  class PlaylistRepository extends AbstractTableView implements ListSelectionListener {

    private static final long serialVersionUID = 3842568503545896845L;

    /** Selected smart playlist */
    SmartPlaylist selectedSP;

    JPopupMenu jpmenu;

    JMenuItem jmiPlay;

    JMenuItem jmiSaveAs;

    JMenuItem jmiDelete;

    JMenuItem jmiProperties;

    JMenuItem jmiPrepParty;

    MouseAdapter ma;

    public PlaylistRepository() {
      super();
      columnsConf = CONF_PLAYLIST_REPOSITORY_COLUMNS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.views.AbstractTableView#initTable()
     */
    @Override
    void initTable() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.views.AbstractTableView#populateTable()
     */
    @Override
    JajukTableModel populateTable() {
      return new PlaylistRepositoryTableModel();
    }

    public String getDesc() {
      return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.views.IView#initUI()
     */
    public void initUI() {
      // Popup menus
      jpmenu = new JPopupMenu();

      jmiPlay = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.0"));
      jmiPlay.addActionListener(this);
      jpmenu.add(jmiPlay);

      jmiDelete = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.3"));
      jmiDelete.addActionListener(this);
      jpmenu.add(jmiDelete);

      jmiSaveAs = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.2"));
      jmiSaveAs.addActionListener(this);
      jpmenu.add(jmiSaveAs);

      jmiPrepParty = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.19"));
      jmiPrepParty.addActionListener(this);
      jpmenu.add(jmiPrepParty);

      jmiProperties = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.4"));
      jmiProperties.addActionListener(this);
      jpmenu.add(jmiProperties);

      

      SwingWorker sw = new SwingWorker() {
        public Object construct() {
          PlaylistRepository.super.construct();
          // Add this generic menu item manually to ensure it's the last one in
          // the list for GUI reasons
          jtable.getMenu().add(jmiDelete);
          jtable.getMenu().add(jmiProperties);
          jtable.getSelectionModel().addListSelectionListener(PlaylistRepository.this);
          jtable.getColumnModel().getSelectionModel().addListSelectionListener(
              PlaylistRepository.this);
          // Add specific behavior on left click
          jtable.setCommand(new ILaunchCommand() {
            public void launch(int nbClicks) {
              int iSelectedCol = jtable.getSelectedColumn();
              // selected column in view Test click on play icon launch track
              // only
              // if only first column is selected (fixes issue with
              // Ctrl-A)
              if (jtable.getSelectedColumnCount() == 1
              // click on play icon
                  && (jtable.convertColumnIndexToModel(iSelectedCol) == 0)
                  // double click on any column and edition state false
                  || nbClicks == 2) {
                // selected row in view
                PlaylistFile plf = (PlaylistFile) jtable.getSelection().get(0);
                List<File> alFiles = Util.getPlayableFiles(plf);
                if (alFiles.size() > 0) {
                  // launch it
                  FIFO.getInstance().push(
                      Util.createStackItems(alFiles, ConfigurationManager
                          .getBoolean(CONF_STATE_REPEAT), true),
                      ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
                } else {
                  Messages.showErrorMessage(10, plf.getName());
                }
              }
            }
          });
          return null;
        }

        public void finished() {
          PlaylistRepository.super.finished();
          jtbEditable.setVisible(false);
        }
      };
      sw.start();
    }

    public void valueChanged(ListSelectionEvent e) {
      if (e.getValueIsAdjusting()) {
        return;
      }
      Util.waiting();
      SwingWorker sw = new SwingWorker() {
        PlaylistFile plf;

        @Override
        public void finished() {
          if (plf != null) {
            selectPlaylist(plf);
          }
        }

        @Override
        public Object construct() {
          int row = jtable.convertRowIndexToModel(jtable.getSelectedRow());
          JajukTableModel model = (JajukTableModel) jtable.getModel();
          plf = (PlaylistFile) model.getItemAt(row);
          // load the playlist
          try {
            plf.forceRefresh();
          } catch (JajukException e1) {
            Log.error(e1);
            Messages.showErrorMessage(17);
          }
          return null;
        }

      };
      sw.start();
    }

    /**
     * Set current Smart playlist
     * 
     * @param sp
     */
    void selectPlaylistFileItem(SmartPlaylist sp) {
      // remove item border
      if (selectedSP != null) {
        selectedSP.getIcon().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      }
      sp.getIcon().setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
      // set new item
      this.selectedSP = sp;
    }

    /**
     * Display the playlist menu
     * 
     * @param e
     */
    private void showMenu(MouseEvent e) {
      // cannot delete special playlists
      jmiDelete.setEnabled(false);
      // Save as is only for special playlists
      jmiSaveAs.setEnabled(true);
      jmiProperties.setEnabled(false);

      jmiPlay.setEnabled(true);
      jmiPrepParty.setEnabled(true);
      jpmenu.show(e.getComponent(), e.getX(), e.getY());
    }
  }
}
