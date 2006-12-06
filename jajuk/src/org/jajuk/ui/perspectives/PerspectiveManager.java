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
package org.jajuk.ui.perspectives;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.IPerspective;
import org.jajuk.ui.IView;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.browser.BrowserEngineManager;
import org.jdesktop.jdic.browser.WebBrowser;

import com.vlsolutions.swing.toolbars.ToolBarContainer;
import com.vlsolutions.swing.toolbars.ToolBarPanel;

/**
 * Perspectives Manager
 * 
 * @author Bertrand Florat
 * @version 1.0
 * @created 14 nov. 03
 */
public class PerspectiveManager implements ITechnicalStrings {
	/** Current perspective */
	private static IPerspective currentPerspective = null;

	/** Perspective name -> perspective */
	private static HashMap<String, IPerspective> hmNameInstance = new HashMap<String, IPerspective>(
			10);

	/** perspective */
	private static Set<IPerspective> perspectives = new LinkedHashSet<IPerspective>(
			10);

	/** Date used by probe */
	private static long lTime;

	/** Temporary perspective name used when parsing */
	private static String sPerspectiveName;

	/**
	 * Reset registered perspectives
	 * 
	 */
	private static void reset() {
		perspectives.clear();
		hmNameInstance.clear();
	}

	/**
	 * Load configuration file
	 * 
	 * @throws JajukException
	 */
	public static void load() throws JajukException {
		// si fichiers existent, decouverte dynamique
		// si aucun fichier ou si nouvelle version majeure, lecture des fichiers
		// defaut du jar
		registerDefaultPerspectives();
		if (Main.isUpgradeDetected()) {
			// upgrade message
			Messages.showInfoMessage(Messages.getString("Note.0")); //$NON-NLS-1$
			// force loadinf of defaults perspectives
			for (IPerspective perspective : getPerspectives()) {
				// Remove current conf file to force using default file from the
				// jar
				File loadFile = new File(FILE_JAJUK_DIR + '/'
						+ perspective.getClass().getName() + ".xml");
				if (loadFile.exists()) {
					loadFile.delete();
				}
			}
		}
		// Load each perspective
		try {
			for (IPerspective perspective : getPerspectives()) {
				perspective.load();
			}
		} catch (Exception e) {
			throw new JajukException("108", e); //$NON-NLS-1$
		}
	}

	/**
	 * Begins management
	 */
	public static void init() {
		// Use physical perspective as a default
		IPerspective perspective = hmNameInstance
				.get(PERSPECTIVE_NAME_PHYSICAL);
		//If it is a crash recover, force physical perspective to avoid
		//being locked on a buggy perspecive like Information
		if (!Main.isCrashRecover()) {
			String sPerspective = Main.getDefaultPerspective();
			/*
			 * take a look to see if a default perspective is set (About tray
			 * for exemple)
			 */
			if (sPerspective == null) {
				sPerspective = ConfigurationManager
						.getProperty(CONF_PERSPECTIVE_DEFAULT);
				// no? take the configuration ( user last perspective)
			}
			perspective = hmNameInstance.get(sPerspective);
			// If perspective is no more known, take first perspective found
			if (perspective == null) {
				perspective = perspectives.iterator().next();
			}
		}
		setCurrentPerspective(perspective);
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#getCurrentPerspective()
	 */
	public static IPerspective getCurrentPerspective() {
		return currentPerspective;
	}

	/*
	 * @see org.jajuk.ui.perspectives.IPerspectiveManager#setCurrentPerspective(Perspective)
	 */
	public static void setCurrentPerspective(final IPerspective perspective) {
		Util.waiting();
		// views display
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				perspective.setAsBeenSelected(true);
				for (IView view : perspective.getViews()) {
					if (!view.isPopulated()) {
						view.initUI();
						view.setIsPopulated(true);
					} else {
						// view already populated, should be activated
						view.activate();
					}
				}
				currentPerspective = perspective;
				ToolBarContainer tbcontainer = Main.getToolbarContainer();
				// Remove all non-toolbar items
				if (tbcontainer.getComponentCount() > 0) {
					Component[] components = tbcontainer.getComponents();
					for (int i = 0; i < components.length; i++) {
						if (!(components[i] instanceof ToolBarPanel)) {
							tbcontainer.remove(components[i]);
						}
					}
				}
				tbcontainer.add(perspective.getContentPane(),
						BorderLayout.CENTER);
				// refresh UI
				tbcontainer.revalidate();
				tbcontainer.repaint();
				// Select correct item in perspective selector
				PerspectiveBarJPanel.getInstance().setActivated(perspective);
				// store perspective selection
				ConfigurationManager.setProperty(CONF_PERSPECTIVE_DEFAULT,
						perspective.getID());
				Util.stopWaiting();
			}
		});
	}

	/**
	 * Set current perspective
	 * 
	 * @param sPerspectiveName
	 */
	public static void setCurrentPerspective(String sPerspectiveID) {
		IPerspective perspective = hmNameInstance.get(sPerspectiveID);
		if (perspective == null) {
			perspective = perspectives.iterator().next();
		}
		setCurrentPerspective(perspective);
	}

	/**
	 * Get all perspectives
	 * 
	 * @return all perspectives as a collection
	 */
	public static Set<IPerspective> getPerspectives() {
		return perspectives;
	}

	/**
	 * Get a perspective by ID or null if none associated perspective found
	 * 
	 * @param sID
	 *            perspective ID
	 * @return pespective
	 */
	public static IPerspective getPerspective(String sID) {
		return hmNameInstance.get(sID);
	}

	/**
	 * Saves perspectives and views position in the perspective.xml file
	 */
	public static void commit() throws Exception {
		for (IPerspective perspective : getPerspectives()) {
			perspective.commit();
		}
	}

	/**
	 * Register default perspective configuration. Will be overwritten by
	 * perspective.xml parsing if it exists
	 * 
	 */
	public static void registerDefaultPerspectives() {
		reset();

		IPerspective perspective = null;
		// physical perspective
		perspective = new PhysicalPerspective();
		perspective.setIconPath(ICON_PERSPECTIVE_PHYSICAL);
		perspective.setID(PERSPECTIVE_NAME_PHYSICAL);
		registerPerspective(perspective);

		// Logical perspective
		perspective = new LogicalPerspective();
		perspective.setIconPath(ICON_PERSPECTIVE_LOGICAL);
		perspective.setID(PERSPECTIVE_NAME_LOGICAL);
		registerPerspective(perspective);

		// Player perspective
		perspective = new PlayerPerspective();
		perspective.setIconPath(ICON_PERSPECTIVE_PLAYER);
		perspective.setID(PERSPECTIVE_NAME_PLAYER);
		registerPerspective(perspective);

		// Catalog perspective
		perspective = new CatalogPerspective();
		perspective.setIconPath(ICON_PERSPECTIVE_CATALOG);
		perspective.setID(PERSPECTIVE_NAME_CATALOG);
		registerPerspective(perspective);

		// Information perspective
		// Load info perspective only for windows or x86 linux
		if (Util.isUnderWindows()) {
			// No need to test, we are sure to find IE under windows
			perspective = new InfoPerspective();
			perspective.setIconPath(ICON_PERSPECTIVE_INFORMATION);
			perspective.setID(PERSPECTIVE_NAME_INFO);
			registerPerspective(perspective);
			// force using IE under Windows to avoid freezes
			BrowserEngineManager.instance().setActiveEngine(
					BrowserEngineManager.IE);
		} else if (Util.isUnderLinux()
				&& System.getProperty("os.arch").equals("i386")) {
			try {
				/*
				 * Check mozilla executable is available in the PATH (exec()
				 * method uses PATH natively). Don't autorize to install this
				 * perspective if mozilla is not present as it can causes
				 * freezes
				 */
				Process proc = Runtime.getRuntime().exec(
						new String[] { "mozilla", "--version" });
				int out = proc.waitFor();
				/*
				 * mozilla available ? / 0 return code means mozilla is found
				 * and is a binary / 1 return code means mozilla is found but
				 * cannot be executed under JNLP because it is a sh script but
				 * we don't care it cannot be executed, we just test its
				 * presence
				 */
				if (out == 0 || out == 1) {
					// Now check browser can actually be loaded by JDIC
					WebBrowser browser = new WebBrowser();
					if (browser.getBrowserEngine() == null) {
						Log.debug("Brower engine: "
								+ browser.getBrowserEngine());
						throw new Exception("Cannot execute mozilla");
					}
					// OK, create the perspective
					perspective = new InfoPerspective();
					perspective.setIconPath(ICON_PERSPECTIVE_INFORMATION);
					perspective.setID(PERSPECTIVE_NAME_INFO);
					registerPerspective(perspective);
				} else {
					throw new Exception("Cannot execute mozilla");
				}
			} catch (Exception e) {
				Log.debug("No mozilla available, disable InfoPerspective");
			}
		}

		// Configuration perspective
		perspective = new ConfigurationPerspective();
		perspective.setIconPath(ICON_PERSPECTIVE_CONFIGURATION);
		perspective.setID(PERSPECTIVE_NAME_CONFIGURATION);
		registerPerspective(perspective);

		// Stats perspective
		perspective = new StatPerspective();
		perspective.setIconPath(ICON_PERSPECTIVE_STATISTICS);
		perspective.setID(PERSPECTIVE_NAME_STATISTICS);
		registerPerspective(perspective);

		// Help perspective
		perspective = new HelpPerspective();
		perspective.setIconPath(ICON_PERSPECTIVE_HELP);
		perspective.setID(PERSPECTIVE_NAME_HELP);
		registerPerspective(perspective);
	}

	/**
	 * Register a new perspective
	 * 
	 * @param perspective
	 * @return registered perspective
	 */
	public static IPerspective registerPerspective(IPerspective perspective) {
		hmNameInstance.put(perspective.getID(), perspective);
		perspectives.add(perspective);
		return perspective;
	}

}
