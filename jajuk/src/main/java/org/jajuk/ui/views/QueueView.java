/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
 *  $Revision$
 */

package org.jajuk.ui.views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Playlist;
import org.jajuk.base.SmartPlaylist;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.ILaunchCommand;
import org.jajuk.ui.helpers.IndexHighlighterPredicate;
import org.jajuk.ui.helpers.JajukTableModel;
import org.jajuk.ui.helpers.PlaylistEditorTransferHandler;
import org.jajuk.ui.helpers.PlaylistTableModel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukJToolbar;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.ui.widgets.JajukToggleButton;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceColorScheme;
import org.jvnet.substance.api.SubstanceSkin;

/**
 * Adapter for playlists editors *.
 */
public class QueueView extends PlaylistView {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -2851288035506442507L;

  /** DOCUMENT_ME. */
  private JScrollPane jsp;

  /** DOCUMENT_ME. */
  private JajukToggleButton jtbAutoScroll;

  /** Last scrolled-item *. */
  private StackItem lastScrolledItem;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  @Override
  public void initUI() {
    plf = new SmartPlaylist(Playlist.Type.QUEUE, Integer.toString(Playlist.Type.QUEUE.ordinal()),
        null, null);
    // Control panel
    jpEditorControl = new JPanel();
    jpEditorControl.setBorder(BorderFactory.createEtchedBorder());
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
    jlTitle = new JLabel(" [" + QueueModel.getQueue().size() + "]");

    jbClear = new JajukButton(IconLoader.getIcon(JajukIcons.CLEAR));
    jbClear.setToolTipText(Messages.getString("QueueView.1"));
    jbClear.addActionListener(this);

    jtbAutoScroll = new JajukToggleButton(IconLoader.getIcon(JajukIcons.AUTOSCROLL));
    jtbAutoScroll.setToolTipText(Messages.getString("QueueView.2"));
    jtbAutoScroll.setSelected(Conf.getBoolean(Const.CONF_AUTO_SCROLL));
    jtbAutoScroll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Conf.setProperty(Const.CONF_AUTO_SCROLL, Boolean.toString(jtbAutoScroll.isSelected()));
        if (jtbAutoScroll.isSelected()) {
          autoScroll();
        }
      }
    });

    JToolBar jtb = new JajukJToolbar();
    jtb.add(jbSave);
    jtb.add(jbRemove);
    jtb.add(jbAddShuffle);
    jtb.add(jbUp);
    jtb.add(jbDown);
    jtb.addSeparator();
    jtb.add(jbClear);

    // Add items
    jpEditorControl.setLayout(new MigLayout("insets 5", "[][grow][]"));
    jpEditorControl.add(jtb, "left,gapright 15::");
    jpEditorControl.add(jlTitle, "right,gapright 5");
    jpEditorControl.add(jtbAutoScroll, "right");
    editorModel = new PlaylistTableModel(true);
    editorTable = new JajukTable(editorModel, CONF_QUEUE_COLUMNS);
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
    setLayout(new BorderLayout());
    add(jpEditorControl, BorderLayout.NORTH);
    jsp = new JScrollPane(editorTable);
    jsp.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    add(jsp, BorderLayout.CENTER);
    // menu items
    jmiFilePlay = new JMenuItem(Messages.getString("TracksTableView.7"), IconLoader
        .getIcon(JajukIcons.PLAY_16X16));
    // We don't use regular action for the play because it has very special
    // behavior here in the queue view : it must go to selection without keeping
    // previous FIFO
    jmiFilePlay.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        goToSelection();
      }
    });
    initMenuItems();

    SubstanceSkin theme = SubstanceLookAndFeel.getCurrentSkin();
    SubstanceColorScheme scheme = theme.getMainActiveColorScheme();
    Color queueHighlighterColor = null;
    if (scheme.isDark()) {
      queueHighlighterColor = scheme.getUltraLightColor();
    } else {
      queueHighlighterColor = scheme.getLineColor();
    }
    ColorHighlighter colorHighlighter = new ColorHighlighter(new IndexHighlighterPredicate(),
        queueHighlighterColor, null);
    editorTable.addHighlighter(colorHighlighter);
    // register events
    ObservationManager.register(this);
    // Add key listener to enable row suppression using SUPR key
    editorTable.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        // The fact that a selection can be removed or not is
        // in the jbRemove state
        if (e.getKeyCode() == KeyEvent.VK_DELETE && jbRemove.isEnabled()) {
          removeSelection();
          // Refresh table
          refreshQueue();
        }
      }
    });
    // Add specific behavior on left click
    editorTable.setCommand(new ILaunchCommand() {
      public void launch(int nbClicks) {
        int iSelectedCol = editorTable.getSelectedColumn();
        // Convert column selection as columns may have been moved
        iSelectedCol = editorTable.convertColumnIndexToModel(iSelectedCol);
        // double click, launches selected track and all after
        if (nbClicks == 2
        // click on play icon
            || (nbClicks == 1 && iSelectedCol == 0)) {
          StackItem item = editorModel.getStackItem(editorTable.getSelectedRow());
          if (item.isPlanned()) {
            item.setPlanned(false);
            item.setRepeat(Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL));
            item.setUserLaunch(true);
            QueueModel.push(item, Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
          } else { // non planned items
            goToSelection();
          }
        }
      }
    });
    //  Note : don't add a ListSelectionListener here, see JajukTable code, 
    //  all the event code is centralized over there 
    editorTable.addListSelectionListener(this);
    // Register keystrokes over table
    super.setKeystrokes();
    // Force a first need refresh event
    update(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
  }

  /**
   * Go to selected row, do it asynchronously because FIFO.goTO() can freeze the
   * GUI
   */
  private void goToSelection() {
    new Thread("Queue Selection Thread") {
      @Override
      public void run() {
        try {
          QueueModel.goTo(editorTable.getSelectedRow());
          // remove selection for planned tracks
          ListSelectionModel lsm = editorTable.getSelectionModel();
          editorModel.setRefreshing(true);
          editorTable.getSelectionModel().removeSelectionInterval(lsm.getMinSelectionIndex(),
              lsm.getMaxSelectionIndex());
        } catch (Exception e) {
          Log.error(e);
        } finally {
          editorModel.setRefreshing(false);
        }
      }
    }.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.PlaylistView#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.QUEUE_NEED_REFRESH);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.DEVICE_MOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_UNMOUNT);
    eventSubjectSet.add(JajukEvents.CUSTOM_PROPERTIES_ADD);
    eventSubjectSet.add(JajukEvents.CUSTOM_PROPERTIES_REMOVE);
    eventSubjectSet.add(JajukEvents.VIEW_REFRESH_REQUEST);
    eventSubjectSet.add(JajukEvents.RATE_CHANGED);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("PlaylistFileItem.5");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          JajukEvents subject = event.getSubject();
          editorTable.setAcceptColumnsEvents(false); // flag reloading to avoid
          // wrong
          if (JajukEvents.QUEUE_NEED_REFRESH.equals(subject)
              || JajukEvents.DEVICE_REFRESH.equals(subject)
              || JajukEvents.DEVICE_MOUNT.equals(subject)
              || JajukEvents.DEVICE_UNMOUNT.equals(subject)
              || JajukEvents.RATE_CHANGED.equals(subject)
              || JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
            editorModel.getItems().clear();
            editorModel.getPlanned().clear();
            refreshQueue();
            // Only scroll if song actually changed, otherwise, any queue refresh
            // would scroll and annoy users
            if (Conf.getBoolean(CONF_AUTO_SCROLL) && QueueModel.getCurrentItem() != null
                && !QueueModel.getCurrentItem().equals(lastScrolledItem)) {
              autoScroll();
              lastScrolledItem = QueueModel.getCurrentItem();
            }
          } else if (JajukEvents.CUSTOM_PROPERTIES_ADD.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) {
              // can be null at view populate
              return;
            }
            // create a new model
            editorModel = new PlaylistTableModel(true);
            editorModel.populateModel(editorTable.getColumnsConf());
            editorTable.setModel(editorModel);
            setRenderers();
            editorTable.addColumnIntoConf((String) properties.get(Const.DETAIL_CONTENT));
            editorTable.showColumns(editorTable.getColumnsConf());

            editorModel.getItems().clear();
            editorModel.getPlanned().clear();
            refreshQueue();
          } else if (JajukEvents.CUSTOM_PROPERTIES_REMOVE.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) { // can be null at view
              // populate
              return;
            }
            editorModel = new PlaylistTableModel(true);
            editorModel.populateModel(editorTable.getColumnsConf());
            editorTable.setModel(editorModel);
            setRenderers();
            // remove item from configuration cols
            editorTable.removeColumnFromConf((String) properties.get(Const.DETAIL_CONTENT));
            editorTable.showColumns(editorTable.getColumnsConf());

            editorModel.getItems().clear();
            editorModel.getPlanned().clear();
            refreshQueue();
          } else if (JajukEvents.VIEW_REFRESH_REQUEST.equals(subject)) {
            // force filter to refresh if the events has been triggered by the
            // table itself after a column change
            JTable table = (JTable) event.getDetails().get(Const.DETAIL_CONTENT);
            if (table.equals(editorTable)) {
              editorModel.getItems().clear();
              editorModel.getPlanned().clear();
              refreshQueue();
            }
          }
        } catch (Exception e) {
          Log.error(e);
        } finally {
          editorTable.setAcceptColumnsEvents(true);
          // Update number of tracks remaining
          jlTitle.setText(" [" + QueueModel.getQueue().size() + "]");
        }
      }
    });

  }

  /**
   * Auto scroll to played track if option is enabled.
   */
  private void autoScroll() {

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {

        if (QueueModel.getQueueSize() > 0) {
          double index = QueueModel.getIndex();
          double size = QueueModel.getQueueSize() + QueueModel.getPlanned().size();
          double factor = (index / size);
          int value = (int) (factor * jsp.getVerticalScrollBar().getMaximum());

          // 'center' played track
          value -= (jsp.getVerticalScrollBar().getHeight() / 2) - (editorTable.getRowHeight() / 2);

          if (value < 0) {
            value = 0;
          }
          if (value >= jsp.getVerticalScrollBar().getMinimum()
              && value <= jsp.getVerticalScrollBar().getMaximum()) {
            jsp.getVerticalScrollBar().setValue(value);

          }
        }
      }
    });

  }

  /**
   * Refresh queue. DOCUMENT_ME
   */
  private void refreshQueue() {
    // when nothing is selected, set default button state
    if (editorTable.getSelectionModel().getMinSelectionIndex() == -1) {
      setDefaultButtonState();
    }
    editorModel.setItems(QueueModel.getQueue());
    editorModel.setPlanned(QueueModel.getPlanned());
    ((JajukTableModel) editorTable.getModel()).populateModel(editorTable.getColumnsConf());
    // save selection to avoid reseting selection the user is doing
    int[] rows = editorTable.getSelectedRows();

    try {
      editorModel.setRefreshing(true);
      // force table refresh
      editorModel.fireTableDataChanged();

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
  }

  /**
   * Set default button state.
   */
  private void setDefaultButtonState() {
    // set buttons
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
    // disable prepare party for queue playlist
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.PlaylistView#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent ae) {
    try {
      if (ae.getSource() == jbSave) {
        // special playlist, same behavior than a save as
        plf.saveAs();
        // notify playlist repository to refresh
        ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
      } else if (ae.getSource() == jbDown || ae.getSource() == jbUp
          || ae.getSource() == jmiFileDown || ae.getSource() == jmiFileUp) {
        int iRow = editorTable.getSelectedRow();
        if (iRow != -1) { // -1 means nothing is selected
          if (ae.getSource() == jbDown || ae.getSource() == jmiFileDown) {
            plf.down(iRow);
            if (iRow < editorTable.getModel().getRowCount() - 1) {
              // force immediate table refresh
              refreshQueue();
              editorTable.getSelectionModel().setSelectionInterval(iRow + 1, iRow + 1);
            }
          } else if (ae.getSource() == jbUp || ae.getSource() == jmiFileUp) {
            plf.up(iRow);
            if (iRow > 0) {
              // force immediate table refresh
              refreshQueue();
              editorTable.getSelectionModel().setSelectionInterval(iRow - 1, iRow - 1);
            }
          }
        }
      } else if (ae.getSource() == jbRemove || ae.getSource() == jmiFileRemove) {
        removeSelection();
        refreshQueue();
      } else if (ae.getSource() == jbAddShuffle) {
        int iRow = editorTable.getSelectedRow();
        if (iRow < 0
        // no row is selected, add to the end
            || iRow > QueueModel.getQueue().size()) {
          // row can be on planned track if user select a planned track and if
          // fifo is reduced after tracks have been played
          iRow = QueueModel.getQueue().size();
        }
        File file = FileManager.getInstance().getShuffleFile();
        List<File> files = new ArrayList<File>();
        files.add(file);
        QueueModel.insert(UtilFeatures.createStackItems(files, Conf
            .getBoolean(Const.CONF_STATE_REPEAT_ALL), true), iRow);
        refreshQueue();
      } else if (ae.getSource() == jbClear) {
        // Reset the FIFO
        QueueModel.reset(); // reinit all variables
        try {
          ActionManager.getAction(JajukActions.STOP_TRACK).perform(null);
          ObservationManager.notify(new JajukEvent(JajukEvents.ZERO));
        } catch (Exception e) {
          Log.error(e);
        }
      }
    } catch (Exception e2) {
      Log.error(e2);
    }
  }

  /**
   * Removes the selection. DOCUMENT_ME
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
   * Called when table selection changed.
   * 
   * @param e DOCUMENT_ME
   */
  @Override
  public void valueChanged(ListSelectionEvent e) {
    ListSelectionModel selection = (ListSelectionModel) e.getSource();
    if (!selection.isSelectionEmpty()) {
      updateSelection();
      updateInformationView(selectedFiles);
      // Refresh the preference menu according to the selection
      pjmFilesEditor.resetUI(editorTable.getSelection());
      int selectedRow = selection.getMaxSelectionIndex();
      // true if selected line is a planned track
      boolean bPlanned = false;
      if (selectedRow > editorModel.getItems().size() - 1) {
        // means it is a planned track
        bPlanned = true;
      }
      // -- now analyze each button --
      // Remove button
      if (bPlanned) {
        jbRemove.setEnabled(false);
        jmiFileRemove.setEnabled(false);
      } else {
        // check for current track case : we can't remove currently
        // played track
        jbRemove.setEnabled(!selectionContainsCurrentTrack(selection));
        jmiFileRemove.setEnabled(!selectionContainsCurrentTrack(selection));
      }

      // Add shuffle button
      // No adding for planned track
      jbAddShuffle.setEnabled(!bPlanned);

      // Up button
      if (selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()) {
        // check if several rows have been selected :
        // doesn't supported yet
        jbUp.setEnabled(false);
        jmiFileUp.setEnabled(false);
      } else {
        // still here ?
        if (bPlanned) {
          // No up/down buttons for planned tracks
          jbUp.setEnabled(false);
          jmiFileUp.setEnabled(false);
        } else { // normal item
          if (selection.getMinSelectionIndex() == 0) {
            // already at the top
            jbUp.setEnabled(false);
            jmiFileUp.setEnabled(false);
          } else {
            jbUp.setEnabled(true);
            jmiFileUp.setEnabled(true);
          }
        }
      }
      // Down button
      if (selection.getMinSelectionIndex() != selection.getMaxSelectionIndex()) {
        // check if several rows have been selected :
        // doesn't supported yet
        jbDown.setEnabled(false);
        jmiFileDown.setEnabled(false);
      } else { // yet here ?
        if (bPlanned) {
          // No up/down buttons for planned tracks
          jbDown.setEnabled(false);
          jmiFileDown.setEnabled(false);
        } else { // normal item
          if (selection.getMaxSelectionIndex() < editorModel.getItems().size() - 1) {
            // a normal item can't go in the planned items
            jbDown.setEnabled(true);
            jmiFileDown.setEnabled(true);
          } else {
            jbDown.setEnabled(false);
            jmiFileDown.setEnabled(false);
          }
        }
      }
    }
  }

  /**
   * Return whether a given row selection contains the current played track.
   * 
   * @param selection the selection
   * 
   * @return whether a given row selection contains the current played track
   */
  private boolean selectionContainsCurrentTrack(ListSelectionModel selection) {
    for (int i = selection.getMinSelectionIndex(); i <= selection.getMaxSelectionIndex(); i++) {
      if (QueueModel.getItem(i).equals(QueueModel.getCurrentItem())) {
        return true;
      }
    }
    return false;
  }

}