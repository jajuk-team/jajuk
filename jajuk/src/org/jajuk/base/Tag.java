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
package org.jajuk.base;

import java.io.File;

import org.jajuk.i18n.Messages;
import org.jajuk.tag.ITagImpl;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * abstract tag, independent from real implementation
 * @author Bertrand Florat 
 * @created 25 oct. 2003
 */
public class Tag implements ITechnicalStrings{

    /** Current tag impl* */
    private ITagImpl tagImpl;
    /** Current file* */
    private File fio;
    /**Is this tag corrupted ?*/
    private boolean bCorrupted = false;
   

    /**
     * Tag constructor
     * 
     * @param fio
     */
    public Tag(java.io.File fio)throws JajukException {
        this(fio,false);
    }
    
    /**
     * Tag constructor
     * @bIgnoreError : ignore errror and keep instance
     * @param fio
     */
    public Tag(java.io.File fio,boolean bIgnoreErrors)throws JajukException {
        try{
            this.fio = fio;
            Type type = TypeManager.getInstance().getTypeByExtension(Util.getExtension(fio));
            tagImpl = type.getTagImpl();
            tagImpl.setFile(fio);
            bCorrupted = false;
        } catch (Exception e) {
            bCorrupted = true;
            if (!bIgnoreErrors) throw new JajukException("103",fio.getName(), e); //$NON-NLS-1$
        }   
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
            Log.warn("Wrong track name:"+fio.getName()); //$NON-NLS-1$
        }
        return sTrackName;
    }

    /**
     * @return album name
     */
    public String getAlbumName() {
        if (tagImpl == null){  //if the type doesn't support tags ( like wav )
            return UNKNOWN_ALBUM; //$NON-NLS-1$
        }
        String sAlbumlName = null;
        String sTemp = ""; //$NON-NLS-1$
        try {
            sTemp = tagImpl.getAlbumName().trim();
            if (Messages.getString(UNKNOWN_ALBUM).equals(sTemp)){  //it is done to avoid duplicates unknown albums if the tag is the real string "unknown" in the current language  //$NON-NLS-1$
                sAlbumlName = UNKNOWN_ALBUM; //$NON-NLS-1$
            }
            else if (!"".equals(sTemp)){ //$NON-NLS-1$
                sAlbumlName = sTemp;
            }
        } catch (Exception e) {
            Log.warn("Wrong album name:"+fio.getName()); //$NON-NLS-1$
        }
        if (sAlbumlName == null){  //album tag cannot be found
            if (Boolean.valueOf(ConfigurationManager.getProperty(CONF_TAGS_USE_PARENT_DIR)).booleanValue()){
                sAlbumlName = fio.getParentFile().getName(); //if album is not found, take current dirtectory as album name
            }
            else{
                sAlbumlName = Messages.getString(UNKNOWN_ALBUM);  //album inconnu //$NON-NLS-1$
            }
        }
        sAlbumlName = Util.formatTag(sAlbumlName);
        return sAlbumlName;
    }

    /**
     * @return author name
     */
    public String getAuthorName() {
        String sAuthorName = UNKNOWN_AUTHOR; //$NON-NLS-1$
        //if the type doesn't support tags ( like wav )
        if (tagImpl == null){  
            return sAuthorName;
        }
        String sTemp = ""; //$NON-NLS-1$
        try {
            sTemp = tagImpl.getAuthorName().trim();
            if (Messages.getString(UNKNOWN_AUTHOR).equals(sTemp)){  //it is done to avoid duplicates unknown authors if the tag is the real string "unknown" in the current language  //$NON-NLS-1$
                sAuthorName = UNKNOWN_AUTHOR; //$NON-NLS-1$
            }
            else if (!"".equals(sTemp)){ //$NON-NLS-1$
                sAuthorName = Util.formatTag(sTemp);
            }
        } catch (Exception e) {
            Log.warn("Wrong author name:"+fio.getName()); //$NON-NLS-1$
        }
        return sAuthorName;

    }
    

    /**
     * @return style name
     */
    public String getStyleName() {
        String style = UNKNOWN_STYLE; //$NON-NLS-1$
        //if the type doesn't support tags ( like wav )
        if (tagImpl == null){  
            return style;
        }
        String sTemp = ""; //$NON-NLS-1$
        try {
            sTemp = tagImpl.getStyleName().trim();
            if (Messages.getString(UNKNOWN_STYLE).equals(sTemp)){  //it is done to avoid duplicates unknown styles if the tag is the real string "unknown" in the current language  //$NON-NLS-1$
                style = UNKNOWN_STYLE; //$NON-NLS-1$
            }
            else if (!"".equals(sTemp)){ //$NON-NLS-1$
                if( sTemp.equals("unknown")){ //$NON-NLS-1$
                    sTemp = style;
                }
                style = Util.formatTag(sTemp);
            }
        } catch (Exception e) {
            Log.warn("Wrong style name:"+fio.getName()); //$NON-NLS-1$
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
            Log.warn("Wrong length:"+fio.getName()); //$NON-NLS-1$
        }
        return length;
    }

    /**
     * @return creation year
     */
    public long getYear() {
        long lYear = 0;
        //if the type doesn't support tags ( like wav )
        if (tagImpl == null){  
            return lYear;
        }
        try {
             lYear = tagImpl.getYear(); //check it is an integer
        } catch (Exception e) {
            Log.warn("Wrong year:"+fio.getName()); //$NON-NLS-1$
        }
        return lYear;

    }

    /**
     * @return quality
     */
    public long getQuality() {
        long lQuality = 0l;
        //if the type doesn't support tags ( like wav )
        if (tagImpl == null){  
            return lQuality;
        }
        String sTemp = ""; //$NON-NLS-1$
        try {
            lQuality = tagImpl.getQuality();
        } catch (Exception e) {
            Log.warn("Wrong quality:"+fio.getName()); //$NON-NLS-1$
        }
        return lQuality;
    }

    /**
     * @return comment
     */
    public String getComment() {
        String sComment = ""; //$NON-NLS-1$
        //if the type doesn't support tags ( like wav )
        if (tagImpl == null){  
            return sComment;
        }
        String sTemp = ""; //$NON-NLS-1$
        try {
            sTemp = tagImpl.getComment();
            if (sTemp != null && !sTemp.equals("")){ //$NON-NLS-1$
                sComment = Util.formatTag(sTemp);
            }
        } catch (Exception e) {
            Log.warn("Wrong comment:"+fio.getName()); //$NON-NLS-1$
        }
        return sComment;    
    }
    
     /**
     * @return comment
     */
    public long getOrder() {
        long l = 0l;
        try {
            l = tagImpl.getOrder();
            if (l < 0){
               throw new Exception("Negative Order"); //$NON-NLS-1$
            }
        } catch (Exception e) {
            //just debug, no warn because wrong order are too often and generate too much traces
            Log.warn("Wrong order:"+fio.getName()); //$NON-NLS-1$
            l = 0;
        }
        return l;    
    }
    
    /**
     * @param sTrackName
     */
    public void setTrackName(String sTrackName) throws JajukException{
        try {
            tagImpl.setTrackName(sTrackName);
        } catch (Exception e) {
            throw new JajukException("104",fio.getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * @param sAlbumName
     */
    public void setAlbumName(String sAlbumName) throws JajukException{
        try {
            tagImpl.setAlbumName(sAlbumName);
        } catch (Exception e) {
            throw new JajukException("104",fio.getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * @param sAuthorName
     */
    public void setAuthorName(String sAuthorName) throws JajukException{
        try {
            tagImpl.setAuthorName(sAuthorName);
        } catch (Exception e) {
            throw new JajukException("104",fio.getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * @param style
     */
    public void setStyleName(String style) throws JajukException{
        try {
            tagImpl.setStyleName(style);
        } catch (Exception e) {
            throw new JajukException("104",fio.getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * @param style
     */
    public void setOrder(long lOrder) throws JajukException {
        try {
            tagImpl.setOrder(lOrder);
        } catch (Exception e) {
            throw new JajukException("104",fio.getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * @param sYear
     */
    public void setYear(long lYear) throws JajukException {
        try {
            tagImpl.setYear(lYear);
        } catch (Exception e) {
            throw new JajukException("104",fio.getName(), e); //$NON-NLS-1$
        }
    }

    /**
     * @param sComment
     */
    public void setComment(String sComment) throws JajukException{
        try {
            tagImpl.setComment(sComment);
        } catch (Exception e) {
            throw new JajukException("104",fio.getName(), e); //$NON-NLS-1$
        }
    }
    
    /**
     * Commit tags
     */
    public void commit() throws JajukException{
        try {
            tagImpl.commit();
            InformationJPanel.getInstance().setMessage(Messages.getString("PropertiesWizard.11")+" "+fio.getName(),InformationJPanel.INFORMATIVE); //$NON-NLS-1$ //$NON-NLS-2$
            if (Log.isDebugEnabled()){
                Log.debug(Messages.getString("PropertiesWizard.11")+" "+fio.getName());
            }
        } catch (Exception e) {
             throw new JajukException("104",fio.getName()+"\n"+e.getMessage(),e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public boolean isCorrupted() {
        return bCorrupted;
    }

    public void setCorrupted(boolean corrupted) {
        bCorrupted = corrupted;
    }
  

}