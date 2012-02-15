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

package ext.services.lastfm;

import ext.services.network.Proxy;

import java.awt.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.roarsoftware.lastfm.Album;
import net.roarsoftware.lastfm.Artist;
import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.ImageSize;
import net.roarsoftware.lastfm.PaginatedResult;
import net.roarsoftware.lastfm.Playlist;
import net.roarsoftware.lastfm.scrobble.ResponseStatus;
import net.roarsoftware.lastfm.scrobble.Scrobbler;
import net.roarsoftware.lastfm.scrobble.Source;
import net.roarsoftware.lastfm.scrobble.SubmissionData;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.jajuk.base.Track;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.LocaleManager;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * The Class LastFmService.
 * 
 * This class is responsible of retrieve information from Last.fm web services.
 * <singleton>
 */
public class LastFmService {

  /*
   * DO NOT USE THESE KEYS FOR OTHER APPLICATIONS THAN Jajuk!
   */
  /** The Constant API_KEY.  DOCUMENT_ME */
  private static final String API_KEY = "711591ss6q695ps349o6681pr1oq1467";

  /** The Constant CLIENT_ID.  DOCUMENT_ME */
  private static final String CLIENT_ID = "jaj";

  /** The Constant CLIENT_VERSION.  DOCUMENT_ME */
  private static final String CLIENT_VERSION = "0.2"; // Assigned by Last.FM
  // team

  /** The Constant ARTIST_WILDCARD.  DOCUMENT_ME */
  private static final String ARTIST_WILDCARD = "(%ARTIST%)";

  /** The Constant LANGUAGE_PARAM.  DOCUMENT_ME */
  private static final String LANGUAGE_PARAM = "?setlang=";

  /** The Constant LANGUAGE_WILDCARD.  DOCUMENT_ME */
  private static final String LANGUAGE_WILDCARD = "(%LANGUAGE%)";

  /** The Constant ARTIST_WIKI_URL.  DOCUMENT_ME */
  private static final String ARTIST_WIKI_URL = UtilString.concat("http://www.lastfm.com/music/",
      ARTIST_WILDCARD, "/+wiki", LANGUAGE_PARAM, LANGUAGE_WILDCARD);

  /** The Constant VARIOUS_ARTISTS.  DOCUMENT_ME */
  private static final String VARIOUS_ARTISTS = "Various Artists";

  /** The Constant MIN_DURATION_TO_SUBMIT.  DOCUMENT_ME */
  private static final int MIN_DURATION_TO_SUBMIT = 30;

  /** The Constant MAX_SUBMISSIONS.  DOCUMENT_ME */
  private static final int MAX_SUBMISSIONS = 50;

  /** DOCUMENT_ME. */
  private ext.services.network.Proxy proxy;

  /** DOCUMENT_ME. */
  private Scrobbler scrobbler;

  /** DOCUMENT_ME. */
  private String user;

  /** DOCUMENT_ME. */
  private String password;

  /** DOCUMENT_ME. */
  private boolean handshakePerformed;

  /** DOCUMENT_ME. */
  private Locale locale;

  /** DOCUMENT_ME. */
  private LastFmCache lastFmCache;

  /** The singleton. */
  private static LastFmService self;

  /**
   * Instantiates a new Last.fm service
   * 
   * @param proxy the proxy
   * @param user the Last.fm username
   * @param password the Last.fm password
   * @param locale DOCUMENT_ME
   * @param lastFmCache DOCUMENT_ME
   */
  private LastFmService(ext.services.network.Proxy proxy, String user, String password,
      Locale locale, LastFmCache lastFmCache) {
    this.proxy = proxy;
    this.user = user;
    this.password = password;
    Caller.getInstance().setCache(null);
    Caller.getInstance().setProxy(proxy);
    Caller.getInstance().setUserAgent(CLIENT_ID);
    // Use encoded version name to avoid errors from server
    scrobbler = Scrobbler.newScrobbler(CLIENT_ID, ext.services.network.NetworkUtils
        .encodeString(CLIENT_VERSION), user);
    this.handshakePerformed = false;
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
      Proxy proxy = DownloadManager.getProxy();
      String user = Conf.getString(Const.CONF_LASTFM_USER);
      String pwd = Conf.getString(Const.CONF_LASTFM_PASSWORD);
      Locale locale = LocaleManager.getLocale();
      self = new LastFmService(proxy, user, UtilString.rot13(pwd), locale, cache);
    }
    return self;
  }

  /**
   * Gets the artist.
   * 
   * @param artist DOCUMENT_ME
   * 
   * @return the artist
   */
  public ArtistInfo getArtist(String artist) {
    try {
      // Try to get from cache
      ArtistInfo artistInfo = lastFmCache.retrieveArtistInfo(artist);
      if (artistInfo == null) {
        Artist a = Artist.getInfo(artist, UtilString.rot13(API_KEY));
        if (a != null) {
          artistInfo = LastFmArtist.getArtist(a);
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
   * @param album the album
   * 
   * @return the album
   */
  public AlbumInfo getAlbum(String artist, String album) {
    try {
      // Try to get from cache
      AlbumInfo albumObject = lastFmCache.retrieveAlbumInfo(artist, album);
      if (albumObject == null) {
        Album a = Album.getInfo(artist, album, UtilString.rot13(API_KEY));
        if (a != null) {
          Playlist pl = Playlist.fetchAlbumPlaylist(a.getId(), UtilString.rot13(API_KEY));
          albumObject = LastFmAlbum.getAlbum(a, pl);
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
   * @param artist the artist
   * @param hideVariousArtists if <code>true</code> albums with artist name "Various Artists"
   * are nor returned
   * @param minimumSongNumber albums with less songs than this argument won't be returned
   * 
   * @return the album list
   */
  public AlbumListInfo getAlbumList(String artist, boolean hideVariousArtists, int minimumSongNumber) {
    try {
      // Try to get from cache
      AlbumListInfo albumList = lastFmCache.retrieveAlbumList(artist);
      if (albumList == null) {
        Collection<Album> as = Artist.getTopAlbums(artist, UtilString.rot13(API_KEY));
        if (as != null) {
          AlbumListInfo albums = LastFmAlbumList.getAlbumList(as, artist);

          List<AlbumInfo> result = new ArrayList<AlbumInfo>();
          for (AlbumInfo a : albums.getAlbums()) {
            if (a.getBigCoverURL() != null && !a.getBigCoverURL().isEmpty()) {
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
          albumsFiltered = new ArrayList<AlbumInfo>();
          for (AlbumInfo albumInfo : albumList.getAlbums()) {
            if (!albumInfo.getArtist().equals(VARIOUS_ARTISTS)) {
              albumsFiltered.add(albumInfo);
            }
          }
          albumList.setAlbums(albumsFiltered);
        }

        // Apply filter to hide albums with less than X songs
        if (minimumSongNumber > 0) {
          albumsFiltered = new ArrayList<AlbumInfo>();
          for (AlbumInfo albumInfo : albumList.getAlbums()) {
            AlbumInfo extendedAlbumInfo = getAlbum(artist, albumInfo.getTitle());
            if (extendedAlbumInfo != null && extendedAlbumInfo.getTracks() != null
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
   * Gets the artist top tag.
   * 
   * @param artist the artist
   * 
   * @return the artist top tag
   */
  public String getArtistTopTag(String artist) {
    try {
      Collection<String> topTags = Artist.getTopTags(artist, UtilString.rot13(API_KEY));
      List<String> tags = new ArrayList<String>(topTags);
      return tags.isEmpty() ? "" : tags.get(0);
    } catch (Exception e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the image.
   * 
   * @param album the album
   * 
   * @return the image
   */
  public Image getImage(AlbumInfo album) {
    try {
      Image img = null;
      // Try to retrieve from cache
      img = lastFmCache.retrieveAlbumCover(album);
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
   * 
   * @return the image
   */
  public Image getImage(ArtistInfo artist) {
    try {
      // Try to retrieve from cache
      Image img = lastFmCache.retrieveArtistThumbImage(artist);
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
   * Gets the image of the artist.
   * 
   * @param similar the similar
   * 
   * @return the image
   */
  public Image getImage(SimilarArtistsInfo similar) {
    try {
      // Try to retrieve from cache
      Image img = lastFmCache.retrieveArtistImage(similar);

      if (img != null) {
        return img;
      }

      // Try to get from LastFM
      img = getArtistImageFromLastFM(similar.getArtistName());

      // Get from similar artist info
      if (img == null) {
        String similarUrl = similar.getPicture();
        if (!similarUrl.trim().isEmpty()) {
          img = ext.services.network.NetworkUtils.getImage(ext.services.network.NetworkUtils
              .getConnection(similarUrl, proxy));
        }
      }

      if (img != null) {
        lastFmCache.storeArtistImage(similar, img);
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
   * @param artistName DOCUMENT_ME
   * 
   * @return the artist image from last fm
   */
  private Image getArtistImageFromLastFM(String artistName) {
    try {
      // Try to get from Artist.getImages() method
      PaginatedResult<net.roarsoftware.lastfm.Image> images = Artist.getImages(artistName, 1, 1,
          UtilString.rot13(API_KEY));
      List<net.roarsoftware.lastfm.Image> imageList = new ArrayList<net.roarsoftware.lastfm.Image>(
          images.getPageResults());
      if (!imageList.isEmpty()) {
        Set<ImageSize> sizes = imageList.get(0).availableSizes();
        // Try to get original
        if (sizes.contains(ImageSize.ORIGINAL)) {
          return ext.services.network.NetworkUtils.getImage(ext.services.network.NetworkUtils
              .getConnection(imageList.get(0).getImageURL(ImageSize.ORIGINAL), proxy));
        }
      }
    } catch (IOException e) {
      Log.error(e);
    }
    return null;
  }

  /**
   * Gets the similar artists.
   * 
   * @param artist the artist
   * 
   * @return the similar artists
   */
  public SimilarArtistsInfo getSimilarArtists(String artist) {
    try {
      // Try to get from cache
      SimilarArtistsInfo similar = lastFmCache.retrieveArtistSimilar(artist);
      if (similar == null) {
        Collection<Artist> as = Artist.getSimilar(artist, UtilString.rot13(API_KEY));
        Artist a = Artist.getInfo(artist, UtilString.rot13(API_KEY));
        if (a != null) {
          similar = LastFmSimilarArtists.getSimilarArtists(as, a);
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
   * 
   * @return the wiki text
   */
  public String getWikiText(String artist) {
    try {
      // Try to get from cache
      String wikiText = lastFmCache.retrieveArtistWiki(artist);
      if (wikiText == null) {

        Artist a = Artist.getInfo(artist, locale, UtilString.rot13(API_KEY));
        wikiText = a != null ? a.getWikiSummary() : "";
        if (wikiText != null) {
          wikiText = wikiText.replaceAll("<.*?>", "");
          wikiText = StringEscapeUtils.unescapeHtml(wikiText);
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
   * 
   * @return the wiki url
   */
  public String getWikiURL(String artist) {
    return ARTIST_WIKI_URL.replace(ARTIST_WILDCARD,
        ext.services.network.NetworkUtils.encodeString(artist)).replace(LANGUAGE_WILDCARD,
        locale.getLanguage());
  }

  /**
   * Submits song to Last.fm
   *
   * @param track DOCUMENT_ME
   * @param secondsPlayed seconds the audio file has already played
   * @throws ScrobblerException the scrobbler exception
   */
  public void submit(Track track, long secondsPlayed) throws ScrobblerException {
    // Do all necessary checks
    if (!checkUser() || !checkPassword() || !checkArtist(track) || !checkTitle(track)
        || !checkDuration(track)) {
      return;
    }

    // Get started to play in secs UTC and not in MS (lastfm-bindings API was unclear about it)
    long startedToPlay = (System.currentTimeMillis() - secondsPlayed) / 1000;

    Log.info("Trying to submit song to Last.fm");
    try {
      performHandshakeIfNeeded();
      SubmissionData submissionData = new SubmissionData(track.getArtist().getName2(), track
          .getName(), track.getAlbum().getName2(), (int) track.getDuration(), (int) track
          .getOrder(), Source.USER, null, startedToPlay);
      ResponseStatus status = scrobbler.submit(submissionData);
      if (status.ok()) {
        Log.info("Song submitted to Last.fm");
      } else {
        handshakePerformed = false;
        lastFmCache.addSubmissionData(new FullSubmissionData(track.getArtist().getName2(), track
            .getName(), track.getAlbum().getName2(), (int) track.getDuration(), (int) track
            .getOrder(), Source.USER.toString(), (int) startedToPlay));

        throw new ScrobblerException(status.getStatus());
      }

    } catch (IOException e) {
      Log.error(e);
      handshakePerformed = false;
      lastFmCache.addSubmissionData(new FullSubmissionData(track.getArtist().getName2(), track
          .getName(), track.getAlbum().getName2(), (int) track.getDuration(), (int) track
          .getOrder(), Source.USER.toString(), (int) startedToPlay));
      throw new ScrobblerException(e.getMessage());
    }
  }

  /**
   * Submits cache data to Last.fm
   * 
   * @throws ScrobblerException the scrobbler exception
   */
  public void submitCache() throws ScrobblerException {
    // Do all necessary checks
    if (!checkUser() || !checkPassword()) {
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
        performHandshakeIfNeeded();

        List<SubmissionData> submissionDataList = new ArrayList<SubmissionData>();
        for (ext.services.lastfm.FullSubmissionData submissionData : collectionWithSubmissionData) {
          SubmissionData sd = new SubmissionData(submissionData.getArtist(), submissionData
              .getTitle(), submissionData.getAlbum(), submissionData.getDuration(), submissionData
              .getTrackNumber(), Source.valueOf(submissionData.getSource()), null, submissionData
              .getStartTime());
          submissionDataList.add(sd);
        }

        ResponseStatus status = scrobbler.submit(submissionDataList);
        if (status.ok()) {
          lastFmCache.removeSubmissionData();
          Log.info("Cache submitted to Last.fm");
        } else {
          handshakePerformed = false;
          throw new ScrobblerException(status.getStatus());
        }

      } catch (IOException e) {
        Log.error(e);
        handshakePerformed = false;
        throw new ScrobblerException(e.getMessage());
      }
    }

  }

  /**
   * Submits now playing info to Last.fm
   * 
   * @param track DOCUMENT_ME
   * 
   * @throws ScrobblerException the scrobbler exception
   */
  public void submitNowPlayingInfo(Track track) throws ScrobblerException {
    // Do all necessary checks
    if (!checkUser() || !checkPassword() || !checkArtist(track) || !checkTitle(track)) {
      return;
    }

    Log.info("Trying to submit now playing info to Last.fm");
    try {
      performHandshakeIfNeeded();
      ResponseStatus status = scrobbler.nowPlaying(track.getArtist().getName2(), track.getName(),
          track.getAlbum().getName2(), (int) track.getDuration(), (int) track.getOrder());
      if (status.ok()) {
        Log.info("Now playing info submitted to Last.fm");
      } else {
        handshakePerformed = false;
        throw new ScrobblerException(status.getStatus());
      }
    } catch (IOException e) {
      Log.error(e);
      handshakePerformed = false;
      throw new ScrobblerException(e.getMessage());
    }
  }

  /**
   * Performs handshake for submissions if needed.
   * 
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ScrobblerException the scrobbler exception
   */
  private void performHandshakeIfNeeded() throws IOException, ScrobblerException {
    if (!handshakePerformed) {
      ResponseStatus status = scrobbler.handshake(password);
      if (!status.ok()) {
        throw new ScrobblerException(status.getStatus());
      }
      handshakePerformed = true;
    }
  }

  /**
   * Checks user.
   * 
   * @return true, if check user
   */
  private boolean checkUser() {
    if (user == null || user.equals("")) {
      Log.debug("Don't submit to Last.fm: Empty user");
      return false;
    }
    return true;
  }

  /**
   * Check password.
   * 
   * @return true, if check password
   */
  private boolean checkPassword() {
    if (StringUtils.isBlank(password)) {
      Log.debug("Don't submit to Last.fm: Empty password");
      return false;
    }
    return true;
  }

  /**
   * Check artist.
   * 
   * @param track DOCUMENT_ME
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
   * @param track DOCUMENT_ME
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
   * @param track DOCUMENT_ME
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

  /**
   * Gets the proxy.
   * 
   * @return the proxy
   */
  public ext.services.network.Proxy getProxy() {
    return this.proxy;
  }

  /**
   * Sets the proxy.
   * 
   * @param proxy the new proxy
   */
  public void setProxy(ext.services.network.Proxy proxy) {
    this.proxy = proxy;
  }

  /**
   * Gets the user.
   * 
   * @return the user
   */
  public String getUser() {
    return this.user;
  }

  /**
   * Sets the user.
   * 
   * @param user the new user
   */
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Gets the password.
   * 
   * @return the password
   */
  public String getPassword() {
    return this.password;
  }

  /**
   * Sets the password.
   * 
   * @param password the new password
   */
  public void setPassword(String password) {
    this.password = password;
  }
}
