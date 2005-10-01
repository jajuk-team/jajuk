/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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
package org.jajuk.i18n;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.Main;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utility class to get strings from localized property files
 *<p>Singleton</p>
 * @author     Bertrand Florat
 * @created    5 oct. 2003
 */
public class Messages extends DefaultHandler implements ITechnicalStrings	{
	/**Local ( language) to be used, default is english */
	private String sLocal = "en";  //$NON-NLS-1$
	/**Supported Locals*/
	public ArrayList alLocals = new ArrayList(10);
	/**Locals description */
	public ArrayList alDescs = new ArrayList(10);
	/**self instance for singleton*/
	private static Messages mesg;
	/**Messages themself extracted from an XML file to this properties class**/
	private Properties properties;
	/**english messages used as default**/
	private Properties propertiesEn;
	
		
	/**
	 * Private Constructor
	 */
	private Messages() {
	}
	
	/**
	 * @return Singleton instance
	 */
	public static Messages getInstance(){
	    if ( mesg == null){
	        mesg = new Messages();
	    }
	    return mesg;
	}
	
    /**
     * 
     * @param sKey
     * @return wheter given key exists
     */
    public boolean contains(String sKey){
        return getPropertiesEn().containsKey(sKey);
    }
    
	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
	    String sOut = key;
	    try{
	        sOut = getInstance().getProperties().getProperty(key); 
	        if (sOut == null){ //this property is unknown for this local, try in english
	            sOut = getInstance().getPropertiesEn().getProperty(key);
	        }
	        //at least, returned property is the key name but we trace an error to show it
	        if (sOut == null){
	            Log.error("105","key: "+key,new Exception()); //$NON-NLS-1$ //$NON-NLS-2$
	            sOut = key;
	        }
	    }catch(Exception e){ //system error
	        Log.error(e);
	    }
	    return sOut;
	}
	
	/**
	 * Register a local
	 * @param sName : local name like "english"
	 * @param sLocal : local name like "en"
	 */
	public void registerLocal(String sLocal ,String sDesc){
		alLocals.add(sLocal);
		alDescs.add(sDesc);
	}
	
	/**
	 * Return list of available locals
	 * @return
	 */
	public ArrayList getLocals(){
		return alLocals;
	}
	
	/**
	 * Return list of available locals
	 * @return
	 */
	public ArrayList getDescs(){
		return alDescs;
	}
	
	/**
	 * Change current local
	 * @param sLocal
	 */
	public void setLocal(String sLocal) throws Exception{
	    this.properties = null; //make sure to reinitialize cached strings
	    this.sLocal = sLocal;
  	}
	
	/**Parse a factice properties file inside an XML file as CDATA*
	 * @param sLocal
	 * @return a properties with all entries
	 * @throws Exception
	 */ 
	private Properties parseLangpack(String sLocal) throws Exception {
	    final Properties properties = new Properties();
	    //	  Choose right jajuk_<lang>.properties file to load
		StringBuffer sbFilename = new StringBuffer(FILE_LANGPACK_PART1);
		if (!sLocal.equals("en")){ //for english, properties file is simply jajuk.properties //$NON-NLS-1$
		    sbFilename.append('_').append(sLocal);
		}
		sbFilename.append(FILE_LANGPACK_PART2);
		String sUrl; //property file URL, either in the jajuk.jar jar (normal execution) or found as regular file if in development debug mode
		if (Main.bIdeMode){
		    sUrl = "file:"+System.getProperty("user.dir")+"/src/org/jajuk/i18n/"+ sbFilename.toString(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		else{
		    sUrl = "jar:"+Util.getExecLocation()+"!/org/jajuk/i18n/"+ sbFilename.toString(); //$NON-NLS-1$ //$NON-NLS-2$
		}
		//parse it, actually it is a big properties file as CDATA in an XML file 
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			spf.setNamespaceAware(false);
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(sUrl,new DefaultHandler() {
			    StringBuffer sb = new StringBuffer(15000); //this buffer will contain the entire properties strings 
			    //call for each element strings, actually will be called several time if the element is large (our case : large CDATA)
			    public void characters(char[] ch, int start, int length) throws SAXException {
                    sb.append(ch,start,length);
                 }
			    //call when closing the tag (</body> in our case )
			    public void endElement(String uri, String localName, String qName) throws SAXException {
			        String sWhole = sb.toString();
                    //ok, parse it ( comments start with #)
                    StringTokenizer st = new StringTokenizer(sWhole,"\n"); //$NON-NLS-1$
                    while (st.hasMoreTokens()){
                        String sLine = st.nextToken();
                        if (sLine.length()>0 && !sLine.startsWith("#") && sLine.indexOf('=')!=-1){ //$NON-NLS-1$
                            StringTokenizer stLine = new StringTokenizer(sLine,"="); //$NON-NLS-1$
                            properties.put(stLine.nextToken().trim(),stLine.nextToken()); //trim to ignore space at begin end end of lines
                        }
                    }
			    }
             });
			return properties;
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	
	/**
	 * Return the message display to the user corresponding to the error code.
	 * 
	 * @param pCode Error code.
	 * @return String Message corresponding to the error code.
	 */
	public static String getErrorMessage(String pCode) {
		String sOut = pCode;
		try{
			sOut = getString("Error." + pCode); //$NON-NLS-1$
		}
		catch(Exception e){
			Log.error("105","code: "+pCode,e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return sOut;
	}
	
	
	/**
	 * Show a dialog with specified error message
	 * @param sCode
	 */
	public static void showErrorMessage(final String sCode){
	    SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              JOptionPane.showMessageDialog(Main.getWindow(),"<html><b>"+Messages.getErrorMessage(sCode)+"</b></html>",Messages.getErrorMessage("102"),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$	}
    		}
        });
	}
	
	/**
	 * Show a dialog waiting for a user decision
	 * <p>CAUTION! the thread which calls this method musn't have locks on ressources : otherwise it can conduct to GUI freeze</p>
	 * @param sText : dialog text
	 * @param iType : dialof type : can be JOptionPane.ERROR_MESSAGE, WARNING_MESSAGE
	 */
	public static int getChoice(String sText,int iType){
	    ConfirmDialog confirm = new ConfirmDialog(sText,getTitleForType(iType),iType);
	    if (SwingUtilities.isEventDispatchThread()){ //in the dispatcher thread, no need to use invokeLatter
	        confirm.run();
	    }
	    else{ //not in the awt dispatcher thread, OK, call it in an invokeAndWait to block ui until we get user decision
	        try {
                SwingUtilities.invokeAndWait(confirm);
            } catch (InterruptedException e) {
                Log.error(e);
            } catch (InvocationTargetException e) {
                Log.error(e);
            }
	    }
	    return confirm.getResu();
	}
	
	/**
	 * 
	 * @param iType
	 * @return String for given JOptionPane message type
	 */
	static private String getTitleForType(int iType){
	    switch(iType){
	    	case JOptionPane.ERROR_MESSAGE:
	    	    	return Messages.getString("Error"); //$NON-NLS-1$
	    	case JOptionPane.WARNING_MESSAGE:
	    	    return Messages.getString("Warning"); //$NON-NLS-1$
	    	case JOptionPane.INFORMATION_MESSAGE:
	    	    return Messages.getString("Info"); //$NON-NLS-1$
	    }
	    return ""; //$NON-NLS-1$
	}
	
	
	/**
	 * Show a dialog with specified warning message
	 * @param sMessage
	 */
	public static void showWarningMessage(final String sMessage){
	    MessageDialog message = new MessageDialog(sMessage,getTitleForType(JOptionPane.WARNING_MESSAGE),JOptionPane.WARNING_MESSAGE);
	    if (SwingUtilities.isEventDispatchThread()){ //in the dispatcher thread, no need to use invokeLatter
	        message.run();
	    }
	    else{ //not in the awt dispatcher thread
	            SwingUtilities.invokeLater(message);
        }
	}
	
	/**
	 * Show a dialog with specified error message and an icon
	 * @param sMessage
	 */
	public static void showInfoMessage(final String sMessage,final Icon icon){
	    MessageDialog message = new MessageDialog(sMessage,getTitleForType(JOptionPane.INFORMATION_MESSAGE),JOptionPane.INFORMATION_MESSAGE);
	    if (SwingUtilities.isEventDispatchThread()){ //in the dispatcher thread, no need to use invokeLatter
	        message.run();
	    }
	    else{ //not in the awt dispatcher thread
	            SwingUtilities.invokeLater(message);
        }
	}
	
	/**
	 * Show a dialog with specified error message and infosup
	 * @param sCode
	 * @param sInfoSup
	 */
	public static void showErrorMessage(final String sCode,final String sInfoSup){
	    MessageDialog message = new MessageDialog(Messages.getErrorMessage(sCode)+" : "+sInfoSup,getTitleForType(JOptionPane.ERROR_MESSAGE),JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
	    if (SwingUtilities.isEventDispatchThread()){ //in the dispatcher thread, no need to use invokeLatter
	        message.run();
	    }
	    else{ //not in the awt dispatcher thread
	            SwingUtilities.invokeLater(message);
        }
	}
	
	/**
	 * Show a dialog with specified error message with infos up
	 * @param sMessage
	 * @param sInfoSup
	 */
	public static void showInfoMessage(final String sMessage,final String sInfoSup){
	    MessageDialog message = new MessageDialog(sMessage+" : "+sInfoSup,getTitleForType(JOptionPane.INFORMATION_MESSAGE),JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
	    if (SwingUtilities.isEventDispatchThread()){ //in the dispatcher thread, no need to use invokeLatter
	        message.run();
	    }
	    else{ //not in the awt dispatcher thread
	            SwingUtilities.invokeLater(message);
         }
	}

	/**
	 * Show a dialog with specified error message
	 * @param sMessage
	 */
	public static void showInfoMessage(final String sMessage){
	    MessageDialog message = new MessageDialog(sMessage,getTitleForType(JOptionPane.INFORMATION_MESSAGE),JOptionPane.INFORMATION_MESSAGE);
	    if (SwingUtilities.isEventDispatchThread()){ //in the dispatcher thread, no need to use invokeLatter
	        message.run();
	    }
	    else{ //not in the awt dispatcher thread
	            SwingUtilities.invokeLater(message);
         }
	}
	
	/**
	 * @return Returns the sLocal.
	 */
	public String getLocal() {
		return this.sLocal;
	}
	
	/**
	 * Return true if the messaging system is started, can be usefull mainly at startup by services ( like logs) using them to avoid dead locks
	 * @return
	 */
	public static boolean isInitialized(){
	    return !(mesg == null);
	}
		
    /**
     * @return Returns the properties.
     */
    public Properties getProperties() throws Exception {
        if (this.properties == null){
            this.properties = parseLangpack(this.sLocal);
        }
        return this.properties; 
    }
    
    /**
     * @return Returns the propertiesEn.
     */
    public Properties getPropertiesEn(){
        if (this.propertiesEn == null){
            try{
                this.propertiesEn = parseLangpack("en");  //$NON-NLS-1$
            }
            catch(Exception e){
                Log.error(e);
            }
        }
        return this.propertiesEn;
    }
}

/**
 * Confirmation Dialog
 * @author     Bertrand Florat
 * @created    28 nov. 2004
 */
class ConfirmDialog implements Runnable{

    /**Dialog output*/
    private int iResu = -2;
    
    /**Dialog text*/
    private String sText;
    
    /**Dialog title*/
    private String sTitle;
    
    /**dialog type*/
    private int iType;
        
    /**
     * Confirm dialog constructor
     * @param sText
     * @param sTitle
     * @param iType
     */
    ConfirmDialog(String sText,String sTitle,int iType){
        this.iType = iType;
        this.sText = sText;
        this.sTitle = sTitle;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        iResu = JOptionPane.showConfirmDialog (null, sText,sTitle, iType);
    }
        
    /**
     * 
     * @return the user option
     */
    public int getResu() {
        return iResu;
    }
}

/**
 * Message Dialog
 * @author     Bertrand Florat
 * @created    28 nov. 2004
 */
class MessageDialog implements Runnable{

     /**Dialog text*/
    private String sText;
    
    /**Dialog title*/
    private String sTitle;
    
    /**dialog type*/
    private int iType;
        
    /**
     * Message dialog constructor
     * @param sText
     * @param sTitle
     * @param iType
     */
    MessageDialog(String sText,String sTitle,int iType){
        this.iType = iType;
        this.sText = sText;
        this.sTitle = sTitle;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        JOptionPane optionPane = getNarrowOptionPane(72);
        optionPane.setMessage(sText);
        optionPane.setMessageType(iType);
        JDialog dialog = optionPane.createDialog(null,sTitle);
        dialog.setVisible(true);
    }
    
    /**
     * code from http://java.sun.com/developer/onlineTraining/new2java/supplements/2005/July05.html#1
     * Used to coorectly display long messages
     * @param maxCharactersPerLineCount
     * @return
     */
    public static JOptionPane getNarrowOptionPane(
            int maxCharactersPerLineCount) { 
        // Our inner class definition
        class NarrowOptionPane extends JOptionPane { 
            int maxCharactersPerLineCount;
            NarrowOptionPane(int maxCharactersPerLineCount) { 
                this.maxCharactersPerLineCount = maxCharactersPerLineCount;
            } 
            public int getMaxCharactersPerLineCount() { 
                return maxCharactersPerLineCount;
            } 
        } 
        return new NarrowOptionPane(maxCharactersPerLineCount);
    }
   
}
