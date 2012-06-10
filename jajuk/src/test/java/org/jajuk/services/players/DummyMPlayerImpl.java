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
 *  
 */
package org.jajuk.services.players;

/**
 * .
 */
public class DummyMPlayerImpl {

  /** The Constant POSITION.   */
  public final static float POSITION = 17.7f;

  /** The Constant LENGTH.   */
  public final static float LENGTH = 235.28f;

  /**
   * The main method.
   *
   * @param args the arguments
   * @throws InterruptedException the interrupted exception
   */
  public static void main(final String[] args) throws InterruptedException {
    System.out.println("TestMPlayerPlayerImpl was called!");

    // simulate normal reply by MPlayer to commands that are sent in
    System.out.println("ANS_LENGTH=" + new Float(LENGTH).toString());
    System.out.println("ANS_TIME_POSITION=" + new Float(POSITION).toString());

    Thread.sleep(5000);

    System.out.println("Exiting... (Quit)");
  }
}
