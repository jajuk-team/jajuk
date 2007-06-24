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

import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jdesktop.swingx.JXTree;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.ConditionalHighlighter;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;

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

	/** Concurrency locker * */
	volatile short[] lock = new short[0];

	/** Items selection */
	ArrayList<Item> alSelected = new ArrayList<Item>(100);

	/** Top tree node */
	DefaultMutableTreeNode top;

	protected JTree createTree() {
		jtree = new JXTree(top);
		jtree.putClientProperty("JTree.lineStyle", "Angled");  
		jtree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		/*// Add alternate rows highliter
		Highlighter alternate = null;
		ColorScheme colors = SubstanceLookAndFeel.getActiveColorScheme();
		if (SubstanceLookAndFeel.getTheme().getKind() == ThemeKind.DARK) {
			alternate = new AlternateRowHighlighter(colors
					.getMidColor(), colors.getDarkColor(), colors
					.getForegroundColor());
		} else {
			alternate = new AlternateRowHighlighter(Color.WHITE,
					colors.getUltraLightColor(), colors.getForegroundColor());
		}*/
		//Background color is not actually taken into account with sustance watermarks
		Highlighter playing = new ConditionalHighlighter(Color.BLACK,Color.ORANGE,0,-1) {
			
			protected boolean needsHighlight(ComponentAdapter adapter){
				// Perfs and safety
				if (FIFO.getInstance().getCurrentItem() == null) {
					return false;
				}
				// Compare current played track and the tested element
				DefaultMutableTreeNode item = (DefaultMutableTreeNode) adapter
						.getValue();
				Item itemCurrent = FIFO.getInstance().getCurrentItem()
						.getFile();
				// in physical table view
				if (item instanceof FileNode) {
					File checked = null;
					checked = ((FileNode) item).getFile();
					return (itemCurrent.equals(checked));
				}
				// In logical table view
				else if (item instanceof TrackNode) {
					Track track = ((TrackNode) item).getTrack();
					for (File checked : track.getFiles()) {
						if (itemCurrent.equals(checked)) {
							return true;
						}
					}
				}
				return false;
			}
			
			
			@Override
			protected boolean test(ComponentAdapter adapter) {
				return true;
			}
		
		};
		HighlighterPipeline pipe = new HighlighterPipeline(new Highlighter[]{playing});
		jtree.setHighlighters(pipe);
		return jtree;
	}

}
