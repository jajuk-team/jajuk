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

import static org.jajuk.ui.action.JajukAction.CONFIGURE_DJS;
import static org.jajuk.ui.action.JajukAction.FAST_FORWARD_TRACK;
import static org.jajuk.ui.action.JajukAction.FINISH_ALBUM;
import static org.jajuk.ui.action.JajukAction.MUTE_STATE;
import static org.jajuk.ui.action.JajukAction.NEXT_ALBUM;
import static org.jajuk.ui.action.JajukAction.NEXT_TRACK;
import static org.jajuk.ui.action.JajukAction.PLAY_PAUSE_TRACK;
import static org.jajuk.ui.action.JajukAction.PREVIOUS_ALBUM;
import static org.jajuk.ui.action.JajukAction.PREVIOUS_TRACK;
import static org.jajuk.ui.action.JajukAction.REWIND_TRACK;
import static org.jajuk.ui.action.JajukAction.STOP_TRACK;

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
import org.jajuk.ui.wizard.AmbienceWizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.debug.FormDebugUtils;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.vlsolutions.swing.toolbars.ToolBarPanel;

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

	DropDownButton jbIncRate;

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

	/** Swing Timer to refresh the component */
	private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			update(new Event(EventSubject.EVENT_HEART_BEAT));
		}
	});

	/** Ambience combo listener */
	class ambienceListener implements ActionListener {
		public void actionPerformed(ActionEvent ae) {
			// Ambience Configuration
			if (ambiencesCombo.getSelectedIndex() == 0) {
				// display the wizard
				AmbienceWizard ambience = new AmbienceWizard();
				ambience.show();
				// Reset combo to last selected item
				ambiencesCombo.removeActionListener(ambienceListener);
				Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
						ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE));
				if (defaultAmbience != null) {
					for (int i = 0; i < ambiencesCombo.getItemCount(); i++) {
						if (((JLabel) ambiencesCombo.getItemAt(i)).getText().equals(
								defaultAmbience.getName())) {
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
				ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE, "");
				ObservationManager.notify(new Event(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE));
			} else {// Selected an ambience
				Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
						((JLabel) ambiencesCombo.getSelectedItem()).getText());
				ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE, ambience.getID());
				ObservationManager.notify(new Event(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE));
			}
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
		// Search
		double[][] sizeSearch = new double[][] { { 3, TableLayout.PREFERRED, 3, 100 },
				{ TableLayout.PREFERRED } };
		JPanel jpSearch = new JPanel(new TableLayout(sizeSearch));
		sbSearch = new SearchBox(CommandJPanel.this);
		jpSearch.add(new JLabel(IconLoader.ICON_SEARCH), "1,0");
		jpSearch.add(sbSearch, "3,0");

		// History
		JPanel jpHistory = new JPanel();
		jpHistory.setLayout(new BoxLayout(jpHistory, BoxLayout.X_AXIS));
		jcbHistory = new SteppedComboBox();
		// - Increase rating button
		ActionBase actionIncRate = ActionManager.getAction(JajukAction.INC_RATE);
		actionIncRate.setName(null);
		final JPopupMenu jpmIncRating = new JPopupMenu();
		for (int i = 1; i <= 10; i++) {
			final int j = i;
			JMenuItem jmi = new JMenuItem("+" + i);
			if (ConfigurationManager.getInt(CONF_INC_RATING) == i) {
				jmi.setFont(new Font("dialog", Font.BOLD, ConfigurationManager
						.getInt(CONF_FONTS_SIZE)));
			}
			// Store selected value
			jmi.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ConfigurationManager.setProperty(CONF_INC_RATING, "" + j);
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
		int iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2);
		// size of popup
		jcbHistory.setPopupWidth(iWidth);
		// size of the combo itself, keep it! as text can be very long
		jcbHistory.setPreferredSize(new Dimension(250, 25));
		jcbHistory.setMinimumSize(new Dimension(0, 25));
		jcbHistory.setToolTipText(Messages.getString("CommandJPanel.0"));
		jpHistory.add(jcbHistory);
		JToolBar jtbIncRate = new JToolBar();
		jtbIncRate.setFloatable(false);
		jpHistory.add(Box.createHorizontalStrut(10));
		jbIncRate.addToToolBar(jtbIncRate);
		jpHistory.add(jtbIncRate);

		// Mode toolbar
		// we need an inner toolbar to apply size properly
		JToolBar jtbModes = new JToolBar();
		jtbModes.setBorder(null);
		// make it not floatable as this behavior is managed by vldocking
		jtbModes.setFloatable(false);
		jtbModes.setRollover(true);
		jbRepeat = new JajukToggleButton(ActionManager.getAction(JajukAction.REPEAT_MODE_STATUS_CHANGE));
		jbRepeat.setSelected(ConfigurationManager.getBoolean(CONF_STATE_REPEAT));
		jbRandom = new JajukToggleButton(ActionManager.getAction(JajukAction.SHUFFLE_MODE_STATUS_CHANGED));
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

		// Volume
		jpVolume = new JPanel();
		ActionUtil.installKeystrokes(jpVolume, ActionManager.getAction(JajukAction.DECREASE_VOLUME),
				ActionManager.getAction(JajukAction.INCREASE_VOLUME));

		jpVolume.setLayout(new BoxLayout(jpVolume, BoxLayout.X_AXIS));
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

		// Special functions toolbar
		// Ambience combo
		ambiencesCombo = new SteppedComboBox();
		iWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 4);
		ambiencesCombo.setPopupWidth(iWidth);
		ambiencesCombo.setFont(new Font("dialog", Font.BOLD, ConfigurationManager
				.getInt(CONF_FONTS_SIZE) + 2));
		// size of the combo itself
		// ambiencesCombo.setMaximumSize(new Dimension(100, 32));
		ambiencesCombo.setRenderer(new BasicComboBoxRenderer() {
			private static final long serialVersionUID = -6943363556191659895L;

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
		ambienceListener = new ambienceListener();
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
		ddbGlobalRandom.setAction(ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL));
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
		if (ConfigurationManager.getProperty(CONF_GLOBAL_RANDOM_MODE).equals(MODE_TRACK)) {
			jmiShuffleModeSong.setSelected(true);
		} else if (ConfigurationManager.getProperty(CONF_GLOBAL_RANDOM_MODE).equals(MODE_ALBUM2)) {
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

		jbBestof = new JajukButton(ActionManager.getAction(JajukAction.BEST_OF));

		ddbNovelties = new DropDownButton(IconLoader.ICON_NOVELTIES) {
			private static final long serialVersionUID = 1L;

			@Override
			protected JPopupMenu getPopupMenu() {
				return popupNovelties;
			}
		};
		ddbNovelties.setAction(ActionManager.getAction(JajukAction.NOVELTIES));
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

		jtbSpecial.addSeparator();
		ddbDDJ.addToToolBar(jtbSpecial);
		ddbNovelties.addToToolBar(jtbSpecial);
		ddbGlobalRandom.addToToolBar(jtbSpecial);
		jtbSpecial.add(jbBestof);
		jtbSpecial.add(jbNorm);

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
		jtbPlay.addSeparator();
		jtbPlay.add(jbMute);

		// Add items
		FormLayout layout = new FormLayout(
		// --columns
				"left:min(10dlu;p):grow, 0dlu, " + // ambience
						"left:p, 1dlu" + // smart toolbar
						", min(0dlu;p):grow(0.75), 1dlu," + // glue
						" right:p, 3dlu, " + // search /modes
						"fill:p:grow(0.25), 1dlu, " + // history / player
						"right:min(30dlu;p):grow", // volume / part of history
				// --rows
				"0dlu, p, 0dlu, p, 0dlu"); // rows
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		// Add items
		builder.add(ambiencesCombo, cc.xy(1, 4));
		builder.add(jtbSpecial, cc.xy(3, 4));
		builder.add(jpSearch, cc.xy(7, 2));
		builder.add(jpHistory, cc.xyw(9, 2, 3));
		builder.add(jtbModes, cc.xy(7, 4));
		builder.add(jtbPlay, cc.xy(9, 4));
		builder.add(jpVolume, cc.xy(11, 4));
		JPanel p = builder.getPanel();
		FormDebugUtils.dumpAll(builder.getPanel());
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		add(p);
		// register to player events
		ObservationManager.register(CommandJPanel.this);

		// if a track is playing, display right state
		if (FIFO.getInstance().getCurrentFile() != null) {
			// update initial state
			update(new Event(EventSubject.EVENT_PLAYER_PLAY, ObservationManager
					.getDetailsLastOccurence(EventSubject.EVENT_PLAYER_PLAY)));
			// check if some track has been launched before the view has been
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
						Messages.showErrorMessage(120);
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
			setVolume((float) jsVolume.getValue() / 100);
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
					jbIncRate.setEnabled(false);
					// Reset history so user can launch again stopped
					// track (selection must change to throw an ActionEvent)
					jcbHistory.setSelectedIndex(-1);
					// reset startup position
					ConfigurationManager.setProperty(CONF_STARTUP_LAST_POSITION, "0");
				} else if (EventSubject.EVENT_PLAYER_PLAY.equals(subject)) {
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
				} else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)) {
					ActionManager.getAction(REWIND_TRACK).setEnabled(false);
					ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(false);
					ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PLAY);
				} else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)) {
					ActionManager.getAction(REWIND_TRACK).setEnabled(true);
					ActionManager.getAction(FAST_FORWARD_TRACK).setEnabled(true);
					ActionManager.getAction(PLAY_PAUSE_TRACK).setIcon(IconLoader.ICON_PAUSE);
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
					System.out.println("1");
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
		if (ambiencesCombo.getSelectedIndex() == 1) {
			ActionBase action = ActionManager.getAction(JajukAction.NOVELTIES);
			action.setShortDescription(Messages.getString("JajukWindow.31"));
			action = ActionManager.getAction(JajukAction.BEST_OF);
			action.setShortDescription(Messages.getString("JajukWindow.24"));
			action = ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL);
			action.setShortDescription(Messages.getString("JajukWindow.23"));
		} else {// Selected an ambience
			Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(
					((JLabel) ambiencesCombo.getSelectedItem()).getText());
			ActionBase action = ActionManager.getAction(JajukAction.NOVELTIES);
			action.setShortDescription("<html>" + Messages.getString("JajukWindow.31") + "<p><b>"
					+ ambience.getName() + "</b></p></html>");
			action = ActionManager.getAction(JajukAction.SHUFFLE_GLOBAL);
			action.setShortDescription("<html>" + Messages.getString("JajukWindow.23") + "<p><b>"
					+ ambience.getName() + "</b></p></html>");
			action = ActionManager.getAction(JajukAction.BEST_OF);
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
			popupDDJ.removeAll();
			JMenuItem jmiNew = new JMenuItem(ActionManager.getAction(CONFIGURE_DJS));
			popupDDJ.add(jmiNew);
			Iterator it = DigitalDJManager.getInstance().getDJs().iterator();
			while (it.hasNext()) {
				final DigitalDJ dj = (DigitalDJ) it.next();
				JCheckBoxMenuItem jmi = new JCheckBoxMenuItem(dj.getName(),
						IconLoader.ICON_DIGITAL_DJ_16x16);
				jmi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ConfigurationManager.setProperty(CONF_DEFAULT_DJ, dj.getID());
						populateDJs();
						ActionBase action = ActionManager.getAction(JajukAction.DJ);
						action.setShortDescription("<html>"
								+ Messages.getString("CommandJPanel.18") + "<p><b>" + dj.getName()
								+ "</b></p></html>");
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
		ambiencesCombo.setSelectedIndex(1); // Any by default
		// or any other existing ambience
		Ambience defaultAmbience = AmbienceManager.getInstance().getAmbience(
				ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE));
		if (defaultAmbience != null) {
			for (int i = 0; i < ambiencesCombo.getItemCount(); i++) {
				if (((JLabel) ambiencesCombo.getItemAt(i)).getText().equals(
						defaultAmbience.getName())) {
					ambiencesCombo.setSelectedIndex(i);
					break;
				}
			}
		}
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
		if (e.getSource() == jsVolume) {
			int iOld = jsVolume.getValue();
			int iNew = iOld - (e.getUnitsToScroll() * 3);
			jsVolume.setValue(iNew);
		}
	}

}
