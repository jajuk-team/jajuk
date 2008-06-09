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

import java.awt.Image;
import java.util.List;

import org.jajuk.base.Track;

public interface AudioScrobblerListener {

  void notifyAlbumRetrieved(Track track);

  void notifyCoverRetrieved(AudioScrobblerAlbum album, Image cover);

  void notifyArtistImage(Image img);

  void notifyFinishGetSimilarArtist(AudioScrobblerArtist a, Image img);

  List<AudioScrobblerAlbum> getAlbums();

  void setAlbum(AudioScrobblerAlbum album);

  void setAlbums(List<AudioScrobblerAlbum> album);

  void setImage(Image img);

  void savePicture(Image img, Track track);

  void setLastAlbumRetrieved(String album);

  void setLastArtistRetrieved(String artist);
}
