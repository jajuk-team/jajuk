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
import java.util.ArrayList;

import javax.swing.JDesktopPane;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewManager;

/**
 * Perspective adapter, provide default implementation for perspectives
 *
 * @author     bflorat
 * @created    15 nov. 2003
 */
public abstract class PerspectiveAdapter implements IPerspective,ITechnicalStrings {
	/** Perspective id (class)*/
	private String sID;
	/** Perspective icon path*/
	private String sIconPath;
	/** Perspective views list*/
	private ArrayList alViews = new ArrayList(10);
	/**Associated desktop pane*/
	private JDesktopPane desktop;
	
	
	/**
	 * Constructor
	 * @param sName
	 * @param sIconName
	 */
	public PerspectiveAdapter(){
		this.desktop = new JDesktopPane();
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#addView(org.jajuk.ui.views.IView)
	 */
	public void addView(IView view) {
		alViews.add(view);
		ViewManager.registerView(view);
		int iMainWidth = Main.getWindow().getWidth()- BORDER_X_SIZE; //desktop pane size in pixels
		int iMainHeight = Main.getWindow().getHeight()- BORDER_Y_SIZE; //desktop pane size in pixels
		int iWidth = iMainWidth*view.getLogicalWidth()/100;
		int iHeight = iMainHeight*view.getLogicalHeight()/100;
		int iX = iMainWidth*view.getLogicalX()/100;
		int iY = iMainHeight*view.getLogicalY()/100;
		ViewManager.setVisible(view,view.isShouldBeShown());
		ViewManager.setSize(view,iWidth,iHeight);
		ViewManager.setLocation(view,iX,iY);
		getDesktop().add(ViewManager.getFrame(view));
	}
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#removeView(org.jajuk.ui.views.IView)
	 */
	public void removeView(IView view) {
		alViews.remove(view);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#getID()
	 */
	public String getID() {
		return sID;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#getViews()
	 */
	public ArrayList getViews() {
		return alViews;
	}

	/**
	 * @return Returns the desktop.
	 */
	public Container getDesktop() {
		return desktop;
	}
	
	/**
	 * toString method
	 */
	public String toString(){
		return "Perspective[name="+getID()+" description='"+getDesc()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IPerspective#getIconPath()
	 */
	public String getIconPath() {
		return sIconPath;
	}
	
	/**
	 * Set icon path
	 */
	public void setIconPath(String sIconPath) {
		this.sIconPath = sIconPath;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IPerspective#setID(java.lang.String)
	 */
	public void setID(String sID) {
		this.sID = sID;
	}

}
