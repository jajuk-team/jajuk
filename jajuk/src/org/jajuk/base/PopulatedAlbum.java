/*
 *  Jajuk
 *  Copyright (C) 2006 Ronak Patel
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

import java.util.ArrayList;

import org.jajuk.base.Album;
import org.jajuk.base.Track;

/**
 *  This class contains an Album and an ArrayList of the Tracks
 *  associated with the Album. Used in the exporting process.
 *
 * @author     Ronak Patel
 * @created    Aug 26, 2006
 */
public class PopulatedAlbum {
	private Album album = null;
	private ArrayList<Track> tracks = null;
	
	public PopulatedAlbum(Album album) {
		this.album = album;
		this.tracks = new ArrayList<Track>();
	}
	
	public PopulatedAlbum(Album album, ArrayList<Track> tracks) {
		this.album = album;
		this.tracks = tracks;
	}
	
	public Album getAlbum() {
		return this.album;
	}
	
	public void setAlbum(Album album) {
		this.album = album;
	}
	
	public ArrayList<Track> getTracks() {
		return this.tracks;
	}
	
	public void setTracks(ArrayList<Track> tracks) {
		this.tracks = tracks;
	}
	
	public void addTrack(Track track) {
		if (tracks == null) {
			tracks = new ArrayList<Track>();
		}
		tracks.add(track);
	}
}
