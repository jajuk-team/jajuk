/*
 *  Jajuk
 *  Copyright (C) 2006 The Jajuk Team
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
 *  $Revision: 2164 $
 */

package org.jajuk.reporting;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Author;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Item;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

/**
 * This class exports music contents to XML.
 */
public class XMLExporter extends Exporter implements ITechnicalStrings {

	/** Private Constants */
	private final static String NEWLINE = "\n";

	private final static String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>";

	/** PUBLIC METHODS */

	/**
	 * This method will create a tagging of the specified item
	 * 
	 * @param item
	 *            The item to report (can be an album, a year, an author ,a
	 *            style, a directory or a device)
	 * @return Returns a string containing the report, or null if an error
	 *         occurred.
	 */
	public String process(Item item) {
		if (item instanceof Album) {
			return process((Album) item);
		} else if (item instanceof Author) {
			return process((Author) item);
		} else if (item instanceof Style) {
			return process((Style) item);
		} else if (item instanceof Year) {
			return process((Year) item);
		} else if (item instanceof Directory) {
			return process((Directory) item);
		} else if (item instanceof Device) {
			return process((Device) item);
		} else {
			return null;
		}
	}

	/**
	 * This method will create a tagging of the specified album and its tracks.
	 * 
	 * @param album
	 *            The album to tag.
	 * @return Returns a string containing the tagging, or null if an error
	 *         occurred.
	 */
	public String process(Album album) {
		String content = null;

		// Make sure we have an album.
		if (album != null) {
			content = tagAlbum(album, 0);
		}
		return content;
	}

	/**
	 * This method will create a reporting of the specified year and its albums
	 * and associated tracks.
	 * 
	 * @param year
	 *            The year to report.
	 * @return Returns a string containing the report, or null if an error
	 *         occurred.
	 */
	public String process(Year year) {
		String content = null;
		if (year != null) {
			content = tagYear(year, 0);
		}
		return content;
	}

	/**
	 * This method will create a tagging of the specified author and its albums
	 * and associated tracks.
	 * 
	 * @param author
	 *            The author to tag.
	 * @return Returns a string containing the tagging, or null if an error
	 *         occurred.
	 */
	public String process(Author author) {
		String content = null;
		if (author != null) {
			content = tagAuthor(author, 0);
		}
		return content;
	}

	/**
	 * This method will create a tagging of the specified style.
	 * 
	 * @param style
	 *            The style to tag.
	 * @return Returns a string containing the tagging, or null is an error
	 *         occurred.
	 */
	public String process(Style style) {
		String content = null;
		if (style != null) {
			content = tagStyle(style, 0);
		}
		return content;
	}

	/**
	 * This method will create a tagging of a directory and all its children
	 * files and directories.
	 * 
	 * @param directory
	 *            The directory to start from.
	 * @return Returns a string containing the tagging, or null if an error
	 *         occurred.
	 */
	public String process(Directory directory) {
		String content = null;
		if (directory != null) {
			content = tagDirectory(directory, 0);
		}
		return content;
	}

	/**
	 * This method will create a tagging of a device and all its children files
	 * and directories.
	 * 
	 * @param device
	 *            The device to start from.
	 * @return Returns a string containing the tagging, or null if an error
	 *         occurred.
	 */
	public String process(Device device) {
		String content = null;
		if (device != null) {
			content = tagDevice(device, 0);
		}
		return content;
	}

	/**
	 * @see Exporter.processColllection
	 */
	public String processCollection(int type) {
		String content = "";
		// If we are tagging the physical collection...
		if (type == XMLExporter.PHYSICAL_COLLECTION) {
			// Same effect than selecting all devices
			return process(new ArrayList<Item>(DeviceManager.getInstance().getDevices()));
		} else if (type == LOGICAL_COLLECTION) {
			// Same effect than selecting all styles
			return process(new ArrayList<Item>(StyleManager.getInstance().getStyles()));
		}
		return content;
	}

	/** PRIVATE HELPER METHODS */

	private String exportDirectoryHelper(int level, Directory directory) {
		StringBuffer sb = new StringBuffer();
		// Get the children
		ArrayList children = new ArrayList<Directory>(directory.getDirectories());
		ListIterator itr1 = children.listIterator();
		sb.append(addTabs(level) + Tag.openTag(XML_DIRECTORY) + NEWLINE);
		String sName = Util.formatXML(directory.getName());
		String sID = Util.formatXML(directory.getId());
		String sPath = Util.formatXML(directory.getAbsolutePath());
		// Tag directory data.
		sb.append(addTabs(level + 1) + Tag.tagData(XML_ID, sID) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);
		// Tag children directories
		while (itr1.hasNext()) {
			Directory d = (Directory) itr1.next();
			sb.append(exportDirectoryHelper(level + 1, d));
		}
		// Tag children files
		Iterator itr2 = directory.getFiles().iterator();
		while (itr2.hasNext()) {
			org.jajuk.base.File file = (org.jajuk.base.File) itr2.next();
			sb.append(tagFile(file, level + 1));
		}
		sb.append(addTabs(level) + Tag.closeTag(XML_DIRECTORY) + NEWLINE);
		return sb.toString();
	}

	private String tagFile(org.jajuk.base.File file, int level) {
		StringBuffer sb = new StringBuffer();
		String sFileID = file.getId();
		String sName = Util.formatXML(file.getName());
		String sPath = Util.formatXML(file.getAbsolutePath());
		long lSize = file.getSize();
		sb.append(addTabs(level) + Tag.openTag(XML_FILE) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_ID, sFileID) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_SIZE, lSize) + NEWLINE);
		sb.append(tagTrack(file.getTrack(), level + 1));
		sb.append(addTabs(level) + Tag.closeTag(XML_FILE) + NEWLINE);
		return sb.toString();
	}

	private String tagDirectory(Directory directory, int level) {
		String content = null;

		// Make sure we have a directory.
		if (directory != null) {
			StringBuffer sb = new StringBuffer();
			sb.append(Tag.openTag(XML_DIRECTORY) + NEWLINE);
			String sName = Util.formatXML(directory.getName());
			String sPath = Util.formatXML(directory.getAbsolutePath());
			String sID = directory.getId();

			// Tag directory data.
			sb.append(addTabs(1) + Tag.tagData(XML_ID, sID) + NEWLINE);
			sb.append(addTabs(1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
			sb.append(addTabs(1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);

			// Tag directory children data.
			ListIterator itr1 = new ArrayList<Directory>(directory.getDirectories()).listIterator();
			while (itr1.hasNext()) {
				Directory d = (Directory) itr1.next();
				sb.append(exportDirectoryHelper(1, d));
			}
			// Tag directory file children data.
			Iterator itr2 = directory.getFiles().iterator();
			while (itr2.hasNext()) {
				org.jajuk.base.File file = (org.jajuk.base.File) itr2.next();
				sb.append(tagFile(file, 1));
			}
			sb.append(Tag.closeTag(XML_DIRECTORY) + NEWLINE);
			content = sb.toString();
		}
		return content;
	}

	private String tagDevice(Device device, int level) {
		String content = null;
		String sID = device.getId();

		StringBuffer sb = new StringBuffer();
		sb.append(Tag.openTag(XML_DEVICE) + NEWLINE);

		sb.append(addTabs(1) + Tag.tagData(XML_ID, sID) + NEWLINE);
		sb.append(addTabs(1) + Tag.tagData(XML_NAME, Util.formatXML(device.getName())) + NEWLINE);
		sb.append(addTabs(1) + Tag.tagData(XML_TYPE, Util.formatXML(device.getDeviceTypeS()))
				+ NEWLINE);
		sb.append(addTabs(1) + Tag.tagData(XML_URL, Util.formatXML(device.getUrl())) + NEWLINE);
		sb.append(addTabs(1)
				+ Tag.tagData(XML_DEVICE_MOUNT_POINT, Util.formatXML(device.getMountPoint()))
				+ NEWLINE);

		Directory dir = DirectoryManager.getInstance().getDirectoryForIO(device.getFio());
		// check void devices
		if (dir != null) {
			ListIterator itr = new ArrayList<Directory>(dir.getDirectories()).listIterator();
			// Tag children directories of device.
			while (itr.hasNext()) {
				Directory directory = (Directory) itr.next();
				sb.append(exportDirectoryHelper(1, directory));
			}

			Iterator itr2 = DirectoryManager.getInstance().getDirectoryForIO(device.getFio())
					.getFiles().iterator();
			// Tag children files of device.
			while (itr2.hasNext()) {
				org.jajuk.base.File file = (org.jajuk.base.File) itr2.next();
				sb.append(tagFile(file, 1));
			}
		}
		sb.append(Tag.closeTag(XML_DEVICE) + NEWLINE);
		content = sb.toString();
		return content;
	}

	private String tagTrack(Track track, int level) {
		StringBuffer sb = new StringBuffer();
		String sTrackID = track.getId();
		String sTrackName = Util.formatXML(track.getName());
		String sTrackStyle = Util.formatXML(track.getStyle().getName2());
		String sTrackAuthor = Util.formatXML(track.getAuthor().getName2());
		String sTrackAlbum = Util.formatXML(track.getAlbum().getName2());
		long lTrackLength = track.getDuration();
		long lTrackRate = track.getRate();
		String sTrackComment = Util.formatXML(track.getComment());
		long lTrackOrder = track.getOrder();
		sb.append(addTabs(level) + Tag.openTag(XML_TRACK) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_ID, sTrackID) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_TRACK_NAME, sTrackName) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_TRACK_STYLE, sTrackStyle) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_TRACK_AUTHOR, sTrackAuthor) + NEWLINE);
		sb
				.append(addTabs(level + 1)
						+ Tag.tagData(XML_TRACK_LENGTH, Util.formatTimeBySec(lTrackLength, true))
						+ NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_TRACK_RATE, lTrackRate) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_TRACK_COMMENT, sTrackComment) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_TRACK_ORDER, Util.padNumber(lTrackOrder, 2))
				+ NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_TRACK_ALBUM, sTrackAlbum) + NEWLINE);
		sb.append(addTabs(level) + Tag.closeTag(XML_TRACK) + NEWLINE);
		return sb.toString();
	}

	private String tagAlbum(Album album, int level) {
		StringBuffer sb = new StringBuffer();
		String sAlbumID = album.getId();
		String sAlbumName = Util.formatXML(album.getName2());
		String sStyleName = "";
		String sAuthorName = "";
		String sYear = "";
		Set<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album);
		if (tracks.size() > 0) {
			sStyleName = Util.formatXML(tracks.iterator().next().getStyle().getName2());
			sAuthorName = Util.formatXML(tracks.iterator().next().getAuthor().getName2());
			sYear = tracks.iterator().next().getYear().getName2();
		}
		sb.append(addTabs(level) + Tag.openTag(XML_ALBUM) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_ID, sAlbumID) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_NAME, sAlbumName) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_AUTHOR, sAuthorName) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_STYLE, sStyleName) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_YEAR, sYear) + NEWLINE);
		for (Track track : tracks) {
			sb.append(tagTrack(track, level + 1));
		}
		sb.append(addTabs(level) + Tag.closeTag(XML_ALBUM) + NEWLINE);
		return sb.toString();
	}

	private String tagAuthor(Author author, int level) {
		// long l = System.currentTimeMillis();
		String sAuthorID = author.getId();
		String sAuthorName = Util.formatXML(author.getName2());
		StringBuffer sb = new StringBuffer();
		sb.append(addTabs(level) + Tag.openTag(XML_AUTHOR) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_ID, sAuthorID) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_NAME, sAuthorName) + NEWLINE);
		Set<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(author);
		for (Album album : albums) {
			sb.append(tagAlbum(album, level + 1));
		}
		sb.append(addTabs(level) + Tag.closeTag(XML_AUTHOR) + NEWLINE);
		// System.out.println(System.currentTimeMillis() - l);
		return sb.toString();
	}

	private String tagYear(Year year, int level) {
		String sYearID = year.getId();
		StringBuffer sb = new StringBuffer();
		String sYearName = year.getName();
		sb.append(addTabs(level) + Tag.openTag(XML_YEAR) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_ID, sYearID) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_NAME, sYearName) + NEWLINE);
		for (Album album : AlbumManager.getInstance().getAssociatedAlbums(year)) {
			sb.append(tagAlbum(album, level + 1));
		}
		sb.append(addTabs(level) + Tag.closeTag(XML_YEAR) + NEWLINE);
		return sb.toString();
	}

	private String tagStyle(Style style, int level) {
		StringBuffer sb = new StringBuffer();
		String sStyleID = style.getId();
		String sStyleName = Util.formatXML(style.getName2());
		sb.append(addTabs(level) + Tag.openTag(XML_STYLE) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_ID, sStyleID) + NEWLINE);
		sb.append(addTabs(level + 1) + Tag.tagData(XML_NAME, sStyleName) + NEWLINE);
		for (Album album : AlbumManager.getInstance().getAssociatedAlbums(style)) {
			sb.append(tagAlbum(album, level + 1));
		}
		for (Author author : AuthorManager.getInstance().getAssociatedAuthors(style)) {
			sb.append(tagAuthor(author, level + 1));
		}
		sb.append(addTabs(level) + Tag.closeTag(XML_STYLE) + NEWLINE);
		return sb.toString();
	}

	private String addTabs(int num) {
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < num) {
			sb.append('\t');
			i++;
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.reporting.Exporter#process(java.util.ArrayList)
	 */
	@Override
	public String process(ArrayList<Item> collection) {
		StringBuffer sb = new StringBuffer(XML_HEADER + NEWLINE + Tag.openTag(XML_COLLECTION)
				+ NEWLINE);
		for (Item item : collection) {
			sb.append(process(item));
		}
		// Add I18N nodes
		sb.append(Tag.openTag("i18n"));
		int i = 1;
		while (Messages.getInstance().contains("ReportAction." + i)) {
			sb.append('\t' + Tag.tagData("ReportAction." + i, Messages.getString("ReportAction."
					+ i)));
			i++;
		}
		sb.append('\t' + Tag.tagData("ReportAction.name", Messages.getString("Property_name")));
		sb.append('\t' + Tag.tagData("ReportAction.author", Messages.getString("Property_author")));
		sb.append('\t' + Tag.tagData("ReportAction.style", Messages.getString("Property_style")));
		sb.append('\t' + Tag.tagData("ReportAction.order", Messages.getString("Property_track")));
		sb.append('\t' + Tag.tagData("ReportAction.track", Messages.getString("Item_Track")));
		sb.append('\t' + Tag.tagData("ReportAction.album", Messages.getString("Property_album")));
		sb.append('\t' + Tag.tagData("ReportAction.length", Messages.getString("Property_length")));
		sb.append('\t' + Tag.tagData("ReportAction.year", Messages.getString("Property_year")));
		sb.append('\t' + Tag.tagData("ReportAction.rate", Messages.getString("Property_rate")));
		sb.append('\t' + Tag.tagData("ReportAction.url", Messages.getString("Property_url")));
		sb.append('\t' + Tag.tagData("ReportAction.type", Messages.getString("Property_type")));
		sb.append('\t' + Tag
				.tagData("ReportAction.comment", Messages.getString("Property_comment")));
		sb.append(Tag.closeTag("i18n"));

		sb.append(Tag.closeTag(XML_COLLECTION));
		return sb.toString();
	}
}

/**
 * This class will create taggings. It will create either open tags, closed
 * tags, or full tagging with data.
 */
class Tag {
	public static String openTag(String tagname) {
		return "<" + tagname + ">";
	}

	public static String closeTag(String tagname) {
		return "</" + tagname + ">";
	}

	public static String tagData(String tagname, String data) {
		return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
	}

	public static String tagData(String tagname, long data) {
		return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
	}

	public static String tagData(String tagname, int data) {
		return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
	}

	public static String tagData(String tagname, double data) {
		return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
	}
}
