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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.widgets.PerspectiveBarJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Perspectives Manager
 */
public final class PerspectiveManager {
  /** Current perspective */
  private static IPerspective currentPerspective = null;

  /** Perspective name -> perspective */
  private static Map<String, IPerspective> hmNameInstance = new HashMap<String, IPerspective>(10);

  /**
   * perspective, required despite the Map above in order to keep the order of
   * the perspectives as the order in the Map is undefined
   */
  private static Set<IPerspective> perspectives = new LinkedHashSet<IPerspective>(10);

  /** List of perspectives that need reset from version n-1 */
  // None perspective to reset from 1.6 to 1.7
  private static String[] perspectivesToReset = new String[] {};

  /**
   * private constructor to avoid instantiating utility class
   */
  private PerspectiveManager() {
  }

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
    if (UpgradeManager.isUpgradeDetected()) {
      /*
       * Force loading of defaults perspectives - If this is a migration from a
       * version n-i with i >1, we force a full reset - If it is a migration
       * from version n-1, only reset perspectives with changes (see
       * perspectiveList)
       */

      if (UpgradeManager.isOldMigration()) {
        // upgrade message
        Messages.showInfoMessage(Messages.getString("Note.0"));
      }

      List<String> perspectivesToReset = Arrays.asList(PerspectiveManager.perspectivesToReset);
      for (IPerspective perspective : getPerspectives()) {
        String className = perspective.getClass().getSimpleName();
        // Remove current conf file to force using default file from the
        // jar
        File loadFile = UtilSystem.getConfFileByPath(className + ".xml");
        if (loadFile.exists()
            && (perspectivesToReset.contains(className) || UpgradeManager.isOldMigration())) {
          if (!loadFile.delete()) {
            Log.warn("Could not delete file " + loadFile);
          }
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
    String sPerspective = Main.getDefaultPerspective();
    // Check if a default perspective is forced
    if (sPerspective == null) {
      sPerspective = Conf.getString(Const.CONF_PERSPECTIVE_DEFAULT);
      // no? take the configuration ( user last perspective)
    }
    perspective = hmNameInstance.get(sPerspective);
    // If perspective is no more known, take first perspective found
    if (perspective == null) {
      perspective = perspectives.iterator().next();
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
    UtilGUI.waiting();
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
        JPanel perspectivePanel = Main.getPerspectivePanel();
        if (perspectivePanel.getComponentCount() > 0) {
          Component[] components = perspectivePanel.getComponents();
          for (Component element : components) {
            perspectivePanel.remove(element);
          }
        }
        perspectivePanel.add(perspective.getContentPane(), BorderLayout.CENTER);
        // refresh UI
        perspectivePanel.revalidate();
        perspectivePanel.repaint();
        // Select right item in perspective selector
        PerspectiveBarJPanel.getInstance().setActivated(perspective);
        // store perspective selection
        Conf.setProperty(Const.CONF_PERSPECTIVE_DEFAULT, perspective.getID());
        UtilGUI.stopWaiting();
        // Emit a event
        ObservationManager.notify(new Event(JajukEvents.PERPECTIVE_CHANGED, ObservationManager
            .getDetailsLastOccurence(JajukEvents.FILE_LAUNCHED)));
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
    int iconSize = Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE);
    IPerspective perspective = null;
    // Simple perspective
    perspective = new SimplePerspective();
    ImageIcon icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_SIMPLE);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Files perspective
    perspective = new FilesPerspective();
    icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_PHYSICAL);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Tracks perspective
    perspective = new TracksPerspective();
    icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_LOGICAL);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Catalog perspective
    perspective = new AlbumsPerspective();
    icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_CATALOG);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Playlists perspective
    perspective = new PlaylistsPerspective();
    icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_PLAYLISTS);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Display perspective
    perspective = new DisplayPerspective();
    icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_PLAYER);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Information perspective
    perspective = new InfoPerspective();
    icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_INFORMATION);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Configuration perspective
    perspective = new ConfigurationPerspective();
    icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_CONFIGURATION);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
    }
    perspective.setIcon(icon);
    registerPerspective(perspective);

    // Stats perspective
    perspective = new StatPerspective();
    icon = IconLoader.getIcon(JajukIcons.PERSPECTIVE_STATISTICS);
    if (Conf.getInt(Const.CONF_PERSPECTIVE_ICONS_SIZE) != 40) {
      icon = UtilGUI.getResizedImage(icon, iconSize, iconSize);
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

}
