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

import java.awt.Container;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;

/**
 * View used to show the Jajuk about and contributors.
 * <p>
 * Help perspective *
 */
public class DonateWindow extends JajukJDialog {

  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The Constant BUDGET_LINK.   */
  private static final String BUDGET_LINK = "http://jajuk.info/index.php/Project_budget";

  /** The Constant DONATE_LINK.   */
  private static final String DONATE_LINK = "http://jajuk.info/index.php/Donate";

  /**
   * Constructor.
   */
  public DonateWindow() {
    super();

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setTitle(Messages.getString("JajukDonate.1"));
        initUI();
        pack();
        UtilGUI.centerWindow(DonateWindow.this);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
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
   * 
   */
  public void initUI() {
    Container cp = this.getContentPane();
    cp.setLayout(new MigLayout("", "center", "center"));
    cp.add(new JLabel(IconLoader.getIcon(JajukIcons.LOGO)), "wrap");
    cp.add(new JLabel(Messages.getString("JajukDonate.2")), "wrap");
    JButton jbBudget = new JButton(Messages.getString("JajukDonate.3"));
    jbBudget.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          Desktop.getDesktop().browse(new URI(BUDGET_LINK));
          dispose();
        } catch (IOException e1) {
          Log.error(e1);
        } catch (URISyntaxException e1) {
          Log.error(e1);
        }

      }
    });
    cp.add(jbBudget, "wrap");
    cp.add(new JLabel(Messages.getString("JajukDonate.4")), "wrap");
    JButton jbDonation = new JButton(Messages.getString("JajukDonate.1"));
    jbDonation.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          Desktop.getDesktop().browse(new URI(DONATE_LINK));
          dispose();
        } catch (IOException e1) {
          Log.error(e1);
        } catch (URISyntaxException e1) {
          Log.error(e1);
        }

      }
    });
    cp.add(jbDonation, "wrap");
  }
}
