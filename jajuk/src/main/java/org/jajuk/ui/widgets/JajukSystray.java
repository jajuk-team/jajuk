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

import static org.jajuk.ui.actions.JajukAction.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukAction.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukAction.STOP_TRACK;
import ext.JXTrayIcon;
import ext.SliderMenuItem;

import java.awt.AWTException;
import java.awt.Color;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.WebRadio;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.thumbnails.ThumbnailManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Jajuk systray
 */
public class JajukSystray extends CommandJPanel {
  private static final long serialVersionUID = 1L;

  // Systray variables
  SystemTray stray;

  JXTrayIcon trayIcon;

  public JPopupMenu jmenu;

  JMenuItem jmiExit;

  JMenuItem jmiSlimbar;

  public JMenuItem jmiMute;

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
  JDialog dialog;

  /** Swing Timer to refresh the component */
  private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      update(new Event(EventSubject.EVENT_HEART_BEAT));
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
    jmenu = new JPopupMenu(Messages.getString("JajukWindow.3"));

    jmiExit = new JMenuItem(ActionManager.getAction(JajukAction.EXIT));

    jmiSlimbar = new JMenuItem(ActionManager.getAction(JajukAction.SLIM_JAJUK));

    // force icon to be display in 16x16
    jmiMute = new SizedJMenuItem(ActionManager.getAction(JajukAction.MUTE_STATE));
    jmiShuffle = new SizedJMenuItem(ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL));

    jmiBestof = new SizedJMenuItem(ActionManager.getAction(JajukAction.BEST_OF));
    jmiDJ = new SizedJMenuItem(ActionManager.getAction(JajukAction.DJ));
    jmiFinishAlbum = new SizedJMenuItem(ActionManager.getAction(JajukAction.FINISH_ALBUM));
    jmiNovelties = new SizedJMenuItem(ActionManager.getAction(JajukAction.NOVELTIES));

    jcbmiShowBalloon = new JCheckBoxMenuItem(Messages.getString("ParameterView.185"));
    jcbmiShowBalloon.setState(ConfigurationManager.getBoolean(CONF_UI_SHOW_BALLOON));
    jcbmiShowBalloon.addActionListener(this);
    jcbmiShowBalloon.setToolTipText(Messages.getString("ParameterView.185"));

    jmiPlayPause = new SizedJMenuItem(ActionManager.getAction(JajukAction.PLAY_PAUSE_TRACK));
    jmiStop = new SizedJMenuItem(ActionManager.getAction(JajukAction.STOP_TRACK));
    jmiPrevious = new SizedJMenuItem(ActionManager.getAction(JajukAction.PREVIOUS_TRACK));
    jmiNext = new SizedJMenuItem(ActionManager.getAction(JajukAction.NEXT_TRACK));

    jsPosition = new SliderMenuItem(0, 100, 0);
    jsPosition.setToolTipText(Messages.getString("CommandJPanel.15"));
    jsPosition.addMouseWheelListener(this);
    jsPosition.addChangeListener(this);

    int iVolume = (int) (100 * ConfigurationManager.getFloat(CONF_VOLUME));
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
    jmVolume.setIcon(IconLoader.ICON_VOLUME);
    jmVolume.add(jsVolume);

    // Position menu
    JMenu jmPosition = new JMenu(Messages.getString("JajukWindow.34"));
    jmPosition.setIcon(IconLoader.ICON_POSITION);
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
    // Add a void item that would simply close the tray if clicked, avoid
    // setting "exit" as last item to prevent unwanted exit
    jmenu.add(new JMenuItem(" "));
    jmenu.add(new JMenuItem(" "));

    trayIcon = new JXTrayIcon(IconLoader.ICON_TRAY.getImage());
    if (Util.isUnderWindows()) {
      // auto-resize looks OK under Windows but is ugly under Linux/KDE
      trayIcon.setImageAutoSize(true);
    }
    trayIcon.addMouseMotionListener(new MouseMotionAdapter() {

      @Override
      public void mouseMoved(MouseEvent e) {
        File file = FIFO.getInstance().getCurrentFile();
        Point location = new Point(e.getX() - 50, e.getY() - 250);
        if (file == null || FIFO.isStopped()) {
          return;
        }
        String sOut = getHTMLFormatText(file);
        if (dialog != null) {
          return;
        }
        dialog = new JDialog();
        dialog.setUndecorated(true);
        dialog.getRootPane().setWindowDecorationStyle(JRootPane.NONE);
        dialog.getRootPane().setBorder(new LineBorder(Color.BLACK));
        JLabel jl = new JLabel(sOut);
        jl.setBorder(new EmptyBorder(5, 5, 5, 5));
        dialog.add(jl);
        dialog.setLocation(location);
        dialog.pack();
        dialog.setVisible(true);
        // The toFront() is required under windows when main window is not
        // visible
        dialog.toFront();
        // Dispose the dialog after 5 seconds
        new Thread() {
          public void run() {
            try {
              Thread.sleep(3000);
              dialog.dispose();
              dialog = null;
            } catch (InterruptedException e) {
              Log.error(e);
            }
          }
        }.start();
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

    // check if a file has been already started
    if (FIFO.getInstance().getCurrentFile() == null) {
      update(new Event(EventSubject.EVENT_PLAYER_STOP, ObservationManager
          .getDetailsLastOccurence(EventSubject.EVENT_PLAYER_STOP)));
    } else {
      update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
          .getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
    }
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_ZERO);
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_PAUSE);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_PLAY);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_RESUME);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
    eventSubjectSet.add(EventSubject.EVENT_MUTE_STATE);
    eventSubjectSet.add(EventSubject.EVENT_HEART_BEAT);
    eventSubjectSet.add(EventSubject.EVENT_VOLUME_CHANGED);
    eventSubjectSet.add(EventSubject.EVENT_AMBIENCES_CHANGE);
    eventSubjectSet.add(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE);
    eventSubjectSet.add(EventSubject.EVENT_PARAMETERS_CHANGE);
    eventSubjectSet.add(EventSubject.EVENT_WEBRADIO_LAUNCHED);
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
        ConfigurationManager.setProperty(CONF_UI_SHOW_BALLOON, Boolean.toString(jcbmiShowBalloon
            .getState()));
        // Launch an event that can be trapped by the tray to
        // synchronize the state
        Properties details = new Properties();
        details.put(DETAIL_ORIGIN, this);
        ObservationManager.notify(new Event(EventSubject.EVENT_PARAMETERS_CHANGE, details));
      }

    } catch (Exception e2) {
      Log.error(e2);
    } finally {
      ObservationManager.notify(new Event(EventSubject.EVENT_QUEUE_NEED_REFRESH));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final Event event) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        EventSubject subject = event.getSubject();
        if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
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
          String sID = (String) ObservationManager.getDetail(event, DETAIL_CURRENT_FILE_ID);
          if (sID == null) {
            return;
          }
          File file = FileManager.getInstance().getFileByID(
              (String) ObservationManager.getDetail(event, DETAIL_CURRENT_FILE_ID));
          String sOut = "";
          sOut = getBasicFormatText(file);
          // check show balloon option
          if (ConfigurationManager.getBoolean(CONF_UI_SHOW_BALLOON)) {
            trayIcon.displayMessage(Messages.getString("JajukWindow.35"), sOut,
                TrayIcon.MessageType.INFO);
          }

        } else if (EventSubject.EVENT_WEBRADIO_LAUNCHED.equals(subject)) {
          WebRadio radio = FIFO.getInstance().getCurrentRadio();
          if (radio != null) {
            trayIcon.setToolTip(radio.getName());
          }
          // Enable webradio navigation actions
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(STOP_TRACK).setEnabled(true);
        } else if (EventSubject.EVENT_PLAYER_STOP.equals(subject)) {
          trayIcon.setToolTip(Messages.getString("JajukWindow.18"));
          jmiPlayPause.setEnabled(true);
          jmiStop.setEnabled(false);
          jmiNext.setEnabled(true);
          jmiPrevious.setEnabled(true);
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
          jsPosition.setEnabled(false);
          jsPosition.setValue(0);
          jmiFinishAlbum.setEnabled(false);
        } else if (EventSubject.EVENT_ZERO.equals(subject)) {
          trayIcon.setToolTip(Messages.getString("JajukWindow.18"));
          jmiPlayPause.setEnabled(false);
          jmiStop.setEnabled(false);
          jmiNext.setEnabled(false);
          jmiPrevious.setEnabled(false);
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
          jsPosition.setEnabled(false);
          jsPosition.setValue(0);
          jmiFinishAlbum.setEnabled(false);
        } else if (EventSubject.EVENT_PLAYER_PLAY.equals(subject)) {
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.addMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
          jsPosition.addChangeListener(JajukSystray.this);
          jsPosition.setEnabled(true);
          jmiPlayPause.setEnabled(true);
          jmiStop.setEnabled(true);
          jmiNext.setEnabled(true);
          jmiFinishAlbum.setEnabled(true);
        } else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)) {
          // Apply basic CommandJPanel actions
          JajukSystray.super.update(event);
          // disable position
          jsPosition.setEnabled(false);
          jsPosition.removeMouseWheelListener(JajukSystray.this);
          jsPosition.removeChangeListener(JajukSystray.this);
        } else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)) {
          // Apply basic CommandJPanel actions
          JajukSystray.super.update(event);
          // disable position
          // Avoid adding listeners twice
          if (jsPosition.getMouseWheelListeners().length == 0) {
            jsPosition.addMouseWheelListener(JajukSystray.this);
          }
          if (jsPosition.getChangeListeners().length == 0) {
            jsPosition.addChangeListener(JajukSystray.this);
          }
          jsPosition.setEnabled(true);
        } else if (EventSubject.EVENT_VOLUME_CHANGED.equals(event.getSubject())) {
          JajukSystray.super.update(event);
        } else if (EventSubject.EVENT_HEART_BEAT.equals(subject) && !FIFO.isStopped()
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
        } else if (EventSubject.EVENT_AMBIENCES_CHANGE.equals(subject)
            || EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE.equals(subject)) {
          Ambience ambience = AmbienceManager.getInstance().getSelectedAmbience();
          if (ambience != null) {
            jmAmbience.setText(Messages.getString("JajukWindow.36") + " "
                + AmbienceManager.getInstance().getSelectedAmbience().getName());
          } else {
            jmAmbience.setText(Messages.getString("JajukWindow.37"));
          }
          populateAmbiences();
        } else if (EventSubject.EVENT_PARAMETERS_CHANGE.equals(subject)) {
          jcbmiShowBalloon.setState(ConfigurationManager.getBoolean(CONF_UI_SHOW_BALLOON));
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
   * 
   * @param file
   *          current played file
   * @return text to be displayed in the tray ballon and tooltip with HTML
   *         formating that is used correctly under Linux
   */
  public String getHTMLFormatText(File file) {
    String sOut = "";
    if (file != null) {
      sOut += "<HTML><br>";
      String size = "100x100";
      int maxSize = 30;
      ThumbnailManager.refreshThumbnail(FIFO.getInstance().getCurrentFile().getTrack().getAlbum(),
          size);
      java.io.File cover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + '/'
          + FIFO.getInstance().getCurrentFile().getTrack().getAlbum().getID() + '.' + EXT_THUMB);
      if (cover.canRead()) {
        sOut += "<p ALIGN=center><img src='file:" + cover.getAbsolutePath() + "'/></p><br>";
      }
      // We use gray color for font because, due to a JDIC bug under
      // Linux, the
      // balloon background is white even if the the look and feel is dark
      // (like ebony)
      // but the look and feel makes the text white is it is black
      // initialy
      // so text is white on white is the balloon. It must be displayed in
      // the tooltip too
      // and this issue doesn't affect the tray tooltip. This color is the
      // only one to be correctly displayed
      // in a dark and a light background at the same time
      sOut += "<p><font color='#484848'><b>"
          + Util.getLimitedString(file.getTrack().getName(), maxSize) + "</b></font></p>";
      String sAuthor = Util.getLimitedString(file.getTrack().getAuthor().getName(), maxSize);
      if (!sAuthor.equals(UNKNOWN_AUTHOR)) {
        sOut += "<p><font color='#484848'>" + sAuthor + "</font></p>";
      }
      String sAlbum = Util.getLimitedString(file.getTrack().getAlbum().getName(), maxSize);
      if (!sAlbum.equals(UNKNOWN_ALBUM)) {
        sOut += "<p><font color='#484848'>" + sAlbum + "</font></p>";
      }
      sOut += "</HTML>";
    } else {
      // display a "Ready to play" message
      sOut = Messages.getString("JajukWindow.18");
    }
    return sOut;
  }

  /**
   * 
   * @param file
   *          current played file
   * @return Text to be displayed in the tootip and baloon under windows.
   * 
   */
  public String getBasicFormatText(File file) {
    String sOut = "";
    if (file != null) {
      sOut = "";
      String sAuthor = file.getTrack().getAuthor().getName();
      if (!sAuthor.equals(UNKNOWN_AUTHOR)) {
        sOut += sAuthor + " / ";
      }
      String sAlbum = file.getTrack().getAlbum().getName();
      if (!sAlbum.equals(UNKNOWN_ALBUM)) {
        sOut += sAlbum + " / ";
      }
      sOut += file.getTrack().getName();
    } else {
      // display a "Ready to play" message
      sOut = Messages.getString("JajukWindow.18");
    }
    return sOut;
  }

  /**
   * Populate ambiences
   * 
   */
  void populateAmbiences() {
    // Ambience selection listener
    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        JMenuItem jmi = (JMenuItem) ae.getSource();
        // Selected 'Any" ambience
        JMenuItem all = jmAmbience.getItem(0);
        if (jmi.equals(all)) {
          // reset default ambience
          ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE, "");
        } else {// Selected an ambience
          Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
              jmi.getActionCommand());
          ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE, ambience.getID());
        }
        jmi.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
        ObservationManager.notify(new Event(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE));
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
      if (ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE).equals(ambience.getID())) {
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
      public void run() {
        Player.seek(fPosition);
      }
    }.start();
  }
}
