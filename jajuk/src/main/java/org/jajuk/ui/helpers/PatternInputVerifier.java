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

package org.jajuk.ui.helpers;

import java.util.Locale;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;

import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Messages;

/**
 * Input verifier used for predefined patterns. Pattern should contain at least
 * one / as this pattern verifier is used for organizer and organizer need to
 * create at least one directory to avoid mess on disk
 */
public class PatternInputVerifier extends InputVerifier {

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.InputVerifier#verify(javax.swing.JComponent)
   */
  @Override
  public boolean verify(JComponent input) {
    JFormattedTextField tf = (JFormattedTextField) input;
    String sText = tf.getText().toLowerCase(Locale.getDefault());
    // Check pattern contains at least one /
    if (sText.indexOf('/') == -1) {
      JOptionPane.showMessageDialog(JajukMainWindow.getInstance(), Messages.getString("Error.146"),
          Messages.getString("Error"), JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.InputVerifier#shouldYieldFocus(javax.swing.JComponent)
   */
  @Override
  public boolean shouldYieldFocus(JComponent input) {
    return verify(input);
  }

}