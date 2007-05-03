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

import java.math.BigInteger;

/**
 * This classes provides some hashing code. It contains mainly an implementation
 * of MD5 algorithm and some other trivial hashing methods. The implementation
 * follows RFC 1321 and passes all RFC 1321 tests.
 */

public class MD5Processor {

	private static final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * MD5 hashcoding, return a hashcode in a radix 36 string format
	 * 
	 * @param sIn
	 *            input String
	 * @return hashed output
	 */
	public static final String hash(String sIn) {
		BigInteger bi = new BigInteger(hash(sIn.getBytes()));
		return bi.abs().toString(36);

	}

	/**
	 * MD5 hashcoding, return a hashcode for the given string Follows RFC 1321
	 * 
	 * @param bIn
	 *            input byte array
	 * @return hashed output as byte array
	 */
	public static final byte[] hash(byte[] bIn) {
		byte[] bOut = new byte[16];
		// Step 1: padding
		int q = (bIn.length - 55) / 64;
		if (bIn.length > 55) {
			q++;
		}
		byte[] b1 = new byte[64 * (q + 1)];
		System.arraycopy(bIn, 0, b1, 0, bIn.length);
		b1[bIn.length] = (byte) 0x80;
		// step 2: Append length (in bits )
		long l = bIn.length * 8;
		// b=[b0b1b2b3 b4b5b6b7]
		// b length appends as: [b7b6b5b4 b3b2b1b0]
		b1[b1.length - 1] = (byte) (l >>> 56 & 0xFF);
		b1[b1.length - 2] = (byte) (l >>> 48 & 0xFF);
		b1[b1.length - 3] = (byte) (l >>> 40 & 0xFF);
		b1[b1.length - 4] = (byte) (l >>> 32 & 0xFF);
		b1[b1.length - 5] = (byte) (l >>> 24 & 0xFF);
		b1[b1.length - 6] = (byte) (l >>> 16 & 0xFF);
		b1[b1.length - 7] = (byte) (l >>> 8 & 0xFF);
		b1[b1.length - 8] = (byte) (l & 0xFF);

		// step 3 Initialize MD Buffer
		int a = 0x67452301;
		int b = 0xefcdab89;
		int c = 0x98badcfe;
		int d = 0x10325476;

		// Step 4. Process Message in 16-Word Blocks
		int n = b1.length / 4; // n=nb 32 bits-word we have in the padded
		// message
		int[] x;
		int aa = a;
		int bb = b;
		int cc = c;
		int dd = d;
		for (int i = 0; i < (n / 16); i++) { // for each packet of 512 bits (
			// 64 bytes )
			x = new int[16];
			for (int j = 0; j < 16; j++) {
				x[j] = (((b1[64 * i + 4 * j] & 0xFF))
						| ((b1[64 * i + 4 * j + 1] & 0xFF) << 8)
						| ((b1[64 * i + 4 * j + 2] & 0xFF) << 16) | ((b1[64 * i
						+ 4 * j + 3] & 0xFF) << 24));

			}
			aa = a;
			bb = b;
			cc = c;
			dd = d;

			/* Round 1 */
			a = op(0, a, b, c, d, x[0], 7, 0xd76aa478);
			d = op(0, d, a, b, c, x[1], 12, 0xe8c7b756);
			c = op(0, c, d, a, b, x[2], 17, 0x242070db);
			b = op(0, b, c, d, a, x[3], 22, 0xc1bdceee);
			a = op(0, a, b, c, d, x[4], 7, 0xf57c0faf);
			d = op(0, d, a, b, c, x[5], 12, 0x4787c62a);
			c = op(0, c, d, a, b, x[6], 17, 0xa8304613);
			b = op(0, b, c, d, a, x[7], 22, 0xfd469501);
			a = op(0, a, b, c, d, x[8], 7, 0x698098d8);
			d = op(0, d, a, b, c, x[9], 12, 0x8b44f7af);
			c = op(0, c, d, a, b, x[10], 17, 0xffff5bb1);
			b = op(0, b, c, d, a, x[11], 22, 0x895cd7be);
			a = op(0, a, b, c, d, x[12], 7, 0x6b901122);
			d = op(0, d, a, b, c, x[13], 12, 0xfd987193);
			c = op(0, c, d, a, b, x[14], 17, 0xa679438e);
			b = op(0, b, c, d, a, x[15], 22, 0x49b40821);

			/* Round 2 */
			a = op(1, a, b, c, d, x[1], 5, 0xf61e2562);
			d = op(1, d, a, b, c, x[6], 9, 0xc040b340);
			c = op(1, c, d, a, b, x[11], 14, 0x265e5a51);
			b = op(1, b, c, d, a, x[0], 20, 0xe9b6c7aa);
			a = op(1, a, b, c, d, x[5], 5, 0xd62f105d);
			d = op(1, d, a, b, c, x[10], 9, 0x2441453);
			c = op(1, c, d, a, b, x[15], 14, 0xd8a1e681);
			b = op(1, b, c, d, a, x[4], 20, 0xe7d3fbc8);
			a = op(1, a, b, c, d, x[9], 5, 0x21e1cde6);
			d = op(1, d, a, b, c, x[14], 9, 0xc33707d6);
			c = op(1, c, d, a, b, x[3], 14, 0xf4d50d87);
			b = op(1, b, c, d, a, x[8], 20, 0x455a14ed);
			a = op(1, a, b, c, d, x[13], 5, 0xa9e3e905);
			d = op(1, d, a, b, c, x[2], 9, 0xfcefa3f8);
			c = op(1, c, d, a, b, x[7], 14, 0x676f02d9);
			b = op(1, b, c, d, a, x[12], 20, 0x8d2a4c8a);

			/* Round 3 */
			a = op(2, a, b, c, d, x[5], 4, 0xfffa3942);
			d = op(2, d, a, b, c, x[8], 11, 0x8771f681);
			c = op(2, c, d, a, b, x[11], 16, 0x6d9d6122);
			b = op(2, b, c, d, a, x[14], 23, 0xfde5380c);
			a = op(2, a, b, c, d, x[1], 4, 0xa4beea44);
			d = op(2, d, a, b, c, x[4], 11, 0x4bdecfa9);
			c = op(2, c, d, a, b, x[7], 16, 0xf6bb4b60);
			b = op(2, b, c, d, a, x[10], 23, 0xbebfbc70);
			a = op(2, a, b, c, d, x[13], 4, 0x289b7ec6);
			d = op(2, d, a, b, c, x[0], 11, 0xeaa127fa);
			c = op(2, c, d, a, b, x[3], 16, 0xd4ef3085);
			b = op(2, b, c, d, a, x[6], 23, 0x4881d05);
			a = op(2, a, b, c, d, x[9], 4, 0xd9d4d039);
			d = op(2, d, a, b, c, x[12], 11, 0xe6db99e5);
			c = op(2, c, d, a, b, x[15], 16, 0x1fa27cf8);
			b = op(2, b, c, d, a, x[2], 23, 0xc4ac5665);

			/* Round 4 */
			a = op(3, a, b, c, d, x[0], 6, 0xf4292244);
			d = op(3, d, a, b, c, x[7], 10, 0x432aff97);
			c = op(3, c, d, a, b, x[14], 15, 0xab9423a7);
			b = op(3, b, c, d, a, x[5], 21, 0xfc93a039);
			a = op(3, a, b, c, d, x[12], 6, 0x655b59c3);
			d = op(3, d, a, b, c, x[3], 10, 0x8f0ccc92);
			c = op(3, c, d, a, b, x[10], 15, 0xffeff47d);
			b = op(3, b, c, d, a, x[1], 21, 0x85845dd1);
			a = op(3, a, b, c, d, x[8], 6, 0x6fa87e4f);
			d = op(3, d, a, b, c, x[15], 10, 0xfe2ce6e0);
			c = op(3, c, d, a, b, x[6], 15, 0xa3014314);
			b = op(3, b, c, d, a, x[13], 21, 0x4e0811a1);
			a = op(3, a, b, c, d, x[4], 6, 0xf7537e82);
			d = op(3, d, a, b, c, x[11], 10, 0xbd3af235);
			c = op(3, c, d, a, b, x[2], 15, 0x2ad7d2bb);
			b = op(3, b, c, d, a, x[9], 21, 0xeb86d391);

			a = unsignedAdd(a, aa);
			b = unsignedAdd(b, bb);
			c = unsignedAdd(c, cc);
			d = unsignedAdd(d, dd);
		}
		// step 5 output
		int k;
		for (k = 0; k < 4; k++) {
			bOut[k] = (byte) ((a >>> (8 * k)) & 0xFF);
		}
		for (k = 0; k < 4; k++) {
			bOut[k + 4] = (byte) ((b >>> (8 * k)) & 0xFF);
		}
		for (k = 0; k < 4; k++) {
			bOut[k + 8] = (byte) ((c >>> (8 * k)) & 0xFF);
		}
		for (k = 0; k < 4; k++) {
			bOut[k + 12] = (byte) ((d >>> (8 * k)) & 0xFF);
		}

		return bOut;

	}

	/**
	 * MD5 internal function
	 */
	/*
	 * types: 0: F(X,Y,Z) = XY v not(X) Z 1: G(X,Y,Z) = XZ v Y not(Z) 2:
	 * H(X,Y,Z) = X xor Y xor Z 3: I(X,Y,Z) = Y xor (X v not(Z)) Let [abcd k s
	 * i] denote the operation a = b + ((a + F(b,c,d) + X[k] + T[i]) <<< s).
	 */
	private static int op(int iType, int a, int b, int c, int d, int x_k,
			int s, int t_i) {
		int i2 = a;
		switch (iType) {
		case 0: // f function
			i2 = unsignedAdd(i2, (b & c) | ((~b) & (d)));
			break;
		case 1: // g function
			i2 = unsignedAdd(i2, (b & d) | (c & (~d)));
			break;
		case 2: // h function
			i2 = unsignedAdd(i2, (b) ^ (c) ^ (d));
			break;
		case 3: // i function
			i2 = unsignedAdd(i2, (c) ^ ((b) | (~d)));
			break;
		}
		i2 = unsignedAdd(i2, x_k);
		i2 = unsignedAdd(i2, t_i);
		i2 = rcl(i2, s);
		return unsignedAdd(b, i2);
	}

	/**
	 * Left rotation ( <<< )
	 */
	private static int rcl(int i, int j) {
		return (i << j) | (i >>> (32 - j));
	}

	/**
	 * Non-signed addition. ints are signed, use long instead before computing
	 * and come back to int after
	 */
	private static int unsignedAdd(int i1, int i2) {
		long l1, l2;
		l1 = i1 & 0xffffffffL;
		l2 = i2 & 0xffffffffL;
		return (int) ((l1 + l2) & 0xffffffffL);
	}

	/**
	 * Get a string hexa representation for a byte array
	 */
	public static String toHex(byte[] b) {

		char[] cOut = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			cOut[2 * i] = hex[(b[i] & 0xFF) >>> 4];
			cOut[2 * i + 1] = hex[b[i] & 0xF];
		}
		return new String(cOut);
	}

}
