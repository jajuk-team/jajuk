/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,
 * USA. $Log$
 * USA. Revision 1.2  2003/10/31 13:05:06  bflorat
 * USA. 31/10/2003
 * USA.
 * USA. Revision 1.1  2003/10/26 21:28:49  bflorat
 * USA. 26/10/2003
 * USA. Revision 1.3 2003/10/21 17:51:43 bflorat 21/10/2003
 * 
 * Revision 1.2 2003/10/17 20:36:45 bflorat 17/10/2003
 * 
 * Revision 1.1 2003/10/12 21:08:11 bflorat 12/10/2003
 *  
 */
package org.jajuk.base;

import java.io.File;
import java.lang.reflect.Array;

import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * abstract tag, independent from real implementation
 * 
 * @author bflorat @created 25 oct. 2003
 */
public class Tag {

	/** Current tag impl* */
	private ITagImpl tagImpl;
	/** Current file* */
	private File fio;

	/**
	 * Tag constructor
	 * 
	 * @param fio
	 */
	public Tag(java.io.File fio) {
		Type type = TypeManager.getTypeByExtension(Util.getExtension(fio));
		tagImpl = type.getTagImpl();
		try{
			tagImpl.setFile(fio);
		}
		catch(Exception e){
		//	Log.error("103",e);
		}
		this.fio = fio;
	}

	/**
	 * @return track name as defined in tags are file name otherwise
	 */
	public String getTrackName() {
		String sTrackName = fio.getName();
		try {
			sTrackName = Util.formatTag(tagImpl.getTrackName());
			if (sTrackName == null || sTrackName.equals("")){
				sTrackName = fio.getName();
			}
		} catch (Exception e) {
			Log.error("103", e);
		}
		return sTrackName;
	}

	/**
	 * @return album name
	 */
	public String getAlbumName() {
		String sAlbumlName = fio.getParentFile().getName(); //if album is not found, take current dirtectory as album name
		try {
			sAlbumlName = Util.formatTag(tagImpl.getAlbumName());
			if (sAlbumlName.equals("")){
					sAlbumlName = fio.getParentFile().getName();
			}
		} catch (Exception e) {
			Log.error("103", e);
		}
		return sAlbumlName;
	}

	/**
	 * @return author name
	 */
	public String getAuthorName() {
		String sAuthorName = Messages.getString("Track_unknown_author");
		try {
			sAuthorName = Util.formatTag(tagImpl.getAuthorName());
			if (sAuthorName.equals("")) {
				sAuthorName = Messages.getString("Track_unknown_author");
			}
		} catch (Exception e) {
			Log.error("103", e);
		}
		return sAuthorName;

	}

	/**
	 * @return style name
	 */
	public String getStyleName() {
		String style = Messages.getString("Track_unknown_style");
		try {
			style = Util.formatTag(tagImpl.getStyleName());
			if (style.toLowerCase().equals("unknown")){
				style = Messages.getString("Track_unknown_style");
			}
		} catch (Exception e) {
			Log.error("103", e);
		}
		return style;

	}

	/**
	 * @return length in sec
	 */
	public long getLength() {
		long length = 0;
		try {
			length = tagImpl.getLength();
		} catch (Exception e) {
			Log.error("103", e);
		}
		return length;

	}

	/**
	 * @return creation year
	 */
	public String getYear() {
		String sYear = Messages.getString("Track_unknown_year");
		try {
			sYear = Util.formatTag(tagImpl.getYear());
			if (sYear.equals("0") || sYear.equals("")){
				sYear = Messages.getString("Track_unknown_year");
			}
		} catch (Exception e) {
			Log.error("103", e);
		}
		return sYear;

	}

	/**
	 * @return quality
	 */
	public String getQuality() {
		String sQuality = Messages.getString("Track_unknown_quality");
		try {
			sQuality = Util.formatTag(tagImpl.getQuality());
		} catch (Exception e) {
			Log.error("103", e);
		}
		return sQuality;

	}

	/**
	 * @param sTrackName
	 */
	public void setTrackName(String sTrackName) {
		try {
			tagImpl.setTrackName(sTrackName);
		} catch (Exception e) {
			Log.error("104", e);
		}
	}

	/**
	 * @param sAlbumName
	 */
	public void setAlbumName(String sAlbumName) {
		try {
			tagImpl.setAlbumName(sAlbumName);
		} catch (Exception e) {
			Log.error("104", e);
		}

	}

	/**
	 * @param sAuthorName
	 */
	public void setAuthorName(String sAuthorName) {
		try {
			tagImpl.setAuthorName(sAuthorName);
		} catch (Exception e) {
			Log.error("104", e);
		}

	}

	/**
	 * @param style
	 */
	public void setStyleName(String style) {
		try {
			tagImpl.setStyleName(style);
		} catch (Exception e) {
			Log.error("104", e);
		}

	}

	/**
	 * @param length
	 */
	public void setLength(long length) {
		try {
			tagImpl.setLength(length);
		} catch (Exception e) {
			Log.error("104", e);
		}

	}

	/**
	 * @param sYear
	 */
	public void setYear(String sYear) {
		try {
			tagImpl.setTrackName(sYear);
		} catch (Exception e) {
			Log.error("104", e);
		}

	}

	/**
	 * @param sQuality
	 */
	public void setQuality(String sQuality) {
		try {
			tagImpl.setTrackName(sQuality);
		} catch (Exception e) {
			Log.error("104", e);
		}

	}

}
