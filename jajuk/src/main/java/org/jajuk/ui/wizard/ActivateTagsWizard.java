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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.SwingUtilities;

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
public class ActivateTagsWizard extends JajukJDialog {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private JList availableTags;
  private JList activatedTags;

  private ArrayList<String> availableList = new ArrayList<String>();
  private ArrayList<String> activatedList = new ArrayList<String>();

  public ActivateTagsWizard() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setTitle(Messages.getString("JajukWindow.40"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        initUI();
        refreshList();
        populate();
        setSize(new Dimension(600, 500));
        UtilGUI.centerWindow(ActivateTagsWizard.this);
        setVisible(true);
      }
    });
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
    DefaultListModel model = (DefaultListModel) availableTags.getModel();
    model.clear();
    for (String s : availableList) {
      model.addElement(s);
    }

    availableTags.setModel(model);

    model = (DefaultListModel) activatedTags.getModel();
    model.clear();
    for (String s : activatedList) {
      model.addElement(s);
    }
    activatedTags.setModel(model);
  }

  private void initUI() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout(5, 5));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JTextArea ta = new JTextArea();
    ta.setText(Messages.getString("ActiveTagsWizard.3"));
    ta.setLineWrap(true);
    ta.setEditable(false);
    ta.setWrapStyleWord(true);
    ta.setBorder(BorderFactory.createRaisedBevelBorder());
    mainPanel.add(ta, BorderLayout.NORTH);

    // available tags
    JPanel availablePanel = new JPanel();
    availablePanel.setLayout(new BorderLayout());
    JLabel label = new JLabel(Messages.getString("ActiveTagsWizard.1"));
    label.setPreferredSize(new Dimension(250, 20));
    availablePanel.add(label, BorderLayout.NORTH);
    availableTags = new JList();
    availableTags.setModel(new DefaultListModel());
    availablePanel.add(new JScrollPane(availableTags), BorderLayout.CENTER);
    mainPanel.add(availablePanel, BorderLayout.WEST);

    // control buttons
    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
    Box controlBox = new Box(BoxLayout.Y_AXIS);
    JButton addButton = new JButton(IconLoader.getIcon(JajukIcons.PLAYER_NEXT));
    addButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        for (Object o : availableTags.getSelectedValues()) {
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
        for (Object o : activatedTags.getSelectedValues()) {
          availableList.add(activatedList.remove(activatedList.indexOf(o)));
        }
        populate();
      }
    });
    controlBox.add(removeButton);
    controlPanel.add(controlBox);
    mainPanel.add(controlPanel, BorderLayout.CENTER);

    // activated tags
    JPanel activatedPanel = new JPanel();
    activatedPanel.setLayout(new BorderLayout());
    label = new JLabel(Messages.getString("ActiveTagsWizard.2"));
    label.setPreferredSize(new Dimension(250, 20));
    activatedPanel.add(label, BorderLayout.NORTH);
    activatedTags = new JList();
    activatedTags.setModel(new DefaultListModel());
    activatedPanel.add(new JScrollPane(activatedTags), BorderLayout.CENTER);
    mainPanel.add(activatedPanel, BorderLayout.EAST);

    // confirm buttons
    JPanel confirmPanel = new JPanel();
    confirmPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    JButton okButton = new JButton(Messages.getString("Ok"));
    okButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        okAction();
      }
    });
    confirmPanel.add(okButton);

    JButton cancelButton = new JButton(Messages.getString("Cancel"));
    cancelButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
        dispose();
      }
    });
    confirmPanel.add(cancelButton);
    mainPanel.add(confirmPanel, BorderLayout.SOUTH);

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
