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
 *  $Revision: 3253 $
 */

package org.jajuk.ui.helpers;

import java.util.ArrayList;
import java.util.List;

import org.jajuk.base.Item;

/**
 * Convenient class to manage Items to be moved using Cut/Copy/Paste Actions
 */

public class ItemMoveManager {

  private static ItemMoveManager singleton;

  private final List<Item> itemsToMove = new ArrayList<Item>(20);

  public enum MoveActions {
    CUT, COPY
  }

  private MoveActions moveAction;

  public static ItemMoveManager getInstance() {
    if (singleton == null) {
      singleton = new ItemMoveManager();
    }
    return singleton;
  }

  public void addItems(List<Item> items) {
    itemsToMove.addAll(items);
  }

  public List<Item> getAll() {
    return itemsToMove;
  }

  public void removeAll() {
    itemsToMove.clear();
  }

  public void setAction(MoveActions action) {
    moveAction = action;
  }

  public MoveActions getAction() {
    return moveAction;
  }
}