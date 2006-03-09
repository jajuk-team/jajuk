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

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JList;

import org.jajuk.Main;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.util.Util;

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
    
    /**
     * @throws HeadlessException
     */
    public StylesSelectionDialog() throws HeadlessException {
        super(Main.getWindow(),
             Messages.getString("DigitalDJWizard.14"),true);
        initUI();
        pack();
        Util.setCenteredLocation(Main.getWindow());
        setVisible(true);
        setSize(new Dimension(640,480));
    }

    
    /**
     * 
     * @return selected styles 
     */
    public HashSet<Style> getSelectedStyles(){
        return selectedStyles;
    }
    
    private void initUI(){
        jlist = new JList(StyleManager.getInstance().getStylesList());
        jlist.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        jlist.setAutoscrolls(true);
        jlist.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        okc = new OKCancelPanel(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if (ae.getSource() == okc.getOKButton()){
                    int[] selection = jlist.getSelectedIndices();
                    for (int i=0;i<selection.length;i++){
                        selectedStyles.add(
                            StyleManager.getInstance().getStyleAt(selection[i]));
                    }
                }
                dispose();
            }
        });
        okc.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        double[][] size = new double[][]{{TableLayout.PREFERRED},
                {TableLayout.PREFERRED,TableLayout.PREFERRED}};
        setLayout(new TableLayout(size));
        
        add(jlist,"0,0");
        add(okc,"0,1");
    };
    
    
    

}
