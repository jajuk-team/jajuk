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

import org.jajuk.base.Item;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * An abstract physical or logical tree view. Contains common methods
 */
public abstract class AbstractTreeView extends ViewAdapter {

	/** The tree scrollpane */
	JScrollPane jspTree;

	/** The phyical tree */
	JXTree jtree;

	/** Current selection */
	TreePath[] paths;

	/** Items selection */
	ArrayList<Item> alSelected = new ArrayList<Item>(100);

	/** Top tree node */
	DefaultMutableTreeNode top;

	protected JTree createTree() {
		jtree = new JXTree(top);
		jtree.putClientProperty("JTree.lineStyle", "Angled"); //$NON-NLS-1$ //$NON-NLS-2$
		jtree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		
		Highlighter highlighter = new RolloverHighlighter(Color.LIGHT_GRAY,
				Color.ORANGE);
		Highlighter hAlternate = new AlternateRowHighlighter();
		HighlighterPipeline pipeHighlight = new HighlighterPipeline(
				new Highlighter[] { highlighter,hAlternate });
		jtree.setHighlighters(pipeHighlight);
		jtree.setRolloverEnabled(true);
		return jtree;
	}

}
