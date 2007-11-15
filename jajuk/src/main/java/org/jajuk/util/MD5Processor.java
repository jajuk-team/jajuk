/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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
 *  $$Revision$$
 */
package org.jajuk.util;

import org.jajuk.util.log.Log;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Convenient class to compute MD5 hascodes and return them using predefined
 * base
 */

public class MD5Processor {

	private static final BigInteger vbase = new BigInteger("10128");

	/**
	 * MD5 hashcoding, return a hashcode
	 * 
	 * @param sIn
	 *            input String
	 * @return hashed output
	 */
	public static final String hash(String sIn) {
		java.security.MessageDigest msgDigest;
		try {
			msgDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.error(e);
			return null;
		}
		msgDigest.update(sIn.getBytes());
		byte[] digest = msgDigest.digest();
		BigInteger bi = new BigInteger(digest);
		StringBuilder sb = new StringBuilder(11);
		BigInteger q = bi;
		do {
			sb.append((char) (q.mod(vbase).intValue() + 0xB000));
			q = q.divide(vbase);
		} while (q.intValue() != 0);
		return sb.toString().intern();
	}
}
