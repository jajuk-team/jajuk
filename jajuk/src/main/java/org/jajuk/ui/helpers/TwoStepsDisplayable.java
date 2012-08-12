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
package org.jajuk.ui.helpers;

/**
 * A TwoStepsDisplayable wraps a SwingWorker without forcing user to extends
 * SwingWorker.
 * <p>The two methods own jajuk-specific names to be independent from
 * the swing worker implementation too.</p>
 * <p>This interface also breaks the SwingWorker generics useless in out case
 * </p>
 */
public interface TwoStepsDisplayable {
  /**
   * Long call done in background in a dedicated thread.
   * 
   * @return a resulting object (can be null)
   */
  Object longCall();

  /**
   * Fast call done in the EDT, must be fast !.
   * 
   * @param in 
   */
  void shortCall(Object in);
}
