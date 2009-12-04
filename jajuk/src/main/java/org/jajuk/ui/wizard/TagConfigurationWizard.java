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
 *  $Revision: 3132 $
 */
package org.jajuk.ui.wizard;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.tags.JAudioTaggerTagImpl;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;

/**
 * 
 */
public class TagConfigurationWizard extends JajukJDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JList availableTagsJList;
  private JList activatedTagsJList;

  private ArrayList<String> availableList = new ArrayList<String>();
  private ArrayList<String> activatedList = new ArrayList<String>();

  public TagConfigurationWizard() {
    setTitle(Messages.getString("JajukWindow.40"));
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    initUI();
    refreshList();
    populate();
    pack();
    UtilGUI.centerWindow(TagConfigurationWizard.this);
    setVisible(true);
  }

  private void refreshList() {
    availableList.clear();
    TrackManager tm = TrackManager.getInstance();
    for (String s : JAudioTaggerTagImpl.getSupportedTagFields()) {
      if (!tm.getActivatedExtraTags().contains(s)) {
        availableList.add(s);
      }
    }

    activatedList.clear();
    for (String s : tm.getActivatedExtraTags()) {
      activatedList.add(s);
    }
  }

  private void populate() {
    DefaultListModel model = (DefaultListModel) availableTagsJList.getModel();
    model.clear();
    for (String s : availableList) {
      model.addElement(s);
    }

    availableTagsJList.setModel(model);

    model = (DefaultListModel) activatedTagsJList.getModel();
    model.clear();
    for (String s : activatedList) {
      model.addElement(s);
    }
    activatedTagsJList.setModel(model);
  }

  private void initUI() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new MigLayout("", "", ""));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JTextArea ta = new JTextArea();
    ta.setText(Messages.getString("ActiveTagsWizard.3"));
    ta.setLineWrap(true);
    ta.setEditable(false);
    ta.setWrapStyleWord(true);
    ta.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    ta.setMargin(new Insets(5, 5, 5, 5));
    mainPanel.add(ta, "cell 0 0 3 0, grow");

    // available tags
    JLabel label = new JLabel(Messages.getString("ActiveTagsWizard.1"));
    label.setPreferredSize(new Dimension(250, 20));
    mainPanel.add(label, "cell 0 1");
    availableTagsJList = new JList();
    availableTagsJList.setModel(new DefaultListModel());
    mainPanel.add(new JScrollPane(availableTagsJList), "cell 0 2, grow");

    // control buttons
    Box controlBox = new Box(BoxLayout.Y_AXIS);
    JButton addButton = new JButton(IconLoader.getIcon(JajukIcons.PLAYER_NEXT));
    addButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        for (Object o : availableTagsJList.getSelectedValues()) {
          activatedList.add(availableList.remove(availableList.indexOf(o)));
        }
        populate();
      }
    });
    controlBox.add(addButton);

    JButton removeButton = new JButton(IconLoader.getIcon(JajukIcons.PLAYER_PREVIOUS));
    removeButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        for (Object o : activatedTagsJList.getSelectedValues()) {
          availableList.add(activatedList.remove(activatedList.indexOf(o)));
        }
        populate();
      }
    });
    controlBox.add(removeButton);

    mainPanel.add(controlBox, "cell 1 2, center");

    // activated tags
    label = new JLabel(Messages.getString("ActiveTagsWizard.2"));
    label.setPreferredSize(new Dimension(250, 20));
    mainPanel.add(label, "cell 2 1");
    activatedTagsJList = new JList();
    activatedTagsJList.setModel(new DefaultListModel());
    mainPanel.add(new JScrollPane(activatedTagsJList), "cell 2 2, grow");

    // confirm buttons
    JButton okButton = new JButton(Messages.getString("Ok"));
    okButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        okAction();
      }
    });
    mainPanel.add(okButton, "cell 0 3, align right");

    JButton cancelButton = new JButton(Messages.getString("Cancel"));
    cancelButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
      }
    });
    mainPanel.add(cancelButton, "cell 2 3");

    getContentPane().add(mainPanel);

  }

  /**
   * 
   */
  private void okAction() {
    TrackManager tm = TrackManager.getInstance();
    // cleanup removed tags
    for (PropertyMetaInformation m : tm.getCustomProperties()) {
      if (JAudioTaggerTagImpl.getSupportedTagFields().contains(m.getName())
          && !activatedList.contains(m.getName())) {
        tm.removeProperty(m.getName());
        Properties properties = new Properties();
        properties.put(Const.DETAIL_CONTENT, m.getName());
        JajukEvent event = new JajukEvent(JajukEvents.CUSTOM_PROPERTIES_REMOVE, properties);
        ObservationManager.notify(event);
      }
    }

    boolean deepScanNeeded = false;
    for (String s : activatedList) {
      // check if it is already active
      boolean skip = false;
      for (PropertyMetaInformation m : tm.getCustomProperties()) {
        if (m.getName().equals(s)) {
          skip = true;
          break;
        }
      }
      if (skip)
        continue;

      deepScanNeeded = true;
      // activate new tag
      tm.registerProperty(new PropertyMetaInformation(s, true, false, true, false, false,
          String.class, null));
      Properties properties = new Properties();
      properties.put(Const.DETAIL_CONTENT, s);
      JajukEvent event = new JajukEvent(JajukEvents.CUSTOM_PROPERTIES_ADD, properties);
      ObservationManager.notify(event);
    }

    if (deepScanNeeded) {
      for (Device d : DeviceManager.getInstance().getDevices()) {
        if (d.isReady())
          d.refreshCommand(true);
      }
    }

    setVisible(false);
    dispose();
  }
}
