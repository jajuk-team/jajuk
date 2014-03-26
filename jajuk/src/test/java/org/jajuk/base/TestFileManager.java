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
package org.jajuk.base;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.services.bookmark.History;
import org.jajuk.util.Const;
import org.jajuk.util.error.JajukException;
import org.junit.Test;

public class TestFileManager extends JajukTestCase {
  /**
   * Test method for {@link org.jajuk.base.FileManager#removeFile(File)}.
   * @throws IOException 
   *
   */
  @Test
  public void testRemoveFile() throws IOException {
    // Set-up...
    File file = TestHelpers.getFile();
    // Remove the reference
    FileManager.getInstance().removeFile(file);
    // 1- Check that the collection no more contains the file
    assertTrue(FileManager.getInstance().getFileByID(file.getID()) == null);
    // 2- check that associated track no more contains this file
    assertFalse(file.getTrack().getFiles().contains(file));
  }

  /**
   * Test method for {@link org.jajuk.base.FileManager#changeFileDirectory(org.jajuk.base.File, org.jajuk.base.Directory)}.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws JajukException the jajuk exception
   */
  @Test
  public void testChangeFileDirectory() throws IOException, JajukException {
    // Set-up...
    File oldFile = TestHelpers.getFile();
    oldFile.getDirectory().getFio().mkdirs();
    oldFile.getFIO().createNewFile();
    String newDirName = "top2";
    // Create a top2 directory just bellow device root
    Directory newDir = TestHelpers.getDirectory(newDirName, oldFile.getDevice().getRootDirectory(),
        oldFile.getDevice());
    // Create the physical directory if required
    newDir.getFio().mkdirs();
    // Perform the move
    File newFile = FileManager.getInstance().changeFileDirectory(oldFile, newDir);
    // Now test ...
    //1- Does the new file exist ?
    assertTrue(new java.io.File(newDir.getAbsolutePath() + '/' + oldFile.getName()).exists());
    //2- Does the old file is removed ?
    assertFalse(oldFile.getFIO().exists());
    //3- Does the associated track contains the right file (and only it)
    List<File> files = newFile.getTrack().getFiles();
    assertTrue(files.size() == 1 && files.get(0).equals(newFile));
  }

  @Test
  public void testGetFileByPath() {
    // test with default file 
    testWithFile(TestHelpers.getFile());
    // test with different files
    testWithFile(TestHelpers.getFile("ABC.tst", true));
    testWithFile(TestHelpers.getFile("ABC.tst", true));
    testWithFile(TestHelpers.getFile("0123234327\"§$%!§\"()432ABC-.,_:;#+*'*~\\}][{.tst", true));
  }

  private void testWithFile(File file) {
    assertNotNull("file " + file.getFIO() + " is not found if we look for the actual file name",
        FileManager.getInstance().getFileByPath(file.getFIO().getAbsolutePath()));
    assertNotNull("file " + file.getFIO() + " is not found if we look for the lowercase file name",
        FileManager.getInstance().getFileByPath(file.getFIO().getAbsolutePath().toLowerCase()));
    assertNotNull("file " + file.getFIO() + " is not found if we look for the uppercase file name",
        FileManager.getInstance().getFileByPath(file.getFIO().getAbsolutePath().toUpperCase()));
  }

  public void testFilterRecentlyPlayedTracksEnoughTracks() {
    int totalTracksNb = 500; // the 150 first tracks are recent and should be dropped
    List<File> files = populateHistory(totalTracksNb);
    FileManager.getInstance().filterRecentlyPlayedTracks(files);
    assertEquals(350, files.size()); 
  }
  
  
  public void testFilterRecentlyPlayedTracksLessThanActionNumber() {
    int totalTracksNb = 100; // all the 100 are recent but will not be dropped because 
    // we are under the lower of tracks
    List<File> files = populateHistory(totalTracksNb);
    FileManager.getInstance().filterRecentlyPlayedTracks(files);
    assertEquals(100, files.size());
  }
  
  public void testFilterRecentlyPlayedTracksABitMoreThanActionNumber() {
    int totalTracksNb = 250; // the first 150 are recent but not all of them will be
    // dropped to deal with the lower limit
    List<File> files = populateHistory(totalTracksNb);
    FileManager.getInstance().filterRecentlyPlayedTracks(files);
    assertEquals(Const.NB_TRACKS_ON_ACTION, files.size());
  }

  private List<File> populateHistory(int totalTracksNb) {
    long now = new Date().getTime();
    // create 500 items in collection and add them into history, 
    // we simulate a file per day in history.
    // file0 is now, file499 is 500 days away
    List<File> files = new ArrayList<File>(totalTracksNb);
    for (long i = totalTracksNb - 1; i >= 0; i--) { // i must be a long to avoid out of bounds
      File file = TestHelpers.getFile("file" + i, true);
      files.add(file);
      History.getInstance().addItem(file.getID(), now - i * 1000 * 3600 * 24);
    }
    return files;
  }
}
