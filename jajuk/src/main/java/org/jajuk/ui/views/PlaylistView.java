/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.ui.views;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.Playlist.Type;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.PlayHighlighterPredicate;
import org.jajuk.ui.helpers.PlaylistEditorTransferHandler;
import org.jajuk.ui.helpers.PlaylistRepositoryTableModel;
import org.jajuk.ui.helpers.PlaylistTableModel;
import org.jajuk.ui.helpers.PreferencesJMenu;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukJSplitPane;
import org.jajuk.ui.widgets.JajukJToolbar;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.ui.widgets.SmartPlaylistView;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.decorator.ColorHighlighter;

/**
 * Playlist view. This views contains two parts : the "repository" selection panel (list of smart playlists icons 
 * + the list of playlists table) and the playlist editor table.
 */
public class PlaylistView extends ViewAdapter implements ActionListener, ListSelectionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -2851288035506442507L;
  // --Editor--
  /** The playlist editor panel. */
  private JPanel jpEditor;
  /** The editor controls panel. */
  JPanel jpEditorControl;
  /** Button to run the playlist. */
  private JajukButton jbRun;
  /** Button to save the current playlist. */
  JajukButton jbSave;
  /** Button to remove the current item from the current playlist. */
  JajukButton jbRemove;
  /** Button to push the current item sooner in the playlist. */
  JajukButton jbUp;
  /** Button to drop down the current item in the curren tplaylist. */
  JajukButton jbDown;
  /** Add a shuffle track button. */
  JajukButton jbAddShuffle;
  /** Shuffle playlist button. */
  JajukButton jbShufflePlaylist;
  /** Button to clear the current playlist. */
  JajukButton jbClear;
  /** Title displayed in the editor part. */
  JLabel jlTitle;
  /** Editor table. */
  JajukTable editorTable;
  /** Menu item : play. */
  JMenuItem jmiFilePlay;
  /** Menu item : remove. */
  JMenuItem jmiFileRemove;
  /** Menu item : up. */
  JMenuItem jmiFileUp;
  /** Menu item : down. */
  JMenuItem jmiFileDown;
  /** Menu item : Open with Explorer. */
  JMenuItem jmiOpenExplorer;
  /** Current playlist. */
  Playlist plf;
  /** Editor Model. */
  protected PlaylistTableModel editorModel;
  /** Preference menu for current item. */
  PreferencesJMenu pjmFilesEditor;
  // --- Repository ---
  /** Repository panel. */
  private PlaylistRepository repositoryPanel;
  /** "New" smart playlist. */
  private SmartPlaylistView spNew;
  /** Selected smart playlist. */
  private SmartPlaylistView spSelected;
  /** List of selected files in the editor table. We don't just rely upon JajukTable's selection because we need here a deep selection computation including playlists contents */
  List<File> selectedFiles = new ArrayList<File>(20);
  /** Mouse adapter for smart playlist items. */
  private MouseAdapter ma = new JajukMouseAdapter() {
    private static final long serialVersionUID = 1L;

    @Override
    public void handleAction(final MouseEvent e) {
      SmartPlaylistView sp = (SmartPlaylistView) e.getComponent();
      if (sp == spSelected) {
        List<File> files;
        try {
          files = sp.getPlaylist().getFiles();
        } catch (JajukException e1) {
          Log.error(e1);
          return;
        }
        if ((files == null) || (files.size() == 0)) {
          Messages.showErrorMessage(18);
        } else {
          QueueModel.push(
              UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(files),
                  Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), true), false);
        }
      } else { // user changed of smart playlist selection
        selectSmartPlaylist(sp);
      }
    }

    @Override
    public void handlePopup(final MouseEvent e) {
      SmartPlaylistView sp = (SmartPlaylistView) e.getComponent();
      if (sp == spSelected) {
        // right click
        showSmartMenu(e);
      } else {
        selectSmartPlaylist(sp);
        showSmartMenu(e);
      }
    }

    /**
     * Display the playlist menu
     */
    private void showSmartMenu(MouseEvent e) {
      // We use for smart playlists panels the same popup menu than the one from
      // the repository table
      // but we disable some items like delete or properties
      // Add generic menus
      JPopupMenu menu = new JPopupMenu();
      JMenuItem jmiPlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
      JMenuItem jmiFrontPush = new JMenuItem(
          ActionManager.getAction(JajukActions.PUSH_FRONT_SELECTION));
      JMenuItem jmiPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_SELECTION));
      JMenuItem jmiPlayRepeat = new JMenuItem(
          ActionManager.getAction(JajukActions.PLAY_REPEAT_SELECTION));
      JMenuItem jmiPlayShuffle = new JMenuItem(
          ActionManager.getAction(JajukActions.PLAY_SHUFFLE_SELECTION));
      JMenuItem jmiPrepareParty = new JMenuItem(ActionManager.getAction(JajukActions.PREPARE_PARTY));
      JMenuItem jmiRepositorySaveAs = new JMenuItem(ActionManager.getAction(JajukActions.SAVE_AS));
      menu.add(jmiPlay);
      menu.add(jmiFrontPush);
      menu.add(jmiPush);
      menu.add(jmiPlayRepeat);
      menu.add(jmiPlayShuffle);
      menu.addSeparator();
      menu.add(jmiPrepareParty);
      menu.add(jmiRepositorySaveAs);
      for (MenuElement item : menu.getSubElements()) {
        ((JComponent) item).putClientProperty(Const.DETAIL_SELECTION, spSelected.getPlaylist());
      }
      menu.show(e.getComponent(), e.getX(), e.getY());
    }
  };

  /**
   * Select smart playlist.
   * 
   * @param sp the smart playlist
   */
  private void selectSmartPlaylist(SmartPlaylistView sp) {
    // remove table selection so an event will be thrown if user click on the table
    repositoryPanel.jtable.getSelectionModel().clearSelection();
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

  /**
   * Return the editor table.
   * 
   * @return the editor table
   */
  public JajukTable getTable() {
    return this.editorTable;
  }

  /**
   * Inits the editor panel.
   */
  private void initEditorPanel() {
    jpEditor = new JPanel();
    // Control panel
    jpEditorControl = new JPanel();
    jpEditorControl.setBorder(BorderFactory.createEtchedBorder());
    jbRun = new JajukButton(IconLoader.getIcon(JajukIcons.RUN));
    jbRun.setToolTipText(Messages.getString("AbstractPlaylistEditorView.2"));
    jbRun.addActionListener(this);
    jbSave = new JajukButton(IconLoader.getIcon(JajukIcons.SAVE));
    jbSave.setToolTipText(Messages.getString("AbstractPlaylistEditorView.3"));
    jbSave.addActionListener(this);
    jbRemove = new JajukButton(IconLoader.getIcon(JajukIcons.REMOVE));
    jbRemove.setToolTipText(Messages.getString("AbstractPlaylistEditorView.5"));
    jbRemove.addActionListener(this);
    jbUp = new JajukButton(IconLoader.getIcon(JajukIcons.UP));
    jbUp.setToolTipText(Messages.getString("AbstractPlaylistEditorView.6"));
    jbUp.addActionListener(this);
    jbDown = new JajukButton(IconLoader.getIcon(JajukIcons.DOWN));
    jbDown.setToolTipText(Messages.getString("AbstractPlaylistEditorView.7"));
    jbDown.addActionListener(this);
    jbAddShuffle = new JajukButton(IconLoader.getIcon(JajukIcons.ADD_SHUFFLE));
    jbAddShuffle.setToolTipText(Messages.getString("AbstractPlaylistEditorView.10"));
    jbAddShuffle.addActionListener(this);
    jbShufflePlaylist = new JajukButton(IconLoader.getIcon(JajukIcons.SHUFFLE_GLOBAL_16X16));
    jbShufflePlaylist.setToolTipText(Messages.getString("AbstractPlaylistEditorView.30"));
    jbShufflePlaylist.addActionListener(this);
    jbClear = new JajukButton(IconLoader.getIcon(JajukIcons.CLEAR));
    jbClear.setToolTipText(Messages.getString("AbstractPlaylistEditorView.9"));
    jbClear.addActionListener(this);
    jlTitle = new JLabel("");
    JToolBar jtb = new JajukJToolbar();
    // Add items
    jpEditorControl.setLayout(new MigLayout("ins 0", "[][grow][]"));
    jtb.add(jbRun);
    jtb.add(jbSave);
    jtb.add(jbRemove);
    jtb.add(jbAddShuffle);
    jtb.add(jbShufflePlaylist);
    jtb.add(jbUp);
    jtb.add(jbDown);
    jtb.addSeparator();
    jtb.add(jbClear);
    jpEditorControl.add(jtb, "left,gapright 5::");
    jpEditorControl.add(jlTitle, "center");
    editorModel = new PlaylistTableModel(false);
    editorTable = new JajukTable(editorModel, CONF_PLAYLIST_EDITOR_COLUMNS);
    editorModel.populateModel(editorTable.getColumnsConf());
    editorTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // multi-row
    // selection
    editorTable.setSortable(false);
    editorTable.setTransferHandler(new PlaylistEditorTransferHandler(editorTable));
    setRenderers();
    // just an icon
    editorTable.getColumnModel().getColumn(0).setPreferredWidth(20);
    editorTable.getColumnModel().getColumn(0).setMaxWidth(20);
    editorTable.getTableHeader().setPreferredSize(new Dimension(0, 20));
    editorTable.showColumns(editorTable.getColumnsConf());
    //  Note : don't add a ListSelectionListener here, see JajukTable code, 
    //  all the event code is centralized over there 
    editorTable.addListSelectionListener(this);
    jpEditor.setLayout(new MigLayout("ins 0", "[grow]"));
    jpEditor.add(jpEditorControl, "growx,wrap");
    JScrollPane jsp = new JScrollPane(editorTable);
    jpEditor.add(jsp, "growx");
    initMenuItems();
    ColorHighlighter colorHighlighter = new ColorHighlighter(new PlayHighlighterPredicate(
        editorTable), Color.ORANGE, null);
    editorTable.addHighlighter(colorHighlighter);
    // register events
    ObservationManager.register(this);
    // -- force a refresh --
    // Add key listener to enable row suppression using SUPR key
    editorTable.addKeyListener(new KeyAdapter() {
      @Override
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
      @Override
      public void launch(int nbClicks) {
        int iSelectedCol = editorTable.getSelectedColumn();
        // Convert column selection as columns may have been moved
        iSelectedCol = editorTable.convertColumnIndexToModel(iSelectedCol);
        // double click, launches selected track and all after
        if (nbClicks == 2
        // click on play icon
            || (nbClicks == 1 && iSelectedCol == 0)) {
          StackItem item = editorModel.getStackItem(editorTable.getSelectedRow());
          if (item != null) {
            // We launch all tracks from this
            // position
            // to the end of playlist
            QueueModel.push(editorModel.getItemsFrom(editorTable.getSelectedRow()),
                Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
          }
        }
      }
    });
  }

  /**
   * This factorizes edit panel code between regular playlist view and queue
   * view for all menu items except the play that is queue-specific.
   */
  void initMenuItems() {
    jmiFilePlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
    jmiFilePlay.putClientProperty(Const.DETAIL_SELECTION, editorTable.getSelection());
    JMenuItem jmiFileFrontPush = new JMenuItem(
        ActionManager.getAction(JajukActions.PUSH_FRONT_SELECTION));
    jmiFileFrontPush.putClientProperty(Const.DETAIL_SELECTION, editorTable.getSelection());
    JMenuItem jmiFilePush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_SELECTION));
    jmiFilePush.putClientProperty(Const.DETAIL_SELECTION, editorTable.getSelection());
    JMenuItem jmiFileAddFavorites = new JMenuItem(
        ActionManager.getAction(JajukActions.BOOKMARK_SELECTION));
    jmiFileAddFavorites.putClientProperty(Const.DETAIL_SELECTION, editorTable.getSelection());
    JMenuItem jmiFileProperties = new JMenuItem(
        ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiFileProperties.putClientProperty(Const.DETAIL_SELECTION, editorTable.getSelection());
    jmiFileRemove = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.5"),
        IconLoader.getIcon(JajukIcons.REMOVE));
    jmiFileRemove.addActionListener(this);
    jmiFileUp = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.6"),
        IconLoader.getIcon(JajukIcons.UP));
    jmiFileUp.addActionListener(this);
    jmiFileDown = new JMenuItem(Messages.getString("AbstractPlaylistEditorView.7"),
        IconLoader.getIcon(JajukIcons.DOWN));
    jmiFileDown.addActionListener(this);
    pjmFilesEditor = new PreferencesJMenu(editorTable.getSelection());
    JMenuItem jmiFileCopyURL = new JMenuItem(
        ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiFileCopyURL.putClientProperty(Const.DETAIL_CONTENT, editorTable.getSelection());
    jmiFileCopyURL = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiOpenExplorer = new JMenuItem(ActionManager.getAction(JajukActions.OPEN_EXPLORER));
    jmiOpenExplorer.putClientProperty(Const.DETAIL_CONTENT, editorTable.getSelection());
    editorTable.getMenu().add(jmiFilePlay);
    editorTable.getMenu().add(jmiFileFrontPush);
    editorTable.getMenu().add(jmiFilePush);
    editorTable.getMenu().addSeparator();
    editorTable.getMenu().add(jmiFileRemove);
    editorTable.getMenu().add(jmiFileUp);
    editorTable.getMenu().add(jmiFileDown);
    editorTable.getMenu().addSeparator();
    editorTable.getMenu().add(jmiFileCopyURL);
    editorTable.getMenu().add(jmiOpenExplorer);
    editorTable.getMenu().addSeparator();
    editorTable.getMenu().add(pjmFilesEditor);
    editorTable.getMenu().add(jmiFileAddFavorites);
    editorTable.getMenu().addSeparator();
    editorTable.getMenu().add(jmiFileProperties);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  @Override
  public void initUI() {
    initEditorPanel();
    spNew = new SmartPlaylistView(Type.NEW);
    spNew.addMouseListener(ma);
    SmartPlaylistView spBestof = new SmartPlaylistView(Type.BESTOF);
    spBestof.addMouseListener(ma);
    SmartPlaylistView spNovelties = new SmartPlaylistView(Type.NOVELTIES);
    spNovelties.addMouseListener(ma);
    SmartPlaylistView spBookmark = new SmartPlaylistView(Type.BOOKMARK);
    spBookmark.addMouseListener(ma);
    JPanel jpSmartPlaylists = new JPanel();
    jpSmartPlaylists.setLayout(new FlowLayout(FlowLayout.LEFT));
    jpSmartPlaylists.add(spNew);
    jpSmartPlaylists.add(spBestof);
    jpSmartPlaylists.add(spNovelties);
    jpSmartPlaylists.add(spBookmark);
    JPanel jpRepository = new JPanel(new MigLayout("ins 0", "[grow]"));
    repositoryPanel = new PlaylistRepository();
    repositoryPanel.initUI();
    repositoryPanel.setPerspective(getPerspective());
    jpRepository.add(jpSmartPlaylists, "growx,wrap");
    jpRepository.add(repositoryPanel, "growx");
    JajukJSplitPane split = new JajukJSplitPane(JSplitPane.VERTICAL_SPLIT);
    split.setDividerLocation(0.5d);
    split.add(jpRepository);
    split.add(jpEditor);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(split);
    // Register keystrokes over table
    setKeystrokes();
  }

  /**
   * Add keystroke support.
   */
  protected void setKeystrokes() {
    // Bind keystrokes and selection
    editorTable.putClientProperty(Const.DETAIL_SELECTION, editorTable.getSelection());
    InputMap inputMap = editorTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap actionMap = editorTable.getActionMap();
    // Properties ALT/ENTER
    JajukAction action = ActionManager.getAction(JajukActions.SHOW_PROPERTIES);
    inputMap.put(KeyStroke.getKeyStroke("alt ENTER"), "properties");
    actionMap.put("properties", action);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.CUSTOM_PROPERTIES_ADD);
    eventSubjectSet.add(JajukEvents.CUSTOM_PROPERTIES_REMOVE);
    eventSubjectSet.add(JajukEvents.DEVICE_MOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_UNMOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.VIEW_REFRESH_REQUEST);
    eventSubjectSet.add(JajukEvents.QUEUE_NEED_REFRESH);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    eventSubjectSet.add(JajukEvents.RATE_CHANGED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("AbstractPlaylistEditorView.15");
  }

  /**
   * Sets the renderers. 
   */
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
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          JajukEvents subject = event.getSubject();
          editorTable.setAcceptColumnsEvents(false); // flag reloading to avoid
          // wrong
          // column changed of playlist
          // current playlist has changed
          if (JajukEvents.DEVICE_REFRESH.equals(subject)
              // We listen this event to paint the new running track in table
              || JajukEvents.QUEUE_NEED_REFRESH.equals(subject)
              || JajukEvents.RATE_CHANGED.equals(subject)
              || JajukEvents.DEVICE_MOUNT.equals(subject)
              || JajukEvents.DEVICE_UNMOUNT.equals(subject)
              || JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
            refreshCurrentPlaylist();
          } else if (JajukEvents.CUSTOM_PROPERTIES_ADD.equals(subject)) {
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
            editorTable.addColumnIntoConf((String) properties.get(Const.DETAIL_CONTENT));
            editorTable.showColumns(editorTable.getColumnsConf());
          } else if (JajukEvents.CUSTOM_PROPERTIES_REMOVE.equals(subject)) {
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
            editorTable.removeColumnFromConf((String) properties.get(Const.DETAIL_CONTENT));
            editorTable.showColumns(editorTable.getColumnsConf());
          } else if (JajukEvents.VIEW_REFRESH_REQUEST.equals(subject)) {
            // force filter to refresh if the events has been triggered by the
            // table itself after a column change
            JTable table = (JTable) event.getDetails().get(Const.DETAIL_CONTENT);
            if (table.equals(editorTable)) {
              refreshCurrentPlaylist();
            }
          }
        } catch (Exception e) {
          Log.error(e);
        } finally {
          editorTable.setAcceptColumnsEvents(true);
        }
      }
    });
  }

  /**
   * Refresh current playlist. 
   */
  private void refreshCurrentPlaylist() {
    if (plf == null) { // nothing ? leave
      return;
    }
    List<File> files = null;
    // Try to get playlist content
    try {
      files = plf.getFiles();
      // When nothing is selected, set default button state
      if (editorTable.getSelectionModel().getMinSelectionIndex() == -1) {
        setButtonState();
      }
      editorModel.setItems(UtilFeatures.createStackItems(files,
          Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL), true));
      editorModel.populateModel(editorTable.getColumnsConf());
      int[] rows = editorTable.getSelectedRows();
      try {
        editorModel.setRefreshing(true);
        // Force table refreshing
        editorModel.fireTableDataChanged();
        // Save selection
        for (int element : rows) {
          // set saved selection after a refresh
          editorTable.getSelectionModel().addSelectionInterval(element, element);
        }
      } finally {
        editorModel.setRefreshing(false);
      }
      // Refresh the preference menu according to the selection
      // (useful on rating change for a single-row model for ie)
      pjmFilesEditor.resetUI(editorTable.getSelection());
    } catch (JajukException je) {
      Log.warn("Cannot parse playlist : " + plf.getAbsolutePath());
      // Clear the model so we don't keep previous playlist tracks
      editorModel.clear();
    }
  }

  /**
   * Select playlist.
   * 
   * @param plf the playlist (smart or not)
   */
  private void selectPlaylist(Playlist plf) {
    // remove selection
    editorTable.getSelectionModel().clearSelection();
    PlaylistView.this.plf = plf;
    // set title label
    jlTitle.setText(plf.getName());
    if (plf.getType() == Playlist.Type.BESTOF) {
      jlTitle.setIcon(IconLoader.getIcon(JajukIcons.BESTOF_16X16));
    } else if (plf.getType() == Playlist.Type.BOOKMARK) {
      jlTitle.setIcon(IconLoader.getIcon(JajukIcons.PLAYLIST_BOOKMARK_SMALL));
    } else if (plf.getType() == Playlist.Type.NEW) {
      jlTitle.setIcon(IconLoader.getIcon(JajukIcons.PLAYLIST_NEW_SMALL));
    } else if (plf.getType() == Playlist.Type.NOVELTIES) {
      jlTitle.setIcon(IconLoader.getIcon(JajukIcons.NOVELTIES_16X16));
    } else {
      // remove last smart playlist item border
      if (spSelected != null) {
        spSelected.getIcon().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        spSelected = null;
      }
      jlTitle.setIcon(IconLoader.getIcon(JajukIcons.PLAYLIST_FILE));
    }
    jlTitle.setToolTipText(plf.getName());
    setButtonState();
    refreshCurrentPlaylist();
    updatePlaylistMenuItems();
    UtilGUI.stopWaiting(); // stop waiting
  }

  /**
   * Update buttons state.
   */
  private void setButtonState() {
    try {
      if (plf == null) {
        jbRun.setEnabled(false);
        jbClear.setEnabled(false);
        jbDown.setEnabled(false);
        jbAddShuffle.setEnabled(false);
        jbShufflePlaylist.setEnabled(false);
        jbRemove.setEnabled(false);
        jbUp.setEnabled(false);
        jbSave.setEnabled(false);
      } else {
        if (plf.getType() == Playlist.Type.BESTOF || plf.getType() == Playlist.Type.NOVELTIES) {
          jbClear.setEnabled(false);
          jbDown.setEnabled(false);
          jbAddShuffle.setEnabled(false);
          jbShufflePlaylist.setEnabled(false);
          jbRemove.setEnabled(false);
          jbUp.setEnabled(false);
        } else if (plf.getType() == Playlist.Type.BOOKMARK) {
          jbClear.setEnabled(true);
          jbDown.setEnabled(false);
          jbAddShuffle.setEnabled(false);
          jbShufflePlaylist.setEnabled(false);
          jbRemove.setEnabled(false);
          jbUp.setEnabled(false);
        } else {
          jbClear.setEnabled(true);
          // set it to false just for startup because nothing is selected
          jbDown.setEnabled(false);
          // set it to false just for startup because nothing is selected
          jbUp.setEnabled(false);
          // add at the FIFO end by default even with no selection
          jbAddShuffle.setEnabled(true);
          jbShufflePlaylist.setEnabled(true);
          // set it to false just for startup because nothing is selected
          jbRemove.setEnabled(false);
        }
        // Run button is available only if the playlist is not void
        jbRun.setEnabled(plf.isReady() && plf.getFiles().size() > 0);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
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
                Messages.getString("AbstractPlaylistEditorView.22"),
                InformationJPanel.MessageType.INFORMATIVE);
          } catch (JajukException je) {
            Log.error(je);
            Messages.showErrorMessage(je.getCode());
          }
        } else {
          // Save as for normal playlists
          new Thread("Playlist Action Thread") {
            @Override
            public void run() {
              UtilGUI.waiting();
              try {
                // special playlist, same behavior than a save as
                plf.saveAs();
                // If the new playlist is saved in a known device location,
                // force a
                // refresh to make it visible immediately (issue #1263)
                boolean known = false;
                Device knownDevice = null;
                for (Device device : DeviceManager.getInstance().getDevices()) {
                  if (UtilSystem.isAncestor(device.getFIO(), plf.getFIO())) {
                    known = true;
                    knownDevice = device;
                    break;
                  }
                }
                if (known) {
                  Directory directory = DirectoryManager.getInstance().getDirectoryForIO(
                      plf.getFIO().getParentFile(), knownDevice);
                  directory.refresh(false);
                  // Force a table refresh to show the new playlist if it has
                  // been
                  // saved in a known device
                  ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
                }
              } catch (JajukException je) {
                Log.error(je);
                Messages.showErrorMessage(je.getCode());
              } catch (Exception e) {
                Log.error(e);
                Messages.showErrorMessage(0, e.getMessage());
              } finally {
                UtilGUI.stopWaiting();
              }
            }
          }.start();
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
      } else if (ae.getSource() == jbRemove || ae.getSource() == jmiFileRemove) {
        removeSelection();
      } else if (ae.getSource() == jbAddShuffle) {
        int iRow = editorTable.getSelectedRow();
        if (iRow < 0 || iRow > editorTable.getRowCount()) {
          // no or invalid row is selected, add to the end
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
      } else if (ae.getSource() == jbShufflePlaylist) {
        List<File> files = plf.getFiles();
        Collections.shuffle(files);
        editorModel.fireTableDataChanged();
      }
    } catch (JajukException e2) {
      Log.error(e2);
    } finally {
      // force immediate table refresh
      refreshCurrentPlaylist();
    }
  }

  /**
   * Import files, used when drag / dropping for ie.
   * 
   * @param files files to be imported
   * @param position 
   */
  public void importFiles(List<File> files, int position) {
    plf.addFiles(UtilFeatures.applyPlayOption(files), position);
    refreshCurrentPlaylist();
  }

  /**
   * Removes the selection. 
   */
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
   * Gets the current playlist.
   * 
   * @return Returns current playlist
   */
  public Playlist getCurrentPlaylist() {
    return plf;
  }

  /**
   * Called when table selection changed.
   * 
   * @param e 
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    ListSelectionModel selection = (ListSelectionModel) e.getSource();
    if (!selection.isSelectionEmpty()) {
      updateSelection();
      updateInformationView(selectedFiles);
      // Refresh the preference menu according to the selection
      pjmFilesEditor.resetUI(editorTable.getSelection());
      // -- now analyze each button --
      // Remove button
      // check for first row remove case : we can't remove currently
      // played track
      if (plf.getType() == Playlist.Type.BESTOF || plf.getType() == Playlist.Type.NOVELTIES) {
        // neither for bestof nor novelties playlist
        jbRemove.setEnabled(false);
      } else {
        jbRemove.setEnabled(true);
      }
      // Add shuffle button
      if (plf.getType() == Playlist.Type.BESTOF
          // neither for bestof playlist
          || plf.getType() == Playlist.Type.NOVELTIES || plf.getType() == Playlist.Type.BOOKMARK
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
          || plf.getType() == Playlist.Type.BESTOF || plf.getType() == Playlist.Type.NOVELTIES
          || plf.getType() == Playlist.Type.BOOKMARK) {
        // neither for bestof nor novelties playlist
        jbUp.setEnabled(false);
      } else {
        // still here ?
        if (selection.getMinSelectionIndex() == 0) {
          // check if we selected second track just after current
          // tracks
          jbUp.setEnabled(false); // already at the top
        } else {
          jbUp.setEnabled(true);
        }
      }
      // Down button
      if (selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()
          // check if several rows have been selected :
          // doesn't supported yet
          || plf.getType() == Playlist.Type.BESTOF || plf.getType() == Playlist.Type.NOVELTIES
          || plf.getType() == Playlist.Type.BOOKMARK) {
        jbDown.setEnabled(false);
      } else { // yet here ?
        if (selection.getMaxSelectionIndex() < editorModel.getItems().size() - 1) {
          // a normal item can't go in the planned items
          jbDown.setEnabled(true);
        } else {
          jbDown.setEnabled(false);
        }
      }
    }
  }

  /**
   * Update the Information View with selection size.
   * 
   * @param files : selection to consider
   */
  void updateInformationView(List<File> files) {
    // Update information view
    // Compute recursive selection size, nb of items...
    long lSize = 0l;
    for (File file : files) {
      lSize += file.getSize();
    }
    lSize /= 1048576; // set size in MB
    StringBuilder sbOut = new StringBuilder().append(files.size()).append(
        Messages.getString("FilesTreeView.52"));
    if (lSize > 1024) { // more than 1024 MB -> in GB
      sbOut.append(lSize / 1024).append('.').append(lSize % 1024)
          .append(Messages.getString("FilesTreeView.53"));
    } else {
      sbOut.append(lSize).append(Messages.getString("FilesTreeView.54"));
    }
    InformationJPanel.getInstance().setSelection(sbOut.toString());
  }

  /**
   * Update editor files selection.
   */
  void updateSelection() {
    selectedFiles.clear();
    for (Item item : editorTable.getSelection()) {
      File file = (File) item;
      selectedFiles.add(file);
    }
  }

  /**
   * Disables some tracks menu items if in smart playlist.
   */
  private void updatePlaylistMenuItems() {
    final boolean isReadOnly = (plf.getType() == Playlist.Type.BESTOF
        || plf.getType() == Playlist.Type.NOVELTIES || plf.getType() == Playlist.Type.BOOKMARK);
    jmiFileDown.setEnabled(!isReadOnly);
    jmiFileUp.setEnabled(!isReadOnly);
    jmiFileRemove.setEnabled(!isReadOnly);
  }

  /**
   * This class is not a view but the playlist upper panel of the PlaylistView
   * It leverages the Abstract Playlist code (filters...)
   */
  private class PlaylistRepository extends AbstractTableView {
    /** Generated serialVersionUID. */
    private static final long serialVersionUID = 3842568503545896845L;
    private JMenuItem jmiRepositorySaveAs;
    private JMenuItem jmiPrepareParty;
    /** List of playlists for which we already displayed a warning message if it contains old or external entries. */
    private final List<Playlist> alreadyWarned = new ArrayList<Playlist>(10);

    /**
     * Instantiates a new playlist repository.
     */
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
      // required by abstract superclass, but nothing to do here...
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

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.views.IView#getDesc()
     */
    @Override
    public String getDesc() {
      return null;
    }

    /**
     * Override this method to make sure to provide a non-null view ID when
     * required.
     * 
     * @return the ID
     */
    @Override
    public String getID() {
      return PlaylistView.this.getID() + "/PlaylistRepository";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.views.IView#initUI()
     */
    @Override
    public void initUI() {
      UtilGUI.populate(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.helpers.TwoStepsDisplayable#shortCall(java.lang.Object)
     */
    @Override
    public void shortCall(Object in) {
      jtable = new JajukTable(model, true, columnsConf);
      super.shortCall(null);
      jmiRepositorySaveAs = new JMenuItem(ActionManager.getAction(JajukActions.SAVE_AS));
      jmiRepositorySaveAs.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
      jmiPrepareParty = new JMenuItem(ActionManager.getAction(JajukActions.PREPARE_PARTY));
      jmiPrepareParty.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
      pjmTracks = new PreferencesJMenu(jtable.getSelection());
      jmiOpenExplorer = new JMenuItem(ActionManager.getAction(JajukActions.OPEN_EXPLORER));
      jmiOpenExplorer.putClientProperty(Const.DETAIL_CONTENT, jtable.getSelection());
      jtable.getMenu().add(jmiOpenExplorer);
      jtable.getMenu().add(jmiPrepareParty);
      jtable.getMenu().add(jmiRepositorySaveAs);
      jtable.getMenu().addSeparator();
      jtable.getMenu().add(pjmTracks);
      jtable.getMenu().addSeparator();
      // Add this generic menu item manually to ensure it's the last one in
      // the list for GUI reasons
      jtable.getMenu().add(jmiProperties);
      jtbEditable.setVisible(false);
      jtbSync.setVisible(false);
      // Select "New" playlist as default
      selectSmartPlaylist(spNew);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.helpers.TwoStepsDisplayable#longCall()
     */
    @Override
    public Object longCall() {
      super.longCall();
      return null;
    }

    /* (non-Javadoc)
    * @see org.jajuk.ui.views.AbstractTableView#onSelectionChange()
    */
    @Override
    void onSelectionChange() {
      Playlist playlist = null;
      List<File> files = null;
      try {
        int selectedRow = jtable.getSelectedRow();
        if (selectedRow < 0) {
          return;
        }
        int row = jtable.convertRowIndexToModel(selectedRow);
        JajukTableModel model = (JajukTableModel) jtable.getModel();
        playlist = (Playlist) model.getItemAt(row);
        // load the playlist
        files = playlist.getFiles();
        if (!alreadyWarned.contains(playlist) && playlist.containsExtFiles()) {
          Messages.showWarningMessage(Messages.getErrorMessage(142));
          alreadyWarned.add(playlist);
        }
      } catch (JajukException e1) {
        // Display a warning if the playlist is not parsable but still select it 
        Log.warn("Cannot parse playlist : " + plf.getAbsolutePath());
      }
      // Select the playlist even if it cannot be read (we still have some titles)
      selectPlaylist(playlist);
      if (files != null) {
        updateInformationView(files);
      } else {
        // Reset selection tip
        InformationJPanel.getInstance().setSelection(Messages.getString("InformationJPanel.9"));
      }
    }
  }
}
