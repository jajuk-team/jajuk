/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

package org.jajuk.util.filters;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.Predicate;
import org.jajuk.base.File;
import org.jajuk.base.Playlist;
import org.jajuk.base.Track;
import org.jajuk.services.dj.Ambience;
import org.jajuk.util.Const;

/**
 * List of Predicates (filter criteria)
 * <p>
 * Returns predicates used to decorate iterators
 * </p>.
 */
public class JajukPredicates {

  /**
   * Age-filtering predicate Applied on tracks only.
   */
  public static class AgePredicate implements Predicate {

    /** DOCUMENT_ME. */
    private int iAge = 0;

    /**
     * Instantiates a new age predicate.
     * 
     * @param iAge DOCUMENT_ME
     */
    public AgePredicate(int iAge) {
      this.iAge = iAge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate(Object o) {
      if (!(o instanceof Track)) {
        return false;
      }
      Track track = (Track) o;
      Date now = new Date();
      int iTrackAge = (int) ((now.getTime() - track.getDiscoveryDate().getTime()) / Const.MILLISECONDS_IN_A_DAY);
      if (iTrackAge <= iAge) {
        return true;
      }
      return false;
    }

  }

  /**
   * Ready (mounted) filtering predicate Applied on files only.
   */
  public static class ReadyFilePredicate implements Predicate {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate(Object o) {
      return ((File) o).isReady();
    }

  }

  /**
   * Banned filtering predicate Applied against tracks only.
   */
  public static class BannedTrackPredicate implements Predicate {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate(Object o) {
      Track track = (Track) o;
      return !(track.getBooleanValue(Const.XML_TRACK_BANNED));
    }

  }

  /**
   * Banned filtering predicate Applied against files only.
   */
  public static class BannedFilePredicate implements Predicate {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate(Object o) {
      Track track = ((File) o).getTrack();
      return !(track.getBooleanValue(Const.XML_TRACK_BANNED));
    }

  }

  /**
   * Any file available predicate, applies against tracks only.
   */
  public static class AnyFileReady implements Predicate {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate(Object o) {
      Track track = ((File) o).getTrack();
      return track.getBestFile(true) != null;
    }

  }

  /**
   * Playlist predicate, filter playlists located on unmounted devices.
   */
  public static class ReadyPlaylistPredicate implements Predicate {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate(Object o) {
      return ((Playlist) o).isReady();
    }

  }

  /**
   * Ambience predicate on files, filter by provided ambience.
   */
  public static class AmbiencePredicate implements Predicate {

    /** DOCUMENT_ME. */
    private final Ambience ambience;

    /**
     * Instantiates a new ambience predicate.
     * 
     * @param ambience DOCUMENT_ME
     */
    public AmbiencePredicate(Ambience ambience) {
      this.ambience = ambience;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate(Object o) {
      File tested = (File) o;
      return ambience.getGenres().contains(tested.getTrack().getGenre());
    }

  }

  /**
   * Not Video predicate on tracks, filter video files.
   */
  public static class NotVideoPredicate implements Predicate {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.commons.collections.Predicate#evaluate(java.lang.Object)
     */
    public boolean evaluate(Object o) {
      Track tested = (Track) o;
      List<File> files = tested.getFiles();
      File fileTested = files.get(0);
      return !fileTested.getType().isVideo();
    }

  }

}
