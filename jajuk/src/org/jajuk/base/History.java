/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 **/

package org.jajuk.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Stores all files user read. Only for tracks inside collection ( not for basic files )
 * @author bflorat 
 * @created 19 nov. 2003
 */
public class History extends DefaultHandler implements ITechnicalStrings, ErrorHandler,Observer {
	/** Self instance */
	private static History history;
	
	/** History repository, last play first*/
	private static ArrayList alHistory = new ArrayList(100);
	
	/** History begin date*/
	private static  long lDateStart;
	
	/** Instance getter */
	public static synchronized History getInstance() {
		if (history == null) {
			history = new History();
		}
		return history;
	}

	/** Hidden constructor */
	private History() {
	    ObservationManager.register(EVENT_FILE_LAUNCHED,this);
	    ObservationManager.register(EVENT_PLAY_ERROR,this);
	    //check if something has alredy started
	    if (ObservationManager.getDetail(EVENT_FILE_LAUNCHED,DETAIL_CURRENT_FILE_ID) != null &&
	            ObservationManager.getDetail(EVENT_FILE_LAUNCHED,DETAIL_CURRENT_DATE) != null){
	        update(EVENT_FILE_LAUNCHED);
	    }
	}
	
	/** Add an history item */
	public synchronized void addItem(String sFileId,long lDate){
		if ( ConfigurationManager.getProperty(CONF_HISTORY).equals("0")){  //no history //$NON-NLS-1$
			return ;
		}
		//check the ID maps an existing file
		if (FileManager.getFile(sFileId) == null){
		    return;
		}
		//OK, begin to add the new history item
		HistoryItem hi = new HistoryItem(sFileId,lDate);
		//check if previous history item is not the same, otherwise, leave
		if (alHistory.size() > 0){
		    HistoryItem hiPrevious = (HistoryItem)alHistory.get(0);
		    if (hiPrevious.getFileId().equals(hi.getFileId())){
		        return;
		    }
		}
		alHistory.add(0,hi);
		Properties pDetails = new Properties();
		pDetails.put(DETAIL_HISTORY_ITEM,hi);
		ObservationManager.notify(EVENT_ADD_HISTORY_ITEM,pDetails);
	}
	
	/** Clear history */
	public synchronized void clear(){
		alHistory.clear();
	}
	
	/** Clear history for all history items before iDays days*/
	public synchronized void clear(int iDays){
		//Begins by clearing deleted files
		Iterator it = alHistory.iterator();
		while (it.hasNext()){
			HistoryItem hi = (HistoryItem)it.next();
			if (hi.toString() == null ){
				it.remove();
			}
		}
		//Follow day limits
		if (iDays == -1){ //infinite history
			return;
		}
		it = alHistory.iterator();
		while (it.hasNext()){
			HistoryItem hi = (HistoryItem)it.next();
			if (hi.getDate() < (System.currentTimeMillis()- (iDays*86400000))){
				it.remove();
			}
		}
		
	}
	
	
	/**
	 * Write history on disk
	 *@exception IOException
	 */
	public static void commit()  throws IOException {
		if ( lDateStart == 0) {
			lDateStart = System.currentTimeMillis();
		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_HISTORY), "UTF-8")); //$NON-NLS-1$
		bw.write("<?xml version='1.0' encoding='UTF-8'?>\n"); //$NON-NLS-1$
		bw.write("<history jajuk_version='"+JAJUK_VERSION+"' begin_date='"+Long.toString(lDateStart)+"'>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		Iterator it = alHistory.iterator();
		while ( it.hasNext()){
			HistoryItem hi = (HistoryItem)it.next();
			bw.write("\t<play file='"+hi.getFileId()+"' date='"+hi.getDate()+"'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		bw.write("</history>"); //$NON-NLS-1$
		bw.flush();
		bw.close();
	}
	
	/**
	 * Read history from disk
	 *@exception JajukException
	 */
	public static void load() throws JajukException{
		try{
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			SAXParser saxParser = spf.newSAXParser();
			File frt = new File(FILE_HISTORY);
			saxParser.parse(frt.toURL().toString(),getInstance());
			getInstance().clear(Integer.parseInt(ConfigurationManager.getProperty(CONF_HISTORY))); //delete old history items
		} catch (Exception e) {
			Log.error(e);
			throw new JajukException("119"); //$NON-NLS-1$
		}
	}
	
	/**
	 * 
	 * @return id of last played registered track or null if history is empty
	 */
	public synchronized String getLastFile(){
		HistoryItem hiLast = null;
		if (alHistory.size() == 0){
			return null;
		}
		Iterator it = alHistory.iterator();
		while (it.hasNext()){
			hiLast = (HistoryItem)it.next();
			org.jajuk.base.File file = FileManager.getFile(hiLast.getFileId());
			if ( file!= null ){
				break;
			}
		}
		return hiLast.getFileId();
	}
	
	/**
	 * Return the history item by index
	 * @param index
	 * @return
	 */
	public synchronized HistoryItem getHistoryItem(int index){
		return (index>= 0? (HistoryItem)alHistory.get(index):null);
	}
	
	
	/**
	 * parsing warning
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void warning(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("119") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * parsing error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void error(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("119") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * parsing fatal error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void fatalError(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("119") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * Called at parsing start
	 */
	public void startDocument() {
		Log.debug("Starting history file parsing..."); //$NON-NLS-1$
	}

	/**
	 * Called at parsing end
	 */
	public void endDocument() {
		Log.debug("History file parsing done"); //$NON-NLS-1$
	}

	/**
	 * Called when we start an element
	 *  
	 */
	public void startElement(String sUri, String sName, String sQName, Attributes attributes) throws SAXException {
		if (sQName.equals("history")){ //$NON-NLS-1$
			History.lDateStart = Long.parseLong(attributes.getValue(attributes.getIndex("begin_date"))); //$NON-NLS-1$
		}
		else if (sQName.equals("play")){ //$NON-NLS-1$
			String sID = attributes.getValue(attributes.getIndex("file")); //$NON-NLS-1$
			//test if this fiel is still kwown int the collection
			if (FileManager.getFile(sID) != null){
			    HistoryItem hi = new HistoryItem(sID,Long.parseLong(attributes.getValue(attributes.getIndex("date")))); //$NON-NLS-1$ //$NON-NLS-2$
			    alHistory.add(hi);
			}
		}
	}

	/**
	 * Called when we reach the end of an element
	 */
	public void endElement(String sUri, String sName, String sQName) throws SAXException {

	}

	/**
	 * @return Returns the alHistory.
	 */
	public ArrayList getHistory() {
		return alHistory;
	}

    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
	public void update(String subject) {
	    try {
	        if (subject.equals(EVENT_FILE_LAUNCHED)){
	            String sFileID = (String)ObservationManager.getDetail(EVENT_FILE_LAUNCHED,DETAIL_CURRENT_FILE_ID);
	            long lDate =( (Long)ObservationManager.getDetail(EVENT_FILE_LAUNCHED,DETAIL_CURRENT_DATE)).longValue();
	            addItem(sFileID,lDate);
	        }
	        else if (subject.equals(EVENT_PLAY_ERROR)){
	            if (alHistory.size()>0){
	                alHistory.remove(0);
	            }
	        }
	    }
	    catch(Exception e){
	        Log.error(e);
	        return;
	    }
	}

}
