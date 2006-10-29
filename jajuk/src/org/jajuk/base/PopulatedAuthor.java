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

/**
 * This class contains an Author and an ArrayList of PopulatedAlbums associated
 * with this Author. This class is used in the exporting process.
 * 
 * @author Ronak Patel
 * @created Aug 26, 2006
 */
public class PopulatedAuthor {
    private Author author = null;

    private ArrayList<PopulatedAlbum> albums = null;

    public PopulatedAuthor(Author author) {
	this.author = author;
	this.albums = new ArrayList<PopulatedAlbum>();
    }

    public PopulatedAuthor(Author author, ArrayList<PopulatedAlbum> albums) {
	this.author = author;
	this.albums = albums;
    }

    public ArrayList<PopulatedAlbum> getAlbums() {
	return albums;
    }

    public void setAlbums(ArrayList<PopulatedAlbum> albums) {
	this.albums = albums;
    }

    public Author getAuthor() {
	return author;
    }

    public void setAuthor(Author author) {
	this.author = author;
    }

    public void addAlbum(PopulatedAlbum album) {
	if (albums == null) {
	    albums = new ArrayList<PopulatedAlbum>();
	}
	albums.add(album);
    }
}
