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
package org.jajuk.base;

import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * An Album *
 * <p>
 * Logical item
 * 
 * @Author Bertrand Florat
 * @created 17 oct. 2003
 */
public class Album extends Item implements Comparable {

	private static final long serialVersionUID = 1L;

	/**
	 * Album constructor
	 * 
	 * @param id
	 * @param sName
	 */
	public Album(String sId, String sName) {
		super(sId, sName);
	}

	/**
	 * Return album name, dealing with unkwnown for any language
	 * 
	 * @return album name
	 */
	public String getName2() {
		String sOut = getName();
		if (sOut.equals(UNKNOWN_ALBUM)) {
			sOut = Messages.getString(UNKNOWN_ALBUM);
		}
		return sOut;
	}

	/**
	 * toString method
	 */
	public String toString() {
		return "Album[ID=" + getId() + " Name={{" + getName() + "}}]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
	}

	/**
	 * Equal method to check two albums are identical
	 * 
	 * @param otherAlbum
	 * @return
	 */
	public boolean equals(Object otherAlbum) {
		if (otherAlbum == null) {
			return false;
		}
		return this.getId().equals(((Album) otherAlbum).getId());
	}

	/**
	 * Album hashcode ( used by the equals method )
	 */
	public int hashCode() {
		return getId().hashCode();
	}

	/**
	 * Alphabetical comparator used to display ordered lists
	 * 
	 * @param other
	 *            item to be compared
	 * @return comparaison result
	 */
	public int compareTo(Object o) {
		Album otherAlbum = (Album) o;
		// compare using name and id to differenciate unknown items
		return (getName2() + getId()).compareToIgnoreCase(otherAlbum.getName2()
				+ otherAlbum.getId());
	}

	/**
	 * @return whether the albumr is Unknown or not
	 */
	public boolean isUnknown() {
		return this.getName().equals(UNKNOWN_ALBUM);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIdentifier()
	 */
	final public String getIdentifier() {
		return XML_ALBUM;
	}

	/**
	 * Get item description
	 */
	public String getDesc() {
		return Messages.getString("Item_Album") + " : " + getName2(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getHumanValue(java.lang.String)
	 */
	public String getHumanValue(String sKey) {
		if (XML_NAME.equals(sKey)) {
			return getName2();
		}
		// default
		return super.getHumanValue(sKey);
	}

	/**
	 * 
	 * @return associated best cover file available or null if none
	 */
	public File getCoverFile() {
		File fCover = null;
		File fDir = null; // analyzed directory
		// search for local covers in all directories mapping the current track
		// to reach other devices covers and display them together
		Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(this);
		if (tracks.size() == 0) {
			return null;
		}
		// List if directories we have to look in
		HashSet<Directory> dirs = new HashSet<Directory>(2);
		for (Track track : tracks) {
			for (org.jajuk.base.File file : track.getFiles()) {
				// note that hashset ensures directory unicity
				dirs.add(file.getDirectory());
			}
		}
		// look for absolute cover in collection
		for (Directory dir : dirs) {
			String sAbsolut = dir.getStringValue(XML_DIRECTORY_DEFAULT_COVER);
			if (sAbsolut != null && !sAbsolut.trim().equals("")) { //$NON-NLS-1$
				File fAbsoluteDefault = new File(dir.getAbsolutePath() + '/' + sAbsolut); //$NON-NLS-1$.getAbsoluteFile();
				if (fAbsoluteDefault.canRead()) {
					return fAbsoluteDefault;
				}
			}
		}
		// look for standard cover in collection
		for (Directory dir : dirs) {
			fDir = dir.getFio(); // store this dir
			java.io.File[] files = fDir.listFiles();// null if none file
			// found
			for (int i = 0; files != null && i < files.length; i++) {
				if (files[i].canRead() // test file is readable
						&& files[i].length() < MAX_COVER_SIZE * 1024) {
					// check size to avoid out of memory errors
					String sExt = Util.getExtension(files[i]);
					if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png") || sExt.equalsIgnoreCase("gif")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						if (Util.isStandardCover(files[i].getAbsolutePath())) {
							return files[i];
						}
					}
				}
			}
		}
		// none ? OK, return first cover file we find
		for (Directory dir : dirs) {
			fDir = dir.getFio(); // store this dir
			java.io.File[] files = fDir.listFiles();// null if none file
			// found
			for (int i = 0; files != null && i < files.length; i++) {
				if (files[i].canRead() // test file is readable
						&& files[i].length() < MAX_COVER_SIZE * 1024) {
					// check size to avoid out of memory errors
					String sExt = Util.getExtension(files[i]);
					if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png") || sExt.equalsIgnoreCase("gif")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						return files[i];
					}
				}
			}
		}
		return fCover;
	}

	/**
	 * Build a advanced HTML desciption of the album with covers, total length
	 * etc..
	 * 
	 * @param album
	 * @return the description
	 */
	public String getAdvancedDescription() {
		String size = "200x200";
		Util.refreshThumbnail(this, size);
		java.io.File cover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + '/'
				+ getId() + '.' + EXT_THUMB);
		Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(this);
		Track firstTrack = tracks.iterator().next();
		// Check if this album has a single author or not
		boolean bSingleAuthor = true;
		Author author = firstTrack.getAuthor();
		for (Track track : tracks) {
			if (!track.getAuthor().equals(author)) {
				bSingleAuthor = false;
				break;
			}
		}
		// Check if this album is single style or not
		boolean bSingleStyle = true;
		Style style = firstTrack.getStyle();
		for (Track track : tracks) {
			if (!track.getStyle().equals(style)) {
				bSingleStyle = false;
				break;
			}
		}
		// Check if this album is single year or not
		boolean bSingleYear = true;
		long year = firstTrack.getYear().getValue();
		for (Track track : tracks) {
			if (track.getYear().getValue() != year) {
				bSingleYear = false;
				break;
			}
		}
		String sOut = "<html><TABLE><TR><TD VALIGN='TOP'> <b>" + getName2() + "</b><br><br>";
		// display cover if available
		if (cover.canRead()) {
			sOut += "<img src='file:" + cover.getAbsolutePath() + "'/><br>";
		}
		// Display author as global value only if it is a single author album
		if (bSingleAuthor) {
			sOut += "<br>" + Messages.getString("Property_author") + ": "
					+ firstTrack.getAuthor().getName2();
		}
		// Display style
		if (bSingleStyle) {
			sOut += "<br>" + Messages.getString("Property_style") + ": "
					+ firstTrack.getStyle().getName2();
		}
		// Display year
		if (bSingleYear) {
			sOut += "<br>" + Messages.getString("Property_year") + ": " + firstTrack.getYear();
		}
		// Compute total length in secs
		long length = 0;
		for (Track track : tracks) {
			length += track.getLength();
		}
		sOut += "<br>" + Messages.getString("Property_length") + ": "
				+ Util.formatTimeBySec(length, false) + "</TD><TD VALIGN='TOP'><br>";
		// Show each track detail
		for (Track track : tracks) {
			sOut += "<br><b>" + track.getName() + " (";
			sOut += Util.formatTimeBySec(track.getLength(), false) + ") ";
			if (!bSingleYear
					&& track.getYear().getValue() != 0 ) {
				sOut += " - "+track.getYear()+ "   ";
			}
			//Show author if known and if it is not already shown at album level
			if (!bSingleAuthor
					&& !track.getAuthor().getName2().equals(Messages.getString(UNKNOWN_AUTHOR))) {
				sOut += " - "+track.getAuthor().getName2() + "   ";
			}
		}
		sOut += "</TD></TR></TABLE></html>";
		return sOut;
	}
}
