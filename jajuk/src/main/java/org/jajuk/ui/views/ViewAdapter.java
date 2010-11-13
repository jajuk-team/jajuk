/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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
 *  $Revision$
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
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXPanel;

/**
 * Default implementation for views.
 */
public abstract class ViewAdapter extends JXPanel implements IView, Const, Comparable<IView>,
    Observer {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1526958318911770642L;

  /** Populated state. */
  private boolean bIsPopulated = false;

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
    // View title
    key.setName(getDesc());
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
    return bIsPopulated;
  }

  /**
   * Sets the is populated.
   * 
   * @param isPopulated Defines, if this View is populated fully.
   */
  @Override
  public void setIsPopulated(boolean isPopulated) {
    bIsPopulated = isPopulated;
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

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Container#removeAll()
   */
  @Override
  public void removeAll() {
    // We have to override removeAll() to work around a memory leak related to
    // JXBusyLabel..

    // first look for any JXBusyLabel and stop it
    stopAllBusyLabels(this);

    super.removeAll();
  }

  /**
   * walk through the list of components and stop any BusyLabel.
   */
  public void stopAllBusyLabels() {
    stopAllBusyLabels(this);
  }

  /**
   * walk through the list of components and stop any BusyLabel.
   * 
   * @param c DOCUMENT_ME
   */
  private static void stopAllBusyLabels(Container c) {
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component comp = c.getComponent(i);
      if (comp instanceof JXBusyLabel) {
        JXBusyLabel busy = (JXBusyLabel) comp;
        if (busy.isBusy()) {
          // make sure we correctly stop the BusyLabel in all cases here,
          // sometimes this did not work...
          // this can probably removed after upgrading swingx, see
          // https://swingx.dev.java.net/issues/show_bug.cgi?id=626
          busy.setBusy(false);
        }
      } else if (comp instanceof Container) {
        // recursively call the Container to also look at it's components
        stopAllBusyLabels((Container) comp);
      }
    }
  }

  /**
   * Cleanup.
   * DOCUMENT_ME
   */
  public void cleanup() {
    // unregister any component that is still registered as observer
    cleanupRecursive(this);
  }

  /**
   * walk through the list of components and unregister any Observer to free all references.
   * 
   * @param c DOCUMENT_ME
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
