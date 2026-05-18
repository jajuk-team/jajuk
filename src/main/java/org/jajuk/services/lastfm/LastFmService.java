/*
 * aTunes 1.14.0 code adapted by Jajuk team
 *
 * Original copyright notice bellow :
 *
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
package org.jajuk.services.lastfm;

import ext.services.network.Proxy;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.Track;
import org.jajuk.services.lastfm.model.*;
import org.jajuk.services.lastfm.scrobble.ScrobblerException;
import org.jajuk.util.*;
import org.jajuk.util.log.Log;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Class LastFmService.
 * <br>
 * This class is responsible of retrieve information from Last.fm web services.
 * <singleton>
 */
public class LastFmService {
  // team
  /** The Constant ARTIST_WILDCARD. */
  private static final String ARTIST_WILDCARD = "(%ARTIST%)";
  /** The Constant LANGUAGE_PARAM. */
  private static final String LANGUAGE_PARAM = "?setlang=";
  /** The Constant LANGUAGE_WILDCARD. */
  private static final String LANGUAGE_WILDCARD = "(%LANGUAGE%)";
  /** The Constant ARTIST_WIKI_URL. */
  private static final String ARTIST_WIKI_URL = UtilString.concat("https://www.lastfm.com/music/",
          ARTIST_WILDCARD, "/+wiki", LANGUAGE_PARAM, LANGUAGE_WILDCARD);
  /** The Constant VARIOUS_ARTISTS. */
  private static final String VARIOUS_ARTISTS = "Various Artists";
  /** The Constant MIN_DURATION_TO_SUBMIT. */
  private static final int MIN_DURATION_TO_SUBMIT = 30;
  /** The Constant MAX_SUBMISSIONS. */
  private static final int MAX_SUBMISSIONS = 50;
  private final Locale locale;
  private final LastFmCache lastFmCache;
  /** The singleton. */
  private static LastFmService self;

  /** Last FM client */
  private final static LastFmClient lastFmClient = new LastFmClient();
  private final static MusicBrainzClient musicBrainzClient = new MusicBrainzClient();

  /**
   * Instantiates a new Last.fm service
   */
  private LastFmService(Locale locale, LastFmCache lastFmCache) {
    Proxy proxy = DownloadManager.getProxy();
    // Use encoded version name to avoid errors from server
    this.locale = locale;
    this.lastFmCache = lastFmCache;
  }

  /**
   * Return the LastFMService singleton.
   *
   * @return the LastFMService singleton
   */
  static public LastFmService getInstance() {
    if (self == null) {
      LastFmCache cache = new LastFmCache();
      Locale locale = LocaleManager.getLocale();
      self = new LastFmService(locale, cache);
    }
    return self;
  }

  /**
   * Gets the artist.
   *
   * @return the artist
   */
  public ArtistInfo getArtist(String artist) {
    try {
      // Try to get from cache
      ArtistInfo artistInfo = lastFmCache.retrieveArtistInfo(artist);
      if (artistInfo == null) {
        artistInfo = lastFmClient.getArtist(artist);
        if (artistInfo != null) {
          musicBrainzClient.checkArtistImageUrl(artistInfo);
          lastFmCache.storeArtistInfo(artist, artistInfo);
        }
      }
      return artistInfo;
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the album.
   *
   * @param artist the artist
   * @param album  the album
   * @return the album
   */
  public AlbumInfo getAlbum(String artist, String album) {
    try {
      // Try to get from cache
      AlbumInfo albumObject = lastFmCache.retrieveAlbumInfo(artist, album);
      if (albumObject == null) {
        albumObject = lastFmClient.getAlbumInfo(artist, album);
        if (albumObject != null) {
          lastFmCache.storeAlbumInfo(artist, album, albumObject);
        }
      }
      return albumObject;
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the album list.
   *
   * @param artist             the artist
   * @param hideVariousArtists if <code>true</code> albums with artist name "Various Artists"
   *                           are nor returned
   * @param minimumSongNumber  albums with less songs than this argument won't be returned
   * @return the album list
   */
  public AlbumListInfo getAlbumList(String artist, boolean hideVariousArtists, int minimumSongNumber) {
    try {
      // Try to get from cache
      AlbumListInfo albumList = lastFmCache.retrieveAlbumList(artist);
      if (albumList == null) {
        //Collection<Album> as = Artist.getTopAlbums(artist, UtilString.rot13(API_KEY));
        //if (as != null) {
        List<AlbumInfo> albums = lastFmClient.getTopAlbums(artist, 10);
        if (albums != null) {
          //AlbumListInfo albums = LastFmAlbumList.getAlbumList(as, artist);
          List<AlbumInfo> result = new ArrayList<>();
          //for (AlbumInfo a : albums.getAlbums()) {
          for (AlbumInfo a : albums) {
            if (a.getBigCoverURL() != null && !a.getBigCoverURL().isEmpty()) { //NOSONAR
              result.add(a);
            }
          }
          albumList = new LastFmAlbumList();
          albumList.setArtist(artist);
          albumList.setAlbums(result);
          lastFmCache.storeAlbumList(artist, albumList);
        }
      }
      if (albumList != null) {
        List<AlbumInfo> albumsFiltered = null;
        // Apply filter to hide "Various Artists" albums
        if (hideVariousArtists) {
          albumsFiltered = new ArrayList<>();
          for (AlbumInfo albumInfo : albumList.getAlbums()) {
            if (!albumInfo.getArtist().equals(VARIOUS_ARTISTS)) { //NOSONAR
              albumsFiltered.add(albumInfo);
            }
          }
          albumList.setAlbums(albumsFiltered);
        }
        // Apply filter to hide albums with less than X songs
        if (minimumSongNumber > 0) {
          albumsFiltered = new ArrayList<>();
          for (AlbumInfo albumInfo : albumList.getAlbums()) {
            AlbumInfo extendedAlbumInfo = getAlbum(artist, albumInfo.getTitle());
            if (extendedAlbumInfo != null && extendedAlbumInfo.getTracks() != null //NOSONAR
                    && extendedAlbumInfo.getTracks().size() >= minimumSongNumber) {
              albumsFiltered.add(albumInfo);
            }
          }
        }
        if (albumsFiltered != null) {
          albumList.setAlbums(albumsFiltered);
        }
      }
      return albumList;
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the image.
   *
   * @param album the album
   * @return the image
   */
  public Image getImage(AlbumInfo album) {
    try {
      Proxy proxy = DownloadManager.getProxy();
      // Try to retrieve from cache
      Image img = lastFmCache.retrieveAlbumCover(album);
      if (img == null && album.getBigCoverURL() != null && !album.getBigCoverURL().isEmpty()) {
        img = ext.services.network.NetworkUtils.getImage(ext.services.network.NetworkUtils
                .getConnection(album.getBigCoverURL(), proxy));
        lastFmCache.storeAlbumCover(album, img);
      }
      return img;
    } catch (IOException e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the image of an artist.
   *
   * @param artist the artist
   * @return the image
   */
  public Image getImage(ArtistInfo artist) {
    try {
      // Try to retrieve from cache
      Image img = lastFmCache.retrieveArtistThumbImage(artist);
      Proxy proxy = DownloadManager.getProxy();
      if (img == null && artist.getImageUrl() != null && !artist.getImageUrl().isEmpty()) {
        // Try to get from Artist.getImages() method
        img = getArtistImageFromLastFM(artist.getName());
        // if not then get from artist info
        if (img == null) {
          img = ext.services.network.NetworkUtils.getImage(ext.services.network.NetworkUtils
                  .getConnection(artist.getImageUrl(), proxy));
        }
        lastFmCache.storeArtistThumbImage(artist, img);
      }
      return img;
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Returns current artist image at LastFM.
   *
   * @return the artist image from last fm
   */
  private Image getArtistImageFromLastFM(String artistName) {
    try {
      // Try to get from Artist.getImages() method
      List<LastFmImage> imageList = lastFmClient.getLastFmImages(artistName, 10);
      if (!imageList.isEmpty()) {
        //Set<ImageSize> sizes = imageList.get(0).availableSizes();
        // Try to get original
        //if (sizes.contains(ImageSize.ORIGINAL)) {
        Proxy proxy = DownloadManager.getProxy();
        //  return ext.services.network.NetworkUtils.getImage(ext.services.network.NetworkUtils
        //      .getConnection(imageList.get(0).getImageURL(ImageSize.ORIGINAL), proxy));
        //}
        LastFmImage image = imageList.get(0);
        return ext.services.network.NetworkUtils.getImage(ext.services.network.NetworkUtils
                .getConnection(image.getUrl(), proxy));
      }
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the similar artists.
   *
   * @param artist the artist
   * @return the similar artists
   */
  public SimilarArtistsInfo getSimilarArtists(String artist) {
    try {
      // Try to get from cache
      SimilarArtistsInfo similar = lastFmCache.retrieveArtistSimilar(artist);
      if (similar == null) {
        ArtistInfo artistInfo = lastFmClient.getArtist(artist);
        if (artistInfo != null) {
          // Get (max) 15 similar artists
          List<ArtistInfo> similarArtists = lastFmClient.getSimilar(artistInfo, LastFmSimilarArtists.MAX_SIMILAR_ARTISTS);
          // Replace place holder image
          for (ArtistInfo similarArtist : similarArtists) {
            musicBrainzClient.checkArtistImageUrl(similarArtist);
          }
          // Build similar object
          similar = LastFmSimilarArtists.getSimilarArtists(similarArtists, artistInfo);
          lastFmCache.storeArtistSimilar(artist, similar);
        }
      }
      return similar;
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the wiki text.
   *
   * @param artist the artist
   * @return the wiki text
   */
  public String getWikiText(String artist) {
    try {
      // Try to get from cache
      String wikiText = lastFmCache.retrieveArtistWiki(artist);
      if (wikiText == null) {
        //Artist a = Artist.getInfo(artist, locale, null, UtilString.rot13(API_KEY));
        ArtistInfo info = lastFmClient.getArtist(artist);
        LastFmArtistDetail a;
        if (info != null) {
          a = lastFmClient.getArtistDetail(info.getId(), true);
        } else {
          a = lastFmClient.getArtistDetail(artist, false);
        }
        if (a != null && a.getWikiContent() != null) {
          wikiText = a.getWikiContent();
        }
        //wikiText = a != null ? a.getWikiSummary() : "";
        if (wikiText != null) {
          wikiText = wikiText.replaceAll("<.*?>", "");
          wikiText = StringEscapeUtils.unescapeHtml3(wikiText);
        }
        lastFmCache.storeArtistWiki(artist, wikiText);
      }
      return wikiText;
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the wiki url.
   *
   * @param artist the artist
   * @return the wiki url
   */
  public String getWikiURL(String artist) {
    return ARTIST_WIKI_URL.replace(ARTIST_WILDCARD,
            ext.services.network.NetworkUtils.encodeString(artist)).replace(LANGUAGE_WILDCARD,
            locale.getLanguage());
  }

  /**
   * Submits song to Last.fm using the modern API 2.0 (updateNowPlaying).
   *
   * @param track        The track that was played
   * @param millisPlayed ms the audio file has already played
   * @throws ScrobblerException the scrobbler exception
   */
  public void submit(Track track, long millisPlayed) throws ScrobblerException {
    // 1. Perform necessary checks (same as old logic)
    if (!checkSessionKey() || !checkArtist(track) || !checkTitle(track)
            || !checkDuration(track)) {
      //Log.warn("Last.fm for track: " + track.getName());
      return;
    }
    try {
      lastFmClient.updateNowPlaying(track);
    } catch (ScrobblerException e) {
      if (e.getMessage().contains("Invalid session key") || e.getMessage().contains("Error 9")) {
        Conf.removeProperty(Const.CONF_LASTFM_SESSION_KEY);
      }
      throw e;
    }
  }

  public void addSubmission(FullSubmissionData fullSubmissionData) throws ScrobblerException {
    if (!checkSessionKey()) {
      return;
    }
    lastFmCache.addSubmissionData(fullSubmissionData);
  }

  public void removeSubmission(FullSubmissionData fullSubmissionData) throws ScrobblerException {
    // TODO instead of clearing all, should remove only the data from parameter
    lastFmCache.removeSubmissionData();
  }

  /**
   * Submits cache data to Last.fm
   *
   * @throws ScrobblerException the scrobbler exception
   */
  public void submitCache() throws ScrobblerException {
    // Do all necessary checks
    if (!checkSessionKey()) {
      return;
    }
    List<FullSubmissionData> collectionWithSubmissionData = lastFmCache.getSubmissionData();
    if (!collectionWithSubmissionData.isEmpty()) {
      // More than MAX_SUBMISSIONS submissions at once are not allowed
      int size = collectionWithSubmissionData.size();
      if (size > MAX_SUBMISSIONS) {
        collectionWithSubmissionData = collectionWithSubmissionData.subList(size - MAX_SUBMISSIONS,
                size);
      }
      Log.info("Trying to submit cache to Last.fm");
      try {
        lastFmClient.scrobble(collectionWithSubmissionData);
        lastFmCache.removeSubmissionData();
        Log.info("Cache submitted to Last.fm");
      } catch (ScrobblerException e) {
        // If it's an invalid session, clear it and re-throw to trigger re-auth
        if (e.getMessage().contains("Invalid session key") || e.getMessage().contains("Error 9")) {
          Conf.removeProperty(Const.CONF_LASTFM_SESSION_KEY);

        }
        throw e;
      } catch (IOException e) {
        Log.error(e);
        throw new ScrobblerException(e.getMessage());
      }
    }
  }

  /**
   * Checks session key.
   *
   * @return true, if session key is present in config
   */
  private boolean checkSessionKey() {
    String key = Conf.getString(Const.CONF_LASTFM_SESSION_KEY);
    if (key == null || key.isEmpty()) {
      Log.debug("Don't submit to Last.fm: Empty session key");
      return false;
    }
    return true;
  }

  /**
   * Check artist.
   *
   * @return true, if check artist
   */
  private boolean checkArtist(Track track) {
    String sArtist = track.getArtist().getName2();
    if (StringUtils.isBlank(sArtist)
            || sArtist.equalsIgnoreCase(Messages.getString("unknown_artist"))) {
      Log.debug("Don't submit to Last.fm: Unknown artist");
      return false;
    }
    return true;
  }

  /**
   * Check title.
   *
   * @return true, if check title
   */
  private boolean checkTitle(Track track) {
    if (StringUtils.isBlank(track.getName())) {
      Log.debug("Don't submit to Last.fm: Unknown Title");
      return false;
    }
    return true;
  }

  /**
   * Check duration.
   *
   * @return true, if check duration
   */
  private boolean checkDuration(Track track) {
    if (track.getDuration() < MIN_DURATION_TO_SUBMIT) {
      Log.debug(UtilString.concat("Don't submit to Last.fm: Lenght < ", MIN_DURATION_TO_SUBMIT));
      return false;
    }
    return true;
  }
}
