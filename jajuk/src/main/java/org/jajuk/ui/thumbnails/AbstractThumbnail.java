/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui.thumbnails;

import com.vlsolutions.swing.docking.ShadowBorder;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.TransferHandler;

import org.jajuk.Main;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.actions.ActionBase;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.views.CoverView;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Album thumb represented as album cover + (optionally) others text information
 * and some features like dnd, menu item to play, search cover, album popup
 * display...
 */
public abstract class AbstractThumbnail extends JPanel implements ITechnicalStrings,
    ActionListener, Transferable {

  /** Size */
  int size;

  ArrayList<Item> alSelected = new ArrayList<Item>(100);

  public JLabel jlIcon;

  static private long lDateLastMove;

  static private Point lastPosition;

  private boolean selected = false;

  JPopupMenu jmenu;

  JMenuItem jmiPlay;

  JMenuItem jmiPush;

  JMenuItem jmiDelete;

  JMenuItem jmiPlayShuffle;

  JMenuItem jmiPlayRepeat;

  JMenuItem jmiGetCovers;

  JMenuItem jmiShowPopup;

  JMenuItem jmiCDDBWizard;

  JMenuItem jmiProperties;

  JMenuItem jmiOpenLastFMSite;

  /** Dragging flag used to disable simple click behavior */
  private static boolean bDragging = false;

  /** Current details dialog */
  private static ThumbnailPopup details;

  private static AbstractThumbnail last;

  private static AbstractThumbnail mouseOverItem = null;

  // Number of clicks on the thumb
  private int clickNumber;

  /** Associated file */
  File fCover;

  /** Timer used to launch popup */
  static {
    Timer timerPopup = new Timer(200, new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        try {
          // Close popup ASAP when over none catalog item
          if (mouseOverItem == null) {
            if (details != null) {
              details.dispose();
              details = null;
            }
            last = null;
            // display a popup after n seconds only if item changed
          } else if ((System.currentTimeMillis() - lDateLastMove >= 700) && mouseOverItem != last
              && !bDragging) {
            // Store current item
            last = mouseOverItem;
            // Finally display the popup (Leave if user unselected
            // the option "Show catalog popups"
            if (ConfigurationManager.getBoolean(CONF_SHOW_POPUPS)) {
              mouseOverItem.displayPopup();
            }
          }
          bDragging = false;
        } catch (Exception e) {
          // Make sure not to exit
          Log.error(e);
        }
      }
    });
    timerPopup.start();
  }

  /**
   * Constructor
   * 
   * @param size :
   *          size of the thumbnail
   */
  public AbstractThumbnail(int size) {
    this.size = size;
    setSelected(false);
  }

  /**
   * display a popup over the catalog item
   */
  public void displayPopup() {
    // Display popup out of dispatcher thread as it takes too mush time to
    // execute and we don't risk display concurrency in this popup
    new Thread() {
      public void run() {
        // close popup if any visible
        if (details != null) {
          details.dispose();
          details = null;
        }
        // don't show details if the contextual popup menu
        // is visible
        if (jmenu.isVisible()) {
          return;
        }
        Util.waiting();
        String description = getDescription();
        if (description != null) {
          details = new ThumbnailPopup(description, new Rectangle(jlIcon.getLocationOnScreen(),
              new Dimension(jlIcon.getWidth(), jlIcon.getHeight())), true);
          Util.stopWaiting();
        }
      }
    }.start();
  }

  abstract public void populate() throws Exception;

  /** Return HTML text to display in the popup */
  public abstract String getDescription();

  /**
   * Performs common UI operations for any kind of thumb
   */
  void postPopulate() {
    // Album menu
    jmenu = new JPopupMenu();
    jmiPlay = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_SELECTION));
    jmiPlay.putClientProperty(DETAIL_SELECTION, getItem());
    jmiPush = new JMenuItem(ActionManager.getAction(JajukAction.PUSH_SELECTION));
    jmiPush.addActionListener(this);
    Action actionDeleteFile = ActionManager.getAction(JajukAction.DELETE);
    jmiDelete = new JMenuItem(actionDeleteFile);
    jmiDelete.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiDelete.addActionListener(this);
    jmiPlayShuffle = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_SHUFFLE_SELECTION));
    jmiPlayShuffle.putClientProperty(DETAIL_SELECTION, getItem());
    jmiPlayRepeat = new JMenuItem(ActionManager.getAction(JajukAction.PLAY_REPEAT_SELECTION));
    jmiPlayRepeat.putClientProperty(DETAIL_SELECTION, getItem());
    jmiGetCovers = new JMenuItem(Messages.getString("CatalogView.7"), IconLoader.ICON_COVER_16x16);
    jmiGetCovers.addActionListener(this);
    jmiShowPopup = new JMenuItem(Messages.getString("CatalogView.20"), IconLoader.ICON_POPUP);
    jmiShowPopup.addActionListener(this);
    jmiCDDBWizard = new JMenuItem(ActionManager.getAction(JajukAction.CDDB_SELECTION));
    jmiCDDBWizard.putClientProperty(DETAIL_SELECTION, alSelected);
    jmiProperties = new JMenuItem(ActionManager.getAction(JajukAction.SHOW_PROPERTIES));
    jmiProperties.putClientProperty(DETAIL_SELECTION, getItem());
    ActionBase actionOpenLastFM = ActionManager.getAction(JajukAction.LAUNCH_IN_BROWSER);
    // Change action label
    jmiOpenLastFMSite = new JMenuItem(actionOpenLastFM);
    jmiOpenLastFMSite.setText(Messages.getString("AbstractThumbnail.0"));
    jmiOpenLastFMSite.setToolTipText(Messages.getString("AbstractThumbnail.0"));
    // We add all menu items, each implementation of this class should hide
    // (setVisible(false)) menu items that are not available in their
    // context
    jmenu.add(jmiPlay);
    jmenu.add(jmiPush);
    jmenu.add(jmiDelete);
    jmenu.add(jmiPlayShuffle);
    jmenu.add(jmiPlayRepeat);
    jmenu.add(jmiCDDBWizard);
    jmenu.add(jmiGetCovers);
    jmenu.add(jmiShowPopup);
    jmenu.add(jmiOpenLastFMSite);
    jmenu.add(jmiProperties);

    jlIcon.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent e) {
        // Notify the mouse listener that we are dragging
        bDragging = true;
        JComponent c = (JComponent) e.getSource();
        TransferHandler handler = c.getTransferHandler();
        handler.exportAsDrag(c, e, TransferHandler.COPY);
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        lDateLastMove = System.currentTimeMillis();
        lastPosition = e.getPoint();
      }

    });

    jlIcon.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          handlePopup(e);
        } else if ((e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == 0) {
          // Leave if already dragging
          if (bDragging) {
            return;
          }
          // Left click
          if (e.getButton() == MouseEvent.BUTTON1 && e.getSource() == jlIcon) {
            // if second click (item already selected), play
            // We have to check time between last two click as a
            // single clicks is
            // managed by both mouse listeners
            if (selected && clickNumber > 0) {
              launch();
            }
            clickNumber++;
          }
        }
      }

      public void mouseEntered(MouseEvent e) {
        mouseOverItem = AbstractThumbnail.this;
      }

      public void mouseExited(MouseEvent e) {
        // Consider an exit only if mouse really moved to avoid
        // closing popup when popup appears over the mouse cursor
        // (then, a mouseExit event is thrown)
        if (!e.getPoint().equals(lastPosition) &&
        // Don't close popup if user is still over it
            !(details != null && details.contains(e.getPoint()))) {
          mouseOverItem = null;
        }
      }

      public void mouseReleased(MouseEvent e) {
        // Leave if already dragging
        if (bDragging) {
          return;
        }
        if (e.isPopupTrigger()) {
          handlePopup(e);
        }
      }

      public void handlePopup(final MouseEvent e) {
        if (e.getSource() == jlIcon) {
          // Show contextual menu
          jmenu.show(jlIcon, e.getX(), e.getY());
          // Hide any details frame
          if (details != null) {
            details.dispose();
            details = null;
          }
        }
      }

    });
  }

  /**
   * 
   * @param b
   */
  public void setSelected(boolean b) {
    selected = b;
    // Reset number of clicks
    if (!b) {
      clickNumber = 0;
    }
    // Add a shadow for selected items
    if (b) {
      setBorder(new ShadowBorder());
    } else {
      // add an empty border of the same size than the border to avoid
      // image moves when setting borders
      setBorder(BorderFactory.createEmptyBorder(3, 2, 5, 5));
    }
  }

  public abstract void launch();

  /**
   * If the thumb represents something (album, author...) known in the
   * collection, the implementation of this method should return the associated
   * item
   * 
   * @return the collection item
   */
  public abstract Item getItem();

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    // Menu items
    if (e.getSource() == jmiDelete) {
      Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(getItem());
      for (Track track : tracks) {
        org.jajuk.base.File file = track.getPlayeableFile(false);
        if (file != null) {
          alSelected.add(file);
        }
      }
    } else if (e.getSource() == jmiGetCovers) {
      // This item is enabled only for albums
      new Thread() {
        public void run() {
          JDialog jd = new JDialog(Main.getWindow(), Messages.getString("CatalogView.18"));
          org.jajuk.base.File file = null;
          Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(getItem());
          if (tracks.size() > 0) {
            Track track = tracks.iterator().next();
            file = track.getPlayeableFile(false);
          }
          CoverView cv = null;
          if (file != null) {
            cv = new CoverView(file);
            cv.setID("catalog/0");
            cv.initUI();
            jd.add(cv);
            jd.setSize(400, 450);
            jd.setLocationByPlatform(true);
            jd.setVisible(true);
          } else {
            Messages.showErrorMessage(166);
          }
        }
      }.start();
    } else if (e.getSource() == jmiShowPopup) {
      this.displayPopup();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
   */
  public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
   */
  public DataFlavor[] getTransferDataFlavors() {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
   */
  public boolean isDataFlavorSupported(DataFlavor flavor) {
    return false;
  }

}
