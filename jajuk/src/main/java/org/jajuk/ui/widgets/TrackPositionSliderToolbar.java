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
package org.jajuk.ui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * DOCUMENT_ME.
 */
public class TrackPositionSliderToolbar extends JPanel implements ChangeListener,
    MouseWheelListener, Observer {

  /** Generic playing position toolbar, used in information panel a full screen. */
  private static final long serialVersionUID = 1L;

  /** Last slider manual move date. */
  private long lDateLastAdjust;

  /** DOCUMENT_ME. */
  private JSlider jsPosition;

  /** DOCUMENT_ME. */
  private JLabel jlCurrent;

  /** DOCUMENT_ME. */
  String sCurrentStatus;

  /** Swing Timer to refresh the component. */
  private final Timer timer = new Timer(JajukTimer.D_MS_HEARTBEAT, new ActionListener() {

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        update(new JajukEvent(JajukEvents.HEART_BEAT));
      } catch (Exception ex) {
        Log.error(ex);
      }
    }
  });

  /**
   * Instantiates a new track position slider toolbar.
   */
  public TrackPositionSliderToolbar() {
    super();

    initGui();

    // check if some errors occurred before the view has been displayed
    if (ObservationManager.containsEvent(JajukEvents.PLAY_ERROR)) {
      update(new JajukEvent(JajukEvents.PLAY_ERROR,
          ObservationManager.getDetailsLastOccurence(JajukEvents.PLAY_ERROR)));
    }

    // check if some track has been launched before the view has been
    // displayed
    UtilFeatures.updateStatus(this);

    // register for given events
    ObservationManager.register(this);

    timer.start();
  }

  /**
   * Inits the gui. DOCUMENT_ME
   */
  private void initGui() {
    setLayout(new MigLayout("ins 0 5 0 5", "[70%,fill][30%,grow]"));
    setToolTipText(Messages.getString("InformationJPanel.7"));
    jsPosition = new JSlider(0, 100, 0);
    jsPosition.addChangeListener(this);
    jsPosition.setOpaque(false);
    jsPosition.addMouseWheelListener(this);
    jsPosition.setEnabled(false);
    jlCurrent = new JLabel();
    jlCurrent.setToolTipText(Messages.getString("CommandJPanel.15"));
    jlCurrent.addMouseListener(new TimeDisplaySwitchMouseAdapter());
<<<<<<< HEAD
    add(jsPosition, "grow");
    add(jlCurrent, "grow,left");
=======
    add(jsPosition,"grow");
    add(jlCurrent,"grow,left");
>>>>>>> hotfix/1.9.5
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent
   * )
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == jsPosition && !jsPosition.getValueIsAdjusting()) {
      lDateLastAdjust = System.currentTimeMillis();
      setPosition((float) jsPosition.getValue() / 100);
    }
  }

  /**
   * Call a seek.
   * 
   * @param fPosition DOCUMENT_ME
   */
  private void setPosition(final float fPosition) {
    new Thread("TrackSlider Position Thread") {

      @Override
      public void run() {
        Player.seek(fPosition);
      }
    }.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @seejava.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
   * MouseWheelEvent)
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource() == jsPosition) {
      int iOld = jsPosition.getValue();
      int iNew = iOld - (e.getUnitsToScroll() * 3);
      jsPosition.setValue(iNew);
    }
  }

  /**
   * Gets the current position.
   * 
   * @return Position value
   */
  public int getCurrentPosition() {
    return this.jsPosition.getValue();
  }

  /**
   * Set the current status for current track ex : 01:01:01/02:02:02.
   * 
   * @param lTime DOCUMENT_ME
   * @param length DOCUMENT_ME
   */
  public final void setCurrentTimeMessage(long lTime, long length) {
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
    /*
     * same as default... case 0: { string = UtilString.formatTimeBySec(lTime) +
     * " / " + UtilString.formatTimeBySec(length); break; }
     */
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

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.PLAY_ERROR);
    eventSubjectSet.add(JajukEvents.PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#update(org.jajuk.events.JajukEvent)
   */
  @Override
  public final void update(final JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    // do not insert this subject inside the invokeLater because we have to
    // leave the awt dispatcher called inside the setMessage and THEN, sleep
    // for 2 secs.
    if (JajukEvents.PLAY_ERROR.equals(subject)) {
      try {
        // we synchronize this code to make sure error message is
        // visible all 2 secs
        synchronized (this) {
          // reset data
          setCurrentTimeMessage(0, 0);
        }
      } catch (Exception e) {
        Log.error(e);
      }
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (JajukEvents.HEART_BEAT.equals(subject) && !QueueModel.isStopped()
              && !Player.isPaused()) {
            long length = JajukTimer.getInstance().getCurrentTrackTotalTime();
            long lTime = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
            int iPos = (int) (100 * Player.getCurrentPosition());
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
            jsPosition.removeChangeListener(TrackPositionSliderToolbar.this);
            jsPosition.setValue(iPos);
            jsPosition.addChangeListener(TrackPositionSliderToolbar.this);
          } else if (JajukEvents.ZERO.equals(subject) || JajukEvents.PLAYER_STOP.equals(subject)) {
            setCurrentTimeMessage(0, 0);
            jsPosition.setEnabled(false);
            jsPosition.removeMouseWheelListener(TrackPositionSliderToolbar.this);
            jsPosition.removeChangeListener(TrackPositionSliderToolbar.this);
            // use set value, not setPosition that would cause
            // a seek that could fail with some formats
            jsPosition.setValue(0);
            // reset startup position
            Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, "0");
            jsPosition.addMouseWheelListener(TrackPositionSliderToolbar.this);
            jsPosition.addChangeListener(TrackPositionSliderToolbar.this);
          } else if (JajukEvents.PLAYER_PAUSE.equals(subject)) {
            jsPosition.setEnabled(false);
            jsPosition.removeMouseWheelListener(TrackPositionSliderToolbar.this);
            jsPosition.removeChangeListener(TrackPositionSliderToolbar.this);
          } else if (JajukEvents.PLAYER_RESUME.equals(subject)) {
            // Avoid adding listeners twice
            if (jsPosition.getMouseWheelListeners().length == 0) {
              jsPosition.addMouseWheelListener(TrackPositionSliderToolbar.this);
            }
            if (jsPosition.getChangeListeners().length == 0) {
              jsPosition.addChangeListener(TrackPositionSliderToolbar.this);
            }
            jsPosition.setEnabled(true);
          }
        }
      });
    }
  }

  /**
   * Gets the current status message.
   * 
   * @return the current status message
   */
  public String getCurrentStatusMessage() {
    return sCurrentStatus;
  }

  /**
   * Small MouseAdapter to loop through the different ways of displaying the
   * elapsed time.
   */
  private final class TimeDisplaySwitchMouseAdapter extends MouseAdapter {

    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
      int currentFormat = Conf.getInt(Const.CONF_FORMAT_TIME_ELAPSED);
      Conf.setProperty(Const.CONF_FORMAT_TIME_ELAPSED,
          Integer.toString(((currentFormat + 1) % Const.FORMAT_TIME_ELAPSED_MAX)));
    }
  }
}
