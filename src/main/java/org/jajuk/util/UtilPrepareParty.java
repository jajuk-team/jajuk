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
package org.jajuk.util;

import ext.ProcessLauncher;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.jajuk.base.FileManager;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.SmartPlaylist;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.dj.Ambience;
import org.jajuk.services.dj.AmbienceManager;
import org.jajuk.services.dj.DigitalDJ;
import org.jajuk.services.dj.DigitalDJManager;
import org.jajuk.ui.helpers.StarsHelper;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Utilities for the Prepare Party Wizard. Extracted into a separate class for
 * easier testing.
 */
public class UtilPrepareParty {
  /** character that is used to replace if filename normalization is used. */
  private static final String FILLER_CHAR = "_";

  /**
   * Instantiates a new util prepare party.
   * 
   * private constructor to avoid instantiation
   */
  private UtilPrepareParty() {
  }

  /**
   * Filter provided list by removing files that have lower rating.
   * 
   * @param files the list to process.
   * @param rate The require rating level
   * 
   * @return The adjusted list.
   */
  public static List<org.jajuk.base.File> filterRating(List<org.jajuk.base.File> files, Integer rate) {
    final List<org.jajuk.base.File> newFiles = new ArrayList<org.jajuk.base.File>();
    for (org.jajuk.base.File file : files) {
      // only add files that have a rate equal or higher than the level set
      if (StarsHelper.getStarsNumber(file.getTrack()) >= rate) {
        newFiles.add(file);
      }
    }
    return newFiles;
  }

  /**
   * Filter the provided list by removing files if the specified length (in
   * minutes) is exceeded.
   * 
   * @param files The list of files to process.
   * @param time The number of minutes playing length to have at max.
   * 
   * @return The modified list.
   */
  public static List<org.jajuk.base.File> filterMaxLength(List<org.jajuk.base.File> files,
      Integer time) {
    final List<org.jajuk.base.File> newFiles = new ArrayList<org.jajuk.base.File>();
    long accumulated = 0;
    for (org.jajuk.base.File file : files) {
      // check if we now exceed the max length, getDuration() is in seconds, but
      // we want to use minutes
      if ((accumulated + file.getTrack().getDuration()) / 60 > time) {
        return newFiles;
      }
      accumulated += file.getTrack().getDuration();
      newFiles.add(file);
    }
    // there were not enough files to reach the limit, return the full list
    return files;
  }

  /**
   * Filter the provided list by removing files after the specified size is
   * reached.
   * 
   * @param files The list of files to process.
   * @param size The size in MB that should not be exceeded.
   * 
   * @return The modified list.
   */
  public static List<org.jajuk.base.File> filterMaxSize(List<org.jajuk.base.File> files,
      Integer size) {
    final List<org.jajuk.base.File> newFiles = new ArrayList<org.jajuk.base.File>();
    long accumulated = 0;
    for (org.jajuk.base.File file : files) {
      // check if we now exceed the max size, getSize() is in byte, but we want
      // to use MB
      if ((accumulated + file.getSize()) / (1024 * 1024) > size) {
        return newFiles;
      }
      accumulated += file.getSize();
      newFiles.add(file);
    }
    // there were not enough files to reach the limit, return the full list
    return files;
  }

  /**
   * Filter the provided list by removing files after the specified number of
   * tracks is reached.
   * 
   * @param files The list of files to process.
   * @param tracks The number of tracks to limit the list.
   * 
   * @return The modified list.
   */
  public static List<org.jajuk.base.File> filterMaxTracks(List<org.jajuk.base.File> files,
      Integer tracks) {
    final List<org.jajuk.base.File> newFiles = new ArrayList<org.jajuk.base.File>();
    int count = 0;
    for (org.jajuk.base.File file : files) {
      // check if we have reached the max
      if (count > tracks) {
        return newFiles;
      }
      count++;
      newFiles.add(file);
    }
    // there were not enough files to reach the limit, return the full list
    return files;
  }

  /**
   * Filter the provided list by removing files so only the specified media is
   * included.
   * 
   * @param files The list of files to process.
   * @param ext The number of tracks to filter the list.
   * 
   * @return The modified list.
   */
  public static List<org.jajuk.base.File> filterMedia(final List<org.jajuk.base.File> files,
      final String ext) {
    final List<org.jajuk.base.File> newFiles = new ArrayList<org.jajuk.base.File>();
    for (org.jajuk.base.File file : files) {
      if (file.getType() != null && file.getType().getExtension() != null
          && file.getType().getExtension().equals(ext)) {
        newFiles.add(file);
      }
    }
    return newFiles;
  }

  /** Map containing all the replacements that we do to "normalize" a filename. */
  private static Map<Character, String> replaceMap = null;

   /**
   * Get files from the specified DJ.
   * 
   * @param name The name of the DJ.
   * 
   * @return A list of files.
   */
  public static List<org.jajuk.base.File> getDJFiles(final String name) {
    DigitalDJ dj = DigitalDJManager.getInstance().getDJByName(name);
    return dj.generatePlaylist();
  }

  /**
   * Get files from the specified Ambience.
   * 
   * @param name The name of the Ambience.
   * 
   * @return A list of files.
   */
  public static List<org.jajuk.base.File> getAmbienceFiles(String name) {
    final List<org.jajuk.base.File> files;
    Ambience ambience = AmbienceManager.getInstance().getAmbienceByName(name);
    files = new ArrayList<org.jajuk.base.File>();
    // Get a shuffle selection
    List<org.jajuk.base.File> allFiles = FileManager.getInstance().getGlobalShufflePlaylist();
    // Keep only right genres and check for unicity
    for (org.jajuk.base.File file : allFiles) {
      if (ambience.getGenres().contains(file.getTrack().getGenre())) {
        files.add(file);
      }
    }
    return files;
  }

  /**
   * Get files from the specified Playlist. If the name of the playlist is equal
   * to the name of the temporary playlist provided to the Wizard, then this
   * Playlist is used instead.
   * 
   * @param name The name of the Playlist.
   * @param tempPlaylist The playlist provided upon starting of the Wizard, null if none
   * provided.
   * 
   * @return A list of files.
   * 
   * @throws JajukException the jajuk exception
   */
  public static List<org.jajuk.base.File> getPlaylistFiles(String name, Playlist tempPlaylist)
      throws JajukException {
    // if we chose the temp-playlist, use this one
    if (tempPlaylist != null && name.equals(tempPlaylist.getName())) {
      return tempPlaylist.getFiles();
    }
    // get the Playlist from the Manager by name
    Playlist playlist = PlaylistManager.getInstance().getPlaylistByName(name);
    return playlist.getFiles();
  }

  /**
   * Get files in random order.
   * 
   * @return Returns a list of all files shuffled into random order.
   */
  public static List<org.jajuk.base.File> getShuffleFiles() {
    // Get a shuffle selection from all files
    return FileManager.getInstance().getGlobalShufflePlaylist();
  }

  /**
   * Get files from the BestOf-Playlist.
   * 
   * @return The list of files that match the "BestOf"-criteria
   * 
   * @throws JajukException the jajuk exception
   */
  public static List<org.jajuk.base.File> getBestOfFiles() throws JajukException {
    Playlist pl = new SmartPlaylist(Playlist.Type.BESTOF, "tmp", "temporary", null);
    return pl.getFiles();
  }

  /**
   * Get the files from the current "Novelties"-criteria.
   * 
   * @return The files that are new currently.
   * 
   * @throws JajukException the jajuk exception
   */
  public static List<org.jajuk.base.File> getNoveltiesFiles() throws JajukException {
    Playlist pl = new SmartPlaylist(Playlist.Type.NOVELTIES, "tmp", "temporary", null);
    return pl.getFiles();
  }

  /**
   * Get the files from the current Queue.
   * 
   * @return The currently queued files.
   * 
   * @throws JajukException the jajuk exception
   */
  public static List<org.jajuk.base.File> getQueueFiles() throws JajukException {
    Playlist pl = new SmartPlaylist(Playlist.Type.QUEUE, "tmp", "temporary", null);
    return pl.getFiles();
  }

  /**
   * Get the files that are bookmarked.
   * 
   * @return The currently bookmarked files.
   * 
   * @throws JajukException the jajuk exception
   */
  public static List<org.jajuk.base.File> getBookmarkFiles() throws JajukException {
    Playlist pl = new SmartPlaylist(Playlist.Type.BOOKMARK, "tmp", "temporary", null);
    return pl.getFiles();
  }

  /**
   * Split the commandline into separate elements by observing double quotes.
   * 
   * @param command The command in one string. E.g. "perl /usr/bin/pacpl".
   * 
   * @return A list of single command elements. e.g. {"perl", "/usr/bin/pacpl"}
   */
  private static List<String> splitCommand(String command) {
    List<String> list = new ArrayList<String>();
    StringBuilder word = new StringBuilder();
    boolean quote = false;
    int i = 0;
    while (i < command.length()) {
      char c = command.charAt(i);
      // word boundary
      if (Character.isWhitespace(c) && !quote) {
        i++;
        // finish current word
        list.add(word.toString());
        word = new StringBuilder();
        // skip more whitespaces
        while (Character.isWhitespace(command.charAt(i)) && i < command.length()) {
          i++;
        }
      } else {
        // on quote we either start or end a quoted string
        if (c == '"') {
          quote = !quote;
        }
        word.append(c);
        i++;
      }
    }
    // finish last word
    if (word.length() > 0) {
      list.add(word.toString());
    }
    return list;
  }

  /**
   * Check if the Perl Audio Converter can be used.
   * 
   * @param pacpl The command-string to call pacpl, e.g. "pacpl" or "perl
   * C:\pacpl\pacpl", ...
   * 
   * @return true, if check pacpl
   */
  public static boolean checkPACPL(String pacpl) {
    // here we just want to verify that we find pacpl
    // first build the commandline for "pacpl --help"
    // see the manual page of "pacpl"
    List<String> list = splitCommand(pacpl);
    list.add("--help");
    // create streams for catching stdout and stderr
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    int ret = 0;
    final ProcessLauncher launcher = new ProcessLauncher(out, err, 10000);
    try {
      ret = launcher.exec(list.toArray(new String[list.size()]));
    } catch (IOException e) {
      ret = -1;
      Log.debug("Exception while checking for 'pacpl', cannot use functionality to convert media files while copying: "
          + e.getMessage());
    }
    // if we do not find the application or if we got an error, log some details
    // and disable notification support
    if (ret != 0) {
      // log out the results
      Log.debug("pacpl command returned to out(" + ret + "): " + out.toString());
      Log.debug("pacpl command returned to err: " + err.toString());
      Log.info("Cannot use functionality to convert media files, application 'pacpl' seems to be not available correctly.");
      return false;
    }
    // pacpl is enabled and seems to be supported by the OS
    return true;
  }

  /**
   * Call the external application "pacpl" to convert the specified file into
   * the specified format and store the resulting file in the directory listed.
   * 
   * @param pacpl The command-string to call pacpl, e.g. "pacpl" or "perl
   * C:\pacpl\pacpl", ...
   * @param file The file to convert.
   * @param toFormat The target format.
   * @param toDir The target location.
   * @param newName The new name to use (this is used for normalizing and numbering
   * the files, ...)
   * 
   * @return 0 if processing was OK, otherwise the return code indicates the
   * return code provided by the pacpl script
   * 
   * TODO: currently this uses the target-location as temporary
   * directory if intermediate-conversion to WAV is necessary, this
   * might be sub-optimal for Flash-memory where too many writes kills
   * the media card earlier. We probably should use the temporary
   * directory for conversion instead and do another copy at the end.
   */
  public static int convertPACPL(String pacpl, File file, String toFormat, java.io.File toDir,
      String newName) {
    // first build the commandline for "pacpl"
    // see the manual page of "pacpl"
    // first split the command itself with observing quotes, splitting is
    // necessary because it can be something like "perl <locatoin>/pacpl"
    List<String> list = splitCommand(pacpl);
    // where to store the file
    list.add("--outdir");
    list.add(toDir.getAbsolutePath());
    // specify new filename
    list.add("--outfile");
    list.add(newName);
    // specify output format
    list.add("--to");
    list.add(toFormat);
    // now add the actual file to convert
    list.add(file.getAbsolutePath());
    // create streams for catching stdout and stderr
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    int ret = 0;
    StringBuilder commandLog = new StringBuilder();
    for (String arg : list) {
      commandLog.append(arg + " ");
    }
    Log.debug("Using this pacpl command: {{" + commandLog.toString() + "}}");
    final ProcessLauncher launcher = new ProcessLauncher(out, err);
    try {
      ret = launcher.exec(list.toArray(new String[list.size()]), null,
          new java.io.File(System.getProperty("java.io.tmpdir")));
    } catch (IOException e) {
      ret = -1;
      Log.error(e);
    }
    // log out the results
    if (!out.toString().isEmpty()) {
      Log.debug("pacpl command returned to out(" + ret + "): " + out.toString());
      if (out.toString().indexOf("encode failed") != -1) {
        ret = -1;
      }
    } else {
      Log.debug("pacpl command returned: " + ret);
    }
    if (!err.toString().isEmpty()) {
      Log.debug("pacpl command returned to err: " + err.toString());
      if (err.toString().indexOf("encode failed") != -1) {
        ret = -1;
      }
    }
    return ret;
  }

  /**
   * Copies the files contained in the list to the specified directory.
   * 
   * @param files The list of flies to copy.
   * @param destDir The target location.
   * @param isNormalize 
   * @param isConvertMedia 
   * @param media 
   * @param convertCommand 
   */
  public static void copyFiles(final List<org.jajuk.base.File> files, final java.io.File destDir,
      final boolean isNormalize, final boolean isConvertMedia, final String media,
      final String convertCommand) {
    Thread thread = new Thread("PrepareParty - File Copy") {
      @Override
      public void run() {
        UtilGUI.waiting();
        // start time to display elapsed time at the end
        long lRefreshDateStart = System.currentTimeMillis();
        // start copying and create a playlist on the fly
        int convert_errors = 0;
        final java.io.File file = new java.io.File(destDir.getAbsolutePath() + "/playlist.m3u");
        try {
          final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
          try {
            bw.write(Const.PLAYLIST_NOTE);
            int count = 0;
            for (final org.jajuk.base.File entry : files) {
              // update progress
              count++;
              // We can use the actual file name as we do numbering of the files,
              // this is important for existing playlists to keep the order
              String name = StringUtils.leftPad(Integer.valueOf(count).toString(), 5, '0') + '_'
                  + entry.getFIO().getName();
              // normalize filenames if necessary
              if (isNormalize) {
                name = StringUtils.stripAccents(name);
              }
              // check if we need to convert the file format
              if (isConvertMedia && !entry.getType().getExtension().equals(media)) {
                // Notify that we are converting a file
                Properties properties = new Properties();
                properties.put(Const.DETAIL_CONTENT, entry.getName());
                properties.put(Const.DETAIL_NEW, name + "." + media);
                ObservationManager.notify(new JajukEvent(JajukEvents.FILE_CONVERSION, properties));
                int ret = UtilPrepareParty.convertPACPL(convertCommand, entry.getFIO(), media,
                    destDir, name);
                if (ret != 0) {
                  convert_errors++;
                  // do a normal copy of original format if it cannot be converted
                  FileUtils.copyFile(entry.getFIO(), new File(destDir, name));
                } else {
                  // Conversion is done, new filename is <oldname.old_extension.target_extension>
                  name = name + "." + media;
                }
              } else {
                // do a normal copy otherwise
                FileUtils.copyFile(entry.getFIO(), new File(destDir, name));
              }
              // increase hits for this track/file as it is likely played outside of Jajuk
              entry.getTrack().incHits();
              // write playlist as well
              bw.newLine();
              bw.write(name);
              // Notify that a file has been copied
              Properties properties = new Properties();
              properties.put(Const.DETAIL_CONTENT, entry.getName());
              ObservationManager.notify(new JajukEvent(JajukEvents.FILE_COPIED, properties));
            }
            bw.flush();
          } finally {
            bw.close();
          }
          // Send a last event with null properties to inform the
          // client that the party is done
          ObservationManager.notify(new JajukEvent(JajukEvents.FILE_COPIED));
        } catch (final IOException e) {
          Log.error(e);
          Messages.showErrorMessage(180, e.getMessage());
          return;
        } finally {
          long refreshTime = System.currentTimeMillis() - lRefreshDateStart;
          // inform the user about the number of resulting tracks
          StringBuilder sbOut = new StringBuilder();
          sbOut.append(Messages.getString("PreparePartyWizard.31")).append(" ")
              .append(destDir.getAbsolutePath()).append(".\n").append(files.size()).append(" ")
              .append(Messages.getString("PreparePartyWizard.23")).append(" ")
              .append(((refreshTime < 1000) ? refreshTime + " ms." : refreshTime / 1000 + " s."));
          // inform user if converting did not work
          if (convert_errors > 0) {
            sbOut.append("\n").append(Integer.toString(convert_errors))
                .append(Messages.getString("PreparePartyWizard.36"));
          }
          String message = sbOut.toString();
          Log.debug(message);
          UtilGUI.stopWaiting();
          // Display end of copy message with stats
          Messages.showInfoMessage(message);
        }
      }
    };
    thread.start();
  }
}
