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
package org.jajuk.services.players;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.File;
import org.jajuk.util.error.JajukException;

/**
 * DOCUMENT_ME.
 */
public class TestQueueList extends JajukTestCase {

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#add(org.jajuk.services.players.StackItem)}.
   *
   * @throws Exception the exception
   */
  public void testAddStackItem() throws Exception {
    QueueList impl = new QueueList();
    impl.add(new StackItem(JUnitHelpers.getFile("file1", false)));
    assertEquals(1, impl.size());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#add(int, org.jajuk.services.players.StackItem)}.
   *
   * @throws Exception the exception
   */
  public void testAddIntStackItem() throws Exception {
    QueueList impl = new QueueList();
    File file1 = JUnitHelpers.getFile("file1", false);
    File file2 = JUnitHelpers.getFile("file2", false);

    impl.add(new StackItem(file1));
    assertEquals(1, impl.size());
    impl.add(0, new StackItem(file2));
    assertEquals(2, impl.size());

    // "2" should be first!
    assertEquals(file2, impl.get(0).getFile());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#addAll(java.util.Collection)}.
   *
   * @throws Exception the exception
   */
  public void testAddAllCollectionOfQextendsStackItem() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);
    assertEquals(3, impl.size());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#addAll(int, java.util.Collection)}.
   *
   * @throws Exception the exception
   */
  public void testAddAllIntCollectionOfQextendsStackItem() throws Exception {
    QueueList impl = new QueueList();

    impl.add(new StackItem(JUnitHelpers.getFile("file10", false)));
    assertEquals(1, impl.size());

    File file1 = JUnitHelpers.getFile("file1", false);
    File file2 = JUnitHelpers.getFile("file2", false);
    File file3 = JUnitHelpers.getFile("file3", false);

    List<StackItem> set = new ArrayList<StackItem>();
    set.add(new StackItem(file1));
    set.add(new StackItem(file2));
    set.add(new StackItem(file3));

    impl.addAll(0, set);
    assertEquals(4, impl.size());
    assertEquals(file1, impl.get(0).getFile());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#clear()}.
   *
   * @throws Exception the exception
   */
  public void testClear() throws Exception {
    QueueList impl = new QueueList();

    impl.add(new StackItem(JUnitHelpers.getFile("file10", false)));
    assertEquals(1, impl.size());

    impl.clear();
    assertEquals(0, impl.size());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#contains(java.lang.Object)}.
   *
   * @throws Exception the exception
   */
  public void testContains() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);
    assertEquals(3, impl.size());

    assertTrue(impl.contains(new StackItem(JUnitHelpers.getFile("file1", false))));
    assertFalse(impl.contains(new StackItem(JUnitHelpers.getFile("file4", false))));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#containsAll(java.util.Collection)}.
   *
   * @throws Exception the exception
   */
  public void testContainsAll() throws Exception {
    QueueList impl = new QueueList();

    List<StackItem> set = prepareQueueImpl(impl);
    assertEquals(3, impl.size());

    assertTrue(impl.containsAll(set));

    // add a file that is not in the queue
    set.add(new StackItem(JUnitHelpers.getFile("file4", false)));
    assertFalse(impl.containsAll(set));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#get(int)}.
   *
   * @throws Exception the exception
   */
  public void testGet() throws Exception {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#indexOf(java.lang.Object)}.
   *
   * @throws Exception the exception
   */
  public void testIndexOf() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);
    assertEquals(3, impl.size());

    assertEquals(0, impl.indexOf(new StackItem(JUnitHelpers.getFile("file1", false))));
    assertEquals(1, impl.indexOf(new StackItem(JUnitHelpers.getFile("file2", false))));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#isEmpty()}.
   *
   * @throws Exception the exception
   */
  public void testIsEmpty() throws Exception {
    QueueList impl = new QueueList();

    assertTrue(impl.isEmpty());

    impl.add(new StackItem(JUnitHelpers.getFile("file1", false)));

    assertFalse(impl.isEmpty());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#iterator()}.
   *
   * @throws Exception the exception
   */
  public void testIterator() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);
    assertEquals(3, impl.size());

    Iterator<StackItem> it = impl.iterator();
    assertTrue(it.hasNext());
    it.next();
    assertTrue(it.hasNext());
    it.next();
    assertTrue(it.hasNext());
    it.next();
    assertFalse(it.hasNext());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#lastIndexOf(java.lang.Object)}.
   *
   * @throws Exception the exception
   */
  public void testLastIndexOf() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);
    assertEquals(3, impl.size());

    assertEquals(2, impl.lastIndexOf(new StackItem(JUnitHelpers.getFile("file3", false))));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#listIterator()}.
   *
   * @throws Exception the exception
   */
  public void testListIterator() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);
    assertEquals(3, impl.size());

    assertNotNull(impl.listIterator());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#listIterator(int)}.
   *
   * @throws Exception the exception
   */
  public void testListIteratorInt() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);
    assertEquals(3, impl.size());

    assertNotNull(impl.listIterator(1));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#remove(java.lang.Object)}.
   *
   * @throws Exception the exception
   */
  public void testRemoveObject() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);
    assertEquals(3, impl.size());

    // first it works
    assertTrue(impl.remove(new StackItem(JUnitHelpers.getFile("file2", false))));

    // then the element is not found any more
    assertFalse(impl.remove(new StackItem(JUnitHelpers.getFile("file2", false))));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#remove(int)}.
   *
   * @throws Exception the exception
   */
  public void testRemoveInt() throws Exception {
    QueueList impl = new QueueList();

    prepareQueueImpl(impl);

    // first it removes an item
    assertNotNull(impl.remove(2));

    // then there is no more item to remove
    try {
      assertNull(impl.remove(2));
      fail("Should catch an Exception here");
    } catch (IndexOutOfBoundsException e) {
      assertTrue(e.getMessage(), e.getMessage().contains("Index: 2, Size: 2"));
    }
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#removeAll(java.util.Collection)}.
   *
   * @throws Exception the exception
   */
  public void testRemoveAll() throws Exception {
    QueueList impl = new QueueList();

    List<StackItem> set = prepareQueueImpl(impl);

    assertTrue(impl.removeAll(set));
    assertEquals(0, impl.size());

    // no more changes
    assertFalse(impl.removeAll(set));
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#retainAll(java.util.Collection)}.
   *
   * @throws Exception the exception
   */
  public void testRetainAll() throws Exception {
    QueueList impl = new QueueList();

    List<StackItem> set = prepareQueueImpl(impl);

    impl.retainAll(set);
    assertEquals(3, impl.size());

    impl.retainAll(new HashSet<StackItem>());
    assertEquals(0, impl.size());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#set(int, org.jajuk.services.players.StackItem)}.
   *
   * @throws Exception the exception
   */
  public void testSet() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    impl.set(0, new StackItem(JUnitHelpers.getFile("file4", false)));

    assertEquals("file4", impl.get(0).getFile().getName());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#size()}.
   *
   * @throws Exception the exception
   */
  public void testSize() throws Exception {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#subList(int, int)}.
   *
   * @throws Exception the exception
   */
  public void testSubList() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    List<StackItem> items = impl.subList(1, 3);
    assertEquals(2, items.size());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#toArray()}.
   *
   * @throws Exception the exception
   */
  public void testToArray() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    Object[] arr = impl.toArray();
    assertEquals(3, arr.length);
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#toArray(T[])}.
   *
   * @throws Exception the exception
   */
  public void testToArrayTArray() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    StackItem[] arr = impl.toArray(new StackItem[] {});
    assertEquals(3, arr.length);
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#getQueue()}.
   *
   * @throws Exception the exception
   */
  public void testGetQueue() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    List<StackItem> queue = impl.getQueue();
    assertEquals(3, queue.size());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#sizePlanned()}.
   *
   * @throws Exception the exception
   */
  public void testSizePlanned() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    assertEquals(0, impl.sizePlanned());
    impl.addPlanned(new StackItem(JUnitHelpers.getFile("file2", false)));

    assertEquals(1, impl.sizePlanned());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#addPlanned(org.jajuk.services.players.StackItem)}.
   *
   * @throws Exception the exception
   */
  public void testAddPlannedStackItem() throws Exception {
    // tested above
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#addPlanned(java.util.List)}.
   *
   * @throws Exception the exception
   */
  public void testAddPlannedListOfStackItem() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    prepareQueueImplPlanned(impl);

    assertEquals(2, impl.sizePlanned());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#getPlanned(int)}.
   *
   * @throws Exception the exception
   */
  public void testGetPlannedInt() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    prepareQueueImplPlanned(impl);

    assertEquals("file9", impl.getPlanned(0).getFile().getName());
    assertEquals("file8", impl.getPlanned(1).getFile().getName());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#getPlanned()}.
   *
   * @throws Exception the exception
   */
  public void testGetPlanned() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    prepareQueueImplPlanned(impl);

    assertEquals(2, impl.getPlanned().size());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#removePlannedFromList(java.util.List)}.
   *
   * @throws Exception the exception
   */
  public void testRemovePlannedFromList() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    prepareQueueImplPlanned(impl);

    List<File> items = new ArrayList<File>();

    impl.removePlannedFromList(items);

    assertEquals(0, items.size());

    items.add(JUnitHelpers.getFile("file2", false));
    items.add(JUnitHelpers.getFile("file8", false)); // this should be removed
    items.add(JUnitHelpers.getFile("file11", false));

    assertEquals(3, items.size());

    impl.removePlannedFromList(items);

    assertEquals(2, items.size());
    assertEquals("file2", items.get(0).getName());
    assertEquals("file11", items.get(1).getName());

  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#clearPlanned()}.
   *
   * @throws Exception the exception
   */
  public void testClearPlanned() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    prepareQueueImplPlanned(impl);

    impl.clearPlanned();

    assertEquals(3, impl.size());
    assertEquals(0, impl.sizePlanned());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#containsRepeat()}.
   *
   * @throws Exception the exception
   */
  public void testContainsRepeat() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    // repeat is not automatically set
    assertFalse(impl.containsRepeat());

    impl.get(1).setRepeat(true);

    assertTrue(impl.containsRepeat());

    impl.get(1).setRepeat(false);

    assertFalse(impl.containsRepeat());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#popNextPlanned()}.
   *
   * @throws Exception the exception
   */
  public void testPopNextPlanned() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    prepareQueueImplPlanned(impl);

    assertNotNull(impl.popNextPlanned());
    assertNotNull(impl.popNextPlanned());
    assertNull(impl.popNextPlanned());
  }

  /**
   * Test method for {@link org.jajuk.services.players.QueueList#containsOnlyRepeat()}.
   *
   * @throws Exception the exception
   */
  public void testContainsOnlyRepeat() throws Exception {
    QueueList impl = new QueueList();
    prepareQueueImpl(impl);

    // repeat is not automatically set
    assertFalse(impl.containsOnlyRepeat());

    impl.get(1).setRepeat(true);

    assertFalse(impl.containsOnlyRepeat());

    impl.get(0).setRepeat(true);
    impl.get(2).setRepeat(true);

    assertTrue(impl.containsOnlyRepeat());

    impl.get(1).setRepeat(false);

    assertFalse(impl.containsOnlyRepeat());
  }

  /**
   * Prepare queue impl.
   * DOCUMENT_ME
   *
   * @param impl DOCUMENT_ME
   * @return the list
   * @throws JajukException the jajuk exception
   * @throws Exception the exception
   */
  private List<StackItem> prepareQueueImpl(QueueList impl) throws JajukException, Exception {
    List<StackItem> set = new ArrayList<StackItem>();
    set.add(new StackItem(JUnitHelpers.getFile("file1", false)));
    set.add(new StackItem(JUnitHelpers.getFile("file2", false)));
    set.add(new StackItem(JUnitHelpers.getFile("file3", false)));

    impl.addAll(set);
    return set;
  }

  /**
   * Prepare queue impl planned.
   * DOCUMENT_ME
   *
   * @param impl DOCUMENT_ME
   * @return the list
   * @throws JajukException the jajuk exception
   * @throws Exception the exception
   */
  private List<StackItem> prepareQueueImplPlanned(QueueList impl) throws JajukException, Exception {
    List<StackItem> set = new ArrayList<StackItem>();
    set.add(new StackItem(JUnitHelpers.getFile("file9", false)));
    set.add(new StackItem(JUnitHelpers.getFile("file8", false)));

    impl.addPlanned(set);
    return set;
  }
}
