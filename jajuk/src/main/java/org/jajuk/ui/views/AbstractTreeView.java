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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jajuk.base.Item;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.PreferencesJMenu;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXTree;

/**
 * An abstract files or tracks tree view. Contains common methods
 */
public abstract class AbstractTreeView extends ViewAdapter {

  private static final long serialVersionUID = 8330315957562739918L;

  /** The tree scrollpane */
  JScrollPane jspTree;

  /** The phyical tree */
  JXTree jtree;

  /** Current selection */
  TreePath[] paths;

  /** Resursive items selection */
  Set<Item> selectedRecursively = new HashSet<Item>(100);

  /** Items selection */
  List<Item> alSelected = new ArrayList<Item>(100);

  /** Top tree node */
  DefaultMutableTreeNode top;

  javax.swing.JPopupMenu jmenu;

  JMenuItem jmiPlay;

  JMenuItem jmiPush;

  JMenuItem jmiFrontPush;

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

  JMenuItem jmiCopyURL;

  /** Jtree scroller position* */
  int pos;

  /** Preference menu */
  PreferencesJMenu pjmTracks;

  /**
   * Used to differentiate user action tree collapse from code tree collapse
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
    jmiReport.putClientProperty(Const.DETAIL_ORIGIN, XML_STYLE);
    jmiReport.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiProperties = new JMenuItem(ActionManager.getAction(JajukActions.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiCopyURL = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiCopyURL.putClientProperty(Const.DETAIL_CONTENT, alSelected);
    pjmTracks = new PreferencesJMenu(alSelected);
  }

  abstract void populateTree();

  abstract void expand();

  /**
   * Add keystroke support on the tree
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
          try {
            get();
          } catch (InterruptedException e) {
            Log.error(e);
          } catch (ExecutionException e) {
            Log.error(e);
          }
          SwingUtilities.updateComponentTreeUI(jtree);
          bAutoCollapse = true;
          expand();
          bAutoCollapse = false;
          // Reset last position in tree
          // The scrollbar must be set after current EDT work to be effective,
          // so queue it
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              if (jspTree != null) {
                jspTree.getVerticalScrollBar().setValue(pos);
              }
            }
          });

        }
      };
      sw.execute();
      // Make sure to refresh cells (useful to remove highlighters for ie)
      repaint();
    }
  }

}
