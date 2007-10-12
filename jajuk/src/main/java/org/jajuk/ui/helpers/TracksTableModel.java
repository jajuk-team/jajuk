/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 * $Revision$
 */

package org.jajuk.ui.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

import org.jajuk.base.File;
import org.jajuk.base.Item;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.base.TrackComparator;
import org.jajuk.base.TrackManager;
import org.jajuk.base.Type;
import org.jajuk.ui.widgets.IconLabel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;

/**
 * Table model used for logical table view
 */
public class TracksTableModel extends JajukTableModel {

	private static final long serialVersionUID = 1L;

	/**
	 * Model constructor
	 * 
	 * @param iColNum
	 *            number of rows
	 * @param sColName
	 *            columns names
	 */
	public TracksTableModel() {
		super(12);
		setEditable(ConfigurationManager.getBoolean(CONF_LOGICAL_TABLE_EDITION));
		// Columns names
		// First column is play icon, need to set a space character
		// for proper display in some look and feel
		vColNames.add(" ");
		vId.add(XML_PLAY);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_NAME));
		vId.add(XML_NAME);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_ALBUM));
		vId.add(XML_ALBUM);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_AUTHOR));
		vId.add(XML_AUTHOR);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_STYLE));
		vId.add(XML_STYLE);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_RATE));
		vId.add(XML_TRACK_RATE);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_LENGTH));
		vId.add(XML_TRACK_LENGTH);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_COMMENT));
		vId.add(XML_TRACK_COMMENT);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ADDED));
		vId.add(XML_TRACK_ADDED);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_ORDER));
		vId.add(XML_TRACK_ORDER);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_YEAR));
		vId.add(XML_YEAR);

		vColNames.add(Messages.getString(PROPERTY_SEPARATOR + XML_TRACK_HITS));
		vId.add(XML_TRACK_HITS);

		// custom properties now
		for (PropertyMetaInformation meta:TrackManager.getInstance().getCustomProperties()){
			vColNames.add(meta.getName());
			vId.add(meta.getName());
		}
	}

	/**
	 * Fill model with data using an optional filter property and pattern
	 */
	@SuppressWarnings("unchecked")
	public synchronized void populateModel(String sPropertyName, String sPattern) {
		// Filter mounted files if needed and apply sync table with tree option
		// if needed
		boolean bShowWithTree = true;
		ArrayList<Track> alToShow = null;
		// look at selection
		boolean bSyncWithTreeOption = ConfigurationManager.getBoolean(CONF_OPTIONS_SYNC_TABLE_TREE);
		Set<Track> alTracks = TrackManager.getInstance().getTracks();
		alToShow = new ArrayList<Track>(alTracks.size());
		for (Track track : alTracks) {
			bShowWithTree = !bSyncWithTreeOption // no tree/table sync option
					// tree selection = null means none selection have been
					// done so far
					|| treeSelection == null
					// check if the tree selection contains the current file
					|| (treeSelection.size() > 0 && treeSelection.contains(track));
			// show it if no sync option or if item is in the selection
			if (!track.shouldBeHidden() && bShowWithTree) {
				alToShow.add(track);
			}
		}
		// Filter values using given pattern
		if (sPropertyName != null && sPattern != null) {
			// null means no filtering
			Iterator it = alToShow.iterator();
			// Prepare filter pattern
			String sNewPattern = sPattern;
			if (!ConfigurationManager.getBoolean(CONF_REGEXP)) {
				// do we use regular expression or not? if not, we allow
				// user to use '*'
				sNewPattern = sNewPattern.replaceAll("\\*", ".*");
				sNewPattern = ".*" + sNewPattern + ".*";
			} else if ("".equals(sNewPattern)) {
				// in regexp mode, if none
				// selection, display all rows
				sNewPattern = ".*";
			}
			while (it.hasNext()) {
				Track track = (Track) it.next();
				if (sPropertyName != null && sNewPattern != null) {
					// if name or value is null, means there is no filter
					String sValue = track.getHumanValue(sPropertyName);
					if (sValue == null) {
						// try to filter on a unknown property, don't take
						// this file
						continue;
					} else {
						boolean bMatch = false;
						try { // test using regular expressions
							bMatch = sValue.toLowerCase().matches(sNewPattern.toLowerCase());
							// test if the file property contains this
							// property value (ignore case)
						} catch (PatternSyntaxException pse) {
							// wrong pattern syntax
							bMatch = false;
						}
						if (!bMatch) {
							it.remove(); // no? remove it
						}
					}
				}
			}
		}
		// sort by album
		Collections.sort(alToShow, new TrackComparator(2));
		Iterator it = alToShow.iterator();
		int iColNum = iNumberStandardCols + TrackManager.getInstance().getCustomProperties().size();
		iRowNum = alToShow.size();
		it = alToShow.iterator();
		oValues = new Object[iRowNum][iColNum];
		oItems = new Item[iRowNum];
		bCellEditable = new boolean[iRowNum][iColNum];
		for (int iRow = 0; it.hasNext(); iRow++) {
			Track track = (Track) it.next();
			setItemAt(iRow, track);
			LinkedHashMap properties = track.getProperties();
			// Id
			oItems[iRow] = track;
			// Play
			IconLabel il = null;
			if (track.getPlayeableFile(true) != null) {
				il = new IconLabel(PLAY_ICON, "", null, null, null, Messages
						.getString("TracksTreeView.1"));
			} else {
				il = new IconLabel(UNMOUNT_PLAY_ICON, "", null, null, null, Messages
						.getString("TracksTreeView.1")
						+ Messages.getString("AbstractTableView.10"));
			}
			// Note: if you want to add an image, use an ImageIcon class and
			// change
			oValues[iRow][0] = il;
			bCellEditable[iRow][0] = false;
			// check track has an associated tag editor (not null)
			boolean bHasATagEditor = false;
			File file = track.getFiles().get(0);
			// all files have the same type
			Type type = file.getType();
			if (type != null) {
				bHasATagEditor = (type.getTaggerClass() != null);
			}
			// Track name
			oValues[iRow][1] = track.getName();
			bCellEditable[iRow][1] = bHasATagEditor;
			// Album
			oValues[iRow][2] = track.getAlbum().getName2();
			bCellEditable[iRow][2] = bHasATagEditor;
			// Author
			oValues[iRow][3] = track.getAuthor().getName2();
			bCellEditable[iRow][3] = bHasATagEditor;
			// Style
			oValues[iRow][4] = track.getStyle().getName2();
			bCellEditable[iRow][4] = bHasATagEditor;
			// Rate
			IconLabel ilRate = Util.getStars(track.getRate());
			oValues[iRow][5] = ilRate;
			bCellEditable[iRow][5] = false;
			ilRate.setInteger(true);
			// Length
			oValues[iRow][6] = Util.formatTimeBySec(track.getDuration(), false);
			bCellEditable[iRow][6] = false;
			// Comment
			oValues[iRow][7] = track.getValue(XML_TRACK_COMMENT);
			bCellEditable[iRow][7] = bHasATagEditor;
			// Date discovery
			oValues[iRow][8] = track.getAdditionDate(); // show date using
			// default local format
			// and not technical
			// representation
			bCellEditable[iRow][8] = false;
			// Order
			oValues[iRow][9] = track.getOrder();
			bCellEditable[iRow][9] = bHasATagEditor;
			// Year
			oValues[iRow][10] = track.getYear().getValue();
			bCellEditable[iRow][10] = bHasATagEditor;
			// Hits
			oValues[iRow][11] = track.getHits();
			bCellEditable[iRow][11] = false;
			// Custom properties now
			Iterator it2 = TrackManager.getInstance().getCustomProperties().iterator();
			for (int i = 0; it2.hasNext(); i++) {
				PropertyMetaInformation meta = (PropertyMetaInformation) it2.next();
				Object o = properties.get(meta.getName());
				if (o != null) {
					oValues[iRow][iNumberStandardCols + i] = o;
				} else {
					oValues[iRow][iNumberStandardCols + i] = meta.getDefaultValue();
				}
				// Date values not editable, use properties panel instead to
				// edit
				bCellEditable[iRow][iNumberStandardCols + i] = !(meta.getType().equals(Date.class));
			}
		}
	}
}