/*
 *  Jajuk
 *  Copyright (C) 2004 bflorat
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

/**
 *  A FIFO item
 *
 * @author     bflorat
 * @created    9 nov. 2004
 */
public class StackItem {

    /**Associated file*/
    private File file;
    
    /**Repeat flag*/
    private boolean bRepeat = false;
    
    /**User launch  flag*/
    private boolean bUserLaunch  = false;
    
    /**Planned track ?*/
    private boolean bPlanned = false;
        
    /**Visible track (only used for planned tracks)*/
    private boolean bVisible = true;
    
    
    /**
     * Constructor
     * @param file associated file
     */ 
    public StackItem(File file){
         this.file =file; 
     }
    
    
    /**
     * Constructor 
     * @param file
     * @param bUserLauched
     */
    public StackItem(File file,boolean bUserLauched){
         this(file,false,bUserLauched);
     }
    
    /**
     * Constructor 
     * @param file
     * @param bUserLauched
     */
    public StackItem(File file,boolean bRepeat,boolean bUserLauched){
         this.file =file; 
         this.bRepeat = bRepeat;
         this.bUserLaunch = bUserLauched;
         this.bPlanned = false;
         this.bVisible = true;
     }
   
    
    /**
     * @return Returns the bRepeat.
     */
    public boolean isRepeat() {
        return bRepeat;
    }
    
    /**
     * @param repeat The bRepeat to set.
     */
    public void setRepeat(boolean repeat) {
        bRepeat = repeat;
    }
    
    /**
     * @return Returns the bVisible.
     */
    public boolean isVisible() {
        return bVisible;
    }
    
    /**
     * @param visible The bVisible to set.
     */
    public void setVisible(boolean visible) {
        bVisible = visible;
    }
    
    /**
     * @return Returns the file.
     */
    public File getFile() {
        return file;
    }
    /**
     * @return Returns the bUserLaunch.
     */
    public boolean isUserLaunch() {
        return bUserLaunch;
    }
    /**
     * @param userLaunch The bUserLaunch to set.
     */
    public void setUserLaunch(boolean userLaunch) {
        bUserLaunch = userLaunch;
    }
    /**
     * @return Returns the bPlanned.
     */
    public boolean isPlanned() {
        return bPlanned;
    }
    /**
     * @param planned The bPlanned to set.
     */
    public void setPlanned(boolean planned) {
        bPlanned = planned;
    }
}
