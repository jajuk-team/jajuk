/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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

package org.jajuk.services.reporting;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.jajuk.base.Album;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.Artist;
import org.jajuk.base.ArtistManager;
import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.base.Item;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Year;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;

/**
 * This class exports music contents to XML.
 */
public class XMLExporter extends Exporter {

  /** Private Constants. */
  private static final String NEWLINE = "\n";

  /** The Constant XML_HEADER.  DOCUMENT_ME */
  private static final String XML_HEADER = "<?xml version='1.0' encoding='UTF-8'?>";

  /** DOCUMENT_ME. */
  private final BufferedWriter writer;

  /** Do we want to export tracks ?*. */
  private boolean showTracks = true;

  /**
   * PUBLIC METHODS.
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   */

  public XMLExporter() throws IOException {
    cache = SessionService.getConfFileByPath(Const.FILE_REPORTING_CACHE_FILE + "_XML_"
        + System.currentTimeMillis());
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cache, false), "UTF-8"));
  }

  /**
   * This method will create a tagging of the specified item.
   * 
   * @param item The item to report (can be an album, a year, an artist, a genre, a
   * directory or a device)
   * 
   * @return Returns a string containing the report, or null if an error
   * occurred.
   * 
   * @throws Exception the exception
   */
  public void process(Item item) throws Exception {
    if (item instanceof Album) {
      process((Album) item);
    } else if (item instanceof Artist) {
      process((Artist) item);
    } else if (item instanceof Genre) {
      process((Genre) item);
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
   * @param album The album to tag.
   * 
   * @return Returns a string containing the tagging, or null if an error
   * occurred.
   * 
   * @throws Exception the exception
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
   * @param year The year to report.
   * 
   * @return Returns a string containing the report, or null if an error
   * occurred.
   * 
   * @throws Exception the exception
   */
  public void process(Year year) throws Exception {
    if (year != null) {
      tagYear(year, 0);
    }
  }

  /**
   * This method will create a tagging of the specified artist and its albums
   * and associated tracks.
   * 
   * @param artist The artist to tag.
   * 
   * @return Returns a string containing the tagging, or null if an error
   * occurred.
   * 
   * @throws Exception the exception
   */
  public void process(Artist artist) throws Exception {
    if (artist != null) {
      tagArtist(artist, 0);
    }
  }

  /**
   * This method will create a tagging of the specified genre.
   * 
   * @param genre The genre to tag.
   * 
   * @return Returns a string containing the tagging, or null is an error
   * occurred.
   * 
   * @throws Exception the exception
   */
  public void process(Genre genre) throws Exception {
    if (genre != null) {
      tagGenre(genre, 0);
    }
  }

  /**
   * This method will create a tagging of a directory and all its children files
   * and directories.
   * 
   * @param directory The directory to start from.
   * 
   * @return Returns a string containing the tagging, or null if an error
   * occurred.
   * 
   * @throws Exception the exception
   */
  public void process(Directory directory) throws Exception {
    if (directory != null) {
      tagDirectory(directory);
    }
  }

  /**
   * This method will create a tagging of a device and all its children files
   * and directories.
   * 
   * @param device The device to start from.
   * 
   * @return Returns a string containing the tagging, or null if an error
   * occurred.
   * 
   * @throws Exception the exception
   */
  public void process(Device device) throws Exception {
    if (device != null) {
      tagDevice(device);
    }
  }

  /**
   * Process collection.
   * 
   * @param type DOCUMENT_ME
   * 
   * @throws Exception the exception
   * 
   * @see Exporter.processColllection
   */
  @Override
  @SuppressWarnings("unchecked")
  public void processCollection(int type) throws Exception {
    // If we are tagging the physical collection...
    if (type == Exporter.PHYSICAL_COLLECTION) {
      // Same effect than selecting all devices
      process((List<Item>) DeviceManager.getInstance().getItems());
    } else if (type == LOGICAL_COLLECTION) {
      // Same effect than selecting all genres
      process((List<Item>) GenreManager.getInstance().getItems());
    }
  }

  /**
   * PRIVATE HELPER METHODS.
   * 
   * @param level DOCUMENT_ME
   * @param directory DOCUMENT_ME
   * 
   * @throws Exception the exception
   */

  private void exportDirectoryHelper(int level, Directory directory) throws Exception {
    // Get the children
    List<Directory> children = new ArrayList<Directory>(directory.getDirectories());
    writer.write(addTabs(level) + Tag.openTag(Const.XML_DIRECTORY) + NEWLINE);
    String sName = UtilString.formatXML(directory.getName());
    String sID = UtilString.formatXML(directory.getID());
    String sPath = UtilString.formatXML(directory.getAbsolutePath());
    // Tag directory data.
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_PATH, sPath) + NEWLINE);
    // Tag children directories
    for (Directory d : children) {
      exportDirectoryHelper(level + 1, d);
    }
    // Tag children files
    for (org.jajuk.base.File file : directory.getFiles()) {
      tagFile(file, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_DIRECTORY) + NEWLINE);
  }

  /**
   * Tag file.
   * DOCUMENT_ME
   * 
   * @param file DOCUMENT_ME
   * @param level DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  private void tagFile(org.jajuk.base.File file, int level) throws Exception {
    String sFileID = file.getID();
    String sName = UtilString.formatXML(file.getName());
    String sPath = UtilString.formatXML(file.getAbsolutePath());
    long lSize = file.getSize();
    writer.write(addTabs(level) + Tag.openTag(Const.XML_FILE) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sFileID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_PATH, sPath) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_SIZE, lSize) + NEWLINE);
    tagTrack(file.getTrack(), level + 1);
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_FILE) + NEWLINE);
  }

  /**
   * Tag directory.
   * DOCUMENT_ME
   * 
   * @param directory DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  private void tagDirectory(Directory directory) throws Exception {
    // Make sure we have a directory.
    if (directory != null) {
      writer.write(Tag.openTag(Const.XML_DIRECTORY) + NEWLINE);
      String sName = UtilString.formatXML(directory.getName());
      String sPath = UtilString.formatXML(directory.getAbsolutePath());
      String sID = directory.getID();

      // Tag directory data.
      writer.write(addTabs(1) + Tag.tagData(Const.XML_ID, sID) + NEWLINE);
      writer.write(addTabs(1) + Tag.tagData(Const.XML_NAME, sName) + NEWLINE);
      writer.write(addTabs(1) + Tag.tagData(Const.XML_PATH, sPath) + NEWLINE);

      // Tag directory children data.
      for (Directory d : new ArrayList<Directory>(directory.getDirectories())) {
        exportDirectoryHelper(1, d);
      }
      // Tag directory file children data.
      for (org.jajuk.base.File file : directory.getFiles()) {
        tagFile(file, 1);
      }
      writer.write(Tag.closeTag(Const.XML_DIRECTORY) + NEWLINE);
    }
  }

  /**
   * Tag device.
   * DOCUMENT_ME
   * 
   * @param device DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  private void tagDevice(Device device) throws Exception {
    String sID = device.getID();
    writer.write(Tag.openTag(Const.XML_DEVICE) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(Const.XML_ID, sID) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(Const.XML_NAME, UtilString.formatXML(device.getName()))
        + NEWLINE);
    writer.write(addTabs(1)
        + Tag.tagData(Const.XML_TYPE, UtilString.formatXML(device.getDeviceTypeS())) + NEWLINE);
    writer.write(addTabs(1) + Tag.tagData(Const.XML_URL, UtilString.formatXML(device.getUrl()))
        + NEWLINE);
    Directory dir = DirectoryManager.getInstance().getDirectoryForIO(device.getFIO(), device);
    // check void devices
    if (dir != null) {
      // Tag children directories of device.
      for (Directory directory : new ArrayList<Directory>(dir.getDirectories())) {
        exportDirectoryHelper(1, directory);
      }
      // Tag children files of device.
      for (org.jajuk.base.File file : DirectoryManager.getInstance()
          .getDirectoryForIO(device.getFIO(), device).getFiles()) {
        tagFile(file, 1);
      }
    }
    writer.write(Tag.closeTag(Const.XML_DEVICE) + NEWLINE);
  }

  /**
   * Tag track.
   * DOCUMENT_ME
   * 
   * @param track DOCUMENT_ME
   * @param level DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  private void tagTrack(Track track, int level) throws Exception {
    String sTrackID = track.getID();
    String sTrackName = UtilString.formatXML(track.getName());
    String sTrackGenre = UtilString.formatXML(track.getGenre().getName2());
    String sTrackArtist = UtilString.formatXML(track.getArtist().getName2());
    String sTrackAlbum = UtilString.formatXML(track.getAlbum().getName2());
    long lTrackLength = track.getDuration();
    long lTrackRate = track.getRate();
    String sTrackComment = UtilString.formatXML(track.getComment());
    long lTrackOrder = track.getOrder();
    long lTrackDiscNumber = track.getDiscNumber();
    writer.write(addTabs(level) + Tag.openTag(Const.XML_TRACK) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sTrackID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_NAME, sTrackName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_GENRE, sTrackGenre) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_ARTIST, sTrackArtist) + NEWLINE);
    writer.write(addTabs(level + 1)
        + Tag.tagData(Const.XML_TRACK_LENGTH, UtilString.formatTimeBySec(lTrackLength)) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_RATE, lTrackRate) + NEWLINE);
    writer
        .write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_COMMENT, sTrackComment) + NEWLINE);
    writer.write(addTabs(level + 1)
        + Tag.tagData(Const.XML_TRACK_ORDER, UtilString.padNumber(lTrackOrder, 2)) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_TRACK_ALBUM, sTrackAlbum) + NEWLINE);
    writer.write(addTabs(level + 1)
        + Tag.tagData(Const.XML_TRACK_DISC_NUMBER, UtilString.padNumber(lTrackDiscNumber, 2))
        + NEWLINE);
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_TRACK) + NEWLINE);
  }

  /**
   * Tag album.
   * DOCUMENT_ME
   * 
   * @param album DOCUMENT_ME
   * @param level DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  private void tagAlbum(Album album, int level) throws Exception {
    String sAlbumID = album.getID();
    String sAlbumName = UtilString.formatXML(album.getName2());
    String sGenreName = "";
    String sArtistName = "";
    String sYear = "";
    List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(album, true);
    if (tracks.size() > 0) {
      sGenreName = UtilString.formatXML(tracks.iterator().next().getGenre().getName2());
      sArtistName = UtilString.formatXML(tracks.iterator().next().getArtist().getName2());
      sYear = tracks.iterator().next().getYear().getName2();
    }
    writer.write(addTabs(level) + Tag.openTag(Const.XML_ALBUM) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sAlbumID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sAlbumName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ARTIST, sArtistName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_GENRE, sGenreName) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_YEAR, sYear) + NEWLINE);
    // For full collection, we don't show detailed tracks for performance
    // reasons
    if (showTracks) {
      for (Track track : tracks) {
        tagTrack(track, level + 1);
      }
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_ALBUM) + NEWLINE);
  }

  /**
   * Tag artist.
   * DOCUMENT_ME
   * 
   * @param artist DOCUMENT_ME
   * @param level DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  private void tagArtist(Artist artist, int level) throws Exception {
    String sArtistID = artist.getID();
    String sArtistName = UtilString.formatXML(artist.getName2());
    writer.write(addTabs(level) + Tag.openTag(Const.XML_ARTIST) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sArtistID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sArtistName) + NEWLINE);
    List<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(artist);
    for (Album album : albums) {
      tagAlbum(album, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_ARTIST) + NEWLINE);
  }

  /**
   * Tag year.
   * DOCUMENT_ME
   * 
   * @param year DOCUMENT_ME
   * @param level DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  private void tagYear(Year year, int level) throws Exception {
    String sYearID = year.getID();
    String sYearName = year.getName();
    writer.write(addTabs(level) + Tag.openTag(Const.XML_YEAR) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sYearID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sYearName) + NEWLINE);
    List<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(year);
    for (Album album : albums) {
      tagAlbum(album, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_YEAR) + NEWLINE);
  }

  /**
   * Tag genre.
   * DOCUMENT_ME
   * 
   * @param genre DOCUMENT_ME
   * @param level DOCUMENT_ME
   * 
   * @throws Exception the exception
   */
  private void tagGenre(Genre genre, int level) throws Exception {
    String sGenreID = genre.getID();
    String sGenreName = UtilString.formatXML(genre.getName2());
    writer.write(addTabs(level) + Tag.openTag(Const.XML_GENRE) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_ID, sGenreID) + NEWLINE);
    writer.write(addTabs(level + 1) + Tag.tagData(Const.XML_NAME, sGenreName) + NEWLINE);
    List<Album> albums = AlbumManager.getInstance().getAssociatedAlbums(genre);
    for (Album album : albums) {
      tagAlbum(album, level + 1);
    }
    List<Artist> artists = ArtistManager.getInstance().getAssociatedArtists(genre);
    for (Artist artist : artists) {
      tagArtist(artist, level + 1);
    }
    writer.write(addTabs(level) + Tag.closeTag(Const.XML_GENRE) + NEWLINE);
  }

  /**
   * Adds the tabs.
   * DOCUMENT_ME
   * 
   * @param num DOCUMENT_ME
   * 
   * @return the string
   */
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
   * @see org.jajuk.reporting.Exporter#process(java.util.List)
   */
  @Override
  public void process(List<Item> collection) throws Exception {
    try {
      writer.write(XML_HEADER + NEWLINE + Tag.openTag(Const.XML_COLLECTION) + NEWLINE);
      // Sort the collection thanks a tree set (we can't use Collections.sort()
      // here due to generics)
      TreeSet<Item> ts = new TreeSet<Item>();
      for (Item item : collection) {
        ts.add(item);
      }
      for (Item item : ts) {
        process(item);
      }
      // Add I18N nodes
      writer.write(Tag.openTag("i18n"));
      int i = 1;
      while (Messages.contains("ReportAction." + i)) {
        writer.write('\t' + Tag.tagData("ReportAction." + i,
            Messages.getString("ReportAction." + i)));
        i++;
      }
      writer.write('\t' + Tag.tagData("ReportAction.name",
          Messages.getHumanPropertyName(Const.XML_NAME)));
      writer.write('\t' + Tag.tagData("ReportAction.artist",
          Messages.getHumanPropertyName(Const.XML_ARTIST)));
      writer.write('\t' + Tag.tagData("ReportAction.genre",
          Messages.getHumanPropertyName(Const.XML_GENRE)));
      writer.write('\t' + Tag.tagData("ReportAction.order",
          Messages.getHumanPropertyName(Const.XML_TRACK_ORDER)));
      writer.write('\t' + Tag.tagData("ReportAction.track",
          Messages.getHumanPropertyName(Const.XML_TRACK)));
      writer.write('\t' + Tag.tagData("ReportAction.album",
          Messages.getHumanPropertyName(Const.XML_ALBUM)));
      writer.write('\t' + Tag.tagData("ReportAction.length",
          Messages.getHumanPropertyName(Const.XML_TRACK_LENGTH)));
      writer.write('\t' + Tag.tagData("ReportAction.year",
          Messages.getHumanPropertyName(Const.XML_YEAR)));
      writer.write('\t' + Tag.tagData("ReportAction.rate",
          Messages.getHumanPropertyName(Const.XML_TRACK_RATE)));
      writer.write('\t' + Tag.tagData("ReportAction.url",
          Messages.getHumanPropertyName(Const.XML_URL)));
      writer.write('\t' + Tag.tagData("ReportAction.type",
          Messages.getHumanPropertyName(Const.XML_TYPE)));
      writer.write('\t' + Tag.tagData("ReportAction.comment",
          Messages.getHumanPropertyName(Const.XML_TRACK_COMMENT)));
      writer.write(Tag.closeTag("i18n"));
      writer.write(Tag.closeTag(Const.XML_COLLECTION));
    } finally {
      writer.flush();
      writer.close();
    }

  }

  /**
   * Sets the show tracks.
   * 
   * @param showTracks the new show tracks
   */
  protected void setShowTracks(boolean showTracks) {
    this.showTracks = showTracks;
  }

}

/**
 * This class will create taggings. It will create either open tags, closed
 * tags, or full tagging with data.
 */
final class Tag {
  /**
   * private constructor to avoid instantiating utility class
   */
  private Tag() {
  }

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
