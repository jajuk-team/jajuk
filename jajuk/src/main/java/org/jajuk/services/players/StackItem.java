/*
 *  Jajuk
 *  Copyright (C) 2004 The Jajuk Team
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
 *  $Revision: 3156 $
 */

package org.jajuk.services.players;

import org.jajuk.base.File;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * A FIFO item
 */
public class StackItem {

  /** Associated file */
  private File file;

  /** Repeat flag */
  private boolean bRepeat = false;

  /** User launch flag */
  private boolean bUserLaunch = false;

  /** Planned track ? */
  private boolean bPlanned = false;

  /**
   * Constructor
   * 
   * @param file
   *          associated file
   */
  public StackItem(File file) throws JajukException {
    if (file == null) {
      throw new JajukException(0);
    }
    this.file = file;
  }

  /**
   * Constructor
   * 
   * @param file
   * @param bUserLauched
   */
  public StackItem(File file, boolean bUserLauched) throws JajukException {
    this(file, false, bUserLauched);
  }

  /**
   * Constructor
   * 
   * @param file
   * @param bUserLauched
   */
  public StackItem(File file, boolean bRepeat, boolean bUserLauched) throws JajukException {
    if (file == null) {
      throw new JajukException(0);
    }
    this.file = file;
    this.bRepeat = bRepeat;
    this.bUserLaunch = bUserLauched;
    this.bPlanned = false;
  }

  /**
   * @return Returns the bRepeat.
   */
  public boolean isRepeat() {
    return bRepeat;
  }

  /**
   * @param repeat
   *          The bRepeat to set.
   */
  public void setRepeat(boolean repeat) {
    bRepeat = repeat;
  }

  /**
   * @return Returns the file.
   */
  public File getFile() {
    return file;
  }

  /**
   * @return Returns the bUserLaunch.
   */
  public boolean isUserLaunch() {
    return bUserLaunch;
  }

  /**
   * @param userLaunch
   *          The bUserLaunch to set.
   */
  public void setUserLaunch(boolean userLaunch) {
    bUserLaunch = userLaunch;
  }

  /**
   * @return Returns the bPlanned.
   */
  public boolean isPlanned() {
    return bPlanned;
  }

  /**
   * @param planned
   *          The bPlanned to set.
   */
  public void setPlanned(boolean planned) {
    bPlanned = planned;
  }

  /**
   * Clone method
   * 
   * @return a clonned stack item
   */
  public Object clone() {
    StackItem item = null;
    try {
      item = new StackItem(file, bRepeat, bUserLaunch);
      item.setPlanned(bPlanned);
    } catch (JajukException je) { // can be thrown if FileManager return a
      // null file
      Log.error(je);
      item = null;
    }
    return item;
  }

  /**
   * Equals method
   * 
   * @return whether both items are equals. Condition : file is the same and
   *         planned flag is the same
   */
  public boolean equals(Object o) {
    if (!(o instanceof StackItem) || o == null) {
      return false;
    }
    StackItem itemOther = (StackItem) o;
    File fOther = itemOther.getFile();
    if (fOther == null || file == null) {
      return false;
    }
    return (fOther.equals(file) && itemOther.isPlanned() == isPlanned());
  }

  /**
   * toString method
   */
  public String toString() {
    return file.toString();
  }

}
