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
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
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
	/** Perspective name*/
	private static ArrayList alNames = new ArrayList(10);
	/**perspective  */
	private static ArrayList alPerspectives = new ArrayList(10);
	/** Self instance */
	private static PerspectiveManager pm = null;
	/** Parsing temporary variable */
	private static IPerspective pCurrent = null;
	
	private long lTime;

	private String sPerspectiveName;
	
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
			throw new JajukException("115"); //$NON-NLS-1$
		}
	}

		/**
		 * Begins management
		 */
		public static void init() {
			int index = alNames.indexOf(ConfigurationManager.getProperty(CONF_PERSPECTIVE_DEFAULT));
			IPerspective perspective = (IPerspective)alPerspectives.get(index);
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
		Util.waiting();
		/**views display */
		Iterator it = perspective.getViews().iterator();
		while ( it.hasNext()){
			IView view = (IView)it.next();
			if (!view.isDisplayed()){
				view.display();
				view.setIsDisplayed(true);
			}
		}
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
		Util.stopWaiting();
	}

	
	/** 
	 * Set current perspective
	 * @param sPerspectiveName
	 */
	public static void setCurrentPerspective(String sPerspectiveName) {
		int index = alNames.indexOf(sPerspectiveName);
		setCurrentPerspective((IPerspective)alPerspectives.get(index));
	}
	
	/**
	 * Notify the manager for  a perspective change request
	 * @param sPerspectiveName
	 */
	public static void notify(String sPerspectiveName){
		int index = alNames.indexOf(sPerspectiveName);
		IPerspective perspective = (IPerspective)alPerspectives.get(index);
		if (perspective != null){
			setCurrentPerspective(perspective);
			ConfigurationManager.setProperty(CONF_PERSPECTIVE_DEFAULT,perspective.getName());
		}
	}
	
	/**
	 * Get all perspectives
	 * @return all perspectives as a collection
	 */
	public static ArrayList getPerspectives(){
		return alPerspectives;
	}

	/**
	 * parsing warning
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void warning(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("115") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * parsing error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void error(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("115") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * parsing fatal error
	 * 
	 * @param spe
	 * @exception SAXException
	 */
	public void fatalError(SAXParseException spe) throws SAXException {
		throw new SAXException(Messages.getErrorMessage("115") + " / " + spe.getSystemId() + "/" + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	/**
	 * Called at parsing start
	 */
	public void startDocument() {
		Log.debug("Starting perspective file parsing..."); //$NON-NLS-1$
	}

	/**
	 * Called at parsing end
	 */
	public void endDocument() {
		Log.debug("Perspective file parsing done."); //$NON-NLS-1$
	}

	/**
	 * Called when we start an element
 	*/
	public void startElement(String sUri, String sName, String sQName, Attributes attributes) throws SAXException {
		String sClassName = null;
		String sIconPath = null;
		if (sQName.equals("perspective")) { //$NON-NLS-1$
			try {
				lTime = System.currentTimeMillis();
				pCurrent = null;
				sClassName = attributes.getValue(attributes.getIndex("class")); //$NON-NLS-1$
				sIconPath = attributes.getValue(attributes.getIndex("icon")); //$NON-NLS-1$
				sPerspectiveName = sClassName; //stored to be used during views parsing 
				IPerspective perspective = (IPerspective) Class.forName(sClassName).newInstance();
				perspective.setName(sClassName);
				perspective.setIconPath(sIconPath);
				alNames.add(sClassName);
				alPerspectives.add(perspective);
				pCurrent = perspective;
			} catch (Exception e) {
				Log.error("116", sClassName, e); //$NON-NLS-1$
			}
		} else if (sQName.equals("view")) { //$NON-NLS-1$
			if (pCurrent != null) {
				IView view = null;
				int iWidth = 0;
				int iHeight = 0;
				int iX = 0;
				int iY = 0;
				long l = System.currentTimeMillis();
				try {
					sClassName = attributes.getValue(attributes.getIndex("class"));  //$NON-NLS-1$
					iWidth = Integer.parseInt(attributes.getValue(attributes.getIndex("width"))); //$NON-NLS-1$
					iHeight = Integer.parseInt(attributes.getValue(attributes.getIndex("height"))); //$NON-NLS-1$
					iX = Integer.parseInt(attributes.getValue(attributes.getIndex("x"))); //$NON-NLS-1$
					iY = Integer.parseInt(attributes.getValue(attributes.getIndex("y"))); //$NON-NLS-1$
					view = (IView) Class.forName(sClassName).newInstance();
				} catch (Exception e) {
					Log.error("116", sClassName, e); //$NON-NLS-1$
					return;
				}
				pCurrent.addView(view,iWidth,iHeight,iX,iY);
				Log.debug(new StringBuffer("Registered view: ").append(attributes.getValue(0)).append(" in ").append(System.currentTimeMillis()-l).append(" ms").toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}

	/**
	 * End of an element
	 */
	public void endElement(String uri, String localName, String qName) {
		if (qName.equals("perspective")) { //$NON-NLS-1$
			pCurrent = null;
			Log.debug(new StringBuffer("Registered perspective: ").append(sPerspectiveName).append(" in: ").append(System.currentTimeMillis()-lTime).append(" ms").toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

}
