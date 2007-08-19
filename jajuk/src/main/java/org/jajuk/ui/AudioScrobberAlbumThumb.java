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

package org.jajuk.ui;

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.vlsolutions.swing.docking.ShadowBorder;

import ext.services.lastfm.AudioScrobblerAlbum;

/**
 * Last.FM Album thumb represented as album cover + (optionally) others text
 * information display...
 */
public class AudioScrobberAlbumThumb extends JPanel implements ITechnicalStrings {

	private static final long serialVersionUID = 1L;

	/** Associated album */
	AudioScrobblerAlbum album;

	public JLabel jlIcon;

	static private long lDateLastMove;

	static private Point lastPosition;

	/** Current details dialog */
	private static AlbumPopup details;

	private static AudioScrobberAlbumThumb last;

	private static AudioScrobberAlbumThumb mouseOverItem = null;

	/** Timer used to launch popup */
	static {
		Timer timerPopup = new Timer(200, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					// Close popup ASAP when over none catalog item
					if (mouseOverItem == null) {
						if (details != null) {
							details.dispose();
						}
						last = null;
						// display a popup after n seconds only if item changed
					} else if ((System.currentTimeMillis() - lDateLastMove >= 1000)
							&& mouseOverItem != last) {
						// close popup if any visible
						if (details != null) {
							details.dispose();
						}
						// Store current item
						last = mouseOverItem;
						// Finally display the popup (Leave if user unselected
						// the option "Show catalog popups"
						if (ConfigurationManager.getBoolean(CONF_SHOW_POPUPS)) {
							mouseOverItem.displayPopup();
						}
					}
				} catch (Exception e) {
					// Make sure not to exit
					Log.error(e);
				}
			}
		});
		timerPopup.start();
	}

	/**
	 * Constructor
	 * 
	 * @param album :
	 *            associated album
	 */
	public AudioScrobberAlbumThumb(AudioScrobblerAlbum album) {
		this.album = album;
	}

	/**
	 * display a popup over the catalog item
	 */
	public void displayPopup() {
		AlbumPopup popup = new AlbumPopup(album, jlIcon);
		details = popup;
	}

	public void populate() throws Exception{
		jlIcon = new JLabel();
		//Download thumb
		URL remote = new URL(album.getCoverURL());
		DownloadManager.downloadCover(remote);
		String cache = Util.getConfFileByPath(FILE_CACHE).getAbsolutePath() + '/'
				+ Util.getOnlyFile(remote.toString());
		ImageIcon downloadedImage = new ImageIcon(cache);
		ImageIcon ii = Util.getResizedImage(downloadedImage, 100, 100);
		jlIcon.setIcon(ii);
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		add(jlIcon);
		jlIcon.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				super.mouseMoved(e);
				lDateLastMove = System.currentTimeMillis();
				lastPosition = e.getLocationOnScreen();
			}

		});

		jlIcon.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				mouseOverItem = AudioScrobberAlbumThumb.this;
			}
			public void mouseExited(MouseEvent e) {
				// Consider an exit only if mouse really moved to avoid
				// closing popup when popup appears over the mouse cursor
				// (then, a mouseExit event is thrown)
				if (!e.getLocationOnScreen().equals(lastPosition)) {
					mouseOverItem = null;
				}
			}
		});
		jlIcon.setBorder(new ShadowBorder());
	}

	/**
	 * @return the album
	 */
	public AudioScrobblerAlbum getAlbum() {
		return album;
	}

}
