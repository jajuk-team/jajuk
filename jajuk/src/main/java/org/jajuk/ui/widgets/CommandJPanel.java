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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
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
import org.jajuk.ui.helpers.PlayerStateMediator;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilFeatures;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;

/**
 * Command panel ( static view )
 * <p>
 * Singleton
 * </p>
 */
public class CommandJPanel extends JXPanel implements ActionListener, ChangeListener, Observer,
    MouseWheelListener {

  private static final long serialVersionUID = 1L;

  // singleton
  private static CommandJPanel command;

  // widgets declaration

  private JajukToggleButton jbRepeat;

  private JajukToggleButton jbRandom;

  private JajukToggleButton jbContinue;

  private JajukToggleButton jbIntro;
  
  private JajukToggleButton jbKaraoke;

  private JToolBar jtbSpecial;

  private DropDownButton ddbGlobalRandom;

  private JRadioButtonMenuItem jmiShuffleModeSong;

  private JRadioButtonMenuItem jmiShuffleModeAlbum;

  private JRadioButtonMenuItem jmiShuffleModeAlbum2;

  private JPopupMenu popupGlobalRandom;

  private JajukButton jbBestof;

  private DropDownButton ddbNovelties;

  private JPopupMenu popupNovelties;

  private DropDownButton ddbWebRadio;

  private XJPopupMenu popupWebRadio;

  private JRadioButtonMenuItem jmiNoveltiesModeSong;

  private JRadioButtonMenuItem jmiNoveltiesModeAlbum;

  private JajukButton jbNorm;

  private DropDownButton ddbDDJ;

  private JPopupMenu popupDDJ;

  private JButton jbPrevious;

  private JButton jbNext;

  private JPressButton jbRew;

  private JButton jbPlayPause;

  private JButton jbStop;

  private JPressButton jbFwd;

  private JPanel jpVolume;

  JSlider jsVolume;

  private PreferenceToolbar evaltoobar;

  private SizedButton jbMute;

  // variables declaration
  /** Repeat mode flag */
  static boolean bIsRepeatEnabled = false;

  /** Shuffle mode flag */
  static boolean bIsShuffleEnabled = false;

  /** Continue mode flag */
  static boolean bIsContinueEnabled = true;

  /** Intro mode flag */
  static boolean bIsIntroEnabled = false;

  /** Forward or rewind jump size in track percentage */
  static final float JUMP_SIZE = 0.1f;

  private JajukToggleButton jbRepeatAll;

  /**
   * @return singleton
   */
  public static CommandJPanel getInstance() {
    if (command == null) {
      command = new CommandJPanel();
    }
    return command;
  }

  /**
   * Constructor, this objects needs to be implemented for the tray (child
   * object)
   */
  CommandJPanel() {
    super();
  }

  public void initUI() {
    // Instanciate the PlayerStateMediator to listen for player basic controls
    PlayerStateMediator.getInstance();

    // mute
    jbMute = new SizedButton(ActionManager.getAction(MUTE_STATE), false) {
      private static final long serialVersionUID = -1;

      @Override
      public int getW() {
        return 28;
      }

      public int getH() {
        return 24;
      }
    };
    jbMute.setBorder(null);

    // Mode toolbar
    // we need an inner toolbar to apply size properly
    JToolBar jtbModes = new JajukJToolbar();
    // make it not floatable as this behavior is managed by vldocking
    jbRepeat = new JajukToggleButton(ActionManager.getAction(JajukActions.REPEAT_MODE));
    jbRepeat.setSelected(Conf.getBoolean(Const.CONF_STATE_REPEAT));
    jbRepeatAll = new JajukToggleButton(ActionManager.getAction(JajukActions.REPEAT_ALL_MODE));
    jbRepeatAll.setSelected(Conf.getBoolean(Const.CONF_STATE_REPEAT_ALL));
    jbRandom = new JajukToggleButton(ActionManager.getAction(JajukActions.SHUFFLE_MODE));
    jbRandom.setSelected(Conf.getBoolean(Const.CONF_STATE_SHUFFLE));
    jbContinue = new JajukToggleButton(ActionManager.getAction(JajukActions.CONTINUE_MODE));
    jbContinue.setSelected(Conf.getBoolean(Const.CONF_STATE_CONTINUE));
    jbIntro = new JajukToggleButton(ActionManager.getAction(JajukActions.INTRO_MODE));
    jbIntro.setSelected(Conf.getBoolean(Const.CONF_STATE_INTRO));
    jbKaraoke = new JajukToggleButton(ActionManager.getAction(JajukActions.KARAOKE_MODE));
    jbKaraoke.setSelected(Conf.getBoolean(Const.CONF_STATE_KARAOKE));
    jtbModes.add(jbRepeat);
    jtbModes.add(Box.createHorizontalStrut(4));
    jtbModes.add(jbRepeatAll);
    jtbModes.add(Box.createHorizontalStrut(4));
    jtbModes.add(jbRandom);
    jtbModes.add(Box.createHorizontalStrut(4));
    jtbModes.add(jbContinue);
    jtbModes.add(Box.createHorizontalStrut(4));
    jtbModes.add(jbIntro);
    jtbModes.add(Box.createHorizontalStrut(4));
    jtbModes.add(jbKaraoke);

    // Eval toolbar
    evaltoobar = new PreferenceToolbar();

    // Volume
    jpVolume = new JPanel(new MigLayout("insets 0,gapx 5", "[][grow]", "[grow]"));
    jpVolume.addMouseWheelListener(CommandJPanel.this);
    ActionUtil.installKeystrokes(jpVolume, ActionManager.getAction(JajukActions.DECREASE_VOLUME),
        ActionManager.getAction(JajukActions.INCREASE_VOLUME));

    int iVolume = (int) (100 * Conf.getFloat(Const.CONF_VOLUME));
    // Perform bounds test, -1 or >100 can occur in some undefined cases (see
    // #1169)
    if (iVolume > 100) {
      iVolume = 100;
    } else if (iVolume < 0) {
      iVolume = 0;
    }
    jsVolume = new JSlider(0, 100, iVolume);
    jsVolume.setToolTipText(iVolume + " %");
    jsVolume.addChangeListener(CommandJPanel.this);
    MuteAction.setVolumeIcon(iVolume);
    jpVolume.add(jbMute);
    jpVolume.add(jsVolume, "growx");
    
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

    popupWebRadio = new XJPopupMenu(JajukWindow.getInstance());
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

    // Play toolbar
    JToolBar jtbPlay = new JajukJToolbar();
    ActionUtil.installKeystrokes(jtbPlay, ActionManager.getAction(NEXT_ALBUM), ActionManager
        .getAction(PREVIOUS_ALBUM));
    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbPlayPause = new JajukButton(ActionManager.getAction(PAUSE_RESUME_TRACK));
    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));

    jtbPlay.add(jbStop);
    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbNext);

    // Add items
    setLayout(new MigLayout("insets 5,gapx 15", "[grow][grow][grow]"));
    add(jtbWebRadio, "left,split 2");
    add(jtbSpecial, "left");
    add(jtbPlay, "center,split 2");
    add(jpVolume, "center,grow,width 25::100");
    add(jtbModes, "right,split 2,gap right 10");
    add(evaltoobar, "right");

    // register to player events
    ObservationManager.register(CommandJPanel.this);

    // Update initial status
    UtilFeatures.updateStatus(this);

  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.SPECIAL_MODE);
    eventSubjectSet.add(JajukEvents.ZERO);
    eventSubjectSet.add(JajukEvents.REPEAT_MODE_STATUS_CHANGED);
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.CLEAR_HISTORY);
    eventSubjectSet.add(JajukEvents.VOLUME_CHANGED);
    eventSubjectSet.add(JajukEvents.DJS_CHANGE);
    eventSubjectSet.add(JajukEvents.AMBIENCES_CHANGE);
    eventSubjectSet.add(JajukEvents.WEBRADIOS_CHANGE);
    eventSubjectSet.add(JajukEvents.AMBIENCES_SELECTION_CHANGE);
    eventSubjectSet.add(JajukEvents.WEBRADIO_LAUNCHED);
    return eventSubjectSet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
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
  public void stateChanged(ChangeEvent e) {
    if (e.getSource() == jsVolume) {
      setVolume((float) jsVolume.getValue() / 100);
      MuteAction.setVolumeIcon(jsVolume.getValue());
    }
  }

  /**
   * @return Volume value
   */
  public int getCurrentVolume() {
    return this.jsVolume.getValue();
  }

  public void setVolume(final float fVolume) {
    jsVolume.removeChangeListener(this);
    jpVolume.removeMouseWheelListener(this);
    // if user move the volume slider, unmute
    if (Player.isMuted()) {
      Player.mute(false);
    }
    Player.setVolume(fVolume);
    jsVolume.addChangeListener(CommandJPanel.this);
    jpVolume.addMouseWheelListener(CommandJPanel.this);
    jsVolume.setToolTipText((int) (fVolume * 100) + " %");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.PLAYER_PAUSE.equals(subject)) {
          // Disable volume control when pausing to fix an mplayer
          // issue:
          // setting the volume resume the file
          jsVolume.setEnabled(false);
          jpVolume.removeMouseWheelListener(CommandJPanel.this);
        } else if (JajukEvents.PLAYER_RESUME.equals(subject)) {
          // Enable the volume when resuming (fix a mplayer issue, see
          // above)
          jsVolume.setEnabled(true);
          jpVolume.addMouseWheelListener(CommandJPanel.this);
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
          jsVolume.removeChangeListener(CommandJPanel.this);
          jsVolume.setValue((int) (100 * Player.getCurrentVolume()));
          jsVolume.setToolTipText((int) (100 * Player.getCurrentVolume()) + " %");
          jsVolume.addChangeListener(CommandJPanel.this);
          jbMute.setSelected(Player.isMuted());
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
        }
      }
    });
  }

  /**
   * Populate DJs
   * 
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
        JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(dj.getName(), IconLoader
            .getIcon(JajukIcons.DIGITAL_DJ_16X16));
        jmi.addActionListener(new ActionListener() {
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
   * Populate webradios
   * 
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
   * ToString() method
   */
  @Override
  public String toString() {
    return getClass().getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @seejava.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
   * MouseWheelEvent)
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource() == jsVolume || e.getSource() == jpVolume) {
      int iOld = jsVolume.getValue();
      int iNew = iOld - (e.getUnitsToScroll() * 3);
      jsVolume.setValue(iNew);
    }
  }

  public void setRepeatSelected(final boolean b) {
    this.jbRepeat.setSelected(b);
  }

  public void setRepeatAllSelected(final boolean b) {
    this.jbRepeatAll.setSelected(b);
  }

  public void setRandomSelected(final boolean b) {
    this.jbRandom.setSelected(b);
  }

  public void setContinueSelected(final boolean b) {
    this.jbContinue.setSelected(b);
  }

  public void setContinueBorder(final Border border) {
    this.jbContinue.setBorder(border);
  }

  public void setIntroSelected(final boolean b) {
    this.jbIntro.setSelected(b);
  }
  
  public void setKaraokeSelected(final boolean b) {
    this.jbKaraoke.setSelected(b);
  }

}
