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
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;
import org.jfree.ui.about.AboutPanel;
import org.jfree.ui.about.Licences;
import org.jfree.ui.about.SystemPropertiesPanel;

/**
 * View used to show the Jajuk about and contributors.
 * <p>
 * Help perspective *
 */
public class AboutWindow extends JajukJDialog {

  /**
   * Handle clicking on the license text.
   */
  private static final class LicenseMouseListener extends MouseAdapter {
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent me) {
      if (me.getClickCount() == 1
          && ((me.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK)
          && ((me.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK)) {
        try {
          UtilGUI.showPictureDialog("http://www.jajuk.info/images/flbf.jpg");
        } catch (Exception e) {
          Log.debug("Ignoring exception in AboutWindow: ", e);
        }
      }
    }
  }

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** License panel. */
  private JPanel jpLicence;

  /** General informations panel. */
  private AboutPanel ap;

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
    SwingUtilities.invokeLater(new Runnable() {
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
    jta.addMouseListener(new LicenseMouseListener());

    jpLicence.add(new JScrollPane(jta));
    jtp = new JTabbedPane();
    JPanel jpAbout = new JPanel();
    jpAbout.setLayout(new BoxLayout(jpAbout, BoxLayout.Y_AXIS));
    ap = new AboutPanel("Jajuk", Const.JAJUK_VERSION + " <" + Const.JAJUK_CODENAME + ">" + " "
        + Const.JAJUK_VERSION_DATE, Messages.getString("AboutView.11"), INFOS, IconLoader.getIcon(
        JajukIcons.LOGO).getImage());
    jpAbout.add(ap);
    jpAbout.add(Box.createVerticalGlue());
    spp = new SystemPropertiesPanel();
    jtp.addTab(Messages.getString("AboutView.7"), jpAbout);
    jtp.addTab(Messages.getString("AboutView.8"), jpLicence);
    jtp.addTab(Messages.getString("AboutView.9"), spp);
    add(jtp);

    // Add key listener to enable Escape key to close the window
    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        // allow to close the dialog with Escape
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          dispose();
        }
      }
    });
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
