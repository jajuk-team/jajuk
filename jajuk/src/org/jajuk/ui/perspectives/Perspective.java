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
 * Revision 1.2  2003/10/10 15:29:57  sgringoi
 * *** empty log message ***
 *
 */
package org.jajuk.ui.perspectives;


import java.util.Vector;

import javax.swing.JPanel;

import org.jajuk.ui.views.IView;

/**
 * Representation of a perspective
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public abstract class Perspective extends JPanel {
	
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

	/*
	 * @see java.awt.Component#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		PerspectiveManagerFactory.getPerspectiveManager().setCurrentPerspective(this);

		super.setVisible(b);
	}

}