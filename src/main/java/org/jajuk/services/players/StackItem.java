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
package org.jajuk.services.players;

import org.jajuk.base.File;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * A FIFO item.
 */
public class StackItem implements Cloneable {
  /** Associated file. */
  private final File file;
  /** Repeat flag. */
  private boolean bRepeat;
  /** User launch flag. */
  private boolean bUserLaunch;
  /** Planned track ?. */
  private boolean bPlanned;

  /**
   * Constructor.
   *
   * @param file associated file
   *
   * @throws JajukException the jajuk exception
   */
  public StackItem(File file) throws JajukException {
    this(file, false, false);
  }

  /**
   * Constructor.
   *
   * @throws JajukException the jajuk exception
   */
  public StackItem(File file, boolean bUserLauched) throws JajukException {
    this(file, false, bUserLauched);
  }

  /**
   * Constructor.
   *
   * @throws JajukException the jajuk exception
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
   * Checks if is repeat.
   *
   * @return Returns the bRepeat.
   */
  public boolean isRepeat() {
    return bRepeat;
  }

  /**
   * Sets the repeat.
   *
   * @param repeat The bRepeat to set.
   */
  public void setRepeat(boolean repeat) {
    bRepeat = repeat;
  }

  /**
   * Gets the file.
   *
   * @return Returns the file.
   */
  public File getFile() {
    return file;
  }

  /**
   * Checks if is user launch.
   *
   * @return Returns the bUserLaunch.
   */
  public boolean isUserLaunch() {
    return bUserLaunch;
  }

  /**
   * Sets the user launch.
   *
   * @param userLaunch The bUserLaunch to set.
   */
  public void setUserLaunch(boolean userLaunch) {
    bUserLaunch = userLaunch;
  }

  /**
   * Checks if is planned.
   *
   * @return Returns the bPlanned.
   */
  public boolean isPlanned() {
    return bPlanned;
  }

  /**
   * Sets the planned.
   *
   * @param planned The bPlanned to set.
   */
  public void setPlanned(boolean planned) {
    bPlanned = planned;
  }

  /**
   * Clone method.
   *
   * @return a cloned stack item
   *
   * @throws CloneNotSupportedException the clone not supported exception
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
   * Equals method.
   *
   * @return whether both items are equals. Condition : file is the same and
   * planned flag is the same
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
   * Hash code method to go along with equals.
   *
   * @return the int
   */
  @Override
  public int hashCode() {
    // ignore three boolean parameters for now and just use the file as
    // hashcode...
    return file.hashCode();
  }

  /**
   * toString method.
   *
   * @return the string
   */
  @Override
  public String toString() {
    return file.toString();
  }
}
