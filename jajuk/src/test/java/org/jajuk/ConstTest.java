/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
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
 *  
 */
package org.jajuk;

/**
 * Contains common constants shared by jajuk unit tests.
 */
public interface ConstTest {
  /** Temporary directory */
  String TEMP_PATH = System.getProperty("java.io.tmpdir");
  /** Path of all unit tests created files, dropped before each test **/
  String BASE_DIRECTORY_PATH = TEMP_PATH + "/jajuk_tests";
  /** Parent folder for sample devices (with music) */
  String DEVICES_BASE_PATH = BASE_DIRECTORY_PATH + "/sample_devices";
  /** Tech tests folder */
  String TECH_TESTS_PATH = BASE_DIRECTORY_PATH + "/tech_tests";
  /** Sample workspace path  */
  String SAMPLE_WORKSPACE_PATH = BASE_DIRECTORY_PATH + "/sample_workspace";
}
