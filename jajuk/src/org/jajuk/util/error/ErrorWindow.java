/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Log$
 * Revision 1.1  2003/10/07 21:02:19  bflorat
 * Initial commit
 *
 */
package org.jajuk.util.error;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

/**
 * 
 * 
 * @author		sgringoi
 * @version	1.0
 * @created		5 oct. 2003
 */
public class ErrorWindow extends JFrame {

	private javax.swing.JPanel jContentPane = null;

	private javax.swing.JButton jButton = null;
	
	/** Exception to display */
	private JajukException jajukException = null;
	
	private javax.swing.JTextPane jTextPane = null;
	/**
	 * This is the default constructor
	 * 
	 * @param pExcp Original exception.
	 */
	public ErrorWindow(JajukException pExcp) {
		super();

		jajukException = pExcp;
		initialize();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(300, 200);
		this.setContentPane(getJContentPane());
		this.setTitle("Error window");
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private javax.swing.JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new javax.swing.JPanel();
			jContentPane.setLayout(new java.awt.BorderLayout());
			jContentPane.add(getJButton(), java.awt.BorderLayout.SOUTH);
			jContentPane.add(getJTextPane(), java.awt.BorderLayout.NORTH);
		}
		return jContentPane;
	}
	/**
	 * This method initializes jButton
	 * 
	 * @return javax.swing.JButton
	 */
	private javax.swing.JButton getJButton() {
		final ErrorWindow winInstance = this;
		
		if(jButton == null) {
			jButton = new javax.swing.JButton();
			jButton.setName("CloseButton");
			jButton.setText("Close");
			
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					winInstance.dispose();
				}
			};
			
			jButton.addActionListener(al);
		}
		return jButton;
	}
	/**
	 * This method initializes jTextPane
	 * 
	 * @return javax.swing.JTextPane
	 */
	private javax.swing.JTextPane getJTextPane() {
		if(jTextPane == null) {
			jTextPane = new javax.swing.JTextPane();
			jTextPane.setText(jajukException.getLocalizedMessage());
		}
		return jTextPane;
	}
	/**
	 * @see java.awt.Component#setVisible(boolean)
	 */
	public void setVisible(boolean b) {
		jajukException.printStackTrace();
		super.setVisible(b);
	}

}
