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

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.ui.IPerspective;
import org.jajuk.ui.IView;
import org.jajuk.ui.ViewManager;
import org.jajuk.ui.views.CoverView;
import org.jajuk.util.ConfigurationManager;

/**
 * Perspective adapter, provide default implementation for perspectives
 *
 * @author     bflorat
 * @created    15 nov. 2003
 */
public abstract class PerspectiveAdapter implements IPerspective,ITechnicalStrings {
	/** Perspective name*/
	private String sName;
	/** Perspective icon name*/
	private String sIconName;
	/** Perspective views list*/
	private ArrayList alViews = new ArrayList(10);
	/**Associated desktop pane*/
	private JDesktopPane desktop;
	
	
	/**
	 * Constructor
	 * @param sName
	 * @param sIconName
	 */
	public PerspectiveAdapter(String sName,String sIconName){
		this.sName = sName;
		this.sIconName = sIconName;
		this.desktop = new JDesktopPane();
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#addView(org.jajuk.ui.views.IView)
	 */
	public void addView(IView view,int iWidth,int iHeight,int iX,int iY) {
		//test the unique cover case : don't register the view if the option is disabled  
		if ((view instanceof CoverView) && !ConfigurationManager.getBoolean(CONF_OPTIONS_COVER) ){
			return;
		}
		alViews.add(view);
		ViewManager.registerView(view,this);
		int iCommandYSize = 108;
		int iCommandXSize = 45;
		ViewManager.setSize(view,(Main.jframe.getWidth()-iCommandXSize)*iWidth/100,(Main.jframe.getHeight()-iCommandYSize)*iHeight/100);
		ViewManager.setLocation(view,(Main.jframe.getWidth()-iCommandXSize)*iX/100,(Main.jframe.getHeight()-iCommandYSize)*iY/100);
		ViewManager.setVisible(view,true);
		getDesktop().add(ViewManager.getFrame(view));
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#removeView(org.jajuk.ui.views.IView)
	 */
	public void removeView(IView view) {
		alViews.remove(view);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#getName()
	 */
	public String getName() {
		return sName;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#getIconName()
	 */
	public String getIconName() {
		return sIconName;
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
	public JDesktopPane getDesktop() {
		return desktop;
	}
	
	/**
	 * toString method
	 */
	public String toString(){
		return "Perspective[name="+getName()+" description='"+getDesc()+"]";
	}

}
