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
package org.jajuk.services.mpris;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * D-Bus interface definition for MPRIS Player methods.
 * This tells dbus-java which methods to expose remotely.
 */
@DBusInterfaceName("org.mpris.MediaPlayer2.Player")
public interface MprisPlayerInterface extends DBusInterface {

  void Next();

  void Previous();

  void Pause();

  void PlayPause();

  void Stop();

  void Play();

  void Seek(long offset);

  void SetPosition(String trackId, long position);
}