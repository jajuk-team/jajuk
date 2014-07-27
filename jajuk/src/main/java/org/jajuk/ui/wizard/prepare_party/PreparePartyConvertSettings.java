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
package org.jajuk.ui.wizard.prepare_party;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;

import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.util.Messages;

/**
 * Inner dialog of prepare party wizard used to configure PACPL (format converter)s
 */
public class PreparePartyConvertSettings extends JajukJDialog implements ActionListener {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** Listener which is informed about changes to the settings. */
  ChangeListener listener;
  /** Text field for the input of the command. */
  JTextField jtfPACPLCommand;
  /** OK/Cancel buttons. */
  OKCancelPanel okp;

  /**
   * Instantiates a new prepare party convert settings.
   * 
   * @param listener Listener for changes
   * @param command The current command
   * @param parent Parent frame
   */
  public PreparePartyConvertSettings(ChangeListener listener, String command, Frame parent) {
    super(parent, true);
    setLocationRelativeTo(parent);
    this.listener = listener;
    setTitle(Messages.getString("PreparePartyWizard.38"));
    setAlwaysOnTop(true);
    okp = new OKCancelPanel(this);
    jtfPACPLCommand = new JTextField(command);
    // select all
    jtfPACPLCommand.setSelectionStart(0);
    jtfPACPLCommand.setSelectionEnd(command.length());
    // Add items
    setLayout(new MigLayout("insets 10,gapx 10,gapy 15", "[]"));
    add(new JLabel(Messages.getString("PreparePartyWizard.39")), "wrap");
    add(jtfPACPLCommand, "grow,wrap");
    add(okp, "wrap");
    getRootPane().setDefaultButton(okp.getOKButton());
    pack();
    setVisible(true);
  }

 @Override
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == okp.getCancelButton()) {
      dispose(); // close window
    }
    if (e.getSource() == okp.getOKButton()) {
      // inform the caller about the new value
      listener.stateChanged(new ChangeEvent(jtfPACPLCommand.getText()));
      // exit
      dispose();
    }
  }
}
