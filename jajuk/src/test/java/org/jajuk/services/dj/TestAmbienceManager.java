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
 *  $Revision$
 */
package org.jajuk.services.dj;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Properties;

import org.jajuk.JajukTestCase;
import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;

/**
 * DOCUMENT_ME.
 */
public class TestAmbienceManager extends JajukTestCase {

  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // clean out all leftover ambiences from other testing
    for (Ambience amb : AmbienceManager.getInstance().getAmbiences()) {
      AmbienceManager.getInstance().removeAmbience(amb.getID());
    }

    super.setUp();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#getRegistrationKeys()}.
   */
  public final void testGetRegistrationKeys() {
    java.util.Set<JajukEvents> set = AmbienceManager.getInstance().getRegistrationKeys();
    assertTrue(set.contains(JajukEvents.GENRE_NAME_CHANGED));
  }

  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceManager#getInstance()}
   * .
   */
  public final void testGetInstance() {
    assertNotNull(AmbienceManager.getInstance());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceManager#load()}.
   *
   * @throws Exception the exception
   */
  public final void testLoad() throws Exception {
    // make sure "UpgradeManager.bFirstSession" is not set
    {
      Class<?> c = UpgradeManager.class;
      Field f = c.getDeclaredField("bFirstSession");
      f.setAccessible(true);
      f.setBoolean(null, Boolean.FALSE);
    }
    assertFalse(UpgradeManager.isFirstSession());

    assertEquals(0, AmbienceManager.getInstance().getAmbiences().size());

    // first without any key
    AmbienceManager.getInstance().load();
    // creates the 14 default ambiences
    assertEquals(14, AmbienceManager.getInstance().getAmbiences().size());

    // clean out all leftover ambiences from other testing
    for (Ambience amb : AmbienceManager.getInstance().getAmbiences()) {
      AmbienceManager.getInstance().removeAmbience(amb.getID());
    }
    assertEquals(0, AmbienceManager.getInstance().getAmbiences().size());

    { // remove all leftover properties
      Properties properties = Conf.getProperties();
      Enumeration<Object> e = properties.keys();
      while (e.hasMoreElements()) {
        String sKey = (String) e.nextElement();
        if (sKey.matches(Const.AMBIENCE_PREFIX + ".*")) {
          properties.remove(sKey);
        }
      }
    }

    // then add set some Ambience-items
    Genre genre1 = GenreManager.getInstance().registerGenre("genre1");
    Genre genre2 = GenreManager.getInstance().registerGenre("genre2");
    Conf.setProperty(Const.AMBIENCE_PREFIX + "12/myambience", genre1.getID() + "," + genre2.getID());
    assertEquals(0, AmbienceManager.getInstance().getAmbiences().size());

    { // check all the conditions to find out why it fails in Hudson
      assertFalse(UpgradeManager.isFirstSession());
      Properties properties = Conf.getProperties();
      Enumeration<Object> e = properties.keys();
      int nMatch = 0;
      while (e.hasMoreElements()) {
        String sKey = (String) e.nextElement();
        if (sKey.matches(Const.AMBIENCE_PREFIX + ".*")) {
          if (sKey.substring(Const.AMBIENCE_PREFIX.length()).indexOf('/') == -1) {
            continue;
          }

          nMatch++;
        }
      }
      assertEquals(properties.toString(), 1, nMatch);
    }
    AmbienceManager.getInstance().load();
    assertEquals(1, AmbienceManager.getInstance().getAmbiences().size());
    assertNotNull(AmbienceManager.getInstance().getAmbience("12"));

    // now test with an ambience with invalid format, i.e. only "12", not
    // "12/name"
    for (Ambience amb : AmbienceManager.getInstance().getAmbiences()) {
      AmbienceManager.getInstance().removeAmbience(amb.getID());
    }
    assertEquals(0, AmbienceManager.getInstance().getAmbiences().size());
    Conf.setProperty(Const.AMBIENCE_PREFIX + "12", genre1.getID() + "," + genre2.getID());
    Conf.removeProperty(Const.AMBIENCE_PREFIX + "12/myambience");
    AmbienceManager.getInstance().load();
    // now 14 as this could not be loaded and thus the default ones were
    // loaded...
    assertEquals(14, AmbienceManager.getInstance().getAmbiences().size());

    UpgradeManager.setFirstSession();
    AmbienceManager.getInstance().load();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#getAmbiences()}.
   */
  public final void testGetAmbiences() {
    // tested above
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#getAmbience(java.lang.String)}
   * .
   */
  public final void testGetAmbience() {
    // this creates the 14 default ambiences
    AmbienceManager.getInstance().createDefaultAmbiences();

    assertNotNull(AmbienceManager.getInstance().getAmbience("0"));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#getAmbienceByName(java.lang.String)}
   * .
   */
  public final void testGetAmbienceByName() {
    // this creates the 14 default ambiences
    AmbienceManager.getInstance().createDefaultAmbiences();

    assertNotNull(AmbienceManager.getInstance().getAmbienceByName(Messages.getString("Ambience.9")));
  }

  /**
   * Test get ambience by name invalid.
   * DOCUMENT_ME
   */
  public final void testGetAmbienceByNameInvalid() {
    // this creates the 14 default ambiences
    AmbienceManager.getInstance().createDefaultAmbiences();

    assertNull(AmbienceManager.getInstance().getAmbienceByName("notexistingone"));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#registerAmbience(org.jajuk.services.dj.Ambience)}
   * .
   */
  public final void testRegisterAmbience() {
    assertEquals(0, AmbienceManager.getInstance().getAmbiences().size());
    AmbienceManager.getInstance().registerAmbience(new Ambience("20", "ambience1"));
    assertEquals(1, AmbienceManager.getInstance().getAmbiences().size());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#getSelectedAmbience()}.
   */
  public final void testGetSelectedAmbience() {
    // first with no ambience and no default set
    assertNull(AmbienceManager.getInstance().getSelectedAmbience());

    // this creates the 14 default ambiences
    AmbienceManager.getInstance().createDefaultAmbiences();

    Conf.setProperty(Const.CONF_DEFAULT_AMBIENCE, "2"); // by ID

    // now we should find one
    assertNotNull(AmbienceManager.getInstance().getSelectedAmbience());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#update(org.jajuk.events.JajukEvent)}
   * .
   */
  public final void testUpdate() {
    Properties prop = new Properties();

    Genre genre1 = GenreManager.getInstance().registerGenre("genre1");
    Genre genre2 = GenreManager.getInstance().registerGenre("genre2");

    Ambience ambience = new Ambience("23", "testamb", new String[] { "genre1" });

    prop.put(Const.DETAIL_OLD, genre1);
    prop.put(Const.DETAIL_NEW, genre2);

    AmbienceManager.getInstance().registerAmbience(ambience);

    AmbienceManager.getInstance().update(new JajukEvent(JajukEvents.GENRE_NAME_CHANGED, prop));

    assertEquals(1, AmbienceManager.getInstance().getAmbiences().size());
    assertEquals(1, AmbienceManager.getInstance().getAmbiences().iterator().next().getGenres()
        .size());
    assertEquals("genre2", AmbienceManager.getInstance().getAmbiences().iterator().next()
        .getGenres().iterator().next().getName());
  }

  /**
   * Test method for {@link org.jajuk.services.dj.AmbienceManager#commit()}.
   */
  public final void testCommit() {
    // this creates the 14 default ambiences
    AmbienceManager.getInstance().createDefaultAmbiences();

    // set one ambience prefix to have it removed before
    Conf.setProperty(Const.AMBIENCE_PREFIX + "12/ambience", "testvalue");

    AmbienceManager.getInstance().commit();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#removeAmbience(java.lang.String)}
   * .
   */
  public final void testRemoveAmbience() {
    assertEquals(0, AmbienceManager.getInstance().getAmbiences().size());

    AmbienceManager.getInstance().registerAmbience(new Ambience("30", "nextone"));
    assertEquals(1, AmbienceManager.getInstance().getAmbiences().size());

    AmbienceManager.getInstance().removeAmbience("30");
    assertEquals(0, AmbienceManager.getInstance().getAmbiences().size());

  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.services.dj.AmbienceManager#createDefaultAmbiences()}.
   */
  public final void testCreateDefaultAmbiences() {
    assertEquals(0, AmbienceManager.getInstance().getAmbiences().size());
    AmbienceManager.getInstance().createDefaultAmbiences();

    // currently 14 ambiences are defined as default ones
    assertEquals(14, AmbienceManager.getInstance().getAmbiences().size());
  }

}
