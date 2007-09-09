/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.ui.views;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Track;
import org.jajuk.base.WebRadio;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.FontManager;
import org.jajuk.ui.FontManager.JajukFont;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;

import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ext.services.lyrics.LyricsService;

/**
 * Lyrics view
 * <p>
 * Data comes currently from http://www.lyrc.com.ar
 * </p>
 */
public class LyricsView extends ViewAdapter implements Observer {

	private static final long serialVersionUID = 2229941034734574056L;

	private JTextArea textarea;

	JScrollPane jsp;

	private JLabel jlTitle;

	private JLabel jlAuthor;

	private String sURL;

	private Track track;

	private String lyrics;

	private JMenuItem jmiCopyToClipboard;

	private JMenuItem jmiLaunchInBrowser;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("LyricsView.0");
	}

	public LyricsView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#initUI()
	 */
	public void initUI() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		textarea = new JTextArea();
		textarea.setLineWrap(true);
		textarea.setWrapStyleWord(true);
		textarea.setEditable(false);
		textarea.setMargin(new Insets(10, 10, 10, 10));
		textarea.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
		textarea.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					handlePopup(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					handlePopup(e);
				}
			}
		});
		jlAuthor = new JLabel();
		jlAuthor.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN_L));
		jlTitle = new JLabel();
		jlTitle.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN_XL));
		jsp = new JScrollPane(textarea);
		textarea.setFont(FontManager.getInstance().getFont(JajukFont.PLAIN));
		int height = getHeight() - 200;
		FormLayout layout = new FormLayout(
		// --columns
				"3dlu,p:grow, 3dlu",
				// --rows
				"5dlu, p, 3dlu, p, 3dlu,fill:" + height + ":grow,3dlu"); // rows
		PanelBuilder builder = new PanelBuilder(layout);
		CellConstraints cc = new CellConstraints();
		// Add items
		builder.add(jlTitle, cc.xy(2, 2));
		builder.add(jlAuthor, cc.xy(2, 4));
		builder.add(jsp, cc.xy(2, 6));
		JPanel p = builder.getPanel();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(p);

		ObservationManager.register(this);
		// check if a track has already been launched
		update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
				.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));

	}

	public void handlePopup(final MouseEvent e) {
		JPopupMenu menu = new JPopupMenu();
		jmiCopyToClipboard = new JMenuItem(ActionManager.getAction(JajukAction.COPY_TO_CLIPBOARD));
		menu.add(jmiCopyToClipboard);
		jmiLaunchInBrowser = new JMenuItem(ActionManager.getAction(JajukAction.LAUNCH_IN_BROWSER));
		jmiLaunchInBrowser.putClientProperty(DETAIL_CONTENT, sURL);
		menu.add(jmiLaunchInBrowser);
		menu.show(textarea, e.getX(), e.getY());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#getRegistrationKeys()
	 */
	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		eventSubjectSet.add(EventSubject.EVENT_WEBRADIO_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_LYRICS_DOWNLOADED);
		return eventSubjectSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
	 */
	public void update(final Event event) {
		EventSubject subject = event.getSubject();
		if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)) {
			new Thread() {
				public void run() {
					File file = FIFO.getInstance().getCurrentFile();
					if (file != null) {
						track = FIFO.getInstance().getCurrentFile().getTrack();
						sURL = "http://www.lyrc.com.ar/en/tema1en.php?artist="
								+ track.getAuthor().getName2() + "&songname=" + track.getName();
						// Launch lyrics service asynchronously and out of the
						// AWT dispatcher thread
						lyrics = LyricsService.getLyrics(track.getAuthor().getName2(), track
								.getName());
						// Notify to make UI changes
						if (lyrics != null && track != null && sURL != null) {
							ObservationManager.notify(new Event(
									EventSubject.EVENT_LYRICS_DOWNLOADED));
						}
					}
				}
			}.start();
		} else if (subject.equals(EventSubject.EVENT_ZERO)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jsp.setVisible(false);
					jlAuthor.setText("");
					jlTitle.setText(Messages.getString("JajukWindow.18"));
				}
			});
		} else if (subject.equals(EventSubject.EVENT_WEBRADIO_LAUNCHED)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					WebRadio radio = (WebRadio) event.getDetails().get(DETAIL_CONTENT);
					if (radio != null) {
						jlTitle.setText(radio.getName());
						jlAuthor.setText("");
						jsp.setVisible(false);
					}
				}
			});
		} else if (subject.equals(EventSubject.EVENT_LYRICS_DOWNLOADED)) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					jsp.setVisible(true);
					textarea.setToolTipText(sURL);
					if (lyrics.length() > 0) {
						textarea.setText(lyrics);
					} else {
						textarea.setText(Messages.getString("WikipediaView.3"));
					}
					// Make sure to display the begin of the text (must be
					// done in a thread to be executed when textarea display
					// is actually finished)
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							jsp.getVerticalScrollBar().setValue(0);
						}
					});
					jlAuthor.setText(track.getAuthor().getName2());
					jlTitle.setText(track.getName());
					Util.copyData = sURL;
				}
			});

		}
	}
}
