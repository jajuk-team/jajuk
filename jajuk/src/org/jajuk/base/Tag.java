/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 * $Revision$
 */
package org.jajuk.base;

import java.io.File;

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
			Log.error("103",fio.getName(),e);
		}
		this.fio = fio;
	}

	/**
	 * @return track name as defined in tags are file name otherwise
	 */
	public String getTrackName() {
		String sTrackName = fio.getName();
		String sTemp = "";
		try {
			sTemp = tagImpl.getTrackName();
			if (sTemp != null && !sTemp.equals("")){
				sTrackName = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103", fio.getName(),e);
		}
		return sTrackName;
	}

	/**
	 * @return album name
	 */
	public String getAlbumName() {
		String sAlbumlName = fio.getParentFile().getName(); //if album is not found, take current dirtectory as album name
		//TODO make it an option
		String sTemp = "";
		try {
			sTemp = tagImpl.getAlbumName();
			if (sTemp != null && !sTemp.equals("")){
				sAlbumlName = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103",fio.getName(), e);
		}
		return sAlbumlName;
	}

	/**
	 * @return author name
	 */
	public String getAuthorName() {
		String sAuthorName = Messages.getString("Track_unknown_author");
		String sTemp = "";
		try {
			sTemp = tagImpl.getAuthorName();
			if (sTemp != null && !sTemp.equals("")){
				sAuthorName = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103", fio.getName(),e);
		}
		return sAuthorName;

	}

	/**
	 * @return style name
	 */
	public String getStyleName() {
		String style = Messages.getString("Track_unknown_style");
		String sTemp = "";
		try {
			sTemp = tagImpl.getStyleName();
			if (sTemp != null && !sTemp.equals("")){
				if(sTemp.equals("unknown")){
					sTemp = style;
				}
				style = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103", fio.getName(),e);
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
			Log.error("103", fio.getName(),e);
		}
		return length;

	}

	/**
	 * @return creation year
	 */
	public String getYear() {
		String sYear = Messages.getString("Track_unknown_year");
		String sTemp = "";
		try {
			sTemp = tagImpl.getYear();
			if (sTemp != null && !sTemp.equals("")&& !sTemp.equals("0")){
				sYear = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103", fio.getName(),e);
		}
		return sYear;

	}

	/**
	 * @return quality
	 */
	public String getQuality() {
		String sQuality = Messages.getString("Track_unknown_quality");
		String sTemp = "";
		try {
			sTemp = tagImpl.getQuality();
			if (sTemp != null && !sTemp.equals("")){
				sQuality = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103",fio.getName(), e);
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
			Log.error("104",fio.getName(), e);
		}
	}

	/**
	 * @param sAlbumName
	 */
	public void setAlbumName(String sAlbumName) {
		try {
			tagImpl.setAlbumName(sAlbumName);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e);
		}

	}

	/**
	 * @param sAuthorName
	 */
	public void setAuthorName(String sAuthorName) {
		try {
			tagImpl.setAuthorName(sAuthorName);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e);
		}

	}

	/**
	 * @param style
	 */
	public void setStyleName(String style) {
		try {
			tagImpl.setStyleName(style);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e);
		}

	}

	/**
	 * @param length
	 */
	public void setLength(long length) {
		try {
			tagImpl.setLength(length);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e);
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
			Log.error("104", fio.getName(),e);
		}

	}

}
