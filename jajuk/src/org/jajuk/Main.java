/*
 * Jajuk Copyright (C) 2003 bflorat
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307,USA
 * $Log$
 * Revision 1.14  2003/11/11 20:34:29  bflorat
 * 11/11/2003
 *
 * Revision 1.13  2003/11/07 23:57:01  bflorat
 * 08/11/2003
 *
 *  
 */
package org.jajuk;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

import org.jajuk.base.Collection;
import org.jajuk.base.FIFO;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.base.TypeManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.InformationJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.ui.PerspectiveBarJPanel;
import org.jajuk.ui.perspectives.IPerspectiveManager;
import org.jajuk.ui.perspectives.PerspectiveManagerFactory;
import org.jajuk.ui.views.DeviceView;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 * Jajuk lauching class
 * 
 * @author bflorat @created 3 oct. 2003
 */
public class Main implements ITechnicalStrings {

	public static JFrame jframe;
	public static CommandJPanel command;
	public static PerspectiveBarJPanel perspectiveBar;
	public static InformationJPanel information;

	public static void main(String[] args) {
		try {
			//	log startup
			Log.getInstance();
			Log.setVerbosity(Log.DEBUG);

			//registers supported types
			try {
				//TODO get player impl in user-conf.xml
				TypeManager.registerType(Messages.getString("Main.Mpeg_layer_3_5"), EXT_MP3, PLAYER_IMPL_JAVALAYER, TAG_IMPL_MP3INFO, true); //$NON-NLS-1$ //$NON-NLS-2$
				TypeManager.registerType(Messages.getString("Main.Playlist_7"), EXT_PLAYLIST, PLAYER_IMPL_JAVALAYER, null, false); //$NON-NLS-1$ //$NON-NLS-2$
				TypeManager.registerType(Messages.getString("Main.Ogg_vorbis_9"), EXT_OGG, PLAYER_IMPL_JAVALAYER, null, true); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (Exception e1) {
				Log.error(Messages.getString("Main.Error_registering_players_11"), e1); //$NON-NLS-1$
			}

			//perform initial checkups
			initialCheckups();

			//Display user configuration
			Log.debug(System.getProperties().toString());

			//starts ui
			jframe = new JFrame("Jajuk : Just Another Jukebox"); //$NON-NLS-1$
			Dimension dScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
			jframe.setSize((int) (0.9 * dScreenSize.getWidth()), (int) (0.9 * dScreenSize.getHeight()));
			//TODO see for automatic maximalize
			jframe.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					exit(0);
				}
			});
			Container container = jframe.getContentPane();
			// Create the perspective manager
			IPerspectiveManager perspectiveManager = PerspectiveManagerFactory.getPerspectiveManager();
			perspectiveManager.setParentContainer(container);

			//Creates the command panel
			command = CommandJPanel.getInstance();

			// Create the perspective tool bar panel
			perspectiveBar = new PerspectiveBarJPanel();

			// Create the information bar panel
			information = new InformationJPanel();
			//****temp
			information.setMessage("Now playing foo track...", InformationJPanel.INFORMATIVE); //temp //$NON-NLS-1$
			information.setSelection("124 items : 4.5Mo"); //temp //$NON-NLS-1$
			information.setCurrentStatusMessage("00:01:02/00:05:12"); //$NON-NLS-1$
			information.setTotalStatus(50);
			information.setTotalStatusMessage("00:23:23/01:34:56"); //$NON-NLS-1$
			information.setCurrentStatus(76);
			//**************************

			//Load collection
			Collection.load();

			//Starts the FIFO
			new FIFO().start();

			//Add static panels
			container.add(command, BorderLayout.NORTH);
			container.add(perspectiveBar, BorderLayout.WEST);
			container.add(information, BorderLayout.SOUTH);
			
			//Create desktop pane
			final JDesktopPane desktop = new JDesktopPane();
			//	desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

			//add views
			DeviceView deviceView = DeviceView.getInstance();
			final JInternalFrame ji = new JInternalFrame("Devices", true, true, true, true);
			ji.setContentPane(deviceView);
			ji.setSize(600, 600);
			ji.setLocation(600, 0);
			ji.setVisible(true);
			desktop.add(ji);
			
			container.add(desktop);

			
			//Set menu bar to the frame
			jframe.setJMenuBar(JajukJMenuBar.getInstance());
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			jframe.show();

		} catch (JajukException je) { //last chance to catch any error for logging purpose
			Log.error(je);
			exit(1);
		} catch (Exception e) { //last chance to catch any error for logging purpose
			Log.error("106", e); //$NON-NLS-1$
			exit(1);
		} finally {
		}
	}

	/**
	 * Performs some basic startup tests
	 * 
	 * @throws Exception
	 */
	private static void initialCheckups() throws Exception {
		//check for jajuk home directory presence
		File fJajukDir = new File(FILE_JAJUK_DIR);
		if (!fJajukDir.exists() || !fJajukDir.isDirectory()) {
			fJajukDir.mkdir(); //create the directory if it doesn't exist
		}
		//check for collection.xml file
		File fCollection = new File(FILE_COLLECTION);
		if (!fCollection.exists()) { //if collection file doesn't exit, create it empty
			Collection.commit();
		}
		//	check for perspectives.xml file
		File fPerspectives = new File(FILE_PERSPECTIVES_CONF);
		if (!fPerspectives.exists()) {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fPerspectives));
			bw.write(XML_PERSPECTIVES_CONF);
			bw.close();
		}
	}

	/**
	 * Exit code, used to perform saves...
	 * 
	 * @param iExitCode
	 *                exit code
	 *                <p>
	 *                0 : normal exit
	 *                <p>1: unexpected error
	 */
	public static void exit(int iExitCode) {
		try {
			org.jajuk.base.Collection.commit();
		} catch (IOException e) {
			Log.error("", e);
		}
		System.exit(iExitCode);

	}

}
