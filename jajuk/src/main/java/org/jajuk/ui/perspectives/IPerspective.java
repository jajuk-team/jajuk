/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.ui.perspectives;

import java.awt.Container;
import java.util.Set;

import javax.swing.ImageIcon;

import org.jajuk.ui.views.IView;

/**
 * Representation of a perspective.
 */
public interface IPerspective {
  /**
   * Gets the id.
   * 
   * @return the perspective's id
   */
  String getID();

  /**
   * Gets the desc.
   * 
   * @return the desc
   */
  String getDesc();

  /**
   * Return the icon.
   * 
   * @return perspective icon
   */
  ImageIcon getIcon();

  /**
   * Gets the views.
   * 
   * @return Arraylist views registered in the perspective.
   */
  Set<IView> getViews();

  /**
   * Gets the content pane.
   * 
   * @return Returns the desktop.
   */
  Container getContentPane();

  /**
   * Serialize the perspective.
   * 
   * @throws Exception the exception
   */
  void commit() throws Exception;

  /**
   * Deserialize the perspective.
   * 
   * @throws Exception the exception
   */
  void load() throws Exception;

  /**
   * Restore defaults views.
   */
  void restoreDefaults();

  /**
   * As been selected flag.
   * 
   * @param b 
   */
  void setAsBeenSelected(boolean b);
}
