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
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;

import org.jajuk.TestHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Genre;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.events.JajukEvents;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.views.TracksTreeView.TracksMouseAdapter;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * .
 */
public class TestTracksTreeView extends JajukTestCase {
  /* (non-Javadoc)
   * @see org.jajuk.JajukTestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    try {
      // initialize the actions
      ActionManager.getInstance();
    } catch (HeadlessException e) {
      // this is thrown in automated tests on Hudson/Sonar
    }
    super.setUp();
  }

  /**
   * Test method for {@link org.jajuk.ui.views.TracksTreeView#initUI()}.
   */
  public final void testInitUI() {
    TracksTreeView view = new TracksTreeView();
    tryInitUI(view);
  }

  /**
   * Try init ui.
   * 
   *
   * @param view 
   */
  private void tryInitUI(TracksTreeView view) {
    try {
      view.initUI();
    } catch (ExceptionInInitializerError e) {
      // reported on headless settings
    } catch (HeadlessException e) {
      // reported on headless settings
    } catch (NullPointerException e) {
      // sometimes reported deep inside Swing components, I could not find out
      // why...
    }
  }

  /**
   * Test method for {@link org.jajuk.ui.views.TracksTreeView#populateTree()}.
   */
  public final void testPopulateTree() {
    TracksTreeView view = new TracksTreeView();
    tryInitUI(view);
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    // try with different settings
    Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "0");
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "1");
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "2");
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "3");
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "4");
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "5");
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "6");
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "7");
    try {
      view.populateTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for {@link org.jajuk.ui.views.TracksTreeView#expand()}.
   */
  public final void testExpand() {
    TracksTreeView view = new TracksTreeView();
    try {
      view.expand();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for {@link org.jajuk.ui.views.TracksTreeView#getDesc()}.
   */
  public final void testGetDesc() {
    TracksTreeView view = new TracksTreeView();
    assertNotNull(view.getDesc());
  }

  /**
   * Test method for {@link org.jajuk.ui.views.TracksTreeView#TracksTreeView()}.
   */
  public final void testTracksTreeView() {
    new TracksTreeView();
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#getRegistrationKeys()}.
   */
  public final void testGetRegistrationKeys() {
    TracksTreeView view = new TracksTreeView();
    Set<JajukEvents> set = view.getRegistrationKeys();
    assertNotNull(set);
    assertTrue(set.contains(JajukEvents.FILE_LAUNCHED));
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByGenre()}.
   */
  public final void testPopulateTreeByGenre() {
    TracksTreeView view = new TracksTreeView();
    try {
      view.populateTreeByGenre();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByArtist()}.
   */
  public final void testPopulateTreeByArtist() {
    TracksTreeView view = new TracksTreeView();
    try {
      view.populateTreeByArtist();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByYear()}.
   */
  public final void testPopulateTreeByYear() {
    TracksTreeView view = new TracksTreeView();
    try {
      view.populateTreeByYear();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByAlbum()}.
   */
  public final void testPopulateTreeByAlbum() {
    TracksTreeView view = new TracksTreeView();
    try {
      view.populateTreeByAlbum();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByDiscovery()}.
   */
  public final void testPopulateTreeByDiscovery() {
    TracksTreeView view = new TracksTreeView();
    try {
      view.populateTreeByDiscovery();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByRate()}.
   */
  public final void testPopulateTreeByRate() {
    TracksTreeView view = new TracksTreeView();
    try {
      view.populateTreeByRate();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByHits()}.
   */
  public final void testPopulateTreeByHits() {
    TracksTreeView view = new TracksTreeView();
    try {
      view.populateTreeByHits();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for.
   *
   * {@link org.jajuk.ui.views.TracksTreeView#actionPerformed(java.awt.event.ActionEvent)}
   * .
   */
  public final void testActionPerformed() {
    TracksTreeView view = new TracksTreeView();
    view.actionPerformed(new ActionEvent(this, 1, ""));
  }

  /**
   * Test tracks tree selection listener.
   * 
   */
  public final void testTracksTreeSelectionListener() {
    TracksTreeView view = new TracksTreeView();
    TracksTreeView.TracksTreeSelectionListener task = view.new TracksTreeSelectionListener();
    try {
      task.valueChanged(new TreeSelectionEvent(this, new TreePath("test"), false, null, null));
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test tracks mouse adapter.
   * 
   */
  public final void testTracksMouseAdapter() {
    TracksTreeView view = new TracksTreeView();
    TracksMouseAdapter ad = view.new TracksMouseAdapter(null);
    assertNotNull(ad);
    MouseEvent event = new MouseEvent(new Component() {
      private static final long serialVersionUID = 1L;
    }, 1, 2l, 3, 2, 2, 3, true);
    assertTrue(event.isPopupTrigger());
    try {
      ad.handlePopup(event);
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    // popup trigger
    try {
      ad.mousePressed(event);
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    // normal event with CTRL is handled here
    event = new MouseEvent(new Component() {
      private static final long serialVersionUID = 1L;
    }, 1, 2l, InputEvent.CTRL_DOWN_MASK, 2, 2, 3, false);
    try {
      ad.mousePressed(event);
    } catch (NullPointerException e) {
      // reported on headless settings
    }
    event = new MouseEvent(new Component() {
      private static final long serialVersionUID = 1L;
    }, 1, 2l, 0, 2, 2, 3, true);
    try {
      ad.mouseReleased(event);
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test genre node.
   * 
   */
  public final void testGenreNode() {
    GenreNode ad = new GenreNode(getGenre());
    assertNotNull(ad);
    TestHelpers.ToStringTest(ad);
    assertNotNull(ad.getGenre());
    assertEquals("name", ad.getGenre().getName());
  }

  /**
   * Gets the genre.
   *
   * @return the genre
   */
  private Genre getGenre() {
    return TestHelpers.getGenre();
  }

  /**
   * Test artist node.
   * 
   */
  public final void testArtistNode() {
    ArtistNode ad = new ArtistNode(getArtist());
    assertNotNull(ad);
    TestHelpers.ToStringTest(ad);
    assertNotNull(ad.getArtist());
    assertEquals("name", ad.getArtist().getName());
  }

  /**
   * Gets the artist.
   *
   * @return the artist
   */
  private Artist getArtist() {
    return TestHelpers.getArtist("name");
  }

  /**
   * Test year node.
   * 
   */
  public final void testYearNode() {
    YearNode ad = new YearNode(getYear());
    assertNotNull(ad);
    TestHelpers.ToStringTest(ad);
    assertNotNull(ad.getYear());
    assertEquals("2000", ad.getYear().getName());
  }

  /**
   * Gets the year.
   *
   * @return the year
   */
  private Year getYear() {
    return TestHelpers.getYear(2000);
  }

  /**
   * Test album node.
   * 
   */
  public final void testAlbumNode() {
    AlbumNode ad = new AlbumNode(getAlbum());
    assertNotNull(ad);
    TestHelpers.ToStringTest(ad);
    assertNotNull(ad.getAlbum());
    assertEquals("name", ad.getAlbum().getName());
  }

  /**
   * Gets the album.
   *
   * @return the album
   */
  private Album getAlbum() {
    return TestHelpers.getAlbum("name", 234);
  }

  /**
   * Test track node.
   * 
   */
  public final void testTrackNode() {
    TrackNode ad = new TrackNode(getTrack());
    assertNotNull(ad);
    TestHelpers.ToStringTest(ad);
    assertNotNull(ad.getTrack());
    assertEquals("name", ad.getTrack().getName());
  }

  /**
   * Gets the track.
   *
   * @return the track
   */
  private Track getTrack() {
    return TrackManager.getInstance().registerTrack("name", getAlbum(), getGenre(), getArtist(),
        123, getYear(), 1, TestHelpers.getType(), 1);
  }

  /**
   * Test discovery date node.
   * 
   */
  public final void testDiscoveryDateNode() {
    DiscoveryDateNode ad = new DiscoveryDateNode(null);
    assertNotNull(ad);
  }

  /**
   * Test tracks tree cell renderer.
   * 
   */
  public final void testTracksTreeCellRenderer() {
    TracksTreeCellRenderer ad = new TracksTreeCellRenderer();
    assertNotNull(ad);
    Icon icon = ad.getIcon();
    // make sure we have a different icon after each call
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), new GenreNode(getGenre()), true,
        true, true, 1, true));
    assertFalse(ad.getIcon().equals(icon));
    icon = ad.getIcon();
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), new ArtistNode(getArtist()), true,
        true, true, 1, true));
    assertFalse(icon.equals(ad.getIcon()));
    icon = ad.getIcon();
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), new YearNode(getYear()), true, true,
        true, 1, true));
    assertFalse(icon.equals(ad.getIcon()));
    icon = ad.getIcon();
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), new AlbumNode(getAlbum()), true,
        true, true, 1, true));
    assertFalse(icon.equals(ad.getIcon()));
    icon = ad.getIcon();
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), new TrackNode(getTrack()), true,
        true, true, 1, true));
    assertFalse(icon.equals(ad.getIcon()));
    icon = ad.getIcon();
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), new DiscoveryDateNode("str"), true,
        true, true, 1, true));
    assertFalse(icon.equals(ad.getIcon()));
    icon = ad.getIcon();
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), "unknown object", true, true, true,
        1, true));
    assertFalse(icon.equals(ad.getIcon()));
    icon = ad.getIcon();
  }

  /**
   * Test tracks tree expansion listener.
   * 
   */
  public final void testTracksTreeExpansionListener() {
    TracksTreeExpansionListener ad = new TracksTreeExpansionListener();
    assertNotNull(ad);
    {
      Genre genre = getGenre();
      assertNull(genre.getProperties().get(Const.XML_EXPANDED));
      ad.treeExpanded(new TreeExpansionEvent("dummy", new TreePath(new GenreNode(genre))));
      assertEquals(true, genre.getProperties().get(Const.XML_EXPANDED));
      ad.treeCollapsed(new TreeExpansionEvent("dummy", new TreePath(new GenreNode(genre))));
      assertNull(genre.getProperties().get(Const.XML_EXPANDED));
    }
    {
      Artist genre = getArtist();
      assertNull(genre.getProperties().get(Const.XML_EXPANDED));
      ad.treeExpanded(new TreeExpansionEvent("dummy", new TreePath(new ArtistNode(genre))));
      assertEquals(true, genre.getProperties().get(Const.XML_EXPANDED));
      ad.treeCollapsed(new TreeExpansionEvent("dummy", new TreePath(new ArtistNode(genre))));
      assertNull(genre.getProperties().get(Const.XML_EXPANDED));
    }
    {
      Album genre = getAlbum();
      assertNull(genre.getProperties().get(Const.XML_EXPANDED));
      ad.treeExpanded(new TreeExpansionEvent("dummy", new TreePath(new AlbumNode(genre))));
      assertEquals(true, genre.getProperties().get(Const.XML_EXPANDED));
      ad.treeCollapsed(new TreeExpansionEvent("dummy", new TreePath(new AlbumNode(genre))));
      assertNull(genre.getProperties().get(Const.XML_EXPANDED));
    }
    {
      Year genre = getYear();
      assertNull(genre.getProperties().get(Const.XML_EXPANDED));
      ad.treeExpanded(new TreeExpansionEvent("dummy", new TreePath(new YearNode(genre))));
      assertEquals(true, genre.getProperties().get(Const.XML_EXPANDED));
      ad.treeCollapsed(new TreeExpansionEvent("dummy", new TreePath(new YearNode(genre))));
      assertNull(genre.getProperties().get(Const.XML_EXPANDED));
    }
  }
}
