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
package org.jajuk.ui.widgets;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.UIManager;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXHyperlink;

/**
 * Generic toggle hyperlink to open/collapse a collapsable panel.
 */
public class ToggleLink extends JXHyperlink {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 8652043694603450888L;

  /**
   * The Constructor.
   * 
   * @param text hyperlink text
   * @param panel the associated collapsible panel
   */
  public ToggleLink(String text, JXCollapsiblePane panel) {
    // get the built-in toggle action
    Action toggleAction = panel.getActionMap().get(JXCollapsiblePane.TOGGLE_ACTION);
    setAction(toggleAction);
    setText(text);
    setFont(getFont().deriveFont(Font.BOLD));
    // use the collapse/expand icons from the JTree UI
    toggleAction.putValue(JXCollapsiblePane.COLLAPSE_ICON, UIManager.getIcon("Tree.expandedIcon"));
    toggleAction.putValue(JXCollapsiblePane.EXPAND_ICON, UIManager.getIcon("Tree.collapsedIcon"));
    setFocusPainted(false);
  }
}
