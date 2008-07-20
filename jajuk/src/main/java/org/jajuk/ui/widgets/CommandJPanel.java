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
import static org.jajuk.ui.actions.JajukActions.FAST_FORWARD_TRACK;
import static org.jajuk.ui.actions.JajukActions.FINISH_ALBUM;
import static org.jajuk.ui.actions.JajukActions.MUTE_STATE;
import static org.jajuk.ui.actions.JajukActions.NEXT_ALBUM;
import static org.jajuk.ui.actions.JajukActions.NEXT_TRACK;
import static org.jajuk.ui.actions.JajukActions.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_ALBUM;
import static org.jajuk.ui.actions.JajukActions.PREVIOUS_TRACK;
import static org.jajuk.ui.actions.JajukActions.REWIND_TRACK;
import static org.jajuk.ui.actions.JajukActions.STOP_TRACK;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.vlsolutions.swing.toolbars.ToolBarPanel;

import ext.DropDownButton;
import ext.SwingWorker;
import ext.scrollablepopupmenu.XCheckedButton;
import ext.scrollablepopupmenu.XJPopupMenu;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import org.jajuk.base.FileManager;
import org.jajuk.base.SearchResult;
import org.jajuk.base.SearchResult.SearchResultType;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.bookmark.History;
import org.jajuk.services.bookmark.HistoryItem;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.services.players.FIFO;
import org.jajuk.services.players.Player;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.actions.ActionBase;
import org.jajuk.ui.actions.ActionManager;
import org.jajuk.ui.actions.ActionUtil;
import org.jajuk.ui.actions.JajukActions;
import org.jajuk.ui.actions.MuteAction;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.JajukTimer;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.wizard.AmbienceWizard;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;

/**
 * Command panel ( static view )
 * <p>
 * Singleton
 * </p>
 */
public class CommandJPanel extends JXPanel implements Const, ActionListener,
    ListSelectionListener, ChangeListener, Observer, MouseWheelListener {

  private static final long serialVersionUID = 1L;

  // singleton
  private static CommandJPanel command;

  // Toolbar panel
  ToolBarPanel topPanel;

  // widgets declaration
  SearchBox sbSearch;

  SteppedComboBox jcbHistory;

  DropDownButton jbIncRate;

  private JajukToggleButton jbRepeat;

  private JajukToggleButton jbRandom;

  private JajukToggleButton jbContinue;

  private JajukToggleButton jbIntro;

  JToolBar jtbSpecial;

  DropDownButton ddbGlobalRandom;

  JRadioButtonMenuItem jmiShuffleModeSong;

  JRadioButtonMenuItem jmiShuffleModeAlbum;

  JRadioButtonMenuItem jmiShuffleModeAlbum2;

  JPopupMenu popupGlobalRandom;

  JajukButton jbBestof;

  DropDownButton ddbNovelties;

  JPopupMenu popupNovelties;

  DropDownButton ddbWebRadio;

  XJPopupMenu popupWebRadio;

  JRadioButtonMenuItem jmiNoveltiesModeSong;

  JRadioButtonMenuItem jmiNoveltiesModeAlbum;

  JajukButton jbNorm;

  DropDownButton ddbDDJ;

  JPopupMenu popupDDJ;

  SteppedComboBox ambiencesCombo;

  JButton jbPrevious;

  JButton jbNext;

  JPressButton jbRew;

  JButton jbPlayPause;

  JButton jbStop;

  JPressButton jbFwd;

  JPanel jpVolume;

  JSlider jsVolume;

  private JajukToggleButton jbMute;

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

  /** Swing Timer to refresh the component */
  private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      update(new Event(JajukEvents.EVENT_HEART_BEAT));
    }
  });

  /** Ambience combo listener */
  class AmbienceListener implements ActionListener {
    public void actionPerformed(ActionEvent ae) {
      // Ambience Configuration
      if (ambiencesCombo.getSelectedIndex() == 0) {
        // display the wizard
        AmbienceWizard ambience = new AmbienceWizard();
        ambience.show();
        // Reset combo to last selected item
        ambiencesCombo.removeActionListener(ambienceListener);
        Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
            Conf.getString(CONF_DEFAULT_AMBIENCE));
        if (defaultAmbience != null) {
          for (int i = 0; i < ambiencesCombo.getItemCount(); i++) {
            if (((JLabel) ambiencesCombo.getItemAt(i)).getText().equals(defaultAmbience.getName())) {
              ambiencesCombo.setSelectedIndex(i);
              break;
            }
          }
        } else {
          ambiencesCombo.setSelectedIndex(1);
        }
        ambiencesCombo.addActionListener(ambienceListener);
      }
      // Selected 'Any" ambience
      else if (ambiencesCombo.getSelectedIndex() == 1) {
        // reset default ambience
        Conf.setProperty(CONF_DEFAULT_AMBIENCE, "");
        ObservationManager.notify(new Event(JajukEvents.EVENT_AMBIENCES_SELECTION_CHANGE));
      } else {// Selected an ambience
        Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
            ((JLabel) ambiencesCombo.getSelectedItem()).getText());
        Conf.setProperty(CONF_DEFAULT_AMBIENCE, ambience.getID());
        ObservationManager.notify(new Event(JajukEvents.EVENT_AMBIENCES_SELECTION_CHANGE));
      }
    }
  }

  /** An instance of the ambience combo listener */
  AmbienceListener ambienceListener;

  /**
   * @return singleton
   */
  public static synchronized CommandJPanel getInstance() {
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
    // mute
    jbMute = new JajukToggleButton(ActionManager.getAction(MUTE_STATE));
  }

  public void initUI() {
    // Search
    double[][] sizeSearch = new double[][] { { 3, TableLayout.PREFERRED, 3, 100 }, { 25 } };
    JPanel jpSearch = new JPanel(new TableLayout(sizeSearch));
    sbSearch = new SearchBox(CommandJPanel.this);
    JLabel jlSearch = new JLabel(IconLoader.ICON_SEARCH);
    jlSearch.setToolTipText(Messages.getString("CommandJPanel.23"));
    // Clear search text when clicking on the search icon
    jlSearch.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        sbSearch.setText("");
      }
    });
    jpSearch.add(jlSearch, "1,0");
    jpSearch.add(sbSearch, "3,0");

    // History
    JPanel jpHistory = new JPanel();
    jcbHistory = new SteppedComboBox();
    JLabel jlHistory = new JLabel(IconLoader.ICON_HISTORY);
    jlHistory.setToolTipText(Messages.getString("CommandJPanel.0"));
    // - Increase rating button
    ActionBase actionIncRate = ActionManager.getAction(JajukActions.INC_RATE);
    actionIncRate.setName(null);
    final JPopupMenu jpmIncRating = new JPopupMenu();
    for (int i = 1; i <= 10; i++) {
      final int j = i;
      JMenuItem jmi = new JMenuItem("+" + i);
      if (Conf.getInt(CONF_INC_RATING) == i) {
        jmi.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      }
      // Store selected value
      jmi.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          Conf.setProperty(CONF_INC_RATING, "" + j);
        }
      });
      jpmIncRating.add(jmi);
    }
    jbIncRate = new DropDownButton(IconLoader.ICON_INC_RATING) {
      private static final long serialVersionUID = 1L;

      @Override
      protected JPopupMenu getPopupMenu() {
        return jpmIncRating;
      }
    };
    jbIncRate.setAction(actionIncRate);
    // we use a combo box model to make sure we get good performances after
    // rebuilding the entire model like after a refresh
    jcbHistory.setModel(new DefaultComboBoxModel(History.getInstance().getHistory()));
    // None selection because if we start in stop mode, a selection of the
    // first item will not launch the track because the selected item is
    // still the same and no action event is thrown (Java >= 1.6)
    jcbHistory.setSelectedItem(null);
    int iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
    // size of popup
    jcbHistory.setPopupWidth(iWidth);
    // size of the combo itself, keep it! as text can be very long
    jcbHistory.setPreferredSize(new Dimension(250, 25));
    jcbHistory.setMinimumSize(new Dimension(0, 25));
    jcbHistory.setToolTipText(Messages.getString("CommandJPanel.0"));
    jcbHistory.addActionListener(CommandJPanel.this);
    JToolBar jtbIncRate = new JToolBar();
    jtbIncRate.setFloatable(false);
    jbIncRate.addToToolBar(jtbIncRate);
    double[][] sizeHistory = new double[][] {
        { 3, TableLayout.PREFERRED, 3, TableLayout.FILL, 10, TableLayout.PREFERRED }, { 25 } };
    jpHistory.setLayout(new TableLayout(sizeHistory));
    jpHistory.add(jlHistory, "1,0");
    jpHistory.add(jcbHistory, "3,0");
    jpHistory.add(jtbIncRate, "5,0");

    // Mode toolbar
    // we need an inner toolbar to apply size properly
    JToolBar jtbModes = new JToolBar();
    jtbModes.setBorder(null);
    // make it not floatable as this behavior is managed by vldocking
    jtbModes.setFloatable(false);
    jtbModes.setRollover(true);
    jbRepeat = new JajukToggleButton(ActionManager
        .getAction(JajukActions.REPEAT_MODE_STATUS_CHANGE));
    jbRepeat.setSelected(Conf.getBoolean(CONF_STATE_REPEAT));
    jbRandom = new JajukToggleButton(ActionManager
        .getAction(JajukActions.SHUFFLE_MODE_STATUS_CHANGED));
    jbRandom.setSelected(Conf.getBoolean(CONF_STATE_SHUFFLE));
    jbContinue = new JajukToggleButton(ActionManager
        .getAction(JajukActions.CONTINUE_MODE_STATUS_CHANGED));
    jbContinue.setSelected(Conf.getBoolean(CONF_STATE_CONTINUE));
    jbIntro = new JajukToggleButton(ActionManager.getAction(JajukActions.INTRO_MODE_STATUS_CHANGED));
    jbIntro.setSelected(Conf.getBoolean(CONF_STATE_INTRO));
    jtbModes.add(jbRepeat);
    jtbModes.addSeparator();
    jtbModes.add(jbRandom);
    jtbModes.addSeparator();
    jtbModes.add(jbContinue);
    jtbModes.addSeparator();
    jtbModes.add(jbIntro);

    // Volume
    jpVolume = new JPanel();
    jpVolume.addMouseWheelListener(CommandJPanel.this);
    ActionUtil.installKeystrokes(jpVolume, ActionManager.getAction(JajukActions.DECREASE_VOLUME),
        ActionManager.getAction(JajukActions.INCREASE_VOLUME));

    jpVolume.setLayout(new BoxLayout(jpVolume, BoxLayout.X_AXIS));
    int iVolume = (int) (100 * Conf.getFloat(CONF_VOLUME));
    if (iVolume > 100) { // can occur in some undefined cases
      iVolume = 100;
    }
    jsVolume = new JSlider(0, 100, iVolume);
    jsVolume.setToolTipText(iVolume + " %");
    jsVolume.addChangeListener(CommandJPanel.this);
    jsVolume.addMouseWheelListener(CommandJPanel.this);
    MuteAction.setVolumeIcon(iVolume);
    jpVolume.add(jsVolume);
    jpVolume.add(Box.createHorizontalStrut(5));
    jpVolume.add(jbMute);

    // Special functions toolbar
    // Ambience combo
    ambiencesCombo = new SteppedComboBox();
    iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4);
    ambiencesCombo.setPopupWidth(iWidth);
    // size of the combo itself
    ambiencesCombo.setRenderer(new BasicComboBoxRenderer() {
      private static final long serialVersionUID = -6943363556191659895L;

      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index,
          boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        JLabel jl = (JLabel) value;
        setIcon(jl.getIcon());
        setText(jl.getText());
        return this;
      }
    });
    ambiencesCombo.setToolTipText(Messages.getString("DigitalDJWizard.66"));
    populateAmbiences();
    ambienceListener = new AmbienceListener();
    ambiencesCombo.addActionListener(ambienceListener);
    jtbSpecial = new JToolBar();
    jtbSpecial.setBorder(null);
    jtbSpecial.setRollover(true);
    jtbSpecial.setFloatable(false);
    ddbGlobalRandom = new DropDownButton(IconLoader.ICON_SHUFFLE_GLOBAL) {
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
    if (Conf.getString(CONF_GLOBAL_RANDOM_MODE).equals(MODE_TRACK)) {
      jmiShuffleModeSong.setSelected(true);
    } else if (Conf.getString(CONF_GLOBAL_RANDOM_MODE).equals(MODE_ALBUM2)) {
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

    ddbNovelties = new DropDownButton(IconLoader.ICON_NOVELTIES) {
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
    if (Conf.getString(CONF_NOVELTIES_MODE).equals(MODE_TRACK)) {
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
    popupDDJ = new JPopupMenu();
    ddbDDJ = new DropDownButton(IconLoader.ICON_DIGITAL_DJ) {
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
    ddbWebRadio = new DropDownButton(IconLoader.ICON_WEBRADIO) {
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

    ddbDDJ.addToToolBar(jtbSpecial);
    ddbNovelties.addToToolBar(jtbSpecial);
    ddbGlobalRandom.addToToolBar(jtbSpecial);
    jtbSpecial.add(jbBestof);
    jtbSpecial.add(jbNorm);

    // Radio tool bar
    JToolBar jtbWebRadio = new JToolBar();
    jtbWebRadio.setBorder(null);
    jtbWebRadio.setRollover(true);
    jtbWebRadio.setFloatable(false);
    ddbWebRadio.addToToolBar(jtbWebRadio);

    // Play toolbar
    JToolBar jtbPlay = new JToolBar();
    jtbPlay.setBorder(null);
    jtbPlay.setFloatable(false);
    // add some space to get generic size
    jtbPlay.setRollover(true);
    ActionUtil.installKeystrokes(jtbPlay, ActionManager.getAction(NEXT_ALBUM), ActionManager
        .getAction(PREVIOUS_ALBUM));
    jbPrevious = new JajukButton(ActionManager.getAction(PREVIOUS_TRACK));
    jbNext = new JajukButton(ActionManager.getAction(NEXT_TRACK));
    jbRew = new JPressButton(ActionManager.getAction(REWIND_TRACK));
    jbPlayPause = new JajukButton(ActionManager.getAction(PLAY_PAUSE_TRACK));
    jbStop = new JajukButton(ActionManager.getAction(STOP_TRACK));
    jbFwd = new JPressButton(ActionManager.getAction(FAST_FORWARD_TRACK));

    jtbPlay.add(jbPrevious);
    jtbPlay.add(jbRew);
    jtbPlay.add(jbPlayPause);
    jtbPlay.add(jbStop);
    jtbPlay.add(jbFwd);
    jtbPlay.add(jbNext);

    // Add items
    FormLayout layout = new FormLayout(
    // --columns
        "3dlu,fill:min(10dlu;p):grow(0.5), 3dlu, " + // ambience
            "left:p, 2dlu" + // smart toolbar
            ", min(0dlu;p):grow(0.04), 3dlu," + // glue
            " right:p, 10dlu, " + // search /modes
            "fill:p, 5dlu, " + // history/player
            "fill:min(60dlu;p):grow(0.2),3dlu", // volume/part of
        // history
        // --rows
        "2dlu, p, 2dlu, p, 2dlu"); // rows
    PanelBuilder builder = new PanelBuilder(layout);// , new
    // FormDebugPanel() );
    CellConstraints cc = new CellConstraints();
    // Add items
    builder.add(jtbWebRadio, cc.xyw(2, 2, 3));// grid width = 3
    builder.add(ambiencesCombo, cc.xy(2, 4));
    builder.add(jtbSpecial, cc.xy(4, 4));
    builder.add(jpSearch, cc.xyw(6, 2, 4));
    builder.add(jpHistory, cc.xyw(10, 2, 4));
    builder.add(jtbModes, cc.xy(8, 4));
    builder.add(jtbPlay, cc.xy(10, 4));
    builder.add(jpVolume, cc.xy(12, 4));
    JPanel p = builder.getPanel();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    add(p);
    // register to player events
    ObservationManager.register(CommandJPanel.this);

    // if a track is playing, display right state
    if (FIFO.isPlayingRadio()) {
      // update initial state
      update(new Event(JajukEvents.EVENT_WEBRADIO_LAUNCHED));
    } else if (!FIFO.isStopped()) {
      // update initial state
      update(new Event(JajukEvents.EVENT_PLAYER_PLAY, ObservationManager
          .getDetailsLastOccurence(JajukEvents.EVENT_PLAYER_PLAY)));
      // update the history bar
      update(new Event(JajukEvents.EVENT_FILE_LAUNCHED));
    } else {
      update(new Event(JajukEvents.EVENT_PLAYER_STOP));
    }
    // start timer
    timer.start();
  }

  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EVENT_PLAYER_PLAY);
    eventSubjectSet.add(JajukEvents.EVENT_PLAYER_STOP);
    eventSubjectSet.add(JajukEvents.EVENT_PLAYER_PAUSE);
    eventSubjectSet.add(JajukEvents.EVENT_PLAYER_RESUME);
    eventSubjectSet.add(JajukEvents.EVENT_PLAY_ERROR);
    eventSubjectSet.add(JajukEvents.EVENT_SPECIAL_MODE);
    eventSubjectSet.add(JajukEvents.EVENT_ZERO);
    eventSubjectSet.add(JajukEvents.EVENT_MUTE_STATE);
    eventSubjectSet.add(JajukEvents.EVENT_REPEAT_MODE_STATUS_CHANGED);
    eventSubjectSet.add(JajukEvents.EVENT_FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.EVENT_CLEAR_HISTORY);
    eventSubjectSet.add(JajukEvents.EVENT_VOLUME_CHANGED);
    eventSubjectSet.add(JajukEvents.EVENT_DJS_CHANGE);
    eventSubjectSet.add(JajukEvents.EVENT_AMBIENCES_CHANGE);
    eventSubjectSet.add(JajukEvents.EVENT_WEBRADIOS_CHANGE);
    eventSubjectSet.add(JajukEvents.EVENT_AMBIENCES_SELECTION_CHANGE);
    eventSubjectSet.add(JajukEvents.EVENT_WEBRADIO_LAUNCHED);
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
      if (ae.getSource() == jcbHistory) {
        HistoryItem hi;
        hi = History.getInstance().getHistoryItem(jcbHistory.getSelectedIndex());
        if (hi != null) {
          org.jajuk.base.File file = FileManager.getInstance().getFileByID(hi.getFileId());
          if (file != null) {
            try {
              FIFO.push(
                  new StackItem(file, Conf.getBoolean(CONF_STATE_REPEAT), true),
                  Conf.getBoolean(CONF_OPTIONS_PUSH_ON_CLICK));
            } catch (JajukException je) {
              // can be thrown if file is null
            }
          } else {
            Messages.showErrorMessage(120);
            jcbHistory.setSelectedItem(null);
          }
        }
      } else if (ae.getSource().equals(jmiNoveltiesModeSong)) {
        Conf.setProperty(CONF_NOVELTIES_MODE, MODE_TRACK);
      } else if (ae.getSource().equals(jmiNoveltiesModeAlbum)) {
        Conf.setProperty(CONF_NOVELTIES_MODE, MODE_ALBUM);
      } else if (ae.getSource().equals(jmiShuffleModeSong)) {
        Conf.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_TRACK);
      } else if (ae.getSource().equals(jmiShuffleModeAlbum)) {
        Conf.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_ALBUM);
      } else if (ae.getSource().equals(jmiShuffleModeAlbum2)) {
        Conf.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_ALBUM2);
      } else if (ae.getSource().equals(jmiShuffleModeAlbum2)) {
        Conf.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_ALBUM2);
      }
    } catch (Exception e) {
      Log.error(e);
    } finally {
      ObservationManager.notify(new Event(JajukEvents.EVENT_QUEUE_NEED_REFRESH));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
   */
  public void valueChanged(final ListSelectionEvent e) {
    SwingWorker sw = new SwingWorker() {
      @Override
      public Object construct() {
        if (!e.getValueIsAdjusting()) {
          SearchResult sr = sbSearch.getResult(sbSearch.getSelectedIndex());
          try {
            // If user selected a file
            if (sr.getType() == SearchResultType.FILE) {
              FIFO.push(
                  new StackItem(sr.getFile(), Conf.getBoolean(CONF_STATE_REPEAT),
                      true), Conf.getBoolean(CONF_OPTIONS_PUSH_ON_CLICK));
            }
            // User selected a web radio
            else if (sr.getType() == SearchResultType.WEBRADIO) {
              FIFO.launchRadio(sr.getWebradio());
            }
          } catch (JajukException je) {
            Log.error(je);
          }
        }
        return null;
      }

      @Override
      public void finished() {
        if (!e.getValueIsAdjusting()) {
          sbSearch.hidePopup();
          requestFocusInWindow();
        }
      }
    };
    sw.start();
  }

  /*
   * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
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
    jsVolume.removeChangeListener(CommandJPanel.this);
    jsVolume.removeMouseWheelListener(CommandJPanel.this);
    // if user move the volume slider, unmute
    if (Player.isMuted()) {
      Player.mute(false);
    }
    Player.setVolume(fVolume);
    jsVolume.addChangeListener(CommandJPanel.this);
    jsVolume.addMouseWheelListener(CommandJPanel.this);
    jsVolume.setToolTipText((int) (fVolume * 100) + " %");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(final Event event) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        JajukEvents subject = event.getSubject();
        if (JajukEvents.EVENT_PLAYER_STOP.equals(subject)) {
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          // Enable the play button to allow restarting the queue but disable if
          // the queue is void
          boolean bQueueNotVoid = (FIFO.getFIFO().size() > 0);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(bQueueNotVoid);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(bQueueNotVoid);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(bQueueNotVoid);
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(bQueueNotVoid);
          ActionManager.getAction(NEXT_TRACK).setEnabled(bQueueNotVoid);

          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PLAY);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setName(Messages.getString("JajukWindow.12"));
          ActionManager.getAction(STOP_TRACK).setEnabled(false);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(false);
          jbIncRate.setEnabled(false);
          // Reset history so user can launch again stopped
          // track (selection must change to throw an ActionEvent)
          jcbHistory.setSelectedIndex(-1);
          // reset startup position
          Conf.setProperty(CONF_STARTUP_LAST_POSITION, "0");
        } else if (JajukEvents.EVENT_ZERO.equals(subject)) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(false);
          ActionManager.getAction(NEXT_TRACK).setEnabled(false);
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(false);
          ActionManager.getAction(STOP_TRACK).setEnabled(false);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(false);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(false);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(false);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PAUSE);
          jbIncRate.setEnabled(false);
          // Reset history so user can launch again stopped
          // track (selection must change to throw an ActionEvent)
          jcbHistory.setSelectedIndex(-1);
          // reset startup position
          Conf.setProperty(CONF_STARTUP_LAST_POSITION, "0");
        } else if (JajukEvents.EVENT_PLAYER_PLAY.equals(subject)) {
          jbIncRate.setEnabled(true);
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(REWIND_TRACK).setEnabled(true);
          ActionManager.getAction(PLAY_PAUSE_TRACK).setEnabled(true);
          ActionManager.getAction(STOP_TRACK).setEnabled(true);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_ALBUM).setEnabled(true);
          ActionManager.getAction(PREVIOUS_ALBUM).setEnabled(true);
          ActionManager.getAction(FINISH_ALBUM).setEnabled(true);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PAUSE);
        } else if (JajukEvents.EVENT_PLAYER_PAUSE.equals(subject)) {
          // Disable volume control when pausing to fix an mplayer
          // issue:
          // setting the volume resume the file
          jsVolume.setEnabled(false);
          jsVolume.removeMouseWheelListener(CommandJPanel.this);
          ActionManager.getAction(REWIND_TRACK).setEnabled(false);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PLAY);
        } else if (JajukEvents.EVENT_PLAYER_RESUME.equals(subject)) {
          // Enable the volume when resuming (fix a mplayer issue, see
          // above)
          jsVolume.setEnabled(true);
          jsVolume.addMouseWheelListener(CommandJPanel.this);
          ActionManager.getAction(REWIND_TRACK).setEnabled(true);
          ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
          // We need to set the icon here because the event can be
          // thrown by the information panel, not directly the
          // PlayPauseAction
          ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PAUSE);
        } else if (JajukEvents.EVENT_SPECIAL_MODE.equals(subject)) {
          if (ObservationManager.getDetail(event, DETAIL_ORIGIN).equals(DETAIL_SPECIAL_MODE_NORMAL)) {
            // deselect shuffle mode
            Conf.setProperty(CONF_STATE_SHUFFLE, FALSE);
            JajukJMenuBar.getInstance().setShuffleSelected(false);
            CommandJPanel.getInstance().jbRandom.setSelected(false);
            // computes planned tracks
            FIFO.computesPlanned(true);
          }
        } else if (JajukEvents.EVENT_REPEAT_MODE_STATUS_CHANGED.equals(subject)) {
          if (ObservationManager.getDetail(event, DETAIL_SELECTION).equals(FALSE)) {
            // deselect repeat mode
            Conf.setProperty(CONF_STATE_REPEAT, FALSE);
            JajukJMenuBar.getInstance().setRepeatSelected(false);
            CommandJPanel.getInstance().jbRepeat.setSelected(false);
          }
        } else if (JajukEvents.EVENT_FILE_LAUNCHED.equals(subject)) {
          // Remove history listener, otherwise you'll get a looping
          // event generation
          jcbHistory.removeActionListener(CommandJPanel.this);
          if (jcbHistory.getItemCount() > 0) {
            jcbHistory.setSelectedIndex(0);
          }
          jcbHistory.addActionListener(CommandJPanel.this);
        } else if (JajukEvents.EVENT_CLEAR_HISTORY.equals(event.getSubject())) {
          // clear selection bar (data itself is clear
          // from the model by History class)
          jcbHistory.setSelectedItem(null);
        } else if (JajukEvents.EVENT_VOLUME_CHANGED.equals(event.getSubject())) {
          jsVolume.removeChangeListener(CommandJPanel.this);
          jsVolume.setValue((int) (100 * Player.getCurrentVolume()));
          jsVolume.addChangeListener(CommandJPanel.this);
          jbMute.setSelected(Player.isMuted());
          MuteAction.setVolumeIcon(100 * Player.getCurrentVolume());
        } else if (JajukEvents.EVENT_DJS_CHANGE.equals(event.getSubject())) {
          populateDJs();
          // If no more DJ, change the tooltip
          if (DigitalDJManager.getInstance().getDJs().size() == 0) {
            ActionBase action = ActionManager.getAction(JajukActions.DJ);
            action.setShortDescription(Messages.getString("CommandJPanel.18"));
          }
        } else if (JajukEvents.EVENT_AMBIENCES_CHANGE.equals(event.getSubject())
            || JajukEvents.EVENT_AMBIENCES_SELECTION_CHANGE.equals(event.getSubject())) {
          populateAmbiences();
          updateTooltips();
        } else if (JajukEvents.EVENT_WEBRADIOS_CHANGE.equals(event.getSubject())) {
          populateWebRadios();
        } else if (JajukEvents.EVENT_WEBRADIO_LAUNCHED.equals(event.getSubject())) {
          ActionManager.getAction(PREVIOUS_TRACK).setEnabled(true);
          ActionManager.getAction(NEXT_TRACK).setEnabled(true);
          ActionManager.getAction(STOP_TRACK).setEnabled(true);
          populateWebRadios();
        } else if ((JajukEvents.EVENT_MUTE_STATE.equals(event.getSubject())) &&
        // Update mute icon look when changing the volume
            (!Player.isMuted())) {
          MuteAction.setVolumeIcon(getCurrentVolume());
        }

      }
    });
  }

  /**
   * Update global functions tooltip after a change in ambiences or an ambience
   * selection using the ambience selector
   * 
   */
  private void updateTooltips() {
    // Selected 'Any" ambience
    if (ambiencesCombo.getSelectedIndex() == 1) {
      ActionBase action = ActionManager.getAction(JajukActions.NOVELTIES);
      action.setShortDescription(Messages.getString("JajukWindow.31"));
      action = ActionManager.getAction(JajukActions.BEST_OF);
      action.setShortDescription(Messages.getString("JajukWindow.24"));
      action = ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL);
      action.setShortDescription(Messages.getString("JajukWindow.23"));
    } else {// Selected an ambience
      Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
          ((JLabel) ambiencesCombo.getSelectedItem()).getText());
      ActionBase action = ActionManager.getAction(JajukActions.NOVELTIES);
      action.setShortDescription("<html>" + Messages.getString("JajukWindow.31") + "<p><b>"
          + ambience.getName() + "</b></p></html>");
      action = ActionManager.getAction(JajukActions.SHUFFLE_GLOBAL);
      action.setShortDescription("<html>" + Messages.getString("JajukWindow.23") + "<p><b>"
          + ambience.getName() + "</b></p></html>");
      action = ActionManager.getAction(JajukActions.BEST_OF);
      action.setShortDescription("<html>" + Messages.getString("JajukWindow.24") + "<p><b>"
          + ambience.getName() + "</b></p></html>");
    }
  }

  /**
   * Populate DJs
   * 
   */
  private void populateDJs() {
    try {
      ddbDDJ.setToolTipText("<html>" + Messages.getString("CommandJPanel.18") + "<p><b>"
          + DigitalDJManager.getCurrentDJ() + "</b></html>");
      popupDDJ.removeAll();
      JMenuItem jmiNew = new JMenuItem(ActionManager.getAction(CONFIGURE_DJS));
      popupDDJ.add(jmiNew);
      Iterator<DigitalDJ> it = DigitalDJManager.getInstance().getDJs().iterator();
      while (it.hasNext()) {
        final DigitalDJ dj = it.next();
        JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(dj.getName(),
            IconLoader.ICON_DIGITAL_DJ_16X16);
        jmi.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent arg0) {
            Conf.setProperty(CONF_DEFAULT_DJ, dj.getID());
            DigitalDJManager.setCurrentDJ(dj);
            // force to reselect the item
            populateDJs();
            // update action tooltip on main button with right item
            ActionBase action = ActionManager.getAction(JajukActions.DJ);
            action.setShortDescription("<html>" + Messages.getString("CommandJPanel.18") + "<p><b>"
                + dj.getName() + "</b></p></html>");
          }
        });
        popupDDJ.add(jmi);
        jmi.setSelected(Conf.getString(CONF_DEFAULT_DJ).equals(dj.getID()));
      }
    } catch (Exception e) {
      Log.error(e);
    }
  }

  /**
   * Populate ambiences combo
   * 
   */
  void populateAmbiences() {
    ambiencesCombo.removeActionListener(ambienceListener);
    ItemListener[] il = ambiencesCombo.getItemListeners();
    for (ItemListener element : il) {
      ambiencesCombo.removeItemListener(element);
    }
    ambiencesCombo.removeAllItems();
    ambiencesCombo.addItem(new JLabel(Messages.getString("CommandJPanel.19"),
        IconLoader.ICON_CONFIGURATION, SwingConstants.LEFT));
    ambiencesCombo.addItem(new JLabel("<html><i>" + Messages.getString("DigitalDJWizard.64")
        + "</i></html>", IconLoader.ICON_STYLE, SwingConstants.LEFT));
    // Add available ambiences
    for (final Ambience ambience : AmbienceManager.getInstance().getAmbiences()) {
      ambiencesCombo.addItem(new JLabel(ambience.getName(), IconLoader.ICON_STYLE,
          SwingConstants.LEFT));
    }
    // Select right item
    Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
        Conf.getString(CONF_DEFAULT_AMBIENCE));
    if (defaultAmbience != null) {
      for (int i = 0; i < ambiencesCombo.getItemCount(); i++) {
        if (((JLabel) ambiencesCombo.getItemAt(i)).getText().equals(defaultAmbience.getName())) {
          ambiencesCombo.setSelectedIndex(i);
          break;
        }
      }
    } else {
      // or "any" ambience
      ambiencesCombo.setSelectedIndex(1);
    }
    ambiencesCombo.addActionListener(ambienceListener);
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
      ActionBase actionConf = ActionManager.getAction(JajukActions.CONFIGURE_WEBRADIOS);
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
            Conf.setProperty(CONF_DEFAULT_WEB_RADIO, radio.getName());
            // force to reselect the item
            populateWebRadios();
            // update action tooltip on main button with right item
            ActionBase action = ActionManager.getAction(JajukActions.WEB_RADIO);
            action.setShortDescription("<html>" + Messages.getString("CommandJPanel.25") + "<p><b>"
                + radio.getName() + "</b></p></html>");
          }
        });
        jmi.setSelected(Conf.getString(CONF_DEFAULT_WEB_RADIO).equals(
            radio.getName()));
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
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.getSource() == jsVolume) {
      int iOld = jsVolume.getValue();
      int iNew = iOld - (e.getUnitsToScroll() * 3);
      jsVolume.setValue(iNew);
    } else if (e.getSource() == jpVolume) {
      int oldVolume = (int) (100 * Player.getCurrentVolume());
      int newVolume = oldVolume - (e.getUnitsToScroll() * 3);
      if (Player.isMuted()) {
        Player.mute(false);
      }

      if (newVolume > 100) {
        newVolume = 100;
      } else if (newVolume < 0) {
        newVolume = 0;
      }

      Player.setVolume((float) newVolume / 100);
      jpVolume.setToolTipText(newVolume + " %");
      MuteAction.setVolumeIcon(newVolume);
    }
  }

  public void setRepeatSelected(final boolean b) {
    this.jbRepeat.setSelected(b);
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

  // public void setMuteSelected(final boolean b) {
  // this.jbMute.setSelected(b);
  // }
}
