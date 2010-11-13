/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

package org.jajuk.ui.windows;

import com.vlsolutions.swing.docking.ui.DockingUISettings;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.CommandJPanel;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJMenuBar;
import org.jajuk.ui.widgets.PerspectiveBarJPanel;
import org.jajuk.ui.widgets.SearchJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;

/**
 * Jajuk main window
 * <p>
 * Singleton.
 */
public class JajukMainWindow extends JFrame implements IJajukWindow, Observer {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Self instance. */
  private static JajukMainWindow jw;

  /** Left side perspective selection panel. */
  private PerspectiveBarJPanel perspectiveBar;

  /** Main frame panel. */
  private JPanel jpFrame;

  /** specific perspective panel. */
  private JPanel perspectivePanel;

  /** State decorator. */
  private WindowStateDecorator decorator;

  /**
   * Get the window instance and create the specific WindowStateHandler.
   * 
   * @return the instance
   */
  public static JajukMainWindow getInstance() {
    if (jw == null) {
      jw = new JajukMainWindow();

      // Install global keystrokes
      WindowGlobalKeystrokeManager.getInstance();

      jw.decorator = new WindowStateDecorator(jw) {
        @Override
        public void specificBeforeShown() {
          jw.applyStoredSize();
          if (UtilSystem.isUnderLinux()) {
            // hide and show again is a workaround for a toFront() issue
            // under Metacity, see
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472274
            jw.setVisible(false);
          }
        }

        @Override
        public void specificAfterShown() {
          // Apply size and location again
          // (required by Gnome for ie to fix the 0-sized maximized
          // frame)
          jw.applyStoredSize();
          jw.toFront();
          jw.setState(Frame.NORMAL);
          // Need focus for keystrokes
          jw.requestFocus();
          // display right title if a track is launched at startup
          jw.update(new JajukEvent(JajukEvents.FILE_LAUNCHED, ObservationManager
              .getDetailsLastOccurence(JajukEvents.FILE_LAUNCHED)));
        }

        @Override
        public void specificBeforeHidden() {
          // hide the window only if it is explicitely required
          jw.saveSize();
        }

        @Override
        public void specificAfterHidden() {
          // Nothing particular
        }
      };
    }
    return jw;
  }

  /**
   * Constructor.
   */
  private JajukMainWindow() {
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

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.windows.IJajukWindow#initUI()
   */
  @Override
  public void initUI() {
    if (UtilSystem.isUnderOSX()) {
      // mac integration
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      System.setProperty("apple.awt.showGrowBox", "false");
    }

    setTitle(Messages.getString("JajukWindow.17"));
    setIconImage(IconLoader.getIcon(JajukIcons.LOGO).getImage());
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    // register for given events
    ObservationManager.register(this);
    addWindowListener(new WindowAdapter() {

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

      @Override
      public void windowIconified(WindowEvent we) {
        // If user set the minimize to tray option and if the tray is supported, we 
        // minimize to tray only
        if (Conf.getBoolean(Const.CONF_MINIMIZE_TO_TRAY) && SystemTray.isSupported()) {
          getWindowStateDecorator().display(false);
        }
      }

      @Override
      public void windowDeiconified(WindowEvent we) {
        getWindowStateDecorator().display(true);
      }

    });

    // Light drag and drop for VLDocking
    UIManager.put("DragControler.paintBackgroundUnderDragRect", Boolean.FALSE);
    DockingUISettings.getInstance().installUI();

    // Creates the panel
    jpFrame = (JPanel) getContentPane();
    jpFrame.setOpaque(true);
    jpFrame.setLayout(new BorderLayout());

    // create the command bar
    CommandJPanel command = CommandJPanel.getInstance();
    command.initUI();

    // Create the search bar
    SearchJPanel searchPanel = SearchJPanel.getInstance();
    searchPanel.initUI();

    // Add the search bar
    jpFrame.add(searchPanel, BorderLayout.NORTH);

    // Create and add the information bar panel
    InformationJPanel information = InformationJPanel.getInstance();

    // Add the information panel
    jpFrame.add(information, BorderLayout.SOUTH);

    // Create the perspective manager
    try {
      PerspectiveManager.load();
    } catch (JajukException e) {
      // problem loading the perspective, let Main to handle this
      Log.debug("Cannot create main window");
      throw new RuntimeException(e);
    }
    perspectivePanel = new JXPanel();
    // Make this panel extensible
    perspectivePanel.setLayout(new BoxLayout(perspectivePanel, BoxLayout.X_AXIS));

    // Set menu bar to the frame
    JajukMainWindow.getInstance().setJMenuBar(JajukJMenuBar.getInstance());

    // Create the perspective tool bar panel
    perspectiveBar = PerspectiveBarJPanel.getInstance();
    jpFrame.add(perspectiveBar, BorderLayout.WEST);

    // Initialize and add the desktop
    PerspectiveManager.init();

    // Add main container (contains toolbars + desktop)
    JPanel commandDesktop = new JPanel(new MigLayout("insets 0,gapy 0", "[grow]", "[grow][]"));
    commandDesktop.add(perspectivePanel, "grow,wrap");
    commandDesktop.add(command, "grow");
    jpFrame.add(commandDesktop, BorderLayout.CENTER);
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
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    return eventSubjectSet;
  }

  /**
   * Save current window size and position.
   */
  public void saveSize() {

    boolean maxmimized = false;

    if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)
        && (getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
      Log.debug("Frame maximized");
      maxmimized = true;
    }
    Conf.setProperty(Const.CONF_WINDOW_MAXIMIZED, Boolean.toString(maxmimized));

    String sValue = (int) getLocationOnScreen().getX() + "," + (int) getLocationOnScreen().getY()
        + "," + getBounds().width + "," + getBounds().height;
    Log.debug("Frame moved or resized, new bounds=" + sValue);

    // Store the new position
    Conf.setProperty(Const.CONF_WINDOW_POSITION, sValue);
  }

  /**
   * Apply size and position stored as property.
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

    // first get the stored position to get the correct display
    String sPosition = Conf.getString(Const.CONF_WINDOW_POSITION);

    // workaround: needed for old configuration files to avoid an exception in
    // the
    // StringTokenizer, since Jajuk 1.9 Jajuk stores in an extra property if it
    // is maximized
    if (sPosition.equals(Const.FRAME_MAXIMIZED)) {
      // Always set a size that is used when un-maximazing the frame
      setBounds(Const.FRAME_INITIAL_BORDER, Const.FRAME_INITIAL_BORDER, iScreenWidth - 2
          * Const.FRAME_INITIAL_BORDER, iScreenHeight - 2 * Const.FRAME_INITIAL_BORDER);
      if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
        setExtendedState(Frame.MAXIMIZED_BOTH);
      }
      return;
    }
    // workaround: end
    // could be removed in future releases, also Const.FRAME_MAXIMIZED

    StringTokenizer st = new StringTokenizer(sPosition, ",");
    iX = Integer.parseInt(st.nextToken());
    iY = Integer.parseInt(st.nextToken());
    iHorizSize = Integer.parseInt(st.nextToken());
    iVertSize = Integer.parseInt(st.nextToken());
    // second set the stored position/size
    setLocation(iX, iY);
    setSize(iHorizSize, iVertSize);

    // get the display conf where the main frame is displayed, if the position
    // is outside, the default screen is returned
    GraphicsConfiguration gConf = UtilGUI.getGraphicsDeviceOfMainFrame().getDefaultConfiguration();
    int iScreenXzero = (int) gConf.getBounds().getX();
    int iScreenYzero = (int) gConf.getBounds().getY();
    iScreenWidth = (int) gConf.getBounds().getWidth();
    iScreenHeight = (int) gConf.getBounds().getHeight();

    // check if position/size is correct

    // if X position is higher than screen width, set default
    if (iX < iScreenXzero || iX > iScreenXzero + iScreenWidth) {
      iX = Const.FRAME_INITIAL_BORDER;
    }

    // if Y position is higher than screen height, set default
    if (iY < iScreenYzero || iY > iScreenYzero + iScreenHeight) {
      iY = Const.FRAME_INITIAL_BORDER;
    }

    // if zero horiz size or
    // if height > to screen height (switching from a dual to a single head
    // for ie),
    // set max size available (minus some space to deal with task bars)
    if (iHorizSize <= 0 || iHorizSize > iScreenWidth) {
      iHorizSize = iScreenWidth - 2 * Const.FRAME_INITIAL_BORDER;
    }
    // Same for width
    if (iVertSize <= 0 || iVertSize > iScreenHeight) {
      iVertSize = iScreenHeight - 2 * Const.FRAME_INITIAL_BORDER;
    }

    setLocation(iX, iY);
    setSize(iHorizSize, iVertSize);

    // was the frame maximized
    if (Conf.getBoolean(Const.CONF_WINDOW_MAXIMIZED)) {
      if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
        // are we on the primary display
        if (gConf.getBounds().equals(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration().getBounds())) {
          // default size, if frame is unmaximized
          setSize(iScreenWidth - 2 * Const.FRAME_INITIAL_BORDER, iScreenHeight - 2
              * Const.FRAME_INITIAL_BORDER);
          if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH))
            setExtendedState(Frame.MAXIMIZED_BOTH);
        } else {
          // setExtendedState not be used on the other displays, because Java
          // takes always the solution of the primary display...
          setBounds(gConf.getBounds());
        }
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public final void update(JajukEvent event) {
    final JajukEvents subject = event.getSubject();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if (subject.equals(JajukEvents.FILE_LAUNCHED)) {
          String title = QueueModel.getPlayingFileTitle();
          if (title != null) {
            setTitle(title);
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
   * Gets the perspective panel.
   * 
   * @return the perspective panel
   */
  public JPanel getPerspectivePanel() {
    return perspectivePanel;
  }

}
