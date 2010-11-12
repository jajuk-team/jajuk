/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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
 *  $Revision: 5520 $
 */
package org.jajuk.util.error;

/**
 * JajukException runtime exception : to be thown in case of internal bug only
 */
public class JajukRuntimeException extends RuntimeException {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public JajukRuntimeException() {
    super();
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   * @param cause
   */
  public JajukRuntimeException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param message
   */
  public JajukRuntimeException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param cause
   */
  public JajukRuntimeException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

}
