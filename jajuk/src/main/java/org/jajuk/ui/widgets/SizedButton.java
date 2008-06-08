/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.widgets;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.jajuk.util.UtilGUI;

/**
 * Button whose icon has a fixed size
 */
public class SizedButton extends JButton {

  private static final long serialVersionUID = -3859493434696496345L;

  //private int width = 16;

  //private int height = 16;

  private boolean showText = false;

  /**
   * Menu item with a fixed 16x216 icon dimension
   * 
   * @param action
   *          action
   */
  public SizedButton(Action action) {
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
   * @param Do
   *          we want to display text ?
   */
  public SizedButton(Action action, int width, int height, boolean bText) {
    super(action);
    //this.width = width;
    //this.height = height;
    this.showText = bText;
  }

  @Override
  public void setIcon(Icon icon) {
    super.setIcon(UtilGUI.getResizedImage((ImageIcon) icon, 16, 16));
  }

  @Override
  public void setText(String text) {
    if (showText) {
      super.setText(text);
    } else {
      super.setText(null);
    }
  }

}
