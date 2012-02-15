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
package org.jajuk.ui.actions;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;

/**
 * DOCUMENT_ME.
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
    final JEditorPane text = new JEditorPane("text/html", getTraces());
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
        StringSelection data = new StringSelection(text.getText());
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
        text.setText(getTraces());
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

  /**
   * Gets the traces.
   * 
   * @return Current traces
   */
  private String getTraces() {
    // Store system properties
    StringBuilder traces = new StringBuilder("<HTML><font color='green'><b>")
        .append(cleanHTML(UtilString.getAnonymizedSystemProperties().toString())).append("<br>")
        .append(cleanHTML(UtilString.getAnonymizedJajukProperties().toString()))
        .append("</b></font><br>");
    // Store last traces
    for (String line : Log.getSpool()) {
      traces.append(line).append("<br>");
    }
    traces.append("</HTML>");

    return traces.toString();
  }

  /**
   * Replace some HTML in the properties to make them suitable for printing.
   * 
   * @param str DOCUMENT_ME
   * 
   * @return the string
   */
  private static String cleanHTML(String str) {
    // don't allow HTML-formatting
    return str.replace("<", "&lt;");
  }
}
