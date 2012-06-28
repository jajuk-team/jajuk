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
package org.jajuk.services.cddb;

import entagged.freedb.FreedbTrack;

import org.jajuk.base.Track;

/**
 * A CDDB track.
 */
public class CDDBTrack implements FreedbTrack {
  Track track;

  /**
   * Instantiates a new cDDB track.
   * 
   * @param track 
   */
  public CDDBTrack(Track track) {
    this.track = track;
  }

  /* (non-Javadoc)
   * @see entagged.freedb.FreedbTrack#getLength()
   */
  @Override
  public int getLength() {
    return (int) track.getDuration();
  }

  /* (non-Javadoc)
   * @see entagged.freedb.FreedbTrack#getPreciseLength()
   */
  @Override
  public float getPreciseLength() {
    return track.getDuration();
  }

  /**
   * Gets the track.
   * 
   * @return the track
   */
  public Track getTrack() {
    return track;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "CDDB: " + track.toString();
  }
}