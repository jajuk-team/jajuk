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
package org.jajuk;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.SwingUtilities;

import junit.framework.Assert;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Artist;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.base.Year;
import org.jajuk.base.YearManager;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Const;
import org.jajuk.util.log.Log;

/**
 * Small helper class with functionality that is used in multiple unit tests
 */
public class JUnitHelpers {

  /**
   * Set a temporary session directory and make sure it exists and is writeable.
   *
   * @throws IOException
   *           If the temporary directory can not be created or is not writeable
   */
  public static void createSessionDirectory() throws IOException {
    // get a temporary file name
    File tempdir = File.createTempFile("test", "");
    if (!tempdir.delete()) {
      throw new IOException("Could not create the temporary session directory at "
          + tempdir.getAbsolutePath() + ", could not remove the temporary file.");
    }

    // set the directory as base directory for the workspace
    SessionService.setWorkspace(tempdir.getAbsolutePath());

    // read the session directory that we are using now for caching
    File sessiondir = SessionService.getConfFileByPath(Const.FILE_CACHE);

    // create the directory structure
    sessiondir.mkdirs();

    // do some checks
    if (!sessiondir.exists()) {
      throw new IOException("Could not create the temporary session directory at "
          + sessiondir.getAbsolutePath());
    }
    if (!sessiondir.isDirectory()) {
      throw new IOException("Could not create the temporary session directory at "
          + sessiondir.getAbsolutePath() + ", not a directory!");
    }
    if (!sessiondir.canWrite()) {
      throw new IOException("Could not create the temporary session directory at "
          + sessiondir.getAbsolutePath() + ", not writeable!");
    }

    // make sure the directory is removed at the end of the tests again
    sessiondir.getParentFile().deleteOnExit();
  }

  /**
   * Helper method for removing emma-reports for classes with only static
   * methods
   * <p/>
   * see
   * http://sourceforge.net/tracker/index.php?func=detail&aid=1173251&group_id
   * =108932&atid=651900
   *
   * add this to the test case for any class that has only static methods where
   * EMMA reports the default constructor as not covered
   *
   * Template: <code>

   // helper method to emma-coverage of the unused constructor
   public void testPrivateConstructor() throws Exception {
     JUnitHelpers.executePrivateConstructor(<yourclass>.class);
   }

   * </code>
   *
   * @param targetClass
   */
  public static <T> T executePrivateConstructor(final Class<T> targetClass) throws Exception {
    final Constructor<T> c = targetClass.getDeclaredConstructor(new Class[] {});
    c.setAccessible(true);
    return c.newInstance((Object[]) null);
  }

  public static void EqualsTest(final Object obj, final Object equal, final Object notequal) {
    // none of the three should be null
    Assert.assertNotNull("Object in EqualsTest should not be null!", obj);
    Assert.assertNotNull("Equals-object in EqualsTest should not be null!", equal);
    Assert.assertNotNull("Non-equal-object in EqualsTest should not be null!", notequal);

    // make sure different objects are passed in
    Assert.assertFalse("Object and equals-object in EqualsTest should not be identical",
        obj == equal);
    Assert.assertFalse("Object and non-equals-object in EqualsTest should not be identical",
        obj == notequal);

    // make sure correct objects are passed
    Assert.assertTrue("Classes of objects in EqualsTest should be equal!", obj.getClass().equals(
        equal.getClass()));
    Assert.assertTrue("Classes of objects in EqualsTest should be equal!", obj.getClass().equals(
        notequal.getClass()));

    // make sure correct parameters are passed
    // equal should be equal to obj, not-equal should not be equal to obj!
    Assert.assertTrue("Object and equal-object should be equal in EqualsTest!", obj.equals(equal));
    Assert.assertFalse("Object and non-equal-object should not be equal in EqualsTest!", obj
        .equals(notequal));

    // first test some general things that should be true with equals

    // reflexive: equals to itself
    Assert
        .assertTrue("Reflexive: object should be equal to itself in EqualsTest!", obj.equals(obj));
    Assert.assertTrue("Reflexive: equal-object should be equal to itself in EqualsTest!", equal
        .equals(equal));
    Assert.assertTrue("Reflexive: non-equal-object should be equal to itself in EqualsTest!",
        notequal.equals(notequal));

    // not equals to null
    Assert.assertFalse("Object should not be equal to null in EqualsTest!", obj.equals(null));
    Assert.assertFalse("Equal-object should not be equal to null in EqualsTest!", equal
        .equals(null));
    Assert.assertFalse("Non-equal-object should not be equal to null in EqualsTest!", notequal
        .equals(null));

    // not equals to a different type of object
    Assert.assertFalse("Object should not be equal to an arbitrary string in EqualsTest!", obj
        .equals("TestString"));

    // then test some things with another object that should be equal

    // symmetric, if one is (not) equal to another then the reverse must be true
    Assert.assertTrue("Symmetric: Object should be equal to equal-object in EqualsTest", obj
        .equals(equal));
    Assert.assertTrue("Symmetric: Equals-object should be equal to object in EqualsTest!", equal
        .equals(obj));
    Assert.assertFalse("Symmetric: Object should NOT be equal to non-equal-object in EqualsTest",
        obj.equals(notequal));
    Assert.assertFalse("Symmetric: Non-equals-object should NOT be equal to object in EqualsTest!",
        notequal.equals(obj));

    // transitive: if a.equals(b) and b.equals(c) then a.equals(c)
    // not tested right now

    // hashCode: equal objects should have equal hash code
    Assert.assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!", obj
        .hashCode() == equal.hashCode());
    Assert.assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!", obj
        .hashCode() == obj.hashCode());
    Assert.assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!", equal
        .hashCode() == equal.hashCode());
    Assert.assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!",
        notequal.hashCode() == notequal.hashCode());
  }

  @SuppressWarnings("unchecked")
  public static <T> void CompareToTest(final Comparable<T> obj, final Comparable<T> equal,
      final Comparable<T> notequal) {
    // none of the three should be null
    Assert.assertNotNull("Object in CompareToTest should not be null!", obj);
    Assert.assertNotNull("Equals-object in CompareToTest should not be null!", equal);
    Assert.assertNotNull("Non-equal-object in CompareToTest should not be null!", notequal);

    // make sure different objects are passed in
    Assert.assertFalse("Object and equals-object in CompareToTest should not be identical",
        obj == equal);
    Assert.assertFalse("Object and non-equals-object in CompareToTest should not be identical",
        obj == notequal);

    // make sure correct parameters are passed
    // equal should be equal to obj, not-equal should not be equal to obj!
    Assert.assertEquals("Object and equal-object should compare in CompareToTest!", 0, obj
        .compareTo((T) equal));
    Assert.assertFalse("Object and non-equal-object should not compare in CompareToTest!", 0 == obj
        .compareTo((T) notequal));

    // first test some general things that should be true with equals

    // reflexive: equals to itself
    Assert.assertEquals("Reflexive: object should be equal to itself in CompareToTest!", 0, obj
        .compareTo((T) obj));
    Assert.assertEquals("Reflexive: equal-object should be equal to itself in CompareToTest!", 0,
        equal.compareTo((T) equal));
    Assert.assertEquals("Reflexive: non-equal-object should be equal to itself in CompareToTest!",
        0, notequal.compareTo((T) notequal));

    // not equals to null
    Assert.assertFalse("Object should not be equal to null in CompareToTest!", 0 == obj
        .compareTo(null));
    Assert.assertFalse("Equal-object should not be equal to null in CompareToTest!", 0 == equal
        .compareTo(null));
    Assert.assertFalse("Non-equal-object should not be equal to null in CompareToTest!",
        0 == notequal.compareTo(null));

    // not equals to a different type of object
    /*
     * Assert.assertFalse("Object should not be equal to an arbitrary string in CompareToTest!" , 0
     * == obj.compareTo("TestString"));
     */

    // then test some things with another object that should be equal

    // symmetric, if one is (not) equal to another then the reverse must be true
    Assert.assertEquals("Symmetric: Object should be equal to equal-object in CompareToTest", 0,
        obj.compareTo((T) equal));
    Assert.assertEquals("Symmetric: Equals-object should be equal to object in CompareToTest!", 0,
        equal.compareTo((T) obj));
    Assert.assertFalse(
        "Symmetric: Object should NOT be equal to non-equal-object in CompareToTest", 0 == obj
            .compareTo((T) notequal));
    Assert.assertFalse(
        "Symmetric: Non-equals-object should NOT be equal to object in CompareToTest!",
        0 == notequal.compareTo((T) obj));

    // transitive: if a.equals(b) and b.equals(c) then a.equals(c)
    // not tested right now
  }

  /**
   * Run some general tests on the toString method. This static method is used
   * in tests for classes that overwrite toString().
   *
   * @param obj
   *          The object to test toString(). This should be an object of a type
   *          that overwrites toString()
   *
   */
  public static void ToStringTest(final Object obj) {
    // toString should not return null
    Assert.assertNotNull("A derived toString() should not return null!", obj.toString());

    // toString should not return an empty string
    Assert.assertFalse("A derived toString() should not return an empty string!", obj.toString()
        .equals(""));

    // check that calling it multiple times leads to the same value
    String value = obj.toString();
    for (int i = 0; i < 10; i++) {
      Assert.assertEquals(
          "toString() is expected to result in the same result across repeated calls!", value, obj
              .toString());
    }
  }

  /**
   * Run some generic tests on the derived clone-method.
   *
   * We need to do this via reflection as the clone()-method in Object is
   * protected and the Cloneable interface does not include a public "clone()".
   *
   * @param obj
   *          The object to test clone for.
   */
  public static void CloneTest(final Cloneable obj) throws Exception {
    final Method m = obj.getClass().getMethod("clone", new Class[] {});
    Assert.assertNotNull("Need to find a method called 'clone' in object of type '"
        + obj.getClass().getName() + "' in CloneTest!", m);
    // Assert.assertTrue("Method 'clone' on object of type '" +
    // obj.getClass().getName() + "' needs to be accessible in
    // CloneTest!",
    // m.isAccessible());

    // clone should return a different object, not the same again
    Assert.assertTrue("clone() should not return the object itself in CloneTest!", obj != m.invoke(
        obj, new Object[] {}));

    // should return the same type of object
    Assert.assertTrue(
        "clone() should return the same type of object (i.e. the same class) in CloneTest!", m
            .invoke(obj, new Object[] {}).getClass() == obj.getClass());

    // cloned objects should be equal to the original object
    Assert.assertTrue(
        "clone() should return an object that is equal() to the original object in CloneTest!", m
            .invoke(obj, new Object[] {}).equals(obj));
  }

  /**
   * Checks certain assumption that are made for the hashCode() method
   *
   * @param obj
   *          An Object that override the hasCode() method.
   *
   * @throws Exception
   */
  public static void HashCodeTest(final Object obj, final Object equ) {
    Assert
        .assertFalse(
            "HashCodeTest expects two distinct objects with equal hashCode, but the same object is provided twice!",
            obj == equ);

    // The same object returns the same hashCode always
    final int hash = obj.hashCode();
    Assert.assertEquals("hashCode() on object returned different hash after some iterations!",
        hash, obj.hashCode());
    Assert.assertEquals("hashCode() on object returned different hash after some iterations!",
        hash, obj.hashCode());
    Assert.assertEquals("hashCode() on object returned different hash after some iterations!",
        hash, obj.hashCode());
    Assert.assertEquals("hashCode() on object returned different hash after some iterations!",
        hash, obj.hashCode());
    Assert.assertEquals("hashCode() on object returned different hash after some iterations!",
        hash, obj.hashCode());

    // equal objects must have the same hashCode
    // the other way around is not required,
    // different objects can have the same hashCode!!
    Assert
        .assertEquals(
            "Equal Assert failed, but input to HashCodeTest should be two equal objects! Check if the class implements equals() as well to fullfill this contract",
            obj, equ);
    Assert.assertEquals("Equal objects should have equal hashCode() by Java contract!", obj
        .hashCode(), equ.hashCode());
  }

  @SuppressWarnings( { "unchecked", "rawtypes" })
  public static void EnumTest(Enum enumtype, Class enumclass, String element)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    // check valueOf()
    Assert.assertEquals(enumtype, Enum.valueOf(enumclass, element));

    // check values()
    Method m = enumclass.getMethod("values", (Class[]) null);
    Object obj = m.invoke(enumtype, (Object[]) null);
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj instanceof Object[]);

    // check existing valeOf()
    obj = Enum.valueOf(enumclass, element);
    Assert.assertNotNull(obj);
    Assert.assertTrue(obj instanceof Enum);

    // check non-existing valueOf
    try {
      Enum.valueOf(enumclass, "nonexistingenumelement");
      Assert
          .fail("Should catch exception IllegalArgumentException when calling Enum.valueOf() with incorrect enum-value!");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage(), e.getMessage().contains("No enum const class"));
    }
  }

  public static void clearSwingUtilitiesQueue() throws InterruptedException,
      InvocationTargetException {
    // we try to wait for all items that were sent via
    // "SwingUtilities.invokeLater()" in order
    // to have a deterministic state in the tests where we know that the
    // asynchronous action
    // done in any "invokeLater()" was actually finished

    SwingUtilities.invokeAndWait(new Runnable() {
      @Override
      public void run() {
        // nothing to do here, we just want the runnable to run...
      }
    });

    // Thread.currentThread().
  }

  public static void waitForThreadToFinish(final String name) throws InterruptedException {
    int count = Thread.currentThread().getThreadGroup().activeCount();

    Thread[] threads = new Thread[count];
    /* int received = */Thread.currentThread().getThreadGroup().enumerate(threads);

    /*
     * we ignore this check as we can not do anything anyway if we receive not all threads if(count
     * != received) { throw new IllegalStateException("Could not read all threads: Expected: " +
     * count + " Found: " + received); }
     */

    for (Thread t : threads) {
      if (t != null && name.equals(t.getName())) {
        t.join();
      }
    }
  }

  public static void waitForAllWorkToFinishAndCleanup() throws InterruptedException,
      InvocationTargetException {
    ObservationManager.clear();

    QueueModel.stopRequest();
    QueueModel.clear();

    FileManager.getInstance().clear();
    DirectoryManager.getInstance().clear();
    DeviceManager.getInstance().cleanAllDevices();
    History.getInstance().clear();

    // wait a bit to let deferred actions take place before we shut down
    JUnitHelpers.waitForThreadToFinish("Cover Refresh Thread");
    JUnitHelpers.waitForThreadToFinish("Queue Push Thread");
    JUnitHelpers.waitForThreadToFinish("Device Refresh Thread");
    JUnitHelpers.waitForThreadToFinish("Playlist Prepare Party Thread");
    JUnitHelpers.waitForThreadToFinish("LastFM Update Thread");
    JUnitHelpers.waitForThreadToFinish("Parameter Catalog refresh Thread");
    JUnitHelpers.waitForThreadToFinish("Manual Refresh Thread");

    // clear this for all available events
    // for(JajukEvents event : JajukEvents.values()) {
    // JUnitHelpers.waitForThreadToFinish("Event Executor for: " +
    // event.toString());
    // }
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.ALARMS_CHANGE.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.ALBUM_CHANGED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: " + JajukEvents.BANNED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: " + JajukEvents.CDDB_WIZARD.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.CLEAR_HISTORY.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.COVER_NEED_REFRESH.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.DEVICE_REFRESH.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.FILE_FINISHED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.FILE_LAUNCHED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.FILE_NAME_CHANGED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.LANGUAGE_CHANGED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: " + JajukEvents.MUTE_STATE.toString());
    JUnitHelpers
        .waitForThreadToFinish("Event Executor for: " + JajukEvents.PLAYER_PAUSE.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: " + JajukEvents.PLAYER_PLAY.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.PLAYER_RESUME.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: " + JajukEvents.PLAYER_STOP.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: " + JajukEvents.PLAY_ERROR.toString());
    JUnitHelpers
        .waitForThreadToFinish("Event Executor for: " + JajukEvents.PLAY_OPENING.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.PREFERENCES_RESET.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: " + JajukEvents.RATE_RESET.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.GENRE_NAME_CHANGED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.VOLUME_CHANGED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: "
        + JajukEvents.WEBRADIO_LAUNCHED.toString());
    JUnitHelpers.waitForThreadToFinish("Event Executor for: " + JajukEvents.ZERO.toString());

    JUnitHelpers.clearSwingUtilitiesQueue();
  }

  /**
   * Return a file named "test.tst" on a mounted device.
   * @return a file named "test.tst" on a mounted device.
   */
  public static org.jajuk.base.File getFile() {
    return getFile("test.tst", true);
  }

  public static org.jajuk.base.File getFile(String name, boolean mount) {
    Genre genre = getGenre();
    Album album = getAlbum("name", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test

    Artist artist = getArtist("myartist");
    Year year = getYear(2000);

    Type type = getType();
    Track track = TrackManager.getInstance().registerTrack(name, album, genre, artist, 120, year,
        1, type, 1);

    Device device = getDevice();
    if (mount & !device.isMounted()) {
      try {
        device.mount(true);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    Directory dir = JUnitHelpers.getDirectory();

    return FileManager.getInstance().registerFile(name, dir, track, 120, 70);
  }

  public static Album getAlbum(String name, int discID) {
    return AlbumManager.getInstance().registerAlbum(name, discID);
  }

  public static Album getAlbum() {
    return getAlbum("name", 0);
  }

  public static Artist getArtist(String name) {
    return ArtistManager.getInstance().registerArtist(name);
  }

  public static Artist getArtist() {
    return getArtist("name");
  }

  public static Genre getGenre(String name) {
    return GenreManager.getInstance().registerGenre(name);
  }

  public static Genre getGenre() {
    return getGenre("name");
  }

  public static Year getYear(int year) {
    return YearManager.getInstance().registerYear("" + year);
  }

  public static Year getYear() {
    return getYear(2000);
  }

  public static Device getDevice(String name, long type, String url) {
    return DeviceManager.getInstance().registerDevice(name, type, url);
  }

  public static Device getDevice() {
    Device device = getDevice("name", Device.TYPE_DIRECTORY, ConstTest.PATH_DEVICE);
    // Create the jajuk test device if required
    new File(device.getUrl()).mkdirs();
    // Create at least a void file in the device
    try {
      new File(device.getUrl() + "/audio1.mp3").createNewFile();
    } catch (IOException e) {
      Log.error(e);
    }
    return device;
  }

  public static Directory getDirectory(String name, Directory parent, Device device) {
    return DirectoryManager.getInstance().registerDirectory(name, parent, device);
  }

  /** The "any" directory is the top dir of the device pointing toward $java.io.tmpdir*/
  public static Directory getDirectory() {
    Device device = getDevice();
    Directory topdir = getTopDirectory();
    return DirectoryManager.getInstance().registerDirectory("dir", topdir, device);
  }

  /** A topdir directory */
  public static Directory getTopDirectory() {
    return DirectoryManager.getInstance().registerDirectory(getDevice());
  }

  @SuppressWarnings("unchecked")
  public static Type getType() {
    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();
    return TypeManager.getInstance().registerType("type", "mp3", cl, null);
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
    public void play(org.jajuk.base.File file, float fPosition, long length, float fVolume)
        throws Exception {

    }

    @Override
    public void pause() throws Exception {

    }

    @Override
    public int getState() {

      return 0;
    }

    @Override
    public long getElapsedTimeMillis() {

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
    public long getDurationSec() {

      return 0;
    }
  }

  public static Track getTrack(int i) {
    Genre genre = getGenre();
    Album album = getAlbum("myalbum", 0);
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test

    Artist artist = getArtist("myartist_" + i);
    Year year = getYear(2000);

    Type type = getType();
    return TrackManager.getInstance().registerTrack("track_" + i, album, genre, artist, 120, year,
        1, type, 1);
  }
}
