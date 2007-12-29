/**
 * aTunes 1.6.6
 * Copyright (C) 2006-2007 Alex Aranda (fleax) alex@atunes.org
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package ext.services.lastfm;

import ext.XMLUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AudioScrobblerAlbum {

  private String artist;
  private String title;
  private String url;
  private Date releaseDate;
  private String coverURL;
  private String smallCoverURL;
  private ArrayList<AudioScrobblerTrack> tracks;

  private static final SimpleDateFormat df = new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.US);

  protected static AudioScrobblerAlbum getAlbum(Document xml) {
    AudioScrobblerAlbum album = new AudioScrobblerAlbum();

    Element element = (Element) xml.getElementsByTagName("album").item(0);

    album.artist = element.getAttribute("artist");
    album.title = element.getAttribute("title");
    album.url = ext.XMLUtils.getChildElementContent(element, "url");
    try {
      album.releaseDate = df.parse(ext.XMLUtils.getChildElementContent(element, "releasedate")
          .trim());
    } catch (ParseException e) {
      album.releaseDate = null;
    }
    album.coverURL = XMLUtils.getChildElementContent(XMLUtils.getChildElement(element, "coverart"),
        "medium");
    album.smallCoverURL = XMLUtils.getChildElementContent(XMLUtils.getChildElement(element,
        "coverart"), "small");
    NodeList tracks = ((Element) element.getElementsByTagName("tracks").item(0))
        .getElementsByTagName("track");

    album.tracks = new ArrayList<AudioScrobblerTrack>();
    for (int i = 0; i < tracks.getLength(); i++)
      album.tracks.add(AudioScrobblerTrack.getTrack((Element) tracks.item(i)));

    return album;
  }

  protected static ArrayList<AudioScrobblerAlbum> getAlbumList(Document xml) {
    ArrayList<AudioScrobblerAlbum> albums = new ArrayList<AudioScrobblerAlbum>();

    Element element = (Element) xml.getElementsByTagName("topalbums").item(0);
    String artist = element.getAttribute("artist");
    NodeList list = element.getElementsByTagName("album");

    for (int i = 0; i < list.getLength(); i++) {
      Element alb = (Element) list.item(i);
      AudioScrobblerAlbum album = new AudioScrobblerAlbum();
      album.artist = artist;
      album.title = XMLUtils.getChildElementContent(alb, "name");
      album.url = XMLUtils.getChildElementContent(alb, "url");
      try {
        String date = XMLUtils.getChildElementContent(alb, "releasedate").trim();
        album.releaseDate = df.parse(date);
      } catch (ParseException e) {
        album.releaseDate = null;
      }
      album.coverURL = XMLUtils.getChildElementContent(XMLUtils.getChildElement(alb, "image"),
          "medium");
      album.smallCoverURL = XMLUtils.getChildElementContent(XMLUtils.getChildElement(alb, "image"),
          "small");
      albums.add(album);
    }

    return albums;
  }

  public String getArtist() {
    return artist;
  }

  public Date getReleaseDate() {
    return releaseDate;
  }

  public String getYear() {
    if (releaseDate == null)
      return "";
    Calendar c = Calendar.getInstance();
    c.setTime(releaseDate);
    return Integer.toString(c.get(Calendar.YEAR));
  }

  public String getTitle() {
    return title;
  }

  public ArrayList<AudioScrobblerTrack> getTracks() {
    return tracks;
  }

  public String getUrl() {
    return url;
  }

  public String getArtistUrl() {
    return url.substring(0, url.lastIndexOf('/'));
  }

  public String getCoverURL() {
    return coverURL;
  }

  public String getSmallCoverURL() {
    return smallCoverURL;
  }

  @Override
  public String toString() {
    return artist + " - " + title;
  }
}
