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

package org.jajuk.ui.perspectives;

import java.util.HashSet;
import java.util.Set;

import org.jajuk.i18n.Messages;
import org.jajuk.ui.IView;
import org.jajuk.ui.views.StatView;
import org.jajuk.ui.views.ViewFactory;

/**
 * Statistics perspective
 * 
 * @author Bertrand Florat
 * @created 15 nov. 2003
 */
public class StatPerspective extends PerspectiveAdapter {

	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IPerspective#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("Perspective_Description_Statistics"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IPerspective#getViews()
	 */
	public Set<IView> getViews() {
		if (views != null) {
			return views;
		}
		views = new HashSet<IView>(1);
		IView view = ViewFactory.createView(StatView.class, this);
		views.add(view);
		return views;
	}

}
