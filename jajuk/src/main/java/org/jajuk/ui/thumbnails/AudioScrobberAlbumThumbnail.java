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

package org.jajuk.ui.thumbnails;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Item;
import org.jajuk.i18n.Messages;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.VerticalLayout;
import org.jvnet.substance.SubstanceLookAndFeel;

import java.awt.Color;
import java.awt.MediaTracker;
import java.io.File;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.vlsolutions.swing.docking.ShadowBorder;

import ext.services.lastfm.AudioScrobblerAlbum;
import ext.services.lastfm.AudioScrobblerService;
import ext.services.lastfm.AudioScrobblerTrack;
import ext.services.network.NetworkUtils;

/**
 * Last.FM Album thumb represented as album cover + (optionally) others text
 * information display...
 */
public class AudioScrobberAlbumThumbnail extends AbstractThumbnail {

	private static final long serialVersionUID = -804471264407148566L;

	/** Associated album */
	AudioScrobblerAlbum album;

	/** Popup thumbnail cache */
	File fThumb;

	/**
	 * @param album :
	 *            associated album
	 */
	public AudioScrobberAlbumThumbnail(AudioScrobblerAlbum album) {
		super(100);
		this.album = album;
	}

	public void populate() throws Exception {
		jlIcon = new JLabel();
		// Download thumb
		URL remote = new URL(album.getCoverURL());
		DownloadManager.downloadCover(remote);
		String cache = Util.getConfFileByPath(FILE_CACHE).getAbsolutePath() + '/'
				+ Util.getOnlyFile(remote.toString());
		// Store file reference (to generate the popup thumb for ie)
		fCover = new File(cache);
		fThumb = Util.getConfFileByPath(FILE_CACHE + "/" + System.currentTimeMillis() + '.'
				+ Util.getExtension(fCover));

		ImageIcon downloadedImage = new ImageIcon(cache);
		ImageIcon ii = Util.getResizedImage(downloadedImage, 100, 100);
		if (ii.getImageLoadStatus() != MediaTracker.COMPLETE) {
			Log.debug("Image Loading status: " + ii.getImageLoadStatus());
		}
		// Free images memory
		downloadedImage.getImage().flush();
		ii.getImage().flush();
		postPopulate();
		jlIcon.setIcon(ii);
		setLayout(new VerticalLayout(2));
		add(jlIcon);
		JLabel jlTitle = new JLabel(Util.getLimitedString(album.getTitle(), 15));
		jlTitle.setToolTipText(album.getTitle());
		add(jlTitle);
		jlIcon.setBorder(new ShadowBorder());
		// disable inadequate menu items
		jmenu.remove(jmiAlbumCDDBWizard);
		jmenu.remove(jmiGetCovers);
		if (getItem() == null) {
			jmenu.remove(jmiAlbumPlay);
			jmenu.remove(jmiAlbumPlayRepeat);
			jmenu.remove(jmiAlbumPlayShuffle);
			jmenu.remove(jmiAlbumPush);
			jmenu.remove(jmiAlbumProperties);
		}
		//Set URL to open
		jmiOpenLastFMSite.putClientProperty(DETAIL_CONTENT, "http://www.lastfm.fr/music/"
				+ NetworkUtils.encodeString(album.getArtist()) + '/'
				+ NetworkUtils.encodeString(album.getTitle()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getItem()
	 */
	@Override
	public Item getItem() {
		Album item = AlbumManager.getInstance().getAlbumByName(album.getTitle());
		if (item != null) {
			return item;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.thumbnails.AbstractThumbnail#getDescription()
	 */
	@Override
	String getDescription() {
		// Create the thumb image if needed
		try {
			Util.createThumbnail(fCover, fThumb, 200);
		} catch (Exception e) {
			Log.error(e);
		}
		// populate album detail
		if (album.getTracks() == null) {
			AudioScrobblerAlbum album = AudioScrobblerService.getInstance().getAlbum(
					this.album.getArtist(), this.album.getTitle());
			if (album != null) {
				this.album = album;
			}
		}
		Color bgcolor = SubstanceLookAndFeel.getActiveColorScheme().getUltraLightColor();
		Color fgcolor = SubstanceLookAndFeel.getActiveColorScheme().getForegroundColor();
		String sOut = "<html bgcolor='#" + Util.getHTMLColor(bgcolor) + "'><TABLE color='"
				+ Util.getHTMLColor(fgcolor) + "'><TR><TD VALIGN='TOP'> <b>" + album.getTitle()
				+ "</b><br><br>";
		// display cover if available
		if (fThumb.canRead()) {
			sOut += "<img src='file:" + fThumb.getAbsolutePath() + "'><br>";
		}
		// Display author as global value only if it is a single author album
		// We use file://<item type>?<item id> as HTML hyperlink format
		sOut += "<br>" + Messages.getString("Property_author") + " : " + album.getArtist();
		// Display year if available
		String year = album.getYear();
		if (!Util.isVoid(year)) {
			sOut += "<br>" + Messages.getString("Property_year") + " : " + year;
		}
		sOut += "</TD><TD>";
		// Show each track detail if available
		if (album.getTracks() != null) {
			for (AudioScrobblerTrack track : album.getTracks()) {
				sOut += "<b>" + track.getTitle() + "</b><br>";
			}
		}
		sOut += "</TD></TR></TABLE></html>";
		return sOut;
	}
}
