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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.tags.Tag;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.widgets.OKCancelPanel;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Wizard allowing user to select the extra tags to be displayed by jajuk.
 */
public class ExtraTagsConfigurationWizard extends JajukJDialog {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  private JList availableTagsJList;

  private JList activatedTagsJList;

  /** OK/Cancel buttons. */
  private OKCancelPanel okp;

  private List<String> availableList = new ArrayList<String>();

  private List<String> activatedList = new ArrayList<String>();

  /**
   * Instantiates a new extra tags configuration wizard.
   */
  public ExtraTagsConfigurationWizard() {
    super(JajukMainWindow.getInstance(), true);
    setTitle(Messages.getString("JajukWindow.40"));
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    initUI();
    refreshList();
    populate();
    setLocationByPlatform(true);
    setPreferredSize(new Dimension(600, 600));
    pack();
    setVisible(true);
  }

  /**
   * Refresh list.
   * 
   */
  private void refreshList() {
    availableList.clear();
    for (String s : Tag.getSupportedTagFields()) {
      if (!Tag.getActivatedExtraTags().contains(s)) {
        availableList.add(s);
      }
    }

    activatedList.clear();
    for (String s : Tag.getActivatedExtraTags()) {
      activatedList.add(s);
    }
  }

  /**
   * Populate.
   * 
   */
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

  /**
   * Inits the ui.
   * 
   */
  private void initUI() {
    JTextArea jta = new JTextArea() {
      private static final long serialVersionUID = 1L;

      /**
       * Display an info label in the text area
       *
       * @param g
       *          
       */
      @Override
      public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(IconLoader.getIcon(JajukIcons.INFO).getImage(), 4, 3, 16, 16, null);
      }
    };
    jta.setTabSize(2);
    jta.setText('\t' + Messages.getString("ActiveTagsWizard.3"));
    jta.setLineWrap(true);
    jta.setEditable(false);
    jta.setWrapStyleWord(true);
    jta.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    // available tags
    availableTagsJList = new JList(new DefaultListModel());

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

    // Add items
    setLayout(new MigLayout("ins 5,gapx 5,gapy 5", "[grow][20][grow]", "[grow 5][][grow 95][]"));

    // Keep the pad : it fixes a known "issue/feature" with some text component. MigLayout manual
    // (see http://migcalendar.com/miglayout/whitepaper.html) says "Note! Padding multi-line
    // components derived from JTextComponent (such as JTextArea) without setting a explicit minimum
    // size may result in an continuous size escalation (animated!)"
    add(jta, "grow,span,wrap,pad 5 5 -5 -5");
    add(new JLabel(Messages.getString("ActiveTagsWizard.1")));
    add(new JLabel());
    add(new JLabel(Messages.getString("ActiveTagsWizard.2")), "wrap");
    add(new JScrollPane(availableTagsJList), "grow,left,sg list");
    add(jpButtons, "center");
    add(new JScrollPane(activatedTagsJList), "grow,right,sg list,wrap");
    add(okp, "span,right");
  }

  /**
   * Ok action.
   * 
   */
  private void okAction() {
    TrackManager tm = TrackManager.getInstance();
    // cleanup removed tags
    for (PropertyMetaInformation m : tm.getCustomProperties()) {
      if (Tag.getSupportedTagFields().contains(m.getName()) && !activatedList.contains(m.getName())) {
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
            if (d.isReady()) {
              d.manualRefreshDeep();
            }
          }
        }
      }).start();
    }

    setVisible(false);
    dispose();
  }
}
