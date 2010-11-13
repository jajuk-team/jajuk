/*
 *  Jajuk
 *  Copyright (C) 2003-2010 The Jajuk Team
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

package org.jajuk.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.ItemManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jdesktop.swingx.JXDatePicker;

/**
 * New property wizard.
 */
public class NewPropertyWizard extends CustomPropertyWizard implements KeyListener {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** DOCUMENT_ME. */
  private final JTextField jtfName;

  /** DOCUMENT_ME. */
  private final JComboBox jcbClass;

  /** DOCUMENT_ME. */
  private final JTextField jtfDefault;

  /** DOCUMENT_ME. */
  private final JCheckBox jcbDefault;

  /** DOCUMENT_ME. */
  private final JXDatePicker jdpDefault;

  // Types
  /** The Constant STRING.  DOCUMENT_ME */
  private static final int STRING = 0;

  /** The Constant LONG.  DOCUMENT_ME */
  private static final int LONG = 1;

  /** The Constant DOUBLE.  DOCUMENT_ME */
  private static final int DOUBLE = 2;

  /** The Constant BOOLEAN.  DOCUMENT_ME */
  private static final int BOOLEAN = 3;

  /** The Constant DATE.  DOCUMENT_ME */
  private static final int DATE = 4;

  /**
   * Constructor.
   */
  public NewPropertyWizard() {
    super(Messages.getString("NewPropertyWizard.0"));
    populate();// create default UI
    // Name
    jtfName = new JTextField();
    jtfName.addKeyListener(this);
    // Type, class
    JLabel jlClass = new JLabel(Messages.getString("NewPropertyWizard.3"));
    jcbClass = new JComboBox();
    jcbClass.addItem(Messages.getString(Const.FORMAT_STRING));
    jcbClass.addItem(Messages.getString(Const.FORMAT_NUMBER));
    jcbClass.addItem(Messages.getString(Const.FORMAT_FLOAT));
    jcbClass.addItem(Messages.getString(Const.FORMAT_BOOLEAN));
    jcbClass.addItem(Messages.getString(Const.FORMAT_DATE));
    jcbClass.addItemListener(this);
    // Default
    JLabel jlDefault = new JLabel(Messages.getString("NewPropertyWizard.5"));
    jtfDefault = new JTextField(20);
    jcbDefault = new JCheckBox();
    jcbDefault.setEnabled(false);
    jdpDefault = new JXDatePicker();
    jdpDefault.setEnabled(false);

    // Add items
    setLayout(new MigLayout("insets 15,gapx 10,gapy 15", "[][grow]"));
    add(jlItemChoice);
    add(jcbItemChoice, "grow,wrap");
    add(jlName);
    add(jtfName, "grow,wrap");
    add(jlClass);
    add(jcbClass, "grow,wrap");
    add(jlDefault);
    add(jtfDefault, "split 3,grow");
    add(jcbDefault, "grow");
    add(jdpDefault, "grow,wrap");
    add(okp, "cell 1 4, span,right");
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowActivated(WindowEvent e) {
        jtfName.requestFocusInWindow();
      }
    });
    getRootPane().setDefaultButton(okp.getOKButton());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource().equals(this.okp.getOKButton())) {
      // check the property is not already used internally
      for (String element : Const.XML_RESERVED_ATTRIBUTE_NAMES) {
        /*
         * check user can't create a property that is the localized name of an
         * existing standard attribute. Note that a potential bug can occur if
         * user change language
         */
        if (element.equalsIgnoreCase(jtfName.getText()) || jtfName.getText().matches(",")) {
          Messages.showErrorMessage(110);
          return;
        }
      }
      ItemManager im = getItemManager();
      // check if this property is not already used
      for (PropertyMetaInformation meta : im.getCustomProperties()) {
        if (meta.getName().equals(jtfName.getText())) {
          Messages.showErrorMessage(162);
          return;
        }
      }
      // check format
      if (!UtilString.isXMLValid(jtfName.getText())) {
        Messages.showErrorMessage(140);
        return;
      }
      // OK, store it
      // get selected format
      Class<?> cType = null;
      switch (jcbClass.getSelectedIndex()) {
      case STRING:
        cType = String.class;
        break;
      case LONG:
        cType = Long.class;
        break;
      case DOUBLE:
        cType = Double.class;
        break;
      case BOOLEAN:
        cType = Boolean.class;
        break;
      case DATE:
        cType = Date.class;
        break;
      default:
        return;
      }
      String sProperty = jtfName.getText();
      Object oDefault = jtfDefault.getText();
      // Check number and float formats (others are safe)
      try {
        if (cType.equals(Long.class)) {
          Long.parseLong(jtfDefault.getText());
        } else if (cType.equals(Double.class)) {
          Double.parseDouble(jtfDefault.getText());
        }
      } catch (Exception e) {
        Messages.showErrorMessage(137);
        return;
      }
      // set default
      if (cType.equals(Boolean.class)) {
        oDefault = jcbDefault.isSelected();
      } else if (cType.equals(Date.class)) {
        oDefault = jdpDefault.getDate();
      }
      PropertyMetaInformation meta = new PropertyMetaInformation(sProperty, true, false, true,
          true, true, cType, oDefault);
      im.registerProperty(meta);
      im.applyNewProperty(meta);
      Properties properties = new Properties();
      properties.put(Const.DETAIL_CONTENT, sProperty);
      JajukEvent event = new JajukEvent(JajukEvents.CUSTOM_PROPERTIES_ADD, properties);
      ObservationManager.notify(event);
      dispose();
    } else if (ae.getSource().equals(this.okp.getCancelButton())) {
      dispose();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    // Date format
    if (jcbClass.getSelectedIndex() == DATE) {
      jdpDefault.setEnabled(true);
    } else {
      jdpDefault.setEnabled(false);
    }
    // Boolean format
    if (jcbClass.getSelectedIndex() == BOOLEAN) {
      jcbDefault.setEnabled(true);
    } else {
      jcbDefault.setEnabled(false);
    }
    // Others formats
    if (jcbClass.getSelectedIndex() != BOOLEAN && jcbClass.getSelectedIndex() != DATE) {
      jtfDefault.setEnabled(true);
    } else {
      jtfDefault.setEnabled(false);
    }
    // Ok button
    if (jcbItemChoice.getSelectedIndex() != -1 && jcbClass.getSelectedIndex() != -1
        && jtfName.getText().length() > 0) {
      okp.getOKButton().setEnabled(true);
    } else {
      okp.getOKButton().setEnabled(false);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
   */
  @Override
  public void keyTyped(KeyEvent e) {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
   */
  @Override
  public void keyPressed(KeyEvent e) {
    // required by interface, but nothing to do here...
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
   */
  @Override
  public void keyReleased(KeyEvent e) {
    if (jcbItemChoice.getSelectedIndex() != -1 && jcbClass.getSelectedIndex() != -1
        && jtfName.getText().length() > 0) {
      okp.getOKButton().setEnabled(true);
    } else {
      okp.getOKButton().setEnabled(false);
    }
  }
}