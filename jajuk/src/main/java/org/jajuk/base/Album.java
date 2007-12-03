/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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

import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;

/**
 * An Album *
 * <p>
 * Logical item
 */
public class Album extends LogicalItem implements Comparable<Album> {

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
		return "Album[ID=" + getID() + " Name={{" + getName() + "}}]";
	}

	/**
	 * Alphabetical comparator used to display ordered lists
	 * 
	 * @param other
	 *            item to be compared
	 * @return comparison result
	 */
	public int compareTo(Album otherAlbum) {
		// compare using name and id to differentiate unknown items
		StringBuilder current = new StringBuilder(getName2());
		current.append(getID());
		StringBuilder other = new StringBuilder(otherAlbum.getName2());
		other.append(otherAlbum.getID());
		return current.toString().compareToIgnoreCase(other.toString());
	}

	/**
	 * @return whether the album is Unknown or not
	 */
	public boolean isUnknown() {
		return this.getName().equals(UNKNOWN_ALBUM);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIdentifier()
	 */
	final public String getLabel() {
		return XML_ALBUM;
	}

	/**
	 * Get item description
	 */
	public String getDesc() {
		return Messages.getString("Item_Album") + " : " + getName2();
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
		Set<Track> tracks = TrackManager.getInstance()
				.getAssociatedTracks(this);
		if (tracks.size() == 0) {
			return null;
		}
		// List if directories we have to look in
		HashSet<Directory> dirs = new HashSet<Directory>(2);
		for (Track track : tracks) {
			for (org.jajuk.base.File file : track.getFiles()) {
				if (file.isReady()) {
					// note that hashset ensures directory unicity
					dirs.add(file.getDirectory());
				}
			}
		}
		// look for absolute cover in collection
		for (Directory dir : dirs) {
			String sAbsolut = dir.getStringValue(XML_DIRECTORY_DEFAULT_COVER);
			if (sAbsolut != null && !sAbsolut.trim().equals("")) {
				File fAbsoluteDefault = new File(dir.getAbsolutePath() + '/'
						+ sAbsolut);
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
					if (sExt.equalsIgnoreCase("jpg")
							|| sExt.equalsIgnoreCase("png")
							|| sExt.equalsIgnoreCase("gif")) {
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
					if (sExt.equalsIgnoreCase("jpg")
							|| sExt.equalsIgnoreCase("png")
							|| sExt.equalsIgnoreCase("gif")) {
						return files[i];
					}
				}
			}
		}
		return fCover;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Item#getIconRepresentation()
	 */
	@Override
	public ImageIcon getIconRepresentation() {
		return IconLoader.ICON_ALBUM;
	}

	/**
	 * @return album average rating
	 */
	public long getRate() {
		float rate = 0f;
		int nb = 0;
		for (Track track : TrackManager.getInstance().getAssociatedTracks(this)) {
			rate += track.getRate();
			nb++;
		}
		return Math.round(rate / nb);
	}

	/**
	 * 
	 * @param size
	 *            size using format 100x100
	 * @return album thumb for given size
	 */
	public ImageIcon getThumbnail(String size) {
		File fCover = Util.getConfFileByPath(FILE_THUMBS + '/' + size + '/'
				+ getID() + '.' + EXT_THUMB);
		// Check if thumb already exists
		if (!fCover.exists() || fCover.length() == 0) {
			return IconLoader.noCoversCache.get(size);
		}
		// Create the image using Toolkit and not ImageIO API to be able to
		// flush all the image data
		Image img = Toolkit.getDefaultToolkit().getImage(
				fCover.getAbsolutePath());
		// Free thumb memory
		img.flush();
		return new ImageIcon(img);
	}

}
