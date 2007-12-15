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

import org.w3c.dom.Element;

import ext.services.xml.XMLUtils;

public class AudioScrobblerArtist {

  private String name;
  private String match;
  private String url;
  private String imageUrl;

  public String getImageUrl() {
    return imageUrl;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

  protected static AudioScrobblerArtist getArtist(Element el) {
    AudioScrobblerArtist artist = new AudioScrobblerArtist();
    artist.name = XMLUtils.getChildElementContent(el, "name");
    artist.match = XMLUtils.getChildElementContent(el, "match");
    artist.url = XMLUtils.getChildElementContent(el, "url");
    artist.imageUrl = XMLUtils.getChildElementContent(el, "image");
    return artist;
  }

  public String getMatch() {
    return match;
  }

}
