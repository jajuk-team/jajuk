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
 * $Revision: 3612 $
 */
package org.jajuk.ui.widgets;

import static org.jajuk.ui.actions.JajukActions.FAST_FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.FINISH_ALBUM;
import static org.jajuk.ui.actions.JajukActions.MUTE_STATE;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;
import ext.DropDownButton;
import ext.SwingWorker;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.base.File;
import org.jajuk.base.SearchResult;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.actions.MuteAction;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Jajuk Slim Interface
 * <p>
 * Singleton
 * </p>
 */
public final class JajukSlimbar extends JFrame implements Observer, MouseWheelListener,
    ListSelectionListener, ActionListener {

  private static final long serialVersionUID = 1L;

  private JButton jbInfo;

  private SizedButton jbPrevious;

  private SizedButton jbNext;

  private SizedButton jbPlayPause;

  private SizedButton jbStop;

  private PreferenceToolbar preferences;

  private DropDownButton jddbSmart;

  private JPopupMenu jpmSmart;

  private JMenuItem jbBestof;

  private JMenuItem jbNovelties;

  private JMenuItem jbRandom;

  private SizedButton jbFinishAlbum;

  private JButton jbMaximize;

  private SizedButton jbVolume;

  private SearchBox sbSearch;

  private JToolBar slimJajuk;

  private String title = "";

  private boolean bInitialized = false;

  JajukBalloon balloon;

  /** True if user close the slim bar from the taskbar */
  private boolean closing = false;

  private static JajukSlimbar self;

  public static JajukSlimbar getInstance() {
    if (self == null) {
      self = new JajukSlimbar();
    }
    return self;
  }

  /**
   * This mouse motion listener allows the whole slim bar dragging
   */

  private MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
    @Override
    public void mouseDragged(MouseEvent e) {
      Point point = e.getLocationOnScreen();
      // compute coordinates of the event relative to the frame, not the screen
      // so we can compensate the frame size to avoid it to jump when applying
      // the new location
      Point relativePoint = SwingUtilities.convertPoint(((JComponent) e.getSource()).getParent(),
          ((JComponent) e.getSource()).getLocation(), JajukSlimbar.this.getRootPane());
      point = new Point((int) (point.getX() - relativePoint.getX()),
          (int) (point.getY() - relativePoint.getY()));
      setLocation(point);
      Conf.setProperty(Const.CONF_SLIMBAR_POSITION, (int) point.getX() + "," + (int) point.getY());
    }
  };

  private JajukSlimbar() {
    setUndecorated(true);
    setAlwaysOnTop(true);
    // Set windows decoration to look and feel
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
  }

  /**
   * 
   * @return whether the slimbar is loaded
   */
  public static boolean isLoaded() {
    return (self != null);
  }

  /**
   * 
   * @return whether the method initUI() has already been executed
   */
  public boolean isInitialized() {
    return bInitialized;
  }

  public void initUI() {
    setIconImage(IconLoader.getIcon(JajukIcons.LOGO).getImage());

    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        try {
          closing = true;
          ActionManager.getAction(JajukActions.EXIT).perform(null);
        } catch (Exception e1) {
          Log.error(e1);
        }
      }
    });

    jbInfo = new JButton(IconLoader.getIcon(JajukIcons.INFO));
    jbInfo.addActionListener(this);
    jbInfo.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        showBalloon();
      }
    });

    JToolBar jtbPlay = new JajukJToolbar();
    ActionUtil.installKeystrokes(jtbPlay, ActionManager.getAction(NEXT_ALBUM), ActionManager
        .getAction(PREVIOUS_ALBUM));
    jbPrevious = new SizedButton(ActionManager.getAction(PREVIOUS_TRACK), 16, 16, false);
    jbPrevious.addMouseMotionListener(motionAdapter);

    jbNext = new SizedButton(ActionManager.getAction(NEXT_TRACK), 16, 16, false);
    jbNext.addMouseMotionListener(motionAdapter);

    jbPlayPause = new SizedButton(ActionManager.getAction(PLAY_PAUSE_TRACK), 16, 16, false);
    jbPlayPause.addMouseMotionListener(motionAdapter);

    jbStop = new SizedButton(ActionManager.getAction(STOP_TRACK), 16, 16, false);
    jbStop.addMouseMotionListener(motionAdapter);

    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbStop);
    jtbPlay.add(jbNext);

    JToolBar jtbSmart = new JajukJToolbar();

    jddbSmart = new DropDownButton(IconLoader.getIcon(JajukIcons.INC_RATING)) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JPopupMenu getPopupMenu() {
        return jpmSmart;
      }
    };

    jbBestof = new JMenuItem(ActionManager.getAction(JajukActions.BEST_OF));
    jbBestof.setIcon(IconLoader.getIcon(JajukIcons.BESTOF_16X16));
    jbBestof.addActionListener(this);

    jbNovelties = new JMenuItem(ActionManager.getAction(JajukActions.NOVELTIES));
    jbNovelties.setIcon(IconLoader.getIcon(JajukIcons.NOVELTIES_16X16));
    jbNovelties.addActionListener(this);

    jbRandom = new JMenuItem(ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL));
    jbRandom.setIcon(IconLoader.getIcon(JajukIcons.SHUFFLE_GLOBAL_16X16));
    jbRandom.addActionListener(this);

    jpmSmart = new JPopupMenu();
    jpmSmart.add(jbRandom);
    jpmSmart.add(jbBestof);
    jpmSmart.add(jbNovelties);
    jddbSmart.addToToolBar(jtbSmart);
    jddbSmart.addMouseMotionListener(motionAdapter);

    if (JajukActions.SHUFFLE_GLOBAL.toString()
        .equals(Conf.getString(Const.CONF_SLIMBAR_SMART_MODE))) {
      jddbSmart.setAction(ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL));
      jddbSmart.setIcon(IconLoader.getIcon(JajukIcons.SHUFFLE_GLOBAL_16X16));
    } else if (JajukActions.BEST_OF.toString()
        .equals(Conf.getString(Const.CONF_SLIMBAR_SMART_MODE))) {
      jddbSmart.setAction(ActionManager.getAction(JajukActions.BEST_OF));
      jddbSmart.setIcon(IconLoader.getIcon(JajukIcons.BESTOF_16X16));
    } else if (JajukActions.NOVELTIES.toString().equals(
        Conf.getString(Const.CONF_SLIMBAR_SMART_MODE))) {
      jddbSmart.setAction(ActionManager.getAction(JajukActions.NOVELTIES));
      jddbSmart.setIcon(IconLoader.getIcon(JajukIcons.NOVELTIES_16X16));
    }

    preferences = new PreferenceToolbar();
    jtbSmart.add(preferences);

    JToolBar jtbTools = new JajukJToolbar();

    int iVolume = (int) (100 * Conf.getFloat(Const.CONF_VOLUME));
    if (iVolume > 100) { // can occur in some undefined cases
      iVolume = 100;
    }
    jbVolume = new SizedButton(ActionManager.getAction(MUTE_STATE), 16, 16, false);

    jbVolume.addMouseMotionListener(motionAdapter);
    jbVolume.addMouseWheelListener(this);
    jbVolume.setText(null);
    MuteAction.setVolumeIcon(iVolume);

    jbMaximize = new JajukButton(ActionManager.getAction(JajukActions.SLIM_JAJUK));
    jbMaximize.addMouseMotionListener(motionAdapter);

    jtbTools.add(jbVolume);
    jtbTools.addSeparator();
    jtbTools.add(jbMaximize);
    jtbTools.add(new SizedButton(ActionManager.getAction(JajukActions.EXIT), 16, 16, false));

    // Continue
    jbFinishAlbum = new SizedButton(ActionManager.getAction(JajukActions.FINISH_ALBUM));

    // Search
    sbSearch = new SearchBox(this);
    sbSearch.setPreferredSize(new Dimension(22, 18));
    sbSearch.setMaximumSize(new Dimension(22, 18));
    sbSearch.addMouseMotionListener(motionAdapter);

    slimJajuk = new JajukJToolbar();

    slimJajuk.add(Box.createHorizontalStrut(4));
    slimJajuk.add(jbInfo);
    slimJajuk.addSeparator();
    slimJajuk.add(sbSearch);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbSmart);
    slimJajuk.add(jbFinishAlbum);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbPlay);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbTools);
    slimJajuk.add(Box.createHorizontalStrut(2));

    slimJajuk.setBorder(BorderFactory.createRaisedBevelBorder());

    getRootPane().setToolTipText(getPlayerInfo());

    add(slimJajuk);
    ObservationManager.register(this);

    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    updateCurrentTitle();
    setVisible(true);
    setAlwaysOnTop(true);

    // Set location
    String lastPosition = Conf.getString(Const.CONF_SLIMBAR_POSITION);
    int x = 0;
    int y = 0;
    int iScreenWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
    int iScreenHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());

    try {
      StringTokenizer st = new StringTokenizer(lastPosition, ",");
      x = Integer.parseInt(st.nextToken());
      y = Integer.parseInt(st.nextToken());
      // Reset if position are out of bounds (after a resolution changing for
      // eg)
      if (x < 0 || x > iScreenWidth) {
        x = 0;
      }
      if (y < 0 || y > iScreenHeight) {
        y = 0;
      }
      Point point = new Point(x, y);
      setLocation(point);
    } catch (Exception e) {
      Log.debug("Cannot restore slimbar position");
      Log.error(e);
    }
    pack();
   
    // Force initial message refresh
    UtilFeatures.updateStatus(this);
    
    bInitialized = true;
  }

  private void updateCurrentTitle() {
    File file = FIFO.getCurrentFile();
    if (FIFO.isPlayingRadio()) {
      title = FIFO.getCurrentRadio().getName();
    } else if (file != null && !FIFO.isStopped()) {
      title = UtilString.buildTitle(FIFO.getCurrentFile());
    } else {
      title = Messages.getString("JajukWindow.18");
    }
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setTitle(title);
        jbPlayPause.setToolTipText(title);
        jbInfo.setToolTipText(title);
      }
    });
  }

  /**
   * @return Player Info : current and next track
   */
  public String getPlayerInfo() {
    try {
      String currentTrack = UtilString.buildTitle(FIFO.getCurrentFile());
      String nextTrack = "";
      try {
        nextTrack = UtilString.buildTitle(FIFO.getItem(FIFO.getIndex() + 1).getFile());
      } catch (Exception e) {
        nextTrack = UtilString.buildTitle(FIFO.getPlanned().get(0).getFile());
      }
      return "  |  Playing: " + currentTrack + "  |  Next: " + nextTrack;
    } catch (Exception e) {
      return Messages.getString("JajukWindow.17");
    }
  }

  /**
   * We want to alert the main hook thread to consider the slim bar window has
   * visible when user closed the slimbar from the taskbar to save this state
   * and display the slimbar at next startup
   * 
   * @return whether the slim bar is visible
   */
  @Override
  public boolean isVisible() {
    return super.isVisible() || closing;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource().equals(jbVolume)) {
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
      jbVolume.setToolTipText(newVolume + " %");
      MuteAction.setVolumeIcon(newVolume);
    }
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.QUEUE_NEED_REFRESH);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.MUTE_STATE);
    eventSubjectSet.add(JajukEvents.RATE_CHANGED);
    return eventSubjectSet;
  }

  public void update(final Event event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
      updateCurrentTitle();
      ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
      ActionManager.getAction(NEXT_TRACK).setEnabled(true);
      ActionManager.getAction(REWIND_TRACK).setEnabled(true);
      ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(true);
      ActionManager.getAction(STOP_TRACK).setEnabled(true);
      ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
      ActionManager.getAction(NEXT_ALBUM).setEnabled(true);
      ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(true);
      ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
    } else if (JajukEvents.PLAYER_PAUSE.equals(subject)) {
      jbPlayPause.setIcon(IconLoader.getIcon(JajukIcons.PLAY_16X16));
    } else if (JajukEvents.PLAYER_RESUME.equals(subject)) {
      jbPlayPause.setIcon(IconLoader.getIcon(JajukIcons.PAUSE_16X16));
    } else if (JajukEvents.MUTE_STATE.equals(subject)) {
      MuteAction.setVolumeIcon(100 * Player.getCurrentVolume());
    } else if (JajukEvents.PLAYER_STOP.equals(subject)) {
      // reset title
      updateCurrentTitle();
      // Enable the play button to allow restarting the queue but disable if
      // the queue is void
      boolean bQueueNotVoid = (FIFO.getFIFO().size() > 0);
      ActionManager.getAction(PREVIOUS_TRACK).setEnabled(bQueueNotVoid);
      ActionManager.getAction(NEXT_TRACK).setEnabled(bQueueNotVoid);
      ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(bQueueNotVoid);
      ActionManager.getAction(NEXT_ALBUM).setEnabled(bQueueNotVoid);
      ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(bQueueNotVoid);
      ActionManager.getAction(REWIND_TRACK).setEnabled(false);
      ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.getIcon(JajukIcons.PLAY));
      ActionManager.getAction(PLAY_PAUSE_TRACK).setName(Messages.getString("JajukWindow.12"));
      ActionManager.getAction(STOP_TRACK).setEnabled(false);
      ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
      ActionManager.getAction(FINISH_ALBUM).setEnabled(false);
    } else if (JajukEvents.WEBRADIO_LAUNCHED.equals(event.getSubject())) {
      updateCurrentTitle();
      ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
      ActionManager.getAction(NEXT_TRACK).setEnabled(true);
      ActionManager.getAction(STOP_TRACK).setEnabled(true);
    } else if (JajukEvents.RATE_CHANGED.equals(event.getSubject())) {
      // Update rate button tooltip
      updateCurrentTitle();
    }
  }

  public void valueChanged(final ListSelectionEvent e) {
    SwingWorker sw = new SwingWorker() {
      @Override
      public Object construct() {
        if (!e.getValueIsAdjusting()) {
          SearchResult sr = sbSearch.getResult(sbSearch.getSelectedIndex());
          try {
            // If user selected a file
            if (sr.getType() == SearchResultType.FILE) {
              FIFO.push(
                  new StackItem(sr.getFile(), Conf.getBoolean(Const.CONF_STATE_REPEAT), true), Conf
                      .getBoolean(Const.CONF_OPTIONS_PUSH_ON_CLICK));
            }
            // User selected a web radio
            else if (sr.getType() == SearchResultType.WEBRADIO) {
              FIFO.launchRadio(sr.getWebradio());
            }
          } catch (JajukException je) {
            Log.error(je);
          }
        }
        return null;
      }

      @Override
      public void finished() {
        if (!e.getValueIsAdjusting()) {
          sbSearch.hidePopup();
          requestFocusInWindow();
        }
      }
    };
    sw.start();
  }

  public void actionPerformed(final ActionEvent ae) {
    if (ae.getSource() == jbBestof) {
      jddbSmart.setAction(ActionManager.getAction(JajukActions.BEST_OF));
      jddbSmart.setIcon(IconLoader.getIcon(JajukIcons.BESTOF_16X16));
      Conf.setProperty(Const.CONF_SLIMBAR_SMART_MODE, JajukActions.BEST_OF.toString());
    } else if (ae.getSource() == jbNovelties) {
      jddbSmart.setAction(ActionManager.getAction(JajukActions.NOVELTIES));
      jddbSmart.setIcon(IconLoader.getIcon(JajukIcons.NOVELTIES_16X16));
      Conf.setProperty(Const.CONF_SLIMBAR_SMART_MODE, JajukActions.NOVELTIES.toString());
    } else if (ae.getSource() == jbRandom) {
      jddbSmart.setAction(ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL));
      jddbSmart.setIcon(IconLoader.getIcon(JajukIcons.SHUFFLE_GLOBAL_16X16));
      Conf.setProperty(Const.CONF_SLIMBAR_SMART_MODE, JajukActions.SHUFFLE_GLOBAL.toString());
    } else if (ae.getSource() == jbInfo) {
      showBalloon();
    }
  }

  /**
   * Display the current playing album balloon when moving mouse over jbInfo or
   * when clicking on it
   */
  private void showBalloon() {
    // Leave if baloon already visible
    if (balloon != null && balloon.isVisible()) {
      return;
    }
    balloon = new JajukBalloon(FIFO.getCurrentFileTitle());
    Point buttonLocation = jbInfo.getLocationOnScreen();
    Point location = null;
    // If slimbar is too height in the screen, display the popup bellow it
    if (buttonLocation.y < balloon.getHeight() + 10) {
      location = new Point(buttonLocation.x, buttonLocation.y + 25);
    } else {
      location = new Point(buttonLocation.x, buttonLocation.y - (5 + balloon.getHeight()));
    }
    balloon.setLocation(location);
    balloon.display();
  }
}