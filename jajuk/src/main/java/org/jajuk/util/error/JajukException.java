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
 *  $Revision$
 */
package org.jajuk.util.error;

import org.jajuk.util.Messages;

/**
 * JajukException.
 */
public class JajukException extends Exception {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** Error code. */
  private final int code;

  /**
   * JajukException constructor.
   * 
   * @param code DOCUMENT_ME
   */
  public JajukException(int code) {
    this(code, null, null);
  }

  /**
   * JajukException constructor.
   * 
   * @param pCause Original exception of the error.
   * @param code DOCUMENT_ME
   */
  public JajukException(int code, Throwable pCause) {
    this(code, null, pCause);
  }

  /**
   * Gets the code.
   * 
   * @return the code
   */
  public int getCode() {
    return this.code;
  }

  /**
   * JajukException constructor.
   * 
   * @param code Code of the current error.
   * @param pMessage Message.
   * @param pCause Original exception of the error.
   */
  public JajukException(int code, String pMessage, Throwable pCause) {
    super((pMessage != null && pMessage.length() > 0) ? Messages.getErrorMessage(code) + " : "
        + pMessage : Messages.getErrorMessage(code), pCause);
    this.code = code;
  }

  /**
   * JajukException constructor.
   * 
   * @param code Code of the current error.
   * @param pMessage Message.
   */
  public JajukException(int code, String pMessage) {
    super((pMessage != null && pMessage.length() > 0) ? Messages.getErrorMessage(code) + " : "
        + pMessage : Messages.getErrorMessage(code));
    this.code = code;
  }

}
