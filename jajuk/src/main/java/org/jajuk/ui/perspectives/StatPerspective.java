/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
 *  http://jajuk.info
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

import javax.swing.ImageIcon;

import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;

/**
 * Statistics perspective.
 */
public class StatPerspective extends PerspectiveAdapter {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IPerspective#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("Perspective_Description_Statistics");
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.perspectives.IPerspective#getIcon()
   */
  @Override
  public ImageIcon getIcon() {
    return IconLoader.getIcon(JajukIcons.PERSPECTIVE_STATISTICS);
  }
}
