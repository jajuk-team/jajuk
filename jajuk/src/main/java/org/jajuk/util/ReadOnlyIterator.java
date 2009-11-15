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
 *  $Revision$
 */
package org.jajuk.util;

import java.util.Iterator;

/**
 * A convenient class that prevent remove() method from an iterator <br>
 * Generic class.
 */
public class ReadOnlyIterator<V> {

  /** Managed iterator *. */
  Iterator<V> iterator;

  /**
   * Instantiates a new read only iterator.
   * 
   * @param it DOCUMENT_ME
   */
  public ReadOnlyIterator(Iterator<V> it) {
    this.iterator = it;
  }

  /**
   * Next.
   * DOCUMENT_ME
   * 
   * @return the v
   */
  public V next() {
    return iterator.next();
  }

  /**
   * Checks for next.
   * DOCUMENT_ME
   * 
   * @return true if...
   */
  public boolean hasNext() {
    return iterator.hasNext();
  }

}
