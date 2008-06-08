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
import java.util.ArrayList;
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
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Playlist;
import org.jajuk.base.Playlist.Type;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
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
import org.jajuk.util.ITechnicalStrings;
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
  JMenuItem jmiFileUp;
  JMenuItem jmiFileDown;
  JMenuItem jmiFileProperties;
  /** Current playlist */
  Playlist plf;
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

  /** Selected smart playlist */
  SmartPlaylist spSelected;

  ArrayList<File> selectedFiles = new ArrayList<File>(20);

  JPopupMenu jpmenu;

  JMenuItem jmiPlay;

  JMenuItem jmiSaveAs;

  JMenuItem jmiDelete;

  JMenuItem jmiProperties;

  JMenuItem jmiPrepParty;

  MouseAdapter ma = new MouseAdapter() {

    public void mousePressed(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handlePopup(e);
        // Left click
      } else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
        SmartPlaylist sp = (SmartPlaylist) e.getComponent();
        if (sp == spSelected) {
          ArrayList<File> files;
          try {
            files = sp.getPlaylist().getFiles();
          } catch (JajukException e1) {
            Log.error(e1);
            return;
          }
          if ((files == null) || (files.size() == 0)) {
            Messages.showErrorMessage(18);
          } else {
            FIFO.getInstance().push(
                Util.createStackItems(Util.applyPlayOption(files), ConfigurationManager
                    .getBoolean(ITechnicalStrings.CONF_STATE_REPEAT), true), false);
          }
        } else { // user changed of smart playlist selection
          selectSmartPlaylist(sp);
        }
      }
    }

    public void mouseReleased(MouseEvent e) {
      if (e.isPopupTrigger()) {
        handlePopup(e);
      }
    }

    void handlePopup(final MouseEvent e) {
      SmartPlaylist sp = (SmartPlaylist) e.getComponent();
      if (sp == spSelected) {
        // right click
        showMenu(e);
      } else {
        selectSmartPlaylist(sp);
        showMenu(e);
      }
    }

    /**
     * Display the playlist menu
     */
    private void showMenu(MouseEvent e) {
      jmiDelete.setEnabled(true);
      jmiSaveAs.setEnabled(false);
      jmiProperties.setEnabled(true);
      jmiPlay.setEnabled(true);
      jmiPrepParty.setEnabled(true);
      jpmenu.show(e.getComponent(), e.getX(), e.getY());
    }
  };

  void selectSmartPlaylist(SmartPlaylist sp) {
    // remove item border
    if (spSelected != null) {
      spSelected.getIcon().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    }
    sp.getIcon().setBorder(BorderFactory.createLineBorder(Color.ORANGE, 5));
    // set new item
    spSelected = sp;
    try {
      selectedFiles.clear();
      selectedFiles.addAll(sp.getPlaylist().getFiles());
    } catch (JajukException e) {
      Log.error(e);
      return;
    }
    // Update playlist editor
    selectPlaylist(sp.getPlaylist());
  }

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
    jtb.add(jbPrepParty);
    jtb.addSeparator();
    jtb.add(jbClear);

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
    JScrollPane jsp = new JScrollPane(editorTable);
    jsp.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    jpEditor.add(jsp, "0,1");
    // menu items
    jmiFilePlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
    jmiFilePlay.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFilePush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_SELECTION));
    jmiFilePush.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFileAddFavorites = new JMenuItem(ActionManager.getAction(JajukActions.BOOKMARK_SELECTION));
    jmiFileAddFavorites.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFileProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiFileProperties.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jmiFileUp = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.6"),
        IconLoader.ICON_UP);
    jmiFileUp.addActionListener(this);
    jmiFileDown = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.7"),
        IconLoader.ICON_DOWN);
    jmiFileDown.addActionListener(this);

    editorTable.getMenu().add(jmiFilePlay);
    editorTable.getMenu().add(jmiFilePush);
    editorTable.getMenu().add(jmiFileAddFavorites);
    editorTable.getMenu().add(jmiFileUp);
    editorTable.getMenu().add(jmiFileDown);
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
                ConfigurationManager.getBoolean(CONF_OPTIONS_PUSH_ON_CLICK));
          }
        }
      }
    });

    // Popup menus
    jpmenu = new JPopupMenu();

    jmiPlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
    jmiPlay.putClientProperty(DETAIL_SELECTION, selectedFiles);
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

    jmiProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(DETAIL_SELECTION, editorTable.getSelection());
    jpmenu.add(jmiProperties);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    initEditorPanel();

    spNew = new SmartPlaylist(Type.NEW);
    spNew.addMouseListener(ma);
    spBestof = new SmartPlaylist(Type.BESTOF);
    spBestof.addMouseListener(ma);
    spNovelties = new SmartPlaylist(Type.NOVELTIES);
    spNovelties.addMouseListener(ma);
    spBookmark = new SmartPlaylist(Type.BOOKMARK);
    spBookmark.addMouseListener(ma);
    JPanel jpSmartPlaylists = new JPanel();
    jpSmartPlaylists.setLayout(new FlowLayout(FlowLayout.LEFT));
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
    // Select "New" playlist as default
    selectSmartPlaylist(spNew);
  }

  public Set<JajukEvents> getRegistrationKeys() {
    HashSet<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EVENT_CUSTOM_PROPERTIES_ADD);
    eventSubjectSet.add(JajukEvents.EVENT_CUSTOM_PROPERTIES_REMOVE);
    eventSubjectSet.add(JajukEvents.EVENT_DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.EVENT_FILE_COPIED);
    eventSubjectSet.add(JajukEvents.EVENT_VIEW_REFRESH_REQUEST);
    eventSubjectSet.add(JajukEvents.EVENT_QUEUE_NEED_REFRESH);
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
          JajukEvents subject = event.getSubject();
          editorTable.acceptColumnsEvents = false; // flag reloading to avoid
          // wrong
          // column changed of playlist
          // current playlist has changed
          if (JajukEvents.EVENT_DEVICE_REFRESH.equals(subject)
          // We listen this event to paint the new running track in table
              || JajukEvents.EVENT_QUEUE_NEED_REFRESH.equals(subject)) {
            refreshCurrentPlaylist();
          } else if (JajukEvents.EVENT_FILE_COPIED.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) {
              // if no property, the party is done
              InformationJPanel.getInstance().setMessage("", InformationJPanel.INFORMATIVE);
            } else {
              String filename = properties.getProperty(DETAIL_CONTENT);
              if (filename != null) {
                InformationJPanel.getInstance()
                    .setMessage(Messages.getString("Device.31") + filename + "]",
                        InformationJPanel.INFORMATIVE);
              }
            }
          } else if (JajukEvents.EVENT_CUSTOM_PROPERTIES_ADD.equals(subject)) {
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
          } else if (JajukEvents.EVENT_CUSTOM_PROPERTIES_REMOVE.equals(subject)) {
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
          } else if (JajukEvents.EVENT_VIEW_REFRESH_REQUEST.equals(subject)) {
            // force filter to refresh if the events has been triggered by the
            // table itself after a column change
            JTable table = (JTable) event.getDetails().get(DETAIL_CONTENT);
            if (table.equals(editorTable)) {
              refreshCurrentPlaylist();
            }
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
      setButtonState();
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

  private void selectPlaylist(Playlist plf) {
    // remove selection
    editorTable.getSelectionModel().clearSelection();
    PlaylistView.this.plf = plf;
    // set title label
    jlTitle.setText(plf.getName());
    if (plf.getType() == Playlist.Type.BESTOF) {
      jlTitle.setIcon(IconLoader.ICON_BESTOF_16x16);
    } else if (plf.getType() == Playlist.Type.BOOKMARK) {
      jlTitle.setIcon(IconLoader.ICON_PLAYLIST_BOOKMARK_SMALL);
    } else if (plf.getType() == Playlist.Type.NEW) {
      jlTitle.setIcon(IconLoader.ICON_PLAYLIST_NEW_SMALL);
    } else if (plf.getType() == Playlist.Type.NOVELTIES) {
      jlTitle.setIcon(IconLoader.ICON_NOVELTIES_16x16);
    } else {
      // remove last smart playlist item border
      if (spSelected != null) {
        spSelected.getIcon().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        spSelected = null;
      }
      jlTitle.setIcon(IconLoader.ICON_PLAYLIST_FILE);
    }
    jlTitle.setToolTipText(plf.getName());
    setButtonState();
    refreshCurrentPlaylist();
    Util.stopWaiting(); // stop waiting
  }

  /**
   * Update buttons state
   * 
   */
  private void setButtonState() {
    try {
      if (plf == null) {
        jbRun.setEnabled(false);
        jbClear.setEnabled(false);
        jbDown.setEnabled(false);
        jbAddShuffle.setEnabled(false);
        jbRemove.setEnabled(false);
        jbUp.setEnabled(false);
        jbPrepParty.setEnabled(false);
        jbSave.setEnabled(false);
      } else {
        if (plf.getType() == Playlist.Type.BESTOF || plf.getType() == Playlist.Type.NOVELTIES) {
          jbClear.setEnabled(false);
          jbDown.setEnabled(false);
          jbAddShuffle.setEnabled(false);
          jbRemove.setEnabled(false);
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
          jbPrepParty.setEnabled(true);
        }
        // Run button is available only if the playlist is not void
        jbRun.setEnabled(plf.getFiles().size() > 0);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  public void actionPerformed(ActionEvent ae) {
    try {
      if (ae.getSource() == jbRun) {
        plf.play();
      } else if (ae.getSource() == jbSave) {
        // normal playlist
        if (plf.getType() == Playlist.Type.NORMAL) {
          try {
            plf.commit();
            InformationJPanel.getInstance().setMessage(
                Messages.getString("AbstractPlaylistEditorView.22"), InformationJPanel.INFORMATIVE);
          } catch (JajukException je) {
            Log.error(je);
            Messages.showErrorMessage(je.getCode());
          }
        } else {
          try {
            // special playlist, same behavior than a save as
            plf.saveAs();
            // Force a table refresh to show the new playlist if it has been
            // saved in a known device
            ObservationManager.notify(new Event(JajukEvents.EVENT_DEVICE_REFRESH));
          } catch (JajukException je) {
            Log.error(je);
            Messages.showErrorMessage(je.getCode());
          }
        }
      } else if (ae.getSource() == jbClear) {
        // if it is the queue playlist, stop the selection
        plf.clear();
      } else if (ae.getSource() == jbDown || ae.getSource() == jbUp
          || ae.getSource() == jmiFileDown || ae.getSource() == jmiFileUp) {
        int iRow = editorTable.getSelectedRow();
        if (iRow != -1) { // -1 means nothing is selected
          if (ae.getSource() == jbDown || ae.getSource() == jmiFileDown) {
            plf.down(iRow);
            if (iRow < editorTable.getModel().getRowCount() - 1) {
              editorTable.getSelectionModel().setSelectionInterval(iRow + 1, iRow + 1);
            }
          } else if (ae.getSource() == jbUp || ae.getSource() == jmiFileUp) {
            plf.up(iRow);
            if (iRow > 0) {
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
        plf.prepareParty();
      }
    } catch (Exception e2) {
      Log.error(e2);
    } finally {
      // force immediate table refresh
      refreshCurrentPlaylist();
    }
  }

  /**
   * Import files, used when drag / dropping for ie
   * 
   * @param files
   */
  public void importFiles(List<File> files) {
    plf.addFiles(Util.applyPlayOption(files));
    refreshCurrentPlaylist();
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
   * @return Returns current playlist
   */
  public Playlist getCurrentPlaylist() {
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
        if (plf.getType() == Playlist.Type.BESTOF || plf.getType() == Playlist.Type.NOVELTIES) {
          // neither for bestof nor novelties playlist
          jbRemove.setEnabled(false);
        } else {
          jbRemove.setEnabled(true);
        }
      }
      // Add shuffle button
      if (plf.getType() == Playlist.Type.BESTOF
          // neither for bestof playlist
          || plf.getType() == Playlist.Type.NOVELTIES
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
          || plf.getType() == Playlist.Type.BESTOF || plf.getType() == Playlist.Type.NOVELTIES) {
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
          || plf.getType() == Playlist.Type.BESTOF || plf.getType() == Playlist.Type.NOVELTIES) {
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

    JMenuItem jmiRepositorySaveAs;

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
      SwingWorker sw = new SwingWorker() {
        public Object construct() {
          PlaylistRepository.super.construct();

          jmiRepositorySaveAs = new JMenuItem(ActionManager.getAction(JajukActions.SAVE_AS));
          jmiRepositorySaveAs.putClientProperty(DETAIL_SELECTION, jtable.getSelection());
          jtable.getMenu().add(jmiRepositorySaveAs);

          // Add this generic menu item manually to ensure it's the last one in
          // the list for GUI reasons
          jtable.getMenu().add(jmiProperties);
          jtable.getSelectionModel().addListSelectionListener(PlaylistRepository.this);
          // Add specific behavior on left click
          jtable.setCommand(new ILaunchCommand() {
            public void launch(int nbClicks) {
              int iSelectedCol = jtable.getSelectedColumn();
              // selected column in view Test click on play icon launch track
              // only if only first column is selected (fixes issue with
              // Ctrl-A)
              if (jtable.getSelectedColumnCount() == 1
              // click on play icon
                  && (jtable.convertColumnIndexToModel(iSelectedCol) == 0)
                  // double click on any column and edition state false
                  || nbClicks == 2) {
                // selected row in view
                Playlist plf = (Playlist) jtable.getSelection().get(0);
                List<File> alFiles = Util.getPlayableFiles(plf);
                if (alFiles.size() > 0) {
                  // launch it
                  FIFO.getInstance().push(
                      Util.createStackItems(alFiles, ConfigurationManager
                          .getBoolean(CONF_STATE_REPEAT), true),
                      ConfigurationManager.getBoolean(CONF_OPTIONS_PUSH_ON_CLICK));
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

      SwingWorker sw = new SwingWorker() {
        Playlist plf;
        boolean bErrorLoading = false;

        @Override
        public void finished() {
          if (!bErrorLoading && plf != null) {
            selectPlaylist(plf);
          }
        }

        @Override
        public Object construct() {
          int selectedRow = jtable.getSelectedRow();
          if (selectedRow < 0) {
            return null;
          }
          int row = jtable.convertRowIndexToModel(selectedRow);
          JajukTableModel model = (JajukTableModel) jtable.getModel();
          plf = (Playlist) model.getItemAt(row);
          // load the playlist
          try {
            plf.getFiles();
          } catch (JajukException e1) {
            Log.error(e1);
            Messages.showErrorMessage(17);
            bErrorLoading = true;
          }
          return null;
        }

      };
      sw.start();
    }

    /**
     * Display the playlist menu
     * 
     * @param e
     */
//    private void showMenu(MouseEvent e) {
//      // cannot delete special playlists
//      jmiDelete.setEnabled(false);
//      // Save as is only for special playlists
//      jmiProperties.setEnabled(false);
//
//      jmiPlay.setEnabled(true);
//      jtable.getMenu().show(e.getComponent(), e.getX(), e.getY());
//    }
  }
}
