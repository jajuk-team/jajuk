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
package org.jajuk.ui.views;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;

import org.jajuk.base.AlbumArtistManager;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.File;
import org.jajuk.base.GenreManager;
import org.jajuk.base.Item;
import org.jajuk.base.ItemManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.PlayHighlighterPredicate;
import org.jajuk.ui.helpers.PreferencesJMenu;
import org.jajuk.ui.helpers.TableTransferHandler;
import org.jajuk.ui.helpers.TwoStepsDisplayable;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.ui.widgets.JajukToggleButton;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.error.CannotRenameException;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.NoneAccessibleFileException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.autocomplete.ComboBoxCellEditor;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

import ext.AutoCompleteDecorator;
import net.miginfocom.swing.MigLayout;

/**
 * Abstract table view : common implementation for both files and tracks table
 * views.
 */
public abstract class AbstractTableView extends ViewAdapter implements ActionListener,
    ItemListener, TableModelListener, TwoStepsDisplayable, ListSelectionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -4418626517605128694L;
  JajukTable jtable;
  JPanel jpControl;
  JajukToggleButton jtbEditable;
  private JComboBox<String> jcbProperty;
  private JTextField jtfValue;
  /** Table model. */
  JajukTableModel model;
  /** Currently applied filter. */
  String sAppliedFilter = "";
  /** Currently applied criteria. */
  String sAppliedCriteria;
  /** Do search panel need a search. */
  private boolean bNeedSearch = false;
  /** Default time in ms before launching a search automatically. */
  private static final int WAIT_TIME = 200;
  /** Date last key pressed. */
  private long lDateTyped;
  /** Editable table configuration name, must be overwritten by child classes. */
  String editableConf;
  /** Columns to show table configuration name, must be overwritten by child classes. */
  String columnsConf;
  JMenuItem jmiDelete;
  JMenuItem jmiBookmark;
  JMenuItem jmiProperties;
  JMenuItem jmiFileCopyURL;
  PreferencesJMenu pjmTracks;
  /** The table/tree sync toggle button. */
  JajukToggleButton jtbSync;
  private volatile boolean bStopThread = false;
  /** Launches a thread used to perform dynamic filtering when user is typing. */
  private Thread filteringThread = new Thread("Dynamic user input filtering thread") {
    @Override
    public void run() {
      while (!bStopThread) {
        try {
          Thread.sleep(200);
        } catch (InterruptedException ie) {
          Log.error(ie);
        }
        try {
          if (bNeedSearch && (System.currentTimeMillis() - lDateTyped >= WAIT_TIME)) {
            sAppliedFilter = jtfValue.getText();
            sAppliedCriteria = getApplyCriteria();
            applyFilter(sAppliedCriteria, sAppliedFilter);
            bNeedSearch = false;
          }
        } catch (Exception ie) {
          Log.error(ie);
        }
      }
    }
  };

  /**
   * Gets the apply criteria.
   *
   * @return Applied criteria
   */
  private String getApplyCriteria() {
    int indexCombo = jcbProperty.getSelectedIndex();
    if (indexCombo == 0) { // first criteria is special: any
      sAppliedCriteria = XML_ANY;
    } else { // otherwise, take criteria from model
      sAppliedCriteria = model.getIdentifier(indexCombo);
    }
    return sAppliedCriteria;
  }

  /**
   * Code used in child class SwingWorker for long delay computations (used in
   * initUI()).
   *
   * @return the object
   */
  @Override
  public Object longCall() {
    model = populateTable();
    return null;
  }

  /**
   * Code used in child class SwingWorker for display computations (used in
   * initUI()).
   */
  @Override
  public void shortCall(Object in) {
    // Add generic menus
    JMenuItem jmiPlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
    jmiPlay.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiPlay);
    JMenuItem jmiFrontPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_FRONT_SELECTION));
    jmiFrontPush.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiFrontPush);
    JMenuItem jmiPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_SELECTION));
    jmiPush.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiPush);
    JMenuItem jmiPlayRepeat = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_REPEAT_SELECTION));
    jmiPlayRepeat.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiPlayRepeat);
    JMenuItem jmiPlayShuffle = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SHUFFLE_SELECTION));
    jmiPlayShuffle.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiPlayShuffle);
    jtable.getMenu().addSeparator();
    jmiDelete = new JMenuItem(ActionManager.getAction(JajukActions.DELETE));
    jmiDelete.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jtable.getMenu().add(jmiDelete);
    jmiFileCopyURL = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiFileCopyURL.putClientProperty(Const.DETAIL_CONTENT, jtable.getSelection());
    jtable.getMenu().add(jmiFileCopyURL);
    jmiBookmark = new JMenuItem(ActionManager.getAction(JajukActions.BOOKMARK_SELECTION));
    jmiBookmark.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    jmiProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    pjmTracks = new PreferencesJMenu(jtable.getSelection());
    // Set a default behavior for double click or click on the play column
    jtable.setCommand(nbClicks -> {
      // Ignore event if several rows are selected
      if (jtable.getSelectedColumnCount() != 1) {
        return;
      }
      int iSelectedCol = jtable.getSelectedColumn();
      // Convert column selection as columns may have been moved
      iSelectedCol = jtable.convertColumnIndexToModel(iSelectedCol);
      // We launch the selection :
      // - In any case if user clicked on the play column (column 0)
      // - Or in case of double click on any column when table is not editable
      if (iSelectedCol == 0 || // click on play icon
          // double click on any column and edition state false
          (nbClicks == 2 && !jtbEditable.isSelected())) {
        Item item = model.getItemAt(jtable.convertRowIndexToModel(jtable.getSelectedRow()));
        List<File> files = UtilFeatures.getPlayableFiles(item);
        if (files.size() > 0) {
          // launch it
          QueueModel.push(
              UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(files),
                  Conf.getBoolean(Const.CONF_STATE_REPEAT), true),
              Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
        } else {
          Messages.showErrorMessage(10);
        }
      }
    });
    // Control panel
    jpControl = new JPanel();
    jpControl.setBorder(BorderFactory.createEtchedBorder());
    // Create the sync toggle button and restore its state
    jtbSync = new JajukToggleButton(ActionManager.getAction(JajukActions.SYNC_TREE_TABLE));
    jtbSync.putClientProperty(Const.DETAIL_VIEW, getID());
    jtbSync.setSelected(Conf.getBoolean(Const.CONF_SYNC_TABLE_TREE + "." + getID()));
    createGenericGUI(jtbSync);
    ColorHighlighter colorHighlighter = new ColorHighlighter(new PlayHighlighterPredicate(jtable),
        Color.ORANGE, null);
    jtable.addHighlighter(colorHighlighter);
    // refresh columns conf in case of some attributes been removed
    // or added before view instantiation
    Properties properties = ObservationManager
        .getDetailsLastOccurence(JajukEvents.CUSTOM_PROPERTIES_ADD);
    JajukEvent event = new JajukEvent(JajukEvents.CUSTOM_PROPERTIES_ADD, properties);
    update(event);
    initTable(); // perform type-specific init
    // Register keystrokes
    setKeystrokes();
  }

  /**
   * Generic part of the panel
   * @param component the component to display before filter
   */
  void createGenericGUI(JComponent component) {
    jtbEditable = new JajukToggleButton(IconLoader.getIcon(JajukIcons.EDIT));
    jtbEditable.setToolTipText(Messages.getString("AbstractTableView.11"));
    jtbEditable.addActionListener(this);
    JLabel jlFilter = new JLabel(Messages.getString("AbstractTableView.0"));
    // properties combo box, fill with columns names expect ID
    jcbProperty = new JComboBox<>();
    // "any" criteria
    jcbProperty.addItem(Messages.getString("AbstractTableView.8"));
    for (int i = 1; i < model.getColumnCount(); i++) {
      // Others columns except ID
      jcbProperty.addItem(model.getColumnName(i));
    }
    jcbProperty.setToolTipText(Messages.getString("AbstractTableView.1"));
    jcbProperty.addItemListener(this);
    JLabel jlEquals = new JLabel(Messages.getString("AbstractTableView.7"));
    jtfValue = new JTextField();
    jtfValue.setFont(FontManager.getInstance().getFont(JajukFont.SEARCHBOX));
    jtfValue.setMargin(new Insets(0, 3, 0, 0));
    jtfValue.setForeground(new Color(172, 172, 172));
    jtfValue.addKeyListener(new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        bNeedSearch = true;
        lDateTyped = System.currentTimeMillis();
        // Start filtering thread
        if (!filteringThread.isAlive()) {
          filteringThread.start();
        }
      }
    });
    // Add a focus listener to select all the text and ease previous text cleanup
    jtfValue.addFocusListener(new FocusListener() {
      @Override
      public void focusLost(FocusEvent e) {
        jtfValue.setCaretPosition(jtfValue.getText().length());
      }

      @Override
      public void focusGained(FocusEvent e) {
        jtfValue.selectAll();
      }
    });
    jtfValue.setToolTipText(Messages.getString("AbstractTableView.3"));
    jpControl.setLayout(new MigLayout("insets 5", "[][][grow,gp 70][grow]"));
    jpControl.add(jtbEditable, "gapleft 5");
    jpControl.add(component, "gapright 15");
    jpControl.add(jlFilter, "split 2");
    jpControl.add(jcbProperty, "grow,gapright 15");
    jpControl.add(jlEquals, "split 2");
    jpControl.add(jtfValue, "grow,gapright 2");
    setCellEditors();
    JScrollPane jsp = new JScrollPane(jtable);
    setLayout(new MigLayout("ins 0", "[grow]", "[][grow]"));
    add(jpControl, "wrap,grow");
    add(jsp, "grow");
    jtable.setTransferHandler(new TableTransferHandler(jtable));
    jtable.showColumns(jtable.getColumnsConf());
    applyFilter(null, null);
    jtable.getSelectionModel().addListSelectionListener(this);
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // Register keystrokes
    setKeystrokes();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<>();
    eventSubjectSet.add(JajukEvents.DEVICE_MOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_UNMOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.CUSTOM_PROPERTIES_ADD);
    eventSubjectSet.add(JajukEvents.CUSTOM_PROPERTIES_REMOVE);
    eventSubjectSet.add(JajukEvents.RATE_CHANGED);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    eventSubjectSet.add(JajukEvents.VIEW_REFRESH_REQUEST);
    eventSubjectSet.add(JajukEvents.TREE_SELECTION_CHANGED);
    return eventSubjectSet;
  }

  /**
   * Apply a filter, to be implemented by files and tracks tables, alter the
   * model.
   */
  public void applyFilter(final String sPropertyName, final String sPropertyValue) {
    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
      @Override
      public Void doInBackground() {
        model.removeTableModelListener(AbstractTableView.this);
        model.populateModel(sPropertyName, sPropertyValue, jtable.getColumnsConf());
        model.addTableModelListener(AbstractTableView.this);
        return null;
      }

      @Override
      public void done() {
        int[] selection = jtable.getSelectedRows();
        // Force table repaint (for instance for rating stars update)
        model.fireTableDataChanged();
        // Restore selection (even if rows content may have change) if is is not now out of bound
        boolean outOfBounds = false;
        for (int index : selection) {
          if (index >= model.getRowCount()) {
            outOfBounds = true;
            break;
          }
        }
        if (!outOfBounds) {
          jtable.setSelectedRows(selection);
        }
        UtilGUI.stopWaiting();
      }
    };
    UtilGUI.waiting();
    sw.execute();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(() -> {
      try {
        jtable.setAcceptColumnsEvents(false); // flag reloading to avoid wrong
        // column
        // events
        JajukEvents subject = event.getSubject();
        if (JajukEvents.DEVICE_MOUNT.equals(subject)
            || JajukEvents.DEVICE_UNMOUNT.equals(subject)) {
          jtable.clearSelection();
          // force filter to refresh
          applyFilter(sAppliedCriteria, sAppliedFilter);
        } else if (JajukEvents.TREE_SELECTION_CHANGED.equals(subject)) {
          // Check if the sync tree table option is set for this tree
          if (jtbSync.isSelected()) {
            // Consume only events from the same perspective and different view
            // (for table/tree synchronization)
            Properties details = event.getDetails();
            if (details != null) {
              String sourcePerspective = details.getProperty(Const.DETAIL_PERSPECTIVE);
              String sourceView = details.getProperty(Const.DETAIL_VIEW);
              if (!(sourcePerspective.equals(getPerspective().getID()))
                  || sourceView.equals(getID())) {
                return;
              }
            }
            // Update model tree selection
            model.setTreeSelection((Set<Item>) details.get(Const.DETAIL_SELECTION));
            // force redisplay to apply the filter
            jtable.clearSelection();
            // force filter to refresh
            applyFilter(sAppliedCriteria, sAppliedFilter);
          }
        } else if (JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
          // force redisplay to apply the filter
          jtable.clearSelection();
          // force filter to refresh
          applyFilter(sAppliedCriteria, sAppliedFilter);
        } else if (JajukEvents.DEVICE_REFRESH.equals(subject)) {
          // force filter to refresh
          applyFilter(sAppliedCriteria, sAppliedFilter);
        } else if (JajukEvents.VIEW_REFRESH_REQUEST.equals(subject)) {
          // force filter to refresh if the events has been triggered by the
          // table itself after a column change
          JTable table = (JTable) event.getDetails().get(Const.DETAIL_CONTENT);
          if (table.equals(jtable)) {
            applyFilter(sAppliedCriteria, sAppliedFilter);
          }
        } else if (JajukEvents.RATE_CHANGED.equals(subject)) {
          // Keep current selection and nb of rows
          int[] selection = jtable.getSelectedRows();
          // force filter to refresh
          applyFilter(sAppliedCriteria, sAppliedFilter);
          jtable.setSelectedRows(selection);
        } else if (JajukEvents.CUSTOM_PROPERTIES_ADD.equals(subject)) {
          Properties properties = event.getDetails();
          if (properties == null) {
            // can be null at view populate
            return;
          }
          model = populateTable();
          model.addTableModelListener(AbstractTableView.this);
          jtable.setModel(model);
          setCellEditors();
          // add new item in configuration columns
          jtable.addColumnIntoConf((String) properties.get(Const.DETAIL_CONTENT));
          jtable.showColumns(jtable.getColumnsConf());
          applyFilter(sAppliedCriteria, sAppliedFilter);
          jcbProperty.addItem((String) properties.get(Const.DETAIL_CONTENT));
        } else if (JajukEvents.CUSTOM_PROPERTIES_REMOVE.equals(subject)) {
          Properties properties = event.getDetails();
          if (properties == null) { // can be null at view
            // populate
            return;
          }
          // remove item from configuration columns
          model = populateTable();// create a new model
          model.addTableModelListener(AbstractTableView.this);
          jtable.setModel(model);
          setCellEditors();
          jtable.addColumnIntoConf((String) properties.get(Const.DETAIL_CONTENT));
          jtable.showColumns(jtable.getColumnsConf());
          applyFilter(sAppliedCriteria, sAppliedFilter);
          jcbProperty.removeItem(properties.get(Const.DETAIL_CONTENT));
        }
      } catch (Exception e) {
        Log.error(e);
      } finally {
        jtable.setAcceptColumnsEvents(true); // make sure to remove this flag
      }
    });
  }

  /**
   * Add keystroke support.
   */
  private void setKeystrokes() {
    jtable.putClientProperty(Const.DETAIL_SELECTION, jtable.getSelection());
    InputMap inputMap = jtable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap actionMap = jtable.getActionMap();
    // Delete
    Action action = ActionManager.getAction(JajukActions.DELETE);
    inputMap.put(KeyStroke.getKeyStroke("DELETE"), "delete");
    actionMap.put("delete", action);
    // Properties ALT/ENTER
    action = ActionManager.getAction(JajukActions.SHOW_PROPERTIES);
    inputMap.put(KeyStroke.getKeyStroke("alt ENTER"), "properties");
    actionMap.put("properties", action);
  }

  /**
   * Fill the table.
   *
   * @return the jajuk table model
   */
  abstract JajukTableModel populateTable();

  /**
   * Sets the cell editors.
   */
  private void setCellEditors() {
    for (TableColumn tc : ((DefaultTableColumnModelExt) jtable.getColumnModel()).getColumns(true)) {
      TableColumnExt col = (TableColumnExt) tc;
      String sIdentifier = model.getIdentifier(col.getModelIndex());
      // create a combo box for genres, note that we can't add new
      // genres dynamically
      if (Const.XML_GENRE.equals(sIdentifier)) {
        JComboBox<String> jcb = new JComboBox<>(GenreManager.getInstance().getGenresList());
        jcb.setEditable(true);
        AutoCompleteDecorator.decorate(jcb);
        col.setCellEditor(new ComboBoxCellEditor(jcb));
        col.setSortable(true);
      }
      // create a combo box for artists, note that we can't add new
      // artists dynamically
      else if (Const.XML_ARTIST.equals(sIdentifier)) {
        JComboBox<String> jcb = new JComboBox<>(ArtistManager.getArtistsList());
        jcb.setEditable(true);
        AutoCompleteDecorator.decorate(jcb);
        col.setCellEditor(new ComboBoxCellEditor(jcb));
      }
      // Same for for album-artists
      else if (Const.XML_ALBUM_ARTIST.equals(sIdentifier)) {
        JComboBox<String> jcb = new JComboBox<>(AlbumArtistManager.getAlbumArtistsList());
        jcb.setEditable(true);
        AutoCompleteDecorator.decorate(jcb);
        col.setCellEditor(new ComboBoxCellEditor(jcb));
      }
      // create a button for playing
      else if (Const.XML_PLAY.equals(sIdentifier)) {
        col.setMinWidth(PLAY_COLUMN_SIZE);
        col.setMaxWidth(PLAY_COLUMN_SIZE);
      } else if (Const.XML_TRACK_RATE.equals(sIdentifier)) {
        col.setMinWidth(RATE_COLUMN_SIZE);
        col.setMaxWidth(RATE_COLUMN_SIZE);
      }
    }
  }

  /**
   * Detect property change.
   */
  @Override
  public void itemStateChanged(ItemEvent ie) {
    if (ie.getSource() == jcbProperty) {
      sAppliedFilter = jtfValue.getText();
      sAppliedCriteria = getApplyCriteria();
      applyFilter(sAppliedCriteria, sAppliedFilter);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @seejavax.swing.event.TableModelListener#tableChanged(javax.swing.event. TableModelEvent)
   */
  @Override
  public void tableChanged(TableModelEvent e) {
    // Check the table change event has not been generated by a
    // fireModelDataChange call
    if (e.getColumn() < 0) {
      return;
    }
    String sKey = model.getIdentifier(e.getColumn());
    Object oValue = model.getValueAt(e.getFirstRow(), e.getColumn());
    /* can be Boolean or String */
    Item item = model.getItemAt(e.getFirstRow());
    try {
      // file filter used by physical table view to change only the
      // file, not all files associated with the track
      Set<File> filter = null;
      if (item instanceof File) {
        filter = new HashSet<>();
        filter.add((File) item);
      }
      Item itemNew = ItemManager.changeItem(item, sKey, oValue, filter);
      model.setItemAt(e.getFirstRow(), itemNew); // update model
      // user message
      InformationJPanel.getInstance().setMessage(
          Messages.getString("PropertiesWizard.8") + ": " + Messages.getHumanPropertyName(sKey),
          InformationJPanel.MessageType.INFORMATIVE);
      // Require refresh of all tables
      Properties properties = new Properties();
      properties.put(Const.DETAIL_ORIGIN, AbstractTableView.this);
      // No real device change if Webradio view
      if (!(this instanceof WebRadioView)) {
        ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH, properties));
      }
    } catch (NoneAccessibleFileException | CannotRenameException none) {
      Messages.showErrorMessage(none.getCode());
      ((JajukTableModel) jtable.getModel()).undo(e.getFirstRow(), e.getColumn());
    } catch (JajukException je) {
      Log.error("104", je);
      Messages.showErrorMessage(104, je.getMessage());
      ((JajukTableModel) jtable.getModel()).undo(e.getFirstRow(), e.getColumn());
    }
  }

  /*
  * (non-Javadoc)
  *
  * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
  */
  @Override
  public void actionPerformed(final ActionEvent e) {
    // Editable state
    if (e.getSource() == jtbEditable) {
      Conf.setProperty(editableConf, Boolean.toString(jtbEditable.isSelected()));
      model.setEditable(jtbEditable.isSelected());
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.views.ViewAdapter#cleanup()
   */
  @Override
  public void cleanup() {
    // stop the thread that is waiting for input
    if (filteringThread != null) {
      bStopThread = true;
      try {
        filteringThread.join();
        filteringThread = null;
      } catch (InterruptedException e) {
        Log.error(e);
      }
    }
    super.cleanup();
  }

  /**
   * Called when table selection changed.
   *
   * @param e the List selection event
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      // leave during normal refresh
      return;
    }
    // Ignore event if the model is refreshing
    if (((JajukTableModel) jtable.getModel()).isRefreshing()) {
      return;
    }
    // We absolutely need to perform the actual treatment in the next EDT call because otherwise,
    // the selection is wrong because the selection event is catch first here and after in the JajukTable
    // valueChanged() that performs the actual selection computation. Doing this invokeLater ensure to
    // serialize the event treatment in the correct order.
    SwingUtilities.invokeLater(() -> {
      // Call view specific behavior on selection change
      onSelectionChange();
      // Hide the copy url if several items selection. Do not simply disable them
      // as the getMenu() method enable all menu items
      jmiFileCopyURL.setVisible(jtable.getSelectedRowCount() < 2);
      // Compute Information view message
      if (AbstractTableView.this instanceof TracksTableView) {
        int rows = jtable.getSelection().size();
        String sbOut = rows +
                Messages.getString("TracksTreeView.31");
        InformationJPanel.getInstance().setSelection(sbOut);
      } else if (AbstractTableView.this instanceof FilesTableView) {
        // Compute recursive selection size, nb of items...
        long lSize = 0L;
        for (Item item : jtable.getSelection()) {
          if (item instanceof File) {
            lSize += ((File) item).getSize();
          }
        }
        int items = jtable.getSelection().size();
        lSize /= 1048576; // set size in MB
        StringBuilder sbOut = new StringBuilder().append(items).append(
            Messages.getString("FilesTreeView.52"));
        if (lSize > 1024) { // more than 1024 MB -> in GB
          sbOut.append(lSize / 1024).append('.').append(lSize % 1024)
              .append(Messages.getString("FilesTreeView.53"));
        } else {
          sbOut.append(lSize).append(Messages.getString("FilesTreeView.54"));
        }
        InformationJPanel.getInstance().setSelection(sbOut.toString());
      }
      // Refresh the preference menu according to the selection
      // (Useless for WebRadioView)
      if (!(AbstractTableView.this instanceof WebRadioView)) {
        pjmTracks.resetUI(jtable.getSelection());
      }
    });
  }

  /**
   * Callback method called on table selection change.
   */
  void onSelectionChange() {
    // Do nothing by default
  }

  /**
  * Table initialization after table display.
  * Default implementation for table initialization :
  * update editable button state.
  *
  */
  void initTable() {
    jtbEditable.setSelected(Conf.getBoolean(editableConf));
    // Sort by name by default.
    jtable.setSortOrder(1, SortOrder.ASCENDING);
  }
}
