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
 *  
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;

import org.jajuk.base.Playlist;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Action for saving as... an item
 */
public class SaveAsAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new save as action.
   */
  SaveAsAction() {
    super(Messages.getString("PhysicalPlaylistRepositoryView.2"), IconLoader
        .getIcon(JajukIcons.SAVE_AS), true);
  }

  /**
   * Invoked when an action occurs.
   * 
   * @param e 
   */
  @Override
  @SuppressWarnings("unchecked")
  public void perform(final ActionEvent e) {
    new Thread("SaveAsAction") {
      @Override
      public void run() {
        JComponent source = (JComponent) e.getSource();
        // TODO Do better here, accept a single playlist for ie
        Object o = source.getClientProperty(Const.DETAIL_SELECTION);
        Playlist playlist = null;
        try {
          if (o instanceof List) {
            playlist = ((List<Playlist>) o).get(0);
          } else {
            playlist = (Playlist) o;
          }
          playlist.saveAs();
          InformationJPanel.getInstance().setMessage(
              Messages.getString("AbstractPlaylistEditorView.22"),
              InformationJPanel.MessageType.INFORMATIVE);
          ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
        } catch (JajukException je) {
          Log.error(je);
          Messages.showErrorMessage(je.getCode());
        } catch (Exception ex) {
          Log.error(ex);
        }
      }
    }.start();
  }
}
