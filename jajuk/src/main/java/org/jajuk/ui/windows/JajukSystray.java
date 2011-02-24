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

package org.jajuk.ui.windows;

import ext.JXTrayIcon;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.actions.MuteAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.PlayerStateMediator;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.widgets.CommandJPanel;
import org.jajuk.ui.widgets.JajukInformationDialog;
import org.jajuk.ui.widgets.SearchBox;
import org.jajuk.ui.widgets.SizedJMenuItem;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Jajuk systray <br>
 * Extends CommandJPanel for volume slider heritage only.
 */
public class JajukSystray extends CommandJPanel implements IJajukWindow {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** State decorator. */
  private WindowStateDecorator decorator;

  // Systray variables
  /** DOCUMENT_ME. */
  SystemTray stray;

  /** DOCUMENT_ME. */
  JXTrayIcon trayIcon;

  /** DOCUMENT_ME. */
  JPopupMenu jmenu;

  /** DOCUMENT_ME. */
  JMenuItem jmiExit;

  /** DOCUMENT_ME. */
  JMenuItem jmiMute;

  /** DOCUMENT_ME. */
  JMenuItem jmiShuffle;

  /** DOCUMENT_ME. */
  JMenuItem jmiBestof;

  /** DOCUMENT_ME. */
  JMenuItem jmiDJ;

  /** DOCUMENT_ME. */
  JMenuItem jmiNovelties;

  /** DOCUMENT_ME. */
  JMenuItem jmiFinishAlbum;

  /** DOCUMENT_ME. */
  JMenuItem jmiPlayPause;

  /** DOCUMENT_ME. */
  JMenuItem jmiStop;

  /** DOCUMENT_ME. */
  JMenuItem jmiPrevious;

  /** DOCUMENT_ME. */
  JMenuItem jmiNext;

  /** DOCUMENT_ME. */
  JMenu jmAmbience;

  /** Self instance singleton. */
  private static JajukSystray jsystray;

  /** HTML Tooltip. */
  JajukInformationDialog balloon;

  /**
   * Checks if is loaded.
   * 
   * @return whether the tray is loaded
   */
  public static boolean isLoaded() {
    return (jsystray != null);
  }

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static JajukSystray getInstance() {
    if (jsystray == null) {
      jsystray = new JajukSystray();
      jsystray.decorator = new WindowStateDecorator(jsystray) {
        @Override
        public void specificAfterHidden() {
          if (jsystray != null) {
            jsystray.closeSystray();
            jsystray = null;
          }
        }

        @Override
        public void specificBeforeHidden() {
          // Nothing particular to do here
        }

        @Override
        public void specificAfterShown() {
          // Force initial message refresh
          UtilFeatures.updateStatus(jsystray);
        }

        @Override
        public void specificBeforeShown() {
          // Nothing particular to do here
        }
      };
    }
    return jsystray;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.widgets.JajukWindow#getWindowStateDecorator()
   */
  @Override
  public WindowStateDecorator getWindowStateDecorator() {
    return decorator;
  }

  /**
   * Systray constructor.
   */
  public JajukSystray() {
    super();
    if (SystemTray.isSupported()) {
      stray = SystemTray.getSystemTray();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.widgets.CommandJPanel#initUI()
   */
  @Override
  public final void initUI() {
    // Instanciate the PlayerStateMediator to listen for player basic controls
    PlayerStateMediator.getInstance();
    jmenu = new JPopupMenu(Messages.getString("JajukWindow.3"));
    jmiExit = new JMenuItem(ActionManager.getAction(JajukActions.EXIT));

    // force icon to be display in 16x16
    jmiMute = new SizedJMenuItem(ActionManager.getAction(JajukActions.MUTE_STATE));
    jmiMute.addMouseWheelListener(this);

    jmiShuffle = new SizedJMenuItem(ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL));

    jmiBestof = new SizedJMenuItem(ActionManager.getAction(JajukActions.BEST_OF));
    jmiDJ = new SizedJMenuItem(ActionManager.getAction(JajukActions.DJ));
    jmiFinishAlbum = new SizedJMenuItem(ActionManager.getAction(JajukActions.FINISH_ALBUM));
    jmiNovelties = new SizedJMenuItem(ActionManager.getAction(JajukActions.NOVELTIES));

    jmiPlayPause = new SizedJMenuItem(ActionManager.getAction(JajukActions.PAUSE_RESUME_TRACK));
    jmiStop = new SizedJMenuItem(ActionManager.getAction(JajukActions.STOP_TRACK));
    jmiPrevious = new SizedJMenuItem(ActionManager.getAction(JajukActions.PREVIOUS_TRACK));
    jmiNext = new SizedJMenuItem(ActionManager.getAction(JajukActions.NEXT_TRACK));

    JLabel jlTitle = new JLabel("Jajuk");
    jlTitle.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          // show main window if it is not visible and hide it if it is visible
          WindowState mainWindowState = JajukMainWindow.getInstance().getWindowStateDecorator()
              .getWindowState();
          boolean bShouldDisplayMainWindow = !(mainWindowState == WindowState.BUILT_DISPLAYED);
          JajukMainWindow.getInstance().getWindowStateDecorator().display(bShouldDisplayMainWindow);
        }
      }

    });
    jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD_TITLE));
    SearchBox searchBox = new SearchBox();
    JPanel jpTitle = new JPanel(new MigLayout("ins 5", "[][grow]"));
    jpTitle.add(jlTitle, "left,gapx 20px");
    jpTitle.add(searchBox, "right,grow");

    // Ambiences menu
    Ambience defaultAmbience = AmbienceManager.getInstance().getSelectedAmbience();
    jmAmbience = new JMenu(Messages.getString("JajukWindow.36")
        + " "
        + ((defaultAmbience == null) ? Messages.getString("DigitalDJWizard.64")
            : defaultAmbience.getName()));
    populateAmbiences();

    jmenu.add(jpTitle);
    jmenu.addSeparator();
    jmenu.add(jmAmbience);
    jmenu.addSeparator();
    jmenu.add(jmiPlayPause);
    jmenu.add(jmiStop);
    jmenu.add(jmiPrevious);
    jmenu.add(jmiNext);
    jmenu.addSeparator();
    jmenu.add(jmiShuffle);
    jmenu.add(jmiBestof);
    jmenu.add(jmiDJ);
    jmenu.add(jmiNovelties);
    jmenu.add(jmiFinishAlbum);
    jmenu.addSeparator();
    jmenu.add(jmiMute);
    jmenu.addSeparator();
    jmenu.add(jmiExit);
    // Add a row under Linux to fix an issue : sometimes, when left-clicking on
    // the tray, the exit menu item is executed and then close Jajuk accidently
    if (UtilSystem.isUnderLinux()) {
      jmenu.add("");
    }

    trayIcon = new JXTrayIcon(IconLoader.getIcon(JajukIcons.TRAY).getImage());
    if (!UtilSystem.isUnderLinux()) {
      // auto-resize looks OK under Windows but is ugly under Linux/KDE
      trayIcon.setImageAutoSize(true);
    }
    trayIcon.addMouseMotionListener(new MouseMotionAdapter() {

      long dateLastMove = 0;

      @Override
      public void mouseMoved(MouseEvent e) {
        // [PERF] Consider only a single event per second
        if (System.currentTimeMillis() - dateLastMove < 1000) {
          return;
        }
        dateLastMove = System.currentTimeMillis();
        String title = QueueModel.getCurrentFileTitle();
        if (balloon != null && balloon.isVisible()) {
          return;
        }
        // Useful for #1582 ([Linux] Void entry in task bar for information dialog)
        if (UtilGUI.getActiveWindow() != null //can happen if tray only
            && UtilGUI.getActiveWindow().equals(JajukMainWindow.getInstance())) {
          balloon = new JajukInformationDialog(title, null);
        } else {
          balloon = new JajukInformationDialog(title, UtilGUI.getActiveWindow());
        }
        Point location = new Point(e.getX() - balloon.getWidth(), e.getY()
            - (20 + balloon.getHeight()));
        balloon.setLocation(location);
        balloon.display();
      }
    });
    trayIcon.setJPopuMenu(jmenu);
    // Note that under OSX, popup gesture recognition is inverted : a left click return true
    if (UtilSystem.isUnderOSX()) {

      // Don't use a JajukMouseAdapter here because tray has specific behavior under OSX
      trayIcon.addMouseListener(new MouseAdapter() {

        // Under OSX, the event to consider is PRESSED, not RELEASED, 
        // see http://developer.apple.com/mac/library/documentation/Java/Conceptual/Java14Development/07-NativePlatformIntegration/NativePlatformIntegration.html
        @Override
        public void mousePressed(MouseEvent e) {
          if (!e.isPopupTrigger()) { //we invert here because it is a systray item
            // popup gesture recognized, display the jdialog
            trayIcon.showJPopupMenu(e);
          } else {
            showHideWindow(e);
          }
        }
      });

    } else {
      trayIcon.addMouseListener(new JajukMouseAdapter() {

        @Override
        public void handleActionSingleClick(MouseEvent e) {
          showHideWindow(e);
        }

        @Override
        public void handlePopup(final MouseEvent e) {
          trayIcon.showJPopupMenu(e);
        }
      });
    }
    try {
      stray.add(trayIcon);
    } catch (AWTException e) {
      Log.error(e);
      return;
    }
    // Register needed events
    ObservationManager.register(this);

  }

  /**
   * Invert current window visibility with a left click on the tray icon.
   * 
   * @param e DOCUMENT_ME
   */
  private void showHideWindow(MouseEvent e) {
    WindowStateDecorator windowDecorator = null;
    if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_MAIN_WINDOW) {
      windowDecorator = JajukMainWindow.getInstance().getWindowStateDecorator();
    } else if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_SLIMBAR_TRAY) {
      windowDecorator = JajukSlimbar.getInstance().getWindowStateDecorator();
    } else if (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_FULLSCREEN) {
      windowDecorator = JajukFullScreenWindow.getInstance().getWindowStateDecorator();
    }

    // show Main if no other found, i.e. only Systray is displayed
    if (windowDecorator == null) {
      windowDecorator = JajukMainWindow.getInstance().getWindowStateDecorator();
    }

    // Invert visibility for the current window
    boolean bShouldDisplay = !(windowDecorator.getWindowState() == WindowState.BUILT_DISPLAYED);
    windowDecorator.display(bShouldDisplay);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.widgets.CommandJPanel#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAYER_PLAY);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.AMBIENCES_CHANGE);
    eventSubjectSet.add(JajukEvents.AMBIENCES_SELECTION_CHANGE);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public final void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
          jmiPlayPause.setEnabled(true);
          jmiStop.setEnabled(true);
          jmiNext.setEnabled(true);
          jmiPrevious.setEnabled(true);
          jmiFinishAlbum.setEnabled(true);
        } else if (JajukEvents.PLAYER_STOP.equals(subject)) {
          // Enable the play button to allow restarting the queue but disable if
          // the queue is void
          boolean bQueueNotVoid = (QueueModel.getQueue().size() > 0);
          jmiPlayPause.setEnabled(bQueueNotVoid);
          jmiNext.setEnabled(bQueueNotVoid);
          jmiPrevious.setEnabled(bQueueNotVoid);
          jmiStop.setEnabled(false);
          jmiFinishAlbum.setEnabled(false);
        } else if (JajukEvents.ZERO.equals(subject)) {
          jmiPlayPause.setEnabled(false);
          jmiStop.setEnabled(false);
          jmiNext.setEnabled(false);
          jmiPrevious.setEnabled(false);
          jmiFinishAlbum.setEnabled(false);
        } else if (JajukEvents.PLAYER_PLAY.equals(subject)) {
          jmiPlayPause.setEnabled(true);
          jmiStop.setEnabled(true);
          jmiNext.setEnabled(true);
          jmiFinishAlbum.setEnabled(true);
        } else if (JajukEvents.VOLUME_CHANGED.equals(event.getSubject())) {
          JajukSystray.super.update(event);
        } else if (JajukEvents.AMBIENCES_CHANGE.equals(subject)
            || JajukEvents.AMBIENCES_SELECTION_CHANGE.equals(subject)) {
          Ambience ambience = AmbienceManager.getInstance().getSelectedAmbience();
          if (ambience != null) {
            jmAmbience.setText(Messages.getString("JajukWindow.36") + " "
                + AmbienceManager.getInstance().getSelectedAmbience().getName());
          } else {
            jmAmbience.setText(Messages.getString("JajukWindow.37"));
          }
          populateAmbiences();
        }
      }
    });
  }

  /**
   * Hide systray.
   */
  public void closeSystray() {
    if (stray != null && trayIcon != null) {
      stray.remove(trayIcon);
    }
  }

  /**
   * Populate ambiences.
   */
  final void populateAmbiences() {
    // Ambience selection listener
    ActionListener al = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent ae) {
        JMenuItem jmi = (JMenuItem) ae.getSource();
        // Selected 'Any" ambience
        JMenuItem all = jmAmbience.getItem(0);
        if (jmi.equals(all)) {
          // reset default ambience
          Conf.setProperty(Const.CONF_DEFAULT_AMBIENCE, "");
        } else {// Selected an ambience
          Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
              jmi.getActionCommand());
          Conf.setProperty(Const.CONF_DEFAULT_AMBIENCE, ambience.getID());
        }
        jmi.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
        ObservationManager.notify(new JajukEvent(JajukEvents.AMBIENCES_SELECTION_CHANGE));
      }
    };
    // Remove all item
    jmAmbience.removeAll();
    // Add "all" ambience
    JMenuItem jmiAll = new JMenuItem("<html><i>" + Messages.getString("DigitalDJWizard.64")
        + "</i></html>");
    jmiAll.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    jmiAll.addActionListener(al);
    jmAmbience.add(jmiAll);

    // Add available ambiences
    for (Ambience ambience : AmbienceManager.getInstance().getAmbiences()) {
      JMenuItem jmi = new JMenuItem(ambience.getName());
      if (Conf.getString(Const.CONF_DEFAULT_AMBIENCE).equals(ambience.getID())) {
        jmiAll.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
        jmi.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      }
      jmi.addActionListener(al);
      jmAmbience.add(jmi);
    }
  }

  /**
   * Gets the menu.
   * 
   * @return the menu
   */
  public JPopupMenu getMenu() {
    return this.jmenu;
  }

  /**
   * Gets the tray icon.
   * 
   * @return the trayIcon
   */
  public TrayIcon getTrayIcon() {
    return this.trayIcon;
  }

  /*
  * (non-Javadoc)
  * 
  * @seejava.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event. MouseWheelEvent)
  */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource().equals(jmiMute) && !Conf.getBoolean(Const.CONF_BIT_PERFECT)) {
      int oldVolume = (int) (100 * Player.getCurrentVolume());
      int newVolume = oldVolume - (e.getUnitsToScroll() * 3);
      if (Player.isMuted()) {
        Player.mute(false);
      }
      if (newVolume > 100) {
        newVolume = 100;
      } else if (newVolume < 0) {
        newVolume = 0;
      }
      Player.setVolume((float) newVolume / 100);
      MuteAction.setVolumeIcon(newVolume);
    }
  }

}
