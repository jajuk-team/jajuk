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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
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
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;

/**
 * Status / information panel ( static view )
 */
public final class InformationJPanel extends JXPanel implements Observer {

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

  /** Swing Timer to refresh the component */
  private final Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {

    public void actionPerformed(ActionEvent e) {
      try {
        update(new JajukEvent(JajukEvents.HEART_BEAT));
      } catch (Exception ex) {
        Log.error(ex);
      }
    }
  });

  /**
   * Singleton access
   * 
   * @return
   */
  public static InformationJPanel getInstance() {
    if (ijp == null) {
      ijp = new InformationJPanel();
    }
    return ijp;
  }

  // widgets declaration

  public JLabel jlMessage;

  JLabel jlSelection;

  JLabel jlTotal;

  // attributes

  String sMessage;

  /** Current message type */
  int iType = 0;

  String sSelection;

  String sTotalStatus;

  private final TrackPositionSliderToolbar trackPositionSliderToolbar;

  private InformationJPanel() {
    super();
    // message bar
    JToolBar jtbMessage = new JajukJToolbar();
    // Set a zero minimum size to allow user to reduce window width
    jtbMessage.setMinimumSize(new Dimension(0, 0));
    // We use toolbar to display vertical separator lines
    jlMessage = new JLabel();
    jlMessage.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    setMessage(Messages.getString("JajukWindow.18"), InformationJPanel.INFORMATIVE);
    jtbMessage.add(jlMessage);
    jtbMessage.add(Box.createHorizontalGlue());
    jtbMessage.addSeparator();

    trackPositionSliderToolbar = new TrackPositionSliderToolbar();

    // total progress bar
    JToolBar jtbTotal = new JajukJToolbar();
    jlTotal = new JLabel();
    jlTotal.setToolTipText(Messages.getString("InformationJPanel.5"));
    jtbTotal.add(jlTotal);
    jtbTotal.add(Box.createHorizontalGlue());
    jtbTotal.addSeparator();

    // selection bar
    JToolBar jtbSelection = new JajukJToolbar();
    jlSelection = new JLabel();
    jtbSelection.add(jlSelection);
    jtbSelection.add(Box.createHorizontalGlue());

    // add widgets
    setLayout(new MigLayout("insets 2", "[40%,grow][40%,grow][10%,grow][10%,grow]"));
    add(jtbMessage, "grow");
    add(trackPositionSliderToolbar, "grow");
    add(jtbTotal, "grow");
    add(jtbSelection, "grow");

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

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAY_ERROR);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.THUMB_CREATED);
    return eventSubjectSet;
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
  public String getTotalTimeMessage() {
    return sTotalStatus;
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
                  InformationJPanel.ERROR);
            } else {// default message
              setMessage(Messages.getString("Error.007") + fCurrent.getName(),
                  InformationJPanel.ERROR);
            }
          } else if (o instanceof WebRadio) {
            WebRadio radio = (WebRadio) o;

            // display associated error code is given
            if (detail != null) {
              setMessage(Messages.getErrorMessage(errorCode) + ": " + radio.toString(),
                  InformationJPanel.ERROR);
            } else {// default message
              setMessage(Messages.getString("Error.007") + radio.toString(),
                  InformationJPanel.ERROR);
            }
          }
        }
      } catch (Exception e) {
        Log.error(e);
      }
    } else if (JajukEvents.THUMB_CREATED.equals(subject)) {
      Album album = (Album) event.getDetails().get(Const.DETAIL_CONTENT);
      setMessage(Messages.getString("CatalogView.5") + " " + album.getName2(),
          InformationJPanel.INFORMATIVE);
    } else {
      final long timeToPlay = JajukTimer.getInstance().getTotalTimeToPlay();
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          if (JajukEvents.HEART_BEAT.equals(subject) && !QueueModel.isStopped()
              && !Player.isPaused()) {
            String sCurrentTotalMessage = UtilString.formatTimeBySec(timeToPlay);
            setTotalTimeMessage(sCurrentTotalMessage + " [" + QueueModel.getCountTracksLeft() + "]");
          } else if (JajukEvents.ZERO.equals(subject) || JajukEvents.PLAYER_STOP.equals(subject)) {
            // reset startup position
            Conf.setProperty(Const.CONF_STARTUP_LAST_POSITION, "0");
            setTotalTimeMessage("00:00:00");
            setMessage(Messages.getString("JajukWindow.18"), InformationJPanel.INFORMATIVE);
          } else if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
            File file = QueueModel.getPlayingFile();
            if (file != null) {
              StringBuffer sb = new StringBuffer();
              sb.append(Messages.getString("FIFO.10")).append("    ").append(
                  QueueModel.getIndex() + 1).append(" - ").append(
                  file.getTrack().getAuthor().getName2()).append(" - ").append(
                  file.getTrack().getName()).append("  /  ").append(file.getTrack().getYear())
                  .append(" - ").append(file.getTrack().getAlbum().getName2());

              setMessage(sb.toString(), InformationJPanel.INFORMATIVE);
            }
          } else if (JajukEvents.WEBRADIO_LAUNCHED.equals(subject)) {
            if (event.getDetails() == null) {
              return;
            }
            WebRadio radio = (WebRadio) event.getDetails().get(Const.DETAIL_CONTENT);
            if (radio != null) {
              String message = Messages.getString("FIFO.14") + " " + radio.getName();
              setMessage(message, InformationJPanel.INFORMATIVE);
            }
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

}