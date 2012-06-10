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

import org.jajuk.util.UtilGUI;

/**
 * Button whose icon has a fixed size All icons are resized to 16x16 pixels by
 * default Override getWidth() and getHeight() methods to set your own
 * dimensions (we can't set width and height directly because the setIcon method
 * is called in constructor before we can do anything).
 */
public class SizedButton extends JajukButton {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -3859493434696496345L;

  private boolean showText = false;

  /**
   * Menu item with a fixed 16x216 icon dimension.
   * 
   * @param action action
   */
  public SizedButton(Action action) {
    super(action);
  }

  /**
   * Instantiates a new sized button.
   * 
   * @param text 
   * @param icon 
   */
  public SizedButton(String text, Icon icon) {
    super(text, icon);
  }

  /**
   * The Constructor.
   * 
   * @param action Action
   * @param bText 
   */
  public SizedButton(Action action, boolean bText) {
    super(action);
    this.showText = bText;
  }

  /* (non-Javadoc)
   * @see javax.swing.AbstractButton#setIcon(javax.swing.Icon)
   */
  @Override
  public void setIcon(Icon icon) {
    int width = getW();
    int height = getH();
    super.setIcon(UtilGUI.getResizedImage((ImageIcon) icon, width, height));
  }

  /* (non-Javadoc)
   * @see javax.swing.AbstractButton#setText(java.lang.String)
   */
  @Override
  public void setText(String text) {
    if (showText) {
      super.setText(text);
    } else {
      super.setText(null);
    }
  }

  /**
   * Return the width in pixels Must be overwritten to get a button with a
   * resizeable size != 16.
   * 
   * @return the W
   */
  public int getW() {
    return 16;
  }

  /**
   * Return the width in pixels Must be overwritten to get a button with a
   * resizeable size != 16.
   * 
   * @return the H
   */
  public int getH() {
    return 16;
  }

}
