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
package ext;

/**
 * <h3>MersenneTwister and MersenneTwisterFast</h3>
 * <p><b>Version 13</b>, based on version MT199937(99/10/29)
 * of the Mersenne Twister algorithm found at
 * <a href="http://www.math.keio.ac.jp/matumoto/emt.html">
 * The Mersenne Twister Home Page</a>, with the initialization
 * improved using the new 2002/1/26 initialization algorithm
 * By Sean Luke, October 2004.
 * 
 * <p><b>MersenneTwister</b> is a drop-in subclass replacement
 * for java.util.Random.  It is properly synchronized and
 * can be used in a multithreaded environment.  On modern VMs such
 * as HotSpot, it is approximately 1/3 slower than java.util.Random.
 * 
 * <p><b>MersenneTwisterFast</b> is not a subclass of java.util.Random.  It has
 * the same public methods as Random does, however, and it is
 * algorithmically identical to MersenneTwister.  MersenneTwisterFast
 * has hard-code inlined all of its methods directly, and made all of them
 * final (well, the ones of consequence anyway).  Further, these
 * methods are <i>not</i> synchronized, so the same MersenneTwisterFast
 * instance cannot be shared by multiple threads.  But all this helps
 * MersenneTwisterFast achieve well over twice the speed of MersenneTwister.
 * java.util.Random is about 1/3 slower than MersenneTwisterFast.
 * 
 * <h3>About the Mersenne Twister</h3>
 * <p>This is a Java version of the C-program for MT19937: Integer version.
 * The MT19937 algorithm was created by Makoto Matsumoto and Takuji Nishimura,
 * who ask: "When you use this, send an email to: matumoto@math.keio.ac.jp
 * with an appropriate reference to your work".  Indicate that this
 * is a translation of their algorithm into Java.
 * 
 * <p><b>Reference. </b>
 * Makato Matsumoto and Takuji Nishimura,
 * "Mersenne Twister: A 623-Dimensionally Equidistributed Uniform
 * Pseudo-Random Number Generator",
 * <i>ACM Transactions on Modeling and Computer Simulation,</i>
 * Vol. 8, No. 1, January 1998, pp 3--30.
 * 
 * <h3>About this Version</h3>
 * 
 * <p><b>Changes Since V12:</b> clone() method added.
 * 
 * <p><b>Changes Since V11:</b> stateEquals(...) method added.  MersenneTwisterFast
 * is equal to other MersenneTwisterFasts with identical state; likewise
 * MersenneTwister is equal to other MersenneTwister with identical state.
 * This isn't equals(...) because that requires a contract of immutability
 * to compare by value.
 * 
 * <p><b>Changes Since V10:</b> A documentation error suggested that
 * setSeed(int[]) required an int[] array 624 long.  In fact, the array
 * can be any non-zero length.  The new version also checks for this fact.
 * 
 * <p><b>Changes Since V9:</b> readState(stream) and writeState(stream)
 * provided.
 * 
 * <p><b>Changes Since V8:</b> setSeed(int) was only using the first 28 bits
 * of the seed; it should have been 32 bits.  For small-number seeds the
 * behavior is identical.
 * 
 * <p><b>Changes Since V7:</b> A documentation error in MersenneTwisterFast
 * (but not MersenneTwister) stated that nextDouble selects uniformly from
 * the full-open interval [0,1].  It does not.  nextDouble's contract is
 * identical across MersenneTwisterFast, MersenneTwister, and java.util.Random,
 * namely, selection in the half-open interval [0,1).  That is, 1.0 should
 * not be returned.  A similar contract exists in nextFloat.
 * 
 * <p><b>Changes Since V6:</b> License has changed from LGPL to BSD.
 * New timing information to compare against
 * java.util.Random.  Recent versions of HotSpot have helped Random increase
 * in speed to the point where it is faster than MersenneTwister but slower
 * than MersenneTwisterFast (which should be the case, as it's a less complex
 * algorithm but is synchronized).
 * 
 * <p><b>Changes Since V5:</b> New empty constructor made to work the same
 * as java.util.Random -- namely, it seeds based on the current time in
 * milliseconds.
 * 
 * <p><b>Changes Since V4:</b> New initialization algorithms.  See
 * (see <a href="http://www.math.keio.ac.jp/matumoto/MT2002/emt19937ar.html"</a>
 * http://www.math.keio.ac.jp/matumoto/MT2002/emt19937ar.html</a>)
 * 
 * <p>The MersenneTwister code is based on standard MT19937 C/C++
 * code by Takuji Nishimura,
 * with suggestions from Topher Cooper and Marc Rieffel, July 1997.
 * The code was originally translated into Java by Michael Lecuyer,
 * January 1999, and the original code is Copyright (c) 1999 by Michael Lecuyer.
 * 
 * <h3>Java notes</h3>
 * 
 * <p>This implementation implements the bug fixes made
 * in Java 1.2's version of Random, which means it can be used with
 * earlier versions of Java.  See
 * <a href="http://www.javasoft.com/products/jdk/1.2/docs/api/java/util/Random.html">
 * the JDK 1.2 java.util.Random documentation</a> for further documentation
 * on the random-number generation contracts made.  Additionally, there's
 * an undocumented bug in the JDK java.util.Random.nextBytes() method,
 * which this code fixes.
 * 
 * <p> Just like java.util.Random, this
 * generator accepts a long seed but doesn't use all of it.  java.util.Random
 * uses 48 bits.  The Mersenne Twister instead uses 32 bits (int size).
 * So it's best if your seed does not exceed the int range.
 * 
 * <p>MersenneTwister can be used reliably
 * on JDK version 1.1.5 or above.  Earlier Java versions have serious bugs in
 * java.util.Random; only MersenneTwisterFast (and not MersenneTwister nor
 * java.util.Random) should be used with them.
 * 
 * <h3>License</h3>
 * 
 * Copyright (c) 2003 by Sean Luke. <br>
 * Portions copyright (c) 1993 by Michael Lecuyer. <br>
 * All rights reserved. <br>
 * 
 * <p>Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <ul>
 * <li> Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * <li> Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * <li> Neither the name of the copyright owners, their employers, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * </ul>
 * <p>THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * @version 13
 */

public final class MersenneTwister extends java.util.Random {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  // Period parameters
  /** The Constant N.  DOCUMENT_ME */
  private static final int N = 624;

  /** The Constant M.  DOCUMENT_ME */
  private static final int M = 397;

  /** The Constant MATRIX_A.  DOCUMENT_ME */
  private static final int MATRIX_A = 0x9908b0df; // private static final * constant vector a

  /** The Constant UPPER_MASK.  DOCUMENT_ME */
  private static final int UPPER_MASK = 0x80000000; // most significant w-r bits

  /** The Constant LOWER_MASK.  DOCUMENT_ME */
  private static final int LOWER_MASK = 0x7fffffff; // least significant r bits

  // Tempering parameters
  /** The Constant TEMPERING_MASK_B.  DOCUMENT_ME */
  private static final int TEMPERING_MASK_B = 0x9d2c5680;

  /** The Constant TEMPERING_MASK_C.  DOCUMENT_ME */
  private static final int TEMPERING_MASK_C = 0xefc60000;

  /** DOCUMENT_ME. */
  private int mt[]; // the array for the state vector

  /** DOCUMENT_ME. */
  private int mti; // mti==N+1 means mt[N] is not initialized

  /** DOCUMENT_ME. */
  private int mag01[];

  /** Singleton. */
  private static MersenneTwister self = new MersenneTwister();

  /**
   * Return the singleton.
   *
   * @return the singleton
   */
  public static MersenneTwister getInstance() {
    return self;
  }

  /**
   * Constructor using the default seed.
   */
  private MersenneTwister() {
    this(System.currentTimeMillis());
  }

  /**
   * Constructor using a given seed.  Though you pass this seed in
   * as a long, it's best to make sure it's actually an integer.
   * 
   * @param seed DOCUMENT_ME
   */
  private MersenneTwister(final long seed) {
    super(seed); /* just in case */
    setSeed(seed);
  }

  /**
   * Initialize the pseudo random number generator.  Don't
   * pass in a long that's bigger than an int (Mersenne Twister
   * only uses the first 32 bits for its seed).
   * 
   * @param seed DOCUMENT_ME
   */

  @Override
  public final void setSeed(final long seed) {
    // it's always good style to call super
    super.setSeed(seed);
    mt = new int[N];
    mag01 = new int[2];
    mag01[0] = 0x0;
    mag01[1] = MATRIX_A;
    mt[0] = (int) (seed & 0xffffffff);
    for (mti = 1; mti < N; mti++) {
      mt[mti] = (1812433253 * (mt[mti - 1] ^ (mt[mti - 1] >>> 30)) + mti);
      /* See Knuth TAOCP Vol2. 3rd Ed. P.106 for multiplier. */
      /* In the previous versions, MSBs of the seed affect */
      /* only MSBs of the array mt[]. */
      /* 2002/01/09 modified by Makoto Matsumoto */
      mt[mti] &= 0xffffffff;
      /* for >32 bit machines */
    }
  }

  /**
  * Returns an integer with <i>bits</i> bits filled with a random number.
  * 
  * @param bits DOCUMENT_ME
  * 
  * @return the int
  */
  @Override
  synchronized protected int next(final int bits) {
    int y;

    if (mti >= N) // generate N words at one time
    {
      int kk;
      final int[] mt = this.mt; // locals are slightly faster
      final int[] mag01 = this.mag01; // locals are slightly faster

      for (kk = 0; kk < N - M; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + M] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      for (; kk < N - 1; kk++) {
        y = (mt[kk] & UPPER_MASK) | (mt[kk + 1] & LOWER_MASK);
        mt[kk] = mt[kk + (M - N)] ^ (y >>> 1) ^ mag01[y & 0x1];
      }
      y = (mt[N - 1] & UPPER_MASK) | (mt[0] & LOWER_MASK);
      mt[N - 1] = mt[M - 1] ^ (y >>> 1) ^ mag01[y & 0x1];

      mti = 0;
    }

    y = mt[mti++];
    y ^= y >>> 11; // TEMPERING_SHIFT_U(y)
    y ^= (y << 7) & TEMPERING_MASK_B; // TEMPERING_SHIFT_S(y)
    y ^= (y << 15) & TEMPERING_MASK_C; // TEMPERING_SHIFT_T(y)
    y ^= (y >>> 18); // TEMPERING_SHIFT_L(y)

    return y >>> (32 - bits); // hope that's right!
  }

  /*
   * If you've got a truly old version of Java, you can omit these two next methods.
   */

  /**
   * This method is missing from jdk 1.0.x and below.  JDK 1.1
   * includes this for us, but what the heck.
   * 
   * @return true, if next boolean
   */
  @Override
  public boolean nextBoolean() {
    return next(1) != 0;
  }

  /**
   * This generates a coin flip with a probability <tt>probability</tt>
   * of returning true, else returning false. <tt>probability</tt> must
   * be between 0.0 and 1.0, inclusive.  Not as precise a random real
   * event as nextBoolean(double), but twice as fast. To explicitly
   * use this, remember you may need to cast to float first.
   * 
   * @param probability DOCUMENT_ME
   * 
   * @return true, if next boolean
   */

  public boolean nextBoolean(final float probability) {
    if (probability < 0.0f || probability > 1.0f)
      throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
    if (probability == 0.0f)
      return false; // fix half-open issues
    else if (probability == 1.0f)
      return true; // fix half-open issues
    return nextFloat() < probability;
  }

  /**
   * This generates a coin flip with a probability <tt>probability</tt>
   * of returning true, else returning false. <tt>probability</tt> must
   * be between 0.0 and 1.0, inclusive.
   * 
   * @param probability DOCUMENT_ME
   * 
   * @return true, if next boolean
   */

  public boolean nextBoolean(final double probability) {
    if (probability < 0.0 || probability > 1.0)
      throw new IllegalArgumentException("probability must be between 0.0 and 1.0 inclusive.");
    if (probability == 0.0)
      return false; // fix half-open issues
    else if (probability == 1.0)
      return true; // fix half-open issues
    return nextDouble() < probability;
  }

  /**
   * This method is missing from JDK 1.1 and below.  JDK 1.2
   * includes this for us, but what the heck.
   * 
   * @param n DOCUMENT_ME
   * 
   * @return the int
   */

  @Override
  public int nextInt(final int n) {
    if (n <= 0)
      throw new IllegalArgumentException("n must be > 0");

    if ((n & -n) == n)
      return (int) ((n * (long) next(31)) >> 31);

    int bits, val;
    do {
      bits = next(31);
      val = bits % n;
    } while (bits - val + (n - 1) < 0);
    return val;
  }

  /**
   * This method is for completness' sake.
   * Returns a long drawn uniformly from 0 to n-1.  Suffice it to say,
   * n must be > 0, or an IllegalArgumentException is raised.
   * 
   * @param n DOCUMENT_ME
   * 
   * @return the long
   */

  public long nextLong(final long n) {
    if (n <= 0)
      throw new IllegalArgumentException("n must be > 0");

    long bits, val;
    do {
      bits = (nextLong() >>> 1);
      val = bits % n;
    } while (bits - val + (n - 1) < 0);
    return val;
  }

  /**
   * A bug fix for versions of JDK 1.1 and below.  JDK 1.2 fixes
   * this for us, but what the heck.
   * 
   * @return the double
   */
  @Override
  public double nextDouble() {
    return (((long) next(26) << 27) + next(27)) / (double) (1L << 53);
  }

  /**
   * A bug fix for versions of JDK 1.1 and below.  JDK 1.2 fixes
   * this for us, but what the heck.
   * 
   * @return the float
   */

  @Override
  public float nextFloat() {
    return next(24) / ((float) (1 << 24));
  }

  /**
   * A bug fix for all versions of the JDK.  The JDK appears to
   * use all four bytes in an integer as independent byte values!
   * Totally wrong. I've submitted a bug report.
   * 
   * @param bytes DOCUMENT_ME
   */

  @Override
  public void nextBytes(final byte[] bytes) {
    for (int x = 0; x < bytes.length; x++) {
      bytes[x] = (byte) next(8);
    }
  }

  /**
   * For completeness' sake, though it's not in java.util.Random.
   * 
   * @return the char
   */

  public char nextChar() {
    // chars are 16-bit UniCode values
    return (char) (next(16));
  }

  /**
   * For completeness' sake, though it's not in java.util.Random.
   * 
   * @return the byte
   */

  public byte nextByte() {
    return (byte) (next(8));
  }

}
