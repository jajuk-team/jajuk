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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.log.Log;

/**
 * Manages views
 *
 * @author     bflorat
 * @created    16 nov. 2003
 * TODO : TBI : Automatic resizing of views when closing/opening with menu bar
 */
public class ViewManager implements ITechnicalStrings{

	/**View -> is visible ( Boolean ) */
	static HashMap hmViewIsVisible = new HashMap(20);
	
	
	/**View -> container hashmap */
	static HashMap hmViewContainer = new HashMap(20);
	
	
	/**Maintain relation view/perspective, a view can be in only one perspective*/
	public static void registerView(final IView view){
		final JInternalFrame ji = new JInternalFrame(view.getDesc(),true,true,true,true);
		ji.setContentPane((JComponent)view);
		ji.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		ji.addInternalFrameListener(new InternalFrameAdapter(){
			public void internalFrameClosing(InternalFrameEvent e) {
				ViewManager.notify(EVENT_VIEW_CLOSE_REQUEST,view);
				JajukJMenuBar.getInstance().refreshViews();
			}
		});
		//auto-selection behavior
		ji.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {
				try {
					ji.setSelected(true);
				} catch (PropertyVetoException e1) {
					Log.error(e1);
				}
			}
		});
		hmViewContainer.put(view,ji);
	}
	
	
	
	/**
	 * Notify the manager for a request ( refresh...)
	 * @param sEvent
	 * @param view
	 */
	public static void notify(String sEvent,IView view){
		try{
			if (sEvent.equals(EVENT_VIEW_REFRESH_REQUEST)){
				view.refresh();
			}
			else if (sEvent.equals(EVENT_VIEW_CLOSE_REQUEST)){
				setVisible(view,false);
			}
			else if (sEvent.equals(EVENT_VIEW_SHOW_REQUEST)){
				setVisible(view,true);
			}
		}catch(Exception e){
			Log.error("118",sEvent,e);
			Messages.showErrorMessage("118",sEvent);
		}
	}
	
	/**
	 * Notify the manager for a request ( refresh...) for all view of a type
	 * @param sEvent
	 * @param c views class
	 */
	public static void notify(String sEvent,Class c){
		Iterator it = hmViewContainer.keySet().iterator();
		while ( it.hasNext()){
			IView view = (IView)it.next();
			if ( view.getClass().equals(c)){
				notify(sEvent,view);
			}
		}
	}
	
	
	/**
	 * Set size for a view
	 * @param view
	 * @param iWidth
	 * @param iHeight
	 */
	public static void setSize(IView view,int iWidth,int iHeight){
		JInternalFrame frame = (JInternalFrame)hmViewContainer.get(view);
		frame.setSize(iWidth,iHeight);
	}
	
	/**
	 * Set location for a view
	 * @param view
	 * @param iX
	 * @param iY
	 */
	public static void setLocation(IView view,int iX,int iY){
		JInternalFrame frame = (JInternalFrame)hmViewContainer.get(view);
		frame.setLocation(iX,iY);
	}
	
	/**
	 *Return visible state for a view
	 * @param view
	 * @return
	 */
	public static boolean isVisible(IView view){
			return ((Boolean)hmViewIsVisible.get(view)).booleanValue();
	}
	
	/**
	 * Set a view visible
	 * @param view
	 * @param b
	 */
	public static void setVisible(IView view,boolean b){
		JInternalFrame frame = (JInternalFrame)hmViewContainer.get(view);
		frame.setVisible(b);
		hmViewIsVisible.put(view,new Boolean(b));
	}
	
	/**
	 * Get the UI asscoiated with a view
	 * @param view
	 * @return
	 */
	public static JInternalFrame getFrame(IView view){
		return (JInternalFrame)hmViewContainer.get(view);
	}
	

}
