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
 *  $Revision$
 */

package org.jajuk.ui;

import org.jajuk.i18n.Messages;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionListener;

import javax.swing.JPanel;

/**
 * Ok Cancel generic panel
 */
public class OKCancelPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JajukButton jbOk;

	private JajukButton jbCancel;

	/** Associated action listener */
	ActionListener al;

	public OKCancelPanel(ActionListener al) {
		this.al = al;
		// buttons
		double[][] dSize = {
				{ TableLayout.TRAILING, TableLayout.FILL, TableLayout.TRAILING,
						TableLayout.FILL, TableLayout.TRAILING }, { 0.99 } };
		setLayout(new TableLayout(dSize));
		jbOk = new JajukButton(Messages.getString("OK")); 
		jbOk.addActionListener(al);
		jbCancel = new JajukButton(Messages.getString("Cancel")); 
		jbCancel.addActionListener(al);
		add(jbOk, "1,0"); 
		add(jbCancel, "3,0"); 
	}

	/**
	 * OK Cancel panel with given button names
	 * 
	 * @param al
	 * @param sOKTitle
	 * @param sCancelTitle
	 */
	public OKCancelPanel(ActionListener al, String sOKTitle, String sCancelTitle) {
		this(al);
		jbOk.setText(sOKTitle);
		jbCancel.setText(sCancelTitle);
	}

	public JajukButton getOKButton() {
		return jbOk;
	}

	public JajukButton getCancelButton() {
		return jbCancel;
	}

}
