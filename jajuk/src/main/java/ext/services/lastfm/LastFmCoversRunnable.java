/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

package ext.services.lastfm;

import java.awt.Image;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * The Class LastFmCoversRunnable.
 */
public class LastFmCoversRunnable implements Runnable {

  /** The listener. */
  ContextListener listener;

  /** The service. */
  private LastFmService service;

  /** The albums. */
  private List<? extends AlbumInfo> albums;

  /** The interrupted. */
  private volatile boolean interrupted;

  /** The id. */
  long id;

  /** The audio file. */
  AudioObject audioObject;

  /**
   * Instantiates a new audio scrobbler covers runnable.
   * 
   * @param listener the listener
   * @param service the service
   * @param albums the albums
   * @param id the id
   * @param audioObject DOCUMENT_ME
   */
  public LastFmCoversRunnable(ContextListener listener, LastFmService service,
      List<? extends AlbumInfo> albums, long id, AudioObject audioObject) {
    this.listener = listener;
    this.service = service;
    this.albums = albums;
    this.id = id;
    this.audioObject = audioObject;
  }

  /**
   * Interrupt.
   */
  public void interrupt() {
    interrupted = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run() {
    if (albums != null) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          listener.notifyStartRetrievingCovers(id);
        }
      });
      for (int i = 0; i < albums.size(); i++) {
        final Image img;
        final AlbumInfo album = albums.get(i);
        if (!interrupted) {
          img = service.getImage(album);
        } else {
          img = null;
        }

        if (!interrupted) {
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              listener.notifyCoverRetrieved(album, img, id);
            }
          });
        }
      }
    }
  }
}
