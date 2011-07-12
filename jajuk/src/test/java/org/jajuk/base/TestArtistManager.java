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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.ThreadTestHelper;
import org.jajuk.services.covers.Cover;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.tags.ITagImpl;
import org.jajuk.util.Const;
import org.jajuk.util.ReadOnlyIterator;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;

/**
 *
 */
public class TestArtistManager extends JajukTestCase {

  private static final int NUMBER_OF_TESTS = 10;
  private static final int NUMBER_OF_THREADS = 10;

  /**
   * Test method for {@link org.jajuk.base.ArtistManager#getLabel()}.
   */
  public final void testGetLabel() {
    assertEquals(Const.XML_ARTISTS, ArtistManager.getInstance().getLabel());
  }

  /**
   * Test method for {@link org.jajuk.base.ArtistManager#getInstance()}.
   */
  public final void testGetInstance() {
    assertNotNull(ArtistManager.getInstance());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.ArtistManager#registerArtist(java.lang.String)}.
   */
  public final void testRegisterArtistString() {
    Artist artist = ArtistManager.getInstance().registerArtist("name");
    assertNotNull(artist);
    assertTrue(StringUtils.isNotBlank(artist.getID()));
    assertEquals("name", artist.getName());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.ArtistManager#createID(java.lang.String)}.
   */
  public final void testCreateID() {
    String id = ArtistManager.createID("name");

    // same for same name
    assertEquals(id, ArtistManager.createID("name"));

    // different for other name
    assertFalse(id.equals(ArtistManager.createID("name2")));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.ArtistManager#registerArtist(java.lang.String, java.lang.String)}
   * .
   */
  public final void testRegisterArtistStringString() {
    Artist artist = ArtistManager.getInstance().registerArtist("4", "name");
    assertNotNull(artist);
    assertEquals("4", artist.getID());
    assertEquals("name", artist.getName());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.ArtistManager#changeArtistName(org.jajuk.base.Artist, java.lang.String)}
   * .
   *
   * @throws Exception
   */
  public final void testChangeArtistName() throws Exception {
    StartupCollectionService.registerItemManagers();

    Artist artistold = JUnitHelpers.getArtist("nameold");

    // we get the same object back if we have the same name
    assertTrue(artistold == ArtistManager.getInstance().changeArtistName(artistold, "nameold"));

    // now try with a new name
    Artist artist = ArtistManager.getInstance().changeArtistName(artistold, "namenew");
    assertFalse(artistold == artist); // not null
    assertFalse("4".equals(artist.getID())); // new ID
    assertTrue(StringUtils.isNotBlank(artist.getID())); // useful ID
    assertEquals("namenew", artist.getName()); // correct name

    // test with Tracks for the artist and Queue Model playing that file
    File track1 = getFile(14, artist);
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(track1));
    QueueModel.insert(list, 0);
    QueueModel.goTo(0); // to have the Queue in playing mode

    // verify that Queue is playing now
    assertFalse(QueueModel.isStopped());
    assertNotNull(QueueModel.getPlayingFile());
    assertNotNull(QueueModel.getPlayingFile().getTrack());
    assertNotNull(QueueModel.getPlayingFile().getTrack().getArtist());
    assertEquals(track1.getTrack(), QueueModel.getPlayingFile().getTrack());
    assertEquals(artist, QueueModel.getPlayingFile().getTrack().getArtist());

    // now try to change again with the track and the item playing in the queue
    artist = ArtistManager.getInstance().changeArtistName(artist, "namenewnew");
    assertFalse(artistold == artist); // not null
    assertFalse("4".equals(artist.getID())); // new ID
    assertTrue(StringUtils.isNotBlank(artist.getID())); // useful ID
    assertEquals("namenewnew", artist.getName()); // correct name
  }

  // test this in a thread as well to cover the synchronized block...
  public final void testChangeArtistNameThreads() throws Exception {
    StartupCollectionService.registerItemManagers();

    final Artist artistold = JUnitHelpers.getArtist("nameold");

    // we get the same object back if we have the same name
    assertTrue(artistold == ArtistManager.getInstance().changeArtistName(artistold, "nameold"));

    ThreadTestHelper helper = new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

    helper.executeTest(new ThreadTestHelper.TestRunnable() {
      @Override
      public void doEnd(int threadnum) throws Exception {
        // do stuff at the end, nothing here for now
      }

      @Override
      public void run(int threadnum, int iter) throws Exception {
        // just call the method in a thread multiple times at the same time
        ArtistManager.getInstance().changeArtistName(artistold, "namenew");
      }
    });
  }

  public void testMultipleThreads() {
  }

  @SuppressWarnings("unchecked")
  private File getFile(int i, Artist artist) throws Exception {
    Genre genre = JUnitHelpers.getGenre("name");
    Album album = JUnitHelpers.getAlbum("myalbum", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read
    // covers for
    // this test

    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    IPlayerImpl imp = new JUnitHelpers.MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

    Type type = TypeManager.getInstance().registerType("name", "tst", cl, MyTagImpl.class);

    Track track = TrackManager.getInstance().registerTrack("name", album, genre, artist, 120, year,
        1, type, 1);

    Device device = JUnitHelpers.getDevice();
    device.mount(true);
    Directory dir = JUnitHelpers.getDirectory();

    File file = new org.jajuk.base.File(Integer.valueOf(i).toString(), "test.tst", dir, track, 120,
        70);

    track.addFile(file);

    return file;
  }

  /**
   * Test method for
   * {@link org.jajuk.base.ArtistManager#format(java.lang.String)}.
   */
  public final void testFormat() {
    assertEquals("Testname", ArtistManager.format("testname"));

    // trim spaces
    assertEquals("Testname", ArtistManager.format("  testname  "));

    // -
    assertEquals("Te s tname", ArtistManager.format("  te-s-tname  "));

    // _
    assertEquals("Te s tname", ArtistManager.format("  te_s_tname  "));

    // all of them
    assertEquals("TE s tnam  e ", ArtistManager.format("  tE_s_tnam--e-  "));
  }

  /**
   * Test method for {@link org.jajuk.base.ArtistManager#getArtistsList()}.
   */
  public final void testGetArtistsList() {
    List<String> list = ArtistManager.getArtistsList();
    assertNotNull(list);

    // not sure how many elements we should expect as this is static and other
    // tests
    // could already have added some items, let's just try to add a new one
    int i = list.size();
    ArtistManager.getInstance().registerArtist("newandnotanywhereelseusedname");

    // the vector should be updated directly, we use the same in the
    // combobox-models,
    // this is the reason for using Vector in the first place!
    assertEquals(i + 1, list.size());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.ArtistManager#getArtistByID(java.lang.String)}.
   */
  public final void testGetArtistByID() {
    Artist artist = ArtistManager.getInstance().registerArtist("anothernewartist");

    Artist artist2 = ArtistManager.getInstance().getArtistByID(artist.getID());
    assertEquals("anothernewartist", artist2.getName());
  }

  /**
   * Test method for {@link org.jajuk.base.ArtistManager#getArtists()}.
   */
  public final void testGetArtists() {
    List<Artist> list = ArtistManager.getInstance().getArtists();
    assertNotNull(list);

    // not sure how many elements we should expect as this is static and other
    // tests
    // could already have added some items, let's just try to add a new one
    int i = list.size();
    ArtistManager.getInstance().registerArtist("newname");

    // the list is a copy, so we need to get it again
    list = ArtistManager.getInstance().getArtists();
    assertEquals(i + 1, list.size());
  }

  /**
   * Test method for {@link org.jajuk.base.ArtistManager#getArtistsIterator()}.
   */
  public final void testGetArtistsIterator() {
    ArtistManager.getInstance().registerArtist("anothernewname");
    ReadOnlyIterator<Artist> it = ArtistManager.getInstance().getArtistsIterator();
    assertNotNull(it);
    assertTrue(it.hasNext()); // there can be items from before, so just expect
    // at least one item...
  }

  /**
   * Test method for
   * {@link org.jajuk.base.ArtistManager#getAssociatedArtists(org.jajuk.base.Item)}
   * .
   *
   * @throws Exception
   */
  public final void testGetAssociatedArtists() throws Exception {
    // empty list with invalid item
    List<Artist> list = ArtistManager.getInstance().getAssociatedArtists(null);
    assertNotNull(list);
    assertEquals(0, list.size());

    Artist artist = ArtistManager.getInstance().registerArtist("myartisthere");
    File file = getFile(15, artist); // also registers the Track
    list = ArtistManager.getInstance().getAssociatedArtists(file.getTrack());
    assertNotNull(list);
    assertEquals(1, list.size());
    assertEquals("myartisthere", list.get(0).getName());

    Album album = file.getTrack().getAlbum();
    album.getTracksCache().add(file.getTrack());

    list = ArtistManager.getInstance().getAssociatedArtists(album);
    assertNotNull(list);
    assertEquals(1, list.size());
    assertEquals("myartisthere", list.get(0).getName());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.ArtistManager#getArtistByName(java.lang.String)}.
   */
  public final void testGetArtistByName() {
    Artist artist = ArtistManager.getInstance().registerArtist("anothernewartist");

    Artist artist2 = ArtistManager.getInstance().getArtistByName("anothernewartist");
    assertEquals(artist.getID(), artist2.getID());
  }

  public final void testSorting() {
    // make sure we have "ordered state"
    ArtistManager.getInstance().switchToOrderState();

    List<String> ids = new ArrayList<String>();
    ids.add(ArtistManager.getInstance().registerArtist("anothernewartist").getID());
    ids.add(ArtistManager.getInstance().registerArtist("yet another artist").getID());
    ids.add(ArtistManager.getInstance().registerArtist("one more artist").getID());
    ids.add(ArtistManager.getInstance().registerArtist("number 10").getID());
    ids.add(ArtistManager.getInstance().registerArtist("number 11").getID());

    // now they are sorted by name
    Iterator<? extends Item> it = ArtistManager.getInstance().getItemsIterator();
    assertEquals("anothernewartist", it.next().getName());
    assertEquals("number 10", it.next().getName());
    assertEquals("number 11", it.next().getName());
    assertEquals("one more artist", it.next().getName());
    assertEquals("yet another artist", it.next().getName());
    assertFalse(it.hasNext());

    // make sure we can fetch all of these by ID
    for (String id : ids) {
      assertNotNull("Did not find ID: " + id, ArtistManager.getInstance().getArtistByID(id));
    }

    assertNull(ArtistManager.getInstance().getArtistByID("notexisting"));
    assertNull(ArtistManager.getInstance().getArtistByID("number 12"));
    assertNull(ArtistManager.getInstance().getArtistByID("number 09"));
  }

  public static class MyTagImpl implements ITagImpl {

    @Override
    public void commit() throws Exception {

    }

    @Override
    public String getAlbumArtist() throws Exception {

      return null;
    }

    @Override
    public String getAlbumName() throws Exception {

      return null;
    }

    @Override
    public String getArtistName() throws Exception {

      return null;
    }

    @Override
    public String getComment() throws Exception {

      return null;
    }

    @Override
    public long getDiscNumber() throws Exception {

      return 0;
    }

    @Override
    public long getLength() throws Exception {

      return 0;
    }

    @Override
    public long getOrder() throws Exception {

      return 0;
    }

    @Override
    public long getQuality() throws Exception {

      return 0;
    }

    @Override
    public String getGenreName() throws Exception {

      return null;
    }

    @Override
    public String getTrackName() throws Exception {

      return null;
    }

    @Override
    public String getYear() throws Exception {

      return null;
    }

    @Override
    public void setAlbumArtist(String albumArtist) throws Exception {

    }

    @Override
    public void setAlbumName(String albumName) throws Exception {

    }

    @Override
    public void setArtistName(String artistName) throws Exception {

    }

    @Override
    public void setComment(String comment) throws Exception {

    }

    @Override
    public void setDiscNumber(long discnumber) throws Exception {

    }

    @Override
    public void setFile(java.io.File fio) throws Exception {

    }

    @Override
    public void setOrder(long order) throws Exception {

    }

    @Override
    public void setGenreName(String genre) throws Exception {

    }

    @Override
    public void setTrackName(String trackName) throws Exception {

    }

    @Override
    public void setYear(String year) throws Exception {

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
     * @see org.jajuk.services.tags.ITagImpl#setTagField(java.lang.String,
     * java.lang.String)
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

    /* (non-Javadoc)
     * @see org.jajuk.services.tags.ITagImpl#getSupportedTagFields()
     */
    @Override
    public List<String> getSupportedTagFields() {
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
}
