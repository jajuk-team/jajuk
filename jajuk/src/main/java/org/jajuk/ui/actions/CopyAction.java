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
 *  $$Revision: 2920 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComponent;

import org.jajuk.base.Item;
import org.jajuk.ui.helpers.ItemMoveManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

/**
 * Action class for Copy
 */

public class CopyAction extends ActionBase {

  private static final long serialVersionUID = 1L;

  CopyAction() {
    super(Messages.getString("FilesTreeView.3"), IconLoader.ICON_COPY, "ctrl C", true, false);
    setShortDescription(Messages.getString("FilesTreeView.3"));
  }

  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    ArrayList<Item> alSelected = (ArrayList<Item>) source.getClientProperty(DETAIL_SELECTION);

    ItemMoveManager.getInstance().removeAll();
    ItemMoveManager.getInstance().addItems(alSelected);
    ItemMoveManager.getInstance().setAction(ItemMoveManager.MoveActions.COPY);
  }
}