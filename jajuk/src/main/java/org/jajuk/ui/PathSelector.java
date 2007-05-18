/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukFileFilter;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This is a widgets that contains an editable textfield given a PATH
 * and a PATH selection button opening up a file selector
 */
public class PathSelector extends JPanel implements ITechnicalStrings {

	private static final long serialVersionUID = -8370527665529267952L;

	JTextField jtfUrl;

	JButton button;

	/**
	 * Construct a Path Selector
	 * 
	 * @param filter
	 *            the filter used to select the item
	 * @param sDefault 
	 * 			Initialized path, null of none
	 */
	public PathSelector(final JajukFileFilter filter, String sDefault) {
		// Set layout
		double[][] size = new double[][] { { 200, 10, TableLayout.PREFERRED },
				{ TableLayout.PREFERRED } };
		setLayout(new TableLayout(size));
		setOpaque(false);
		// Build items
		jtfUrl = new JTextField();
		if (sDefault != null){
			jtfUrl.setText(sDefault);
		}
		jtfUrl.setToolTipText(Messages.getString("Path"));
		jtfUrl.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		button = new JButton(IconLoader.ICON_OPEN_FILE);
		button.setToolTipText(Messages.getString("Path"));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JajukFileChooser jfc = new JajukFileChooser(filter);
				jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));
				jfc.setMultiSelectionEnabled(false);
				String sUrl = jtfUrl.getText();
				if (!sUrl.equals("")) {
					// if URL is already set, use it as current directory
					jfc.setCurrentDirectory(new File(sUrl));
				}
				int returnVal = jfc.showOpenDialog(Main.getWindow());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = jfc.getSelectedFile();
					String newPath = file.getAbsolutePath();
					//Call specific operation if URL changed
					if (!jtfUrl.getText().equals(newPath)){
						performOnURLChange();
					}
					jtfUrl.setText(newPath);
					
				}
			}

		});
		//Add items
		add(jtfUrl,"0,0");
		add(button,"2,0");
	}
	
	/**
	 * 
	 * @return URL
	 */
	public String getUrl(){
		return jtfUrl.getText();
	}
	
	public void setEnabled(boolean b){
		jtfUrl.setEnabled(b);
		button.setEnabled(b);								
	
	
	}
	
	/**
	 * This method can be extended to perform specific actions when selected changes URL 
	 *
	 */
	public void performOnURLChange(){
		
	}
	
	
	/**
	 * Set tooltip
	 * @param s
	 */
	public void setToolTipText(String s){
		jtfUrl.setToolTipText(s);
		button.setToolTipText(s);
	}

}
