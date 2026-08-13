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
 *  $Revision: 3132 $
 */
package org.jajuk.services.tags;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.base.TypeManager;
import org.jajuk.util.error.JajukException;

public class TestTag extends JajukTestCase {
  /**
   * Test method for {@link org.jajuk.services.tags.Tag#Tag(java.io.File, boolean)}.
   * @throws Exception 
   */
  @Test
  public void testTag() throws Exception {
    // can read tag-type
    assertNotNull(TypeManager.getInstance().registerType("testtag", "tst", null,
        NoTagsTagImpl.class));
    Tag tag = new Tag(new File("somefile.tst"), false);
    assertFalse(tag.isCorrupted());
    tag = new Tag(new File("somefile.tst"), true);
    assertFalse(tag.isCorrupted());
    assertNotNull(TypeManager.getInstance().registerType("testtagnul", "nul", null, null));
    // null, do not ignore errors
    try {
      new Tag(new File("somefile.nul"), false);
      fail("Expect exception here");
    } catch (JajukException e) {
      // expected here
    }
    // null, do ignore errors
    tag = new Tag(new File("somefile.nul"), true);
    assertTrue(tag.isCorrupted());
  }

  @Test
  public void testTagInvalidTypes() throws Exception {
    // null, do not ignore errors
    try {
      new Tag(null, false);
      fail("Expect exception here");
    } catch (JajukException e) {
      // expected here
    }
    // null, but ignore errors, but is set to "corrupted"
    Tag tag = new Tag(null, true);
    assertTrue(tag.isCorrupted());
    // cannot read tag-type, do not ignore errors
    try {
      new Tag(new File("somefile"), false);
      fail("Expect exception here");
    } catch (JajukException e) {
      // expected here
    }
    // cannot read tag-type, but ignore errors and set to corrupted
    tag = new Tag(new File("somefile"), true);
    assertTrue(tag.isCorrupted());
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#hashCode()}.
   * @throws Exception 
   */
  @Test
  public void testHashCode() throws Exception {
    // can read tag-type
    assertNotNull(TypeManager.getInstance().registerType("testtag", "tst", null,
        NoTagsTagImpl.class));
    Tag tag = new Tag(new File("somefile.tst"), false);
    Tag equ = new Tag(new File("somefile.tst"), false);
    TestHelpers.HashCodeTest(tag, equ);
    tag = new Tag(null, true);
    equ = new Tag(null, true);
    TestHelpers.HashCodeTest(tag, equ);
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getTrackName()}.
   * @throws Exception 
   */
  @Test
  public void testGetTrackName() throws Exception {
    assertNotNull(TypeManager.getInstance().registerType("testtag", "tst", null,
        NoTagsTagImpl.class));
    Tag tag = new Tag(new File("somefile.tst"), false);
    assertEquals("somefile", tag.getTrackName());
    assertNotNull(TypeManager.getInstance().registerType("testtagnul", "nul", null, null));
    tag = new Tag(new File("somefile.nul"), true);
    assertEquals("somefile", tag.getTrackName());
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getAlbumName()}.
   */
  @Test
  public void testGetAlbumName() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getArtistName()}.
   */
  @Test
  public void testGetArtistName() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getAlbumArtist()}.
   */
  @Test
  public void testGetAlbumArtist() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getGenreName()}.
   */
  @Test
  public void testGetGenreName() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getLength()}.
   */
  @Test
  public void testGetLength() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getDiscNumber()}.
   */
  @Test
  public void testGetDiscNumber() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getYear()}.
   */
  @Test
  public void testGetYear() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getQuality()}.
   */
  @Test
  public void testGetQuality() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getComment()}.
   */
  @Test
  public void testGetComment() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getOrder()}.
   */
  @Test
  public void testGetOrder() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getLyrics()}.
   */
  @Test
  public void testGetLyrics() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getCovers()}.
   */
  @Test
  public void testGetCovers() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setTrackName(java.lang.String)}.
   */
  @Test
  public void testSetTrackName() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setAlbumName(java.lang.String)}.
   */
  @Test
  public void testSetAlbumName() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setArtistName(java.lang.String)}.
   */
  @Test
  public void testSetArtistName() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setAlbumArtist(java.lang.String)}.
   */
  @Test
  public void testSetAlbumArtist() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setGenreName(java.lang.String)}.
   */
  @Test
  public void testSetGenreName() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setOrder(long)}.
   */
  @Test
  public void testSetOrder() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setYear(java.lang.String)}.
   */
  @Test
  public void testSetYear() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setDiscNumber(long)}.
   */
  @Test
  public void testSetDiscNumber() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setComment(java.lang.String)}.
   */
  @Test
  public void testSetComment() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setLyrics(java.lang.String)}.
   */
  @Test
  public void testSetLyrics() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#deleteLyrics()}.
   */
  @Test
  public void testDeleteLyrics() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#commit()}.
   */
  @Test
  public void testCommit() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#isCorrupted()}.
   */
  @Test
  public void testIsCorrupted() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#setCorrupted(boolean)}.
   */
  @Test
  public void testSetCorrupted() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getFio()}.
   */
  @Test
  public void testGetFio() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#equals(java.lang.Object)}.
   * @throws Exception 
   */
  @Test
  public void testEqualsObject() throws Exception {
    assertNotNull(TypeManager.getInstance().registerType("testtag", "tst", null,
        NoTagsTagImpl.class));
    Tag tag = new Tag(new File("somefile.tst"), false);
    Tag equ = new Tag(new File("somefile.tst"), false);
    Tag notequ = new Tag(new File("somefile1.tst"), false);
    TestHelpers.EqualsTest(tag, equ, notequ);
    notequ = new Tag(null, true);
    TestHelpers.EqualsTest(tag, equ, notequ);
    tag = new Tag(null, true);
    equ = new Tag(null, true);
    notequ = new Tag(new File("somefile.tst"), false);
    TestHelpers.EqualsTest(tag, equ, notequ);
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#toString()}.
   * @throws Exception 
   */
  @Test
  public void testToString() throws Exception {
    assertNotNull(TypeManager.getInstance().registerType("testtag", "tst", null,
        NoTagsTagImpl.class));
    Tag tag = new Tag(new File("somefile.tst"), false);
    Tag tag2 = new Tag(null, true);
    TestHelpers.ToStringTest(tag);
    TestHelpers.ToStringTest(tag2);
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getTagForFio(java.io.File, boolean)}.
   */
  @Test
  public void testGetTagForFio() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#clearCache()}.
   */
  @Test
  public void testClearCache() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getTagField(java.lang.String)}.
   */
  @Test
  public void testGetTagField() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getSupportedTagFields()}.
   */
  @Test
  public void testGetSupportedTagFields() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.services.tags.Tag#getActivatedExtraTags()}.
   */
  @Test
  public void testGetActivatedExtraTags() {
    // TODO: implement test
  }
}
