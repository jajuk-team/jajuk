/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision: 3974 $$
 */
package org.jajuk.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Item;
import org.jajuk.base.LogicalItem;
import org.jajuk.base.Playlist;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.players.StackItem;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * General use utilities methods
 */
public final class UtilFeatures {

  /**
   * Genres
   */
  public static final String[] GENRES = { "Blues", "Classic Rock", "Country", "Dance", "Disco",
      "Funk", "Grunge", "Hip-Hop", "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B",
      "Rap", "Reggae", "Rock", "Techno", "Industrial", "Alternative", "Ska", "Death Metal",
      "Pranks", "Soundtrack", "Euro-Techno", "Ambient", "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion",
      "Trance", "Classical", "Instrumental", "Acid", "House", "Game", "Sound Clip", "Gospel",
      "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
      "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic",
      "Pop-Folk", "Eurodance", "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40",
      "Christian Rap", "Pop/Funk", "Jungle", "Native American", "Cabaret", "New Wave",
      "Psychedelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal", "Acid Punk", "Acid Jazz",
      "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock",
      "National Folk", "Swing", "Fast Fusion", "Bebob", "Latin", "Revival", "Celtic", "Bluegrass",
      "Avantgarde", "Gothic Rock", "Progressive Rock", "Psychedelic Rock", "Symphonic Rock",
      "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour", "Speech",
      "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Brass", "Primus",
      "Porn Groove", "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad",
      "Power Ballad", "Rhytmic Soul", "Freestyle", "Duet", "Punk Rock", "Drum Solo", "Acapella",
      "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club-House", "Hardcore", "Terror",
      "Indie", "BritPop", "Negerpunk", "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal",
      "Black Metal", "Crossover", "Contemporary C", "Christian Rock", "Merengue", "Salsa",
      "Thrash Metal", "Anime", "JPop", "SynthPop" };

  /**
   * @param alFiles
   * @return Given list to play with shuffle or others runles applied
   */
  @SuppressWarnings("unchecked")
  public static List<org.jajuk.base.File> applyPlayOption(final List<org.jajuk.base.File> alFiles) {
    if (Conf.getBoolean(Const.CONF_STATE_SHUFFLE)) {
      // we need all these casts for clone() to be callable here
      final List<org.jajuk.base.File> alFilesToPlay = (List<org.jajuk.base.File>) ((ArrayList<org.jajuk.base.File>) alFiles)
          .clone();
      Collections.shuffle(alFilesToPlay, UtilSystem.getRandom());
      return alFilesToPlay;
    }
    return alFiles;
  }

  /**
   * Convert a list of files into a list of StackItem
   * <p>
   * null files are ignored
   * </p>
   * 
   * @param alFiles
   * @param bRepeat
   * @param bUserLauched
   * @return
   */
  public static List<StackItem> createStackItems(final List<org.jajuk.base.File> alFiles,
      final boolean bRepeat, final boolean bUserLauched) {
    final List<StackItem> alOut = new ArrayList<StackItem>(alFiles.size());
    for (org.jajuk.base.File file : alFiles) {
      if (file != null) {
        try {
          final StackItem item = new StackItem(file);
          item.setRepeat(bRepeat);
          item.setUserLaunch(bUserLauched);
          alOut.add(item);
        } catch (final JajukException je) {
          Log.error(je);
        }
      }
    }
    return alOut;
  }

  /**
   * Filter a given file list by ambience
   * 
   * @param al
   *          file list
   * @param ambience
   *          ambience
   * @return the list filtered
   */
  public static List<org.jajuk.base.File> filterByAmbience(final List<org.jajuk.base.File> al,
      final Ambience ambience) {
    // Void filter, return the input
    if ((ambience == null) || (ambience.getStyles().size() == 0)) {
      return al;
    }
    // Filter by ambience
    final List<org.jajuk.base.File> out = new ArrayList<org.jajuk.base.File>(al.size() / 2);
    for (final org.jajuk.base.File file : al) {
      if (ambience.getStyles().contains(file.getTrack().getStyle())) {
        out.add(file);
      }
    }
    return out;
  }

  /**
   * Convenient method for getPlayableFiles(collection<item>)
   * 
   * @param item
   * @return files
   */
  public static List<org.jajuk.base.File> getPlayableFiles(Item item) {
    List<Item> list = new ArrayList<Item>(1);
    list.add(item);
    return getPlayableFiles(list);
  }

  /**
   * Computes file selection from item collection
   * <p>
   * We assume that the collection elements all own the same type
   * </p>
   * Unmounted files are selected according to the value of
   * CONF_OPTIONS_HIDE_UNMOUNTED option
   * 
   * @param selection
   *          an item selection (directories, files...)
   * @return the files (empty list if none matching)
   */
  public static List<org.jajuk.base.File> getPlayableFiles(List<Item> selection) {
    // computes selection
    List<org.jajuk.base.File> files = new ArrayList<org.jajuk.base.File>(100);
    if (selection == null || selection.size() == 0) {
      return files;
    }
    for (Item item : selection) {
      // computes logical selection if any
      if (item instanceof Track) {
        files.add(((Track) item).getPlayeableFile(Conf
            .getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)));
      } else if (item instanceof LogicalItem) {
        List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item);
        for (Track track : tracks) {
          files.add(track.getPlayeableFile(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)));
        }
      }
      // computes physical selection if any
      else if (item instanceof org.jajuk.base.File) {
        files.add((org.jajuk.base.File) item);
      } else if (item instanceof Directory) {
        files = ((Directory) item).getFilesRecursively();
      } else if (item instanceof Device) {
        files = ((Device) item).getFilesRecursively();
      } else if (item instanceof Playlist) {
        try {
          files = ((Playlist) item).getFiles();
        } catch (JajukException e) {
          Log.error(e);
        }
      }
    }
    return files;
  }

  /**
   * @param col
   * @return a single shuffle element from a list, null if none element in
   *         provided collection
   */
  public static Object getShuffleItem(final Collection<? extends Object> col) {
    if (col.size() == 0) {
      return null;
    }
    List<? extends Object> list = null;
    if (col instanceof List) {
      list = (List<? extends Object>) col;
    } else {
      list = new ArrayList<Object>(col);
    }
    return list.get((int) (Math.random() * list.size()));
  }

  /** Return a genre string for a given genre id * */
  public static String getStringGenre(final int i) {
    if ((i >= 0) && (i < 126)) {
      return GENRES[i];
    } else {
      return Messages.getString("unknown_style");
    }
  }

  /**
   * Tell whether a file is an absolute default cover or not
   * 
   * @param directory
   *          Jajuk Directory in which we analyze the given file name
   * @param sFileName
   * @return whether the given filename is an absolute default cover
   */
  public static boolean isAbsoluteDefaultCover(final Directory directory, final String sFilename) {
    final String sDefault = directory.getStringValue(Const.XML_DIRECTORY_DEFAULT_COVER);
    if ((sDefault != null) && sDefault.equals(sFilename)) {
      return true;
    }
    return false;
  }

  /**
   * @param sFileName
   * @return whether the given filename is a standard cover or not
   */
  public static boolean isStandardCover(final File file) {
    String sFileName = file.getName();
    return sFileName.toLowerCase().matches(".*" + Const.FILE_DEFAULT_COVER + ".*")
        || sFileName.toLowerCase().matches(".*" + Const.FILE_DEFAULT_COVER_2 + ".*")
        // just for previous compatibility, now it is a directory
        // property
        || sFileName.toLowerCase().matches(".*" + Const.FILE_ABSOLUTE_DEFAULT_COVER + ".*");

  }

  /**
   * No constructor
   */
  private UtilFeatures() {
  }

  /**
   * Try to compute time length in milliseconds using BasicPlayer API. (code
   * from jlGui 2.3)
   */
  public static long getTimeLengthEstimation(final Map<String, Object> properties) {
    long milliseconds = -1;
    int byteslength = -1;
    if (properties != null) {
      if (properties.containsKey("audio.length.bytes")) {
        byteslength = ((Integer) properties.get("audio.length.bytes")).intValue();
      }
      if (properties.containsKey("duration")) {
        milliseconds = (((Long) properties.get("duration")).longValue()) / 1000;
      } else {
        // Try to compute duration
        int bitspersample = -1;
        int channels = -1;
        float samplerate = -1.0f;
        int framesize = -1;
        if (properties.containsKey("audio.samplesize.bits")) {
          bitspersample = ((Integer) properties.get("audio.samplesize.bits")).intValue();
        }
        if (properties.containsKey("audio.channels")) {
          channels = ((Integer) properties.get("audio.channels")).intValue();
        }
        if (properties.containsKey("audio.samplerate.hz")) {
          samplerate = ((Float) properties.get("audio.samplerate.hz")).floatValue();
        }
        if (properties.containsKey("audio.framesize.bytes")) {
          framesize = ((Integer) properties.get("audio.framesize.bytes")).intValue();
        }
        if (bitspersample > 0) {
          milliseconds = (int) (1000.0f * byteslength / (samplerate * channels * (bitspersample / 8)));
        } else {
          milliseconds = (int) (1000.0f * byteslength / (samplerate * framesize));
        }
      }
    }
    return milliseconds;
  }

  /**
   * 
   * @param selection
   * @return first item in selection preference
   */
  public static long getPreferenceForSelection(List<? extends Item> selection) {
    if (selection.size() == 0) {
      return Const.PREFERENCE_UNSET;
    }
    List<Track> trackList = new ArrayList<Track>(10);
    // For each entry of the selection (can be album, year, track,
    // directory...),
    // we add all associated tracks and we get equals preference if any
    for (Item i : selection) {
      trackList.addAll(TrackManager.getInstance().getAssociatedTracks(i));
    }
    // List shouldn't be void but we test it for security
    if (selection.size() == 0) {
      return Const.PREFERENCE_UNSET;
    }
    Track firstTrack = trackList.get(0);
    long preferenceFirstItem = firstTrack.getLongValue(Const.XML_TRACK_PREFERENCE);
    for (int i = 1; i < trackList.size(); i++) {
      Track track = trackList.get(i);
      if (track.getLongValue(Const.XML_TRACK_PREFERENCE) != preferenceFirstItem) {
        return Const.PREFERENCE_UNSET;
      }
    }
    return preferenceFirstItem;
  }

}
