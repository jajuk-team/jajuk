/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk.util.filters;

import java.util.function.Predicate;
import org.jajuk.base.File;
import org.jajuk.base.Playlist;
import org.jajuk.base.Track;
import org.jajuk.services.dj.Ambience;
import org.jajuk.util.Const;

import java.util.Date;

/**
 * List of Predicates (filter criteria)
 * <p>
 * Returns predicates used to decorate iterators
 * </p>.
 */
public class JajukPredicates {
  /**
   * Age-filtering predicate
   */
  public static class AgePredicate implements Predicate<Track> {
    private int iAge = 0;

    public AgePredicate(int iAge) {
      this.iAge = iAge;
    }

    @Override
    public boolean test(Track track) {
      if (track == null) {
        return false;
      }
      Date now = new Date();
      int iTrackAge = (int) ((now.getTime() - track.getDiscoveryDate().getTime()) / Const.MILLISECONDS_IN_A_DAY);
      return iTrackAge <= iAge;
    }
  }

  /**
   * Ready (mounted) filtering predicate Applied on files only.
   */
  public static class ReadyFilePredicate implements Predicate<File> {
    @Override
    public boolean test(File f) {
      return f != null && f.isReady();
    }
  }

  /**
   * Banned filtering predicate Applied against tracks only.
   */
  public static class BannedTrackPredicate implements Predicate<Track> {
    @Override
    public boolean test(Track track) {
      return track != null && !track.getBooleanValue(Const.XML_TRACK_BANNED);
    }
  }

  /**
   * Banned filtering predicate Applied against files only.
   */
  public static class BannedFilePredicate implements Predicate<File> {
    @Override
    public boolean test(File f) {
      if (f == null) {
        return false;
      }
      Track track = f.getTrack();
      return track != null && !track.getBooleanValue(Const.XML_TRACK_BANNED);
    }
  }

  /**
   * Any file available predicate, applies against tracks only.
   */
  public static class AnyFileReady implements Predicate<File> {
    @Override
    public boolean test(File f) {
      if (f == null) {
        return false;
      }
      Track track = f.getTrack();
      return track != null && track.getBestFile(true) != null;
    }
  }

  /**
   * Playlist predicate, filter playlists located on unmounted devices.
   */
  public static class ReadyPlaylistPredicate implements Predicate<Playlist> {
    @Override
    public boolean test(Playlist p) {
      return p != null && p.isReady();
    }
  }

  /**
   * Ambience predicate on files, filter by provided ambience.
   */
  public static class AmbiencePredicate implements Predicate<File> {
    private final Ambience ambience;

    /**
     * Instantiates a new ambience predicate.
     * 
     * @param ambience the ambience
     */
    public AmbiencePredicate(Ambience ambience) {
      this.ambience = ambience;
    }

    @Override
    public boolean test(File tested) {
      if (tested == null || tested.getTrack() == null) {
        return false;
      }
      return ambience.getGenres().contains(tested.getTrack().getGenre());
    }
  }
  
}
