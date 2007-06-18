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
package org.jajuk.ui;

import static org.jajuk.ui.action.JajukAction.BEST_OF;
import static org.jajuk.ui.action.JajukAction.CONFIGURE_DJS;
import static org.jajuk.ui.action.JajukAction.DECREASE_VOLUME;
import static org.jajuk.ui.action.JajukAction.FAST_FORWARD_TRACK;
import static org.jajuk.ui.action.JajukAction.FINISH_ALBUM;
import static org.jajuk.ui.action.JajukAction.INCREASE_VOLUME;
import static org.jajuk.ui.action.JajukAction.MUTE_STATE;
import static org.jajuk.ui.action.JajukAction.NEXT_ALBUM;
import static org.jajuk.ui.action.JajukAction.NEXT_TRACK;
import static org.jajuk.ui.action.JajukAction.NOVELTIES;
import static org.jajuk.ui.action.JajukAction.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.action.JajukAction.PREVIOUS_ALBUM;
import static org.jajuk.ui.action.JajukAction.PREVIOUS_TRACK;
import static org.jajuk.ui.action.JajukAction.REPEAT_MODE_STATUS_CHANGE;
import static org.jajuk.ui.action.JajukAction.REWIND_TRACK;
import static org.jajuk.ui.action.JajukAction.SHUFFLE_GLOBAL;
import static org.jajuk.ui.action.JajukAction.SHUFFLE_MODE_STATUS_CHANGED;
import static org.jajuk.ui.action.JajukAction.STOP_TRACK;

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.History;
import org.jajuk.base.HistoryItem;
import org.jajuk.base.JajukTimer;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Player;
import org.jajuk.base.SearchResult;
import org.jajuk.base.StackItem;
import org.jajuk.dj.Ambience;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.dj.DigitalDJ;
import org.jajuk.dj.DigitalDJManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.action.ActionBase;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.ActionUtil;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.vlsolutions.swing.toolbars.ToolBarConstraints;
import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarIO;
import com.vlsolutions.swing.toolbars.ToolBarPanel;
import com.vlsolutions.swing.toolbars.VLToolBar;

import ext.DropDownButton;
import ext.SwingWorker;

/**
 * Command panel ( static view )
 * <p>
 * Singleton
 * </p>
 */
public class CommandJPanel extends JXPanel implements ITechnicalStrings, ActionListener,
		ListSelectionListener, ChangeListener, Observer, MouseWheelListener {

	private static final long serialVersionUID = 1L;

	// singleton
	static private CommandJPanel command;

	// Toolbar panel
	ToolBarPanel topPanel;

	// widgets declaration
	SearchBox sbSearch;

	SteppedComboBox jcbHistory;
	
	JButton jbIncRate;

	public JajukToggleButton jbRepeat;

	public JajukToggleButton jbRandom;

	public JajukToggleButton jbContinue;

	public JajukToggleButton jbIntro;

	JToolBar jtbSpecial;

	DropDownButton ddbGlobalRandom;

	JRadioButtonMenuItem jmiShuffleModeSong;

	JRadioButtonMenuItem jmiShuffleModeAlbum;

	JRadioButtonMenuItem jmiShuffleModeAlbum2;

JPopupMenu popupGlobalRandom;

	JajukButton jbBestof;

	DropDownButton ddbNovelties;

	JPopupMenu popupNovelties;

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

	JLabel jlVolume;

	JPanel jpVolume;

	JSlider jsVolume;

	JPanel jpPosition;

	JLabel jlPosition;

	JSlider jsPosition;

	public JajukToggleButton jbMute;

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

	/** Last slider manual move date */
	private static long lDateLastAdjust;

	/** Swing Timer to refresh the component */
	private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			update(new Event(EventSubject.EVENT_HEART_BEAT));
		}
	});

	/** Ambience combo listener */
	class ambienceListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			// Selected 'Any" ambience
			if (ambiencesCombo.getSelectedIndex() == 0) {
				// reset default ambience
				ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE, ""); 
			} else {// Selected an ambience
				Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
						(String) ambiencesCombo.getSelectedItem());
				ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE, ambience.getID());
			}
			ObservationManager.notify(new Event(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE));
		}
	}

	/** An instance of the ambience combo listener */
	ambienceListener ambienceListener;

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
		ToolBarContainer container = Main.getToolbarContainer();
		topPanel = container.getToolBarPanelAt(BorderLayout.NORTH);
		container.setMinimumSize(new Dimension(0,0));
		topPanel.setOpaque(true);
		// Search
		VLToolBar vltbSearch = new VLToolBar("search"); 
		vltbSearch.setMinimumSize(new Dimension(0,0));
		double[][] sizeSearch = new double[][] {
				{ 3, TableLayout.PREFERRED, 3, TableLayout.PREFERRED }, { TableLayout.PREFERRED } };
		JPanel jpSearch = new JPanel(new TableLayout(sizeSearch));
		sbSearch = new SearchBox(CommandJPanel.this);
		jpSearch.add(new JLabel(IconLoader.ICON_SEARCH), "1,0"); 
		jpSearch.add(sbSearch, "3,0"); 
		vltbSearch.add(jpSearch);

		// History
		VLToolBar vltbHistory = new VLToolBar("history"); 
		jcbHistory = new SteppedComboBox();
		ActionBase actionIncRate = ActionManager.getAction(JajukAction.INC_RATE);
		actionIncRate.setName(null);
		jbIncRate = new JButton(actionIncRate);
		vltbHistory.add(jcbHistory);
		vltbHistory.add(jbIncRate);
		// we use a combo box model to make sure we get good performances after
		// rebuilding the entire model like after a refresh
		jcbHistory.setModel(new DefaultComboBoxModel(History.getInstance().getHistory()));
		int iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
		// size of popup
		jcbHistory.setPopupWidth(iWidth);
		// size of the combo itself, keep it! as text can be very long
		jcbHistory.setPreferredSize(new Dimension(300, 25));
		jcbHistory.setToolTipText(Messages.getString("CommandJPanel.0")); 
		jcbHistory.addActionListener(CommandJPanel.this);

		// Mode toolbar
		VLToolBar vltbModes = new VLToolBar("modes"); 
		vltbModes.setOpaque(false);
		vltbModes.setCollapsible(false);
		// we need an inner toolbar to apply size properly
		JToolBar jtbModes = new JToolBar();
		jtbModes.setOpaque(false);
		jtbModes.setBorder(null);
		// make it not floatable as this behavior is managed by vldocking
		jtbModes.setFloatable(false);
		jtbModes.setRollover(true);
		jbRepeat = new JajukToggleButton(ActionManager.getAction(REPEAT_MODE_STATUS_CHANGE));
		jbRepeat.setSelected(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));
		jbRandom = new JajukToggleButton(ActionManager.getAction(SHUFFLE_MODE_STATUS_CHANGED));
		jbRandom.setSelected(ConfigurationManager.getBoolean(CONF_STATE_SHUFFLE));
		jbContinue = new JajukToggleButton(ActionManager
				.getAction(JajukAction.CONTINUE_MODE_STATUS_CHANGED));
		jbContinue.setSelected(ConfigurationManager.getBoolean(CONF_STATE_CONTINUE));
		jbIntro = new JajukToggleButton(ActionManager
				.getAction(JajukAction.INTRO_MODE_STATUS_CHANGED));
		jbIntro.setSelected(ConfigurationManager.getBoolean(CONF_STATE_INTRO));
		jtbModes.add(jbRepeat);
		jtbModes.addSeparator();
		jtbModes.add(jbRandom);
		jtbModes.addSeparator();
		jtbModes.add(jbContinue);
		jtbModes.addSeparator();
		jtbModes.add(jbIntro);
		vltbModes.add(jtbModes);

		// Volume
		VLToolBar vltbVolume = new VLToolBar("volume"); 
		vltbVolume.setOpaque(false);
		jpVolume = new JPanel();
		ActionUtil.installKeystrokes(jpVolume, ActionManager.getAction(DECREASE_VOLUME),
				ActionManager.getAction(INCREASE_VOLUME));

		jpVolume.setLayout(new BoxLayout(jpVolume, BoxLayout.X_AXIS));
		jpVolume.setOpaque(false);
		jlVolume = new JLabel(IconLoader.ICON_VOLUME);
		int iVolume = (int) (100 * ConfigurationManager.getFloat(CONF_VOLUME));
		if (iVolume > 100) { // can occur in some undefined cases
			iVolume = 100;
		}
		jsVolume = new JSlider(0, 100, iVolume);
		jsVolume.setBorder(new DropShadowBorder());
		jpVolume.add(jlVolume);
		jpVolume.add(jsVolume);
		jsVolume.setToolTipText(Messages.getString("CommandJPanel.14")); 
		jsVolume.addChangeListener(CommandJPanel.this);
		jsVolume.addMouseWheelListener(CommandJPanel.this);
		// size of the combo itself
		vltbVolume.add(jpVolume);

		// Position
		VLToolBar vltbPosition = new VLToolBar("position"); 
		vltbPosition.setOpaque(false);
		jpPosition = new JPanel();
		jpPosition.setOpaque(false);
		jpPosition.setLayout(new BoxLayout(jpPosition, BoxLayout.X_AXIS));
		jlPosition = new JLabel(IconLoader.ICON_POSITION);
		jsPosition = new JSlider(0, 100, 0);
		jsPosition.setBorder(new DropShadowBorder());
		jpPosition.add(jlPosition);
		jpPosition.add(jsPosition);
		jsPosition.addChangeListener(CommandJPanel.this);
		jsPosition.setEnabled(false);
		jsPosition.setToolTipText(Messages.getString("CommandJPanel.15")); 
		vltbPosition.add(jpPosition);

		// Special functions toolbar
		VLToolBar vltbSpecial = new VLToolBar("smart"); 
		vltbSpecial.setOpaque(false);
		vltbSpecial.setCollapsible(false);
		// Ambience combo
		ambiencesCombo = new SteppedComboBox();
		iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4);
		ambiencesCombo.setPopupWidth(iWidth);
		ambiencesCombo.setFont(new Font(
				"dialog", Font.BOLD, ConfigurationManager.getInt(CONF_FONTS_SIZE) + 2));
		// size of the combo itself
		//ambiencesCombo.setMaximumSize(new Dimension(100, 20));
		populateAmbiences();
		ambienceListener = new ambienceListener();
		ambiencesCombo.addActionListener(ambienceListener);
		jtbSpecial = new JToolBar();
		jtbSpecial.setOpaque(false);
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
		ddbGlobalRandom.setAction(ActionManager.getAction(SHUFFLE_GLOBAL));
		popupGlobalRandom = new JPopupMenu();
		//Global shuffle
		jmiShuffleModeSong = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.20")); 
		jmiShuffleModeSong.addActionListener(this);
		//album / album
		jmiShuffleModeAlbum = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.21")); 
		jmiShuffleModeAlbum.addActionListener(this);
		//Shuffle album / album
		jmiShuffleModeAlbum2 = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.22")); 
		jmiShuffleModeAlbum2.addActionListener(this);
		if (ConfigurationManager.getProperty(CONF_GLOBAL_RANDOM_MODE).equals(MODE_TRACK)) {
			jmiShuffleModeSong.setSelected(true);
		} else if(ConfigurationManager.getProperty(CONF_GLOBAL_RANDOM_MODE).equals(MODE_ALBUM2)) {
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

		jbBestof = new JajukButton(ActionManager.getAction(BEST_OF));

		ddbNovelties = new DropDownButton(IconLoader.ICON_NOVELTIES) {
			private static final long serialVersionUID = 1L;

			@Override
			protected JPopupMenu getPopupMenu() {
				return popupNovelties;
			}
		};
		ddbNovelties.setAction(ActionManager.getAction(NOVELTIES));
		popupNovelties = new JPopupMenu();
		jmiNoveltiesModeSong = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.20")); 
		jmiNoveltiesModeSong.addActionListener(this);
		jmiNoveltiesModeAlbum = new JRadioButtonMenuItem(Messages.getString("CommandJPanel.21")); 
		jmiNoveltiesModeAlbum.addActionListener(this);
		if (ConfigurationManager.getProperty(CONF_NOVELTIES_MODE).equals(MODE_TRACK)) {
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
		ddbDDJ.setAction(ActionManager.getAction(JajukAction.DJ));
		populateDJs();
		// no text visible
		ddbDDJ.setText(""); 

		jtbSpecial.add(ambiencesCombo);
		jtbSpecial.addSeparator();
		ddbDDJ.addToToolBar(jtbSpecial);
		ddbNovelties.addToToolBar(jtbSpecial);
		ddbGlobalRandom.addToToolBar(jtbSpecial);
		jtbSpecial.add(jbBestof);
		jtbSpecial.add(jbNorm);
		vltbSpecial.add(jtbSpecial);

		// Play toolbar
		VLToolBar vltbPlay = new VLToolBar("player"); 
		vltbPlay.setOpaque(false);
		vltbPlay.setCollapsible(false);
		JToolBar jtbPlay = new JToolBar();
		jtbPlay.setBorder(null);
		jtbPlay.setFloatable(false);
		jtbPlay.setOpaque(false);
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
		jtbPlay.addSeparator();
		jtbPlay.add(jbMute);
		
		// we use a strut as empty borders are now always applied on toolbars
		vltbPlay.add(jtbPlay);

		boolean bToolbarInstallationOK = false; // flag
	
		// Load stored toolbar configuration
		if (Util.getConfFileByPath(FILE_TOOLBARS_CONF).exists()) {
			try {
				// Read toolbars configuration
				container.registerToolBar(vltbSearch);
				container.registerToolBar(vltbHistory);
				container.registerToolBar(vltbModes);
				container.registerToolBar(vltbVolume);
				container.registerToolBar(vltbPosition);
				container.registerToolBar(vltbPlay);
				container.registerToolBar(vltbSpecial);

				// install them from XML
				ToolBarIO tbIO = new ToolBarIO(container);
				FileInputStream in = new FileInputStream(Util.getConfFileByPath(FILE_TOOLBARS_CONF));
				tbIO.readXML(in);
				// Check toolbars have been actually installed as the XML
				// toolbar conf file could be voided
				Component[] panels = container.getComponents();
				int installedToolbars = 0;
				for (int i = 0; i < panels.length; i++) {
					ToolBarPanel panel = (ToolBarPanel) panels[i];
					installedToolbars += panel.getComponentCount();
				}
				if (installedToolbars != container.getRegisteredToolBars().size()) {
					throw new Exception("Wrong number of toolbars");
				}
				bToolbarInstallationOK = true;
				in.close();
			} catch (Exception e) {
				Log.error(e);
				bToolbarInstallationOK = false;
			}
		}

		if (!bToolbarInstallationOK) { // toolbars have not been installed
			topPanel.add(vltbSearch, new ToolBarConstraints(0, 0));
			topPanel.add(vltbHistory, new ToolBarConstraints(0, 1));
			topPanel.add(vltbVolume, new ToolBarConstraints(0, 2));
			topPanel.add(vltbPosition, new ToolBarConstraints(0, 3));
			topPanel.add(vltbPlay, new ToolBarConstraints(1, 0));
			topPanel.add(vltbSpecial, new ToolBarConstraints(1, 1));
			topPanel.add(vltbModes, new ToolBarConstraints(1, 2));

		}

		// register to player events
		ObservationManager.register(CommandJPanel.this);

		// if a track is playing, display right state
		if (FIFO.getInstance().getCurrentFile() != null) {
			// update initial state
			update(new Event(EventSubject.EVENT_PLAYER_PLAY, ObservationManager
					.getDetailsLastOccurence(EventSubject.EVENT_PLAYER_PLAY)));
			// check if some track has been lauched before the view has been
			// displayed
			update(new Event(EventSubject.EVENT_HEART_BEAT));
		}
		// start timer
		timer.start();
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_PLAYER_PLAY);
		eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
		eventSubjectSet.add(EventSubject.EVENT_PLAYER_PAUSE);
		eventSubjectSet.add(EventSubject.EVENT_PLAYER_RESUME);
		eventSubjectSet.add(EventSubject.EVENT_PLAY_ERROR);
		eventSubjectSet.add(EventSubject.EVENT_SPECIAL_MODE);
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		eventSubjectSet.add(EventSubject.EVENT_MUTE_STATE);
		eventSubjectSet.add(EventSubject.EVENT_REPEAT_MODE_STATUS_CHANGED);
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_CLEAR_HISTORY);
		eventSubjectSet.add(EventSubject.EVENT_VOLUME_CHANGED);
		eventSubjectSet.add(EventSubject.EVENT_DJS_CHANGE);
		eventSubjectSet.add(EventSubject.EVENT_AMBIENCES_CHANGE);
		eventSubjectSet.add(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE);
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
					org.jajuk.base.File file = FileManager.getInstance()
							.getFileByID(hi.getFileId());
					if (file != null) {
						try {
							FIFO.getInstance().push(
									new StackItem(file, ConfigurationManager
											.getBoolean(CONF_STATE_REPEAT), true), false);
						} catch (JajukException je) {
							// can be thrown if file is null
						}
					} else {
						Messages.showErrorMessage("120");  
						jcbHistory.setSelectedItem(null);
					}
				}
			} else if (ae.getSource().equals(jmiNoveltiesModeSong)) {
				ConfigurationManager.setProperty(CONF_NOVELTIES_MODE, MODE_TRACK);
			} else if (ae.getSource().equals(jmiNoveltiesModeAlbum)) {
				ConfigurationManager.setProperty(CONF_NOVELTIES_MODE, MODE_ALBUM);
			} else if (ae.getSource().equals(jmiShuffleModeSong)) {
				ConfigurationManager.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_TRACK);
			} else if (ae.getSource().equals(jmiShuffleModeAlbum)) {
				ConfigurationManager.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_ALBUM);
			} else if (ae.getSource().equals(jmiShuffleModeAlbum2)) {
				ConfigurationManager.setProperty(CONF_GLOBAL_RANDOM_MODE, MODE_ALBUM2);
			}
		} catch (Exception e) {
			Log.error(e);
		} finally {
			ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(final ListSelectionEvent e) {
		SwingWorker sw = new SwingWorker() {
			public Object construct() {
				if (!e.getValueIsAdjusting()) {
					SearchResult sr = sbSearch.alResults.get(sbSearch.jlist.getSelectedIndex());
					try {
						FIFO.getInstance().push(
								new StackItem(sr.getFile(), ConfigurationManager
										.getBoolean(CONF_STATE_REPEAT), true), false);
					} catch (JajukException je) {
						Log.error(je);
					}
				}
				return null;
			}

			public void finished() {
				if (!e.getValueIsAdjusting()) {
					sbSearch.popup.hide();
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
	if (e.getSource() == jsVolume && !jsVolume.getValueIsAdjusting()) {
			// this value should be low to make sure we can reach zero
			if (System.currentTimeMillis() - lDateLastAdjust > 20) {
				setVolume((float) jsVolume.getValue() / 100);
				lDateLastAdjust = System.currentTimeMillis();
			}
		} else if (e.getSource() == jsPosition && !jsPosition.getValueIsAdjusting()) {
			lDateLastAdjust = System.currentTimeMillis();
			setPosition((float) jsPosition.getValue() / 100);
		}
	}
	

	/**
	 * Call a seek
	 * 
	 * @param fPosition
	 */
	private void setPosition(final float fPosition) {
		new Thread() {
			public void run() {
				Player.seek(fPosition);
			}
		}.start();
	}

	/**
	 * @return Position value
	 */
	public int getCurrentPosition() {
		return this.jsPosition.getValue();
	}

	/**
	 * @return Volume value
	 */
	public int getCurrentVolume() {
		return this.jsVolume.getValue();
	}

	private void setVolume(final float fVolume) {
		jsVolume.removeChangeListener(CommandJPanel.this);
		jsVolume.removeMouseWheelListener(CommandJPanel.this);
		// if user move the volume slider, unmute
		if (Player.isMuted()) {
			Player.mute(false);
		}
		Player.setVolume(fVolume);
		jsVolume.addChangeListener(CommandJPanel.this);
		jsVolume.addMouseWheelListener(CommandJPanel.this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(final Event event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				EventSubject subject = event.getSubject();
				if (EventSubject.EVENT_PLAYER_STOP.equals(subject)
						|| EventSubject.EVENT_ZERO.equals(subject)) {
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
					jsPosition.setEnabled(false);
					jbIncRate.setEnabled(false);
					jsPosition.removeMouseWheelListener(CommandJPanel.this);
					jsPosition.removeChangeListener(CommandJPanel.this);
					// use set value, not
					// setPosition that would cause
					// a seek that could fail with
					// some formats
					jsPosition.setValue(0);
					// Reset history so user can launch again stopped
					// track (selection must change to throw an ActionEvent)
					jcbHistory.setSelectedIndex(-1);
					// reset startup position
					ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION, "0");
				} else if (EventSubject.EVENT_PLAYER_PLAY.equals(subject)) {
					// remove and re-add listener to make sure not to add it
					// twice
					jsPosition.removeMouseWheelListener(CommandJPanel.this);
					jsPosition.addMouseWheelListener(CommandJPanel.this);
					jsPosition.removeChangeListener(CommandJPanel.this);
					jsPosition.addChangeListener(CommandJPanel.this);
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
					ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PAUSE);
					jsPosition.setEnabled(true);
				} else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)) {
					ActionManager.getAction(REWIND_TRACK).setEnabled(false);
					ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
					ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PLAY);
					jsPosition.setEnabled(false);
					jsPosition.removeMouseWheelListener(CommandJPanel.this);
					jsPosition.removeChangeListener(CommandJPanel.this);
				} else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)) {
					// remove and re-add listener to make sure not to add it
					// twice
					jsPosition.removeMouseWheelListener(CommandJPanel.this);
					jsPosition.addMouseWheelListener(CommandJPanel.this);
					jsPosition.removeChangeListener(CommandJPanel.this);
					jsPosition.addChangeListener(CommandJPanel.this);
					ActionManager.getAction(REWIND_TRACK).setEnabled(true);
					ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
					ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PAUSE);
					jsPosition.setEnabled(true);
				} else if (EventSubject.EVENT_HEART_BEAT.equals(subject) && !FIFO.isStopped()
						&& !Player.isPaused()) {
					// if position is adjusting, no dont disturb user
					if (jsPosition.getValueIsAdjusting() || Player.isSeeking()) {
						return;
					}
					// make sure not to set to old position
					if ((System.currentTimeMillis() - lDateLastAdjust) < 2000) {
						return;
					}
					int iPos = (int) (100 * JajukTimer.getInstance().getCurrentTrackPosition());
					jsPosition.removeChangeListener(CommandJPanel.this);
					jsPosition.removeChangeListener(CommandJPanel.this);
					jsPosition.setValue(iPos);
					jsPosition.addChangeListener(CommandJPanel.this);
				} else if (EventSubject.EVENT_SPECIAL_MODE.equals(subject)) {
					if (ObservationManager.getDetail(event, DETAIL_ORIGIN).equals(
							DETAIL_SPECIAL_MODE_NORMAL)) {
						// deselect shuffle mode
						ConfigurationManager.setProperty(CONF_STATE_SHUFFLE, FALSE);
						JajukJMenuBar.getInstance().jcbmiShuffle.setSelected(false);
						CommandJPanel.getInstance().jbRandom.setSelected(false);
						// computes planned tracks
						FIFO.getInstance().computesPlanned(true);
					}
				} else if (EventSubject.EVENT_REPEAT_MODE_STATUS_CHANGED.equals(subject)) {
					if (ObservationManager.getDetail(event, DETAIL_SELECTION).equals(FALSE)) {
						// deselect repeat mode
						ConfigurationManager.setProperty(CONF_STATE_REPEAT, FALSE);
						JajukJMenuBar.getInstance().jcbmiRepeat.setSelected(false);
						CommandJPanel.getInstance().jbRepeat.setSelected(false);
					}
				} else if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
					// Remove history listener, otherwise you'll get a looping
					// event generation
					jcbHistory.removeActionListener(CommandJPanel.this);
					if (jcbHistory.getItemCount() > 0) {
						jcbHistory.setSelectedIndex(0);
					}
					jcbHistory.addActionListener(CommandJPanel.this);
				} else if (EventSubject.EVENT_CLEAR_HISTORY.equals(event.getSubject())) {
					// clear selection bar (data itself is clear
					// from the model by History class)
					jcbHistory.setSelectedItem(null);
				} else if (EventSubject.EVENT_VOLUME_CHANGED.equals(event.getSubject())) {
					jsVolume.removeChangeListener(CommandJPanel.this);
					jsVolume.setValue((int) (100 * Player.getCurrentVolume()));
					jsVolume.addChangeListener(CommandJPanel.this);
					jbMute.setSelected(false);
				} else if (EventSubject.EVENT_DJS_CHANGE.equals(event.getSubject())) {
					populateDJs();
					// If no more DJ, change the tooltip
					if (DigitalDJManager.getInstance().getDJs().size() == 0) {
						ActionBase action = ActionManager.getAction(JajukAction.DJ);
						action.setShortDescription(Messages.getString("CommandJPanel.18"));    
					}
				} else if (EventSubject.EVENT_AMBIENCES_CHANGE.equals(event.getSubject())
						|| EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE.equals(event.getSubject())) {
					populateAmbiences();
					updateTooltips();
				}
			}
		});
	}

	/**
	 * Update global functions tooltip after a change in ambiences or an
	 * ambience selection using the ambience selector
	 * 
	 */
	private void updateTooltips() {
		// Selected 'Any" ambience
		if (ambiencesCombo.getSelectedIndex() == 0) {
			ActionBase action = ActionManager.getAction(JajukAction.NOVELTIES);
			action.setShortDescription(Messages.getString("JajukWindow.31")); 
			action = ActionManager.getAction(JajukAction.BEST_OF);
			action.setShortDescription(Messages.getString("JajukWindow.24")); 
			action = ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL);
			action.setShortDescription(Messages.getString("JajukWindow.23")); 
		} else {// Selected an ambience
			Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
					(String) ambiencesCombo.getSelectedItem());
			ActionBase action = ActionManager.getAction(JajukAction.NOVELTIES);
			action
					.setShortDescription("<html>" + Messages.getString("JajukWindow.31") + "<p><b>" + ambience.getName() + "</b></p></html>");    
			action = ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL);
			action
					.setShortDescription("<html>" + Messages.getString("JajukWindow.23") + "<p><b>" + ambience.getName() + "</b></p></html>");    
			action = ActionManager.getAction(JajukAction.BEST_OF);
			action
					.setShortDescription("<html>" + Messages.getString("JajukWindow.24") + "<p><b>" + ambience.getName() + "</b></p></html>");    
		}
	}

	/**
	 * Populate DJs
	 * 
	 */
	private void populateDJs() {
		try {
			popupDDJ.removeAll();
			JMenuItem jmiNew = new JMenuItem(ActionManager.getAction(CONFIGURE_DJS)); 
			popupDDJ.add(jmiNew);
			Iterator it = DigitalDJManager.getInstance().getDJs().iterator();
			while (it.hasNext()) {
				final DigitalDJ dj = (DigitalDJ) it.next();
				JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(dj.getName(), IconLoader.ICON_DIGITAL_DJ_16x16);
				jmi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ConfigurationManager.setProperty(CONF_DEFAULT_DJ, dj.getID());
						populateDJs();
						ActionBase action = ActionManager.getAction(JajukAction.DJ);
						action
								.setShortDescription("<html>" + Messages.getString("CommandJPanel.18") + "<p><b>" + dj.getName() + "</b></p></html>");    
					}
				});
				popupDDJ.add(jmi);
				jmi.setSelected(ConfigurationManager.getProperty(CONF_DEFAULT_DJ)
						.equals(dj.getID()));
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
		ambiencesCombo.removeAllItems();
		ambiencesCombo.addItem("<html><i>" + 
				Messages.getString("DigitalDJWizard.64") + "</i></html>");  
		// Add available ambiences
		for (final Ambience ambience : AmbienceManager.getInstance().getAmbiences()) {
			ambiencesCombo.addItem(ambience.getName());
		}
		// Select right item
		ambiencesCombo.setSelectedIndex(0); // Any by default
		// or any other existing ambience
		Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
				ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE));
		if (defaultAmbience != null) {
			ambiencesCombo.setSelectedItem(defaultAmbience.getName());
		}
		ambiencesCombo.setToolTipText(Messages.getString("DigitalDJWizard.66")); 
		ambiencesCombo.addActionListener(ambienceListener);
	}

	/**
	 * ToString() method
	 */
	public String toString() {
		return getClass().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getSource() == jsPosition) {
			int iOld = jsPosition.getValue();
			int iNew = iOld - (e.getUnitsToScroll() * 3);
			jsPosition.setValue(iNew);
		} else if (e.getSource() == jsVolume) {
			int iOld = jsVolume.getValue();
			int iNew = iOld - (e.getUnitsToScroll() * 3);
			jsVolume.setValue(iNew);
		}
	}

}
