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

import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.Item;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.PlaylistFile;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.base.PlaylistManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.PlaylistFileItem;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.wizard.PropertiesWizard;
import org.jajuk.util.EventSubject;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import ext.SwingWorker;

/**
 * Shows playlist files
 * <p>
 * Physical perspective *
 * <p>
 * Singleton
 */
abstract public class AbstractPlaylistRepositoryView extends ViewAdapter implements Observer,
		ActionListener {

	/** Selected playlist file item */
	PlaylistFileItem plfiSelected;

	/** Queue playlist */
	PlaylistFileItem plfiQueue;

	/** New playlist */
	PlaylistFileItem plfiNew;

	/** Queue playlist */
	PlaylistFileItem plfiBookmarks;

	/** Bestof playlist */
	PlaylistFileItem plfiBestof;

	/** Novelties playlist */
	PlaylistFileItem plfiNovelties;

	/** List of playlistfile item */
	ArrayList<PlaylistFileItem> alPlaylistFileItems = new ArrayList<PlaylistFileItem>(10);

	/** Concorency flag */
	boolean bReleased = true;

	JPanel jpRoot;

	JPopupMenu jpmenu;

	JMenuItem jmiPlay;

	JMenuItem jmiSaveAs;

	JMenuItem jmiDelete;

	JMenuItem jmiProperties;

	MouseAdapter ma;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#display()
	 */
	public void initUI() {
		// needed to get the vertical scroller (don't ask why)
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		// root pane
		jpRoot = new JPanel();
		jpRoot.setLayout(new BoxLayout(jpRoot, BoxLayout.Y_AXIS));

		// Popup menus
		jpmenu = new JPopupMenu();

		jmiPlay = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.0"));
		jmiPlay.addActionListener(this);
		jpmenu.add(jmiPlay);

		jmiDelete = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.3"));
		jmiDelete.addActionListener(this);
		jpmenu.add(jmiDelete);

		jmiSaveAs = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.2"));
		jmiSaveAs.addActionListener(this);
		jpmenu.add(jmiSaveAs);

		jmiProperties = new JMenuItem(Messages.getString("PhysicalPlaylistRepositoryView.4"));
		jmiProperties.addActionListener(this);
		jpmenu.add(jmiProperties);

		// mouse adapter
		// mouse adapter
		ma = new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				handlePopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				handlePopup(e);
			}

			public void handlePopup(final MouseEvent e) {
				if (e.isPopupTrigger()) {
					PlaylistFileItem plfi = (PlaylistFileItem) e.getComponent();
					if (plfi == plfiSelected) {
						// right click
						showMenu(plfi, e);
					} else {
						try {
							plfi.getPlaylistFile().getFiles();
						} catch (JajukException je) {
							Log.error(je.getCode(), plfi.getName(), null); //$NON-NLS-1$
							Messages.showErrorMessage(je.getCode(), plfi.getName()); //$NON-NLS-1$
							selectQueue();
							return;
						}
						selectPlaylistFileItem(plfi);
						showMenu(plfi, e);
					}
				}
			}

			public void mouseClicked(final MouseEvent e) {
				PlaylistFileItem plfi = (PlaylistFileItem) e.getComponent();
				if (plfi == plfiSelected) {
					try {
						play(plfi);
					} catch (JajukException je) {
						Log.error(je.getCode(), plfiSelected.getName(), null); //$NON-NLS-1$
						Messages.showErrorMessage(je.getCode(), plfiSelected.getName()); //$NON-NLS-1$
						selectQueue();
						return;
					}
				} else { // we selected a new playlist file
					try {
						plfi.getPlaylistFile().getFiles();
					} catch (JajukException je) {
						Log.error(je.getCode(), plfi.getName(), null); //$NON-NLS-1$
						Messages.showErrorMessage(je.getCode(), plfi.getName()); //$NON-NLS-1$
						selectQueue();
						return;
					}
					Util.waiting();
					selectPlaylistFileItem(plfi);
				}
			}
		};
		// refresh
		populatePlaylists();
		// make sure playlists items are packed to the top no ugly horizontal
		// scrolling
		jpRoot.add(Box.createVerticalStrut(500));
		JScrollPane jsp = new JScrollPane(jpRoot, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		jsp.getVerticalScrollBar().setUnitIncrement(60);
		add(jsp);
		// Register on the list for subject we are interrested in
		ObservationManager.register(this);
		// set queue playlist as default in playlist editor
		selectPlaylistFileItem(plfiQueue);
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_MOUNT);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_UNMOUNT);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
		return eventSubjectSet;
	}

	private void selectQueue() {
		selectPlaylistFileItem(plfiQueue);
	}

	/**
	 * Display the playlist menu
	 * 
	 * @param plfi
	 * @param e
	 */
	private void showMenu(PlaylistFileItem plfi, MouseEvent e) {
		if (plfi.getType() != PlaylistFileItem.PLAYLIST_TYPE_NORMAL) {
			// cannot delete special playlists
			jmiDelete.setEnabled(false);
			// Save as is only for special playlists
			jmiSaveAs.setEnabled(true);
			jmiProperties.setEnabled(false);
		} else {
			jmiDelete.setEnabled(true);
			jmiSaveAs.setEnabled(false);
			jmiProperties.setEnabled(true);
		}
		// cannot play the queue playlist : nonsense
		if (plfiSelected.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE) {
			jmiPlay.setEnabled(false);
		} else {
			jmiPlay.setEnabled(true);
		}
		jpmenu.show(e.getComponent(), e.getX(), e.getY());

	}

	/**
	 * Set current playlist file item
	 * 
	 * @param plfi
	 */
	synchronized void selectPlaylistFileItem(PlaylistFileItem plfi) {
		// remove item border
		if (plfiSelected != null) {
			plfiSelected.getIcon().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		}
		plfi.getIcon().setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
		// set new item
		this.plfiSelected = plfi;
		FIFO.getInstance().setPlaylist(plfi.getPlaylistFile());
		Properties properties = new Properties();
		properties.put(DETAIL_ORIGIN, AbstractPlaylistRepositoryView.this);
		properties.put(DETAIL_SELECTION, plfi);
		properties.put(DETAIL_TARGET, PerspectiveManager.getCurrentPerspective().getID());
		ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_SELECTION_CHANGED,
				properties));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("PhysicalPlaylistRepositoryView.6");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.PhysicalPlaylistRepositoryView";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(final Event event) {
		EventSubject subject = event.getSubject();
		if (subject.equals(EventSubject.EVENT_DEVICE_MOUNT)
				|| subject.equals(EventSubject.EVENT_DEVICE_UNMOUNT)
				|| subject.equals(EventSubject.EVENT_DEVICE_REFRESH)) {

			SwingWorker sw = new SwingWorker() {
				public Object construct() {
					bReleased = false; // take "MUTEX"
					if (jpRoot.getComponentCount() > 0) {
						jpRoot.removeAll();
					}
					populatePlaylists();
					// try to keep back previous selected playlist if it
					// exists
					if (plfiSelected == null || !alPlaylistFileItems.contains(plfiSelected)) {
						plfiSelected = plfiQueue;
					}
					return null;
				}

				public void finished() {
					// make sure specials playlists are packed to the top
					jpRoot.add(Box.createVerticalStrut(500));
					if (plfiSelected.getPlaylistFile().getType() != PlaylistFileItem.PLAYLIST_TYPE_NORMAL
							|| PlaylistFileManager.getInstance().getPlaylistFileByID(
									plfiSelected.getPlaylistFile().getId()) != null) {
						selectPlaylistFileItem(plfiSelected);
					} else { // means previously selected playlist has
						// changed, select queue
						selectPlaylistFileItem(plfiQueue);
					}
					AbstractPlaylistRepositoryView.this.revalidate();
					AbstractPlaylistRepositoryView.this.repaint();
					bReleased = true;
				}
			};
			int i = 0;
			while (!bReleased && i < 100) {
				// wait until ressource is released
				// we can't just synchronize because action is done in 2 methods
				// of swing worker
				try {
					Thread.sleep(100);
					i++;
				} catch (InterruptedException e) {
					Log.error(e);
				}
			}
			sw.start();
		}
	}

	/**
	 * Create special playlists from collection, this is called by both logicial
	 * and physical populate methods
	 */
	synchronized void populatePlaylists() {
		alPlaylistFileItems.clear();
		// special playlists
		JPanel jpSpecials = new JPanel();
		jpSpecials.setBorder(BorderFactory.createTitledBorder(Messages
				.getString("PhysicalPlaylistRepositoryView.8")));
		jpSpecials.setLayout(new BoxLayout(jpSpecials, BoxLayout.Y_AXIS));
		// queue
		// note we give an id : this id is only used to match current playlist
		if (plfiQueue == null) {
			plfiQueue = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_QUEUE,
					IconLoader.ICON_PLAYLIST_QUEUE, new PlaylistFile(
							PlaylistFileItem.PLAYLIST_TYPE_QUEUE, "1", null, null), Messages
							.getString("PhysicalPlaylistRepositoryView.9"));
			plfiQueue.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.10"));
			plfiQueue.addMouseListener(ma);
		} else if (plfiSelected != null
				&& plfiQueue.getPlaylistFile().equals(plfiSelected.getPlaylistFile())) {
			plfiSelected = plfiQueue;
		}
		jpSpecials.add(plfiQueue);
		alPlaylistFileItems.add(plfiQueue);

		// new
		if (plfiNew == null) {
			plfiNew = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NEW,
					IconLoader.ICON_PLAYLIST_NEW, new PlaylistFile(
							PlaylistFileItem.PLAYLIST_TYPE_NEW, "2", null, null), Messages
							.getString("PhysicalPlaylistRepositoryView.11"));
			plfiNew.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.12"));
			plfiNew.addMouseListener(ma);
		} else if (plfiSelected != null
				&& plfiNew.getPlaylistFile().equals(plfiSelected.getPlaylistFile())) {
			plfiSelected = plfiNew;
		}
		jpSpecials.add(plfiNew);
		alPlaylistFileItems.add(plfiNew);
		// bookmark
		if (plfiBookmarks == null) {
			plfiBookmarks = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK,
					IconLoader.ICON_PLAYLIST_BOOKMARK, new PlaylistFile(
							PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK, "3", null, null), Messages
							.getString("PhysicalPlaylistRepositoryView.13"));
			plfiBookmarks.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.14"));
			plfiBookmarks.addMouseListener(ma);
		} else if (plfiSelected != null
				&& plfiBookmarks.getPlaylistFile().equals(plfiSelected.getPlaylistFile())) {
			plfiSelected = plfiBookmarks;
		}
		jpSpecials.add(plfiBookmarks);
		alPlaylistFileItems.add(plfiBookmarks);
		// Best of
		if (plfiBestof == null) {
			plfiBestof = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_BESTOF,
					IconLoader.ICON_PLAYLIST_BESTOF, new PlaylistFile(
							PlaylistFileItem.PLAYLIST_TYPE_BESTOF, "4", null, null), Messages
							.getString("PhysicalPlaylistRepositoryView.15"));
			plfiBestof.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.16"));
			plfiBestof.addMouseListener(ma);
		} else if (plfiSelected != null
				&& plfiBestof.getPlaylistFile().equals(plfiSelected.getPlaylistFile())) {
			plfiSelected = plfiBestof;
		}
		jpSpecials.add(plfiBestof);
		alPlaylistFileItems.add(plfiBestof);
		if (plfiSelected != null
				&& plfiBestof.getPlaylistFile().equals(plfiSelected.getPlaylistFile())) {
			plfiSelected = plfiBestof;
		}
		// Novelties
		if (plfiNovelties == null) {
			plfiNovelties = new PlaylistFileItem(PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES,
					IconLoader.ICON_PLAYLIST_NOVELTIES, new PlaylistFile(
							PlaylistFileItem.PLAYLIST_TYPE_NOVELTIES, "1", null, null), Messages
							.getString("PhysicalPlaylistRepositoryView.17"));
			plfiNovelties.setToolTipText(Messages.getString("PhysicalPlaylistRepositoryView.18"));
			plfiNovelties.addMouseListener(ma);
		} else if (plfiSelected != null
				&& plfiNovelties.getPlaylistFile().equals(plfiSelected.getPlaylistFile())) {
			plfiSelected = plfiNovelties;
		}
		jpSpecials.add(plfiNovelties);
		alPlaylistFileItems.add(plfiNovelties);

		jpRoot.add(jpSpecials);
	}

	abstract public void removeItem(PlaylistFileItem plfiSelected);

	abstract public void play(PlaylistFileItem plfi) throws JajukException;

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(final ActionEvent ae) {
		new Thread() {
			public void run() {
				// Property action is available even for unmounted items
				if (ae.getSource() == jmiProperties) {
					ArrayList<Item> alItems = new ArrayList<Item>(1);
					if (AbstractPlaylistRepositoryView.this instanceof PhysicalPlaylistRepositoryView) {
						alItems.add(plfiSelected.getPlaylistFile());
					} else {
						alItems.add(PlaylistManager.getInstance().getPlayList(
								plfiSelected.getPlaylistFile()));
					}
					new PropertiesWizard(alItems);
				}
				// Others actions: not available for unmounted items
				else {
					if (ae.getSource() == jmiDelete) {
						removeItem(plfiSelected);
						ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
					} else if (ae.getSource() == jmiPlay) {
						try {
							play(plfiSelected);
						} catch (JajukException je) {
							Log.error(je.getCode(), "{{" + plfiSelected.getName() + "}}", null);
							Messages.showErrorMessage(je.getCode(), plfiSelected.getName());
							selectQueue();
							return;
						}
					} else if (ae.getSource() == jmiProperties) {
						ArrayList<Item> alItems = new ArrayList<Item>(1);
						if (AbstractPlaylistRepositoryView.this instanceof PhysicalPlaylistRepositoryView) {
							alItems.add(plfiSelected.getPlaylistFile());
						} else {
							alItems.add(PlaylistManager.getInstance().getPlayList(
									plfiSelected.getPlaylistFile()));
						}
						new PropertiesWizard(alItems);
					} else if (ae.getSource() == jmiSaveAs) { // save as
						try {
							plfiSelected.getPlaylistFile().saveAs();
							ObservationManager.notify(new Event(EventSubject.EVENT_DEVICE_REFRESH));
						} catch (JajukException je) {
							Log.error(je);
							Messages.showErrorMessage(je.getCode());
						} catch (Exception e) {
							Log.error(e);
						}
						ObservationManager.notify(new Event(EventSubject.EVENT_PLAYLIST_REFRESH));
					}
				}
			}
		}.start();
	}

	/**
	 * @return Returns the alPlaylistFileItems.
	 */
	public ArrayList getPlaylistFileItems() {
		return alPlaylistFileItems;
	}

	/**
	 * @return Returns the plfiQueue.
	 */
	public PlaylistFileItem getQueue() {
		return plfiQueue;
	}

}
