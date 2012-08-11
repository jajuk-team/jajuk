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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.File;
import org.jajuk.base.Genre;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.TreeRootElement;
import org.jajuk.ui.helpers.TreeTransferHandler;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jvnet.substance.api.renderers.SubstanceDefaultTreeCellRenderer;

/**
 * Logical tree view.
 */
public class TracksTreeView extends AbstractTreeView implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** Sorting method selection combo. */
  private JComboBox jcbSort;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("TracksTreeView.0");
  }

  /**
   * Constructor.
   */
  public TracksTreeView() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  @Override
  public void initUI() {
    super.initUI();
    // ComboBox sort
    JLabel jlSort = new JLabel(Messages.getString("Sort"));
    jcbSort = new JComboBox();
    jcbSort.addItem(Messages.getHumanPropertyName(Const.XML_GENRE)); // sort by
    // Genre/Artist/Album
    jcbSort.addItem(Messages.getHumanPropertyName(Const.XML_ARTIST)); // sort by
    // Artist/Album
    jcbSort.addItem(Messages.getHumanPropertyName(Const.XML_ALBUM)); // sort by Album
    jcbSort.addItem(Messages.getHumanPropertyName(Const.XML_YEAR)); // sort by Year
    jcbSort.addItem(Messages.getString("TracksTreeView.35")); // sort by
    // Discovery Date
    jcbSort.addItem(Messages.getHumanPropertyName(Const.XML_TRACK_RATE)); // sort by rate
    jcbSort.addItem(Messages.getHumanPropertyName(Const.XML_TRACK_HITS)); // sort by hits
    // Load stored index, reset to index 0 in case of out of bounds (can happen after a version
    // upgrade)
    if (Conf.getInt(Const.CONF_LOGICAL_TREE_SORT_ORDER) >= jcbSort.getItemCount()) {
      Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, "0");
    }
    jcbSort.setSelectedIndex(Conf.getInt(Const.CONF_LOGICAL_TREE_SORT_ORDER));
    jcbSort.setActionCommand(JajukEvents.LOGICAL_TREE_SORT.toString());
    jcbSort.addActionListener(this);
    // Album details
    final JMenuItem jmiShowAlbumDetails = new JMenuItem(
        ActionManager.getAction(JajukActions.SHOW_ALBUM_DETAILS));
    jmiShowAlbumDetails.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    top = new TreeRootElement(Messages.getString("TracksTreeView.27"));
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // populate the tree
    populateTree();
    // create tree
    createTree(false);
    jtree.setCellRenderer(new TracksTreeCellRenderer());
    /**
     * CAUTION ! we register several listeners against this tree Swing can't
     * ensure the order where listeners will treat them so don't rely on the
     * mouse listener to get correct selection from selection listener
     */
    // Tree selection listener to detect a selection
    jtree.addTreeSelectionListener(new TracksTreeSelectionListener());
    // Listen for double click
    jtree.addMouseListener(new TracksMouseAdapter(jmiShowAlbumDetails));
    // Expansion analyze to keep expended state
    jtree.addTreeExpansionListener(new TracksTreeExpansionListener());
    jtree.setAutoscrolls(true);
    // DND support
    jtree.setTransferHandler(new TreeTransferHandler(jtree));
    jtree.setDragEnabled(true);
    jspTree = new JScrollPane(jtree);
    jspTree.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    setLayout(new MigLayout("ins 3", "[][grow][][]", "[][grow]"));
    add(jlSort, "left,gapx 5::");
    add(jcbSort, "grow,left");
    add(jtbSync, "right");
    add(jbCollapseAll, "right,wrap");
    add(jspTree, "grow,span");
    expand();
  }

  /**
   * Fill the tree.
   */
  @Override
  public void populateTree() {
    // Use a refreshing flag, not a 'synchronized' here (see deadlock, bug #1756 (Deadlock in AbstractTreeView and PerspectiveManager) 
    if (refreshing) {
      Log.debug("Tree view already refreshing. Leaving.");
      return;
    }
    try {
      refreshing = true;
      // delete previous tree
      top.removeAllChildren();
      TrackComparatorType comparatorType = TrackComparatorType.values()[Conf
          .getInt(Const.CONF_LOGICAL_TREE_SORT_ORDER)];
      if (comparatorType == TrackComparatorType.GENRE_ARTIST_ALBUM) {
        populateTreeByGenre();
      }// Artist/album
      else if (comparatorType == TrackComparatorType.ARTIST_ALBUM) {
        populateTreeByArtist();
      }
      // Album
      else if (comparatorType == TrackComparatorType.ALBUM) {
        populateTreeByAlbum();
      }
      // Year / album
      else if (comparatorType == TrackComparatorType.YEAR_ALBUM) {
        populateTreeByYear();
      }
      // discovery date / album
      else if (comparatorType == TrackComparatorType.DISCOVERY_ALBUM) {
        populateTreeByDiscovery();
      }
      // Rate / album
      else if (comparatorType == TrackComparatorType.RATE_ALBUM) {
        populateTreeByRate();
      }
      // Hits / album
      else if (comparatorType == TrackComparatorType.HITS_ALBUM) {
        populateTreeByHits();
      }
      // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472844 for a
      // small memory leak that is caused here...
      if (jtree != null && jtree.getModel() != null) {
        ((DefaultTreeModel) (jtree.getModel())).reload();
      }
    } finally {
      refreshing = false;
    }
  }

  /**
   * Fill the tree by genre.
   */
  @SuppressWarnings("unchecked")
  public void populateTreeByGenre() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        GenreNode genreNode = null;
        Genre genre = track.getGenre();
        ArtistNode artistNode = null;
        Artist artist = track.getArtist();
        AlbumNode albumNode = null;
        Album album = track.getAlbum();
        // create genre
        {
          Enumeration<GenreNode> e = top.children();
          boolean b = false;
          while (e.hasMoreElements()) { // check the genre doesn't
            // already exist
            GenreNode sn = e.nextElement();
            if (sn.getGenre().equals(genre)) {
              b = true;
              genreNode = sn;
              break;
            }
          }
          if (!b) {
            genreNode = new GenreNode(genre);
            top.add(genreNode);
          }
        }
        if (genreNode == null) {
          continue;
        }
        // create artist
        {
          Enumeration<ArtistNode> e2 = genreNode.children();
          boolean b = false;
          while (e2.hasMoreElements()) { // check if the artist doesn't
            // already exist
            ArtistNode an = e2.nextElement();
            if (an.getArtist().equals(artist)) {
              b = true;
              artistNode = an;
              break;
            }
          }
          if (!b) {
            artistNode = new ArtistNode(artist);
            genreNode.add(artistNode);
          }
        }
        // create album
        if (artistNode == null) {
          continue;
        }
        Enumeration<AlbumNode> e3 = artistNode.children();
        boolean b = false;
        while (e3.hasMoreElements()) {
          AlbumNode an = e3.nextElement();
          if (an.getAlbum().equals(album)) {
            b = true;
            albumNode = an;
            break;
          }
        }
        if (!b) {
          albumNode = new AlbumNode(album);
          artistNode.add(albumNode);
        }
        // create track
        assert albumNode != null;
        albumNode.add(new TrackNode(track));
      }
    }
  }

  /**
   * Fill the tree by artist.
   */
  @SuppressWarnings("unchecked")
  public void populateTreeByArtist() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        ArtistNode artistNode = null;
        Artist artist = track.getArtist();
        AlbumNode albumNode = null;
        Album album = track.getAlbum();
        // create artist
        {
          Enumeration<ArtistNode> e = top.children();
          boolean b = false;
          while (e.hasMoreElements()) { // check if the artist doesn't
            // already exist
            ArtistNode an = e.nextElement();
            if (an.getArtist().equals(artist)) {
              b = true;
              artistNode = an;
              break;
            }
          }
          if (!b) {
            artistNode = new ArtistNode(artist);
            top.add(artistNode);
          }
        }
        if (artistNode == null) {
          continue;
        }
        // create album
        Enumeration<AlbumNode> e2 = artistNode.children();
        boolean b = false;
        while (e2.hasMoreElements()) { // check if the album doesn't
          // already exist
          AlbumNode an = e2.nextElement();
          if (an.getAlbum().equals(album)) {
            b = true;
            albumNode = an;
            break;
          }
        }
        if (!b) {
          albumNode = new AlbumNode(album);
          artistNode.add(albumNode);
        }
        // create track
        if (albumNode != null) {
          albumNode.add(new TrackNode(track));
        }
      }
    }
  }

  /**
   * Fill the tree by year.
   */
  @SuppressWarnings("unchecked")
  public void populateTreeByYear() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        YearNode yearNode = null;
        AlbumNode albumNode = null;
        Album album = track.getAlbum();
        Year year = track.getYear();
        // create Year
        {
          Enumeration<YearNode> e = top.children();
          boolean b = false;
          // check if the artist doesn't already exist
          while (e.hasMoreElements()) {
            YearNode yn = e.nextElement();
            if (yn.getYear().equals(year)) {
              b = true;
              yearNode = yn;
              break;
            }
          }
          if (!b) {
            yearNode = new YearNode(year);
            top.add(yearNode);
          }
        }
        if (yearNode == null) {
          continue;
        }
        // create album
        Enumeration<AlbumNode> e1 = yearNode.children();
        boolean b = false;
        while (e1.hasMoreElements()) { // check if the album doesn't
          // already exist
          AlbumNode an = e1.nextElement();
          if (an.getAlbum().equals(album)) {
            b = true;
            albumNode = an;
            break;
          }
        }
        if (!b) {
          albumNode = new AlbumNode(album);
          yearNode.add(albumNode);
        }
        // create track
        if (albumNode != null) {
          albumNode.add(new TrackNode(track));
        }
      }
    }
  }

  /**
   * Fill the tree.
   */
  public void populateTreeByAlbum() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        addTrackAndAlbum(top, track);
      }
    }
  }

  /**
   * Fill the tree by discovery.
   */
  public void populateTreeByDiscovery() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    // Create separator nodes
    DefaultMutableTreeNode nodeWeekly = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.36"));
    DefaultMutableTreeNode nodeMontly = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.37"));
    DefaultMutableTreeNode nodeThreeMontly = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.44"));
    DefaultMutableTreeNode nodeSixMontly = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.38"));
    DefaultMutableTreeNode nodeYearly = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.40"));
    DefaultMutableTreeNode nodeTwoYearly = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.41"));
    DefaultMutableTreeNode nodeFiveYearly = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.42"));
    DefaultMutableTreeNode nodeTenYearly = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.43"));
    DefaultMutableTreeNode nodeOlder = new DiscoveryDateNode(
        Messages.getString("TracksTreeView.39"));
    // Add separator nodes
    top.add(nodeWeekly);
    top.add(nodeMontly);
    top.add(nodeThreeMontly);
    top.add(nodeSixMontly);
    top.add(nodeYearly);
    top.add(nodeTwoYearly);
    top.add(nodeFiveYearly);
    top.add(nodeTenYearly);
    top.add(nodeOlder);
    Date today = new Date();
    // Sort tracks into these categories
    for (Track track : tracks) {
      if (track.shouldBeHidden()) {
        continue;
      }
      // less than one week ?
      long diff = today.getTime() - track.getDiscoveryDate().getTime();
      if (diff < 604800000l) {
        addTrackAndAlbum(nodeWeekly, track);
      } else if (diff < 2628000000l) {
        addTrackAndAlbum(nodeMontly, track);
      } else if (diff < 7884000000l) {
        addTrackAndAlbum(nodeThreeMontly, track);
      } else if (diff < 15768000000l) {
        addTrackAndAlbum(nodeSixMontly, track);
      } else if (diff < 31536000000l) {
        addTrackAndAlbum(nodeYearly, track);
      } else if (diff < 63072000000l) {
        addTrackAndAlbum(nodeTwoYearly, track);
      } else if (diff < 157680000000l) {
        addTrackAndAlbum(nodeFiveYearly, track);
      } else if (diff < 315360000000l) {
        addTrackAndAlbum(nodeTenYearly, track);
      } else {
        addTrackAndAlbum(nodeOlder, track);
      }
    }
  }

  /**
   * Fill the tree by Rate.
   */
  public void populateTreeByRate() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        addTrackAndAlbum(top, track);
      }
    }
  }

  /**
   * Fill the tree by Hits.
   */
  public void populateTreeByHits() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        addTrackAndAlbum(top, track);
      }
    }
  }

  /**
   * Utility method used by populateByDiscovery method.
   * 
   * @param node 
   * @param track 
   */
  @SuppressWarnings("unchecked")
  private void addTrackAndAlbum(DefaultMutableTreeNode node, Track track) {
    boolean bAlbumExists = false;
    AlbumNode currentAlbum = null;
    Enumeration<AlbumNode> e = node.children();
    while (e.hasMoreElements()) {
      AlbumNode an = e.nextElement();
      if (an.getAlbum().equals(track.getAlbum())) {
        bAlbumExists = true;
        currentAlbum = an;
        break;
      }
    }
    if (!bAlbumExists) {
      currentAlbum = new AlbumNode(track.getAlbum());
      node.add(currentAlbum);
    }
    // create track
    if (currentAlbum != null) {
      currentAlbum.add(new TrackNode(track));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jcbSort) {
      UtilGUI.waiting();
      SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
        @Override
        public Void doInBackground() {
          // Set comparator
          Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER,
              Integer.toString(jcbSort.getSelectedIndex()));
          populateTree();
          return null;
        }

        @Override
        public void done() {
          SwingUtilities.updateComponentTreeUI(jtree);
          UtilGUI.stopWaiting();
        }
      };
      sw.execute();
    }
  }

  /**
   * Manages auto-expand.
   */
  @Override
  void expand() {
    // expand all
    for (int i = 0; i < jtree.getRowCount(); i++) {
      boolean bExp = false;
      Object o = jtree.getPathForRow(i).getLastPathComponent();
      if (o instanceof GenreNode) {
        Genre genre = ((GenreNode) o).getGenre();
        bExp = genre.getBooleanValue(Const.XML_EXPANDED);
      } else if (o instanceof ArtistNode) {
        Artist artist = ((ArtistNode) o).getArtist();
        bExp = artist.getBooleanValue(Const.XML_EXPANDED);
      } else if (o instanceof AlbumNode) {
        Album album = ((AlbumNode) o).getAlbum();
        bExp = album.getBooleanValue(Const.XML_EXPANDED);
      } else if (o instanceof YearNode) {
        Year year = ((YearNode) o).getYear();
        bExp = year.getBooleanValue(Const.XML_EXPANDED);
      }
      // now expand row if it should be expanded
      if (bExp) {
        jtree.expandRow(i);
      }
    }
  }

  // needs to be inner class as it accesses various members
  /**
   * .
   */
  class TracksTreeSelectionListener implements TreeSelectionListener {
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
     * .TreeSelectionEvent)
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
      TreePath[] tpSelected = jtree.getSelectionModel().getSelectionPaths();
      if (tpSelected == null) {
        return;
      }
      // get all components recursively
      alSelected.clear();
      selectedRecursively.clear();
      int items = handleSelected(tpSelected);
      StringBuilder sbOut = new StringBuilder().append(items).append(
          Messages.getString("TracksTreeView.31"));
      InformationJPanel.getInstance().setSelection(sbOut.toString());
      // Notify the tree selection change (used by tree/table sync)
      if (!bInternalAction) {
        Properties properties = new Properties();
        properties.put(Const.DETAIL_SELECTION, selectedRecursively);
        properties
            .put(Const.DETAIL_PERSPECTIVE, PerspectiveManager.getCurrentPerspective().getID());
        properties.put(Const.DETAIL_VIEW, getID());
        ObservationManager.notify(new JajukEvent(JajukEvents.TREE_SELECTION_CHANGED, properties));
      }
      // Update preference menu
      pjmTracks.resetUI(alSelected);
    }

    /**
     * Handle selected.
     * 
     * @param tpSelected 
     * 
     * @return the int
     */
    @SuppressWarnings("unchecked")
    private int handleSelected(TreePath[] tpSelected) {
      int items = 0;
      for (TreePath element : tpSelected) {
        Object o = element.getLastPathComponent();
        if (o instanceof TreeRootElement) {
          // collection node
          items = TrackManager.getInstance().getElementCount();
          List<Track> allTracks = TrackManager.getInstance().getTracks();
          selectedRecursively.addAll(allTracks);
          break;
        } else {
          Object userObject = ((DefaultMutableTreeNode) o).getUserObject();
          if (userObject instanceof Item) {
            alSelected.add((Item) userObject);
          }
        }
        // return all child nodes recursively
        Enumeration<DefaultMutableTreeNode> e2 = ((DefaultMutableTreeNode) o)
            .depthFirstEnumeration();
        while (e2.hasMoreElements()) {
          DefaultMutableTreeNode node = e2.nextElement();
          if (node instanceof TrackNode) {
            Track track = ((TrackNode) node).getTrack();
            // don't count the same track several time
            // if user select directory and then tracks
            // inside
            selectedRecursively.add(track);
            items++;
          }
        }
      }
      return items;
    }
  }

  /**
   * Tracks Tree view mouse adapter.
   */
  class TracksMouseAdapter extends JajukMouseAdapter {
    private final JMenuItem jmiShowAlbumDetails;

    /**
     * Instantiates a new tracks mouse adapter.
     * 
     * @param jmiShowAlbumDetails 
     */
    public TracksMouseAdapter(JMenuItem jmiShowAlbumDetails) {
      super();
      this.jmiShowAlbumDetails = jmiShowAlbumDetails;
    }

    /* (non-Javadoc)
     * @see org.jajuk.ui.helpers.JajukMouseAdapter#handleActionSeveralClicks(java.awt.event.MouseEvent)
     */
    @Override
    public void handleActionSeveralClicks(final MouseEvent e) {
      TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
      if (path != null) {
        Object o = path.getLastPathComponent();
        if (o instanceof TrackNode) {
          Track track = ((TrackNode) o).getTrack();
          File file = track.getBestFile(false);
          if (file != null) {
            try {
              QueueModel.push(new StackItem(file, Conf.getBoolean(Const.CONF_STATE_REPEAT),
                  true), Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
            } catch (JajukException je) {
              Log.error(je);
            }
          } else {
            Messages.showErrorMessage(10, track.getName());
          }
        }
      }
    }

    /* (non-Javadoc)
     * @see org.jajuk.ui.helpers.JajukMouseAdapter#handlePopup(java.awt.event.MouseEvent)
     */
    @Override
    public void handlePopup(final MouseEvent e) {
      TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
      if (path == null) {
        return;
      }
      // right click on a selected node set right click behavior
      // identical to konqueror tree:
      // if none or 1 node is selected, a right click on
      // another node select it. if more than 1, we keep selection and
      // display a popup for them
      if (jtree.getSelectionCount() < 2) {
        jtree.getSelectionModel().setSelectionPath(path);
      }
      paths = jtree.getSelectionModel().getSelectionPaths();
      // test mix between types ( not allowed )
      String sClass = paths[0].getLastPathComponent().getClass().toString();
      for (int i = 0; i < paths.length; i++) {
        if (!paths[i].getLastPathComponent().getClass().toString().equals(sClass)) {
          return;
        }
      }
      // display menus according node type
      buildMenu(e);
    }

    /**
     * Builds the menu.
     * 
     * @param e 
     */
    private void buildMenu(final MouseEvent e) {
      if (paths[0].getLastPathComponent() instanceof TrackNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.addSeparator();
        jmenu.add(jmiDelete);
        jmenu.addSeparator();
        jmenu.add(pjmTracks);
        jmenu.add(jmiAddFavorite);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        jmenu.show(jtree, e.getX(), e.getY());
      } else if (paths[0].getLastPathComponent() instanceof AlbumNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.add(jmiPlayShuffle);
        jmenu.add(jmiPlayRepeat);
        jmenu.addSeparator();
        jmenu.add(jmiDelete);
        jmenu.addSeparator();
        jmenu.add(jmiCDDBWizard);
        jmenu.add(jmiReport);
        jmenu.add(jmiShowAlbumDetails);
        jmenu.addSeparator();
        jmenu.add(jmiAddFavorite);
        jmenu.add(pjmTracks);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        jmenu.show(jtree, e.getX(), e.getY());
      } else if (paths[0].getLastPathComponent() instanceof ArtistNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.add(jmiPlayShuffle);
        jmenu.add(jmiPlayRepeat);
        jmenu.addSeparator();
        jmenu.add(jmiDelete);
        jmenu.addSeparator();
        jmenu.add(jmiReport);
        jmenu.addSeparator();
        jmenu.add(pjmTracks);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        jmenu.show(jtree, e.getX(), e.getY());
      } else if (paths[0].getLastPathComponent() instanceof GenreNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.add(jmiPlayShuffle);
        jmenu.add(jmiPlayRepeat);
        jmenu.addSeparator();
        jmenu.add(jmiDelete);
        jmenu.addSeparator();
        jmenu.add(jmiReport);
        jmenu.addSeparator();
        jmenu.add(pjmTracks);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        jmenu.show(jtree, e.getX(), e.getY());
      } else if (paths[0].getLastPathComponent() instanceof YearNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.add(jmiPlayShuffle);
        jmenu.add(jmiPlayRepeat);
        jmenu.addSeparator();
        jmenu.add(pjmTracks);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        jmenu.show(jtree, e.getX(), e.getY());
      } else if (paths[0].getLastPathComponent() instanceof DefaultMutableTreeNode) {
        // Collection menu
        JPopupMenu jmenuCollection = new JPopupMenu();
        // Collection Report
        Action actionReportCollection = ActionManager.getAction(JajukActions.CREATE_REPORT);
        JMenuItem jmiCollectionReport = new JMenuItem(actionReportCollection);
        // Add custom data to this component in order to allow the ReportAction
        // to be able to get it
        jmiCollectionReport.putClientProperty(Const.DETAIL_ORIGIN, COLLECTION_LOGICAL);
        jmenuCollection.add(jmiCollectionReport);
        // Find duplicate files
        Action actionDuplicateFiles = ActionManager.getAction(JajukActions.FIND_DUPLICATE_FILES);
        JMenuItem jmiCollectionDuplicateFiles = new JMenuItem(actionDuplicateFiles);
        jmenuCollection.add(jmiCollectionDuplicateFiles);
        jmenuCollection.show(jtree, e.getX(), e.getY());
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.views.AbstractTreeView#scrollTo(org.jajuk.base.Item)
   */
  @Override
  void scrollTo(Item item) {
    // Set manual change because we force here tree selection and
    // we don't want to force table views to synchronize
    bInternalAction = true;
    try {
      // Clear selection so we only select new synchronized item
      jtree.getSelectionModel().clearSelection();
      // make sure the main element is expanded
      jtree.expandRow(0);
      Track track = null;
      // received item is a file when the event comes from a queue view in the
      // track perspective
      if (item instanceof File) {
        track = ((File) item).getTrack();
      } else {
        track = (Track) item;
      }
      for (int i = 0; i < jtree.getRowCount(); i++) {
        Object o = jtree.getPathForRow(i).getLastPathComponent();
        if (o instanceof AlbumNode) {
          Album testedAlbum = ((AlbumNode) o).getAlbum();
          if (track.getAlbum().equals(testedAlbum)) {
            jtree.expandRow(i);
            jtree.scrollPathToVisible(jtree.getPathForRow(i));
          }
        } else if (o instanceof ArtistNode) {
          Artist testedArtist = ((ArtistNode) o).getArtist();
          if (track.getArtist().equals(testedArtist)) {
            jtree.expandRow(i);
            jtree.scrollPathToVisible(jtree.getPathForRow(i));
          }
        } else if (o instanceof GenreNode) {
          Genre testedGenre = ((GenreNode) o).getGenre();
          if (track.getGenre().equals(testedGenre)) {
            jtree.expandRow(i);
            jtree.scrollPathToVisible(jtree.getPathForRow(i));
          }
        } else if (o instanceof YearNode) {
          Year testedYear = ((YearNode) o).getYear();
          if (track.getYear().equals(testedYear)) {
            jtree.expandRow(i);
            jtree.scrollPathToVisible(jtree.getPathForRow(i));
          }
        } else if (o instanceof TrackNode) {
          Track tested = ((TrackNode) o).getTrack();
          // == here thanks to .intern optimization
          if (tested.getID() == track.getID()) {
            jtree.expandRow(i);
            jtree.scrollPathToVisible(jtree.getPathForRow(i));
            jtree.getSelectionModel().addSelectionPath(jtree.getPathForRow(i));
          }
        }
      }
    } finally {
      bInternalAction = false;
    }
  }
}

/**
 * Genre node
 */
class GenreNode extends DefaultMutableTreeNode {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param track
   */
  public GenreNode(Genre track) {
    super(track);
  }

  /**
   * return a string representation of this track node
   */
  @Override
  public String toString() {
    return getGenre().getName2();
  }

  /**
   * @return Returns the track.
   */
  public Genre getGenre() {
    return (Genre) super.getUserObject();
  }
}

/**
 * Artist node
 */
class ArtistNode extends DefaultMutableTreeNode {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param artist
   */
  public ArtistNode(Artist artist) {
    super(artist);
  }

  /**
   * return a string representation of this artist node
   */
  @Override
  public String toString() {
    return getArtist().getName2();
  }

  /**
   * @return Returns the artist.
   */
  public Artist getArtist() {
    return (Artist) super.getUserObject();
  }
}

/**
 * Year node
 */
class YearNode extends DefaultMutableTreeNode {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param artist
   */
  public YearNode(Year year) {
    super(year);
  }

  /**
   * return a string representation of this node
   */
  @Override
  public String toString() {
    if (getYear().getValue() > 0) {
      return getYear().getName();
    } else {
      return Messages.getString("unknown_year");
    }
  }

  /**
   * @return Returns the year.
   */
  public Year getYear() {
    return (Year) super.getUserObject();
  }
}

/**
 * Album node
 */
class AlbumNode extends DefaultMutableTreeNode {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param album
   */
  public AlbumNode(Album album) {
    super(album);
  }

  /**
   * return a string representation of this album node
   */
  @Override
  public String toString() {
    return getAlbum().getName2();
  }

  /**
   * @return Returns the album.
   */
  public Album getAlbum() {
    return (Album) super.getUserObject();
  }
}

/**
 * Track node
 */
class TrackNode extends DefaultMutableTreeNode {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param track
   */
  public TrackNode(Track track) {
    super(track);
  }

  /**
   * return a string representation of this track node
   */
  @Override
  public String toString() {
    return getTrack().getName();
  }

  /**
   * @return Returns the track.
   */
  public Track getTrack() {
    return (Track) super.getUserObject();
  }
}

/**
 * 
 * Discovery date filter tree node
 */
class DiscoveryDateNode extends DefaultMutableTreeNode {
  /**
   * @param string
   */
  public DiscoveryDateNode(String string) {
    super(string);
  }

  /**
   * We have to override this method for drag and drop
   * whish waits for an item. A period is not an item. 
   * 
   * @see DefaultMutableTreeNode.getUserObject() 
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getUserObject() {
    List<Item> out = new ArrayList<Item>(10);
    Enumeration<DefaultMutableTreeNode> childrens = children();
    while (childrens.hasMoreElements()) {
      DefaultMutableTreeNode node = childrens.nextElement();
      out.add((Item) node.getUserObject());
    }
    return out;
  }

  private static final long serialVersionUID = 7123195836014138019L;
}

class TracksTreeCellRenderer extends SubstanceDefaultTreeCellRenderer {
  private static final long serialVersionUID = 1L;

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
    if (value instanceof GenreNode) {
      setIcon(IconLoader.getIcon(JajukIcons.GENRE));
    } else if (value instanceof ArtistNode) {
      setIcon(IconLoader.getIcon(JajukIcons.ARTIST));
    } else if (value instanceof YearNode) {
      setIcon(IconLoader.getIcon(JajukIcons.YEAR));
    } else if (value instanceof AlbumNode) {
      setIcon(IconLoader.getIcon(JajukIcons.ALBUM));
    } else if (value instanceof TrackNode) {
      setIcon(IconLoader.getIcon(JajukIcons.TRACK));
      // Discovery date filter
    } else if (value instanceof DiscoveryDateNode) {
      setIcon(IconLoader.getIcon(JajukIcons.DISCOVERY_DATE));
      // collection node
    } else {
      setIcon(IconLoader.getIcon(JajukIcons.LIST));
    }
    return this;
  }
}

class TracksTreeExpansionListener implements TreeExpansionListener {
  @Override
  public void treeCollapsed(TreeExpansionEvent event) {
    Object o = event.getPath().getLastPathComponent();
    if (o instanceof GenreNode) {
      Genre genre = ((GenreNode) o).getGenre();
      genre.removeProperty(Const.XML_EXPANDED);
    } else if (o instanceof ArtistNode) {
      Artist artist = ((ArtistNode) o).getArtist();
      artist.removeProperty(Const.XML_EXPANDED);
    } else if (o instanceof AlbumNode) {
      Album album = ((AlbumNode) o).getAlbum();
      album.removeProperty(Const.XML_EXPANDED);
    } else if (o instanceof YearNode) {
      Year year = ((YearNode) o).getYear();
      year.removeProperty(Const.XML_EXPANDED);
    }
  }

  @Override
  public void treeExpanded(TreeExpansionEvent event) {
    Object o = event.getPath().getLastPathComponent();
    if (o instanceof GenreNode) {
      Genre genre = ((GenreNode) o).getGenre();
      genre.setProperty(Const.XML_EXPANDED, true);
    } else if (o instanceof ArtistNode) {
      Artist artist = ((ArtistNode) o).getArtist();
      artist.setProperty(Const.XML_EXPANDED, true);
    } else if (o instanceof AlbumNode) {
      Album album = ((AlbumNode) o).getAlbum();
      album.setProperty(Const.XML_EXPANDED, true);
    } else if (o instanceof YearNode) {
      Year year = ((YearNode) o).getYear();
      year.setProperty(Const.XML_EXPANDED, true);
    }
  }
}