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
package org.jajuk.util.filters;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.jajuk.ConstTest;
import org.jajuk.TestHelpers;
import org.jajuk.JajukTestCase;

/**
 * .
 */
public class TestDirectoryFilter extends JajukTestCase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.filters.DirectoryFilter#accept(java.io.File)}.
   * @throws IOException 
   */
  public void testAcceptFile() throws IOException {
    assertTrue(DirectoryFilter.getInstance().accept(new File(ConstTest.TEMP_PATH)));
    assertFalse(DirectoryFilter.getInstance().accept(TestHelpers.getFile().getFIO()));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.filters.DirectoryFilter#getDescription()}.
   */
  public void testGetDescription() {
    // contents is locale specific
    assertTrue(StringUtils.isNotBlank(DirectoryFilter.getInstance().getDescription()));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.filters.DirectoryFilter#getInstance()}.
   */
  public void testGetInstance() {
    assertNotNull(DirectoryFilter.getInstance());
  }
}
