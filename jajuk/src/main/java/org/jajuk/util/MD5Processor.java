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
package org.jajuk.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.jajuk.util.log.Log;

/**
 * Convenient class to compute MD5 hashcodes and return them using predefined
 * base.
 */
public final class MD5Processor {
  private static MessageDigest msgDigest;
  static {
    try {
      msgDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      Log.error(e);
    }
  }

  /**
   * private constructor to avoid instantiating utility class.
   */
  private MD5Processor() {
  }

  /**
   * MD5 hashcoding, return a hashcode.
   * 
   * @param sIn input String
   * 
   * @return hashed output
   */
  public static final String hash(String sIn) {
    try {
      msgDigest.update(sIn.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // This cannot normaly happen
      Log.error(e);
      return null;
    }
    byte[] digest = msgDigest.digest();
    // Important : we internalize the result of this computation
    // because all equals between items is done with '==' operator
    // against strings and new discovered items call this method
    return new BigInteger(digest).abs().toString(36).intern();
  }
}
