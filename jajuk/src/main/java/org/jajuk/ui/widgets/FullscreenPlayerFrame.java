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

import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.views.AnimationView;
import org.jajuk.ui.views.CoverView;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
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

  private final DisplayMode origDisplayMode;

  private final GraphicsDevice graphicsDevice;

  private boolean fullscreen = false;

  private JajukButton jbPrevious;

  private JajukButton jbNext;

  private JajukButton jbPlayPause;

  private JajukButton jbStop;

  private JajukButton jbFull;

  private CoverView coverView;

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
    // Full screen switch button
    jbFull = new JajukButton(ActionManager.getAction(JajukActions.FULLSCREEN_JAJUK));

    // Animation view
    AnimationView animationView = new AnimationView();
    animationView.initUI();

    // Cover view
    coverView = new CoverView();
    coverView.initUI(false);

    // Player toolbar
    JToolBar jtbPlay = new JajukJToolbar();
    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    jbPrevious.setIcon(IconLoader.getIcon(JajukIcons.PLAYER_PREVIOUS_BIG));
    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbNext.setIcon(IconLoader.getIcon(JajukIcons.PLAYER_NEXT_BIG));
    jbPlayPause = new JajukButtonSetIconAdapter(ActionManager.getAction(PAUSE_RESUME_TRACK));
    jbPlayPause.setIcon(IconLoader.getIcon(JajukIcons.PAUSE));
    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
    jbStop.setIcon(IconLoader.getIcon(JajukIcons.PLAYER_STOP_BIG));
    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbStop);
    jtbPlay.add(jbNext);

    // Information panel
    TrackPositionSliderToolbar tpst = new TrackPositionSliderToolbar();

    // Add items
    setLayout(new MigLayout("ins 0", "[grow]", "[][grow][60%][][]"));
    add(jbFull, "right,wrap");
    add(animationView, "alignx center,aligny top,grow,gap bottom 20,wrap");
    add(coverView, "alignx center,grow,gap bottom 20,wrap");
    add(jtbPlay, "alignx center,gap bottom 20,wrap");
    add(tpst, "alignx center,aligny bottom,gap bottom 10");

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
        }
      }
    });
  }
}
