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

package org.jajuk.base.exporters;

/**
 *  This class will create taggings. It will create either
 *  open tags, closed tags, or full tagging with data.
 *
 * @author     Ronak Patel
 * @created    Aug 20, 2006
 */
public class Tag {
	public static String openTag(String tagname) {
		return "<"+tagname+">";
	}
	
	public static String closeTag(String tagname) {
		return "</"+tagname+">";
	}
	
	public static String tagData(String tagname, String data) {
		return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
	}
	
	public static String tagData(String tagname, long data) {
		return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
	}
	
	public static String tagData(String tagname, int data) {
		return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
	}
	
	public static String tagData(String tagname, double data) {
		return Tag.openTag(tagname) + data + Tag.closeTag(tagname);
	}
}
