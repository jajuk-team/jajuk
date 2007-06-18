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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukHtmlPanel;
import org.jajuk.ui.action.ActionBase;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.ui.action.JajukAction;
import org.jajuk.ui.perspectives.InfoPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import info.clearthought.layout.TableLayout;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Wikipedia view
 */
public class WikipediaView extends ViewAdapter implements ITechnicalStrings, Observer,
		ActionListener {

	private static final long serialVersionUID = 1L;

	JLabel jlLanguage;

	JComboBox jcbLanguage;

	/** Cobra web browser */
	JajukHtmlPanel browser;

	JButton jbCopy;

	JButton jbLaunchInExternalBrowser;

	JToggleButton jbAuthorSearch;
	
	JToggleButton jbAlbumSearch;

	JToggleButton jbTrackSearch;

	/** Language index */
	int indexLang = 0;

	/** Item index, default:author */
	int indexItem = 0;

	/** Current search */
	String search = null;

	/**
	 * Constructor
	 * 
	 */
	public WikipediaView() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("WikipediaView.0");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#populate()
	 */
	public void initUI() {
		jlLanguage = new JLabel(Messages.getString("WikipediaView.1"));
		jcbLanguage = new JComboBox();
		for (String sLocale : Messages.getLocales()) {
			jcbLanguage.addItem(Messages.getHumanForLocale(sLocale));
		}
		// get stored language
		indexLang = Messages.getLocales().indexOf(
				ConfigurationManager.getProperty(CONF_WIKIPEDIA_LANGUAGE));
		jcbLanguage.setSelectedIndex(indexLang);
		jcbLanguage.addActionListener(this);
		// Buttons
		ActionBase aCopy = ActionManager.getAction(JajukAction.COPY_TO_CLIPBOARD);
		// Remove text inside the button
		aCopy.setName(null);
		jbCopy = new JButton(aCopy);
		ActionBase aBrowse = ActionManager.getAction(JajukAction.LAUNCH_IN_BROWSER);
		// Remove text inside the button
		aBrowse.setName(null);
		jbLaunchInExternalBrowser = new JButton(aBrowse);
		jbAuthorSearch = new JToggleButton(IconLoader.ICON_AUTHOR,false);
		jbAuthorSearch.setToolTipText(Messages.getString("WikipediaView.5"));
		jbAuthorSearch.addActionListener(this);
		jbAlbumSearch = new JToggleButton(IconLoader.ICON_ALBUM,true);
		jbAlbumSearch.setToolTipText(Messages.getString("WikipediaView.6"));
		jbAlbumSearch.addActionListener(this);
		jbTrackSearch = new JToggleButton(IconLoader.ICON_TRACK,false);
		jbTrackSearch.setToolTipText(Messages.getString("WikipediaView.7"));
		jbTrackSearch.addActionListener(this);

		JToolBar jtb = new JToolBar();
		jtb.setFloatable(false);
		jtb.setRollover(true);
		jtb.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		//Add items
		jtb.add(jbAuthorSearch);
		jtb.add(jbAlbumSearch);
		jtb.add(jbTrackSearch);
		jtb.addSeparator();
		jtb.add(jbCopy);
		jtb.add(jbLaunchInExternalBrowser);
		jtb.addSeparator();
		jtb.add(jcbLanguage);
		
		JPanel jpCommand = new JPanel();
		jpCommand.setBorder(BorderFactory.createEtchedBorder());
		jpCommand.setLayout(new FlowLayout(FlowLayout.LEFT));
		jpCommand.add(jtb);
		
		// global layout
		double size[][] = { { 2, TableLayout.FILL, 5 },
				{ TableLayout.PREFERRED, 5, TableLayout.FILL } };
		setLayout(new TableLayout(size));
		browser = new JajukHtmlPanel();
		add(jpCommand, "1,0");
		add(browser, "1,2");

		// Display default page at startup is none track launch
		// avoid to launch this if a track is playing
		// to avoid thread concurrency
		if (FIFO.getInstance().getCurrentFile() == null) {
			reset();
		}

		// subscriptions to events
		ObservationManager.register(WikipediaView.this);

		// force event
		update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
				.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		eventSubjectSet.add(EventSubject.EVENT_AUTHOR_CHANGED);
		eventSubjectSet.add(EventSubject.EVENT_ALBUM_CHANGED);
		eventSubjectSet.add(EventSubject.EVENT_TRACK_CHANGED);
		eventSubjectSet.add(EventSubject.EVENT_PERPECTIVE_CHANGED);
		return eventSubjectSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		EventSubject subject = event.getSubject();
		// Make a search after a stop period
		if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)
				|| subject.equals(EventSubject.EVENT_PERPECTIVE_CHANGED)) {
			// Do not perform search if current perspective is not info for
			// perfs
			if (!(PerspectiveManager.getCurrentPerspective() instanceof InfoPerspective)) {
				return;
			}
			// If current state is stopped, reset page
			if (FIFO.getInstance().getCurrentFile() == null) {
				reset();
				return;
			}
			// OK, so a file should be running, display author info
			Properties details = ObservationManager
					.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED);

			// Launch search
			launchSearch(false);
		}
		// Reset the page when stopping
		else if (subject.equals(EventSubject.EVENT_ZERO)) {
			reset();
		}
		// User changed current track tags, so we have to reload
		// new author wikipedia page
		else if (subject.equals(EventSubject.EVENT_AUTHOR_CHANGED)
				|| subject.equals(EventSubject.EVENT_ALBUM_CHANGED)
				|| subject.equals(EventSubject.EVENT_TRACK_CHANGED)) {
			update(new Event(EventSubject.EVENT_FILE_LAUNCHED));
		}
	}

	/**
	 * Perform wikipedia search
	 * 
	 * @param bForceReload
	 *            force the page display
	 */
	private void launchSearch(final boolean bForceReload) {
		new Thread() {
			public void run() {
				try {
					String search = null;
					if (FIFO.getInstance().getCurrentFile() != null) {
						switch (indexItem) {
						// Author
						case 0:
							search = FIFO.getInstance().getCurrentFile().getTrack().getAuthor()
									.getName2();
							// don't display page if item is unknown
							if (Messages.getString(UNKNOWN_AUTHOR).equals(search)) {
								search = null;
							}
							break;
						// Album
						case 1:
							search = FIFO.getInstance().getCurrentFile().getTrack().getAlbum()
									.getName2();
							// don't display page if item is unknown
							if (Messages.getString(UNKNOWN_ALBUM).equals(search)) {
								search = null;
							}
							break;
						// Title
						case 2:
							search = FIFO.getInstance().getCurrentFile().getTrack().getName();
							break;
						}
					}
					// If search is still null, display an nothing found page
					if (search == null) {
						browser.setUnknow();
						return;
					}
					// Avoid reloading an existing page
					if (!bForceReload && search.equals(WikipediaView.this.search)) {
						return;
					}
					// Store the search to avoid future identical searches
					WikipediaView.this.search = search;

					URL url = new URL(("http://" + Messages.getLocales().get(indexLang)
							+ ".wikipedia.org/wiki/" + search).replaceAll(" ", "_"));
					Log.debug("Wikipedia search: " + url);
					Util.copyData = url.toString();
					Util.url = url;
					browser.setURL(url);
				} catch (Exception e) {
					Log.error(e);
				}

			}
		}.start();
	}

	/*
	 * Reset view
	 */
	private void reset() {
		// Reset current search
		this.search = null;
		// Display jajuk page (in a thread to avoid freezing UI)
		new Thread() {
			public void run() {
				if (browser != null) {
					try {
						browser.clearDocument();
					} catch (Exception e) {
						Log.error(e);
					}
				}
			}
		}.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == jcbLanguage) {
			indexLang = jcbLanguage.getSelectedIndex();
			// update index
			ConfigurationManager.setProperty(CONF_WIKIPEDIA_LANGUAGE, Messages.getLocales().get(
					indexLang));
			// force launch wikipedia search for this language
			launchSearch(true);
		} else if (arg0.getSource() == jbAlbumSearch) {
			indexItem = 0;
			//Select item as the second click will deselect it (toggle button)
			jbAlbumSearch.setSelected(true);
			//deselect others buttons
			jbAuthorSearch.setSelected(false);
			jbTrackSearch.setSelected(false);
			// force event
			launchSearch(true);
		} else if (arg0.getSource() == jbAuthorSearch) {
			indexItem = 1;
			//Select item as the second click will deselect it (toggle button)
			jbAuthorSearch.setSelected(true);
			//deselect others buttons
			jbAlbumSearch.setSelected(false);
			jbTrackSearch.setSelected(false);
			// force event
			launchSearch(true);
		} else if (arg0.getSource() == jbTrackSearch) {
			indexItem = 2;
			//Select item as the second click will deselect it (toggle button)
			jbTrackSearch.setSelected(true);
			//deselect others buttons
			jbAuthorSearch.setSelected(false);
			jbAlbumSearch.setSelected(false);
			// force event
			launchSearch(true);
		}
	}

}
