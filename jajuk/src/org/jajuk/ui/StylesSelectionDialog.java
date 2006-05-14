/*
 *  Jajuk
 *  Copyright (C) 2004 bflorat
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

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.i18n.Messages;

/**
 *  Allow a user to select a list of styles 
 *
 * @author     Bertrand Florat
 * @created    9 march 2006
 */
public class StylesSelectionDialog extends JDialog {

    JList jlist;
    OKCancelPanel okc;
    
    HashSet<Style> selectedStyles;
    
    HashSet<Style> disabledStyles;
    
    /**
     * @throws HeadlessException
     */
    public StylesSelectionDialog(HashSet disabledStyles) throws HeadlessException {
        super();
        this.selectedStyles = new HashSet();
        this.disabledStyles = disabledStyles; 
        setLocationByPlatform(true);
        setTitle(Messages.getString("DigitalDJWizard.14"));
        setModal(true);
        initUI();
        pack();
    }
    
    /**
     * Set selected item
     * @param selection or null to void it
     */
    public void setSelection(HashSet<Style> selection){
        if (selection != null){
            int[] indices = new int[selection.size()];
            int comp = 0;
            for (int i=0;i<jlist.getModel().getSize();i++){
                for (Style style:selection){
                    if (style.getName2().equals((String)jlist.getModel().getElementAt(i))){
                        indices[comp] = i;
                        comp ++;
                    }
                }
            }
            jlist.setSelectedIndices(indices);
        }
    }

    
    /**
     * 
     * @return selected styles 
     */
    public HashSet<Style> getSelectedStyles(){
        return selectedStyles;
    }
    
    private void initUI(){
        Vector<String> list = (Vector)StyleManager.getInstance().getStylesList().clone();
        //remove disabled items
        if (disabledStyles != null){
            Iterator it = list.iterator();
            while (it.hasNext()){
                String testedStyle = (String)it.next();
                for (Style disabledStyle:disabledStyles){
                    if (disabledStyle.getName2().equals(testedStyle)){
                        it.remove();
                    }
                }
            }
        }
        //main part of the dialog
        jlist = new JList(list);
        jlist.setLayoutOrientation(JList.VERTICAL_WRAP);
        jlist.setPreferredSize(new Dimension(600,600));
        jlist.setVisibleRowCount(-1);
        okc = new OKCancelPanel(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (ae.getSource() == okc.getOKButton()){
                    int[] selection = jlist.getSelectedIndices();
                    for (int i=0;i<selection.length;i++){
                        selectedStyles.add(
                            StyleManager.getInstance().getStyleByName( 
                                (String)jlist.getModel().getElementAt(selection[i])));
                    }
                }
                dispose();
            }
        });
        JScrollPane jsp = new JScrollPane(jlist);
        setLayout(new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS));
        add(jsp);
        add(Box.createVerticalStrut(20));
        add(okc);
        add(Box.createVerticalStrut(20));
        getRootPane().setDefaultButton(okc.getOKButton());
    };
    
    
    

}
