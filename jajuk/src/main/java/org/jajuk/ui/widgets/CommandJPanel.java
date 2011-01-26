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
package org.jajuk.ui.widgets;

import static org.jajuk.ui.actions.JajukActions.CONFIGURE_DJS;
import static org.jajuk.ui.actions.JajukActions.FINISH_ALBUM;
import static org.jajuk.ui.actions.JajukActions.MUTE_STATE;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PAUSE_RESUME_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;
import ext.DropDownButton;
import ext.scrollablepopupmenu.XCheckedButton;
import ext.scrollablepopupmenu.XJPopupMenu;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukAction;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.actions.MuteAction;
import org.jajuk.ui.helpers.JajukMouseAdapter;
import org.jajuk.ui.helpers.PlayerStateMediator;
import org.jajuk.ui.substance.CircleButtonShaper;
import org.jajuk.ui.substance.LeftConcaveButtonShaper;
import org.jajuk.ui.substance.RightConcaveButtonShaper;
import org.jajuk.ui.substance.RoundRectButtonShaper;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;
import org.jvnet.substance.SubstanceLookAndFeel;

/**
 * Command panel ( static view )
 * <p>
 * Singleton
 * </p>.
 */
public class CommandJPanel extends JXPanel implements ActionListener, ChangeListener, Observer,
    MouseWheelListener {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  // singleton
  /** DOCUMENT_ME. */
  private static CommandJPanel command = new CommandJPanel();

  // widgets declaration

  /** Continue mode button. */
  private JajukToggleButton jbContinue;

  /** DOCUMENT_ME. */
  private JajukToggleButton jbRepeat;

  /** DOCUMENT_ME. */
  private JajukToggleButton jbRandom;

  /** DOCUMENT_ME. */
  private JToolBar jtbSpecial;

  /** DOCUMENT_ME. */
  private DropDownButton ddbGlobalRandom;

  /** DOCUMENT_ME. */
  private JRadioButtonMenuItem jmiShuffleModeSong;

  /** DOCUMENT_ME. */
  private JRadioButtonMenuItem jmiShuffleModeAlbum;

  /** DOCUMENT_ME. */
  private JRadioButtonMenuItem jmiShuffleModeAlbum2;

  /** DOCUMENT_ME. */
  private JPopupMenu popupGlobalRandom;

  /** DOCUMENT_ME. */
  private JajukButton jbBestof;

  /** DOCUMENT_ME. */
  private DropDownButton ddbNovelties;

  /** DOCUMENT_ME. */
  private JPopupMenu popupNovelties;

  /** DOCUMENT_ME. */
  private DropDownButton ddbWebRadio;

  /** DOCUMENT_ME. */
  private XJPopupMenu popupWebRadio;

  /** DOCUMENT_ME. */
  private JRadioButtonMenuItem jmiNoveltiesModeSong;

  /** DOCUMENT_ME. */
  private JRadioButtonMenuItem jmiNoveltiesModeAlbum;

  /** DOCUMENT_ME. */
  private JajukButton jbNorm;

  /** DOCUMENT_ME. */
  private DropDownButton ddbDDJ;

  /** DOCUMENT_ME. */
  private JPopupMenu popupDDJ;

  /** DOCUMENT_ME. */
  private JButton jbPrevious;

  /** DOCUMENT_ME. */
  private JButton jbNext;

  /** DOCUMENT_ME. */
  private JButton jbPlayPause;

  /** DOCUMENT_ME. */
  private JButton jbStop;

  /** DOCUMENT_ME. */
  private JSlider jsVolume;

  /** DOCUMENT_ME. */
  private JLabel jlVolume;

  /** DOCUMENT_ME. */
  private PreferenceToolbar evaltoobar;

  /** DOCUMENT_ME. */
  private JajukButton jbMute;

  // variables declaration
  /** DOCUMENT_ME. */
  private JajukToggleButton jbRepeatAll;

  /**
   * Gets the instance.
   * 
   * @return singleton
   */
  public static CommandJPanel getInstance() {
    return command;
  }

  /**
   * Constructor, this objects needs to be implemented for the tray (child
   * object).
   */
  public CommandJPanel() {
    super();
  }

  /**
   * Inits the ui.
   * 
   */
  public void initUI() {
    // Instanciate the PlayerStateMediator to listen for player basic controls
    PlayerStateMediator.getInstance();

    // Install keystrokes on invisible components
    ActionUtil.installKeystrokes(CommandJPanel.this, ActionManager.getAction(NEXT_ALBUM),
        ActionManager.getAction(PREVIOUS_ALBUM));

    // Mode toolbar
    // we need an inner toolbar to apply size properly
    JPanel jpModes = new JPanel();
    jpModes.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 3));
    // make it not floatable as this behavior is managed by vldocking
    jbContinue = new JajukToggleButton(ActionManager.getAction(JajukActions.CONTINUE_MODE));
    jbContinue.setSelected(Conf.getBoolean(Const.CONF_STATE_CONTINUE));
    jbRepeat = new JajukToggleButton(ActionManager.getAction(JajukActions.REPEAT_MODE));
    jbRepeat.setSelected(Conf.getBoolean(Const.CONF_STATE_REPEAT));
    jbRepeatAll = new JajukToggleButton(ActionManager.getAction(JajukActions.REPEAT_ALL_MODE));
    jbRepeatAll.setSelected(Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL));
    jbRandom = new JajukToggleButton(ActionManager.getAction(JajukActions.SHUFFLE_MODE));
    jbRandom.setSelected(Conf.getBoolean(Const.CONF_STATE_SHUFFLE));
    jpModes.add(jbContinue);
    jpModes.add(jbRepeat);
    jpModes.add(jbRepeatAll);
    jpModes.add(jbRandom);

    // Eval toolbar
    evaltoobar = new PreferenceToolbar();

    // Volume
    int iVolume = (int) (100 * Conf.getFloat(Const.CONF_VOLUME));
    // Perform bounds test, -1 or >100 can occur in some undefined cases (see
    // #1169)
    if (iVolume > 100) {
      iVolume = 100;
    } else if (iVolume < 0) {
      iVolume = 0;
    }
    jsVolume = new JSlider(0, 100, iVolume);
    jlVolume = new JLabel(iVolume + " %");
    jsVolume.addChangeListener(CommandJPanel.this);
    jbMute = new JajukButton(ActionManager.getAction(MUTE_STATE));
    jbMute.setText(null);
    jbMute.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new RoundRectButtonShaper());
    MuteAction.setVolumeIcon(iVolume);
    jbMute.addMouseWheelListener(CommandJPanel.this);
    jsVolume.addMouseWheelListener(CommandJPanel.this);
    ActionUtil.installKeystrokes(jbMute, ActionManager.getAction(JajukActions.DECREASE_VOLUME),
        ActionManager.getAction(JajukActions.INCREASE_VOLUME));
    ActionUtil.installKeystrokes(jsVolume, ActionManager.getAction(JajukActions.DECREASE_VOLUME),
        ActionManager.getAction(JajukActions.INCREASE_VOLUME));

    // Special functions toolbar
    jtbSpecial = new JajukJToolbar();
    ddbGlobalRandom = new DropDownButton(IconLoader.getIcon(JajukIcons.SHUFFLE_GLOBAL)) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JPopupMenu getPopupMenu() {
        return popupGlobalRandom;
      }
    };
    ddbGlobalRandom.setAction(ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL));
    popupGlobalRandom = new JPopupMenu();
    // Global shuffle
    jmiShuffleModeSong = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.20"));
    jmiShuffleModeSong.addActionListener(this);
    // album / album
    jmiShuffleModeAlbum = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.21"));
    jmiShuffleModeAlbum.addActionListener(this);
    // Shuffle album / album
    jmiShuffleModeAlbum2 = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.22"));
    jmiShuffleModeAlbum2.addActionListener(this);
    if (Conf.getString(Const.CONF_GLOBAL_RANDOM_MODE).equals(Const.MODE_TRACK)) {
      jmiShuffleModeSong.setSelected(true);
    } else if (Conf.getString(Const.CONF_GLOBAL_RANDOM_MODE).equals(Const.MODE_ALBUM2)) {
      jmiShuffleModeAlbum2.setSelected(true);
    } else {
      jmiShuffleModeAlbum.setSelected(true);
    }
    ButtonGroup bgGlobalRandom = new ButtonGroup();
    bgGlobalRandom.add(jmiShuffleModeSong);
    bgGlobalRandom.add(jmiShuffleModeAlbum);
    bgGlobalRandom.add(jmiShuffleModeAlbum2);
    popupGlobalRandom.add(jmiShuffleModeSong);
    popupGlobalRandom.add(jmiShuffleModeAlbum);
    popupGlobalRandom.add(jmiShuffleModeAlbum2);
    ddbGlobalRandom.setText("");// no text visible

    jbBestof = new JajukButton(ActionManager.getAction(JajukActions.BEST_OF));

    ddbNovelties = new DropDownButton(IconLoader.getIcon(JajukIcons.NOVELTIES)) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JPopupMenu getPopupMenu() {
        return popupNovelties;
      }
    };
    ddbNovelties.setAction(ActionManager.getAction(JajukActions.NOVELTIES));
    popupNovelties = new JPopupMenu();
    jmiNoveltiesModeSong = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.20"));
    jmiNoveltiesModeSong.addActionListener(this);
    jmiNoveltiesModeAlbum = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.22"));
    jmiNoveltiesModeAlbum.addActionListener(this);
    if (Conf.getString(Const.CONF_NOVELTIES_MODE).equals(Const.MODE_TRACK)) {
      jmiNoveltiesModeSong.setSelected(true);
    } else {
      jmiNoveltiesModeAlbum.setSelected(true);
    }
    ButtonGroup bgNovelties = new ButtonGroup();
    bgNovelties.add(jmiNoveltiesModeSong);
    bgNovelties.add(jmiNoveltiesModeAlbum);
    popupNovelties.add(jmiNoveltiesModeSong);
    popupNovelties.add(jmiNoveltiesModeAlbum);
    ddbNovelties.setText("");// no text visible
    jbNorm = new JajukButton(ActionManager.getAction(FINISH_ALBUM));

    // Radio tool bar
    popupDDJ = new JPopupMenu();
    ddbDDJ = new DropDownButton(IconLoader.getIcon(JajukIcons.DIGITAL_DJ)) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JPopupMenu getPopupMenu() {
        return popupDDJ;
      }
    };
    ddbDDJ.setAction(ActionManager.getAction(JajukActions.DJ));
    populateDJs();
    // no text visible
    ddbDDJ.setText("");

    popupWebRadio = new XJPopupMenu(JajukMainWindow.getInstance());
    ddbWebRadio = new DropDownButton(IconLoader.getIcon(JajukIcons.WEBRADIO)) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JPopupMenu getPopupMenu() {
        return popupWebRadio;
      }
    };
    ddbWebRadio.setAction(ActionManager.getAction(JajukActions.WEB_RADIO));
    populateWebRadios();
    // no text
    ddbWebRadio.setText("");
    JToolBar jtbWebRadio = new JajukJToolbar();
    ddbWebRadio.addToToolBar(jtbWebRadio);

    ddbDDJ.addToToolBar(jtbSpecial);
    ddbNovelties.addToToolBar(jtbSpecial);
    ddbGlobalRandom.addToToolBar(jtbSpecial);
    jtbSpecial.add(jbBestof);
    jtbSpecial.add(jbNorm);

    // Play buttons
    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    // Manage right click : replay the track (this not triggers an action so we use a MouseAdapter here)
    jbPrevious.addMouseListener(new JajukMouseAdapter() {
      @Override
      public void handlePopup(final MouseEvent me) {
        // Create an ActionEvent from this MouseEvent with a custom modifier : the right click
        ActionEvent ae = new ActionEvent(jbPrevious, 0, PREVIOUS_TRACK.name(), 4332424);
        try {
          ActionManager.getAction(PREVIOUS_TRACK).perform(ae);
        } catch (Exception e) {
          Log.error(e);
        }
      }
    });
    // Compute concavity of player icon
    int concavity = IconLoader.getIcon(JajukIcons.PLAYER_PLAY).getIconHeight();
    jbPrevious.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new RightConcaveButtonShaper(concavity));
    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbNext.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new LeftConcaveButtonShaper(concavity));
    jbPlayPause = new JajukButton(ActionManager.getAction(PAUSE_RESUME_TRACK));
    jbPlayPause.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new CircleButtonShaper());
    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
    jbStop.putClientProperty(SubstanceLookAndFeel.BUTTON_SHAPER_PROPERTY,
        new RoundRectButtonShaper());

    // Add items
    setLayout(new MigLayout("insets 5 0 0 5", "[grow][grow][grow]"));

    add(jtbWebRadio, "left,split 2,gapright 10");
    add(jtbSpecial, "left,gapright 10");

    add(jbStop, "center,split 7,width 40!,height 30,gapright 5!");
    add(jbPrevious, "center,width 58!,height 30!,gapright 0");
    add(jbPlayPause, "center,width 45!,height 45!,gapright 0");
    add(jbNext, "center,width 58!,height 30!,gapright 3");
    add(jbMute, "center,width 42!,height 30!,gapright 5");
    add(jsVolume, "center,growx,width 25::100,gapright 3");
    add(jlVolume, "width 40!,gapright 10");

    add(jpModes, "right,split 2,gapright 5");
    add(evaltoobar, "right");

    // register to player events
    ObservationManager.register(CommandJPanel.this);

    // Update initial status
    UtilFeatures.updateStatus(this);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.PLAYER_PLAY);
    eventSubjectSet.add(JajukEvents.PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.SPECIAL_MODE);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.REPEAT_MODE_STATUS_CHANGED);
    eventSubjectSet.add(JajukEvents.CLEAR_HISTORY);
    eventSubjectSet.add(JajukEvents.VOLUME_CHANGED);
    eventSubjectSet.add(JajukEvents.DJS_CHANGE);
    eventSubjectSet.add(JajukEvents.WEBRADIOS_CHANGE);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    eventSubjectSet.add(JajukEvents.PARAMETERS_CHANGE);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(final ActionEvent ae) {
    // do not run this in a separate thread because Player actions would die
    // with the thread
    try {
      if (ae.getSource().equals(jmiNoveltiesModeSong)) {
        Conf.setProperty(Const.CONF_NOVELTIES_MODE, Const.MODE_TRACK);
      } else if (ae.getSource().equals(jmiNoveltiesModeAlbum)) {
        Conf.setProperty(Const.CONF_NOVELTIES_MODE, Const.MODE_ALBUM);
      } else if (ae.getSource().equals(jmiShuffleModeSong)) {
        Conf.setProperty(Const.CONF_GLOBAL_RANDOM_MODE, Const.MODE_TRACK);
      } else if (ae.getSource().equals(jmiShuffleModeAlbum)) {
        Conf.setProperty(Const.CONF_GLOBAL_RANDOM_MODE, Const.MODE_ALBUM);
      } else if (ae.getSource().equals(jmiShuffleModeAlbum2)) {
        Conf.setProperty(Const.CONF_GLOBAL_RANDOM_MODE, Const.MODE_ALBUM2);
      } else if (ae.getSource().equals(jmiShuffleModeAlbum2)) {
        Conf.setProperty(Const.CONF_GLOBAL_RANDOM_MODE, Const.MODE_ALBUM2);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /*
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent )
   */
  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
   */
  @Override
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == jsVolume) {
      float newVolume = (float) jsVolume.getValue() / 100;
      Player.setVolume(newVolume);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @seejava.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event. MouseWheelEvent)
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource() == jsVolume
        || (e.getSource() == jbMute && !Conf.getBoolean(Const.CONF_BIT_PERFECT))) {
      int iOld = jsVolume.getValue();
      float newVolume = ((float) (iOld - (e.getUnitsToScroll() * 3))) / 100;
      Player.setVolume(newVolume);
    }
  }

  /**
   * Gets the current volume.
   * 
   * @return Volume value
   */
  public int getCurrentVolume() {
    return this.jsVolume.getValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.PLAYER_PAUSE.equals(subject)) {
          // Disable volume control when pausing to fix an mplayer
          // issue:
          // setting the volume resume the file
          jsVolume.setEnabled(false);
          jbMute.removeMouseWheelListener(CommandJPanel.this);
          jsVolume.removeMouseWheelListener(CommandJPanel.this);
        } else if (JajukEvents.PLAYER_RESUME.equals(subject)) {
          // Enable the volume when resuming (fix a mplayer issue, see
          // above)
          jsVolume.setEnabled(!Conf.getBoolean(Const.CONF_BIT_PERFECT));
          jbMute.addMouseWheelListener(CommandJPanel.this);
          jsVolume.addMouseWheelListener(CommandJPanel.this);
        } else if (JajukEvents.PLAYER_PLAY.equals(subject)) {
          jsVolume.setEnabled(!Conf.getBoolean(Const.CONF_BIT_PERFECT));
        } else if (JajukEvents.SPECIAL_MODE.equals(subject)) {
          if (ObservationManager.getDetail(event, Const.DETAIL_ORIGIN).equals(
              Const.DETAIL_SPECIAL_MODE_NORMAL)) {
            // deselect shuffle mode
            Conf.setProperty(Const.CONF_STATE_SHUFFLE, Const.FALSE);
            JajukJMenuBar.getInstance().setShuffleSelected(false);
            CommandJPanel.getInstance().jbRandom.setSelected(false);
            // computes planned tracks
            QueueModel.computesPlanned(true);
          }
        } else if (JajukEvents.REPEAT_MODE_STATUS_CHANGED.equals(subject)) {
          if (ObservationManager.getDetail(event, Const.DETAIL_SELECTION).equals(Const.FALSE)) {
            // deselect repeat mode
            Conf.setProperty(Const.CONF_STATE_REPEAT, Const.FALSE);
            JajukJMenuBar.getInstance().setRepeatSelected(false);
            CommandJPanel.getInstance().jbRepeat.setSelected(false);
          }
        } else if (JajukEvents.VOLUME_CHANGED.equals(event.getSubject())) {
          // Update volume GUI
          jsVolume.removeChangeListener(CommandJPanel.this);
          jbMute.removeMouseWheelListener(CommandJPanel.this);
          jsVolume.removeMouseWheelListener(CommandJPanel.this);
          jsVolume.setValue((int) (100 * Player.getCurrentVolume()));
          String sVolume = (int) (100 * Player.getCurrentVolume()) + " %";
          jsVolume.setToolTipText(sVolume);
          jlVolume.setText(sVolume);
          jbMute.setSelected(Player.isMuted());
          jsVolume.addChangeListener(CommandJPanel.this);
          jbMute.addMouseWheelListener(CommandJPanel.this);
          jsVolume.addMouseWheelListener(CommandJPanel.this);
        } else if (JajukEvents.DJS_CHANGE.equals(event.getSubject())) {
          populateDJs();
          // If no more DJ, change the tooltip
          if (DigitalDJManager.getInstance().getDJs().size() == 0) {
            JajukAction action = ActionManager.getAction(JajukActions.DJ);
            action.setShortDescription(Messages.getString("CommandJPanel.18"));
          }
        } else if (JajukEvents.WEBRADIOS_CHANGE.equals(event.getSubject())) {
          populateWebRadios();
        } else if (JajukEvents.WEBRADIO_LAUNCHED.equals(event.getSubject())) {
          populateWebRadios();
        } else if (JajukEvents.PARAMETERS_CHANGE.equals(event.getSubject())) {
          // Disable volume GUI in bit perfect mode
          jsVolume.setEnabled(!Conf.getBoolean(Const.CONF_BIT_PERFECT));
        }
      }
    });
  }

  /**
   * Populate DJs.
   */
  private void populateDJs() {
    try {
      ddbDDJ.setToolTipText(Const.HTML + Messages.getString("CommandJPanel.18") + Const.P_B
          + DigitalDJManager.getCurrentDJ() + "</b></html>");
      popupDDJ.removeAll();
      JMenuItem jmiNew = new JMenuItem(ActionManager.getAction(CONFIGURE_DJS));
      popupDDJ.add(jmiNew);
      Iterator<DigitalDJ> it = DigitalDJManager.getInstance().getDJs().iterator();
      while (it.hasNext()) {
        final DigitalDJ dj = it.next();
        JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(dj.getName(),
            IconLoader.getIcon(JajukIcons.DIGITAL_DJ_16X16));
        jmi.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent arg0) {
            Conf.setProperty(Const.CONF_DEFAULT_DJ, dj.getID());
            DigitalDJManager.setCurrentDJ(dj);
            // force to reselect the item
            populateDJs();
            // update action tooltip on main button with right item
            JajukAction action = ActionManager.getAction(JajukActions.DJ);
            action.setShortDescription(Const.HTML + Messages.getString("CommandJPanel.18")
                + Const.P_B + dj.getName() + Const.B_P_HTML);
          }
        });
        popupDDJ.add(jmi);
        jmi.setSelected(Conf.getString(Const.CONF_DEFAULT_DJ).equals(dj.getID()));
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Populate webradios.
   */
  private void populateWebRadios() {
    try {
      // Update button tooltip
      ddbWebRadio.setToolTipText(WebRadioManager.getCurrentWebRadioTooltip());
      // Clear previous elements
      popupWebRadio.removeAll();
      // Add configure radios item
      JajukAction actionConf = ActionManager.getAction(JajukActions.CONFIGURE_WEBRADIOS);
      XCheckedButton jmiConf = new XCheckedButton(actionConf);
      // Set icon so it is correctly displayed after a selection
      jmiConf.setCheckedIcon((ImageIcon) actionConf.getValue(Action.SMALL_ICON));
      // The icon should be always displayed
      jmiConf.setIconAlwaysVisible(true);
      popupWebRadio.add(jmiConf);
      for (final WebRadio radio : WebRadioManager.getInstance().getWebRadios()) {
        XCheckedButton jmi = new XCheckedButton(radio.getName());
        jmi.addActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            Conf.setProperty(Const.CONF_DEFAULT_WEB_RADIO, radio.getName());
            // force to reselect the item
            populateWebRadios();
            // update action tooltip on main button with right item
            JajukAction action = ActionManager.getAction(JajukActions.WEB_RADIO);
            action.setShortDescription(Const.HTML + Messages.getString("CommandJPanel.25")
                + Const.P_B + radio.getName() + Const.B_P_HTML);
          }
        });
        jmi.setSelected(Conf.getString(Const.CONF_DEFAULT_WEB_RADIO).equals(radio.getName()));
        // Show the check icon
        jmi.setDisplayCheck(true);
        popupWebRadio.add(jmi);
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * ToString() method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return getClass().getName();
  }

  /**
   * Sets the repeat selected.
   * 
   * @param b the new repeat selected
   */
  public void setRepeatSelected(final boolean b) {
    this.jbRepeat.setSelected(b);
  }

  /**
   * Sets the repeat all selected.
   * 
   * @param b the new repeat all selected
   */
  public void setRepeatAllSelected(final boolean b) {
    this.jbRepeatAll.setSelected(b);
  }

  /**
   * Sets the random selected.
   * 
   * @param b the new random selected
   */
  public void setRandomSelected(final boolean b) {
    this.jbRandom.setSelected(b);
  }
}
