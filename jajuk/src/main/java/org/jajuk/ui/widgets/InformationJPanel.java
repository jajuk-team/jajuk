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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Album;
import org.jajuk.base.File;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;

/**
 * Status / information panel ( static view ).
 */
public final class InformationJPanel extends JXPanel implements Observer {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * DOCUMENT_ME.
   */
  public static enum MessageType {
    
    /** DOCUMENT_ME. */
    INFORMATIVE, 
 /** DOCUMENT_ME. */
 ERROR, 
 /** DOCUMENT_ME. */
 WARNING
  }

  /** Self instance. */
  private static InformationJPanel ijp = new InformationJPanel();

  /** Swing Timer to refresh the component. */
  private final Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {

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
   * Singleton access.
   * 
   * @return the instance
   */
  public static InformationJPanel getInstance() {
    return ijp;
  }

  // widgets declaration

  /** DOCUMENT_ME. */
  public JLabel jlMessage;

  /** DOCUMENT_ME. */
  JLabel jlSelection;

  /** DOCUMENT_ME. */
  JLabel jlTotal;

  // attributes

  /** DOCUMENT_ME. */
  String sMessage;

  /** Current message type. */
  MessageType type = MessageType.INFORMATIVE;

  /** DOCUMENT_ME. */
  String sSelection;

  /** DOCUMENT_ME. */
  String sTotalStatus;

  /** DOCUMENT_ME. */
  private final TrackPositionSliderToolbar trackPositionSliderToolbar;

  /**
   * Instantiates a new information j panel.
   */
  private InformationJPanel() {
    super();
    // message bar
    JToolBar jtbMessage = new JajukJToolbar();
    // Set a zero minimum size to allow user to reduce window width
    jtbMessage.setMinimumSize(new Dimension(0, 0));
    // We use toolbar to display vertical separator lines
    jlMessage = new JLabel();
    setMessage(Messages.getString("JajukWindow.18"), MessageType.INFORMATIVE);
    jtbMessage.add(jlMessage);
    jtbMessage.add(Box.createHorizontalGlue());
    jtbMessage.addSeparator();

    trackPositionSliderToolbar = new TrackPositionSliderToolbar();

    // total progress bar
    JToolBar jtbTotal = new JajukJToolbar();

    jtbTotal.addSeparator();
    jlTotal = new JLabel();
    jlTotal.setToolTipText(Messages.getString("InformationJPanel.5"));
    jtbTotal.add(Box.createHorizontalGlue());
    jtbTotal.add(jlTotal);
    jtbTotal.add(Box.createHorizontalGlue());
    jtbTotal.addSeparator();

    // selection bar
    JToolBar jtbSelection = new JajukJToolbar();
    jlSelection = new JLabel(Messages.getString("InformationJPanel.9"));
    jtbSelection.add(jlSelection);
    jtbSelection.add(Box.createHorizontalGlue());

    // add widgets
    setLayout(new MigLayout("insets 2", "[40%,grow][40%,grow][10%,grow][10%,grow]"));
    add(jtbMessage, "grow,left");
    add(trackPositionSliderToolbar, "grow");
    add(jtbTotal, "grow");
    add(jtbSelection, "grow,right");

    // check if some errors occurred before the view has been displayed
    if (ObservationManager.containsEvent(JajukEvents.PLAY_ERROR)) {
      update(new JajukEvent(JajukEvents.PLAY_ERROR, ObservationManager
          .getDetailsLastOccurence(JajukEvents.PLAY_ERROR)));
    }

    // check if some track has been launched before the view has been
    // displayed
    UtilFeatures.updateStatus(this);

    // register for given events
    ObservationManager.register(this);
    // start timer
    timer.start();
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
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAY_ERROR);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.THUMB_CREATED);
    eventSubjectSet.add(JajukEvents.FILE_COPIED);
    eventSubjectSet.add(JajukEvents.FILE_CONVERSION);
    return eventSubjectSet;
  }

  /**
   * Gets the message.
   * 
   * @return the message
   */
  public String getMessage() {
    return sMessage;
  }

  /**
   * Gets the selection.
   * 
   * @return the selection
   */
  public String getSelection() {
    return this.sSelection;
  }

  /**
   * Sets the message.
   * 
   * @param sMessage DOCUMENT_ME
   * @param messageType DOCUMENT_ME
   */
  public void setMessage(final String sMessage, final MessageType messageType) {
    this.sMessage = sMessage;
    this.type = messageType;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        InformationJPanel.this.sMessage = sMessage;
        jlMessage.setText(sMessage);
        jlMessage.setToolTipText(sMessage);
        //Use default look and feel color for informative
        if (messageType == MessageType.ERROR) {
          jlMessage.setForeground(Color.RED);
        } else if (messageType == MessageType.WARNING) {
          jlMessage.setForeground(Color.ORANGE);
        } else if (messageType == MessageType.INFORMATIVE) {
          jlMessage.setForeground(UtilGUI.getForegroundColor());
        }
      }
    });
  }

  /**
   * Sets the selection.
   * 
   * @param sSelection DOCUMENT_ME
   */
  public void setSelection(String sSelection) {
    this.sSelection = sSelection;
    jlSelection.setText(sSelection);
    jlSelection.setToolTipText(sSelection);
  }

  /**
   * Gets the total time message.
   * 
   * @return the total time message
   */
  public String getTotalTimeMessage() {
    return sTotalStatus;
  }

  /**
   * Sets the total time message.
   * 
   * @param string DOCUMENT_ME
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
  @Override
  public void update(final JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    // do not insert this subject inside the invokeLater because we have to
    // leave the awt dispatcher called inside the setMessage and THEN, sleep
    // for 2 secs.
    if (JajukEvents.PLAY_ERROR.equals(subject)) {
      try {
        // we synchronize this code to make sure error message is
        // visible all 2 secs
        synchronized (this) {
          // set error message
          Object o = ObservationManager.getDetail(event, Const.DETAIL_CONTENT);
          // current item is a file
          // display associated error code is given
          Object detail = ObservationManager.getDetail(event, Const.DETAIL_REASON);
          int errorCode = 0;
          if (detail != null) {
            errorCode = Integer.parseInt((String) detail);
          }
          // Already playing a track
          if (o instanceof File) {
            File fCurrent = (File) o;
            if (detail != null) {
              setMessage(Messages.getErrorMessage(errorCode) + ": " + fCurrent.getAbsolutePath(),
                  InformationJPanel.MessageType.ERROR);
            } else {// default message
              setMessage(Messages.getString("Error.007") + fCurrent.getName(),
                  InformationJPanel.MessageType.ERROR);
            }
          } else if (o instanceof WebRadio) {
            WebRadio radio = (WebRadio) o;

            // display associated error code is given
            if (detail != null) {
              setMessage(Messages.getErrorMessage(errorCode) + ": " + radio.toString(),
                  InformationJPanel.MessageType.ERROR);
            } else {// default message
              setMessage(Messages.getString("Error.007") + radio.toString(),
                  InformationJPanel.MessageType.ERROR);
            }
          }
        }
      } catch (Exception e) {
        Log.error(e);
      }
    } else if (JajukEvents.THUMB_CREATED.equals(subject)) {
      Album album = (Album) event.getDetails().get(Const.DETAIL_CONTENT);
      setMessage(Messages.getString("CatalogView.5") + " " + album.getName2(),
          InformationJPanel.MessageType.INFORMATIVE);
    } else {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (JajukEvents.HEART_BEAT.equals(subject) && !QueueModel.isStopped()
              && !Player.isPaused() && !QueueModel.isPlayingRadio()) {
            final long timeToPlay;
            if (QueueModel.containsRepeat()) {
              // if repeat mode, total
              // time has no sense
              timeToPlay = -1;
            } else {
              timeToPlay = JajukTimer.getInstance().getTotalTimeToPlay();
            }
            String sCurrentTotalMessage = UtilString.formatTimeBySec(timeToPlay);
            setTotalTimeMessage(sCurrentTotalMessage + " [" + QueueModel.getCountTracksLeft() + "]");
          } else if (JajukEvents.ZERO.equals(subject) || JajukEvents.PLAYER_STOP.equals(subject)) {
            // reset startup position
            Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, "0");
            setTotalTimeMessage("00:00:00");
            setMessage(Messages.getString("JajukWindow.18"),
                InformationJPanel.MessageType.INFORMATIVE);
          } else if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
            File file = QueueModel.getPlayingFile();
            if (file != null) {
              String message = "";
              String pattern = Conf.getString(Const.CONF_PATTERN_INFORMATION);
              try {
                message = UtilString.applyPattern(file, pattern, false, false);
              } catch (JajukException e) {
                Log.error(e);
              }
              setMessage(message, InformationJPanel.MessageType.INFORMATIVE);
            }
          } else if (JajukEvents.WEBRADIO_LAUNCHED.equals(subject)) {
            setTotalTimeMessage("00:00:00");
            if (event.getDetails() == null) {
              return;
            }
            WebRadio radio = (WebRadio) event.getDetails().get(Const.DETAIL_CONTENT);
            if (radio != null) {
              String message = Messages.getString("FIFO.14") + " " + radio.getName();
              setMessage(message, InformationJPanel.MessageType.INFORMATIVE);
            }
          } else if (JajukEvents.FILE_COPIED.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) {
              // if no property, the party is done
              setMessage("", InformationJPanel.MessageType.INFORMATIVE);
            } else {
              String filename = properties.getProperty(Const.DETAIL_CONTENT);
              if (filename != null) {
                setMessage(Messages.getString("Device.45") + filename + "]",
                    InformationJPanel.MessageType.INFORMATIVE);
              }
            }
          } else if (JajukEvents.FILE_CONVERSION.equals(subject)) {
            Properties properties = event.getDetails();
            if (properties == null) {
              // if no property, the party is done
              setMessage("", InformationJPanel.MessageType.INFORMATIVE);
            } else {
              String filename = properties.getProperty(Const.DETAIL_CONTENT);
              String target = properties.getProperty(Const.DETAIL_NEW);
              if (filename != null) {
                setMessage(Messages.getString("Device.46") + filename
                    + Messages.getString("Device.47") + target + "]",
                    InformationJPanel.MessageType.INFORMATIVE);
              }
            }
          }
        }
      });
    }
  }

  /**
   * toString() method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return getClass().getName();
  }

  /**
   * Gets the message type.
   * 
   * @return the message type
   */
  public MessageType getMessageType() {
    return type;
  }

}