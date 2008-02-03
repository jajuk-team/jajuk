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
import java.util.List;

import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.LogicalItem;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

/**
 * Display properties action. Allows displaying properties on single or multiple
 * items of the same type.
 * <p>
 * Action emitter is responsible to ensure all items provided share the same
 * type
 * </p>
 * <p>
 * Selection data is provided using the swing properties DETAIL_SELECTION
 * </p>
 */
public class ShowPropertiesAction extends SelectionAction {

  private static final long serialVersionUID = -8078402652430413821L;

  ShowPropertiesAction() {
    super(Messages.getString("TracksTableView.14"), IconLoader.ICON_PROPERTIES, true);
    setShortDescription(Messages.getString("ShowPropertiesAction.0"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.ActionBase#perform(java.awt.event.ActionEvent)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void perform(ActionEvent e) throws Exception {
    super.perform(e);
    // If selection contains files, we have to show the tracks along with files
    if (selection.size() == 0) {
      return;
    }
    Item first = selection.get(0);
    if (first instanceof File) {
      List<Item> tracks = new ArrayList<Item>(selection.size());
      for (Item file : selection) {
        tracks.add(((File) file).getTrack());
      }
      new PropertiesWizard(selection, tracks);
    } else if (first instanceof Track) {
      new PropertiesWizard(selection);
    } else if (first instanceof LogicalItem || first instanceof Directory) {
      // Author, Album, Style... : display the dual properties panel: one for
      // the item itself, the other with all tracks
      List<Item> tracks = new ArrayList<Item>(selection.size());
      for (Item i : selection) {
        tracks.addAll(TrackManager.getInstance().getAssociatedTracks(i));
      }
      new PropertiesWizard(selection, tracks);
    } else {
      // All others types: just display the properties window
      new PropertiesWizard(selection);
    }
  }

}