/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

package org.jajuk.ui;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeListener;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.JajukTimer;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Player;
import org.jajuk.dj.Ambience;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.tray.SystemTray;
import org.jdesktop.jdic.tray.TrayIcon;

import ext.SliderMenuItem;

/**
 * Jajuk systray
 * 
 * @author Administrateur
 * @created 22 sept. 2004
 */
public class JajukSystray extends CommandJPanel implements ChangeListener {
	private static final long serialVersionUID = 1L;

	// Systray variables
	SystemTray stray = SystemTray.getDefaultSystemTray();

	TrayIcon trayIcon;

	JPopupMenu jmenu;

	JMenuItem jmiExit;

	JMenuItem jmiMute;

	JMenuItem jmiAbout;

	JMenuItem jmiShuffle;

	JMenuItem jmiBestof;

	JMenuItem jmiDJ;

	JMenuItem jmiNovelties;

	JMenuItem jmiNorm;

	JMenuItem jmiPause;

	JMenuItem jmiStop;

	JMenuItem jmiPrevious;

	JMenuItem jmiNext;

	JLabel jlVolume;

	JLabel jlPosition;

	JMenu jmAmbience;

	long lDateLastAdjust;

	/** Visible at startup? */
	JCheckBoxMenuItem jcbmiVisible;

	/** Self instance singleton */
	private static JajukSystray jsystray;

	/** Swing Timer to refresh the component */
	private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT,
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					update(new Event(EventSubject.EVENT_HEART_BEAT));
				}
			});

	/**
	 * 
	 * @return singleton
	 */
	public static JajukSystray getInstance() {
		if (jsystray == null) {
			jsystray = new JajukSystray();
		}
		return jsystray;
	}

	/**
	 * Reset the systray (useful for language reload)
	 * 
	 */
	public static void dispose() {
		if (jsystray != null) {
			jsystray.closeSystray();
			jsystray = null;
		}
	}

	/**
	 * Systray constructor
	 * 
	 */
	public JajukSystray() {

		jmenu = new JPopupMenu(Messages.getString("JajukWindow.3")); //$NON-NLS-1$

		jmiExit = new JMenuItem(ActionManager.getAction(JajukAction.EXIT));
		jmiMute = new JMenuItem(ActionManager.getAction(JajukAction.MUTE_STATE));
		jmiAbout = new JMenuItem(ActionManager
				.getAction(JajukAction.SHOW_ABOUT));
		jmiShuffle = new JMenuItem(ActionManager
				.getAction(JajukAction.SHUFFLE_GLOBAL));
		jmiBestof = new JMenuItem(ActionManager.getAction(JajukAction.BEST_OF));
		jmiDJ = new JMenuItem(ActionManager.getAction(JajukAction.DJ));
		jmiNorm = new JMenuItem(ActionManager
				.getAction(JajukAction.FINISH_ALBUM));
		jmiNovelties = new JMenuItem(ActionManager
				.getAction(JajukAction.NOVELTIES));

		jcbmiVisible = new JCheckBoxMenuItem(Messages
				.getString("JajukWindow.8")); //$NON-NLS-1$
		jcbmiVisible.setState(JajukWindow.getInstance().isVisible());
		jcbmiVisible.addActionListener(this);
		jcbmiVisible.setToolTipText(Messages.getString("JajukWindow.25")); //$NON-NLS-1$

		jmiPause = new JMenuItem(ActionManager
				.getAction(JajukAction.PLAY_PAUSE_TRACK));
		jmiStop = new JMenuItem(ActionManager.getAction(JajukAction.STOP_TRACK));
		jmiPrevious = new JMenuItem(ActionManager
				.getAction(JajukAction.PREVIOUS_TRACK));
		jmiNext = new JMenuItem(ActionManager.getAction(JajukAction.NEXT_TRACK));

		jlPosition = new JLabel(Util.getIcon(ICON_POSITION));
		String sTitle = Messages.getString("JajukWindow.34"); //$NON-NLS-1$
		jsPosition = new SliderMenuItem(0, 100, 0, sTitle);
		jsPosition.setToolTipText(Messages.getString("CommandJPanel.15")); //$NON-NLS-1$
		jsPosition.addMouseWheelListener(this);
		jsPosition.addChangeListener(this);

		/**
		 * Important: due to a bug probably in swing or jdic, we have to add a
		 * jmenuitem in the popup menu and not the panel itself, otherwise no
		 * action event occurs
		 */

		jlVolume = new JLabel(Util.getIcon(ICON_VOLUME));
		sTitle = Messages.getString("JajukWindow.33"); //$NON-NLS-1$
		int iVolume = (int) (100 * ConfigurationManager.getFloat(CONF_VOLUME));
		if (iVolume > 100) { // can occur in some undefined cases
			iVolume = 100;
		}
		jsVolume = new SliderMenuItem(0, 100, iVolume, sTitle);
		jsVolume.setToolTipText(sTitle); //$NON-NLS-1$
		jsVolume.addMouseWheelListener(this);
		jsVolume.addChangeListener(this);

		// Ambiences menu
		Ambience defaultAmbience = AmbienceManager.getInstance()
				.getDefaultAmbience();
		jmAmbience = new JMenu(Messages.getString("JajukWindow.36")
				+ " "
				+ ((defaultAmbience == null) ? Messages
						.getString("DigitalDJWizard.64") : defaultAmbience
						.getName()));
		populateAmbiences();

		jmenu.add(new JLabel(Util.getIcon(IMAGE_TRAY_TITLE)));
		jmenu.addSeparator();
		jmenu.add(jmAmbience);
		jmenu.addSeparator();
		jmenu.add(jcbmiVisible);
		jmenu.addSeparator();
		jmenu.add(jmiPause);
		jmenu.add(jmiStop);
		jmenu.add(jmiPrevious);
		jmenu.add(jmiNext);
		jmenu.addSeparator();
		jmenu.add(jmiShuffle);
		jmenu.add(jmiBestof);
		jmenu.add(jmiDJ);
		jmenu.add(jmiNovelties);
		jmenu.add(jmiNorm);
		jmenu.addSeparator();
		jmenu.add(jmiAbout);
		jmenu.addSeparator();
		jmenu.add(jmiMute);
		jmenu.addSeparator();
		jmenu.add(jsPosition);
		jmenu.add(jsVolume);
		jmenu.addSeparator();
		jmenu.add(jmiExit);
		jmenu.add(new JMenuItem(" ")); // used to close the tray
		trayIcon = new TrayIcon(Util.getIcon(ICON_TRAY), Messages
				.getString("JajukWindow.18"), jmenu); //$NON-NLS-1$);
		trayIcon.setIconAutoSize(true);
		trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// hide menu if opened
				if (jmenu != null && jmenu.isVisible()) {
					jmenu.setVisible(false);
				}
				// show window if it is not visible and hide it if it is visible
				if (!JajukWindow.getInstance().isVisible()) {
					JajukWindow.getInstance().setShown(true);
				} else {
					JajukWindow.getInstance().setShown(false);
				}
			}
		});
		stray.addTrayIcon(trayIcon);
		// start timer
		timer.start();
		// Register needed events
		ObservationManager.register(this);

		// check if a file has been already started
		if (FIFO.getInstance().getCurrentFile() == null) {
			update(new Event(EventSubject.EVENT_PLAYER_STOP, ObservationManager
					.getDetailsLastOccurence(EventSubject.EVENT_PLAYER_STOP)));
		} else {
			update(new Event(
					EventSubject.EVENT_FILE_LAUNCHED,
					ObservationManager
							.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
		}
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_PLAYER_PAUSE);
		eventSubjectSet.add(EventSubject.EVENT_PLAYER_PLAY);
		eventSubjectSet.add(EventSubject.EVENT_PLAYER_RESUME);
		eventSubjectSet.add(EventSubject.EVENT_PLAYER_STOP);
		eventSubjectSet.add(EventSubject.EVENT_MUTE_STATE);
		eventSubjectSet.add(EventSubject.EVENT_HEART_BEAT);
		eventSubjectSet.add(EventSubject.EVENT_VOLUME_CHANGED);
		eventSubjectSet.add(EventSubject.EVENT_AMBIENCES_CHANGE);
		eventSubjectSet.add(EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE);
		return eventSubjectSet;
	}

	public JPopupMenu getPopup() {
		return jmenu;
	}

	/**
	 * ActionListener
	 */
	public void actionPerformed(final ActionEvent e) {
		// do not run this in a separate thread because Player actions would die
		// with the thread
		try {
			if (e.getSource() == jcbmiVisible) {
				ConfigurationManager.setProperty(CONF_SHOW_AT_STARTUP, Boolean
						.toString(jcbmiVisible.getState()));
			}
		} catch (Exception e2) {
			Log.error(e2);
		} finally {
			ObservationManager.notify(new Event(
					EventSubject.EVENT_PLAYLIST_REFRESH)); // refresh
			// playlist
			// editor
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(final Event event) {
		if (jsystray == null) { // test if the systray is visible
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				EventSubject subject = event.getSubject();
				if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
					// remove and re-add listener to make sure not to add it
					// twice
					jsPosition.removeMouseWheelListener(JajukSystray.this);
					jsPosition.addMouseWheelListener(JajukSystray.this);
					jsPosition.removeChangeListener(JajukSystray.this);
					jsPosition.addChangeListener(JajukSystray.this);
					jsPosition.setEnabled(true);
					String sID = (String) ObservationManager.getDetail(event,
							DETAIL_CURRENT_FILE_ID);
					if (sID == null) {
						return;
					}
					File file = FileManager.getInstance().getFileByID(
							(String) ObservationManager.getDetail(event,
									DETAIL_CURRENT_FILE_ID));
					String sOut = ""; //$NON-NLS-1$
					if (file != null) {
						String sAuthor = file.getTrack().getAuthor().getName();
						if (!sAuthor.equals(UNKNOWN_AUTHOR)) {
							sOut += sAuthor + " / "; //$NON-NLS-1$
						}
						String sAlbum = file.getTrack().getAlbum().getName();
						if (!sAlbum.equals(UNKNOWN_ALBUM)) {
							sOut += sAlbum + " / "; //$NON-NLS-1$
						}
						sOut += file.getTrack().getName();
						if (ConfigurationManager
								.getBoolean(CONF_OPTIONS_SHOW_POPUP)) {
							trayIcon
									.displayMessage(
											Messages
													.getString("JajukWindow.35"), sOut, TrayIcon.INFO_MESSAGE_TYPE); //$NON-NLS-1$
						}
					} else {
						sOut = Messages.getString("JajukWindow.18"); //$NON-NLS-1$
					}
					trayIcon.setToolTip(sOut);
				} else if (EventSubject.EVENT_PLAYER_STOP.equals(subject)
						|| EventSubject.EVENT_ZERO.equals(subject)) {
					trayIcon.setToolTip(Messages.getString("JajukWindow.18")); //$NON-NLS-1$
					jmiPause.setEnabled(false);
					jmiStop.setEnabled(false);
					jmiNext.setEnabled(false);
					jmiPrevious.setEnabled(false);
					jsPosition.removeMouseWheelListener(JajukSystray.this);
					jsPosition.removeChangeListener(JajukSystray.this);
					jsPosition.setEnabled(false);
					jsPosition.setValue(0);
					jmiNorm.setEnabled(false);
				} else if (EventSubject.EVENT_PLAYER_PLAY.equals(subject)) {
					jsPosition.removeMouseWheelListener(JajukSystray.this);
					jsPosition.addMouseWheelListener(JajukSystray.this);
					jsPosition.removeChangeListener(JajukSystray.this);
					jsPosition.addChangeListener(JajukSystray.this);
					jsPosition.setEnabled(true);
					jmiPause.setEnabled(true);
					jmiStop.setEnabled(true);
					jmiNext.setEnabled(true);
					jmiPrevious.setEnabled(true);
					jmiNorm.setEnabled(true);
				} else if (EventSubject.EVENT_PLAYER_PAUSE.equals(subject)) {
					jsPosition.removeMouseWheelListener(JajukSystray.this);
					jsPosition.removeChangeListener(JajukSystray.this);
					jsPosition.setEnabled(false);
				} else if (EventSubject.EVENT_PLAYER_RESUME.equals(subject)) {
					JajukSystray.super.update(event);
				} else if (EventSubject.EVENT_VOLUME_CHANGED.equals(event
						.getSubject())) {
					JajukSystray.super.update(event);
				} else if (EventSubject.EVENT_HEART_BEAT.equals(subject)
						&& !FIFO.isStopped() && !Player.isPaused()) {
					JajukSystray.super.update(event);
				} else if (EventSubject.EVENT_AMBIENCES_CHANGE.equals(subject)
						|| EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE
								.equals(subject)) {
					Ambience ambience = AmbienceManager.getInstance()
							.getDefaultAmbience();
					if (ambience != null) {
						jmAmbience.setText(Messages.getString("JajukWindow.36")
								+ " "
								+ AmbienceManager.getInstance()
										.getDefaultAmbience().getName());
					} else {
						jmAmbience
								.setText(Messages.getString("JajukWindow.37"));
					}
					populateAmbiences();
				}

			}

		});
	}

	/**
	 * Hide systray
	 */
	public void closeSystray() {
		if (stray != null && trayIcon != null) {
			stray.removeTrayIcon(trayIcon);
		}
	}

	/**
	 * Populate ambiences
	 * 
	 */
	void populateAmbiences() {
		// Ambience selection listener
		ActionListener al = new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				JMenuItem jmi = (JMenuItem) ae.getSource();
				// Selected 'Any" ambience
				JMenuItem all = jmAmbience.getItem(0);
				if (jmi.equals(all)) {
					// reset default ambience
					ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE, ""); //$NON-NLS-1$
				} else {// Selected an ambience
					Ambience ambience = AmbienceManager.getInstance()
							.getAmbienceByName(jmi.getActionCommand());
					ConfigurationManager.setProperty(CONF_DEFAULT_AMBIENCE,
							ambience.getID());
				}
				jmi.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
				ObservationManager.notify(new Event(
						EventSubject.EVENT_AMBIENCES_SELECTION_CHANGE));
			}
		};
		// Remove all item
		jmAmbience.removeAll();
		// Add "all" ambience
		JMenuItem jmiAll = new JMenuItem("<html><i>" + //$NON-NLS-1$
				Messages.getString("DigitalDJWizard.64") + "</i></html>");
		jmiAll.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
		jmiAll.addActionListener(al);
		jmAmbience.add(jmiAll);

		// Add available ambiences
		for (Ambience ambience : AmbienceManager.getInstance().getAmbiences()) {
			JMenuItem jmi = new JMenuItem(ambience.getName());
			if (ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE).equals(
					ambience.getID())) {
				jmiAll.setFont(new Font("Dialog", Font.PLAIN, 12)); //$NON-NLS-1$
				jmi.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
			}
			jmi.addActionListener(al);
			jmAmbience.add(jmi);
		}
	}

}
