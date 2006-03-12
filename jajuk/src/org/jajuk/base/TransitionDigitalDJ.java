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
    
    /**Startup style**/
    private Style startupStyle;
    
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

    /**
    /* (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#toXML()
     **/
    public String toXML(){
        StringBuffer sb = new StringBuffer(2000);
        sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        sb.append("<dj jajuk_version='1.2' name='"+sName+"' type='transition'>\n");
        sb.append("\t<general_parameters use-ratings='"+bUseRatings+"' ");
        sb.append("rating_level='"+iRatingFloor+"' ");
        sb.append("start_with='"+startupStyle.getId()+"'>");
        sb.append("\t</general_parameters>");
        sb.append("\t<transitions>");
        for (Transition transition: alTransitions){
            String sFrom = "";
            for (Style style:transition.getFrom()){
               sFrom += style.getId()+","; 
            }
            sFrom = sFrom.substring(0,sFrom.length()-1); //remove last coma
            String sTo = "";
            for (Style style:transition.getTo()){
               sTo += style.getId()+","; 
            }
            sTo = sTo.substring(0,sTo.length()-1); //remove last coma
            sb.append("\t\t<transition from='"+sFrom+"' to='"+sTo+"'/>");
        }
        sb.append("\t</transitions>");
        sb.append("</dj>");
        return sb.toString();
    }

    public Style getStartupStyle() {
        return this.startupStyle;
    }

    public void setStartupStyle(Style startupStyle) {
        this.startupStyle = startupStyle;
    }
    
}
