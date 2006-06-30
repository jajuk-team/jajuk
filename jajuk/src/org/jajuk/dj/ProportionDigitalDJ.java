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
import java.util.Collections;
import java.util.HashMap;

import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.util.ITechnicalStrings;


/**
 *  Type description
 *
 * @author     Bertrand Florat
 * @created    1 mars 2006
 */
public class ProportionDigitalDJ extends DigitalDJ implements ITechnicalStrings{

    /**Set of proportions*/
    private ArrayList<Proportion> proportions;
    
    /**
     * @param sID
     */
    public ProportionDigitalDJ(String sID) {
        super(sID);
        this.proportions = new ArrayList(10);
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#generatePlaylist()
     */
    @Override
    public ArrayList<File> generatePlaylist() {
        ArrayList<File> out = new ArrayList(100);
        out = getSequence(); 
        if ( !bUnicity && out.size() > 0){
            while (out.size() < MIN_TRACKS_NUMBER_WITHOUT_UNICITY){
                out.addAll(getSequence());
            }
        }
        return out;
    }
    
     /**
     * 
     * @return a single loop sequence
     */
    private ArrayList<File> getSequence(){
        ArrayList<File> out = new ArrayList(100);
        HashMap<Proportion,ArrayList<File>> list = new HashMap(10); 
        //get a global shuffle selection, we will keep only tracks with wanted styles
        ArrayList<File> global = FileManager.getInstance().getGlobalShufflePlaylist(); 
        //Select by rate if needed
        filterFilesByRate(global);
        for (File file:global){
            for (Proportion prop:proportions){
                if (prop.getStyles().contains(file.getTrack().getStyle())){
                    ArrayList files = list.get(prop);
                    if (files == null){ //not yet file list
                        files = new ArrayList(100);
                        list.put(prop,files);
                    }
                    files.add(file);
                }
            }
        }
        //check if all properties are represented
        if (list.keySet().size() < proportions.size()){
            return out; //return void list
        }
        //now, keep the smallest list before applying proportion
        Proportion minProp = null;
        int iMinSize = 0;
        float fTotal = 0;
        for (Proportion prop:list.keySet()){
            fTotal += prop.getProportion();
            ArrayList files = list.get(prop);
            //keep proportion with smallest number of files
            if (minProp == null || files.size() < iMinSize ){
                minProp = prop;
                iMinSize = files.size();
            }
        }
        //apply proportions
        for (Proportion prop:list.keySet()){
            ArrayList<File> files = list.get(prop);
            out.addAll(files.subList(0,(int)(iMinSize*prop.getProportion())));
        }
        //complete this shuffle files if total sum < 100%
        if (fTotal < 1.0){
           int iNbAdditional = (int)((1.0-fTotal)*iMinSize);
           for (int i=0;i<iNbAdditional;i++){
               out.add(global.get((int)(Math.random()*global.size())));
           }
        }
        //shuffle selection
        Collections.shuffle(out);
        return out;
    }
    
    /**
     * @return Proportions
     */
    public ArrayList<Proportion> getProportions() {
        return this.proportions;
    }
    
   /**
    * (non-Javadoc)
    * @see dj.DigitalDJ#toXML()
    **/
    public String toXML(){
        StringBuffer sb = new StringBuffer(2000);
        sb.append(toXMLGeneralParameters());
        sb.append("\t<"+XML_DJ_PROPORTIONS+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        for (Proportion proportion: proportions){
            String stylesDesc = ""; //$NON-NLS-1$
            for (Style style:proportion.getStyles()){
                stylesDesc += style.getId()+',';
            }
            //remove trailing coma
            stylesDesc = stylesDesc.substring(0,stylesDesc.length() - 1);
            sb.append("\t\t<"+XML_DJ_PROPORTION+" "+XML_DJ_STYLES+"='"+stylesDesc+"' "+ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            		XML_DJ_VALUE+"='"+proportion.getProportion()+"'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        sb.append("\t</"+XML_DJ_PROPORTIONS+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append("</"+XML_DJ_DJ+">\n"); //$NON-NLS-1$ //$NON-NLS-2$
        return sb.toString();
    }

	public void setProportions(ArrayList<Proportion> proportions) {
		this.proportions = proportions;
	}
    
    

}
