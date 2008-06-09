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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class AudioScrobblerSimilarArtists {

  private String artistName;
  private String picture;
  private List<AudioScrobblerArtist> artists;

  public final static int MAX_SIMILAR_ARTISTS = 15;

  public String getArtistName() {
    return artistName;
  }

  public String getPicture() {
    return picture;
  }

  public List<AudioScrobblerArtist> getArtists() {
    return artists;
  }

  protected static AudioScrobblerSimilarArtists getSimilarArtists(Document xml) {
    Element el = (Element) xml.getElementsByTagName("similarartists").item(0);
    AudioScrobblerSimilarArtists similar = new AudioScrobblerSimilarArtists();
    similar.artistName = el.getAttribute("artist");
    similar.picture = el.getAttribute("picture");

    similar.artists = new ArrayList<AudioScrobblerArtist>();
    NodeList artists = el.getElementsByTagName("artist");
    for (int i = 0; i < artists.getLength(); i++) {
      if (i == MAX_SIMILAR_ARTISTS)
        break;
      Element e = (Element) artists.item(i);
      similar.artists.add(AudioScrobblerArtist.getArtist(e));
    }

    return similar;
  }

}
