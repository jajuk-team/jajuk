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
package org.jajuk.services.dbus;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * 
 */
public class DBusSignalImpl implements DBusInterface {


/**
   * 
   */
  public static class FileChangedSignal extends DBusSignal {
    String filename;

    /**
     * @param path
     * @param args
     * @param filename
     * @throws DBusException
     */
    public FileChangedSignal(String filename, String path, Object... args) throws DBusException {
      super(path, args);
      this.filename = filename;
    }
    
  }

/* (non-Javadoc)
   * @see org.freedesktop.dbus.DBusInterface#isRemote()
   */
  public boolean isRemote() {
    return false;
  }

  
}
