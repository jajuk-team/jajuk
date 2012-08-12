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
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.wizard.CDDBWizard;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.filters.JajukPredicates;
import org.jajuk.util.log.Log;

/**
 * Find tags from CDDB on selection
 * <p>
 * Action emitter is responsible to ensure all items provided share the same
 * type
 * </p>
 * <p>
 * Selection data is provided using the swing properties DETAIL_SELECTION
 * </p>.
 */
public class CDDBSelectionAction extends SelectionAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -8078402652430413821L;

  /**
   * Instantiates a new cDDB selection action.
   */
  CDDBSelectionAction() {
    super(Messages.getString("TracksTreeView.34"), IconLoader.getIcon(JajukIcons.CDDB), true);
    setShortDescription(Messages.getString("TracksTreeView.34"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent e) throws Exception {
    try {
      CDDBSelectionAction.super.perform(e);
      // Check selection is not void
      if (selection.size() == 0) {
        return;
      }
      // - We perform here fast computation, no need to use a SwingWorker here.
      // Actual network call to freedb is done in the CDDBWizard class that uses
      // a SwingWorker.
      // Build a list of tracks from various items
      List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(selection, true);
      // Remove video tracks found (clips)
      CollectionUtils.filter(tracks, new JajukPredicates.NotVideoPredicate());
      // Display the wizard
      new CDDBWizard(tracks);
    } catch (Exception ex) {
      Log.error(ex);
    }
  }
}
