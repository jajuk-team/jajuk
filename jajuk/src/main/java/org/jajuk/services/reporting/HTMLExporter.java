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
 *  
 */

package org.jajuk.services.reporting;

import java.io.File;
import java.util.List;

import org.jajuk.base.Album;
import org.jajuk.base.Artist;
import org.jajuk.base.Device;
import org.jajuk.base.Directory;
import org.jajuk.base.Genre;
import org.jajuk.base.Item;
import org.jajuk.base.Year;
import org.jajuk.services.core.SessionService;
import org.jajuk.util.Const;
import org.jajuk.util.UtilSystem;

/**
 * This class exports music contents to HTML.
 */
public class HTMLExporter extends Exporter {

  /**
   * PUBLIC METHODS.
   */

  public HTMLExporter() {
    cache = SessionService.getConfFileByPath(Const.FILE_REPORTING_CACHE_FILE + "_html_"
        + System.currentTimeMillis());
  }

  /**
   * Process collection.
   * 
   * @param type 
   * 
   * @throws Exception the exception
   * 
   * @see Exporter.processColllection
   */
  @Override
  public void processCollection(int type) throws Exception {
    // Get an instance of the XMLExporter.
    XMLExporter xmlExporter = (XMLExporter) ExporterFactory.createExporter("xml");
    // If we are exporting the physical collection...
    if (type == PHYSICAL_COLLECTION) {
      // Create an xml tagging of the collection.
      xmlExporter.processCollection(PHYSICAL_COLLECTION);
      XMLTransformer.xmlToHTML(xmlExporter.getCacheFile(), cache, Const.XSLT_DEVICE);
      // Else if we are exporting the logical genre collection...
    } else if (type == LOGICAL_COLLECTION) {
      // Create an xml tagging of the collection.
      xmlExporter.setShowTracks(false);
      xmlExporter.processCollection(LOGICAL_COLLECTION);
      XMLTransformer.xmlToHTML(xmlExporter.getCacheFile(), cache, Const.XSLT_COLLECTION_LOGICAL);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.reporting.Exporter#process(java.util.List)
   */
  @Override
  public void process(List<Item> collection) throws Exception {
    // Get an instance of the XMLExporter.
    XMLExporter xmlExporter = (XMLExporter) ExporterFactory.createExporter("xml");
    // Create an xml tagging of this collection
    xmlExporter.process(collection);
    Item first = collection.get(0);
    if (first instanceof Genre) {
      XMLTransformer.xmlToHTML(xmlExporter.getCacheFile(), cache, Const.XSLT_GENRE);
    } else if (first instanceof Artist) {
      XMLTransformer.xmlToHTML(xmlExporter.getCacheFile(), cache, Const.XSLT_ARTIST);
    } else if (first instanceof Year) {
      XMLTransformer.xmlToHTML(xmlExporter.getCacheFile(), cache, Const.XSLT_YEAR);
    } else if (first instanceof Album) {
      XMLTransformer.xmlToHTML(xmlExporter.getCacheFile(), cache, Const.XSLT_ALBUM);
    } else if (first instanceof Device) {
      XMLTransformer.xmlToHTML(xmlExporter.getCacheFile(), cache, Const.XSLT_DEVICE);
    } else if (first instanceof Directory) {
      XMLTransformer.xmlToHTML(xmlExporter.getCacheFile(), cache, Const.XSLT_DIRECTORY);
    }
  }

  /* (non-Javadoc)
   * @see org.jajuk.services.reporting.Exporter#saveToFile(java.lang.String)
   */
  @Override
  public void saveToFile(String sPath) throws Exception {
    super.saveToFile(sPath);
    // Copy CSS files
    String sCSSAll = new File(sPath).getParent() + '/' + Const.FILE_REPORTING_CSS_ALL_FILENAME;
    String sCSSPrint = new File(sPath).getParent() + '/' + Const.FILE_REPORTING_CSS_PRINT_FILENAME;
    UtilSystem.copy(Const.FILE_REPORTING_CSS_ALL_PATH, sCSSAll);
    UtilSystem.copy(Const.FILE_REPORTING_CSS_PRINT_PATH, sCSSPrint);
  }
}
