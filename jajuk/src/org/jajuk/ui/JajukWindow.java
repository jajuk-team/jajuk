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
 * $Revision$
 */

package org.jajuk.ui;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Event;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Jajuk main window
 * <p>Singleton
 *
 * @author     Bertrand Florat
 * @created    23 mars 2004
 */
public class JajukWindow extends JFrame implements ITechnicalStrings,ComponentListener,Observer {
	
	/**Initial width at startup*/
	private int iWidth ; 
	/**Initial height at startup*/
	private int iHeight;
	/**Self instance*/
	private static JajukWindow jw;
	/**Show window at startup?*/
	private boolean bVisible = true;
	
	
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
		//mac integration 
		System.setProperty( "apple.laf.useScreenMenuBar", "true");//$NON-NLS-1$ //$NON-NLS-2$ 
		jw = this;
		bVisible = ConfigurationManager.getBoolean(CONF_SHOW_AT_STARTUP,true);
		iWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		iHeight = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
		setTitle(Messages.getString("JajukWindow.17"));  //$NON-NLS-1$
		setIconImage(Util.getIcon(ICON_LOGO).getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addComponentListener(this);
		//register for given events
		ObservationManager.register(EVENT_FILE_LAUNCHED,this);
		ObservationManager.register(EVENT_ZERO,this);
		
		addWindowListener(new WindowAdapter() {
            public void windowDeiconified(WindowEvent arg0) {
        	    setShown(true);
    	    }
        	public void windowIconified(WindowEvent arg0) {
			}
			public void windowClosing(WindowEvent we) {
			    //  check if a device is refreshing
				if (DeviceManager.isAnyDeviceRefreshing()){
					int iResu = Messages.getChoice(Messages.getString("Confirmation_exit_refreshing"),JOptionPane.WARNING_MESSAGE);  //$NON-NLS-1$ //$NON-NLS-2$
					if (iResu != JOptionPane.YES_OPTION){
						return;
					}
				}
			    Main.exit(0);
				return; 
			}
		});
		//display correct title if a track is lauched at startup
		update(new Event(EVENT_FILE_LAUNCHED,ObservationManager.getDetailsLastOccurence(EVENT_FILE_LAUNCHED)));
        
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
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		String subject = event.getSubject();
		if (subject.equals(EVENT_FILE_LAUNCHED)){
			File file = FIFO.getInstance().getCurrentFile();
			if (file != null){
				setTitle(file.getTrack().getName());
			}
		}
		else  if (subject.equals(EVENT_ZERO)){
			setTitle(Messages.getString("JajukWindow.17")); //$NON-NLS-1$
		}
		
	}
		
	/**
	 * @return Returns the bVisible.
	 */
	public boolean isVisible() {
		return bVisible;
	}
	/**
	 * @param visible The bVisible to set.
	 */
	public void setShown(boolean visible) {
	    //start ui if needed
	    if (visible && !Main.isUILauched()){
	        if (SwingUtilities.isEventDispatchThread()){ //must be lauched from another thread
	            Thread t = new Thread(){ 
	                public void run(){
	                    try {
	                        Main.launchUI();
	                    } catch (Exception e) {
	                        Log.error(e);
	                    }
	                }
	            };
	            t.start();
	        }
	        else{
	            try {
	                Main.launchUI();
	            } catch (Exception e) {
	                Log.error(e);
	            }
	        }
	    }
	     //store state
	    bVisible = visible;
		//show 
		if (visible){
			setState(Frame.NORMAL);
			setVisible(true);
			toFront();
		}
		//hide
		else{
		    if (Main.isNoTaskBar()){ //hide the window only if it is explicitely required 
		        setVisible(false);
		    }
		 }
	}
}
