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
 * Revision 1.1  2003/11/16 17:57:18  bflorat
 * 16/11/2003
 *
 * Revision 1.4  2003/10/17 20:43:55  bflorat
 * 17/10/2003
 *
 * Revision 1.3  2003/10/16 15:56:30  sgringoi
 * Rename getIdView() in getId()
 * Add getComponent() method
 *
 * Revision 1.2  2003/10/10 15:29:57  sgringoi
 * *** empty log message ***
 *
 */
package org.jajuk.ui;

import java.awt.Component;

/**
 * View
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public interface IView {
	
	/**
	 * Returns the view identifier.
	 * @return View identifier.
	 */
	public abstract String getName();
	
	/**
	 * Returns the view description.
	 * @return View description.
	 */
	public abstract String getDesc();
		
	/**
	 * Set the view visible.
	 * @param pVisible - true to make the view visible; false to make it invisible.
	 */
	public void setVisible(boolean pVisible);
	

}
