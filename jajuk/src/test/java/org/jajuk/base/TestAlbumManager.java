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
package org.jajuk.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.services.covers.Cover;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.tags.ITagImpl;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Const;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;

/**
 *
 */
public class TestAlbumManager extends JajukTestCase {

  /*
   * (non-Javadoc)
   *
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.base.AlbumManager#getLabel()}.
   */
  public void testGetLabel() {
    assertEquals(Const.XML_ALBUMS, AlbumManager.getInstance().getLabel());
  }

  /**
   * Test method for {@link org.jajuk.base.AlbumManager#getRegistrationKeys()}.
   */
  public void testGetRegistrationKeys() {
    Set<JajukEvents> events = AlbumManager.getInstance().getRegistrationKeys();
    assertTrue(events.contains(JajukEvents.FILE_LAUNCHED));
  }

  /**
   * Test method for {@link org.jajuk.base.AlbumManager#getInstance()}.
   */
  public void testGetInstance() {
    assertNotNull(AlbumManager.getInstance());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#createID(java.lang.String, java.lang.String, long)}
   * .
   */
  public void testCreateID() {
    assertNotNull(AlbumManager.createID("name", 3));
    // other cases...
    assertNotNull(AlbumManager.createID("name", 0));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#registerAlbum(java.lang.String, java.lang.String, long)}
   * .
   */
  public void testRegisterAlbumStringStringStringLong() {
    assertNotNull(AlbumManager.getInstance().registerAlbum("1", "name1", 1));

    assertNotNull(AlbumManager.getInstance().getAlbumByID("1"));
  }

  public void testRegisterAlbumEmptyArtist() {
    AlbumManager.getInstance().clear();

    assertNotNull(AlbumManager.getInstance().registerAlbum("1", "name1", 1));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#registerAlbum(java.lang.String, java.lang.String, long)}
   * .
   */
  public void testRegisterAlbumStringStringLong() {
    assertNotNull(AlbumManager.getInstance().registerAlbum("2", "name2", 1));

    assertNotNull(AlbumManager.getInstance().getAlbumByName("name2"));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#changeAlbumName(org.jajuk.base.Album, java.lang.String)}
   * .
   *
   * @throws Exception
   */
  public void testChangeAlbumName() throws Exception {
    StartupCollectionService.registerItemManagers();

    Album album = AlbumManager.getInstance().registerAlbum("name3", 1);
    assertNotNull(album);

    AlbumManager.getInstance().changeAlbumName(album, "name4");

    assertNull(AlbumManager.getInstance().getAlbumByName("name3"));
    assertNotNull(AlbumManager.getInstance().getAlbumByName("name4"));
  }

  public void testChangeAlbumNameWithTrack() throws Exception {
    Album album = AlbumManager.getInstance().registerAlbum("name3", 1);
    assertNotNull(album);

    getTrack(5, album);
    getTrack(6, album);

    AlbumManager.getInstance().changeAlbumName(album, "name4");

    assertNull(AlbumManager.getInstance().getAlbumByName("name3"));
    assertNotNull(AlbumManager.getInstance().getAlbumByName("name4"));
  }

  public void testChangeAlbumNameWithQueue() throws Exception {
    StartupCollectionService.registerItemManagers();

    Album album = AlbumManager.getInstance().registerAlbum("name3", 1);
    assertNotNull(album);

    getTrack(5, album);
    getTrack(6, album);

    QueueModel.insert(new StackItem(album.getTracksCache().get(0).getFiles().get(0)), 0);
    QueueModel.goTo(0);

    AlbumManager.getInstance().changeAlbumName(album, "name4");

    assertNull(AlbumManager.getInstance().getAlbumByName("name3"));
    assertNotNull(AlbumManager.getInstance().getAlbumByName("name4"));
  }

  public void testChangeAlbumNameSameName() throws Exception {
    StartupCollectionService.registerItemManagers();

    Album album = AlbumManager.getInstance().registerAlbum("3", "name3", 1);
    assertNotNull(album);

    // nothing happens if we use the same name
    Album album2 = AlbumManager.getInstance().changeAlbumName(album, "name3");

    // we expect the same physical item
    assertTrue(album2.toString(), album == album2);
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#format(java.lang.String)}.
   */
  public void testFormat() {
    Album album = AlbumManager.getInstance().registerAlbum("3", "name3", 1);
    assertNotNull(album);

    assertEquals("Name1", AlbumManager.format("name1"));
    assertEquals("Name1", AlbumManager.format(" name1"));
    assertEquals("Name1", AlbumManager.format("name1 "));
    assertEquals("Name1", AlbumManager.format(" name1   "));
    assertEquals("   name1", AlbumManager.format("--_name1"));
    assertEquals("Na  me1", AlbumManager.format("na-_me1"));
    assertEquals("Name1 name2", AlbumManager.format("name1 name2"));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#getAlbumByID(java.lang.String)}.
   */
  public void testGetAlbumByID() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.base.AlbumManager#getAlbums()}.
   */
  public void testGetAlbums() {
    AlbumManager.getInstance().clear();

    assertNotNull(AlbumManager.getInstance().registerAlbum("1", "name1", 1));
    assertNotNull(AlbumManager.getInstance().registerAlbum("2", "name2", 1));

    assertEquals(2, AlbumManager.getInstance().getAlbums().size());
  }

  /**
   * Test method for {@link org.jajuk.base.AlbumManager#getAlbumsIterator()}.
   */
  public void testGetAlbumsIterator() {
    AlbumManager.getInstance().clear();

    assertNotNull(AlbumManager.getInstance().registerAlbum("1", "name1", 1));
    assertNotNull(AlbumManager.getInstance().registerAlbum("2", "name2", 1));

    assertTrue(AlbumManager.getInstance().getAlbumsIterator().hasNext());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#getAssociatedAlbums(org.jajuk.base.Item)}
   * .
   *
   * @throws Exception
   */
  public void testGetAssociatedAlbums() throws Exception {
    Album album = AlbumManager.getInstance().registerAlbum("1", "name1", 1);

    Track track = getTrack(1, album);

    List<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(track);
    assertNotNull(albums);
    assertEquals(albums.toString(), 1, albums.size());

    Genre genre = track.getGenre();
    Artist artist = track.getArtist();
    Year year = track.getYear();

    albums = AlbumManager.getInstance().getAssociatedAlbums(genre);
    assertNotNull(albums);
    assertEquals(albums.toString(), 1, albums.size());

    albums = AlbumManager.getInstance().getAssociatedAlbums(artist);
    assertNotNull(albums);
    assertEquals(albums.toString(), 1, albums.size());

    albums = AlbumManager.getInstance().getAssociatedAlbums(year);
    assertNotNull(albums);
    assertEquals(albums.toString(), 1, albums.size());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#getBestOfAlbums(boolean, int)}.
   *
   * @throws Exception
   */
  public void testGetBestOfAlbums() throws Exception {
    AlbumManager.getInstance().clear();

    Album album = AlbumManager.getInstance().registerAlbum("1", "name1", 1);
    getTrack(2, album);
    getTrack(3, album);

    album = AlbumManager.getInstance().registerAlbum("2", "name2", 1);
    getTrack(4, album);
    getTrack(5, album);

    album = AlbumManager.getInstance().registerAlbum("name3", 1);
    getTrack(4, album);
    getTrack(5, album);

    album = AlbumManager.getInstance().registerAlbum("name4", 1);
    getTrack(4, album);
    getTrack(5, album);

    album = AlbumManager.getInstance().registerAlbum("name5", 1);
    getTrack(4, album);
    getTrack(5, album);

    List<Album> albums = AlbumManager.getInstance().getBestOfAlbums(false, 3);
    assertEquals(albums.toString(), 3, albums.size());

    // TODO verify with many albums and actual data
  }

  public void testGetBestOfAlbumsLess() throws Exception {
    AlbumManager.getInstance().clear();

    Album album = AlbumManager.getInstance().registerAlbum("1", "name1", 1);
    getTrack(2, album);
    getTrack(3, album);

    album = AlbumManager.getInstance().registerAlbum("2", "name2", 1);
    getTrack(4, album);
    getTrack(5, album);

    album = AlbumManager.getInstance().registerAlbum("name3", 1);
    getTrack(4, album);
    getTrack(5, album);

    album = AlbumManager.getInstance().registerAlbum("name4", 1);
    getTrack(4, album);
    getTrack(5, album);

    album = AlbumManager.getInstance().registerAlbum("name5", 1);
    getTrack(4, album);
    getTrack(5, album);

    // check if we request more than we have, currently we get back some less
    // for some reason...
    List<Album> albums = AlbumManager.getInstance().getBestOfAlbums(false, 10);
    assertEquals(albums.toString(), 4, albums.size());
  }

  public void testGetBestOfAlbumZero() {
    AlbumManager.getInstance().clear();

    List<Album> albums = AlbumManager.getInstance().getBestOfAlbums(false, 3);
    assertEquals(albums.toString(), 0, albums.size());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#getNewestAlbums(boolean, int)}.
   *
   * @throws Exception
   */
  public void testGetNewestAlbums() throws Exception {
    Album album = AlbumManager.getInstance().registerAlbum("name1", 1);
    getTrack(2, album);
    getTrack(3, album);

    album = AlbumManager.getInstance().registerAlbum("name2", 1);
    getTrack(4, album);
    getTrack(5, album);

    album = AlbumManager.getInstance().registerAlbum("name3", 1);
    getTrack(6, album);
    getTrack(7, album);

    album = AlbumManager.getInstance().registerAlbum("name4", 1);
    getTrack(8, album);
    getTrack(9, album);

    album = AlbumManager.getInstance().registerAlbum("name5", 1);
    getTrack(10, album);
    getTrack(11, album);

    List<Album> albums = AlbumManager.getInstance().getNewestAlbums(false, 3);
    assertEquals(albums.toString(), 3, albums.size());

    // TODO verify with many albums and actual data
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#getRarelyListenAlbums(boolean, int)}.
   *
   * @throws Exception
   */
  public void testGetRarelyListenAlbums() throws Exception {
    AlbumManager.getInstance().clear();

    Album album = AlbumManager.getInstance().registerAlbum("name1", 1);
    getTrack(2, album);
    getTrack(3, album);

    album = AlbumManager.getInstance().registerAlbum("name2", 1);
    getTrack(4, album);
    getTrack(5, album);

    album = AlbumManager.getInstance().registerAlbum("name3", 1);
    getTrack(6, album);
    getTrack(7, album);

    album = AlbumManager.getInstance().registerAlbum("name4", 1);
    getTrack(8, album);
    getTrack(9, album);

    album = AlbumManager.getInstance().registerAlbum("name5", 1);
    getTrack(10, album);
    getTrack(11, album);

    List<Album> albums = AlbumManager.getInstance().getRarelyListenAlbums(false, 3);
    assertEquals(albums.toString(), 3, albums.size());

    // TODO verify with many albums and actual data
  }

  /**
   * Test method for {@link org.jajuk.base.AlbumManager#getMaxRate()}.
   *
   * @throws Exception
   */
  public void testGetMaxRate() throws Exception {
    AlbumManager.getInstance().clear();

    Album album = AlbumManager.getInstance().registerAlbum("name1", 1);
    Track track = getTrack(1, album);
    track.setRate(12);
    track = getTrack(2, album);
    track.setRate(14);

    album = AlbumManager.getInstance().registerAlbum("name2", 1);
    track = getTrack(3, album);
    track.setRate(10);
    track = getTrack(4, album);
    track.setRate(15);

    album = AlbumManager.getInstance().registerAlbum("name3", 1);
    track = getTrack(5, album);
    track.setRate(10);
    track = getTrack(6, album);
    track.setRate(15);

    album = AlbumManager.getInstance().registerAlbum("name4", 1);
    track = getTrack(7, album);
    track.setRate(10);
    track = getTrack(8, album);
    track.setRate(15);

    album = AlbumManager.getInstance().registerAlbum("name5", 1);
    track = getTrack(9, album);
    track.setRate(10);
    track = getTrack(10, album);
    track.setRate(15);

    // zero without refresh
    assertEquals(0, AlbumManager.getInstance().getMaxRate());

    // refresh it now
    AlbumManager.getInstance().refreshMaxRating();

    // now it should be ok, it is the combined rate of all tracks
    assertEquals(26, AlbumManager.getInstance().getMaxRate());
  }

  /**
   * Test method for {@link org.jajuk.base.AlbumManager#refreshMaxRating()}.
   */
  public void testRefreshMaxRating() {
    // tested above
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#update(org.jajuk.events.JajukEvent)}.
   */
  public void testUpdate() {
    // do it a number of times to trigger the refreshMax after 10 times
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
    AlbumManager.getInstance().update(new JajukEvent(JajukEvents.FILE_LAUNCHED, null));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AlbumManager#getAlbumByName(java.lang.String)}.
   */
  public void testGetAlbumByName() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.base.AlbumManager#orderCache()}.
   *
   * @throws Exception
   */
  public void testOrderCache() throws Exception {
    AlbumManager.getInstance().clear();

    Album album = AlbumManager.getInstance().registerAlbum("1", "name1", 1);
    getTrack(2, album);
    getTrack(3, album);

    album = AlbumManager.getInstance().registerAlbum("2", "name2", 1);
    getTrack(4, album);
    getTrack(5, album);

    AlbumManager.getInstance().orderCache();
  }

  @SuppressWarnings("unchecked")
  private Track getTrack(int i, Album album) throws Exception {
    Genre genre = JUnitHelpers.getGenre("name");
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE);
    // don't read covers for this test

    Artist artist = JUnitHelpers.getArtist("atist_" + i);
    Year year = YearManager.getInstance().registerYear(Integer.valueOf(i).toString());

    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();
    ITagImpl tagimp = new MyTagImpl();
    Class<ITagImpl> tl = (Class<ITagImpl>) tagimp.getClass();

    Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", cl, tl);
    Track track = TrackManager.getInstance().registerTrack("track_" + i, album, genre, artist, 120,
        year, 1, type, 1);

    Device device = JUnitHelpers.getDevice();
    Directory dir = JUnitHelpers.getDirectory();
    if (!device.isMounted()) {
      device.mount(true);
    }
    File file = FileManager.getInstance().registerFile("file_" + i + ".tst", dir, track, 200, 100);
    file.getFIO().createNewFile();
    track.addFile(file);

    TypeManager.getInstance().registerType("test", "tst", cl, tl);

    return track;
  }

  public static class MyTagImpl implements ITagImpl {

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#commit()
     */
    @Override
    public void commit() throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getAlbumArtist()
     */
    @Override
    public String getAlbumArtist() throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getAlbumName()
     */
    @Override
    public String getAlbumName() throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getName()
     */
    @Override
    public String getArtistName() throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getComment()
     */
    @Override
    public String getComment() throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getDiscNumber()
     */
    @Override
    public long getDiscNumber() throws Exception {

      return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getLength()
     */
    @Override
    public long getLength() throws Exception {

      return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getOrder()
     */
    @Override
    public long getOrder() throws Exception {

      return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getQuality()
     */
    @Override
    public long getQuality() throws Exception {

      return 0;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getGenreName()
     */
    @Override
    public String getGenreName() throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getTrackName()
     */
    @Override
    public String getTrackName() throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getYear()
     */
    @Override
    public String getYear() throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setAlbumArtist(java.lang.String)
     */
    @Override
    public void setAlbumArtist(String sAlbumArtist) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setAlbumName(java.lang.String)
     */
    @Override
    public void setAlbumName(String sAlbumName) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setArtistName(java.lang.String)
     */
    @Override
    public void setArtistName(String sArtistName) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setComment(java.lang.String)
     */
    @Override
    public void setComment(String sComment) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setDiscNumber(long)
     */
    @Override
    public void setDiscNumber(long discnumber) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setFile(java.io.File)
     */
    @Override
    public void setFile(java.io.File fio) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setOrder(long)
     */
    @Override
    public void setOrder(long lOrder) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setGenreName(java.lang.String)
     */
    @Override
    public void setGenreName(String genre) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setTrackName(java.lang.String)
     */
    @Override
    public void setTrackName(String sTrackName) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setYear(java.lang.String)
     */
    @Override
    public void setYear(String sYear) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getTagField(java.lang.String)
     */
    @Override
    public String getTagField(String tagFieldKey) throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setTagField(java.lang.String, java.lang.String)
     */
    @Override
    public void setTagField(String tagFieldKey, String tagFieldValue)
        throws FieldDataInvalidException, KeyNotFoundException {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#deleteLyrics()
     */
    @Override
    public void deleteLyrics() throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getLyrics()
     */
    @Override
    public String getLyrics() throws Exception {

      return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#setLyrics(java.lang.String)
     */
    @Override
    public void setLyrics(String sLyrics) throws Exception {

    }

    /*
     * (non-Javadoc)
     *
     * @see org.jajuk.services.tags.ITagImpl#getSupportedTagFields()
     */
    @Override
    public ArrayList<String> getSupportedTagFields() {
      // TODO Auto-generated method stub
      return null;
    }

    /* (non-Javadoc)
     * @see org.jajuk.services.tags.ITagImpl#getCovers()
     */
    @Override
    public List<Cover> getCovers() throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

  }

  // needs to be public to be callable from the outside...
  public static class MockPlayer implements IPlayerImpl {
    @Override
    public void stop() throws Exception {

    }

    @Override
    public void setVolume(float fVolume) throws Exception {

    }

    @Override
    public void seek(float fPosition) {

    }

    @Override
    public void resume() throws Exception {

    }

    @Override
    public void play(WebRadio radio, float fVolume) throws Exception {

    }

    @Override
    public void play(File file, float fPosition, long length, float fVolume) throws Exception {

    }

    @Override
    public void pause() throws Exception {

    }

    @Override
    public int getState() {

      return 0;
    }

    @Override
    public long getElapsedTime() {

      return 0;
    }

    @Override
    public float getCurrentVolume() {

      return 0;
    }

    @Override
    public float getCurrentPosition() {

      return 0;
    }

    @Override
    public long getCurrentLength() {

      return 0;
    }
  }
}
