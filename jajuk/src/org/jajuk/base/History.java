/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
 * $Revision$
 **/

package org.jajuk.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Stores all files user read. Only for tracks inside collection ( not for basic files )
 * 
 * @author bflorat @created 19 nov. 2003
 */
public class History extends DefaultHandler implements ITechnicalStrings, ErrorHandler {
	/** Self instance */
	private static History history;
	
	/** History repository*/
	private static ArrayList alHistory = new ArrayList(100);
	
	/** History begin date*/
	private static  long lDateStart;
	
	/** Instance getter */
	public static History getInstance() {
		if (history == null) {
			history = new History();
		}
		return history;
	}

	/** Hidden constructor */
	private History() {
	}
	
	/** Add an history item */
	public void addItem(String sFileId,long lDate){
		HistoryItem hi = new HistoryItem(sFileId,lDate);
		alHistory.add(hi);
	}
	
	/** Clear history */
	public void clear(){
		alHistory.clear();
	}
	
	/**
	 * Write history on disk
	 *@exception IOException
	 */
	public static void commit()  throws IOException {
		if ( lDateStart == 0) {
			lDateStart = System.currentTimeMillis();
		}
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_HISTORY), "UTF-8"));
		bw.write("<?xml version='1.0' encoding='UTF-8'?>\n");
		bw.write("<history begin_date='"+Long.toString(lDateStart)+"'>\n");
		Iterator it = alHistory.iterator();
		while ( it.hasNext()){
			HistoryItem hi = (HistoryItem)it.next();
			bw.write("\t<play file='"+hi.getFileId()+"' date='"+hi.getDate()+"'/>\n");
		}
		bw.write("</history>");
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
		} catch (Exception e) {
			Log.error(e);
			throw new JajukException("119");
		}
	}
	
	/**
	 * 
	 * @return id of last played registered track or null if history is empty
	 */
	public String getLastFile(){
		if (alHistory.size() == 0){
			return null;
		}
		HistoryItem hiLast = (HistoryItem)alHistory.get(alHistory.size()); 
		return hiLast.getFileId();
	}
	
	
	/**
	 * parsing warning
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void warning(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("119") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * parsing error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void error(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("119") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * parsing fatal error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void fatalError(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("119") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * Called at parsing start
	 */
	public void startDocument() {
		Log.debug("Starting history file parsing...");
	}

	/**
	 * Called at parsing end
	 */
	public void endDocument() {
		Log.debug("History file parsing done");
	}

	/**
	 * Called when we start an element
	 *  
	 */
	public void startElement(String sUri, String sName, String sQName, Attributes attributes) throws SAXException {
		if (sQName.equals("history")){
			History.lDateStart = Long.parseLong(attributes.getValue(attributes.getIndex("begin_date")));
		}
		else if (sQName.equals("play")){
			HistoryItem hi = new HistoryItem(attributes.getValue(attributes.getIndex("file")),Long.parseLong(attributes.getValue(attributes.getIndex("date"))));
			alHistory.add(hi);
		}
	}

	/**
	 * Called when we reach the end of an element
	 */
	public void endElement(String sUri, String sName, String sQName) throws SAXException {

	}

}


/**
 * An history item
 *
 * @author     bflorat
 * @created    19 nov. 2003
 */
class HistoryItem{
	/**File Id*/
	private String sFileId;
	/**Play date*/
	private long lDate;
	
	public HistoryItem(String sFileId,long lDate){
		this.sFileId = sFileId;
		this.lDate = lDate;
	}
	
	
	/**
	 * @return Returns the date.
	 */
	public long getDate() {
		return lDate;
	}

	/**
	 * @param date The date to set.
	 */
	public void setDate(long lDate) {
		this.lDate = lDate;
	}

	/**
	 * @return Returns the sFileId.
	 */
	public String getFileId() {
		return sFileId;
	}

	/**
	 * @param fileId The sFileId to set.
	 */
	public void setFileId(String fileId) {
		sFileId = fileId;
	}

}
