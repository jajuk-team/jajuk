/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

package org.jajuk.ui.widgets;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.jajuk.util.UtilGUI;

/**
 * Menu item whose icon has a fixed size.
 */
public class SizedJMenuItem extends JMenuItem {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -3859493402696496345L;

  /**
   * Menu item with a fixed 16x216 icon dimension.
   * 
   * @param action action
   */
  public SizedJMenuItem(Action action) {
    super(action);
  }

  /**
   * Sets the icon.
   * 
   * @param icon DOCUMENT_ME
   */
  @Override
  public void setIcon(Icon icon) {
    super.setIcon(UtilGUI.getResizedImage((ImageIcon) icon, 16, 16));
  }

}
