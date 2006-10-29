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

import java.awt.Component;
import java.awt.event.ComponentEvent;

import javax.swing.JPanel;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.IPerspective;
import org.jajuk.ui.IView;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

import com.vlsolutions.swing.docking.DockKey;

/**
 * Default implementation for views
 * 
 * @author Bertrand Florat
 * @created 15 nov. 2003
 */
public abstract class ViewAdapter extends JPanel implements IView,
	ITechnicalStrings {

    /** Populated state */
    private boolean bIsPopulated = false;

    /**
         * View ID; note that a same view can be used several times in the same
         * or in others perspectives
         */
    private String sID;

    /** Associated perspective* */
    private IPerspective perspective;

    /** Associated DockKey */
    private DockKey key;

    /**
         * Constructor
         */
    public ViewAdapter() {
	// create a new DockKey (note that ID is set in setID() method)
	key = new DockKey();
	setOpaque(true);
	// View title
	key.setName(Messages.getString(getDesc()));
	// View icon
	key.setIcon(Util.getIcon(ICON_VIEW));
    }

    /**
         * toString method
         */
    public String toString() {
	return "View[name=" + getID() + " description='" + getDesc() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    /**
         * @return Returns the bIsPopulated.
         */
    public boolean isPopulated() {
	return bIsPopulated;
    }

    /**
         * @param isDisplayed
         *                The bIsPopulated to set.
         */
    public void setIsPopulated(boolean isPopulated) {
	bIsPopulated = isPopulated;
    }

    /**
         * View refresh
         */
    public void refresh() {
	if (getComponentCount() > 0) {
	    removeAll();
	}
	initUI();
	this.revalidate();
	this.repaint();
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
         */
    public void componentHidden(ComponentEvent e) {
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
         */
    public void componentMoved(ComponentEvent e) {
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
         */
    public void componentResized(ComponentEvent e) {
    }

    /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
         */
    public void componentShown(ComponentEvent e) {
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.vlsolutions.swing.docking.Dockable#getDockKey()
         */
    public DockKey getDockKey() {
	return key;
    }

    /*
         * (non-Javadoc)
         * 
         * @see com.vlsolutions.swing.docking.Dockable#getComponent()
         */
    public Component getComponent() {
	return this;
    }

    /**
         * Activate
         */
    public void activate() {
    }

    /**
         * Default impl for ID
         */
    public String getID() {
	return sID;
    }

    /**
         * Set the view ID
         * 
         * @param sID
         */
    public void setID(String sID) {
	key.setKey(sID);
	this.sID = sID;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.jajuk.ui.IView#getPerspective()
         */
    public IPerspective getPerspective() {
	return null;
    }

    /*
         * (non-Javadoc)
         * 
         * @see org.jajuk.ui.IView#setPerspective(org.jajuk.ui.IPerspective)
         */
    public void setPerspective(IPerspective perspective) {
	this.perspective = perspective;
    }

}
