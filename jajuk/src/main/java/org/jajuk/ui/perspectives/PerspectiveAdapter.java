/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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

package org.jajuk.ui.perspectives;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableResolver;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockingContext;
import com.vlsolutions.swing.docking.DockingDesktop;

import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;

import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.ViewFactory;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilSystem;
import org.jajuk.util.log.Log;

/**
 * Perspective adapter, provide default implementation for perspectives
 */
public abstract class PerspectiveAdapter extends DockingDesktop implements IPerspective,
    Const {
  /** Perspective id (class) */
  private String sID;

  /** Perspective icon */
  private ImageIcon icon;

  /**
   * As been selected flag (workaround for VLDocking issue when saving position)
   */
  protected boolean bAsBeenSelected = false;

  /**
   * Constructor
   * 
   * @param sName
   * @param sIconName
   */
  public PerspectiveAdapter() {
    this.sID = getClass().getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.perspectives.IPerspective#getID()
   */
  public String getID() {
    return sID;
  }

  /**
   * toString method
   */
  @Override
  public String toString() {
    return "Perspective[name=" + getID() + " description='" + getDesc() + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IPerspective#getIconPath()
   */
  public ImageIcon getIcon() {
    return icon;
  }

  /**
   * Set icon path
   */
  public void setIcon(ImageIcon icon) {
    this.icon = icon;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.perspectives.IPerspective#commit()
   */
  public void commit() throws Exception {
    // workaround for a VLDocking issue + performances
    if (!bAsBeenSelected) {
      return;
    }
    // The writeXML method must be called in the EDT to avoid freezing, it
    // requires a lock some UI components
    File saveFile = UtilSystem.getConfFileByPath(getClass().getSimpleName() + ".xml");
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(saveFile));
    writeXML(out);
    out.flush();
    out.close();
    // Now reopen the file and remove everything between <TabGroups> and
    // </TabGroups> due to a bug in VLDocking (otherwise, VLDocking force
    // creating phantom views)
    StringBuilder sb = new StringBuilder();
    BufferedReader in = new BufferedReader(new FileReader(saveFile));
    try {
      String line = null;
      boolean stop = false;
      for (;;) {
        line = in.readLine();
        if (line == null) {
          break;
        }

        if (line.indexOf("<TabGroups>") != -1) {
          stop = true;
        }
        if (line.indexOf("</TabGroups>") != -1) {
          stop = false;
        }
        if (!stop && line.indexOf("</TabGroups>") == -1) {
          sb.append(line).append('\n');
        }
      }
    } finally {
      in.close();
    }
    // write file again
    out = new BufferedOutputStream(new FileOutputStream(saveFile));
    try {
      out.write(sb.toString().getBytes());
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
  public void load() throws Exception {

    // Try to read XML conf file from home directory
    File loadFile = UtilSystem.getConfFileByPath(getClass().getSimpleName() + ".xml");
    // If file doesn't exist (normally only at first install), read
    // perspective conf from the jar
    URL url = loadFile.toURI().toURL();
    if (!loadFile.exists()) {
      url = UtilSystem.getResource(FILE_DEFAULT_PERSPECTIVES_PATH + '/'
          + getClass().getSimpleName() + ".xml");
    }
    BufferedInputStream in = new BufferedInputStream(url.openStream());
    // then, load the workspace
    DockingContext ctx = new DockingContext();
    DockableResolver resolver = new DockableResolver() {
      public Dockable resolveDockable(String keyName) {
        Dockable view = null;
        try {
          StringTokenizer st = new StringTokenizer(keyName, "/");
          String className = st.nextToken();
          view = ViewFactory.createView(Class.forName(className), PerspectiveAdapter.this);
        } catch (Exception e) {
          Log.error(e);
        }
        return view;
      }
    };
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
          + getClass().getSimpleName() + ".xml");
      in = new BufferedInputStream(url.openStream());
      ctx.readXML(in);
    } finally {
      in.close(); // stream isn't closed
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.perspectives.IPerspective#getContentPane()
   */
  public Container getContentPane() {
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IPerspective#restaureDefaults()
   */
  public void restoreDefaults() {
    // SHOULD BE CALLED ONLY FOR THE CURRENT PERSPECTIVE
    // to ensure views are not invisible
    try {
      // Remove current conf file to force using default file from the
      // jar
      File loadFile = UtilSystem.getConfFileByPath(getClass().getSimpleName() + ".xml");
      if(!loadFile.delete()) {
        Log.warn("Could not delete file " + loadFile.toString());
      }
      // Remove all registered dockables
      DockableState[] ds = getDockables();
      for (DockableState element : ds) {
        remove(element.getDockable());
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
  public void setAsBeenSelected(boolean b) {
    bAsBeenSelected = b;
  }

  public Set<IView> getViews() {
    Set<IView> views = new HashSet<IView>();
    DockableState[] dockables = getDockables();
    for (DockableState element : dockables) {
      views.add((IView) element.getDockable());
    }
    return views;
  }

}
