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
 *  $$Revision$$
 */

package org.jajuk.ui.action;

import org.jajuk.base.File;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class FindDuplicateFilesAction extends ActionBase {

	private static final long serialVersionUID = 1L;

	protected FindDuplicateFilesAction(String name, boolean enabled) {
		super(name, enabled);
	}

	@Override
	protected void perform(ActionEvent evt) throws Exception {
		List<File> duplicateFilesList = new ArrayList<File>();
		for (Track track : TrackManager.getInstance().getTracks()) {
			List<File> trackFileList = track.getFiles();
			if (trackFileList.size() > 1) {
				for (int i = 1; i < trackFileList.size(); i++) {
					duplicateFilesList.add(trackFileList.get(i));
				}
			}
		}
		if (duplicateFilesList.size() < 1) {
			Messages.showInfoMessage("FindDuplicateFilesAction.0");
		} else {
			Messages.showDetailedErrorMessage(168, "",
					convertToString(duplicateFilesList));
		}
	}

	private String convertToString(List<File> duplicateFilesList) {
		StringBuffer buffer = new StringBuffer();
		for (File file : duplicateFilesList) {
			buffer.append('\t');
			buffer.append(file.getName());
			buffer.append('\n');
		}
		return buffer.toString();
	}
}
