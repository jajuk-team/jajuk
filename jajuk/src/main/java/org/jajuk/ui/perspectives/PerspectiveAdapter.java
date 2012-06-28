/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
package org.jajuk.ui.perspectives;

import com.vlsolutions.swing.docking.AutoHideButton;
import com.vlsolutions.swing.docking.AutoHideExpandPanel;
import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableResolver;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingContext;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.event.DockingActionCloseEvent;
import com.vlsolutions.swing.docking.event.DockingActionDockableEvent;
import com.vlsolutions.swing.docking.event.DockingActionEvent;
import com.vlsolutions.swing.docking.event.DockingActionListener;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.services.core.SessionService;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewAdapter;
import org.jajuk.ui.views.ViewFactory;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;
import org.xml.sax.SAXException;

/**
 * Perspective adapter, provide default implementation for perspectives.
 */
public abstract class PerspectiveAdapter extends DockingDesktop implements IPerspective, Const {
  /** The Constant XML_EXT.   */
  private static final String XML_EXT = ".xml";
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 698162872976536725L;
  /** Perspective id (class). */
  private final String sID;
  /** As been selected flag (workaround for VLDocking issue when saving position). */
  protected boolean bAsBeenSelected = false;

  /**
   * Constructor.
   */
  public PerspectiveAdapter() {
    super();
    this.sID = getClass().getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.perspectives.IPerspective#getID()
   */
  @Override
  public String getID() {
    return sID;
  }

  /**
   * toString method.
   * 
   * @return the string
   */
  @Override
  public String toString() {
    return "Perspective[name=" + getID() + " description='" + getDesc() + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.perspectives.IPerspective#commit()
   */
  @Override
  public void commit() throws IOException {
    // workaround for a VLDocking issue + performances
    if (!bAsBeenSelected) {
      return;
    }
    // The writeXML method must be called in the EDT to avoid freezing, it
    // requires a lock some UI components
    File saveFile = SessionService.getConfFileByPath(getClass().getSimpleName() + XML_EXT);
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile));
    try {
      writeXML(out);
      out.flush();
    } finally {
      out.close();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.perspectives.IPerspective#load()
   */
  @Override
  public void load() throws IOException, ParserConfigurationException, SAXException {
    // Try to read XML conf file from home directory
    File loadFile = SessionService.getConfFileByPath(getClass().getSimpleName() + XML_EXT);
    // If file doesn't exist (normally only at first install), read
    // perspective conf from the jar
    URL url = loadFile.toURI().toURL();
    if (!loadFile.exists()) {
      url = UtilSystem.getResource(FILE_DEFAULT_PERSPECTIVES_PATH + '/'
          + getClass().getSimpleName() + XML_EXT);
    }
    BufferedInputStream in = new BufferedInputStream(url.openStream());
    // then, load the workspace
    try {
      DockingContext ctx = new DockingContext();
      DockableResolver resolver = new DockableResolver() {
        @Override
        public Dockable resolveDockable(String keyName) {
          Dockable view = null;
          try {
            StringTokenizer st = new StringTokenizer(keyName, "/");
            String className = st.nextToken();
            int id = Integer.parseInt(st.nextToken());
            view = ViewFactory.createView(Class.forName(className), PerspectiveAdapter.this, id);
          } catch (Exception e) {
            Log.error(e);
          }
          return view;
        }
      };
      // register a listener to unregister the view upon closing
      ctx.addDockingActionListener(new DockingActionListener() {
        @Override
        public void dockingActionPerformed(DockingActionEvent dockingactionevent) {
          // on closing/removing of a view try to unregister it at the
          // ObservationManager
          if (dockingactionevent instanceof DockingActionCloseEvent) {
            Dockable obj = ((DockingActionDockableEvent) dockingactionevent).getDockable();
            if (obj instanceof Observer) {
              ObservationManager.unregister((Observer) obj);
            }
            // it seems the Docking-library does not unregister these things by itself
            // so we need to do it on our own here as well. We create the Dockable (i.e.
            // the View) from scratch every time (see constructor of JajukJMenuBar where we create
            // the menu entries to add new views and ViewFactory)
            unregisterDockable(obj);
            // workaround for DockingDesktop-leaks, we need to remove the Dockable from the
            // "TitleBar"
            // if it is one of those that are hidden on the left side.
            removeFromDockingDesktop(PerspectiveAdapter.this, obj);
            // do some additional cleanup on the View itself if necessary
            if (obj instanceof ViewAdapter) {
              ((ViewAdapter) obj).cleanup();
            }
          }
        }

        @Override
        public boolean acceptDockingAction(DockingActionEvent dockingactionevent) {
          // always accept here
          return true;
        }
      });
      ctx.setDockableResolver(resolver);
      setContext(ctx);
      ctx.addDesktop(this);
      try {
        ctx.readXML(in);
      } catch (Exception e) {
        // error parsing the file, user can't be blocked, use
        // default conf
        Log.error(e);
        Log.debug("Error parsing conf file, use defaults - " + getID());
        url = UtilSystem.getResource(FILE_DEFAULT_PERSPECTIVES_PATH + '/'
            + getClass().getSimpleName() + XML_EXT);
        in = new BufferedInputStream(url.openStream());
        ctx.readXML(in);
      }
    } finally {
      in.close(); // stream isn't closed
    }
  }

  /**
   * Helper method that performs some additional cleanup for the Dockable.
   * 
   * @param c The Container to look at, usually the DockingDesktop, i.e.
   * the PerspectiveAdapter in this case.
   * @param dockable The Dockable to remove/replace.
   */
  private static void removeFromDockingDesktop(Container c, Dockable dockable) {
    /**
     * walk through the list of components and replace the Dockable with 
     * an empty new one whereever necessary to free all references
     */
    for (int i = 0; i < c.getComponentCount(); i++) {
      Component comp = c.getComponent(i);
      // on the AutoHideExpandPanel, we need to set a new Dockable on the TitleBar
      // as it otherwise keeps the Dockable as "target"
      if (comp instanceof AutoHideExpandPanel) {
        AutoHideExpandPanel panel = (AutoHideExpandPanel) comp;
        panel.getTitleBar().setDockable(new Dockable() {
          @Override
          public DockKey getDockKey() {
            return new DockKey();
          }

          @Override
          public Component getComponent() {
            return null;
          }
        });
      }
      // the AutoHideButton points at the dockable, replace it with a new one here as well
      if (comp instanceof AutoHideButton) {
        AutoHideButton button = (AutoHideButton) comp;
        if (button.getDockable() == dockable) {
          // set an empty dockable to free up this one...
          button.init(new Dockable() {
            @Override
            public DockKey getDockKey() {
              return new DockKey();
            }

            @Override
            public Component getComponent() {
              return null;
            }
          }, button.getZone());
        }
      }
      // recursively call the Container to also look at it's components
      if (comp instanceof Container) {
        removeFromDockingDesktop((Container) comp, dockable);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.perspectives.IPerspective#getContentPane()
   */
  @Override
  public Container getContentPane() {
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IPerspective#restaureDefaults()
   */
  @Override
  public void restoreDefaults() {
    // SHOULD BE CALLED ONLY FOR THE CURRENT PERSPECTIVE
    // to ensure views are not invisible
    try {
      // Remove current conf file to force using default file from the
      // jar
      File loadFile = SessionService.getConfFileByPath(getClass().getSimpleName() + XML_EXT);
      // lazy deletion, the file can be already removed by a previous reset
      loadFile.delete();
      // Remove all registered dockables
      DockableState[] ds = getDockables();
      for (DockableState element : ds) {
        close(element.getDockable());
      }
      // force reload
      load();
      // set perspective again to force UI refresh
      PerspectiveManager.setCurrentPerspective(this);
    } catch (Exception e) {
      // display an error message
      Log.error(e);
      Messages.showErrorMessage(163);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IPerspective#setAsBeenSelected()
   */
  @Override
  public void setAsBeenSelected(boolean b) {
    bAsBeenSelected = b;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.perspectives.IPerspective#getViews()
   */
  @Override
  public Set<IView> getViews() {
    Set<IView> views = new HashSet<IView>();
    DockableState[] dockables = getDockables();
    for (DockableState element : dockables) {
      views.add((IView) element.getDockable());
    }
    return views;
  }
}
