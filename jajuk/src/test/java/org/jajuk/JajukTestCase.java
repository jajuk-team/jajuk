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
package org.jajuk;

import junit.framework.TestCase;

import org.jajuk.base.Collection;
import org.jajuk.services.startup.StartupCollectionService;

/**
 * DOCUMENT_ME.
 */
public abstract class JajukTestCase extends TestCase {

  /*
   * (non-Javadoc)
   * 
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    // let's clean up before we begin any test
    JUnitHelpers.waitForAllWorkToFinishAndCleanup();
    // Clean the collection
    StartupCollectionService.registerItemManagers();
    Collection.clearCollection();
    super.setUp();
  }
}
