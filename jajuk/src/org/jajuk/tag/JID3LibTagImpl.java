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
 * USA. Revision 1.2  2003/11/07 23:58:14  bflorat
 * USA. 08/11/2003
 * USA.
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
import org.farng.mp3.id3.AbstractID3;
import org.farng.mp3.id3.AbstractID3v2;
import org.farng.mp3.id3.AbstractID3v2Frame;
import org.farng.mp3.id3.FrameBodyTALB;
import org.farng.mp3.id3.FrameBodyTCON;
import org.farng.mp3.id3.FrameBodyTIT2;
import org.farng.mp3.id3.FrameBodyTLEN;
import org.farng.mp3.id3.FrameBodyTPE1;
import org.farng.mp3.id3.FrameBodyTYER;
import org.farng.mp3.id3.ID3v1;
import org.farng.mp3.id3.ID3v2_2;
import org.jajuk.base.ITagImpl;
import org.jajuk.util.Util;

import de.vdheide.mp3.ID3v2;

/**
 * Type description
 * 
 * @author bflorat @created 25 oct. 2003
 */
public class JID3LibTagImpl implements ITagImpl {

	/** CurrentMP3File* */
	private MP3File mp3File;

	/** current tag * */
	private AbstractID3 tag;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getTrackName()
	 */
	public String getTrackName() throws Exception {
		if (tag instanceof ID3v1 ){
			return ((ID3v1)tag).getTitle();
		}
		else if (tag instanceof AbstractID3v2 ){
			FrameBodyTIT2 frame = (FrameBodyTIT2)(((AbstractID3v2)tag).getFrame("TIT2").getBody()); 
			return frame.getText();
		}
		return null; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getAlbumName()
	 */
	public String getAlbumName() throws Exception {
		if (tag instanceof ID3v1 ){
		return ((ID3v1)tag).getAlbum();
	}
	else if (tag instanceof AbstractID3v2 ){
		FrameBodyTALB frame = (FrameBodyTALB)(((AbstractID3v2)tag).getFrame("TABL").getBody()); 
		return frame.getText();
	}
	return null; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getAuthorName()
	 */
	public String getAuthorName() throws Exception {
		if (tag instanceof ID3v1 ){
			return ((ID3v1)tag).getArtist();
		}
		else if (tag instanceof AbstractID3v2 ){
			FrameBodyTPE1 frame = (FrameBodyTPE1)(((AbstractID3v2)tag).getFrame("TPE1").getBody()); 
			return frame.getText();
		}
	return null; 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getStyleName()
	 */
	public String getStyleName() throws Exception {
		if (tag instanceof ID3v1 ){
			return Util.getStringGenre((int)((ID3v1)tag).getGenre());
		}
		else if (tag instanceof AbstractID3v2 ){
			FrameBodyTCON frame = (FrameBodyTCON)(((AbstractID3v2)tag).getFrame("TCON").getBody()); 
			return frame.getText();
		}		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getLength()
	 */
	public long getLength() throws Exception {
		if (tag instanceof ID3v1 ){
			return ((ID3v1)tag).getSize();
		}
	/*	else if (tag instanceof AbstractID3v2 ){
			AbstractID3v2Frame frame = ((AbstractID3v2)tag).getFrame("TLEN");
			if (frame != null) {
				FrameBodyTLEN frameBody = (FrameBodyTLEN)(((AbstractID3v2)tag).getFrame("TLEN").getBody()); 
				return Long.parseLong(frameBody.getText());
			}
			else{
				return ((ID3v2_2)tag).getSize().
			}
			
		}*/
			return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getYear()
	 */
	public String getYear() throws Exception {
		if (tag instanceof ID3v1 ){
			return ((ID3v1)tag).getYear();
		}
		else if (tag instanceof AbstractID3v2 ){
			FrameBodyTYER frame = (FrameBodyTYER)(((AbstractID3v2)tag).getFrame("TYER").getBody()); 
			return frame.getText();
		}	
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.ITagImpl#getQuality()
	 */
	public String getQuality() throws Exception {
		return Integer.toString(mp3File.getBitRate());
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
		if (mp3File.hasID3v1Tag()){
			this.tag = mp3File.getID3v1Tag();
		}
		if (mp3File.hasID3v2Tag()){
				this.tag = mp3File.getID3v2Tag();
		}
	}
}
