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

import org.jajuk.util.UtilString;

/**
 * The Class ScrobblerException.
 */
public class ScrobblerException extends Exception {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -7383228060002549115L;

  /** DOCUMENT_ME. */
  private int status = -1;

  /**
   * Instantiates a new scrobbler exception.
   * 
   * @param cause the cause
   */
  public ScrobblerException(String cause) {
    super(UtilString.concat("Error submitting to Last.fm. Cause: ", cause));
  }

  /**
   * Instantiates a new scrobbler exception.
   * 
   * @param status DOCUMENT_ME
   */
  public ScrobblerException(int status) {
    super(UtilString.concat("Error submitting to Last.fm. Status: ", Integer.valueOf(status)));
    this.status = status;
  }

  /**
   * Returns the status (-1 if no status was set).
   * 
   * @return the status
   */
  public int getStatus() {
    return status;
  }

}
