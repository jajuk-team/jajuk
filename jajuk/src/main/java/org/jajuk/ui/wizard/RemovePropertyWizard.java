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

package org.jajuk.ui.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JComboBox;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.ItemManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;

/**
 * Remove property wizard.
 */
public class RemovePropertyWizard extends CustomPropertyWizard {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  JComboBox jcbName;

  /**
   * Constructor.
   */
  public RemovePropertyWizard() {
    super(Messages.getString("RemovePropertyWizard.0"));
    jcbName = new JComboBox();
    populate();// create default UI
    jcbName.addItemListener(this);
    populateProperties();// fill properties combo with properties for
    setLayout(new MigLayout("insets 10,gapx 10,gapy 15"));
    add(jlItemChoice, "");
    add(jcbItemChoice, "width 200::,wrap");
    add(jlName, "");
    add(jcbName, "width 200::,wrap,grow");
    add(okp, "span,right");
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
      ItemManager im = getItemManager();
      String sProperty = jcbName.getSelectedItem().toString();
      im.removeProperty(sProperty);
      Properties properties = new Properties();
      properties.put(Const.DETAIL_CONTENT, sProperty);
      JajukEvent event = new JajukEvent(JajukEvents.CUSTOM_PROPERTIES_REMOVE, properties);
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
    if (e.getSource() == jcbItemChoice) {
      populateProperties();
    }
    // update OK button state
    if (jcbItemChoice.getSelectedIndex() != -1 && jcbName.getSelectedIndex() != -1) {
      okp.getOKButton().setEnabled(true);
      okp.getOKButton().requestFocusInWindow();
    } else {
      okp.getOKButton().setEnabled(false);
    }
  }

  /**
   * Populate properties.
   * 
   */
  public final void populateProperties() {
    // clear combo
    jcbName.removeAllItems();
    // refresh properties list for this item
    ItemManager im = getItemManager();
    if (im != null) {
      Iterator<PropertyMetaInformation> it = im.getUserCustomProperties().iterator();
      while (it.hasNext()) {
        PropertyMetaInformation meta = it.next();
        jcbName.addItem(meta.getName());
      }
    }
  }

}