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

import org.jajuk.util.Util;

/**
 * A duration encapsulates a track or album length in secs, it is mainly used in
 * tables to ease the cell renderers recognition
 */
public class Duration implements Comparable<Duration> {
  private long duration;

  /**
   * 
   * @param duration
   *          item duration in secs
   */
  public Duration(long duration) {
    this.duration = duration;
  }

  /**
   * Return a string representation of this duration with zero paddings
   */
  public String toString() {
    return Util.formatTimeBySec(duration, false);
  }

  public int compareTo(Duration other) {
    return (int) (duration - other.getDuration());
  }

  public long getDuration() {
    return this.duration;
  }
}
