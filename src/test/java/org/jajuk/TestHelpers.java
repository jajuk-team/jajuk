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
package org.jajuk;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.base.Year;
import org.jajuk.base.YearManager;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.core.ExitService;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.services.webradio.WebRadioOrigin;
import org.jajuk.util.Const;
import org.jajuk.util.MD5Processor;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Small helper class with functionality that is used in multiple unit tests.
 */
public class TestHelpers {
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
   * 
   * // helper method to emma-coverage of the unused constructor
   * public void testPrivateConstructor() throws Exception {
   * JUnitHelpers.executePrivateConstructor(<yourclass>.class);
   * }
   * </code>
   *
   * @param <T> 
   * @param targetClass 
   * @return the t
   * @throws Exception the exception
   */
  public static <T> T executePrivateConstructor(final Class<T> targetClass) throws Exception {
    final Constructor<T> c = targetClass.getDeclaredConstructor(new Class[] {});
    c.setAccessible(true);
    return c.newInstance((Object[]) null);
  }

  /**
   * Equals test.
   * 
   *
   * @param obj 
   * @param equal 
   * @param notequal 
   */
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
    Assert.assertTrue("Classes of objects in EqualsTest should be equal!",
        obj.getClass().equals(equal.getClass()));
    Assert.assertTrue("Classes of objects in EqualsTest should be equal!",
        obj.getClass().equals(notequal.getClass()));
    // make sure correct parameters are passed
    // equal should be equal to obj, not-equal should not be equal to obj!
    Assert.assertTrue("Object and equal-object should be equal in EqualsTest!", obj.equals(equal));
    Assert.assertFalse("Object and non-equal-object should not be equal in EqualsTest!",
        obj.equals(notequal));
    // first test some general things that should be true with equals
    // reflexive: equals to itself
    Assert
        .assertTrue("Reflexive: object should be equal to itself in EqualsTest!", obj.equals(obj));
    Assert.assertTrue("Reflexive: equal-object should be equal to itself in EqualsTest!",
        equal.equals(equal));
    Assert.assertTrue("Reflexive: non-equal-object should be equal to itself in EqualsTest!",
        notequal.equals(notequal));
    // not equals to null
    Assert.assertFalse("Object should not be equal to null in EqualsTest!", obj.equals(null));
    Assert.assertFalse("Equal-object should not be equal to null in EqualsTest!",
        equal.equals(null));
    Assert.assertFalse("Non-equal-object should not be equal to null in EqualsTest!",
        notequal.equals(null));
    // not equals to a different type of object
    Assert.assertFalse("Object should not be equal to an arbitrary string in EqualsTest!",
        obj.equals("TestString"));
    // then test some things with another object that should be equal
    // symmetric, if one is (not) equal to another then the reverse must be true
    Assert.assertTrue("Symmetric: Object should be equal to equal-object in EqualsTest",
        obj.equals(equal));
    Assert.assertTrue("Symmetric: Equals-object should be equal to object in EqualsTest!",
        equal.equals(obj));
    Assert.assertFalse("Symmetric: Object should NOT be equal to non-equal-object in EqualsTest",
        obj.equals(notequal));
    Assert.assertFalse("Symmetric: Non-equals-object should NOT be equal to object in EqualsTest!",
        notequal.equals(obj));
    // transitive: if a.equals(b) and b.equals(c) then a.equals(c)
    // not tested right now
    // hashCode: equal objects should have equal hash code
    Assert.assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!",
        obj.hashCode() == equal.hashCode());
    Assert.assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!",
        obj.hashCode() == obj.hashCode());
    Assert.assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!",
        equal.hashCode() == equal.hashCode());
    Assert.assertTrue("Transitive: Equal objects should have equal hash-code in EqualsTest!",
        notequal.hashCode() == notequal.hashCode());
  }

  /**
   * Compare to test.
   * 
   *
   * @param <T> 
   * @param obj 
   * @param equal 
   * @param notequal 
   */
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
    Assert.assertEquals("Object and equal-object should compare in CompareToTest!", 0,
        obj.compareTo((T) equal));
    Assert.assertFalse("Object and non-equal-object should not compare in CompareToTest!",
        0 == obj.compareTo((T) notequal));
    // first test some general things that should be true with equals
    // reflexive: equals to itself
    Assert.assertEquals("Reflexive: object should be equal to itself in CompareToTest!", 0,
        obj.compareTo((T) obj));
    Assert.assertEquals("Reflexive: equal-object should be equal to itself in CompareToTest!", 0,
        equal.compareTo((T) equal));
    Assert.assertEquals("Reflexive: non-equal-object should be equal to itself in CompareToTest!",
        0, notequal.compareTo((T) notequal));
    // not equals to null
    Assert.assertFalse("Object should not be equal to null in CompareToTest!",
        0 == obj.compareTo(null));
    Assert.assertFalse("Equal-object should not be equal to null in CompareToTest!",
        0 == equal.compareTo(null));
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
        "Symmetric: Object should NOT be equal to non-equal-object in CompareToTest",
        0 == obj.compareTo((T) notequal));
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
          "toString() is expected to result in the same result across repeated calls!", value,
          obj.toString());
    }
  }

  /**
   * Run some generic tests on the derived clone-method.
   * 
   * We need to do this via reflection as the clone()-method in Object is
   * protected and the Cloneable interface does not include a public "clone()".
   *
   * @param obj The object to test clone for.
   * @throws Exception the exception
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
    Assert.assertTrue("clone() should not return the object itself in CloneTest!",
        obj != m.invoke(obj, new Object[] {}));
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
   * Checks certain assumption that are made for the hashCode() method.
   *
   * @param obj An Object that override the hasCode() method.
   * @param equ 
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
    Assert.assertEquals("Equal objects should have equal hashCode() by Java contract!",
        obj.hashCode(), equ.hashCode());
  }

  /**
   * Enum test.
   * 
   *
   * @param enumtype 
   * @param enumclass 
   * @param element 
   * @throws NoSuchMethodException the no such method exception
   * @throws InvocationTargetException the invocation target exception
   * @throws IllegalAccessException the illegal access exception
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
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

  /**
   * Clear swing utilities queue.
   * 
   *
   * @throws InterruptedException the interrupted exception
   * @throws InvocationTargetException the invocation target exception
   */
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

  /**
   * Wait for a specific thread to finish.
   * @param name 
   * @throws InterruptedException the interrupted exception
   */
  public static void waitForThreadToFinish(final String name) throws InterruptedException {
    int count = Thread.currentThread().getThreadGroup().activeCount();
    Thread[] threads = new Thread[count];
    Thread.currentThread().getThreadGroup().enumerate(threads);
    for (Thread t : threads) {
      if (t != null && name.equals(t.getName())) {
        t.join();
      }
    }
  }

  public static void push(final List<StackItem> items, final boolean bKeepPrevious,
      final boolean bPushNext) {
    try {
      Class<?> queueModelClass = Class.forName("org.jajuk.services.players.QueueModel");
      Method pushMethod = queueModelClass.getDeclaredMethod("pushCommand", List.class,
          boolean.class, boolean.class);
      pushMethod.setAccessible(true);
      pushMethod.invoke(null, items, bKeepPrevious, bPushNext);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void push(final StackItem item, final boolean bKeepPrevious, final boolean bPushNext) {
    try {
      Class<?> queueModelClass = Class.forName("org.jajuk.services.players.QueueModel");
      Method pushMethod = queueModelClass.getDeclaredMethod("pushCommand", StackItem.class,
          boolean.class, boolean.class);
      pushMethod.setAccessible(true);
      pushMethod.invoke(null, item, bKeepPrevious, bPushNext);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Wait for all threads to finish.
   * 
   *
  * @throws InterruptedException the interrupted exception
   */
  public static void waitForAllThreadToFinish() throws InterruptedException {
    int count = Thread.currentThread().getThreadGroup().activeCount();
    Log.info(count + " threads active");
    Thread[] threads = new Thread[count];
    Thread.currentThread().getThreadGroup().enumerate(threads);
    for (Thread t : threads) {
      if (t != null && t.getClass().getPackage().getName().startsWith("org.jajuk")) {
        Log.info(t.getClass().getPackage().getName() + "/" + t.getName());
        t.join();
      }
    }
  }

  /**
   * Cleanup all the environment
   * 
   *
   * @throws InterruptedException the interrupted exception
   * @throws InvocationTargetException the invocation target exception
   */
  public static void cleanup() throws InterruptedException, InvocationTargetException {
    ObservationManager.clear();
    // Reset everything
    QueueModel.stopRequest();
    QueueModel.clear();
    FileManager.getInstance().clear();
    DirectoryManager.getInstance().clear();
    cleanAllDevices();
    History.getInstance().clear();
    //Reset everything again as it could have been changed during threads finishing
    ObservationManager.clear();
    // Reset everything
    QueueModel.stopRequest();
    QueueModel.clear();
    FileManager.getInstance().clear();
    DirectoryManager.getInstance().clear();
    cleanAllDevices();
    History.getInstance().clear();
  }

  public static void forceExitState(boolean state) {
    try {
      Field exitingField = ExitService.class.getDeclaredField("bExiting");
      exitingField.setAccessible(true);
      exitingField.setBoolean(null, state);
    } catch (SecurityException e) {
      Log.error(e);
    } catch (NoSuchFieldException e) {
      Log.error(e);
    } catch (IllegalArgumentException e) {
      Log.error(e);
    } catch (IllegalAccessException e) {
      Log.error(e);
    }
  }

  /**
  * Clean all devices.
  */
  public static void cleanAllDevices() {
    for (Device device : DeviceManager.getInstance().getDevices()) {
      // Do not auto-refresh CD as several CD may share the same mount
      // point
      if (device.getType() == Device.Type.FILES_CD) {
        continue;
      }
      FileManager.getInstance().cleanDevice(device.getName());
      DirectoryManager.getInstance().cleanDevice(device.getName());
      PlaylistManager.getInstance().cleanDevice(device.getName());
    }
    DeviceManager.getInstance().clear();
  }

  /**
     * Return a file named "test.tst" on a mounted device.
     * @return a file named "test.tst" on a mounted device.
   * @throws IOException 
     */
  public static org.jajuk.base.File getFile() {
    return getFile("test.tst", true);
  }

  public static org.jajuk.base.File getFile(String name, Directory dir, boolean mount,
      Class<? extends IPlayerImpl> clazz, Album album) {
    Genre genre = getGenre();
    album.setProperty(Const.XML_ALBUM_DISCOVERED_COVER, Const.COVER_NONE); // don't read covers for
    // this test
    Artist artist = getArtist("myartist");
    Year year = getYear(2000);
    Type type = getType(clazz);
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
    org.jajuk.base.File file = FileManager.getInstance().registerFile(name, dir, track, 120, 70);
    try {
      file.getFIO().createNewFile();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return file;
  }

  public static org.jajuk.base.File getFile(String name, Directory dir, boolean mount,
      Class<? extends IPlayerImpl> clazz) {
    Album album = getAlbum("name", 0);
    return getFile(name, dir, mount, clazz, album);
  }

  /**
   * Gets the file.
   *
   * @param name 
   * @param mount 
   * @return the file
   * @throws IOException 
   */
  public static org.jajuk.base.File getFile(String name, boolean mount) {
    Directory dir = TestHelpers.getDirectory();
    return getFile(name, dir, mount, MockPlayer.class);
  }

  /**
   * Gets a playlist listing the default file and located in the default directory
   *
   * @return the playlist
   * @throws IOException 
   */
  public static Playlist getPlaylist() throws IOException {
    Device device = getDevice();
    Directory dir = TestHelpers.getDirectory();
    File playlistFile = new File(dir.getAbsolutePath() + "/sample_playlist.m3u");
    org.jajuk.base.File file1 = getFile("1", true);
    org.jajuk.base.File file2 = getFile("2", true);
    BufferedWriter bw = new BufferedWriter(new FileWriter(playlistFile));
    bw.write(file1.getAbsolutePath() + "\n");
    bw.write(file2.getAbsolutePath());
    bw.flush();
    bw.close();
    String id = MD5Processor.hash(new StringBuilder(device.getName()).append(dir.getRelativePath())
        .append(playlistFile.getName()).toString());
    org.jajuk.base.Playlist playlist = PlaylistManager.getInstance().registerPlaylistFile(id,
        playlistFile.getName(), dir);
    // Force playlist loading
    try {
      playlist.getFiles();
    } catch (JajukException e) {
      Log.error(e);
    }
    return playlist;
  }

  /**
   * Gets a void playlist
   *
   * @return the playlist
   * @throws IOException 
   */
  public static Playlist getVoidPlaylist() throws IOException {
    Device device = getDevice();
    Directory dir = TestHelpers.getDirectory();
    File playlistFile = new File(dir.getAbsolutePath() + "/sample_playlist.m3u");
    String id = MD5Processor.hash(new StringBuilder(device.getName()).append(dir.getRelativePath())
        .append(playlistFile.getName()).toString());
    org.jajuk.base.Playlist playlist = PlaylistManager.getInstance().registerPlaylistFile(id,
        playlistFile.getName(), dir);
    return playlist;
  }

  /**
   * Gets the album.
   *
   * @param name 
   * @param discID 
   * @return the album
   */
  public static Album getAlbum(String name, int discID) {
    return AlbumManager.getInstance().registerAlbum(name, discID);
  }

  /**
   * Gets the album.
   *
   * @return the album
   */
  public static Album getAlbum() {
    return getAlbum("name", 0);
  }

  /**
   * Gets the artist.
   *
   * @param name 
   * @return the artist
   */
  public static Artist getArtist(String name) {
    return ArtistManager.getInstance().registerArtist(name);
  }

  /**
   * Gets the artist.
   *
   * @return the artist
   */
  public static Artist getArtist() {
    return getArtist("name");
  }

  /**
   * Gets the genre.
   *
   * @param name 
   * @return the genre
   */
  public static Genre getGenre(String name) {
    return GenreManager.getInstance().registerGenre(name);
  }

  /**
   * Gets the genre.
   *
   * @return the genre
   */
  public static Genre getGenre() {
    return getGenre("name");
  }

  /**
   * Gets the year.
   *
   * @param year 
   * @return the year
   */
  public static Year getYear(int year) {
    return YearManager.getInstance().registerYear("" + year);
  }

  /**
   * Gets the year.
   *
   * @return the year
   */
  public static Year getYear() {
    return getYear(2000);
  }

  /**
   * Gets the device.
   *
   * @param name 
   * @param type 
   * @param url 
   * @return the device
   */
  public static Device getDevice(String name, Device.Type type, String url) {
    // Create the jajuk test device if required
    new File(url).mkdirs();
    // Create at least a void file in the device
    try {
      new File(url + "/audio1.mp3").createNewFile();
    } catch (IOException e) {
      Log.error(e);
    }
    Device device = DeviceManager.getInstance().registerDevice(name, type, url);
    //Register the associated root directory
    DirectoryManager.getInstance().registerDirectory(device);
    return device;
  }

  /**
   * Gets the device., create it on disk if required
   *
   * @return the device
   */
  public static Device getDevice() {
    return getDevice("sample_device", Device.Type.DIRECTORY, ConstTest.DEVICES_BASE_PATH
        + "/sample_device");
  }

  /**
   * Gets the directory, create it on disk if required
   *
   * @param name 
   * @param parent 
   * @param device 
   * @return the directory
   */
  public static Directory getDirectory(String name, Directory parent, Device device) {
    // create the directory if it doesn't exist yet
    Directory dir = DirectoryManager.getInstance().registerDirectory(name, parent, device);
    dir.getFio().mkdirs();
    return dir;
  }

  public static Directory getDirectory(String name) {
    Device device = getDevice();
    Directory topdir = getTopDirectory();
    Directory dir = DirectoryManager.getInstance().registerDirectory(name, topdir, device);
    dir.getFio().mkdirs();
    return dir;
  }

  /**
   * The "any" directory is the top dir of the device 
   *, create it on disk if required
   * @return the directory
   */
  public static Directory getDirectory() {
    return getDirectory("dir");
  }

  /**
  * Returns a web radio
  *
  * @return the webradio
  */
  public static WebRadio getWebRadio(String name, String url, WebRadioOrigin origin) {
    WebRadio radio = WebRadioManager.getInstance().registerWebRadio(name);
    radio.setProperty(Const.XML_URL, url);
    radio.setProperty(Const.XML_ORIGIN, origin);
    return radio;
  }

  /**
  * Returns a web radio (invalid URL)
  *
  * @return the webradio
  */
  public static WebRadio getWebRadio() {
    return getWebRadio("preset1", "http://preset1", WebRadioOrigin.CUSTOM);
  }

  /**
  * Returns a list of web radio
  *
  * @return the list of web radio
  */
  public static List<WebRadio> getWebRadios() {
    //Reset radios
    WebRadioManager.getInstance().cleanup();
    WebRadio custom1 = getWebRadio("Custom 1", "http://custom1", WebRadioOrigin.CUSTOM);
    custom1.setProperty(Const.XML_BITRATE, new Long(127));
    custom1.setProperty(Const.XML_FREQUENCY, new Long(45000));
    custom1.setProperty(Const.XML_KEYWORDS, "foo,bar");
    custom1.setProperty(Const.XML_GENRE, "Pop");
    custom1.setProperty(Const.XML_DESC, "a cool radio");
    WebRadio custom2 = getWebRadio("Custom 2", "http://custom2", WebRadioOrigin.CUSTOM);
    WebRadio preset1 = getWebRadio("Preset 1", "http://preset1", WebRadioOrigin.PRESET);
    WebRadio preset2 = getWebRadio("Preset 2", "http://preset2", WebRadioOrigin.PRESET);
    List<WebRadio> radios = new ArrayList<WebRadio>();
    radios.add(custom1);
    radios.add(custom2);
    radios.add(preset1);
    radios.add(preset2);
    return radios;
  }

  /**
   * A topdir directory.
   *
   * @return the top directory
   */
  public static Directory getTopDirectory() {
    return DirectoryManager.getInstance().registerDirectory(getDevice());
  }

  /**
   * Gets the type.
   *
   * @return the type
   */
  public static Type getType() {
    return getType(MockPlayer.class);
  }

  /**
  * Gets the type with provided mplayer implementation class
  * @param playerImpl
  *
  * @return the type
  */
  public static Type getType(Class<? extends IPlayerImpl> clazz) {
    return TypeManager.getInstance().registerType("type", "mp3", clazz, null);
  }

  /**
   * Gets the track.
   *
   * @param i 
   * @return the track
   */
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

  /**
   * Helper to set a private field.
   *
   * @param obj the object to work on
   * @param fieldName the field name
   * @param value the field value to set
   * @throws SecurityException the security exception
   * @throws NoSuchFieldException the no such field exception
   * @throws IllegalArgumentException the illegal argument exception
   * @throws IllegalAccessException the illegal access exception
   */
  public static void setAttribute(Object obj, String fieldName, Object value)
      throws SecurityException, NoSuchFieldException, IllegalArgumentException,
      IllegalAccessException {
    Field field = obj.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(obj, value);
  }

  /**
   * Print a dump of all current threads to System.out
   */
  public static void dumpThreads() {
    Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
    Iterator<Thread> i = traces.keySet().iterator();
    while (i.hasNext()) {
      Thread thd = i.next();
      System.out.println("*** Thread id" + thd.getId() + ":" + thd.getName() + " ***");
      StackTraceElement[] trace = traces.get(thd);
      for (int j = 0; j < trace.length; ++j) {
        System.out.println(trace[j]);
      }
      System.out.println();
    }
  }
}
