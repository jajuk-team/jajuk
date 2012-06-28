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
 *  
 */
package org.jajuk.base;

import junit.framework.TestCase;

import org.jajuk.JUnitHelpers;
import org.jajuk.base.SearchResult.SearchResultType;

/**
 * .
 */
public class TestSearchResult extends TestCase {
  /**
   * Test method for {@link org.jajuk.base.SearchResult#hashCode()}.
   *
   * @throws Exception the exception
   */
  public void testHashCode() throws Exception {
    // TODO: this fails currently because there is no equals in SearchResult, should we add one? For now we just cover hashCode()
    // hashcode only looks at "sResu" parameter
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "");
    SearchResult equ = new SearchResult(JUnitHelpers.getFile("file2", true), "");
    assertEquals(res.hashCode(), equ.hashCode());
    //JUnitHelpers.HashCodeTest(res, equ);
    //    res = new SearchResult(new WebRadio("web", "url"), "webradio");
    //    equ = new SearchResult(new WebRadio("web", "url"), "webradio");
    //    JUnitHelpers.HashCodeTest(res, equ);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#SearchResult(org.jajuk.base.File)}.
   *
   * @throws Exception the exception
   */
  public void testSearchResultFile() throws Exception {
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "");
    assertNotNull(res);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#SearchResult(org.jajuk.base.File, java.lang.String)}.
   *
   * @throws Exception the exception
   */
  public void testSearchResultFileString() throws Exception {
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "testresult");
    assertNotNull(res);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#SearchResult(org.jajuk.services.webradio.WebRadio, java.lang.String)}.
   */
  public void testSearchResultWebRadioString() {
    SearchResult res = new SearchResult(JUnitHelpers.getWebRadio(), "testresult");
    assertNotNull(res);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#compareTo(org.jajuk.base.SearchResult)}.
   *
   * @throws Exception the exception
   */
  public void testCompareTo() throws Exception {
    // compareTo only looks at sResu-parameter
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu");
    SearchResult equ = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu");
    SearchResult notequ = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu1");
    JUnitHelpers.CompareToTest(res, equ, notequ);
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#getFile()}.
   *
   * @throws Exception the exception
   */
  public void testGetFile() throws Exception {
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu");
    assertEquals("file2", res.getFile().getName());
  }

  /**
   * Test method for {@link org.jajuk.base.SearchResult#getType()}.
   *
   * @throws Exception the exception
   */
  public void testGetType() throws Exception {
    SearchResult res = new SearchResult(JUnitHelpers.getFile("file2", true), "testresu");
    assertEquals(SearchResultType.FILE, res.getType());
    res = new SearchResult(JUnitHelpers.getWebRadio(), JUnitHelpers.getWebRadio().getName());
    assertEquals(SearchResultType.WEBRADIO, res.getType());
  }
}
