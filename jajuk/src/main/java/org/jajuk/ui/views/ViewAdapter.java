/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import com.vlsolutions.swing.docking.DockKey;

import java.awt.Component;
import java.awt.event.ComponentEvent;

import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.util.ITechnicalStrings;
import org.jdesktop.swingx.JXPanel;

/**
 * Default implementation for views
 */
public abstract class ViewAdapter extends JXPanel implements IView, ITechnicalStrings, Comparable<IView> {

  /** Populated state */
  private boolean bIsPopulated = false;

  /**
   * View ID; note that a same view can be used several times in the same or in
   * others perspectives
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
    // View title
    key.setName(getDesc());
    key.setResizeWeight(0.5f);
    setOpaque(true);
  }

  /**
   * toString method
   */
  public String toString() {
    return "View[name=" + getID() + " description='" + getDesc() + "']";
  }

  /**
   * @return Returns the bIsPopulated.
   */
  public boolean isPopulated() {
    return bIsPopulated;
  }

  /**
   * @param isDisplayed
   *          The bIsPopulated to set.
   */
  public void setIsPopulated(boolean isPopulated) {
    bIsPopulated = isPopulated;
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
    return perspective;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#setPerspective(org.jajuk.ui.IPerspective)
   */
  public void setPerspective(IPerspective perspective) {
    this.perspective = perspective;
  }

  /**
   * 
   * @return Natural order
   */
  public int compareTo(IView other) {
    return getDesc().compareTo((other).getDesc());
  }

  /**
   * Called when the view perspective is selected
   */
  public void onPerspectiveSelection() {

  }

}
