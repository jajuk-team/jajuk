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
 * Revision 1.2  2003/10/09 21:15:29  bflorat
 * *** empty log message ***
 *
 * Revision 1.1  2003/10/07 21:02:23  bflorat
 * Initial commit
 *
 */
package org.jajuk;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.UIManager;

import javazoom.jl.player.Player;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.CommandJPanel;
import org.jajuk.ui.JajukJMenuBar;
import org.jajuk.util.log.Log;

import com.sun.corba.se.internal.iiop.LocalClientRequestImpl;

/**
 * Jajuk lauching class
 *
 * @author     bflorat
 * @created    3 oct. 2003
 */
public class Main {

	public static void main(String[] args) {
		try{
		//perform initial checkups
		initialCheckups();
		
		//set language ( test only, will take default locale )
		Messages.setLocal("en"); //$NON-NLS-1$
			
		//log startup
		Log.getInstance();
		Log.setVerbosity(Log.DEBUG);
		
		//Display user configuration
		Log.debug(System.getProperties().toString());
		
		//set look and feel
	 	 //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		  //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		  //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		
		//starts ui
		JFrame jf = new JFrame("Jajuk : Just Another Jukebox"); //$NON-NLS-1$
		jf.setSize(1280,1024);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.addWindowListener(new WindowAdapter(){  //call property AMI exit before exiting
			   public void windowClosing(WindowEvent we){
				   System.exit(0);
			   }
		   });
		Container container = jf.getContentPane(); //default layout for content panes are Border Layout
		CommandJPanel command = new CommandJPanel();
		//command.setPreferredSize(new Dimension(1021,20));
		container.add(command,BorderLayout.NORTH);
		container.add(Box.createVerticalStrut(1000),BorderLayout.CENTER);
		
		jf.setJMenuBar(JajukJMenuBar.getInstance());
		
		jf.show();
		//test java layer
		//Player player = new Player(new FileInputStream(new File("/data/mp3/morcheeba/big_calm/friction.mp3"))); //$NON-NLS-1$
		//player.play();
		}
		catch(Exception e){  //last chance to catch any error for logging purpose
			Log.error(Messages.getString("Main.uncatched_exception_2"),e); //$NON-NLS-1$
		}
	}
	
	
	/**
	 * Performs some basic startup tests
	 * @throws Exception
	 */
	private static void initialCheckups() throws Exception{
		//check for jajuk home directory presence
		File fJajukDir = new File(System.getProperty("user.home")+"/.jajuk"); //$NON-NLS-1$ //$NON-NLS-2$
		if (!fJajukDir.exists() || !fJajukDir.isDirectory()){
			fJajukDir.mkdir(); //create the directory if it doesn't exist
		}
	}
	
}
