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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.AreaAveragingScaleFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import layout.TableLayout;

import org.jajuk.base.Cover;
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
public class CoverView extends ViewAdapter implements Observer,ComponentListener,ActionListener{

	/**Current directory used as a cache for perfs*/
	private File fDir;
	
	/**List of available covers for the current file*/
	ArrayList alCovers = new ArrayList(20);
	
	//control panel
	JPanel jpControl;
	JButton jbPrevious;
	JButton jbNext;
	JButton jbSave;
	JButton jbSaveAs;
	JButton jbDefault;

	/**Date last resize (used for adjustment management)*/
	private long lDateLastResize;
	
	/**Disk covers*/
	ArrayList alFiles = new ArrayList(10);
	
	JLabel jl;
	
	/**Used Cover index*/
	int index = 0;
	
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
		jbPrevious.addActionListener(this);
		jbPrevious.setToolTipText(Messages.getString("CoverView.4")); //$NON-NLS-1$
		jbNext = new JButton(Util.getIcon(ICON_NEXT));
		jbNext.addActionListener(this);
		jbNext.setToolTipText(Messages.getString("CoverView.5")); //$NON-NLS-1$
		jbSave = new JButton(Util.getIcon(ICON_SAVE));
		jbSave.addActionListener(this);
		jbSave.setToolTipText(Messages.getString("CoverView.6")); //$NON-NLS-1$
		jbSaveAs = new JButton(Util.getIcon(ICON_SAVE_AS));
		jbSaveAs.addActionListener(this);
		jbSaveAs.setToolTipText(Messages.getString("CoverView.7")); //$NON-NLS-1$
		jbDefault = new JButton(Util.getIcon(ICON_DEFAULT_COVER));
		jbDefault.addActionListener(this);
		jbDefault.setToolTipText(Messages.getString("CoverView.8")); //$NON-NLS-1$
		jpControl.add(jbPrevious,"0,0");//$NON-NLS-1$
		jpControl.add(jbNext,"2,0");//$NON-NLS-1$
		jpControl.add(jbSave,"4,0");//$NON-NLS-1$
		jpControl.add(jbSaveAs,"6,0");//$NON-NLS-1$
		jpControl.add(jbDefault,"8,0");//$NON-NLS-1$
		
		ObservationManager.register(EVENT_COVER_REFRESH,this);
		ObservationManager.register(EVENT_PLAYER_STOP,this);
	
		//check if the cover should be refreshed at startup
		update(EVENT_COVER_REFRESH);
	}
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public synchronized void update(String subject){
	    try{
	        if ( EVENT_COVER_REFRESH.equals(subject)){
	            org.jajuk.base.File fCurrent = FIFO.getInstance().getCurrentFile();
	            //if current file is null ( probably a file cannot be read ) 
	            if ( fCurrent == null){
	                displayCurrentCover();
	                return;
	            }
	            java.io.File fDir = new java.io.File(fCurrent.getAbsolutePath()).getParentFile();
	            if ( !fDir.exists() || (this.fDir!= null && this.fDir.equals(fDir)) ){  //if we are always in the same directory, just leave to save cpu
	                return;
	            }
	            alCovers.clear();
	            java.io.File[] files = fDir.listFiles();
	            for (int i=0;i<files.length;i++){
	                String sExt = Util.getExtension(files[i]);
	                if (sExt.equalsIgnoreCase("jpg") || sExt.equalsIgnoreCase("png") || sExt.equalsIgnoreCase("gif")){ //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                    alCovers.add(new Cover(files[i].toURL(),Cover.LOCAL_COVER));
	                }
	            }
	            if (alCovers.size() == 0){
	                alCovers.add(new Cover()); //add the default cover
		        }
	            else{
	                Collections.sort(alCovers); //sort the list 
		        }
	            index = alCovers.size()-1;  //current index points to the best available cover
	            displayCurrentCover();
	        }
	        else if ( EVENT_PLAYER_STOP.equals(subject)){
	            alCovers.clear();
	            alCovers.add(new Cover()); //add the default cover
	            index = 0;
	            displayCurrentCover();
	        }
	    }
	    catch(Exception e){
	        Log.error(e);
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
	 * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
	 */
	public void componentResized(ComponentEvent e) {
	    Log.debug("Cover resized");
	    long lCurrentDate = System.currentTimeMillis();  //adjusting code
		if ( lCurrentDate - lDateLastResize < 500){  //display image every 500 ms to save CPU
			lDateLastResize = lCurrentDate;
			return;
		}
	    displayCurrentCover();
	}

	/**
	 * Display current cover
	 *
	 */
	private void displayCurrentCover(){
	    if ( alCovers.size() == 0 ){
	        return;
	    }
	    Cover cover = (Cover)alCovers.get(index); 
	    JInternalFrame ji = ViewManager.getFrame(this);
	    Log.debug("Cover size: "+(ji.getWidth()-8)+"/"+(ji.getHeight()-60));
	    ImageFilter filter = new AreaAveragingScaleFilter(ji.getWidth()-8,ji.getHeight()-60);
	    Image img = createImage(new FilteredImageSource(cover.getImage().getSource(),filter));
	    jl = new JLabel(new ImageIcon(img));
	    URL url = cover.getURL();
	    if (url != null){
	        jl.setToolTipText(url.toString());
	    }
	    SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				removeAll();
				add(jpControl,"0,0");//$NON-NLS-1$
				add(jl,"0,1");//$NON-NLS-1$
				SwingUtilities.updateComponentTreeUI(CoverView.this.getRootPane());//refresh
			}
		});
	}

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == jbPrevious){  //previous : show a better cover
            index = index+1;
            if (index > alCovers.size()-1){
                index = 0;
            }
            displayCurrentCover();
        }
        else if(e.getSource() == jbNext){ //next : show a worse cover
            index = index-1;
            if (index < 0){
                index = alCovers.size()-1;
            }
            displayCurrentCover();
        }
    }
}
