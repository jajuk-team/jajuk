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
public class StackItem implements Cloneable {

  /** Associated file */
  private final File file;

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
  @Override
  public Object clone() throws CloneNotSupportedException {
    try {
      StackItem item = new StackItem(file, bRepeat, bUserLaunch);
      item.setPlanned(bPlanned);

      return item;
    } catch (JajukException je) { // can be thrown if FileManager return a
      // null file
      Log.error(je);
      return null;
    }
  }

  /**
   * Equals method
   * 
   * @return whether both items are equals. Condition : file is the same and
   *         planned flag is the same
   */
  @Override
  public boolean equals(Object o) {
    // also includes check on null...
    if (!(o instanceof StackItem)) {
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
   * Hash code method to go along with equals
   * 
   */
  @Override
  public int hashCode() {
    // ignore three boolean parameters for now and just use the file as
    // hashcode...
    return file.hashCode();
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return file.toString();
  }

}
