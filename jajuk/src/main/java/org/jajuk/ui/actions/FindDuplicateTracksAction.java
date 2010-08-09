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

package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackManager;
import org.jajuk.base.TrackComparator.TrackComparatorType;
import org.jajuk.ui.widgets.DuplicateTracksList;
import org.jajuk.ui.windows.JajukMainWindow;
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

  /**
   * Instantiates a new find duplicate tracks action.
   */
  FindDuplicateTracksAction() {
    super(Messages.getString("FindDuplicateTracksAction.2"), IconLoader.getIcon(JajukIcons.SEARCH),
        true);
    setShortDescription(Messages.getString("FindDuplicateTracksAction.2"));
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

      private List<List<File>> duplicateTracksList = null;

      @Override
      protected Void doInBackground() throws Exception {
        duplicateTracksList = new ArrayList<List<File>>();
        List<Track> tracks = TrackManager.getInstance().getTracks();
        // For finding duplicate files, we don't just rely on the number of files associated with
        // a track (>1), we also find almost-identical tracks, ie based on album name, not its ID
        // because then, we can't detect identical files located in several directories with a
        // different
        // set of files (because track uses album id in its hashcode and album id uses CDDB discid
        // computed
        // by jajuk based on the duration of all files in a given directory)

        // Sort using the ALMOST-IDENTICAL
        TrackComparator comparator = new TrackComparator(TrackComparatorType.ALMOST_IDENTICAL);
        Collections.sort(tracks, comparator);
        // Now re-compare each track to find adjacent duplicates
        Track previous = null;
        for (Track track : tracks) {
          if (previous != null && comparator.compare(previous, track) == 0) {
            Set<File> duplicateFilesSet = new HashSet<File>();
            // Only consider ready files because we want user to be able to drop files
            duplicateFilesSet.addAll(previous.getReadyFiles());
            duplicateFilesSet.addAll(track.getReadyFiles());
            duplicateTracksList.add(new ArrayList<File>(duplicateFilesSet));
          }
          previous = track;
        }
        return null;
      }

      @Override
      public void done() {
        try {
          if (duplicateTracksList.size() < 1) {
            Messages.showInfoMessage(Messages.getString("FindDuplicateTracksAction.0"));
          } else {
            final JOptionPane optionPane = UtilGUI.getNarrowOptionPane(100);
            final JDialog duplicateFiles = optionPane.createDialog(null, Messages
                .getString("FindDuplicateTracksAction.3"));
            duplicateFiles.setResizable(true);

            JButton jbClose = new JButton(Messages.getString("Close"));
            jbClose.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) {
                duplicateFiles.dispose();
              }
            });

            // Create and set up the content pane.
            JComponent newContentPane = new DuplicateTracksList(duplicateTracksList, jbClose);
            newContentPane.setOpaque(true);
            UtilGUI.setEscapeKeyboardAction(duplicateFiles, newContentPane);
            duplicateFiles.setContentPane(newContentPane);

            // Display the window.
            duplicateFiles.pack();
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
