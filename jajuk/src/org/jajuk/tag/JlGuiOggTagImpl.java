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

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import javazoom.jlgui.player.amp.tag.OggVorbisInfo;

import org.jajuk.util.Util;

/**
 *  jlGui Ogg vorbis tager implementation
 *
 * @author     Bertrand Florat
 * @created    27 avr. 2004
 */
public class JlGuiOggTagImpl implements ITagImpl {

	private OggVorbisInfo oggInfo;
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getTrackName()
	 */
	public String getTrackName() throws Exception {
		String sOut = oggInfo.getTitle();
		if ( sOut == null ||  sOut.startsWith("Track")){//$NON-NLS-1$
			return ""; //doing that, the item wil be the default jajuk unknown string //$NON-NLS-1$
		}
		return sOut;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getAlbumName()
	 */
	public String getAlbumName() throws Exception {
		String sOut = oggInfo.getAlbum();
		if (  sOut == null || sOut.equals("title")){//$NON-NLS-1$
			return ""; //doing that, the item wil be the default jajuk unknown string //$NON-NLS-1$
		}
		return sOut;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getAuthorName()
	 */
	public String getAuthorName() throws Exception {
		String sOut = oggInfo.getArtist();
		if (  sOut == null || sOut.equals("title")){//$NON-NLS-1$
			return ""; //doing that, the item wil be the default jajuk unknown string //$NON-NLS-1$
		}
		return sOut;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getStyleName()
	 */
	public String getStyleName() throws Exception {
		String sOut = oggInfo.getGenre();
		if (  sOut == null || sOut.equals("genre")){//$NON-NLS-1$
			return ""; //doing that, the item wil be the default jajuk unknown string //$NON-NLS-1$
		}
		//Sometimes, the style has this form : (nb)
		if ( sOut.startsWith("(") && sOut.indexOf(')')!=-1){//$NON-NLS-1$//$NON-NLS-2$
			sOut = sOut.substring(1,sOut.indexOf(')'));
			try{
				sOut = Util.genres[Integer.parseInt(sOut)];
			}
			catch(Exception e){
				return ""; //error, return unknown //$NON-NLS-1$
			}
		}
		return sOut;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getLength()
	 */
	public long getLength() throws Exception {
		return oggInfo.getPlayTime();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getYear()
	 */
	public String getYear() throws Exception {
		String sOut = oggInfo.getYear();
		if ( sOut == null ){//$NON-NLS-1$
			return ""; //doing that, the item wil be the default jajuk unknown string //$NON-NLS-1$
		}
		return sOut;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getQuality()
	 */
	public String getQuality() throws Exception {
		return Integer.toString(oggInfo.getBitRate()/1000);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getComment()
	 */
	public String getComment() throws Exception {
		Vector v = oggInfo.getComment();
		String sOut = "";
		if (v != null){
		    Iterator it = v.iterator();
		    while (it.hasNext()){
		        sOut += it.next().toString();
		    }
		}
		return sOut;
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

	public void setComment(String sComment) throws Exception {
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setFile(java.io.File)
	 */
	public void setFile(File fio) throws Exception {
		oggInfo = new OggVorbisInfo();
		oggInfo.load(fio);
	}
    
     /* (non-Javadoc)
     * @see org.jajuk.tag.ITagImpl#getTagItem(java.lang.String)
     */
    public String getTagItem(String sTagItem) throws Exception {
        return null;
    }

    /* (non-Javadoc)
     * @see org.jajuk.tag.ITagImpl#setTagItem(java.lang.String, java.lang.String)
     */
    public void setTagItem(String sTagItem, String sValue) throws Exception {
    }

}
