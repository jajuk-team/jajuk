/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

package org.jajuk.ui.helpers;

import java.util.ArrayList;
import java.util.List;

import org.jajuk.base.Item;

/**
 * Convenient class to manage Items to be moved using Cut/Copy/Paste Actions.
 */

public class ItemMoveManager {

  /** DOCUMENT_ME. */
  private static ItemMoveManager singleton = new ItemMoveManager();

  /** DOCUMENT_ME. */
  private final List<Item> itemsToMove = new ArrayList<Item>(20);

  /**
   * DOCUMENT_ME.
   */
  public enum MoveActions {

    /** DOCUMENT_ME. */
    CUT,
    
    /** DOCUMENT_ME. */
    COPY
  }

  /** DOCUMENT_ME. */
  private MoveActions moveAction;

  /**
   * Gets the single instance of ItemMoveManager.
   * 
   * @return single instance of ItemMoveManager
   */
  public static ItemMoveManager getInstance() {
    return singleton;
  }

  /**
   * Adds the items.
   * DOCUMENT_ME
   * 
   * @param items DOCUMENT_ME
   */
  public void addItems(List<Item> items) {
    itemsToMove.addAll(items);
  }

  /**
   * Gets the all.
   * 
   * @return the all
   */
  public List<Item> getAll() {
    return itemsToMove;
  }

  /**
   * Removes the all.
   * DOCUMENT_ME
   */
  public void removeAll() {
    itemsToMove.clear();
  }

  /**
   * Sets the action.
   * 
   * @param action the new action
   */
  public void setAction(MoveActions action) {
    moveAction = action;
  }

  /**
   * Gets the action.
   * 
   * @return the action
   */
  public MoveActions getAction() {
    return moveAction;
  }
}