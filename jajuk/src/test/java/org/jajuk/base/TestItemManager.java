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
package org.jajuk.base;

import ext.services.xml.XMLUtils;

import java.util.Date;
import java.util.Iterator;

import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.services.startup.StartupCollectionService;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;

/**
 * DOCUMENT_ME.
 */
public class TestItemManager extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.base.ItemManager#ItemManager()}.
   */

  public final void testItemManager() {
    new LocalIM();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#switchAllManagersToOrderState()}.
   */

  public final void testSwitchAllManagersToOrderState() {
    // without Managers this will do nothing
    ItemManager.switchAllManagersToOrderState();

    StartupCollectionService.registerItemManagers();

    // now it should do more
    ItemManager.switchAllManagersToOrderState();
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#switchToOrderState()}.
   */

  public final void testSwitchToOrderState() {
    ItemManager man = new LocalIM();
    man.switchToOrderState();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#registerItemManager(java.lang.Class, org.jajuk.base.ItemManager)}
   * .
   */

  public final void testRegisterItemManager() {
    ItemManager man = new LocalIM();

    ItemManager.registerItemManager(String.class, man);
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#getXMLTag()}.
   */

  public final void testGetLabel() {
    ItemManager man = new LocalIM();

    assertEquals("TestLabel", man.getXMLTag());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#getMetaInformation(java.lang.String)}.
   */

  public final void testGetMetaInformation() {
    ItemManager man = new LocalIM();

    assertNotNull(man.getMetaInformation(Const.XML_ID));
    assertNull(man.getMetaInformation("unknown_property"));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#getHumanType(java.lang.String)}.
   */

  public final void testGetHumanType() {
    assertEquals("anykey", Messages.getHumanPropertyName("anykey"));
    assertFalse(Messages.getHumanPropertyName("disc_number"),
        "disc_number".equals(Messages.getHumanPropertyName("disc_number")));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#removeProperty(java.lang.String)}.
   */

  public final void testRemoveProperty() {
    ItemManager man = new LocalIM();

    // one not existing
    man.removeProperty("notexisting");

    // another one existing
    man.removeProperty(Const.XML_ID);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#applyRemoveProperty(org.jajuk.base.PropertyMetaInformation)}
   * .
   */

  public final void testApplyRemoveProperty() {
    ItemManager man = new LocalIM();

    PropertyMetaInformation meta = man.getMetaInformation(Const.XML_ID);

    // register some item before
    man.registerItem(new TestItem("3", "name"));

    man.applyRemoveProperty(meta);

    man.applyNewProperty(meta);
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#applyNewProperty(org.jajuk.base.PropertyMetaInformation)}
   * .
   */

  public final void testApplyNewProperty() {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#toXML()}.
   */

  public final void testToXML() {
    ItemManager man = new LocalIM();
    String xml = man.toXML();
    assertTrue(StringUtils.isNotBlank(xml));

    StringBuilder sb = new StringBuilder(xml);
    sb.append("</");
    sb.append(man.getXMLTag());
    sb.append('>');

    // valid xml?
    assertNotNull(sb.toString(), XMLUtils.getDocument(sb.toString()));
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#getProperties()}.
   */

  public final void testGetProperties() {
    ItemManager man = new LocalIM();
    assertNotNull(man.getProperties());
    assertEquals(1, man.getProperties().size()); // only one property registered
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#getCustomProperties()}.
   */

  public final void testGetCustomProperties() {
    ItemManager man = new LocalIM();

    assertNotNull(man.getCustomProperties());
    assertEquals(0, man.getCustomProperties().size());

    // add a custom property.
    man.registerProperty(new PropertyMetaInformation("testprop", true, true, false, false, false,
        String.class, null));

    assertNotNull(man.getCustomProperties());
    assertEquals(1, man.getCustomProperties().size()); // now one property is
    // registered
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#getVisibleProperties()}.
   */

  public final void testGetVisibleProperties() {
    ItemManager man = new LocalIM();

    assertNotNull(man.getVisibleProperties());
    assertEquals(0, man.getVisibleProperties().size());

    // add a custom property.
    man.registerProperty(new PropertyMetaInformation("testprop", true, true, true, false, false,
        String.class, null));

    assertNotNull(man.getVisibleProperties());
    assertEquals(1, man.getVisibleProperties().size()); // now one visible
    // property is
    // registered
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#getItemManager(java.lang.String)}.
   */

  public final void testGetItemManagerString() {
    assertNotNull(ItemManager.getItemManager(Const.XML_DEVICE));
    assertNotNull(ItemManager.getItemManager(Const.XML_TRACK));
    assertNotNull(ItemManager.getItemManager(Const.XML_ALBUM));
    assertNotNull(ItemManager.getItemManager(Const.XML_ARTIST));
    assertNotNull(ItemManager.getItemManager(Const.XML_GENRE));
    assertNotNull(ItemManager.getItemManager(Const.XML_DIRECTORY));
    assertNotNull(ItemManager.getItemManager(Const.XML_FILE));
    assertNotNull(ItemManager.getItemManager(Const.XML_PLAYLIST_FILE));
    assertNotNull(ItemManager.getItemManager(Const.XML_TYPE));
    assertNull(ItemManager.getItemManager("notexisting"));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#getItemManager(java.lang.Class)}.
   */

  public final void testGetItemManagerClassOfQ() {
    StartupCollectionService.registerItemManagers();

    assertNotNull(ItemManager.getItemManager(org.jajuk.base.Album.class));
    assertNotNull(ItemManager.getItemManager(org.jajuk.base.Artist.class));
    assertNotNull(ItemManager.getItemManager(org.jajuk.base.File.class));

    // take care, String.class is used for LocalIM which might be registered...
    assertNull(ItemManager.getItemManager(Date.class));
  }

  /**
  * Test method for {@link org.jajuk.base.ItemManager#cleanup()}.
  */

  public final void testCleanup() {
    ItemManager man = new LocalIM();
    man.cleanup();

    // TODO: add more sophisticated testing here
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#cleanOrphanTracks(org.jajuk.base.Item)}.
   */

  public final void testCleanOrphanTracks() {
    ItemManager man = new LocalIM();
    man.cleanOrphanTracks(null);

    // TODO: add more sophisticated testing here
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#removeItem(org.jajuk.base.Item)}.
   */

  public final void testRemoveItem() {
    ItemManager man = new LocalIM();
    man.removeItem(null);

    Item item = new TestItem("4", "name4");

    man.registerItem(item);

    assertEquals(1, man.getItems().size());

    man.removeItem(item);

    assertEquals(0, man.getItems().size());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#registerItem(org.jajuk.base.Item)}.
   */

  public final void testRegisterItem() {
    // tested in previous test
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#registerProperty(org.jajuk.base.PropertyMetaInformation)}
   * .
   */

  public final void testRegisterProperty() {
    // tested in testGetCustomProperties()
  }

  /**
   * Test method for.
   *
   * @throws Exception the exception
   * {@link org.jajuk.base.ItemManager#changeItem(org.jajuk.base.Item, java.lang.String, java.lang.Object, java.util.Set)}
   * .
   */

  public final void testChangeItem() throws Exception {
    ItemManager man = new LocalIM();

    Item item = new TestItem("5", "name5");
    man.registerItem(item);

    ItemManager.changeItem(item, Const.XML_ID, "6", null);

    // TODO: add more sophisticated testing here
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#getElementCount()}.
   */

  public final void testGetElementCount() {
    ItemManager man = new LocalIM();

    assertEquals(0, man.getElementCount());

    Item item = new TestItem("5", "name5");
    man.registerItem(item);

    assertEquals(1, man.getElementCount());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.base.ItemManager#getItemByID(java.lang.String)}.
   */

  public final void testGetItemByID() {
    ItemManager man = new LocalIM();

    Item item = new TestItem("5", "name5");
    man.registerItem(item);

    assertNotNull(man.getItemByID("5"));

    assertNull(man.getItemByID("6"));
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#getItems()}.
   */

  public final void testGetItems() {
    ItemManager man = new LocalIM();

    Item item = new TestItem("7", "name7");
    man.registerItem(item);
    item = new TestItem("8", "name8");
    man.registerItem(item);

    assertEquals(man.getItems().toString(), 2, man.getItems().size());
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#getItemsIterator()}.
   */

  public final void testGetItemsIterator() {
    ItemManager man = new LocalIM();

    Item item = new TestItem("9", "name9");
    man.registerItem(item);
    item = new TestItem("10", "name10");
    man.registerItem(item);

    Iterator<? extends Item> items = man.getItemsIterator();
    assertNotNull(items);
    assertTrue(items.hasNext());
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#clear()}.
   */

  public final void testClear() {
    ItemManager man = new LocalIM();

    Item item = new TestItem("9", "name9");
    man.registerItem(item);
    item = new TestItem("10", "name10");
    man.registerItem(item);

    assertEquals(2, man.getElementCount());

    man.clear();

    assertEquals(0, man.getElementCount());
  }

  /**
   * Test method for {@link org.jajuk.base.ItemManager#forceSorting()}.
   */

  public final void testForceSorting() {
    ItemManager man = new LocalIM();

    Item item = new TestItem("9", "name9");
    man.registerItem(item);
    item = new TestItem("10", "name10");
    man.registerItem(item);

    // now the order is in there, first 9, then 10
    Iterator<? extends Item> it = man.getItemsIterator();
    assertEquals("9", it.next().getID());
    assertEquals("10", it.next().getID());

    assertEquals(2, man.getElementCount());
    man.forceSorting();

    // still the same size
    assertEquals(2, man.getElementCount());

    // the iterator should still return 9 before 10
    it = man.getItemsIterator();
    assertEquals("9", it.next().getID());
    assertEquals("10", it.next().getID());
  }

  /**
   * Test sorting.
   * DOCUMENT_ME
   */
  public final void testSorting() {
    // first run without "ordered state"
    ItemManager man = new LocalIM();
    runSortingTest(man, true);

    // now use one with ordered state
    man = new LocalIM();
    man.switchToOrderState();
    runSortingTest(man, false);
  }

  /**
   * Run sorting test.
   * DOCUMENT_ME
   *
   * @param man DOCUMENT_ME
   * @param notYetOrderedState DOCUMENT_ME
   */
  private void runSortingTest(ItemManager man, boolean notYetOrderedState) {
    Item item = new TestItem("9", "name9");
    man.registerItem(item);
    item = new TestItem("10", "name10");
    man.registerItem(item);
    item = new TestItem("8", "name8");
    man.registerItem(item);
    item = new TestItem("11", "name11");
    man.registerItem(item);
    // will be added again because there is no exists-checking done
    item = new TestItem("8", "name8");
    man.registerItem(item);
    item = new TestItem("7", "name7");
    man.registerItem(item);
    item = new TestItem("12", "name12");
    man.registerItem(item);

    // only ordered as long as we have not switched to ordered state
    if (notYetOrderedState) {
      // now the order is in there, first 9, then 10
      Iterator<? extends Item> it = man.getItemsIterator();
      assertEquals("9", it.next().getID());
      assertEquals("10", it.next().getID());
      assertEquals("8", it.next().getID());
      assertEquals("11", it.next().getID());
      assertEquals("8", it.next().getID());
      assertEquals("7", it.next().getID());
      assertEquals("12", it.next().getID());
    }

    // in ordered state we do not add items multiple times
    if (notYetOrderedState) {
      assertEquals(7, man.getElementCount());
    } else {
      assertEquals(6, man.getElementCount());
    }
    man.forceSorting();

    // still the same size
    // in ordered state we do not add items multiple times
    if (notYetOrderedState) {
      assertEquals(7, man.getElementCount());
    } else {
      assertEquals(6, man.getElementCount());
    }

    if (notYetOrderedState) {
      // the iterator should still return 9 before 10
      Iterator<? extends Item> it = man.getItemsIterator();
      assertEquals("9", it.next().getID());
      assertEquals("10", it.next().getID());
      assertEquals("8", it.next().getID());
      assertEquals("11", it.next().getID());
      assertEquals("8", it.next().getID());
      assertEquals("7", it.next().getID());
      assertEquals("12", it.next().getID());
    }
  }

  /**
   * DOCUMENT_ME.
   */
  private static class LocalIM extends ItemManager {

    /* (non-Javadoc)
     * @see org.jajuk.base.ItemManager#getLabel()
     */
    @Override
    public String getXMLTag() {
      return "TestLabel";
    }

    /**
     * Instantiates a new local im.
     */
    public LocalIM() {
      super();

      registerProperty(new PropertyMetaInformation(Const.XML_ID, false, true, false, false, false,
          String.class, null));

    }
  }

  /**
   * DOCUMENT_ME.
   */
  private static class TestItem extends Item implements Comparable<Item> {

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Item o) {
      return getID().compareTo(o.getID());
    }

    /**
     * Instantiates a new test item.
     *
     * @param id DOCUMENT_ME
     * @param name DOCUMENT_ME
     */
    public TestItem(String id, String name) {
      super(id, name);
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.Item#getDesc()
     */
    @Override
    public String getDesc() {
      return "desc";
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.Item#getIconRepresentation()
     */
    @Override
    public ImageIcon getIconRepresentation() {
      return null;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    @Override
    public String getXMLTag() {
      return "Test";
    }

  }
}
