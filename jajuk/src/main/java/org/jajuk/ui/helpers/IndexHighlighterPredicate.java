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
package org.jajuk.ui.helpers;

import java.awt.Component;

import org.jajuk.services.players.QueueModel;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

/**
 * Jajuk playing highlighter used in some tables to change the cell background
 * if the index is at the given row.
 */
public class IndexHighlighterPredicate implements HighlightPredicate {
  /**
   * Instantiates a new index highlighter predicate.
   */
  public IndexHighlighterPredicate() {
  }

  /* (non-Javadoc)
   * @see org.jdesktop.swingx.decorator.HighlightPredicate#isHighlighted(java.awt.Component, org.jdesktop.swingx.decorator.ComponentAdapter)
   */
  @Override
  public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
    // always highlight the current index
    if (QueueModel.getIndex() == adapter.row) {
      return true;
    }
    return false;
  }
}