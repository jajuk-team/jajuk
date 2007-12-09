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
import java.util.List;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class DuplicateFilesList extends JPanel implements ListSelectionListener {
	
	private static final long serialVersionUID = 1L;
	
    private JList list;
    private DefaultListModel listModel;

    private static final String deleteString = "Delete";
    
    private JButton deleteButton;
    
    public DuplicateFilesList(List<File> Files) {
        super(new BorderLayout());

        listModel = new DefaultListModel();
        for (File f : Files){
        	listModel.addElement(f.getName() + " => " + f.getAbsolutePath());
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
            //This method can be called only if
            //there's a valid selection
            //so go ahead and remove whatever's selected.
            int index = list.getSelectedIndex();
            listModel.remove(index);

            int size = listModel.getSize();

            if (size == 0) { //Nobody's left, disable firing.
                deleteButton.setEnabled(false);

            } else { //Select an index.
                if (index == listModel.getSize()) {
                    //removed item in last position
                    index--;
                }

                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
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