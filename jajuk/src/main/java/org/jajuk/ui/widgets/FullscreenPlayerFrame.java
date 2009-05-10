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

import static org.jajuk.ui.actions.JajukActions.FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PAUSE_RESUME_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.FlowLayout;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.views.AnimationView;
import org.jajuk.ui.views.CoverView;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * 
 */
public class FullscreenPlayerFrame extends JWindow {

  /**
   * 
   */
  private static final long serialVersionUID = -2859302706462954993L;

  private static FullscreenPlayerFrame instance = null;

  private DisplayMode origDisplayMode;

  private GraphicsDevice graphicsDevice;

  private boolean fullscreen = false;

  private JPanel topPanel;

  private JajukButton jbPrevious;

  private JajukButton jbNext;

  private JPressButton jbRew;

  private JajukButton jbPlayPause;

  private JajukButton jbStop;

  private JPressButton jbFwd;

  private JajukButton jbFull;

  private JPanel bottomPanel;

  private JPanel jSliderPanel;

  public static FullscreenPlayerFrame getInstance() {
    if (instance == null) {
      instance = new FullscreenPlayerFrame();
    }
    return instance;
  }

  /**
   * @return the fullscreen
   */
  public boolean isFullscreen() {
    return this.fullscreen;
  }

  public FullscreenPlayerFrame() {
    // get the active graphic device and store the current mode
    this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment()
        .getDefaultScreenDevice();
    this.origDisplayMode = graphicsDevice.getDisplayMode();

    initGui();

  }

  /**
   * 
   */
  private void initGui() {

    // North
    jbFull = new JajukButton(ActionManager.getAction(JajukActions.FULLSCREEN_JAJUK));

    JPanel menuPanel = new JPanel();
    menuPanel.setLayout(new BorderLayout());
    menuPanel.add(jbFull, BorderLayout.EAST);

    AnimationView animationView = new AnimationView();
    animationView.initUI();

    topPanel = new JPanel();
    topPanel.setLayout(new BorderLayout());

    topPanel.add(menuPanel, BorderLayout.NORTH);
    topPanel.add(animationView, BorderLayout.CENTER);

    // Center
    CoverView coverView = new CoverView();
    coverView.initUI(false);

    // South
    bottomPanel = new JPanel();
    bottomPanel.setLayout(new BorderLayout());

    // Player toolbar
    JToolBar jtbPlay = new JajukJToolbar();
    jtbPlay.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 5));

    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    jbPrevious.setIcon(IconLoader.getIcon(JajukIcons.PLAYER_PREVIOUS_BIG));

    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbNext.setIcon(IconLoader.getIcon(JajukIcons.PLAYER_NEXT_BIG));

    jbRew = new JPressButton(ActionManager.getAction(REWIND_TRACK));
    jbRew.setIcon(IconLoader.getIcon(JajukIcons.PLAYER_REWIND_BIG));

    jbPlayPause = new JajukButtonSetIconAdapter(ActionManager.getAction(PAUSE_RESUME_TRACK));
    jbPlayPause.setIcon(IconLoader.getIcon(JajukIcons.PAUSE));

    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
    jbStop.setIcon(IconLoader.getIcon(JajukIcons.PLAYER_STOP_BIG));

    jbFwd = new JPressButton(ActionManager.getAction(FORWARD_TRACK));
    jbFwd.setIcon(IconLoader.getIcon(JajukIcons.PLAYER_FORWARD_BIG));

    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbRew);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbStop);
    jtbPlay.add(jbFwd);
    jtbPlay.add(jbNext);

    jSliderPanel = new JPanel();
    jSliderPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    TrackPositionSliderToolbar tpst = new TrackPositionSliderToolbar();
    jSliderPanel.add(tpst);

    bottomPanel.add(jtbPlay, BorderLayout.NORTH);
    bottomPanel.add(jSliderPanel, BorderLayout.SOUTH);

    // put everything together
    getContentPane().add(topPanel, BorderLayout.NORTH);
    getContentPane().add(coverView, BorderLayout.CENTER);
    getContentPane().add(bottomPanel, BorderLayout.SOUTH);

  }

  public void setFullScreen(final boolean enable) {

    // Show or hide the frame
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        fullscreen = enable;

        if (enable) {
          // check, if we can paint fullscreen
          if (graphicsDevice.isFullScreenSupported()) {

            setVisible(true);
            graphicsDevice.setFullScreenWindow(instance);

            // topPanel should have 10% of the dispaly resolution height
            topPanel.setPreferredSize(new Dimension(graphicsDevice.getDisplayMode().getWidth(),
                (graphicsDevice.getDisplayMode().getHeight() / 100) * 10));
            validate();
          } else {
            fullscreen = false;
            Log.error(new JajukException(178, "", null));
          }
        } else {
          // set everything like it was before entering fullscreen mode
          graphicsDevice.setDisplayMode(origDisplayMode);
          graphicsDevice.setFullScreenWindow(null);
          setVisible(false);
        }
      }
    });
  }
}
