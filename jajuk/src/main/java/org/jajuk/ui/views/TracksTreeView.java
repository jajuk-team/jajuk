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

package org.jajuk.ui.views;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

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
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.TransferableTreeNode;
import org.jajuk.ui.helpers.TreeRootElement;
import org.jajuk.ui.helpers.TreeTransferHandler;
import org.jajuk.ui.helpers.FontManager.JajukFont;
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

  /** DOCUMENT_ME. */
  private JComboBox jcbSort;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
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
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.DEVICE_MOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_UNMOUNT);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    return eventSubjectSet;
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
    jcbSort.addItem(Messages.getString("Property_style")); // sort by
    // Genre/Artist/Album
    jcbSort.addItem(Messages.getString("Property_author")); // sort by
    // Artist/Album
    jcbSort.addItem(Messages.getString("Property_album")); // sort by Album
    jcbSort.addItem(Messages.getString("Property_year")); // sort by Year
    jcbSort.addItem(Messages.getString("TracksTreeView.35")); // sort by
    // Discovery Date
    jcbSort.addItem(Messages.getString("Property_rate")); // sort by rate
    jcbSort.addItem(Messages.getString("Property_hits")); // sort by hits
    jcbSort.setSelectedIndex(Conf.getInt(Const.CONF_LOGICAL_TREE_SORT_ORDER));
    jcbSort.setActionCommand(JajukEvents.LOGICAL_TREE_SORT.toString());
    jcbSort.addActionListener(this);

    // Album details
    final JMenuItem jmiShowAlbumDetails = new JMenuItem(ActionManager
        .getAction(JajukActions.SHOW_ALBUM_DETAILS));
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
     * ensure the order where listeners will treat them so don't count in the
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
    new TreeTransferHandler(jtree, DnDConstants.ACTION_COPY_OR_MOVE, true);
    jspTree = new JScrollPane(jtree);
    jspTree.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    setLayout(new MigLayout("ins 5", "[][grow]", "[][grow]"));
    add(jlSort, "left,gapx 5::");
    add(jcbSort, "grow,wrap");
    add(jspTree, "grow,span");
    expand();
  }

  /**
   * Fill the tree.
   */

  @Override
  public synchronized void populateTree() {
    // delete previous tree
    top.removeAllChildren();

    // see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472844 for a small memory leak that is caused here...
    if(jtree != null && jtree.getModel() != null) {
      ((DefaultTreeModel)(jtree.getModel())).reload();
    }
    
    TrackComparatorType comparatorType = TrackComparatorType.values()[Conf
        .getInt(Const.CONF_LOGICAL_TREE_SORT_ORDER)];
    if (comparatorType == TrackComparatorType.STYLE_AUTHOR_ALBUM) {
      populateTreeByStyle();
    }// Author/album
    else if (comparatorType == TrackComparatorType.AUTHOR_ALBUM) {
      populateTreeByAuthor();
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
  }

  /**
   * Fill the tree by style.
   */
  @SuppressWarnings("unchecked")
  public void populateTreeByStyle() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        StyleNode styleNode = null;
        Style style = track.getStyle();
        AuthorNode authorNode = null;
        Author author = track.getAuthor();
        AlbumNode albumNode = null;
        Album album = track.getAlbum();

        // create style
        Enumeration e = top.children();
        boolean b = false;
        while (e.hasMoreElements()) { // check the style doesn't
          // already exist
          StyleNode sn = (StyleNode) e.nextElement();
          if (sn.getStyle().equals(style)) {
            b = true;
            styleNode = sn;
            break;
          }
        }
        if (!b) {
          styleNode = new StyleNode(style);
          top.add(styleNode);
        }
        // create author
        if (styleNode != null) {
          e = styleNode.children();
        } else {
          continue;
        }
        b = false;
        while (e.hasMoreElements()) { // check if the author doesn't
          // already exist
          AuthorNode an = (AuthorNode) e.nextElement();
          if (an.getAuthor().equals(author)) {
            b = true;
            authorNode = an;
            break;
          }
        }
        if (!b) {
          authorNode = new AuthorNode(author);
          styleNode.add(authorNode);
        }
        // create album
        if (authorNode != null) {
          e = authorNode.children();
        } else {
          continue;
        }
        b = false;
        while (e.hasMoreElements()) {
          AlbumNode an = (AlbumNode) e.nextElement();
          if (an.getAlbum().equals(album)) {
            b = true;
            albumNode = an;
            break;
          }
        }
        if (!b) {
          albumNode = new AlbumNode(album);
          authorNode.add(albumNode);
        }
        // create track
        assert albumNode != null;
        albumNode.add(new TrackNode(track));
      }
    }
  }

  /**
   * Fill the tree by author.
   */
  @SuppressWarnings("unchecked")
  public void populateTreeByAuthor() {
    List<Track> tracks = TrackManager.getInstance().getTracks();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        AuthorNode authorNode = null;
        Author author = track.getAuthor();
        AlbumNode albumNode = null;
        Album album = track.getAlbum();

        // create author
        Enumeration e = top.children();
        boolean b = false;
        while (e.hasMoreElements()) { // check if the author doesn't
          // already exist
          AuthorNode an = (AuthorNode) e.nextElement();
          if (an.getAuthor().equals(author)) {
            b = true;
            authorNode = an;
            break;
          }
        }
        if (!b) {
          authorNode = new AuthorNode(author);
          top.add(authorNode);
        }
        // create album
        if (authorNode != null) {
          e = authorNode.children();
        } else {
          continue;
        }
        b = false;
        while (e.hasMoreElements()) { // check if the album doesn't
          // already exist
          AlbumNode an = (AlbumNode) e.nextElement();
          if (an.getAlbum().equals(album)) {
            b = true;
            albumNode = an;
            break;
          }
        }
        if (!b) {
          albumNode = new AlbumNode(album);
          authorNode.add(albumNode);
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
        Enumeration e = top.children();
        boolean b = false;
        // check if the author doesn't already exist
        while (e.hasMoreElements()) {
          YearNode yn = (YearNode) e.nextElement();
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
        // create album
        if (yearNode != null) {
          e = yearNode.children();
        } else {
          continue;
        }
        b = false;
        while (e.hasMoreElements()) { // check if the album doesn't
          // already exist
          AlbumNode an = (AlbumNode) e.nextElement();
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
    DefaultMutableTreeNode nodeWeekly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.36"));
    DefaultMutableTreeNode nodeMontly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.37"));
    DefaultMutableTreeNode nodeThreeMontly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.44"));
    DefaultMutableTreeNode nodeSixMontly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.38"));
    DefaultMutableTreeNode nodeYearly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.40"));
    DefaultMutableTreeNode nodeTwoYearly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.41"));
    DefaultMutableTreeNode nodeFiveYearly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.42"));
    DefaultMutableTreeNode nodeTenYearly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.43"));
    DefaultMutableTreeNode nodeOlder = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.39"));
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
   *          DOCUMENT_ME
   * @param track
   *          DOCUMENT_ME
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

  /**
   * Create a Misc node.
   */
  @SuppressWarnings("unchecked")
  public void cleanTree() {
    AuthorNode amisc = new AuthorNode(AuthorManager.getInstance().registerAuthor("Misc"));

    DefaultMutableTreeNode authorNode = null;
    DefaultMutableTreeNode albumNode = new DefaultMutableTreeNode();
    AlbumNode misc;
    Enumeration eAuthor = top.children();

    while (eAuthor.hasMoreElements()) {
      authorNode = (AuthorNode) eAuthor.nextElement();
      misc = new AlbumNode(AlbumManager.getInstance().registerAlbum("Misc", "Misc", 0));

      for (Enumeration<AlbumNode> eAlbum = authorNode.children(); eAlbum.hasMoreElements();) {
        albumNode = eAlbum.nextElement();
        if (albumNode.getChildCount() < MIN_TRACKS_NUMBER) {
          while (albumNode.getChildCount() > 0) {
            // FIXME: why do we do an empty busy loop here?
          }
        }
      }
      authorNode.remove(albumNode);
      if (misc.getChildCount() > 0) {
        authorNode.add(misc);
      }
    }
    if (amisc.getChildCount() > 0) {
      top.add(amisc);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jcbSort) {
      UtilGUI.waiting();
      SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
        @Override
        public Void doInBackground() {
          // Set comparator
          Conf.setProperty(Const.CONF_LOGICAL_TREE_SORT_ORDER, Integer.toString(jcbSort
              .getSelectedIndex()));
          populateTree();
          return null;
        }

        @Override
        public void done() {
          try {
            get();
          } catch (InterruptedException e1) {
            Log.error(e1);
          } catch (ExecutionException e1) {
            Log.error(e1);
          }
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
      if (o instanceof StyleNode) {
        Style style = ((StyleNode) o).getStyle();
        bExp = style.getBooleanValue(Const.XML_EXPANDED);
      } else if (o instanceof AuthorNode) {
        Author author = ((AuthorNode) o).getAuthor();
        bExp = author.getBooleanValue(Const.XML_EXPANDED);
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
   * DOCUMENT_ME.
   */
  class TracksTreeSelectionListener implements TreeSelectionListener {

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
     */
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
      if (Conf.getBoolean(Const.CONF_OPTIONS_SYNC_TABLE_TREE)) {
        // if table is synchronized with tree, notify the
        // selection change
        Properties properties = new Properties();
        properties.put(Const.DETAIL_SELECTION, selectedRecursively);
        properties.put(Const.DETAIL_ORIGIN, PerspectiveManager.getCurrentPerspective().getID());
        ObservationManager.notify(new JajukEvent(JajukEvents.SYNC_TREE_TABLE, properties));
      }
      // Update preference menu
      pjmTracks.resetUI(alSelected);
    }

    /**
     * Handle selected.
     * 
     * @param tpSelected
     *          DOCUMENT_ME
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
        } else if (o instanceof TransferableTreeNode) {
          // this is a standard node except "by date"
          // discovery nodes
          alSelected.add((Item) ((TransferableTreeNode) o).getUserObject());
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
   * Tracks Tree view mouse adapter
   */
  class TracksMouseAdapter extends JajukMouseAdapter {

    /** DOCUMENT_ME. */
    private final JMenuItem jmiShowAlbumDetails;

    /**
     * Instantiates a new tracks mouse adapter.
     * 
     * @param jmiShowAlbumDetails
     *          DOCUMENT_ME
     */
    public TracksMouseAdapter(JMenuItem jmiShowAlbumDetails) {
      super();
      this.jmiShowAlbumDetails = jmiShowAlbumDetails;
    }

    @Override
    public void handleActionSeveralClicks(final MouseEvent e) {
      TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
      if (path != null) {
        Object o = path.getLastPathComponent();
        if (o instanceof TrackNode) {
          Track track = ((TrackNode) o).getTrack();
          File file = track.getPlayeableFile(false);
          if (file != null) {
            try {
              QueueModel.push(new StackItem(file, Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL),
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
     *          DOCUMENT_ME
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
      } else if (paths[0].getLastPathComponent() instanceof AuthorNode) {
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
      } else if (paths[0].getLastPathComponent() instanceof StyleNode) {
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
}

/**
 * Style node
 */
class StyleNode extends TransferableTreeNode {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param track
   */
  public StyleNode(Style track) {
    super(track);
  }

  /**
   * return a string representation of this track node
   */
  @Override
  public String toString() {
    return getStyle().getName2();
  }

  /**
   * @return Returns the track.
   */
  public Style getStyle() {
    return (Style) super.getUserObject();
  }
}

/**
 * Author node
 */
class AuthorNode extends TransferableTreeNode {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param author
   */
  public AuthorNode(Author author) {
    super(author);
  }

  /**
   * return a string representation of this author node
   */
  @Override
  public String toString() {
    return getAuthor().getName2();
  }

  /**
   * @return Returns the author.
   */
  public Author getAuthor() {
    return (Author) super.getUserObject();
  }
}

/**
 * Year node
 */
class YearNode extends TransferableTreeNode {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param author
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
class AlbumNode extends TransferableTreeNode {

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
class TrackNode extends TransferableTreeNode {

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

  private static final long serialVersionUID = 7123195836014138019L;

}

class TracksTreeCellRenderer extends SubstanceDefaultTreeCellRenderer {
  private static final long serialVersionUID = 1L;

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
      boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));

    if (value instanceof StyleNode) {
      setIcon(IconLoader.getIcon(JajukIcons.STYLE));
    } else if (value instanceof AuthorNode) {
      setIcon(IconLoader.getIcon(JajukIcons.AUTHOR));
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
  public void treeCollapsed(TreeExpansionEvent event) {
    Object o = event.getPath().getLastPathComponent();
    if (o instanceof StyleNode) {
      Style style = ((StyleNode) o).getStyle();
      style.removeProperty(Const.XML_EXPANDED);
    } else if (o instanceof AuthorNode) {
      Author author = ((AuthorNode) o).getAuthor();
      author.removeProperty(Const.XML_EXPANDED);
    } else if (o instanceof AlbumNode) {
      Album album = ((AlbumNode) o).getAlbum();
      album.removeProperty(Const.XML_EXPANDED);
    } else if (o instanceof YearNode) {
      Year year = ((YearNode) o).getYear();
      year.removeProperty(Const.XML_EXPANDED);
    }
  }

  public void treeExpanded(TreeExpansionEvent event) {
    Object o = event.getPath().getLastPathComponent();
    if (o instanceof StyleNode) {
      Style style = ((StyleNode) o).getStyle();
      style.setProperty(Const.XML_EXPANDED, true);
    } else if (o instanceof AuthorNode) {
      Author author = ((AuthorNode) o).getAuthor();
      author.setProperty(Const.XML_EXPANDED, true);
    } else if (o instanceof AlbumNode) {
      Album album = ((AlbumNode) o).getAlbum();
      album.setProperty(Const.XML_EXPANDED, true);
    } else if (o instanceof YearNode) {
      Year year = ((YearNode) o).getYear();
      year.setProperty(Const.XML_EXPANDED, true);
    }
  }
}