/*
 *  Jajuk
 *  Copyright (C) 2007 bflorat
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

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.JajukFileFilter;
import org.jajuk.util.Util;

/**
 * This is a compount widgets that contains an editable textfield given a PATH
 * and a PATH selection button opening up a file selector
 * 
 * @author Bertrand Florat
 * @created 5 march 07
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
		double[][] size = new double[][] { { 200, 10, TableLayout.FILL, TableLayout.PREFERRED },
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
		button = new JButton(Util.getIcon(ICON_OPEN_FILE));
		button.setToolTipText(Messages.getString("Path"));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JajukFileChooser jfc = new JajukFileChooser(new JajukFileFilter(
						JajukFileFilter.DirectoryFilter.getInstance()));
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setDialogTitle(Messages.getString("DeviceWizard.43"));//$NON-NLS-1$
				jfc.setMultiSelectionEnabled(false);
				String sUrl = jtfUrl.getText();
				if (!sUrl.equals("")) {
					// if url is already set, use it as current directory
					jfc.setCurrentDirectory(new File(sUrl));
				}
				int returnVal = jfc.showOpenDialog(Main.getWindow());
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = jfc.getSelectedFile();
					jtfUrl.setText(file.getAbsolutePath());
				}
			}

		});
		//Add items
		add(jtfUrl,"0,0");
		add(button,"3,0");
	}
	
	/**
	 * 
	 * @return URL
	 */
	public String getUrl(){
		return jtfUrl.getText();
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
