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
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.jajuk.base.Album;
import org.jajuk.base.Author;
import org.jajuk.base.Directory;
import org.jajuk.base.Track;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.covers.Cover;
import org.jajuk.services.players.FIFO;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukFileChooser;
import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.error.TimeOutException;
import org.jajuk.util.filters.GIFFilter;
import org.jajuk.util.filters.ImageFilter;
import org.jajuk.util.filters.JPGFilter;
import org.jajuk.util.filters.PNGFilter;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.border.DropShadowBorder;

/**
 * Cover view. Displays an image for the current album
 */
public class CoverView extends ViewAdapter implements Observer, ComponentListener, ActionListener,
    ITechnicalStrings {

  private static final long serialVersionUID = 1L;

  /** Default cover */
  private static Cover coverDefault;

  /** Error counter to check connection availability */
  private static int iErrorCounter = 0;

  /**
   * Connected one flag : true if jajuk managed once to connect to the web to
   * bring covers
   */
  private static boolean bOnceConnected = false;

  /** Reference File for cover */
  private org.jajuk.base.File fileReference;

  /** File directory used as a cache for perfs */
  private Directory dirReference;

  /** List of available covers for the current file */
  private final ArrayList<Cover> alCovers = new ArrayList<Cover>(20);

  // control panel
  JPanel jpControl;

  JajukButton jbPrevious;

  JajukButton jbNext;

  JajukButton jbDelete;

  JajukButton jbSave;

  JajukButton jbDefault;

  JLabel jlSize;

  JLabel jlFound;

  JLabel jlSearching;

  JComboBox jcbAccuracy;

  /** Date last resize (used for adjustment management) */
  private long lDateLastResize;

  /** URL and size of the image */
  JLabel jl;

  /** Used Cover index */
  int index = 0;

  /** Generic locker */
  private final byte[] bLock = new byte[0];

  /** Event ID */
  private volatile int iEventID;

  /** Flag telling that user wants to display a better cover */
  private boolean bGotoBetter = false;

  /** Final image to display */
  private ImageIcon ii;

  /**
   * Constructor
   * 
   * @param sID
   *          ID used to store independently parameters of views
   */
  public CoverView() {
  }

  /**
   * Constructor
   * 
   * @param file
   *          Reference file
   * 
   */
  public CoverView(final org.jajuk.base.File file) {
    fileReference = file;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jcbAccuracy) {
      // Note that we have to store/retrieve accuracy using an id. When
      // this view is used from a popup, we can't use perspective id
      ConfigurationManager.setProperty(ITechnicalStrings.CONF_COVERS_ACCURACY + "_"
          + ((getPerspective() == null) ? "popup" : getPerspective().getID()), Integer
          .toString(jcbAccuracy.getSelectedIndex()));

      new Thread() {
        @Override
        public void run() {
          // force refresh
          if (getPerspective() == null) {
            dirReference = null;
          }
          update(new Event(JajukEvents.EVENT_COVER_REFRESH, ObservationManager
              .getDetailsLastOccurence(JajukEvents.EVENT_COVER_REFRESH)));
        }
      }.start();
    } else if (e.getSource() == jbPrevious) { // previous : show a
      // better cover
      bGotoBetter = true;
      index++;
      if (index > alCovers.size() - 1) {
        index = 0;
      }
      displayCurrentCover();
      bGotoBetter = false; // make sure default behavior is to go
      // to worse covers
    } else if (e.getSource() == jbNext) { // next : show a worse cover
      bGotoBetter = false;
      index--;
      if (index < 0) {
        index = alCovers.size() - 1;
      }
      displayCurrentCover();
    } else if (e.getSource() == jbDelete) { // delete a local cover
      final Cover cover = alCovers.get(index);
      // show confirmation message if required
      if (ConfigurationManager.getBoolean(ITechnicalStrings.CONF_CONFIRMATIONS_DELETE_COVER)) {
        final int iResu = Messages.getChoice(Messages.getString("Confirmation_delete_cover")
            + " : " + cover.getURL().getFile(), JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (iResu != JOptionPane.YES_OPTION) {
          return;
        }
      }
      // yet there? ok, delete the cover
      try {
        final File file = new File(cover.getURL().getFile());
        if (file.isFile() && file.exists()) {
          file.delete();
          // check that file has been really deleted (sometimes,
          // we get no exception)
          if (file.exists()) {
            throw new Exception("");
          }
        } else { // not a file, must have a problem
          throw new Exception("");
        }
      } catch (final Exception ioe) {
        Log.error(131, ioe);
        Messages.showErrorMessage(131);
        return;
      }
      // If this was the absolute cover, remove the reference in the
      // collection
      if (cover.getType() == Cover.ABSOLUTE_DEFAULT_COVER) {
        dirReference.removeProperty("default_cover");
      }
      // reorganize covers
      synchronized (bLock) {
        alCovers.remove(index);
        index--;
        if (index < 0) {
          index = alCovers.size() - 1;
        }
        ObservationManager.notify(new Event(JajukEvents.EVENT_COVER_REFRESH));
      }
    } else if (e.getSource() == jbDefault) { // choose a default
      // first commit this cover on the disk if it is a remote cover
      final Cover cover = alCovers.get(index);
      final String sFilename = Util.getOnlyFile(cover.getURL().toString());
      if (cover.getType() == Cover.REMOTE_COVER) {
        String sFilePath = dirReference.getFio().getPath() + "/" + sFilename;
        // Add a jajuk suffix to know this cover has been downloaded by
        // jajuk
        final int pos = sFilePath.lastIndexOf('.');
        sFilePath = new StringBuilder(sFilePath).insert(pos,
            ITechnicalStrings.FILE_JAJUK_DOWNLOADED_FILES_SUFFIX).toString();
        try {
          // copy file from cache
          final File fSource = DownloadManager.downloadCover(cover.getURL(), cover.getDownloadID());
          final File file = new File(sFilePath);
          Util.copy(fSource, file);
          final Cover cover2 = new Cover(file.toURL(), Cover.ABSOLUTE_DEFAULT_COVER);
          if (!alCovers.contains(cover2)) {
            alCovers.add(cover2);
            setFoundText();
          }
          // Remove previous thumbs to avoid using outdated images
          org.jajuk.base.File fCurrent = fileReference;
          if (fCurrent == null) {
            fCurrent = FIFO.getInstance().getCurrentFile();
          }
          ThumbnailManager.cleanThumbs(fCurrent.getTrack().getAlbum());
          refreshThumbs(cover);
          InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),
              InformationJPanel.INFORMATIVE);
        } catch (final Exception ex) {
          Log.error(24, ex);
          Messages.showErrorMessage(24);
        }
      } else {
        refreshThumbs(cover);
        InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.8"),
            InformationJPanel.INFORMATIVE);
      }
      ObservationManager.notify(new Event(JajukEvents.EVENT_COVER_REFRESH));
      // then make it the default cover in this directory
      dirReference.setProperty("default_cover", Util.getOnlyFile(sFilename));

    } else if ((e.getSource() == jbSave)
        && ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
      // save a file as... (can be local now)
      new Thread() {
        @Override
        public void run() {
          final Cover cover = alCovers.get(index);
          final JajukFileChooser jfchooser = new JajukFileChooser(new JajukFileFilter(GIFFilter
              .getInstance(), PNGFilter.getInstance(), JPGFilter.getInstance()));
          jfchooser.setAcceptDirectories(true);
          jfchooser.setCurrentDirectory(dirReference.getFio());
          jfchooser.setDialogTitle(Messages.getString("CoverView.10"));
          final File finalFile = new File(dirReference.getFio().getPath() + "/"
              + Util.getOnlyFile(cover.getURL().toString()));
          jfchooser.setSelectedFile(finalFile);
          final int returnVal = jfchooser.showSaveDialog(JajukWindow.getInstance());
          File fNew = null;
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            fNew = jfchooser.getSelectedFile();
            // if user try to save as without changing file name
            if (fNew.getAbsolutePath().equals(cover.getFile().getAbsolutePath())) {
              return;
            }
            try {
              Util.copy(cover.getFile(), fNew);
              InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),
                  InformationJPanel.INFORMATIVE);
              ObservationManager.notify(new Event(JajukEvents.EVENT_COVER_REFRESH));
            } catch (final Exception ex) {
              Log.error(24, ex);
              Messages.showErrorMessage(24);
            }
          }
        }
      }.start();
    } else if (e.getSource() == jbSave) {
      // save a file with its original name
      new Thread() {
        @Override
        public void run() {
          final Cover cover = alCovers.get(index);
          // should not happen, only remote covers here
          if (cover.getType() != Cover.REMOTE_COVER) {
            Log.debug("Try to save a local cover");
            return;
          }
          String sFilePath = null;
          sFilePath = dirReference.getFio().getPath() + "/"
              + Util.getOnlyFile(cover.getURL().toString());
          // Add a jajuk suffix to know this cover has been downloaded
          // by jajuk
          final int pos = sFilePath.lastIndexOf('.');
          sFilePath = new StringBuilder(sFilePath).insert(pos,
              ITechnicalStrings.FILE_JAJUK_DOWNLOADED_FILES_SUFFIX).toString();
          try {
            // copy file from cache
            final File fSource = DownloadManager.downloadCover(cover.getURL(), cover
                .getDownloadID());
            final File file = new File(sFilePath);
            Util.copy(fSource, file);
            InformationJPanel.getInstance().setMessage(Messages.getString("CoverView.11"),
                InformationJPanel.INFORMATIVE);
            final Cover cover2 = new Cover(file.toURL(), Cover.ABSOLUTE_DEFAULT_COVER);
            if (!alCovers.contains(cover2)) {
              alCovers.add(cover2);
              setFoundText();
            }
            ObservationManager.notify(new Event(JajukEvents.EVENT_COVER_REFRESH));
            // add new cover in others cover views
          } catch (final Exception ex) {
            Log.error(24, ex);
            Messages.showErrorMessage(24);
          }
        }
      }.start();
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
   */
  @Override
  public void componentResized(final ComponentEvent e) {
    Log.debug("Cover resized");
    final long lCurrentDate = System.currentTimeMillis(); // adjusting code
    if (lCurrentDate - lDateLastResize < 500) { // display image every
      // 500 ms to save CPU
      lDateLastResize = lCurrentDate;
      return;
    }
    displayCurrentCover();
    CoverView.this.revalidate(); // make sure the image is repainted
    CoverView.this.repaint(); // make sure the image is repainted
  }

  /**
   * 
   * @param file
   * @return an accurate google search query for a file
   */
  public String createQuery(final org.jajuk.base.File file) {
    String sQuery = "";
    int iAccuracy = 0;
    try {
      iAccuracy = ConfigurationManager.getInt(ITechnicalStrings.CONF_COVERS_ACCURACY + "_"
          + ((getPerspective() == null) ? "popup" : getPerspective().getID()));
    } catch (final Exception e) {
      // can append if accuracy never set
      Log.debug("Unknown accuracy");
    }
    final Track track = file.getTrack();
    final Author author = track.getAuthor();
    final Album album = track.getAlbum();
    switch (iAccuracy) {
    case 0: // low, default
      if (!author.isUnknown()) {
        sQuery += author.getName() + " ";
      }
      if (!album.isUnknown()) {
        sQuery += album.getName() + " ";
      }
      break;
    case 1: // medium
      if (!author.isUnknown()) {
        sQuery += "\"" + author.getName() + "\" ";
        // put quotes around it
      }
      if (!album.isUnknown()) {
        sQuery += "\"" + album.getName() + "\" ";
      }
      break;
    case 2: // high
      if (!author.isUnknown()) {
        sQuery += "+\"" + author.getName() + "\" ";
        // put "" around it
      }
      if (!album.isUnknown()) {
        sQuery += "+\"" + album.getName() + "\" ";
      }
      break;
    case 3: // by author
      if (!author.isUnknown()) {
        sQuery += author.getName() + " ";
      }
      break;
    case 4: // by album
      if (!album.isUnknown()) {
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
   * Display given cover
   * 
   * @param index
   *          index of the cover to display
   * 
   */
  private void displayCover(final int index) {
    if ((alCovers.size() == 0) || (index >= alCovers.size()) || (index < 0)) {
      // just a check
      alCovers.add(CoverView.coverDefault); // display default cover by default
      displayCover(0);
      return;
    }
    final Cover cover = alCovers.get(index); // take image at the given index
    final URL url = cover.getURL();
    // enable delete button only for local covers
    if ((cover.getType() == Cover.LOCAL_COVER) || (cover.getType() == Cover.ABSOLUTE_DEFAULT_COVER)) {
      jbDelete.setEnabled(true);
    } else {
      jbDelete.setEnabled(false);
    }
    if (url != null) {
      jbSave.setEnabled(false);
      String sType = " (L)"; // local cover
      if (cover.getType() == Cover.REMOTE_COVER) {
        sType = "(@)"; // Web cover
        jbSave.setEnabled(true);
      }
      final String size = cover.getSize();
      jl = new JLabel(ii);
      jl.setBorder(new DropShadowBorder(Color.BLACK, 5, 0.5f, 5, false, true, false, true));
      jl.setMinimumSize(new Dimension(0, 0)); // required for info
      // node resizing
      jl.setToolTipText("<html>" + url.toString() + "<br>" + size + "K");
      setSizeText(size + "K" + sType);
      setFoundText();
      // make sure the image is repainted to avoid overlapping covers
      CoverView.this.revalidate();
      CoverView.this.repaint();
    }
    // set tooltip for previous and next track
    try {
      int indexPrevious = index + 1;
      if (indexPrevious > alCovers.size() - 1) {
        indexPrevious = 0;
      }
      final URL urlPrevious = (alCovers.get(indexPrevious)).getURL();
      if (urlPrevious != null) {
        jbPrevious.setToolTipText("<html>" + Messages.getString("CoverView.4") + "<br>"
            + urlPrevious.toString() + "</html>");
      }
      int indexNext = index - 1;
      if (indexNext < 0) {
        indexNext = alCovers.size() - 1;
      }
      final URL urlNext = (alCovers.get(indexNext)).getURL();
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
    add(jpControl, "0,0");
    add(jl, "0,2,c,c");
    searching(false);
  }

  /**
   * Display current cover (at this.index), try all covers in case of error
   */
  private void displayCurrentCover() {
    final SwingWorker sw = new SwingWorker() {
      @Override
      public Object construct() {
        synchronized (bLock) {
          removeComponentListener(CoverView.this);
          // remove listener to avoid looping
          if (alCovers.size() == 0) {
            // should not append
            alCovers.add(CoverView.coverDefault);
            // Add at last the default cover if all remote cover has
            // been discarded
            try {
              prepareDisplay(0);
            } catch (JajukException e) {
              Log.error(e);
            }
            return null;
          }
          if ((alCovers.size() == 1) && ((alCovers.get(0)).getType() == Cover.DEFAULT_COVER)) {
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
              setFoundText();
            }
          }
          // if this code is executed, it means than no available
          // cover was found, then display default cover
          alCovers.add(CoverView.coverDefault); // Add at last the default cover
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
      public void finished() {
        displayCover(index);
        removeComponentListener(CoverView.this);
        addComponentListener(CoverView.this); // listen for resize
      }
    };
    sw.start();
  }

  /**
   * 
   * @return number of real covers (not default) covers found
   */
  private int getCoverNumber() {
    synchronized (bLock) {
      return alCovers.size();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("CoverView.3");
  }

  public Set<JajukEvents> getRegistrationKeys() {
    final HashSet<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EVENT_COVER_REFRESH);
    eventSubjectSet.add(JajukEvents.EVENT_ZERO);
    eventSubjectSet.add(JajukEvents.EVENT_COVER_CHANGE);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    // global layout
    final double size[][] = { { TableLayoutConstants.FILL },
        { TableLayoutConstants.PREFERRED, 5, TableLayoutConstants.FILL, 5 } };
    setLayout(new TableLayout(size));
    // Control panel
    jpControl = new JPanel();
    jpControl.setBorder(BorderFactory.createEtchedBorder());
    final JToolBar jtb = new JToolBar();
    jtb.setRollover(true);
    jtb.setBorder(null);
    jbPrevious = new JajukButton(IconLoader.ICON_PREVIOUS);
    jbPrevious.addActionListener(this);
    jbPrevious.setToolTipText(Messages.getString("CoverView.4"));
    jbNext = new JajukButton(IconLoader.ICON_NEXT);
    jbNext.addActionListener(this);
    jbNext.setToolTipText(Messages.getString("CoverView.5"));
    jbDelete = new JajukButton(IconLoader.ICON_DELETE);
    jbDelete.addActionListener(this);
    jbDelete.setToolTipText(Messages.getString("CoverView.2"));
    jbSave = new JajukButton(IconLoader.ICON_SAVE);
    jbSave.addActionListener(this);
    jbSave.setToolTipText(Messages.getString("CoverView.6"));
    jbDefault = new JajukButton(IconLoader.ICON_DEFAULT_COVER);
    jbDefault.addActionListener(this);
    jbDefault.setToolTipText(Messages.getString("CoverView.8"));
    jlSize = new JLabel("");
    jlFound = new JLabel("");
    jlSearching = new JLabel("", IconLoader.ICON_NET_SEARCH, SwingConstants.CENTER);
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
        return this;
      }
    });
    jcbAccuracy.setMinimumSize(new Dimension(20, 0));
    jcbAccuracy.setToolTipText(Messages.getString("ParameterView.155"));

    jcbAccuracy.addItem(IconLoader.ICON_ACCURACY_LOW);
    jcbAccuracy.addItem(IconLoader.ICON_ACCURACY_MEDIUM);
    jcbAccuracy.addItem(IconLoader.ICON_ACCURACY_HIGH);
    jcbAccuracy.addItem(IconLoader.ICON_AUTHOR);
    jcbAccuracy.addItem(IconLoader.ICON_ALBUM);
    jcbAccuracy.addItem(IconLoader.ICON_TRACK);
    int index = 1; // medium accuracy
    try {
      index = ConfigurationManager.getInt(ITechnicalStrings.CONF_COVERS_ACCURACY + "_"
          + ((getPerspective() == null) ? "popup" : getPerspective().getID()));
    } catch (final Exception e) {
      // Will reach this point at first launch
    }
    jcbAccuracy.setSelectedIndex(index);
    jcbAccuracy.addActionListener(this);

    jtb.add(jbPrevious);
    jtb.add(jbNext);
    jtb.addSeparator();
    jtb.add(jbDelete);
    jtb.add(jbSave);
    jtb.add(jbDefault);

    final double sizeControl[][] = {
    // Toolbar
        { 5, TableLayoutConstants.PREFERRED, 10,
        // size label
            TableLayoutConstants.FILL, 10,
            // nb of found covers label
            TableLayoutConstants.FILL, 5,
            // Accuracy combo
            TableLayoutConstants.PREFERRED, 5,
            // searching icon
            25, 5 }, { 3, 30, 3 } };
    final TableLayout layout = new TableLayout(sizeControl);
    jpControl.setLayout(layout);

    jpControl.add(jtb, "1,1");
    jpControl.add(jlSize, "3,1,c,c");
    jpControl.add(jlFound, "5,1");
    jpControl.add(jcbAccuracy, "7,1");
    jpControl.add(jlSearching, "9,1,c,c");
    ObservationManager.register(this);
    try {
      // instantiate default cover
      if (CoverView.coverDefault == null) {
        CoverView.coverDefault = new Cover(ITechnicalStrings.IMAGES_SPLASHSCREEN,
            Cover.DEFAULT_COVER);
      }
    } catch (final Exception e) {
      Log.error(e);
    }
    add(jpControl, "0,0");
    // request cover refresh after a while to make sure the window owns its
    // definitive dimension so we avoid the cover to resize at startup
    new Thread() {
      @Override
      public void run() {
        try {
          Thread.sleep(3000);
        } catch (final Exception e) {
          Log.error(e);
        }
        // check if a track has already been launched
        if (FIFO.getInstance().isPlayingRadio()) {
          update(new Event(JajukEvents.EVENT_WEBRADIO_LAUNCHED, ObservationManager
              .getDetailsLastOccurence(JajukEvents.EVENT_WEBRADIO_LAUNCHED)));
        } else {
          update(new Event(JajukEvents.EVENT_COVER_REFRESH));
        }

      }
    }.start();
  }

  /**
   * To be refactored
   * 
   * @return whether this view is in current perspective
   */
  public boolean isInCurrentPerspective() {
    if ((getPerspective() == null)
        || getPerspective().equals(PerspectiveManager.getCurrentPerspective())) {
      return true;
    }
    return false;
  }

  /**
   * Long action to compute image to display (download, resizing...)
   * 
   * @param index
   * @return null (just used by the SwingWorker)
   * @throws JajukException
   */
  private Object prepareDisplay(final int index) throws JajukException {
    final int iLocalEventID = iEventID;
    Log.debug("display index: " + index);
    searching(true); // lookup icon
    // find next correct cover
    ImageIcon icon = null;
    Cover cover = null;
    try {
      if (iEventID == iLocalEventID) {
        cover = alCovers.get(index); // take image at the given index
        Image img = cover.getImage();
        icon = new ImageIcon(img);
        if (icon.getIconHeight() == 0 || icon.getIconWidth() == 0) {
          throw new Exception("Wrong picture, size is null");
        }
      } else {
        Log.debug("Download stopped - 2");
        return null;
      }
    } catch (final Exception e) { // this cover cannot be loaded
      setCursor(Util.DEFAULT_CURSOR);
      searching(false);
      Log.error(e);
      throw new JajukException(0);
    }
    final int iDisplayAreaHeight = CoverView.this.getHeight() - 30;
    final int iDisplayAreaWidth = CoverView.this.getWidth() - 8;
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
      if (icon.getIconHeight() * (fWidthRatio) <= iDisplayAreaHeight) {
        iNewHeight = (int) (icon.getIconHeight() * fWidthRatio);
      } else {
        // no? so we optimize width
        iNewHeight = iDisplayAreaHeight;
        iNewWidth = (int) (icon.getIconWidth() * ((float) iNewHeight / icon.getIconHeight()));
      }
    }
    if (iEventID == iLocalEventID) {
      ii = Util.getResizedImage(icon, iNewWidth, iNewHeight);
      // Free source and destination image buffer
      icon.getImage().flush();
      ii.getImage().flush();
    } else {
      Log.debug("Download stopped - 2");
      return null;
    }
    return null;
  }

  /**
   * Refresh default cover thumb (used in catalog view)
   * 
   */
  private void refreshThumbs(final Cover cover) {
    // refresh thumbs
    try {
      for (int i = 0; i < 4; i++) {
        final Album album = dirReference.getFiles().iterator().next().getTrack().getAlbum();
        final File fThumb = Util.getConfFileByPath(ITechnicalStrings.FILE_THUMBS + '/'
            + (50 + 50 * i) + "x" + (50 + 50 * i) + '/' + album.getID() + '.'
            + ITechnicalStrings.EXT_THUMB);
        ThumbnailManager.createThumbnail(cover.getFile(), fThumb, (50 + 50 * i));
      }
      ObservationManager.notify(new Event(JajukEvents.EVENT_COVER_DEFAULT_CHANGED));
    } catch (final Exception ex) {
      Log.error(24, ex);
    }
  }

  /**
   * Display or hide search icon
   * 
   * @param bSearching
   */
  public void searching(final boolean bSearching) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (bSearching) {
          jlSearching.setIcon(IconLoader.ICON_NET_SEARCH);
        } else {
          jlSearching.setIcon(null);
        }
      }
    });
  }

  /**
   * Set the cover Found text
   */
  private void setFoundText() {
    SwingUtilities.invokeLater(new Runnable() {
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
   * Set the cover Found text
   * 
   * @param sFound
   *          specified text
   */
  private void setFoundText(final String sFound) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (sFound != null) {
          jlFound.setText(sFound);
        }
      }
    });
  }

  /**
   * Set the cover size text
   * 
   * @param sFound
   */
  private void setSizeText(final String sSize) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (sSize != null) {
          jlSize.setText(sSize);
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final Event event) {
    removeComponentListener(CoverView.this);
    addComponentListener(CoverView.this); // listen for resize
    final JajukEvents subject = event.getSubject();
    iEventID = (int) (Integer.MAX_VALUE * Math.random());
    final int iLocalEventID = iEventID;
    synchronized (bLock) {// block any concurrent cover update
      try {
        searching(true);
        if (JajukEvents.EVENT_COVER_REFRESH.equals(subject)) {
          // Ignore this event if a reference file has been set and if
          // this event has already been handled
          if ((fileReference != null) && (dirReference != null)) {
            return;
          }
          org.jajuk.base.File fCurrent = fileReference;
          // check if a file has been given for this cover view
          // if not, take current cover
          if (fCurrent == null) {
            fCurrent = FIFO.getInstance().getCurrentFile();
          }
          // no current cover
          if (fCurrent == null) {
            dirReference = null;
          } else {
            // store this dir
            dirReference = fCurrent.getDirectory();
          }
          // remove all existing covers
          alCovers.clear();
          if (dirReference == null) {
            alCovers.add(CoverView.coverDefault);
            index = 0;
            displayCurrentCover();
            return;
          }
          // search for local covers in all directories mapping
          // the current track to reach other devices covers and
          // display them together
          final Track trackCurrent = fCurrent.getTrack();
          final ArrayList<org.jajuk.base.File> alFiles = trackCurrent.getFiles();
          // list of files mapping the track
          for (final org.jajuk.base.File file : alFiles) {
            final Directory dirScanned = file.getDirectory();
            if (!dirScanned.getDevice().isMounted()) {
              // if the device is not ready, just ignore it
              continue;
            }
            final java.io.File[] files = dirScanned.getFio().listFiles();
            // null if none file found
            boolean bAbsoluteCover = false;
            // whether an absolute cover (unique) has been found
            for (int i = 0; (files != null) && (i < files.length); i++) {
              // check size to avoid out of memory errors
              if (files[i].length() > ITechnicalStrings.MAX_COVER_SIZE * 1024) {
                continue;
              }
              final JajukFileFilter filter = ImageFilter.getInstance();
              if (filter.accept(files[i])) {
                if (!bAbsoluteCover
                    && Util.isAbsoluteDefaultCover(fCurrent.getDirectory(), files[i].getName())) {
                  // test the cover is not already used
                  final Cover cover = new Cover(files[i].toURL(), Cover.ABSOLUTE_DEFAULT_COVER);
                  if (!alCovers.contains(cover)) {
                    alCovers.add(cover);
                  }
                  bAbsoluteCover = true;
                } else { // normal local cover
                  final Cover cover = new Cover(files[i].toURL(), Cover.LOCAL_COVER);
                  if (!alCovers.contains(cover)) {
                    alCovers.add(cover);
                  }
                }
              }
            }
          }
          // then we search for web covers online if max
          // connection errors number is not reached or if user
          // already managed to connect
          if (ConfigurationManager.getBoolean(ITechnicalStrings.CONF_COVERS_AUTO_COVER)
              && (CoverView.bOnceConnected || (CoverView.iErrorCounter < ITechnicalStrings.STOP_TO_SEARCH))) {
            try {
              final String sQuery = createQuery(fCurrent);
              Log.debug("Query={{" + sQuery + "}}");
              if (!sQuery.equals("")) {
                // there is not enough information in tags
                // for a web search
                ArrayList<URL> alUrls;
                alUrls = DownloadManager.getRemoteCoversList(sQuery);
                CoverView.bOnceConnected = true;
                // user managed once to connect to the web
                if (alUrls.size() > ITechnicalStrings.MAX_REMOTE_COVERS) {
                  // limit number of remote covers
                  alUrls = new ArrayList<URL>(alUrls
                      .subList(0, ITechnicalStrings.MAX_REMOTE_COVERS));
                }
                Collections.reverse(alUrls);
                // set best results to be displayed first
                final Iterator<URL> it2 = alUrls.iterator();
                // add found covers
                while (it2.hasNext() && (iEventID == iLocalEventID)) {
                  // load each cover (pre-load or post-load)
                  // and stop if a signal has been emitted
                  final URL url = it2.next();
                  try {
                    final Cover cover = new Cover(url, Cover.REMOTE_COVER);
                    // Create a cover with given url ( image
                    // will be really downloaded when
                    // required if no preload)
                    if (!alCovers.contains(cover)) {
                      Log.debug("Found Cover: {{" + url.toString() + "}}");
                      alCovers.add(cover);
                    }
                  } catch (final Exception e) {
                    Log.error(e); // can occur in case of
                    // timeout or error
                    // during cover download
                    if (e instanceof TimeOutException) {
                      CoverView.iErrorCounter++;
                      if (CoverView.iErrorCounter == ITechnicalStrings.STOP_TO_SEARCH) {
                        Log.warn("Too many connection fails, stop to search for covers online");
                        InformationJPanel.getInstance().setMessage(Messages.getString("Error.030"),
                            InformationJPanel.WARNING);
                      }
                    }
                  }
                }
                if (iEventID != iLocalEventID) {
                  // a stop signal has been emitted
                  // from a concurrent thread
                  Log.debug("Download stopped - 1");
                  return;
                }
              }
            } catch (final Exception e) {
              if (e instanceof TimeOutException) {
                Log.warn(e.getMessage());
                // can occur in case of timeout or error during
                // covers list download
                CoverView.iErrorCounter++;
                if (CoverView.iErrorCounter == ITechnicalStrings.STOP_TO_SEARCH) {
                  Log.warn("Too many connection fails," + " stop to search for covers online");
                  InformationJPanel.getInstance().setMessage(Messages.getString("Error.030"),
                      InformationJPanel.WARNING);
                }
              } else {
                Log.error(e);
              }
            }
          }
          if (alCovers.size() == 0) {// add the default cover if none
            // other cover has been found
            alCovers.add(CoverView.coverDefault);
          }
          Collections.sort(alCovers); // sort the list
          Log.debug("Local cover list: {{" + alCovers + "}}");
          if (ConfigurationManager.getBoolean(ITechnicalStrings.CONF_COVERS_SHUFFLE)) {
            // choose a random cover
            index = (int) (Math.random() * alCovers.size());
          } else {
            index = alCovers.size() - 1;
            // current index points to the best available cover
          }
          setFoundText(); // update found text
          displayCurrentCover();
        } else if (JajukEvents.EVENT_ZERO.equals(subject)
            || JajukEvents.EVENT_WEBRADIO_LAUNCHED.equals(subject)) {
          // Ignore this event if a reference file has been set
          if (fileReference != null) {
            return;
          }
          setFoundText("");
          setSizeText("");
          alCovers.clear();
          alCovers.add(CoverView.coverDefault); // add the default cover
          index = 0;
          displayCurrentCover();
          dirReference = null;
        } else if (JajukEvents.EVENT_COVER_CHANGE.equals(subject) && isInCurrentPerspective()) {
          // Ignore this event if a reference file has been set
          if (fileReference != null) {
            return;
          }
          // choose a random cover
          index = (int) (Math.random() * alCovers.size() - 1);
          displayCurrentCover();
        }
      } catch (final Exception e) {
        Log.error(e);
      } finally {
        searching(false); // hide searching icon
      }
    }
  }
}