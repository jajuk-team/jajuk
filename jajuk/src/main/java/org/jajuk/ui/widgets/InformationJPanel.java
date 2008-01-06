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

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.base.File;
import org.jajuk.base.WebRadio;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Status / information panel ( static view )
 */
public class InformationJPanel extends JPanel implements ITechnicalStrings, Observer,
    ChangeListener, MouseWheelListener {

  private static final long serialVersionUID = 1L;

  // consts
  /** Informative message type ( displayed in blue ) * */

  public static final int INFORMATIVE = 0;

  /** Informative message type ( displayed in red )* */
  public static final int ERROR = 1;

  /** Warning message type ( displayed in orange )* */
  public static final int WARNING = 2;

  /** Self instance */
  static private InformationJPanel ijp = null;

  /** Last slider manual move date */
  private static long lDateLastAdjust;

  /** Swing Timer to refresh the component */
  private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      update(new Event(EventSubject.EVENT_HEART_BEAT));
    }
  });

  /**
   * Singleton access
   * 
   * @return
   */
  public static synchronized InformationJPanel getInstance() {
    if (ijp == null) {
      ijp = new InformationJPanel();
    }
    return ijp;
  }

  // widgets declaration

  JLabel jlMessage;

  JLabel jlSelection;

  JLabel jlTotal;

  JSlider jsPosition;

  JLabel jlCurrent;

  // attributes

  String sMessage;

  /** Current message type */
  int iType = 0;

  String sSelection;

  int iTotalStatus;

  String sTotalStatus;

  int iCurrentStatus;

  String sCurrentStatus;

  private InformationJPanel() {
    // dimensions
    // set current jpanel properties
    double size[][] = { { 0.44, 0.13, 0.10, 0.33 }, { TableLayout.PREFERRED } };
    setLayout(new TableLayout(size));

    // message bar
    JToolBar jtbMessage = new JToolBar();
    jtbMessage.setFloatable(false);
    // Set a zero minimum size to allow user to reduce window width
    jtbMessage.setMinimumSize(new Dimension(0, 0));
    // We use toolbar to display vertical separator lines
    jlMessage = new JLabel();
    setMessage(Messages.getString("JajukWindow.18"), InformationJPanel.INFORMATIVE);
    jtbMessage.add(jlMessage);
    jtbMessage.add(Box.createHorizontalGlue());
    jtbMessage.addSeparator();

    // selection bar
    JToolBar jtbSelection = new JToolBar();
    jtbSelection.setFloatable(false);
    jtbMessage.setMinimumSize(new Dimension(0, 0));
    jlSelection = new JLabel();
    jtbSelection.add(jlSelection);
    jtbSelection.add(Box.createHorizontalGlue());
    jtbSelection.addSeparator();

    // total progress bar
    JToolBar jtbTotal = new JToolBar();
    jtbTotal.setMinimumSize(new Dimension(0, 0));
    jtbTotal.setFloatable(false);
    jlTotal = new JLabel();
    jlTotal.setToolTipText(Messages.getString("InformationJPanel.5"));
    jtbTotal.add(jlTotal);
    jtbTotal.add(Box.createHorizontalGlue());
    jtbTotal.addSeparator();

    // current progress bar
    JToolBar jtbProgress = new JToolBar();
    jtbProgress.setMinimumSize(new Dimension(0, 0));
    jtbProgress.setFloatable(false);
    jtbProgress.setToolTipText(Messages.getString("InformationJPanel.7"));
    jsPosition = new JSlider(0, 100, 0);
    jsPosition.addChangeListener(this);
    jsPosition.addMouseWheelListener(InformationJPanel.this);
    jsPosition.setEnabled(false);
    jsPosition.setToolTipText(Messages.getString("CommandJPanel.15"));
    jtbProgress.add(jsPosition);
    jlCurrent = new JLabel();
    jtbProgress.add(jlCurrent);
    jtbProgress.add(Box.createHorizontalStrut(6));

    // add widgets
    add(jtbMessage, "0,0");
    add(jtbSelection, "1,0");
    add(jtbTotal, "2,0");
    add(jtbProgress, "3,0");

    // check if some track has been launched before the view has been
    // displayed
    update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
        .getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
    // check if some errors occured before the view has been displayed
    if (ObservationManager.containsEvent(EventSubject.EVENT_PLAY_ERROR)) {
      update(new Event(EventSubject.EVENT_PLAY_ERROR, ObservationManager
          .getDetailsLastOccurence(EventSubject.EVENT_PLAY_ERROR)));
    }
    // Check if a webradio has been launch before this view is visible
    update(new Event(EventSubject.EVENT_WEBRADIO_LAUNCHED, ObservationManager
        .getDetailsLastOccurence(EventSubject.EVENT_WEBRADIO_LAUNCHED)));
    // register for given events
    ObservationManager.register(this);
    // start timer
    timer.start();
  }

  public Set<EventSubject> getRegistrationKeys() {
    HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
    eventSubjectSet.add(EventSubject.EVENT_ZERO);
    eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_PLAY_ERROR);
    eventSubjectSet.add(EventSubject.EVENT_WEBRADIO_LAUNCHED);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_PAUSE);
    eventSubjectSet.add(EventSubject.EVENT_PLAYER_RESUME);
    return eventSubjectSet;
  }

  /**
   * @return
   */
  public int getCurrentStatus() {
    return iCurrentStatus;
  }

  /**
   * @return
   */
  public int getTotalTime() {
    return iTotalStatus;
  }

  /**
   * @return
   */
  public String getMessage() {
    return sMessage;
  }

  /**
   * @return
   */
  public String getSelection() {
    return this.sSelection;
  }

  /**
   * @param label
   */
  public void setMessage(final String sMessage, final int iMessageType) {
    this.sMessage = sMessage;
    this.iType = iMessageType;
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        InformationJPanel.this.sMessage = sMessage;
        jlMessage.setText(sMessage);
        jlMessage.setToolTipText(sMessage);
      }
    });
  }

  /**
   * @param label
   */
  public void setSelection(String sSelection) {
    this.sSelection = sSelection;
    jlSelection.setText(sSelection);
    jlSelection.setToolTipText(sSelection);
  }

  /**
   * @return
   */
  public String getCurrentStatusMessage() {
    return sCurrentStatus;
  }

  /**
   * @return
   */
  public String getTotalTimeMessage() {
    return sTotalStatus;
  }

  /**
   * 
   * Set the current status for current track ex : 01:01:01/02:02:02
   * 
   * @param string
   */
  public void setCurrentTimeMessage(String string) {
    sCurrentStatus = string;
    jlCurrent.setText(string);
  }

  /**
   * @param string
   */
  public void setTotalTimeMessage(String string) {
    sTotalStatus = string;
    jlTotal.setText(string);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final Event event) {
    final EventSubject subject = event.getSubject();
    // do not insert this subject inside the invokeLater because we have to
    // leave the awt dispatcher called inside the setMessage and THEN, sleep
    // for 2 secs.
    if (EventSubject.EVENT_PLAY_ERROR.equals(subject)) {
      try {
        // we synchronize this code to make sure error message is
        // visible all 2
        // secs
        synchronized (this) {
          // reset data
          setCurrentTimeMessage(Util.formatTimeBySec(0, false) + " / "
              + Util.formatTimeBySec(0, false));
          // set error message
          Object o = ObservationManager.getDetail(event, DETAIL_CONTENT);
          // current item is a file
          if (o instanceof File) {
            File fCurrent = (File) o;
            if (fCurrent != null) {
              // display associated error code is given
              String sReason = (String) ObservationManager.getDetail(event, DETAIL_REASON);
              if (sReason != null) {
                setMessage(Messages.getString("Error." + sReason) + ": "
                    + fCurrent.getAbsolutePath(), InformationJPanel.ERROR);
              } else {// default message
                setMessage(Messages.getString("Error.007") + ": " + fCurrent.getAbsolutePath(),
                    InformationJPanel.ERROR);
              }
            } else { // none specified file
              setMessage(Messages.getString("Error.007"), InformationJPanel.ERROR);
            }
          } else if (o instanceof WebRadio) {
            WebRadio radio = (WebRadio) o;
            if (radio != null) {
              // display associated error code is given
              String sReason = (String) ObservationManager.getDetail(event, DETAIL_REASON);
              if (sReason != null) {
                setMessage(Messages.getString("Error." + sReason) + ": " + radio.toString(),
                    InformationJPanel.ERROR);
              } else {// default message
                setMessage(Messages.getString("Error.007") + ": " + radio.toString(),
                    InformationJPanel.ERROR);
              }
            } else { // none specified file
              setMessage(Messages.getString("Error.170"), InformationJPanel.ERROR);
            }
          }
        }
      } catch (Exception e) {
        Log.error(e);
      }
    } else {
      // [PERF] compute this outside the AWT thread for perfs
      final long timeToPlay = JajukTimer.getInstance().getTotalTimeToPlay();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (EventSubject.EVENT_HEART_BEAT.equals(subject) && !FIFO.isStopped()
              && !Player.isPaused()) {
            long length = JajukTimer.getInstance().getCurrentTrackTotalTime();
            long lTime = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
            int iPos = (int) (100 * JajukTimer.getInstance().getCurrentTrackPosition());
            String sCurrentTotalMessage = Util.formatTimeBySec(timeToPlay, false);
            setTotalTimeMessage(sCurrentTotalMessage + " [" + FIFO.getInstance().getFIFO().size()
                + "]");
            setCurrentTimeMessage(Util.formatTimeBySec(lTime, false) + " / "
                + Util.formatTimeBySec(length, false));
            // Make sure to enable the slider
            if (!jsPosition.isEnabled()) {
              jsPosition.setEnabled(true);
            }
            // if position is adjusting, don't disturb user
            if (jsPosition.getValueIsAdjusting() || Player.isSeeking()) {
              return;
            }
            // make sure not to set to old position
            if ((System.currentTimeMillis() - lDateLastAdjust) < 2000) {
              return;
            }
            // remove and re-add listener to make sure not to add it
            // twice
            jsPosition.removeChangeListener(InformationJPanel.this);
            jsPosition.setValue(iPos);
            jsPosition.addChangeListener(InformationJPanel.this);
          } else if (EventSubject.EVENT_ZERO.equals(subject)
              || EventSubject.EVENT_PLAYER_STOP.equals(subject)) {
            setCurrentTimeMessage(Util.formatTimeBySec(0, false) + " / "
                + Util.formatTimeBySec(0, false));
            jsPosition.setEnabled(false);
            jsPosition.removeMouseWheelListener(InformationJPanel.this);
            jsPosition.removeChangeListener(InformationJPanel.this);
            // use set value, not setPosition that would cause
            // a seek that could fail with some formats
            jsPosition.setValue(0);
            // reset startup position
            ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION, "0");
            setTotalTimeMessage("00:00:00");
            setMessage(Messages.getString("JajukWindow.18"), InformationJPanel.INFORMATIVE);
          } else if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
            File file = FIFO.getInstance().getCurrentFile();
            if (file != null) {
              String sMessage = Messages.getString("FIFO.10") + " " + file.getTrack().getName()
                  + " " + Messages.getString("By") + " " + file.getTrack().getAuthor().getName2()
                  + " " + Messages.getString("On") + " " + file.getTrack().getAlbum().getName2();

              setMessage(sMessage, InformationJPanel.INFORMATIVE);
            }
          } else if (EventSubject.EVENT_WEBRADIO_LAUNCHED.equals(subject)) {
            if (event.getDetails() == null) {
              return;
            }
            WebRadio radio = (WebRadio) event.getDetails().get(DETAIL_CONTENT);
            if (radio != null) {
              String sMessage = Messages.getString("FIFO.14") + " " + radio.getName();
              setMessage(sMessage, InformationJPanel.INFORMATIVE);
            }
          } else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)) {
            jsPosition.setEnabled(false);
            jsPosition.removeMouseWheelListener(InformationJPanel.this);
            jsPosition.removeChangeListener(InformationJPanel.this);
          } else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)) {
            // Avoid adding listeners twice
            if (jsPosition.getMouseWheelListeners().length == 0) {
              jsPosition.addMouseWheelListener(InformationJPanel.this);
            }
            if (jsPosition.getChangeListeners().length == 0) {
              jsPosition.addChangeListener(InformationJPanel.this);
            }
            jsPosition.setEnabled(true);
          }
        }
      });
    }
  }

  /**
   * toString() method
   */
  public String toString() {
    return getClass().getName();
  }

  public int getMessageType() {
    return iType;
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

  /**
   * @return Position value
   */
  public int getCurrentPosition() {
    return this.jsPosition.getValue();
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
    }
  }
}