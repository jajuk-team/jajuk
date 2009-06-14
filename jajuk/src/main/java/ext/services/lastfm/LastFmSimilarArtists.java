/*
 * Adapted by Jajuk team
 * Copyright (C) 2003-2009 the Jajuk Team
 * http://jajuk.info
 * 
 * aTunes 1.14.0
 * Copyright (C) 2006-2009 Alex Aranda, Sylvain Gaudard, Thomas Beckers and contributors
 *
 * See http://www.atunes.org/wiki/index.php?title=Contributing for information about contributors
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
import java.util.Collection;
import java.util.List;

import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.ImageSize;

/**
 * The Class LastFmSimilarArtists.
 */
public class LastFmSimilarArtists implements SimilarArtistsInfo {

    /** The Constant MAX_SIMILAR_ARTISTS. */
    public static final int MAX_SIMILAR_ARTISTS = 15;

    /** The artist name. */
    private String artistName;

    /** The picture. */
    private String picture;

    /** The artists. */
    private List<ArtistInfo> artists;

    /**
     * Gets the similar artists.
     * 
     * @return the similar artists
     */
    public static SimilarArtistsInfo getSimilarArtists(Collection<Artist> as, Artist a) {
        List<Artist> list = new ArrayList<Artist>(as);
        LastFmSimilarArtists similar = new LastFmSimilarArtists();

        similar.artistName = a.getName();
        similar.picture = a.getImageURL(ImageSize.LARGE);

        similar.artists = new ArrayList<ArtistInfo>();
        for (int i = 0; i < list.size(); i++) {
            if (i == MAX_SIMILAR_ARTISTS) {
                break;
            }
            similar.artists.add(LastFmArtist.getArtist(list.get(i)));
        }

        return similar;
    }

    /**
     * Gets the artist name.
     * 
     * @return the artist name
     */
    public String getArtistName() {
        return artistName;
    }

    /**
     * Gets the artists.
     * 
     * @return the artists
     */
    public List<ArtistInfo> getArtists() {
        return artists;
    }

    /**
     * Gets the picture.
     * 
     * @return the picture
     */
    public String getPicture() {
        return picture;
    }

    /**
     * Sets the artist name.
     * 
     * @param artistName
     *            the artistName to set
     */
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    /**
     * Sets the artists.
     * 
     * @param artists
     *            the artists to set
     */
    public void setArtists(List<? extends ArtistInfo> artists) {
        this.artists = artists != null ? new ArrayList<ArtistInfo>(artists) : null;
    }

    /**
     * Sets the picture.
     * 
     * @param picture
     *            the picture to set
     */
    public void setPicture(String picture) {
        this.picture = picture;
    }

}
