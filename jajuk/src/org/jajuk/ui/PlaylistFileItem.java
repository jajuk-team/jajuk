/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
 * $Revision$
 */

package org.jajuk.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Font;
import java.awt.dnd.DnDConstants;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.base.PlaylistFile;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

	/**
	 * A physical playlist icon + text
	 *
	 * @author     Bertrand Florat
	 * @created    31 dec. 2004	 
	 */
	public class PlaylistFileItem extends JPanel {
		
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
        public static final int PLAYLIST_TYPE_NOVELTIES = 5;
        
        private static final Font font = new Font("Dialog",Font.PLAIN,10);
        
        /**
		 * Constructor
		 * @param iType : Playlist file type : 0: normal, 1:new, 2:bookmarks, 3:bestif
		 * @param sIcon : icon to be shown
		 * @param sName : nom of the playlist file to be displayed
		 */
		public PlaylistFileItem(int iType,String sIcon,PlaylistFile plf, String sName){
			this.iType = iType;
			this.plf = plf;
			double[][] dSize = {{85},{40,10,5}};
            TableLayout layout = new TableLayout(dSize);
            layout.setVGap(5);
            setLayout(layout);
			JLabel jlIcon = new JLabel(Util.getIcon(sIcon)); 
			JLabel jlName = new JLabel(sName);
			jlName.setFont(font); //$NON-NLS-1$
			add(jlIcon,"0,0,c,c");
            add(jlName,"0,1,c,c");
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
		
		/**
		 * Get a name for this playlist file item
		 * @return playlist file item name ( playlist name or label for special ones )
		 */
		public String getName(){
			String sOut=""; //$NON-NLS-1$
			switch(iType){
			case 0:  //regular playlist
				PlaylistFile plf = getPlaylistFile();
				if (plf != null){
				    sOut = plf.getName();
				}
				break;
			case 1:  //new playlist
				sOut = Messages.getString("PlaylistFileItem.2"); //$NON-NLS-1$
				break;
			case 2:  //bookmarks
				sOut = Messages.getString("PlaylistFileItem.3"); //$NON-NLS-1$
				break;
			case 3:  //bestof
				sOut = Messages.getString("PlaylistFileItem.4"); //$NON-NLS-1$
				break;
			case 4:  //queue
				sOut = Messages.getString("PlaylistFileItem.5"); //$NON-NLS-1$
				break;
			case 5:  //novelties
                sOut = Messages.getString("PlaylistFileItem.1"); //$NON-NLS-1$
                break;
            }
            return sOut;
		}
			
		
	}
