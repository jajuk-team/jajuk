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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.views.IView;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

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
        registerDefaultPerspectives();
        try{
            Iterator it = getPerspectives().iterator();
            while (it.hasNext()){ //for each perspective
                IPerspective perspective = (IPerspective)it.next();
                perspective.load();
            } 
        } catch (Exception e) {
            Log.error(e);
            throw new JajukException("115"); //$NON-NLS-1$
        }
    }
    
    /**
     * Begins management
     */
    public static void init() {
        String sPerspective = Main.getDefaultPerspective();  //take a look to see if a default perspective is set (About tray for exemple)
        if (sPerspective == null){
            sPerspective = ConfigurationManager.getProperty(CONF_PERSPECTIVE_DEFAULT); //no? take the configuration ( user last perspective)
        }
        int index = alNames.indexOf(sPerspective);
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
					if (!view.isPopulated() ){
						view.populate();
						view.setIsPopulated(true);
					}	
				}
				currentPerspective = perspective;
				if (Main.jpContentPane.getComponentCount() > 0 ){
				    Main.jpContentPane.removeAll();
				}
				Main.jpContentPane.add(perspective.getContentPane(),BorderLayout.CENTER);
				Main.jpContentPane.revalidate();
				Main.jpContentPane.repaint();
				PerspectiveBarJPanel.getInstance().setActivated(perspective);
			}
		});
		Util.stopWaiting();
	}  
    
    /** 
     * Set current perspective
     * @param sPerspectiveName
     */
    public static void setCurrentPerspective(String sPerspectiveID) {
        int index = alNames.indexOf(sPerspectiveID);
        if (index != -1){
            setCurrentPerspective((IPerspective)alPerspectives.get(index));
        }
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
        Iterator it = getPerspectives().iterator();
        while (it.hasNext()){ //for each perspective
            IPerspective perspective = (IPerspective)it.next();
            perspective.commit();
        }
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
        perspective.setDefaultViews();
        registerPerspective(perspective);
        
        //Logical perspective
        perspective = new LogicalPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_LOGICAL);
        perspective.setID(PERSPECTIVE_NAME_LOGICAL);
        perspective.setDefaultViews();
        registerPerspective(perspective);
        
        // Player perspective
        perspective = new PlayerPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_PLAYER);
        perspective.setID(PERSPECTIVE_NAME_PLAYER);
        perspective.setDefaultViews();
        registerPerspective(perspective);
        
        //Configuration perspective
        perspective = new ConfigurationPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_CONFIGURATION);
        perspective.setID(PERSPECTIVE_NAME_CONFIGURATION);
        perspective.setDefaultViews();
        registerPerspective(perspective);
        
        //Stats perspective
        perspective = new StatPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_STATISTICS);
        perspective.setID(PERSPECTIVE_NAME_STATISTICS);
        perspective.setDefaultViews();
        registerPerspective(perspective);
        
        //Help perspective
        perspective = new HelpPerspective();
        perspective.setIconPath(ICON_PERSPECTIVE_HELP);
        perspective.setID(PERSPECTIVE_NAME_HELP);
        perspective.setDefaultViews();
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
