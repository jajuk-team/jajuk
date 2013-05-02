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
 * While a perspective disposition has not been changed, the views disposition is loaded
 * from a default file for each perspective from the jajuk jar file.
 * Note that we cannot properly handle commit on views resize here because we don't actually control
 * how many and when resizes will occur. They can occur asynchronously after initUI() and a commit 
 * may then be catastrophic because some views would stay closed. We commit on docking events and we commit on jajuk exit to 
 * handle resizing.
 */
@SuppressWarnings("serial")
public abstract class PerspectiveAdapter extends DockingDesktop implements IPerspective, Const {
  /** Perspective id (class). */
  private final String sID;
  private boolean restoringDefaults = false;

  /**
   * Constructor.
   */
  public PerspectiveAdapter() {
    super();
    this.sID = getClass().getName();
  }

  @Override
  public synchronized void commit() throws IOException {
    if (restoringDefaults) {
      return;
    }
    try {
      // The writeXML method must be called in the EDT to avoid freezing, it
      // requires a lock some UI components
      File saveFile = SessionService.getConfFileByPath(PerspectiveAdapter.this.getClass()
          .getSimpleName() + Const.FILE_XML_EXT);
      BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile));
      try {
        writeXML(out);
        out.flush();
      } finally {
        out.close();
      }
      Log.debug("Perspective " + getID() + " commited");
    } catch (Exception e) {
      Log.error(e);
      throw new IOException(e);
    }
  }

  @Override
  public void load() throws IOException, ParserConfigurationException, SAXException {
    // Try to read XML conf file from home directory
    File loadFile = SessionService.getConfFileByPath(getClass().getSimpleName()
        + Const.FILE_XML_EXT);
    // If file doesn't exist (normally only at first install), read
    // perspective conf from the jar
    URL url = loadFile.toURI().toURL();
    if (!loadFile.exists()) {
      url = UtilSystem.getResource(FILE_DEFAULT_PERSPECTIVES_PATH + '/'
          + getClass().getSimpleName() + Const.FILE_XML_EXT);
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
      addDockableListener(ctx);
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
            + getClass().getSimpleName() + Const.FILE_XML_EXT);
        in.close();
        BufferedInputStream defaultConf = new BufferedInputStream(url.openStream());
        ctx.readXML(defaultConf);
        // Delete the corrupted file
        loadFile.delete();
      }
    } finally {
      if (in != null) {
        in.close();
      }
    }
  }

  private void addDockableListener(DockingContext ctx) {
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
          // Seems that the Docking-library does not unregister these things by itself
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
        // Save the new disposition
        try {
          commit();
        } catch (IOException e) {
          Log.error(e);
        }
      }

      @Override
      public boolean acceptDockingAction(DockingActionEvent dockingactionevent) {
        // always accept here
        return true;
      }
    });
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

  @Override
  public Container getContentPane() {
    return this;
  }

  @Override
  public void restoreDefaults() {
    // SHOULD BE CALLED ONLY FOR THE CURRENT PERSPECTIVE
    // to ensure views are not invisible
    try {
      // Disable listeners because the load() method will throw many events and we don't want
      // the perspective to be commited to an intermediate state
      restoringDefaults = true;
      // Remove current conf file to force using default file from the jar
      File loadFile = SessionService.getConfFileByPath(getClass().getSimpleName()
          + Const.FILE_XML_EXT);
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
      // Force a manual commit
      commit();
    } catch (Exception e) {
      // display an error message
      Log.error(e);
      Messages.showErrorMessage(163);
    } finally {
      restoringDefaults = false;
    }
  }

  @Override
  public Set<IView> getViews() {
    Set<IView> views = new HashSet<IView>();
    DockableState[] dockables = getDockables();
    for (DockableState element : dockables) {
      views.add((IView) element.getDockable());
    }
    return views;
  }

  @Override
  public String getID() {
    return sID;
  }

  @Override
  public String toString() {
    return "Perspective[name=" + getID() + " description='" + getDesc() + "]";
  }
}
