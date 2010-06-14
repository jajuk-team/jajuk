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
package org.jajuk.ui.perspectives;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.services.startup.StartupGUIService;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.widgets.PerspectiveBarJPanel;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Perspectives Manager.
 */
public final class PerspectiveManager {

  /** Current perspective. */
  private static IPerspective currentPerspective = null;

  /** Perspective name -> perspective. */
  private static Map<String, IPerspective> hmNameInstance = new HashMap<String, IPerspective>(10);

  /** perspective, required despite the Map above in order to keep the order of the perspectives as the order in the Map is undefined. */
  private static Set<IPerspective> perspectives = new LinkedHashSet<IPerspective>(10);

  /** List of perspectives that need reset from version n-1. */
  // None perspective to reset from 1.6 to 1.7
  private static String[] perspectivesToReset = new String[] {};

  /**
   * private constructor to avoid instantiating utility class.
   */
  private PerspectiveManager() {
  }

  /**
   * Reset registered perspectives.
   */
  private static void reset() {
    perspectives.clear();
    hmNameInstance.clear();
  }

  /**
   * Load configuration file.
   * 
   * @throws JajukException the jajuk exception
   */
  public static void load() throws JajukException {
    registerDefaultPerspectives();
    if (UpgradeManager.isUpgradeDetected()) {
      /*
       * Force loading of defaults perspectives
       * 
       * - If this is a migration from a version n-i with i >1, we force a full reset
       * 
       * - If it is a migration from version n-1, only reset perspectives with changes (see
       * perspectiveList)
       */
      if (UpgradeManager.doNeedPerspectiveResetAtUpgrade()) {
        // upgrade message
        Messages.showInfoMessage(Messages.getString("Note.0"));
        try {
          resetPerspectives();
        } catch (IOException e) {
          Log.error(e);
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

  private static void resetPerspectives() throws IOException {
    List<String> perspectivesToReset = Arrays.asList(PerspectiveManager.perspectivesToReset);
    for (IPerspective perspective : getPerspectives()) {
      String className = perspective.getClass().getSimpleName();
      // Remove current conf file to force using default file from the
      // jar
      File loadFile = SessionService.getConfFileByPath(className + ".xml");
      if (loadFile.exists()
          && (perspectivesToReset.contains(className) || UpgradeManager.isMajorMigration())) {
        UtilSystem.deleteFile(loadFile);
      }
    }
  }

  /**
   * Begins management.
   */
  public static void init() {
    // Use Simple perspective as a default
    IPerspective perspective = hmNameInstance.get(SimplePerspective.class.getName());
    String sPerspective = StartupGUIService.getDefaultPerspective();
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
  /**
   * Gets the current perspective.
   * 
   * @return the current perspective
   */
  public static IPerspective getCurrentPerspective() {
    return PerspectiveManager.currentPerspective;
  }

  /*
   * @see org.jajuk.ui.perspectives.IPerspectiveManager#setCurrentPerspective(Perspective)
   */
  /**
   * Sets the current perspective.
   * 
   * @param perspective the new current perspective
   */
  protected static void setCurrentPerspective(final IPerspective perspective) {
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
        JPanel perspectivePanel = JajukMainWindow.getInstance().getPerspectivePanel();
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
        ObservationManager.notify(new JajukEvent(JajukEvents.PERSPECTIVE_CHANGED,
            ObservationManager.getDetailsLastOccurence(JajukEvents.FILE_LAUNCHED)));
      }
    });
  }

  /**
   * Set current perspective.
   * 
   * @param sPerspectiveID DOCUMENT_ME
   */
  public static void setCurrentPerspective(String sPerspectiveID) {
    IPerspective perspective = hmNameInstance.get(sPerspectiveID);
    if (perspective == null) {
      perspective = perspectives.iterator().next();
    }
    setCurrentPerspective(perspective);
  }

  /**
   * Get all perspectives.
   * 
   * @return all perspectives as a collection
   */
  public static Set<IPerspective> getPerspectives() {
    return perspectives;
  }

  /**
   * Saves perspectives and views position in the perspective.xml file Must be
   * executed in EDT to avoid dead locks on getComponent()
   * 
   * @throws Exception the exception
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
   */
  private static void registerDefaultPerspectives() {
    reset();

    // Simple perspective
    registerPerspective(new SimplePerspective());

    // Files perspective
    registerPerspective(new FilesPerspective());

    // Tracks perspective
    registerPerspective(new TracksPerspective());

    // Catalog perspective
    registerPerspective(new AlbumsPerspective());

    // Playlists perspective
    registerPerspective(new PlaylistsPerspective());

    // Display perspective
    registerPerspective(new DisplayPerspective());

    // Information perspective
    registerPerspective(new InfoPerspective());

    // Configuration perspective
    registerPerspective(new ConfigurationPerspective());

    // Stats perspective
    registerPerspective(new StatPerspective());
  }

  /**
   * Register a new perspective.
   * 
   * @param perspective DOCUMENT_ME
   * 
   * @return registered perspective
   */
  private static IPerspective registerPerspective(IPerspective perspective) {
    hmNameInstance.put(perspective.getID(), perspective);
    perspectives.add(perspective);
    return perspective;
  }
}
