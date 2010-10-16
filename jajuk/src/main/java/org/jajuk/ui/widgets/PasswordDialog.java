/*
 *  Jajuk
 *  Copyright (C) 2003-2009 The Jajuk Team
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

package org.jajuk.ui.widgets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Messages;

/**
 * A password dialog.
 */
public class PasswordDialog extends JajukJDialog implements ActionListener {
  
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  JPasswordField pf;

  /** DOCUMENT_ME. */
  JOptionPane optionPane;

  /**
   * Instantiates a new password dialog.
   * 
   * @param sMessage DOCUMENT_ME
   */
  public PasswordDialog(String sMessage) {
    setTitle(sMessage);
    pf = new JPasswordField(20);
    // Create the JOptionPane.
    optionPane = new JOptionPane(new Object[] { Messages.getString("DownloadManager.0"), pf },
        JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
    optionPane.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (prop.equals(JOptionPane.VALUE_PROPERTY)) {
          String sPwd = new String(pf.getPassword());
          if (sPwd.trim().equals("")) {
            // set a string to password to avoid asking again
            sPwd = "NOP";
          }
          optionPane.setValue(sPwd);
          dispose();
        }
      }
    });

    // Make this dialog display it.
    setContentPane(optionPane);

    // Handle window closing correctly.
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

    // Register an event handler that puts the text into the option pane.
    pf.addActionListener(this);

    // Ensure the text field always gets the first focus.
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentShown(ComponentEvent ce) {
        pf.requestFocusInWindow();
      }
    });
    setLocation(
        JajukMainWindow.getInstance().getX() + JajukMainWindow.getInstance().getWidth() / 3,
        JajukMainWindow.getInstance().getY() + JajukMainWindow.getInstance().getHeight() / 3);
    pack();
    setVisible(true);
  }

  /**
   * This method handles events for the text field.
   * 
   * @param e DOCUMENT_ME
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    optionPane.setValue(new String(pf.getPassword()));
    dispose();
  }

  /**
   * Gets the option pane.
   * 
   * @return the option pane
   */
  public JOptionPane getOptionPane() {
    return optionPane;
  }

}
