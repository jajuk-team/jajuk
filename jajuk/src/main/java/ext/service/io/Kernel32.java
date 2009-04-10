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

// JNA library
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.WString;

/*
 * Thanks to Paul Loy from the JNA mailing list ->
 * https://jna.dev.java.net/servlets/ReadMsg?list=users&msgNo=928
 * 
 * Requires: JNA https://jna.dev.java.net/#getting_started
 */
public interface Kernel32 extends Library {

  /*
   * http://msdn2.microsoft.com/en-us/library/aa364989(VS.85).aspx
   */
  /**
   * <p>
   * Unicode (wchar_t*) version of GetShortPathName()
   * </p>
   * <code>
   * DWORD WINAPI GetShortPathNameW( __in LPCTSTR lpszLongPath,
   *                                 __out LPTSTR lpdzShortPath,
   *                                 __in DWORD cchBuffer );
   * </code>
   */
  public int GetShortPathNameW(WString inPath, Memory outPathBuffer, int outPathBufferSize);

}
