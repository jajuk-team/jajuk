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

import java.awt.Container;

/**
 * Manager perspectives interface.
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public interface IPerspectiveManager {
  /* {author=Sébastien Gringoire, version=1.0}*/

	/**
	 * Return the perspectives list manage by the manager.
	 */
	public Perspective[] getPerspectives();
	
	/**
	 * Return the current perspective.
	 * 
	 * @return Perspective - The current perspective
	 */
	public Perspective getCurrentPerspective();
	
	/**
	 * Set the perspectives container.
	 * 
	 * @return void 
	 */
	public void setMainWindow(Container pContainer);

	/**
	 * Set the current perspective.
	 * 
	 * @return void 
	 */
	void setCurrentPerspective(Perspective pCurPersp);
	
	/**
	 * Return the perspective corresponding to the name pName.
	 * 
	 * @param pName Perspective name.
	 * @return Perspective Perspective named pName.
	 */
	public Perspective getPerspective(String pName);
}