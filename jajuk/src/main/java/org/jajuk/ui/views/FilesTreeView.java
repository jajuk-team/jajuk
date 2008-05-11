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

import java.awt.Component;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
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

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.actions.RefactorAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.ItemMoveManager;
import org.jajuk.ui.helpers.TransferableTreeNode;
import org.jajuk.ui.helpers.TreeRootElement;
import org.jajuk.ui.helpers.TreeTransferHandler;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.wizard.DeviceWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jvnet.substance.SubstanceDefaultTreeCellRenderer;

/**
 * Physical tree view
 */
public class FilesTreeView extends AbstractTreeView implements ActionListener,
    org.jajuk.services.events.Observer {

  private static final long serialVersionUID = 1L;

  /** Files selection */
  ArrayList<File> alFiles = new ArrayList<File>(100);

  /** Directories selection */
  ArrayList<Directory> alDirs = new ArrayList<Directory>(10);

  /** Collection export */
  JPopupMenu jmenuCollection;

  JMenuItem jmiCollectionReport;

  JMenuItem jmiCollectionDuplicateFiles;

  JMenuItem jmiDirRefresh;

  JMenuItem jmiDirDesynchro;

  JMenuItem jmiDirResynchro;

  JMenuItem jmiDirCreatePlaylist;

  JMenuItem jmiDirRefactor;

  JMenuItem jmiDevMount;

  JMenuItem jmiDevUnmount;

  JMenuItem jmiDevRefresh;

  JMenuItem jmiDevSynchronize;

  JMenuItem jmiDevTest;

  JMenuItem jmiDevOrganize;

  JMenuItem jmiDevConfiguration;

  JMenuItem jmiPlaylistFileCopy;

  JMenuItem jmiPlaylistFileCut;

  JMenuItem jmiPlaylistFilePaste;

  /**
   * Used to differentiate user action tree collapse from code tree colapse*
   */
  private boolean bAutoCollapse = false;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("FilesTreeView.0");
  }

  /** Constructor */
  public FilesTreeView() {
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_DEVICE_MOUNT);
    eventSubjectSet.add(EventSubject.EVENT_DEVICE_UNMOUNT);
    eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
    eventSubjectSet.add(EventSubject.EVENT_CDDB_WIZARD);
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

    // Collection menu
    jmenuCollection = new JPopupMenu();
    // Export
    Action actionReportCollection = ActionManager.getAction(JajukAction.CREATE_REPORT);
    jmiCollectionReport = new JMenuItem(actionReportCollection);
    // Add custom data to this component in order to allow the ReportAction
    // to be able to get it
    jmiCollectionReport.putClientProperty(DETAIL_ORIGIN, COLLECTION_PHYSICAL);
    jmenuCollection.add(jmiCollectionReport);

    Action actionDuplicateFiles = ActionManager.getAction(JajukAction.FIND_DUPLICATE_FILES);
    jmiCollectionDuplicateFiles = new JMenuItem(actionDuplicateFiles);
    jmenuCollection.add(jmiCollectionDuplicateFiles);

    // Directory menu
    Action actionRefreshDir = ActionManager.getAction(JajukAction.REFRESH);
    jmiDirRefresh = new JMenuItem(actionRefreshDir);
    jmiDirRefresh.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiDirRefresh.addActionListener(this);
    jmiDirDesynchro = new JMenuItem(Messages.getString("FilesTreeView.14"),
        IconLoader.ICON_DIRECTORY_DESYNCHRO);
    jmiDirDesynchro.addActionListener(this);
    jmiDirResynchro = new JMenuItem(Messages.getString("FilesTreeView.15"),
        IconLoader.ICON_DIRECTORY_SYNCHRO);
    jmiDirResynchro.addActionListener(this);
    jmiDirCreatePlaylist = new JMenuItem(Messages.getString("FilesTreeView.16"));
    jmiDirCreatePlaylist.setEnabled(false);
    jmiDirCreatePlaylist.addActionListener(this);
    jmiDirRefactor = new JMenuItem(Messages.getString(("FilesTreeView.62")),
        IconLoader.ICON_REORGANIZE);
    jmiDirRefactor.addActionListener(this);

    // Device menu
    jmiDevMount = new JMenuItem(Messages.getString("FilesTreeView.28"), IconLoader.ICON_UNMOUNT);
    jmiDevMount.addActionListener(this);
    jmiDevUnmount = new JMenuItem(Messages.getString("FilesTreeView.29"), IconLoader.ICON_UNMOUNT);
    jmiDevUnmount.addActionListener(this);
    jmiDevRefresh = new JMenuItem(Messages.getString("FilesTreeView.30"), IconLoader.ICON_REFRESH);
    jmiDevRefresh.addActionListener(this);
    jmiDevSynchronize = new JMenuItem(Messages.getString("FilesTreeView.31"),
        IconLoader.ICON_SYNCHRO);
    jmiDevSynchronize.addActionListener(this);
    jmiDevTest = new JMenuItem(Messages.getString("FilesTreeView.32"), IconLoader.ICON_TEST);
    jmiDevTest.addActionListener(this);
    jmiDevConfiguration = new JMenuItem(Messages.getString("FilesTreeView.55"),
        IconLoader.ICON_CONFIGURATION);
    jmiDevConfiguration.addActionListener(this);
    jmiDevOrganize = new JMenuItem(Messages.getString(("FilesTreeView.62")),
        IconLoader.ICON_REORGANIZE);
    jmiDevOrganize.addActionListener(this);

    // playlist menu
    // File menu
    jmiPlaylistFileCopy = new JMenuItem(Messages.getString("FilesTreeView.40"));
    jmiPlaylistFileCopy.setEnabled(false);
    jmiPlaylistFileCopy.addActionListener(this);
    jmiPlaylistFileCut = new JMenuItem(Messages.getString("FilesTreeView.41"));
    jmiPlaylistFileCut.setEnabled(false);
    jmiPlaylistFileCut.addActionListener(this);
    jmiPlaylistFilePaste = new JMenuItem(Messages.getString("FilesTreeView.42"));
    jmiPlaylistFilePaste.setEnabled(false);
    jmiPlaylistFilePaste.addActionListener(this);

    // Add Action Listener
    jmiCopy.addActionListener(this);
    jmiCut.addActionListener(this);
    jmiPaste.addActionListener(this);

    // By default disable paste
    jmiPaste.setEnabled(false);

    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    top = new TreeRootElement(Messages.getString("FilesTreeView.47"));

    // Register on the list for subject we are interested in
    ObservationManager.register(this);

    // fill the tree model
    populateTree();

    // create tree
    createTree();

    jtree.setCellRenderer(new SubstanceDefaultTreeCellRenderer() {
      private static final long serialVersionUID = 1L;

      @Override
      public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
          boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
        if (value instanceof FileNode) {
          setBorder(null);
          File file = ((FileNode) value).getFile();

          // Note: file.getName() is better here as it will do less and not
          // create java.io.File in File
          String ext = Util.getExtension(file.getName());
          Type type = TypeManager.getInstance().getTypeByExtension(ext);
          // Find associated icon with this type
          URL icon = null;
          String sIcon;
          if (type != null) {
            sIcon = (String) type.getProperties().get(XML_TYPE_ICON);
            try {
              icon = new URL(sIcon);
            } catch (MalformedURLException e) {
              Log.error(e);
            }
          }
          if (icon == null) {
            setIcon(IconLoader.ICON_TYPE_WAV);
          } else {
            setIcon(new ImageIcon(icon));
          }
        } else if (value instanceof PlaylistFileNode) {
          setBorder(null);
          setIcon(IconLoader.ICON_PLAYLIST_FILE);
        } else if (value instanceof DeviceNode) {
          setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 0));
          Device device = ((DeviceNode) value).getDevice();
          switch ((int) device.getType()) {
          case 0:
            if (device.isMounted()) {
              setIcon(IconLoader.ICON_DEVICE_DIRECTORY_MOUNTED_SMALL);
            } else {
              setIcon(IconLoader.ICON_DEVICE_DIRECTORY_UNMOUNTED_SMALL);
            }
            break;
          case 1:
            if (device.isMounted()) {
              setIcon(IconLoader.ICON_DEVICE_CD_MOUNTED_SMALL);
            } else {
              setIcon(IconLoader.ICON_DEVICE_CD_UNMOUNTED_SMALL);
            }
            break;
          case 2:
            if (device.isMounted()) {
              setIcon(IconLoader.ICON_DEVICE_NETWORK_DRIVE_MOUNTED_SMALL);
            } else {
              setIcon(IconLoader.ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED_SMALL);
            }
            break;
          case 3:
            if (device.isMounted()) {
              setIcon(IconLoader.ICON_DEVICE_EXT_DD_MOUNTED_SMALL);
            } else {
              setIcon(IconLoader.ICON_DEVICE_EXT_DD_UNMOUNTED_SMALL);
            }
            break;
          case 4:
            if (device.isMounted()) {
              setIcon(IconLoader.ICON_DEVICE_PLAYER_MOUNTED_SMALL);
            } else {
              setIcon(IconLoader.ICON_DEVICE_PLAYER_UNMOUNTED_SMALL);
            }
            break;
          }
        } else if (value instanceof DirectoryNode) {
          setBorder(null);
          Directory dir = ((DirectoryNode) value).getDirectory();
          boolean bSynchro = dir.getBooleanValue(XML_DIRECTORY_SYNCHRONIZED);
          if (bSynchro) { // means this device is not synchronized
            setIcon(IconLoader.ICON_DIRECTORY_SYNCHRO);
          } else {
            setIcon(IconLoader.ICON_DIRECTORY_DESYNCHRO);
          }
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

    // Tree selection listener to detect a selection (single click
    // , manages simple or multiple selections)
    jtree.addTreeSelectionListener(new TreeSelectionListener() {
      @SuppressWarnings("unchecked")
      public void valueChanged(TreeSelectionEvent e) {
        // Avoid concurrency with the mouse listener
        synchronized (lock) {
          paths = jtree.getSelectionModel().getSelectionPaths();
          // nothing selected, can be called during dnd
          if (paths == null) {
            return;
          }
          int items = 0;
          long lSize = 0;
          // get all components recursively
          selectedRecursively.clear();
          alSelected.clear();
          for (int i = 0; i < paths.length; i++) {
            Object o = paths[i].getLastPathComponent();
            if (o instanceof TreeRootElement) {// root node
              items = FileManager.getInstance().getElementCount();
              selectedRecursively.addAll(FileManager.getInstance().getFiles());
              for (Item item : selectedRecursively) {
                lSize += ((File) item).getSize();
              }
              break;
            } else {
              Item item = (Item) ((TransferableTreeNode) o).getData();
              alSelected.add(item);
            }
            // return all childs nodes recursively
            Enumeration<DefaultMutableTreeNode> e2 = ((DefaultMutableTreeNode) o)
                .depthFirstEnumeration();
            while (e2.hasMoreElements()) {
              DefaultMutableTreeNode node = e2.nextElement();
              if (node instanceof FileNode) {
                File file = ((FileNode) node).getFile();
                // don't count same file twice if user
                // select directory and then files inside
                selectedRecursively.add(file);
                lSize += file.getSize();
                items++;
              } else if (node instanceof PlaylistFileNode) {
                Playlist plf = ((PlaylistFileNode) node).getPlaylistFile();
                selectedRecursively.add(plf);
                items++;
              }
            }
          }
          lSize /= 1048576; // set size in MB
          StringBuilder sbOut = new StringBuilder().append(items).append(
              Messages.getString("FilesTreeView.52"));
          if (lSize > 1024) { // more than 1024 MB -> in GB
            sbOut.append(lSize / 1024).append('.').append(lSize % 1024).append(
                Messages.getString("FilesTreeView.53"));
          } else {
            sbOut.append(lSize).append(Messages.getString("FilesTreeView.54"));
          }
          InformationJPanel.getInstance().setSelection(sbOut.toString());
          if (ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE)) {
            // if table is synchronized with tree, notify the
            // selection change
            Properties properties = new Properties();
            properties.put(DETAIL_SELECTION, selectedRecursively);
            properties.put(DETAIL_ORIGIN, PerspectiveManager.getCurrentPerspective().getID());
            ObservationManager.notify(new Event(EventSubject.EVENT_SYNC_TREE_TABLE, properties));
          }
          // Check CDDB requests
          if (alSelected.size() > 0 // alSelected = 0 for collection
              // selection
              && alSelected.get(0) instanceof Directory) {
            boolean bShowCDDB = false;
            for (Item item : alSelected) {
              // check it is a directory (can be a file if user
              // selects n files + n directories)
              if (!(item instanceof Directory)) {
                continue;
              }
              // if at least one selected dir contains a file,
              // show option
              if (item instanceof Directory) {
                Directory dir = (Directory) item;
                if (dir.getFiles().size() > 0) {
                  bShowCDDB = true;
                }
              }
            }
            jmiCDDBWizard.setEnabled(bShowCDDB);
          }
        }
      }
    });
    // Listen for single / double click
    jtree.addMouseListener(new MouseAdapter() {

      @Override
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
              if (o instanceof FileNode) {
                File file = ((FileNode) o).getFile();
                try {
                  FIFO.getInstance()
                      .push(
                          new StackItem(file, ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
                              true), ConfigurationManager.getBoolean(CONF_OPTIONS_PUSH_ON_CLICK));
                } catch (JajukException je) {
                  Log.error(je);
                }
              }
              // double click on a playlist
              else if (o instanceof PlaylistFileNode) {
                Playlist plf = ((PlaylistFileNode) o).getPlaylistFile();
                ArrayList<File> alToPlay = null;
                try {
                  alToPlay = plf.getFiles();
                } catch (JajukException je) {
                  Log.error(je.getCode(), plf.getName(), null);
                  Messages.showErrorMessage(je.getCode(), plf.getName());
                  return;
                }
                // check playlist contains accessible
                // tracks
                if (alToPlay == null || alToPlay.size() == 0) {
                  Messages.showErrorMessage(18);
                  return;
                } else {
                  FIFO.getInstance().push(
                      Util.createStackItems(Util.applyPlayOption(alToPlay), ConfigurationManager
                          .getBoolean(CONF_STATE_REPEAT), true), false);
                }
              }
            }
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          handlePopup(e);
        }
      }

      public void handlePopup(final MouseEvent e) {
        TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
        if (path == null) {
          return;
        }
        // right click on a selected node set Right click
        // behavior identical to konqueror tree:
        // if none or 1 node is selected, a right click on
        // another node select it if more than 1, we keep selection and
        // display a popup for them
        if (jtree.getSelectionCount() < 2) {
          jtree.getSelectionModel().setSelectionPath(path);
        }
        paths = jtree.getSelectionModel().getSelectionPaths();
        alFiles.clear();
        alDirs.clear();
        // test mix between types ( not allowed )
        Class<?> c = paths[0].getLastPathComponent().getClass();
        for (int i = 0; i < paths.length; i++) {
          if (!paths[i].getLastPathComponent().getClass().equals(c)) {
            return;
          }
        }
        // Test that all items are mounted or hide menu item
        // device:mono selection for the moment
        if (c.equals(DeviceNode.class)) {
          Device device = ((DeviceNode) (paths[0].getLastPathComponent())).getDevice();
          if (device.isMounted()) {
            jmiDevMount.setEnabled(false);
            jmiDevUnmount.setEnabled(true);
          } else {
            jmiDevMount.setEnabled(true);
            jmiDevUnmount.setEnabled(false);
          }
          final Directory dir = DirectoryManager.getInstance().registerDirectory(device);
          boolean bShowCDDB = false;
          if (dir.getFiles().size() > 0) {
            bShowCDDB = true;
          }
          jmiCDDBWizard.setEnabled(bShowCDDB);
        }
        if (c.equals(DirectoryNode.class)) {
          for (int i = 0; i < paths.length; i++) {
            Directory dir = ((DirectoryNode) (paths[i].getLastPathComponent())).getDirectory();
            if (!dir.getDevice().isMounted()) {
              continue;
            }
          }
        }
        if (c.equals(FileNode.class)) {
          for (int i = 0; i < paths.length; i++) {
            File file = ((FileNode) (paths[i].getLastPathComponent())).getFile();
            if (!file.isReady()) {
              continue;
            }
          }
        }
        jmiDelete.setEnabled(true);
        if (c.equals(PlaylistFileNode.class)) {
          for (int i = 0; i < paths.length; i++) {
            Playlist plf = ((PlaylistFileNode) (paths[i].getLastPathComponent())).getPlaylistFile();
            if (!plf.isReady()) {
              jmiDelete.setEnabled(false);
              continue;
            }
          }
        }

        // get all components recursively
        for (int i = 0; i < paths.length; i++) {
          Object o = paths[i].getLastPathComponent();
          // return all childs nodes recursively
          Enumeration e2 = ((DefaultMutableTreeNode) o).depthFirstEnumeration();
          while (e2.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e2.nextElement();
            if (node instanceof FileNode) {
              alFiles.add(((FileNode) node).getFile());
            } else if (node instanceof DirectoryNode) {
              Directory dir = ((DirectoryNode) node).getDirectory();
              alDirs.add(dir);
            }
          }
        }
        // display menus according node type
        if (paths[0].getLastPathComponent() instanceof FileNode) {
          jmenu = new JPopupMenu();
          jmenu.add(jmiPlay);
          jmenu.add(jmiPush);
          jmenu.add(jmiCut);
          jmenu.add(jmiCopy);
          jmenu.add(jmiRename);
          jmenu.add(jmiAddFavorite);
          jmenu.add(jmiDelete);
          jmenu.add(jmiProperties);
          jmenu.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof DirectoryNode) {
          jmenu = new JPopupMenu();
          jmenu.add(jmiPlay);
          jmenu.add(jmiPush);
          jmenu.add(jmiPlayShuffle);
          jmenu.add(jmiPlayRepeat);
          jmenu.add(jmiDirRefresh);
          jmenu.add(jmiCut);
          jmenu.add(jmiCopy);
          jmenu.add(jmiPaste);
          jmenu.add(jmiRename);
          jmenu.add(jmiNewFolder);
          jmenu.add(jmiDirDesynchro);
          jmenu.add(jmiDirResynchro);
          jmenu.add(jmiAddFavorite);
          jmenu.add(jmiCDDBWizard);
          jmenu.add(jmiReport);
          jmenu.add(jmiDirRefactor);
          jmenu.add(jmiDelete);
          jmenu.add(jmiProperties);
          jmenu.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof PlaylistFileNode) {
          jmenu = new JPopupMenu();
          jmenu.add(jmiPlay);
          jmenu.add(jmiPush);
          jmenu.add(jmiPlayShuffle);
          jmenu.add(jmiPlayRepeat);
          jmenu.add(jmiAddFavorite);
          jmenu.add(jmiDelete);
          jmenu.add(jmiProperties);
          jmenu.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof DeviceNode) {
          jmenu = new JPopupMenu();
          jmenu.add(jmiPlay);
          jmenu.add(jmiPush);
          jmenu.add(jmiPaste);
          jmenu.add(jmiPlayShuffle);
          jmenu.add(jmiPlayRepeat);
          jmenu.add(jmiNewFolder);
          jmenu.add(jmiDevMount);
          jmenu.add(jmiDevUnmount);
          jmenu.add(jmiDevRefresh);
          jmenu.add(jmiDevTest);
          jmenu.add(jmiCDDBWizard);
          jmenu.add(jmiDevOrganize);
          jmenu.add(jmiReport);
          jmenu.add(jmiDevSynchronize);
          jmenu.add(jmiDevConfiguration);
          jmenu.add(jmiProperties);
          Device device = ((DeviceNode) paths[0].getLastPathComponent()).getDevice();
          // if the device is not synchronized
          if (device.getValue(XML_DEVICE_SYNCHRO_SOURCE).equals("")) {
            jmiDevSynchronize.setEnabled(false);
          } else {
            jmiDevSynchronize.setEnabled(true);
          }
          // operations on devices are mono-target expect for
          // reporting
          if (paths.length > 1) {
            // Disable all menu items except reporting
            for (int i = 0; i < jmenu.getSubElements().length; i++) {
              ((JMenuItem) jmenu.getSubElements()[i]).setEnabled(false);
            }
            jmiReport.setEnabled(true);
          } else {
            // Enable all menu items
            for (int i = 0; i < jmenu.getSubElements().length; i++) {
              ((JMenuItem) jmenu.getSubElements()[i]).setEnabled(true);
            }
          }
          jmenu.show(jtree, e.getX(), e.getY());
        } else if (paths[0].getLastPathComponent() instanceof DefaultMutableTreeNode) {
          // collection
          jmenuCollection.show(jtree, e.getX(), e.getY());
        }

      }
    });
    // Expansion analyzed to keep expended state
    jtree.addTreeExpansionListener(new TreeExpansionListener() {
      public void treeCollapsed(TreeExpansionEvent event) {
        Object o = event.getPath().getLastPathComponent();
        if (o instanceof DirectoryNode && !bAutoCollapse) {
          Directory dir = ((DirectoryNode) o).getDirectory();
          dir.removeProperty(XML_EXPANDED);
        } else if (o instanceof DeviceNode && !bAutoCollapse) {
          Device device = ((DeviceNode) o).getDevice();
          device.removeProperty(XML_EXPANDED);
        }
      }

      public void treeExpanded(TreeExpansionEvent event) {
        Object o = event.getPath().getLastPathComponent();
        if (o instanceof DirectoryNode && !bAutoCollapse) {
          Directory dir = ((DirectoryNode) o).getDirectory();
          dir.setProperty(XML_EXPANDED, true);
        } else if (o instanceof DeviceNode && !bAutoCollapse) {
          Device device = ((DeviceNode) o).getDevice();
          device.setProperty(XML_EXPANDED, true);
        }

      }
    });
    jtree.setAutoscrolls(true);
    // DND support
    new TreeTransferHandler(jtree, DnDConstants.ACTION_COPY_OR_MOVE, true);
    // tree itself
    jspTree = new JScrollPane(jtree);
    jspTree.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    add(jspTree);
    // expand all
    expand();
  }

  /** Fill the tree */
  public synchronized void populateTree() {
    top.removeAllChildren();
    // add devices
    Iterator<Device> it1 = DeviceManager.getInstance().getDevices().iterator();
    while (it1.hasNext()) {
      Device device = it1.next();
      DefaultMutableTreeNode nodeDevice = new DeviceNode(device);
      top.add(nodeDevice);
    }
    // add directories
    ArrayList<Directory> directories = null;
    directories = new ArrayList<Directory>(DirectoryManager.getInstance().getDirectories());
    Iterator<Directory> it2 = directories.iterator();
    while (it2.hasNext()) {
      Directory directory = it2.next();
      if (directory.shouldBeHidden()) {
        continue;
      }
      // device root directory, do not display
      if (directory.getParentDirectory() != null) {
        // parent directory is a device
        if (directory.getParentDirectory().getName().equals("")) {
          DeviceNode deviceNode = DeviceNode.getDeviceNode(directory.getDevice());
          if (deviceNode != null) {
            deviceNode.add(new DirectoryNode(directory));
          }
        } else { // parent directory not root
          DirectoryNode parentDirectoryNode = DirectoryNode.getDirectoryNode(directory
              .getParentDirectory());
          if (parentDirectoryNode != null) { // paranoia check
            parentDirectoryNode.add(new DirectoryNode(directory));
          }
        }
      }
    }
    // add files
    ArrayList<File> files = new ArrayList<File>(FileManager.getInstance().getFiles());
    Iterator<File> it3 = files.iterator();
    while (it3.hasNext()) {
      File file = it3.next();
      if (file.shouldBeHidden()) { // should be hidden by option
        continue;
      }
      DirectoryNode directoryNode = DirectoryNode.getDirectoryNode(file.getDirectory());
      if (directoryNode == null) {
        // means this file is at root of a device
        DeviceNode deviceNode = DeviceNode.getDeviceNode(file.getDevice());
        deviceNode.add(new FileNode(file));
      } else {
        // this file is in a regular directory
        directoryNode.add(new FileNode(file));
      }
    }

    // add playlists
    ArrayList<Playlist> playlists = new ArrayList<Playlist>(PlaylistManager.getInstance()
        .getPlaylists());
    Iterator<Playlist> it4 = playlists.iterator();
    while (it4.hasNext()) {
      Playlist playlistFile = it4.next();
      // should be hidden by option
      if (playlistFile.shouldBeHidden()) {
        continue;
      }
      DirectoryNode directoryNode = DirectoryNode.getDirectoryNode(playlistFile.getDirectory());
      if (directoryNode == null) {
        // Add the playlist under the device node
        DeviceNode deviceNode = DeviceNode.getDeviceNode(playlistFile.getDirectory().getDevice());
        if (deviceNode != null) {
          deviceNode.add(new PlaylistFileNode(playlistFile));
        }
      } else {
        // Add the playlist under a common directory node
        directoryNode.add(new PlaylistFileNode(playlistFile));
      }
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    // multiple selection on properties(note we handle files and dirs
    // properties later)
    if ((alFiles != null && (e.getSource() == jmiDirRefactor || e.getSource() == jmiDevOrganize))) {
      Util.waiting();
      for (Item item : alSelected) {
        // Check if user made a global cancel
        if (RefactorAction.bStopAll) {
          RefactorAction.bStopAll = false;
          return;
        }
        // If user selected a device, take associated directory
        if (item instanceof Device) {
          item = ((Device) item).getRootDirectory();
        }
        final Directory dir = (Directory) item;
        Util.waiting();
        new RefactorAction(dir.getFilesRecursively());
      }
    } else if (e.getSource() == jmiDevMount) {
      for (int i = 0; i < paths.length; i++) {
        DeviceNode node = (DeviceNode) (paths[i].getLastPathComponent());
        Device device = node.getDevice();
        try {
          device.mount();
          jtree.expandPath(new TreePath(node.getPath()));
        } catch (Exception ex) {
          Messages.showErrorMessage(11);
        }
      }
    } else if (e.getSource() == jmiDevUnmount) {
      for (int i = 0; i < paths.length; i++) {
        DeviceNode node = (DeviceNode) (paths[i].getLastPathComponent());
        Device device = node.getDevice();
        try {
          device.unmount();
          jtree.collapsePath(new TreePath(node.getPath()));
        } catch (Exception ex) {
          Messages.showErrorMessage(12);
        }
      }
    } else if (e.getSource() == jmiDevRefresh) {
      Device device = ((DeviceNode) (paths[0].getLastPathComponent())).getDevice();
      device.refresh(true, true); // ask user if he wants to make deep or
      // fast scan
    } else if (e.getSource() == jmiDevSynchronize) {
      Device device = ((DeviceNode) (paths[0].getLastPathComponent())).getDevice();
      device.synchronize(true);
    } else if (e.getSource() == jmiDevTest) {
      new Thread() {
        // test asynchronously in case of delay (samba
        // pbm for ie)
        @Override
        public void run() {
          Device device = ((DeviceNode) (paths[0].getLastPathComponent())).getDevice();
          if (device.test()) {
            Messages.showInfoMessage(Messages.getString("DeviceView.21"), IconLoader.ICON_OK);
          } else {
            Messages.showInfoMessage(Messages.getString("DeviceView.22"), IconLoader.ICON_KO);
          }
        }
      }.start();
    } else if (e.getSource() == jmiDirDesynchro) {
      for (Directory dir : alDirs) {
        dir.setProperty(XML_DIRECTORY_SYNCHRONIZED, false);
      }
      jtree.revalidate();
      jtree.repaint();
    } else if (e.getSource() == jmiDirResynchro) {
      for (Directory dir : alDirs) {
        dir.setProperty(XML_DIRECTORY_SYNCHRONIZED, true);
      }
      jtree.revalidate();
      jtree.repaint();
    } else if (e.getSource() == jmiCopy || e.getSource() == jmiCut) {
      jmiPaste.setEnabled(true);
      jmenu.repaint();
    } else if (e.getSource() == jmiPaste) {
      if ("Cut".equals(ItemMoveManager.getInstance().getAction())) {
        jmiPaste.setEnabled(false);
        jmenu.repaint();
      }
    } else if (e.getSource() == jmiDevConfiguration) {
      Device device = ((DeviceNode) paths[0].getLastPathComponent()).getDevice();
      DeviceWizard dw = new DeviceWizard();
      dw.updateWidgets(device);
      dw.pack();
      dw.setVisible(true);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */

  public void update(Event event) {
    final EventSubject subject = event.getSubject();
    if (subject.equals(EventSubject.EVENT_DEVICE_MOUNT)
        || subject.equals(EventSubject.EVENT_DEVICE_UNMOUNT)
        || subject.equals(EventSubject.EVENT_DEVICE_REFRESH)) {
      SwingWorker sw = new SwingWorker() {
        @Override
        public Object construct() {
          populateTree();
          return null;
        }

        @Override
        public void finished() {
          SwingUtilities.updateComponentTreeUI(jtree);
          bAutoCollapse = true;
          expand();
          bAutoCollapse = false;
          int i = jspTree.getVerticalScrollBar().getValue();
          jspTree.getVerticalScrollBar().setValue(i);
        }
      };
      sw.start();
    }
  }

  /**
   * Manages auto-expand Expand behavior is:
   * <p>
   * At startup, tree expand state is the same that the one kept at last session
   * (we use XML_EXPANDED stored properties to restore it)
   * </p>
   * <p>
   * When mounting a device from the tree, the device node is expanded
   * </p>
   * <p>
   * When unmounting a device from the tree, the device node is collapsed
   * </p>
   */
  private void expand() {
    // begin by expanding all needed devices and directory, only after,
    // collapse unmounted devices if required
    for (int i = 0; i < jtree.getRowCount(); i++) {
      Object o = jtree.getPathForRow(i).getLastPathComponent();
      if (o instanceof DeviceNode) {
        Device device = ((DeviceNode) o).getDevice();
        if (device.getBooleanValue(XML_EXPANDED)) {
          jtree.expandRow(i);
        }
        // Collapse node (useful to hide an live-unmounteddevice for ie
        // )
        else {
          jtree.collapseRow(i);
        }
      } else if (o instanceof DirectoryNode) {
        Directory dir = ((DirectoryNode) o).getDirectory();
        if (dir.getBooleanValue(XML_EXPANDED)) {
          jtree.expandRow(i);
        }
      }
    }
  }
}

/**
 * File node
 */
class FileNode extends TransferableTreeNode {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param file
   */
  public FileNode(File file) {
    super(file);
  }

  /**
   * return a string representation of this file node
   */
  @Override
  public String toString() {
    return ((File) super.getData()).getName();
  }

  /**
   * @return Returns the file.
   */
  public File getFile() {
    return (File) super.getData();
  }

}

/**
 * Device node
 */
class DeviceNode extends TransferableTreeNode {

  private static final long serialVersionUID = 1L;

  /** device -> deviceNode hashmap */
  public static HashMap<Device, DeviceNode> hmDeviceDeviceNode = new HashMap<Device, DeviceNode>(
      100);

  /**
   * Constructor
   * 
   * @param device
   */
  public DeviceNode(Device device) {
    super(device);
    hmDeviceDeviceNode.put(device, this);
  }

  /** Return associated device node */
  public static DeviceNode getDeviceNode(Device device) {
    return hmDeviceDeviceNode.get(device);
  }

  /**
   * return a string representation of this device node
   */
  @Override
  public String toString() {
    return ((Device) super.getData()).getName();
  }

  /**
   * @return Returns the device.
   */
  public Device getDevice() {
    return (Device) super.getData();
  }

}

/**
 * Directory node
 */
class DirectoryNode extends TransferableTreeNode {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** directory -> directoryNode hashmap */
  public static HashMap<Directory, DirectoryNode> hmDirectoryDirectoryNode = new HashMap<Directory, DirectoryNode>(
      100);

  /**
   * Constructor
   * 
   * @param Directory
   */
  public DirectoryNode(Directory directory) {
    super(directory);
    hmDirectoryDirectoryNode.put(directory, this);
  }

  /** Return associated directory node */
  public static DirectoryNode getDirectoryNode(Directory directory) {
    return hmDirectoryDirectoryNode.get(directory);
  }

  /**
   * return a string representation of this directory node
   */
  @Override
  public String toString() {
    return ((Directory) getData()).getName();
  }

  /**
   * @return Returns the directory.
   */
  public Directory getDirectory() {
    return (Directory) getData();
  }

}

/**
 * Playlist node
 */
class PlaylistFileNode extends TransferableTreeNode {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param Playlist
   */
  public PlaylistFileNode(Playlist playlistFile) {
    super(playlistFile);
  }

  /**
   * return a string representation of this playlistFile node
   */
  @Override
  public String toString() {
    return ((Playlist) super.getData()).getName();
  }

  /**
   * @return Returns the playlist node.
   */
  public Playlist getPlaylistFile() {
    return (Playlist) getData();
  }

}
