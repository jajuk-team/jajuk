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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  Manages Digital DJs
 *  <p>Singleton</p>
 * @author     Bertrand Florat
 * @created    01/03/2006
 */
public class DigitalDJManager implements ITechnicalStrings{

    /**List of registated DJs name->DJ*/
    private static HashMap<String,DigitalDJ> djs;
    
    /**self instance*/
    private static DigitalDJManager dj;
    
    /**
     * no instanciation
     */
    private DigitalDJManager() {
        djs = new HashMap();
    }
    
    /**
     * @return self instance
     */
    public static DigitalDJManager getInstance(){
        if (dj == null){
            dj = new DigitalDJManager();
        }
        return dj;
    }
    
    /**
     * Register a DJ stores in fio file
     * @param fio
     */
    public void registerDJ(File fio){
        
    }
    
    /**
     * 
     * @return DJs iteration
     */
    public Collection<DigitalDJ> getDJs(){
        return djs.values();
    }
    
    /**
     * 
     * @return DJ by name
     */
    public DigitalDJ getDJ(String sName){
        return djs.get(sName);
    }
    
    /**
     * Commit given dj on disk
     * @param dj
     */
    public void commit(DigitalDJ dj){
    	try{
    		BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_JAJUK_DIR+"/"+
    			dj.getID()+"."+XML_DJ_EXTENSION));
    		bw.write(dj.toXML());
    		bw.flush();
    		bw.close();
    	}
    	catch(Exception e){
    		Log.error("145",(dj!=null)?dj.getName():null,e);
    	}
    }
    
    /**
     * Load all DJs (.dj files) found in jajuk home directory 
     *
     */
    public static void loadAllDJs(){
    	try{
    		File[] files = new File(FILE_JAJUK_DIR).listFiles(new FileFilter() {
			
				public boolean accept(File file) {
					if (file.isFile() && file.getPath().endsWith('.'+XML_DJ_EXTENSION)){
						return true;
					}
					return false;
				}
			
			});
    		for (int i=0;i<files.length;i++){
    			try{ //try each DJ to continue others if one fails
    				DigitalDJFactory factory = DigitalDJFactory.getFactory(files[i]);
    				DigitalDJ dj = factory.getDJ(files[i]);
    				djs.put(dj.getName(),dj);
    			}
    			catch(Exception e){
    				Log.error("144",files[i].getAbsolutePath(),e);
    			}
    		}
    	}
    	catch(Exception e){
    		Log.error(e);
    	}
    }
    

}

/**
 * This class is responsable from creating different factories
 * @author Bertrand Florat
 **/
abstract class DigitalDJFactory extends DefaultHandler implements ITechnicalStrings{
	
	/**Factory type (class name)*/
	private static String factoryType; 
	
	/**DJ type (class name)*/
	protected String type; 
	
	/**DJ name*/
	protected String name; 
	
	/**DJ Fade duration*/
	protected int fadeDuration; 
	
	/**Use ratings*/
	protected boolean bUseRatings; 
	
	/**Rating level*/
	protected int iRatingLevel; 
	
	/**General parameters handlers*/
	abstract class GeneralDefaultHandler extends DefaultHandler{
    	/**
         * Called when we start an element
         *  
         */
        public void startElement(String sUri, String s, String sQName, Attributes attributes) throws SAXException {
        	if (XML_DJ_DJ.equals(sQName)){
        		name = attributes.getValue(attributes.getIndex(XML_NAME));
        		type = attributes.getValue(attributes.getIndex(XML_TYPE));
        	}
        	else if (XML_DJ_GENERAL.equals(sQName)){
        		bUseRatings = Boolean.parseBoolean(attributes.getValue(attributes.getIndex(XML_DJ_USE_RATINGS)));
        		if (bUseRatings){
        			iRatingLevel = Integer.parseInt(attributes.getValue(attributes.getIndex(XML_DJ_RATING_LEVEL)));
        		}
        		fadeDuration = Integer.parseInt(attributes.getValue(attributes.getIndex(XML_DJ_FADE_DURATION)));
        	}
        	else{//others implementation dependant-operation
        		othersTags(sQName,attributes);
        	}
        }
        
        /**Non geenral tags operations*/
        abstract protected void othersTags(String sQname,Attributes attributes);
    };
	
	/**
	 * 
	 * @param file DJ configuration file (XML)
	 * @return the right factory
	 */
	protected static DigitalDJFactory getFactory(File file) throws Exception{
		//Parse the file to get DJ type 
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(file,new DefaultHandler(){
        	/**
             * Called when we start an element
             */
            public void startElement(String sUri, String s, String sQName, Attributes attributes) throws SAXException {
            	if (XML_DJ_DJ.equals(sQName)){
            		factoryType = attributes.getValue(attributes.getIndex(XML_TYPE));
            	}
            }
        });
        if (XML_DJ_PROPORTION_CLASS.equals(factoryType)){
        	return new DigitalDJFactoryProportionImpl();
        }
        else if (XML_DJ_TRANSITION_CLASS.equals(factoryType)){
        	return new DigitalDJFactoryTransitionImpl();
        }
		else if (XML_DJ_AMBIENCE_CLASS.equals(factoryType)){
            return new DigitalDJFactoryAmbienceImpl();
        }
        return null;
	}
	
	
	/**
	 * 
	 * @return DigitalDJ from associated factory
	 * @param file DJ file
	 */
	abstract DigitalDJ getDJ(File file) throws Exception;
		
	
}

/**
 * Proportion dj factory
 * @author Bertrand florat
 *
 */
class DigitalDJFactoryProportionImpl extends DigitalDJFactory{

	/**Intermediate styles variable used during parsing*/
	private String styles;
	
	/**Intermediate proportion variable used during parsing*/
	private float proportion;
	
	private HashSet<Proportion> proportions = new HashSet();	
	
	@Override
	DigitalDJ getDJ(File file) throws Exception{
		//Parse XML file to populate the DJ
		DefaultHandler handler = new GeneralDefaultHandler() {

			@Override
			protected void othersTags(String sQname, Attributes attributes) {
				if (XML_DJ_PROPORTION.equals(sQname)){
					styles = attributes.getValue(attributes.getIndex(XML_DJ_STYLES));
					proportion = Float.parseFloat(attributes.getValue(attributes.getIndex(XML_DJ_VALUE)));
					StringTokenizer st = new StringTokenizer(styles,",");
					HashSet<Style> styles = new HashSet();
					while (st.hasMoreTokens()){
						styles.add((Style)StyleManager.getInstance().getItem(st.nextToken()));
					}
					proportions.add(new Proportion(styles,proportion));
				}
				
			}
		};
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(file,handler);
        ProportionDigitalDJ dj = new ProportionDigitalDJ(name);
        dj.setFadingDuration(fadeDuration);
        dj.setRatingLevel(iRatingLevel);
        dj.setUseRatings(bUseRatings);
        dj.setProportions(proportions);
        return dj;
	}
	
	/**No direct constructor*/
	DigitalDJFactoryProportionImpl(){
		
	}
}


/**
 * Ambience dj factory
 * @author Bertrand florat
 *
 */
class DigitalDJFactoryAmbienceImpl extends DigitalDJFactory{

    private Ambience ambience;    
    
    @Override
    DigitalDJ getDJ(File file) throws Exception{
        //Parse XML file to populate the DJ
        DefaultHandler handler = new GeneralDefaultHandler() {
            @Override
            protected void othersTags(String sQname, Attributes attributes) {
                if (XML_DJ_AMBIENCE.equals(sQname)){
                    String sAmbienceName = 
                        attributes.getValue(attributes.getIndex(XML_DJ_VALUE));
                    ambience = AmbienceManager.getInstance().getAmbience(sAmbienceName);
                }
            }
        };
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(file,handler);
        AmbienceDigitalDJ dj = new AmbienceDigitalDJ(name);
        dj.setFadingDuration(fadeDuration);
        dj.setRatingLevel(iRatingLevel);
        dj.setUseRatings(bUseRatings);
        dj.setAmbience(ambience);
        return dj;
    }
    
    /**No direct constructor*/
    DigitalDJFactoryAmbienceImpl(){
        
    }
}


/**
 * Transition dj factory
 * @author Bertrand florat
 *
 */
class DigitalDJFactoryTransitionImpl extends DigitalDJFactory{

	/**Intermediate transition list*/
	private ArrayList<Transition> transitions = new ArrayList(10);	
	
	@Override
	DigitalDJ getDJ(File file) throws Exception{
		//Parse XML file to populate the DJ
		DefaultHandler handler = new GeneralDefaultHandler() {

			@Override
			protected void othersTags(String sQname, Attributes attributes) {
				if (XML_DJ_TRANSITION.equals(sQname)){
					int number = Integer.parseInt(attributes.getValue(attributes.getIndex(XML_DJ_NUMBER)));
					String fromStyles = attributes.getValue(attributes.getIndex(XML_DJ_FROM));
					StringTokenizer st = new StringTokenizer(fromStyles,",");
					HashSet<Style> hsFromStyles = new HashSet();
					while (st.hasMoreTokens()){
						hsFromStyles.add((Style)StyleManager.getInstance().getItem(st.nextToken()));
					}
					String toStyles = attributes.getValue(attributes.getIndex(XML_DJ_TO));
					HashSet<Style> hsToStyles = new HashSet();
					while (st.hasMoreTokens()){
						hsToStyles.add((Style)StyleManager.getInstance().getItem(st.nextToken()));
					}
					transitions.add(new Transition(hsFromStyles,hsToStyles,number));
				}
				
			}
		};
		SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser saxParser = spf.newSAXParser();
        saxParser.parse(file,handler);
        TransitionDigitalDJ dj = new TransitionDigitalDJ(name);
        dj.setFadingDuration(fadeDuration);
        dj.setRatingLevel(iRatingLevel);
        dj.setUseRatings(bUseRatings);
        dj.setTransitions(transitions);
        return dj;
	}
	
	/**No direct constructor*/
	DigitalDJFactoryTransitionImpl(){
		
	}
}