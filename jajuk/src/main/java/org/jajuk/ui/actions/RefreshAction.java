/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * DOCUMENT_ME.
 */
public class RefreshAction extends JajukAction {
  
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new refresh action.
   */
  RefreshAction() {
    super(Messages.getString("ActionRefresh.0"), IconLoader.getIcon(JajukIcons.REFRESH), true);
    setShortDescription(Messages.getString("ActionRefresh.0"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    // Get required data from the tree (selected node and node type)
    // A single item (directory or device) is allowed
    final List<Item> alSelected = (ArrayList<Item>) source
        .getClientProperty(Const.DETAIL_SELECTION);
    Item item = alSelected.get(0);
    final Directory dir;
    if (item instanceof Directory) {
      dir = (Directory) item;
      dir.manualRefresh(true, true);
    } else if (item instanceof Device) {
      Device device = (Device) item;
      // ask user if he wants to make deep or fast scan
      device.refresh(true, true, false);
    }

  }
}