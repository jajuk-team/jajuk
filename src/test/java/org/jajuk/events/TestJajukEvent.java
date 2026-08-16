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
package org.jajuk.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;

/**
 * .
 */
public class TestJajukEvent extends JajukTestCase {
  /**
   * Test method for {@link org.jajuk.events.JajukEvent#hashCode()}.
   */
  @Test
  public void testHashCode() {
    JajukEvent event1 = new JajukEvent(JajukEvents.VOLUME_CHANGED);
    JajukEvent event2 = new JajukEvent(JajukEvents.VOLUME_CHANGED);
    TestHelpers.HashCodeTest(event1, event2);
  }

  /**
   * Test hash code2.
   * 
   */
  @Test
  public void testHashCode2() {
    Properties prop1 = new Properties();
    prop1.setProperty("test", "value");
    Properties prop2 = new Properties();
    prop2.setProperty("test", "value");
    JajukEvent event1 = new JajukEvent(JajukEvents.VOLUME_CHANGED, prop1);
    JajukEvent event2 = new JajukEvent(JajukEvents.VOLUME_CHANGED, prop2);
    TestHelpers.HashCodeTest(event1, event2);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.events.JajukEvent#JajukEvent(org.jajuk.events.JajukEvents, java.util.Properties)}
   * .
   */
  @Test
  public void testJajukEventJajukEventsProperties() {
    Properties prop1 = new Properties();
    prop1.setProperty("test", "value");
    new JajukEvent(JajukEvents.VOLUME_CHANGED, prop1);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.events.JajukEvent#JajukEvent(org.jajuk.events.JajukEvents)}
   * .
   */
  @Test
  public void testJajukEventJajukEvents() {
    new JajukEvent(JajukEvents.VOLUME_CHANGED);
  }

  /**
   * Test method for {@link org.jajuk.events.JajukEvent#getDetails()}.
   */
  @Test
  public void testGetDetails() {
    Properties prop1 = new Properties();
    prop1.setProperty("test", "value");
    JajukEvent event1 = new JajukEvent(JajukEvents.VOLUME_CHANGED, prop1);
    assertNotNull(event1.getDetails());
    assertNotNull(event1.getDetails().get("test"));
    assertNull(event1.getDetails().get("notexist"));
    JajukEvent event2 = new JajukEvent(JajukEvents.VOLUME_CHANGED);
    assertNull(event2.getDetails());
  }

  /**
   * Test method for {@link org.jajuk.events.JajukEvent#getSubject()}.
   */
  @Test
  public void testGetSubject() {
    Properties prop1 = new Properties();
    prop1.setProperty("test", "value");
    JajukEvent event1 = new JajukEvent(JajukEvents.VOLUME_CHANGED, prop1);
    assertEquals(JajukEvents.VOLUME_CHANGED, event1.getSubject());
  }

  /**
   * Test method for {@link org.jajuk.events.JajukEvent#toString()}.
   */
  @Test
  public void testToString() {
    Properties prop1 = new Properties();
    prop1.setProperty("test", "value");
    JajukEvent event1 = new JajukEvent(JajukEvents.VOLUME_CHANGED, prop1);
    JajukEvent event2 = new JajukEvent(JajukEvents.VOLUME_CHANGED);
    TestHelpers.ToStringTest(event1);
    TestHelpers.ToStringTest(event2);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.events.JajukEvent#equals(java.lang.Object)}.
   */
  @Test
  public void testEqualsObject() {
    JajukEvent event1 = new JajukEvent(JajukEvents.VOLUME_CHANGED);
    JajukEvent event2 = new JajukEvent(JajukEvents.VOLUME_CHANGED);
    JajukEvent event3 = new JajukEvent(JajukEvents.FILE_FINISHED);
    TestHelpers.EqualsTest(event1, event2, event3);
  }

  /**
   * Test equals object2.
   * 
   */
  @Test
  public void testEqualsObject2() {
    Properties prop1 = new Properties();
    prop1.setProperty("test", "value");
    Properties prop2 = new Properties();
    prop2.setProperty("test", "value");
    Properties prop3 = new Properties();
    prop3.setProperty("test", "diffvalue");
    JajukEvent event1 = new JajukEvent(JajukEvents.VOLUME_CHANGED, prop1);
    JajukEvent event2 = new JajukEvent(JajukEvents.VOLUME_CHANGED, prop2);
    JajukEvent event3 = new JajukEvent(JajukEvents.VOLUME_CHANGED, prop3);
    TestHelpers.EqualsTest(event1, event2, event3);
  }
}
