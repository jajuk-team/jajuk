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
package org.jajuk.services.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * DOCUMENT_ME.
 */
public class DBusSignalImpl implements DBusInterface {

  /**
   * DOCUMENT_ME.
   */
  public static class FileChangedSignal extends DBusSignal {

    /** DOCUMENT_ME. */
    String filename;

    /**
     * The Constructor.
     *
     * @param filename DOCUMENT_ME
     * @param path DOCUMENT_ME
     * @param args DOCUMENT_ME
     * @throws DBusException the d bus exception
     */
    public FileChangedSignal(String filename, String path, Object... args) throws DBusException {
      super(path, args);
      this.filename = filename;
    }

    /**
     * Gets the filename.
     * 
     * @return the filename
     */
    public String getFilename() {
      return this.filename;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.freedesktop.dbus.DBusInterface#isRemote()
   */
  @Override
  public boolean isRemote() {
    return false;
  }

}
