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
 *  $$Revision$$
 */
package org.jajuk.ui.action;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.FontManager;
import org.jajuk.ui.FontManager.JajukFont;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

public class DebugLogAction extends ActionBase {
	private static final long serialVersionUID = 1L;

	DebugLogAction() {
		super(Messages.getString("JajukJMenuBar.23"), IconLoader.ICON_TRACES, true);
		setShortDescription(Messages.getString("JajukJMenuBar.23")); 
	}

	public void perform(ActionEvent evt) {
		final JEditorPane text = new JEditorPane("text/html", getTraces()); 
		text.setEditable(false);
		text.setMargin(new Insets(10, 10, 10, 10));
		text.setOpaque(true);
		text.setBackground(Color.WHITE);
		text.setForeground(Color.DARK_GRAY);
		text.setFont(FontManager.getInstance().getFont(JajukFont.BOLD)); 
		final JDialog dialog = new JDialog(Main.getWindow(), Messages.getString("DebugLogAction.0"), false);
		JPanel jp = new JPanel();
		double[][] size = new double[][] { { 0.5f,20,0.5f }, { TableLayout.FILL, 10, 20, 5 } };
		jp.setLayout(new TableLayout(size));
		JButton jbRefresh = new JButton(Messages.getString("DebugLogAction.1"));
		jbRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Refresh traces
				text.setText(getTraces());
			}
		});
		JButton jbClose = new JButton(Messages.getString("Close"));
		jbClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
		jp.add(new JScrollPane(text), "0,0,2,0");
		jp.add(jbRefresh, "0,2");
		jp.add(jbClose, "2,2");
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
		for (String line : Log.getSpool()){
			traces += line + "<br>";
		}
		traces += "</HTML>";
		return traces;
	}
}
