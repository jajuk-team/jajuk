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
import java.util.Iterator;
import java.util.List;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;

import entagged.audioformats.AudioFile;
import entagged.audioformats.AudioFileIO;
import entagged.audioformats.Tag;

/**
 *  jlGui MP3 tager implementation
 *
 * @author     Bertrand Florat
 * @created    26 avr. 2004
 */
public class EntaggedTagImpl implements ITagImpl,ITechnicalStrings {

	private Tag tag;
	private AudioFile audioFile;
    
	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getTrackName()
	 */
	public String getTrackName() throws Exception {
		String sOut = tag.getFirstTitle();
		if ( sOut == null || sOut.startsWith("Track")){//$NON-NLS-1$
			return ""; //doing that, the item wil be the default jajuk unknown string //$NON-NLS-1$
		}
		return sOut;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getAlbumName()
	 */
	public String getAlbumName() throws Exception {
		String sOut = tag.getFirstAlbum();
		if ( sOut == null || sOut.equals("title")){//$NON-NLS-1$
			return ""; //doing that, the item wil be the default jajuk unknown string //$NON-NLS-1$
		}
		return sOut;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getAuthorName()
	 */
	public String getAuthorName() throws Exception {
		String sOut = tag.getFirstArtist();
		if ( sOut == null || sOut.equals("title")){//$NON-NLS-1$
			return ""; //doing that, the item wil be the default jajuk unknown string //$NON-NLS-1$
		}
		return sOut;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getStyleName()
	 */
	public String getStyleName() throws Exception {
		String sOut = tag.getFirstGenre();
		if ( sOut==null || sOut.equals("genre")){//$NON-NLS-1$
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
		return audioFile.getLength();
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getYear()
	 */
	public long getYear() throws Exception {
		String sOut = tag.getFirstYear();
		if ( sOut == null ){//$NON-NLS-1$
			return 0;
		}
		return Long.parseLong(sOut);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getQuality()
	 */
	public long getQuality() throws Exception {
		long lQuality = audioFile.getBitrate();
		return lQuality;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#getComment()
	 */
	public String getComment() throws Exception {
		List list = tag.getComment();
		String sOut = ""; //$NON-NLS-1$
		if (list != null){
		    Iterator it = list.iterator();
		    while (it.hasNext()){
		        sOut += ' '+it.next().toString();
		    }
		}
        if (sOut.contains("()")){ //$NON-NLS-1$
            sOut = sOut.substring(9,sOut.length()); //remove [LAN]()
        }
		return sOut;
	}

      /* (non-Javadoc)
     * @see org.jajuk.tag.ITagImpl#getOrder()
     */
    public long getOrder() throws Exception {
        String sOrder = tag.getFirstTrack();
        return Long.parseLong(sOrder); //try to parse integer
    }

    
    /* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setTrackName(java.lang.String)
	 */
	public void setTrackName(String sTrackName) throws Exception {
        tag.setTitle(sTrackName);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setAlbumName(java.lang.String)
	 */
	public void setAlbumName(String sAlbumName) throws Exception {
        tag.setAlbum(sAlbumName);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setAuthorName(java.lang.String)
	 */
	public void setAuthorName(String sAuthorName) throws Exception {
	    tag.setArtist(sAuthorName);
    }

	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setStyleName(java.lang.String)
	 */
	public void setStyleName(String style) throws Exception {
	    tag.setGenre(style);
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.ITagImpl#setOrder(java.lang.String)
     */
    public void setOrder(long lOrder) throws Exception {
        tag.setTrack(Long.toString(lOrder));
    }

	public void setComment(String sComment) throws Exception {
	    tag.setComment(sComment);
    }
	
	/* (non-Javadoc)
	 * @see org.jajuk.base.ITagImpl#setFile(java.io.File)
	 */
	public void setFile(File fio) throws Exception {
		try{
		    audioFile = AudioFileIO.read(fio);
            tag = audioFile.getTag();
   //         tag.setEncoding("ISO-8859-1");
        }
        catch(Throwable t){ //can throw OutOfMemory errors
            System.gc(); //call garbage collector to avoid than folowing throw make itself an out of memory
            throw new JajukException("103",fio.toString(),t); //$NON-NLS-1$
        }
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
    
     /* (non-Javadoc)
     * @see org.jajuk.tag.ITagImpl#commit()
     */
    public void commit() throws Exception{
      //  tag.setEncoding("ISO-8859-1");//have to set it here to fix an issue with audio-formats 0.12
        audioFile.commit();
    }

    /* (non-Javadoc)
     * @see org.jajuk.tag.ITagImpl#setYear(int)
     */
    public void setYear(long lYear) throws Exception {
        tag.setYear(Long.toString(lYear));
    }

}
