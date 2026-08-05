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
package org.jajuk.ui.wizard;

import net.miginfocom.swing.MigLayout;
import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.util.*;
import org.jajuk.util.log.Log;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/**
 * View used to show the Jajuk about and contributors.
 * <p>
 * Help perspective *
 */
public class AboutWindow extends JajukJDialog {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** License panel. */
  private JPanel jpLicence;
  /** JVM properties panel. */
  private JPanel spp;
  /** Tabbed pane with previous panels. */
  private JTabbedPane jtp;
  /** Additional informations. */
  private static final String INFOS = "http://jajuk.info";

  /**
   * Constructor.
   */
  public AboutWindow() {
    SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            setTitle(Messages.getString("JajukJMenuBar.16"));
            initUI();
            //for some reasons, required to avoid blank dialogs
            pack();
            setSize(new Dimension(600, 300));
            UtilGUI.centerWindow(AboutWindow.this);
            setVisible(true);
        }
    });
  }

  /**
   * Creates the system properties panel manually (replacement for SystemPropertiesPanel).
   *
   * @return the panel with system properties table
   */
  private JPanel createSystemPropertiesPanel() {
    JPanel panel = new JPanel(new BorderLayout());

    // Collect system properties
    Properties props = System.getProperties();
    Object[] propNames = props.keySet().toArray();

    // Sort property names alphabetically
    Arrays.sort(propNames, (a, b) -> a.toString().compareTo(b.toString()));

    // Create table model: Property Name | Value
    DefaultTableModel model = new DefaultTableModel(
            new String[] { Messages.getString("AboutView.1"), Messages.getString("AboutView.2") },
            0
    ) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    // Populate table
    for (Object propNameObj : propNames) {
      String propName = propNameObj.toString();
      String propValue = props.getProperty(propName);

      // Mask sensitive properties
      if (propName.toLowerCase().contains("password") ||
              propName.toLowerCase().contains("secret") ||
              propName.toLowerCase().contains("key")) {
        propValue = "***";
      }

      // Truncate very long values
      if (propValue != null && propValue.length() > 500) {
        propValue = propValue.substring(0, 497) + "...";
      }

      model.addRow(new Object[] { propName, propValue != null ? propValue : "" });
    }

    // Create table
    JTable table = new JTable(model);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.getColumnModel().getColumn(0).setPreferredWidth(300);
    table.getColumnModel().getColumn(1).setPreferredWidth(350);
    table.setRowHeight(24);

    // Add horizontal scroll
    table.getTableHeader().setReorderingAllowed(false);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(null);

    panel.add(scrollPane, BorderLayout.CENTER);

    return panel;
  }

  /**
   * Loads the GPL license text from resources.
   * Tries multiple locations to ensure compatibility across different deployment modes.
   *
   * @return the license text or a fallback message
   */
  private String loadLicenseText() {
    StringBuilder license = new StringBuilder();
    String[] possibleLocations = {
            "/src/legals/LICENSE-GPL.txt",           // During development
            "/org/jajuk/legals/LICENSE-GPL.txt",     // After packaging
            "/LICENSE-GPL.txt",                      // Root of JAR
            "LICENSE-GPL.txt"                        // Fallback (filesystem)
    };

    // Try loading from ClassLoader resources first
    for (String location : possibleLocations) {
      try {
        InputStream is = getClass().getResourceAsStream(location);
        if (is != null) {
          license.append(new String(is.readAllBytes(), StandardCharsets.UTF_8));
          return license.toString();
        }
      } catch (IOException e) {
        Log.debug("Could not load license from: " + location);
        // Continue to next location
      }
    }

    // Fallback: try filesystem path relative to workspace
    try {
      java.io.File licenseFile = new java.io.File("src/legals/LICENSE-GPL.txt");
      if (licenseFile.exists()) {
        license.append(Files.readString(Paths.get(licenseFile.toURI()), StandardCharsets.UTF_8));
        return license.toString();
      }
    } catch (IOException e) {
      Log.error("Could not load license from filesystem: " + e.getMessage());
    }

    // Last resort: display error message
    return "GNU GENERAL PUBLIC LICENSE\n" +
            "Version 2, June 1991\n\n" +
            "Unable to load complete license text from resources.\n" +
            "Please refer to http://www.gnu.org/licenses/gpl-2.0.html\n\n" +
            "Jajuk is free software licensed under the GPL v2.\n" +
            "You can redistribute it and/or modify it under the terms\n" +
            "of the GNU General Public License as published by the\n" +
            "Free Software Foundation; either version 2 of the License,\n" +
            "or (at your option) any later version.";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  /**
   * Inits the ui.
   * 
   */
  public void initUI() {
    // license panel
    jpLicence = new JPanel(new BorderLayout());
    JTextArea jta = new JTextArea(loadLicenseText());
    jta.setLineWrap(true);
    jta.setWrapStyleWord(true);
    jta.setCaretPosition(0);
    jta.setEditable(false);
    jpLicence.add(new JScrollPane(jta));
    jtp = new JTabbedPane();
    JPanel jpAbout = new JPanel(new MigLayout("ins 5", "[grow]", "[grow][grow][grow]"));
    jpAbout.add(new JLabel(IconLoader.getIcon(JajukIcons.LOGO)), "left,split 2");
    jpAbout.add(new JLabel("Jajuk " + Const.JAJUK_VERSION + " <" + Const.JAJUK_CODENAME + ">" + " "
        + Const.JAJUK_VERSION_DATE), "wrap");
    jpAbout.add(new JLabel(Messages.getString("AboutView.11")), "center,wrap,grow");
    jpAbout.add(new JLabel(INFOS), "center,grow,wrap");
    spp = createSystemPropertiesPanel();
    jtp.addTab(Messages.getString("AboutView.7"), jpAbout);
    jtp.addTab(Messages.getString("AboutView.8"), jpLicence);
    jtp.addTab(Messages.getString("AboutView.9"), spp);
    add(jtp);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  /**
   * Gets the desc.
   * 
   * @return the desc
   */
  public String getDesc() {
    return Messages.getString("AboutView.10");
  }
}
