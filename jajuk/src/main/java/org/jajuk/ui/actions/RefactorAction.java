/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision:3308 $$
 */

package org.jajuk.ui.actions;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.jajuk.base.Directory;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.filters.NotAudioFilter;
import org.jajuk.util.log.Log;

public class RefactorAction implements ITechnicalStrings {

  private static boolean bStopAll = false;

  private static String sFS = java.io.File.separator;

  private List<File> alFiles = null;

  /** [PERF] Stores directory to be refreshed to avoid rescanning them twice */
  private final List<Directory> toBeRefreshed = new ArrayList<Directory>(1);

  /**
   * 
   * @param pFiles
   *          files to be reorganized (can be from different directories)
   */
  public RefactorAction(final List<File> pFiles) {
    // check the directory user selected contains some files
    if (pFiles.size() == 0) {
      Messages.showErrorMessage(18);
      return;
    }
    alFiles = pFiles;
    String sFiles = "";
    for (final File f : alFiles) {
      sFiles += f.getName() + "\n";
    }
    if (ConfigurationManager.getBoolean(ITechnicalStrings.CONF_CONFIRMATIONS_REFACTOR_FILES)) {
      final int iResu = Messages.getChoice(Messages.getString("Confirmation_refactor_files")
          + " : \n" + sFiles, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
      if (iResu != JOptionPane.YES_OPTION) {
        // Cancel
        if (iResu == JOptionPane.CANCEL_OPTION) {
          RefactorAction.bStopAll = true;
        }
        UtilGUI.stopWaiting();
        return;
      }
    }
    new Thread() {
      @Override
      public void run() {
        UtilGUI.waiting();
        refactor();
        ObservationManager.notify(new Event(JajukEvents.EVENT_DEVICE_REFRESH));
      }
    }.start();
    UtilGUI.stopWaiting();
  }

  /**
   * Refactoring itself
   */
  public void refactor() {
    boolean bOKToOverwrite = false;
    String sErrors = "";
    String filename;
    for (final File fCurrent : alFiles) {
      final Track tCurrent = fCurrent.getTrack();
      try {
        filename = UtilString.applyPattern(fCurrent, ConfigurationManager
            .getProperty(ITechnicalStrings.CONF_REFACTOR_PATTERN), true, true);
      } catch (final JajukException je) {
        sErrors += je.getMessage() + '\n';
        continue;
      }

      filename += "." + tCurrent.getType().getExtension();
      filename = filename.replace("/", RefactorAction.sFS);
      final java.io.File fOld = fCurrent.getIO();
      final String sPathname = fCurrent.getDevice().getFio().getPath() + RefactorAction.sFS
          + filename;
      final java.io.File fNew = new java.io.File(sPathname);

      // Confirm if destination dir already exist
      if (fNew.getParentFile().exists() && !bOKToOverwrite) {
        final int resu = Messages.getChoice(Messages.getString("Warning.5"),
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (resu == JOptionPane.NO_OPTION) {
          continue;
        } else if (resu == JOptionPane.CANCEL_OPTION) {
          return;
        }
      }
      // This flag is used to avoid displaying the confirmation at each
      // file
      bOKToOverwrite = true;
      fNew.getParentFile().mkdirs();

      // Move file and related cover but save old Directory pathname
      // for future deletion
      try {
        final java.io.File fCover = tCurrent.getAlbum().getCoverFile();
        if (fCover != null) {
          fCover
              .renameTo(new java.io.File(fNew.getParent() + RefactorAction.sFS + fCover.getName()));
        }
      } catch (Exception e) {
        // This exception can be thrown by instance if default cover is not readable
        Log.error(e);
      }
      // Rename audio files
      boolean bRenameSuccess = false;

      // Test if source and target files are equals
      if (fNew.getAbsolutePath().equalsIgnoreCase(fOld.getAbsolutePath())) {
        sErrors += fCurrent.getAbsolutePath() + " (" + Messages.getString("Error.160") + ")\n";
      } else {
        try {
          bRenameSuccess = fOld.renameTo(fNew);
          if (!bRenameSuccess) {
            sErrors += fCurrent.getAbsolutePath() + " (" + Messages.getString("Error.154") + ")\n";
          }
          Log.debug("[Refactoring] {{" + fNew.getAbsolutePath() + "}} Success ? " + bRenameSuccess);

        } catch (Exception e) {
          Log.error(e);
          sErrors += fCurrent.getAbsolutePath() + " (" + Messages.getString("Error.161") + ")\n";
        }
      }

      // Register and scans new directories
      String sFirstDir = null;
      final String sTest[] = sPathname.split(fCurrent.getDevice().getFio().getPath().replace("\\",
          "\\\\"));
      sFirstDir = sTest[1].split("\\" + RefactorAction.sFS)[1];

      final Directory dir = DirectoryManager.getInstance().registerDirectory(sFirstDir,
          DirectoryManager.getInstance().getDirectoryForIO(fCurrent.getDevice().getFio()),
          fCurrent.getDevice());

      // Ask to refresh this directory afterward
      if (!toBeRefreshed.contains(dir)) {
        toBeRefreshed.add(dir);
      }

      // See if old directory contain other files and move them
      final java.io.File dOld = fOld.getParentFile();
      final java.io.File[] list = dOld.listFiles(new JajukFileFilter(NotAudioFilter.getInstance()));
      if (list == null) {
        DirectoryManager.getInstance().removeDirectory(fOld.getParent());
      } else if (list.length != 0) {
        for (final java.io.File f : list) {
          f.renameTo(new java.io.File(fNew.getParent() + RefactorAction.sFS + f.getName()));
        }
      } else if (list.length == 0) {
        if (dOld.delete()) {
          DirectoryManager.getInstance().removeDirectory(fOld.getParent());

        }
      }

      InformationJPanel.getInstance().setMessage(
          Messages.getString("RefactorWizard.0") + sPathname, 0);
    }
    // Refresh and cleanup required directories
    for (final Directory dir : toBeRefreshed) {
      DirectoryManager.refreshDirectory(dir);
      dir.getDevice().cleanRemovedFiles();
    }
    if (!sErrors.equals("")) {
      Messages.showDetailedErrorMessage(147, "", sErrors);
    } else {
      InformationJPanel.getInstance().setMessage(Messages.getString("Success"),
          InformationJPanel.INFORMATIVE);
    }

  }

  public static boolean isStopAll() {
    return bStopAll;
  }
  
  public static void resetStopAll() {
    bStopAll = false;
  }
}
