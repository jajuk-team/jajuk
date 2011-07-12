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
package org.jajuk.util.filters;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.jajuk.JajukTestCase;
import org.jajuk.base.TypeManager;
import org.jajuk.services.startup.StartupCollectionService;

/**
 * DOCUMENT_ME.
 */
public class TestAudioFilter extends JajukTestCase {

  /**
   * Test method for.
   *
   * {@link org.jajuk.util.filters.AudioFilter#accept(java.io.File)}.
   */
  public void testAcceptFile() {
    StartupCollectionService.registerTypes();

    // normal files
    assertFalse(AudioFilter.getInstance().accept(new File("test.tst")));
    assertTrue(AudioFilter.getInstance().accept(new File("test.mp3")));
    assertTrue(AudioFilter.getInstance().accept(new File("test.ogg")));
    assertTrue(AudioFilter.getInstance().accept(new File("test.MP3"))); // files
    // copied
    // from
    // windows
    // to
    // linux
    // might
    // be
    // uppercase
    assertTrue(AudioFilter.getInstance().accept(new File("test.OGG")));
    assertTrue(AudioFilter.getInstance().accept(new File("test.mP3"))); // manually
    // entered
    // filenames
    // might
    // be
    // lowercase/uppercase
    // mixed
    assertTrue(AudioFilter.getInstance().accept(new File("test.ogG")));

    // directories, depends on the setting
    AudioFilter.getInstance().setAcceptDirectories(false);
    assertFalse(AudioFilter.getInstance().accept(new File(System.getProperty("java.io.tmpdir"))));
    AudioFilter.getInstance().setAcceptDirectories(true);
    assertTrue(AudioFilter.getInstance().accept(new File(System.getProperty("java.io.tmpdir"))));
  }

  /**
   * Test method for {@link org.jajuk.util.filters.AudioFilter#getDescription()}
   * .
   */
  public void testGetDescription() {
    StartupCollectionService.registerTypes();

    assertTrue(AudioFilter.getInstance().getDescription(),
        StringUtils.containsIgnoreCase(AudioFilter.getInstance().getDescription(), "mp3"));
    assertTrue(AudioFilter.getInstance().getDescription(),
        StringUtils.containsIgnoreCase(AudioFilter.getInstance().getDescription(), "ogg"));

    // try removing all types
    TypeManager.getInstance().clear();
    assertEquals("", AudioFilter.getInstance().getDescription());
  }

  /**
   * Test method for {@link org.jajuk.util.filters.AudioFilter#getInstance()}.
   */
  public void testGetInstance() {
    assertNotNull(AudioFilter.getInstance());
  }
}
