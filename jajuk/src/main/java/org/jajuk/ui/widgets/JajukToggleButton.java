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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.border.Border;

/**
 * A Jajuk button with two states.
 */
public class JajukToggleButton extends JajukButton {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant PRESSED_BORDER.   */
  private static final Border PRESSED_BORDER = BorderFactory.createLoweredBevelBorder();

  /** The Constant UNPRESSED_BORDER.   */
  private static final Border UNPRESSED_BORDER = BorderFactory.createRaisedBevelBorder();

  /**
   * Instantiates a new jajuk toggle button.
   */
  public JajukToggleButton() {
    this(false);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param selected 
   */
  public JajukToggleButton(boolean selected) {
    this(null, null, selected);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param icon 
   */
  public JajukToggleButton(Icon icon) {
    this(icon, false);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param icon 
   * @param selected 
   */
  public JajukToggleButton(Icon icon, boolean selected) {
    this(null, icon, selected);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param text 
   */
  public JajukToggleButton(String text) {
    this(text, false);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param text 
   * @param selected 
   */
  public JajukToggleButton(String text, boolean selected) {
    this(text, null, selected);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param a 
   */
  public JajukToggleButton(Action a) {
    this(a, false);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param a 
   * @param selected 
   */
  public JajukToggleButton(Action a, boolean selected) {
    super(a);
    setSelected(selected);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param text 
   * @param icon 
   */
  public JajukToggleButton(String text, Icon icon) {
    this(text, icon, false);
  }

  /**
   * Instantiates a new jajuk toggle button.
   * 
   * @param text 
   * @param icon 
   * @param selected 
   */
  public JajukToggleButton(String text, Icon icon, boolean selected) {
    super(text, icon);
    setSelected(selected);
  }

  /* (non-Javadoc)
   * @see javax.swing.AbstractButton#setSelected(boolean)
   */
  @Override
  public final void setSelected(boolean b) {
    super.setSelected(b);
    setBorder(b ? PRESSED_BORDER : UNPRESSED_BORDER);
  }

  /* (non-Javadoc)
   * @see javax.swing.AbstractButton#fireActionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  protected void fireActionPerformed(ActionEvent event) {
    setSelected(!isSelected());
    super.fireActionPerformed(event);
  }

}
