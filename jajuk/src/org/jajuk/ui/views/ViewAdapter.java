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

import java.awt.Dimension;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import org.jajuk.util.ITechnicalStrings;

/**
 *  Default implementation for views
 *
 * @author     bflorat
 * @created    15 nov. 2003
 */
public abstract class ViewAdapter extends JPanel implements IView,ITechnicalStrings {
	
	/**Displayed state */
	private boolean bIsPopulated = false;
	private static final Dimension d = new Dimension(0,0);
	/**
	 * Constructor
	 */
	public ViewAdapter()  {
		super();
		setOpaque(true);
	}
	
	/**
	 * toString method
	 */
	public String toString(){
		return "View[name="+getID()+" description='"+getDesc()+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
		
	/**
	 * @return Returns the bIsPopulated.
	 */
	public boolean isPopulated() {
		return bIsPopulated;
	}
	
	/**
	 * @param isDisplayed The bIsPopulated to set.
	 */
	public void setIsPopulated(boolean isPopulated) {
		bIsPopulated = isPopulated;
	}
	
	/**
	 * View refresh
	 */
	public void refresh(){
	    if (getComponentCount() > 0){
	        removeAll();
	    }
	    populate();
		this.revalidate();
		this.repaint();
	}
	
	
	/* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    public void componentHidden(ComponentEvent e) {
    }


    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    public void componentMoved(ComponentEvent e) {
    }


    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    public void componentResized(ComponentEvent e) {
    }


    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    public void componentShown(ComponentEvent e) {
    }
	
      
    public Dimension getMinimumSize(){
        return d;
    }

    
}
