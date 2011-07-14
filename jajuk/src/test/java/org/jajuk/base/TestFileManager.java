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

import java.io.IOException;
import java.util.List;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.util.error.JajukException;
import org.junit.Test;

/**
 * 
 */
public class TestFileManager extends JajukTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
   /**
   * Test method for {@link org.jajuk.base.FileManager#removeFile(File)}.
   * @throws IOException 
   */
  @Test
  public void testRemoveFile() {
     // Set-up...
    File file = JUnitHelpers.getFile();
    
    // Remove the reference
    FileManager.getInstance().removeFile(file);
    
    // 1- Check that the collection no more contains the file
    assertTrue(FileManager.getInstance().getFileByID(file.getID()) == null);
    
    // 2- check that associated track no more contains this file
    assertFalse(file.getTrack().getFiles().contains(file));
    
    
  }

  /**
   * Test method for {@link org.jajuk.base.FileManager#changeFileDirectory(org.jajuk.base.File, org.jajuk.base.Directory)}.
   * @throws IOException 
   */
  @Test
  public void testChangeFileDirectory() throws IOException, JajukException {
    // Set-up...
    File oldFile = JUnitHelpers.getFile();
    oldFile.getDirectory().getFio().mkdirs();
    oldFile.getFIO().createNewFile();
    String newDirName = "top2";
    // Create a top2 directory just bellow device root
    Directory newDir = JUnitHelpers.getDirectory(newDirName,
        oldFile.getDevice().getRootDirectory(), oldFile.getDevice());
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
}
