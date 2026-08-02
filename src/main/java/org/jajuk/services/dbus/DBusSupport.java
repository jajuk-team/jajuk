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
package org.jajuk.services.dbus;

import org.freedesktop.dbus.interfaces.DBusInterface;

/**
 * Interface defining methods exposed over D-Bus for Jajuk control.
 * Compatible with dbus-java 5.2 (hypfvieh)
 */
public interface DBusSupport extends DBusInterface {

  void previous() throws Exception;
  void next() throws Exception;
  void rewind() throws Exception;
  void playPause() throws Exception;
  void stop() throws Exception;
  void forward() throws Exception;
  void exit() throws Exception;
  void shuffleGlobal() throws Exception;
  void previousAlbum() throws Exception;
  void nextAlbum() throws Exception;
  void increaseVolume() throws Exception;
  void decreaseVolume() throws Exception;
  void mute() throws Exception;
  String currentHTML() throws Exception;
  String current() throws Exception;
  void banCurrent() throws Exception;
  void showCurrentlyPlaying() throws Exception;
  void bookmarkCurrentlyPlaying() throws Exception;
}