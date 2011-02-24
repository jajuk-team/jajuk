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
 *  $Revision: 3132 $
 */
package org.jajuk;

import java.io.File;

/**
 * Contains common constants shared by jajuk unit tests 
 */
public interface ConstTest {

  // sometimes temporary path has trailing separator, we try to handle this here
  String PATH_DEVICE = (System.getProperty("java.io.tmpdir").endsWith(File.separator) ? System
      .getProperty("java.io.tmpdir")
      + "jajuk_tests" + File.separator + "device_1" : System.getProperty("java.io.tmpdir")
      + File.separator + "jajuk_tests" + File.separator + "device_1");

}
