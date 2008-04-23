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

import static org.jajuk.ui.actions.JajukAction.MUTE_STATE;
import static org.jajuk.ui.actions.JajukAction.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukAction.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukAction.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.actions.JajukAction.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukAction.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukAction.STOP_TRACK;
import ext.DropDownButton;
import ext.JScrollingText;
import ext.SwingWorker;
import info.clearthought.layout.TableLayout;

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
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.base.SearchResult;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionBase;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Jajuk Slim Interface
 * <p>
 * Singleton
 * </p>
 */
public class JajukSlimWindow extends JFrame implements ITechnicalStrings, Observer,
    MouseWheelListener, ListSelectionListener, ActionListener {

  private static final long serialVersionUID = 1L;

  JLabel jajuk;

  JButton jbPrevious;

  JButton jbNext;

  JButton jbPlayPause;

  JButton jbStop;

  DropDownButton jbIncRate;

  DropDownButton jddbSmart;

  JPopupMenu jpmSmart;

  JMenuItem jbBestof;

  JMenuItem jbNovelties;

  JMenuItem jbRandom;

  JMenuItem jbFinishAlbum;

  JButton jbMaximize;

  SizedButton jbVolume;

  JScrollingText scrollingText;

  SearchBox sbSearch;

  JToolBar slimJajuk;

  /** True if user close the slim bar from the taskbar */
  private boolean closing = false;

  private static JajukSlimWindow self;

  public static JajukSlimWindow getInstance() {
    if (self == null) {
      self = new JajukSlimWindow();
    }
    return self;
  }

  /**
   * This mouse motion listener allows the whole slim bar dragging
   */
  private MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
    public void mouseDragged(MouseEvent e) {
      Point point = e.getLocationOnScreen();
      setLocation(point);
      ConfigurationManager.setProperty(CONF_SLIMBAR_POSITION, (int) point.getX() + ","
          + (int) point.getY());
    }
  };

  private JajukSlimWindow() {
    setUndecorated(true);
    // Set windows decoration to look and feel
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
  }

  public void initUI() {
    setIconImage(IconLoader.ICON_LOGO.getImage());

    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {
        try {
          closing = true;
          ActionManager.getAction(JajukAction.EXIT).perform(null);
        } catch (Exception e1) {
          Log.error(e1);
        }
      }
    });

    JToolBar jtbPlay = new JToolBar();
    jtbPlay.setBorder(null);
    jtbPlay.setRollover(true);
    ActionUtil.installKeystrokes(jtbPlay, ActionManager.getAction(NEXT_ALBUM), ActionManager
        .getAction(PREVIOUS_ALBUM));
    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    jbPrevious.addMouseMotionListener(motionAdapter);
    jbPrevious.setIcon(IconLoader.ICON_PREVIOUS);
    jbPrevious.addMouseMotionListener(motionAdapter);

    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbNext.setIcon(IconLoader.ICON_NEXT);
    jbNext.addMouseMotionListener(motionAdapter);

    jbPlayPause = new JajukButton(ActionManager.getAction(PLAY_PAUSE_TRACK));
    jbPlayPause.setIcon(IconLoader.ICON_PAUSE_16x16);
    jbPlayPause.addMouseMotionListener(motionAdapter);

    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
    jbStop.setIcon(IconLoader.ICON_STOP_16x16);
    jbStop.addMouseMotionListener(motionAdapter);

    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbStop);
    jtbPlay.add(jbNext);

    JToolBar jtbSmart = new JToolBar();
    jtbSmart.setBorder(null);

    jddbSmart = new DropDownButton(IconLoader.ICON_INC_RATING) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JPopupMenu getPopupMenu() {
        return jpmSmart;
      }
    };

    jbBestof = new JMenuItem(ActionManager.getAction(JajukAction.BEST_OF));
    jbBestof.setIcon(IconLoader.ICON_BESTOF_16x16);
    jbBestof.addActionListener(this);

    jbNovelties = new JMenuItem(ActionManager.getAction(JajukAction.NOVELTIES));
    jbNovelties.setIcon(IconLoader.ICON_NOVELTIES_16x16);
    jbNovelties.addActionListener(this);

    jbRandom = new JMenuItem(ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL));
    jbRandom.setIcon(IconLoader.ICON_SHUFFLE_GLOBAL_16x16);
    jbRandom.addActionListener(this);

    jbFinishAlbum = new JMenuItem(ActionManager.getAction(JajukAction.FINISH_ALBUM));
    jbFinishAlbum.setIcon(IconLoader.ICON_FINISH_ALBUM_16x16);
    jbFinishAlbum.addActionListener(this);

    jpmSmart = new JPopupMenu();
    jpmSmart.add(jbBestof);
    jpmSmart.add(jbNovelties);
    jpmSmart.add(jbRandom);
    jpmSmart.add(jbFinishAlbum);
    jddbSmart.addToToolBar(jtbSmart);
    jddbSmart.addMouseMotionListener(motionAdapter);
    jddbSmart.setAction(ActionManager.getAction(JajukAction.BEST_OF));
    jddbSmart.setIcon(IconLoader.ICON_BESTOF_16x16);

    ActionBase actionIncRate = ActionManager.getAction(JajukAction.INC_RATE);
    actionIncRate.setName(null);
    final JPopupMenu jpmIncRating = new JPopupMenu();
    for (int i = 1; i <= 10; i++) {
      final int j = i;
      JMenuItem jmi = new JMenuItem("+" + i);
      if (ConfigurationManager.getInt(CONF_INC_RATING) == i) {
        jmi.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      }
      // Store selected value
      jmi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          ConfigurationManager.setProperty(CONF_INC_RATING, "" + j);
        }
      });
      jpmIncRating.add(jmi);
    }
    jbIncRate = new DropDownButton(IconLoader.ICON_INC_RATING) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JPopupMenu getPopupMenu() {
        return jpmIncRating;
      }
    };
    jbIncRate.setAction(actionIncRate);
    jbIncRate.addToToolBar(jtbSmart);

    JToolBar jtbTools = new JToolBar();
    jtbTools.setBorder(null);

    int iVolume = (int) (100 * ConfigurationManager.getFloat(CONF_VOLUME));
    if (iVolume > 100) { // can occur in some undefined cases
      iVolume = 100;
    }
    jbVolume = new SizedButton(ActionManager.getAction(MUTE_STATE), 16, 16, false);

    jbVolume.addMouseMotionListener(motionAdapter);
    jbVolume.addMouseWheelListener(this);
    jbVolume.setText(null);
    setVolumeIcon(iVolume);

    jbMaximize = new JajukButton(ActionManager.getAction(JajukAction.SLIM_JAJUK));
    jbMaximize.addMouseMotionListener(motionAdapter);

    jtbTools.add(jbVolume);
    jtbTools.addSeparator();
    jtbTools.add(jbMaximize);

    // Search
    double[][] sizeSearch = new double[][] { { 20 }, { 22 } };
    JPanel jpSearch = new JPanel(new TableLayout(sizeSearch));
    sbSearch = new SearchBox(this);
    sbSearch.addMouseMotionListener(motionAdapter);
    jpSearch.add(sbSearch, "0,0");

    JToolBar jtbText = new JToolBar();
    jtbText.setBorder(null);

    scrollingText = new JScrollingText(getPlayerInfo(), -3);
    scrollingText.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    scrollingText.setPreferredSize(new Dimension(150, 15));
    scrollingText.start();

    jtbText.add(scrollingText);

    slimJajuk = new JToolBar();
    slimJajuk.setBorder(null);
    slimJajuk.setFloatable(false);
    slimJajuk.setRollover(true);

    slimJajuk.add(jpSearch);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbSmart);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbPlay);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbTools);

    slimJajuk.setBorder(BorderFactory.createRaisedBevelBorder());

    getRootPane().setToolTipText(getPlayerInfo());

    add(slimJajuk);
    ObservationManager.register(this);

    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    if (FIFO.getInstance().getCurrentFile() != null) {
      setTitle(Util.buildTitle(FIFO.getInstance().getCurrentFile()));
    } else {
      setTitle(Messages.getString("JajukWindow.17"));
    }
    setVisible(true);
    setAlwaysOnTop(true);

    // Set location
    String lastPosition = ConfigurationManager.getProperty(CONF_SLIMBAR_POSITION);
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
      setLocation(new Point(x, y));
    } catch (Exception e) {
      Log.debug("Cannot restore slimbar position");
      Log.error(e);
    }
    pack();
    // Notify that slimbar is now visible (menu bar is interested in)
    ObservationManager.notify(new Event(EventSubject.EVENT_PARAMETERS_CHANGE));
  }

  /**
   * @return Player Info : current and next track
   */
  public String getPlayerInfo() {
    try {
      String currentTrack = Util.buildTitle(FIFO.getInstance().getCurrentFile());
      String nextTrack = "";
      try {
        nextTrack = Util.buildTitle(FIFO.getInstance().getItem(FIFO.getInstance().getIndex() + 1)
            .getFile());
      } catch (Exception e) {
        nextTrack = Util.buildTitle(FIFO.getInstance().getPlanned().get(0).getFile());
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
  public boolean isVisible() {
    return super.isVisible() || closing;
  }

  /**
   * Set Volume Icon
   */
  public void setVolumeIcon(final float fVolume) {
    if (fVolume <= 0) {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/mute_16x16.png"));
      jbVolume.setIcon(icon);
    } else if (fVolume <= 25) {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/volume1.png"));
      jbVolume.setIcon(icon);
    } else if (fVolume <= 50) {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/volume2.png"));
      jbVolume.setIcon(icon);
    } else if (fVolume <= 75) {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/volume3.png"));
      jbVolume.setIcon(icon);
    } else {
      Icon icon = new ImageIcon(Util.getResource("icons/16x16/volume4.png"));
      jbVolume.setIcon(icon);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource() == jbVolume) {
      int oldVolume = (int) (100 * Player.getCurrentVolume());
      int newVolume = oldVolume - (e.getUnitsToScroll() * 3);
      jbVolume.removeMouseWheelListener(this);
      // if user move the volume slider, unmute
      if (Player.isMuted()) {
        Player.mute(false);
      }

      if (newVolume > 100)
        newVolume = 100;
      else if (newVolume < 0)
        newVolume = 0;

      Player.setVolume((float) newVolume / 100);
      jbVolume.addMouseWheelListener(this);
      jbVolume.setToolTipText(newVolume + " %");
      setVolumeIcon(newVolume);
    }
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_WEBRADIO_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_PAUSE);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_RESUME);
    eventSubjectSet.add(EventSubject.EVENT_QUEUE_NEED_REFRESH);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
    eventSubjectSet.add(EventSubject.EVENT_MUTE_STATE);
    return eventSubjectSet;
  }

  public void update(final Event event) {
    EventSubject subject = event.getSubject();
    if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
      setTitle(Util.buildTitle(FIFO.getInstance().getCurrentFile()));
      scrollingText.setText(getPlayerInfo());
    } else if (EventSubject.EVENT_PLAYER_STOP.equals(subject)) {
      scrollingText.setText(Messages.getString("JajukWindow.17"));
    } else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)) {
      jbPlayPause.setIcon(IconLoader.ICON_PLAY_16x16);
    } else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)) {
      jbPlayPause.setIcon(IconLoader.ICON_PAUSE_16x16);
    } else if (EventSubject.EVENT_MUTE_STATE.equals(subject)) {
      if (Player.isMuted()) {
        setVolumeIcon(0);
      } else {
        setVolumeIcon((int) (100 * Player.getCurrentVolume()));
      }
    }
  }

  public void valueChanged(final ListSelectionEvent e) {
    SwingWorker sw = new SwingWorker() {
      public Object construct() {
        if (!e.getValueIsAdjusting()) {
          SearchResult sr = sbSearch.alResults.get(sbSearch.jlist.getSelectedIndex());
          try {
            // If user selected a file
            if (sr.getType() == SearchResultType.FILE) {
              FIFO.getInstance().push(
                  new StackItem(sr.getFile(), ConfigurationManager.getBoolean(CONF_STATE_REPEAT),
                      true), ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_CLICK));
            }
            // User selected a web radio
            else if (sr.getType() == SearchResultType.WEBRADIO) {
              FIFO.getInstance().launchRadio(sr.getWebradio());
            }
          } catch (JajukException je) {
            Log.error(je);
          }
        }
        return null;
      }

      public void finished() {
        if (!e.getValueIsAdjusting()) {
          sbSearch.popup.hide();
          requestFocusInWindow();
        }
      }
    };
    sw.start();
  }

  public void actionPerformed(final ActionEvent ae) {
    if (ae.getSource() == jbBestof) {
      jddbSmart.setAction(ActionManager.getAction(JajukAction.BEST_OF));
      jddbSmart.setIcon(IconLoader.ICON_BESTOF_16x16);
    } else if (ae.getSource() == jbNovelties) {
      jddbSmart.setAction(ActionManager.getAction(JajukAction.NOVELTIES));
      jddbSmart.setIcon(IconLoader.ICON_NOVELTIES_16x16);
    } else if (ae.getSource() == jbRandom) {
      jddbSmart.setAction(ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL));
      jddbSmart.setIcon(IconLoader.ICON_SHUFFLE_GLOBAL_16x16);
    } else if (ae.getSource() == jbFinishAlbum) {
      jddbSmart.setAction(ActionManager.getAction(JajukAction.FINISH_ALBUM));
      jddbSmart.setIcon(IconLoader.ICON_FINISH_ALBUM_16x16);
    }
  }
}