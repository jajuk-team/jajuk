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
 *  $Revision$
 */

package org.jajuk.ui.views;

import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jajuk.base.FIFO;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  Cover view. Displays an image for the current album
 * <p>Physical and logical perspectives
 * @author     bflorat
 * @created   28 dec. 2003
 */
public class CoverView extends ViewAdapter implements Observer,ComponentListener{

	/**Current Image*/
	private static Image image;
	
	/**Current directory*/
	private File fDir;
		
	
	/**
	 * Constructor
	 */
	public CoverView() {
		addComponentListener(this);
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		try {
			image = java.awt.Toolkit.getDefaultToolkit().getImage(new URL(IMAGES_SPLASHSCREEN));
		} catch (MalformedURLException e) {
			Log.error(e);
		}
		ObservationManager.register(EVENT_COVER_REFRESH,this);
		ObservationManager.register(EVENT_PLAYER_STOP,this);
		
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		if (ConfigurationManager.getBoolean(CONF_OPTIONS_COVER)){
			JInternalFrame ji = ViewManager.getFrame(this);
			ImageFilter filter = new AreaAveragingScaleFilter(ji.getWidth()-8,ji.getHeight()-30);
			Image img = createImage(new FilteredImageSource(image.getSource(),filter));
			JLabel jl = new JLabel(new ImageIcon(img));
			removeAll(); //remove old picture
			add(jl);
			SwingUtilities.updateComponentTreeUI(this.getRootPane());//refresh
		}
	}
	
	/**
	 * Display the default Cover
	 */
	public void displayDefault(){
		try {
			image = java.awt.Toolkit.getDefaultToolkit().getImage(new URL(IMAGES_SPLASHSCREEN));
		} catch (MalformedURLException e) {
			Log.error(e);
			return;
		}
		display();
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject){
		if ( EVENT_COVER_REFRESH.equals(subject)){
			org.jajuk.base.File fCurrent = FIFO.getInstance().getCurrentFile();
			//if current file is null ( probably a file cannot be read ) 
			if ( fCurrent == null){
				displayDefault();
				return;
			}
			java.io.File fDir = new java.io.File(fCurrent.getAbsolutePath()).getParentFile();
			if ( !fDir.exists() || (this.fDir!= null && this.fDir.equals(fDir)) ){  //if we are always in the same directory, just leave to save cpu
				return;
			}
			this.fDir = fDir;
			java.io.File[] files = fDir.listFiles();
			boolean bFound = false;
			//first, search for a 'cover.jpg' file
			for (int i=0;i<files.length;i++){
				if (files[i].getName().equalsIgnoreCase(FILE_DEFAULT_COVER)){
					image = java.awt.Toolkit.getDefaultToolkit().getImage(files[i].getAbsolutePath());
					bFound = true;
					break;
				}
			}
			if (!bFound){  //no cover file, search for a 'front.jpg' file
				for (int i=0;i<files.length;i++){
					if (files[i].getName().equalsIgnoreCase(FILE_DEFAULT_COVER_2)){
						image = java.awt.Toolkit.getDefaultToolkit().getImage(files[i].getAbsolutePath());
						bFound = true;
						break;
					}
				}
			}
			if (!bFound){  //no cover file, take the first image we find
				for (int i=0;i<files.length;i++){
					String sExt = Util.getExtension(files[i]);
					if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png") || sExt.equalsIgnoreCase("gif")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						image = java.awt.Toolkit.getDefaultToolkit().getImage(files[i].getAbsolutePath());
						bFound = true;
						break;
					}
				}
			}
			if ( !bFound){
				displayDefault();
				return;
			}
			display();
		}
		else if ( EVENT_PLAYER_STOP.equals(subject)){
			displayDefault();
		}
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("CoverView.3");	 //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.CoverView"; //$NON-NLS-1$
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
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
		new Thread(){
			public void run(){
				try {
					Thread.sleep(1000); //to avoid to much display during resizing
					display();
				} catch (InterruptedException e) {
					Log.error(e);
				}
			}
		}.start();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
	}


}
