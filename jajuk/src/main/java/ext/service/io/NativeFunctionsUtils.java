/*
 * Common-jukebox
 * Copyright (C) 2008 Common-jukebox team
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
 * 
 * Initially created from aTunes 1.8.2
 * Copyright (C) 2006-2008 Alex Aranda, Sylvain Gaudard, Thomas Beckers and contributors
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
public class NativeFunctionsUtils {

  // private static final Logger logger = new Logger();

  // hiden utility class constructor
  private NativeFunctionsUtils() {
    super();
  }

  private static Kernel32 nativelib;
  static {
    try {
      Native.setProtected(true);
      nativelib = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class);
    } catch (UnsatisfiedLinkError e) {
      Log.error(e);
    }
  }

  private static final int CHAR_BYTE_WIDTH = 2;

  /**
   * Returns the 8.3 (DOS) file-/pathname for a given file. Only avaible for
   * Windows, so check if this operating system is used before calling. The
   * filename must include the path as whole and be passed as String.
   * 
   * @param longPathName
   * @return File/Path in 8.3 format
   */
  public static String getShortPathNameW(String longPathName) {
    WString pathname = new WString(longPathName);
    int bufferSize = (pathname.length() * CHAR_BYTE_WIDTH) + CHAR_BYTE_WIDTH;
    Memory buffer = new Memory(bufferSize);

    if (nativelib.GetShortPathNameW(pathname, buffer, bufferSize) == 0) {
      return "";
    }
    return buffer.getString(0, true);
  }

}
