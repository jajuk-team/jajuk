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
package org.jajuk.ui.views;

import java.awt.event.ComponentListener;


/**
 * View
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public interface IView extends ComponentListener{
	
	/**
	 * Returns the view identifier.
	 * @return View identifier.
	 */
	public String getID();
	
	/**
	 * Returns the view description as i18n key
	 * @return View description.
	 */
	public String getDesc();
	
	/**
	 * Set the view visible.
	 * @param pVisible - true to make the view visible; false to make it invisible.
	 */
	public void setVisible(boolean pVisible);
	
	/**
	 * Populate the view 
	 */
	public void populate();
	
	/**
	 * Get displayed state
	 * @return populated state
	 */
	public boolean isPopulated();
	
	/**
	 * @param The bIsPopulated to set.
	 */
	public void setIsPopulated(boolean isDisplayed) ;
	
	/**
	 * View refresh
	 */
	public void refresh();
	
	/**
     * @return Returns the iHeight.
     */
    public int getLogicalHeight();
   
    /**
     * @param height The iHeight to set.
     */
    public void setLogicalHeight(int height);
    
    /**
     * @return Returns the iWidth.
     */
    public int getLogicalWidth();
    
    /**
     * @param width The iWidth to set.
     */
    public void setLogicalWidth(int width) ;
    
    /**
     * @return Returns the iX.
     */
    public int getLogicalX();
    
    /**
     * @param ix The iX to set.
     */
    public void setLogicalX(int ix);
    
    /**
     * @return Returns the iY.
     */
    public int getLogicalY() ;
    
    /**
     * @param iy The iY to set.
     */
    public void setLogicalY(int iy) ;

    /**
     * Set all view coordonates
     * @param iWidth
     * @param iHeight
     * @param iX
     * @param iY
     * @return view itself
     */
    public IView setLogicalCoord(int iWidth,int iHeight,int iX,int iY);
    
    /**
     * Set the should be shown flag
     * @param b 
     * @return IView
     */
    public IView setShouldBeShown(boolean b);
    
    /**
     * 
     * @return should be shown flag
     */
    public boolean isShouldBeShown();
    	
    
    

}
