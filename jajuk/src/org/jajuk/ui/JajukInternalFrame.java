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

import java.awt.Cursor;

import javax.swing.JInternalFrame;

import org.jajuk.util.Util;

/**
 *  Jajuk internal frame. Extends JInternal frames to bring:
 * <p>Fixes JRE 1.4.2 bug : 4705698
 *
 * @author     bflorat
 * @created    10 mars 2004
 */
public class JajukInternalFrame extends JInternalFrame {
	
	/**Waiting state*/
	private boolean bWaiting = false;
	
	/**fixes JRE 1.4.2 bug : 4705698
	 * @return cursor
	 */
	public Cursor getCursor(){
		if ( bWaiting){
			return 	Util.WAIT_CURSOR;
		}
		else{
			return super.getCursor();
		}
	}
	
	/** Constructor
	 * 
	 * @param title
	 * @param resizable
	 * @param closable
	 * @param maximizable
	 * @param iconifiable
	 */
	public JajukInternalFrame(String title, boolean resizable, boolean closable, 
			boolean maximizable, boolean iconifiable) {
		super(title,resizable,closable,maximizable,iconifiable);
	}
	
	/**
	 * 
	 * @param bWaiting waiting state
	 */
	public void setWaiting(boolean bWaiting){
		this.bWaiting = bWaiting;
	}
	
	/**
	 * Get waiting state
	 * @return waiting state
	 */
	public boolean getWaiting(){
		return bWaiting;
	}
	
}
