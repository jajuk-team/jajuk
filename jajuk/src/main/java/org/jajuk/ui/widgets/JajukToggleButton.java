/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
package org.jajuk.ui.widgets;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.border.Border;

public class JajukToggleButton extends JajukButton {
  private static final long serialVersionUID = 1L;

  private static final Border PRESSED_BORDER = BorderFactory.createLoweredBevelBorder();

  private static final Border UNPRESSED_BORDER = BorderFactory.createRaisedBevelBorder();

  public JajukToggleButton() {
    this(false);
  }

  public JajukToggleButton(boolean selected) {
    this(null, null, selected);
  }

  public JajukToggleButton(Icon icon) {
    this(icon, false);
  }

  public JajukToggleButton(Icon icon, boolean selected) {
    this(null, icon, selected);
  }

  public JajukToggleButton(String text) {
    this(text, false);
  }

  public JajukToggleButton(String text, boolean selected) {
    this(text, null, selected);
  }

  public JajukToggleButton(Action a) {
    this(a, false);
  }

  public JajukToggleButton(Action a, boolean selected) {
    super(a);
    setSelected(selected);
  }

  public JajukToggleButton(String text, Icon icon) {
    this(text, icon, false);
  }

  public JajukToggleButton(String text, Icon icon, boolean selected) {
    super(text, icon);
    setSelected(selected);
  }

  @Override
  public final void setSelected(boolean b) {
    super.setSelected(b);
    setBorder(b ? PRESSED_BORDER : UNPRESSED_BORDER);
  }

  @Override
  protected void fireActionPerformed(ActionEvent event) {
    setSelected(!isSelected());
    super.fireActionPerformed(event);
  }
}
