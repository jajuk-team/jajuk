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
package org.jajuk.ui.perspectives;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.widgets.PerspectiveBarJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Perspectives Manager
 */
public class PerspectiveManager implements ITechnicalStrings {
  /** Current perspective */
  private static IPerspective currentPerspective = null;

  /** Perspective name -> perspective */
  private static Map<String, IPerspective> hmNameInstance = new HashMap<String, IPerspective>(
      10);

  /** perspective, required despite the Map above
	in order to keep the order of the perspectives as the 
	order in the Map is undefined */
  private static Set<IPerspective> perspectives = new LinkedHashSet<IPerspective>(10);

  /**
   * Reset registered perspectives
   * 
   */
  private static void reset() {
    perspectives.clear();
    hmNameInstance.clear();
  }

  /**
   * Load configuration file
   * 
   * @throws JajukException
   */
  public static void load() throws JajukException {
    registerDefaultPerspectives();
    if (Main.isUpgradeDetected()) {
      // upgrade message
      Messages.showInfoMessage(Messages.getString("Note.0"));
      // force loading of defaults perspectives
      for (IPerspective perspective : getPerspectives()) {
        // Remove current conf file to force using default file from the
        // jar
        File loadFile = Util.getConfFileByPath(perspective.getClass().getSimpleName() + ".xml");
        if (loadFile.exists()) {
          loadFile.delete();
        }
      }
    }
    // Load each perspective
    try {
      for (IPerspective perspective : getPerspectives()) {
        perspective.load();
      }
    } catch (Exception e) {
      throw new JajukException(108, e);
    }
  }

  /**
   * Begins management
   */
  public static void init() {
    // Use Simple perspective as a default
    IPerspective perspective = hmNameInstance.get(SimplePerspective.class.getName());
    // If it is a crash recover, force physical perspective to avoid
    // being locked on a buggy perspective like Information
    if (!Main.isCrashRecover()) {
      String sPerspective = Main.getDefaultPerspective();
      // Check if a default perspective is forced
      if (sPerspective == null) {
        sPerspective = ConfigurationManager.getProperty(CONF_PERSPECTIVE_DEFAULT);
        // no? take the configuration ( user last perspective)
      }
      perspective = hmNameInstance.get(sPerspective);
      // If perspective is no more known, take first perspective found
      if (perspective == null) {
        perspective = perspectives.iterator().next();
      }
    }
    setCurrentPerspective(perspective);
  }

  /*
   * @see org.jajuk.ui.perspectives.IPerspectiveManager#getCurrentPerspective()
   */
  public static IPerspective getCurrentPerspective() {
    return PerspectiveManager.currentPerspective;
  }

  /*
   * @see org.jajuk.ui.perspectives.IPerspectiveManager#setCurrentPerspective(Perspective)
   */
  public static void setCurrentPerspective(final IPerspective perspective) {
    Util.waiting();
    // views display
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        perspective.setAsBeenSelected(true);
        PerspectiveManager.currentPerspective = perspective;
        for (IView view : perspective.getViews()) {
          if (!view.isPopulated()) {
            try {
              view.initUI();
            } catch (Exception e) {
              Log.error(e);
            }
            view.setIsPopulated(true);
          }
          // Perform specific view operation at perspective display
          view.onPerspectiveSelection();
        }
        // Clear the perspective panel
        JPanel perspectivePanel = Main.perspectivePanel;
        if (perspectivePanel.getComponentCount() > 0) {
          Component[] components = perspectivePanel.getComponents();
          for (int i = 0; i < components.length; i++) {
              perspectivePanel.remove(components[i]);
          }
        }
        Main.perspectivePanel.add(perspective.getContentPane(), BorderLayout.CENTER);
        // refresh UI
        Main.perspectivePanel.revalidate();
        Main.perspectivePanel.repaint();
        // Select right item in perspective selector
        PerspectiveBarJPanel.getInstance().setActivated(perspective);
        // store perspective selection
        ConfigurationManager.setProperty(CONF_PERSPECTIVE_DEFAULT, perspective.getID());
        Util.stopWaiting();
        // Emit a event
        ObservationManager.notify(new Event(EventSubject.EVENT_PERPECTIVE_CHANGED,
            ObservationManager.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
      }
    });
  }

  /**
   * Set current perspective
   * 
   * @param sPerspectiveName
   */
  public static void setCurrentPerspective(String sPerspectiveID) {
    IPerspective perspective = hmNameInstance.get(sPerspectiveID);
    if (perspective == null) {
      perspective = perspectives.iterator().next();
    }
    setCurrentPerspective(perspective);
  }

  /**
   * Get all perspectives
   * 
   * @return all perspectives as a collection
   */
  public static Set<IPerspective> getPerspectives() {
    return perspectives;
  }

  /**
   * Get a perspective by ID or null if none associated perspective found
   * 
   * @param sID
   *          perspective ID
   * @return perspective
   */
  public static IPerspective getPerspective(String sID) {
    return hmNameInstance.get(sID);
  }

  /**
   * Saves perspectives and views position in the perspective.xml file Must be
   * executed in EDT to avoid dead locks on getComponent()
   */
  public static void commit() throws Exception {
    for (IPerspective perspective : getPerspectives()) {
      perspective.commit();
    }
  }

  /**
   * Register default perspective configuration. Will be overwritten by
   * perspective.xml parsing if it exists
   * <p>
   * We set an icon for each perspective, resizing it if user selected another
   * size than 40x40
   * </p>
   * 
   */
  public static void registerDefaultPerspectives() {
    reset();
    int iconSize = ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE);
    IPerspective perspective = null;
    // Simple perspective
    perspective = new SimplePerspective();
    ImageIcon icon = IconLoader.ICON_PERSPECTIVE_SIMPLE;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Files perspective
    perspective = new FilesPerspective();
    icon = IconLoader.ICON_PERSPECTIVE_PHYSICAL;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Tracks perspective
    perspective = new TracksPerspective();
    icon = IconLoader.ICON_PERSPECTIVE_LOGICAL;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Catalog perspective
    perspective = new AlbumsPerspective();
    icon = IconLoader.ICON_PERSPECTIVE_CATALOG;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Playlists perspective
    perspective = new PlaylistsPerspective();
    icon = IconLoader.ICON_PERSPECTIVE_PLAYLISTS;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Display perspective
    perspective = new DisplayPerspective();
    icon = IconLoader.ICON_PERSPECTIVE_PLAYER;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Information perspective
    perspective = new InfoPerspective();
    icon = IconLoader.ICON_PERSPECTIVE_INFORMATION;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Configuration perspective
    perspective = new ConfigurationPerspective();
    icon = IconLoader.ICON_PERSPECTIVE_CONFIGURATION;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Stats perspective
    perspective = new StatPerspective();
    icon = IconLoader.ICON_PERSPECTIVE_STATISTICS;
    if (ConfigurationManager.getInt(CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = Util.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);
  }

  /**
   * Register a new perspective
   * 
   * @param perspective
   * @return registered perspective
   */
  public static IPerspective registerPerspective(IPerspective perspective) {
    hmNameInstance.put(perspective.getID(), perspective);
    perspectives.add(perspective);
    return perspective;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.mozilla.xpcom.IAppFileLocProvider#getFile(java.lang.String,
   *      boolean[])
   */
  public File getFile(String prop, boolean[] persistent) {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.mozilla.xpcom.IAppFileLocProvider#getFiles(java.lang.String)
   */
  public File[] getFiles(String prop) {
    return null;
  }
}
