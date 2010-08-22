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
package org.jajuk.services.startup;

import ext.JSplash;

import java.awt.SystemTray;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.windows.JajukSystray;
import org.jajuk.ui.wizard.TipOfTheDayWizard;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UpgradeManager;
import org.jajuk.util.log.Log;

/**
 * Startup facilities for GUI part.
 */
public class StartupGUIService {

  /** default perspective to choose, if null, we take the configuration one. */
  private static String sPerspective;

  /** splash screen. */
  private static JSplash sc;

  /**
   * Instantiates a new startup gui service.
   */
  private StartupGUIService() {
    // private constructor to hide it from the outside
  }

  /**
   * Gets the default perspective.
   * 
   * @return Returns the sPerspective.
   */
  public static String getDefaultPerspective() {
    return sPerspective;
  }

  /**
   * Sets the default perspective.
   * 
   * @param perspective The sPerspective to set.
   */
  public static void setDefaultPerspective(final String perspective) {
    sPerspective = perspective;
  }

  /**
   * Launch splash screen.
   * DOCUMENT_ME
   * 
   * @throws InterruptedException the interrupted exception
   * @throws InvocationTargetException the invocation target exception
   */
  public static void launchSplashScreen() throws InterruptedException, InvocationTargetException {
    // Launch splashscreen. Depends on: log.setVerbosity,
    // configurationManager.load (for local)
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        sc = new JSplash(Const.IMAGES_SPLASHSCREEN, true, true, false, Const.JAJUK_COPYRIGHT,
            Const.JAJUK_VERSION + " \"" + Const.JAJUK_CODENAME + "\"" + " "
                + Const.JAJUK_VERSION_DATE, FontManager.getInstance().getFont(JajukFont.SPLASH));
        sc.setProgress(0, Messages.getString("SplashScreen.0"));
        // Actually show the splashscreen only if required
        if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_MAIN_WINDOW
            || Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_FULLSCREEN) {
          sc.splashOn();
        }
      }
    });
  }

  /**
   * Fire step one over.
   * DOCUMENT_ME
   */
  public static void fireStepOneOver() {
    if (sc != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          sc.setProgress(5, Messages.getString("Main.22"));
        }
      });
    }
  }

  /**
   * Fire step two over.
   * DOCUMENT_ME
   */
  public static void fireStepTwoOver() {
    if (sc != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          sc.setProgress(10, Messages.getString("SplashScreen.1"));
        }
      });
    }
  }

  /**
   * Fire step three over.
   * DOCUMENT_ME
   */
  public static void fireStepThreeOver() {
    if (sc != null) {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          sc.setProgress(70, Messages.getString("SplashScreen.2"));
        }
      });
    }
  }
    

  /**
   * Startup over.
   * DOCUMENT_ME
   */
  public static void startupOver() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (sc != null) {
          sc.setProgress(100);
          sc.splashOff();
          sc = null;
        }
      }
    });

  }

  /**
   * Display the right window according to configuration and handles problems.
   */
  public static void launchUI() {
    // ui init
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        try {
          // Start up action manager
          ActionManager.getInstance();

          // Display progress
          sc.setProgress(80, Messages.getString("SplashScreen.3"));

          // show window according to startup mode
          if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_MAIN_WINDOW) {
            JajukMainWindow mainWindow = JajukMainWindow.getInstance();
            mainWindow.getWindowStateDecorator().display(true);
          }
          // Show full screen according to startup mode. If the fullscreen mode
          // is no more available (because the user changed the platform for
          // ie), force the main window mode
          else if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_FULLSCREEN) {
            // important that the fs frame is shown on the correct display
            JajukMainWindow.getInstance().applyStoredSize();
            // Display progress
            sc.setProgress(80, Messages.getString("SplashScreen.3"));
            ActionManager.getAction(JajukActions.FULLSCREEN_JAJUK).perform(null);
          }
          // Start the slimbar if required
          else if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_SLIMBAR_TRAY) {
            ActionManager.getAction(JajukActions.SLIM_JAJUK).perform(null);
          }
          // In all cases, display the tray is user didn't force to hide it and
          // if the platform supports it
          if (Conf.getBoolean(Const.CONF_SHOW_SYSTRAY) && SystemTray.isSupported()) {
            JajukSystray tray = JajukSystray.getInstance();
            tray.getWindowStateDecorator().display(true);
          }
          // Display tip of the day if required (not at the first
          // session to avoid displaying too many windows once)
          if (Conf.getBoolean(Const.CONF_SHOW_TIP_ON_STARTUP) && !UpgradeManager.isFirstSession()) {
            final TipOfTheDayWizard tipsView = new TipOfTheDayWizard();
            tipsView.setLocationRelativeTo(JajukMainWindow.getInstance());
            tipsView.setVisible(true);
          }
        } catch (final Exception e) { 
          // last chance to catch any error for
          // logging purpose
          Log.error(106, e);
        } finally {
          if (sc != null) {
            // Display progress
            sc.setProgress(100);
            sc.splashOff();

            // free resources
            sc = null;
          }
          // Notify any first time wizard to startup refresh
          synchronized (StartupCollectionService.canLaunchRefresh) {
            StartupCollectionService.canLaunchRefresh.notify();
          }
        }
      }
    });

  }

}
