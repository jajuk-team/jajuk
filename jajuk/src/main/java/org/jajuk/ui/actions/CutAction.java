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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JComponent;

import org.jajuk.base.Item;
import org.jajuk.ui.helpers.ItemMoveManager;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Action class for Cut.
 */
public class CutAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new cut action.
   */
  CutAction() {
    super(Messages.getString("FilesTreeView.4"), IconLoader.getIcon(JajukIcons.CUT), "ctrl X",
        true, false);
    setShortDescription(Messages.getString("FilesTreeView.4"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void perform(ActionEvent e) {
    JComponent source = (JComponent) e.getSource();
    ArrayList<Item> alSelected = (ArrayList<Item>) source.getClientProperty(Const.DETAIL_SELECTION);
    ItemMoveManager.getInstance().removeAll();
    ItemMoveManager.getInstance().addItems(alSelected);
    ItemMoveManager.getInstance().setAction(ItemMoveManager.MoveActions.CUT);
  }
}