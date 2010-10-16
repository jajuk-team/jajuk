/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jajuk.base.Item;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.LazyLoadingTreeExpander;
import org.jajuk.ui.helpers.PreferencesJMenu;
import org.jajuk.ui.widgets.JajukToggleButton;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jdesktop.swingx.JXTree;

/**
 * An abstract files or tracks tree view. Contains common methods
 */
public abstract class AbstractTreeView extends ViewAdapter {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 8330315957562739918L;

  /** The tree scrollpane. */
  JScrollPane jspTree;

  /** The phyical tree. */
  JXTree jtree;

  /** The table/tree sync toggle button */
  JajukToggleButton jtbSync;

  /** the collapse all button */
  JButton jbCollapseAll;

  /** Current selection. */
  TreePath[] paths;

  /** Resursive items selection. */
  Set<Item> selectedRecursively = new HashSet<Item>(100);

  /** Items selection. */
  List<Item> alSelected = new ArrayList<Item>(100);

  /** Top tree node. */
  DefaultMutableTreeNode top;

  /** DOCUMENT_ME. */
  javax.swing.JPopupMenu jmenu;

  /** DOCUMENT_ME. */
  JMenuItem jmiPlay;

  /** DOCUMENT_ME. */
  JMenuItem jmiPush;

  /** DOCUMENT_ME. */
  JMenuItem jmiFrontPush;

  /** DOCUMENT_ME. */
  JMenuItem jmiPlayShuffle;

  /** DOCUMENT_ME. */
  JMenuItem jmiPlayRepeat;

  /** DOCUMENT_ME. */
  JMenuItem jmiCut;

  /** DOCUMENT_ME. */
  JMenuItem jmiCopy;

  /** DOCUMENT_ME. */
  JMenuItem jmiPaste;

  /** DOCUMENT_ME. */
  JMenuItem jmiRename;

  /** DOCUMENT_ME. */
  JMenuItem jmiDelete;

  /** DOCUMENT_ME. */
  JMenuItem jmiNewFolder;

  /** DOCUMENT_ME. */
  JMenuItem jmiAddFavorite;

  /** DOCUMENT_ME. */
  JMenuItem jmiReport;

  /** DOCUMENT_ME. */
  JMenuItem jmiProperties;

  /** DOCUMENT_ME. */
  JMenuItem jmiCDDBWizard;

  /** DOCUMENT_ME. */
  JMenuItem jmiCopyURL;

  /** Jtree scroller position*. */
  private int pos;

  /** Preference menu. */
  PreferencesJMenu pjmTracks;

  /** Used to differentiate user action tree collapse from code tree collapse. */
  boolean bManualAction = true;
  
  /** Used to differentiate tree/table sync due to internal events from users's ones. */
  boolean bInternalAction = false;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.DEVICE_MOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_UNMOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.CDDB_WIZARD);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    eventSubjectSet.add(JajukEvents.TABLE_SELECTION_CHANGED);
    return eventSubjectSet;
  }

  /**
   * Creates the tree. DOCUMENT_ME
   * 
   * @return the j tree
   */
  protected JTree createTree(boolean bLazy) {
    jtree = new JXTree(top);
    jtree.putClientProperty("JTree.lineStyle", "Angled");
    jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    setKeystrokes();

    // set the special controller for doing lazy loading if used for this View
    if (bLazy) {
      final LazyLoadingTreeExpander controller = new LazyLoadingTreeExpander(
          (DefaultTreeModel) jtree.getModel());
      jtree.addTreeWillExpandListener(controller);
    }

    return jtree;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.IView#initUI()
   */
  @Override
  public void initUI() {
    jmiPlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
    jmiPlay.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiFrontPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_FRONT_SELECTION));
    jmiFrontPush.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_SELECTION));
    jmiPush.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiPlayShuffle = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SHUFFLE_SELECTION));
    jmiPlayShuffle.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiPlayRepeat = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_REPEAT_SELECTION));
    jmiPlayRepeat.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiCut = new JMenuItem(ActionManager.getAction(JajukActions.CUT));
    jmiCut.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiCopy = new JMenuItem(ActionManager.getAction(JajukActions.COPY));
    jmiCopy.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiPaste = new JMenuItem(ActionManager.getAction(JajukActions.PASTE));
    jmiPaste.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiRename = new JMenuItem(ActionManager.getAction(JajukActions.RENAME));
    jmiRename.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiDelete = new JMenuItem(ActionManager.getAction(JajukActions.DELETE));
    jmiDelete.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiNewFolder = new JMenuItem(ActionManager.getAction(JajukActions.NEW_FOLDER));
    jmiNewFolder.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiAddFavorite = new JMenuItem(ActionManager.getAction(JajukActions.BOOKMARK_SELECTION));
    jmiAddFavorite.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiCDDBWizard = new JMenuItem(ActionManager.getAction(JajukActions.CDDB_SELECTION));
    jmiCDDBWizard.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiReport = new JMenuItem(ActionManager.getAction(JajukActions.CREATE_REPORT));
    // Add custom data to this component in order to allow the ReportAction
    // to be able to get it
    jmiReport.putClientProperty(Const.DETAIL_ORIGIN, XML_GENRE);
    jmiReport.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiCopyURL = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiCopyURL.putClientProperty(Const.DETAIL_CONTENT, alSelected);
    pjmTracks = new PreferencesJMenu(alSelected);

    // Create the sync toggle button and restore its state
    jtbSync = new JajukToggleButton(ActionManager.getAction(JajukActions.SYNC_TREE_TABLE));
    jtbSync.putClientProperty(Const.DETAIL_VIEW, getID());
    jtbSync.setSelected(Conf.getBoolean(Const.CONF_SYNC_TABLE_TREE + "." + getID()));

    // Create the collapse all button, no need to a dedicated Action here as it
    // is used only in this class
    jbCollapseAll = new JButton(IconLoader.getIcon(JajukIcons.REMOVE));
    jbCollapseAll.setToolTipText(Messages.getString("AbstractTreeView.0"));
    jbCollapseAll.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        jtree.collapseAll();
        // better to show at least the first level of items
        jtree.expandRow(0);
        jtree.setSelectionInterval(0, 0);
      }
    });

  }

  /**
   * Populate tree. DOCUMENT_ME
   */
  abstract void populateTree();

  /**
   * Expand. DOCUMENT_ME
   */
  abstract void expand();

  /**
   * Expand a given item
   */
  abstract void scrollTo(Item item);

  /**
   * Add keystroke support on the tree.
   */
  private void setKeystrokes() {
    jtree.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    InputMap inputMap = jtree.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap actionMap = jtree.getActionMap();

    // Delete
    Action action = ActionManager.getAction(JajukActions.DELETE);
    inputMap.put(KeyStroke.getKeyStroke("DELETE"), "delete");
    actionMap.put("delete", action);
    // Ctrl C
    action = ActionManager.getAction(JajukActions.COPY);
    inputMap.put(KeyStroke.getKeyStroke("ctrl C"), "copy");
    actionMap.put("copy", action);
    // Ctrl X
    action = ActionManager.getAction(JajukActions.CUT);
    inputMap.put(KeyStroke.getKeyStroke("ctrl X"), "cut");
    actionMap.put("cut", action);
    // Ctrl V
    action = ActionManager.getAction(JajukActions.PASTE);
    inputMap.put(KeyStroke.getKeyStroke("ctrl V"), "paste");
    actionMap.put("paste", action);
    // Properties ALT/ENTER
    action = ActionManager.getAction(JajukActions.SHOW_PROPERTIES);
    inputMap.put(KeyStroke.getKeyStroke("alt ENTER"), "properties");
    actionMap.put("properties", action);
    // Rename / F2
    action = ActionManager.getAction(JajukActions.RENAME);
    inputMap.put(KeyStroke.getKeyStroke("F2"), "rename");
    actionMap.put("rename", action);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  @Override
  public void update(JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.DEVICE_MOUNT) || subject.equals(JajukEvents.DEVICE_UNMOUNT)
        || subject.equals(JajukEvents.DEVICE_REFRESH)
        || subject.equals(JajukEvents.PARAMETERS_CHANGE)) {
      SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
        @Override
        public Void doInBackground() {
          if (jspTree != null) {
            pos = jspTree.getVerticalScrollBar().getValue();
          }
          populateTree();
          return null;
        }

        @Override
        public void done() {
          SwingUtilities.updateComponentTreeUI(jtree);
          bManualAction = false;
          expand();
          bManualAction = true;
          // Reset last position in tree
          // The scrollbar must be set after current EDT work to be effective,
          // so queue it
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              if (jspTree != null) {
                jspTree.getVerticalScrollBar().setValue(pos);
              }
            }
          });

        }
      };
      sw.execute();
    } else if (JajukEvents.TABLE_SELECTION_CHANGED.equals(subject)) {
      // Check if the sync tree table option is set for this tree
      if (Conf.getBoolean(Const.CONF_SYNC_TABLE_TREE + "." + getID())) {
        // Consume only events from the same perspective and different view
        // (for table/tree synchronization)
        Properties details = event.getDetails();
        if (details != null) {
          String sourcePerspective = details.getProperty(Const.DETAIL_PERSPECTIVE);
          IView sourceView = (IView) details.get(Const.DETAIL_VIEW);
          if (!(sourcePerspective.equals(getPerspective().getID()))
              //source view is null if the table is outside a view like CDDB dialog
              || sourceView == null
              // Same view ? ignore...
              || sourceView.getID().equals(getID())) {
            return;
          }

          @SuppressWarnings("unchecked")
          List<Item> selection = (List<Item>) details.get(Const.DETAIL_SELECTION);
          if (selection.size() == 0) {
            return;
          }
          // for tree/table consideration, we only expand the first found item, we don't
          // support
          // multiple expands (useful?)
          final Item item = selection.get(0);
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              scrollTo(item);
            }
          });
        }
      }
    }
  }
}
