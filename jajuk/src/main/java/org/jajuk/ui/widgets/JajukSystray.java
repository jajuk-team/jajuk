/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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

package org.jajuk.ui.widgets;

import ext.JXTrayIcon;
import ext.SliderMenuItem;

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
import java.util.Properties;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.ui.helpers.PlayerStateMediator;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Jajuk systray
 * <br> Extends CommandJPanel for volume slider heritage only
 */
public class JajukSystray extends CommandJPanel {
  private static final long serialVersionUID = 1L;

  // Systray variables
  SystemTray stray;

  JXTrayIcon trayIcon;

  JPopupMenu jmenu;

  JMenuItem jmiExit;

  JMenuItem jmiSlimbar;

  JMenuItem jmiMute;

  JMenuItem jmiShuffle;

  JMenuItem jmiBestof;

  JMenuItem jmiDJ;

  JMenuItem jmiNovelties;

  JMenuItem jmiFinishAlbum;

  JMenuItem jmiPlayPause;

  JMenuItem jmiStop;

  JMenuItem jmiPrevious;

  JMenuItem jmiNext;

  JLabel jlVolume;

  JLabel jlPosition;

  JMenu jmAmbience;

  long lDateLastAdjust;

  JSlider jsPosition;

  /** Show balloon? */
  JCheckBoxMenuItem jcbmiShowBalloon;

  /** Self instance singleton */
  private static JajukSystray jsystray;

  /** HTML Tooltip */
  JajukBalloon balloon;

  /** Swing Timer to refresh the component */
  private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      update(new JajukEvent(JajukEvents.HEART_BEAT));
    }
  });

  /**
   * 
   * @return whether the tray is loaded
   */
  public static boolean isLoaded() {
    return (jsystray != null);
  }

  /**
   * 
   * @return singleton
   */
  public static JajukSystray getInstance() {
    if (jsystray == null) {
      jsystray = new JajukSystray();
    }
    return jsystray;
  }

  /**
   * Reset the systray (useful for language reload)
   * 
   */
  public void dispose() {
    if (jsystray != null) {
      jsystray.closeSystray();
      jsystray = null;
    }
  }

  /**
   * Systray constructor
   * 
   */
  public JajukSystray() {
    stray = SystemTray.getSystemTray();
    initUI();
  }

  public void initUI() {
    // Instanciate the PlayerStateMediator to listen for player basic controls
    PlayerStateMediator.getInstance();
    jmenu = new JPopupMenu(Messages.getString("JajukWindow.3"));
    jmiExit = new JMenuItem(ActionManager.getAction(JajukActions.EXIT));
    jmiSlimbar = new JMenuItem(ActionManager.getAction(JajukActions.SLIM_JAJUK));

    // force icon to be display in 16x16
    jmiMute = new SizedJMenuItem(ActionManager.getAction(JajukActions.MUTE_STATE));
    jmiShuffle = new SizedJMenuItem(ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL));

    jmiBestof = new SizedJMenuItem(ActionManager.getAction(JajukActions.BEST_OF));
    jmiDJ = new SizedJMenuItem(ActionManager.getAction(JajukActions.DJ));
    jmiFinishAlbum = new SizedJMenuItem(ActionManager.getAction(JajukActions.FINISH_ALBUM));
    jmiNovelties = new SizedJMenuItem(ActionManager.getAction(JajukActions.NOVELTIES));

    jcbmiShowBalloon = new JCheckBoxMenuItem(Messages.getString("ParameterView.185"));
    jcbmiShowBalloon.setState(Conf.getBoolean(Const.CONF_UI_SHOW_BALLOON));
    jcbmiShowBalloon.addActionListener(this);
    jcbmiShowBalloon.setToolTipText(Messages.getString("ParameterView.185"));

    jmiPlayPause = new SizedJMenuItem(ActionManager.getAction(JajukActions.PLAY_PAUSE_TRACK));
    jmiStop = new SizedJMenuItem(ActionManager.getAction(JajukActions.STOP_TRACK));
    jmiPrevious = new SizedJMenuItem(ActionManager.getAction(JajukActions.PREVIOUS_TRACK));
    jmiNext = new SizedJMenuItem(ActionManager.getAction(JajukActions.NEXT_TRACK));

    jsPosition = new SliderMenuItem(0, 100, 0);
    jsPosition.setToolTipText(Messages.getString("CommandJPanel.15"));
    jsPosition.addMouseWheelListener(this);
    jsPosition.addChangeListener(this);

    int iVolume = (int) (100 * Conf.getFloat(Const.CONF_VOLUME));
    if (iVolume > 100) { // can occur in some undefined cases
      iVolume = 100;
    }
    jsVolume = new SliderMenuItem(0, 100, iVolume);
    jsVolume.addMouseWheelListener(this);
    jsVolume.addChangeListener(this);

    // Ambiences menu
    Ambience defaultAmbience = AmbienceManager.getInstance().getSelectedAmbience();
    jmAmbience = new JMenu(Messages.getString("JajukWindow.36")
        + " "
        + ((defaultAmbience == null) ? Messages.getString("DigitalDJWizard.64") : defaultAmbience
            .getName()));
    populateAmbiences();
    // Volume menu
    JMenu jmVolume = new JMenu(Messages.getString("JajukWindow.33"));
    jmVolume.setIcon(IconLoader.getIcon(JajukIcons.VOLUME));
    jmVolume.add(jsVolume);

    // Position menu
    JMenu jmPosition = new JMenu(Messages.getString("JajukWindow.34"));
    jmPosition.setIcon(IconLoader.getIcon(JajukIcons.POSITION));
    jmPosition.add(jsPosition);

    // Add a title. Important: do not add a JLabel, it present action event
    // to occur under windows
    JMenuItem jmiTitle = new JMenuItem("Jajuk");
    jmiTitle.setFont(FontManager.getInstance().getFont(JajukFont.BOLD_TITLE));
    jmenu.add(jmiTitle);
    jmenu.addSeparator();
    jmenu.add(jmAmbience);
    jmenu.addSeparator();
    jmenu.add(jcbmiShowBalloon);
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
    jmenu.add(jmiSlimbar);
    jmenu.add(jmiMute);
    jmenu.add(jmVolume);
    jmenu.add(jmPosition);
    jmenu.addSeparator();
    jmenu.add(jmiExit);
    // Add a row under Linux to fix an issue : sometimes, when left-clicking on
    // the tray, the exit menu item is executed and then close Jajuk accidently
    if (UtilSystem.isUnderLinux()) {
      jmenu.add("");
    }

    trayIcon = new JXTrayIcon(IconLoader.getIcon(JajukIcons.TRAY).getImage());
    if (UtilSystem.isUnderWindows()) {
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
        String title = FIFO.getCurrentFileTitle();
        if (balloon != null && balloon.isVisible()) {
          return;
        }
        balloon = new JajukBalloon(title);
        Point location = new Point(e.getX() - balloon.getWidth(), e.getY()
            - (20 + balloon.getHeight()));
        balloon.setLocation(location);
        balloon.display();
      }
    });
    trayIcon.setJPopuMenu(jmenu);
    trayIcon.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          // show main window if it is not visible and hide it if it is visible
          boolean bShouldDisplayMainWindow = !JajukWindow.getInstance().isWindowVisible();
          JajukWindow.getInstance().display(bShouldDisplayMainWindow);
        }
      }

    });
    try {
      stray.add(trayIcon);
    } catch (AWTException e) {
      Log.error(e);
      return;
    }
    // start timer
    timer.start();
    // Register needed events
    ObservationManager.register(this);

    // Force initial message refresh
    UtilFeatures.updateStatus(this);
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.PLAYER_PLAY);
    eventSubjectSet.add(JajukEvents.PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.HEART_BEAT);
    eventSubjectSet.add(JajukEvents.VOLUME_CHANGED);
    eventSubjectSet.add(JajukEvents.AMBIENCES_CHANGE);
    eventSubjectSet.add(JajukEvents.AMBIENCES_SELECTION_CHANGE);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /**
   * ActionListener
   */
  public void actionPerformed(final ActionEvent e) {
    // do not run this in a separate thread because Player actions would die
    // with the thread
    try {
      if (e.getSource() == jcbmiShowBalloon) {
        Conf.setProperty(Const.CONF_UI_SHOW_BALLOON, Boolean.toString(jcbmiShowBalloon.getState()));
        // Launch an event that can be trapped by the tray to
        // synchronize the state
        Properties details = new Properties();
        details.put(Const.DETAIL_ORIGIN, this);
        ObservationManager.notify(new JajukEvent(JajukEvents.PARAMETERS_CHANGE, details));
      }

    } catch (Exception e2) {
      Log.error(e2);
    } finally {
      ObservationManager.notify(new JajukEvent(JajukEvents.QUEUE_NEED_REFRESH));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public final void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
          // remove and re-add listener to make sure not to add it
          // twice
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.addMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
          jsPosition.addChangeListener(JajukSystray.this);
          jsPosition.setEnabled(true);
          jmiPlayPause.setEnabled(true);
          jmiStop.setEnabled(true);
          jmiNext.setEnabled(true);
          jmiPrevious.setEnabled(true);
          jmiFinishAlbum.setEnabled(true);
          String sID = (String) ObservationManager.getDetail(event, Const.DETAIL_CURRENT_FILE_ID);
          if (sID == null) {
            return;
          }
          File file = FileManager.getInstance().getFileByID(
              (String) ObservationManager.getDetail(event, Const.DETAIL_CURRENT_FILE_ID));
          String sOut = "";
          if (file != null) {
            sOut = file.getBasicFormatText();
          } else {
            // display a "Ready to play" message
            sOut = Messages.getString("JajukWindow.18");
          }
          // check show balloon option
          if (Conf.getBoolean(Const.CONF_UI_SHOW_BALLOON)) {
            trayIcon.displayMessage(Messages.getString("JajukWindow.35"), sOut,
                TrayIcon.MessageType.INFO);
          }
        } else if (JajukEvents.PLAYER_STOP.equals(subject)) {
          // Enable the play button to allow restarting the queue but disable if
          // the queue is void
          boolean bQueueNotVoid = (FIFO.getFIFO().size() > 0);
          jmiPlayPause.setEnabled(bQueueNotVoid);
          jmiNext.setEnabled(bQueueNotVoid);
          jmiPrevious.setEnabled(bQueueNotVoid);
          jmiStop.setEnabled(false);
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
          jsPosition.setEnabled(false);
          jsPosition.setValue(0);
          jmiFinishAlbum.setEnabled(false);
        } else if (JajukEvents.ZERO.equals(subject)) {
          jmiPlayPause.setEnabled(false);
          jmiStop.setEnabled(false);
          jmiNext.setEnabled(false);
          jmiPrevious.setEnabled(false);
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
          jsPosition.setEnabled(false);
          jsPosition.setValue(0);
          jmiFinishAlbum.setEnabled(false);
        } else if (JajukEvents.PLAYER_PLAY.equals(subject)) {
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.addMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
          jsPosition.addChangeListener(JajukSystray.this);
          jsPosition.setEnabled(true);
          jmiPlayPause.setEnabled(true);
          jmiStop.setEnabled(true);
          jmiNext.setEnabled(true);
          jmiFinishAlbum.setEnabled(true);
        } else if (JajukEvents.PLAYER_PAUSE.equals(subject)) {
          // disable position
          jsPosition.setEnabled(false);
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
        } else if (JajukEvents.PLAYER_RESUME.equals(subject)) {
          // disable position
          // Avoid adding listeners twice
          if (jsPosition.getMouseWheelListeners().length == 0) {
            jsPosition.addMouseWheelListener(JajukSystray.this);
          }
          if (jsPosition.getChangeListeners().length == 0) {
            jsPosition.addChangeListener(JajukSystray.this);
          }
          jsPosition.setEnabled(true);
        } else if (JajukEvents.VOLUME_CHANGED.equals(event.getSubject())) {
          JajukSystray.super.update(event);
        } else if (JajukEvents.HEART_BEAT.equals(subject) && !FIFO.isStopped()
            && !Player.isPaused()) {
          int iPos = (int) (100 * JajukTimer.getInstance().getCurrentTrackPosition());
          // Make sure to enable the slider
          if (!jsPosition.isEnabled()) {
            jsPosition.setEnabled(true);
          }
          // if position is adjusting, no don't disturb user
          if (jsPosition.getValueIsAdjusting() || Player.isSeeking()) {
            return;
          }
          // make sure not to set to old position
          if ((System.currentTimeMillis() - lDateLastAdjust) < 2000) {
            return;
          }
          // remove and re-add listener to make sure not to add it
          // twice
          jsPosition.removeChangeListener(JajukSystray.this);
          jsPosition.setValue(iPos);
          jsPosition.addChangeListener(JajukSystray.this);
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
        } else if (JajukEvents.PARAMETERS_CHANGE.equals(subject)) {
          jcbmiShowBalloon.setState(Conf.getBoolean(Const.CONF_UI_SHOW_BALLOON));
        }
      }
    });
  }

  /**
   * Hide systray
   */
  public void closeSystray() {
    if (stray != null && trayIcon != null) {
      stray.remove(trayIcon);
    }
  }

  /**
   * Populate ambiences
   * 
   */
  final void populateAmbiences() {
    // Ambience selection listener
    ActionListener al = new ActionListener() {
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

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource() == jsPosition) {
      int iOld = jsPosition.getValue();
      int iNew = iOld - (e.getUnitsToScroll() * 3);
      jsPosition.setValue(iNew);
    } else {
      super.mouseWheelMoved(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == jsPosition && !jsPosition.getValueIsAdjusting()) {
      lDateLastAdjust = System.currentTimeMillis();
      setPosition((float) jsPosition.getValue() / 100);
    } else {
      super.stateChanged(e);
    }
  }

  /**
   * Call a seek
   * 
   * @param fPosition
   */
  private void setPosition(final float fPosition) {
    new Thread() {
      @Override
      public void run() {
        Player.seek(fPosition);
      }
    }.start();
  }

  public JPopupMenu getMenu() {
    return this.jmenu;
  }
}
