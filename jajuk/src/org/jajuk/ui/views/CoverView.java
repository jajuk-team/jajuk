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
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jajuk.base.FIFO;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
import org.jajuk.ui.ViewManager;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;

/**
 *  Cover view. Displays an image for the current album
 * <p>Physical and logical perspectives
 * @author     bflorat
 * @created   28 dec. 2003
 */
public class CoverView extends ViewAdapter implements Observer{

	/**Current Image*/
	private static Image image;
	
	/**Current directory*/
	private File fDir;
		
	
	/**
	 * Constructor
	 */
	public CoverView() {
		setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
		image = java.awt.Toolkit.getDefaultToolkit().getImage(IMAGES_SPLASHSCREEN);
		ObservationManager.register(EVENT_COVER_REFRESH,this);
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
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(String subject){
		if ( subject.equals(EVENT_COVER_REFRESH)){
			java.io.File fDir = new java.io.File(FIFO.getInstance().getCurrentFile().getAbsolutePath()).getParentFile();
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
			if (!bFound){  //no cover file, take the first image we find
				for (int i=0;i<files.length;i++){
					String sExt = Util.getExtension(files[i]);
					if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png") || sExt.equalsIgnoreCase("gif")){
						image = java.awt.Toolkit.getDefaultToolkit().getImage(files[i].getAbsolutePath());
						bFound = true;
						break;
					}
				}
			}
			if ( !bFound){
				image = java.awt.Toolkit.getDefaultToolkit().getImage(IMAGES_SPLASHSCREEN);
			}
			display();
		}
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Cover view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.CoverView";
	}


}
