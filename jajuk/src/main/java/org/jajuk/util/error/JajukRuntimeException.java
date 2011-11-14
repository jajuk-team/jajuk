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

/**
 * JajukException runtime exception : to be thown in case of internal bug only.
 */
public class JajukRuntimeException extends RuntimeException {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new jajuk runtime exception.
   */
  public JajukRuntimeException() {
    super();
  }

  /**
   * The Constructor.
   * 
   * @param message DOCUMENT_ME
   * @param cause DOCUMENT_ME
   */
  public JajukRuntimeException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * The Constructor.
   * 
   * @param message DOCUMENT_ME
   */
  public JajukRuntimeException(String message) {
    super(message);
  }

  /**
   * The Constructor.
   * 
   * @param cause DOCUMENT_ME
   */
  public JajukRuntimeException(Throwable cause) {
    super(cause);
  }
}
