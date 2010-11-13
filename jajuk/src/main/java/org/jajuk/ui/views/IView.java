/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
 *  http://jajuk.info
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

import com.vlsolutions.swing.docking.Dockable;

import java.awt.event.ComponentListener;

import org.jajuk.ui.perspectives.IPerspective;

/**
 * A view.
 */
public interface IView extends ComponentListener, Dockable {

  /**
   * Returns the view identifier.
   * 
   * @return View identifier.
   */
  String getID();

  /**
   * Set view ID.
   * 
   * @param sID DOCUMENT_ME
   */
  void setID(String sID);

  /**
   * Returns the view description as i18n key.
   * 
   * @return View description.
   */
  String getDesc();

  /**
   * Display view UI.
   */
  void initUI();

  /**
   * Get displayed state.
   * 
   * @return populated state
   */
  boolean isPopulated();

  /**
   * Sets the is populated.
   * 
   * @param isDisplayed DOCUMENT_ME
   */
  void setIsPopulated(boolean isDisplayed);

  /**
   * Gets the perspective.
   * 
   * @return current perspective for this view
   */
  IPerspective getPerspective();

  /**
   * Sets the perspective.
   * 
   * @param perspective DOCUMENT_ME
   */
  void setPerspective(IPerspective perspective);

  /**
   * Called when the view perspective is selected.
   */
  void onPerspectiveSelection();

}
