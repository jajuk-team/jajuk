/*
 *  Jajuk
 *  Copyright (C) 2003 fdutron
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

import java.awt.Component;
import java.awt.Cursor;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jajuk.ui.views.IView;
import org.jajuk.util.Util;

/**
 *  Jajuk container. Extends JPanel
 *
 * @author     fdutron
 * @created    07 octobre 2004
 */
public class JajukContainer extends JPanel{
	
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
	
	public void setView(IView c){
	    setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
	    if (getComponentCount() > 0){
	        removeAll();
	    }
	    add((Component)c);
	}
	
	/** Constructor
	 * 
	 * @param view
	 */
	public JajukContainer(IView c) {
		setView(c);
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
