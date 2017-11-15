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
package org.jajuk.ui.actions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.log.Log;

import net.miginfocom.swing.MigLayout;

/**
 * .
 */
public class DebugLogAction extends JajukAction {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new debug log action.
   */
  DebugLogAction() {
    super(Messages.getString("JajukJMenuBar.23"), IconLoader.getIcon(JajukIcons.TRACES), true);
    setShortDescription(Messages.getString("JajukJMenuBar.23"));
  }

  /* (non-Javadoc)
   * @see org.jajuk.ui.actions.JajukAction#perform(java.awt.event.ActionEvent)
   */
  @Override
  public void perform(ActionEvent evt) {
    final JEditorPane text = new JEditorPane("text/html", getHTMLTraces());
    text.setEditable(false);
    text.setMargin(new Insets(10, 10, 10, 10));
    text.setOpaque(true);
    text.setBackground(Color.WHITE);
    text.setForeground(Color.DARK_GRAY);
    text.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
    final JDialog dialog = new JDialog(JajukMainWindow.getInstance(),
        Messages.getString("DebugLogAction.0"), false);
    JButton jbCopy = new JButton(Messages.getString("DebugLogAction.2"),
        IconLoader.getIcon(JajukIcons.COPY_TO_CLIPBOARD));
    jbCopy.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        StringSelection data = new StringSelection(getRawTraces());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(data, data);
      }
    });
    JButton jbRefresh = new JButton(Messages.getString("DebugLogAction.1"),
        IconLoader.getIcon(JajukIcons.REFRESH));
    jbRefresh.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // Refresh traces
        text.setText(getHTMLTraces());
      }
    });
    JButton jbClose = new JButton(Messages.getString("Close"), IconLoader.getIcon(JajukIcons.CLOSE));
    jbClose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
    dialog.setLayout(new MigLayout("insets 10", "[grow]"));
    JScrollPane panel = new JScrollPane(text);
    UtilGUI.setEscapeKeyboardAction(dialog, panel);
    dialog.add(panel, "grow,wrap");
    dialog.add(jbCopy, "split 3,right,sg button");
    dialog.add(jbRefresh, "split 3,right,sg button");
    dialog.add(jbClose, "right,sg button");
    dialog.setPreferredSize(new Dimension(800, 600));
    dialog.pack();
    dialog.setLocationRelativeTo(JajukMainWindow.getInstance());
    dialog.setVisible(true);
  }

  private String getHTMLTraces() {
    final Properties systemProperties = System.getProperties();
    final Properties jajukProperties = Conf.getProperties();
    StringBuilder traces = new StringBuilder("<HTML><font color='green'><b>")
        //Add build date in case the version was not properly set in maintenance branches (like missing 'dev' suffix)
        .append(Const.JAJUK_VERSION).append('/').append(Const.JAJUK_VERSION_DATE).append('/')
        .append(cleanHTML(systemProperties.toString())).append("<br>")
        .append(cleanHTML(jajukProperties.toString())).append("</b></font><br>");
    // Display last traces in clear
    for (String line : Log.getSpool(false)) {
      traces.append(line).append("<br>");
    }
    traces.append("</HTML>");
    return traces.toString();
  }

  private String getRawTraces() {
    final Properties systemProperties = System.getProperties();
    final Properties jajukProperties = Conf.getProperties();
    StringBuilder traces = new StringBuilder()
        //Add build date in case the version was not propertly set in maintenance branches (like missing 'dev' suffix)
        .append(Const.JAJUK_VERSION).append('/').append(Const.JAJUK_VERSION_DATE).append('/')
        .append(systemProperties.toString()).append("\n").append(jajukProperties.toString())
        .append("\n");
    // Display last traces in clear
    for (String line : Log.getSpool(false)) {
      traces.append(line).append("\n");
    }
    return traces.toString();
  }

  /**
   * Replace some HTML in the properties to make them suitable for printing.
   * 
   * @param str 
   * 
   * @return the string
   */
  private static String cleanHTML(String str) {
    // don't allow HTML-formatting
    return str.replace("<", "&lt;");
  }
}
