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
 * $Log$
 * Revision 1.1  2003/11/16 17:57:18  bflorat
 * 16/11/2003
 *
  */
package org.jajuk.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.*;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Perspectives Manager
 * 
 * @author bflorat
 * @version 1.0 @created 14 nov. 03
 */
public class PerspectiveManager extends DefaultHandler implements ITechnicalStrings, ErrorHandler {
	/** Current perspective */
	private IPerspective currentPerspective = null;
	/** Perspective name -> perspective hashtable */
	private HashMap hmPerspectives = new HashMap(5);
	/** Self instance */
	private static PerspectiveManager pm = null;
	/** Parsing temporary variable */
	private IPerspective pCurrent = null;

	/**
	 * Constructor for PerspectiveManagerImpl.
	 */
	private PerspectiveManager(){
	}
	
	
	/**
	 * Load configuration file
	 * @throws JajukException
	 */
	public void load() throws JajukException{
		try{
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			XMLReader xmlr;
			SAXParser saxParser = spf.newSAXParser();
			xmlr = saxParser.getXMLReader();
			xmlr.setContentHandler(getInstance());
			xmlr.setErrorHandler(getInstance());
			File frt = new File(FILE_PERSPECTIVES_CONF);
			xmlr.parse(frt.toURL().toString());	
		} catch (Exception e) {
			Log.error(e);
			throw new JajukException("115");
		}
	}

		/**
		 * Begins management
		 */
		public void init() {
			IPerspective perspective = (IPerspective)hmPerspectives.get(ConfigurationManager.getProperty(CONF_PERSPECTIVE_DEFAULT));
			setCurrentPerspective(perspective);
		}
	
	
	/**
	 * Return Singleton
	 * 
	 * @return
	 */
	public static PerspectiveManager getInstance() {
		if (pm == null) {
			pm = new PerspectiveManager();
		}
		return pm;
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getPerspectives()
	 */
	public ArrayList getPerspectives() {
		return null;
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getCurrentPerspective()
	 */
	public IPerspective getCurrentPerspective() throws JajukException {
		if (currentPerspective == null) {
			// Current perspective creation
			String perspName = ConfigurationManager.getProperty(CONF_PERSPECTIVE_DEFAULT); //$NON-NLS-1$
			try {
				setCurrentPerspective((IPerspective) Class.forName(perspName).newInstance());
			} catch (Exception e) {
				JajukException je = new JajukException("003", perspName, e); //$NON-NLS-1$
				throw je;
			}
		}

		return currentPerspective;
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setCurrentPerspective(Perspective)
	 */
	public void setCurrentPerspective(IPerspective perspective) {
		currentPerspective = perspective;
		Main.jframe.getContentPane().add(perspective.getDesktop());
		perspective.getDesktop().repaint();
		PerspectiveBarJPanel.getInstance().setActivated(perspective);
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getPerspective(String)
	 */
	public IPerspective getPerspective(String pName) {
		return null;
	}
	
	/**
	 * Notify the manager for  a perspective change request
	 * @param sPerspectiveName
	 */
	public void notify(String sPerspectiveName){
		IPerspective perspective = (IPerspective)hmPerspectives.get(sPerspectiveName);
		if (perspective != null){
			setCurrentPerspective(perspective);
		}
	}

	/**
	 * parsing warning
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void warning(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("115") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * parsing error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void error(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("115") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * parsing fatal error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void fatalError(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("115") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
	}

	/**
	 * Called at parsing start
	 */
	public void startDocument() {
		Log.debug("Starting perspective file parsing...");
	}

	/**
	 * Called at parsing end
	 */
	public void endDocument() {
		Log.debug("Perspective file parsing done.");
	}

	/**
	 * Called when we start an element
 	*/
	public void startElement(String sUri, String sName, String sQName, Attributes attributes) throws SAXException {
		String sClassName = null;
		if (sQName.equals("perspectives")) {
		} else if (sQName.equals("perspective")) {
			try {
				pCurrent = null;
				 sClassName = attributes.getValue(attributes.getIndex("class")); 
				IPerspective perspective = (IPerspective) Class.forName(sClassName).newInstance();
				hmPerspectives.put(sClassName, perspective);
				Log.debug("Registered perspective: "+sClassName);
				pCurrent = perspective;
			} catch (Exception e) {
				Log.error("116", sClassName, e);
			}
		} else if (sQName.equals("view")) {
			if (pCurrent != null) {
				IView view = null;
				int iWidth = 0;
				int iHeight = 0;
				int iX = 0;
				int iY = 0;
				try {
					sClassName = attributes.getValue(attributes.getIndex("class")); 
					iWidth = Integer.parseInt(attributes.getValue(attributes.getIndex("width")));
					iHeight = Integer.parseInt(attributes.getValue(attributes.getIndex("height")));
					iX = Integer.parseInt(attributes.getValue(attributes.getIndex("x")));
					iY = Integer.parseInt(attributes.getValue(attributes.getIndex("y")));
					view = (IView) Class.forName(sClassName).newInstance();
				} catch (Exception e) {
					Log.error("116", sClassName, e);
					return;
				}
				pCurrent.addView(view,iWidth,iHeight,iX,iY);
				Log.debug("Registered view: "+attributes.getValue(0));
			}

		}
	}

	/**
	 * End of an element
	 */
	public void endElement(String uri, String localName, String qName) {
		if (qName.equals("perspective")) {
			pCurrent = null;
		}

	}

}
