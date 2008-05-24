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

import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jajuk.base.Item;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jdesktop.swingx.JXTree;

/**
 * An abstract files or tracks tree view. Contains common methods
 */
public abstract class AbstractTreeView extends ViewAdapter {

  /** The tree scrollpane */
  JScrollPane jspTree;

  /** The phyical tree */
  JXTree jtree;

  /** Current selection */
  TreePath[] paths;

  /** Concurrency locker * */
  volatile short[] lock = new short[0];

  /** Resursive items selection */
  HashSet<Item> selectedRecursively = new HashSet<Item>(100);

  /** Items selection */
  ArrayList<Item> alSelected = new ArrayList<Item>(100);

  /** Top tree node */
  DefaultMutableTreeNode top;

  javax.swing.JPopupMenu jmenu;

  JMenuItem jmiPlay;

  JMenuItem jmiPush;

  JMenuItem jmiPlayShuffle;

  JMenuItem jmiPlayRepeat;

  JMenuItem jmiCut;

  JMenuItem jmiCopy;

  JMenuItem jmiPaste;

  JMenuItem jmiRename;

  JMenuItem jmiDelete;

  JMenuItem jmiNewFolder;

  JMenuItem jmiAddFavorite;

  JMenuItem jmiReport;

  JMenuItem jmiProperties;

  JMenuItem jmiCDDBWizard;

  /** Jtree scroller position* */
  int pos;
  
  /**
   * Used to differentiate user action tree collapse from code tree colapse*
   */
  boolean bAutoCollapse = false;

  protected JTree createTree() {
    jtree = new JXTree(top);
    jtree.putClientProperty("JTree.lineStyle", "Angled");
    jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    setKeystrokes();
    return jtree;
  }

  public void initUI() {
    jmiPlay = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SELECTION));
    jmiPlay.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiPush = new JMenuItem(ActionManager.getAction(JajukActions.PUSH_SELECTION));
    jmiPush.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiPlayShuffle = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_SHUFFLE_SELECTION));
    jmiPlayShuffle.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiPlayRepeat = new JMenuItem(ActionManager.getAction(JajukActions.PLAY_REPEAT_SELECTION));
    jmiPlayRepeat.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiCut = new JMenuItem(ActionManager.getAction(JajukActions.CUT));
    jmiCut.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiCopy = new JMenuItem(ActionManager.getAction(JajukActions.COPY));
    jmiCopy.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiPaste = new JMenuItem(ActionManager.getAction(JajukActions.PASTE));
    jmiPaste.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiRename = new JMenuItem(ActionManager.getAction(JajukActions.RENAME));
    jmiRename.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiDelete = new JMenuItem(ActionManager.getAction(JajukActions.DELETE));
    jmiDelete.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiNewFolder = new JMenuItem(ActionManager.getAction(JajukActions.NEW_FOLDER));
    jmiNewFolder.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiAddFavorite = new JMenuItem(ActionManager.getAction(JajukActions.BOOKMARK_SELECTION));
    jmiAddFavorite.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiCDDBWizard = new JMenuItem(ActionManager.getAction(JajukActions.CDDB_SELECTION));
    jmiCDDBWizard.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiReport = new JMenuItem(ActionManager.getAction(JajukActions.CREATE_REPORT));
    // Add custom data to this component in order to allow the ReportAction
    // to be able to get it
    jmiReport.putClientProperty(DETAIL_ORIGIN, XML_STYLE);
    jmiReport.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(DETAIL_SELECTION, alSelected);
  }

  abstract void populateTree();

  abstract void expand();
  
  /**
   * Add keystroke support on the tree
   */
  private void setKeystrokes() {
    jtree.putClientProperty(DETAIL_SELECTION, alSelected);
    InputMap inputMap = jtree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    ActionMap actionMap = jtree.getActionMap();

    // Delete
    Action action = ActionManager.getAction(JajukActions.DELETE);
    inputMap.put(KeyStroke.getKeyStroke("DELETE"), "delete");
    actionMap.put("delete", action);
    // Ctrl C
    action = ActionManager.getAction(JajukActions.COPY);
    inputMap.put(KeyStroke.getKeyStroke("ctrl C"), "copy");
    actionMap.put("copy", action);
    // Ctrl V
    action = ActionManager.getAction(JajukActions.PASTE);
    inputMap.put(KeyStroke.getKeyStroke("ctrl V"), "paste");
    actionMap.put("paste", action);
  }

  public void update(Event event) {
    final JajukEvents subject = event.getSubject();
    if (subject.equals(JajukEvents.EVENT_DEVICE_MOUNT)
        || subject.equals(JajukEvents.EVENT_DEVICE_UNMOUNT)
        || subject.equals(JajukEvents.EVENT_DEVICE_REFRESH)) {
      SwingWorker sw = new SwingWorker() {
        @Override
        public Object construct() {
          pos = jspTree.getVerticalScrollBar().getValue();
          populateTree();
          return null;
        }

        @Override
        public void finished() {
          SwingUtilities.updateComponentTreeUI(jtree);
          bAutoCollapse = true;
          expand();
          bAutoCollapse = false;
          // Reset last position in tree
          // The scrollbar must be set after current EDT work to be effective,
          // so queue it
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              jspTree.getVerticalScrollBar().setValue(pos);
            }
          });

        }
      };
      sw.start();
      // Make sure to refresh cells (useful to remove highlighters for ie)
      repaint();
    }
  }

}
