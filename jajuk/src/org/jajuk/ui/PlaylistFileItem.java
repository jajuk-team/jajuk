/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Release$
 */

package org.jajuk.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.dnd.DnDConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.base.PlaylistFile;
import org.jajuk.util.Util;

	/**
	 * A physical playlist icon + text
	 *
	 * @author     bflorat
	 * @created    31 dec. 2004	 
	 */
	public class PlaylistFileItem extends JPanel{
		
		/** Associated playlist file*/
		PlaylistFile plf;
		
		/**Associated name*/
		String sName;
		
		/** Playlist file type : 0: normal, 1:new, 2:bookmarks, 3:bestif*/
		int iType = 0;
		
		public static final int PLAYLIST_TYPE_NORMAL = 0;
		public static final int PLAYLIST_TYPE_NEW = 1;
		public static final int PLAYLIST_TYPE_BOOKMARK = 2;
		public static final int PLAYLIST_TYPE_BESTOF = 3;
		public static final int PLAYLIST_TYPE_QUEUE = 4;
		
		
		/**
		 * Constructor
		 * @param iType : Playlist file type : 0: normal, 1:new, 2:bookmarks, 3:bestif
		 * @param sIcon : icon to be shown
		 * @param sName : nom of the playlist file to be displayed
		 */
		public PlaylistFileItem(int iType,String sIcon,PlaylistFile plf, String sName){
			this.iType = iType;
			this.plf = plf;
			setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
			setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			JPanel jpIcon  = new JPanel();
			jpIcon.setLayout(new BoxLayout(jpIcon,BoxLayout.X_AXIS));
			JLabel jlIcon = new JLabel(Util.getIcon(sIcon)); 
			jpIcon.add(Box.createGlue());
			jpIcon.add(jlIcon);
			jpIcon.add(Box.createGlue());
			add(jpIcon);
			JLabel jlName = new JLabel(sName);
			jlName.setFont(new Font("Dialog",Font.PLAIN,10));
			JPanel jpName  = new JPanel();
			jpName.setLayout(new BoxLayout(jpName,BoxLayout.X_AXIS));
			jpName.add(Box.createGlue());
			jpName.add(jlName);
			jpName.add(Box.createGlue());
			jpName.setPreferredSize(new Dimension(40,10));
			add(jpName);
			add(Box.createVerticalGlue());
			new PlaylistTransferHandler(this,DnDConstants.ACTION_COPY_OR_MOVE);
		}
		
				
		/**
		 * @return Returns the playlist file.
		 */
		public PlaylistFile getPlaylistFile() {
			return plf;
		}

		/**
		 * @return Returns the Type.
		 */
		public int getType() {
			return iType;
		}

	}
