/*
 *  Jajuk
 *  Copyright (C) 2006 Bertrand Florat
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

import net.infonode.docking.util.ViewMap;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.views.WikipediaView;

/**
 *  Information perspective, display various information about played tracks
 *
 * @author     Bertrand Florat
 * @created    12/12/2005
 */
public class InfoPerspective extends PerspectiveAdapter {
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.PerspectiveAdapter#setDefaultViews()
     */
    public void setDefaultViews() {
        ViewMap viewMap = new ViewMap();
		
		IView view = new WikipediaView();
		net.infonode.docking.View dockingCatalogView = addView(view);
		viewMap.addView(0,dockingCatalogView);
		
        setRootWindow(viewMap,dockingCatalogView);
  }
	
    /* (non-Javadoc)
	 * @see org.jajuk.ui.perspectives.IPerspective#commit()
	 */
	public void commit() throws IOException{
	    commit(FILE_INFO_PERSPECTIVE);
	}
	
	/* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.IPerspective#load()
     */
    public void load() throws IOException {
        load(FILE_INFO_PERSPECTIVE);
    }
    /* (non-Javadoc)
     * @see org.jajuk.ui.perspectives.IPerspective#getDesc()
     */
    public String getDesc() {
        return Messages.getString("Perspective_Description_Info"); //$NON-NLS-1$
    }

}
