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

import javax.swing.JTextField;

import org.jajuk.util.log.Log;

/**
 * A label that user whom users can copy content.
 */
public class CopyableLabel extends JTextField {

  /** Default serial UID. */
  private static final long serialVersionUID = 1L;

  /**
   * Build a Copyable label.
   *
   * @param text : text to display
   */
  public CopyableLabel(String text) {
    super(text);
    setBorder(null);
    setOpaque(false);
    setEditable(false);
    // Not focusable to avoid tabbing between field focus this button instead next field
    setFocusable(false);
  }

  /**
   * Override setText() method to make sure users can't change it.
   */
  public void setText() {
    Log.debug("Label edition is not allowed");
  }

}
