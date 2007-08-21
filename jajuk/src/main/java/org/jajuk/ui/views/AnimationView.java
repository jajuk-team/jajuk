/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.WebRadio;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import com.jgoodies.animation.Animation;
import com.jgoodies.animation.Animations;
import com.jgoodies.animation.Animator;
import com.jgoodies.animation.animations.BasicTextAnimation;
import com.jgoodies.animation.components.BasicTextLabel;

/**
 * Animation-based view
 */
public class AnimationView extends ViewAdapter implements ITechnicalStrings, Observer,
		ComponentListener {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_FRAME_RATE = 15;

	private static final int DEFAULT_DURATION = 5000;

	private static final int DEFAULT_PAUSE = 500;

	/** Current panel width* */
	private int iSize;

	private BasicTextLabel btl1;

	private Animator animator;

	public AnimationView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("AnimationView.0");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#populate()
	 */
	public void initUI() {
		setLayout(new BorderLayout());
		addComponentListener(this);
		btl1 = new BasicTextLabel(" ");
		// setBackground(Color.WHITE);
		// setForeground(Color.BLACK);
		add(btl1);

		ObservationManager.register(this);
		// check if a track or a webradio has already been launched
		if (FIFO.getInstance().isPlayingRadio()) {
			update(new Event(EventSubject.EVENT_WEBRADIO_LAUNCHED, ObservationManager
					.getDetailsLastOccurence(EventSubject.EVENT_WEBRADIO_LAUNCHED)));
		} else {
			update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
					.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
		}
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_WEBRADIO_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		return eventSubjectSet;
	}

	/** Set the text to be displayed* */
	public void setText(final String sText) {
		SwingUtilities.invokeLater(new Runnable() {
			// this is mandatory to
			// get actual getWitdth
			public void run() {
				iSize = AnimationView.this.getWidth();
				// current width. Must be called inside an invoke and wait,
				// otherwise, returns zero
				Font font = null;
				boolean bOk = false;
				int i = 40;
				while (!bOk) {
					font = new Font("verdana", Font.BOLD, i);
					FontMetrics fontMetrics = Main.getWindow().getFontMetrics(font);
					int iFontSize = SwingUtilities.computeStringWidth(fontMetrics, sText);
					if (iFontSize <= iSize - 150) {
						bOk = true;
					} else {
						i--;
					}
				}
				btl1.setFont(font);
				if (animator != null) {
					animator.stop();
				}
				Animation animPause = Animations.pause(DEFAULT_PAUSE);
				Animation anim = null;
				// select a random animation
				int iShuffle = (int) (Math.random() * 3);
				switch (iShuffle) {
				case 0:
					anim = BasicTextAnimation.defaultScale(btl1, DEFAULT_DURATION, sText,
							Color.darkGray);
					break;
				case 1:
					anim = BasicTextAnimation.defaultSpace(btl1, DEFAULT_DURATION, sText,
							Color.darkGray);
					break;
				case 2:
					anim = BasicTextAnimation.defaultFade(btl1, DEFAULT_DURATION, sText,
							Color.darkGray);
					break;
				}
				Animation animAll = Animations.sequential(anim, animPause);
				anim = Animations.repeat(Float.POSITIVE_INFINITY, animAll);
				animator = new Animator(anim, DEFAULT_FRAME_RATE);
				animator.start();
			}
		});
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
				String s = "";
				try {
					s = Util.applyPattern(file, ConfigurationManager
							.getProperty(CONF_ANIMATION_PATTERN), false);
				} catch (JajukException e) {
					Log.error(e);
				}
				setText(s);
			}
		} else if (subject.equals(EventSubject.EVENT_ZERO)) {
			setText(Messages.getString("JajukWindow.18"));
		} else if (subject.equals(EventSubject.EVENT_WEBRADIO_LAUNCHED)) {
			WebRadio radio = (WebRadio) event.getDetails().get(DETAIL_CONTENT);
			if (radio != null) {
				setText(radio.getName());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				iSize = SwingUtilities.getRootPane(AnimationView.this).getWidth(); // current
				// width
				if (FIFO.getInstance().isPlayingRadio()) {
					update(new Event(EventSubject.EVENT_WEBRADIO_LAUNCHED, ObservationManager
							.getDetailsLastOccurence(EventSubject.EVENT_WEBRADIO_LAUNCHED)));
				} else {
					update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
							.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
				}
				// force redisplay
			}
		});
		Log.debug("View resized, new width=" + iSize);
	}
}
