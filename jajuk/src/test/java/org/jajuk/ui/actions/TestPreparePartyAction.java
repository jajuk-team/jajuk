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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.actions;

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.apache.commons.io.FileUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * 
 */
public class TestPreparePartyAction extends JajukTestCase {

  /**
   * Test method for
   * {@link org.jajuk.ui.actions.PreparePartyAction#perform(java.awt.event.ActionEvent)}
   * .
   */
  public void testPerform() {
    new PreparePartyAction();
  }

  /**
   * Test method for
   * {@link org.jajuk.ui.actions.PreparePartyAction#PreparePartyAction()}.
   * 
   * @throws Exception
   */
  public void testPreparePartyAction() throws Exception {
    PreparePartyAction action = new PreparePartyAction();

    // once with empty properties, this is called from the menu
    try {
      action.perform(new ActionEvent(new JButton(), 1, null));
    } catch (HeadlessException e) {
      // happens when tests are run on servers without ui
    }

    // then with a single and with a list of playlists, this is called from the
    // PlaylistView
    JButton button = new JButton();
    button.putClientProperty(Const.DETAIL_SELECTION, getPlaylist(10, false));
    try {
      action.perform(new ActionEvent(button, 1, null)); // once with empty
      // properties
    } catch (HeadlessException e) {
      // happens when tests are run on servers without ui
    }

    List<Playlist> list = new ArrayList<Playlist>();
    list.add(getPlaylist(11, false));
    button.putClientProperty(Const.DETAIL_SELECTION, list);
    try {
      action.perform(new ActionEvent(button, 1, null)); // once with empty
      // properties
    } catch (HeadlessException e) {
      // happens when tests are run on servers without ui
    }
  }

  private static Playlist getPlaylist(int i, boolean register) throws Exception {
    Genre genre = JUnitHelpers.getGenre();
    Album album = JUnitHelpers.getAlbum("name", 23);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test

    Artist artist = JUnitHelpers.getArtist("name");
    Year year = JUnitHelpers.getYear(2000);

    Type type = JUnitHelpers.getType();
    Track track = TrackManager.getInstance().registerTrack("name", album, genre, artist, 120, year,
        1, type, 1);

    Device device = JUnitHelpers.getDevice();
    if (!device.isMounted()) {
      device.mount(true);
    }
    Directory dir = DirectoryManager.getInstance().registerDirectory(device);
    Log.debug("Dir: " + dir.getFio());
    dir.getFio().mkdirs();

    java.io.File f = java.io.File.createTempFile("jajukFile", ".mp3", dir.getFio());

    File file = FileManager.getInstance().registerFile(Integer.valueOf(i).toString(), f.getName(),
        dir, track, 120, 70);

    final Playlist list;
    if (register) {
      list = PlaylistManager.getInstance().registerPlaylistFile(
          "test-" + Integer.valueOf(i).toString(), "My Playlist-" + Integer.valueOf(i).toString(),
          dir);
    } else {
      list = new Playlist(Integer.valueOf(i).toString(), "New list", dir);
    }

    // write the playlist so we can add files to it
    if (!list.getFIO().exists()) {
      // just create an empty file, # is a comment here
      FileUtils.writeStringToFile(list.getFIO(), "#");
    }

    // add a file to the playlist
    list.addFile(file);

    return list;
  }
}
