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

package org.jajuk.dj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.util.Util;


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
            HashSet<Style> set = transition.getFrom().getStyles();
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
        ArrayList<File> out = new ArrayList(500);
        //get a global shuffle selection
        ArrayList<File> global = FileManager.getInstance().getGlobalShufflePlaylist(); 
        //Select by rate if needed
        filterFilesByRate(global);
        //None element, leave
        if (global.size() == 0){
            return out;
        }
        //Sort tracks by ambience (set of styles)
        HashMap<Ambience,ArrayList<File>> hmAmbienceFiles = new HashMap(100); 
        for (File file:global){
            Transition tr = getTransition(file.getTrack().getStyle());
            //add the file, note that styles associated with none transition are
            //added in null transition
            Ambience from = null;
            if (tr != null){
                from = tr.getFrom();
            }
            ArrayList<File> files = hmAmbienceFiles.get(from);
            if (files == null){
                files = new ArrayList(100);
            }
            files.add(file);
            hmAmbienceFiles.put(from,files);
        }
        //Get first track
        for (File file:global){
            if (file.getTrack().getStyle().equals(startupStyle)){
                out.add(file);
                break;
            }
        }
        //none matching track? add a shuffle file
        if (out.size() == 0){
            out.add((File)Util.getShuffleItem(global));
        }
        //compute number of items to add
        int items = global.size() - 1; //by default, collection size (minus one already added)
        if (!bUnicity && items < MIN_TRACKS_NUMBER_WITHOUT_UNICITY){ 
            //under a limit, if collection is too small and no unicity, use several times the same files
            items = MIN_TRACKS_NUMBER_WITHOUT_UNICITY;
        }
        Ambience current  = getAmbience(out.get(0).getTrack().getStyle());
        int comp = 1; //item compt
        //start transition applying
        while (comp < items){
            Ambience next = null;
            Transition tr = null;
            //find next ambience
            if (current != null){
                for (Transition transition:transitions){
                    for (Style style:current.getStyles()){
                        if (transition.getFrom().getStyles().contains(style)){
                            tr = transition;
                            next = tr.getTo();
                            break;
                        }
                    }
                }
            }
            else{ //startyp style doesn't match any known ambience, take an ambience associated with
                //any file style
                File fShuffle = (File)Util.getShuffleItem(global);
                next = getAmbience(fShuffle.getTrack().getStyle());
            }
            //store the new ambience
            current = next;
            int iterations = 1;
            if (tr != null){
                iterations = tr.getNbTracks();
            }
            for (int j=0;j<iterations;j++){
                ArrayList<File> files = hmAmbienceFiles.get(next);
                //take any file from files associated with this ambience
                //if no more files, try from 'null' ambience
                if (files.size() > 0){
                    File file = (File)Util.getShuffleItem(files);
                    out.add(file);
                    //unicity in selection, remove it from this ambience
                    if (bUnicity){
                        files.remove(file);
                    }
                }
                else{ //no more files in this ambience, search in null ambience
                    files = hmAmbienceFiles.get(null);
                    //no more tracks even in others, leave
                    if (files.size() == 0){
                        return out;
                    }
                    else{
                        File file = (File)Util.getShuffleItem(files);
                        out.add(file);
                        //unicity in selection, remove it from this ambience
                        if (bUnicity){
                            files.remove(file);
                        }   
                    }
                }
                comp ++;
            }
        }
        return out;
    }
        
    /**
     * 
     * @return next ambience according to transitions or null if no transition defined
     */
    private Ambience getNextAmbience(Ambience ambience){
        Ambience out = null;
        for (Transition transition:transitions){
            for (Style style:ambience.getStyles()){
                if (transition.getFrom().getStyles().contains(style)){
                    return transition.getTo();
                }
            }
        }
        //if here, it means than the given style is not in from styles, return null
        return null;
    }
   
     /**
     * @return ambience associated with a style known in transitions or null if none
     */
    private Ambience getAmbience(Style style){
        Ambience out = null;
        for (Transition transition:transitions){
            if (transition.getFrom().getStyles().contains(style)){
                return transition.getFrom();
            }
        }
        return null;
    }
   
        
    /**
     * (non-Javadoc)
     * @see dj.DigitalDJ#toXML()
     **/
     public String toXML(){
         StringBuffer sb = new StringBuffer(2000);
         sb.append(toXMLGeneralParameters());
         sb.append("\t<"+XML_DJ_TRANSITIONS+ " "+XML_DJ_STARTUP_STYLE+"='"+
             getStartupStyle().getId()+"'>\n");
         for (Transition transition: transitions){
             sb.append("\t\t<"+XML_DJ_TRANSITION+" "+
            		 XML_DJ_FROM+"='"+transition.getFrom().toXML()+
                     "' "+XML_DJ_TO+"='"+transition.getTo().toXML()+"' "+
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
