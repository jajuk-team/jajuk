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

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.Graphics;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.browser.WebBrowser;

/**
 * Wikipedia view
 * 
 * @author Bertrand Florat
 * @created 12/12/2005
 */
public class WikipediaView extends ViewAdapter implements ITechnicalStrings,
		Observer, ChangeListener {

	private static final long serialVersionUID = 1L;

	// control panel
	JPanel jpControl;

	JLabel jlLanguage;

	JSpinner jspLanguage;

	/** JDIC web browser: ie or netscape */
	WebBrowser browser;

	/** Langage index */
	int index = 0;

	/** Current search */
	String search = ""; //$NON-NLS-1$

	/**
	 * Constructor
	 * 
	 */
	public WikipediaView() {
	}

	/**
	 * Overwride paint method to fix an issue with WebBrowser: when user select
	 * another perspective and come back, it is void We have to force setUrl to
	 * repaint it
	 */
	public void paint(Graphics g) {
		super.paint(g);
		launchSearch(this.search);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#getDesc()
	 */
	public String getDesc() {
		return "WikipediaView.0"; //$NON-NLS-1$
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
		double sizeControl[][] =
		// Language by lang
		{ { 3 * iXspace, TableLayout.PREFERRED, iXspace, 200, 3 * iXspace },
				{ 25 } };
		jpControl.setLayout(new TableLayout(sizeControl));
		jlLanguage = new JLabel(Messages.getString("WikipediaView.1")); //$NON-NLS-1$
		jspLanguage = new JSpinner();
		String[] model = new String[Messages.getInstance().getDescs().size()];
		// set startup locale
		int i = 0;
		String local = ConfigurationManager
				.getProperty(CONF_WIKIPEDIA_LANGUAGE);
		if (local == null || local.trim().length() == 0) {
			index = 0; // english as default in case of error
		} else {
			for (String s : Messages.getInstance().getDescs()) {
				model[i] = Messages.getString(s);
				// set index to current language
				if (Messages.getInstance().getLocals().get(i).equals(local)) {
					index = i;
				}
				i++;
			}
		}
		// we use a spinner and not a combo because browser native window
		// can hide combo popup
		jspLanguage = new JSpinner(new SpinnerListModel(model));
		int defaultLang = Messages.getInstance().getLocals().indexOf(
				ConfigurationManager.getProperty(CONF_WIKIPEDIA_LANGUAGE));
		jspLanguage.setValue(Messages.getString(Messages.getInstance()
				.getDescs().get(defaultLang)));
		jspLanguage.addChangeListener(this);
		jpControl.add(jlLanguage, "1,0");//$NON-NLS-1$
		jpControl.add(jspLanguage, "3,0");//$NON-NLS-1$

		// global layout
		double size[][] = { { 0.99 }, { 30, 0, TableLayout.FILL } };
		setLayout(new TableLayout(size));
		browser = new WebBrowser();
		WebBrowser.setDebug(true);
		launchSearch(""); //$NON-NLS-1$

		add(jpControl, "0,0"); //$NON-NLS-1$
		add(browser, "0,2"); //$NON-NLS-1$

		// subscriptions to events
		ObservationManager.register(this);

		// force event
		update(new Event(EventSubject.EVENT_FILE_LAUNCHED, ObservationManager
				.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED)));
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_ZERO);
		eventSubjectSet.add(EventSubject.EVENT_AUTHOR_CHANGED);
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
				&& FIFO.getInstance().getCurrentFile() != null) {
			Properties details = ObservationManager
					.getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED);
			String search = FIFO.getInstance().getCurrentFile().getTrack()
					.getAuthor().getName2();
			if (details != null && !search.equals(this.search)) { // a
				// file
				// has
				// been
				// laucnh
				// before
				// view
				// creation
				this.search = search;
				launchSearch(search);
			}
		}
		// Reset the page when stopping
		else if (subject.equals(EventSubject.EVENT_ZERO)) {
			this.search = ""; // reset //$NON-NLS-1$
			launchSearch(search);
		}
		// User changed author name, so we have to reload new author wikipedia
		// page
		else if (subject.equals(EventSubject.EVENT_AUTHOR_CHANGED)) {
			update(new Event(EventSubject.EVENT_FILE_LAUNCHED));
		}
	}

	/**
	 * Perform wikipedia search
	 * 
	 * @param search
	 */
	private void launchSearch(String search) {
		try {
			URL url = new URL("http://" + //$NON-NLS-1$
					Messages.getInstance().getLocals().get(index)
					+ ".wikipedia.org/wiki/" + search); //$NON-NLS-1$
			Log.debug("Wikipedia search: " + url); //$NON-NLS-1$
			if (browser != null) {
				browser.setURL(url);
			}
		} catch (MalformedURLException e) {
			Log.error(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
	 */
	public void stateChanged(ChangeEvent arg0) {
		if (arg0.getSource() == jspLanguage) {
			// update index
			int i = 0;
			for (String sDesc : Messages.getInstance().getDescs()) {
				if (jspLanguage.getValue().equals(Messages.getString(sDesc))) {
					this.index = i;
					ConfigurationManager.setProperty(CONF_WIKIPEDIA_LANGUAGE,
							Messages.getInstance().getLocals().get(index));
					break;
				} else {
					i++;
				}
			}
			// launch wikipedia search for this language
			launchSearch(this.search);
		}
	}

}
