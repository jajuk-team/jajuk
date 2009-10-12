/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.helpers;

import java.util.List;
import java.util.Set;

import org.jajuk.JajukTestCase;

import org.jajuk.JUnitHelpers;
import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.base.TestAlbumManager.MockPlayer;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestPlayerStateMediator extends JajukTestCase {

  @Override
  protected void setUp() throws Exception {
    // to install actions...
    ActionManager.getInstance();

    super.setUp();
  }

  /**
   * Test method for
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
   * Test method for
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
   * Test method for
   * {@link org.jajuk.ui.helpers.PlayerStateMediator#update(org.jajuk.events.JajukEvent)}.
   * 
   * @throws Exception
   */
  public final void testUpdatePlay() throws Exception {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAYER_PLAY, null));
  }

  public final void testUpdateStop() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAYER_STOP, null));
  }

  public final void testUpdateStopQueueModel() throws Exception {
    PlayerStateMediator med = PlayerStateMediator.getInstance();

    // test with queue size > 0
    Device device = new Device("1", "name");
    device.setUrl(System.getProperty("java.io.tmpdir"));

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

  @SuppressWarnings("unchecked")
  private File getFile(int i, Directory dir) {
    Style style = new Style(Integer.valueOf(i).toString(), "name");
    Album album = new Album(Integer.valueOf(i).toString(), "name", "artis", 23);
    album.setProperty(Const.XML_ALBUM_COVER, "none"); // don't read covers for
    // this test

    Author author = new Author(Integer.valueOf(i).toString(), "name");
    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

    Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", cl, null);
    Track track = new Track(Integer.valueOf(i).toString(), "name", album, style, author, 120, year,
        1, type, 1);

    return FileManager.getInstance().registerFile(Integer.valueOf(i).toString(), "test.tst", dir,
        track, 120, 70);
  }

  public final void testUpdatePaused() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAYER_PAUSE, null));
  }

  public final void testUpdateResume() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAYER_RESUME, null));
  }

  public final void testUpdateOpeningError() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.PLAY_OPENING, null));
  }

  public final void testUpdateZero() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.ZERO, null));
  }

  public final void testUpdateWebradio() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED, null));
  }

  public final void testUpdateVolume() {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.VOLUME_CHANGED, null));
  }

  public final void testUpdateMute() throws Exception {
    PlayerStateMediator med = PlayerStateMediator.getInstance();
    med.update(new JajukEvent(JajukEvents.MUTE_STATE, null));
    JUnitHelpers.clearSwingUtilitiesQueue();

    // test with muted player
    Player.mute();

    med.update(new JajukEvent(JajukEvents.MUTE_STATE, null));
    JUnitHelpers.clearSwingUtilitiesQueue();
    
    Player.mute(false);

    med.update(new JajukEvent(JajukEvents.MUTE_STATE, null));
  }
}
