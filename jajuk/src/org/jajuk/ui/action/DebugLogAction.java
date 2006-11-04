/*
 * Author: Bart Cremers
 * Date: 13-dec-2005
 * Time: 8:43:46
 */
package org.jajuk.ui.action;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * @author Bertrand Florat
 * @since 01-oct-2006
 */
public class DebugLogAction extends ActionBase {
	private static final long serialVersionUID = 1L;

	DebugLogAction() {
		super(
				Messages.getString("JajukJMenuBar.23"), Util.getIcon(ICON_TRACES),true); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
		setShortDescription(Messages.getString("JajukJMenuBar.23")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) {
		// Store current traces
		String traces = "";
		Iterator it = Log.getSpool();
		while (it.hasNext()) {
			traces += it.next().toString() + '\n';
		}
		JTextArea jtaTraces = new JTextArea(traces); //$NON-NLS-1$
		jtaTraces.setLineWrap(true);
		jtaTraces.setWrapStyleWord(true);
		jtaTraces.setEditable(false);
		jtaTraces.setMargin(new Insets(10, 10, 10, 10));
		jtaTraces.setOpaque(true);
		jtaTraces.setForeground(Color.DARK_GRAY);
		jtaTraces.setFont(new Font("Dialog", Font.BOLD, 12)); //$NON-NLS-1$
		JDialog dialog = new JDialog(Main.getWindow(), Messages
				.getString("DebugLogAction.0"), true);
		dialog.add(new JScrollPane(jtaTraces));
		dialog.setPreferredSize(new Dimension(800, 600));
		dialog.pack();
		dialog.setLocationRelativeTo(Main.getWindow());
		dialog.setVisible(true);
	}
}
