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
package org.jajuk.services.osd;

import java.util.Properties;
import java.util.Set;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.services.players.IPlayerImpl;
import org.jajuk.services.players.TestQueueModel.MockPlayer;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.util.Const;

import junit.framework.TestCase;

/**
 * 
 */
public class TestOSDSupportImpl extends TestCase {

  /**
   * Test method for {@link org.jajuk.services.osd.OSDSupportImpl#getRegistrationKeys()}.
   */
  public void testGetRegistrationKeys() {
    OSDSupportImpl impl = new OSDSupportImpl();
    Set<JajukEvents> set = impl.getRegistrationKeys();
    assertTrue(set.toString(), set.contains(JajukEvents.FILE_LAUNCHED));
    assertTrue(set.toString(), set.contains(JajukEvents.WEBRADIO_LAUNCHED));
  }

  /**
   * Test method for {@link org.jajuk.services.osd.OSDSupportImpl#update(org.jajuk.events.JajukEvent)}.
   * @throws Exception 
   */
  public void testUpdate() throws Exception {
    // run update with Files/WebRadio
    
    OSDSupportImpl impl = new OSDSupportImpl();
    
    File file = getFile(1);
    
    Properties prop = new Properties();
    prop.setProperty(Const.DETAIL_CURRENT_FILE_ID, file.getID());
    
    impl.update(new JajukEvent(JajukEvents.FILE_LAUNCHED, prop));
    
    prop.put(Const.DETAIL_CONTENT, new WebRadio("testing", "url"));
    impl.update(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED, prop));
  }

  @SuppressWarnings("unchecked")
  private File getFile(int i) throws Exception {
    Style style = new Style(Integer.valueOf(i).toString(), "name");
    Album album = new Album(Integer.valueOf(i).toString(), "name", "artis", 23);
    album.setProperty(Const.XML_ALBUM_COVER, "none"); // don't read covers for
    // this test

    Author author = new Author(Integer.valueOf(i).toString(), "name");
    Year year = new Year(Integer.valueOf(i).toString(), "2000");

    IPlayerImpl imp = new MockPlayer();
    Class<IPlayerImpl> cl = (Class<IPlayerImpl>) imp.getClass();

    Type type = new Type(Integer.valueOf(i).toString(), "name", "mp3", cl, null);
    Track track = new Track(Integer.valueOf(i).toString(), "name", album, style, author, 120, year, 1,
        type, 1);

    Device device = new Device(Integer.valueOf(i).toString(), "name");
    device.setUrl(System.getProperty("java.io.tmpdir"));
    device.mount(true);

    Directory dir = new Directory(Integer.valueOf(i).toString(), "name", null, device);

    return FileManager.getInstance().registerFile(Integer.valueOf(i).toString(), "test.tst", dir, track, 120, 70);
  }
  
  /**
   * Test method for {@link org.jajuk.services.osd.OSDSupportImpl#isOSDAvailable()}.
   */
  public void testIsOSDAvailable() {
    // just call it here, test should also run on windows...
    OSDSupportImpl.isOSDAvailable();
  }
  
  public void testRegisterOSD() {
    // make sure any previous test did not leave it registered
    OSDSupportImpl.unregisterOSDSupport();
    
    // try to register first
    OSDSupportImpl.registerOSDSupport();
    
    // a second call CAN fail if it worked before, so we accept an exception, but can not expect it
    try {
      OSDSupportImpl.registerOSDSupport();
    } catch (IllegalArgumentException e) {
      // can happen if it worked the first time
    }
  }

  public void testUnregisterOSD() {
    // just call it first, might not do much
    OSDSupportImpl.unregisterOSDSupport();
        
    // try to register now
    OSDSupportImpl.registerOSDSupport();

    // now unregister can do more if the register call fully worked
    OSDSupportImpl.unregisterOSDSupport();
  }
}
