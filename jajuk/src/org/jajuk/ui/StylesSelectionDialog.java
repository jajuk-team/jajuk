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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.dj.Ambience;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.i18n.Messages;

/**
 *  Allow a user to select a list of styles 
 *
 * @author     Bertrand Florat
 * @created    9 march 2006
 */
public class StylesSelectionDialog extends JDialog implements ActionListener{

    private static final long serialVersionUID = 1L;
    JComboBox jcbAmbiences;
    JList jlist;
    OKCancelPanel okc;
    
    HashSet<Style> selectedStyles;
    
    HashSet<Style> disabledStyles;
    
    Vector<String> list;
    
    /**
     * @throws HeadlessException
     */
    public StylesSelectionDialog(HashSet disabledStyles) throws HeadlessException {
        super();
        this.selectedStyles = new HashSet();
        this.disabledStyles = disabledStyles; 
        setLocationByPlatform(true);
        setTitle(Messages.getString("DigitalDJWizard.14")); //$NON-NLS-1$
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
            //reset all indices to -1 to avoid selecting zero th item
            for (int i=0;i<selection.size();i++){
                indices[i] = -1;
            }
            //find all matching items
            int comp = 0;
            for (int i=0;i<jlist.getModel().getSize();i++){
                String modelStyle = (String)jlist.getModel().getElementAt(i);
                for (Style style:selection){
                    if (style.getName2().equals(modelStyle)){
                        indices[comp] = i;
                        comp ++;
                    }
                }
            }
            //select item in the list
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
        list = (Vector)StyleManager.getInstance().getStylesList().clone();
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
        //populate ambience combo
        jcbAmbiences = new JComboBox();
        for (Ambience ambience:AmbienceManager.getInstance().getAmbiences()){
           jcbAmbiences.addItem(ambience.getName()); 
        }
        //none ambience selected by default
        jcbAmbiences.setSelectedIndex(-1);
        jcbAmbiences.addActionListener(this);
        JPanel jpAmbiences = new JPanel();
        double[][] layoutCombo = new double[][]{
                {TableLayout.PREFERRED,10,TableLayout.FILL},
                {20}
        };
        jpAmbiences.setLayout(new TableLayout(layoutCombo));
        jpAmbiences.add(new JLabel(Messages.getString("DigitalDJWizard.58")),"0,0"); //$NON-NLS-1$ //$NON-NLS-2$
        jpAmbiences.add(jcbAmbiences,"2,0"); //$NON-NLS-1$
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
        double[][] layout = new double[][]{
                {10,TableLayout.PREFERRED,10},
                {10,20,5,TableLayout.PREFERRED,5,20,10}
        };
        setLayout(new TableLayout(layout));
        add(jpAmbiences,"1,1"); //$NON-NLS-1$
        add(jsp,"1,3"); //$NON-NLS-1$
        add(okc,"1,5"); //$NON-NLS-1$
        getRootPane().setDefaultButton(okc.getOKButton());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(jcbAmbiences)){
            ArrayList<Ambience> alAmbiences = new ArrayList(AmbienceManager.getInstance().getAmbiences());
            Ambience ambience = alAmbiences.get(jcbAmbiences.getSelectedIndex());
            //select all styles for this ambience
            setSelection(ambience.getStyles());
        }
    };
    
    
    

}
