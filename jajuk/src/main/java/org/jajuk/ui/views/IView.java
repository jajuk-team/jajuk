/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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

import org.jajuk.ui.perspectives.IPerspective;

import java.awt.event.ComponentListener;

import com.vlsolutions.swing.docking.Dockable;

/**
 * A view
 */
public interface IView extends ComponentListener, Dockable {

	/**
	 * Returns the view identifier.
	 * 
	 * @return View identifier.
	 */
	public String getID();

	/**
	 * Set view ID
	 * 
	 * @param sID
	 */
	public void setID(String sID);

	/**
	 * Returns the view description as i18n key
	 * 
	 * @return View description.
	 */
	public String getDesc();

	/**
	 * Display view UI
	 */
	public void initUI();

	/**
	 * Get displayed state
	 * 
	 * @return populated state
	 */
	public boolean isPopulated();

	/**
	 * @param The
	 *            bIsPopulated to set.
	 */
	public void setIsPopulated(boolean isDisplayed);

	/**	 
	 *  
	 * @return current perspective for this view
	 */
	public IPerspective getPerspective();

	/**
	 * 
	 * @param perspective
	 */
	public void setPerspective(IPerspective perspective);
	
	/**
	 * Called when the view perspective is selected
	 */
	public void onPerspectiveSelection();

}
