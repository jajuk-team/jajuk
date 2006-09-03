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
 *  This class contains a Style and an ArrayList of 
 *  PopulatedAuthors associated with this Style.
 *  This class is used in the exporting process.
 *
 * @author     Ronak Patel
 * @created    Aug 26, 2006
 */
public class PopulatedStyle {
	private Style style = null;
	private ArrayList<PopulatedAuthor> authors = null;
	
	public PopulatedStyle(Style style) {
		this.style = style;
		this.authors = new ArrayList<PopulatedAuthor>();
	}
	
	public PopulatedStyle(Style style, ArrayList<PopulatedAuthor> authors) {
		this.style = style;
		this.authors = authors;
	}

	public ArrayList<PopulatedAuthor> getAuthors() {
		return authors;
	}

	public void setAuthors(ArrayList<PopulatedAuthor> authors) {
		this.authors = authors;
	}

	public Style getStyle() {
		return style;
	}

	public void setStyle(Style style) {
		this.style = style;
	}
	
	public void addAuthor(PopulatedAuthor author) {
		if (this.authors == null) {
			authors = new ArrayList<PopulatedAuthor>();
		}
		authors.add(author);
	}
}
