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
package org.jajuk.ui.helpers;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.jajuk.TestHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.services.notification.NotificatorTypes;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.webradio.WebRadioOrigin;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestPlayerStateMediator extends JajukTestCase {
  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // to install actions...
    ActionManager.getInstance();
    super.setUp();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.helpers.PlayerStateMediator#getInstance()}.
   */
  public final void testGetInstance() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    assertNotNull(med);
    // once again to cover other if-branch
    med = PlayerStateMediator.getInstance();
    assertNotNull(med);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.helpers.PlayerStateMediator#getRegistrationKeys()}.
   */
  public final void testGetRegistrationKeys() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    Set<JajukEvents> set = med.getRegistrationKeys();
    assertTrue(set.toString(), set.contains(JajukEvents.PLAYER_PLAY));
    assertTrue(set.toString(), set.contains(JajukEvents.VOLUME_CHANGED));
    assertTrue(set.toString(), set.contains(JajukEvents.MUTE_STATE));
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.ui.helpers.PlayerStateMediator#update(org.jajuk.events.JajukEvent)}
   * .
   */
  public final void testUpdatePlay() throws Exception {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAYER_PLAY, null));
  }

  /**
   * Test update stop.
   * 
   */
  public final void testUpdateStop() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAYER_STOP, null));
  }

  /**
   * Test update stop queue model.
   * 
   *
   * @throws Exception the exception
   */
  public final void testUpdateStopQueueModel() throws Exception {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    // test with queue size > 0
    Device device = TestHelpers.getDevice();
    // no files without a directory
    List<File> files = device.getFilesRecursively();
    assertEquals(0, files.size()); // no file available
    Directory dir = DirectoryManager.getInstance().registerDirectory(device);
    File file = getFile(9, dir);
    QueueModel.insert(new StackItem(file), 0);
    assertTrue(QueueModel.getQueue().toString(), QueueModel.getQueue().size() > 0);
    // run the method
    med.update(new JajukEvent(JajukEvents.PLAYER_STOP, null));
  }

  /**
   * Gets the file.
   *
   * @param i 
   * @param dir 
   * @return the file
   */
  private File getFile(int i, Directory dir) {
    Genre genre = TestHelpers.getGenre();
    Album album = TestHelpers.getAlbum("name", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test
    Artist artist = TestHelpers.getArtist("name");
    Year year = TestHelpers.getYear(2000);
    Type type = TestHelpers.getType();
    Track track = TrackManager.getInstance().registerTrack("name", album, genre, artist, 120, year,
        1, type, 1);
    return FileManager.getInstance().registerFile(Integer.valueOf(i).toString(), "test.tst", dir,
        track, 120, 70);
  }

  /**
   * Test update paused.
   * 
   */
  public final void testUpdatePaused() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAYER_PAUSE, null));
  }

  /**
   * Test update resume.
   * 
   */
  public final void testUpdateResume() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAYER_RESUME, null));
  }

  /**
   * Test update opening error.
   * 
   */
  public final void testUpdateOpeningError() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAY_OPENING, null));
  }

  /**
   * Test update zero.
   * 
   */
  public final void testUpdateZero() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.ZERO, null));
  }

  /**
   * Test update webradio.
   * 
   */
  public final void testUpdateWebradio() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED, null));
  }

  /**
   * Test update webradio notifcator.
   * 
   */
  public final void testUpdateWebradioNotifcator() {
    // enable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_NOTIFICATOR_TYPE, NotificatorTypes.TOAST.name());
    Properties prop = new Properties();
    prop.put(Const.DETAIL_CONTENT,
        TestHelpers.getWebRadio("myradio", "http://foo", WebRadioOrigin.CUSTOM));
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED, prop));
  }

  /**
   * Test update file launched.
   * 
   *
   * @throws Exception the exception
   */
  public final void testUpdateFileLaunched() throws Exception {
    // enable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_NOTIFICATOR_TYPE, NotificatorTypes.TOAST.name());
    Directory dir = TestHelpers.getDirectory();
    File file = getFile(3, dir);
    Properties prop = new Properties();
    prop.put(Const.DETAIL_CURRENT_FILE_ID, file.getID());
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.FILE_LAUNCHED, prop));
    TestHelpers.clearSwingUtilitiesQueue();
  }

  /**
   * Test update file launched null.
   * 
   */
  public final void testUpdateFileLaunchedNull() {
    // enable Tooltip/Notification
    Conf.setProperty(Const.CONF_UI_NOTIFICATOR_TYPE, NotificatorTypes.TOAST.name());
    // just provide empty properties
    Properties prop = new Properties();
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.FILE_LAUNCHED, prop));
  }

  /**
   * Test update volume.
   * 
   */
  public final void testUpdateVolume() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.VOLUME_CHANGED, null));
  }

  /**
   * Test update mute.
   * 
   *
   * @throws Exception the exception
   */
  public final void testUpdateMute() throws Exception {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.MUTE_STATE, null));
    TestHelpers.clearSwingUtilitiesQueue();
    // test with muted player
    Player.mute();
    med.update(new JajukEvent(JajukEvents.MUTE_STATE, null));
    TestHelpers.clearSwingUtilitiesQueue();
    Player.mute(false);
    med.update(new JajukEvent(JajukEvents.MUTE_STATE, null));
  }
}
