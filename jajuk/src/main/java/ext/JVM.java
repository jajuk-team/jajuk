/*
 * This file has been adapted to Jajuk by the Jajuk Team.
 *
 * The original copyrights and license follow:
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * 
 */
package ext;

/**
 * Deals with the different version of the Java Virtual Machine. <br>
 */
public class JVM {

  /** The Constant JDK1_0.  DOCUMENT_ME */
  public static final int JDK1_0 = 10;

  /** The Constant JDK1_1.  DOCUMENT_ME */
  public static final int JDK1_1 = 11;

  /** The Constant JDK1_2.  DOCUMENT_ME */
  public static final int JDK1_2 = 12;

  /** The Constant JDK1_3.  DOCUMENT_ME */
  public static final int JDK1_3 = 13;

  /** The Constant JDK1_4.  DOCUMENT_ME */
  public static final int JDK1_4 = 14;

  /** The Constant JDK1_5.  DOCUMENT_ME */
  public static final int JDK1_5 = 15;

  /** The Constant JDK1_6.  DOCUMENT_ME */
  public static final int JDK1_6 = 16;

  /** The Constant JDK1_7.  DOCUMENT_ME */
  public static final int JDK1_7 = 17;

  /** The Constant JDK1_8.  DOCUMENT_ME */
  public static final int JDK1_8 = 18;

  /** The Constant JDK1_9.  DOCUMENT_ME */
  public static final int JDK1_9 = 19;

  /** DOCUMENT_ME. */
  private static JVM current;
  static {
    current = new JVM();
  }

  /**
   * Current.
   * 
   * @return the current JVM object
   */
  public static JVM current() {
    return current;
  }

  /** DOCUMENT_ME. */
  private int jdkVersion;

  /**
   * Creates a new JVM data from the <code>java.version</code> System property
   */
  public JVM() {
    this(System.getProperty("java.version"));
  }

  /**
   * Constructor for the OS object.
   * 
   * @param pJavaVersion DOCUMENT_ME
   */
  public JVM(String pJavaVersion) {
    if (pJavaVersion.startsWith("1.9.")) {
      jdkVersion = JDK1_9;
    } else if (pJavaVersion.startsWith("1.8.")) {
      jdkVersion = JDK1_8;
    } else if (pJavaVersion.startsWith("1.7.")) {
      jdkVersion = JDK1_7;
    } else if (pJavaVersion.startsWith("1.6.")) {
      jdkVersion = JDK1_6;
    } else if (pJavaVersion.startsWith("1.5.")) {
      jdkVersion = JDK1_5;
    } else if (pJavaVersion.startsWith("1.4.")) {
      jdkVersion = JDK1_4;
    } else if (pJavaVersion.startsWith("1.3.")) {
      jdkVersion = JDK1_3;
    } else if (pJavaVersion.startsWith("1.2.")) {
      jdkVersion = JDK1_2;
    } else if (pJavaVersion.startsWith("1.1.")) {
      jdkVersion = JDK1_1;
    } else if (pJavaVersion.startsWith("1.0.")) {
      jdkVersion = JDK1_0;
    } else {
      // unknown version, assume 1.5
      jdkVersion = JDK1_5;
    }
  }

  /**
   * Checks if is or later.
   * 
   * @param pVersion DOCUMENT_ME
   * 
   * @return true, if is or later
   */
  public boolean isOrLater(int pVersion) {
    return jdkVersion >= pVersion;
  }

  /**
   * Checks if is one dot one.
   * 
   * @return true, if is one dot one
   */
  public boolean isOneDotOne() {
    return jdkVersion == JDK1_1;
  }

  /**
   * Checks if is one dot two.
   * 
   * @return true, if is one dot two
   */
  public boolean isOneDotTwo() {
    return jdkVersion == JDK1_2;
  }

  /**
   * Checks if is one dot three.
   * 
   * @return true, if is one dot three
   */
  public boolean isOneDotThree() {
    return jdkVersion == JDK1_3;
  }

  /**
   * Checks if is one dot four.
   * 
   * @return true, if is one dot four
   */
  public boolean isOneDotFour() {
    return jdkVersion == JDK1_4;
  }

  /**
   * Checks if is one dot five.
   * 
   * @return true, if is one dot five
   */
  public boolean isOneDotFive() {
    return jdkVersion == JDK1_5;
  }

  /**
   * Checks if is one dot six.
   * 
   * @return true, if is one dot six
   */
  public boolean isOneDotSix() {
    return jdkVersion == JDK1_6;
  }

  /**
   * Checks if is one dot seven.
   * 
   * @return true, if is one dot seven
   */
  public boolean isOneDotSeven() {
    return jdkVersion == JDK1_7;
  }

  /**
   * Checks if is one dot eight.
   * 
   * @return true, if is one dot eight
   */
  public boolean isOneDotEight() {
    return jdkVersion == JDK1_8;
  }

  /**
   * Checks if is one dot nine.
   * 
   * @return true, if is one dot nine
   */
  public boolean isOneDotNine() {
    return jdkVersion == JDK1_9;
  }

}
