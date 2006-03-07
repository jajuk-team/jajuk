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

import java.awt.Component;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;

import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardPage;

/**
 * DJ creation wizard
 *
 * @author     Bertrand Florat
 * @created    4 mars 2006
 */
public class DigitalDJWizard implements ITechnicalStrings{

    /**
     * 
     * DJ type choice 
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class WizardPage1 extends WizardPage{
        ButtonGroup bgTypes;
        JRadioButton jrbTransitions;
        JRadioButton jrbProp;
        JRadioButton jrbAmbiance;
        
        public WizardPage1(){
            super(Long.toString(System.currentTimeMillis()),Messages.getString("DigitalDJWizard.0"));
            double[][] size = new double[][]
                                           {{20,TableLayout.FILL,20},
                    {20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20}};
            setLayout(new TableLayout(size));
            bgTypes = new ButtonGroup();
            jrbTransitions = new JRadioButton(Messages.getString("DigitalDJWizard.1"));
            jrbTransitions.setSelected(true);
            jrbProp = new JRadioButton(Messages.getString("DigitalDJWizard.2"));
            jrbAmbiance = new JRadioButton(Messages.getString("DigitalDJWizard.3"));
            bgTypes.add(jrbProp);
            bgTypes.add(jrbTransitions);
            add(jrbTransitions,"1,1");
            add(jrbProp,"1,3");
            add(jrbAmbiance,"1,5");
        }
        
        public static final String getDescription() {
            return Messages.getString("DigitalDJWizard.0");
        }
        
        protected String validateContents (Component component, Object o) {
            return null;
        }
    }
    
     /**
     * 
     * Proportions type 
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class WizardPageProportion extends WizardPage{
        JLabel jlName;
        JTextField jtfName;
        JCheckBox jcbUseRatings;
        JLabel jlRatingLevel;
        JSlider jsRatingLevel;
        JLabel jlFadeDuration;
        JSlider jsFadeDuration;
        JCheckBox jcbUnicity;
       
        /**
         * Page constructor
         *
         */
        public WizardPageProportion(){
            super(Long.toString(System.currentTimeMillis()),
                Messages.getString("DigitalDJWizard.5"));
            jlName = new JLabel(Messages.getString("DigitalDJWizard.6"));
            jtfName = new JTextField();
            jtfName.setToolTipText(Messages.getString("DigitalDJWizard.6"));
            jcbUseRatings = new JCheckBox(Messages.getString("DigitalDJWizard.7"),true);
            jcbUseRatings.setToolTipText(Messages.getString("DigitalDJWizard.7"));
            jlRatingLevel = new JLabel(Messages.getString("DigitalDJWizard.8"));
            jsRatingLevel = new JSlider(0,4,2);
            jsRatingLevel.setMajorTickSpacing(1);
            jsRatingLevel.setMinorTickSpacing(1);
            jsRatingLevel.setPaintTicks(true);
            jsRatingLevel.setPaintLabels(true);
            jsRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.8"));
            jlFadeDuration = new JLabel(Messages.getString("DigitalDJWizard.9"));
            jsFadeDuration = new JSlider(0,30,10);
            jsFadeDuration.setMajorTickSpacing(10);
            jsFadeDuration.setMinorTickSpacing(1);
            jsFadeDuration.setPaintTicks(true);
            jsFadeDuration.setPaintLabels(true);
            jsFadeDuration.setToolTipText(Messages.getString("DigitalDJWizard.9"));
            jcbUnicity = new JCheckBox(Messages.getString("DigitalDJWizard.10"),false);
            jcbUnicity.setToolTipText(Messages.getString("DigitalDJWizard.10"));
            double[][] size = new double[][]
                    {{10,TableLayout.PREFERRED,20,TableLayout.PREFERRED,10},
                    {20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,
                        TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20}};
            setLayout(new TableLayout(size));
            add(jlName,"1,1");
            add(jtfName,"3,1");
            add(jcbUseRatings,"1,3");
            add(jlRatingLevel,"1,5");
            add(jsRatingLevel,"3,5");
            add(jlFadeDuration,"1,7");
            add(jsFadeDuration,"3,7");
            add(jcbUnicity,"1,9");
        }
        
        public static final String getDescription() {
            return Messages.getString("DigitalDJWizard.5");
        }
        
        protected String validateContents (Component component, Object o) {
            return null;
        }
    }
    
    
    
    /**
     * Wizard constructor
     */
    public DigitalDJWizard() {
        //All we do here is assemble the list of WizardPage subclasses we
        //want to show:
        WizardPage[] pages = new WizardPage[] {
            new WizardPage1(),new WizardPageProportion()
        };
        
        //Use the utility method to compose a Wizard
        System.setProperty("wizard.sidebar.image",IMAGE_DJ);
        Wizard wizard = WizardPage.createWizard(Messages.getString("DigitalDJWizard.4"),pages);
             
        //And show it onscreen
        WizardDisplayer.showWizard(wizard);
    }

}
