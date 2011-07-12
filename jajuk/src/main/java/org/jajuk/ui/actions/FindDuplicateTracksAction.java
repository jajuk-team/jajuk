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
 *  $Revision$
 */

package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.wizard.DuplicateTracksDialog;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;

/**
 * DOCUMENT_ME.
 */
public class FindDuplicateTracksAction extends JajukAction {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Result : a list of dups files for a given track. */
  List<List<File>> duplicateTracksList;

  /** Temporary storage during dups detection. */
  private Map<String, Set<File>> mapTrackDups;

  /** Track comparator. */
  TrackComparator comparator = new TrackComparator(TrackComparatorType.ALMOST_IDENTICAL);

  /**
   * Instantiates a new find duplicate tracks action.
   */
  FindDuplicateTracksAction() {
    super(Messages.getString("FindDuplicateTracksAction.2"), IconLoader.getIcon(JajukIcons.SEARCH),
        true);
    setShortDescription(Messages.getString("FindDuplicateTracksAction.2"));
  }

  /**
   * Add a dup for a given track.
   * 
   * @param files list of files
   * @param track DOCUMENT_ME
   */
  private void addDup(Track track, List<File> files) {
    // Ignore case where thy are none ready files
    if (files.size() > 0) {
      String key = comparator.buildIdenticalTestFootprint(track).toLowerCase();
      Set<File> dups = mapTrackDups.get(key);
      if (dups == null) {
        // We sort files by path because we don't want to allow user to drop files from different directories
        dups = new TreeSet<File>();
        mapTrackDups.put(key, dups);
      }
      dups.addAll(files);
    }
  }

  /*
   * Return the next track relative to current position or null if it is the last track
   * @return the next track relative to current position or null if it is the last track
   */
  /**
   * Gets the next track.
   * 
   * @param tracks DOCUMENT_ME
   * @param index DOCUMENT_ME
   * 
   * @return the next track
   */
  private Track getNextTrack(List<Track> tracks, int index) {
    Track next = null;
    if (index < tracks.size() - 1) {
      next = tracks.get(index + 1);
    }
    return next;
  }

  /**
   * Return either all or only mounted files for given track
   * according to OPTIONS_HIDE_UNMOUNTED option.
   * 
   * @param track DOCUMENT_ME
   * 
   * @return either all or only mounted files for given track
   */
  private List<File> getFiles(Track track) {
    if (Conf.getBoolean(Const.CONF_OPTIONS_HIDE_UNMOUNTED)) {
      return track.getReadyFiles();
    } else {
      return track.getFiles();
    }
  }

  /**
   * Create the dups list.
   */
  void populateDups() {
    duplicateTracksList = new ArrayList<List<File>>();
    // Use a tree map so footprints are sorted
    mapTrackDups = new TreeMap<String, Set<File>>();

    List<Track> tracks = TrackManager.getInstance().getTracks();

    // For finding duplicate files, we don't just rely on the number of files associated with
    // a track (>1), we also find almost-identical tracks, ie based on album name, not its ID
    // because then, we can't detect identical files located in several directories with a
    // different set of files (because track uses album id in its hashcode and album id uses CDDB discid
    // computed by jajuk based on the duration of all files in a given directory)

    // Sort using the ALMOST-IDENTICAL
    Collections.sort(tracks, comparator);

    int index = 0;
    while (index <= tracks.size() - 1) {
      Track track = tracks.get(index);
      Track next = getNextTrack(tracks, index);
      // 1- Find dups files for the same track
      if (getFiles(track).size() > 1) {
        addDup(track, getFiles(track));
      }
      // 2- Compare each track to find adjacent duplicates (different tracks)
      if (next != null && comparator.compare(track, next) == 0) {
        addDup(track, getFiles(track));
        addDup(next, getFiles(next));
      }
      index++;
    }

    // Build final list (note that it is already sorted by track, mapTrackDups is a TreeMap)
    for (String footprint : mapTrackDups.keySet()) {
      Set<File> dups = mapTrackDups.get(footprint);
      // dups can be 1 in fuzzy search if track1 ~= track2 and track1 files are mounted and not the tracks2's ones
      if (dups.size() > 1) {
        duplicateTracksList.add(new ArrayList<File>(dups));
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(final ActionEvent evt) throws Exception {
    UtilGUI.waiting();
    SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {

      @Override
      protected Void doInBackground() throws Exception {
        populateDups();
        return null;
      }

      @Override
      public void done() {
        try {
          if (duplicateTracksList.size() == 0) {
            Messages.showInfoMessage(Messages.getString("FindDuplicateTracksAction.0"));
          } else {
            final JOptionPane optionPane = UtilGUI.getNarrowOptionPane(100);
            final JDialog duplicateFiles = optionPane.createDialog(null, Messages
                .getString("FindDuplicateTracksAction.3"));
            duplicateFiles.setResizable(true);

            JButton jbClose = new JButton(Messages.getString("Close"));
            jbClose.addActionListener(new ActionListener() {
              @Override
              public void actionPerformed(ActionEvent e) {
                duplicateFiles.dispose();
              }
            });

            // Create and set up the content pane.
            JComponent newContentPane = new DuplicateTracksDialog(duplicateTracksList, jbClose);
            newContentPane.setOpaque(true);
            UtilGUI.setEscapeKeyboardAction(duplicateFiles, newContentPane);
            duplicateFiles.setContentPane(newContentPane);

            // Display the window.
            duplicateFiles.setSize(800, 600);
            duplicateFiles.setLocationRelativeTo(JajukMainWindow.getInstance());
            duplicateFiles.setVisible(true);
          }
        } finally {
          UtilGUI.stopWaiting();
        }
      }
    };
    sw.execute();
  }
}
