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
package org.jajuk.ui.widgets;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.KeyStroke;

/**
 * jajuk default button, comes with few GUI default properties
 */
public class JajukButton extends JButton {

  private static final long serialVersionUID = 1L;

  public JajukButton() {
    this(null, null);
  }

  public JajukButton(Icon icon) {
    this(null, icon);
  }

  public JajukButton(String text) {
    this(text, null);
  }

  public JajukButton(Action a) {
    super(a);
    setOpaque(false);
    setRolloverEnabled(true);
  }

  public JajukButton(String text, Icon icon) {
    super(text, icon);
    setOpaque(false);
  }

  @Override
  protected void init(String text, Icon icon) {
    // Hide action text on button
    if (icon != null) {
      putClientProperty("hideActionText", Boolean.TRUE);
    }
    super.init(text, icon);
  }

  @Override
  protected void configurePropertiesFromAction(Action action) {
    if (action.getValue(Action.SMALL_ICON) != null) {
      putClientProperty("hideActionText", Boolean.TRUE);
    }

    super.configurePropertiesFromAction(action);

    KeyStroke stroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
    if (stroke != null) {
      InputMap keyMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
      keyMap.put(stroke, "action");

      ActionMap actionMap = getActionMap();
      actionMap.put("action", new ActionWrapper());
    }
  }

  private class ActionWrapper extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public void actionPerformed(ActionEvent e) {
      fireActionPerformed(e);
    }
  }
}
