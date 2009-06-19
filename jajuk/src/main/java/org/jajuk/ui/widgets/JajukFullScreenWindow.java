/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.widgets;

import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PAUSE_RESUME_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.substance.CircleButtonShaper;
import org.jajuk.ui.substance.LeftConcaveButtonShaper;
import org.jajuk.ui.substance.RightConcaveButtonShaper;
import org.jajuk.ui.substance.RoundRectButtonShaper;
import org.jajuk.ui.views.AnimationView;
import org.jajuk.ui.views.CoverView;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jvnet.substance.SubstanceLookAndFeel;

/**
 * The full screen window
 */
public class JajukFullScreenWindow extends JWindow {

  private static final long serialVersionUID = -2859302706462954993L;

  private static JajukFullScreenWindow instance = null;

  private final DisplayMode origDisplayMode;

  private final GraphicsDevice graphicsDevice;

  private boolean fullscreen = false;

  private JButton jbPrevious;

  private JButton jbNext;

  private JButton jbPlayPause;

  private JButton jbStop;

  private JajukButton jbFull;

  private JajukButton jbExit;

  private CoverView coverView;

  public static JajukFullScreenWindow getInstance() {
    if (instance == null) {
      instance = new JajukFullScreenWindow();
    }
    return instance;
  }

  /**
   * @return the fullscreen
   */
  public boolean isFullscreen() {
    return this.fullscreen;
  }

  public JajukFullScreenWindow() {
    // get the active graphic device and store the current mode
    this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice();
    this.origDisplayMode = graphicsDevice.getDisplayMode();

    initUI();
  }

  /**
   * 
   * @return whether the window is loaded
   */
  public static boolean isLoaded() {
    return (instance != null);
  }

  /**
   * 
   */
  private void initUI() {
    // Full screen switch button
    jbFull = new JajukButton(ActionManager.getAction(JajukActions.FULLSCREEN_JAJUK));

    // Exit button
    jbExit = new JajukButton(ActionManager.getAction(JajukActions.EXIT));

    // Animation view
    AnimationView animationView = new AnimationView();
    animationView.initUI();

    // Cover view
    coverView = new CoverView();
    coverView.initUI(false);

    // Player toolbar
    JPanel jtbPlay = getPlayerPanel();

    // Information panel
    TrackPositionSliderToolbar tpst = new TrackPositionSliderToolbar();

    // Add items
    setLayout(new MigLayout("ins 0", "[grow]", "[][grow][70%!][][]"));
    add(jbFull, "right,split 2,gapright 5");
    add(jbExit, "right,wrap");
    add(animationView, "alignx center,aligny top,grow,gap bottom 20,wrap");
    add(coverView, "alignx center, grow,gap bottom 20,wrap");
    add(jtbPlay, "alignx center,gap bottom 20,wrap");
    add(tpst, "alignx center,width 50%!,aligny bottom,gap bottom 10");

  }

  /**
   * @return
   */
  private JPanel getPlayerPanel() {
    JPanel jPanelPlay = new JPanel();
    jPanelPlay.setLayout(new MigLayout("insets 5", "[grow][grow][grow]"));

    // previous
    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    int concavity = IconLoader.getIcon(JajukIcons.PLAYER_PLAY).getIconHeight();
    jbPrevious.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new RightConcaveButtonShaper(concavity));
    jbPrevious.setBorderPainted(true);
    jbPrevious.setContentAreaFilled(true);
    jbPrevious.setFocusPainted(true);

    // next
    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbNext.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new LeftConcaveButtonShaper(concavity));

    // play pause
    jbPlayPause = new JajukButton(ActionManager.getAction(PAUSE_RESUME_TRACK));
    jbPlayPause.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new CircleButtonShaper());

    // stop
    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
    jbStop.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new RoundRectButtonShaper());

    jPanelPlay.add(jbStop, "center,split 6,width 35!,height 30,gapright 5!");
    jPanelPlay.add(jbPrevious, "center,width 62!,height 30!,gapright 0");
    jPanelPlay.add(jbPlayPause, "center,width 45!,height 45!,gapright 0");
    jPanelPlay.add(jbNext, "center,width 62!,height 30!,gapright 3");

    return jPanelPlay;
  }

  public void setFullScreen(final boolean enable) {

    // Show or hide the frame
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fullscreen = enable;

        if (enable) {
          // check, if we can paint fullscreen
          if (graphicsDevice.isFullScreenSupported()) {
            JajukWindow.getInstance().setVisible(false);

            setVisible(true);
            graphicsDevice.setFullScreenWindow(instance);

            // topPanel should have 10% of the display resolution height
            setPreferredSize(new Dimension(graphicsDevice.getDisplayMode().getWidth(),
                (graphicsDevice.getDisplayMode().getHeight() / 100) * 10));

            validate();
          } else {
            fullscreen = false;
            Messages.showErrorMessage(178);
            Log.error(new JajukException(178, "", null));
          }
        } else {
          // set everything like it was before entering fullscreen mode
          graphicsDevice.setDisplayMode(origDisplayMode);
          graphicsDevice.setFullScreenWindow(null);
          setVisible(false);
          JajukWindow.getInstance().setVisible(true);
        }
      }
    });
  }
}
