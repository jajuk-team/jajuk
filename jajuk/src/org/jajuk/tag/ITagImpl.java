/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
package org.jajuk.tag;


/**
 * Mandatory methods required for all tag implementations
 * @author Bertrand Florat 
 * @created 25 oct. 2003
 */
public interface ITagImpl {

	/**
	 * @return track name as defined in tags are file name otherwise
	 */
	public String getTrackName() throws Exception;

	/**
	 * @return album name
	 */
	public String getAlbumName() throws Exception;

	/**
	 * @return author name
	 */
	public String getAuthorName() throws Exception;

	/**
	 * @return style name
	 */
	public String getStyleName() throws Exception;

	/**
	 * @return length in sec
	 */
	public long getLength() throws Exception;

	/**
	 * @return creation year
	 */
	public String getYear() throws Exception;

	/**
	 * @return quality
	 */
	public String getQuality() throws Exception;

	/**
	 * @param sTrackName
	 */
	public void setTrackName(String sTrackName) throws Exception;

	/**
	 * @param sAlbumName
	 */
	public void setAlbumName(String sAlbumName) throws Exception;

	/**
	 * @param sAuthorName
	 */
	public void setAuthorName(String sAuthorName) throws Exception;

	/**
	 * @param style
	 */
	public void setStyleName(String style) throws Exception;

	/**
	 * @param length
	 */
	public void setLength(long length) throws Exception;

	/**
	 * @param sYear
	 */
	public void setYear(String sYear) throws Exception;

	/**
	 * @param sQuality
	 */
	public void setQuality(String sQuality) throws Exception;

	/**
	 * Set current file to work with.
	 * 
	 * @param fio
	 */
	public void setFile(java.io.File fio) throws Exception;
	
	
}
