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


import java.util.ArrayList;

import javax.swing.JDesktopPane;

import org.jajuk.ui.views.IView;
/**
 * Representation of a perspective
 * @author		bflorat
 * @created		15 nov. 2003
 */

public interface IPerspective {

	
	/**
	 * Add a view to the perspective.
	 * @param view View to add to the perspective.
	 * @param iWidthPer desktop width percentile required y the view 
	 * @param iHeightPer desktop height percentile required y the view
	 * @param iXPer desktop X coordonate for the upper-left point of the view
	 * @param iYPer desktop Y coordonate for the upper-left point of the view
	 */
	public void addView(IView view,int iWidthPer,int iHeightPer,int iXPer,int iYPer);
	/**
		 * 
		 * 
		 * @param view 
		 * @return void 
		 */
		
	
	/**
	 * Remove a view from the perspective.
	 * 
	 * @param view View to remove.
	 * @return void 
	 */
	public void removeView(IView view);
		
	/**
	 * @return the perspective's name.
	 */
	public String getName();
	
	/**
	 * Set name ( class ) of the perspective
	 * @param sName name ( class ) of the perspective
	 */
	public void setName(String sName);
	
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
	public JDesktopPane getDesktop() ;

}