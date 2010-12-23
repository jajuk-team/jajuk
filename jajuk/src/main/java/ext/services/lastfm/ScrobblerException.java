/*
 * Adapted by Jajuk team
 * Copyright (C) 2003-2009 the Jajuk Team
 * http://jajuk.info
 * 
 * aTunes 1.14.0
 * Copyright (C) 2006-2009 Alex Aranda, Sylvain Gaudard, Thomas Beckers and contributors
 *
 * See http://www.atunes.org/wiki/index.php?title=Contributing for information about contributors
 *
 * http://www.atunes.org
 * http://sourceforge.net/projects/atunes
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
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
