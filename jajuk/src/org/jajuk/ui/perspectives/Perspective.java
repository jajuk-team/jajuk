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
 * $Log$
 * Revision 1.1  2003/10/07 21:02:18  bflorat
 * Initial commit
 *
 */
package org.jajuk.ui.perspectives;


import java.util.Vector;

import org.jajuk.ui.views.IView;

/**
 * Representation of a perspective
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public abstract class Perspective {
	
	/** List of views showned in the perspective */
	private Vector views = null;

	/**
	 * Constructor of Perspective
	 */
	public Perspective() {
		super();
		
		views = new Vector();
	}

	/**
	 * Add a view to the perspective.
	 * 
	 * @param pView View to add to the perspective.
	 * @return void 
	 */
	protected void addView(IView pView) {
		views.add(pView);
	}
	
	/**
	 * Remove a view from the perspective.
	 * 
	 * @param pView View to remove.
	 * @return void 
	 */
	public void removeView(IView pView) {
		views.remove(pView);
	}
	
	/**
	 * Show the perspective.
	 * The perspective become the current perspective.
	 * 
	 * @return void 
	 */
	public void show() {
		PerspectiveManagerFactory.getPerspectiveManager().setCurrentPerspective(this);
	}
}