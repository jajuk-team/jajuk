/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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

import java.io.File;

import org.jajuk.util.ITechnicalStrings;

import entagged.audioformats.Tag;

/**
 * MPlayer tager implementation
 * 
 * @author Bertrand Florat
 * @created 2006/10/07
 */
public class MPlayerTagImpl implements ITagImpl, ITechnicalStrings {

	private Tag tag;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getTrackName()
	 */
	public String getTrackName() throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getAlbumName()
	 */
	public String getAlbumName() throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getAuthorName()
	 */
	public String getAuthorName() throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getStyleName()
	 */
	public String getStyleName() throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getLength()
	 */
	public long getLength() throws Exception {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getYear()
	 */
	public long getYear() throws Exception {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getQuality()
	 */
	public long getQuality() throws Exception {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getComment()
	 */
	public String getComment() throws Exception {
		return ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.tag.ITagImpl#getOrder()
	 */
	public long getOrder() throws Exception {
		String sOrder = tag.getFirstTrack();
		return Long.parseLong(sOrder); // try to parse integer
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#setTrackName(java.lang.String)
	 */
	public void setTrackName(String sTrackName) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#setAlbumName(java.lang.String)
	 */
	public void setAlbumName(String sAlbumName) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#setAuthorName(java.lang.String)
	 */
	public void setAuthorName(String sAuthorName) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#setStyleName(java.lang.String)
	 */
	public void setStyleName(String style) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#setOrder(java.lang.String)
	 */
	public void setOrder(long lOrder) throws Exception {
	}

	public void setComment(String sComment) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.tag.ITagImpl#getTagItem(java.lang.String)
	 */
	public String getTagItem(String sTagItem) throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.tag.ITagImpl#setTagItem(java.lang.String,
	 *      java.lang.String)
	 */
	public void setTagItem(String sTagItem, String sValue) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.tag.ITagImpl#commit()
	 */
	public void commit() throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.tag.ITagImpl#setYear(int)
	 */
	public void setYear(long lYear) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.tag.ITagImpl#setFile(java.io.File)
	 */
	public void setFile(File fio) throws Exception {
	}

}
