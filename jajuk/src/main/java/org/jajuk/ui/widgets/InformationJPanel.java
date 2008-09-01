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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.text.MessageFormat;
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
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * Status / information panel ( static view )
 */
public final class InformationJPanel extends JPanel implements Const, Observer,
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
  private static InformationJPanel ijp = null;

  /** Last slider manual move date */
  private static long lDateLastAdjust;

  /** Swing Timer to refresh the component */
  private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      update(new Event(JajukEvents.HEART_BEAT));
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
    jtbProgress.add(jsPosition);
    jlCurrent = new JLabel();
    jlCurrent.setToolTipText(Messages.getString("CommandJPanel.15"));
    jlCurrent.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int currentFormat = Conf.getInt(CONF_FORMAT_TIME_ELAPSED);
        Conf.setProperty(CONF_FORMAT_TIME_ELAPSED, Integer
            .toString(((currentFormat + 1) % FORMAT_TIME_ELAPSED_MAX)));
      }
    });
    jtbProgress.add(jlCurrent);
    jtbProgress.add(Box.createHorizontalStrut(6));

    // add widgets
    add(jtbMessage, "0,0");
    add(jtbSelection, "1,0");
    add(jtbTotal, "2,0");
    add(jtbProgress, "3,0");

    // check if some track has been launched before the view has been
    // displayed
    update(new Event(JajukEvents.FILE_LAUNCHED, ObservationManager
        .getDetailsLastOccurence(JajukEvents.FILE_LAUNCHED)));
    // check if some errors occured before the view has been displayed
    if (ObservationManager.containsEvent(JajukEvents.PLAY_ERROR)) {
      update(new Event(JajukEvents.PLAY_ERROR, ObservationManager
          .getDetailsLastOccurence(JajukEvents.PLAY_ERROR)));
    }
    // Check if a track or a webradio has been launch before this view is
    // visible
    if (FIFO.isPlayingRadio()) {
      update(new Event(JajukEvents.WEBRADIO_LAUNCHED, ObservationManager
          .getDetailsLastOccurence(JajukEvents.WEBRADIO_LAUNCHED)));
    } else {
      update(new Event(JajukEvents.FILE_LAUNCHED, ObservationManager
          .getDetailsLastOccurence(JajukEvents.FILE_LAUNCHED)));
    }
    // register for given events
    ObservationManager.register(this);
    // start timer
    timer.start();
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAY_ERROR);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
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
  public void setCurrentTimeMessage(long lTime, long length) {
    String string;
    int timeFormat = 0;
    // Set the required decimal precision for percentage here
    DecimalFormat df = new DecimalFormat("0"); // (0.##) for 2 decimal places
    try {
      timeFormat = Conf.getInt(Const.CONF_FORMAT_TIME_ELAPSED);
    } catch (Exception e) {
      Log.debug(e);
    }
    float lTimePercent = 0f;
    if (lTime > 0) {
      lTimePercent = (float) ((float) lTime / (float) length * 100.0);
    }
    switch (timeFormat) {
    case 0: {
      string = UtilString.formatTimeBySec(lTime) + " / " + UtilString.formatTimeBySec(length);
      break;
    }
    case 1: {
      string = "-" + UtilString.formatTimeBySec(length - lTime) + " / "
          + UtilString.formatTimeBySec(length);
      break;
    }
    case 2: {
      string = df.format(lTimePercent) + " % / " + UtilString.formatTimeBySec(length);
      break;
    }
    case 3: {
      string = df.format(lTimePercent - 100f) + " % / " + UtilString.formatTimeBySec(length);
      break;
    }
    default: {
      string = UtilString.formatTimeBySec(lTime) + " / " + UtilString.formatTimeBySec(length);
    }
    }
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
    final JajukEvents subject = event.getSubject();
    // do not insert this subject inside the invokeLater because we have to
    // leave the awt dispatcher called inside the setMessage and THEN, sleep
    // for 2 secs.
    if (JajukEvents.PLAY_ERROR.equals(subject)) {
      try {
        // we synchronize this code to make sure error message is
        // visible all 2
        // secs
        synchronized (this) {
          // reset data
          setCurrentTimeMessage(0, 0);
          // set error message
          Object o = ObservationManager.getDetail(event, DETAIL_CONTENT);
          // current item is a file
          if (o instanceof File) {
            File fCurrent = (File) o;

            // display associated error code is given
            String sReason = (String) ObservationManager.getDetail(event, DETAIL_REASON);
            if (sReason != null) {
              setMessage(Messages.getString("Error." + sReason) + ": "
                  + fCurrent.getAbsolutePath(), InformationJPanel.ERROR);
            } else {// default message
              setMessage(Messages.getString("Error.007") + ": " + fCurrent.getAbsolutePath(),
                  InformationJPanel.ERROR);
            }
          } else if (o instanceof WebRadio) {
            WebRadio radio = (WebRadio) o;

            // display associated error code is given
            String sReason = (String) ObservationManager.getDetail(event, DETAIL_REASON);
            if (sReason != null) {
              setMessage(Messages.getString("Error." + sReason) + ": " + radio.toString(),
                  InformationJPanel.ERROR);
            } else {// default message
              setMessage(Messages.getString("Error.007") + ": " + radio.toString(),
                  InformationJPanel.ERROR);
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
          if (JajukEvents.HEART_BEAT.equals(subject) && !FIFO.isStopped()
              && !Player.isPaused()) {
            long length = JajukTimer.getInstance().getCurrentTrackTotalTime();
            long lTime = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
            int iPos = (int) (100 * JajukTimer.getInstance().getCurrentTrackPosition());
            String sCurrentTotalMessage = UtilString.formatTimeBySec(timeToPlay);
            setTotalTimeMessage(sCurrentTotalMessage + " [" + FIFO.getFIFO().size()
                + "]");
            setCurrentTimeMessage(lTime, length);
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
          } else if (JajukEvents.ZERO.equals(subject)
              || JajukEvents.PLAYER_STOP.equals(subject)) {
            setCurrentTimeMessage(0, 0);
            jsPosition.setEnabled(false);
            jsPosition.removeMouseWheelListener(InformationJPanel.this);
            jsPosition.removeChangeListener(InformationJPanel.this);
            // use set value, not setPosition that would cause
            // a seek that could fail with some formats
            jsPosition.setValue(0);
            // reset startup position
            Conf.setProperty(CONF_STARTUP_LAST_POSITION, "0");
            setTotalTimeMessage("00:00:00");
            setMessage(Messages.getString("JajukWindow.18"), InformationJPanel.INFORMATIVE);
            jsPosition.addMouseWheelListener(InformationJPanel.this);
            jsPosition.addChangeListener(InformationJPanel.this);
          } else if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
            File file = FIFO.getCurrentFile();
            if (file != null) {
              MessageFormat sMessageFormat = new MessageFormat(Messages.getString("FIFO.10") + " "
                  + Messages.getString("InformationJPanel.8"));
              Object[] stArgs = { file.getTrack().getName(),
                  file.getTrack().getAuthor().getName2(), file.getTrack().getAlbum().getName2() };
              String message = sMessageFormat.format(stArgs);
              setMessage(message, InformationJPanel.INFORMATIVE);
            }
          } else if (JajukEvents.WEBRADIO_LAUNCHED.equals(subject)) {
            if (event.getDetails() == null) {
              return;
            }
            WebRadio radio = (WebRadio) event.getDetails().get(DETAIL_CONTENT);
            if (radio != null) {
              String message = Messages.getString("FIFO.14") + " " + radio.getName();
              setMessage(message, InformationJPanel.INFORMATIVE);
            }
          } else if (JajukEvents.PLAYER_PAUSE.equals(subject)) {
            jsPosition.setEnabled(false);
            jsPosition.removeMouseWheelListener(InformationJPanel.this);
            jsPosition.removeChangeListener(InformationJPanel.this);
          } else if (JajukEvents.PLAYER_RESUME.equals(subject)) {
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
  @Override
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

      @Override
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