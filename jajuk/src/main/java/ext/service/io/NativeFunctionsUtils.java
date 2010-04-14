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
 *  $Revision$
 */
package ext.service.io;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.WString;

import org.jajuk.util.log.Log;

/*
 * Thanks to Paul Loy from the JNA mailing list ->
 * https://jna.dev.java.net/servlets/ReadMsg?list=users&msgNo=928
 * 
 * Requires: JNA https://jna.dev.java.net/#getting_started
 */
/**
 * DOCUMENT_ME.
 */
public class NativeFunctionsUtils {

  // private static final Logger logger = new Logger();

  // hiden utility class constructor
  /**
   * Instantiates a new native functions utils.
   */
  private NativeFunctionsUtils() {
    super();
  }

  /** DOCUMENT_ME. */
  private static Kernel32 nativelib;
  static {
    try {
      Native.setProtected(true);
      nativelib = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class);
    } catch (UnsatisfiedLinkError e) {
      Log.error(e);
    }
  }

  /** The Constant CHAR_BYTE_WIDTH.  DOCUMENT_ME */
  private static final int CHAR_BYTE_WIDTH = 2;

  /**
   * Returns the 8.3 (DOS) file-/pathname for a given file. Only available for
   * Windows, so check if this operating system is used before calling. The
   * filename must include the path as whole and be passed as String.
   * 
   * @param longPathName DOCUMENT_ME
   * 
   * @return File/Path in 8.3 format
   */
  public static synchronized String getShortPathNameW(String longPathName) {
    WString pathname = new WString(longPathName);
    int bufferSize = (pathname.length() * CHAR_BYTE_WIDTH) + CHAR_BYTE_WIDTH;
    Memory buffer = new Memory(bufferSize);

    if(nativelib == null) {
      return "";
    }

    if (nativelib.GetShortPathNameW(pathname, buffer, bufferSize) == 0) {
      return "";
    }
    return buffer.getString(0, true);
  }

}
