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

    /**List of transitions, need to be a list, not a set for offset*/
    private ArrayList<Transition> transitions;
    
    /**Startup style**/
    private Style startupStyle;
    
    /**
     * @param sName
     */
    public TransitionDigitalDJ(String sName) {
        super(sName);
        this.transitions = new ArrayList(10);
    }
    
     /**
     * @return DJ transitions
     */
    public ArrayList getTransitions() {
        return this.transitions;
    }
    
    /**
     * Delete a transition at given offset
     * @param offset
     */
    public void deleteTransition(int offset) {
        this.transitions.remove(offset);
    }
    
    /**
     * Add a transition
     * @param transition
     * @param offset
     */
    public void addTransition(Transition transition,int offset) {
        this.transitions.add(offset,transition);
    }
    
     /**
     * 
     * @param style
     * @return transition mapping this style or null if none maps it
     */
    public Transition getTransition(Style style){
        Transition out = null;
        for (Transition transition: transitions){
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
     * (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#toXML()
     **/
     public String toXML(){
         StringBuffer sb = new StringBuffer(2000);
         sb.append(toXMLGeneralParameters());
         sb.append("\t<"+XML_DJ_TRANSITIONS+">\n");
         for (Transition transition: transitions){
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
             sb.append("\t\t<"+XML_DJ_TRANSITION+" "+
            		 XML_DJ_FROM+"='"+sFrom+"' "+XML_DJ_TO+"='"+sTo+"' "+
            		 XML_DJ_NUMBER+"='"+transition.getNbTracks()+"'/>\n");
         }
         sb.append("\t</"+XML_DJ_TRANSITIONS+">\n");
         sb.append("</"+XML_DJ_DJ+">\n");
         return sb.toString();
     }

    public Style getStartupStyle() {
        return this.startupStyle;
    }

    public void setStartupStyle(Style startupStyle) {
        this.startupStyle = startupStyle;
    }

	public void setTransitions(ArrayList<Transition> transitions) {
		this.transitions = transitions;
	}
    
}
