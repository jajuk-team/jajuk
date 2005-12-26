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

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
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
import org.jdesktop.swingx.JXFrame;

/**
 *  Jajuk main window
 * <p>Singleton
 *
 * @author     Bertrand Florat
 * @created    23 mars 2004
 */
public class JajukWindow extends JXFrame implements ITechnicalStrings,Observer {
	
	/**Max width*/
	private int iMaxWidth ; 
	/**Max height*/
	private int iMaxHeight;
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
		//mac integration (disable for the moment as users reported issues) 
		//System.setProperty( "apple.laf.useScreenMenuBar", "true");//$NON-NLS-1$ //$NON-NLS-2$ 
		jw = this;
		bVisible = ConfigurationManager.getBoolean(CONF_SHOW_AT_STARTUP,true);
		iMaxWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
		iMaxHeight = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
        setTitle(Messages.getString("JajukWindow.17"));  //$NON-NLS-1$
		setIconImage(Util.getIcon(ICON_LOGO).getImage());
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//register for given events
		ObservationManager.register(EVENT_FILE_LAUNCHED,this);
		ObservationManager.register(EVENT_ZERO,this);
        addWindowListener(new WindowAdapter() {
        
            public void windowDeiconified(WindowEvent arg0) {
                setFocusableWindowState(true);
           }
        	public void windowIconified(WindowEvent arg0) {
                setFocusableWindowState(false);
            }
        	public void windowClosing(WindowEvent we) {
        	    //hide window ASAP
        	    setVisible(false);
        	    Main.exit(0);
        	}
        });
        //display correct title if a track is lauched at startup
		update(new Event(EVENT_FILE_LAUNCHED,ObservationManager.getDetailsLastOccurence(EVENT_FILE_LAUNCHED)));
   }
	
    
    public void addComponentListener(){
        addComponentListener(new ComponentListener() {
        
            public void componentShown(ComponentEvent e) {
            }
        
            public void componentResized(ComponentEvent e) {
                saveSize();
            }
        
            public void componentMoved(ComponentEvent e) {
                saveSize();
             }
        
            public void componentHidden(ComponentEvent e) {
            }
        
        });
    }
    
    
    /**
     * Save current window size and position
     *
     */
    private void saveSize(){
        Rectangle rec = getBounds();
        ConfigurationManager.setProperty(CONF_WINDOW_POSITION,
            (int)rec.getMinX()+","+(int)rec.getMinY()+","+(int)rec.getWidth()+","+(int)rec.getHeight());  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    /**
     * Apply size and position stored as property
     *
     */
    public void applyStoredSize(){
        //read stored position and size
        String sPosition = ConfigurationManager.getProperty(CONF_WINDOW_POSITION);
        StringTokenizer st =new StringTokenizer(sPosition,","); //$NON-NLS-1$
        int iX = Integer.parseInt((String)st.nextToken());
        int iY = Integer.parseInt((String)st.nextToken());
        int iXsize = Integer.parseInt((String)st.nextToken());
        if (iXsize == 0){ //if zero, display max size
            iXsize = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth());
        }
        int iYsize = Integer.parseInt((String)st.nextToken());
        if (iYsize == 0){//if zero, display max size
            iYsize = (int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight());
        }
        setLocation(iX,iY);
        setSize(iXsize,iYsize);
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
    		setVisible(true);
     	}
		//hide
		else{
		    if (Main.isNoTaskBar()){ //hide the window only if it is explicitely required 
		        setVisible(false);
            }
		 }
	}
   
}


    
