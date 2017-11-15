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

import com.vlsolutions.swing.docking.ui.DockingUISettings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

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
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;

import net.miginfocom.swing.MigLayout;

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
  /** Number of pixels around window at initial startup. */
  private static final int FRAME_INITIAL_BORDER = 60;
  /** Window minimal width in pixels, set a bit less than 1024px 
   * (lowest resolution of compatible screens) to avoid a side effect 
   * due to negative coordinates which leads to display the frame on 
   * the other screen if larger */
  private static final int FRAME_MIN_WIDTH_PX = 1000;
  /** Window minimal height in pixels*/
  private static final int FRAME_MIN_HEIGHT_PX = 600;

  /**
   * Get the window instance and create the specific WindowStateHandler.
   * 
   * @return the instance
   */
  public static synchronized JajukMainWindow getInstance() {
    if (jw == null) {
      jw = new JajukMainWindow();
      // Install global keystrokes
      WindowGlobalKeystrokeManager.getInstance();
      jw.decorator = new WindowStateDecorator(jw) {
        @Override
        public void specificBeforeShown() {
          //Nothing here, frame bounds is set after display (see next method)
        }

        @Override
        public void specificAfterShown() {
          // We have to force the new frame state, otherwise the window is deiconified but never gets focus
          jw.setExtendedState(Frame.NORMAL);
          //We have to call this next in the EDT to make sure that the window is displayed so maximalize() method get 
          //proper screen for jw window.
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              if (Conf.getBoolean(Const.CONF_WINDOW_MAXIMIZED)) {
                jw.maximalize();
              } else {
            	  // We set bounds after display, otherwise, the window is blank under Gnome3
                jw.applyStoredSize();
              }
            }
          });
          // Need focus for keystrokes
          jw.requestFocus();
          // Make sure to display right title if a track or a webradio is launched at startup
          // Indeed, the window can appear after the track/webradio has been launched and miss this event 
          UtilFeatures.updateStatus(jw);
        }

        @Override
        public void specificBeforeHidden() {
          // This is required to store last position of frame before hide
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
    eventSubjectSet.add(JajukEvents.WEBRADIO_INFO_UPDATED);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    return eventSubjectSet;
  }

  /**
   * Save current window size and position.
   */
  public void saveSize() {
    boolean maximized = false;
    if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)
        && (getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
      maximized = true;
    }
    Conf.setProperty(Const.CONF_WINDOW_MAXIMIZED, Boolean.toString(maximized));
    String sValue = (int) getLocationOnScreen().getX() + "," + (int) getLocationOnScreen().getY()
        + "," + getBounds().width + "," + getBounds().height;
    Log.debug("Frame position position stored as :" + sValue + " maximalized=" + maximized);
    // Store the new position
    Conf.setProperty(Const.CONF_WINDOW_POSITION, sValue);
  }

  /**
   * Return the forced position as a rectangle or null if no forced position is provided or if the provided position is invalid
   * <br>See http://jajuk.info/index.php/Hidden_options
   * <br>The forced position is an hidden option used to force Jajuk window position manually.
   * @return the forced position as a rectangle or null
   */
  private Rectangle getForcedPosition() {
    try {
      String forcedPosition = Conf.getString(Const.CONF_FRAME_POS_FORCED);
      int x = 0;
      int y = 0;
      int horizSize = 0;
      int vertSize = 0;
      if (UtilString.isNotEmpty(forcedPosition)) {
        StringTokenizer st = new StringTokenizer(forcedPosition, ",");
        x = Integer.parseInt(st.nextToken());
        y = Integer.parseInt(st.nextToken());
        horizSize = Integer.parseInt(st.nextToken());
        vertSize = Integer.parseInt(st.nextToken());
        return new Rectangle(x, y, horizSize, vertSize);
      }
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Return the stored position as a rectangle or default coordinates if no stored position is provided or if the stored position is invalid.
   * @return the stored position as a rectangle or null
   */
  Rectangle getStoredPosition() {
    try {
      String storedPosition = Conf.getString(Const.CONF_WINDOW_POSITION);
      int x = 0;
      int y = 0;
      int horizSize = 0;
      int vertSize = 0;
      if (UtilString.isNotEmpty(storedPosition)) {
        StringTokenizer st = new StringTokenizer(storedPosition, ",");
        // We need to floor the position to zero due to issues with dual screens that can produce negative x and y 
        x = Integer.parseInt(st.nextToken());
        x = Math.max(x, 0);
        y = Integer.parseInt(st.nextToken());
        y = Math.max(y, 0);
        horizSize = Integer.parseInt(st.nextToken());
        vertSize = Integer.parseInt(st.nextToken());
        return new Rectangle(x, y, horizSize, vertSize);
      }
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Return whether the window should be maximalized.
   * <br>Maximized state here refers to maximum size of JFrame on a desktop screen however not covering the taskbar.
   * <br>Prior to 1.9, "max" was inside CONF_WINDOW_POSITION, then it is 
   * externalize in a specific boolean property : CONF_WINDOW_MAXIMIZED
   * 
   * @return whether the window should be maximalized.
   */
  private boolean isMaximalizationRequired() {
    // CONF_WINDOW_POSITION contains the last session stored position or "max" if maximalized (jajuk <1.9)
    String sPosition = Conf.getString(Const.CONF_WINDOW_POSITION);
    // workaround: needed for old configuration files to avoid an exception in
    // the StringTokenizer, since Jajuk 1.9 Jajuk stores in an extra property if it
    // is maximized
    if (Const.FRAME_MAXIMIZED.equals(sPosition)) {
      return true;
    }
    return Conf.getBoolean(Const.CONF_WINDOW_MAXIMIZED);
  }

  /**
   * Actually maximalize this frame.
   * Do not call this when hidden before the first screen will always been returned.
   */
  private void maximalize() {
    GraphicsConfiguration gConf = UtilGUI.getGraphicsDeviceOfMainFrame().getDefaultConfiguration();
    setMaximizedBounds(gConf.getBounds());
    setExtendedState(getExtendedState() | Frame.MAXIMIZED_BOTH);
    setBounds(FRAME_INITIAL_BORDER, FRAME_INITIAL_BORDER,
        (int) (gConf.getBounds().getWidth() - 2 * FRAME_INITIAL_BORDER), (int) (gConf.getBounds()
            .getHeight() - 2 * FRAME_INITIAL_BORDER));
  }

  /**
   * Check if provided position is correct
   * @return whether provided position is valid.
   */
  private boolean isPositionValid(Rectangle position) {
    GraphicsConfiguration gConf = UtilGUI.getGraphicsDeviceOfMainFrame().getDefaultConfiguration();
    if (position.getX() < gConf.getBounds().getX()
        || position.getX() > gConf.getBounds().getWidth()) {
      return false;
    }
    if (position.getY() < gConf.getBounds().getY()
        || position.getY() > gConf.getBounds().getHeight()) {
      return false;
    }
    if (position.getWidth() <= 0 || position.getWidth() > gConf.getBounds().getWidth()
        || position.getWidth() < 800) {
      return false;
    }
    if (position.getHeight() <= 0 || position.getHeight() > gConf.getBounds().getHeight()
        || position.getHeight() < 600) {
      return false;
    }
    return true;
  }

  /**
  * Apply size and position stored as property.
  * <br>
  * Note that defaults sizes (for very first startup) are set in
   {@code Conf.setDefaultProperties()} method ,see {@code CONF_WINDOW_POSITION}
  */
  public void applyStoredSize() {
    try {
      setMinimumSize(new Dimension(FRAME_MIN_WIDTH_PX, FRAME_MIN_HEIGHT_PX));
      Rectangle forcedPosition = getForcedPosition();
      if (forcedPosition != null) {
        setBounds(forcedPosition);
      } else {
        if (isMaximalizationRequired()
            && Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
          maximalize();
        } else {
          Rectangle storedPosition = getStoredPosition();
          // Note that setBounds handle out of bounds issues like task bar overriding, 
          // number of screens changes since previous jajuk session...
          setBounds(storedPosition);
        }
      }
    } catch (Exception e) {
      Log.error(e);
      maximalize();
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
        } else if (subject.equals(JajukEvents.WEBRADIO_INFO_UPDATED)) {
          Properties webradioInfoUpdatedEvent = ObservationManager
              .getDetailsLastOccurence(JajukEvents.WEBRADIO_INFO_UPDATED);
          String currentRadioTrack = (String) webradioInfoUpdatedEvent
              .get(Const.CURRENT_RADIO_TRACK);
          if (currentRadioTrack != null) {
            // We use vertical bar to allow scripting like MSN plugins to
            // detect jajuk frames and extract current track
            setTitle("\\ " + currentRadioTrack + " /");
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
