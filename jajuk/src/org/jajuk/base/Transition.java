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
import java.util.Collections;
import java.util.HashSet;

/**
 *  Represent a transition from one style to another (used by digital DJs)
 *
 * @author     Bertrand Florat
 * @created    27/02/2006
 */
public class Transition {
    /** From styles  */
    private HashSet<Style> from;
    
    /** To styles  */
    private HashSet<Style> to;
    
    /** Nb of tracks  */
    private int nb = 2;
    
    /**Nb of played tracks*/
    private int played = 0;
    
    /**Constructor
     * @param from source styles
     * @param to destination style
     * @param nb number of tracks played before changing style
     * */
    public Transition(HashSet from, HashSet to, int nb){
        this.from = from;
        this.to = to;
        this.nb = nb;
    }
    
    
    /**
     * equals method
     * @return whether two object are equals
     */
    public boolean equals(Object other){
        if (!(other instanceof Transition)){
            return false;
        }
        return getFrom().equals(((Transition)other).getFrom())
            && getTo().equals(((Transition)other).getTo());
    }


    /**
     * @return Returns the from.
     */
    /**
     * @return
     */
    public HashSet getFrom() {
        return this.from;
    }
        
    /**
     * From String, return style1,style2,...
     */
    public String getFromString(){
        String out = "";
        for (Style s:from){
            out += s.getName2()+',';
        }
        if (out.length() > 0){
            out = out.substring(0,out.length()-1); //remove trailling ,
        }
        return out;
    }
    
    /**
     * "To" String, return style1,style2,...
     */
    public String getToString(){
        String out = "";
        for (Style s:to){
            out += s.getName2()+',';
        }
        if (out.length() > 0){
            out = out.substring(0,out.length()-1); //remove trailling ,
        }
        return out;
    }

    /**
     * @return Returns the to.
     */
    /**
     * @return
     */
    public HashSet getTo() {
        return this.to;
    }
    
    public void addFromStyle(Style style){
        from.add(style);
    }
    
    /**
     * @param style
     */
    public void removeFromStyle(Style style){
        from.remove(style);
    }
    
    public void addToStyle(Style style){
        to.add(style);
    }
    
    /**
     * @param style
     */
    public void removeToStyle(Style style){
        to.remove(style);
    }
    
    /**
     * 
     * @return next style to be played or null if no idea
     */
    public Style getNextStyle(){
        if (to.size() == 0){
            return null;
        }
        else if (to.size() == 1){
            return to.iterator().next();
        }
        else{
            //several destination styles, return a shuffle one
            ArrayList<Style> alStyles = new ArrayList(to);
            Collections.shuffle(alStyles);
            return alStyles.get(0);
        }
    }


    public int getNbTracks() {
        return this.nb;
    }


    public void setFrom(HashSet<Style> from) {
        this.from = from;
    }


    public void setTo(HashSet<Style> to) {
        this.to = to;
    }


    public void setNb(int nb) {
        this.nb = nb;
    }
}
