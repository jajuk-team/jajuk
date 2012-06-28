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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.PlaylistManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.players.QueueModel;
import org.jajuk.services.webradio.WebRadio;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * .
 */
public class DeleteSelectionAction extends SelectionAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  private List<File> alFiles;
  private List<File> rejFiles;
  private List<Directory> alDirs;
  private List<Directory> rejDirs;
  private List<Directory> emptyDirs;

  /**
   * Instantiates a new delete selection action.
   */
  DeleteSelectionAction() {
    super(Messages.getString("FilesTreeView.7"), IconLoader.getIcon(JajukIcons.DELETE), true);
    setAcceleratorKey(KeyStroke.getKeyStroke("DELETE"));
    setShortDescription(Messages.getString("FilesTreeView.7"));
  }

  /**
   * Called on files deletion (long task).
   */
  class FilesDeletionThread extends Thread {
    /**
     * Instantiates a new files deletion thread.
     */
    FilesDeletionThread() {
      super("Files Deletion Thread");
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      UtilGUI.waiting();
      for (File f : alFiles) {
        try {
          Directory d = f.getDirectory();
          UtilSystem.deleteFile(f.getFIO());
          FileManager.getInstance().removeFile(f);
          if (d.getFiles().size() == 0) {
            emptyDirs.add(f.getDirectory());
          }
        } catch (Exception ioe) {
          Log.error(131, ioe);
          rejFiles.add(f);
        }
      }
      UtilGUI.stopWaiting();
      InformationJPanel.getInstance().setMessage(Messages.getString("ActionDelete.0"),
          InformationJPanel.MessageType.INFORMATIVE);
      if (rejFiles.size() > 0) {
        String rejString = "";
        for (File f : rejFiles) {
          rejString += f.getName() + "\n";
        }
        // Operation failed... display list of files in error
        Messages.showWarningMessage(Messages.getErrorMessage(136) + "\n" + rejString);
      }
      // requires device refresh
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    }
  }

  /**
   * Called on directories deletion (long task).
   */
  class DirectoriesDeletionThread extends Thread {
    /**
     * Instantiates a new directories deletion thread.
     */
    DirectoriesDeletionThread() {
      super("Directories Deletion Thread");
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
      UtilGUI.waiting();
      for (Directory d : alDirs) {
        try {
          for (File f : d.getFiles()) {
            if (QueueModel.getPlayingFile() != null && f.equals(QueueModel.getPlayingFile())) {
              throw new Exception("File currently in use");
            }
          }
          UtilSystem.deleteDir(new java.io.File(d.getAbsolutePath()));
          DirectoryManager.getInstance().removeDirectory(d.getID());
        } catch (Exception ioe) {
          Log.error(131, ioe);
          rejDirs.add(d);
        }
      }
      UtilGUI.stopWaiting();
      InformationJPanel.getInstance().setMessage(Messages.getString("ActionDelete.1"),
          InformationJPanel.MessageType.INFORMATIVE);
      if (rejDirs.size() > 0) {
        String rejString = "";
        for (Directory d : rejDirs) {
          rejString += d.getName() + "\n";
        }
        // Operation failed... display list of files in error
        Messages.showWarningMessage(Messages.getErrorMessage(136) + "\n\n" + rejString);
      }
      // requires device refresh
      ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.actions.SelectionAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent e) throws Exception {
    // Make sure to consider selection as a raw playlist, not its content
    expandPlaylists = false;
    super.perform(e);
    // Get required data from the tree (selected node and node type)
    alFiles = new ArrayList<File>(selection.size());
    rejFiles = new ArrayList<File>(selection.size());
    alDirs = new ArrayList<Directory>(selection.size());
    rejDirs = new ArrayList<Directory>(selection.size());
    emptyDirs = new ArrayList<Directory>(selection.size());
    // Compute all files to move from various items list
    if (selection.size() == 0) {
      Log.debug("None item to move");
      return;
    }
    Item first = selection.get(0);
    if (first instanceof Album || first instanceof Artist || first instanceof Genre) {
      List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(selection, true);
      for (Track track : tracks) {
        alFiles.addAll(track.getFiles());
      }
    } else {
      for (Item item : selection) {
        if (item instanceof WebRadio) {
          WebRadioManager.getInstance().removeItem(item);
          ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
        } else if (item instanceof File) {
          alFiles.add((File) item);
        } else if (item instanceof Track) {
          alFiles.addAll(((Track) item).getFiles());
        } else if (item instanceof Directory) {
          alDirs.add((Directory) item);
        } else if ((item instanceof Playlist)
            && (Conf.getBoolean(Const.CONF_CONFIRMATIONS_DELETE_FILE))) {
          // file delete confirmation
          Playlist plf = (Playlist) item;
          String sFileToDelete = plf.getAbsolutePath();
          String sMessage = Messages.getString("Confirmation_delete") + "\n" + sFileToDelete;
          int i = Messages.getChoice(sMessage, JOptionPane.YES_NO_CANCEL_OPTION,
              JOptionPane.WARNING_MESSAGE);
          if (i == JOptionPane.YES_OPTION) {
            try {
              PlaylistManager.getInstance().deletePlaylistFile(plf);
              // requires device refresh
              ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
            } catch (Exception e2) {
              Log.error(e2);
              Messages.showErrorMessage(131);
            }
          }
        }
      }
    }
    //  Consider files (selection can contain both files and directories)
    if (alFiles.size() > 0) {
      handleFiles();
    }
    // Now consider directories (selection can contain both files and directories)
    if (alDirs.size() > 0) {
      handleDirectories();
    }
  }

  /**
   * Handle directories.
   */
  private void handleDirectories() {
    // Ask if a confirmation is required
    if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_DELETE_FILE)) {
      String sFiles = "";
      int count = 0;
      for (Directory d : alDirs) {
        sFiles += d.getAbsolutePath() + "\n";
        count += d.getFilesRecursively().size();
        for (File f : d.getFilesRecursively()) {
          sFiles += "  + " + f.getName() + "\n";
        }
      }
      int iResu = Messages.getChoice(Messages.getString("Confirmation_delete_dirs") + " : \n"
          + sFiles + "\n" + count + " " + Messages.getString("Confirmation_file_number"),
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        return;
      }
      // Check if selection doesn't contain playing file's directory
      for (Directory dir : alDirs) {
        if (QueueModel.getPlayingFile() != null && QueueModel.getPlayingFile().hasAncestor(dir)) {
          Messages
              .showWarningMessage(Messages.getErrorMessage(172) + " : " + dir.getAbsolutePath());
          return;
        }
      }
    }
    //Actual deletion
    new DirectoriesDeletionThread().start();
  }

  /**
   * Handle files.
   */
  private void handleFiles() {
    // Ask if a confirmation is required
    if (Conf.getBoolean(Const.CONF_CONFIRMATIONS_DELETE_FILE)) {
      String sFiles = "";
      for (File f : alFiles) {
        sFiles += f.getName() + "\n";
      }
      int iResu = Messages.getChoice(Messages.getString("Confirmation_delete_files") + " : \n\n"
          + sFiles + "\n" + alFiles.size() + " " + Messages.getString("Confirmation_file_number"),
          JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        return;
      }
    }
    // Check if selection doesn't contain playing file
    for (File f : alFiles) {
      if (QueueModel.getPlayingFile() != null && f.equals(QueueModel.getPlayingFile())) {
        Messages.showWarningMessage(Messages.getErrorMessage(172) + " : " + f.getAbsolutePath());
        return;
      }
    }
    //Actual deletion
    new FilesDeletionThread().start();
    // Cleanup empty directories
    if (emptyDirs.size() > 0) {
      String emptyDirsString = "";
      for (Directory d : emptyDirs) {
        emptyDirsString += d.getName() + "\n";
      }
      int iResu = Messages.getChoice(Messages.getString("Confirmation_delete_empty_dirs")
          + " : \n\n" + emptyDirsString, JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.INFORMATION_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        return;
      } else {
        for (Directory d : emptyDirs) {
          try {
            UtilSystem.deleteDir(new java.io.File(d.getAbsolutePath()));
            DirectoryManager.getInstance().removeDirectory(d.getID());
          } catch (Exception ioe) {
            Log.error(131, ioe);
            rejDirs.add(d);
          }
        }
        if (rejDirs.size() > 0) {
          String rejString = "";
          for (Directory d : rejDirs) {
            rejString += d.getName() + "\n";
          }
          // Operation failed... display list of files in error
          Messages.showWarningMessage(Messages.getErrorMessage(136) + "\n" + rejString);
        }
        // requires device refresh
        ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
      }
    }
  }
}
