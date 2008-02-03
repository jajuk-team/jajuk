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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

import org.jajuk.base.Item;

/**
 * Convenient abstract class to factorize operations on selection
 */
public abstract class SelectionAction extends ActionBase {

  ArrayList<Item> selection = null;

  protected SelectionAction(String msg, ImageIcon icon, boolean enabled) {
    super(msg, icon, enabled);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.ActionBase#perform(java.awt.event.ActionEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void perform(ActionEvent e) throws Exception {
    JComponent source = (JComponent) e.getSource();
    Object o = source.getClientProperty(DETAIL_SELECTION);
    if (o instanceof Item) {
      selection = new ArrayList<Item>(1);
      selection.add((Item) o);
    } else if (o instanceof ArrayList) {
      selection = (ArrayList<Item>) source.getClientProperty(DETAIL_SELECTION);
    }
  }

}
