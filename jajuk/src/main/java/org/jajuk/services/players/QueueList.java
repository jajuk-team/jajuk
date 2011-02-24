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
package org.jajuk.services.players;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jajuk.base.File;

/**
 * Provides a container that encapsulates the normal queued items as well as the automatically planned items.
 */
public class QueueList implements List<StackItem> {

  /** Fifo itself, contains jajuk File objects. */
  private static volatile List<StackItem> alQueue = new ArrayList<StackItem>(50);

  /** Planned tracks, played if the normal queue is exhausted. */
  private static volatile List<StackItem> alPlanned = new ArrayList<StackItem>(10);

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#add(java.lang.Object)
   */
  @Override
  public boolean add(StackItem e) {
    return alQueue.add(e);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#add(int, java.lang.Object)
   */
  @Override
  public void add(int index, StackItem element) {
    alQueue.add(index, element);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#addAll(java.util.Collection)
   */
  @Override
  public boolean addAll(Collection<? extends StackItem> c) {
    return alQueue.addAll(c);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#addAll(int, java.util.Collection)
   */
  @Override
  public boolean addAll(int index, Collection<? extends StackItem> c) {
    return alQueue.addAll(index, c);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#clear()
   */
  @Override
  public void clear() {
    alQueue.clear();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#contains(java.lang.Object)
   */
  @Override
  public boolean contains(Object o) {
    return alQueue.contains(o);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#containsAll(java.util.Collection)
   */
  @Override
  public boolean containsAll(Collection<?> c) {
    return alQueue.containsAll(c);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#get(int)
   */
  @Override
  public StackItem get(int index) {
    return alQueue.get(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#indexOf(java.lang.Object)
   */
  @Override
  public int indexOf(Object o) {
    return alQueue.indexOf(o);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#isEmpty()
   */
  @Override
  public boolean isEmpty() {
    return alQueue.isEmpty();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#iterator()
   */
  @Override
  public Iterator<StackItem> iterator() {
    return alQueue.iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#lastIndexOf(java.lang.Object)
   */
  @Override
  public int lastIndexOf(Object o) {
    return alQueue.lastIndexOf(o);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#listIterator()
   */
  @Override
  public ListIterator<StackItem> listIterator() {
    return alQueue.listIterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#listIterator(int)
   */
  @Override
  public ListIterator<StackItem> listIterator(int index) {
    return alQueue.listIterator(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#remove(java.lang.Object)
   */
  @Override
  public boolean remove(Object o) {
    return alQueue.remove(o);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#remove(int)
   */
  @Override
  public StackItem remove(int index) {
    return alQueue.remove(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#removeAll(java.util.Collection)
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    return alQueue.removeAll(c);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#retainAll(java.util.Collection)
   */
  @Override
  public boolean retainAll(Collection<?> c) {
    return alQueue.retainAll(c);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#set(int, java.lang.Object)
   */
  @Override
  public StackItem set(int index, StackItem element) {
    return alQueue.set(index, element);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#size()
   */
  @Override
  public int size() {
    return alQueue.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#subList(int, int)
   */
  @Override
  public List<StackItem> subList(int fromIndex, int toIndex) {
    return alQueue.subList(fromIndex, toIndex);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#toArray()
   */
  @Override
  public Object[] toArray() {
    return alQueue.toArray();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.List#toArray(T[])
   */
  @Override
  public <T> T[] toArray(T[] a) {
    return alQueue.toArray(a);
  }

  /**
   * Gets a copy of the queue.
   * 
   * @return the queue
   */
  @SuppressWarnings("unchecked")
  public List<StackItem> getQueue() {
    return (List<StackItem>) ((ArrayList<StackItem>) alQueue).clone();
  }

  // ////////////////////////////////////////////////////////////
  // Methods for Planned tracks

  /**
   * Number of planned items.
   * 
   * @return the current number of planned items.
   */
  public int sizePlanned() {
    return alPlanned.size();
  }

  /**
   * Adds the item to the list of planned tracks.
   * 
   * @param e The StackItem to add.
   * 
   * @return true always.
   */
  public boolean addPlanned(StackItem e) {
    return alPlanned.add(e);
  }

  /**
   * Adds the planned.
   * DOCUMENT_ME
   * 
   * @param c DOCUMENT_ME
   */
  public void addPlanned(Collection<? extends StackItem> c) {
    for (StackItem item : c) {
      item.setPlanned(true);
      alPlanned.add(item);
    }
  }

  /**
   * Retrieve the planned track on a specified position.
   * 
   * @param index The position in the list of planned items to retrieve.
   * 
   * @return The StackItem at position index.
   */
  public StackItem getPlanned(int index) {
    return alPlanned.get(index);
  }

  /**
   * Gets a copy of the list of planned items.
   * 
   * @return The planned items.
   */
  @SuppressWarnings("unchecked")
  public List<StackItem> getPlanned() {
    return (List<StackItem>) ((ArrayList<StackItem>) alPlanned).clone();
  }

  /**
   * Removes the planned from list.
   * 
   * @param alFiles DOCUMENT_ME
   */
  public void removePlannedFromList(List<File> alFiles) {
    for (StackItem item : alPlanned) {
      // remove will do "contains" internally"
      alFiles.remove(item.getFile());
    }
  }

  /**
   * Clear planned.
   * DOCUMENT_ME
   */
  public void clearPlanned() {
    alPlanned.clear();
  }

  // ///////////////////////////////////////////////////////
  // Additional Methods for Queue handling

  /**
   * Contains repeat.
   * DOCUMENT_ME
   * 
   * @return true if...
   */
  public boolean containsRepeat() {
    for (StackItem item : alQueue) {
      if (item.isRepeat()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Pop next planned.
   * DOCUMENT_ME
   * 
   * @return the stack item
   */
  public StackItem popNextPlanned() {
    if (sizePlanned() != 0) {
      // return and remove the planned track
      return alPlanned.remove(0);
    } else {
      return null;
    }
  }

  /**
   * Contains only repeat.
   * DOCUMENT_ME
   * 
   * @return true if...
   */
  public boolean containsOnlyRepeat() {
    for (StackItem item : alQueue) {
      if (!item.isRepeat()) {
        return false;
      }
    }
    return true;
  }
}
