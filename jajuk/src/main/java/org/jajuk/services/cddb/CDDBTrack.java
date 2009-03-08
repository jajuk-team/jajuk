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
package org.jajuk.services.cddb;

import entagged.freedb.FreedbTrack;

import org.jajuk.base.Track;

/**
 * A CDDB track
 */
public class CDDBTrack implements FreedbTrack {

  Track track;

  public CDDBTrack(Track track) {
    this.track = track;
  }

  public int getLength() {
    return (int) track.getDuration();

  }

  public float getPreciseLength() {
    return track.getDuration();
  }

  public Track getTrack() {
    return track;
  }
  
  @Override
  public String toString() {
    return "CDDB: " + track.toString();
  }

}