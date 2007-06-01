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

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.log.Log;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Jajuk main window
 * <p>
 * Singleton
 */
public class JajukWindow extends JFrame implements ITechnicalStrings, Observer {

	private static final long serialVersionUID = 1L;

	/** Max width */
	private int iMaxWidth;

	/** Max height */
	private int iMaxHeight;

	/** Self instance */
	private static JajukWindow jw;

	/** Show window at startup? */
	private boolean bVisible = true;

	/**
	 * Get instance
	 * 
	 * @return
	 */
	public static JajukWindow getInstance() {
		if (jw == null) {
			jw = new JajukWindow();
		}
		return jw;
	}

	/**
	 * Constructor
	 */
	public JajukWindow() {
		// mac integration
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		jw = this;
		bVisible = ConfigurationManager.getBoolean(CONF_UI_SHOW_AT_STARTUP, true);
		iMaxWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		iMaxHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		setTitle(Messages.getString("JajukWindow.17"));
		setIconImage(IconLoader.ICON_LOGO_FRAME.getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// register for given events
		ObservationManager.register(this);
		addWindowListener(new WindowAdapter() {

			public void windowDeiconified(WindowEvent arg0) {
				setFocusableWindowState(true);
				bVisible = true;
				toFront();
			}

			public void windowIconified(WindowEvent arg0) {
				setFocusableWindowState(false);
				bVisible = false;
			}

			public void windowClosing(WindowEvent we) {
				// Save windows position
				saveSize();

				// hide window ASAP
				setVisible(false);
				Main.exit(0);
			}
		});

		// display correct title if a track is launched at startup
		update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
				.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		return eventSubjectSet;
	}

	/**
	 * Save current window size and position
	 * 
	 */
	public void saveSize() {
		String sValue = null;
		// If user maximized the frame, store this information and not screen
		// bounds
		// (fix for windows issue: at next startup, the screen is shifted by few
		// pixels)
		if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)
				&& (getExtendedState() & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
			Log.debug("Frame maximized");
			sValue = FRAME_MAXIMIZED;
		} else {
			sValue = (int) getLocationOnScreen().getX() + "," + (int) getLocationOnScreen().getY()
					+ "," + getBounds().width + "," + getBounds().height;
			Log.debug("Frame moved or resized, new bounds=" + sValue);
		}
		// Store the new position
		ConfigurationManager.setProperty(CONF_WINDOW_POSITION, sValue);
	}

	/**
	 * Apply size and position stored as property
	 * 
	 */
	public void applyStoredSize() {
		int iScreenWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		int iScreenHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		int iX = 0;
		int iY = 0;
		int iHorizSize = 0;
		int iVertSize = 0;
		// Forced frame position ?
		String sForcedValue = ConfigurationManager.getProperty(CONF_FRAME_POS_FORCED);
		if (sForcedValue != null && !sForcedValue.trim().equals("")) {
			try {
				StringTokenizer st = new StringTokenizer(sForcedValue, ",");
				iX = Integer.parseInt(st.nextToken());
				iY = Integer.parseInt(st.nextToken());
				iHorizSize = Integer.parseInt(st.nextToken());
				iVertSize = Integer.parseInt(st.nextToken());
				setBounds(iX, iY, iHorizSize, iVertSize);
			} catch (Exception e) {
				// Wrong forced value
				Log.error(e);
				setBounds(60, 60, iScreenWidth - 120, iScreenHeight - 120);
			}
			return;
		}
		// Detect strange or buggy Window Manager like XGL using this test
		// and apply default size for them
		if (!Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
			setBounds(60, 60, iScreenWidth - 120, iScreenHeight - 120);
			return;
		}
		// read stored position and size
		String sPosition = ConfigurationManager.getProperty(CONF_WINDOW_POSITION);
		// If user left jajuk maximized, reset this simple configuration
		if (sPosition.equals(FRAME_MAXIMIZED)) {
			// Always set a size that is used when un-maximalizing the frame
			setBounds(50, 50, iScreenWidth - 100, iScreenHeight - 100);
			if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
				setExtendedState(Frame.MAXIMIZED_BOTH);
			}
			return;
		}
		StringTokenizer st = new StringTokenizer(sPosition, ",");
		iX = Integer.parseInt(st.nextToken());
		// if X position is higher than screen width, set 0
		if (iX > iScreenWidth) {
			iX = 0;
		}
		iY = Integer.parseInt(st.nextToken());
		// if Y position is higher than screen height, set 0
		if (iY > iScreenHeight) {
			iY = 0;
		}
		iHorizSize = Integer.parseInt(st.nextToken());
		// if zero horiz size
		if (iHorizSize <= 0) {
			iHorizSize = (int) (1.5 * iScreenWidth);
		}
		// if zero vertical size
		iVertSize = Integer.parseInt(st.nextToken());
		if (iVertSize <= 0) {
			iVertSize = (int) (1.5 * iScreenHeight);
		}
		// If width > to screen width (switching from a dual to a single head
		// for ie),
		// set max size available (minus some horiz space in case of)
		if (iHorizSize > iScreenWidth) {
			iHorizSize = iScreenWidth - 10;
		}
		if (iVertSize > iScreenHeight) {
			iVertSize = iScreenHeight - 50;
		}
		setLocation(iX, iY);
		setSize(iHorizSize, iVertSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		EventSubject subject = event.getSubject();
		if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)) {
			File file = FIFO.getInstance().getCurrentFile();
			if (file != null) {
				setTitle(file.getTrack().getName());
			}
		} else if (subject.equals(EventSubject.EVENT_ZERO)) {
			setTitle(Messages.getString("JajukWindow.17"));
		}
	}

	/**
	 * @return Returns the bVisible.
	 */
	public boolean isWindowVisible() {
		return bVisible;
	}

	/**
	 * @param visible
	 *            The bVisible to set.
	 */
	public void display(final boolean visible) {
		// start ui if needed
		if (visible && !Main.isUILaunched()) {
			try {
				Main.launchUI();
			} catch (Exception e) {
				Log.error(e);
			}
		}
		// Show or hide the frame
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// store state
				bVisible = visible;
				// show
				if (visible) {
					applyStoredSize();
					//hide and show again is a workaround for a toFront() issue under Metacity
					//see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472274
					setVisible(false);
					toFront();
					setVisible(true);
					setState(Frame.NORMAL);
				}
				// hide
				else {
					// hide the window only if it is explicitely required
					saveSize();
					setVisible(false);
				}
			}
		});
	}
}
