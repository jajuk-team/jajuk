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
package org.jajuk.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.Item;
import org.jajuk.base.Playlist;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.helpers.ItemMoveManager;
import org.jajuk.ui.widgets.InformationJPanel;
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
public class PasteAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new paste action.
   */
  PasteAction() {
    super(Messages.getString("ActionMove.0"), IconLoader.getIcon(JajukIcons.PASTE), "ctrl V", true);
    setShortDescription(Messages.getString("ActionMove.0"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void perform(ActionEvent e) {
    //TODO : rework this method into smaller units
    JComponent source = (JComponent) e.getSource();
    final List<Item> alSelected = (List<Item>) source.getClientProperty(Const.DETAIL_SELECTION);
    final List<Item> itemsToMove = ItemMoveManager.getInstance().getAll();
    final ItemMoveManager.MoveActions moveAction = ItemMoveManager.getInstance().getAction();
    final List<File> alFiles = new ArrayList<>(alSelected.size());
    final List<Playlist> alPlaylists = new ArrayList<>(alSelected.size());
    final List<Directory> alDirs = new ArrayList<>(alSelected.size());
    new Thread("Paste Thread") {
      @SuppressWarnings("cast")
      @Override
      public void run() {
        UtilGUI.waiting();
        // Compute all files to move from various items list
        if (itemsToMove.size() == 0) {
          Log.debug("None item to move");
          return;
        }
        Item first = itemsToMove.get(0);
        if (first instanceof Album || first instanceof Artist || first instanceof Genre) {
          List<Track> tracks = TrackManager.getInstance().getAssociatedTracks(itemsToMove, true);
          for (Track track : tracks) {
            alFiles.addAll(track.getFiles());
          }
        } else {
          for (Item item : itemsToMove) {
            if (item instanceof File) {
              alFiles.add((File) item);
            } else if (item instanceof Track) {
              alFiles.addAll(((Track) item).getFiles());
            } else if (item instanceof Directory) {
              alDirs.add((Directory) item);
            } else if (item instanceof Playlist) {
              alPlaylists.add((Playlist) item);
            }
          }
        }
        // Compute destination directory
        // alSelected can contain either a single Directory or a single Device
        Item item = alSelected.get(0);
        java.io.File dir;
        Directory destDir;
        if (item instanceof Directory) {
          dir = new java.io.File(((Directory) item).getAbsolutePath());
          destDir = (Directory) item;
        } else if (item instanceof Device) {
          dir = new java.io.File(((Device) item).getRootDirectory().getAbsolutePath());
          destDir = ((Device) item).getRootDirectory();
        } else {
          dir = ((File) item).getDirectory().getFio();
          destDir = ((File) item).getDirectory();
        }
        // Compute source directories
        // We need to find the highest directory in order to refresh it along
        // with the destination file to avoid phantom references
        List<Directory> srcDirs = new ArrayList<>(1);
        for (File file : alFiles) {
          boolean parentAlreadyPresent = false;
          // We have to iterate using items index because the collection can
          // grow
          for (Directory directory : srcDirs) {
            if (file.getDirectory().isChildOf(directory)) {
              parentAlreadyPresent = true;
              break;
            }
          }
          if (!parentAlreadyPresent && !srcDirs.contains(file.getDirectory())) {
            srcDirs.add(file.getDirectory());
          }
        }
        for (Playlist pl : alPlaylists) {
          boolean parentAlreadyPresent = false;
          // We have to iterate using items index because the collection can
          // grow
          for (Directory directory : srcDirs) {
            if (pl.getDirectory().isChildOf(directory)) {
              parentAlreadyPresent = true;
              break;
            }
          }
          if (!parentAlreadyPresent && !srcDirs.contains(pl.getDirectory())) {
            srcDirs.add(pl.getDirectory());
          }
        }
        boolean overwriteAll = false;
        boolean bErrorOccured = false;
        if (moveAction == ItemMoveManager.MoveActions.CUT) {
          for (File f : alFiles) {
            if (!overwriteAll) {
              java.io.File newFile = new java.io.File(dir.getAbsolutePath() + "/" + f.getName());
              if (newFile.exists()) {
                int iResu = Messages.getChoice(Messages.getString("Confirmation_file_overwrite")
                    + " : \n\n" + f.getName(), Messages.YES_NO_ALL_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                if (iResu == JOptionPane.NO_OPTION || iResu == JOptionPane.CANCEL_OPTION) {
                  UtilGUI.stopWaiting();
                  return;
                }
                if (iResu == Messages.ALL_OPTION) {
                  overwriteAll = true;
                }
              }
            }
            try {
              showMessage(f.getFIO());
              FileManager.getInstance().changeFileDirectory(f, destDir);
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
              bErrorOccured = true;
            }
          }
          for (Playlist pl : alPlaylists) {
            if (!overwriteAll) {
              java.io.File newFile = new java.io.File(dir.getAbsolutePath() + "/" + pl.getName());
              if (newFile.exists()) {
                int iResu = Messages.getChoice(Messages.getString("Confirmation_file_overwrite")
                    + " : \n\n" + pl.getName(), Messages.YES_NO_ALL_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                if (iResu == JOptionPane.NO_OPTION || iResu == JOptionPane.CANCEL_OPTION) {
                  UtilGUI.stopWaiting();
                  return;
                }
                if (iResu == Messages.ALL_OPTION) {
                  overwriteAll = true;
                }
              }
            }
            try {
              showMessage(pl.getFIO());
              final java.io.File fileNew = new java.io.File(dir.getAbsolutePath() + "/" + pl.getName());
              if (!pl.getFIO().renameTo(fileNew)) {
                throw new Exception("Cannot move item: " + pl.getFIO().getAbsolutePath() + " to "
                    + fileNew.getAbsolutePath());
              }
              // Refresh source and destination
              destDir.refresh(false);
              // Refresh source directories as well
              for (Directory srcDir : srcDirs) {
                srcDir.refresh(false);
              }
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
              bErrorOccured = true;
            }
          }
          for (Directory d : alDirs) {
            try {
              java.io.File src = new java.io.File(d.getAbsolutePath());
              java.io.File dst = new java.io.File(dir.getAbsolutePath() + "/" + d.getName());
              showMessage(src);
              java.io.File newDir = new java.io.File(dst.getAbsolutePath());
              if (!src.renameTo(newDir)) {
                throw new Exception("Cannot move item: " + src.getAbsolutePath() + " to " + dst.getAbsolutePath());
              }
              DirectoryManager.getInstance().removeDirectory(d.getID());
              destDir.refresh(false);
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
              bErrorOccured = true;
            }
          }
          try {
            destDir.refresh(false);
            // Refresh source directories as well
            for (Directory srcDir : srcDirs) {
              srcDir.refresh(false);
            }
          } catch (Exception e1) {
            Log.error(e1);
            bErrorOccured = true;
          }
        } else if (moveAction == ItemMoveManager.MoveActions.COPY) {
          for (File f : alFiles) {
            if (!overwriteAll) {
              java.io.File newFile = new java.io.File(dir.getAbsolutePath() + "/" + f.getName());
              if (newFile.exists()) {
                int iResu = Messages.getChoice(Messages.getString("Confirmation_file_overwrite")
                    + " : \n\n" + f.getName(), Messages.YES_NO_ALL_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                if (iResu == JOptionPane.NO_OPTION || iResu == JOptionPane.CANCEL_OPTION) {
                  UtilGUI.stopWaiting();
                  return;
                }
                if (iResu == Messages.ALL_OPTION) {
                  overwriteAll = true;
                }
              }
            }
            try {
              showMessage(f.getFIO());
              UtilSystem.copyToDir(f.getFIO(), dir);
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
              bErrorOccured = true;
            }
          }
          for (Playlist pl : alPlaylists) {
            if (!overwriteAll) {
              java.io.File newFile = new java.io.File(dir.getAbsolutePath() + "/" + pl.getName());
              if (newFile.exists()) {
                int iResu = Messages.getChoice(Messages.getString("Confirmation_file_overwrite")
                    + " : \n\n" + pl.getName(), Messages.YES_NO_ALL_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
                if (iResu == JOptionPane.NO_OPTION || iResu == JOptionPane.CANCEL_OPTION) {
                  UtilGUI.stopWaiting();
                  return;
                }
                if (iResu == Messages.ALL_OPTION) {
                  overwriteAll = true;
                }
              }
            }
            try {
              showMessage(pl.getFIO());
              UtilSystem.copyToDir(pl.getFIO(), dir);
              // Refresh source and destination
              destDir.refresh(false);
              // Refresh source directories as well
              for (Directory srcDir : srcDirs) {
                srcDir.refresh(false);
              }
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
              bErrorOccured = true;
            }
          }
          for (Directory d : alDirs) {
            try {
              java.io.File src = new java.io.File(d.getAbsolutePath());
              java.io.File dst = new java.io.File(dir.getAbsolutePath() + "/" + d.getName());
              showMessage(src);
              UtilSystem.copyRecursively(src, dst);
            } catch (Exception ioe) {
              Log.error(131, ioe);
              Messages.showErrorMessage(131);
              bErrorOccured = true;
            }
          }
          try {
            destDir.refresh(false);
          } catch (Exception e1) {
            Log.error(e1);
            bErrorOccured = true;
          }
        }
        ObservationManager.notify(new JajukEvent(JajukEvents.DEVICE_REFRESH));
        UtilGUI.stopWaiting();
        if (!bErrorOccured) {
          InformationJPanel.getInstance().setMessage(Messages.getString("Success"),
              InformationJPanel.MessageType.INFORMATIVE);
        }
      }
    }.start();
  }

  /**
   * Display currently copied file to information panel.
   *
   * @param file The file to display in the information panel
   */
  private void showMessage(java.io.File file) {
    String message = Messages.getString("Device.45");
    message += file.getAbsolutePath() + "]";
    InformationJPanel.getInstance().setMessage(message, InformationJPanel.MessageType.INFORMATIVE);
  }
}
