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

package org.jajuk.ui.perspectives;

import java.io.IOException;

import net.infonode.docking.SplitWindow;
import net.infonode.docking.util.ViewMap;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.views.AnimationView;
import org.jajuk.ui.views.CoverView;
import org.jajuk.ui.views.IView;

/**
 *  Player perspective, contains view usefull to be displayed duriong playing
 *
 * @author     bflorat
 * @created    13 août 2004
 */
public class PlayerPerspective extends PerspectiveAdapter {
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.PerspectiveAdapter#setDefaultViews()
     */
    public void setDefaultViews() {
        ViewMap viewMap = new ViewMap();
		
		IView view = new AnimationView();
		net.infonode.docking.View dockingAnimationView = addView(view);
		viewMap.addView(0,dockingAnimationView);
		
	    view = new CoverView();
	    net.infonode.docking.View dockingCoverView1 = addView(view);
		viewMap.addView(3,dockingCoverView1);
		
		view = new CoverView();
	    net.infonode.docking.View dockingCoverView2 = addView(view);
		viewMap.addView(4,dockingCoverView2);
		
		SplitWindow vertCoversSplit = new SplitWindow(true,0.5f,dockingCoverView1,dockingCoverView2);
        SplitWindow horMainSplit = new SplitWindow(false,0.2f,dockingAnimationView,vertCoversSplit);
		
        setRootWindow(viewMap,horMainSplit);
        
        
	}
	
    /* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#commit()
	 */
	public void commit() throws IOException{
	    commit(FILE_PLAYER_PERSPECTIVE);
	}
	
	/* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.IPerspective#load()
     */
    public void load() throws IOException {
        load(FILE_PLAYER_PERSPECTIVE);
    }
    /* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.IPerspective#getDesc()
     */
    public String getDesc() {
        return Messages.getString("Perspective_Description_Player"); //$NON-NLS-1$
    }

}
