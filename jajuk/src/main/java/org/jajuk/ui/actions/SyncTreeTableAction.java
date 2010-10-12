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
import java.util.Properties;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.widgets.JajukToggleButton;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Action of requiring table and tree synchronization.
 * <br>When called against a tree view, selecting an item in a table view will expand and scroll the item in tree.
 * <br>When called against a table view, selecting an item in a tree view will filter the table accordingly.
 */
public class SyncTreeTableAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * @param name
   * @param icon
   * @param enabled
   */
  protected SyncTreeTableAction() {
    super(Messages.getString("SyncTreeTableAction.0"), IconLoader
        .getIcon(JajukIcons.TREE_TABLE_SYNC), true);
    setShortDescription(Messages.getString("SyncTreeTableAction.1"));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) throws Exception {
    JajukToggleButton source = (JajukToggleButton) evt.getSource();
    String currentViewID = (String) (source.getClientProperty(Const.DETAIL_VIEW));
    // Store the new sync state into the view-level property
    Conf.setProperty(Const.CONF_SYNC_TABLE_TREE + "." + currentViewID, Boolean.toString(source
        .isSelected()));
    // If the sync button is deselected, we force the tables to refresh to un-filtered state
    // We use the RATE_CHANGED event because this event force table views
    // refreshing but not the tree view's one.
    if (!source.isSelected()) {
      ObservationManager.notify(new JajukEvent(JajukEvents.RATE_CHANGED));
    }
    // Re-apply last interesting events so effect is token live
    Properties detailsTableEvent = ObservationManager
        .getDetailsLastOccurence(JajukEvents.TABLE_SELECTION_CHANGED);
    if (detailsTableEvent != null) {
      ObservationManager.notify(new JajukEvent(JajukEvents.TABLE_SELECTION_CHANGED,
          detailsTableEvent));
    }
    Properties detailsTreeEvent = ObservationManager
        .getDetailsLastOccurence(JajukEvents.TREE_SELECTION_CHANGED);
    if (detailsTableEvent != null) {
      ObservationManager
          .notify(new JajukEvent(JajukEvents.TREE_SELECTION_CHANGED, detailsTreeEvent));
    }
  }

}
