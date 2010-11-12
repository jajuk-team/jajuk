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

package org.jajuk.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jfree.ui.about.Licences;
import org.jfree.ui.about.SystemPropertiesPanel;

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
  private SystemPropertiesPanel spp;

  /** Tabbed pane with previous panels. */
  private JTabbedPane jtp;

  /** Additional informations. */
  private static final String INFOS = "http://jajuk.info";

  /**
   * Constructor.
   */
  public AboutWindow() {
    super();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setTitle(Messages.getString("JajukJMenuBar.16"));
        initUI();
        setSize(new Dimension(600, 300));
        UtilGUI.centerWindow(AboutWindow.this);
        setVisible(true);
      }

    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  /**
   * Inits the ui.
   * DOCUMENT_ME
   */
  public void initUI() {
    // license panel
    jpLicence = new JPanel(new BorderLayout());
    JTextArea jta = new JTextArea(Licences.getInstance().getGPL());
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
    spp = new SystemPropertiesPanel();
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
