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
package org.jajuk.ui.perspectives;


import java.awt.Container;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.jajuk.ui.views.IView;
/**
 * Representation of a perspective
 * @author		bflorat
 * @created		15 nov. 2003
 */

public interface IPerspective {

	public void setDefaultViews();
	/**
	 * Add a view to the perspective but don't place it.
	 *  @param view View to add to the perspective.
	 */
	public net.infonode.docking.View addView(IView view);
	
	/**
	 * Add a view to the perspective.
	 *  @param view View to add to the perspective.
	 */
	public net.infonode.docking.View addViewAndPlaceIt(IView view);
	
	/**
	 * Remove a view from the perspective.
	 * 
	 * @param view View to remove.
	 * @return void 
	 */
	public void removeView(IView view);
	
	/**
	 * Remove all views from the perspective.
	 * 
	 * @return void 
	 */
	public void removeAllView();
	
	/**
	 * @return the perspective's id
	 */
	public String getID();
	
	/**
	 * Set id ( class ) of the perspective
	 * @param sID ( class ) of the perspective
	 */
	public void setID(String sID);
	
	/**
	 *  Type description
	 *
	 * @author     bflorat
	 * @created    15 nov. 2003
	 */
	public abstract String getDesc();
	
	/**
	 * Return the icon's jar path   
	 * @return String Icon's jar path representing the perspective.
	 */
	public abstract String getIconPath();
	

	/**
	 * Set icon path inside jar
	 * @param sIconPath icon path inside jar
	 */
	public void setIconPath(String sIconPath);
	
	/**
	 * @return Arraylist views registered in the perspective.
	 */
	public ArrayList getViews();
	
	/**
	 * @return Returns the desktop.
	 */
	public Container getDesktop() ;
	
	/**
	 * Serialize the perspective
	 */
	public void commit()throws IOException;
	
	/**
	 * Deserialize the perspective
	 */
	public void load()throws IOException;
		
}