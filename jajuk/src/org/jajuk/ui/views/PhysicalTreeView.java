/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA. 
 * $Revision$
 */

package org.jajuk.ui.views;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.PlaylistFile;

/**
 * Physical tree view
 * 
 * @author bflorat @created 28 nov. 2003
 */
public class PhysicalTreeView extends ViewAdapter {

	/** Self instance */
	private static PhysicalTreeView ptv;

	/** The phyical tree */
	JTree jtree;
	
	/** Top tree node */
	DefaultMutableTreeNode top;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Physical tree view";
	}

	/** Return singleton */
	public static PhysicalTreeView getInstance() {
		if (ptv == null) {
			ptv = new PhysicalTreeView();
		}
		return ptv;
	}

	/** Constructor */
	public PhysicalTreeView() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		top = new DefaultMutableTreeNode("top");
		//top.add(new DefaultMutableTreeNode("toto"));
		jtree = new JTree(top);
		jtree.putClientProperty("JTree.lineStyle", "Angled");
		jtree.setRowHeight(25);
		jtree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		jtree.setCellRenderer(new DefaultTreeCellRenderer() {
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
				if (value instanceof File ){
					setIcon(new ImageIcon(ICON_BESTOF));
					//TODO set real icons
				}
				else if (value instanceof PlaylistFile){
					setIcon(new ImageIcon(ICON_CONTINUE_ON));
				}
				return this;
			}
		});
		DefaultTreeModel treeModel = new DefaultTreeModel(top);
		treeModel.addTreeModelListener(new TreeModelListener(){

			public void treeNodesChanged(TreeModelEvent e) {
				DefaultMutableTreeNode node;
        node = (DefaultMutableTreeNode)
                 (e.getTreePath().getLastPathComponent());

        try {
            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode)
                   (node.getChildAt(index));
        } catch (NullPointerException exc) {}
			
			}

			public void treeNodesInserted(TreeModelEvent e) {
			}

			public void treeNodesRemoved(TreeModelEvent e) {
			}

			public void treeStructureChanged(TreeModelEvent e) {
			}
		
		});
		add(new JScrollPane(jtree));
	}
	
	/**Fill the tree */
	public void populate(){
		File file = FileManager.getShuffleFile();
		DefaultMutableTreeNode mtn = new DefaultMutableTreeNode(file);
		top.add(mtn);
		
	}

}
