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

import java.awt.Container;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.infonode.docking.DockingWindow;
import net.infonode.docking.RootWindow;
import net.infonode.docking.theme.SlimFlatDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewManager;

/**
 * Perspective adapter, provide default implementation for perspectives
 *
 * @author     bflorat
 * @created    15 nov. 2003
 */
public abstract class PerspectiveAdapter implements IPerspective,ITechnicalStrings {
	/** Perspective id (class)*/
	private String sID;
	/** Perspective icon path*/
	private String sIconPath;
	/** Perspective views list*/
	private ArrayList alViews = new ArrayList(10);
	/**Associated desktop pane*/
	protected JPanel jpPerspectiveContentPane;
	/**Contained by desktop pane*/
    public RootWindow rootWindow;
	
	
	
	/**
	 * Constructor
	 * @param sName
	 * @param sIconName
	 */
	public PerspectiveAdapter(){
		this.jpPerspectiveContentPane = new JPanel();
		this.jpPerspectiveContentPane.setLayout(new BoxLayout(this.jpPerspectiveContentPane,BoxLayout.Y_AXIS));
	}
	public void setDefaultViews(){
		
	}
	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#addView(org.jajuk.ui.views.IView)
	 */
	public net.infonode.docking.View addView(IView view) {
		alViews.add(view);
		net.infonode.docking.View dockingView = ViewManager.registerView(view);
		return dockingView;
	}
	/**
	 * @param viewMap
	 * @param add a view 
	 */
	public net.infonode.docking.View addViewAndPlaceIt(IView view){
	    net.infonode.docking.View dockingView = addView(view);
		DockingUtil.addWindow(dockingView,rootWindow);
		return dockingView;
    
    }
	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#removeView(org.jajuk.ui.views.IView)
	 */
	public void removeView(IView view) {
	 	ViewManager.removeView(view); // ?
		alViews.remove(view);
	}

	/* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.IPerspective#removeAllView()
     */
    public void removeAllView() {
        Iterator iterator = alViews.iterator();
        while(iterator.hasNext()){
            IView currentView = (IView)iterator.next();
            ViewManager.removeView(currentView);
        }
       alViews.clear();
       if (getContentPane().getComponentCount() > 0){
           getContentPane().removeAll();
       }
    }
	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#getID()
	 */
	public String getID() {
		return sID;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#getViews()
	 */
	public ArrayList getViews() {
		return alViews;
	}

	/**
	 * @return Returns the desktop.
	 */
	public Container getContentPane() {
		return jpPerspectiveContentPane;
	}
	
	/**
	 * toString method
	 */
	public String toString(){
		return "Perspective[name="+getID()+" description='"+getDesc()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IPerspective#getIconPath()
	 */
	public String getIconPath() {
		return sIconPath;
	}
	
	/**
	 * Set icon path
	 */
	public void setIconPath(String sIconPath) {
		this.sIconPath = sIconPath;
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IPerspective#setID(java.lang.String)
	 */
	public void setID(String sID) {
		this.sID = sID;
	}
	
    /* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.IPerspective#commit()
     */
    public void commit() throws IOException {
    }
    /* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.IPerspective#load()
     */
    public void load() throws IOException {
    }
    
    
	/**
	 * @param strFile
	 * @throws IOException Serialise the perspective
	 */
	protected  void commit(String strFile) throws IOException{
	    ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(strFile));
		rootWindow.write(oos);
		oos.close();
	}
	
	
	/**
	 * @param strFile
	 * @throws IOException Unserialise the perspective
	 */
	protected  void load(String strFile) throws IOException {
	    File  file = new File(strFile);  
        if ( file.exists() && file.canRead()){ //try to read serialized perspective, if file doesn't exist, it will be created at next shutdown
            rootWindow.read(new ObjectInputStream(new FileInputStream(strFile)));    
        }
    }
    
	/**
	 * @param viewMap
	 * @param dockingWindow create the root window and add it to desktop
	 */
	protected void setRootWindow(ViewMap viewMap,DockingWindow dockingWindow){
		rootWindow = new RootWindow(viewMap,dockingWindow);
		rootWindow.getRootWindowProperties().addSuperObject(SlimFlatDockingTheme.createRootWindowProperties());
		rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
		jpPerspectiveContentPane.add(rootWindow);
	}
	
}
