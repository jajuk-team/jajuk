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
package org.jajuk.ui.perspectives;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDesktopPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.views.AboutView;
import org.jajuk.ui.views.AnimationView;
import org.jajuk.ui.views.CDScanView;
import org.jajuk.ui.views.CoverView;
import org.jajuk.ui.views.DeviceView;
import org.jajuk.ui.views.HelpView;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.LogicalPlaylistEditorView;
import org.jajuk.ui.views.LogicalPlaylistRepositoryView;
import org.jajuk.ui.views.LogicalTableView;
import org.jajuk.ui.views.LogicalTreeView;
import org.jajuk.ui.views.ParameterView;
import org.jajuk.ui.views.PhysicalPlaylistEditorView;
import org.jajuk.ui.views.PhysicalPlaylistRepositoryView;
import org.jajuk.ui.views.PhysicalTableView;
import org.jajuk.ui.views.PhysicalTreeView;
import org.jajuk.ui.views.StatView;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Perspectives Manager
 * 
 * @author bflorat
 * @version 1.0 @created 14 nov. 03
 */
public class PerspectiveManager  implements ITechnicalStrings {
    /** Current perspective */
    private static IPerspective currentPerspective = null;
    /** Perspective name*/
    private static ArrayList alNames = new ArrayList(10);
    /**perspective  */
    private static ArrayList alPerspectives = new ArrayList(10);
    /** Parsing temporary variable */
    private static IPerspective pCurrent = null;
    /**Date used by probe */
    private static long lTime;
    /**Temporary perspective name used when parsing*/
    private static String sPerspectiveName;
    /**Commit flag, if false, configuration will not be commited*/
    public static boolean bShouldCommit = true;
    
    
    
    /**
     * Reset registered perspectives
     *
     */
    private static void reset(){
        alNames.clear();
        alPerspectives.clear();
    }
    
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
            saxParser.parse(frt.toURL().toString(),new DefaultHandler(){
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
                            perspective.setID(sClassName);
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
                                boolean bShouldBeShown = Boolean.valueOf(attributes.getValue(attributes.getIndex("show"))).booleanValue(); //$NON-NLS-1$
                                view = (IView) Class.forName(sClassName).newInstance();
                                view.setLogicalCoord(iWidth,iHeight,iX,iY);
                                view.setShouldBeShown(bShouldBeShown);
                            } catch (Exception e) {
                                Log.error("116", sClassName, e); //$NON-NLS-1$
                                return;
                            }
                            pCurrent.addView(view);
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
            });
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
    
    
    /*
     * @see org.jajuk.ui.perspectives.IPerspectiveManager#getCurrentPerspective()
     */
    public static IPerspective getCurrentPerspective()  {
        return currentPerspective;
    }
    
    /*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setCurrentPerspective(Perspective)
	 */
public static void setCurrentPerspective(final IPerspective perspective) {
		Util.waiting();
		//views display 
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Iterator it = perspective.getViews().iterator();
				while ( it.hasNext()){
					final IView view = (IView)it.next();
					if (!view.isPopulated() && view.isShouldBeShown()){
						view.populate();
						view.setIsPopulated(true);
					}	
				}
				currentPerspective = perspective;
				JDesktopPane desktop = (JDesktopPane)perspective.getDesktop();
				JPanel jpDesktop = Main.jpDesktop;
				jpDesktop.removeAll();
				jpDesktop.add(desktop,BorderLayout.CENTER);
				jpDesktop.repaint();  //this repaint is mandatory to avoid strange paintings
				PerspectiveBarJPanel.getInstance().setActivated(perspective);
				JajukJMenuBar.getInstance().refreshViews();
				Util.stopWaiting();
			}
			
		});
	}  
    
    /** 
     * Set current perspective
     * @param sPerspectiveName
     */
    public static void setCurrentPerspective(String sPerspectiveID) {
        int index = alNames.indexOf(sPerspectiveID);
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
            ConfigurationManager.setProperty(CONF_PERSPECTIVE_DEFAULT,perspective.getID());
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
     * Saves perspectives and views position in the perspective.xml file
     */
    public static void commit() throws IOException {
        if (!bShouldCommit){
            return;
        }
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_PERSPECTIVES_CONF),"UTF-8")); //$NON-NLS-1$
        bw.write("<?xml version='1.0' encoding='UTF-8'?>\n"); //$NON-NLS-1$
        bw.write("<perspectives jajuk_version='"+JAJUK_VERSION+"'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
        Iterator it = getPerspectives().iterator();
        while (it.hasNext()){ //for each perspective
            IPerspective perspective = (IPerspective)it.next();
            bw.write("\t<perspective class='"+perspective.getClass().getName()+"' icon='"+perspective.getIconPath()+"'>\n"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
            Iterator it2 = perspective.getViews().iterator();
            while (it2.hasNext()){ //for each view in this perspective
                IView view = (IView)it2.next();
                bw.write("\t\t<view class='"+view.getClass().getName()//$NON-NLS-1$
                        + "' width='"+view.getLogicalWidth()//$NON-NLS-1$
                        +"' height='"+view.getLogicalHeight()//$NON-NLS-1$
                        +"' x='"+view.getLogicalX()//$NON-NLS-1$
						+"' y='"+view.getLogicalY()//$NON-NLS-1$
            			+"' show='"+Boolean.toString(view.isShouldBeShown())//$NON-NLS-1$
            			+"'/>\n"); //$NON-NLS-1$
            }   
            bw.write("\t</perspective>\n"); //$NON-NLS-1$
        }
        bw.write("</perspectives>"); //$NON-NLS-1$
        bw.close();
    }
    
    
    /**
     * Register default perspective configuration. Will be overwritten by perspective.xml parsing if it exists
     *
     */
    public static void registerDefaultPerspectives(){
        //reset
        reset();
        IPerspective perspective = null;
        //physical perspective
        perspective = new PhysicalPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_PHYSICAL);
        perspective.setID(PERSPECTIVE_NAME_PHYSICAL);
        perspective.addView(new AnimationView().setLogicalCoord(100,10,0,0).setShouldBeShown(false));
        perspective.addView(new PhysicalTreeView().setLogicalCoord(30,100,0,0).setShouldBeShown(true));
        perspective.addView(new PhysicalTableView().setLogicalCoord(60,70,40,0).setShouldBeShown(true));
        perspective.addView(new CoverView().setLogicalCoord(20,30,80,70).setShouldBeShown(true));
        perspective.addView(new PhysicalPlaylistRepositoryView().setLogicalCoord(10,100,30,0).setShouldBeShown(true));
        perspective.addView(new PhysicalPlaylistEditorView().setLogicalCoord(40,30,40,70).setShouldBeShown(true));
        registerPerspective(perspective);
        //Logical perspective
        perspective = new LogicalPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_LOGICAL);
        perspective.setID(PERSPECTIVE_NAME_LOGICAL);
        perspective.addView(new AnimationView().setLogicalCoord(100,10,0,0).setShouldBeShown(false));
        perspective.addView(new LogicalTreeView().setLogicalCoord(30,100,0,0).setShouldBeShown(true));
        perspective.addView(new LogicalTableView().setLogicalCoord(60,70,40,0).setShouldBeShown(true));
        perspective.addView(new CoverView().setLogicalCoord(20,30,80,70).setShouldBeShown(true));
        perspective.addView(new LogicalPlaylistRepositoryView().setLogicalCoord(10,100,30,0).setShouldBeShown(true));
        perspective.addView(new LogicalPlaylistEditorView().setLogicalCoord(40,30,40,70).setShouldBeShown(true));
        registerPerspective(perspective);
        // Player perspective
        perspective = new PlayerPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_PLAYER);
        perspective.setID(PERSPECTIVE_NAME_PLAYER);
        perspective.addView(new AnimationView().setLogicalCoord(50,100,0,0).setShouldBeShown(false));
        perspective.addView(new CoverView().setLogicalCoord(100,100,0,0).setShouldBeShown(true));
        registerPerspective(perspective);
        //Configuration perspective
        perspective = new ConfigurationPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_CONFIGURATION);
        perspective.setID(PERSPECTIVE_NAME_CONFIGURATION);
        perspective.addView(new ParameterView().setLogicalCoord(60,100,0,0).setShouldBeShown(true));
        perspective.addView(new DeviceView().setLogicalCoord(40,70,60,0).setShouldBeShown(true));
        perspective.addView(new CDScanView().setLogicalCoord(40,30,60,70).setShouldBeShown(true));
        registerPerspective(perspective);
        //Stats perspective
        perspective = new StatPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_STATISTICS);
        perspective.setID(PERSPECTIVE_NAME_STATISTICS);
        perspective.addView(new StatView().setLogicalCoord(100,100,0,0).setShouldBeShown(true));
        registerPerspective(perspective);
        //Help perspective
        perspective = new HelpPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_HELP);
        perspective.setID(PERSPECTIVE_NAME_HELP);
        perspective.addView(new HelpView().setLogicalCoord(70,100,0,0).setShouldBeShown(true));
        perspective.addView(new AboutView().setLogicalCoord(30,100,70,0).setShouldBeShown(true));
        registerPerspective(perspective);
    }
    
    /**
     * Register a new perspective
     * @param perspective
     * @return registered perspective
     */
    public static IPerspective registerPerspective(IPerspective perspective){
        alNames.add(perspective.getID());
        alPerspectives.add(perspective);
        return perspective;
    }
    
}
