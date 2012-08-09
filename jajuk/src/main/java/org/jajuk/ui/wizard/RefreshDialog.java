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

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jdesktop.swingx.JXBusyLabel;

/**
 * Refresh dialog.
 */
public class RefreshDialog extends JajukJDialog {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = -7883506101436294760L;
  private JXBusyLabel jlAction;
  private JProgressBar progress;
  private JLabel jlRefreshing;
  private boolean indeterminate = false;
  private long dateLastUpdateRefresh;
  private long dateLastUpdateProgress;
  /** Minimum dialog refresh interval in ms, avoid to saturate the EDT*. */
  private static int MIN_REFRESH_INTERVAL = 100;

  /**
   * Refresh dialog (labels and a progress bar).
   * 
   * @param indeterminate whether the progress is indeterminate or not
   * @param frame title
   */
  public RefreshDialog(final boolean indeterminate, String title) {
    super(JajukMainWindow.getInstance(),false);
    setTitle(title);
    this.indeterminate = indeterminate;
    setUndecorated(true);
    setIconImage(IconLoader.getIcon(JajukIcons.LOGO).getImage());
    jlAction = new JXBusyLabel();
    progress = new JProgressBar(0, 100);
    progress.setIndeterminate(indeterminate);
    jlRefreshing = new JLabel();
    setLayout(new MigLayout("insets 10,gapx 5, gapy 5", "[500!]"));
    add(jlAction, "center,wrap");
    add(progress, "center,grow,wrap");
    add(jlRefreshing, "center,wrap");
    pack();
    setLocationRelativeTo(RefreshDialog.this);
    setVisible(true);
  }

  /**
   * Sets the action.
   * 
   * 
   * @param action 
   * @param icon 
   */
  public void setAction(final String action, final Icon icon) {
    // check if the dialog is still visible, may be closed before this code is executed
    if (jlAction != null) {
      jlAction.setText(action);
      jlAction.setIcon(icon);
      jlAction.setBusy(true);
    }
  }

  /**
   * Sets the refreshing.
   * 
   * @param path the new refreshing
   */
  public void setRefreshing(final String path) {
    // No more than one GUI refresh every 100 ms
    if ((System.currentTimeMillis() - dateLastUpdateRefresh) < MIN_REFRESH_INTERVAL) {
      return;
    }
    dateLastUpdateRefresh = System.currentTimeMillis();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        // check if the dialog is still visible, may be closed before this code is executed
        if (jlRefreshing != null) {
          jlRefreshing.setText(path);
          jlRefreshing.setToolTipText(path);
        }
      }
    });
  }

  /**
   * Sets the progress.
   * 
   * @param pos position from 0 to 100
   */
  public void setProgress(final int pos) {
    if (!this.indeterminate) {
      // No more than one GUI refresh every 100 ms
      if ((System.currentTimeMillis() - dateLastUpdateProgress) < MIN_REFRESH_INTERVAL) {
        return;
      }
      dateLastUpdateProgress = System.currentTimeMillis();
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          // check if the dialog is still visible, may be closed before this code is executed
          if (progress != null) {
            progress.setValue(pos);
          }
        }
      });
    }
  }

  /* (non-Javadoc)
   * @see java.awt.Window#dispose()
   */
  @Override
  public void dispose() {
    // Required to avoid a memory leak
    if (jlAction != null) {
      jlAction.setBusy(false);
      jlAction = null;
    }
    super.dispose();
  }
}
