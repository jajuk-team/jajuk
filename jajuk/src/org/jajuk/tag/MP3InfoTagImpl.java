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
 * $Revision$
 *
 */

package org.jajuk.tag;

import java.io.File;

import org.jajuk.base.ITagImpl;
import org.jajuk.util.Util;

import de.ueberdosis.mp3info.ExtendedID3Tag;
import de.ueberdosis.mp3info.ID3Reader;
import de.ueberdosis.mp3info.facades.Wamp;
import de.ueberdosis.mp3info.id3v2.ID3V2Tag;
import de.ueberdosis.util.OutputCtr;

/**
 *  Type description
 *
 * @author     bflorat
 * @created    25 oct. 2003
 */
public class MP3InfoTagImpl implements ITagImpl {

	
	/**Tags reader*/
	ID3Reader reader;
	/**Tag itself*/
	ExtendedID3Tag tag;
	/*ID3 V2 tag*/
	ID3V2Tag id3v2tag = null;
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getTrackName()
	 */
	public String getTrackName() throws Exception {
		return tag.getTitle();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getAlbumName()
	 */
	public String getAlbumName() throws Exception {
		return tag.getAlbum();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getAuthorName()
	 */
	public String getAuthorName() throws Exception {
		return tag.getArtist();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getStyleName()
	 */
	public String getStyleName() throws Exception {
		if ( id3v2tag == null){
			return tag.getGenreS();  //v1 tag
		}
		int iStyle = new Wamp(id3v2tag).getGenre();
		if ( iStyle < 0){
			return tag.getGenreS();
		}
		return Util.getStringGenre(iStyle);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getLength()
	 */
	public long getLength() throws Exception {
		return tag.getRuntime();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getYear()
	 */
	public String getYear() throws Exception {
		return tag.getYear();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getQuality()
	 */
	public String getQuality() throws Exception {
		return tag.getBitrateS();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setTrackName(java.lang.String)
	 */
	public void setTrackName(String sTrackName) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setAlbumName(java.lang.String)
	 */
	public void setAlbumName(String sAlbumName) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setAuthorName(java.lang.String)
	 */
	public void setAuthorName(String sAuthorName) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setStyleName(java.lang.String)
	 */
	public void setStyleName(String style) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setLength(long)
	 */
	public void setLength(long length) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setYear(java.lang.String)
	 */
	public void setYear(String sYear) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setQuality(java.lang.String)
	 */
	public void setQuality(String sQuality) throws Exception {
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setFile(java.io.File)
	 */
	public void setFile(File fio) throws Exception {
		OutputCtr.setLevel(0);
		reader = new ID3Reader(fio.getAbsolutePath());
		tag = reader.getExtendedID3Tag();
		id3v2tag = ID3Reader.getV2Tag();
	}

}
