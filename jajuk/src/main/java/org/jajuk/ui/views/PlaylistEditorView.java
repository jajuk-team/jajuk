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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
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
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.Duration;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.PlayHighlighterPredicate;
import org.jajuk.ui.helpers.PlaylistEditorTransferHandler;
import org.jajuk.ui.helpers.PlaylistFileItem;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.perspectives.TracksPerspective;
import org.jajuk.ui.widgets.IconLabel;
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
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;

/**
 * Adapter for playlists editors *
 */
public class PlaylistEditorView extends ViewAdapter implements Observer, ActionListener,
    ListSelectionListener {

  private static final long serialVersionUID = -2851288035506442507L;

  JPanel jpControl;

  JajukButton jbRun;

  JajukButton jbSave;

  JajukButton jbRemove;

  JajukButton jbUp;

  JajukButton jbDown;

  JajukButton jbAddShuffle;

  JajukButton jbClear;

  JajukButton jbPrepParty;

  JLabel jlTitle;

  JajukTable jtable;

  JMenuItem jmiFilePlay;

  JMenuItem jmiFilePush;

  JMenuItem jmiFileAddFavorites;

  JMenuItem jmiFileProperties;

  /** playlist editor title : playlist file or playlist name */
  String sTitle;

  /** Current playlist file item */
  PlaylistFileItem plfi;

  /** Playlist file type */
  int iType;

  /** Values */
  List<StackItem> alItems = new ArrayList<StackItem>(10);

  /** Values planned */
  ArrayList<StackItem> alPlanned = new ArrayList<StackItem>(10);

  /** Selection set flag */
  boolean bSettingSelection = false;

  /** Last selected directory using add button */
  java.io.File fileLast;

  /* Cashed icons */
  static final ImageIcon iconNormal = IconLoader.ICON_TRACK_FIFO_NORM;

  static final ImageIcon iconRepeat = IconLoader.ICON_TRACK_FIFO_REPEAT;

  static final ImageIcon iconPlanned = IconLoader.ICON_TRACK_FIFO_PLANNED;

  static final ImageIcon iconPlaylist = IconLoader.ICON_PLAYLIST_FILE;

  /** Model */
  private JajukTableModel model;

  /** Model for table */
  class PlayListEditorTableModel extends JajukTableModel {

    private static final long serialVersionUID = 1L;

    public PlayListEditorTableModel() {
      super(15);
      setEditable(false); // table not editable
      prepareColumns();
      populateModel();
    }

    /**
     * Need to overwrite this method for drag and drop
     */
    public Item getItemAt(int iRow) {
      StackItem si = PlaylistEditorView.this.getItem(iRow);
      if (si != null) {
        return si.getFile();
      }
      return null;
    }

    /**
     * Create columns configuration
     * 
     */
    public synchronized void prepareColumns() {
      vColNames.clear();
      vId.clear();

      // State icon (play/repeat/planned)
      vColNames.add("");
      vId.add("0");

      // Track name
      // Note we display "title" and not "name" for this property for
      // clearness
      vColNames.add(Messages.getString("AbstractPlaylistEditorView.0"));
      vId.add(XML_TRACK_NAME);

      // Album
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ALBUM));
      vId.add(XML_TRACK_ALBUM);

      // Author
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_AUTHOR));
      vId.add(XML_TRACK_AUTHOR);

      // Style
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_STYLE));
      vId.add(XML_TRACK_STYLE);

      // Stars
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_RATE));
      vId.add(XML_TRACK_RATE);

      // Year
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_YEAR));
      vId.add(XML_YEAR);

      // Length
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_LENGTH));
      vId.add(XML_TRACK_LENGTH);

      // comments
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_COMMENT));
      vId.add(XML_TRACK_COMMENT);

      // Added date
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ADDED));
      vId.add(XML_TRACK_ADDED);

      // order
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ORDER));
      vId.add(XML_TRACK_ORDER);

      // Device
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_DEVICE));
      vId.add(XML_DEVICE);

      // Directory
      vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_DIRECTORY));
      vId.add(XML_DIRECTORY);

      // File name
      vColNames.add(Messages.getString("Property_filename"));
      vId.add(XML_FILE);

      // Hits
      vColNames.add(Messages.getString("Property_hits"));
      vId.add(XML_TRACK_HITS);

      // custom properties now
      // for tracks

      for (PropertyMetaInformation meta : TrackManager.getInstance().getCustomProperties()) {
        vColNames.add(meta.getName());
        vId.add(meta.getName());
      }
      // for files
      for (PropertyMetaInformation meta : FileManager.getInstance().getCustomProperties()) {
        vColNames.add(meta.getName());
        vId.add(meta.getName());
      }
    }

    /**
     * Fill model with data using an optional filter property
     */
    public synchronized void populateModel(String sPropertyName, String sPattern) {
      iRowNum = alItems.size() + alPlanned.size();
      oValues = new Object[iRowNum][iNumberStandardCols
          + TrackManager.getInstance().getCustomProperties().size()
          + FileManager.getInstance().getCustomProperties().size()];
      for (int iRow = 0; iRow < iRowNum; iRow++) {
        boolean bPlanned = false;
        Font font = null;
        StackItem item = getItem(iRow);
        if (item.isPlanned()) { // it is a planned file
          bPlanned = true;
          font = FontManager.getInstance().getFont(JajukFont.PLANNED);
        }
        File bf = item.getFile();

        // Play
        if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
          if (bPlanned) {
            oValues[iRow][0] = new IconLabel(iconPlanned, "", null, null, font, Messages
                .getString("AbstractPlaylistEditorView.20"));
          } else {
            if (item.isRepeat()) {
              // normal file, repeated
              oValues[iRow][0] = new IconLabel(iconRepeat, "", null, null, font, Messages
                  .getString("AbstractPlaylistEditorView.19"));
            } else {
              // normal file, not repeated
              oValues[iRow][0] = new IconLabel(iconNormal, "", null, null, font, Messages
                  .getString("AbstractPlaylistEditorView.18"));
            }
          }
        } else {
          oValues[iRow][0] = new IconLabel(iconPlaylist, "", null, null, font, Messages
              .getString("AbstractPlaylistEditorView.21"));
        }
        // Track name
        oValues[iRow][1] = bf.getTrack().getName();
        // Album
        oValues[iRow][2] = bf.getTrack().getAlbum().getName2();
        // Author
        oValues[iRow][3] = bf.getTrack().getAuthor().getName2();
        // Style
        oValues[iRow][4] = bf.getTrack().getStyle().getName2();
        // Rate
        oValues[iRow][5] = Util.getStars(bf.getTrack());
        // Year
        oValues[iRow][6] = bf.getTrack().getYear();
        // Length
        oValues[iRow][7] = new Duration(bf.getTrack().getDuration());
        // Comment
        oValues[iRow][8] = bf.getTrack().getStringValue(XML_TRACK_COMMENT);
        // Date discovery
        oValues[iRow][9] = bf.getTrack().getDiscoveryDate();
        // show date using default local format and not technical
        // representation Order
        oValues[iRow][10] = bf.getTrack().getOrder();
        // Device name
        oValues[iRow][11] = bf.getDevice().getName();
        // directory name
        oValues[iRow][12] = bf.getDirectory().getName();
        // file name
        oValues[iRow][13] = bf.getName();
        // Hits
        oValues[iRow][14] = bf.getTrack().getHits();
        // Custom properties now
        // for tracks
        Iterator<PropertyMetaInformation> it2 = TrackManager.getInstance().getCustomProperties()
            .iterator();
        for (int i = 0; it2.hasNext(); i++) {
          PropertyMetaInformation meta = it2.next();
          LinkedHashMap<String, Object> properties = bf.getTrack().getProperties();
          Object o = properties.get(meta.getName());
          if (o != null) {
            oValues[iRow][iNumberStandardCols + i] = o;
          } else {
            oValues[iRow][iNumberStandardCols + i] = meta.getDefaultValue();
          }
        }
        // for files
        it2 = FileManager.getInstance().getCustomProperties().iterator();
        // note that index lust start at custom track properties size
        for (int i = TrackManager.getInstance().getCustomProperties().size(); it2.hasNext(); i++) {
          PropertyMetaInformation meta = it2.next();
          LinkedHashMap<String, Object> properties = bf.getProperties();
          Object o = properties.get(meta.getName());
          if (o != null) {
            oValues[iRow][iNumberStandardCols + i] = o;
          } else {
            oValues[iRow][iNumberStandardCols + i] = meta.getDefaultValue();
          }
        }
      }
    }
  }

  /**
   * Return item at given position
   * 
   * @param iRow
   * @return
   */
  public Item getItemAt(int iRow) {
    StackItem item = getItem(iRow);
    return item.getFile();
  }

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
    model = new PlayListEditorTableModel();
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
    AlternateRowHighlighter alternate = Util.getAlternateHighlighter();
    jtable.setHighlighters(alternate, colorHighlighter);
    // register events
    ObservationManager.register(this);
    // -- force a refresh --
    // Begin by getting last events details
    Properties properties = ObservationManager
        .getDetailsLastOccurence(EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED);
    // Add current perspective than is used to filter events
    if (properties == null) {
      properties = new Properties();
    }
    // Add key listener to enable row suppression using SUPR key
    jtable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        // The fact that a selection can be removed or not is
        // in the jbRemove state
        if (e.getKeyCode() == KeyEvent.VK_DELETE && jbRemove.isEnabled()) {
          removeSelection();
          // Refresh table
          ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
        }
      }
    });
    properties.put(DETAIL_TARGET, PerspectiveManager.getCurrentPerspective().getID());
    // Add specific behavior on left click
    jtable.setCommand(new ILaunchCommand() {
      public void launch(int nbClicks) {
        if (nbClicks == 2) {
          // double click, launches selected track and all after
          StackItem item = getItem(jtable.getSelectedRow());
          if (item != null) {
            // For the queue
            if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
              if (item.isPlanned()) {
                // we can't launch a planned
                // track, leave
                item.setPlanned(false);
                item.setRepeat(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));
                item.setUserLaunch(true);
                FIFO.getInstance().push(item,
                    ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
              } else { // non planned items
                if (ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK)) {
                  FIFO.getInstance().push(item, true);
                } else {
                  FIFO.getInstance().goTo(jtable.getSelectedRow());
                  // remove selection for planned tracks
                  ListSelectionModel lsm = jtable.getSelectionModel();
                  bSettingSelection = true;
                  jtable.getSelectionModel().removeSelectionInterval(lsm.getMinSelectionIndex(),
                      lsm.getMaxSelectionIndex());
                  bSettingSelection = false;
                }
              }
            }
            // For others playlists, we launch all tracks from this
            // position
            // to the end of playlist
            else {
              FIFO.getInstance().push(getItemsFrom(jtable.getSelectedRow()),
                  ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
            }
          }
        }
      }
    });
    update(new Event(EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED, properties));
    update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_PLAYLIST_REFRESH);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED);
    eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_ADD);
    eventSubjectSet.add(EventSubject.EVENT_CUSTOM_PROPERTIES_REMOVE);
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
      public synchronized void run() { // NEED TO SYNC to avoid out out
        // bound exceptions
        try {
          EventSubject subject = event.getSubject();
          jtable.acceptColumnsEvents = false; // flag reloading to avoid wrong
          // column
          // changed of playlist
          if (EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED.equals(subject)) {
            // test mapping between editor and repository
            String sTargetedPerspective = (String) event.getDetails().get(DETAIL_TARGET);
            if (sTargetedPerspective != null
                && !sTargetedPerspective.equals(getPerspective().getID())) {
              return;
            }
            // clear planned
            // make sure planned is voided if not in Queue
            alPlanned = new ArrayList<StackItem>(0);
            // remove selection
            jtable.getSelectionModel().clearSelection();
            PlaylistFileItem plfi = (PlaylistFileItem) ObservationManager.getDetail(event,
                DETAIL_SELECTION);
            // plfi item is null when used in a perspective without
            // a playlist repository view
            if (plfi == null) {
              plfi = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_QUEUE,
                  IconLoader.ICON_PLAYLIST_QUEUE, new PlaylistFile(
                      PlaylistFileItem.PLAYLIST_TYPE_QUEUE, "1", null, null), Messages
                      .getString("PhysicalPlaylistRepositoryView.9"));
            }
            PlaylistEditorView.this.iType = plfi.getType();
            PlaylistEditorView.this.plfi = plfi;
            // set title label
            jlTitle.setText(plfi.getName());
            switch (plfi.getType()) {
            case PlaylistFileItem.PLAYLIST_TYPE_QUEUE:
              jlTitle.setIcon(IconLoader.ICON_PLAYLIST_QUEUE_SMALL);
              jlTitle.setText(plfi.getName() + " [" + FIFO.getInstance().getFIFO().size() + "]");
              break;
            case PlaylistFileItem.PLAYLIST_TYPE_BESTOF:
              jlTitle.setIcon(IconLoader.ICON_BESTOF_16x16);
              break;
            case PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK:
              jlTitle.setIcon(IconLoader.ICON_PLAYLIST_BOOKMARK_SMALL);
              break;
            case PlaylistFileItem.PLAYLIST_TYPE_NEW:
              jlTitle.setIcon(IconLoader.ICON_PLAYLIST_NEW_SMALL);
              break;
            case PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES:
              jlTitle.setIcon(IconLoader.ICON_NOVELTIES_16x16);
              break;
            default:
              jlTitle.setIcon(IconLoader.ICON_PLAYLIST_FILE);
              break;
            }
            jlTitle.setToolTipText(plfi.getName());
            setDefaultButtonState();
            update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH, ObservationManager
                .getDetailsLastOccurence(EventSubject.EVENT_PLAYLIST_REFRESH))); // force
            // refresh
            Util.stopWaiting(); // stop waiting
          }
          // current playlist has changed
          else if (EventSubject.EVENT_PLAYLIST_REFRESH.equals(subject)
              || EventSubject.EVENT_DEVICE_REFRESH.equals(subject)) {
            if (plfi == null) { // nothing ? leave
              return;
            }
            // Refresh number of tracks if we are in the queue
            if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
              jlTitle.setText(plfi.getName() + " [" + FIFO.getInstance().getFIFOSize() + "]");
            }
            // when nothing is selected, set default button state
            if (jtable.getSelectionModel().getMinSelectionIndex() == -1) {
              setDefaultButtonState();
            }
            try {
              if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
                alItems = FIFO.getInstance().getFIFO();
                alPlanned = FIFO.getInstance().getPlanned();
              } else {
                alItems = Util.createStackItems(plfi.getPlaylistFile().getFiles(),
                    ConfigurationManager.getBoolean(CONF_STATE_REPEAT), true); // PERF
              }
              ((JajukTableModel) jtable.getModel()).populateModel();
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
          } else if (EventSubject.EVENT_PLAYER_STOP.equals(subject)
              && plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
            alItems.clear();
            alPlanned.clear();
            // refresh playlist editor
            update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
          } else if (EventSubject.EVENT_CUSTOM_PROPERTIES_ADD.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) {
              // can be null at view populate
              return;
            }
            // create a new model
            model = new PlayListEditorTableModel();
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
            model = new PlayListEditorTableModel();
            jtable.setModel(model);
            setRenderers();
            // remove item from configuration cols
            jtable.removeColumnFromConf((String) properties.get(DETAIL_CONTENT));
            jtable.showColumns(jtable.getColumnsConf());
          }
        } catch (Exception e) {
          Log.error(e);
        } finally {
          jtable.acceptColumnsEvents = false; // make sure to remove this flag
        }
      }
    });

  }

  /**
   * Set default button state
   * 
   */
  private void setDefaultButtonState() {
    // set buttons
    if (iType == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
      jbClear.setEnabled(true);
      jbUp.setEnabled(false); // set it to false just for startup
      // because nothing is selected
      jbDown.setEnabled(false); // set it to false just for startup
      // because nothing is selected
      jbAddShuffle.setEnabled(true);// add at the FIFO end by
      // default even with no
      // selection
      jbRemove.setEnabled(false); // set it to false just for startup
      // because cursor is over first track
      // and it can't be removed in queue mode
      jbRun.setEnabled(false);
      // disable prepare party for queue playlist
      jbPrepParty.setEnabled(false);
    } else if (iType == PlaylistFileItem.PLAYLIST_TYPE_BESTOF
        || iType == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES) {
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

  /**
   * Return right stack item in normal or planned stacks
   * 
   * @param index
   * @return
   */
  StackItem getItem(int index) {
    if (alItems.size() == 0) {
      return null;
    }
    if (index < alItems.size()) {
      return alItems.get(index);
    } else if (index < (alItems.size() + alPlanned.size())) {
      return alPlanned.get(index - alItems.size());
    } else {
      return null;
    }
  }

  /**
   * Return all stack items from this value to the end of selection
   * 
   * @param index
   * @return an arraylist of stackitems or null if index is out of bounds
   */
  private ArrayList<StackItem> getItemsFrom(int index) {
    if (index < alItems.size()) {
      return new ArrayList<StackItem>(alItems.subList(index, alItems.size()));
    } else {
      return null;
    }
  }

  public void actionPerformed(ActionEvent ae) {
    try {
      if (ae.getSource() == jbRun) {
        plfi.getPlaylistFile().play();
      } else if (ae.getSource() == jbSave) {
        // normal playlist
        if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NORMAL) {
          // if logical editor, warning message
          if (getPerspective() instanceof TracksPerspective) {
            StringBuilder sbOut = new StringBuilder(Messages
                .getString("AbstractPlaylistEditorView.17"));
            Playlist pl = PlaylistManager.getInstance().getPlaylistByID(
                plfi.getPlaylistFile().getHashcode());
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
                    plf.setFiles(plfi.getPlaylistFile().getFiles());
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
              plfi.getPlaylistFile().commit();
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
          plfi.getPlaylistFile().saveAs();
        }
        // notify playlist repository to refresh
        ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));

      } else if (ae.getSource() == jbClear) {
        // if it is the queue playlist, stop the selection
        plfi.getPlaylistFile().clear();
        if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
          FIFO.getInstance().stopRequest();
        }
      } else if (ae.getSource() == jbDown || ae.getSource() == jbUp) {
        int iRow = jtable.getSelectedRow();
        if (iRow != -1) { // -1 means nothing is selected
          if (ae.getSource() == jbDown) {
            plfi.getPlaylistFile().down(iRow);
            if (iRow < jtable.getModel().getRowCount() - 1) {
              // force immediate table refresh
              update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH, ObservationManager
                  .getDetailsLastOccurence(EventSubject.EVENT_PLAYLIST_REFRESH)));
              jtable.getSelectionModel().setSelectionInterval(iRow + 1, iRow + 1);
            }
          } else if (ae.getSource() == jbUp) {
            plfi.getPlaylistFile().up(iRow);
            if (iRow > 0) {
              // force immediate table refresh
              update(new Event(EventSubject.EVENT_PLAYLIST_REFRESH, ObservationManager
                  .getDetailsLastOccurence(EventSubject.EVENT_PLAYLIST_REFRESH)));
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
          if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
            iRow = FIFO.getInstance().getFIFO().size();
          } else {
            iRow = jtable.getRowCount();
          }
        }
        File file = FileManager.getInstance().getShuffleFile();
        try {
          plfi.getPlaylistFile().addFile(iRow, file);
          jbRemove.setEnabled(true);
        } catch (JajukException je) {
          Messages.showErrorMessage(je.getCode());
          Log.error(je);
        }
      } else if (ae.getSource() == jbPrepParty) {
        plfi.getPlaylistFile().storePlaylist();
      }
    } catch (Exception e2) {
      Log.error(e2);
    } finally {
      ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
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
      plfi.getPlaylistFile().remove(iRows[i] - i);
    }
    // set selection to last line if end reached
    int iLastRow = jtable.getRowCount() - 1;
    if (iRows[0] == jtable.getRowCount()) {
      jtable.getSelectionModel().setSelectionInterval(iLastRow, iLastRow);
    }
  }

  /**
   * @return Returns current playlist file item
   */
  public PlaylistFileItem getCurrentPlaylistFileItem() {
    return plfi;
  }

  /**
   * Select the current playlist file item
   * 
   * @param plfi
   */
  public void setCurrentPlaylistFileItem(PlaylistFileItem plfi) {
    this.plfi = plfi;
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
      if (selectedRow > alItems.size() - 1) {
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
        if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE
            && selection.getMinSelectionIndex() == 0
            || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF
            || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES) {
          // neither for bestof nor novelties playlist
          jbRemove.setEnabled(false);
        } else {
          jbRemove.setEnabled(true);
        }
      }
      // Add shuffle button
      if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF
          // neither for bestof playlist
          || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES
          || selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()
          // multiple selection not supported
          || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && selection
              .getMinSelectionIndex() == 0)
          // can't add track at current track position
          || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && selectedRow > FIFO
              .getInstance().getFIFO().size())
      // no add for planned track but user can add over first planned
      // track to expand FIFO
      ) {
        jbAddShuffle.setEnabled(false);
      } else {
        jbAddShuffle.setEnabled(true);
      }
      // Up button
      if (selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()
          // check if several rows have been selected :
          // doesn't supported yet
          || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && FIFO.getInstance()
              .containsRepeat())
          // check if we are in the queue with repeated tracks :
          // not supported yet
          || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF
          || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES) {
        // neither for bestof nor novelties playlist
        jbUp.setEnabled(false);
      } else {
        // still here ?
        if (bPlanned) {
          // No up/down buttons for planned tracks
          jbUp.setEnabled(false);
        } else { // normal item
          if (selection.getMinSelectionIndex() == 0
              || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && selection
                  .getMinSelectionIndex() == 1)) {
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
          || (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE && FIFO.getInstance()
              .containsRepeat())
          // check if we are in the queue with repeated tracks :
          // not supported yet
          || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BESTOF
          || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES) {
        jbDown.setEnabled(false);
      } else { // yet here ?
        if (bPlanned) {
          // No up/down buttons for planned tracks
          jbDown.setEnabled(false);
        } else { // normal item
          if (plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE
              && selection.getMaxSelectionIndex() == 0) {
            // current track can't go down
            jbDown.setEnabled(false);
          } else if (selection.getMaxSelectionIndex() < alItems.size() - 1) {
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