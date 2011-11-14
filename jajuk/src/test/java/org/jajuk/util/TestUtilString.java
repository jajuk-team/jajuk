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
 *  $Revision: 3132 $
 */
package org.jajuk.util;

import java.text.DateFormat;
import java.util.Date;
import java.util.Random;

import org.jajuk.JajukTestCase;
import org.jajuk.TestHelpers;
import org.jajuk.ThreadTestHelper;

public class TestUtilString extends JajukTestCase {
  // settings for the micro-benchmarks done for some methods
  private static final int MATCHES_PER_TEST = 300000;
  private static final int NUMBER_OF_MATCH_TESTS = 5;
  private static final int NUMBER_OF_THREADS = 10;
  private static final int NUMBER_OF_TESTS = 1000;

  private static final Random random = new Random();

  @Override
  public final void setUp() throws Exception {
    random.setSeed(System.currentTimeMillis());
    
    super.setUp();
  }
  
  /**
   * Test method for {@link org.jajuk.util.UtilString#applyPattern(org.jajuk.base.File, java.lang.String, boolean, boolean)}.
   * @throws Exception
   */
  public void testApplyPattern() throws Exception {
    UtilString.applyPattern(TestHelpers.getMockFile(), "somepattern", false, false);
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#containsNonDigitOrLetters(java.lang.String)}.
   */
  public void testContainsNonDigitOrLetters() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#encodeURL(java.lang.String)}.
   */
  public void testEncodeURL() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#escapeString(java.lang.String)}.
   */
  public void testEscapeString() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#format(java.lang.Object, org.jajuk.base.PropertyMetaInformation, boolean)}.
   */
  public void testFormat() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#getLocaleDateFormatter()}.
   */
  public void testGetLocaleDateFormatter() {
    assertNotNull(UtilString.getLocaleDateFormatter());
  }

  public void testMultipleThreads() throws Exception {
    ThreadTestHelper helper = new ThreadTestHelper(NUMBER_OF_THREADS, NUMBER_OF_TESTS);

    helper.executeTest(new ThreadTestHelper.TestRunnable() {
      @Override
      public void doEnd(int threadnum) throws Exception {
        // do stuff at the end ...
      }

      @Override
      public void run(int threadnum, int iter) {
        DateFormat format = UtilString.getLocaleDateFormatter();

        assertNotNull(format.format(new Date()));
      }
    });
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#formatPropertyDesc(java.lang.String)}.
   */
  public void testFormatPropertyDesc() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#formatGenre(java.lang.String)}.
   */
  public void testFormatGenre() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#formatTag(java.lang.String)}.
   */
  public void testFormatTag() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#formatTimeBySec(long)}.
   */
  public void testFormatTimeBySec() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#formatXML(java.lang.String)}.
   */
  public void testFormatXML() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#getAdditionDateFormatter()}.
   */
  public void testGetAdditionDateFormatter() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#getAnonymizedJajukProperties()}.
   */
  public void testGetAnonymizedJajukProperties() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#getAnonymizedSystemProperties()}.
   */
  public void testGetAnonymizedSystemProperties() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#getLimitedString(java.lang.String, int)}.
   */
  public void testGetLimitedString() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#isChar(int)}.
   */
  public void testIsChar() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#isXMLValid(java.lang.String)}.
   */
  public void testIsXMLValid() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#padNumber(long, int)}.
   */
  public void testPadNumber() {
    assertEquals("00099", UtilString.padNumber(99, 5));
    assertEquals("00011", UtilString.padNumber(11, 5));
    assertEquals("00000", UtilString.padNumber(0, 5));
    assertEquals("99999", UtilString.padNumber(99999, 5));
    assertEquals("100000", UtilString.padNumber(100000, 5));
    assertEquals("000-9", UtilString.padNumber(-9, 5));
    assertEquals("00-19", UtilString.padNumber(-19, 5));
    
    assertEquals("1", UtilString.padNumber(1, 1));
    assertEquals("11", UtilString.padNumber(11, 2));
    assertEquals("113", UtilString.padNumber(113, 3));
  }

  public void testPadNumberBenchmark() {
    testPadNumber();
    
    long overall = 0;
    for(int i = 0;i < NUMBER_OF_MATCH_TESTS;i++) {
      long dur = runPadMicroBenchmark();
      System.out.println("Test run took " + dur + "ms");
      overall += dur;
    }
    System.out.println("Average test duration: " + (overall/NUMBER_OF_MATCH_TESTS));
  }

  /**
   * @return
   *
   */
  private long runPadMicroBenchmark() {
    long start = System.currentTimeMillis();

    for(int i = 0; i < MATCHES_PER_TEST*10;i++) {
      UtilString.padNumber(random.nextInt(10000), 5);
      //StringUtils.leftPad(Long.toString(random.nextInt(10000)), 5, '0');
    }

    return System.currentTimeMillis() - start;
  }
  
  /**
   * Test method for {@link org.jajuk.util.UtilString#parse(java.lang.String, java.lang.Class)}.
   */
  public void testParse() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#fastLongParser(java.lang.String)}.
   */
  public void testFastLongParser() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#fastBooleanParser(java.lang.String)}.
   */
  public void testFastBooleanParser() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#rot13(java.lang.String)}.
   */
  public void testRot13() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#matchesIgnoreCaseAndOrder(java.lang.String, java.lang.String)}.
   */
  public void testMatchesIgnoreCaseAndOrder() {
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("", ""));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("", "123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("t", "test"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("t", "TesT"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("tes", "TesT"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("Tes", "test"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("te", "123te123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("te1", "123te123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3te", "123te123"));

    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3te 12", "123te123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3te 3te1", "123te123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3Te 12", "123te123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3Te 3tE1", "123te123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3te 12", "123TE123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3te 3te1", "123TE123"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3te 12", "1 2 3te 12 3"));
    assertTrue(UtilString.matchesIgnoreCaseAndOrder("3te 3te1", "12 3te1 23"));

    assertFalse(UtilString.matchesIgnoreCaseAndOrder("1", ""));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("t", ""));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("test", ""));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("test", "t"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("TesT", "t"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("TesT", "tes"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("test", "Tes"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("123te123", "te"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("123te123", "te1"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("123te123", "3te"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("TesT test", "tt"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("123te123", "1te"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("123te123", "te3"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("3te 12", "123ate123"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("3te 3te1", "123tae123"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("3Te 12", "1323te1323"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("3Te 3tE1", "123.te123"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("3te 12", "123T_E123"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("3te 3te1", "123TEa123"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("3te 12", "1 2 3te 1 2 3"));
    assertFalse(UtilString.matchesIgnoreCaseAndOrder("3te 3te1", "12 3toe1 23"));

  }

  public void testMatchesIgnoreCaseAndOrderBenchmark() {
    long overall = 0;
    for (int i = 0; i < NUMBER_OF_MATCH_TESTS; i++) {
      long dur = runMicroBenchmark();
      System.out.println("Test run took " + dur + "ms");
      overall += dur;
    }
    System.out.println("Average test duration: " + (overall / NUMBER_OF_MATCH_TESTS));
  }

  /**
   * @return
   *
   */
  private long runMicroBenchmark() {
    long start = System.currentTimeMillis();

    RandomString str = new RandomString(30);
    RandomString search = new RandomString(6);
    for (int i = 0; i < MATCHES_PER_TEST; i++) {
      UtilString.matchesIgnoreCaseAndOrder(str.nextString(), search.nextString());
    }

    return System.currentTimeMillis() - start;
  }

  public static class RandomString {

    private static final char[] symbols = new char[63];

    static {
      for (int idx = 0; idx < 10; ++idx)
        symbols[idx] = (char) ('0' + idx);
      for (int idx = 10; idx < 36; ++idx)
        symbols[idx] = (char) ('a' + idx - 10);
      for (int idx = 36; idx < 62; ++idx)
        symbols[idx] = (char) ('A' + idx - 36);
      symbols[62] = ' ';
    }

    private final char[] buf;

    public RandomString(int length) {
      if (length < 1)
        throw new IllegalArgumentException("length < 1: " + length);
      buf = new char[length];
    }

    public String nextString() {
      for (int idx = 0; idx < buf.length; ++idx)
        buf[idx] = symbols[random.nextInt(symbols.length)];
      return new String(buf);
    }

  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#encodeToUnicode(java.lang.String)}.
   */
  public void testEncodeToUnicode() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#byteToHex(byte)}.
   */
  public void testByteToHex() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#concat(java.lang.Object[])}.
   */
  public void testConcat() {
    // TODO: implement test
  }

  /**
   * Test method for {@link org.jajuk.util.UtilString#getTextBetweenChars(java.lang.String, char, char)}.
   */
  public void testGetTextBetweenChars() {
    // TODO: implement test
  }

}
