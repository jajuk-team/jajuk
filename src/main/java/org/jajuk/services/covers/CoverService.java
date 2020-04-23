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
package org.jajuk.services.covers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.jajuk.services.covers.providers.DiscogsCoversProvider;
import org.jajuk.services.covers.providers.ICoversProvider;

/**
 * Covers retrieval service. This service will retrieves covers image links from
 * a provider.
 * 
 * TODO: For now the covers provider is static and instantiated directly in this
 * class. It could be better :<br>
 * - to have another provider in case discogs website change<br>
 * and/or<br>
 * - to externalise the provider url and the pattern in a resource file which
 * could be downloaded from resources project by example. This could make it
 * easier to change without releasing a new Jajuk version.
 */
public class CoverService {

  // Default Provider
  private static ICoversProvider coversProvider = new DiscogsCoversProvider();

  /**
   * Gets the remote covers list.
   * 
   * @param search
   * 
   * @return a list of urls
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public static List<URL> getRemoteCoversList(String search) throws IOException {
    return coversProvider.getRemoteCoversList(search);
  }

  public static ICoversProvider getCoversProvider() {
    return coversProvider;
  }

  public static void setCoversProvider(ICoversProvider newCoversProvider) {
    coversProvider = newCoversProvider;
  }

}
