/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import ext.SwingWorker;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Bookmarks;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.StackItem;
import org.jajuk.base.Style;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.TransferableTreeNode;
import org.jajuk.ui.helpers.TreeRootElement;
import org.jajuk.ui.helpers.TreeTransferHandler;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.wizard.CDDBWizard;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jvnet.substance.SubstanceDefaultTreeCellRenderer;

/**
 * Logical tree view
 */
public class TracksTreeView extends AbstractTreeView implements ActionListener, Observer {

  private static final long serialVersionUID = 1L;

  /** Track selection */
  ArrayList<Track> alTracks;

  JPopupMenu jmenuCollection;

  ButtonGroup btCollection;

  JRadioButtonMenuItem jmiCollectionStyle;

  JRadioButtonMenuItem jmiCollectionAuthor;

  JRadioButtonMenuItem jmiCollectionAlbum;

  JRadioButtonMenuItem jmiCollectionProperties;

  JMenuItem jmiCollectionReport;

  JMenuItem jmiCollectionDuplicateFiles;

  JLabel jlSort;

  JComboBox jcbSort;

  JPopupMenu jmenuStyle;

  JMenuItem jmiStylePlay;

  JMenuItem jmiStylePush;

  JMenuItem jmiStylePlayShuffle;

  JMenuItem jmiStylePlayRepeat;

  JMenuItem jmiStyleDelete;

  JMenuItem jmiStyleAddFavorite;

  JMenuItem jmiStyleReport;

  JMenuItem jmiStyleProperties;

  JPopupMenu jmenuAuthor;

  JMenuItem jmiAuthorPlay;

  JMenuItem jmiAuthorPush;

  JMenuItem jmiAuthorPlayShuffle;

  JMenuItem jmiAuthorPlayRepeat;

  JMenuItem jmiAuthorDelete;

  JMenuItem jmiAuthorAddFavorite;

  JMenuItem jmiAuthorReport;

  JMenuItem jmiAuthorProperties;

  JPopupMenu jmenuAlbum;

  JMenuItem jmiAlbumPlay;

  JMenuItem jmiAlbumPush;

  JMenuItem jmiAlbumPlayShuffle;

  JMenuItem jmiAlbumPlayRepeat;

  JMenuItem jmiAlbumDelete;

  JMenuItem jmiAlbumAddFavorite;

  JMenuItem jmiAlbumReport;

  JMenuItem jmiAlbumCDDBWizard;

  JMenuItem jmiAlbumProperties;

  JPopupMenu jmenuYear;

  JMenuItem jmiYearPlay;

  JMenuItem jmiYearPush;

  JMenuItem jmiYearPlayShuffle;

  JMenuItem jmiYearPlayRepeat;

  JMenuItem jmiYearAddFavorite;

  JMenuItem jmiYearProperties;

  JPopupMenu jmenuTrack;

  JMenuItem jmiTrackPlay;

  JMenuItem jmiTrackPush;

  JMenuItem jmiTrackDelete;

  JMenuItem jmiTrackAddFavorite;

  JMenuItem jmiTrackProperties;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("TracksTreeView.0");
  }

  /** Constructor */
  public TracksTreeView() {
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_DEVICE_MOUNT);
    eventSubjectSet.add(EventSubject.EVENT_DEVICE_UNMOUNT);
    eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    // **Menu items**

    // ComboBox sort
    double[][] dSizeSort = { { 5, TableLayout.PREFERRED, 5, TableLayout.FILL },
        { TableLayout.PREFERRED } };
    JPanel jpsort = new JPanel();
    jpsort.setLayout(new TableLayout(dSizeSort));
    jlSort = new JLabel(Messages.getString("Sort"));
    jcbSort = new JComboBox();
    jcbSort.addItem(Messages.getString("Property_style"));
    jcbSort.addItem(Messages.getString("Property_author"));
    jcbSort.addItem(Messages.getString("Property_album"));
    jcbSort.addItem(Messages.getString("Property_year"));
    jcbSort.addItem(Messages.getString("TracksTreeView.35"));
    jcbSort.setSelectedIndex(ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER));
    jcbSort.setActionCommand(EventSubject.EVENT_LOGICAL_TREE_SORT.toString());
    jcbSort.addActionListener(this);
    jpsort.add(jlSort, "1,0");
    jpsort.add(jcbSort, "3,0");

    // Collection menu
    jmenuCollection = new JPopupMenu();

    Action actionReportCollection = ActionManager.getAction(JajukAction.CREATE_REPORT);
    jmiCollectionReport = new JMenuItem(actionReportCollection);
    // Add custom data to this component in order to allow the ReportAction
    // to be able to get it
    jmiCollectionReport.putClientProperty(DETAIL_ORIGIN, COLLECTION_LOGICAL);
    jmenuCollection.add(jmiCollectionReport);

    Action actionDuplicateFiles = ActionManager.getAction(JajukAction.FIND_DUPLICATE_FILES);
    jmiCollectionDuplicateFiles = new JMenuItem(actionDuplicateFiles);
    jmenuCollection.add(jmiCollectionDuplicateFiles);

    // Style menu
    jmenuStyle = new JPopupMenu();
    jmiStylePlay = new JMenuItem(Messages.getString("TracksTreeView.1"), IconLoader.ICON_PLAY_16x16);
    jmiStylePlay.addActionListener(this);
    jmiStylePush = new JMenuItem(Messages.getString("TracksTreeView.2"), IconLoader.ICON_PUSH);
    jmiStylePush.addActionListener(this);
    jmiStylePlayShuffle = new JMenuItem(Messages.getString("TracksTreeView.3"),
        IconLoader.ICON_SHUFFLE);
    jmiStylePlayShuffle.addActionListener(this);
    jmiStylePlayRepeat = new JMenuItem(Messages.getString("TracksTreeView.4"),
        IconLoader.ICON_REPEAT);
    jmiStylePlayRepeat.addActionListener(this);
    jmiStyleDelete = new JMenuItem(Messages.getString("TracksTreeView.5"));
    jmiStyleDelete.addActionListener(this);
    jmiStyleAddFavorite = new JMenuItem(Messages.getString("TracksTreeView.32"),
        IconLoader.ICON_BOOKMARK_FOLDERS);
    jmiStyleAddFavorite.addActionListener(this);
    Action actionReportStyle = ActionManager.getAction(JajukAction.CREATE_REPORT);
    jmiStyleReport = new JMenuItem(actionReportStyle);
    // Add custom data to this component in order to allow the ReportAction
    // to be able to get it
    jmiStyleReport.putClientProperty(DETAIL_ORIGIN, XML_STYLE);
    jmiStyleReport.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiStyleProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiStyleProperties.putClientProperty(DETAIL_SELECTION, alSelected);
    jmenuStyle.add(jmiStylePlay);
    jmenuStyle.add(jmiStylePush);
    jmenuStyle.add(jmiStylePlayShuffle);
    jmenuStyle.add(jmiStylePlayRepeat);
    jmenuStyle.add(jmiStyleAddFavorite);
    jmenuStyle.add(jmiStyleReport);
    jmenuStyle.add(jmiStyleProperties);

    // Author menu
    jmenuAuthor = new JPopupMenu();
    jmiAuthorPlay = new JMenuItem(Messages.getString("TracksTreeView.8"),
        IconLoader.ICON_PLAY_16x16);
    jmiAuthorPlay.addActionListener(this);
    jmiAuthorPush = new JMenuItem(Messages.getString("TracksTreeView.9"), IconLoader.ICON_PUSH);
    jmiAuthorPush.addActionListener(this);
    jmiAuthorPlayShuffle = new JMenuItem(Messages.getString("TracksTreeView.10"),
        IconLoader.ICON_SHUFFLE);
    jmiAuthorPlayShuffle.addActionListener(this);
    jmiAuthorPlayRepeat = new JMenuItem(Messages.getString("TracksTreeView.11"),
        IconLoader.ICON_REPEAT);
    jmiAuthorPlayRepeat.addActionListener(this);
    jmiAuthorDelete = new JMenuItem(Messages.getString("TracksTreeView.12"));
    jmiAuthorDelete.addActionListener(this);
    jmiAuthorAddFavorite = new JMenuItem(Messages.getString("TracksTreeView.32"),
        IconLoader.ICON_BOOKMARK_FOLDERS);
    jmiAuthorAddFavorite.addActionListener(this);
    Action actionReportAuthor = ActionManager.getAction(JajukAction.CREATE_REPORT);
    jmiAuthorReport = new JMenuItem(actionReportAuthor);
    // Add custom data to this component in order to allow the ReportAction
    // to be able to get it
    jmiAuthorReport.putClientProperty(DETAIL_ORIGIN, XML_AUTHOR);
    jmiAuthorReport.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiAuthorProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiAuthorProperties.putClientProperty(DETAIL_SELECTION, alSelected);
    jmenuAuthor.add(jmiAuthorPlay);
    jmenuAuthor.add(jmiAuthorPush);
    jmenuAuthor.add(jmiAuthorPlayShuffle);
    jmenuAuthor.add(jmiAuthorPlayRepeat);
    jmenuAuthor.add(jmiAuthorAddFavorite);
    jmenuAuthor.add(jmiAuthorReport);
    jmenuAuthor.add(jmiAuthorProperties);

    // Album menu
    jmenuAlbum = new JPopupMenu();
    jmiAlbumPlay = new JMenuItem(Messages.getString("TracksTreeView.15"),
        IconLoader.ICON_PLAY_16x16);
    jmiAlbumPlay.addActionListener(this);
    jmiAlbumPush = new JMenuItem(Messages.getString("TracksTreeView.16"), IconLoader.ICON_PUSH);
    jmiAlbumPush.addActionListener(this);
    jmiAlbumPlayShuffle = new JMenuItem(Messages.getString("TracksTreeView.17"),
        IconLoader.ICON_SHUFFLE);
    jmiAlbumPlayShuffle.addActionListener(this);
    jmiAlbumPlayRepeat = new JMenuItem(Messages.getString("TracksTreeView.18"),
        IconLoader.ICON_REPEAT);
    jmiAlbumPlayRepeat.addActionListener(this);
    jmiAlbumDelete = new JMenuItem(Messages.getString("TracksTreeView.19"));
    jmiAlbumDelete.addActionListener(this);
    jmiAlbumAddFavorite = new JMenuItem(Messages.getString("TracksTreeView.32"),
        IconLoader.ICON_BOOKMARK_FOLDERS);
    jmiAlbumAddFavorite.addActionListener(this);
    Action actionReportAlbum = ActionManager.getAction(JajukAction.CREATE_REPORT);
    jmiAlbumReport = new JMenuItem(actionReportAlbum);
    // Add custom data to this component in order to allow the ReportAction
    // to be able to get it
    jmiAlbumReport.putClientProperty(DETAIL_ORIGIN, XML_ALBUM);
    jmiAlbumReport.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiAlbumCDDBWizard = new JMenuItem(Messages.getString("TracksTreeView.34"),
        IconLoader.ICON_CDDB);
    jmiAlbumCDDBWizard.addActionListener(this);
    jmiAlbumProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiAlbumProperties.putClientProperty(DETAIL_SELECTION, alSelected);
    jmenuAlbum.add(jmiAlbumPlay);
    jmenuAlbum.add(jmiAlbumPush);
    jmenuAlbum.add(jmiAlbumPlayShuffle);
    jmenuAlbum.add(jmiAlbumPlayRepeat);
    jmenuAlbum.add(jmiAlbumAddFavorite);
    jmenuAlbum.add(jmiAlbumCDDBWizard);
    jmenuAlbum.add(jmiAlbumReport);
    jmenuAlbum.add(jmiAlbumProperties);

    // Year menu
    jmenuYear = new JPopupMenu();
    jmiYearPlay = new JMenuItem(Messages.getString("TracksTreeView.15"), IconLoader.ICON_PLAY_16x16);
    jmiYearPlay.addActionListener(this);
    jmiYearPush = new JMenuItem(Messages.getString("TracksTreeView.16"), IconLoader.ICON_PUSH);
    jmiYearPush.addActionListener(this);
    jmiYearPlayShuffle = new JMenuItem(Messages.getString("TracksTreeView.17"),
        IconLoader.ICON_SHUFFLE);
    jmiYearPlayShuffle.addActionListener(this);
    jmiYearPlayRepeat = new JMenuItem(Messages.getString("TracksTreeView.18"),
        IconLoader.ICON_REPEAT);
    jmiYearPlayRepeat.addActionListener(this);
    jmiYearAddFavorite = new JMenuItem(Messages.getString("TracksTreeView.32"),
        IconLoader.ICON_BOOKMARK_FOLDERS);
    jmiYearAddFavorite.addActionListener(this);
    jmiYearProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiYearProperties.putClientProperty(DETAIL_SELECTION, alSelected);
    jmenuYear.add(jmiYearPlay);
    jmenuYear.add(jmiYearPush);
    jmenuYear.add(jmiYearPlayShuffle);
    jmenuYear.add(jmiYearPlayRepeat);
    jmenuYear.add(jmiYearAddFavorite);
    jmenuYear.add(jmiYearProperties);

    // Track menu
    jmenuTrack = new JPopupMenu();
    jmiTrackPlay = new JMenuItem(Messages.getString("TracksTreeView.22"),
        IconLoader.ICON_PLAY_16x16);
    jmiTrackPlay.addActionListener(this);
    jmiTrackPush = new JMenuItem(Messages.getString("TracksTreeView.23"), IconLoader.ICON_PUSH);
    jmiTrackPush.addActionListener(this);
    jmiTrackDelete = new JMenuItem(Messages.getString("TracksTreeView.24"));
    jmiTrackDelete.addActionListener(this);
    jmiTrackAddFavorite = new JMenuItem(Messages.getString("TracksTreeView.32"),
        IconLoader.ICON_BOOKMARK_FOLDERS);
    jmiTrackAddFavorite.addActionListener(this);
    jmiTrackProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiTrackProperties.putClientProperty(DETAIL_SELECTION, alSelected);
    jmenuTrack.add(jmiTrackPlay);
    jmenuTrack.add(jmiTrackPush);
    jmenuTrack.add(jmiTrackAddFavorite);
    jmenuTrack.add(jmiTrackProperties);

    top = new TreeRootElement(Messages.getString("TracksTreeView.27"));

    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // populate the tree
    populateTree();
    // create tree
    createTree();
    jtree.setCellRenderer(new SubstanceDefaultTreeCellRenderer() {
      private static final long serialVersionUID = 1L;

      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
          boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
        if (value instanceof StyleNode) {
          setIcon(IconLoader.ICON_STYLE);
        } else if (value instanceof AuthorNode) {
          setIcon(IconLoader.ICON_AUTHOR);
        } else if (value instanceof YearNode) {
          setIcon(IconLoader.ICON_YEAR);
        } else if (value instanceof AlbumNode) {
          setIcon(IconLoader.ICON_ALBUM);
        } else if (value instanceof TrackNode) {
          setIcon(IconLoader.ICON_TRACK);
          // Discovery date filter
        } else if (value instanceof DiscoveryDateNode) {
          setIcon(IconLoader.ICON_DISCOVERY_DATE);
          // collection node
        } else {
          setIcon(IconLoader.ICON_LIST);
        }
        return this;
      }
    });
    DefaultTreeModel treeModel = new DefaultTreeModel(top);
    // Tree model listener to detect changes in the tree structure
    treeModel.addTreeModelListener(new TreeModelListener() {

      public void treeNodesChanged(TreeModelEvent e) {
        DefaultMutableTreeNode node;
        node = (DefaultMutableTreeNode) (e.getTreePath().getLastPathComponent());
        try {
          int index = e.getChildIndices()[0];
          node = (DefaultMutableTreeNode) (node.getChildAt(index));
        } catch (NullPointerException exc) {
        }
      }

      public void treeNodesInserted(TreeModelEvent e) {
      }

      public void treeNodesRemoved(TreeModelEvent e) {
      }

      public void treeStructureChanged(TreeModelEvent e) {
      }
    });

    // Tree selection listener to detect a selection
    jtree.addTreeSelectionListener(new TreeSelectionListener() {
      @SuppressWarnings("unchecked")
      public void valueChanged(TreeSelectionEvent e) {
        // Avoid concurrency with the mouse listener
        synchronized (lock) {
          TreePath[] tpSelected = jtree.getSelectionModel().getSelectionPaths();
          if (tpSelected == null) {
            return;
          }
          int items = 0;
          // get all components recursively
          alSelected.clear();
          selectedRecursively.clear();
          for (int i = 0; i < tpSelected.length; i++) {
            Object o = tpSelected[i].getLastPathComponent();
            if (o instanceof TreeRootElement) {
              // collection node
              items = TrackManager.getInstance().getElementCount();
              alSelected.addAll(TrackManager.getInstance().getTracks());
              break;
            } else if (o instanceof TransferableTreeNode) {
              // this is a standard node except "by date"
              // discovery
              // nodes
              alSelected.add((Item) ((TransferableTreeNode) o).getData());
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
          StringBuilder sbOut = new StringBuilder().append(items).append(
              Messages.getString("TracksTreeView.31"));
          InformationJPanel.getInstance().setSelection(sbOut.toString());
          if (ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE)) {
            // if table is synchronized with tree, notify the
            // selection change
            Properties properties = new Properties();
            properties.put(DETAIL_SELECTION, selectedRecursively);
            properties.put(DETAIL_ORIGIN, PerspectiveManager.getCurrentPerspective().getID());
            ObservationManager.notify(new Event(EventSubject.EVENT_SYNC_TREE_TABLE, properties));
          }
        }
      }
    });
    // Listen for double click
    MouseListener ml = new MouseAdapter() {

      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          handlePopup(e);
          // Left click
        } else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
          // Avoid concurrency with the selection listener
          synchronized (lock) {
            TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
            if (path == null) {
              return;
            }
            if (e.getClickCount() == 2) {
              Object o = path.getLastPathComponent();
              if (o instanceof TrackNode) {
                Track track = ((TrackNode) o).getTrack();
                File file = track.getPlayeableFile(false);
                if (file != null) {
                  try {
                    FIFO.getInstance().push(
                        new StackItem(file, ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
                            true),
                        ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
                  } catch (JajukException je) {
                    Log.error(je);
                  }
                } else {
                  Messages.showErrorMessage(10, track.getName()); //$NON-NLS-1$
                }
              }
            }

          }
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          handlePopup(e);
        }
      }

      @SuppressWarnings("unchecked")
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
        alTracks = new ArrayList<Track>(100);
        // test mix between types ( not allowed )
        String sClass = paths[0].getLastPathComponent().getClass().toString();
        for (int i = 0; i < paths.length; i++) {
          if (!paths[i].getLastPathComponent().getClass().toString().equals(sClass)) {
            return;
          }
        }
        // get all components recursively
        for (int i = 0; i < paths.length; i++) {
          Object o = paths[i].getLastPathComponent();
          Enumeration<DefaultMutableTreeNode> e2 = ((DefaultMutableTreeNode) o)
              .depthFirstEnumeration();
          // return all childs nodes recursively
          while (e2.hasMoreElements()) {
            DefaultMutableTreeNode node = e2.nextElement();
            if (node instanceof TrackNode) {
              Track track = ((TrackNode) node).getTrack();
              if (track.getPlayeableFile(false) != null) {
                alTracks.add(((TrackNode) node).getTrack());
              }
            }
          }
        }
        // display menus according node type
        if (paths[0].getLastPathComponent() instanceof TrackNode) {
          jmenuTrack.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof AlbumNode) {
          jmenuAlbum.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof AuthorNode) {
          jmenuAuthor.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof StyleNode) {
          jmenuStyle.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof YearNode) {
          jmenuYear.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof DefaultMutableTreeNode) {
          jmenuCollection.show(jtree, e.getX(), e.getY());
        }
      }
    };

    jtree.addMouseListener(ml);
    // Expansion analyze to keep expended state
    jtree.addTreeExpansionListener(new TreeExpansionListener() {
      public void treeCollapsed(TreeExpansionEvent event) {
        Object o = event.getPath().getLastPathComponent();
        if (o instanceof StyleNode) {
          Style style = ((StyleNode) o).getStyle();
          style.removeProperty(XML_EXPANDED);
        } else if (o instanceof AuthorNode) {
          Author author = ((AuthorNode) o).getAuthor();
          author.removeProperty(XML_EXPANDED);
        } else if (o instanceof AlbumNode) {
          Album album = ((AlbumNode) o).getAlbum();
          album.removeProperty(XML_EXPANDED);
        } else if (o instanceof YearNode) {
          Year year = ((YearNode) o).getYear();
          year.removeProperty(XML_EXPANDED);
        }
      }

      public void treeExpanded(TreeExpansionEvent event) {
        Object o = event.getPath().getLastPathComponent();
        if (o instanceof StyleNode) {
          Style style = ((StyleNode) o).getStyle();
          style.setProperty(XML_EXPANDED, true);
        } else if (o instanceof AuthorNode) {
          Author author = ((AuthorNode) o).getAuthor();
          author.setProperty(XML_EXPANDED, true);
        } else if (o instanceof AlbumNode) {
          Album album = ((AlbumNode) o).getAlbum();
          album.setProperty(XML_EXPANDED, true);
        } else if (o instanceof YearNode) {
          Year year = ((YearNode) o).getYear();
          year.setProperty(XML_EXPANDED, true);
        }
      }
    });
    jtree.setAutoscrolls(true);
    // DND support
    new TreeTransferHandler(jtree, DnDConstants.ACTION_COPY_OR_MOVE, true);
    jspTree = new JScrollPane(jtree);
    double[][] dSize = { { TableLayout.FILL }, { 5, TableLayout.PREFERRED, 5, TableLayout.FILL } };
    setLayout(new TableLayout(dSize));
    add(jpsort, "0,1");
    add(jspTree, "0,3");
    expand();
  }

  /** Fill the tree */

  public void populateTree() {
    switch (ConfigurationManager.getInt(CONF_LOGICAL_TREE_SORT_ORDER)) {
    case TrackComparator.STYLE_AUTHOR_ALBUM:
      populateTreeByStyle();
      break;
    case TrackComparator.AUTHOR_ALBUM:
      populateTreeByAuthor();
      break;
    case TrackComparator.ALBUM:
      populateTreeByAlbum();
      break;
    case TrackComparator.YEAR_ALBUM:
      populateTreeByYear();
      break;
    case TrackComparator.DISCOVERY_ALBUM:
      populateTreeByDiscovery();
      break;
    }
  }

  /** Fill the tree by style */
  public void populateTreeByStyle() {
    // delete previous tree
    top.removeAllChildren();
    ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
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

  /** Fill the tree by author */
  public void populateTreeByAuthor() {
    // delete previous tree
    top.removeAllChildren();
    ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
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

  /** Fill the tree by year */
  public void populateTreeByYear() {
    // delete previous tree
    top.removeAllChildren();
    ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
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

  /** Fill the tree */
  public void populateTreeByAlbum() {
    // delete previous tree
    top.removeAllChildren();
    ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    for (Track track : tracks) {
      if (!track.shouldBeHidden()) {
        addTrackAndAlbum(top, track);
      }
    }
  }

  /** Fill the tree by discovery */
  public void populateTreeByDiscovery() {
    // delete previous tree
    top.removeAllChildren();
    ArrayList<Track> tracks = TrackManager.getInstance().getTracksAsList();
    Collections.sort(tracks, TrackManager.getInstance().getComparator());
    // Create separator nodes
    DefaultMutableTreeNode nodeWeekly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.36"));
    DefaultMutableTreeNode nodeMontly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.37"));
    DefaultMutableTreeNode nodeSixMontly = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.38"));
    DefaultMutableTreeNode nodeOlder = new DiscoveryDateNode(Messages
        .getString("TracksTreeView.39"));
    // Add separator nodes
    top.add(nodeWeekly);
    top.add(nodeMontly);
    top.add(nodeSixMontly);
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
      } else if (diff < 2419200000l) {
        addTrackAndAlbum(nodeMontly, track);
      } else if (diff < 14515200000l) {
        addTrackAndAlbum(nodeSixMontly, track);
      } else {
        addTrackAndAlbum(nodeOlder, track);
      }
    }
  }

  /**
   * Utility method used by populateByDiscovery method
   * 
   * @param node
   * @param track
   */
  private void addTrackAndAlbum(DefaultMutableTreeNode node, Track track) {
    boolean bAlbumExists = false;
    AlbumNode currentAlbum = null;
    Enumeration e = node.children();
    while (e.hasMoreElements()) {
      AlbumNode an = (AlbumNode) e.nextElement();
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
   * Create a Misc node
   */
  @SuppressWarnings("unchecked")
  public void cleanTree() {
    AuthorNode amisc = new AuthorNode(AuthorManager.getInstance().registerAuthor("Misc"));

    DefaultMutableTreeNode authorNode = new DefaultMutableTreeNode();
    DefaultMutableTreeNode albumNode = new DefaultMutableTreeNode();
    AlbumNode misc;
    Enumeration eAuthor = top.children();

    while (eAuthor.hasMoreElements()) {
      authorNode = (AuthorNode) eAuthor.nextElement();
      misc = new AlbumNode(AlbumManager.getInstance().registerAlbum("Misc"));

      for (Enumeration<AlbumNode> eAlbum = authorNode.children(); eAlbum.hasMoreElements();) {
        albumNode = eAlbum.nextElement();
        if (albumNode.getChildCount() < MIN_TRACKS_NUMBER) {
          while (albumNode.getChildCount() > 0) {

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
    new Thread() {
      public void run() {
       if (e.getSource() == jmiAlbumCDDBWizard) {
          ArrayList<Item> alTracks = new ArrayList<Item>(20);
          for (Item item : alSelected) {
            Album album = (Album) item;
            alTracks.addAll(album.getTracks());
          }
          Util.waiting();
          new CDDBWizard(alTracks);
        } else if (e.getSource() == jcbSort) {
          Util.waiting();
          // Set comparator
          ConfigurationManager.setProperty(CONF_LOGICAL_TREE_SORT_ORDER, Integer.toString(jcbSort
              .getSelectedIndex()));
          populateTree();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              SwingUtilities.updateComponentTreeUI(jtree);
              Util.stopWaiting();
            }
          });
        } else {
          // compute selection
          ArrayList<File> alFilesToPlay = new ArrayList<File>(alTracks.size());
          Iterator it = alTracks.iterator();
          while (it.hasNext()) {
            File file = ((Track) it.next()).getPlayeableFile(false);
            if (file != null) {
              alFilesToPlay.add(file);
            }
          }
          if (alFilesToPlay.size() == 0) {
            Messages.showErrorMessage(18);
            return;
          }
          if ((e.getSource() == jmiTrackPlay || e.getSource() == jmiAlbumPlay
              || e.getSource() == jmiAuthorPlay || e.getSource() == jmiStylePlay || e.getSource() == jmiYearPlay)) {
            FIFO.getInstance().push(
                Util.createStackItems(Util.applyPlayOption(alFilesToPlay), ConfigurationManager
                    .getBoolean(CONF_STATE_REPEAT), true), false);
          } else if ((e.getSource() == jmiTrackPush || e.getSource() == jmiAlbumPush
              || e.getSource() == jmiAuthorPush || e.getSource() == jmiStylePush || e.getSource() == jmiYearPush)) {
            FIFO.getInstance().push(
                Util.createStackItems(Util.applyPlayOption(alFilesToPlay), ConfigurationManager
                    .getBoolean(CONF_STATE_REPEAT), true), true);
          } else if ((e.getSource() == jmiAlbumPlayShuffle || e.getSource() == jmiAuthorPlayShuffle || e
              .getSource() == jmiStylePlayShuffle)
              || e.getSource() == jmiYearPlayShuffle) {
            Collections.shuffle(alFilesToPlay, new Random());
            FIFO.getInstance().push(
                Util.createStackItems(alFilesToPlay, ConfigurationManager
                    .getBoolean(CONF_STATE_REPEAT), true), false);
          } else if ((e.getSource() == jmiAlbumPlayRepeat || e.getSource() == jmiAuthorPlayRepeat
              || e.getSource() == jmiStylePlayRepeat || e.getSource() == jmiYearPlayRepeat)) {
            FIFO.getInstance().push(
                Util.createStackItems(Util.applyPlayOption(alFilesToPlay), true, true), false);
          } else if ((e.getSource() == jmiStyleAddFavorite || e.getSource() == jmiAlbumAddFavorite
              || e.getSource() == jmiAuthorAddFavorite || e.getSource() == jmiTrackAddFavorite || e
              .getSource() == jmiYearAddFavorite)) {
            Bookmarks.getInstance().addFiles(alFilesToPlay);
          } else if ((e.getSource() == jmiAlbumDelete || e.getSource() == jmiAuthorDelete
              || e.getSource() == jmiStyleDelete || e.getSource() == jmiTrackDelete)) {
            // TBI
          }
        }
      }
    }.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(Event event) {
    EventSubject subject = event.getSubject();
    if (subject.equals(EventSubject.EVENT_DEVICE_MOUNT)
        || subject.equals(EventSubject.EVENT_DEVICE_UNMOUNT)) {
      SwingWorker sw = new SwingWorker() {
        public Object construct() {
          populateTree();
          return null;
        }

        public void finished() {
          SwingUtilities.updateComponentTreeUI(jtree);
          expand();
          int i = jspTree.getVerticalScrollBar().getValue();
          jspTree.getVerticalScrollBar().setValue(i);
        }
      };
      sw.start();
    } else if (subject.equals(EventSubject.EVENT_DEVICE_REFRESH)) {
      SwingWorker sw = new SwingWorker() {
        public Object construct() {
          populateTree();
          return null;
        }

        public void finished() {
          SwingUtilities.updateComponentTreeUI(jtree);
          expand();
          int i = jspTree.getVerticalScrollBar().getValue();
          jspTree.getVerticalScrollBar().setValue(i);
        }
      };
      sw.start();
    }
    // Make sure to refresh cells (usefull to remove highliters for ie)
    repaint();
  }

  /**
   * Manages auto-expand
   * 
   */
  public void expand() {
    // expand all
    for (int i = 0; i < jtree.getRowCount(); i++) {
      Object o = jtree.getPathForRow(i).getLastPathComponent();
      if (o instanceof StyleNode) {
        Style style = ((StyleNode) o).getStyle();
        boolean bExp = style.getBooleanValue(XML_EXPANDED);
        if (bExp) {
          jtree.expandRow(i);
        }
      } else if (o instanceof AuthorNode) {
        Author author = ((AuthorNode) o).getAuthor();
        boolean bExp = author.getBooleanValue(XML_EXPANDED);
        if (bExp) {
          jtree.expandRow(i);
        }
      } else if (o instanceof AlbumNode) {
        Album album = ((AlbumNode) o).getAlbum();
        boolean bExp = album.getBooleanValue(XML_EXPANDED);
        if (bExp) {
          jtree.expandRow(i);
        }
      } else if (o instanceof YearNode) {
        Year year = ((YearNode) o).getYear();
        boolean bExp = year.getBooleanValue(XML_EXPANDED);
        if (bExp) {
          jtree.expandRow(i);
        }
      }
    }
  }

  /**
   * @return Returns the alTracks.
   */
  public ArrayList getTrackSelection() {
    return alTracks;
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
  public String toString() {
    return ((Style) super.getData()).getName2();
  }

  /**
   * @return Returns the track.
   */
  public Style getStyle() {
    return (Style) super.getData();
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
  public String toString() {
    return ((Author) super.getData()).getName2();
  }

  /**
   * @return Returns the author.
   */
  public Author getAuthor() {
    return (Author) super.getData();
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
  public String toString() {
    if (((Year) super.getData()).getValue() > 0) {
      return ((Year) super.getData()).getName();
    } else {
      return Messages.getString("unknown_year");
    }
  }

  /**
   * @return Returns the year.
   */
  public Year getYear() {
    return (Year) super.getData();
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
  public String toString() {
    return ((Album) super.getData()).getName2();
  }

  /**
   * @return Returns the album.
   */
  public Album getAlbum() {
    return (Album) super.getData();
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
  public String toString() {
    return ((Track) super.getData()).getName();
  }

  /**
   * @return Returns the track.
   */
  public Track getTrack() {
    return (Track) super.getData();
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
