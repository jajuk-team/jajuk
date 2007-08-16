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
 *  $$Revision: 2563 $$
 */

package org.jajuk.ui.views;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.AlbumThumb;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

import java.awt.FlowLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.vlsolutions.swing.docking.ShadowBorder;

import ext.FlowScrollPanel;

/**
 * Show suggested albums based on current collection (bestof, novelties) and
 * LAstFM
 */
public class SuggestionView extends ViewAdapter implements ITechnicalStrings, Observer {

	private static final long serialVersionUID = 1L;

	private JTabbedPane tabs;

	private enum SuggestionType {
		BEST_OF, NEWEST, RARE, SIMILAR_ALBUMS, SIMILAR_ARTISTS
	}

	JPanel jpBestof;

	JPanel jpNewest;

	JPanel jpRare;

	JPanel jpSimilarAlbums;

	JPanel jpSimilarAuthors;

	private int comp = 0;

	public SuggestionView() {
		ObservationManager.register(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("SuggestionView.0");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.views.IView#populate()
	 */
	public void initUI() {
		tabs = new JTabbedPane();
		// Add panels
		jpBestof = getPanel(SuggestionType.BEST_OF);
		tabs.add(jpBestof);
		jpNewest = getPanel(SuggestionType.NEWEST);
		tabs.add(jpNewest);
		jpRare = getPanel(SuggestionType.RARE);
		tabs.add(jpRare);
		setTitles();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(tabs);
		// Look for events
		ObservationManager.register(this);
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		eventSubjectSet.add(EventSubject.EVENT_PARAMETERS_CHANGE);
		return eventSubjectSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		EventSubject subject = event.getSubject();
		if (subject.equals(EventSubject.EVENT_FILE_LAUNCHED)) {
			comp++;
			// Change local collection suggestions every 10 track plays
			if (comp % 10 == 0) {
				refreshLocalCollectionTabs();
			}
		} else if (subject.equals(EventSubject.EVENT_PARAMETERS_CHANGE)) {
			// The show/hide unmounted may have changed, refresh local
			// collection panels
			refreshLocalCollectionTabs();
		}
	}

	private void refreshLocalCollectionTabs() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				tabs.remove(jpBestof);
				tabs.remove(jpNewest);
				tabs.remove(jpRare);
				jpBestof = getPanel(SuggestionType.BEST_OF);
				tabs.add(jpBestof, 0);
				jpNewest = getPanel(SuggestionType.NEWEST);
				tabs.add(jpNewest, 1);
				jpRare = getPanel(SuggestionType.RARE);
				tabs.add(jpRare, 2);
				setTitles();
			}
		});
	}

	private void setTitles() {
		tabs.setTitleAt(0, Messages.getString("SuggestionView.1"));
		tabs.setTitleAt(1, Messages.getString("SuggestionView.2"));
		tabs.setTitleAt(2, Messages.getString("SuggestionView.5"));
	}

	private JPanel getPanel(SuggestionType type) {
		FlowScrollPanel out = new FlowScrollPanel();
		out.setLayout(new FlowLayout(FlowLayout.LEFT));
		JScrollPane jsp = new JScrollPane(out);
		jsp.setBorder(null);
		jsp.setViewportBorder(null);
		// TODO Cannot remove border, see online
		out.setScroller(jsp);
		List<Album> albums = null;
		if (type == SuggestionType.BEST_OF) {
			albums = AlbumManager.getInstance().getBestOfAlbums(
					ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
		} else if (type == SuggestionType.NEWEST) {
			albums = AlbumManager.getInstance().getNewestAlbums(
					ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
		} else if (type == SuggestionType.RARE) {
			albums = AlbumManager.getInstance().getRarelyListenAlbums(
					ConfigurationManager.getBoolean(CONF_OPTIONS_HIDE_UNMOUNTED), NB_BESTOF_ALBUMS);
		}
		for (Album album : albums) {
			// Try creating the thumbnail
			Util.refreshThumbnail(album, "100x100");
			AlbumThumb thumb = new AlbumThumb(album, 100, false);
			thumb.populate();
			thumb.setBorder(new ShadowBorder());
			out.add(thumb);
		}
		return out;
	}

}
