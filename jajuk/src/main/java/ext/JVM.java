/*
 * $Id: JVM.java,v 1.2 2005/10/10 18:03:00 rbair Exp $
 *
 * This file has been adapted to Jajuk by the Jajuk Team.
 * Jajuk Copyright (C) 2007 The Jajuk Team
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
 */
package ext;

/**
 * Deals with the different version of the Java Virtual Machine. <br>
 */
public class JVM {

  public static final int JDK1_0 = 10;

  public static final int JDK1_1 = 11;

  public static final int JDK1_2 = 12;

  public static final int JDK1_3 = 13;

  public static final int JDK1_4 = 14;

  public static final int JDK1_5 = 15;

  public static final int JDK1_6 = 16;

  public static final int JDK1_7 = 17;

  public static final int JDK1_8 = 18;

  public static final int JDK1_9 = 19;

  private static JVM current;
  static {
    current = new JVM();
  }

  /**
   * @return the current JVM object
   */
  public static JVM current() {
    return current;
  }

  private int jdkVersion;

  /**
   * Creates a new JVM data from the <code>java.version</code> System property
   * 
   */
  public JVM() {
    this(System.getProperty("java.version"));
  }

  /**
   * Constructor for the OS object
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

  public boolean isOrLater(int pVersion) {
    return jdkVersion >= pVersion;
  }

  public boolean isOneDotOne() {
    return jdkVersion == JDK1_1;
  }

  public boolean isOneDotTwo() {
    return jdkVersion == JDK1_2;
  }

  public boolean isOneDotThree() {
    return jdkVersion == JDK1_3;
  }

  public boolean isOneDotFour() {
    return jdkVersion == JDK1_4;
  }

  public boolean isOneDotFive() {
    return jdkVersion == JDK1_5;
  }

  public boolean isOneDotSix() {
    return jdkVersion == JDK1_6;
  }

  public boolean isOneDotSeven() {
    return jdkVersion == JDK1_7;
  }

  public boolean isOneDotEight() {
    return jdkVersion == JDK1_8;
  }

  public boolean isOneDotNine() {
    return jdkVersion == JDK1_9;
  }

}
