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
 * $Log$
 * Revision 1.1  2003/11/16 17:57:18  bflorat
 * 16/11/2003
 *
 */

package org.jajuk.ui.perspectives;

import org.jajuk.i18n.Messages;

/**
 * Help perspective
 *
 * @author     bflorat
 * @created    15 nov. 2003
 */
public class HelpPerspective extends PerspectiveAdapter{
	
	/**
	 * Constructor
	 *
	 */
	public HelpPerspective(){
		super(PERSPECTIVE_NAME_HELP,ICON_PERSPECTIVE_HELP);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IPerspective#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("Perspective_Description_Help");
	}

}
