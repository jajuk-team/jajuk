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

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;

/**
 *  Ambience manager
 *
 * @author     Bertrand Florat
 * @created    19 march 2006
 */
public class AmbienceManager implements ITechnicalStrings{

    /**Ambience name-> ambience*/
    private static HashMap<String,Ambience> ambiences = new HashMap(10);
    
    /**Self instance*/
    private static AmbienceManager self;
    
    /**
     * No direct constructor
     */
    private AmbienceManager() {
    }
    
    /**
     * 
     * @return singleton
     */
    public static AmbienceManager getInstance(){
        if (self == null){
            self = new AmbienceManager();
        }
        return self;
    }
    
    /** Load properties from in file */
    public static void load() {
        Properties properties = ConfigurationManager.getProperties();
        Enumeration e = properties.keys();
        while (e.hasMoreElements()){
            String sKey = (String)e.nextElement();
            if (sKey.matches(AMBIENCE_PREFIX+".*")){
                HashSet<Style> styles = new HashSet(10);
                StringTokenizer st = new StringTokenizer((String)properties.get(sKey),",");
                while (st.hasMoreTokens()){
                    styles.add((Style)StyleManager.getInstance().getItem(st.nextToken()));
                }
                String ambienceName = sKey.substring(AMBIENCE_PREFIX.length()); 
                Ambience ambience = new Ambience(ambienceName,styles);
                ambiences.put(ambienceName,ambience);
            }
        }
    }
    
     /**
     * 
     * @return list of registated ambiences
     */
    public Collection<Ambience> getAmbiences(){
        return ambiences.values();
    }
    
    /**
     * 
     * @param sName Ambience name
     * @return registrated ambience
     */
    public Ambience getAmbience(String sName){
        return ambiences.get(sName);
    }
    
    /**
     * Register a new ambience
     * @param ambience ambience to register
     */
    public void registerAmbience(Ambience ambience){
        ambiences.put(ambience.getName(),ambience);
        String styles = "";
        for (Style style:ambience.getStyles()){
            styles += style.getId() + ',';
        }
        styles = styles.substring(0,styles.length()-1);
        ConfigurationManager.setProperty(AMBIENCE_PREFIX+ambience.getName(),styles);
    }

}
