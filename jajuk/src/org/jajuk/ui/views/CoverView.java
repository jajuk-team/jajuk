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

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import layout.TableLayout;

import org.jajuk.base.FIFO;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
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
	
	//control panel
	JPanel jpControl;
	JButton jbPrevious;
	JButton jbNext;
	JButton jbSave;
	JButton jbSaveAs;
	JButton jbDefault;
	
	JLabel jl;
	/**
	 * Constructor
	 */
	public CoverView() {
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void populate(){
		//global layout
		double size[][] =
			{{0.99},
			{30,0.99}};
		setLayout(new TableLayout(size));
		//Control panel
		jpControl = new JPanel();
		jpControl.setBorder(BorderFactory.createEtchedBorder());
		int iXspace = 5;
		double sizeControl[][] =
			{{20,iXspace,20,2*iXspace,20,iXspace,20,2*iXspace,20},
				{25}};
		jpControl.setLayout(new TableLayout(sizeControl));
		jbPrevious = new JButton(Util.getIcon(ICON_PREVIOUS));
		jbPrevious.setToolTipText(Messages.getString("CoverView.4")); //$NON-NLS-1$
		jbNext = new JButton(Util.getIcon(ICON_NEXT));
		jbNext.setToolTipText(Messages.getString("CoverView.5")); //$NON-NLS-1$
		jbSave = new JButton(Util.getIcon(ICON_SAVE));
		jbSave.setToolTipText(Messages.getString("CoverView.6")); //$NON-NLS-1$
		jbSaveAs = new JButton(Util.getIcon(ICON_SAVE_AS));
		jbSaveAs.setToolTipText(Messages.getString("CoverView.7")); //$NON-NLS-1$
		jbDefault = new JButton(Util.getIcon(ICON_OK));
		jbDefault.setToolTipText(Messages.getString("CoverView.8")); //$NON-NLS-1$
		jpControl.add(jbPrevious,"0,0");//$NON-NLS-1$
		jpControl.add(jbNext,"2,0");//$NON-NLS-1$
		jpControl.add(jbSave,"4,0");//$NON-NLS-1$
		jpControl.add(jbSaveAs,"6,0");//$NON-NLS-1$
		jpControl.add(jbDefault,"8,0");//$NON-NLS-1$
		
		addComponentListener(this);
		try {
			image = java.awt.Toolkit.getDefaultToolkit().getImage(new URL(IMAGES_SPLASHSCREEN));
		} catch (MalformedURLException e) {
			Log.error(e);
		}
		ObservationManager.register(EVENT_COVER_REFRESH,this);
		ObservationManager.register(EVENT_PLAYER_STOP,this);
	
		//check if the cover should be refreshed at startup
		update(EVENT_COVER_REFRESH);
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
		displayCurrentCover();
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public synchronized void update(String subject){
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
			if (!bFound){  //no cover file, search for a file name containing 'front'
				for (int i=0;i<files.length;i++){
					if (files[i].getName().toLowerCase().indexOf(FILE_DEFAULT_COVER_2) != -1){
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
			displayCurrentCover();
		}
		else if ( EVENT_PLAYER_STOP.equals(subject)){
			displayDefault();
		}
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "CoverView.3";	 //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getID()
	 */
	public String getID() {
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
		displayCurrentCover();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
	 */
	public void componentShown(ComponentEvent e) {
	}

	/**
	 * Display current cover
	 *
	 */
	private void displayCurrentCover(){
	    JInternalFrame ji = ViewManager.getFrame(this);
	    ImageFilter filter = new AreaAveragingScaleFilter(ji.getWidth()-8,ji.getHeight()-30);
	    Image img = createImage(new FilteredImageSource(image.getSource(),filter));
	    jl = new JLabel(new ImageIcon(img));
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				removeAll();
				add(jpControl,"0,0");//$NON-NLS-1$
				add(jl,"0,1");//$NON-NLS-1$
				SwingUtilities.updateComponentTreeUI(CoverView.this.getRootPane());//refresh
			}
			
		});
		
	}
}
