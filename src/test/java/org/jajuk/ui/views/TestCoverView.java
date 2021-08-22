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
package org.jajuk.ui.views;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.util.Set;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestCoverView extends JajukTestCase {
  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.CoverView#componentResized(java.awt.event.ComponentEvent)}
   * .
   */
  public final void testComponentResized() {
    CoverView view = new CoverView();
    // this expects the UI to be available
    view.initUI();
    view.componentResized(new ComponentEvent(new Component() {
      private static final long serialVersionUID = 1L;
    }, 9));
    // resize immediately again to cover the time delay mechanism
    view.componentResized(new ComponentEvent(new Component() {
      private static final long serialVersionUID = 1L;
    }, 9));
  }

  /**
   * Test method for {@link org.jajuk.ui.views.CoverView#CoverView()}.
   */
  public final void testCoverView() {
    new CoverView();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.CoverView#CoverView(org.jajuk.base.File)}.
   */
  public final void testCoverViewFile() {
    new CoverView(getFile());
  }

  /**
   * Gets the file.
   *
   * @return the file
   */
  private File getFile() {
    Genre genre = TestHelpers.getGenre();
    Album album = TestHelpers.getAlbum("name", 0);
    Artist artist = TestHelpers.getArtist("name");
    Year year = TestHelpers.getYear(2000);
    Type type = TestHelpers.getType();
    Track track = TrackManager.getInstance().registerTrack("name", album, genre, artist, 120, year,
        1, type, 1);
    Device device = TestHelpers.getDevice();
    Directory dir = DirectoryManager.getInstance().registerDirectory(device);
    File file = FileManager.getInstance().registerFile("test.tst", dir, track, 120, 70);
    return file;
  }

  /**
   * Test method for {@link org.jajuk.ui.views.CoverView#initUI()}.
   */
  public final void testInitUI() {
    CoverView view = new CoverView();
    view.initUI();
  }

  /**
   * Test method for {@link org.jajuk.ui.views.CoverView#initUI(boolean)}.
   */
  public final void testInitUIBoolean() {
    {
      CoverView view = new CoverView();
      view.initUI(true);
    }
    {
      CoverView view = new CoverView();
      view.initUI(false);
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.CoverView#actionPerformed(java.awt.event.ActionEvent)}
   * .
   */
  public final void testActionPerformed() {
    CoverView view = new CoverView();
    view.initUI();
    // disable confirmations to not show UI during running tests
    Conf.setProperty(Const.CONF_CONFIRMATIONS_DELETE_COVER, "false");
    // different source, will not trigger anything
    view.actionPerformed(new ActionEvent("testsource", 1, "test"));
    // now try to trigger actions on each of the components in the view
    recursiveActionPerformed(view, view);
  }

  /**
   * Recursive action performed.
   * 
   *
   * @param cmp 
   * @param view 
   */
  private void recursiveActionPerformed(Container cmp, CoverView view) {
    for (int i = 0; i < cmp.getComponentCount(); i++) {
      // first call actionPerfomed with this component
      view.actionPerformed(new ActionEvent(cmp.getComponent(i), 1, "test"));
      // then again with CTRL-Mask set to trigger all parts
      view.actionPerformed(new ActionEvent(cmp.getComponent(i), 1, "test", ActionEvent.CTRL_MASK)); // set
      // CTRL_MASK
      // to
      // trigger
      // all
      // code
      // then recusively step into this components
      if (cmp.getComponent(i) instanceof Container) {
        recursiveActionPerformed((Container) cmp.getComponent(i), view);
      }
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.CoverView#createQuery(org.jajuk.base.File)}.
   */
  public final void testCreateQuery() {
    CoverView view = new CoverView();
    view.createQuery(getFile());
  }

  /**
   * Test method for {@link org.jajuk.ui.views.CoverView#getDesc()}.
   */
  public final void testGetDesc() {
    CoverView view = new CoverView();
    assertNotNull(view.getDesc());
    assertFalse(view.getDesc().isEmpty());
  }

  /**
   * Test method for {@link org.jajuk.ui.views.CoverView#getRegistrationKeys()}.
   */
  public final void testGetRegistrationKeys() {
    CoverView view = new CoverView();
    Set<JajukEvents> eventSubjectSet = view.getRegistrationKeys();
    assertNotNull(eventSubjectSet);
    assertTrue(eventSubjectSet.size() > 0);
  }

  /**
   * Test method for {@link org.jajuk.ui.views.CoverView#getCurrentImage()}.
   *
   * @throws Exception the exception
   */
  public final void testGetCurrentImage() throws Exception {
    CoverView view = new CoverView();
    // need to cover initialized
    view.initUI();
    // there is always a dummy image...
    assertNotNull(view.getCurrentImage());
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.CoverView#update(org.jajuk.events.JajukEvent)}.
   */
  public final void testUpdateJajukEvent() {
    CoverView view = new CoverView();
    // this expects the UI to be available
    view.initUI();
    // this is not catched currently, but should still not cause trouble
    view.update(new JajukEvent(JajukEvents.ALARMS_CHANGE));
    // these are catched and handled currently
    view.update(new JajukEvent(JajukEvents.FILE_LAUNCHED));
    view.update(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED));
    view.update(new JajukEvent(JajukEvents.ZERO));
    view.update(new JajukEvent(JajukEvents.PLAYER_STOP));
    view.update(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
  }
}
