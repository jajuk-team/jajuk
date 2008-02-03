/*
 *  Jajuk
 *  Copyright (C) 2005 The Jajuk Team
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
 *  $$Revision: 2644 $$
 */

package org.jajuk.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.jajuk.Main;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jfree.ui.about.AboutPanel;
import org.jfree.ui.about.Licences;
import org.jfree.ui.about.SystemPropertiesPanel;

/**
 * View used to show the Jajuk about and contributors.
 * <p>
 * Help perspective *
 */
public class AboutWindow extends JDialog implements ITechnicalStrings {

  private static final long serialVersionUID = 1L;

  /** Licence panel */
  private JPanel jpLicence;

  /** General informations panel */
  private AboutPanel ap;

  /** JVM properties panel */
  private SystemPropertiesPanel spp;

  /** Tabbed pane with previous panels */
  private JTabbedPane jtp;

  /** Additional informations */
  private static final String INFOS = "http://jajuk.info";

  /**
   * Constructor
   */
  public AboutWindow() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setTitle(Messages.getString("JajukJMenuBar.16"));
        initUI();
        setLocationByPlatform(true);
        setSize(new Dimension(600, 300));
        setVisible(true);
      }

    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    // license panel
    jpLicence = new JPanel(new BorderLayout());
    JTextArea jta = new JTextArea(Licences.getInstance().getGPL());
    jta.setLineWrap(true);
    jta.setWrapStyleWord(true);
    jta.setCaretPosition(0);
    jta.setEditable(false);
    jta.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent me) {
        if (me.getClickCount() == 1
            && ((me.getModifiersEx() & MouseEvent.SHIFT_DOWN_MASK) == MouseEvent.SHIFT_DOWN_MASK)
            && ((me.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) == MouseEvent.CTRL_DOWN_MASK)) {
          try {
            JDialog jd = new JDialog(Main.getWindow());
            ImageIcon ii = new ImageIcon(new URL("http://jajuk.sourceforge.net/01/flbf.jpg"));
            JPanel jp = new JPanel();
            jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
            JLabel jl = new JLabel(ii);
            jp.add(jl);
            jd.setContentPane(jp);
            jd.pack();
            jd.setLocationRelativeTo(Main.getWindow());
            jd.setVisible(true);
          } catch (Exception e) {
            // No logs
          }
        }
      }
    });

    jpLicence.add(new JScrollPane(jta));
    jtp = new JTabbedPane();
    JPanel jpAbout = new JPanel();
    jpAbout.setLayout(new BoxLayout(jpAbout, BoxLayout.Y_AXIS));
    ap = new AboutPanel("Jajuk", JAJUK_VERSION + " \"" + JAJUK_CODENAME + "\"" + " "
        + JAJUK_VERSION_DATE, "<html>Copyright 2003,2007<br>Jajuk team</html>", INFOS,
        IconLoader.ICON_LOGO.getImage());
    jpAbout.add(ap);
    jpAbout.add(Box.createVerticalGlue());
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
  public String getDesc() {
    return Messages.getString("AboutView.10");
  }

}
