/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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

import java.io.File;

import org.jajuk.i18n.Messages;
import org.jajuk.tag.*;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * abstract tag, independent from real implementation
 * 
 * @author bflorat @created 25 oct. 2003
 */
public class Tag implements ITechnicalStrings{

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
			Log.error("103",fio.getName(),e); //$NON-NLS-1$
		}
		this.fio = fio;
	}

	/**
	 * @return track name as defined in tags are file name otherwise
	 */
	public String getTrackName() {
		//by default, track name is the file name without extension
		String sTrackName = Util.removeExtension(fio.getName());
		if (tagImpl == null){  //if the type doesn't support tags ( like wav )
			return sTrackName;
		}
		String sTemp = ""; //$NON-NLS-1$
		try {
			sTemp = tagImpl.getTrackName().trim();
			if (!"".equals(sTemp)){ //$NON-NLS-1$
				sTrackName = Util.formatTag(sTemp);  //remove the extension
			}
		} catch (Exception e) {
			Log.error("103", fio.getName(),e); //$NON-NLS-1$
		}
		return sTrackName;
	}

	/**
	 * @return album name
	 */
	public String getAlbumName() {
		if (tagImpl == null){  //if the type doesn't support tags ( like wav )
			return "unknown_album"; //$NON-NLS-1$
		}
		String sAlbumlName = null;
		String sTemp = ""; //$NON-NLS-1$
		try {
			sTemp = tagImpl.getAlbumName().trim();
			if (Messages.getString("unknown_album").equals(sTemp)){  //it is done to avoid duplicates unknown albums if the tag is the real string "unknown" in the current language  //$NON-NLS-1$
					sAlbumlName = "unknown_album"; //$NON-NLS-1$
			}
			else if (!"".equals(sTemp)){ //$NON-NLS-1$
				sAlbumlName = sTemp;
			}
		} catch (Exception e) {
			Log.error("103",fio.getName(), e); //$NON-NLS-1$
		}
		if (sAlbumlName == null){  //album tag cannot be found
			if (Boolean.valueOf(ConfigurationManager.getProperty(CONF_TAGS_USE_PARENT_DIR)).booleanValue()){
				sAlbumlName = fio.getParentFile().getName(); //if album is not found, take current dirtectory as album name
			}
			else{
				sAlbumlName = Messages.getString("unknown_album");  //album inconnu //$NON-NLS-1$
			}
		}
		sAlbumlName = Util.formatTag(sAlbumlName);
		return sAlbumlName;
	}

	/**
	 * @return author name
	 */
	public String getAuthorName() {
		String sAuthorName = "unknown_author"; //$NON-NLS-1$
		//if the type doesn't support tags ( like wav )
		if (tagImpl == null){  
			return sAuthorName;
		}
		String sTemp = ""; //$NON-NLS-1$
		try {
			sTemp = tagImpl.getAuthorName().trim();
			if (Messages.getString("unknown_author").equals(sTemp)){  //it is done to avoid duplicates unknown authors if the tag is the real string "unknown" in the current language  //$NON-NLS-1$
				sAuthorName = "unknown_author"; //$NON-NLS-1$
			}
			else if (!"".equals(sTemp)){ //$NON-NLS-1$
				sAuthorName = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103", fio.getName(),e); //$NON-NLS-1$
		}
		return sAuthorName;

	}
	

	/**
	 * @return style name
	 */
	public String getStyleName() {
		String style = "unknown_style"; //$NON-NLS-1$
		//if the type doesn't support tags ( like wav )
		if (tagImpl == null){  
			return style;
		}
		String sTemp = ""; //$NON-NLS-1$
		try {
			sTemp = tagImpl.getStyleName().trim();
			if (Messages.getString("unknown_style").equals(sTemp)){  //it is done to avoid duplicates unknown styles if the tag is the real string "unknown" in the current language  //$NON-NLS-1$
				style = "unknown_style"; //$NON-NLS-1$
			}
			else if (!"".equals(sTemp)){ //$NON-NLS-1$
				if( sTemp.equals("unknown")){ //$NON-NLS-1$
					sTemp = style;
				}
				style = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103", fio.getName(),e); //$NON-NLS-1$
		}
		return style;

	}

	/**
	 * @return length in sec
	 */
	public long getLength() {
		long length = 0;
		//if the type doesn't support tags ( like wav )
		if (tagImpl == null){  
			return 0;
		}
		try {
			length = tagImpl.getLength();
		} catch (Exception e) {
			Log.error("103", fio.getName(),e); //$NON-NLS-1$
		}
		return length;
	}

	/**
	 * @return creation year
	 */
	public String getYear() {
		String sYear = "unknown_year"; //$NON-NLS-1$
		//if the type doesn't support tags ( like wav )
		if (tagImpl == null){  
			return sYear;
		}
		String sTemp = ""; //$NON-NLS-1$
		try {
			sTemp = tagImpl.getYear();
			if (sTemp != null && !sTemp.equals("")&& !sTemp.equals("0")){ //$NON-NLS-1$ //$NON-NLS-2$
				sYear = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103", fio.getName(),e); //$NON-NLS-1$
		}
		return sYear;

	}

	/**
	 * @return quality
	 */
	public String getQuality() {
		String sQuality = "unknown_quality"; //$NON-NLS-1$
		//if the type doesn't support tags ( like wav )
		if (tagImpl == null){  
			return sQuality;
		}
		String sTemp = ""; //$NON-NLS-1$
		try {
			sTemp = tagImpl.getQuality();
			if (sTemp != null && !sTemp.equals("")){ //$NON-NLS-1$
				sQuality = Util.formatTag(sTemp);
			}
		} catch (Exception e) {
			Log.error("103",fio.getName(), e); //$NON-NLS-1$
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
			Log.error("104",fio.getName(), e); //$NON-NLS-1$
		}
	}

	/**
	 * @param sAlbumName
	 */
	public void setAlbumName(String sAlbumName) {
		try {
			tagImpl.setAlbumName(sAlbumName);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e); //$NON-NLS-1$
		}

	}

	/**
	 * @param sAuthorName
	 */
	public void setAuthorName(String sAuthorName) {
		try {
			tagImpl.setAuthorName(sAuthorName);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e); //$NON-NLS-1$
		}

	}

	/**
	 * @param style
	 */
	public void setStyleName(String style) {
		try {
			tagImpl.setStyleName(style);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e); //$NON-NLS-1$
		}

	}

	/**
	 * @param length
	 */
	public void setLength(long length) {
		try {
			tagImpl.setLength(length);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e); //$NON-NLS-1$
		}

	}

	/**
	 * @param sYear
	 */
	public void setYear(String sYear) {
		try {
			tagImpl.setTrackName(sYear);
		} catch (Exception e) {
			Log.error("104", e); //$NON-NLS-1$
		}

	}

	/**
	 * @param sQuality
	 */
	public void setQuality(String sQuality) {
		try {
			tagImpl.setTrackName(sQuality);
		} catch (Exception e) {
			Log.error("104", fio.getName(),e); //$NON-NLS-1$
		}

	}

}
