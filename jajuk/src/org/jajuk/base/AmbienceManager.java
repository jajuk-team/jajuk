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
import java.util.Iterator;
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
public class AmbienceManager implements ITechnicalStrings,Observer{

    /**Ambience name-> ambience*/
    private HashMap<String,Ambience> ambiences = new HashMap(10);
    
    /**Self instance*/
    private static AmbienceManager self;
    
    /**
     * No direct constructor
     */
    private AmbienceManager() {
        ObservationManager.register(EVENT_STYLE_NAME_CHANGED,this);
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
    public void load() {
        Properties properties = ConfigurationManager.getProperties();
        Enumeration e = properties.keys();
        while (e.hasMoreElements()){
            String sKey = (String)e.nextElement();
            if (sKey.matches(AMBIENCE_PREFIX+".*")){
                HashSet<Style> styles = new HashSet(10);
                StringTokenizer st = new StringTokenizer((String)properties.get(sKey),",");
                while (st.hasMoreTokens()){
                    Style style = (Style)StyleManager.getInstance().getItem(st.nextToken());
                    if (style != null){
                        styles.add(style);
                    }
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
     * Cette methode sert à ...
     * @param s ma chaine
     * @param i mon entier
     */
    public void mamethode(String s,int i){
        
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
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if (EVENT_STYLE_NAME_CHANGED.equals(subject)){
            Properties properties = event.getDetails();
            Style old = (Style)properties.get(DETAIL_OLD);
            Style newStyle = (Style)properties.get(DETAIL_NEW);
            //replace style into all styles
            for (Ambience ambience:ambiences.values()){
                if (ambience.getStyles().contains(old)){
                    ambience.removeStyle(old);
                    ambience.addStyle(newStyle);
                }
            }
        }
    }
    
    /**
     * Perform required operations before exit
     *
     */
    public void commit(){
        //first, remove all ambience from configuration
        Properties properties = ConfigurationManager.getProperties();
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()){
            String sKey = (String)it.next();
            if (sKey.startsWith(AMBIENCE_PREFIX)){
                it.remove();
            }
        }
        //now create and set each ambience
        for (Ambience ambience:ambiences.values()){
            String styles = "";
            for (Style style:ambience.getStyles()){
                styles += style.getId() + ',';
            }
            styles = styles.substring(0,styles.length()-1);
            ConfigurationManager.setProperty(AMBIENCE_PREFIX+ambience.getName(),styles);
        }
    }
    
    /**
     * Remove a ambience
     * @param sAmbienceName the ambience to remove
     */
    public void removeAmbience(String sAmbienceName){
        this.ambiences.remove(sAmbienceName);
    }
    
}
