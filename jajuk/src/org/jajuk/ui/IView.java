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
 * $Revision$
 *
 */
package org.jajuk.ui;


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
