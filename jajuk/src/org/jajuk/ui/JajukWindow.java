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
 * $Release$
 */

package org.jajuk.ui;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.FIFO;
import org.jajuk.base.FileManager;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;

import snoozesoft.systray4j.SysTrayMenu;
import snoozesoft.systray4j.SysTrayMenuEvent;
import snoozesoft.systray4j.SysTrayMenuIcon;
import snoozesoft.systray4j.SysTrayMenuItem;
import snoozesoft.systray4j.SysTrayMenuListener;

/**
 *  Jajuk main window
 * <p>Singleton
 *
 * @author     bflorat
 * @created    23 mars 2004
 */
public class JajukWindow extends JFrame implements ITechnicalStrings,ComponentListener,SysTrayMenuListener {
	
	/**Initial width at startup*/
	private int iWidth ; 
	/**Initial height at startup*/
	private int iHeight;
	/**Self instance*/
	private static JajukWindow jw;
	/**Show window at startup?*/
	private boolean bVisible = true;
	/**Pause status*/
	private boolean bPaused = false;
	//Systray variables
	SysTrayMenuIcon stmi;
	SysTrayMenu stm;
	SysTrayMenuItem stmiExit;
	SysTrayMenuItem stmiAbout;
	SysTrayMenuItem stmiShuffle;
	SysTrayMenuItem stmiBestof;
	SysTrayMenuItem stmiPause;
	SysTrayMenuItem stmiStop;
	SysTrayMenuItem stmiVisible;
	SysTrayMenuItem stmiHidden;
	
	/**
	 * Get instance
	 * @return
	 */
	public static JajukWindow getInstance(){
		if ( jw == null){
			jw = new JajukWindow();
		}
		return jw;
	}
	
	/**
	 * Constructor
	 */
	public JajukWindow(){
		jw = this;
		bVisible = ConfigurationManager.getBoolean(CONF_SHOW_AT_STARTUP,true);
		iWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		iHeight = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		setTitle(Messages.getString("Main.10"));  //$NON-NLS-1$
		setIconImage(Util.getIcon(ICON_LOGO).getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addComponentListener(this);
		addWindowListener(new WindowAdapter() {
			public void windowIconified(WindowEvent arg0) {
				//systray, only for window for now
				if (Util.underWindows()){
					setVisible(false);
				}
			}
			public void windowClosing(WindowEvent we) {
				Main.exit(0);
				return; 
			}
		});
		//systray, only for window for now
		if (Util.underWindows()){
			URL url = null;
			try {
				url = new URL(ICON_LOGO_ICO);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			stmi = new SysTrayMenuIcon(url);
			stmi.addSysTrayMenuListener(this);
			stm = new SysTrayMenu(stmi,Messages.getString("JajukWindow.3")); //$NON-NLS-1$
			stmiExit =  new SysTrayMenuItem(Messages.getString("JajukWindow.4")); //$NON-NLS-1$
			stmiExit.addSysTrayMenuListener(this);
			stmiAbout =  new SysTrayMenuItem(Messages.getString("JajukWindow.5")); //$NON-NLS-1$
			stmiAbout.addSysTrayMenuListener(this);
			stmiShuffle =  new SysTrayMenuItem(Messages.getString("JajukWindow.6")); //$NON-NLS-1$
			stmiShuffle.addSysTrayMenuListener(this);
			stmiBestof =  new SysTrayMenuItem(Messages.getString("JajukWindow.7")); //$NON-NLS-1$
			stmiBestof.addSysTrayMenuListener(this);
			stmiVisible =  new SysTrayMenuItem(Messages.getString("JajukWindow.8")); //$NON-NLS-1$
			stmiVisible.setEnabled(!bVisible);//if it is already visible, this menu is hidden 
			stmiVisible.addSysTrayMenuListener(this);
			stmiHidden =  new SysTrayMenuItem(Messages.getString("JajukWindow.9")); //$NON-NLS-1$
			stmiHidden.setEnabled(bVisible);
			stmiHidden.addSysTrayMenuListener(this);
			stmiPause = new SysTrayMenuItem(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
			stmiPause.addSysTrayMenuListener(this);
			stmiStop = new SysTrayMenuItem(Messages.getString("JajukWindow.11")); //$NON-NLS-1$
			stmiStop.addSysTrayMenuListener(this);
			stm.addItem(stmiExit);
			stm.addSeparator();
			stm.addItem(stmiAbout);
			stm.addSeparator();
			stm.addItem(stmiShuffle);
			stm.addItem(stmiBestof);
			stm.addSeparator();
			stm.addItem(stmiStop);
			stm.addItem(stmiPause);
			stm.addSeparator();
			stm.addItem(stmiHidden);
			stm.addItem(stmiVisible);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
	 */
	public void componentHidden(ComponentEvent e) {
	}
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
	 */
	public void componentMoved(ComponentEvent e) {
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
	
		int width = getWidth();
		int height = getHeight();
		
		
		/*Goal here is to fix a bug : when starting, restaure the window ( middle button near close ) set a strange size ( very large * very small ). So if size is too small or too large in front of
		 * screen size, we set 100% of screen
		 */
		
		boolean resize = false;
				
		if (width > 1.1*iWidth) { 
			resize = true;
			width = iWidth;
		}
		if (height > 1.1*iHeight) { 
			resize = true;
			height = iHeight;
		}
		if (resize) {
			setSize(width, height);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
	}


	/* (non-Javadoc)
	 * @see snoozesoft.systray4j.SysTrayMenuListener#menuItemSelected(snoozesoft.systray4j.SysTrayMenuEvent)
	 */
	public void menuItemSelected(SysTrayMenuEvent e) {
		if (e.getSource() == stmiExit){
			Main.exit(0);
		}
		else if (e.getSource() == stmiAbout){
			//make frame visible
			if ( !isVisible()){
				setVisible(true);
				setState(Frame.NORMAL);
				SwingUtilities.updateComponentTreeUI(this);
			}
			//set help perspectievb to show "about" view
			PerspectiveManager.setCurrentPerspective(PERSPECTIVE_NAME_HELP);
		}
		else if (e.getSource() == stmiShuffle){
			org.jajuk.base.File file = null;
			file = FileManager.getShuffleFile();
			if (file != null){
				FIFO.getInstance().setBestof(false); //break best of mode if set
				FIFO.getInstance().setGlobalRandom(true);
				FIFO.getInstance().push(file,false,true);
			}
		}
		else if (e.getSource() == stmiBestof){
			org.jajuk.base.File file = null;
			file = FileManager.getBestOfFile();
			if (file != null){
				FIFO.getInstance().setGlobalRandom(false); //break global random mode if set
				FIFO.getInstance().setBestof(true);
				FIFO.getInstance().push(file,false,true);
			}
		}
		else if (e.getSource() == stmiVisible){
			stmiVisible.setEnabled(false);
			stmiHidden.setEnabled(true);
			ConfigurationManager.setProperty(CONF_SHOW_AT_STARTUP,TRUE);
		}
		else if (e.getSource() == stmiHidden){
			stmiHidden.setEnabled(false);
			stmiVisible.setEnabled(true);
			ConfigurationManager.setProperty(CONF_SHOW_AT_STARTUP,FALSE);
		}
		else if (e.getSource() == stmiStop){
			FIFO.getInstance().stopRequest();
		}
		else if (e.getSource() == stmiPause){
			FIFO.getInstance().pauseRequest();
			bPaused = !bPaused;
			if ( bPaused ){
				stmiPause.setLabel(Messages.getString("JajukWindow.12")); //$NON-NLS-1$
			}
			else{
				stmiPause.setLabel(Messages.getString("JajukWindow.10")); //$NON-NLS-1$
			}
		}
	}


	/* (non-Javadoc)
	 * @see snoozesoft.systray4j.SysTrayMenuListener#iconLeftClicked(snoozesoft.systray4j.SysTrayMenuEvent)
	 */
	public void iconLeftClicked(SysTrayMenuEvent e) {
		if ( e.getSource() == stmi){
			if ( !isVisible()){
				setVisible(true);
				setState(Frame.NORMAL);
				SwingUtilities.updateComponentTreeUI(this);
			}
			else{
				setVisible(false);
			}
		}
	}


	/* (non-Javadoc)
	 * @see snoozesoft.systray4j.SysTrayMenuListener#iconLeftDoubleClicked(snoozesoft.systray4j.SysTrayMenuEvent)
	 */
	public void iconLeftDoubleClicked(SysTrayMenuEvent arg0) {
	}
	
	/**
	 * Hide systray 
	 *
	 */
	public void closeSystray(){
		if ( stm != null ){
			stm.hideIcon();
		}
	}
	
	/**
	 * Set systray tooltip
	 * @param s
	 */
	public void setTooltip(String s){
		if ( stm != null){
			stm.setToolTip(s);
		}
	}

	
}
