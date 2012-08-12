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
package org.qdwizard;

/**
 * A screen state:
 * <ul>
 * <li>can cancel ?</li>
 * <li>can finish ?</li>
 * <li>can go next ?</li>
 * <li>can go previous ?</li>
 * </ul>.
 */
public class ScreenState {
  private boolean bCanFinish;
  /** Can Go Next. */
  private boolean bCanGoNext;
  /** Can Go Previous. */
  private boolean bCanGoPrevious;
  /** Can Cancel. */
  private boolean bCanCancel;
  /** Problem. */
  private String sProblem;

  /**
   * Construct a ScreenState.
   */
  public ScreenState() {
    this(false, false, false, false, null);
  }

  /**
   * Construct a ScreenState.
   * 
   * @param bCanGoNext next button is enabled
   * @param bCanGoPrevious previous button is enabled
   * @param bCanCancel cancel button is enabled
   * @param bCanFinish cancel button is enabled
   * @param sProblem problem text
   */
  public ScreenState(boolean bCanGoNext, boolean bCanGoPrevious, boolean bCanCancel,
      boolean bCanFinish, String sProblem) {
    this.bCanGoNext = bCanGoNext;
    this.bCanGoPrevious = bCanGoPrevious;
    this.bCanCancel = bCanCancel;
    this.bCanFinish = bCanFinish;
    this.sProblem = sProblem;
  }

  /**
   * Gets the can finish.
   * 
   * @return Finish button enabled
   */
  public boolean getCanFinish() {
    return bCanFinish;
  }

  /**
   * Set whether the finish button should be enabled.
   * 
   * @param bCanFinish 
   */
  public void setCanFinish(boolean bCanFinish) {
    this.bCanFinish = bCanFinish;
  }

  /**
   * Gets the can go next.
   * 
   * @return Next button enabled
   */
  public boolean getCanGoNext() {
    return bCanGoNext;
  }

  /**
   * Set whether the next button should be enabled.
   * 
   * @param bCanGoNext 
   */
  public void setCanGoNext(boolean bCanGoNext) {
    this.bCanGoNext = bCanGoNext;
  }

  /**
   * Gets the can go previous.
   * 
   * @return Previous button enabled
   */
  public boolean getCanGoPrevious() {
    return bCanGoPrevious;
  }

  /**
   * Set whether the previous button should be enabled.
   * 
   * @param bCanGoPrevious 
   */
  public void setCanGoPrevious(boolean bCanGoPrevious) {
    this.bCanGoPrevious = bCanGoPrevious;
  }

  /**
   * Gets the can cancel.
   * 
   * @return Cancel button enabled
   */
  public boolean getCanCancel() {
    return bCanCancel;
  }

  /**
   * Set whether the cancel (or System menu close) button should be enabled.
   * 
   * @param bCanCancel 
   */
  public void setCanCancel(boolean bCanCancel) {
    this.bCanCancel = bCanCancel;
  }

  /**
   * Gets the problem.
   * 
   * @return Problem button enabled
   */
  public String getProblem() {
    return sProblem;
  }

  /**
   * Set a problem (set to null if problem is fixed).
   * 
   * @param sProblem Problem string or null if no more problem
   */
  public void setProblem(String sProblem) {
    this.sProblem = sProblem;
    setCanGoNext(sProblem == null);
  }
}
