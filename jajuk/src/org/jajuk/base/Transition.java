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
    
    /**Constructor*/
    public Transition(HashSet from, HashSet to){
        this.from = from;
        this.to = to;
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
}
