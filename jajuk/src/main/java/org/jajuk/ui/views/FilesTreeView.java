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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.Type;
import org.jajuk.base.TypeManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.actions.RefactorAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.helpers.ItemMoveManager;
import org.jajuk.ui.helpers.ItemMoveManager.MoveActions;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.LazyLoadingTreeNode;
import org.jajuk.ui.helpers.TreeRootElement;
import org.jajuk.ui.helpers.TreeTransferHandler;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.wizard.DeviceWizard;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jvnet.substance.api.renderers.SubstanceDefaultTreeCellRenderer;

/**
 * Physical tree view.
 */
public class FilesTreeView extends AbstractTreeView implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** Directories selection. */
  List<Directory> alDirs = new ArrayList<Directory>(10);
  JMenuItem jmiDirRefresh;
  JMenuItem jmiDirDesynchro;
  JMenuItem jmiDirResynchro;
  JMenuItem jmiDirCreatePlaylist;
  JMenuItem jmiDirRefactor;
  JMenuItem jmiDirCopyURL;
  JMenuItem jmiOpenExplorer;
  JMenuItem jmiDevMount;
  JMenuItem jmiDevUnmount;
  JMenuItem jmiDevRefresh;
  JMenuItem jmiDevSynchronize;
  JMenuItem jmiDevTest;
  JMenuItem jmiDevOrganize;
  JMenuItem jmiDevConfiguration;
  JMenuItem jmiDevDelete;
  JMenuItem jmiPlaylistFileCopy;
  JMenuItem jmiPlaylistFileCut;
  JMenuItem jmiPlaylistFilePaste;
  JMenuItem jmiPlaylistCopyURL;
  JMenuItem jmiPlaylistPrepareParty;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("FilesTreeView.0");
  }

  /**
   * Constructor.
   */
  public FilesTreeView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  @Override
  public void initUI() {
    super.initUI();
    // Directory menu
    Action actionRefreshDir = ActionManager.getAction(JajukActions.REFRESH);
    jmiDirRefresh = new JMenuItem(actionRefreshDir);
    jmiDirRefresh.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    jmiDirRefresh.addActionListener(this);
    jmiDirDesynchro = new JMenuItem(Messages.getString("FilesTreeView.14"),
        IconLoader.getIcon(JajukIcons.DIRECTORY_DESYNCHRO));
    jmiDirDesynchro.addActionListener(this);
    jmiDirResynchro = new JMenuItem(Messages.getString("FilesTreeView.15"),
        IconLoader.getIcon(JajukIcons.DIRECTORY_SYNCHRO));
    jmiDirResynchro.addActionListener(this);
    jmiDirCreatePlaylist = new JMenuItem(Messages.getString("FilesTreeView.16"));
    jmiDirCreatePlaylist.setEnabled(false);
    jmiDirCreatePlaylist.addActionListener(this);
    jmiDirRefactor = new JMenuItem(Messages.getString(("FilesTreeView.62")),
        IconLoader.getIcon(JajukIcons.REORGANIZE));
    jmiDirRefactor.addActionListener(this);
    jmiDirCopyURL = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiDirCopyURL.putClientProperty(Const.DETAIL_CONTENT, alSelected);
    jmiOpenExplorer = new JMenuItem(ActionManager.getAction(JajukActions.OPEN_EXPLORER));
    jmiOpenExplorer.putClientProperty(Const.DETAIL_CONTENT, alSelected);
    // Device menu
    jmiDevMount = new JMenuItem(Messages.getString("FilesTreeView.28"),
        IconLoader.getIcon(JajukIcons.UNMOUNT));
    jmiDevMount.addActionListener(this);
    jmiDevUnmount = new JMenuItem(Messages.getString("FilesTreeView.29"),
        IconLoader.getIcon(JajukIcons.UNMOUNT));
    jmiDevUnmount.addActionListener(this);
    jmiDevRefresh = new JMenuItem(Messages.getString("FilesTreeView.30"),
        IconLoader.getIcon(JajukIcons.REFRESH));
    jmiDevRefresh.addActionListener(this);
    jmiDevSynchronize = new JMenuItem(Messages.getString("FilesTreeView.31"),
        IconLoader.getIcon(JajukIcons.SYNCHRO));
    jmiDevSynchronize.addActionListener(this);
    jmiDevTest = new JMenuItem(Messages.getString("FilesTreeView.32"),
        IconLoader.getIcon(JajukIcons.TEST));
    jmiDevTest.addActionListener(this);
    jmiDevConfiguration = new JMenuItem(Messages.getString("FilesTreeView.55"),
        IconLoader.getIcon(JajukIcons.CONFIGURATION));
    jmiDevConfiguration.addActionListener(this);
    jmiDevDelete = new JMenuItem(Messages.getString("DeviceView.13"),
        IconLoader.getIcon(JajukIcons.DELETE));
    jmiDevDelete.addActionListener(this);
    jmiDevOrganize = new JMenuItem(Messages.getString(("FilesTreeView.62")),
        IconLoader.getIcon(JajukIcons.REORGANIZE));
    jmiDevOrganize.addActionListener(this);
    // playlist menu
    jmiPlaylistFileCopy = new JMenuItem(Messages.getString("FilesTreeView.40"));
    jmiPlaylistFileCopy.setEnabled(false);
    jmiPlaylistFileCopy.addActionListener(this);
    jmiPlaylistFileCut = new JMenuItem(Messages.getString("FilesTreeView.41"));
    jmiPlaylistFileCut.setEnabled(false);
    jmiPlaylistFileCut.addActionListener(this);
    jmiPlaylistFilePaste = new JMenuItem(Messages.getString("FilesTreeView.42"));
    jmiPlaylistFilePaste.setEnabled(false);
    jmiPlaylistFilePaste.addActionListener(this);
    jmiPlaylistCopyURL = new JMenuItem(ActionManager.getAction(JajukActions.COPY_TO_CLIPBOARD));
    jmiPlaylistCopyURL.putClientProperty(Const.DETAIL_CONTENT, alSelected);
    jmiPlaylistPrepareParty = new JMenuItem(ActionManager.getAction(JajukActions.PREPARE_PARTY));
    jmiPlaylistPrepareParty.putClientProperty(Const.DETAIL_SELECTION, alSelected);
    // Add Action Listener
    jmiCopy.addActionListener(this);
    jmiCut.addActionListener(this);
    jmiPaste.addActionListener(this);
    // By default disable paste
    jmiPaste.setEnabled(false);
    top = new TreeRootElement(Messages.getString("FilesTreeView.47"));
    // Register on the list for subject we are interested in
    ObservationManager.register(this);
    // fill the tree model
    populateTree();
    // create tree
    createTree(true);
    /**
     * CAUTION ! we register several listeners against this tree Swing can't
     * ensure the order where listeners will treat them so don't count in the
     * mouse listener to get correct selection from selection listener
     */
    jtree.setCellRenderer(new FilesTreeCellRenderer());
    // Tree selection listener to detect a selection (single click
    // , manages simple or multiple selections)
    jtree.addTreeSelectionListener(new FilesTreeSelectionListener());
    // Listen for single / double click
    jtree.addMouseListener(new FilesMouseAdapter());
    // Expansion analyzed to keep expended state
    jtree.addTreeExpansionListener(new FilesTreeExpansionListener());
    jtree.setAutoscrolls(true);
    jspTree = new JScrollPane(jtree);
    jspTree.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
    // DND support
    jtree.setDragEnabled(true);
    jtree.setTransferHandler(new TreeTransferHandler(jtree));
    // layout : the tree takes all the available height and we display the
    // command buttons on a different layer (because we don't want to use a
    // dedicated row like in the Tracks tree table : it's too ugly and
    // space-consuming)
    setLayout(new MigLayout("ins 3", "[grow]", "[grow]"));
    final JLayeredPane lp = new JLayeredPane();
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        // At first display and afterwards at each view resize, we cleanup and
        // re-add tree and command buttons adapted to the new view size.
        lp.removeAll();
        jspTree.setBounds(0, 0, getWidth() - 5, getHeight() - 5);
        lp.add(jspTree, JLayeredPane.DEFAULT_LAYER);
        jtbSync.setBounds(getWidth() - 80, 0, 25, 22);
        lp.add(jtbSync, JLayeredPane.POPUP_LAYER);
        jbCollapseAll.setBounds(getWidth() - 50, 0, 23, 22);
        lp.add(jbCollapseAll, JLayeredPane.POPUP_LAYER);
        lp.revalidate();
        lp.repaint();
      }
    });
    add(lp, "grow");
    // expand all
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
      top.removeAllChildren();
      // add all devices as "LazyLoading" nodes so all subsequent elements are
      // only populated if necessary
      List<Device> devices = DeviceManager.getInstance().getDevices();
      for (Device device : devices) {
        DefaultMutableTreeNode nodeDevice = new DeviceNode(device);
        top.add(nodeDevice);
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

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent e) {
    // multiple selection on properties(note we handle files and dirs
    // properties later)
    if ((e.getSource() == jmiDirRefactor || e.getSource() == jmiDevOrganize)) {
      UtilGUI.waiting();
      for (Item item : alSelected) {
        // Check if user made a global cancel
        if (RefactorAction.isStopAll()) {
          RefactorAction.resetStopAll();
          return;
        }
        // If user selected a device, take associated directory
        if (item instanceof Device) {
          item = ((Device) item).getRootDirectory();
        }
        final Directory dir = (Directory) item;
        UtilGUI.waiting();
        new RefactorAction(dir.getFilesRecursively());
      }
    } else if (e.getSource() == jmiDevMount) {
      for (TreePath element : paths) {
        DeviceNode node = (DeviceNode) (element.getLastPathComponent());
        Device device = node.getDevice();
        try {
          device.mount(true);
          jtree.expandPath(new TreePath(node.getPath()));
        } catch (JajukException je) {
          Messages.showErrorMessage(je.getCode());
        } catch (Exception ex) {
          Messages.showErrorMessage(11);
        }
      }
    } else if (e.getSource() == jmiDevUnmount) {
      for (TreePath element : paths) {
        DeviceNode node = (DeviceNode) (element.getLastPathComponent());
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
      // ask user if he wants to make deep
      // or fast scan
      device.refresh(true, true, false, null);
    } else if (e.getSource() == jmiDevSynchronize) {
      Device device = ((DeviceNode) (paths[0].getLastPathComponent())).getDevice();
      device.synchronize(true);
    } else if (e.getSource() == jmiDevTest) {
      new Thread("Files Tree Action Thread") {
        // test asynchronously in case of delay (samba
        // pbm for ie)
        @Override
        public void run() {
          Device device = ((DeviceNode) (paths[0].getLastPathComponent())).getDevice();
          if (device.test()) {
            Messages.showInfoMessage(Messages.getString("DeviceView.21"),
                IconLoader.getIcon(JajukIcons.OK));
          } else {
            Messages.showInfoMessage(Messages.getString("DeviceView.22"),
                IconLoader.getIcon(JajukIcons.KO));
          }
        }
      }.start();
    } else if (e.getSource() == jmiDirDesynchro) {
      setSynchonizationStateRecursively(false);
      jtree.revalidate();
      jtree.repaint();
    } else if (e.getSource() == jmiDirResynchro) {
      setSynchonizationStateRecursively(true);
      jtree.revalidate();
      jtree.repaint();
    } else if (e.getSource() == jmiCopy || e.getSource() == jmiCut) {
      jmiPaste.setEnabled(true);
      jmenu.repaint();
    } else if (e.getSource() == jmiPaste) {
      if (MoveActions.CUT.equals(ItemMoveManager.getInstance().getAction())) {
        jmiPaste.setEnabled(false);
        jmenu.repaint();
      }
    } else if (e.getSource() == jmiDevConfiguration) {
      Device device = ((DeviceNode) paths[0].getLastPathComponent()).getDevice();
      DeviceWizard dw = new DeviceWizard();
      dw.updateWidgets(device);
      dw.pack();
      dw.setVisible(true);
    } else if (e.getSource() == jmiDevDelete) {
      Device device = ((DeviceNode) paths[0].getLastPathComponent()).getDevice();
      DeviceManager.getInstance().removeDevice(device);
      // refresh views
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    }
  }

  /**
   * Sets the synchronization state recursively for all files selected.
   * 
   * @param sync whether the directories should be synchronized
   */
  private void setSynchonizationStateRecursively(boolean sync) {
    Set<Directory> directories = new HashSet<Directory>();
    for (Item item : alSelected) {
      Directory dir = (Directory) item;
      directories.add(dir);
      directories.addAll(dir.getDirectoriesRecursively());
    }
    for (Directory dir : directories) {
      dir.setProperty(Const.XML_DIRECTORY_SYNCHRONIZED, sync);
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
   * .
   */
  @Override
  void expand() {
    // make sure the main element is expanded
    jtree.expandRow(0);
    // begin by expanding all needed devices and directory, only after,
    // collapse unmounted devices if required
    for (int i = 0; i < jtree.getRowCount(); i++) {
      Object o = jtree.getPathForRow(i).getLastPathComponent();
      if (o instanceof DeviceNode) {
        Device device = ((DeviceNode) o).getDevice();
        if (device.getBooleanValue(Const.XML_EXPANDED)) {
          jtree.expandRow(i);
        }
        // Collapse node (useful to hide an live-unmounted device for ie)
        else {
          jtree.collapseRow(i);
        }
      } else if (o instanceof DirectoryNode) {
        Directory dir = ((DirectoryNode) o).getDirectory();
        if (dir.getBooleanValue(Const.XML_EXPANDED)) {
          jtree.expandRow(i);
        }
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
    // Make sure item is a file (may be webradio)
    if (FileManager.getInstance().getFileByID(item.getID()) == null) {
      return;
    }
    // Set manual change because we force here tree selection and
    // we don't want to force table views to synchronize
    bInternalAction = true;
    try {
      // Clear selection so we only select new synchronized item
      jtree.getSelectionModel().clearSelection();
      // Expand recursively item's directory because of the lazy loading stuff
      expandRecursively(item);
      // Now scroll to the item and select it
      for (int i = 0; i < jtree.getRowCount(); i++) {
        Object o = jtree.getPathForRow(i).getLastPathComponent();
        if (o instanceof FileNode) {
          o = ((FileNode) o).getFile();
        } else if (o instanceof PlaylistFileNode) {
          o = ((PlaylistFileNode) o).getPlaylistFile();
        } else {
          continue;
        }
        if (item.equals(o)) {
          jtree.scrollRowToVisible(i);
          jtree.getSelectionModel().addSelectionPath(jtree.getPathForRow(i));
        }
      }
    } finally {
      bInternalAction = false;
    }
  }

  /**
   * Expand recursively all directory nodes of given item.
   * 
   * @param item : file or playlist
   */
  private void expandRecursively(Item item) {
    jtree.expandRow(0);
    boolean stopLoop = false;
    // Keep tree path list here, do not put this call in the loop as
    // it would change at each node expand
    List<TreePath> paths = new ArrayList<TreePath>();
    for (int i = 0; i < jtree.getRowCount(); i++) {
      TreePath path = jtree.getPathForRow(i);
      paths.add(path);
    }
    // item is either a file or a playlist
    for (int i = 0; i < paths.size(); i++) {
      Object o = paths.get(i).getLastPathComponent();
      if (o instanceof DirectoryNode || o instanceof DeviceNode) {
        Directory testedDirectory = null;
        // If the node is a device, search its root directory and check it
        if (o instanceof DeviceNode) {
          Device testedDevice = ((DeviceNode) o).getDevice();
          testedDirectory = testedDevice.getRootDirectory();
        } else {
          testedDirectory = ((DirectoryNode) o).getDirectory();
        }
        if (item instanceof File) {
          File file = (File) item;
          if (file.hasAncestor(testedDirectory)) {
            jtree.expandRow(i);
          }
          if (testedDirectory.equals(file.getDirectory())) {
            stopLoop = true;
          }
        } else if (item instanceof Playlist) {
          Playlist playlist = (Playlist) item;
          if (playlist.hasAncestor(testedDirectory)) {
            jtree.expandRow(i);
          }
          if (testedDirectory.equals(playlist.getDirectory())) {
            stopLoop = true;
          }
        }
      }
    }
    if (!stopLoop) {
      expandRecursively(item);
    }
  }

  /**
   * .
   */
  private class FilesMouseAdapter extends JajukMouseAdapter {
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.jajuk.ui.helpers.JajukMouseAdapter#handleActionSeveralClicks(java.awt.event.MouseEvent)
     */
    @Override
    public void handleActionSeveralClicks(final MouseEvent e) {
      TreePath path = jtree.getPathForLocation(e.getX(), e.getY());
      if (path == null) {
        return;
      }
      Object o = path.getLastPathComponent();
      if (o instanceof FileNode) {
        File file = ((FileNode) o).getFile();
        try {
          QueueModel.push(new StackItem(file, Conf.getBoolean(Const.CONF_STATE_REPEAT), true),
              Conf.getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
        } catch (JajukException je) {
          Log.error(je);
        }
      }
      // double click on a playlist
      else if (o instanceof PlaylistFileNode) {
        Playlist plf = ((PlaylistFileNode) o).getPlaylistFile();
        List<File> alToPlay = null;
        try {
          alToPlay = plf.getFiles();
        } catch (JajukException je) {
          Log.error(je.getCode(), "{{" + plf.getName() + "}}", null);
          Messages.showErrorMessage(je.getCode(), plf.getName());
          return;
        }
        // check playlist contains accessible
        // tracks
        if (alToPlay == null || alToPlay.size() == 0) {
          Messages.showErrorMessage(18);
          return;
        } else {
          QueueModel.push(
              UtilFeatures.createStackItems(UtilFeatures.applyPlayOption(alToPlay),
                  Conf.getBoolean(Const.CONF_STATE_REPEAT), true), false);
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jajuk.ui.helpers.JajukMouseAdapter#handlePopup(java.awt.event.MouseEvent)
     */
    @Override
    @SuppressWarnings("unchecked")
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
        for (TreePath element : paths) {
          Directory dir = ((DirectoryNode) (element.getLastPathComponent())).getDirectory();
          if (!dir.getDevice().isMounted()) {
            continue;
          }
        }
      }
      if (c.equals(FileNode.class)) {
        for (TreePath element : paths) {
          File file = ((FileNode) (element.getLastPathComponent())).getFile();
          if (!file.isReady()) {
            continue;
          }
        }
      }
      jmiDelete.setEnabled(true);
      if (c.equals(PlaylistFileNode.class)) {
        for (TreePath element : paths) {
          Playlist plf = ((PlaylistFileNode) (element.getLastPathComponent())).getPlaylistFile();
          if (!plf.isReady()) {
            jmiDelete.setEnabled(false);
            continue;
          }
        }
      }
      // get all components recursively
      for (TreePath element : paths) {
        Object o = element.getLastPathComponent();
        // return all childs nodes recursively
        Enumeration<DefaultMutableTreeNode> e2 = ((DefaultMutableTreeNode) o)
            .depthFirstEnumeration();
        while (e2.hasMoreElements()) {
          DefaultMutableTreeNode node = e2.nextElement();
          if (node instanceof DirectoryNode) {
            Directory dir = ((DirectoryNode) node).getDirectory();
            alDirs.add(dir);
          }
        }
      }
      // display menus according node type
      if (paths[0].getLastPathComponent() instanceof FileNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.addSeparator();
        jmenu.add(jmiCut);
        jmenu.add(jmiCopy);
        jmenu.add(jmiRename);
        jmenu.add(jmiDelete);
        jmenu.add(jmiCopyURL);
        jmenu.add(jmiOpenExplorer);
        jmenu.addSeparator();
        jmenu.add(pjmTracks);
        jmenu.add(jmiAddFavorite);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        jmenu.show(jtree, e.getX(), e.getY());
      } else if (paths[0].getLastPathComponent() instanceof DirectoryNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.add(jmiPlayShuffle);
        jmenu.add(jmiPlayRepeat);
        jmenu.addSeparator();
        jmenu.add(jmiCut);
        jmenu.add(jmiCopy);
        jmenu.add(jmiPaste);
        jmenu.add(jmiNewFolder);
        jmenu.add(jmiDelete);
        jmenu.add(jmiDirCopyURL);
        jmenu.add(jmiOpenExplorer);
        jmenu.addSeparator();
        jmenu.add(jmiDirRefresh);
        jmenu.add(jmiRename);
        jmenu.add(jmiDirDesynchro);
        jmenu.add(jmiDirResynchro);
        jmenu.addSeparator();
        jmenu.add(jmiCDDBWizard);
        jmenu.add(jmiReport);
        jmenu.add(jmiDirRefactor);
        jmenu.addSeparator();
        jmenu.add(pjmTracks);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        jmenu.show(jtree, e.getX(), e.getY());
      } else if (paths[0].getLastPathComponent() instanceof PlaylistFileNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.add(jmiPlayShuffle);
        jmenu.add(jmiPlayRepeat);
        jmenu.addSeparator();
        jmenu.add(jmiPlaylistCopyURL);
        jmenu.add(jmiPlaylistPrepareParty);
        jmenu.add(jmiOpenExplorer);
        jmenu.add(jmiDelete);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        jmenu.show(jtree, e.getX(), e.getY());
      } else if (paths[0].getLastPathComponent() instanceof DeviceNode) {
        jmenu = new JPopupMenu();
        jmenu.add(jmiPlay);
        jmenu.add(jmiFrontPush);
        jmenu.add(jmiPush);
        jmenu.add(jmiPaste);
        jmenu.add(jmiPlayShuffle);
        jmenu.add(jmiPlayRepeat);
        jmenu.addSeparator();
        jmenu.add(jmiNewFolder);
        jmenu.add(jmiDevMount);
        jmenu.add(jmiDevUnmount);
        jmenu.add(jmiDevRefresh);
        jmenu.add(jmiDevSynchronize);
        jmenu.addSeparator();
        jmenu.add(jmiDevTest);
        jmenu.add(jmiCDDBWizard);
        jmenu.add(jmiDevOrganize);
        jmenu.add(jmiReport);
        jmenu.addSeparator();
        jmenu.add(jmiDevDelete);
        jmenu.add(jmiDevConfiguration);
        jmenu.addSeparator();
        jmenu.add(jmiProperties);
        Device device = ((DeviceNode) paths[0].getLastPathComponent()).getDevice();
        // if the device is not synchronized
        if (device.getValue(Const.XML_DEVICE_SYNCHRO_SOURCE).equals("")) {
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
        // Collection menu
        JPopupMenu jmenuCollection = new JPopupMenu();
        // Export
        Action actionReportCollection = ActionManager.getAction(JajukActions.CREATE_REPORT);
        JMenuItem jmiCollectionReport = new JMenuItem(actionReportCollection);
        // Add custom data to this component in order to allow the ReportAction
        // to be able to get it
        jmiCollectionReport.putClientProperty(Const.DETAIL_ORIGIN, COLLECTION_PHYSICAL);
        jmenuCollection.add(jmiCollectionReport);
        Action actionDuplicateFiles = ActionManager.getAction(JajukActions.FIND_DUPLICATE_FILES);
        JMenuItem jmiCollectionDuplicateFiles = new JMenuItem(actionDuplicateFiles);
        jmenuCollection.add(jmiCollectionDuplicateFiles);
        // collection
        jmenuCollection.show(jtree, e.getX(), e.getY());
      }
    }
  }

  /**
   * .
   */
  private class FilesTreeSelectionListener implements TreeSelectionListener {
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event
     * .TreeSelectionEvent)
     */
    @Override
    public void valueChanged(TreeSelectionEvent e) {
      paths = jtree.getSelectionModel().getSelectionPaths();
      // nothing selected, can be called during dnd
      if (paths == null || paths.length == 0) {
        return;
      }
      int items = 0;
      long lSize = 0;
      // get all components recursively
      selectedRecursively.clear();
      alSelected.clear();
      // Treat case when use selected the tree's root
      Object firstPath = paths[0].getLastPathComponent();
      if (firstPath instanceof TreeRootElement) {
        selectedRecursively.addAll(FileManager.getInstance().getFiles());
      } else {
        // Regular selection, one or more nodes
        for (TreePath element : paths) {
          // return all child nodes recursively, do not count on the tree's
          // model as it is lazy loaded
          Object o = element.getLastPathComponent();
          Item item = (Item) ((DefaultMutableTreeNode) o).getUserObject();
          alSelected.add(item);
          Directory directory = null;
          if (o instanceof DeviceNode) {
            directory = ((DeviceNode) o).getDevice().getRootDirectory();
          } else if (o instanceof DirectoryNode) {
            directory = ((DirectoryNode) o).getDirectory();
          }
          if (directory != null) {
            selectedRecursively.addAll(directory.getFilesRecursively());
            selectedRecursively.addAll(directory.getPlaylistFiles());
          } else if (o instanceof FileNode) {
            selectedRecursively.add(((FileNode) o).getFile());
          } else if (o instanceof PlaylistFileNode) {
            selectedRecursively.add(((PlaylistFileNode) o).getPlaylistFile());
          }
        }
      }
      // Compute recursive selection size, nb of items...
      for (Item item : selectedRecursively) {
        if (item instanceof File) {
          lSize += ((File) item).getSize();
        }
      }
      items = selectedRecursively.size();
      lSize /= 1048576; // set size in MB
      StringBuilder sbOut = new StringBuilder().append(items).append(
          Messages.getString("FilesTreeView.52"));
      if (lSize > 1024) { // more than 1024 MB -> in GB
        sbOut.append(lSize / 1024).append('.').append(lSize % 1024)
            .append(Messages.getString("FilesTreeView.53"));
      } else {
        sbOut.append(lSize).append(Messages.getString("FilesTreeView.54"));
      }
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
      // Enable CDDB retagging only for a single directory selection
      jmiCDDBWizard.setEnabled(alSelected.size() == 1 && alSelected.get(0) instanceof Directory);
      // Enable device refresh for a single item
      jmiDevRefresh.setEnabled(alSelected.size() == 1 && alSelected.get(0) instanceof Device);
      // Enable Copy url for a single item only
      jmiCopyURL.setEnabled(alSelected.size() == 1 && alSelected.get(0) instanceof File);
      jmiDirCopyURL.setEnabled(alSelected.size() == 1 && alSelected.get(0) instanceof Directory);
      jmiOpenExplorer
          .setEnabled(alSelected.size() == 1
              && (alSelected.get(0) instanceof Directory || alSelected.get(0) instanceof File || alSelected
                  .get(0) instanceof Playlist));
      jmiPlaylistCopyURL
          .setEnabled(alSelected.size() == 1 && alSelected.get(0) instanceof Playlist);
      jmiPlaylistPrepareParty.setEnabled(alSelected.size() == 1
          && alSelected.get(0) instanceof Playlist);
      // Update preference menu
      pjmTracks.resetUI(alSelected);
    }
  }

  /**
   * .
   */
  private class FilesTreeExpansionListener implements TreeExpansionListener {
    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.TreeExpansionListener#treeCollapsed(javax.swing.event
     * .TreeExpansionEvent)
     */
    @Override
    public void treeCollapsed(TreeExpansionEvent event) {
      Object o = event.getPath().getLastPathComponent();
      if (o instanceof DirectoryNode && bManualAction) {
        Directory dir = ((DirectoryNode) o).getDirectory();
        dir.removeProperty(Const.XML_EXPANDED);
      } else if (o instanceof DeviceNode && bManualAction) {
        Device device = ((DeviceNode) o).getDevice();
        device.removeProperty(Const.XML_EXPANDED);
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.TreeExpansionListener#treeExpanded(javax.swing.event
     * .TreeExpansionEvent)
     */
    @Override
    public void treeExpanded(TreeExpansionEvent event) {
      Object o = event.getPath().getLastPathComponent();
      if (o instanceof DirectoryNode && bManualAction) {
        Directory dir = ((DirectoryNode) o).getDirectory();
        dir.setProperty(Const.XML_EXPANDED, true);
      } else if (o instanceof DeviceNode && bManualAction) {
        Device device = ((DeviceNode) o).getDevice();
        device.setProperty(Const.XML_EXPANDED, true);
      }
    }
  }

  /**
   * Fill the provided list with sub-elements for that directory, i.e.
   * sub-directories, files and playlists.
   * 
   * @param parent The parent-directory to start from.
   * @param list The list to add elements to. This list can contain elements before
   * which will not be touched.
   */
  static void populateFromDirectory(Directory parent, List<MutableTreeNode> list) {
    // now we get all directories in this device
    for (Directory directory : parent.getDirectories()) {
      if (directory.shouldBeHidden()) {
        continue;
      }
      list.add(new DirectoryNode(directory));
    }
    // then files
    for (File file : parent.getFiles()) {
      if (file.shouldBeHidden()) {
        continue;
      }
      list.add(new FileNode(file));
    }
    // and playlists
    for (Playlist pl : parent.getPlaylistFiles()) {
      if (pl.shouldBeHidden()) {
        continue;
      }
      list.add(new PlaylistFileNode(pl));
    }
  }
}

/**
 * File node
 */
class FileNode extends DefaultMutableTreeNode {
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
    return getFile().getName();
  }

  /**
   * @return Returns the file.
   */
  public File getFile() {
    return (File) super.getUserObject();
  }
}

/**
 * Device node
 */
class DeviceNode extends LazyLoadingTreeNode {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param device
   */
  public DeviceNode(Device device) {
    super(device);
  }

  /**
   * return a string representation of this device node
   */
  @Override
  public String toString() {
    return getDevice().getName();
  }

  /**
   * @return Returns the device.
   */
  public Device getDevice() {
    return (Device) super.getUserObject();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.widgets.LazyLoadingTreeNode#loadChildren(javax.swing.tree. DefaultTreeModel)
   */
  @Override
  public MutableTreeNode[] loadChildren(DefaultTreeModel model) {
    List<MutableTreeNode> list = new ArrayList<MutableTreeNode>();
    // first level is the directory of the device itself, usually only one
    for (Directory parent : getDevice().getDirectories()) {
      // so for each directory that is listed for that Device we build up the
      // list of sub-elements
      FilesTreeView.populateFromDirectory(parent, list);
    }
    return list.toArray(new MutableTreeNode[list.size()]);
  }
}

/**
 * Directory node
 */
class DirectoryNode extends LazyLoadingTreeNode {
  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   * 
   * @param Directory
   */
  public DirectoryNode(Directory directory) {
    super(directory);
  }

  /**
   * return a string representation of this directory node
   */
  @Override
  public String toString() {
    return getDirectory().getName();
  }

  /**
   * @return Returns the directory.
   */
  public Directory getDirectory() {
    return (Directory) getUserObject();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.helpers.LazyLoadingTreeNode#loadChildren(javax.swing.tree .DefaultTreeModel)
   */
  @Override
  public MutableTreeNode[] loadChildren(DefaultTreeModel model) {
    List<MutableTreeNode> list = new ArrayList<MutableTreeNode>();
    // simply collect all items one level below that directory
    FilesTreeView.populateFromDirectory(getDirectory(), list);
    return list.toArray(new MutableTreeNode[list.size()]);
  }
}

/**
 * Playlist node
 */
class PlaylistFileNode extends DefaultMutableTreeNode {
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
    return getPlaylistFile().getName();
  }

  /**
   * @return Returns the playlist node.
   */
  public Playlist getPlaylistFile() {
    return (Playlist) getUserObject();
  }
}

class FilesTreeCellRenderer extends SubstanceDefaultTreeCellRenderer {
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
      String ext = UtilSystem.getExtension(file.getName());
      Type type = TypeManager.getInstance().getTypeByExtension(ext);
      // Find associated icon with this type
      URL icon = null;
      String sIcon;
      if (type != null) {
        sIcon = (String) type.getValue(Const.XML_TYPE_ICON);
        try {
          icon = new URL(sIcon);
        } catch (MalformedURLException e) {
          Log.error(e);
        }
      }
      if (icon == null) {
        setIcon(IconLoader.getIcon(JajukIcons.TYPE_WAV));
      } else {
        setIcon(new ImageIcon(icon));
      }
    } else if (value instanceof PlaylistFileNode) {
      setBorder(null);
      setIcon(IconLoader.getIcon(JajukIcons.PLAYLIST_FILE));
    } else if (value instanceof DeviceNode) {
      setBorder(BorderFactory.createEmptyBorder(2, 0, 3, 0));
      Device device = ((DeviceNode) value).getDevice();
      ImageIcon deviceIconSmall = device.getIconRepresentation();
      setIcon(deviceIconSmall);
    } else if (value instanceof DirectoryNode) {
      setBorder(null);
      Directory dir = ((DirectoryNode) value).getDirectory();
      boolean bSynchro = dir.getBooleanValue(Const.XML_DIRECTORY_SYNCHRONIZED);
      if (bSynchro) { // means this device is not synchronized
        setIcon(IconLoader.getIcon(JajukIcons.DIRECTORY_SYNCHRO));
      } else {
        setIcon(IconLoader.getIcon(JajukIcons.DIRECTORY_DESYNCHRO));
      }
      // collection node
    } else {
      setIcon(IconLoader.getIcon(JajukIcons.LIST));
    }
    return this;
  }
}
