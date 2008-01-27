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

import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jajuk.base.Item;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
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

  protected JTree createTree() {
    jtree = new JXTree(top);
    jtree.putClientProperty("JTree.lineStyle", "Angled");
    jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    return jtree;
  }
  
  public void initUI(){
    jmiPlay = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_SELECTION));
    jmiPlay.putClientProperty(DETAIL_SELECTION,alSelected);
    jmiPush = new JMenuItem(ActionManager.getAction(JajukAction.PUSH_SELECTION));
    jmiPush.putClientProperty(DETAIL_SELECTION,alSelected);
    jmiPlayShuffle = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_SHUFFLE_SELECTION));
    jmiPlayShuffle.putClientProperty(DETAIL_SELECTION,alSelected);
    jmiPlayRepeat = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_REPEAT_SELECTION));
    jmiPlayRepeat.putClientProperty(DETAIL_SELECTION,alSelected);
    jmiCut = new JMenuItem(ActionManager.getAction(JajukAction.CUT));
    jmiCut.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiCopy = new JMenuItem(ActionManager.getAction(JajukAction.COPY));
    jmiCopy.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiPaste = new JMenuItem(ActionManager.getAction(JajukAction.PASTE));
    jmiPaste.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiRename = new JMenuItem(ActionManager.getAction(JajukAction.RENAME));
    jmiRename.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiDelete = new JMenuItem(ActionManager.getAction(JajukAction.DELETE));
    jmiDelete.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiNewFolder = new JMenuItem(ActionManager.getAction(JajukAction.NEW_FOLDER));
    jmiNewFolder.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiAddFavorite = new JMenuItem(ActionManager.getAction(JajukAction.BOOKMARK_SELECTION));
    jmiAddFavorite.putClientProperty(DETAIL_SELECTION,alSelected);
    jmiCDDBWizard = new JMenuItem(ActionManager.getAction(JajukAction.CDDB_SELECTION));
    jmiCDDBWizard.putClientProperty(DETAIL_SELECTION,alSelected);
    jmiReport = new JMenuItem(ActionManager.getAction(JajukAction.CREATE_REPORT));
    // Add custom data to this component in order to allow the ReportAction
    // to be able to get it
    jmiReport.putClientProperty(DETAIL_ORIGIN, XML_STYLE);
    jmiReport.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(DETAIL_SELECTION, alSelected);
  }

}
