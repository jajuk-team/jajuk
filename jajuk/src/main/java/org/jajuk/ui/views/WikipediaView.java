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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.perspectives.InfoPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.browser.WebBrowser;

/**
 * Wikipedia view
 * 
 * @author Bertrand Florat
 * @created 12/12/2005
 */
public class WikipediaView extends ViewAdapter implements ITechnicalStrings, Observer,
        ActionListener {

    private static final long serialVersionUID = 1L;

    // control panel
    JPanel jpControl;

    JLabel jlLanguage;

    JComboBox jcbLanguage;

    /** JDIC web browser: ie or netscape */
    WebBrowser browser;

    /** Langage index */
    int index = 0;

    /** Current search */
    String search = null; //$NON-NLS-1$

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
                { 5, TableLayout.PREFERRED, 5 } };
        jpControl.setLayout(new TableLayout(sizeControl));
        jlLanguage = new JLabel(Messages.getString("WikipediaView.1")); //$NON-NLS-1$
        jcbLanguage = new JComboBox();
        jcbLanguage.setBorder(Util.getShadowBorder());
        for (String sLocale : Messages.getLocales()) {
            jcbLanguage.addItem(Messages.getHumanForLocale(sLocale));
        }
        // get stored language
        index = Messages.getLocales().indexOf(
                ConfigurationManager.getProperty(CONF_WIKIPEDIA_LANGUAGE));
        jcbLanguage.setSelectedIndex(index);
        jcbLanguage.addActionListener(this);
        jpControl.add(jlLanguage, "1,1");//$NON-NLS-1$
        jpControl.add(jcbLanguage, "3,1");//$NON-NLS-1$

        // global layout
        double size[][] = { { 2, TableLayout.FILL, 5 },
                { TableLayout.PREFERRED, 5, TableLayout.FILL } };
        setLayout(new TableLayout(size));
        browser = new WebBrowser();
        // WebBrowser.setDebug(true);
        add(jpControl, "1,0"); //$NON-NLS-1$
        add(browser, "1,2"); //$NON-NLS-1$

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
            // Do not perform search if current perspective is not info for perfs
            if (!(PerspectiveManager.getCurrentPerspective() instanceof InfoPerspective)) {
                return;
            }
            //If current state is stopped, reset page
            if (FIFO.getInstance().getCurrentFile() == null) {
                reset();
                return;
            }
            //OK, so a file should be running, display author info
            Properties details = ObservationManager
                    .getDetailsLastOccurence(EventSubject.EVENT_FILE_LAUNCHED);
            String search = FIFO.getInstance().getCurrentFile().getTrack().getAuthor().getName2();
            //some more checks
            if (details != null && search != null && !search.equals(this.search)) {
                // a file has been launch before view creation
                this.search = search;
                // Do not perform search if current perspective is not info for perfs
                launchSearch(search);
            }
        }
        // Reset the page when stopping
        else if (subject.equals(EventSubject.EVENT_ZERO)) {
            reset();
        }
        // User changed author name, so we have to reload
        // new author wikipedia page
        else if (subject.equals(EventSubject.EVENT_AUTHOR_CHANGED)) {
            update(new Event(EventSubject.EVENT_FILE_LAUNCHED));
        }
    }

    /**
     * Perform wikipedia search
     * 
     * @param search
     */
    private void launchSearch(final String search) {
        new Thread() {
            public void run() {
                try {
                    //Thread.sleep(3000);
                    URL url = new URL("http://" + //$NON-NLS-1$
                            Messages.getLocales().get(index) + ".wikipedia.org/wiki/" + search); //$NON-NLS-1$
                    Log.debug("Wikipedia search: " + url); //$NON-NLS-1$
                    if (browser != null) {
                        browser.setURL(url);
                    }
                } catch (Exception e) {
                    Log.error(e);
                }

            }
        }.start();
    }

    /*
     * Reset view by making a request to jajuk SF website
     */
    private void reset() {
        // Reset current search
        this.search = null;
        // Display jajuk page (in a thread to avoid freezing UI)
        new Thread() {
            public void run() {
                if (browser != null) {
                    try {
                        browser.setURL(new URL(WIKIPEDIA_VIEW_DEFAULT_URL));
                    } catch (MalformedURLException e) {
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
            index = jcbLanguage.getSelectedIndex();
            // update index
            ConfigurationManager.setProperty(CONF_WIKIPEDIA_LANGUAGE, Messages.getLocales().get(
                    index));

            // launch wikipedia search for this language
            launchSearch(this.search);
        }
    }

}
