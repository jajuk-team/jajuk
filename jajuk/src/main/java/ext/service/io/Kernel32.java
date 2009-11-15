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
/**
 * DOCUMENT_ME.
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
   * __out LPTSTR lpdzShortPath,
   * __in DWORD cchBuffer );
   * </code>.
   * 
   * @param inPath DOCUMENT_ME
   * @param outPathBuffer DOCUMENT_ME
   * @param outPathBufferSize DOCUMENT_ME
   * 
   * @return the int
   */
  public int GetShortPathNameW(WString inPath, Memory outPathBuffer, int outPathBufferSize);

}
