/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
 *  $Revision: 2503 $
 */
package org.jajuk.ui.wizard;

import info.clearthought.layout.TableLayout;

import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.jajuk.util.IconLoader;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Refresh dialog
 * 
 */
public class RefreshDialog extends JFrame {

  /**
   * 
   */
  private static final long serialVersionUID = -7883506101436294760L;

  private JXBusyLabel jlAction;

  private JProgressBar progress;

  private JLabel jlRefreshing;

  public RefreshDialog() {
    super();
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        setUndecorated(true);
        setIconImage(IconLoader.ICON_LOGO.getImage());
        jlAction = new JXBusyLabel();
        progress = new JProgressBar(0, 100);
        jlRefreshing = new JLabel();
        double[][] dSize = new double[][] { { 5, 500, 5 }, { 5, 30, 5, 20, 5, 20, 5 } };
        setLayout(new TableLayout(dSize));
        add(jlAction, "1,1,c,c");
        add(progress, "1,3,f,c");
        add(jlRefreshing, "1,5,c,c");
        pack();
        setLocationRelativeTo(RefreshDialog.this);
        setVisible(true);
      }
    });
  }

  public void setAction(final String action, final Icon icon) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        jlAction.setText(action);
        jlAction.setIcon(icon);
        jlAction.setBusy(true);
      }
    });
  }

  public void setRefreshing(final String path) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        jlRefreshing.setText(path);
      }
    });
  }

  /**
   * 
   * @param pos
   *          position from 0 to 100
   */
  public void setProgress(final int pos) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        progress.setValue(pos);
      }
    });
  }

}
