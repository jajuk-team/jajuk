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
 * Revision 1.3  2003/10/24 12:41:53  sgringoi
 * Add the init() and createView() methods
 *
 * Revision 1.2  2003/10/10 15:29:57  sgringoi
 * *** empty log message ***
 *
 */
package org.jajuk.ui;


import java.util.ArrayList;

import javax.swing.JDesktopPane;


/**
 * Representation of a perspective
 * 
 * @author		bflorat
 * @version	1.0
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
	 *  Type description
	 *
	 * @author     bflorat
	 * @created    15 nov. 2003
	 */
	public abstract String getDesc();
	
	/**
	 * Return the icon's name of the perspective. 
	 * @return String Icon's name representing the perspective.
	 */
	public abstract String getIconName();
	
	
	/**
	 * @return Arraylist views registered in the perspective.
	 */
	public ArrayList getViews();
	
	/**
		 * @return Returns the desktop.
		 */
		public JDesktopPane getDesktop() ;

}