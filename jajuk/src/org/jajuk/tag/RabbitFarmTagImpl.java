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
 * $Log$
 * Revision 1.1  2003/11/07 23:58:14  bflorat
 * 08/11/2003
 *
 * Revision 1.1  2003/10/26 21:28:49  bflorat
 * 26/10/2003
 *
 */

package org.jajuk.tag;

import java.io.File;
import java.io.PrintStream;

import org.jajuk.base.ITagImpl;
import org.jajuk.i18n.Messages;

import de.ueberdosis.mp3info.ExtendedID3Tag;
import de.ueberdosis.mp3info.ID3Reader;
import de.ueberdosis.util.OutputCtr;
import de.vdheide.mp3.MP3File;

/**
 *  Type description
 *
 * @author     bflorat
 * @created    25 oct. 2003
 */
public class RabbitFarmTagImpl implements ITagImpl {

	
	/*MP3 file*/
	MP3File mp3file;
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getTrackName()
	 */
	public String getTrackName() throws Exception {
		return mp3file.getTitle().getTextContent();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getAlbumName()
	 */
	public String getAlbumName() throws Exception {
		return mp3file.getAlbum().getTextContent();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getAuthorName()
	 */
	public String getAuthorName() throws Exception {
		return mp3file.getArtist().getTextContent();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getStyleName()
	 */
	public String getStyleName() throws Exception {
		return mp3file.getGenre().getTextContent();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getLength()
	 */
	public long getLength() throws Exception {
		return mp3file.getLength();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getYear()
	 */
	public String getYear() throws Exception {
		return mp3file.getYear().getTextContent();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getQuality()
	 */
	public String getQuality() throws Exception {
		return Integer.toString(mp3file.getBitrate());
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
		mp3file = new MP3File(fio.getAbsolutePath());
	}
	
}
