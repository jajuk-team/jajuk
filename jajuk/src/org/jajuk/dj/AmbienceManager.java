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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;

/**
 *  Ambience manager
 *
 * @author     Bertrand Florat
 * @created    19 march 2006
 */
public class AmbienceManager implements ITechnicalStrings,Observer{

    /**Ambience id-> ambience*/
    private HashMap<String,Ambience> ambiences = new HashMap<String,Ambience>(10);
    
    /**Self instance*/
    private static AmbienceManager self;
    
    /**
     * No direct constructor
     */
    private AmbienceManager() {
        ObservationManager.register(this);
    }
    
    public Set<EventSubject> getRegistrationKeys(){
        HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
        eventSubjectSet.add(EventSubject.EVENT_STYLE_NAME_CHANGED);
        return eventSubjectSet;
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
    
    /** Load properties from in file 
     * Format: jajuk.ambience.<ID>/<name>=style1,style2,...
     * */
    public void load() {
    	//if first startup, define default ambiences
        if (ConfigurationManager.getBoolean(CONF_FIRST_CON)){ 
        	createDefaultAmbiences();
        	return;
    	}
    	Properties properties = ConfigurationManager.getProperties();
        Enumeration e = properties.keys();
        while (e.hasMoreElements()){
            String sKey = (String)e.nextElement();
            if (sKey.matches(AMBIENCE_PREFIX+".*")){ //$NON-NLS-1$
                HashSet<Style> styles = new HashSet<Style>(10);
                StringTokenizer st = new StringTokenizer((String)properties.get(sKey),","); //$NON-NLS-1$
                while (st.hasMoreTokens()){
                    Style style = (Style)StyleManager.getInstance().getItem(st.nextToken());
                    if (style != null){
                        styles.add(style);
                    }
                }
                String ambienceDesc = sKey.substring(AMBIENCE_PREFIX.length());
                int index = ambienceDesc.indexOf('/');
                if (index == -1){
                    continue;
                }
                String ambienceID = ambienceDesc.substring(0,index);
                String ambienceName = ambienceDesc.substring(index+1);
                Ambience ambience = 
                    new Ambience(ambienceID,ambienceName,styles);
                ambiences.put(ambienceID,ambience);
            }
        }
    }
    
     /**
     * 
     * @return list of registated ambiences
     */
    public Collection<Ambience> getAmbiences(){
        ArrayList<Ambience> al = new ArrayList<Ambience>(ambiences.values());
        Collections.sort(al);
        return al;
    }
    
    /**
     * 
     * @param sID Ambience id
     * @return registrated ambience
     */
    public Ambience getAmbience(String sID){
        return ambiences.get(sID);
    }
    
    /**
     * 
     * @param sName Ambience name
     * @return registrated ambience or null if no matching name
     */
    public Ambience getAmbienceByName(String sName){
        for (Ambience ambience: ambiences.values()){
            if (ambience.getName().equals(sName)){
                return ambience;
            }
        }
        return null;
    }
    
    
    /**
     * Register a new ambience
     * @param ambience ambience to register
     */
    public void registerAmbience(Ambience ambience){
        ambiences.put(ambience.getID(),ambience);
    }
    
    /**
     * 
     * @return default Ambience
     */
    public Ambience getDefaultAmbience(){
        String sDefault = ConfigurationManager.getProperty(CONF_DEFAULT_AMBIENCE);
        return getAmbience(sDefault);
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(Event event) {
        EventSubject subject = event.getSubject();
        if (EventSubject.EVENT_STYLE_NAME_CHANGED.equals(subject)){
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
            if (ambience.getStyles().size() > 0){
                String styles = ""; //$NON-NLS-1$
                for (Style style:ambience.getStyles()){
                    styles += style.getId() + ',';
                }
                styles = styles.substring(0,styles.length()-1);
                ConfigurationManager.setProperty(AMBIENCE_PREFIX+
                    ambience.getID()+'/'+ambience.getName(),styles);
            }
        }
    }
    
    /**
     * Remove a ambience
     * @param sAmbienceID the ambience to remove
     */
    public void removeAmbience(String sAmbienceID){
        this.ambiences.remove(sAmbienceID);
        //Propagate the event
        Properties properties = new Properties();
        properties.put(DETAIL_CONTENT,sAmbienceID);
        ObservationManager.notify(new Event(EventSubject.EVENT_AMBIENCE_REMOVED,properties));
    }
    
    /**
     * Create out of the box ambiences
     *
     */
    public void createDefaultAmbiences(){
    	//Define default amience by style name
    	String[] stylesRockPop = new String[]{"Classic Rock","Pop","Rock","Ska","AlternRock","Instrumental Pop", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    			"Instrumental Rock","Southern Rock","Pop/Funk","Folk-Rock","Rock & Roll","Symphonic Rock" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    			,"Ballad","Christian Rock","JPop", "SynthPop"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("0",Messages.getString("Ambience.0"),stylesRockPop)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesRap = new String[]{"Hip-Hop","R&B","Rap","Fusion","Gangsta","Christian Rap", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    			"Porn Groove","Rhytmic Soul","Christian Gangsta"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("1",Messages.getString("Ambience.1"),stylesRap)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesHardRock = new String[]{"Grunge","Metal","Industrial","Death Metal","Fusion","Punk", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    			"Gothic","Darkwave","Fast Fusion","Hard Rock","Gothic Rock","Progressive Rock","Punk Rock" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    			,"Terror","Negerpunk", "Polsk Punk","Heavy Metal","Black Metal","Thrash Metal"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("2",Messages.getString("Ambience.2"),stylesHardRock)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesTechno = new String[]{"Dance","New Age","Techno","Euro-Techno","Ambient","Trance","House", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    			"Game","Space","Techno-Industrial","Eurodance","Dream","Jungle","Rave","Euro-House","Goa", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
    			"Club-House","Hardcore","Beat"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("3",Messages.getString("Ambience.3"),stylesTechno)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesElectro = new String[]{"Trip-Hop","Acid","Electronic","Club"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("4",Messages.getString("Ambience.4"),stylesElectro)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesClassical = new String[]{"Classical","Chorus","Opera", "Chamber Music", "Sonata", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    			"Symphony"}; //$NON-NLS-1$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("5",Messages.getString("Ambience.5"),stylesClassical)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesSoft = new String[]{"Reggae","Acid Jazz","Slow Rock", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    			"Jazz","Easy Listening","Acoustic","Ballad"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("6",Messages.getString("Ambience.6"),stylesSoft)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesParty = new String[]{"Dance", "Disco", "Funk","Ska","Soul","Eurodance","Big Band", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    			"Club","Rhytmic Soul","Dance Hall","Club-House"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("7",Messages.getString("Ambience.7"),stylesParty)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesJazzBlues = new String[]{"Jazz","Jazz+Funk","Bass","Acid Jazz"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("8",Messages.getString("Ambience.8"),stylesJazzBlues)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesWorld = new String[]{"Ethnic","Native American","Tribal","Polka","Celtic","Folklore","Indie"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("9",Messages.getString("Ambience.9"),stylesWorld)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesOthers = new String[]{"Other","Alternative","Soundtrack","Vocal","Meditative","Comedy", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    			"Humour","Speech","Anime"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("10",Messages.getString("Ambience.10"),stylesOthers)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesFolkOldies = new String[]{"Country", "Oldies","Gospel","Pop-Folk","Southern Rock","Cabaret" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    			,"Retro","Folk-Rock","National Folk","Swing","Rock & Roll","Folk","Revival","Chanson"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("11",Messages.getString("Ambience.11"),stylesFolkOldies)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesInde = new String[]{"Noise","AlternRock","New Wave","Psychedelic","Acid Punk", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    			"Avantgarde","Psychedelic Rock","Freestyle","Drum Solo","Drum & Bass"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("12",Messages.getString("Ambience.12"),stylesInde)); //$NON-NLS-1$ //$NON-NLS-2$
    	String[] stylesLatin = new String[]{"Latin","Tango", "Samba","Acapella","Salsa"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    	AmbienceManager.getInstance().registerAmbience(new Ambience("13",Messages.getString("Ambience.13"),stylesLatin)); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
}
