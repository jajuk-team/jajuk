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

import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;

import org.jajuk.util.Util;
import org.jajuk.util.log.Log;


/**
 *  A cover, encapsulates URL, files and manages cover priority to display
 *
 * @author     bflorat
 * @created    22 août 2004
 */
public class Cover implements Comparable,ITechnicalStrings {

    public static final int LOCAL_COVER = 0;
    public static final int REMOTE_COVER = 1;
    public static final int DEFAULT_COVER = 2;
    
    /**Cover URL**/
    private URL url;
    
    /**Cover Type*/
    private int iType;
    
    /**Image*/
    private Image image;
     
   /**
   * Constructor
    * @param sUrl cover url : absolute path for a local file, http url for a remote file
    * @param iType
    */
    public Cover(URL url, int iType) {
        this.url = url;
        this.iType = iType;
        this.image = java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }
    
   /**
    * Default cover
    */
    public Cover(){
        try {
            image = java.awt.Toolkit.getDefaultToolkit().getImage(new URL(IMAGES_SPLASHSCREEN));
            iType = DEFAULT_COVER;
        } catch (MalformedURLException e) {
            Log.error(e);
        }
    }
    
    
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        Cover cOther = (Cover)o;
        if (cOther.equals(this)){
            return 0;
        }
        //Default cover is the less prioritory
        if (getType() == DEFAULT_COVER){
            if (cOther.getType() == DEFAULT_COVER){
                return 0; //i'm a default cover and ther other too
            }
            else{
                return -1; //i'm a defautl cover and the other not
            }
        }
         //local covers are prioritary upon remote ones :
        else if ( getType() == LOCAL_COVER ){
            if (cOther.getType()!=LOCAL_COVER){ //the other is not a local cover
                return 1; //i'm a local cover and the other not
            }
            else{ //both are local covers, analyse name
               String sFile = Util.getOnlyFile(getURL().getFile());
               String sOtherFile = Util.getOnlyFile(cOther.getURL().getFile());
                //     files named "cover" or "front" are prioritary upon others : 
                if ( Util.isStandardCover(sFile)){
                    if ( !Util.isStandardCover(sOtherFile)){
                        return 1; //i'm a local standard cover and the other is only a local non-standard cover
                    }
                    else{
                        return 0; //both are local-standard covers
                    }
                }
                else{
                    if ( Util.isStandardCover(sOtherFile)){
                        return -1;//i'm a local cover and the other is local standard cover 
                    }
                    else{
                        return 0; //both are local non-standard covers
                    }
                }        
            }
        }
        return 0; //any other case
    }

    /**
     * @return Returns the iType.
     */
    public int getType() {
        return iType;
    }
    /**
     * @return Returns the sURL.
     */
    public URL getURL() {
        return url;
    }
    
    
    /**
     * @return Returns the image.
     */
    public Image getImage() {
        return image;
    }
    
    /**
     * toString method
     */
    public String toString(){
        return "Type="+iType +" URL="+url;
    }
    
    /**
     * Equals needed for consitency for sorting
     */
    public boolean equals(Object o){
       Cover cOther = (Cover)o;
       if (getType() == 2 || cOther.getType()==2){
           return  (cOther.getType() == getType()); //either both are default cover, either one is not and so, they are unequal
       }
       //here, all url are not null
       return url.equals(cOther.getURL());
    }
    
}
