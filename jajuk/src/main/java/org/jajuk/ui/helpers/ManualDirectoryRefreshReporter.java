/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
package org.jajuk.ui.helpers;

import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.util.Messages;
import org.jajuk.util.log.Log;

/**
 * Refresh reporter on directories with GUI special operations
 */
public class ManualDirectoryRefreshReporter extends RefreshReporter {
  public ManualDirectoryRefreshReporter(Device device) {
    super(device);
  }

  @Override
  public void cleanupDone() {
    Log.debug("Cleanup done");
  }

  @Override
  public void updateState(Directory dir) {
    // Intentionnal NOP
  }

  @Override
  public void done() {
    long refreshTime = System.currentTimeMillis() - lRefreshDateStart;
    String message = buildFinalMessage(refreshTime);
    Log.debug(message);
    reset();
    // Display end of refresh message with stats
    Messages.showInfoMessage(message);
  }
}
