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
package org.jajuk.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.ThreadTestHelper;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.players.TestQueueModel.MockPlayer;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.services.tags.ITagImpl;
import org.jajuk.util.Const;
import org.jajuk.util.ReadOnlyIterator;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.KeyNotFoundException;

/**
 * 
 */
public class TestAuthorManager extends JajukTestCase {

  private static final int NUMBER_OF_TESTS = 10;
  private static final int NUMBER_OF_THREADS = 10;

  /**
   * Test method for {@link org.jajuk.base.AuthorManager#getLabel()}.
   */
  public final void testGetLabel() {
    assertEquals(Const.XML_AUTHORS, AuthorManager.getInstance().getLabel());
  }

  /**
   * Test method for {@link org.jajuk.base.AuthorManager#getInstance()}.
   */
  public final void testGetInstance() {
    assertNotNull(AuthorManager.getInstance());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AuthorManager#registerAuthor(java.lang.String)}.
   */
  public final void testRegisterAuthorString() {
    Author author = AuthorManager.getInstance().registerAuthor("name");
    assertNotNull(author);
    assertTrue(StringUtils.isNotBlank(author.getID()));
    assertEquals("name", author.getName());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AuthorManager#createID(java.lang.String)}.
   */
  public final void testCreateID() {
    String id = AuthorManager.createID("name");

    // same for same name
    assertEquals(id, AuthorManager.createID("name"));

    // different for other name
    assertFalse(id.equals(AuthorManager.createID("name2")));
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AuthorManager#registerAuthor(java.lang.String, java.lang.String)}
   * .
   */
  public final void testRegisterAuthorStringString() {
    Author author = AuthorManager.getInstance().registerAuthor("4", "name");
    assertNotNull(author);
    assertEquals("4", author.getID());
    assertEquals("name", author.getName());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AuthorManager#changeAuthorName(org.jajuk.base.Author, java.lang.String)}
   * .
   * 
   * @throws Exception
   */
  public final void testChangeAuthorName() throws Exception {
    StartupCollectionService.registerItemManagers();

    Author authorold = new Author("4", "nameold");

    // we get the same object back if we have the same name
    assertTrue(authorold == AuthorManager.getInstance().changeAuthorName(authorold, "nameold"));

    // now try with a new name
    Author author = AuthorManager.getInstance().changeAuthorName(authorold, "namenew");
    assertFalse(authorold == author); // not null
    assertFalse("4".equals(author.getID())); // new ID
    assertTrue(StringUtils.isNotBlank(author.getID())); // useful ID
    assertEquals("namenew", author.getName()); // correct name

    // test with Tracks for the author and Queue Model playing that file
    File track1 = getFile(14, author);
    List<StackItem> list = new ArrayList<StackItem>();
    list.add(new StackItem(track1));
    QueueModel.insert(list, 0);
    QueueModel.goTo(0); // to have the Queue in playing mode

    // verify that Queue is playing now
    assertFalse(QueueModel.isStopped());
    assertNotNull(QueueModel.getPlayingFile());
    assertNotNull(QueueModel.getPlayingFile().getTrack());
    assertNotNull(QueueModel.getPlayingFile().getTrack().getAuthor());
    assertEquals(track1.getTrack(), QueueModel.getPlayingFile().getTrack());
    assertEquals(author, QueueModel.getPlayingFile().getTrack().getAuthor());

    // now try to change again with the track and the item playing in the queue
    author = AuthorManager.getInstance().changeAuthorName(author, "namenewnew");
    assertFalse(authorold == author); // not null
    assertFalse("4".equals(author.getID())); // new ID
    assertTrue(StringUtils.isNotBlank(author.getID())); // useful ID
    assertEquals("namenewnew", author.getName()); // correct name
  }

  // test this in a thread as well to cover the synchronized block...
  public final void testChangeAuthorNameThreads() throws Exception {
    StartupCollectionService.registerItemManagers();

    final Author authorold = new Author("4", "nameold");

    // we get the same object back if we have the same name
    assertTrue(authorold == AuthorManager.getInstance().changeAuthorName(authorold, "nameold"));

    ThreadTestHelper helper = new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

    helper.executeTest(new ThreadTestHelper.TestRunnable() {
      public void doEnd(int threadnum) throws Exception {
        // do stuff at the end, nothing here for now
      }

      public void run(int threadnum, int iter) throws Exception {
        // just call the method in a thread multiple times at the same time
        AuthorManager.getInstance().changeAuthorName(authorold, "namenew");
      }
    });
  }

  public void testMultipleThreads() {
  }

  @SuppressWarnings("unchecked")
  private File getFile(int i, Author author) throws Exception {
    Style style = new Style(Integer.valueOf(i).toString(), "name");
    Album album = new Album(Integer.valueOf(i).toString(), "name", "artis", 23);
    album.setProperty(Const.XML_ALBUM_COVER, "none"); // don't read covers for
    // this test

    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

    Type type = TypeManager.getInstance().registerType("name", "tst", cl, MyTagImpl.class);

    Track track = TrackManager.getInstance().registerTrack("name", album, style, author, 120, year,
        1, type, 1);

    Device device = new Device(Integer.valueOf(i).toString(), "name");
    device.setUrl(System.getProperty("java.io.tmpdir"));
    device.mount(true);

    Directory dir = new Directory(Integer.valueOf(i).toString(), "name", null, device);

    File file = new org.jajuk.base.File(Integer.valueOf(i).toString(), "test.tst", dir, track, 120,
        70);

    track.addFile(file);

    return file;
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AuthorManager#format(java.lang.String)}.
   */
  public final void testFormat() {
    assertEquals("Testname", AuthorManager.format("testname"));

    // trim spaces
    assertEquals("Testname", AuthorManager.format("  testname  "));

    // -
    assertEquals("Te s tname", AuthorManager.format("  te-s-tname  "));

    // _
    assertEquals("Te s tname", AuthorManager.format("  te_s_tname  "));

    // all of them
    assertEquals("TE s tnam  e ", AuthorManager.format("  tE_s_tnam--e-  "));
  }

  /**
   * Test method for {@link org.jajuk.base.AuthorManager#getAuthorsList()}.
   */
  public final void testGetAuthorsList() {
    Vector<String> list = AuthorManager.getAuthorsList();
    assertNotNull(list);

    // not sure how many elements we should expect as this is static and other
    // tests
    // could already have added some items, let's just try to add a new one
    int i = list.size();
    AuthorManager.getInstance().registerAuthor("newandnotanywhereelseusedname");

    // the vector should be updated directly, we use the same in the
    // combobox-models,
    // this is the reason for using Vector in the first place!
    assertEquals(i + 1, list.size());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AuthorManager#getAuthorByID(java.lang.String)}.
   */
  public final void testGetAuthorByID() {
    Author author = AuthorManager.getInstance().registerAuthor("anothernewauthor");

    Author author2 = AuthorManager.getInstance().getAuthorByID(author.getID());
    assertEquals("anothernewauthor", author2.getName());
  }

  /**
   * Test method for {@link org.jajuk.base.AuthorManager#getAuthors()}.
   */
  public final void testGetAuthors() {
    List<Author> list = AuthorManager.getInstance().getAuthors();
    assertNotNull(list);

    // not sure how many elements we should expect as this is static and other
    // tests
    // could already have added some items, let's just try to add a new one
    int i = list.size();
    AuthorManager.getInstance().registerAuthor("newname");

    // the list is a copy, so we need to get it again
    list = AuthorManager.getInstance().getAuthors();
    assertEquals(i + 1, list.size());
  }

  /**
   * Test method for {@link org.jajuk.base.AuthorManager#getAuthorsIterator()}.
   */
  public final void testGetAuthorsIterator() {
    AuthorManager.getInstance().registerAuthor("anothernewname");
    ReadOnlyIterator<Author> it = AuthorManager.getInstance().getAuthorsIterator();
    assertNotNull(it);
    assertTrue(it.hasNext()); // there can be items from before, so just expect
    // at least one item...
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AuthorManager#getAssociatedAuthors(org.jajuk.base.Item)}
   * .
   * 
   * @throws Exception
   */
  public final void testGetAssociatedAuthors() throws Exception {
    // empty list with invalid item
    List<Author> list = AuthorManager.getInstance().getAssociatedAuthors(null);
    assertNotNull(list);
    assertEquals(0, list.size());

    Author author = AuthorManager.getInstance().registerAuthor("myauthorhere");
    File file = getFile(15, author); // also registers the Track
    list = AuthorManager.getInstance().getAssociatedAuthors(file.getTrack());
    assertNotNull(list);
    assertEquals(1, list.size());
    assertEquals("myauthorhere", list.get(0).getName());

    Album album = file.getTrack().getAlbum();
    album.getTracksCache().add(file.getTrack());

    list = AuthorManager.getInstance().getAssociatedAuthors(album);
    assertNotNull(list);
    assertEquals(1, list.size());
    assertEquals("myauthorhere", list.get(0).getName());
  }

  /**
   * Test method for
   * {@link org.jajuk.base.AuthorManager#getAuthorByName(java.lang.String)}.
   */
  public final void testGetAuthorByName() {
    Author author = AuthorManager.getInstance().registerAuthor("anothernewauthor");

    Author author2 = AuthorManager.getInstance().getAuthorByName("anothernewauthor");
    assertEquals(author.getID(), author2.getID());
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
    public String getAuthorName() throws Exception {

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
    public String getStyleName() throws Exception {

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
    public void setAuthorName(String authorName) throws Exception {

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
    public void setStyleName(String style) throws Exception {

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
     * @see org.jajuk.services.tags.ITagImpl#getSupportedTagFields()
     */
    @Override
    public ArrayList<String> getSupportedTagFields() {

      return null;
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

  }
}
