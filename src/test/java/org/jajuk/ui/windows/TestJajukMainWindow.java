/*
 *  Jajuk
 *  Copyright (C) 2003-2015 The Jajuk Team
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
 */
package org.jajuk.ui.windows;

import static org.junit.Assert.assertEquals;

import java.awt.Rectangle;

import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.junit.Test;


public class TestJajukMainWindow {
 
  @Test
  /** Issue  Main window no more visible after multi-screen manipulations #1992 */
  public void testNegativePositions() {
     JajukMainWindow window = JajukMainWindow.getInstance();
     Conf.setProperty(Const.CONF_WINDOW_POSITION, "-32000,-32000,1480,780");
     Rectangle rect = window.getStoredPosition();
     assertEquals("X different de zero",0, rect.x);
     assertEquals("Y different de zero",0, rect.y);
  }
}
