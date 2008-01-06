/*
 *  Jajuk
 *  Copyright (C) 2006 The Jajuk Team
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
 *  $Revision: 2118 $
 */

package org.jajuk.services.reporting;

import java.io.File;
import java.util.ArrayList;

import org.jajuk.base.Item;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

/**
 * The base abstract class for exporting music contents to different formats.
 */
public abstract class Exporter implements ITechnicalStrings {

  /** Public Constants */
  public static final int PHYSICAL_COLLECTION = 0;

  public static final int LOGICAL_COLLECTION = 1;

  /** Cache file (used to handle concurrency issues), set by child classes */
  protected File cache;

  /**
   * This method will export the content to the specified sPath.
   * 
   * @param sPath
   *          The path of the file to export to. Will create it if it does not
   *          exist.
   */
  public void saveToFile(String sPath) throws Exception {
    // Create the final file from the cache file
    File out = new File(sPath);
    Util.copy(cache, out);
  }

  /**
   * This method will take a constant specifying what type of collection to
   * export.
   * 
   * @param type
   *          This XMLExporter constant specifies what type of collection we're
   *          exporting.
   */
  abstract public void processCollection(int type) throws Exception;

  /**
   * This methods will create an HTML String of items
   * 
   * @param collection
   *          An ArrayList of the items to export
   */
  abstract public void process(ArrayList<Item> collection) throws Exception;

  /**
   * 
   * @return the unique cache file used to create the XML temporary stream
   */
  public File getCacheFile() {
    return this.cache;
  }

}
