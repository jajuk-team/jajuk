/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.LogicalItem;
import org.jajuk.base.Playlist;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.players.StackItem;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * General use utilities methods.
 */
public final class UtilFeatures {

  /** Genres. */
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
   * Apply play option.
   * 
   * @param alFiles DOCUMENT_ME
   * 
   * @return Given list to play with shuffle or others rules applied
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
   * </p>.
   * 
   * @param alFiles DOCUMENT_ME
   * @param bRepeat DOCUMENT_ME
   * @param bUserLauched DOCUMENT_ME
   * 
   * @return the list< stack item>
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
   * Filter a given file list by ambience.
   * 
   * @param al file list
   * @param ambience ambience
   * 
   * @return the list filtered or a void list if none available track
   */
  public static List<org.jajuk.base.File> filterByAmbience(final List<org.jajuk.base.File> al,
      final Ambience ambience) {
    // If track list is null, return a void list
    if (al == null || al.size() == 0) {
      return new ArrayList<org.jajuk.base.File>(0);
    }
    // Void filter, return the input
    if ((ambience == null) || (ambience.getGenres().size() == 0)) {
      return al;
    }
    // Filter by ambience
    final List<org.jajuk.base.File> out = new ArrayList<org.jajuk.base.File>(al.size() / 2);
    for (final org.jajuk.base.File file : al) {
      if (ambience.getGenres().contains(file.getTrack().getGenre())) {
        out.add(file);
      }
    }
    return out;
  }

  /**
   * Convenient method for getPlayableFiles(collection<item>).
   * 
   * @param item DOCUMENT_ME
   * 
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
   * CONF_OPTIONS_HIDE_UNMOUNTED option.
   * 
   * @param selection an item selection (directories, files...)
   * 
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
        files.add(((Track) item).getBestFile(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)));
      } else if (item instanceof LogicalItem) {
        List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(item, true);
        for (Track track : tracks) {
          files.add(track.getBestFile(Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)));
        }
      }
      // computes physical selection if any
      else if (item instanceof org.jajuk.base.File) {
        files.add((org.jajuk.base.File) item);
      } else if (item instanceof Directory) {
        files.addAll(((Directory) item).getFilesRecursively());
      } else if (item instanceof Device) {
        files.addAll(((Device) item).getFilesRecursively());
      } else if (item instanceof Playlist) {
        try {
          files.addAll(((Playlist) item).getFiles());
        } catch (JajukException e) {
          Log.error(e);
        }
      }
    }
    return files;
  }

  /**
   * Gets the shuffle item.
   *
   * @param <T> DOCUMENT_ME
   * @param col DOCUMENT_ME
   * @return a single shuffle element from a list, null if none element in
   * provided collection
   */
  public static <T> T getShuffleItem(final Collection<T> col) {
    if (col.size() == 0) {
      return null;
    }
    List<T> list = null;
    if (col instanceof List<?>) {
      list = (List<T>) col;
    } else {
      list = new ArrayList<T>(col);
    }
    return list.get((int) (Math.random() * list.size()));
  }

  /**
   * Return a genre string for a given genre id *.
   * 
   * @param i DOCUMENT_ME
   * 
   * @return the string genre
   */
  public static String getStringGenre(final int i) {
    if ((i >= 0) && (i < 126)) {
      return GENRES[i];
    } else {
      return Messages.getString("unknown_genre");
    }
  }

  /**
   * Checks if is standard cover.
   * 
   * @param file DOCUMENT_ME
   * 
   * @return whether the given filename is a standard cover or not
   */
  public static boolean isStandardCover(final java.io.File file) {
    boolean defaultCover = false;
    String sFileName = file.getName();

    Scanner s = new Scanner(Conf.getString(Const.FILE_DEFAULT_COVER)).useDelimiter(";");
    while (s.hasNext()) {
      String next = s.next();
      defaultCover = sFileName.toLowerCase(Locale.getDefault()).matches(".*" + next + ".*");
      if (defaultCover) {
        break;
      }
    }
    s.close();

    if (!defaultCover) {
      // just for previous compatibility, now it is a directory
      // property
      defaultCover = sFileName.toLowerCase(Locale.getDefault()).matches(
          ".*" + Const.FILE_ABSOLUTE_DEFAULT_COVER + ".*");
    }

    return defaultCover;
  }

  /**
   * No constructor.
   */
  private UtilFeatures() {
  }

  /**
   * Try to compute time length in milliseconds using BasicPlayer API. (code
   * from jlGui 2.3)
   * 
   * @param properties DOCUMENT_ME
   * 
   * @return the time length estimation
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
          milliseconds = (int) (1000.0f * byteslength / (samplerate * channels * (((float) bitspersample) / 8)));
        } else {
          milliseconds = (int) (1000.0f * byteslength / (samplerate * framesize));
        }
      }
    }
    return milliseconds;
  }

  /**
   * Gets the preference for selection.
   * 
   * @param selection DOCUMENT_ME
   * 
   * @return first item in selection preference
   */
  public static long getPreferenceForSelection(List<? extends Item> selection) {
    // We compute preference of first item selection) {
    if (selection.size() == 0) {
      return Const.PREFERENCE_UNSET;
    }
    List<Item> items = new ArrayList<Item>(selection);
    // For each entry of the selection (can be album, year, track,
    // directory...),
    // we add all associated tracks and we get equals preference if any
    List<Track> trackList = TrackManager.getInstance().getAssociatedTracks(items, false);
    // List shouldn't be void (except on collection node selection in tree view
    // for ie)
    if (trackList.size() == 0) {
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

  /**
   * Perform updates on this view to reflect current playing item status.
   * 
   * @param oberver the observer to update
   */
  public static void updateStatus(Observer oberver) {
    // check if a track or a webradio has already been launched
    if (QueueModel.isPlayingRadio()) {
      Properties webradioInfoUpdatedEvent = ObservationManager.getDetailsLastOccurence(JajukEvents.WEBRADIO_INFO_UPDATED);
      Properties webradioLaunchedEvent = ObservationManager.getDetailsLastOccurence(JajukEvents.WEBRADIO_LAUNCHED);
      WebRadio updatedWebRadio = (WebRadio) webradioInfoUpdatedEvent.get(Const.DETAIL_CONTENT);
      WebRadio radio = (WebRadio) webradioLaunchedEvent.get(Const.DETAIL_CONTENT);
      //If web radio has an updated event then use that event else use the default event from the web radio launch      
      if(radio.getName().equals(updatedWebRadio.getName())){
        oberver.update(new JajukEvent(JajukEvents.WEBRADIO_INFO_UPDATED, ObservationManager
            .getDetailsLastOccurence(JajukEvents.WEBRADIO_INFO_UPDATED)));
      }
      else{
        oberver.update(new JajukEvent(JajukEvents.WEBRADIO_LAUNCHED, ObservationManager
            .getDetailsLastOccurence(JajukEvents.WEBRADIO_LAUNCHED)));
      }

    } else if (!QueueModel.isStopped()) {
      oberver.update(new JajukEvent(JajukEvents.FILE_LAUNCHED, ObservationManager
          .getDetailsLastOccurence(JajukEvents.FILE_LAUNCHED)));
      oberver.update(new JajukEvent(JajukEvents.PLAYER_PLAY, ObservationManager
          .getDetailsLastOccurence(JajukEvents.PLAYER_PLAY)));
    } else {
      // if queue is not empty we can activate the control buttons
      if (QueueModel.getQueueSize() > 0) {
        oberver.update(new JajukEvent(JajukEvents.PLAYER_STOP));
      } else {
        oberver.update(new JajukEvent(JajukEvents.ZERO));
      }
    }
    // Force update due to parameter changes
    oberver.update(new JajukEvent(JajukEvents.PARAMETERS_CHANGE));
  }

  /**
   * Return sum of decimal digits in n. Code from
   * http://www.cs.princeton.edu/introcs/51data/CDDB.java.html
   * 
   * @param n DOCUMENT_ME
   * 
   * @return the long
   */
  private static long sumOfDigits(long n) {
    long i = n;
    long sum = 0;
    while (i > 0) {
      sum = sum + (i % 10);
      i = i / 10;
    }
    return sum;
  }

  /**
   * Computes a disk id. Code based on
   * http://www.cs.princeton.edu/introcs/51data/CDDB.java.html
   * 
   * @param durations List of durations
   * 
   * @return the disk ID as a long
   */
  public static long computeDiscID(List<Long> durations) {
    int totalLength = 0;
    int nbTracks = durations.size();
    for (Long l : durations) {
      totalLength += l;
    }
    int checkSum = 0;
    for (Long duration : durations) {
      checkSum += sumOfDigits(duration);
    }
    long xx = checkSum % 255;
    long yyyy = totalLength;
    long zz = nbTracks;
    // XXYYYYZZ
    return ((xx << 24) | (yyyy << 8) | zz);
  }

  /**
   * Shuffle a list of items and ensure that final list first element
   * is different from the initial list's one
   * <p>The list should not be void</p>.
   * 
   * @param list DOCUMENT_ME
   * 
   * @return shuffled list
   */
  public static void forcedShuffle(List<StackItem> list) {
    StackItem first = list.get(0);
    StackItem newFirst = first;
    while (newFirst.equals(first)) {
      Collections.shuffle(list, UtilSystem.getRandom());
      newFirst = list.get(0);
    }
  }

  /**
   * Return a flat list of files for given input list without duplicates nor sorting.
   * 
   * @param in DOCUMENT_ME
   * 
   * @return a flat list of files for given input list
   * 
   * @throws JajukException if a playlist cannot be read
   */
  public static List<File> getFilesForItems(List<Item> in) throws JajukException {
    // We use a set to drop duplicates, for ie if user select both a directory and its files
    Set<File> out = new LinkedHashSet<File>(in.size());
    for (Item item : in) {
      if (item instanceof File) {
        out.add((File) item);
      } else if (item instanceof Directory) {
        Directory dir = (Directory) item;
        out.addAll(dir.getFilesRecursively());
      } else if (item instanceof Directory) {
        Directory dir = (Directory) item;
        out.addAll(dir.getFilesRecursively());
      } else if (item instanceof Device) {
        Device device = (Device) item;
        out.addAll(device.getFilesRecursively());
      } else if (item instanceof Playlist) {
        Playlist playlist = (Playlist) item;
        out.addAll(playlist.getFiles());
      } else if (item instanceof LogicalItem) {
        LogicalItem logical = (LogicalItem) item;
        List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(logical, false);
        for (Track track : tracks) {
          // Only keep available tracks, show a warning if no available file
          File file = track.getBestFile(true);
          if (file == null) {
            InformationJPanel.getInstance().setMessage(
                Messages.getString("Error.010") + " : " + track.getName(),
                InformationJPanel.MessageType.WARNING);
          } else {
            out.add(track.getBestFile(true));
          }

        }
      }
    }
    return new ArrayList<File>(out);
  }
}
