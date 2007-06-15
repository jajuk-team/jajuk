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
import org.jajuk.ui.perspectives.InfoPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Wikipedia view
 */
public class WikipediaView extends ViewAdapter implements ITechnicalStrings, Observer,
		ActionListener {

	private static final long serialVersionUID = 1L;

	// control panel
	JPanel jpControl;

	JLabel jlLanguage;

	JComboBox jcbLanguage;

	JLabel jlSearchOn;

	JComboBox jcbSearchOn;

	/** Cobra web browser */
	JajukHtmlPanel browser;

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
		// Control panel
		jpControl = new JPanel();
		jpControl.setBorder(BorderFactory.createEtchedBorder());
		int iXspace = 10;
		double p = TableLayout.PREFERRED;
		double sizeControl[][] =
		// Language by lang
		{
				{ 3 * iXspace, p, iXspace, p, 3 * iXspace, p, iXspace, p, TableLayout.FILL,
						3 * iXspace }, { 5, p, 5 } };
		jpControl.setLayout(new TableLayout(sizeControl));
		jlLanguage = new JLabel(Messages.getString("WikipediaView.1"));
		jcbLanguage = new JComboBox();
		for (String sLocale : Messages.getLocales()) {
			jcbLanguage.addItem(Messages.getHumanForLocale(sLocale));
		}
		jlSearchOn = new JLabel(Messages.getString("WikipediaView.2"));
		jcbSearchOn = new JComboBox();
		jcbSearchOn.addItem(Messages.getString("Item_Author"));
		jcbSearchOn.addItem(Messages.getString("Item_Album"));
		jcbSearchOn.addItem(Messages.getString("Item_Track"));
		// get stored language
		indexLang = Messages.getLocales().indexOf(
				ConfigurationManager.getProperty(CONF_WIKIPEDIA_LANGUAGE));
		jcbLanguage.setSelectedIndex(indexLang);
		jcbLanguage.addActionListener(this);
		jcbSearchOn.addActionListener(this);
		jpControl.add(jlLanguage, "1,1");
		jpControl.add(jcbLanguage, "3,1");
		jpControl.add(jlSearchOn, "5,1");
		jpControl.add(jcbSearchOn, "7,1");

		// global layout
		double size[][] = { { 2, TableLayout.FILL, 5 },
				{ TableLayout.PREFERRED, 5, TableLayout.FILL } };
		setLayout(new TableLayout(size));
		browser = new JajukHtmlPanel();
		add(jpControl, "1,0");
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

			//Launch search
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
	 * @param bForceReload force the page display
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
					//Avoid reloading an existing page
					if (!bForceReload && search.equals(WikipediaView.this.search)) {
						return;
					}
					// Store the search to avoid future identical searches
					WikipediaView.this.search = search;

					URL url = new URL(("http://" + Messages.getLocales().get(indexLang)
							+ ".wikipedia.org/wiki/" + search).replaceAll(" ", "_"));
					Log.debug("Wikipedia search: " + url);
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
		} else if (arg0.getSource() == jcbSearchOn) {
			indexItem = jcbSearchOn.getSelectedIndex();
			// force launch wikipedia search for this item
			launchSearch(true);
		}
	}

}
