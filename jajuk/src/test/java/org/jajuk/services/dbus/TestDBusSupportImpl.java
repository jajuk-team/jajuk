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
package org.jajuk.services.dbus;

import cx.ath.matthew.unix.UnixSocket;

import java.util.Properties;
import java.util.Set;

import org.jajuk.JajukTestCase;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * .
 */
public class TestDBusSupportImpl extends JajukTestCase {

  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // make sure this is initialized with Actions
    ActionManager.getInstance();

    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#connect()}.
   */

  public final void testConnect() {
    DBusSupportImpl impl = new DBusSupportImpl();

    // ensure that we do find the JNI-Library
    try {
      new UnixSocket();
    } catch (UnsatisfiedLinkError e) {
      Log.fatal("Could not load class UnixSocket, maybe the java.library.path is not set correctly: "
          + System.getProperty("java.library.path") + ": " + e.getMessage());
    }

    // will fail where dbus is not available and report an error to the log...
    impl.connect();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dbus.DBusSupportImpl#disconnect()}.
   */

  public final void testDisconnect() {
    DBusSupportImpl impl = new DBusSupportImpl();

    // should not fail when not connected
    impl.disconnect();

    // try to connect first
    impl.connect();
    // then disconnect
    impl.disconnect();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#forward()}.
   *
   * @throws Exception the exception
   */

  public final void testForward() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.forward();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#next()}.
   *
   * @throws Exception the exception
   */

  public final void testNext() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.next();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#playPause()}
   * .
   *
   * @throws Exception the exception
   */

  public final void testPlayPause() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.playPause();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#previous()}.
   *
   * @throws Exception the exception
   */

  public final void testPrevious() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.previous();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#rewind()}.
   *
   * @throws Exception the exception
   */

  public final void testRewind() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.rewind();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#stop()}.
   *
   * @throws Exception the exception
   */

  public final void testStop() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.stop();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.dbus.DBusSupportImpl#decreaseVolume()}.
   */

  public final void testDecreaseVolume() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.decreaseVolume();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#exit()}.
   */

  public final void testExit() {
    // cannot test this as it stops the process:
    // DBusSupportImpl impl = new DBusSupportImpl();
    // impl.exit();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.dbus.DBusSupportImpl#increaseVolume()}.
   */

  public final void testIncreaseVolume() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.increaseVolume();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#nextAlbum()}
   * .
   *
   * @throws Exception the exception
   */

  public final void testNextAlbum() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.nextAlbum();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.dbus.DBusSupportImpl#previousAlbum()}.
   */

  public final void testPreviousAlbum() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.previousAlbum();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.dbus.DBusSupportImpl#shuffleGlobal()}.
   */

  public final void testShuffleGlobal() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.shuffleGlobal();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#mute()}.
   *
   * @throws Exception the exception
   */

  public final void testMute() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.mute();
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.dbus.DBusSupportImpl#currentHTML()}.
   */

  public final void testCurrentHTML() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    assertNotNull(impl.currentHTML());
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#current()}.
   *
   * @throws Exception the exception
   */

  public final void testCurrent() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    assertNotNull(impl.current());
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.services.dbus.DBusSupportImpl#banCurrent()}.
   */

  public final void testBanCurrent() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.banCurrent();
  }

  /**
   * Test method for {@link org.jajuk.services.dbus.DBusSupportImpl#isRemote()}.
   */

  public final void testIsRemote() {
    DBusSupportImpl impl = new DBusSupportImpl();
    assertFalse(impl.isRemote());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dbus.DBusSupportImpl#getRegistrationKeys()}.
   */

  public final void testGetRegistrationKeys() {
    DBusSupportImpl impl = new DBusSupportImpl();
    Set<JajukEvents> events = impl.getRegistrationKeys();
    assertTrue(events.contains(JajukEvents.FILE_LAUNCHED));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dbus.DBusSupportImpl#update(org.jajuk.events.JajukEvent)}
   * .
   */

  public final void testUpdate() {
    Properties prop = new Properties();
    prop.setProperty(Const.DETAIL_CURRENT_FILE_ID, "testfile");

    DBusSupportImpl impl = new DBusSupportImpl();
    impl.update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
  }

  /**
   * Test update connect.
   * 
   */
  public final void testUpdateConnect() {
    Properties prop = new Properties();
    prop.setProperty(Const.DETAIL_CURRENT_FILE_ID, "testfile");

    DBusSupportImpl impl = new DBusSupportImpl();

    // will fail where dbus is not available and report an error to the log...
    impl.connect();

    impl.update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
  }

  /**
   * Test show currently playing.
   * 
   *
   * @throws Exception the exception
   */
  public final void testShowCurrentlyPlaying() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.showCurrentlyPlaying();
  }

  /**
   * Test bookmark currently playing.
   * 
   *
   * @throws Exception the exception
   */
  public final void testBookmarkCurrentlyPlaying() throws Exception {
    DBusSupportImpl impl = new DBusSupportImpl();
    impl.bookmarkCurrentlyPlaying();
  }
}
