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

import java.util.ArrayList;

import org.jajuk.util.ITechnicalStrings;


/**
 *  Digital DJ
 *
 * @author     Bertrand Florat
 * @created    27/02/2006
 */
public abstract class DigitalDJ implements ITechnicalStrings{
    
    /**DJ name*/
    protected String sName;
    
    /**Startup style*/
    protected Style startupStyle;
    
    /**Use ratings*/
    protected boolean bUseRatings = false;
    
    /**Rating floor*/
    protected int iRatingFloor = 0;
    
    /**Fading duration in sec*/
    protected int iFadingDuration = 0;
    
    /**
     * Constructor
     * @param sName DJ name
     */
    public DigitalDJ(String sName){
        this.sName = sName;
    }
    
    /**
     * toString method
     * @return String representation of this object
     */
    public String toString(){
        return "DJ "+sName;
    }
    
    /**
     * @return XML representation of this DJ
     */
    abstract public String toXML();
    
    /**
     * 
     * @return DJ name
     */
    public String getName(){
        return sName;
    }
    
    /**
     * equals method
     * @return whether two object are equals
     */
    public boolean equals(Object other){
        if (!(other instanceof DigitalDJ)){
            return false;
        }
        String sOtherName = ((DigitalDJ)other).getName();
        return getName().equals(sOtherName);
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.sName = name;
    }

    /**
     * @return
     */
    public Style getStartupStyle() {
        return this.startupStyle;
    }

    /**
     * @param startupStyle
     */
    public void setStartupStyle(Style startupStyle) {
        this.startupStyle = startupStyle;
    }

    /**
     * @return whether this DJ uses ratings
     */
    public boolean isUseRatings() {
        return this.bUseRatings;
    }

    /**
     * @param useRatings
     */
    public void setUseRatings(boolean useRatings) {
        this.bUseRatings = useRatings;
    }

    /**
     * @return DJ fade duration
     */
    public int getFadingDuration() {
        return this.iFadingDuration;
    }

    /**
     * @param fadingDuration
     */
    public void setFadingDuration(int fadingDuration) {
        this.iFadingDuration = fadingDuration;
    }

    /**
     * @return Returns the iRatingFloor.
     */
    public int getRatingFloor() {
        return this.iRatingFloor;
    }

    /**
     * @param ratingFloor The iRatingFloor to set.
     */
    public void setRatingFloor(int ratingFloor) {
        this.iRatingFloor = ratingFloor;
    }
    
    /**
     * 
     * @return Generated playlist
     */
    abstract public ArrayList<File> generatePlaylist();
    
   
}
