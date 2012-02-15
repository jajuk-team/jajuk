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

package org.jajuk.ui.views;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import net.miginfocom.swing.MigLayout;

import org.apache.commons.lang.StringUtils;
import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Directory;
import org.jajuk.base.Track;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.covers.Cover;
import org.jajuk.services.covers.Cover.CoverType;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.tags.Tag;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.widgets.JajukJToolbar;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.GIFFilter;
import org.jajuk.util.filters.ImageFilter;
import org.jajuk.util.filters.JPGFilter;
import org.jajuk.util.filters.PNGFilter;
import org.jajuk.util.log.Log;

/**
 * Cover view. Displays an image for the current album
 */
public class CoverView extends ViewAdapter implements ActionListener {

  /** The Constant PLUS_QUOTE.  DOCUMENT_ME */
  private static final String PLUS_QUOTE = "+\"";

  /** The Constant QUOTE_BLANK.  DOCUMENT_ME */
  private static final String QUOTE_BLANK = "\" ";

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** No cover cover. */
  private static Cover nocover = new Cover(Const.IMAGES_SPLASHSCREEN, CoverType.NO_COVER);

  /** Error counter to check connection availability. */
  private static int iErrorCounter = 0;

  /** Connected one flag : true if jajuk managed once to connect to the web to bring covers. */
  private static boolean bOnceConnected = false;

  /** Reference File for cover. */
  private org.jajuk.base.File fileReference;

  /** File directory used as a cache for perfs. */
  private Directory dirReference;

  /** List of available covers for the current file. */
  private final LinkedList<Cover> alCovers = new LinkedList<Cover>(); // NOPMD

  // control panel
  /** DOCUMENT_ME. */
  private JPanel jpControl;

  /** DOCUMENT_ME. */
  private JajukButton jbPrevious;

  /** DOCUMENT_ME. */
  private JajukButton jbNext;

  /** DOCUMENT_ME. */
  private JajukButton jbDelete;

  /** DOCUMENT_ME. */
  private JajukButton jbSave;

  /** DOCUMENT_ME. */
  private JajukButton jbDefault;

  /** DOCUMENT_ME. */
  private JLabel jlSize;

  /** DOCUMENT_ME. */
  private JLabel jlFound;

  /** DOCUMENT_ME. */
  private JLabel jlSearching;

  /** Cover search accuracy combo. */
  private JComboBox jcbAccuracy;

  /** Date last resize (used for adjustment management). */
  private long lDateLastResize;

  /** URL and size of the image. */
  private JLabel jl;

  /** Used Cover index. */
  private int index = 0;

  /** Event ID., it should be volatile because this mutable field can be set by different threads */
  private volatile int iEventID = 0;//NOSONAR

  /** Flag telling that user wants to display a better cover. */
  private boolean bGotoBetter = false;

  /** Final image to display. */
  private ImageIcon ii;

  /** Force next track cover reload flag*. */
  private boolean bForceCoverReload = true;

  /** DOCUMENT_ME. */
  private boolean includeControls;

  /** Whether the view has not yet been displayed for its first time */
  private boolean initEvent = true;

  /** Thread launch at view init to reset its state */
  private class CoverResetThread extends Thread {
    @Override
    public void run() {
      if (fileReference == null) { // regular cover view
        if (QueueModel.isStopped()) {
          update(new JajukEvent(JajukEvents.ZERO));
        }
        // check if a track has already been launched
        else if (QueueModel.isPlayingRadio()) {
          update(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED,
              ObservationManager.getDetailsLastOccurence(JajukEvents.WEBRADIO_LAUNCHED)));
          // If the view is displayed for the first time, a ComponentResized event is launched at its first display but
          // we want to perform the full process : update past launches files (FILE_LAUNCHED). 
          // But if it is no more the initial resize event, we only want to refresh the cover, not the full story.
        } else if (!initEvent) {
          displayCurrentCover();
        } else {
          update(new JajukEvent(JajukEvents.FILE_LAUNCHED));
        }
      }
      else { // cover view used as dialog
        update(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
      }
      // It will never more be the first time ...
      CoverView.this.initEvent = false;
    }
  }

  /**
   * Constructor.
   */
  public CoverView() {
    super();
  }

  /**
   * Constructor.
   * 
   * @param file Reference file. Used to display cover for a particular file, null if the cover view is used in the "reular" way as a view, not 
   * as a dialog from catalog view for ie.
   */
  public CoverView(final org.jajuk.base.File file) {
    super();
    fileReference = file;
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.views.IView#initUI()
   */
  @Override
  public void initUI() {
    initUI(true);
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.IView#display()
   */
  /**
   * Inits the ui.
   *  
   * @param includeControls DOCUMENT_ME
   */
  public void initUI(boolean includeControls) {
    this.includeControls = includeControls;

    // Control panel
    jlSearching = new JLabel("", IconLoader.getIcon(JajukIcons.NET_SEARCH), SwingConstants.CENTER);
    jpControl = new JPanel();
    if (includeControls) {
      jpControl.setBorder(BorderFactory.createEtchedBorder());
    }

    final JToolBar jtb = new JajukJToolbar();
    jbPrevious = new JajukButton(IconLoader.getIcon(JajukIcons.PLAYER_PREVIOUS_SMALL));
    jbPrevious.addActionListener(this);
    jbPrevious.setToolTipText(Messages.getString("CoverView.4"));
    jbNext = new JajukButton(IconLoader.getIcon(JajukIcons.PLAYER_NEXT_SMALL));
    jbNext.addActionListener(this);
    jbNext.setToolTipText(Messages.getString("CoverView.5"));
    jbDelete = new JajukButton(IconLoader.getIcon(JajukIcons.DELETE));
    jbDelete.addActionListener(this);
    jbDelete.setToolTipText(Messages.getString("CoverView.2"));
    jbSave = new JajukButton(IconLoader.getIcon(JajukIcons.SAVE));
    jbSave.addActionListener(this);
    jbSave.setToolTipText(Messages.getString("CoverView.6"));
    jbDefault = new JajukButton(IconLoader.getIcon(JajukIcons.DEFAULT_COVER));
    jbDefault.addActionListener(this);
    jbDefault.setToolTipText(Messages.getString("CoverView.8"));
    jlSize = new JLabel("");
    jlFound = new JLabel("");
    jcbAccuracy = new JComboBox();
    // Add tooltips on combo items
    jcbAccuracy.setRenderer(new BasicComboBoxRenderer() {
      private static final long serialVersionUID = -6943363556191659895L;

      @Override
      public Component getListCellRendererComponent(final JList list, final Object value,
          final int index, final boolean isSelected, final boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        switch (index) {
        case 0:
          setToolTipText(Messages.getString("ParameterView.156"));
          break;
        case 1:
          setToolTipText(Messages.getString("ParameterView.157"));
          break;
        case 2:
          setToolTipText(Messages.getString("ParameterView.158"));
          break;
        case 3:
          setToolTipText(Messages.getString("ParameterView.216"));
          break;
        case 4:
          setToolTipText(Messages.getString("ParameterView.217"));
          break;
        case 5:
          setToolTipText(Messages.getString("ParameterView.218"));
          break;
        }
        setBorder(new EmptyBorder(0, 3, 0, 3));
        return this;
      }
    });
    jcbAccuracy.setToolTipText(Messages.getString("ParameterView.155"));

    jcbAccuracy.addItem(IconLoader.getIcon(JajukIcons.ACCURACY_LOW));
    jcbAccuracy.addItem(IconLoader.getIcon(JajukIcons.ACCURACY_MEDIUM));
    jcbAccuracy.addItem(IconLoader.getIcon(JajukIcons.ACCURACY_HIGH));
    jcbAccuracy.addItem(IconLoader.getIcon(JajukIcons.ARTIST));
    jcbAccuracy.addItem(IconLoader.getIcon(JajukIcons.ALBUM));
    jcbAccuracy.addItem(IconLoader.getIcon(JajukIcons.TRACK));

    int i = 1; // medium accuracy
    try {
      i = Conf.getInt(Const.CONF_COVERS_ACCURACY + "_"
          + ((getPerspective() == null) ? "popup" : getPerspective().getID()));
    } catch (final NumberFormatException e) {
      // Will reach this point at first launch
    }
    jcbAccuracy.setSelectedIndex(i);
    jcbAccuracy.addActionListener(this);

    jtb.add(jbPrevious);
    jtb.add(jbNext);
    jtb.addSeparator();
    jtb.add(jbDelete);
    jtb.add(jbSave);
    jtb.add(jbDefault);

    if (includeControls) {
      jpControl.setLayout(new MigLayout("insets 5 2 5 2", "[][grow][grow][][25]"));
      jpControl.add(jtb);
      jpControl.add(jlSize, "center,gapright 5::");
      jpControl.add(jlFound, "center,gapright 5::");
      jpControl.add(jcbAccuracy, "grow,width 47!,gapright 5");
      jpControl.add(jlSearching);
    }

    // Cover view used in catalog view should not listen events
    if (fileReference == null) {
      ObservationManager.register(this);
    }
    // global layout
    MigLayout globalLayout = null;
    if (includeControls) {
      globalLayout = new MigLayout("ins 0,gapy 10", "[grow]", "[30!][grow]");
    } else {
      globalLayout = new MigLayout("ins 0,gapy 10", "[grow]", "[grow]");
    }
    setLayout(globalLayout);
    add(jpControl, "grow,wrap");

    // listen for resize. We do it here to avoid a useless resize event at
    // init and an associated blinking effect
    addComponentListener(CoverView.this);

  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jcbAccuracy) {
      handleAccuracy();
    } else if (e.getSource() == jbPrevious) { // previous : show a
      handlePrevious();
    } else if (e.getSource() == jbNext) { // next : show a worse cover
      handleNext();
    } else if (e.getSource() == jbDelete) { // delete a local cover
      handleDelete();
    } else if (e.getSource() == jbDefault) {
      handleDefault();
    } else if ((e.getSource() == jbSave)
        && ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
      // save a file as... (can be local now)
      handleSaveAs();
    } else if (e.getSource() == jbSave) {
      handleSave();
    }
  }

  /**
   * Stores accuracy.
   */
  private void handleAccuracy() {
    // Note that we have to store/retrieve accuracy using an id. When
    // this view is used from a popup, we can't use perspective id
    Conf.setProperty(Const.CONF_COVERS_ACCURACY + "_"
        + ((getPerspective() == null) ? "popup" : getPerspective().getID()),
        Integer.toString(jcbAccuracy.getSelectedIndex()));

    new Thread("Cover Accuracy Thread") {
      @Override
      public void run() {
        // force refresh
        if (getPerspective() == null) {
          dirReference = null;
        }
        update(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
      }
    }.start();
  }

  /**
   * Called on the previous cover button event.
   */
  private void handlePrevious() {
    // better cover
    bGotoBetter = true;
    index++;
    if (index > alCovers.size() - 1) {
      index = 0;
    }
    displayCurrentCover();
    bGotoBetter = false; // make sure default behavior is to go
    // to worse covers
  }

  /**
   * Called on the next cover button event.
   */
  private void handleNext() {
    bGotoBetter = false;
    index--;
    if (index < 0) {
      index = alCovers.size() - 1;
    }
    displayCurrentCover();
  }

  /**
   * Called on the delete cover button event.
   */
  private void handleDelete() {
    // sanity check
    if (index >= alCovers.size()) {
      Log.warn("Cannot delete cover that is not available.");
      return;
    }
    if (index < 0) {
      Log.warn("Cannot delete cover with invalid index.");
      return;
    }

    // get the cover at the specified position
    final Cover cover = alCovers.get(index);

    // don't delete the splashscreen-jpg!!
    if (cover.getType().equals(CoverType.NO_COVER)) {
      Log.warn("Cannot delete default Jajuk cover.");
      return;
    }

    // show confirmation message if required
    if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_DELETE_COVER)) {
      final int iResu = Messages.getChoice(Messages.getString("Confirmation_delete_cover") + " : "
          + cover.getFile(), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        return;
      }
    }

    // yet there? ok, delete the cover
    try {
      final File file = cover.getFile();
      if (file.isFile() && file.exists()) {
        UtilSystem.deleteFile(file);
      } else { // not a file, must have a problem
        throw new Exception("Encountered file which either is not a file or does not exist: "
            + file);
      }
    } catch (final Exception ioe) {
      Log.error(131, ioe);
      Messages.showErrorMessage(131);
      return;
    }

    // If this was the absolute cover, remove the reference in the
    // collection
    if (cover.getType() == CoverType.SELECTED_COVER) {
      dirReference.removeProperty("default_cover");
    }

    // reorganize covers
    synchronized (this) {
      alCovers.remove(index);
      index--;
      if (index < 0) {
        index = alCovers.size() - 1;
      }
      ObservationManager.notify(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
      if (fileReference != null) {
        update(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
      }
    }
  }

  /**
   * Called when saving a cover.
   */
  private void handleSave() {
    // sanity check
    if (index >= alCovers.size()) {
      Log.warn("Cannot save cover that is not available.");
      return;
    }
    if (index < 0) {
      Log.warn("Cannot save cover with invalid index.");
      return;
    }

    // save a file with its original name
    new Thread("Cover Save Thread") {
      @Override
      public void run() {
        final Cover cover = alCovers.get(index);
        // should not happen, only remote covers here
        if (cover.getType() != CoverType.REMOTE_COVER) {
          Log.debug("Try to save a local cover");
          return;
        }
        String sFilePath = null;
        sFilePath = dirReference.getFio().getPath() + "/"
            + UtilSystem.getOnlyFile(cover.getURL().toString());
        sFilePath = convertCoverPath(sFilePath);
        try {
          // copy file from cache
          final File fSource = DownloadManager.downloadToCache(cover.getURL());
          final File file = new File(sFilePath);
          UtilSystem.copy(fSource, file);
          InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),
              InformationJPanel.MessageType.INFORMATIVE);
          final Cover cover2 = new Cover(file, CoverType.SELECTED_COVER);
          if (!alCovers.contains(cover2)) {
            alCovers.add(cover2);
            setFoundText();
          }

          // Reset cached cover in associated albums to make sure that new covers
          // will be discovered in various views like Catalog View.
          resetCachedCover();

          // Notify cover change
          ObservationManager.notify(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
          // add new cover in others cover views
        } catch (final Exception ex) {
          Log.error(24, ex);
          Messages.showErrorMessage(24);
        }
      }
    }.start();
  }

  /**
   * Reset cached cover in associated albums to make sure that new covers
   *  will be discovered in various views like Catalog View.
   */
  private void resetCachedCover() {
    org.jajuk.base.File fCurrent = fileReference;
    if (fCurrent == null) {
      fCurrent = QueueModel.getPlayingFile();
    }
    Set<Album> albums = fCurrent.getDirectory().getAlbums();
    // If we cached NO_COVER for this album, make sure to reset this value
    for (Album album : albums) {
      String cachedCoverPath = album.getStringValue(XML_ALBUM_DISCOVERED_COVER);
      if (COVER_NONE.equals(cachedCoverPath)) {
        album.setProperty(XML_ALBUM_DISCOVERED_COVER, "");
      }
      ObservationManager.notify(new JajukEvent(JajukEvents.COVER_DEFAULT_CHANGED));
    }
  }

  /**
   * Converts a cover path according to options and jajuk conventions.
   *
   * @param sFilePath current cover path
   * @return the converted cover file path
   */
  private String convertCoverPath(String sFilePath) {
    int pos = sFilePath.lastIndexOf('.');
    if (Conf.getBoolean(Const.CONF_COVERS_SAVE_EXPLORER_FRIENDLY)) {
      // Covers should be stored as folder.xxx for windows explorer
      final String ext;
      if (pos == -1) {
        ext = "";
      } else {
        ext = sFilePath.substring(pos, sFilePath.length());
      }
      String parent = new File(sFilePath).getParent();
      return parent + System.getProperty("file.separator") + "Folder" + ext;
    } else {
      if (pos == -1) {
        return sFilePath + Const.FILE_JAJUK_DOWNLOADED_FILES_SUFFIX;
      }

      // Add a jajuk suffix to know this cover has been downloaded by jajuk
      return new StringBuilder(sFilePath).insert(pos, Const.FILE_JAJUK_DOWNLOADED_FILES_SUFFIX)
          .toString();
    }
  }

  /**
   * Called when saving as a cover.
   */
  private void handleSaveAs() {
    // sanity check
    if (index >= alCovers.size()) {
      Log.warn("Cannot save cover that is not available.");
      return;
    }
    if (index < 0) {
      Log.warn("Cannot save cover with invalid index.");
      return;
    }

    new Thread("Cover SaveAs Thread") {
      @Override
      public void run() {
        final Cover cover = alCovers.get(index);
        final JajukFileChooser jfchooser = new JajukFileChooser(new JajukFileFilter(
            GIFFilter.getInstance(), PNGFilter.getInstance(), JPGFilter.getInstance()));
        jfchooser.setAcceptDirectories(true);
        jfchooser.setCurrentDirectory(dirReference.getFio());
        jfchooser.setDialogTitle(Messages.getString("CoverView.10"));
        final File finalFile = new File(dirReference.getFio().getPath() + "/"
            + UtilSystem.getOnlyFile(cover.getURL().toString()));
        jfchooser.setSelectedFile(finalFile);
        final int returnVal = jfchooser.showSaveDialog(JajukMainWindow.getInstance());
        File fNew = null;
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          fNew = jfchooser.getSelectedFile();
          // if user try to save as without changing file name
          if (fNew.getAbsolutePath().equals(cover.getFile().getAbsolutePath())) {
            return;
          }
          try {
            UtilSystem.copy(cover.getFile(), fNew);
            InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),
                InformationJPanel.MessageType.INFORMATIVE);

            // Reset cached cover in associated albums to make sure that new covers
            // will be discovered in various views like Catalog View.
            resetCachedCover();

            // Notify cover change
            ObservationManager.notify(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
          } catch (final Exception ex) {
            Log.error(24, ex);
            Messages.showErrorMessage(24);
          }
        }
      }
    }.start();
  }

  /**
   * Called when making a cover default.
   */
  private void handleDefault() {
    // sanity check
    if (index >= alCovers.size()) {
      Log.warn("Cannot default cover which is not available.");
      return;
    }
    if (index < 0) {
      Log.warn("Cannot default cover with invalid index.");
      return;
    }
    new Thread("Default cover thread") {
      @Override
      public void run() {
        Cover cover = alCovers.get(index);
        org.jajuk.base.File fCurrent = fileReference;
        // Path of the default cover, it is simply the URL of the current cover for local covers
        // but it is another path to a newly created image for tag or remote covers
        String destPath = cover.getFile().getAbsolutePath();
        if (fCurrent == null) {
          fCurrent = QueueModel.getPlayingFile();
        }
        if (cover.getType() == CoverType.TAG_COVER) {
          destPath = dirReference.getFio().getPath() + "/" + cover.getFile().getName();
          destPath = convertCoverPath(destPath);
          File destFile = new File(destPath);
          try {
            // Copy cached file to music directory
            // Note that the refreshCover() methods automatically 
            // extract any track cover tag to an image file in the cache
            UtilSystem.copy(cover.getFile(), destFile);
            Cover cover2 = new Cover(destFile, CoverType.SELECTED_COVER);
            alCovers.add(cover2);
          } catch (Exception ex) {
            Log.error(24, ex);
            Messages.showErrorMessage(24);
            return;
          }
        } else if (cover.getType() == CoverType.REMOTE_COVER) {
          String sFilename = UtilSystem.getOnlyFile(cover.getURL().toString());
          destPath = dirReference.getFio().getPath() + "/" + sFilename;
          destPath = convertCoverPath(destPath);
          try {
            // Download cover and copy file from cache to music directory
            File fSource = DownloadManager.downloadToCache(cover.getURL());
            File fileDest = new File(destPath);
            UtilSystem.copy(fSource, new File(destPath));
            Cover cover2 = new Cover(fileDest, CoverType.SELECTED_COVER);
            if (!alCovers.contains(cover2)) {
              alCovers.add(cover2);
              setFoundText();
            }
          } catch (Exception ex) {
            Log.error(24, ex);
            Messages.showErrorMessage(24);
            return;
          }
        }
        // Remove previous thumbs to avoid using outdated images
        // Reset cached cover
        ThumbnailManager.cleanThumbs(fCurrent.getTrack().getAlbum());
        refreshThumbs(cover);
        InformationJPanel.getInstance().setMessage(Messages.getString("Success"),
            InformationJPanel.MessageType.INFORMATIVE);
        // For every kind of cover types :
        ObservationManager.notify(new JajukEvent(JajukEvents.COVER_DEFAULT_CHANGED));
        ObservationManager.notify(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
        // then make it the default cover for this album
        if (fCurrent != null && fCurrent.getTrack() != null
            && fCurrent.getTrack().getAlbum() != null && cover.getFile() != null) {
          Album album = fCurrent.getTrack().getAlbum();
          album.setProperty(XML_ALBUM_SELECTED_COVER, destPath);
          album.setProperty(XML_ALBUM_DISCOVERED_COVER, destPath);
        }
      }
    }.start();
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent )
   */
  @Override
  public void componentResized(final ComponentEvent e) {
    final long lCurrentDate = System.currentTimeMillis(); // adjusting code
    if (lCurrentDate - lDateLastResize < 500) { // Do consider only one event every 
      // 500 ms to avoid race conditions and lead to unexpected states (verified)
      lDateLastResize = lCurrentDate;
      return;
    }
    lDateLastResize = lCurrentDate;
    Log.debug("Cover resized");
    // Force initial cover refresh. We do this inside this method and not initUI() 
    // because we make sure that the window is fully displayed then (otherwise, we get 
    // a black cover when switching from slimbar to main window for ie)
    CoverResetThread refresh = new CoverResetThread();
    refresh.start();
  }

  /**
   * Creates the query.
   * 
   * @param file DOCUMENT_ME
   * 
   * @return an accurate google search query for a file
   */
  public String createQuery(final org.jajuk.base.File file) {
    String sQuery = "";
    int iAccuracy = 0;
    try {
      iAccuracy = Conf.getInt(Const.CONF_COVERS_ACCURACY + "_"
          + ((getPerspective() == null) ? "popup" : getPerspective().getID()));
    } catch (final NumberFormatException e) {
      // can append if accuracy never set
      Log.debug("Unknown accuracy");
    }
    final Track track = file.getTrack();
    final Artist artist = track.getArtist();
    final Album album = track.getAlbum();
    switch (iAccuracy) {
    case 0: // low, default
      if (!artist.seemsUnknown()) {
        sQuery += artist.getName() + " ";
      }
      if (!album.seemsUnknown()) {
        sQuery += album.getName() + " ";
      }
      break;
    case 1: // medium
      if (!artist.seemsUnknown()) {
        sQuery += '\"' + artist.getName() + QUOTE_BLANK;
        // put quotes around it
      }
      if (!album.seemsUnknown()) {
        sQuery += '\"' + album.getName() + QUOTE_BLANK;
      }
      break;
    case 2: // high
      if (!artist.seemsUnknown()) {
        sQuery += PLUS_QUOTE + artist.getName() + QUOTE_BLANK;
        // put "" around it
      }
      if (!album.seemsUnknown()) {
        sQuery += PLUS_QUOTE + album.getName() + QUOTE_BLANK;
      }
      break;
    case 3: // by artist
      if (!artist.seemsUnknown()) {
        sQuery += artist.getName() + " ";
      }
      break;
    case 4: // by album
      if (!album.seemsUnknown()) {
        sQuery += album.getName() + " ";
      }
      break;
    case 5: // by track name
      sQuery += track.getName();
      break;
    default:
      break;
    }
    return sQuery;
  }

  /**
   * Display given cover.
   * 
   * @param index index of the cover to display
   */
  private void displayCover(final int index) {
    if ((alCovers.size() == 0) || (index >= alCovers.size()) || (index < 0)) {
      // just a check
      alCovers.add(CoverView.nocover); // display nocover by default
      displayCover(0);
      return;
    }
    final Cover cover = alCovers.get(index); // take image at the given index
    final URL url = cover.getURL();
    // enable delete button only for local covers
    jbDelete.setEnabled(cover.getType() == CoverType.LOCAL_COVER
        || cover.getType() == CoverType.SELECTED_COVER
        || cover.getType() == CoverType.STANDARD_COVER);

    //Disable default command for "none" cover
    jbDefault.setEnabled(cover.getType() != CoverType.NO_COVER);

    if (url != null) {
      jbSave.setEnabled(false);
      String sType = " (L)"; // local cover
      if (cover.getType() == CoverType.REMOTE_COVER) {
        sType = "(@)"; // Web cover
        jbSave.setEnabled(true);
      } else if (cover.getType() == CoverType.TAG_COVER) {
        sType = "(T)"; // Tag cover
      }
      final String size = cover.getSize();
      jl = new JLabel(ii);
      jl.setMinimumSize(new Dimension(0, 0)); // required for info
      // node resizing
      if (cover.getType() == CoverType.TAG_COVER) {
        jl.setToolTipText("<html>Tag<br>" + size + "K");
      } else {
        jl.setToolTipText("<html>" + url.toString() + "<br>" + size + "K");
      }
      setSizeText(size + "K" + sType);
      setFoundText();
    }
    // set tooltip for previous and next track
    try {
      int indexPrevious = index + 1;
      if (indexPrevious > alCovers.size() - 1) {
        indexPrevious = 0;
      }
      final URL urlPrevious = alCovers.get(indexPrevious).getURL();
      if (urlPrevious != null) {
        jbPrevious.setToolTipText("<html>" + Messages.getString("CoverView.4") + "<br>"
            + urlPrevious.toString() + "</html>");
      }
      int indexNext = index - 1;
      if (indexNext < 0) {
        indexNext = alCovers.size() - 1;
      }
      final URL urlNext = alCovers.get(indexNext).getURL();
      if (urlNext != null) {
        jbNext.setToolTipText("<html>" + Messages.getString("CoverView.5") + "<br>"
            + urlNext.toString() + "</html>");
      }
    } catch (final Exception e) { // the url code can throw out of bounds
      // exception for unknown reasons so check it
      Log.debug("jl=" + jl + " url={{" + url + "}}");
      Log.error(e);
    }
    if (getComponentCount() > 0) {
      removeAll();
    }
    if (includeControls) {
      add(jpControl, "grow,wrap");
    }
    // Invert the mirrow option when clicking on the cover
    jl.addMouseListener(new JajukMouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (!(cover.getType().equals(CoverType.NO_COVER))) {
          boolean isMirrowed = includeControls ? Conf.getBoolean(Const.CONF_COVERS_MIRROW_COVER)
              : Conf.getBoolean(Const.CONF_COVERS_MIRROW_COVER_FS_MODE);
          // Normal cover view
          if (includeControls) {
            Conf.setProperty(Const.CONF_COVERS_MIRROW_COVER, !isMirrowed + "");
          } else {
            // Full screen mode
            Conf.setProperty(Const.CONF_COVERS_MIRROW_COVER_FS_MODE, !isMirrowed + "");
          }
          ObservationManager.notify(new JajukEvent(JajukEvents.COVER_NEED_REFRESH));
          ObservationManager.notify(new JajukEvent(JajukEvents.PARAMETERS_CHANGE));
        }
      }
    });
    add(jl, "center,wrap");
    // make sure the image is repainted to avoid overlapping covers
    CoverView.this.revalidate();
    CoverView.this.repaint();
    searching(false);
  }

  /**
   * Display current cover (at this.index), try all covers in case of error
   */
  private void displayCurrentCover() {
    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
      @Override
      public Void doInBackground() {
        synchronized (this) {
          // Avoid looping
          if (alCovers.size() == 0) {
            // should not append
            alCovers.add(CoverView.nocover);
            // Add at last the default cover if all remote cover has
            // been discarded
            try {
              prepareDisplay(0);
            } catch (JajukException e) {
              Log.error(e);
            }
            return null;
          }
          if ((alCovers.size() == 1) && ((alCovers.get(0)).getType() == CoverType.NO_COVER)) {
            // only a default cover
            try {
              prepareDisplay(0);
            } catch (JajukException e) {
              Log.error(e);
            }
            return null;
          }
          // else, there is at least one local cover and no
          // default cover
          while (alCovers.size() > 0) {
            try {
              prepareDisplay(index);
              return null; // OK, leave
            } catch (Exception e) {
              Log.debug("Removed cover: {{" + alCovers.get(index) + "}}");
              alCovers.remove(index);
              // refresh number of found covers
              if (!bGotoBetter) {
                // we go to worse covers. If we go to better
                // covers, we just
                // keep the same index try a worse cover...
                if (index - 1 >= 0) {
                  index--;
                } else { // no more worse cover
                  index = alCovers.size() - 1;
                  // come back to best cover
                }
              }
            }
          }
          // if this code is executed, it means than no available
          // cover was found, then display default cover
          alCovers.add(CoverView.nocover); // Add at last the default cover
          // if all remote cover has been discarded
          try {
            index = 0;
            prepareDisplay(index);
          } catch (JajukException e) {
            Log.error(e);
          }
        }
        return null;
      }

      @Override
      public void done() {
        displayCover(index);
      }
    };
    sw.execute();
  }

  /**
   * Gets the cover number.
   * 
   * @return number of real covers (not default) covers found
   */
  private int getCoverNumber() {
    return alCovers.size();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("CoverView.3");
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    final Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.COVER_NEED_REFRESH);
    return eventSubjectSet;
  }

  /**
   * Long action to compute image to display (download, resizing...)
   * 
   * @param index DOCUMENT_ME
   * 
   * @return null (just used by the SwingWorker)
   * 
   * @throws JajukException the jajuk exception
   */
  private Object prepareDisplay(final int index) throws JajukException {
    final int iLocalEventID = this.iEventID;
    Log.debug("display index: " + index);
    searching(true); // lookup icon
    // find next correct cover
    ImageIcon icon = null;
    Cover cover = null;
    try {
      if (this.iEventID == iLocalEventID) {
        cover = alCovers.get(index); // take image at the given index
        Image img = cover.getImage();
        // Never mirror our no cover image
        if (cover.getType().equals(CoverType.NO_COVER)) {
          icon = new ImageIcon(img);
        } else {
          if (
          // should we mirror in our GUI
          (includeControls && Conf.getBoolean(Const.CONF_COVERS_MIRROW_COVER))
          // should we mirror in fullscreen mode
              || (!includeControls && Conf.getBoolean(Const.CONF_COVERS_MIRROW_COVER_FS_MODE))) {
            icon = new ImageIcon(UtilGUI.get3dImage(img));
          } else {
            icon = new ImageIcon(img);
          }
        }

        if (icon.getIconHeight() == 0 || icon.getIconWidth() == 0) {
          throw new JajukException(0, "Wrong picture, size is null");
        }
      } else {
        Log.debug("Download stopped - 2");
        return null;
      }
    } catch (final FileNotFoundException e) {
      setCursor(UtilGUI.DEFAULT_CURSOR);
      searching(false);

      // do not display a stacktrace for FileNotfound as we expect this in cases
      // where the picture is gone on the net
      Log.warn("Cover image not found at URL: "
          + (cover == null ? "<null>" : cover.getURL().toString()));
      return null;
    } catch (final UnknownHostException e) {
      setCursor(UtilGUI.DEFAULT_CURSOR);
      searching(false);

      // do not display a stacktrace for HostNotFound as we expect this in cases
      // where the whole server is gone on the net
      Log.warn("Cover image not found at URL: "
          + (cover == null ? "<null>" : cover.getURL().toString()));
      return null;
    } catch (final IOException e) { // this cover cannot be loaded
      setCursor(UtilGUI.DEFAULT_CURSOR);
      searching(false);
      Log.error(e);
      throw new JajukException(0, e);
    } catch (final InterruptedException e) { // this cover cannot be loaded
      setCursor(UtilGUI.DEFAULT_CURSOR);
      searching(false);
      Log.error(e);
      throw new JajukException(0, e);
    }
    // We apply a 90% of space availability to avoid image cut-offs (see #1283)
    final int iDisplayAreaHeight = (int) (0.9f * CoverView.this.getHeight() - 30);
    final int iDisplayAreaWidth = (int) (0.9f * CoverView.this.getWidth() - 10);
    // check minimum sizes
    if ((iDisplayAreaHeight < 1) || (iDisplayAreaWidth < 1)) {
      return null;
    }
    int iNewWidth;
    int iNewHeight;
    if (iDisplayAreaHeight > iDisplayAreaWidth) {
      // Width is smaller than height : try to optimize height
      iNewHeight = iDisplayAreaHeight; // take all possible height
      // we check now if width will be visible entirely with optimized
      // height
      final float fHeightRatio = (float) iNewHeight / icon.getIconHeight();
      if (icon.getIconWidth() * fHeightRatio <= iDisplayAreaWidth) {
        iNewWidth = (int) (icon.getIconWidth() * fHeightRatio);
      } else {
        // no? so we optimize width
        iNewWidth = iDisplayAreaWidth;
        iNewHeight = (int) (icon.getIconHeight() * ((float) iNewWidth / icon.getIconWidth()));
      }
    } else {
      // Height is smaller or equal than width : try to optimize width
      iNewWidth = iDisplayAreaWidth; // take all possible width
      // we check now if height will be visible entirely with
      // optimized width
      final float fWidthRatio = (float) iNewWidth / icon.getIconWidth();
      if (icon.getIconHeight() * fWidthRatio <= iDisplayAreaHeight) {
        iNewHeight = (int) (icon.getIconHeight() * fWidthRatio);
      } else {
        // no? so we optimize width
        iNewHeight = iDisplayAreaHeight;
        iNewWidth = (int) (icon.getIconWidth() * ((float) iNewHeight / icon.getIconHeight()));
      }
    }
    if (this.iEventID == iLocalEventID) {
      // Note that at this point, the image is fully loaded (done in the ImageIcon constructor)
      ii = UtilGUI.getResizedImage(icon, iNewWidth, iNewHeight);
      // Free source and destination image buffer, see
      // http://forums.sun.com/thread.jspa?threadID=5424304&tstart=60
      icon.getImage().flush();
      ii.getImage().flush();
    } else {
      Log.debug("Download stopped - 2");
      return null;
    }
    return null;
  }

  /**
   * Refresh default cover thumb (used in catalog view).
   * 
   * @param cover DOCUMENT_ME
   */
  private void refreshThumbs(final Cover cover) {
    if (dirReference == null) {
      Log.warn("Cannot refresh thumbnails without reference directory");
      return;
    }
    // refresh thumbs
    try {
      for (int size = 50; size <= 300; size += 50) {
        final Album album = dirReference.getFiles().iterator().next().getTrack().getAlbum();
        final File fThumb = ThumbnailManager.getThumbBySize(album, size);
        ThumbnailManager.createThumbnail(cover.getFile(), fThumb, size);
      }
    } catch (final InterruptedException ex) {
      Log.error(24, ex);
    } catch (final IOException ex) {
      Log.error(24, ex);
    } catch (final RuntimeException ex) {
      Log.error(24, ex);
    }
  }

  /**
   * Display or hide search icon.
   * 
   * @param bSearching DOCUMENT_ME
   */
  public void searching(final boolean bSearching) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (bSearching) {
          jlSearching.setIcon(IconLoader.getIcon(JajukIcons.NET_SEARCH));
        } else {
          jlSearching.setIcon(null);
        }
      }
    });
  }

  /**
   * Set the cover Found text.
   */
  private void setFoundText() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // make sure not to display negative indexes
        int i = getCoverNumber() - index;
        if (i < 0) {
          Log.debug("Negative cover index: " + i);
          i = 0;
        }
        jlFound.setText(i + "/" + getCoverNumber());
      }
    });
  }

  /**
   * Set the cover Found text.
   * 
   * @param sFound specified text
   */
  private void setFoundText(final String sFound) {
    if (sFound != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          jlFound.setText(sFound);
        }
      });
    }
  }

  /**
   * Set the cover size text.
   * 
   * @param sSize DOCUMENT_ME
   */
  private void setSizeText(final String sSize) {
    if (sSize != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          jlSize.setText(sSize);
        }
      });
    }
  }

  /**
   * Gets the current image.
   * 
   * @return the current image
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws InterruptedException the interrupted exception
   * @throws JajukException the jajuk exception
   */
  public Image getCurrentImage() throws IOException, InterruptedException, JajukException {
    if (alCovers.size() > 0) {
      return alCovers.get(0).getImage();
    }
    return CoverView.nocover.getImage();
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(final JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    this.iEventID++;
    final int iLocalEventID = iEventID;
    try {
      searching(true);
      // When receiving this event, check if we should change the cover or
      // not
      // (we don't change cover if playing another track of the same album
      // except if option shuffle cover is set)
      if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
        updateFileLaunched(event, iLocalEventID);
      } else if (JajukEvents.ZERO.equals(subject) || JajukEvents.WEBRADIO_LAUNCHED.equals(subject)
          || JajukEvents.PLAYER_STOP.equals(subject)) {
        updateStopOrWebRadioLaunched();
      } else if (JajukEvents.COVER_NEED_REFRESH.equals(subject)) {
        refreshCovers(iLocalEventID, true);
      }
    } catch (final IOException e) {
      Log.error(e);
    } finally {
      searching(false); // hide searching icon
    }
  }

  /**
   * Update stop or web radio launched.
   * DOCUMENT_ME
   */
  private void updateStopOrWebRadioLaunched() {
    // Ignore this event if a reference file has been set
    if (fileReference != null) {
      return;
    }
    setFoundText("");
    setSizeText("");
    alCovers.clear();
    alCovers.add(CoverView.nocover); // add the default cover
    index = 0;
    displayCurrentCover();
    dirReference = null;
    // Force cover to reload at next track
    bForceCoverReload = true;
    // disable commands
    enableCommands(false);
  }

  /**
   * Update file launched.
   * 
   * @param event DOCUMENT_ME
   * @param iLocalEventID DOCUMENT_ME
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void updateFileLaunched(final JajukEvent event, final int iLocalEventID)
      throws IOException {
    org.jajuk.base.File last = null;
    Properties details = event.getDetails();
    if (details != null) {
      StackItem item = (StackItem) details.get(Const.DETAIL_OLD);
      if (item != null) {
        last = item.getFile();
      }
    }
    // Ignore this event if a reference file has been set and if
    // this event has already been handled
    if ((fileReference != null) && (dirReference != null)) {
      return;
    }
    // if we are always in the same directory, just leave to
    // save cpu
    boolean dirChanged = last == null ? true : !last.getDirectory().equals(
        QueueModel.getPlayingFile().getDirectory());
    if (bForceCoverReload) {
      dirChanged = true;
    }
    refreshCovers(iLocalEventID, dirChanged);

    if (Conf.getBoolean(Const.CONF_COVERS_SHUFFLE)) {
      // Ignore this event if a reference file has been set
      if (fileReference != null) {
        return;
      }
      // choose a random cover
      index = (int) (Math.random() * alCovers.size() - 1);
      displayCurrentCover();
    }
    enableCommands(true);
  }

  /**
   * Convenient method to massively enable/disable this view buttons.
   * 
   * @param enable DOCUMENT_ME
   */
  private void enableCommands(final boolean enable) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        jcbAccuracy.setEnabled(enable);
        jbDefault.setEnabled(enable);
        jbDelete.setEnabled(enable);
        jbNext.setEnabled(enable);
        jbPrevious.setEnabled(enable);
        jbSave.setEnabled(enable);
        jlFound.setVisible(enable);
        jlSize.setVisible(enable);
      }

    });

  }

  /**
   * Covers refreshing effective code
   * <p>
   * Must be called outside the EDT, contains network access
   * </p>.
   * 
   * @param iLocalEventID DOCUMENT_ME
   * @param dirChanged DOCUMENT_ME
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */
  private void refreshCovers(int iLocalEventID, boolean dirChanged) throws IOException {
    // Reset this flag
    bForceCoverReload = false;
    org.jajuk.base.File fCurrent = fileReference;
    // check if a file has been given for this cover view
    // if not, take current cover
    if (fCurrent == null) {
      fCurrent = QueueModel.getPlayingFile();
    }
    // no current cover
    if (fCurrent == null) {
      dirReference = null;
    } else {
      // store this dir
      dirReference = fCurrent.getDirectory();
    }
    if (dirReference == null) {
      alCovers.clear();
      alCovers.add(CoverView.nocover);
      index = 0;
      displayCurrentCover();
      return;
    }

    if (fCurrent == null) {
      throw new IllegalArgumentException("Internal Error: Unexpected value, "
          + "variable fCurrent should not be empty. dirReference: " + dirReference);
    }

    // We only need to refresh the other covers if the directory changed 
    // but we still clear tag-based covers even if directory didn't change
    // so the song-specific tag is taken into account. 
    Iterator<Cover> it = alCovers.iterator();
    while (it.hasNext()) {
      Cover cover = it.next();
      if (cover.getType() == CoverType.TAG_COVER) {
        it.remove();
      }
    }
    if (dirChanged) {
      // remove all existing covers
      alCovers.clear();

      // Search for local covers in all directories mapping
      // the current track to reach other devices covers and
      // display them together
      final Track trackCurrent = fCurrent.getTrack();
      final List<org.jajuk.base.File> alFiles = trackCurrent.getFiles();

      // Add any selected default cover
      String defaultCoverPath = trackCurrent.getAlbum().getStringValue(XML_ALBUM_SELECTED_COVER);
      if (StringUtils.isNotBlank(defaultCoverPath)) {
        File coverFile = new File(defaultCoverPath);
        if (coverFile.exists()) {
          final Cover cover = new Cover(coverFile, CoverType.SELECTED_COVER);
          // Avoid dups
          if (!alCovers.contains(cover)) {
            alCovers.add(cover);
          }
        }
      }

      // list of files mapping the track
      for (final org.jajuk.base.File file : alFiles) {
        final Directory dirScanned = file.getDirectory();
        if (!dirScanned.getDevice().isMounted()) {
          // if the device is not ready, just ignore it
          continue;
        }
        // Now search for regular or standard local covers
        // null if none file found
        final java.io.File[] files = dirScanned.getFio().listFiles();
        for (int i = 0; (files != null) && (i < files.length); i++) {
          // check size to avoid out of memory errors
          if (files[i].length() > Const.MAX_COVER_SIZE * 1024) {
            continue;
          }
          final JajukFileFilter filter = ImageFilter.getInstance();
          if (filter.accept(files[i])) {
            Cover cover = null;
            if (UtilFeatures.isStandardCover(files[i])) {
              cover = new Cover(files[i], CoverType.STANDARD_COVER);
            } else {
              cover = new Cover(files[i], CoverType.LOCAL_COVER);
            }
            if (!alCovers.contains(cover)) {
              alCovers.add(cover);
            }
          }
        }
      }

      // Then we search for web covers online if max
      // connection errors number is not reached or if user
      // already managed to connect.
      // We also drop the query if user required none internet access
      if (Conf.getBoolean(Const.CONF_COVERS_AUTO_COVER)
          && !Conf.getBoolean(Const.CONF_NETWORK_NONE_INTERNET_ACCESS)
          && (CoverView.bOnceConnected || (CoverView.iErrorCounter < Const.STOP_TO_SEARCH))) {
        try {
          final String sQuery = createQuery(fCurrent);
          Log.debug("Query={{" + sQuery + "}}");
          if (!sQuery.isEmpty()) {
            // there is not enough information in tags
            // for a web search
            List<URL> alUrls;
            alUrls = DownloadManager.getRemoteCoversList(sQuery);
            CoverView.bOnceConnected = true;
            // user managed once to connect to the web
            if (alUrls.size() > Const.MAX_REMOTE_COVERS) {
              // limit number of remote covers
              alUrls = new ArrayList<URL>(alUrls.subList(0, Const.MAX_REMOTE_COVERS));
            }
            Collections.reverse(alUrls);
            // set best results to be displayed first
            final Iterator<URL> it2 = alUrls.iterator();
            // add found covers
            while (it2.hasNext() && (iEventID == iLocalEventID)) {
              // load each cover (pre-load or post-load)
              // and stop if a signal has been emitted
              final URL url = it2.next();

              final Cover cover = new Cover(url, CoverType.REMOTE_COVER);
              // Create a cover with given url ( image
              // will be really downloaded when
              // required if no preload)
              if (!alCovers.contains(cover)) {
                Log.debug("Found Cover: {{" + url.toString() + "}}");
                alCovers.add(cover);
              }

            }
            if (iEventID != iLocalEventID) {
              // a stop signal has been emitted
              // from a concurrent thread
              Log.debug("Download stopped - 1");
              return;
            }
          }
        } catch (final IOException e) {
          Log.warn(e.getMessage());
          // can occur in case of timeout or error during
          // covers list download
          CoverView.iErrorCounter++;
          if (CoverView.iErrorCounter == Const.STOP_TO_SEARCH) {
            Log.warn("Too many connection fails," + " stop to search for covers online");
            InformationJPanel.getInstance().setMessage(Messages.getString("Error.030"),
                InformationJPanel.MessageType.WARNING);
          }
        } catch (final Exception e) {
          Log.error(e);
        }
      }
    }

    // Check for tag covers 
    try {
      Tag tag = new Tag(fCurrent.getFIO(), false);
      List<Cover> tagCovers = tag.getCovers();
      // Reverse order of the found tag covers because we want best last
      // in alCovers and we want to keep tag order.
      Collections.reverse(tagCovers);
      for (Cover cover : tagCovers) {
        // Avoid dups
        if (!alCovers.contains(cover)) {
          alCovers.add(cover);
        }
      }
    } catch (JajukException e1) {
      Log.error(e1);
    }

    if (alCovers.size() == 0) {// add the default cover if none
      // other cover has been found
      alCovers.add(CoverView.nocover);
    }

    Collections.sort(alCovers);

    Log.debug("Local cover list: {{" + alCovers + "}}");
    if (Conf.getBoolean(Const.CONF_COVERS_SHUFFLE)) {
      // choose a random cover
      index = (int) (Math.random() * alCovers.size());
    } else {
      index = alCovers.size() - 1;
      // current index points to the best available cover
    }
    displayCurrentCover();
  }
}
