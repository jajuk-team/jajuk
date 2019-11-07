/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.ui.windows;

import static org.jajuk.ui.actions.JajukActions.MUTE_STATE;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PAUSE_RESUME_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.QUEUE_TO_SLIM;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
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

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.jajuk.base.File;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.actions.MuteAction;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.PlayerStateMediator;
import org.jajuk.ui.views.QueueView;
import org.jajuk.ui.widgets.AmbienceComboBox;
import org.jajuk.ui.widgets.JajukButton;
import org.jajuk.ui.widgets.JajukInformationDialog;
import org.jajuk.ui.widgets.JajukJToolbar;
import org.jajuk.ui.widgets.PreferenceToolbar;
import org.jajuk.ui.widgets.SearchBox;
import org.jajuk.ui.widgets.SizedButton;
import org.jajuk.ui.widgets.WebRadioButton;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import ext.DropDownButton;

/**
 * Jajuk Slim Interface
 * <p>
 * Singleton
 * </p>
 * .
 */
public final class JajukSlimbar extends JFrame implements IJajukWindow, Observer,
    MouseWheelListener, ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** A queue view to be used by the show queue view slimbar switch button. Don't use it directly, use the getQueueWindow() method instead that lazy-load it. */
  private JWindow queueViewWindow;
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
  private JToolBar jtbPlay;
  private DropDownButton webRadioButton;
  private String title = "";
  /** State decorator. */
  private WindowStateDecorator decorator;
  JajukInformationDialog balloon;
  private static JajukSlimbar self;

  /**
   * Gets the single instance of JajukSlimbar.
   * 
   * @return single instance of JajukSlimbar
   */
  public static synchronized JajukSlimbar getInstance() {
    if (self == null) {
      self = new JajukSlimbar();
      self.decorator = new WindowStateDecorator(self) {
        @Override
        public void specificAfterHidden() {
          // Nothing particular to do here
        }

        @Override
        public void specificBeforeHidden() {
          // Nothing particular to do here
        }

        @Override
        public void specificAfterShown() {
          // Need focus for keystrokes
          self.requestFocus();
        }

        @Override
        public void specificBeforeShown() {
          self.pack();
        }
      };
    }
    return self;
  }

  /*
  * (non-Javadoc)
  * 
  * @see org.jajuk.events.Observer#getRegistrationKeys()
  */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.QUEUE_NEED_REFRESH);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /**
   * Sets the display queue.
   * 
   * @param display the new display queue
   */
  public void setDisplayQueue(boolean display) {
    if (display) {
      // Set position of queue dialog. We display the queue window either above or bellow the slimbar 
      // according to remaining vertical space.
      int yLocation = this.getLocation().y;
      if (this.getLocation().y + queueViewWindow.getSize().height + this.getSize().height > Toolkit
          .getDefaultToolkit().getScreenSize().getHeight()) {
        yLocation -= queueViewWindow.getSize().height;
      } else {
        yLocation += this.getSize().height;
      }
      getQueueWindow().setLocation(this.getLocation().x, yLocation);
      getQueueWindow().setSize(this.getSize().width, queueViewWindow.getSize().height);
    }
    getQueueWindow().setVisible(display);
    Conf.setProperty(Const.CONF_SLIMBAR_DISPLAY_QUEUE, Boolean.toString(isDisplayQueue()));
  }

  /**
   * Checks if is display queue.
   * 
   * @return true, if is display queue
   */
  public boolean isDisplayQueue() {
    return getQueueWindow().isVisible();
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

  /** This mouse motion listener allows the whole slim bar dragging. */
  private final MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
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
      setDisplayQueue(isDisplayQueue());
      Conf.setProperty(Const.CONF_SLIMBAR_POSITION, (int) point.getX() + "," + (int) point.getY());
    }
  };
  private SizedButton jbQueue;

  /**
   * Instantiates a new jajuk slimbar.
   */
  private JajukSlimbar() {
    setUndecorated(true);
    setAlwaysOnTop(true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.windows.IJajukWindow#initUI()
   */
  @Override
  public void initUI() {
    // Instanciate the PlayerStateMediator to listen for player basic controls
    PlayerStateMediator.getInstance();
    setIconImage(IconLoader.getIcon(JajukIcons.LOGO).getImage());
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        try {
          ActionManager.getAction(JajukActions.EXIT).perform(null);
        } catch (Exception e1) {
          Log.error(e1);
        }
      }
    });
    jbInfo = new JButton(IconLoader.getIcon(JajukIcons.INFO));
    jbInfo.addActionListener(this);
    // Listen for dragging
    jbInfo.addMouseMotionListener(motionAdapter);
    // Listen for balloon displaying
    jbInfo.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        showBalloon();
      }
    });
    jtbPlay = new JajukJToolbar();
    jbPrevious = new SizedButton(ActionManager.getAction(PREVIOUS_TRACK), false);
    jbPrevious.addMouseMotionListener(motionAdapter);
    // Manage right click : replay the track (this not triggers an action so we use a MouseAdapter here)
    jbPrevious.addMouseListener(new JajukMouseAdapter() {
      @Override
      public void handlePopup(final MouseEvent me) {
        // Create an ActionEvent from this MouseEvent with a custom modifier : the right click
        ActionEvent ae = new ActionEvent(jbPrevious, 0, PREVIOUS_TRACK.name(), 4332424);
        try {
          ActionManager.getAction(PREVIOUS_TRACK).perform(ae);
        } catch (Exception e) {
          Log.error(e);
        }
      }
    });
    jbNext = new SizedButton(ActionManager.getAction(NEXT_TRACK), false);
    jbNext.addMouseMotionListener(motionAdapter);
    jbPlayPause = new SizedButton(ActionManager.getAction(PAUSE_RESUME_TRACK), false);
    jbPlayPause.addMouseMotionListener(motionAdapter);
    jbStop = new SizedButton(ActionManager.getAction(STOP_TRACK), false);
    jbStop.addMouseMotionListener(motionAdapter);
    jbQueue = new SizedButton(ActionManager.getAction(QUEUE_TO_SLIM), false);
    jbQueue.addMouseMotionListener(motionAdapter);
    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbStop);
    jtbPlay.add(jbNext);
    jtbPlay.add(jbQueue);
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
    jbVolume = new SizedButton(ActionManager.getAction(MUTE_STATE), false);
    jbVolume.addMouseMotionListener(motionAdapter);
    jbVolume.addMouseWheelListener(this);
    jbVolume.setText(null);
    jbVolume.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        // Request focus to allow volume change
        jbVolume.requestFocus();
        toFront();
      }
    });
    MuteAction.setVolumeIcon(iVolume);
    jbMaximize = new JajukButton(ActionManager.getAction(JajukActions.SLIM_JAJUK));
    jbMaximize.addMouseMotionListener(motionAdapter);
    jtbTools.add(jbVolume);
    jtbTools.addSeparator();
    jtbTools.add(jbMaximize);
    jtbTools.add(new SizedButton(ActionManager.getAction(JajukActions.EXIT), false));
    // Continue
    jbFinishAlbum = new SizedButton(ActionManager.getAction(JajukActions.FINISH_ALBUM));
    // Search
    sbSearch = new SearchBox();
    sbSearch.setPreferredSize(new Dimension(75, 20));
    sbSearch.setMaximumSize(new Dimension(75, 20));
    sbSearch.addMouseMotionListener(motionAdapter);
    slimJajuk = new JajukJToolbar();
    AmbienceComboBox ambienceCombo = new AmbienceComboBox();
    ambienceCombo.setPreferredSize(new Dimension(42, 20));
    ambienceCombo.addMouseMotionListener(motionAdapter);
    // Webradio button
    webRadioButton = new WebRadioButton(IconLoader.getIcon(JajukIcons.WEBRADIO_16X16));
    JToolBar jtbWebRadio = new JajukJToolbar();
    webRadioButton.addToToolBar(jtbWebRadio);
    slimJajuk.add(Box.createHorizontalStrut(4));
    slimJajuk.add(jbInfo);
    slimJajuk.addSeparator();
    slimJajuk.add(sbSearch);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbWebRadio);
    slimJajuk.add(jtbSmart);
    slimJajuk.add(jbFinishAlbum);
    slimJajuk.addSeparator();
    slimJajuk.add(ambienceCombo);
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
    try {
      StringTokenizer st = new StringTokenizer(lastPosition, ",");
      int x = Integer.parseInt(st.nextToken());
      int y = Integer.parseInt(st.nextToken());
      Point point = new Point(x, y);
      // Note that setLocation handle odd positions and fix them
      setLocation(point);
    } catch (Exception e) {
      Log.debug("Cannot restore slimbar position");
      Log.error(e);
    }
    // Force initial message refresh
    UtilFeatures.updateStatus(this);
    // Install global keystrokes
    WindowGlobalKeystrokeManager.getInstance();
  }

  /**
   * Returns or create a queue window to be displayed though the queue view slimbar button.
   * 
   * @return the queue window
   */
  private JWindow getQueueWindow() {
    if (queueViewWindow == null) {
      QueueView queueView = new QueueView();
      queueView.initUI();
      queueView.setBorder(BorderFactory.createLineBorder(Color.BLACK));
      queueViewWindow = new JWindow(this);
      queueViewWindow.getContentPane().add(queueView);
      queueViewWindow.pack();
      // Set position of queue dialog
      queueViewWindow.setLocation(this.getLocation().x, this.getLocation().y
          + this.getSize().height);
      queueViewWindow.setSize(this.getSize().width, queueViewWindow.getSize().height / 2);
      queueViewWindow.setVisible(Conf.getBoolean(Const.CONF_SLIMBAR_DISPLAY_QUEUE));
    }
    return queueViewWindow;
  }

  /**
   * Update current title. 
   */
  private void updateCurrentTitle() {
    File file = QueueModel.getPlayingFile();
    if (QueueModel.isPlayingRadio()) {
      title = QueueModel.getCurrentRadio().getName();
    } else if (file != null && !QueueModel.isStopped()) {
      title = QueueModel.getPlayingFileTitle();
    } else {
      title = Messages.getString("JajukWindow.18");
    }
    setTitle(title);
  }

  /**
   * Gets the player info.
   * 
   * @return Player Info : current and next track
   */
  public String getPlayerInfo() {
    try {
      String currentTrack = QueueModel.getPlayingFileTitle();
      String nextFileTitle = "";
      File nextFile;
      try {
        nextFile = QueueModel.getItem(QueueModel.getIndex() + 1).getFile();
      } catch (Exception e) {
        nextFile = QueueModel.getPlanned().get(0).getFile();
      }
      String pattern = Conf.getString(Const.CONF_PATTERN_FRAME_TITLE);
      try {
        nextFileTitle = UtilString.applyPattern(nextFile, pattern, false, false);
      } catch (JajukException e) {
        Log.error(e);
      }
      return "  |  Playing: " + currentTrack + "  |  Next: " + nextFileTitle;
    } catch (Exception e) {
      return Messages.getString("JajukWindow.17");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seejava.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event. MouseWheelEvent)
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource().equals(jbVolume) && !Conf.getBoolean(Const.CONF_BIT_PERFECT)) {
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
      // Force tooltip refresh live
      hideToolTip(jbVolume);
      postToolTip(jbVolume);
      MuteAction.setVolumeIcon(newVolume);
    }
  }

  /**
   * Force tooltip refresh Thanks Santhosh Kumar
   * http://www.jroller.com/santhosh/entry/tooltips_can_say_more
   * 
   * @param comp 
   */
  public static void postToolTip(JComponent comp) {
    Action action = comp.getActionMap().get("postTip");
    if (action == null) { // no tooltip
      return;
    }
    ActionEvent ae = new ActionEvent(comp, ActionEvent.ACTION_PERFORMED, "postTip",
        EventQueue.getMostRecentEventTime(), 0);
    action.actionPerformed(ae);
  }

  /**
   * Remove tooltip Thanks Santhosh Kumar
   * http://www.jroller.com/santhosh/entry/tooltips_can_say_more
   * 
   * @param comp 
   */
  public static void hideToolTip(JComponent comp) {
    Action action = comp.getActionMap().get("hideTip");
    if (action == null) { // no tooltip
      return;
    }
    ActionEvent ae = new ActionEvent(comp, ActionEvent.ACTION_PERFORMED, "hideTip",
        EventQueue.getMostRecentEventTime(), 0);
    action.actionPerformed(ae);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  @Override
  public void update(final JajukEvent event) {
    // Update window title
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.FILE_LAUNCHED.equals(subject)
            || JajukEvents.WEBRADIO_LAUNCHED.equals(subject)
            || JajukEvents.PLAYER_STOP.equals(subject)) {
          updateCurrentTitle();
        } else if (JajukEvents.PARAMETERS_CHANGE.equals(event.getSubject())) {
          // Disable volume GUI in bit perfect mode
          jbVolume.setEnabled(!Conf.getBoolean(Const.CONF_BIT_PERFECT));
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
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
   * when clicking on it.
   */
  private void showBalloon() {
    // Leave if balloon already visible
    if (balloon != null && balloon.isVisible()) {
      return;
    }
    balloon = new JajukInformationDialog(QueueModel.getCurrentFileTitle());
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

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Window#dispose()
   */
  @Override
  public void dispose() {
    // there are some resources to close in the Search-Box that I could not get
    // rid of with any of the default dispose-methods in Swing...
    sbSearch.close();
    super.dispose();
  }
}