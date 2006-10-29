/*
 *  Jajuk
 *  Copyright (C) 2004 bflorat
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

package org.jajuk.base;

import java.util.Comparator;

/**
 * 
 * Mutli-method track comparator
 * 
 * @author Bertrand Florat
 * @created 8 janv. 2006
 */
public class TrackComparator implements Comparator<Track> {
    /**
         * Sorting method
         */
    private int iSortingMethod = 0;

    /** sorting methods constants */
    public static final int STYLE_AUTHOR_ALBUM = 0;

    public static final int AUTHOR_ALBUM = 1;

    public static final int ALBUM = 2;

    /**
         * Constructor
         * 
         * @param iSortingMethod
         *                Sorting method
         */
    public TrackComparator(int iSortingMethod) {
	this.iSortingMethod = iSortingMethod;
    }

    /**
         * 
         * @param track
         * @return Hashcode string used to compare two tracks in accordance with
         *         the sorting method
         */
    private String getCompareString(Track track) {
	String sHashCompare = null;
	// comparaison based on style, author, album, name and year to
        // differenciate 2 tracks with all the same attributes
	// note we need to use year because in sorted set, we must differenciate
        // 2 tracks with different years
	switch (iSortingMethod) {
	// Style/author/album
	case STYLE_AUTHOR_ALBUM:
	    sHashCompare = new StringBuffer().append(
		    track.getStyle().getName2()).append(
		    track.getAuthor().getName2())// need 2 spaces to make
                                                        // a right sorting (ex:
                                                        // Rock and Rock & Roll)
                                                        // //$NON-NLS-1$
		    .append(track.getAlbum().getName2()) //$NON-NLS-1$
		    .append(track.getName()).toString(); //$NON-NLS-1$
	    break;
	// Author/album
	case AUTHOR_ALBUM:
	    sHashCompare = new StringBuffer().append(
		    track.getAuthor().getName2())// need 2 spaces to make
                                                        // a right sorting (ex:
                                                        // Rock and Rock & Roll)
                                                        // //$NON-NLS-1$
		    .append(track.getAlbum().getName2()) //$NON-NLS-1$
		    .append(track.getName()).toString(); //$NON-NLS-1$
	    break;
	// Album
	case ALBUM:
	    sHashCompare = new StringBuffer().append(
		    track.getAlbum().getName2()) //$NON-NLS-1$
		    .append(track.getName()).toString(); //$NON-NLS-1$
	    break;
	}
	return sHashCompare;
    }

    /**
         * Tracks compare
         * 
         * @param arg0
         * @param arg1
         * @return
         */
    public int compare(Track track1, Track track2) {
	if (track1.equals(track2)) {
	    return 0;
	}
	// if track # is given, sort by # in a same album, otherwise, sort
        // alphabeticaly
	if (track2.getAlbum().equals(track1.getAlbum())
		&& track2.getAuthor().equals(track1.getAuthor())
		&& track2.getStyle().equals(track1.getStyle())
		&& (track1.getOrder() != track2.getOrder())) {
	    // do not use year as an album can contains tracks with
                // different year but we want to keep order
	    return (int) (track1.getOrder() - track2.getOrder());
	}
	String sHashCompare = getCompareString(track1);
	String sHashCompareOther = getCompareString(track2);
	// Do not change this code ! we need to ignore case except if both
        // tracks have the same name
	if (sHashCompare.equalsIgnoreCase(sHashCompareOther)
		&& !sHashCompare.equals(sHashCompareOther)) {
	    return sHashCompare.compareTo(sHashCompareOther);
	} else {
	    return sHashCompare.compareToIgnoreCase(sHashCompareOther);
	}
    }
}
