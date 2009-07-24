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
 *  $Revision: 3132 $
 */
package org.jajuk;

import java.io.File;
import java.io.IOException;

import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;

/**
 * Small helper class with functionality that is used in multiple unit tests
 */
public class JUnitHelpers {

  /**
   * Set a temporary session directory and make sure it exists and is writeable.
   * 
   * @throws IOException
   *           If the temporary directory can not be created or is not writeable
   */
  public static void createSessionDirectory() throws IOException {
    // get a temporary file name
    File tempdir = File.createTempFile("test", "");
    if(!tempdir.delete()) {
      throw new IOException("Could not create the temporary session directory at "
          + tempdir.getAbsolutePath() + ", could not remove the temporary file.");
    }

    // set the directory as base directory for the workspace
    SessionService.setWorkspace(tempdir.getAbsolutePath());

    // read the session directory that we are using now for caching
    File sessiondir = SessionService.getConfFileByPath(Const.FILE_CACHE);

    // create the directory structure
    sessiondir.mkdirs();

    // do some checks
    if (!sessiondir.exists()) {
      throw new IOException("Could not create the temporary session directory at "
          + sessiondir.getAbsolutePath());
    }
    if (!sessiondir.isDirectory()) {
      throw new IOException("Could not create the temporary session directory at "
          + sessiondir.getAbsolutePath() + ", not a directory!");
    }
    if (!sessiondir.canWrite()) {
      throw new IOException("Could not create the temporary session directory at "
          + sessiondir.getAbsolutePath() + ", not writeable!");
    }
    
    // make sure the directory is removed at the end of the tests again
    sessiondir.getParentFile().deleteOnExit();
  }
}
