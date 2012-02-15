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

import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;

/**
 * DOCUMENT_ME.
 */
public class TestTypeManager extends TestCase {

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // clear to make sure previous tests did not add anything
    TypeManager.getInstance().clear();

    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.base.TypeManager#getXMLTag()}.
   */
  public void testGetLabel() {
    assertEquals(Const.XML_TYPES, TypeManager.getInstance().getXMLTag());
  }

  /**
   * Test method for {@link org.jajuk.base.TypeManager#getInstance()}.
   */
  public void testGetInstance() {
    assertNotNull(TypeManager.getInstance());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.TypeManager#registerType(java.lang.String, java.lang.String, java.lang.Class, java.lang.Class)}
   * .
   */
  public void testRegisterType() {
    Type type = TypeManager.getInstance().registerType("testtype", "tst", null, null);
    assertNotNull(type);

    // try to register the same thing, we get back the same type
    Type type2 = TypeManager.getInstance().registerType("testtype", "tst", null, null);
    assertTrue(type == type2); // instance compare on purpose!

    // register the same type
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.TypeManager#isExtensionSupported(java.lang.String)}.
   */
  public void testIsExtensionSupported() {
    // not supported initially
    assertFalse(TypeManager.getInstance().isExtensionSupported("tst"));

    // register a type for it
    assertNotNull(TypeManager.getInstance().registerType("testtype", "tst", null, null));

    // supported now
    assertTrue(TypeManager.getInstance().isExtensionSupported("tst"));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.TypeManager#getTypeByExtension(java.lang.String)}.
   */
  public void testGetTypeByExtension() {
    // not supported initially
    assertNull(TypeManager.getInstance().getTypeByExtension("tst"));

    // register a type for it
    assertNotNull(TypeManager.getInstance().registerType("testtype", "tst", null, null));

    // supported now
    assertNotNull(TypeManager.getInstance().getTypeByExtension("tst"));
  }

  /**
   * Test method for {@link org.jajuk.base.TypeManager#getAllMusicTypes()}.
   *
   * @throws Exception the exception
   */
  public void testGetAllMusicTypes() throws Exception {
    // we need the ItemManagers when using properties
    StartupCollectionService.registerItemManagers();

    // no type initially
    assertEquals(0, TypeManager.getInstance().getAllMusicTypes().size());

    // register a type
    assertNotNull(TypeManager.getInstance().registerType("testtype", "tst", null, null));

    // still no type now as it does not have "music" property set
    assertEquals(0, TypeManager.getInstance().getAllMusicTypes().size());

    // add another type with "true" for "music"
    Type type = TypeManager.getInstance().registerType("testtype2", "tst2", null, null);
    type.setProperty(Const.XML_TYPE_IS_MUSIC, true);

    // now we have one music-type
    assertEquals(1, TypeManager.getInstance().getAllMusicTypes().size());

    // add default types
    TypeManager.registerTypesNoMplayer();

    // many types now
    assertTrue(TypeManager.getInstance().getAllMusicTypes().size() > 1);
  }

  /**
   * Test method for {@link org.jajuk.base.TypeManager#getTypeListString()}.
   *
   * @throws Exception the exception
   */
  public void testGetTypeListString() throws Exception {
    // no type initially
    assertEquals("", TypeManager.getInstance().getTypeListString());

    // register a type
    assertNotNull(TypeManager.getInstance().registerType("testtype", "tst", null, null));

    // one type now
    assertEquals("tst", TypeManager.getInstance().getTypeListString());

    // add default types
    TypeManager.registerTypesNoMplayer();

    // many types now
    assertTrue(TypeManager.getInstance().getTypeListString().length() > 4);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.TypeManager#getTypeByID(java.lang.String)}.
   */
  public void testGetTypeByID() {
    // not available initially
    assertNull(TypeManager.getInstance().getTypeByID("tst"));

    // add one type
    assertNotNull(TypeManager.getInstance().registerType("testtype", "tst", null, null));

    // available now
    assertNotNull(TypeManager.getInstance().getTypeByID("tst"));

    // others still not available
    assertNull(TypeManager.getInstance().getTypeByID("notexist"));
  }

  /**
   * Test method for {@link org.jajuk.base.TypeManager#getTypes()}.
   *
   * @throws Exception the exception
   */
  public void testGetTypes() throws Exception {
    // no type initially
    assertEquals(0, TypeManager.getInstance().getTypes().size());

    // register a type
    assertNotNull(TypeManager.getInstance().registerType("testtype", "tst", null, null));

    // one type now
    assertEquals(1, TypeManager.getInstance().getTypes().size());

    // add default types
    TypeManager.registerTypesNoMplayer();

    // many types now
    assertTrue(TypeManager.getInstance().getTypes().size() > 1);
  }

  /**
   * Test method for {@link org.jajuk.base.TypeManager#getTypesIterator()}.
   *
   * @throws Exception the exception
   */
  public void testGetTypesIterator() throws Exception {
    // no type initially
    assertFalse(TypeManager.getInstance().getTypesIterator().hasNext());

    // register a type
    assertNotNull(TypeManager.getInstance().registerType("testtype", "tst", null, null));

    // one type now
    assertTrue(TypeManager.getInstance().getTypesIterator().hasNext());

    // add default types
    TypeManager.registerTypesNoMplayer();

    // many types now
    assertTrue(TypeManager.getInstance().getTypesIterator().hasNext());
  }

  /**
   * Test method for {@link org.jajuk.base.TypeManager#registerTypesNoMplayer()}
   * .
   *
   * @throws Exception the exception
   */
  public void testRegisterTypesNoMplayer() throws Exception {
    TypeManager.registerTypesNoMplayer();

    assertTrue(TypeManager.getInstance().getTypes().size() > 1);
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.base.TypeManager#registerTypesMplayerAvailable()}.
   */
  public void testRegisterTypesMplayerAvailable() throws Exception {
    TypeManager.registerTypesMplayerAvailable();

    assertTrue(TypeManager.getInstance().getTypes().size() > 1);
  }

}
