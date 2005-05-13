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
 *  $Revision$
 */

package org.jajuk.ui.perspectives;

import java.io.IOException;

import net.infonode.docking.SplitWindow;
import net.infonode.docking.util.ViewMap;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.views.CoverView;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.LogicalPlaylistEditorView;
import org.jajuk.ui.views.LogicalPlaylistRepositoryView;
import org.jajuk.ui.views.LogicalTableView;
import org.jajuk.ui.views.LogicalTreeView;

/**
 * Logical perspective
 *
 * @author     Bertrand Florat
 * @created    15 nov. 2003
 */
public class LogicalPerspective extends PerspectiveAdapter{
	
	/**
	 * Constructor
	 *
	 */
	public LogicalPerspective(){
		super();
	}

	/* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.PerspectiveAdapter#setDefaultViews()
     */
    public void setDefaultViews() {
        ViewMap viewMap = new ViewMap();
		IView view = new LogicalTreeView();
		net.infonode.docking.View dockingLogicalTreeView = addView(view);
		viewMap.addView(0,dockingLogicalTreeView);

        view = new LogicalTableView();
	    net.infonode.docking.View dockingLogicalTableView = addView(view);
		viewMap.addView(1,dockingLogicalTableView);
        
        view = new CoverView("1"); //$NON-NLS-1$
	    net.infonode.docking.View dockingCoverView = addView(view);
		viewMap.addView(2,dockingCoverView);
        
        view = new LogicalPlaylistRepositoryView();
	    net.infonode.docking.View dockingLogicalPlaylistRepository = addView(view);
		viewMap.addView(3,dockingLogicalPlaylistRepository);
        
        view = new LogicalPlaylistEditorView();
	    net.infonode.docking.View dockingPlaylistEditorView = addView(view);
		viewMap.addView(4,dockingPlaylistEditorView);
        
        SplitWindow vertPlaylistCoverSplit = new SplitWindow(true,0.5f,dockingPlaylistEditorView,dockingCoverView);
        SplitWindow horTableCoverSplit = new SplitWindow(false,0.5f,dockingLogicalTableView,vertPlaylistCoverSplit);
        SplitWindow verTreeRepositorySplit = new SplitWindow(true,0.75f,dockingLogicalTreeView,dockingLogicalPlaylistRepository);
        SplitWindow verMainSplit = new SplitWindow(true,0.4f,verTreeRepositorySplit,horTableCoverSplit);
		
        setRootWindow(viewMap,verMainSplit);
        
        
	}

    /* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#commit()
	 */
	public void commit() throws IOException{
	    commit(FILE_LOGICAL_PERSPECTIVE);
	}
	
	/* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.IPerspective#load()
     */
    public void load() throws IOException {
        load(FILE_LOGICAL_PERSPECTIVE);
    }
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IPerspective#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("Perspective_Description_Logical"); //$NON-NLS-1$
	}

}
