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
 * $Revision$
 */

package org.jajuk.ui;

import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Jajuk main window
 * <p>
 * Singleton
 * 
 * @author Bertrand Florat
 * @created 23 mars 2004
 */
public class JajukWindow extends JFrame implements ITechnicalStrings, Observer, ComponentListener {

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
		// mac integration (disable for the moment as users reported issues)
		// System.setProperty( "apple.laf.useScreenMenuBar","true");
		jw = this;
		bVisible = ConfigurationManager.getBoolean(CONF_SHOW_AT_STARTUP, true);
		iMaxWidth = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		iMaxHeight = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		setTitle(Messages.getString("JajukWindow.17")); //$NON-NLS-1$
		setIconImage(Util.getIcon(ICON_LOGO_FRAME).getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// register for given events
		ObservationManager.register(this);
		addWindowListener(new WindowAdapter() {

			public void windowDeiconified(WindowEvent arg0) {
				setFocusableWindowState(true);
			}

			public void windowIconified(WindowEvent arg0) {
				setFocusableWindowState(false);
			}

			public void windowClosing(WindowEvent we) {
				// hide window ASAP
				setVisible(false);
				Main.exit(0);
			}
		});
		
		//Add move/size change listener to save them
		addComponentListener(this);

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
	private void saveSize() {
		String sValue = (int) Math.abs(getLocationOnScreen().getX()) + "," //$NON-NLS-1$
				+ (int) Math.abs(getLocationOnScreen().getY()) + "," //$NON-NLS-1$
				+ (int) getSize().getWidth() + "," + (int) getSize().getHeight();
		ConfigurationManager.setProperty(CONF_WINDOW_POSITION, sValue); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Log.debug("Frame moved or resized, new bounds=" + sValue);
	}

	/**
	 * Apply size and position stored as property
	 * 
	 */
	public void applyStoredSize() {
		// read stored position and size
		String sPosition = ConfigurationManager.getProperty(CONF_WINDOW_POSITION);
		StringTokenizer st = new StringTokenizer(sPosition, ","); //$NON-NLS-1$
		int iX = Integer.parseInt(st.nextToken());
		int iY = Integer.parseInt(st.nextToken());
		int iXsize = Integer.parseInt(st.nextToken());
		if (iXsize == 0) { // if zero, display max size
			iXsize = (int) (Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		}
		int iYsize = Integer.parseInt(st.nextToken());
		if (iYsize == 0) {// if zero, display max size
			iYsize = (int) (Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		}
		setLocation(iX, iY);
		setSize(iXsize, iYsize);
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
			setTitle(Messages.getString("JajukWindow.17")); //$NON-NLS-1$
		}
	}

	/**
	 * @return Returns the bVisible.
	 */
	public boolean isVisible() {
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
					setVisible(true);
					getContentPane().validate();
				}
				// hide
				else {
					if (Main.isNoTaskBar()) {
						// hide the window only if it is explicitely required
						saveSize();
						setVisible(false);
					}
				}
			}

		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {
		saveSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		saveSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
	}

}
