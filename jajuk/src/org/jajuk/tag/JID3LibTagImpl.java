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
 * USA. Revision 1.1  2003/10/26 21:28:49  bflorat
 * USA. 26/10/2003
 * USA.
 */

package org.jajuk.tag;

import java.io.File;
import java.io.IOException;

import org.farng.mp3.AbstractMP3Tag;
import org.farng.mp3.MP3File;
import org.farng.mp3.TagException;
import org.farng.mp3.id3.ID3v1;
import org.jajuk.base.ITagImpl;

/**
 * Type description
 * 
 * @author bflorat @created 25 oct. 2003
 */
public class JID3LibTagImpl implements ITagImpl {

	/** CurrentMP3File* */
	private MP3File mp3File;

	/** current tag * */
	private AbstractMP3Tag tag;

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
	public String getYear() throws Exception {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getQuality()
	 */
	public String getQuality() throws Exception {
		return null;
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
	 * @see org.jajuk.base.ITagImpl#setLength(long)
	 */
	public void setLength(long length) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#setYear(java.lang.String)
	 */
	public void setYear(String sYear) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#setQuality(java.lang.String)
	 */
	public void setQuality(String sQuality) throws Exception {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#setFile(java.io.File)
	 */
	public void setFile(File fio) throws Exception {
		this.mp3File = new MP3File(fio);
		if (this.mp3File.hasID3v1Tag()) {
			tag = mp3File.getID3v1Tag();
			System.out.println("v1 " + fio);
		} else if (mp3File.hasID3v2Tag()) {
			tag = mp3File.getID3v2Tag();
			System.out.println("v2 " + fio);
		} else if (mp3File.hasLyrics3Tag()) {
			tag = mp3File.getLyrics3Tag();
			System.out.println("Lyrics " + fio);
		} else {
			tag=null;
			System.out.println("others " + fio);
		}

	}

}
