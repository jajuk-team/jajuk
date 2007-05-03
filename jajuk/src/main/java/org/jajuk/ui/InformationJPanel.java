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
 *  $Revision$
 */
package org.jajuk.ui;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.JajukTimer;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Player;
import org.jajuk.i18n.Messages;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Status / information panel ( static view )
 */
public class InformationJPanel extends JPanel implements ITechnicalStrings, Observer {
	private static final long serialVersionUID = 1L;

	// consts
	/** Informative message type ( displayed in blue ) * */
	public static final int INFORMATIVE = 0;

	/** Informative message type ( displayed in red )* */
	public static final int ERROR = 1;

	/** Warning message type ( displayed in orange )* */
	public static final int WARNING = 2;

	/** Self instance */
	static private InformationJPanel ijp = null;

	/** Swing Timer to refresh the component */
	private Timer timer = new Timer(JajukTimer.DEFAULT_HEARTBEAT, new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			update(new Event(EventSubject.EVENT_HEART_BEAT));
		}
	});

	/**
	 * Singleton access
	 * 
	 * @return
	 */
	public static synchronized InformationJPanel getInstance() {
		if (ijp == null) {
			ijp = new InformationJPanel();
		}
		return ijp;
	}

	// widgets declaration
	JLabel jlMessage;

	JLabel jlSelection;

	JPanel jpTotal;

	JLabel jlTotal;

	JPanel jpCurrent;

	JProgressBar jpbCurrent;

	JLabel jlCurrent;

	// attributes
	String sMessage;

	/** Current message type */
	int iType = 0;

	String sSelection;

	int iTotalStatus;

	String sTotalStatus;

	int iCurrentStatus;

	String sCurrentStatus;

	private InformationJPanel() {
		// dimensions
		// set current jpanel properties
		double size[][] = { { 0.44, 0.13, 0.10, 0.33 }, { 20 } };
		setLayout(new TableLayout(size));

		// message bar
		jlMessage = new JLabel();
		jlMessage.setOpaque(true);
		setMessage(Messages.getString("JajukWindow.18"), InformationJPanel.INFORMATIVE); //$NON-NLS-1$
		jlMessage.setBorder(Util.getShadowBorder());

		// selection bar
		jlSelection = new JLabel();
		jlSelection.setBorder(Util.getShadowBorder());

		// total progress bar
		jpTotal = new JPanel();
		jpTotal.setToolTipText(Messages.getString("InformationJPanel.5")); //$NON-NLS-1$
		jpTotal.setLayout(new BoxLayout(jpTotal, BoxLayout.X_AXIS));
		jpTotal.setBorder(Util.getShadowBorder());
		jlTotal = new JLabel();
		jpTotal.add(jlTotal);
		jpTotal.add(Box.createHorizontalStrut(3));

		// current progress bar
		jpCurrent = new JPanel();
		jpCurrent.setToolTipText(Messages.getString("InformationJPanel.7")); //$NON-NLS-1$
		jpCurrent.setLayout(new BoxLayout(jpCurrent, BoxLayout.X_AXIS));
		jpCurrent.setBorder(Util.getShadowBorder());
		jpbCurrent = new JProgressBar(0, 100);
		jpbCurrent.setStringPainted(true);
		jlCurrent = new JLabel();
		jpCurrent.add(jlCurrent);
		jpCurrent.add(Box.createHorizontalStrut(6));
		jpCurrent.add(jpbCurrent);

		// add widgets
		add(jlMessage, "0,0"); //$NON-NLS-1$
		add(jlSelection, "1,0"); //$NON-NLS-1$
		add(jpTotal, "2,0"); //$NON-NLS-1$
		add(jpCurrent, "3,0"); //$NON-NLS-1$

		// check if some track has been lauched before the view has been
		// displayed
		update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
				.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
		// check if some errors occured before the view has been displayed
		if (ObservationManager.containsEvent(EventSubject.EVENT_PLAY_ERROR)) {
			update(new Event(EventSubject.EVENT_PLAY_ERROR, ObservationManager
					.getDetailsLastOccurence(EventSubject.EVENT_PLAY_ERROR)));
		}
		// register for given events
		ObservationManager.register(this);
		// start timer
		timer.start();
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_PLAY_ERROR);
		return eventSubjectSet;
	}

	/**
	 * @return
	 */
	public int getCurrentStatus() {
		return iCurrentStatus;
	}

	/**
	 * @return
	 */
	public int getTotalTime() {
		return iTotalStatus;
	}

	/**
	 * @return
	 */
	public String getMessage() {
		return sMessage;
	}

	/**
	 * @return
	 */
	public String getSelection() {
		return this.sSelection;
	}

	/**
	 * @param i
	 */
	public void setCurrentTime(int i) {
		iCurrentStatus = i;
		jpbCurrent.setValue(i);
	}

	/**
	 * @param label
	 */
	public void setMessage(final String sMessage, final int iMessageType) {
		this.sMessage = sMessage;
		this.iType = iMessageType;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				InformationJPanel.this.sMessage = sMessage;
				switch (iMessageType) {
				case INFORMATIVE:
					jlMessage.setForeground(Color.BLUE);
					break;
				case ERROR:
					jlMessage.setForeground(Color.RED);
					break;
				case WARNING:
					jlMessage.setForeground(new Color(255, 80, 0));
					break;
				default:
					jlMessage.setForeground(Color.BLUE);
					break;
				}
				jlMessage.setText(sMessage);
				jlMessage.setToolTipText(sMessage);
			}
		});
	}

	/**
	 * @param label
	 */
	public void setSelection(String sSelection) {
		this.sSelection = sSelection;
		jlSelection.setText(sSelection);
		jlSelection.setToolTipText(sSelection);
	}

	/**
	 * @return
	 */
	public String getCurrentStatusMessage() {
		return sCurrentStatus;
	}

	/**
	 * @return
	 */
	public String getTotalTimeMessage() {
		return sTotalStatus;
	}

	/**
	 * 
	 * Set the current status for current track ex : 01:01:01/02:02:02
	 * 
	 * @param string
	 */
	public void setCurrentTimeMessage(String string) {
		sCurrentStatus = string;
		jlCurrent.setText(string);
	}

	/**
	 * @param string
	 */
	public void setTotalTimeMessage(String string) {
		sTotalStatus = string;
		jlTotal.setText(string);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public synchronized void update(final Event event) {
		// we synchronize this method to make error message is visible all 2
		// secs
		final EventSubject subject = event.getSubject();
		// do not insert this subject inside the invokeLater because we have to
		// leave the awt dispatcher called inside the setMessage and THEN, sleep
		// for 2 secs.
		if (EventSubject.EVENT_PLAY_ERROR.equals(subject)) {
			try {
				// reset data
				setCurrentTimeMessage(Util.formatTimeBySec(0, false)
						+ " / " + Util.formatTimeBySec(0, false)); //$NON-NLS-1$
				setCurrentTime(0);
				// set error message
				File fCurrent = (File) ObservationManager.getDetail(event, DETAIL_CURRENT_FILE);
				if (fCurrent != null) {
					// display associated error code is given
					String sReason = (String) ObservationManager.getDetail(event, DETAIL_REASON);
					if (sReason != null) {
						setMessage(
								Messages.getString("Error." + sReason) + ": " + fCurrent.getAbsolutePath(), InformationJPanel.ERROR);//$NON-NLS-1$ //$NON-NLS-2$
					} else {// default message
						setMessage(
								Messages.getString("Error.007") + ": " + fCurrent.getAbsolutePath(), InformationJPanel.ERROR);//$NON-NLS-1$ //$NON-NLS-2$
					}
				} else { // none specified file
					setMessage(Messages.getString("Error.007"), //$NON-NLS-1$
							InformationJPanel.ERROR);//$NON-NLS-1$
				}
			} catch (Exception e) {
				Log.error(e);
			}
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (EventSubject.EVENT_HEART_BEAT.equals(subject) && !FIFO.isStopped()
							&& !Player.isPaused()) {
						long length = JajukTimer.getInstance().getCurrentTrackTotalTime();
						long lTime = JajukTimer.getInstance().getCurrentTrackEllapsedTime();
						int iPos = (int) (100 * JajukTimer.getInstance().getCurrentTrackPosition());
						setCurrentTime(iPos);
						String sCurrentTotalMessage = Util.formatTimeBySec(JajukTimer.getInstance()
								.getTotalTimeToPlay(), false);
						setTotalTimeMessage(sCurrentTotalMessage + " ["
								+ FIFO.getInstance().getFIFO().size() + "]");

						setCurrentTimeMessage(Util.formatTimeBySec(lTime, false)
								+ " / " + Util.formatTimeBySec(length, false)); //$NON-NLS-1$);
					} else if (EventSubject.EVENT_ZERO.equals(subject)) {
						setCurrentTimeMessage(Util.formatTimeBySec(0, false)
								+ " / " + Util.formatTimeBySec(0, false)); //$NON-NLS-1$
						setCurrentTime(0);
						setTotalTimeMessage("00:00:00");//$NON-NLS-1$
						setMessage(
								Messages.getString("JajukWindow.18"), InformationJPanel.INFORMATIVE); //$NON-NLS-1$
					} else if (EventSubject.EVENT_FILE_LAUNCHED.equals(subject)) {
						File file = FIFO.getInstance().getCurrentFile();
						if (file != null) {
							String sMessage = Messages.getString("FIFO.10") + " " + file.getTrack().getAuthor().getName2() //$NON-NLS-1$ //$NON-NLS-2$
									+ " / " + file.getTrack().getAlbum().getName2() + " / " //$NON-NLS-1$ //$NON-NLS-2$
									+ file.getTrack().getName();//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
							setMessage(sMessage, InformationJPanel.INFORMATIVE);
						}
					}
				}
			});
		}
	}

	/**
	 * toString() method
	 */
	public String toString() {
		return getClass().getName();
	}

	public int getMessageType() {
		return iType;
	}
}