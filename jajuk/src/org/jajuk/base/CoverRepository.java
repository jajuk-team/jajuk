/*
 *  Jajuk
 *  Copyright (C) 2003,2005 Bertrand Florat
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

import java.awt.MediaTracker;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

import org.jajuk.util.DownloadManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;


/**
 *  Cover repository : manages covers image for a good memory usage
 * <p>Singleton</p>
 * @author     Bertrand Florat
 * @created    20 mars 2005
 */
public class CoverRepository implements Observer,ITechnicalStrings {
    
    /**URL-> image objects mapping */
    private HashMap hmUrlImages = new HashMap(30);
    
    /**URL-> image size (in KB) mapping */
    private HashMap hmUrlSize = new HashMap(30);
    
    /**Self instance*/
    static CoverRepository cr;
    
    /**Contains list of urls currently loading*/
    private ArrayList alLoading = new ArrayList(10);
    
    /**
     * Constructor
     */
    private CoverRepository(){
        ObservationManager.register(EVENT_COVER_REFRESH,this);
        ObservationManager.register(EVENT_PLAYER_STOP,this);
        ObservationManager.register(EVENT_ZERO,this);
    }
    
    /**
     * 
     * @return the singleton
     */
    public static CoverRepository getInstance(){
        if (cr == null){
            cr = new CoverRepository();
        }
        return cr;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if ( EVENT_COVER_REFRESH.equals(subject) ||
                EVENT_PLAYER_STOP.equals(subject) || 
                EVENT_ZERO.equals(subject)){ 
            hmUrlImages.clear();
        }
    }
    
    /**
     * Return an image for a given url
     * @param url image URL
     * @param iType Cover type
     * @return
     */
    public ImageIcon getImage(URL url,int iType) throws JajukException{
        //check if this image is already in the repository
        if (hmUrlImages.containsKey(url)){
            return (ImageIcon)hmUrlImages.get(url);
        }
        //no? check if another thread is not already downloading it
        if (alLoading.contains(url)){
            while (alLoading.contains(url)){
                //another thread is downloading this url, wait until it's finished or in error
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    Log.error(e);
                }
            }
            if (hmUrlImages.containsKey(url)){
                return (ImageIcon)hmUrlImages.get(url);
            }
            else{
                throw new JajukException("129"); //$NON-NLS-1$
            }
        }
        try{
            //tells others that we are downloading this URL
            alLoading.add(url);
            long l = System.currentTimeMillis();
            ImageIcon image = null;
            byte[] bData = null;
            if ( iType == Cover.LOCAL_COVER 
                    || iType == Cover.DEFAULT_COVER  
                    || iType == Cover.ABSOLUTE_DEFAULT_COVER){
                image = new ImageIcon(url);
                if ( image.getImageLoadStatus() != MediaTracker.COMPLETE){
                    throw new JajukException("129"); //$NON-NLS-1$
                }
                hmUrlSize.put(url,Integer.toString((int)(Math.ceil((double)new File(url.getFile()).length()/1024))));
            }
            else if (iType == Cover.REMOTE_COVER){
                bData = DownloadManager.download(url);
                image = new ImageIcon(bData); 
                if ( image.getImageLoadStatus() != MediaTracker.COMPLETE){
                    throw new JajukException("129"); //$NON-NLS-1$
                }
                hmUrlSize.put(url,Integer.toString((int)(Math.ceil((double)bData.length/1024))));
            }
            Log.debug("Loaded "+url.toString()+" in  "+(System.currentTimeMillis()-l)+" ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            hmUrlImages.put(url,image); //store the image in the repository
            return image;
        }
        finally{ //make sure to unlock others
            alLoading.remove(url);
        }
    }
    
    /**
     * Return image size for a given URL
     * @param url
     * @return
     */
    public String getSize(URL url){
        if (hmUrlSize.containsKey(url)){
            return (String)hmUrlSize.get(url);
        }
        else{
            return "0"; //$NON-NLS-1$
        }
    }
}
