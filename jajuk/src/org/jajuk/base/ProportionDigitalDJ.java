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
import java.util.HashMap;
import java.util.HashSet;

/**
 *  Type description
 *
 * @author     Bertrand Florat
 * @created    1 mars 2006
 */
public class ProportionDigitalDJ extends DigitalDJ {

    /**Set of Style -> proportions*/
    private HashMap<HashSet<Style>,Float> proportions;
    
    /**
     * @param sName
     */
    public ProportionDigitalDJ(String sName) {
        super(sName);
        this.proportions = new HashMap(10);
    }

    /* (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#generatePlaylist()
     */
    @Override
    public ArrayList<File> generatePlaylist() {
        return null;
    }
    
    /**
     * @return Proportions
     */
    public HashMap<HashSet<Style>,Float> getProportions() {
        return this.proportions;
    }
    
    /**
     * Delete a proportion for given style
     * @param style
     */
    public void deleteProportion(Style style) {
        this.proportions.remove(style);
    }
    
    /**
     * Add a propertion
     * @param style
     * @param proportion as a float. Exemple: 0.5 for 50%
     */
    public void addProportion(HashSet styles,float proportion) {
        this.proportions.put(styles,proportion);
    }
    
     /**
    /* (non-Javadoc)
     * @see org.jajuk.base.DigitalDJ#toXML()
     **/
    public String toXML(){
        StringBuffer sb = new StringBuffer(2000);
        sb.append("<?xml version='1.0' encoding='UTF-8'?>\n");
        sb.append("<dj jajuk_version='1.2' name='"+sName+"' type='proportion'>\n");
        sb.append("\t<general_parameters use-ratings='"+bUseRatings+"' ");
        sb.append("rating_level='"+iRatingFloor+"' ");
        sb.append("\t</general_parameters>");
        sb.append("\t<propotions>");
        for (HashSet<Style> styles: proportions.keySet()){
            String stylesDesc = "";
            for (Style style:styles){
                stylesDesc += style.getId();
            }
            stylesDesc = stylesDesc.substring(0,stylesDesc.length() - 1);
            sb.append("\t\t<proportion styles='"+stylesDesc+"' value='"+proportions.get(styles)+"'/>");
        }
        sb.append("\t</propotions>");
        sb.append("</dj>");
        return sb.toString();
    }
    
    

}
