/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui.helpers;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;

import org.jajuk.util.Util;

/**
 * Menu item whish icon has a fixed size
 */
public class SizedJMenuItem extends JMenuItem {
  private static final long serialVersionUID = -3859493402696496345L;

  private int width = 16;

  private int height = 16;

  /**
   * Menu item with a fixed 16x216 icon dimension
   * 
   * @param action
   *          action
   */
  public SizedJMenuItem(Action action) {
    super(action);
  }

  /**
   * 
   * @param action
   *          Action
   * @param width
   *          fixed icon width
   * @param height
   *          fixed icon height
   */
  public SizedJMenuItem(Action action, int width, int height) {
    super(action);
    this.width = width;
    this.height = height;
  }

  public void setIcon(Icon icon) {
    super.setIcon(Util.getResizedImage((ImageIcon) icon, 16, 16));
  }

}
