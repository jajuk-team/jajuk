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

package org.jajuk.ui.views;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JPanel;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukContainer;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Manages views
 *
 * @author     Bertrand Florat
 * @created    16 nov. 2003
 */
public class ViewManager implements ITechnicalStrings{

	/**Views*/
	static ArrayList alViews = new ArrayList(20);
	
	/**containers hashmap */
	static ArrayList alContainers = new ArrayList(20);
	
	/**DockingViews */
	static ArrayList alDockingViews = new ArrayList(20);
	
	/**Private constructor*/
	private ViewManager(){
	}
	
	public static void removeView(final IView view){
	    alContainers.remove(getContainer(view));
	    alViews.remove(view);
	    alDockingViews.remove(getDockingView(view));
	}
	/**Maintain relation view/perspective, a view can be in only one perspective*/
	public static net.infonode.docking.View registerView(final IView view){
	    final JajukContainer jc = new JajukContainer(view);
	    final net.infonode.docking.View dockingView = makeDockingView(view);
		alViews.add(view);
		alContainers.add(jc);
		alDockingViews.add(dockingView);
		
		return dockingView;
	}
	private static void addViewToPerspective(IView view){
	    if (!view.isPopulated()){
		    view.populate();
		}
		PerspectiveManager.getCurrentPerspective().addViewAndPlaceIt(view);  
	}
	public static net.infonode.docking.View makeDockingView(final IView view){
	    final JajukContainer jc = new JajukContainer(view);
	    final net.infonode.docking.View dockingView = 
			new net.infonode.docking.View(Messages.getString(view.getDesc()),Util.getIcon(ICON_LOGO_FRAME),jc);
	    return dockingView;
	}
			
	/**
	 * Notify the manager for a request ( refresh...)
	 * @param sEvent
	 * @param view
	 */
	public static void notify(String sEvent,final IView view){
		try{
			if (sEvent.equals(EVENT_VIEW_REFRESH_REQUEST)){
				if (!view.isPopulated()){
				    view.populate();
				}
				view.refresh();
			}
			else if (sEvent.equals(EVENT_VIEW_CLOSE_REQUEST)){
			    //setVisible(view,false);
			}
			else if (sEvent.equals(EVENT_VIEW_SHOW_REQUEST)){
				addViewToPerspective(view);
			}
		}catch(Exception e){
			Log.error("118",sEvent,e); //$NON-NLS-1$
			Messages.showErrorMessage("118",sEvent); //$NON-NLS-1$
		}
	}
	
	/**
	 * Notify the manager for a request ( refresh...) for all view of a type
	 * @param sEvent
	 * @param c views class
	 */
	public static void notify(String sEvent,Class c){
		Iterator it = alViews.iterator();
		while ( it.hasNext()){
			IView view = (IView)it.next();
			if ( view.getClass().equals(c) ){
				notify(sEvent,view);
			}
		}
	}
		
	/**
	 * Get the UI asscoiated with a view
	 * @param view
	 * @return
	 */
	public static JPanel getContainer(IView view){
		int index = alViews.indexOf(view);
		if(index <0)
		    return null;
		return (JPanel)alContainers.get(index);
	}
	/**
	 * Get the docking view asscoiated with a view
	 * @param view
	 * @return
	 */
	public static net.infonode.docking.View getDockingView(IView view){
		int index = alViews.indexOf(view);
		if(index <0)
		    return null;
		return (net.infonode.docking.View)alDockingViews.get(index);
	}
	/**
	 * Get a view for a given container
	 * @param c
	 * @return
	 */
	public static IView getViewByContainer(Container c){
		int index = alContainers.indexOf(c);
		if (index < 0){
			return null;
		}
		IView view = (IView)alViews.get(index);
		return view;
	}
	/**
	 * Get a view for a given docking view
	 * @param c
	 * @return
	 */
	public static IView getViewByDockingView(net.infonode.docking.View dockingView){
		int index = alDockingViews.indexOf(dockingView);
		if (index < 0){
			return null;
		}
		IView view = (IView)alViews.get(index);
		return view;
	}
				
}
