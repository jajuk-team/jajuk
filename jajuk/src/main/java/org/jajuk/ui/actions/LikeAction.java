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
 *  $$Revision: 4113 $$
 */
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.services.players.FIFO;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;

/**
 * Like action increase current played preference
 */
public class LikeAction extends JajukAction {

  private static final long serialVersionUID = 1L;

  LikeAction() {
    super(Messages.getString("LikeAction.0"), IconLoader.ICON_LIKE, true);
    setShortDescription(Messages.getString("LikeAction.1"));
  }

  @Override
  public void perform(ActionEvent evt) throws JajukException {
    File file = FIFO.getCurrentFile();
    if (file != null) {
      Track track = file.getTrack();
      long preference = track.getLongValue(XML_TRACK_PREFERENCE);
      if (preference < 3) {
        file.getTrack().setPreference(preference + 1);
      }
    }
  }
}
