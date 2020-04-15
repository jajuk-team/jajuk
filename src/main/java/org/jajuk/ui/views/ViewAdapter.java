/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
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
 *  
 */
package org.jajuk.ui.views;

import com.vlsolutions.swing.docking.DockKey;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;

import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.widgets.JajukTable;
import org.jajuk.util.Const;
import org.jdesktop.swingx.JXPanel;

/**
 * Default implementation for views.
 */
@SuppressWarnings("serial")
public abstract class ViewAdapter extends JXPanel implements IView, Const, Comparable<IView>,
    Observer {
  /** Populated state. */
  private boolean isPopulated = false;
  /** View ID; note that a same view can be used several times in the same or in others perspectives. */
  private String sID;
  /** Associated perspective*. */
  private IPerspective perspective;
  /** Associated DockKey. */
  private final DockKey key;

  /**
   * Constructor.
   */
  public ViewAdapter() {
    super();
    // create a new DockKey (note that ID is set in setID() method)
    key = new DockKey();
    // View title - replacing blank space by the non breaking space fix the issue "Three dots in cover size #2045"
    String keyName = getDesc();
    if (keyName != null) {
      keyName = keyName.replace(" ", "\u00A0");
    }
    key.setName(keyName);
    key.setResizeWeight(0.5f);
    setOpaque(true);
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return "View[name=" + getID() + " description='" + getDesc() + "']";
  }

  /**
   * Checks if is populated.
   * 
   * @return Returns the bIsPopulated.
   */
  @Override
  public boolean isPopulated() {
    return isPopulated;
  }

  @Override
  public void setPopulated() {
    isPopulated = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent
   * )
   */
  @Override
  public void componentHidden(ComponentEvent e) {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent
   * )
   */
  @Override
  public void componentMoved(ComponentEvent e) {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent
   * )
   */
  @Override
  public void componentResized(ComponentEvent e) {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent
   * )
   */
  @Override
  public void componentShown(ComponentEvent e) {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.vlsolutions.swing.docking.Dockable#getDockKey()
   */
  @Override
  public DockKey getDockKey() {
    return key;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.vlsolutions.swing.docking.Dockable#getComponent()
   */
  @Override
  public Component getComponent() {
    return this;
  }

  /**
   * Default impl for ID.
   * 
   * @return the ID
   */
  @Override
  public String getID() {
    return sID;
  }

  /**
   * Set the view ID.
   * 
   * @param sID The new ID of this view.
   */
  @Override
  public void setID(String sID) {
    key.setKey(sID);
    this.sID = sID;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getPerspective()
   */
  @Override
  public IPerspective getPerspective() {
    return perspective;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#setPerspective(org.jajuk.ui.IPerspective)
   */
  @Override
  public void setPerspective(IPerspective perspective) {
    this.perspective = perspective;
  }

  /**
   * Compare this view with another view-object.
   * 
   * @param other The second view to compare to.
   * 
   * @return Natural order
   */
  @Override
  public int compareTo(IView other) {
    return getDesc().compareTo(other.getDesc());
  }

  /**
   * Called when the view perspective is selected.
   */
  @Override
  public void onPerspectiveSelection() {
    // required by interface, but nothing to do here...
  }

  public void cleanup() {
    // unregister any component that is still registered as observer
    cleanupRecursive(this);
  }

  /**
   * walk through the list of components and unregister any Observer to free all references.
   * 
   * @param c 
   */
  private static void cleanupRecursive(Container c) {
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component comp = c.getComponent(i);
      // unregister any Observer that is contained as Component here, e.g. JajukTable instances
      if (comp instanceof Observer) {
        ObservationManager.unregister((Observer) comp);
      }
      // iterate over all KeyListeners and remove them
      for (KeyListener key : comp.getKeyListeners()) {
        comp.removeKeyListener(key);
      }
      if (comp instanceof JajukTable) {
        // JajukTable sends TABLE_SELECTION_CHANGED with views as part of the properties. Therefore try to clean up these references 
        // here for every JajukTable that we find by sending an empty TABLE_SELECTION_CHANGED event which clears the last one that is still stored
        // in the ObservationManager
        ObservationManager.notifySync(new JajukEvent(JajukEvents.TABLE_SELECTION_CHANGED, null));
      }
      // if the component is a nested ViewAdapter (e.g. PlaylistView$PlaylistRepository, 
      // we need to do this cleanup in the nested object as well
      if (comp instanceof ViewAdapter) {
        // we also need to cleanup the RepositoryPanel
        ((ViewAdapter) comp).cleanup();
      }
      // recursively call any Container to also look at it's components
      if (comp instanceof Container) {
        cleanupRecursive((Container) comp);
      }
    }
  }
}
