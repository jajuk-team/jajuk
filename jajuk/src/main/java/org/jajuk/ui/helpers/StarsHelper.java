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

import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.Track;
import org.jajuk.ui.widgets.StarIconLabel;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;

/**
 * Manages stars against a item.
 * Stars are only visible from the GUI, they are never stored into collection itself
 */
public class StarsHelper {

  /** Cache iconLabel for each different rate to save memory and CPU. Map at index 0 stores banned tracks. */
  @SuppressWarnings("unchecked")
  private static Map map[] = { new HashMap<Long, StarIconLabel>(),
      new HashMap<Long, StarIconLabel>(), new HashMap<Long, StarIconLabel>(),
      new HashMap<Long, StarIconLabel>(), new HashMap<Long, StarIconLabel>(),
      new HashMap<Long, StarIconLabel>() };

  /**
   * Gets the icon.
   * 
   * @param starsNumber DOCUMENT_ME
   * 
   * @return the icon
   */
  static public ImageIcon getIcon(int starsNumber) {
    switch (starsNumber) {
    case -1:
      return IconLoader.getIcon(JajukIcons.BAN);
    case 0:
      return IconLoader.getIcon(JajukIcons.STAR_0);
    case 1:
      return IconLoader.getIcon(JajukIcons.STAR_1);
    case 2:
      return IconLoader.getIcon(JajukIcons.STAR_2);
    case 3:
      return IconLoader.getIcon(JajukIcons.STAR_3);
    case 4:
      return IconLoader.getIcon(JajukIcons.STAR_4);
    default:
      return null;
    }
  }

  /**
   * Gets the stars number.
   * 
   * @param item DOCUMENT_ME
   * 
   * @return Number of stars based on the rate of this item
   */
  static public int getStarsNumber(Item item) {
    long lInterval = 1;
    if (item instanceof Track) {
      lInterval = 100;
    } else if ((item instanceof Album) || (item instanceof Playlist)) {
      lInterval = AlbumManager.getInstance().getMaxRate();
    }
    lInterval = lInterval / 4;
    long lRate = item.getRate();
    if (lRate == 0) {
      return 0;
    } else if (lRate <= lInterval) {
      return 1;
    } else if (lRate <= 2 * lInterval) {
      return 2;
    } else if (lRate <= 3 * lInterval) {
      return 3;
    } else {
      return 4;
    }
  }

  /**
   * Gets the stars.
   * 
   * @param item DOCUMENT_ME
   * 
   * @return the stars icon or ban icon if banned
   */
  @SuppressWarnings("unchecked")
  static public StarIconLabel getStarIconLabel(Item item) {
    long rate = item.getRate();
    StarIconLabel sil = null;
    int starsNumber;
    if (item instanceof Track && item.getBooleanValue(Const.XML_TRACK_BANNED)) {
      starsNumber = -1;
    } else {
      starsNumber = getStarsNumber(item);
    }
    sil = (StarIconLabel) map[starsNumber + 1].get(rate);
    if (sil == null) {
      sil = new StarIconLabel(getIcon(starsNumber), "", null, null, null, (int) rate, starsNumber);
      map[starsNumber + 1].put(rate, sil);
    }
    return sil;
  }

}
