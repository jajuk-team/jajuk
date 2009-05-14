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
 * $Revision$
 */

package org.jajuk.ui.widgets;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.File;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * Jajuk main window
 * <p>
 * Singleton
 */
public class JajukWindow extends JFrame implements Observer {

  private static final long serialVersionUID = 1L;

  /** Self instance */
  private static JajukWindow jw;

  /** Show window at startup? */
  private boolean bVisible = true;

  /**
   * Get instance
   * 
   * @return
   */
  public static JajukWindow getInstance() {
    if (jw == null) {
      jw = new JajukWindow();
    }
    return jw;
  }

  /**
   * 
   * @return whether the window is loaded
   */
  public static boolean isLoaded() {
    return (jw != null);
  }

  /**
   * Constructor
   */
  public JajukWindow() {
    // mac integration
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("apple.laf.useScreenMenuBar", "true");
    System.setProperty("apple.awt.showGrowBox", "false");

    jw = this;
    bVisible = (Conf.getInt(Const.CONF_STARTUP_DISPLAY) == Const.DISPLAY_MODE_WINDOW_TRAY);
    setTitle(Messages.getString("JajukWindow.17"));
    setIconImage(IconLoader.getIcon(JajukIcons.LOGO).getImage());
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    // register for given events
    ObservationManager.register(this);
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowDeiconified(WindowEvent arg0) {
        bVisible = true;
        toFront();
      }

      @Override
      public void windowIconified(WindowEvent arg0) {
        bVisible = false;
      }

      @Override
      public void windowClosing(WindowEvent we) {
        // Save windows position
        saveSize();
        try {
          ActionManager.getAction(JajukActions.EXIT).perform(null);
        } catch (Exception e1) {
          Log.error(e1);
        }
      }
    });

    // display correct title if a track is launched at startup
    update(new JajukEvent(JajukEvents.FILE_LAUNCHED, ObservationManager
        .getDetailsLastOccurence(JajukEvents.FILE_LAUNCHED)));
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    return eventSubjectSet;
  }

  /**
   * Save current window size and position
   * 
   */
  public void saveSize() {
    String sValue = null;
    // If user maximized the frame, store this information and not screen
    // bounds
    // (fix for windows issue: at next startup, the screen is shifted by few
    // pixels)
    if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)
        && (getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
      Log.debug("Frame maximized");
      sValue = Const.FRAME_MAXIMIZED;
    } else {
      sValue = (int) getLocationOnScreen().getX() + "," + (int) getLocationOnScreen().getY() + ","
          + getBounds().width + "," + getBounds().height;
      Log.debug("Frame moved or resized, new bounds=" + sValue);
    }
    // Store the new position
    Conf.setProperty(Const.CONF_WINDOW_POSITION, sValue);
  }

  /**
   * Apply size and position stored as property
   * 
   */
  public void applyStoredSize() {
    // Note that defaults sizes (for very first startup) are set in
    // Conf.setDefaultProperties() method ,see
    // CONF_WINDOW_POSITION
    int iScreenWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
    int iScreenHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
    int iX = 0;
    int iY = 0;
    int iHorizSize = 0;
    int iVertSize = 0;
    // Forced frame position ?
    String sForcedValue = Conf.getString(Const.CONF_FRAME_POS_FORCED);
    if (sForcedValue != null && !sForcedValue.trim().equals("")) {
      try {
        StringTokenizer st = new StringTokenizer(sForcedValue, ",");
        iX = Integer.parseInt(st.nextToken());
        iY = Integer.parseInt(st.nextToken());
        iHorizSize = Integer.parseInt(st.nextToken());
        iVertSize = Integer.parseInt(st.nextToken());
        setBounds(iX, iY, iHorizSize, iVertSize);
      } catch (Exception e) {
        // Wrong forced value
        Log.error(e);
        setBounds(Const.FRAME_INITIAL_BORDER, Const.FRAME_INITIAL_BORDER, iScreenWidth - 2
            * Const.FRAME_INITIAL_BORDER, iScreenHeight - 2 * Const.FRAME_INITIAL_BORDER);
      }
      return;
    }
    // Detect strange or buggy Window Manager like XGL using this test
    // and apply default size for them
    if (!Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
      setBounds(Const.FRAME_INITIAL_BORDER, Const.FRAME_INITIAL_BORDER, iScreenWidth - 2
          * Const.FRAME_INITIAL_BORDER, iScreenHeight - 2 * Const.FRAME_INITIAL_BORDER);
      return;
    }
    // read stored position and size
    String sPosition = Conf.getString(Const.CONF_WINDOW_POSITION);
    // If user left jajuk maximized, reset this simple configuration
    if (sPosition.equals(Const.FRAME_MAXIMIZED)) {
      // Always set a size that is used when un-maximalizing the frame
      setBounds(Const.FRAME_INITIAL_BORDER, Const.FRAME_INITIAL_BORDER, iScreenWidth - 2
          * Const.FRAME_INITIAL_BORDER, iScreenHeight - 2 * Const.FRAME_INITIAL_BORDER);
      if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
        setExtendedState(Frame.MAXIMIZED_BOTH);
      }
      return;
    }
    StringTokenizer st = new StringTokenizer(sPosition, ",");
    iX = Integer.parseInt(st.nextToken());
    // if X position is higher than screen width, set default
    if (iX < 0 || iX > iScreenWidth) {
      iX = Const.FRAME_INITIAL_BORDER;
    }
    iY = Integer.parseInt(st.nextToken());
    // if Y position is higher than screen height, set default
    if (iY < 0 || iY > iScreenHeight) {
      iY = Const.FRAME_INITIAL_BORDER;
    }
    iHorizSize = Integer.parseInt(st.nextToken());
    // if zero horiz size or
    // if height > to screen height (switching from a dual to a single head
    // for ie),
    // set max size available (minus some space to deal with task bars)
    if (iHorizSize <= 0 || iHorizSize > iScreenWidth) {
      iHorizSize = iScreenWidth - 2 * Const.FRAME_INITIAL_BORDER;
    }
    // Same for width
    iVertSize = Integer.parseInt(st.nextToken());
    if (iVertSize <= 0 || iVertSize > iScreenHeight) {
      iVertSize = iScreenHeight - 2 * Const.FRAME_INITIAL_BORDER;
    }
    setLocation(iX, iY);
    setSize(iHorizSize, iVertSize);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public final void update(JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
          File file = QueueModel.getPlayingFile();
          if (file != null) {
            setTitle(UtilString.buildTitle(file));
          }
        } else if (subject.equals(JajukEvents.ZERO) || subject.equals(JajukEvents.PLAYER_STOP)) {
          setTitle(Messages.getString("JajukWindow.17"));
        } else if (subject.equals(JajukEvents.WEBRADIO_LAUNCHED)) {
          WebRadio radio = QueueModel.getCurrentRadio();
          if (radio != null) {
            // We use vertical bar to allow scripting like MSN plugins to
            // detect jajuk frames and extract current track
            setTitle("\\ " + radio.getName() + " /");
          }
        }
      }
    });
  }

  /**
   * @return Returns the bVisible.
   */
  public boolean isWindowVisible() {
    return bVisible;
  }

  /**
   * @param visible
   *          The bVisible to set.
   */
  public void display(final boolean visible) {
    if (!visible && !bVisible) {
      return;
    }

    // start ui if needed
    if (visible && !Main.isUILaunched()) {
      try {
        Main.launchWindow();
      } catch (Exception e) {
        Log.error(e);
      }
    }
    // Show or hide the frame
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        // store state
        bVisible = visible;
        // show
        if (visible) {
          applyStoredSize();
          // hide and show again is a workaround for a toFront() issue
          // under Metacity, see
          // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472274
          setVisible(false);
          setVisible(true);
          toFront();
          setState(Frame.NORMAL);
          // Need focus for keystrokes
          requestFocus();
        }
        // hide
        else {
          // hide the window only if it is explicitely required
          saveSize();
          setVisible(false);
        }
      }
    });
  }
}
