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
 * $Log$
 * Revision 1.2  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 * Revision 1.1  2003/10/07 21:02:22  bflorat
 * Initial commit
 *
 */
package org.jajuk.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.jajuk.Main;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.Player;
import org.jajuk.base.TechnicalStrings;
import org.jajuk.base.Type;
import org.jajuk.base.TypesManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
;

/**
 *  Jajuk menu bar
 * <p>Singleton
 *
 * @author     bflorat
 * @created    4 oct. 2003
 */public class JajukJMenuBar extends JMenuBar implements TechnicalStrings,ActionListener{

	static JajukJMenuBar jjmb;
		static JMenu file;
			static JMenuItem jmiFileOpen;
				JajukFileChooser jfchooser;
			static JMenuItem jmiFileExit;
		static JMenu views;
		static JMenu properties;
		static JMenu mode;
		static JMenu help;
		
	
	private JajukJMenuBar(){
		setAlignmentX(0.0f);
		//File menu
		file = new JMenu(Messages.getString("JajukJMenuBar.File_1")); //$NON-NLS-1$
		jmiFileOpen = new JMenuItem(Messages.getString("JajukJMenuBar.Open_file_1"),new ImageIcon(ICON_OPEN_FILE)); //$NON-NLS-1$
		jmiFileOpen.addActionListener(this);
		jmiFileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.ALT_MASK));
		jmiFileOpen.getAccessibleContext().setAccessibleDescription(Messages.getString("JajukJMenuBar.[ALT-F]_2")); //$NON-NLS-1$
		jmiFileExit = new JMenuItem(Messages.getString("JajukJMenuBar.Exit_3"),new ImageIcon(ICON_EXIT)); //$NON-NLS-1$
		jmiFileExit.addActionListener(this);
		jmiFileExit.addActionListener(this);
		jmiFileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.ALT_MASK));
		jmiFileExit.getAccessibleContext().setAccessibleDescription(Messages.getString("JajukJMenuBar.[ALT-X]_4")); //$NON-NLS-1$
		file.add(jmiFileOpen);
		file.add(jmiFileExit);
		
		//Views menu
		views = new JMenu(Messages.getString("JajukJMenuBar.Views_2")); //$NON-NLS-1$
		//TODO remplace this code with an enumeration of available views for this perspective
		JCheckBoxMenuItem jcbmiPhysicalTree = new JCheckBoxMenuItem("Physical tree", true); //$NON-NLS-1$
		JCheckBoxMenuItem jcbmiNavigationBar = new JCheckBoxMenuItem("Navigation bar",true); //$NON-NLS-1$
		JCheckBoxMenuItem jcbmiPlaylistRepository = new JCheckBoxMenuItem("Playlist repository",true); //$NON-NLS-1$
		views.add(jcbmiPhysicalTree);
		views.add(jcbmiNavigationBar);
		views.add(jcbmiPlaylistRepository);
		
		//Properties menu
		properties = new JMenu(Messages.getString("JajukJMenuBar.Properties_3")); //$NON-NLS-1$
		JMenuItem jmiNewProperty = new JMenuItem(Messages.getString("JajukJMenuBar.New_Property_1"),new ImageIcon(ICON_NEW)); //$NON-NLS-1$
		JMenuItem jmiDeleteProperty = new JMenuItem(Messages.getString("JajukJMenuBar.Delete_a_Property_9"),new ImageIcon(ICON_DELETE)); //$NON-NLS-1$
		properties.add(jmiNewProperty);
		properties.add(jmiDeleteProperty);
		properties.addSeparator();
		properties.add(new JMenuItem("property1")); //temp //$NON-NLS-1$
		properties.add(new JMenuItem("property2")); //temp //$NON-NLS-1$
		
		
		//Mode menu
		mode = new JMenu(Messages.getString("JajukJMenuBar.Mode_4")); //$NON-NLS-1$
		JCheckBoxMenuItem jcbmiRepeat = new JCheckBoxMenuItem(Messages.getString("JajukJMenuBar.Repeat_12"), new ImageIcon(ICON_REPEAT),true); //$NON-NLS-1$
		JCheckBoxMenuItem jcbmiShuffle = new JCheckBoxMenuItem(Messages.getString("JajukJMenuBar.Shuffle_13"),new ImageIcon(ICON_SHUFFLE),true); //$NON-NLS-1$
		JCheckBoxMenuItem jcbmiContinue = new JCheckBoxMenuItem(Messages.getString("JajukJMenuBar.Continue_14"),new ImageIcon(ICON_CONTINUE),true); //$NON-NLS-1$
		JCheckBoxMenuItem jcbmiIntro = new JCheckBoxMenuItem(Messages.getString("JajukJMenuBar.Intro_15"),new ImageIcon(ICON_INTRO),true); //$NON-NLS-1$
		mode.add(jcbmiRepeat);
		mode.add(jcbmiShuffle);
		mode.add(jcbmiContinue);
		mode.add(jcbmiIntro);
				
		//Help menu
		help = new JMenu(Messages.getString("JajukJMenuBar.Help_5")); //$NON-NLS-1$
		JMenuItem jmiHelp = new JMenuItem(Messages.getString("JajukJMenuBar.Help_contents_16"),new ImageIcon(ICON_INFO)); //$NON-NLS-1$
		JMenuItem jmiAbout = new JMenuItem(Messages.getString("JajukJMenuBar.About_jajuk_17"),new ImageIcon(ICON_INFO)); //$NON-NLS-1$
		help.add(jmiHelp);
		help.add(jmiAbout);
		
		//add menus
		add(file);
		add(views);
		add(properties);
		add(mode);
		add(help);
	}
	
	static public JajukJMenuBar getInstance(){
		if (jjmb == null){
			jjmb = new JajukJMenuBar();
		}
		return jjmb;
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource()==jmiFileExit){
			Main.exit(0);
		}
		else if (e.getSource()==jmiFileOpen){
			jfchooser = new JajukFileChooser();
			int returnVal = jfchooser.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				java.io.File[] files = jfchooser.getSelectedFiles();
				FIFO.clear();  //stop all currently played tracks
				for (int i=0;i<files.length;i++){
					File file = new File(files[i].getAbsolutePath(),TypesManager.getTypeByExtension(Util.getExtension(files[i])));
					FIFO.push(file);
				}
			}
		}

	}

}
