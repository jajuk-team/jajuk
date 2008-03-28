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

import static org.jajuk.ui.actions.JajukAction.FAST_FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukAction.MUTE_STATE;
import static org.jajuk.ui.actions.JajukAction.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukAction.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukAction.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.actions.JajukAction.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukAction.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukAction.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukAction.STOP_TRACK;
import ext.JScrollingText;
import ext.SwingWorker;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jajuk.Main;
import org.jajuk.base.SearchResult;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.StackItem;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukTimer;
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
 * <p> Singleton</p>
 */
public class JajukSlimWindow extends JFrame implements ITechnicalStrings, Observer,
    MouseWheelListener, ListSelectionListener {

  private static final long serialVersionUID = 1L;

  JLabel jajuk;

  JButton jbPrevious;

  JButton jbNext;

  JPressButton jbRew;

  JButton jbPlayPause;

  JButton jbStop;

  JPressButton jbFwd;

  JButton jbBestof;

  JButton jbNovelties;

  JButton jbRandom;

  JButton jbFinishAlbum;

  JButton jbClose;
  
  JButton jbMaximize;

  JajukToggleButton jbVolume;

  JScrollingText scrollingText;

  SearchBox sbSearch;

  JToolBar slimJajuk;
  
  private static JajukSlimWindow self;
  
  public static JajukSlimWindow getInstance(){
    if (self == null){
      self = new JajukSlimWindow();
      self.initUI();
      self.setVisible(false);
    }
    return self;
  }
  
  private  JajukSlimWindow() {
  }
  
  public void initUI(){
    JToolBar jtbPlay = new JToolBar();
    jtbPlay.setBorder(null);
    jtbPlay.setRollover(true);
    ActionUtil.installKeystrokes(jtbPlay, ActionManager.getAction(NEXT_ALBUM), ActionManager
        .getAction(PREVIOUS_ALBUM));
    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    jbPrevious.setIcon(IconLoader.ICON_PREVIOUS);
    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbNext.setIcon(IconLoader.ICON_NEXT);
    jbRew = new JPressButton(ActionManager.getAction(REWIND_TRACK));
    jbRew.setIcon(IconLoader.ICON_REW_16x16);
    jbPlayPause = new JajukButton(ActionManager.getAction(PLAY_PAUSE_TRACK));
    jbPlayPause.setIcon(IconLoader.ICON_PAUSE_16x16);
    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
    jbStop.setIcon(IconLoader.ICON_STOP_16x16);
    jbFwd = new JPressButton(ActionManager.getAction(FAST_FORWARD_TRACK));
    jbFwd.setIcon(IconLoader.ICON_FWD_16x16);

    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbRew);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbStop);
    jtbPlay.add(jbFwd);
    jtbPlay.add(jbNext);

    JToolBar jtbSmart = new JToolBar();
    jtbSmart.setBorder(null);

    jbBestof = new JajukButton(ActionManager.getAction(JajukAction.BEST_OF));
    jbBestof.setIcon(IconLoader.ICON_BESTOF_16x16);
    jbNovelties = new JajukButton(ActionManager.getAction(JajukAction.NOVELTIES));
    jbNovelties.setIcon(IconLoader.ICON_NOVELTIES_16x16);
    jbRandom = new JajukButton(ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL));
    jbRandom.setIcon(IconLoader.ICON_SHUFFLE_GLOBAL_16x16);
    jbFinishAlbum = new JajukButton(ActionManager.getAction(JajukAction.FINISH_ALBUM));
    jbFinishAlbum.setIcon(IconLoader.ICON_FINISH_ALBUM_16x16);

    jtbSmart.add(jbBestof);
    jtbSmart.add(jbNovelties);
    jtbSmart.add(jbRandom);
    jtbSmart.add(jbFinishAlbum);

    JToolBar jtbTools = new JToolBar();
    jtbTools.setBorder(null);

    int iVolume = (int) (100 * ConfigurationManager.getFloat(CONF_VOLUME));
    if (iVolume > 100) { // can occur in some undefined cases
      iVolume = 100;
    }
    jbVolume = new JajukToggleButton(ActionManager.getAction(MUTE_STATE));
    jbVolume.addMouseWheelListener(this);
    setVolumeIcon(iVolume);

    jbClose = new JajukButton(IconLoader.ICON_CLOSE);
    jbClose.setToolTipText(Messages.getString("Close"));
    jbClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ObservationManager.notify(new Event(EventSubject.EVENT_PARAMETERS_CHANGE));
        if(!JajukWindow.getInstance().isVisible())
          JajukWindow.getInstance().display(true);
        setVisible(false);
      }
    });
    
    jbMaximize = new JajukButton(IconLoader.ICON_FULL_WINDOW);
    jbMaximize.setToolTipText(Messages.getString("Maximize"));
    jbMaximize.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JajukWindow.getInstance().display(true);
        ObservationManager.notify(new Event(EventSubject.EVENT_PARAMETERS_CHANGE));
        setVisible(false);
      }
    });

    jtbTools.add(jbVolume);
    jtbTools.addSeparator();
    jtbTools.add(jbMaximize);
    jtbTools.add(jbClose);

    // Search
    double[][] sizeSearch = new double[][] {{75}, {22}};
    JPanel jpSearch = new JPanel(new TableLayout(sizeSearch));
    sbSearch = new SearchBox(this);
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

    slimJajuk.addSeparator();
    slimJajuk.add(jtbSmart);
    slimJajuk.addSeparator();
    slimJajuk.add(jpSearch);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbPlay);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbText);
    slimJajuk.addSeparator();
    slimJajuk.add(jtbTools);

    slimJajuk.setBorder(BorderFactory.createRaisedBevelBorder());

    add(slimJajuk);
    ObservationManager.register(this);

    setUndecorated(true);
    getRootPane().setWindowDecorationStyle(JRootPane.NONE);
    setTitle(Messages.getString("JajukSlimWindow.0"));
    setVisible(true);
    pack();
    //Notify that slimbar is now visible (menu bar is interested in) 
    ObservationManager.notify(new Event(EventSubject.EVENT_PARAMETERS_CHANGE));
  }

  /**
   * @return Player Info : current and next track
   */
  public String getPlayerInfo() {
    try {
      String currentTrack = Main.getWindow().buildTitle(FIFO.getInstance().getCurrentFile());
      String nextTrack = "";
      try {
        nextTrack = Main.getWindow().buildTitle(
            FIFO.getInstance().getItem(FIFO.getInstance().getIndex() + 1).getFile());
      } catch (Exception e) {
        nextTrack = Main.getWindow().buildTitle(FIFO.getInstance().getPlanned().get(0).getFile());
      }
      return "  |  Playing: " + currentTrack + "  |  Next: " + nextTrack;
    } catch (Exception e) {
      return Messages.getString("JajukWindow.17");
    }
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
    return eventSubjectSet;
  }

  public void update(final Event event) {
    EventSubject subject = event.getSubject();
    if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
      scrollingText.setText(getPlayerInfo());
    } else if (EventSubject.EVENT_PLAYER_STOP.equals(subject)) {
      scrollingText.setText(Messages.getString("JajukWindow.17"));
    } else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)) {
      jbPlayPause.setIcon(IconLoader.ICON_PLAY_16x16);
    } else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)) {
      jbPlayPause.setIcon(IconLoader.ICON_PAUSE_16x16);
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
}