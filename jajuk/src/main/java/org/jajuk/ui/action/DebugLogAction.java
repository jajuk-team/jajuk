/*
 * Author: Bart Cremers
 * Date: 13-dec-2005
 * Time: 8:43:46
 */
package org.jajuk.ui.action;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * @author Bertrand Florat
 * @since 01-oct-2006
 */
public class DebugLogAction extends ActionBase {
	private static final long serialVersionUID = 1L;

	DebugLogAction() {
		super(Messages.getString("JajukJMenuBar.23"), Util.getIcon(ICON_TRACES), true); //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
		setShortDescription(Messages.getString("JajukJMenuBar.23")); //$NON-NLS-1$
	}

	public void perform(ActionEvent evt) {
		final JEditorPane text = new JEditorPane("text/html", getTraces()); //$NON-NLS-1$
		text.setEditable(false);
		text.setMargin(new Insets(10, 10, 10, 10));
		text.setOpaque(true);
		text.setForeground(Color.DARK_GRAY);
		text.setFont(new Font("Dialog", Font.BOLD, ConfigurationManager.getInt(CONF_FONTS_SIZE))); //$NON-NLS-1$
		JDialog dialog = new JDialog(Main.getWindow(), Messages.getString("DebugLogAction.0"), true);
		JPanel jp = new JPanel();
		double[][] size = new double[][] { { TableLayout.FILL }, { TableLayout.FILL, 10, 20, 5 } };
		jp.setLayout(new TableLayout(size));
		JButton jbRefresh = new JButton(Messages.getString("DebugLogAction.1"));
		jbRefresh.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				// Refresh traces
				text.setText(getTraces());
			}

		});
		jp.add(new JScrollPane(text), "0,0");
		jp.add(jbRefresh, "0,2");
		dialog.add(jp);
		dialog.setPreferredSize(new Dimension(800, 600));
		dialog.pack();
		dialog.setLocationRelativeTo(Main.getWindow());
		dialog.setVisible(true);
	}

	/**
	 * 
	 * @return Current traces
	 */
	private String getTraces() {
		// Store system properties
		String traces = "<HTML><font color='green'><b>"
				+ Util.getAnonymizedSystemProperties().toString() + "<br>"
				+ Util.getAnonymizedJajukProperties().toString() + "</b></font><br>";
		// Store last traces
		Iterator it = Log.getSpool();
		while (it.hasNext()) {
			traces += it.next().toString() + "<br>";
		}
		traces += "</HTML>";
		return traces;
	}
}
