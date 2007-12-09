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
 *  
 */

package org.jajuk.ui.widgets;

import org.jajuk.base.File;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

import java.util.ArrayList;
import java.util.List;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class DuplicateFilesList extends JPanel implements ListSelectionListener {
	
	private static final long serialVersionUID = 1L;
	
    private JList list;
    private DefaultListModel listModel;
    private List<File> allFiles;

    private static final String deleteString = "Delete";
    
    private JButton deleteButton;
        
    public DuplicateFilesList(List<List<File>> Files) {
        super(new BorderLayout());
        
        allFiles = new ArrayList<File>();
        listModel = new DefaultListModel();
        
        for (List<File> L : Files){
        	allFiles.add(L.get(0));
        	listModel.addElement(L.get(0).getName() + " => " + L.get(0).getAbsolutePath());
        	L.remove(0);
        	for (File f : L){
        		allFiles.add(f);
        		listModel.addElement("  + " + f.getName() + " => " + f.getAbsolutePath());
        	}
        }
        
        //Create the list and put it in a scroll pane.
        list = new JList(listModel);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(30);
        JScrollPane listScrollPane = new JScrollPane(list);
               
        deleteButton = new JButton(deleteString);
        deleteButton.setActionCommand(deleteString);
        deleteButton.addActionListener(new DeleteListener());

        //Create a panel that uses BoxLayout.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane,
                                           BoxLayout.LINE_AXIS));
        buttonPane.add(deleteButton);
        buttonPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        add(listScrollPane, BorderLayout.CENTER);
        add(buttonPane, BorderLayout.PAGE_END);
    }

    class DeleteListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
        	int indices[] = list.getSelectedIndices();
        	String sFiles= "";
        	for (int i: indices){
        		sFiles += allFiles.get(i).getName() + "\n";
        	}

        	int iResu = Messages.getChoice(Messages
					.getString("Confirmation_delete_files")
                    	+ " : \n\n" + sFiles, JOptionPane.YES_NO_CANCEL_OPTION,
                    	JOptionPane.INFORMATION_MESSAGE);
			if (iResu != JOptionPane.YES_OPTION) {
				return;
			}
        	           
        	for (int i: indices){
        		try{
        			Util.deleteFile(allFiles.get(i).getIO());
        		}catch (Exception ioe) {
    				Log.error(131, ioe);
        		}
        		listModel.remove(i);
        	}
        }
    }
    
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {

            if (list.getSelectedIndex() == -1) {
            //No selection, disable fire button.
                deleteButton.setEnabled(false);

            } else {
            //Selection, enable the fire button.
                deleteButton.setEnabled(true);
            }
        }
    }
}