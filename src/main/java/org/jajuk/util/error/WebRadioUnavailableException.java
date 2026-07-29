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
package org.jajuk.util.error;

import java.io.Serial;

/**
 * Exception thrown when a web radio stream is unavailable or unreachable.
 * This exception carries a user-friendly message for display in the UI.
 */
public class WebRadioUnavailableException extends JajukException {

  @Serial
  private static final long serialVersionUID = 1L;
  private static final int ERROR_CODE_RADIO_UNAVAILABLE = 8;

  public WebRadioUnavailableException(String url, String reason) {
    super(ERROR_CODE_RADIO_UNAVAILABLE,
            "Unable to contact webradio '" + url + "': " + reason);
  }

}