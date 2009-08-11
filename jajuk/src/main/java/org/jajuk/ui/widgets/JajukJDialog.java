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

import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JRootPane;

import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.UtilGUI;

/**
 * Custom JDialog
 */
public class JajukJDialog extends JDialog {

  private static final long serialVersionUID = 3280008357821054703L;

  public JajukJDialog() {
    super();
    
    setIconImage(IconLoader.getIcon(JajukIcons.LOGO).getImage());
  }

  /**
   * @param owner
   * @param modal
   */
  public JajukJDialog(Frame owner, boolean modal) {
    super(owner, modal);

    setIconImage(IconLoader.getIcon(JajukIcons.LOGO).getImage());
  }

  @Override
  protected JRootPane createRootPane() {
    JRootPane rootPane = new JRootPane();
    UtilGUI.setEscapeKeyboardAction(this, rootPane);
    return rootPane;
  }
}
