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
package org.jajuk.ui.helpers;

import java.awt.Component;
import java.util.ArrayList;

import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.services.players.FIFO;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

/**
 * Jajuk playing highlighter used in some tables to change the cell background
 * if the item at given row is playing
 */
public class PlayHighlighterPredicate implements HighlightPredicate {

  private JajukTableModel model;

  public PlayHighlighterPredicate(JajukTableModel model) {
    this.model = model;
  }

  public boolean isHighlighted(Component renderer, ComponentAdapter adapter) {
    Item item = model.getItemAt(adapter.row);
    if (item instanceof File) {
      File file = (File) item;
      if (file.equals(FIFO.getInstance().getCurrentFile())) {
        return true;
      }
    } else if (item instanceof Track) {
      ArrayList<File> files = ((Track) item).getFiles();
      if (files.contains(FIFO.getInstance().getCurrentFile())) {
        return true;
      }
    }
    return false;
  }
}