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
 */
package org.jajuk.ui;

import java.awt.BorderLayout;
import java.io.File;
import java.util.HashMap;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Perspectives Manager
 * 
 * @author bflorat
 * @version 1.0 @created 14 nov. 03
 */
public class PerspectiveManager extends DefaultHandler implements ITechnicalStrings, ErrorHandler {
	/** Current perspective */
	private static IPerspective currentPerspective = null;
	/** Perspective name -> perspective hashtable */
	private static HashMap hmPerspectives = new HashMap(5);
	/** Self instance */
	private static PerspectiveManager pm = null;
	/** Parsing temporary variable */
	private static IPerspective pCurrent = null;

	
	/**
	 * Load configuration file
	 * @throws JajukException
	 */
	public static void load() throws JajukException{
		try{
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			SAXParser saxParser = spf.newSAXParser();
			File frt = new File(FILE_PERSPECTIVES_CONF);
			saxParser.parse(frt.toURL().toString(),getInstance());
			
		} catch (Exception e) {
			Log.error(e);
			throw new JajukException("115");
		}
	}

		/**
		 * Begins management
		 */
		public static void init() {
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
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getCurrentPerspective()
	 */
	public static IPerspective getCurrentPerspective()  {
			return currentPerspective;
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setCurrentPerspective(Perspective)
	 */
	public static void setCurrentPerspective(IPerspective perspective) {
		currentPerspective = perspective;
		JDesktopPane desktop = perspective.getDesktop();
		//Effacement de l'ancien panel
		JPanel jpDesktop = Main.jpDesktop;
		jpDesktop.removeAll();
		jpDesktop.repaint();
		JPanel jpFrame = Main.jpFrame;
		jpFrame.remove(Main.jpDesktop);
		jpFrame.add(jpDesktop,BorderLayout.CENTER);
		jpFrame.repaint();
		jpDesktop.add(desktop,BorderLayout.CENTER);
		jpDesktop.repaint();
		PerspectiveBarJPanel.getInstance().setActivated(perspective);
		JajukJMenuBar.getInstance().refreshViews();
	}

	
	/** 
	 * Set current perspective
	 * @param sPerspectiveName
	 */
	public static void setCurrentPerspective(String sPerspectiveName) {
		setCurrentPerspective((IPerspective)hmPerspectives.get(sPerspectiveName));
	}
	
	/**
	 * Notify the manager for  a perspective change request
	 * @param sPerspectiveName
	 */
	public static void notify(String sPerspectiveName){
		IPerspective perspective = (IPerspective)hmPerspectives.get(sPerspectiveName);
		if (perspective != null){
			setCurrentPerspective(perspective);
			ConfigurationManager.setProperty(CONF_PERSPECTIVE_DEFAULT,perspective.getName());
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
		if (sQName.equals("perspective")) {
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
