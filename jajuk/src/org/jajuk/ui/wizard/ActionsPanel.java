/*
 *  Jajuk
 *  Copyright (C) 2006 bflorat
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *  $Revision$
 */

package org.jajuk.ui.wizard;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *  Problem state buttons panel
 *
 * @author     Bertrand Florat
 * @created    1 may 2006
 */
public class ActionsPanel extends JPanel {

    /**Problem text area*/
    JLabel jlProblem;
    
    JButton jbPrevious;
    JButton jbNext;
    JButton jbFinish;
    JButton jbCancel;
    
    /**Locale*/
    Locale locale;
    
    private static final String[] locales = {"fr","nl","ca","de","es"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final String[] Finish = {"Terminé","Afgerond","Finalitzar","Fertig","Finalizar"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final String[] Cancel = {"Annuler","Annuleren","Cancelar","Abbrechen","Cancelar"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final String[] Previous = {"Précédent","Vooropgaand","Anterior","Zurück","Anterior"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    private static final String[] Next = {"Suivant","Aanstaande","Següent","Weiter","Siguiente"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    private static final int CODE_FINISH = 0;
    private static final int CODE_CANCEL = 1;
    private static final int CODE_PREVIOUS = 2;
    private static final int CODE_NEXT = 3;
    
    /**Associated action listener*/
    ActionListener al;    
    
    /**
     * @param al associated action listener
     */
    public ActionsPanel(ActionListener al,Locale locale) {
        //set locale
        this.locale = locale;
        
        //Problem panel
        jlProblem = new JLabel(); 
        jlProblem.setForeground(Color.RED);
        jlProblem.setFont(new Font("Dialog",Font.BOLD,12)); //$NON-NLS-1$
        
        //Action buttons
        int xSeparator = 10;
        float fButton = 0.24f;
        double[][] layout = new double[][]{
                {TableLayout.FILL,fButton,xSeparator,fButton,xSeparator,fButton,xSeparator,fButton,20},
                {20}
        };
        JPanel jpButtons = new JPanel(new TableLayout(layout));
        jbPrevious = new JButton(getMessage(CODE_PREVIOUS));
        jbPrevious.addActionListener(al);
        jbPrevious.setActionCommand("Prev"); //$NON-NLS-1$
        jbNext = new JButton(getMessage(CODE_NEXT));
        jbNext.addActionListener(al);
        jbNext.setActionCommand("Next"); //$NON-NLS-1$
        jbCancel = new JButton(getMessage(CODE_CANCEL));
        jbCancel.addActionListener(al);
        jbCancel.setActionCommand("Cancel"); //$NON-NLS-1$
        jbFinish = new JButton(getMessage(CODE_FINISH));
        jbFinish.addActionListener(al);
        jbFinish.setActionCommand("Finish"); //$NON-NLS-1$
        jpButtons.add(jbCancel,"1,0"); //$NON-NLS-1$
        jpButtons.add(jbPrevious,"3,0"); //$NON-NLS-1$
        jpButtons.add(jbNext,"5,0"); //$NON-NLS-1$
        jpButtons.add(jbFinish,"7,0"); //$NON-NLS-1$
        
        //Main panel
        double[][] dScreenLayout = new double[][]{
                {TableLayout.FILL},
                {20,5,30}
        };
        TableLayout l = new TableLayout(dScreenLayout);
        setLayout(l);
        add(jlProblem,"0,0"); //$NON-NLS-1$
        add(jpButtons,"0,2"); //$NON-NLS-1$
    }
    
    /**
     * Set buttons states
     * @param bNext
     * @param bFinish
     */
    void setState(boolean bPrevious,boolean bNext,boolean bFinish){
       jbPrevious.setEnabled(bPrevious);
       jbFinish.setEnabled(bFinish);
       jbNext.setEnabled(bNext);
    }
    
    void setProblem(String sProblem){
        jlProblem.setText(sProblem);
        if (sProblem != null && !"".equals(sProblem)){ //$NON-NLS-1$
            jlProblem.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Color.DARK_GRAY));    
        }
        else{
            jlProblem.setBorder(null);
        }
    }
    
    /**
     * I18n utility
     * @param message
     * @return
     */
    private String getMessage(int message){
        String sLocale =  locale.getLanguage();
        List alLocales = Arrays.asList(locales);
        List alOK = Arrays.asList(Finish);
        List alCancel = Arrays.asList(Cancel);
        List alPrevious = Arrays.asList(Previous);
        List alNext = Arrays.asList(Next);
        if (alLocales.contains(sLocale)){
           int index = alLocales.indexOf(sLocale);
            switch(message){
            case CODE_FINISH:
                return Finish[index];
            case CODE_NEXT:
                return Next[index];
            case CODE_CANCEL:
                return Cancel[index];
            case CODE_PREVIOUS:
                return Previous[index];
            }
        }
        else{ //english is default
            switch(message){
            case CODE_FINISH:
                return "OK"; //$NON-NLS-1$
            case CODE_NEXT:
                return "Next"; //$NON-NLS-1$
            case CODE_CANCEL:
                return "Cancel"; //$NON-NLS-1$
            case CODE_PREVIOUS:
                return "Previous"; //$NON-NLS-1$
            }
        }
        return ""; //$NON-NLS-1$
    }
    
  
}
