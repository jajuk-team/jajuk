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

import junit.framework.Assert;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;

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
   * methods <p/> see
   * http://sourceforge.net/tracker/index.php?func=detail&aid=1173251&group_id=108932&atid=651900
   * 
   * add this to the test case for any class that has only static methods where
   * EMMA reports the default constructor as not covered
   * 
   * Template: // helper method to emma-coverage of the unused constructor
   * public void testPrivateConstructor() throws Exception { // For EMMA
   * code-coverage tests
   * JUnitHelpers.executePrivateConstructor(UtilSystem.class); }
   * 
   * @param targetClass
   */
  public static void executePrivateConstructor(final Class<?> targetClass) throws Exception {
    final Constructor<?> c = targetClass.getDeclaredConstructor(new Class[] {});
    c.setAccessible(true);
    c.newInstance((Object[]) null);
  }

  @SuppressWarnings("null")
  public static void EqualsTest(final Object obj, final Object equal, final Object notequal) {
    // none of the three should be null
    Assert.assertFalse("Object in EqualsTest should not be null!", null == obj);
    Assert.assertFalse("Equals-object in EqualsTest should not be null!", null == equal);
    Assert.assertFalse("Non-equal-object in EqualsTest should not be null!", null == notequal);

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
          "toString() is expected to result in the same result across repeated calls!", 
          value, obj.toString());
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
  @SuppressWarnings("null")
  public static void CloneTest(final Cloneable obj) throws Exception {
    final Method m = obj.getClass().getMethod("clone", new Class[] {});
    Assert.assertTrue("Need to find a method called 'clone' in object of type '"
        + obj.getClass().getName() + "' in CloneTest!", null != m);
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
    Assert.assertEquals(
        "Equal Assert.failed, but input to HashCodeTest should be two equal objects!", obj, equ);
    Assert.assertEquals("Equal objects should have equal hashCode() by Java contract!", obj
        .hashCode(), equ.hashCode());
  }

  @SuppressWarnings("unchecked")
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
      obj = Enum.valueOf(enumclass, "nonexistingenumelement");
      Assert
          .fail("Should catch exception IllegalArgumentException when calling Enum.valueOf() with incorrect enum-value!");
    } catch (IllegalArgumentException e) {
      Assert.assertTrue(e.getMessage(), e.getMessage().contains("No enum const class"));
    }
  }
}
