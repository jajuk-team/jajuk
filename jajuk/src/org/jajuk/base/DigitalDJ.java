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
    
    /**DJ unique ID*/
    protected String sID;
    
    /**DJ name*/
    protected String sName;
        
    /**Use ratings*/
    protected boolean bUseRatings = false;
    
    /**Rating floor*/
    protected int iRatingLevel = 0;
    
    /**Fading duration in sec*/
    protected int iFadingDuration = 0;
    
    /**
     * Constructor without ID
     * @param sName DJ name
     */
    public DigitalDJ(String sName){
        this.sName = sName;
        //create a unique ID for this DJ, simply use current time in ms
        this.sID = Long.toString(System.currentTimeMillis());
    }
    
    /**
     * Constructor with ID
     * @param sName DJ name
     * @param sID DJ ID
     */
    public DigitalDJ(String sName,String sID){
        this.sName = sName;
        this.sID = sID;
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
     * @return DJ common parameters 
     */
    protected String toXMLGeneralParameters(){
    	StringBuffer sb = new StringBuffer();
    	sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        sb.append("<"+XML_DJ_DJ+" "+XML_VERSION+"='"+JAJUK_VERSION+"' "+XML_NAME+"='"+sName+"' "+XML_TYPE+"='"+this.getClass().getName()+"'>\n");
        sb.append("\t<"+XML_DJ_GENERAL+" "+XML_DJ_USE_RATINGS+"='"+bUseRatings+"' ");
        sb.append(XML_DJ_RATING_LEVEL+"='"+iRatingLevel+"' ");
        sb.append(XML_DJ_FADE_DURATION+"='"+iFadingDuration+"'/>\n");
        return sb.toString();
    }
    
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
        return this.iRatingLevel;
    }

    /**
     * @param ratingFloor The iRatingFloor to set.
     */
    public void setRatingLevel(int ratingFloor) {
        this.iRatingLevel = ratingFloor;
    }
    
    /**
     * 
     * @return Generated playlist
     */
    abstract public ArrayList<File> generatePlaylist();

    public String getID() {
        return this.sID;
    }
    
   
}
