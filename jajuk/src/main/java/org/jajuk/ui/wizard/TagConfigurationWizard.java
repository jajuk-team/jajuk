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
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.BorderFactory;
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
import org.jajuk.ui.helpers.ManualDeviceRefreshReporter;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Wizard allowing user to select the extra tags to be displayed by jajuk
 */
public class TagConfigurationWizard extends JajukJDialog {

  private static final long serialVersionUID = 1L;
  private JList availableTagsJList;
  private JList activatedTagsJList;
  
  /** OK/Cancel buttons */
  private OKCancelPanel okp;

  private ArrayList<String> availableList = new ArrayList<String>();
  private ArrayList<String> activatedList = new ArrayList<String>();

  public TagConfigurationWizard() {
    super(JajukMainWindow.getInstance(), true);
    setTitle(Messages.getString("JajukWindow.40"));
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    initUI();
    refreshList();
    populate();
    setLocationByPlatform(true);
    setPreferredSize(new Dimension(600, 600));
    pack();
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
    availableTagsJList.clearSelection();
    DefaultListModel model = (DefaultListModel) availableTagsJList.getModel();
    model.clear();
    for (String s : availableList) {
      model.addElement(s);
    }
    availableTagsJList.setModel(model);

    activatedTagsJList.clearSelection();
    model = (DefaultListModel) activatedTagsJList.getModel();
    model.clear();
    for (String s : activatedList) {
      model.addElement(s);
    }
    activatedTagsJList.setModel(model);
  }

  private void initUI() {
    JTextArea jta = new JTextArea(){
      private static final long serialVersionUID = 1L;

      /**
       * Display an info label in the text area
       * 
       * @param g
       *          DOCUMENT_ME
       */
      @Override
      public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(IconLoader.getIcon(JajukIcons.INFO).getImage(), 4, 3, 16, 16, null);
      }
    };
    jta.setTabSize(2);
    jta.setText('\t'+Messages.getString("ActiveTagsWizard.3"));
    jta.setLineWrap(true);
    jta.setEditable(false);
    jta.setWrapStyleWord(true);
    jta.setMargin(new Insets(4, 4, 4, 4));
    jta.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    
    // available tags
    availableTagsJList = new JList();
    availableTagsJList.setModel(new DefaultListModel());
    
    // control buttons
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
  
    // activated tags
    activatedTagsJList = new JList();
    activatedTagsJList.setModel(new DefaultListModel());
    
    // confirm buttons
    okp = new OKCancelPanel(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okp.getOKButton()) {
          okAction();
        } else {
          setVisible(false);
          dispose();
        }
      }
    });
    JPanel jpButtons = new JPanel(new VerticalLayout(5));
    jpButtons.add(addButton);
    jpButtons.add(removeButton);
    
    
    JPanel mainPanel = new JPanel(new MigLayout("ins 5,gapx 5,gapy 5", 
        "[grow][20][grow]", "[grow 5][][grow 95][]"));

    mainPanel.add(jta, "grow,span,wrap");
    mainPanel.add(new JLabel(Messages.getString("ActiveTagsWizard.1")));
    mainPanel.add(new JLabel());
    mainPanel.add(new JLabel(Messages.getString("ActiveTagsWizard.2")),"wrap");
    mainPanel.add(new JScrollPane(availableTagsJList), "grow,left");
    mainPanel.add(jpButtons, "center");
    mainPanel.add(new JScrollPane(activatedTagsJList), "grow,right,wrap");
    mainPanel.add(okp, "span,right");
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
      // we are inside the EDT
      new Thread(new Runnable() {

        @Override
        public void run() {
          for (Device d : DeviceManager.getInstance().getDevices()) {
            ManualDeviceRefreshReporter reporter = null;
            if (d.isReady()) {
              reporter = new ManualDeviceRefreshReporter(d);
              reporter.startup();
              reporter.cleanupDone();
              reporter.refreshStarted();
              d.refreshCommand(true);
              reporter.done(false);
              reporter = null;
            }
          }
        }
      }).start();
    }

    setVisible(false);
    dispose();
  }
}
