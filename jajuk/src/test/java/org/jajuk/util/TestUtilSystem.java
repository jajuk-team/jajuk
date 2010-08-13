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
package org.jajuk.util;

import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.util.error.JajukException;

/**
 * 
 */
public class TestUtilSystem extends JajukTestCase {
  private File file1, file2;

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    file1 = File.createTempFile("test", ".jajuk");
    file2 = File.createTempFile("test", ".jajuk");

    super.setUp();
  }

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    // just try to remove those and ignore errors as the file might not have
    // been created
    file1.delete();
    file2.delete();

    super.tearDown();
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#backupFile(java.io.File, int)}.
   */
  public void testBackupFile() {
    // first test with no backup size set
    Conf.setProperty(Const.CONF_BACKUP_SIZE, "0");
    UtilSystem.backupFile(file1, 1);

    // then set some backup size
    Conf.setProperty(Const.CONF_BACKUP_SIZE, "100");
    UtilSystem.backupFile(file1, 1);

    // TODO: create a huge file and make sure it is truncated during backup
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#copy(java.io.File, java.io.File)}.
   */
  public void testCopyFileFile() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");

    UtilSystem.copy(file1, file2);

    assertEquals("this is some test data", FileUtils.readFileToString(file2));
  }

  public void testCopyFileException() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");
    assertTrue(file2.delete());
    assertTrue(file2.mkdirs()); // should not be able to write if a directory
    // already exists

    try {
      UtilSystem.copy(file1, file2);
      fail("Should report exception");
    } catch (IOException e) {
      //
    }
  }

  public void testCopyFileFileEmpty() throws Exception {
    FileUtils.writeStringToFile(file1, "");

    UtilSystem.copy(file1, file2);

    assertEquals("", FileUtils.readFileToString(file2));
  }

  public void testCopyFileFileNotExists() throws Exception {
    assertTrue(file1.delete());
    assertFalse(file1.exists());
    try {
      UtilSystem.copy(file1, file2);
      fail("Should throw exception");
    } catch (JajukException e) {
      assertEquals(9, e.getCode());
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#copy(java.io.File, java.lang.String)}.
   */
  public void testCopyFileString() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");

    UtilSystem.copy(file1, "testfile");

    // file is written into same directory as file1 here
    assertEquals("this is some test data", FileUtils.readFileToString(new File(new StringBuilder(
        file1.getParentFile().getAbsolutePath()).append('/').append("testfile").toString())));
  }

  public void testCopyFileStringEmpty() throws Exception {
    FileUtils.writeStringToFile(file1, "");

    UtilSystem.copy(file1, "testfile");

    assertEquals("", FileUtils.readFileToString(file2));
  }

  public void testCopyFileStringNotExists() throws Exception {
    assertTrue(file1.delete());
    assertFalse(file1.exists());
    try {
      UtilSystem.copy(file1, "testfile");
      fail("Should throw exception");
    } catch (JajukException e) {
      assertEquals(9, e.getCode());
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#copy(java.net.URL, java.lang.String)}.
   */
  public void testCopyURLString() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");

    UtilSystem.copy(file1.toURI().toURL(), file2.getAbsolutePath());

    // file is written into same directory as file1 here
    // TODO: currently the copy(URL) methods adds a newline at the end, should
    // we change that??
    assertEquals("this is some test data" + (SystemUtils.IS_OS_WINDOWS ? "\r" : "") + "\n",
        FileUtils.readFileToString(file2));
  }

  public void testCopyURLStringEmpty() throws Exception {
    FileUtils.writeStringToFile(file1, "");

    UtilSystem.copy(file1.toURI().toURL(), "testfile");

    assertEquals("", FileUtils.readFileToString(file2));
  }

  public void testCopyURLStringNotExists() {
    assertTrue(file1.delete());
    assertFalse(file1.exists());
    try {
      UtilSystem.copy(file1.toURI().toURL(), "testfile");
      fail("Should throw exception");
    } catch (IOException e) {
      // assertEquals(9, e.getCode());
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#copyRecursively(java.io.File, java.io.File)}
   * .
   */
  public void testCopyRecursivelySimple() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");

    UtilSystem.copyRecursively(file1, file2);

    assertEquals("this is some test data", FileUtils.readFileToString(file2));
  }

  public void testCopyRecursively() throws Exception {
    assertTrue(file1.delete());
    assertTrue(file1.mkdir());
    assertTrue(file2.delete());

    FileUtils.writeStringToFile(new File(file1.getAbsolutePath() + File.separator + "testfile"),
        "this is some test data");

    UtilSystem.copyRecursively(file1, file2);

    assertEquals("this is some test data", FileUtils.readFileToString(new File(file2
        .getAbsolutePath()
        + File.separator + "testfile")));
  }

  public void testCopyRecursivelyCannotMakeDir() throws Exception {
    assertTrue(file1.delete());
    assertTrue(file1.mkdir());

    FileUtils.writeStringToFile(new File(file1.getAbsolutePath() + File.separator + "testfile"),
        "this is some test data");

    try {
      UtilSystem.copyRecursively(file1, file2);
      fail("should throw exception because there is a file where we want to create a directory!");
    } catch (FileNotFoundException e) {

    }

    // assertEquals("", FileUtils.readFileToString(new
    // File(file2.getAbsolutePath() + File.separator
    // + "testfile")));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#copyToDir(java.io.File, java.io.File)}.
   */
  public void testCopyToDir() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");
    assertTrue(file2.delete());
    assertTrue(file2.mkdir());

    UtilSystem.copyToDir(file1, file2);

    assertEquals("this is some test data", FileUtils.readFileToString(new File(file2
        .getAbsolutePath()
        + File.separator + file1.getName())));
  }

  public void testCopyToDirException() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");
    assertTrue(file1.delete());

    try {
      UtilSystem.copyToDir(file1, file2);
      fail("Should throw exception");
    } catch (JajukException e) {
      assertEquals(9, e.getCode());
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#createEmptyFile(java.io.File)}.
   */
  public void testCreateEmptyFile() throws Exception {
    assertTrue(file1.delete());
    assertFalse(file1.exists());

    UtilSystem.createEmptyFile(file1);

    assertEquals("", FileUtils.readFileToString(file1));
  }

  public void testCreateEmptyFileException() {
    assertTrue(file1.delete());
    assertFalse(file1.exists());
    assertTrue(file1.mkdir()); // test should fail if we try to create a file
    // when a directory already exists

    try {
      UtilSystem.createEmptyFile(file1);
      fail("Should report exception");
    } catch (IOException e) {
      //
    }

    // still a directory now
    assertTrue(file1.exists());
    assertTrue(file1.isDirectory());
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#deleteDir(java.io.File)}.
   */
  public void testDeleteDir() throws Exception {
    assertTrue(file1.delete());
    assertTrue(file1.mkdir());

    FileUtils.writeStringToFile(new File(file1.getAbsolutePath() + File.separator + "testfile"),
        "this is some test data");

    UtilSystem.deleteDir(file1);

    assertFalse(file1.exists());
  }

  public void testDeleteDirFile() throws Exception {
    UtilSystem.deleteDir(file1);

    assertFalse(file1.exists());
  }

  public void testDeleteDirDir() throws Exception {
    assertTrue(file1.delete());

    // create a directory inside a directory
    assertTrue(new File(file1.getAbsolutePath() + File.separator + "testdir").mkdirs());

    FileUtils.writeStringToFile(new File(file1.getAbsolutePath() + File.separator + "testdir"
        + File.separator + "testfile"), "this is some test data");

    UtilSystem.deleteDir(file1);

    assertFalse(file1.exists());
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#deleteFile(java.io.File)}.
   */
  public void testDeleteFile() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");

    UtilSystem.deleteFile(file1);

    assertFalse(file1.exists());
  }

 
  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#getExtension(java.io.File)}.
   */
  public void testGetExtensionFile() {
    assertEquals("jajuk", UtilSystem.getExtension(file1));
  }

  public void testGetExtensionFileNone() {
    assertEquals("", UtilSystem.getExtension("/tmp/testfile"));
  }

  public void testGetExtensionFileDot() {
    // TODO: why do we return the full filename in this case? I.e. if there is a
    // "." as first character?
    assertEquals(".testfile", UtilSystem.getExtension(".testfile"));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#getExtension(java.lang.String)}.
   */
  public void testGetExtensionString() {
    assertEquals("jajuk", UtilSystem.getExtension(file1.getAbsolutePath()));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#getFileChecksum(java.io.File)}.
   */
  public void testGetFileChecksum() throws Exception {
    assertNotNull(UtilSystem.getFileChecksum(file1));
    assertFalse(UtilSystem.getFileChecksum(file1).equals(""));

    FileUtils.writeStringToFile(file1, "this is some test data");

    assertNotNull(UtilSystem.getFileChecksum(file1));
    assertFalse(UtilSystem.getFileChecksum(file1).equals(""));
  }

  public void testGetFileChecksumError() {
    try {
      UtilSystem.getFileChecksum(new File("notexistingfile.txt"));
      fail("Should throw exception");
    } catch (JajukException e) {
      assertEquals(103, e.getCode());
    }

  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#getHostName()}.
   */
  public void testGetHostName() {
    assertNotNull(UtilSystem.getHostName());
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#getJarLocation(java.lang.Class)}.
   */
  public void testGetJarLocation() {
    // check upfront to see where it is failing...
    assertNotNull(JajukException.class);
    assertNotNull(JajukException.class.getProtectionDomain());
    assertNotNull(JajukException.class.getProtectionDomain().getCodeSource());
    assertNotNull(JajukException.class.getProtectionDomain().getCodeSource().getLocation());

    assertNotNull(UtilSystem.getJarLocation(JajukException.class));
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#getMPlayerOSXPath()}.
   */
  public void testGetMPlayerOSXPath() {
    UtilSystem.getMPlayerOSXPath();
  }


  /**
   * Test method for {@link org.jajuk.util.UtilSystem#getMPlayerWindowsPath()}.
   */
  public void testGetMPlayerWindowsPath() {
    UtilSystem.getMPlayerWindowsPath();
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#getNormalizedFilename(java.lang.String)}.
   */
  public void testGetNormalizedFilename() {
    // assertEquals(file1.getAbsolutePath(),
    // UtilSystem.getNormalizedFilename(file1.getAbsolutePath()));

    assertEquals("-tmp  -test1----", UtilSystem.getNormalizedFilename("/tmp*|/te\"?st1<>\\:"));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#getOnlyFile(java.lang.String)}.
   */
  public void testGetOnlyFile() {
    assertEquals("name.txt", UtilSystem.getOnlyFile("file:///tmp/some/name.txt"));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#getResource(java.lang.String)}.
   */
  public void testGetResource() {
    assertNull(UtilSystem.getResource("unfoundresource"));

    assertNotNull(UtilSystem.getResource("icons/16x16/add_16x16.png"));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#isAncestor(java.io.File, java.io.File)}.
   */
  public void testIsAncestor() {
    assertFalse(UtilSystem.isAncestor(file1, file2));
    assertTrue(UtilSystem.isAncestor(new File("/tmp/"), new File("/tmp/test.txt")));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#isDescendant(java.io.File, java.io.File)}.
   */
  public void testIsDescendant() {
    assertFalse(UtilSystem.isDescendant(file2, file1));
    assertTrue(UtilSystem.isDescendant(new File("/tmp/test.txt"), new File("/tmp/")));
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#isUnderLinux()}.
   */
  public void testIsUnderLinux() {
    assertEquals(SystemUtils.IS_OS_LINUX, UtilSystem.isUnderLinux());
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#isUnderOSX()}.
   */
  public void testIsUnderOSX() {
       assertEquals(SystemUtils.IS_OS_MAC_OSX, UtilSystem.isUnderOSX());
  }

  
  /**
   * Test method for {@link org.jajuk.util.UtilSystem#isUnderWindows()}.
   */
  public void testIsUnderWindows() {
    assertEquals(SystemUtils.IS_OS_WINDOWS, UtilSystem.isUnderWindows());
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#isUnderWindows32bits()}.
   */
  public void testIsUnderWindows32bits() {
    UtilSystem.isUnderWindows32bits(); // cannot check
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#isUnderWindows64bits()}.
   */
  public void testIsUnderWindows64bits() {
    UtilSystem.isUnderWindows64bits(); // cannot check
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#isValidFileName(java.io.File, java.lang.String)}
   * .
   */
  public void testIsValidFileName() {
    // false whit invalid filenames
    assertFalse(UtilSystem.isValidFileName(null, null));
    assertFalse(UtilSystem.isValidFileName(file1, null));
    assertFalse(UtilSystem.isValidFileName(null, "test.txt"));

    // true with valid filenames
    assertTrue(UtilSystem.isValidFileName(new File("/tmp"), "testfile"));

    // already exists
    assertTrue(UtilSystem.isValidFileName(file1.getParentFile(), file1.getName()));

    // can be created
    assertTrue(file1.delete());
    assertTrue(UtilSystem.isValidFileName(file1.getParentFile(), file1.getName()));
    // should not exist after doing the test
    assertFalse(file1.exists());
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#needFullFC()}.
   */
  public void testNeedFullFC() {
    UtilSystem.needFullFC(); // cannot check
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#readFile(java.lang.String)}.
   */
  public void testReadFile() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");

    StringBuilder builder = UtilSystem.readFile(file1.getAbsolutePath());
    assertEquals("this is some test data", builder.toString());
  }

  public void testReadFileError() {
    try {
      UtilSystem.readFile("notexistingfile");
      fail("Should throw exception");
    } catch (JajukException e) {
      assertEquals(9, e.getCode());
      assertTrue(e.getMessage(), e.getMessage().contains("notexistingfile")); // do
      // we
      // also
      // have
      // the
      // invalid
      // filename
      // in
      // the
      // error?
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#readJarFile(java.lang.String)}.
   */
  public void testReadJarFile() throws Exception {
    try {
      StringBuilder builder = UtilSystem.readJarFile(UtilSystem
          .getJarLocation(JajukException.class).toString());
      assertNotNull(builder);
      assertFalse(builder.toString().isEmpty());
    } catch (NullPointerException e) {
      // TODO: we cannot run this test in eclipse as we do not have a Jajuk.jar
      // file available...
    }

  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#removeExtension(java.lang.String)}.
   */
  public void testRemoveExtension() {
    assertNotNull(UtilSystem.removeExtension(file1.getAbsolutePath()));
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#getClassLoader()}.
   */
  public void testGetClassLoader() {
    assertNotNull(UtilSystem.getClassLoader());
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#getDirFilter()}.
   */
  public void testGetDirFilter() {
    assertNotNull(UtilSystem.getDirFilter());
    assertNotNull(UtilSystem.getDirFilter());
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#getFileFilter()}.
   */
  public void testGetFileFilter() {
    assertNotNull(UtilSystem.getFileFilter());
    assertNotNull(UtilSystem.getFileFilter());
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#replaceInFile(java.io.File, java.lang.String, java.lang.String, java.lang.String)}
   * .
   */
  public void testReplaceInFileNotReplaced() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");

    assertFalse(UtilSystem.replaceInFile(file1, "notfound", "replaced", "UTF-8"));

    assertEquals("this is some test data", FileUtils.readFileToString(file1));
  }

  public void testReplaceInFileReplaced() throws Exception {
    FileUtils.writeStringToFile(file1, "this is some test data");

    assertTrue(UtilSystem.replaceInFile(file1, "test", "replaced", "UTF-8"));

    assertEquals("this is some replaced data", FileUtils.readFileToString(file1));
  }

  /**
   * Test method for {@link org.jajuk.util.UtilSystem#getRandom()}.
   */
  public void testGetRandom() {
    Random rnd = UtilSystem.getRandom();
    assertNotNull(rnd);
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#openInExplorer(java.io.File)}.
   */
  public void testOpenInExplorer() {
    try {
      UtilSystem.openInExplorer(file1.getParentFile());
    } catch (HeadlessException e) {
      // on some servers we cannot initalize any ui and thus cannot test this
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#isRunning(java.lang.Process)}.
   */
  public void testIsRunning() {
    assertTrue(UtilSystem.isRunning(new MockProcess(true)));

    assertFalse(UtilSystem.isRunning(new MockProcess(false)));
  }

  /**
   * Test method for
   * {@link org.jajuk.util.UtilSystem#getExitValue(java.lang.Process)}.
   */
  public void testGetExitValue() {
    assertEquals(0, UtilSystem.getExitValue(new MockProcess(false)));

    assertEquals(-100, UtilSystem.getExitValue(new MockProcess(true)));
  }

  /**
   * 
   */
  private static final class MockProcess extends Process {
    boolean throwInExitValue;

    /**
     * @param throwInWait
     */
    public MockProcess(boolean throwInWait) {
      super();
      this.throwInExitValue = throwInWait;
    }

    @Override
    public int waitFor() throws InterruptedException {
      return 0;
    }

    @Override
    public OutputStream getOutputStream() {

      return null;
    }

    @Override
    public InputStream getInputStream() {

      return null;
    }

    @Override
    public InputStream getErrorStream() {

      return null;
    }

    @Override
    public int exitValue() {
      if (throwInExitValue) {
        throw new IllegalThreadStateException("testexception");
      }

      return 0;
    }

    @Override
    public void destroy() {

    }
  }

  // helper method to emma-coverage of the unused constructor
  public void testPrivateConstructor() throws Exception {
    // For EMMA code-coverage tests
    JUnitHelpers.executePrivateConstructor(UtilSystem.class);
  }
}
