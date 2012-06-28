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
import javax.swing.JButton;

/**
 * jajuk default button, comes with few GUI default properties.
 */
public class JajukButton extends JButton {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new jajuk button.
   */
  public JajukButton() {
    this(null, null);
  }

  /**
   * Instantiates a new jajuk button.
   * 
   * @param icon 
   */
  public JajukButton(Icon icon) {
    this(null, icon);
  }

  /**
   * Instantiates a new jajuk button.
   * 
   * @param a 
   */
  public JajukButton(Action a) {
    super(a);
    setOpaque(false);
    setRolloverEnabled(true);
  }

  /**
   * Instantiates a new jajuk button.
   * 
   * @param text 
   * @param icon 
   */
  public JajukButton(String text, Icon icon) {
    super(text, icon);
    setOpaque(false);
  }

  /* (non-Javadoc)
   * @see javax.swing.AbstractButton#init(java.lang.String, javax.swing.Icon)
   */
  @Override
  protected void init(String text, Icon icon) {
    // Hide action text on button
    if (icon != null) {
      putClientProperty("hideActionText", Boolean.TRUE);
    }
    super.init(text, icon);
  }

  /* (non-Javadoc)
   * @see javax.swing.AbstractButton#configurePropertiesFromAction(javax.swing.Action)
   */
  @Override
  protected void configurePropertiesFromAction(Action action) {
    if (action.getValue(Action.SMALL_ICON) != null) {
      putClientProperty("hideActionText", Boolean.TRUE);
    }
    super.configurePropertiesFromAction(action);
  }
}
