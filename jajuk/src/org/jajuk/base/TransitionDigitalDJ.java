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
import java.util.HashSet;

/**
 *  Type description
 *
 * @author     Bertrand Florat
 * @created    1 mars 2006
 */
public class TransitionDigitalDJ extends DigitalDJ {

    /**List of transitions*/
    private ArrayList<Transition> alTransitions;
    
    /**
     * @param sName
     */
    public TransitionDigitalDJ(String sName) {
        super(sName);
        this.alTransitions = new ArrayList(10);
    }
    
     /**
     * @return DJ transitions
     */
    public ArrayList getTransitions() {
        return this.alTransitions;
    }
    
    /**
     * Delete a transition at given offset
     * @param offset
     */
    public void deleteTransition(int offset) {
        this.alTransitions.remove(offset);
    }
    
    /**
     * Add a transition
     * @param transition
     * @param offset
     */
    public void addTransition(Transition transition,int offset) {
        this.alTransitions.add(offset,transition);
    }
    
     /**
     * 
     * @param style
     * @return transition mapping this style or null if none maps it
     */
    public Transition getTransition(Style style){
        Transition out = null;
        for (Transition transition: alTransitions){
            HashSet<Style> set = transition.getFrom();
            for (Style s:set){
                if (s.equals(style)){
                    return transition;
                }
            }
        }
        return out;
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#generatePlaylist()
     */
    @Override
    public ArrayList<File> generatePlaylist() {
        return null;
    }

    
    /* (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#toXML()
     */
    public String toXML(){
        StringBuffer sb = new StringBuffer(2000);
        sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        sb.append("<DJ jajuk_version='1.2' name='"+sName+"'>\n");
        sb.append("</DJ>");
        return sb.toString();
    }
    
}
