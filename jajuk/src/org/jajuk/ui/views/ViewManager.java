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

package org.jajuk.ui.views;

import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.JajukInternalFrame;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.util.log.Log;

/**
 * Manages views
 *
 * @author     bflorat
 * @created    16 nov. 2003
 */
public class ViewManager implements ITechnicalStrings,ComponentListener{

	/**View -> is visible ( Boolean ) */
	static HashMap hmViewIsVisible = new HashMap(20);
	
	/**Self instance*/
	static ViewManager vm = new ViewManager();
	
	/**Views*/
	static ArrayList alViews = new ArrayList(20);
	
	/**containers hashmap */
	static ArrayList alContainers = new ArrayList(20);
	
	/**Private constructor*/
	private ViewManager(){
	}
	
	
	/**Maintain relation view/perspective, a view can be in only one perspective*/
	public static void registerView(final IView view){
		final JajukInternalFrame ji = new JajukInternalFrame(Messages.getString(view.getDesc()),true,true,true,true);
		ji.setContentPane((ViewAdapter)view);
		ji.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		ji.addInternalFrameListener(new InternalFrameAdapter(){
			public void internalFrameClosing(InternalFrameEvent e) {
				ViewManager.notify(EVENT_VIEW_CLOSE_REQUEST,view);
				JajukJMenuBar.getInstance().refreshViews();
			}
		});
		//auto-selection behavior
		ji.getGlassPane().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
				try {
					if ( !ji.isSelected()){
						ji.setSelected(true);
				
					} 
				}catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		alViews.add(view);
		alContainers.add(ji);
		ji.addComponentListener(vm);
	}
			
	/**
	 * Notify the manager for a request ( refresh...)
	 * @param sEvent
	 * @param view
	 */
	public static void notify(String sEvent,final IView view){
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
			if ( view.getClass().equals(c) && view.isShouldBeShown()){
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
		int index = alViews.indexOf(view);
		JInternalFrame frame = (JInternalFrame)alContainers.get(index);
		frame.setSize(iWidth,iHeight);
	}
	
	/**
	 * Set location for a view
	 * @param view
	 * @param iX
	 * @param iY
	 */
	public static void setLocation(IView view,int iX,int iY){
		int index = alViews.indexOf(view);
		JInternalFrame frame = (JInternalFrame)alContainers.get(index);
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
		int index = alViews.indexOf(view);
		JInternalFrame frame = (JInternalFrame)alContainers.get(index);
		frame.setVisible(b);
		hmViewIsVisible.put(view,new Boolean(b));
	}
	
	/**
	 * Get the UI asscoiated with a view
	 * @param view
	 * @return
	 */
	public static JInternalFrame getFrame(IView view){
		int index = alViews.indexOf(view);
		return (JInternalFrame)alContainers.get(index);
	}
	
	 /* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
		IView view = getViewByContainer((Container)e.getComponent());
		if (view != null){
			view.setShouldBeShown(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
		IView view = getViewByContainer((Container)e.getComponent());
		if (view != null){
			view.setShouldBeShown(true);
		}
	}

	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {
		int iMainWidth = Main.getWindow().getWidth()- BORDER_X_SIZE; //desktop pane size in pixels
		int iMainHeight = Main.getWindow().getHeight()- BORDER_Y_SIZE; //desktop pane size in pixels
		int index = alContainers.indexOf(e.getComponent());
		if (index < 0){
			return;
		}
		IView view = getViewByContainer((Container)e.getComponent());
		if (view != null){
			int iLogicalX = bound((int)(100*(float)e.getComponent().getX()/iMainWidth),PRECISION);
			int iLogicalY = bound((int)(100*(float)e.getComponent().getY()/iMainHeight),PRECISION);
			view.setLogicalX(iLogicalX);
			view.setLogicalY(iLogicalY);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		int iMainWidth = Main.getWindow().getWidth()- BORDER_X_SIZE; //desktop pane size in pixels
		int iMainHeight = Main.getWindow().getHeight()- BORDER_Y_SIZE; //desktop pane size in pixels
		IView view = getViewByContainer((Container)e.getComponent());
		if (view != null){
			int iLogicalWidth = bound((int)(100*(float)e.getComponent().getWidth()/iMainWidth),PRECISION);
			int iLogicalHeight = bound((int)(100*(float)e.getComponent().getHeight()/iMainHeight),PRECISION);
			view.setLogicalWidth(iLogicalWidth);
			view.setLogicalHeight(iLogicalHeight);
		}
	}
	
	/**
	 * Computes a dimension in screen percent for a precision of given percents 
	 * @param i
	 * @return
	 */
	private int bound(int i,int iPrecision){
		if ( i> 100 ){
			i=100;
		}
		else if (i<0){
			i = 0;
		}
		if (i%10 < 5){
			i -= i%iPrecision;
		}
		else{
			i += (iPrecision-(i%iPrecision));
		}
		return i;
	}
	
	/**
	 * Get a view for a given container
	 * @param c
	 * @return
	 */
	public IView getViewByContainer(Container c){
		int index = alContainers.indexOf(c);
		if (index < 0){
			return null;
		}
		IView view = (IView)alViews.get(index);
		return view;
	}
				
}
