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

import org.jajuk.JUnitHelpers;
import org.jajuk.JajukTestCase;
import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.Type;
import org.jajuk.base.Year;
import org.jajuk.events.JajukEvents;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.views.TracksTreeView.TracksMouseAdapter;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;

/**
 * 
 */
public class TestTracksTreeView extends JajukTestCase {

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
   * Test method for
   * {@link org.jajuk.ui.views.TracksTreeView#getRegistrationKeys()}.
   */
  public final void testGetRegistrationKeys() {
    TracksTreeView view = new TracksTreeView();
    Set<JajukEvents> set = view.getRegistrationKeys();
    assertNotNull(set);
    assertTrue(set.contains(JajukEvents.FILE_LAUNCHED));
  }

  /**
   * Test method for
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByStyle()}.
   */
  public final void testPopulateTreeByStyle() {
    TracksTreeView view = new TracksTreeView();

    try {
      view.populateTreeByStyle();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.ui.views.TracksTreeView#populateTreeByAuthor()}.
   */
  public final void testPopulateTreeByAuthor() {
    TracksTreeView view = new TracksTreeView();

    try {
      view.populateTreeByAuthor();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for
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
   * Test method for
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
   * Test method for
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
   * Test method for
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
   * Test method for
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
   * Test method for {@link org.jajuk.ui.views.TracksTreeView#cleanTree()}.
   */
  public final void testCleanTree() {
    TracksTreeView view = new TracksTreeView();

    try {
      view.cleanTree();
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

  /**
   * Test method for
   * {@link org.jajuk.ui.views.TracksTreeView#actionPerformed(java.awt.event.ActionEvent)}
   * .
   */
  public final void testActionPerformed() {
    TracksTreeView view = new TracksTreeView();
    view.actionPerformed(new ActionEvent(this, 1, ""));
  }

  public final void testTracksTreeSelectionListener() {
    TracksTreeView view = new TracksTreeView();
    TracksTreeView.TracksTreeSelectionListener task = view.new TracksTreeSelectionListener();

    try {
      task.valueChanged(new TreeSelectionEvent(this, new TreePath("test"), false, null, null));
    } catch (NullPointerException e) {
      // reported on headless settings
    }
  }

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

  public final void testStyleNode() {
    StyleNode ad = new StyleNode(getStyle());
    assertNotNull(ad);

    JUnitHelpers.ToStringTest(ad);
    assertNotNull(ad.getStyle());
    assertEquals("1", ad.getStyle().getID());
  }

  /**
   * @return
   */
  private Style getStyle() {
    return new Style("1", "name");
  }

  public final void testAuthorNode() {
    AuthorNode ad = new AuthorNode(getAuthor());
    assertNotNull(ad);

    JUnitHelpers.ToStringTest(ad);
    assertNotNull(ad.getAuthor());
    assertEquals("1", ad.getAuthor().getID());
  }

  /**
   * @return
   */
  private Author getAuthor() {
    return new Author("1", "name");
  }

  public final void testYearNode() {
    YearNode ad = new YearNode(getYear());
    assertNotNull(ad);

    JUnitHelpers.ToStringTest(ad);
    assertNotNull(ad.getYear());
    assertEquals("1", ad.getYear().getID());
  }

  /**
   * @return
   */
  private Year getYear() {
    return new Year("1", "name");
  }

  public final void testAlbumNode() {
    AlbumNode ad = new AlbumNode(getAlbum());
    assertNotNull(ad);

    JUnitHelpers.ToStringTest(ad);
    assertNotNull(ad.getAlbum());
    assertEquals("1", ad.getAlbum().getID());
  }

  /**
   * @return
   */
  private Album getAlbum() {
    return new Album("1", "name", 234);
  }

  public final void testTrackNode() {
    TrackNode ad = new TrackNode(getTrack());
    assertNotNull(ad);

    JUnitHelpers.ToStringTest(ad);
    assertNotNull(ad.getTrack());
    assertEquals("1", ad.getTrack().getID());
  }

  /**
   * @return
   */
  private Track getTrack() {
    return new Track("1", "name", getAlbum(), getStyle(), getAuthor(), 123, getYear(), 1, new Type(
        "3", "name", "ext", null, null), 1);
  }

  public final void testDiscoveryDateNode() {
    DiscoveryDateNode ad = new DiscoveryDateNode(null);
    assertNotNull(ad);
  }

  public final void testTracksTreeCellRenderer() {
    TracksTreeCellRenderer ad = new TracksTreeCellRenderer();
    assertNotNull(ad);

    Icon icon = ad.getIcon();
    // make sure we have a different icon after each call
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), new StyleNode(getStyle()), true,
        true, true, 1, true));
    assertFalse(ad.getIcon().equals(icon));
    icon = ad.getIcon();
    assertNotNull(ad.getTreeCellRendererComponent(new JTree(), new AuthorNode(getAuthor()), true,
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

  public final void testTracksTreeExpansionListener() {
    TracksTreeExpansionListener ad = new TracksTreeExpansionListener();
    assertNotNull(ad);

    {
      Style style = getStyle();
      assertNull(style.getProperties().get(Const.XML_EXPANDED));
      ad.treeExpanded(new TreeExpansionEvent("dummy", new TreePath(new StyleNode(style))));
      assertEquals(true, style.getProperties().get(Const.XML_EXPANDED));
      ad.treeCollapsed(new TreeExpansionEvent("dummy", new TreePath(new StyleNode(style))));
      assertNull(style.getProperties().get(Const.XML_EXPANDED));
    }

    {
      Author style = getAuthor();
      assertNull(style.getProperties().get(Const.XML_EXPANDED));
      ad.treeExpanded(new TreeExpansionEvent("dummy", new TreePath(new AuthorNode(style))));
      assertEquals(true, style.getProperties().get(Const.XML_EXPANDED));
      ad.treeCollapsed(new TreeExpansionEvent("dummy", new TreePath(new AuthorNode(style))));
      assertNull(style.getProperties().get(Const.XML_EXPANDED));
    }

    {
      Album style = getAlbum();
      assertNull(style.getProperties().get(Const.XML_EXPANDED));
      ad.treeExpanded(new TreeExpansionEvent("dummy", new TreePath(new AlbumNode(style))));
      assertEquals(true, style.getProperties().get(Const.XML_EXPANDED));
      ad.treeCollapsed(new TreeExpansionEvent("dummy", new TreePath(new AlbumNode(style))));
      assertNull(style.getProperties().get(Const.XML_EXPANDED));
    }

    {
      Year style = getYear();
      assertNull(style.getProperties().get(Const.XML_EXPANDED));
      ad.treeExpanded(new TreeExpansionEvent("dummy", new TreePath(new YearNode(style))));
      assertEquals(true, style.getProperties().get(Const.XML_EXPANDED));
      ad.treeCollapsed(new TreeExpansionEvent("dummy", new TreePath(new YearNode(style))));
      assertNull(style.getProperties().get(Const.XML_EXPANDED));
    }
  }
}
