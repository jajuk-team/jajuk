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
    
    private static final String[] locales = {"fr","nl","ca","de","es"};
    private static final String[] Finish = {"Terminé","Afgerond","Finalitzar","Fertig","Finalizar"};
    private static final String[] Cancel = {"Annuler","Annuleren","Cancelar","Abbrechen","Cancelar"};
    private static final String[] Previous = {"Précédent","Vooropgaand","Anterior","Vorig","Anterior"};
    private static final String[] Next = {"Suivant","Aanstaande","Següent","Folgende","Siguiente"};

    private static final int CODE_FINISH = 0;
    private static final int CODE_CANCEL = 1;
    private static final int CODE_PREVIOUS = 2;
    private static final int CODE_NEXT = 3;
    
    /**Associated action listener*/
    ActionListener al;    
    
    /**
     * @param al associated action listener
     */
    public ActionsPanel(ActionListener al) {
        //Problem panel
        jlProblem = new JLabel(); 
        jlProblem.setForeground(Color.RED);
        jlProblem.setFont(new Font("Dialog",Font.BOLD,12));
        
        //Action buttons
        int xSeparator = 10;
        float fButton = 0.2f;
        double[][] layout = new double[][]{
                {TableLayout.FILL,fButton,xSeparator,fButton,xSeparator,fButton,xSeparator,fButton,20},
                {20}
        };
        JPanel jpButtons = new JPanel(new TableLayout(layout));
        jbPrevious = new JButton(getMessage(CODE_PREVIOUS));
        jbPrevious.addActionListener(al);
        jbPrevious.setActionCommand("Prev");
        jbNext = new JButton(getMessage(CODE_NEXT));
        jbNext.addActionListener(al);
        jbNext.setActionCommand("Next");
        jbCancel = new JButton(getMessage(CODE_CANCEL));
        jbCancel.addActionListener(al);
        jbCancel.setActionCommand("Cancel");
        jbFinish = new JButton(getMessage(CODE_FINISH));
        jbFinish.addActionListener(al);
        jbFinish.setActionCommand("Finish");
        jpButtons.add(jbCancel,"1,0");
        jpButtons.add(jbPrevious,"3,0");
        jpButtons.add(jbNext,"5,0");
        jpButtons.add(jbFinish,"7,0");
        
        //Main panel
        double[][] dScreenLayout = new double[][]{
                {TableLayout.FILL},
                {20,5,30}
        };
        TableLayout l = new TableLayout(dScreenLayout);
        setLayout(l);
        add(jlProblem,"0,0");
        add(jpButtons,"0,2");
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
        if (sProblem != null && !"".equals(sProblem)){
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
        String sLocale =  Locale.getDefault().getLanguage();
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
                return "OK";
            case CODE_NEXT:
                return "Next";
            case CODE_CANCEL:
                return "Cancel";
            case CODE_PREVIOUS:
                return "Previous";
            }
        }
        return "";
    }
    
  
}
