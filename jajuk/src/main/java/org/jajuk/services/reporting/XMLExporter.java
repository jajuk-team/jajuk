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

package org.jajuk.services.reporting;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Set;

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
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * This class exports music contents to XML.
 */
public class XMLExporter extends Exporter implements ITechnicalStrings {

  /** Private Constants */
  private final static String NEWLINE = "\n";

  private final static String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>";

  private BufferedWriter writer;

  /** Do we want to export tracks ?* */
  private boolean showTracks = true;

  /** PUBLIC METHODS */

  public XMLExporter() throws Exception {
    cache = Util
        .getConfFileByPath(FILE_REPORTING_CACHE_FILE + "_XML_" + System.currentTimeMillis());
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cache, false), "UTF-8"));
  }

  /**
   * This method will create a tagging of the specified item
   * 
   * @param item
   *          The item to report (can be an album, a year, an author ,a style, a
   *          directory or a device)
   * @return Returns a string containing the report, or null if an error
   *         occurred.
   */
  public void process(Item item) throws Exception {
    if (item instanceof Album) {
      process((Album) item);
    } else if (item instanceof Author) {
      process((Author) item);
    } else if (item instanceof Style) {
      process((Style) item);
    } else if (item instanceof Year) {
      process((Year) item);
    } else if (item instanceof Directory) {
      process((Directory) item);
    } else if (item instanceof Device) {
      process((Device) item);
    }
  }

  /**
   * This method will create a tagging of the specified album and its tracks.
   * 
   * @param album
   *          The album to tag.
   * @return Returns a string containing the tagging, or null if an error
   *         occurred.
   */
  public void process(Album album) throws Exception {
    // Make sure we have an album.
    if (album != null) {
      tagAlbum(album, 0);
    }
  }

  /**
   * This method will create a reporting of the specified year and its albums
   * and associated tracks.
   * 
   * @param year
   *          The year to report.
   * @return Returns a string containing the report, or null if an error
   *         occurred.
   */
  public void process(Year year) throws Exception {
    if (year != null) {
      tagYear(year, 0);
    }
  }

  /**
   * This method will create a tagging of the specified author and its albums
   * and associated tracks.
   * 
   * @param author
   *          The author to tag.
   * @return Returns a string containing the tagging, or null if an error
   *         occurred.
   */
  public void process(Author author) throws Exception {
    if (author != null) {
      tagAuthor(author, 0);
    }
  }

  /**
   * This method will create a tagging of the specified style.
   * 
   * @param style
   *          The style to tag.
   * @return Returns a string containing the tagging, or null is an error
   *         occurred.
   */
  public void process(Style style) throws Exception {
    if (style != null) {
      tagStyle(style, 0);
    }
  }

  /**
   * This method will create a tagging of a directory and all its children files
   * and directories.
   * 
   * @param directory
   *          The directory to start from.
   * @return Returns a string containing the tagging, or null if an error
   *         occurred.
   */
  public void process(Directory directory) throws Exception {
    if (directory != null) {
      tagDirectory(directory, 0);
    }
  }

  /**
   * This method will create a tagging of a device and all its children files
   * and directories.
   * 
   * @param device
   *          The device to start from.
   * @return Returns a string containing the tagging, or null if an error
   *         occurred.
   */
  public void process(Device device) throws Exception {
    if (device != null) {
      tagDevice(device, 0);
    }
  }

  /**
   * @see Exporter.processColllection
   */
  public void processCollection(int type) throws Exception {
    // If we are tagging the physical collection...
    if (type == XMLExporter.PHYSICAL_COLLECTION) {
      // Same effect than selecting all devices
      process(new ArrayList<Item>(DeviceManager.getInstance().getDevices()));
    } else if (type == LOGICAL_COLLECTION) {
      // Same effect than selecting all styles
      process(new ArrayList<Item>(StyleManager.getInstance().getStyles()));
    }
  }

  /** PRIVATE HELPER METHODS */

  private void exportDirectoryHelper(int level, Directory directory) throws Exception {
    // Get the children
    ArrayList<Directory> children = new ArrayList<Directory>(directory.getDirectories());
    writer.write(addTabs(level) + Tag.openTag(XML_DIRECTORY) + NEWLINE);
    String sName = Util.formatXML(directory.getName());
    String sID = Util.formatXML(directory.getID());
    String sPath = Util.formatXML(directory.getAbsolutePath());
    // Tag directory data.
    writer.write(addTabs(level + 1) + Tag.tagData(XML_ID, sID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);
    // Tag children directories
    for (Directory d : children) {
      exportDirectoryHelper(level + 1, d);
    }
    // Tag children files
    for (org.jajuk.base.File file : directory.getFiles()) {
      tagFile(file, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(XML_DIRECTORY) + NEWLINE);
  }

  private void tagFile(org.jajuk.base.File file, int level) throws Exception {
    String sFileID = file.getID();
    String sName = Util.formatXML(file.getName());
    String sPath = Util.formatXML(file.getAbsolutePath());
    long lSize = file.getSize();
    writer.write(addTabs(level) + Tag.openTag(XML_FILE) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_ID, sFileID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_SIZE, lSize) + NEWLINE);
    tagTrack(file.getTrack(), level + 1);
    writer.write(addTabs(level) + Tag.closeTag(XML_FILE) + NEWLINE);
  }

  private void tagDirectory(Directory directory, int level) throws Exception {
    // Make sure we have a directory.
    if (directory != null) {
      writer.write(Tag.openTag(XML_DIRECTORY) + NEWLINE);
      String sName = Util.formatXML(directory.getName());
      String sPath = Util.formatXML(directory.getAbsolutePath());
      String sID = directory.getID();

      // Tag directory data.
      writer.write(addTabs(1) + Tag.tagData(XML_ID, sID) + NEWLINE);
      writer.write(addTabs(1) + Tag.tagData(XML_NAME, sName) + NEWLINE);
      writer.write(addTabs(1) + Tag.tagData(XML_PATH, sPath) + NEWLINE);

      // Tag directory children data.
      for (Directory d : new ArrayList<Directory>(directory.getDirectories())) {
        exportDirectoryHelper(1, d);
      }
      // Tag directory file children data.
      for (org.jajuk.base.File file : directory.getFiles()) {
        tagFile(file, 1);
      }
      writer.write(Tag.closeTag(XML_DIRECTORY) + NEWLINE);
    }
  }

  private void tagDevice(Device device, int level) throws Exception {
    String sID = device.getID();
    writer.write(Tag.openTag(XML_DEVICE) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(XML_ID, sID) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(XML_NAME, Util.formatXML(device.getName())) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(XML_TYPE, Util.formatXML(device.getDeviceTypeS()))
        + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(XML_URL, Util.formatXML(device.getUrl())) + NEWLINE);
    Directory dir = DirectoryManager.getInstance().getDirectoryForIO(device.getFio());
    // check void devices
    if (dir != null) {
      // Tag children directories of device.
      for (Directory directory : new ArrayList<Directory>(dir.getDirectories())) {
        exportDirectoryHelper(1, directory);
      }
      // Tag children files of device.
      for (org.jajuk.base.File file : DirectoryManager.getInstance().getDirectoryForIO(
          device.getFio()).getFiles()) {
        tagFile(file, 1);
      }
    }
    writer.write(Tag.closeTag(XML_DEVICE) + NEWLINE);
  }

  private void tagTrack(Track track, int level) throws Exception {
    String sTrackID = track.getID();
    String sTrackName = Util.formatXML(track.getName());
    String sTrackStyle = Util.formatXML(track.getStyle().getName2());
    String sTrackAuthor = Util.formatXML(track.getAuthor().getName2());
    String sTrackAlbum = Util.formatXML(track.getAlbum().getName2());
    long lTrackLength = track.getDuration();
    long lTrackRate = track.getRate();
    String sTrackComment = Util.formatXML(track.getComment());
    long lTrackOrder = track.getOrder();
    writer.write(addTabs(level) + Tag.openTag(XML_TRACK) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_ID, sTrackID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_TRACK_NAME, sTrackName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_TRACK_STYLE, sTrackStyle) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_TRACK_AUTHOR, sTrackAuthor) + NEWLINE);
    writer.write(addTabs(level + 1)
        + Tag.tagData(XML_TRACK_LENGTH, Util.formatTimeBySec(lTrackLength, true)) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_TRACK_RATE, lTrackRate) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_TRACK_COMMENT, sTrackComment) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_TRACK_ORDER, Util.padNumber(lTrackOrder, 2))
        + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_TRACK_ALBUM, sTrackAlbum) + NEWLINE);
    writer.write(addTabs(level) + Tag.closeTag(XML_TRACK) + NEWLINE);
  }

  private void tagAlbum(Album album, int level) throws Exception {
    String sAlbumID = album.getID();
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
    writer.write(addTabs(level) + Tag.openTag(XML_ALBUM) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_ID, sAlbumID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_NAME, sAlbumName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_AUTHOR, sAuthorName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_STYLE, sStyleName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_YEAR, sYear) + NEWLINE);
    // For full collection, we don't show detailed tracks for performance
    // reasons
    if (showTracks) {
      for (Track track : tracks) {
        tagTrack(track, level + 1);
      }
    }
    writer.write(addTabs(level) + Tag.closeTag(XML_ALBUM) + NEWLINE);
  }

  private void tagAuthor(Author author, int level) throws Exception {
    String sAuthorID = author.getID();
    String sAuthorName = Util.formatXML(author.getName2());
    writer.write(addTabs(level) + Tag.openTag(XML_AUTHOR) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_ID, sAuthorID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_NAME, sAuthorName) + NEWLINE);
    Set<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(author);
    for (Album album : albums) {
      tagAlbum(album, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(XML_AUTHOR) + NEWLINE);
  }

  private void tagYear(Year year, int level) throws Exception {
    String sYearID = year.getID();
    String sYearName = year.getName();
    writer.write(addTabs(level) + Tag.openTag(XML_YEAR) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_ID, sYearID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_NAME, sYearName) + NEWLINE);
    for (Album album : AlbumManager.getInstance().getAssociatedAlbums(year)) {
      tagAlbum(album, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(XML_YEAR) + NEWLINE);
  }

  private void tagStyle(Style style, int level) throws Exception {
    String sStyleID = style.getID();
    String sStyleName = Util.formatXML(style.getName2());
    writer.write(addTabs(level) + Tag.openTag(XML_STYLE) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_ID, sStyleID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(XML_NAME, sStyleName) + NEWLINE);
    for (Album album : AlbumManager.getInstance().getAssociatedAlbums(style)) {
      tagAlbum(album, level + 1);
    }
    for (Author author : AuthorManager.getInstance().getAssociatedAuthors(style)) {
      tagAuthor(author, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(XML_STYLE) + NEWLINE);
  }

  private String addTabs(int num) {
    StringBuilder sb = new StringBuilder();
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
  public void process(ArrayList<Item> collection) throws Exception {
    try {
      writer.write(XML_HEADER + NEWLINE + Tag.openTag(XML_COLLECTION) + NEWLINE);
      for (Item item : collection) {
        process(item);
      }
      // Add I18N nodes
      writer.write(Tag.openTag("i18n"));
      int i = 1;
      while (Messages.getInstance().contains("ReportAction." + i)) {
        writer.write('\t' + Tag.tagData("ReportAction." + i, Messages
            .getString("ReportAction." + i)));
        i++;
      }
      writer.write('\t' + Tag.tagData("ReportAction.name", Messages.getString("Property_name")));
      writer
          .write('\t' + Tag.tagData("ReportAction.author", Messages.getString("Property_author")));
      writer.write('\t' + Tag.tagData("ReportAction.style", Messages.getString("Property_style")));
      writer.write('\t' + Tag.tagData("ReportAction.order", Messages.getString("Property_track")));
      writer.write('\t' + Tag.tagData("ReportAction.track", Messages.getString("Item_Track")));
      writer.write('\t' + Tag.tagData("ReportAction.album", Messages.getString("Property_album")));
      writer
          .write('\t' + Tag.tagData("ReportAction.length", Messages.getString("Property_length")));
      writer.write('\t' + Tag.tagData("ReportAction.year", Messages.getString("Property_year")));
      writer.write('\t' + Tag.tagData("ReportAction.rate", Messages.getString("Property_rate")));
      writer.write('\t' + Tag.tagData("ReportAction.url", Messages.getString("Property_url")));
      writer.write('\t' + Tag.tagData("ReportAction.type", Messages.getString("Property_type")));
      writer.write('\t' + Tag.tagData("ReportAction.comment", Messages
          .getString("Property_comment")));
      writer.write(Tag.closeTag("i18n"));
      writer.write(Tag.closeTag(XML_COLLECTION));
    } catch (Exception e) {
      throw e;
    } finally {
      writer.flush();
      writer.close();
    }

  }

  protected void setShowTracks(boolean showTracks) {
    this.showTracks = showTracks;
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
