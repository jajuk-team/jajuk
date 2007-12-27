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

import javax.swing.JDialog;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;

/**
 * Custom JDialog
 */
public class JajukJDialog extends JDialog implements ITechnicalStrings {

  private static final long serialVersionUID = 3280008357821054703L;

  public JajukJDialog() {
    // Show jajuk logo as default icon (it is useful for 1.6 as we
    // can't use the 1.6 JDialog.setIcon for now as long as we support 1.5)
    ((java.awt.Frame) getOwner()).setIconImage(IconLoader.ICON_LOGO_FRAME.getImage());
  }

}
