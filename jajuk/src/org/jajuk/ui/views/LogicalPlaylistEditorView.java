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
 *  $Revision$
 */

package org.jajuk.ui.views;



/**
 * Playliost editor/manager 
 * <p>Logical perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
public class LogicalPlaylistEditorView extends AbstractPlaylistEditorView{

	/**Self instance*/
	private static LogicalPlaylistEditorView lpe;
	
	/**Return self instance*/
	public static synchronized LogicalPlaylistEditorView getInstance(){
		if (lpe == null){
			lpe = new LogicalPlaylistEditorView();
		}
		return lpe;
	}
	
	/**
	 * Constructor
	 */
	public LogicalPlaylistEditorView() {
		lpe = this;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.LogicalPlaylistEditorView"; //$NON-NLS-1$
	}
	
}
