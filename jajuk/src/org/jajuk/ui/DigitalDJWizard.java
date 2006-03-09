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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.jajuk.base.DigitalDJManager;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Transition;
import org.jajuk.base.TransitionDigitalDJ;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.netbeans.api.wizard.WizardDisplayer;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardBranchController;
import org.netbeans.spi.wizard.WizardController;
import org.netbeans.spi.wizard.WizardPanelProvider;

/**
 * DJ creation wizard
 *
 * @author     Bertrand Florat
 * @created    4 mars 2006
 */
public class DigitalDJWizard implements ITechnicalStrings{

    /**Wizard action*/
    private static final String KEY_ACTION = "ACTION";
    /**DJ type variable name */
    private static final String KEY_DJ_TYPE = "TYPE";
    /**DJ name variable name */
    private static final String KEY_DJ_NAME = "NAME";
    /**Track unicity */
    private static final String KEY_UNICITY = "UNICITY";
    /**Use ratings flag */
    private static final String KEY_USE_RATINGS = "USE_RATINGS";
    /**Ratings level */
    private static final String KEY_RATINGS_LEVEL = "RATING_LEVEL";
    /**Fade duration*/
    private static final String KEY_FADE_DURATION = "FADE_DURATION";
    
    /**
     * 
     * DJ type choice 
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class TypeSelectionPanel extends JPanel implements ActionListener{
        /**Unique ID for this panel*/
        protected static final String ID = "TYPE_SELECTION"; 
        /**Transition DJ code*/
        private static final int DJ_TYPE_TRANSITION = 0;
        /**Proportions DJ code*/
        private static final int DJ_TYPE_PROPORTION = 1;
        /**Ambience DJ code*/
        private static final int DJ_TYPE_AMBIENCE = 2;
        
        private final WizardController controller;
        private final Map data;

        ButtonGroup bgTypes;
        JRadioButton jrbTransitions;
        JRadioButton jrbProp;
        JRadioButton jrbAmbiance;
        
        public TypeSelectionPanel(WizardController controller,Map data){
            initUI();
            this.controller = controller;
            this.data = data;
            data.put(KEY_DJ_TYPE,0); //default value
        }
        
        /**
         * Create panel UI
         *
         */
        private void initUI(){
            double[][] size = new double[][]
                                           {{20,TableLayout.FILL,20},
                    {20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20}};
            setLayout(new TableLayout(size));
            bgTypes = new ButtonGroup();
            jrbTransitions = new JRadioButton(Messages.getString("DigitalDJWizard.1"));
            jrbTransitions.setSelected(true);
            jrbTransitions.addActionListener(this);
            jrbProp = new JRadioButton(Messages.getString("DigitalDJWizard.2"));
            jrbProp.addActionListener(this);
            jrbAmbiance = new JRadioButton(Messages.getString("DigitalDJWizard.3"));
            jrbAmbiance.addActionListener(this);
            bgTypes.add(jrbProp);
            bgTypes.add(jrbTransitions);
            bgTypes.add(jrbAmbiance);
            add(jrbTransitions,"1,1");
            add(jrbProp,"1,3");
            add(jrbAmbiance,"1,5");
        }
        
        

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == jrbTransitions){
                data.put(KEY_DJ_TYPE,0);
            }
            else if (e.getSource() == jrbProp){
                data.put(KEY_DJ_TYPE,1);
            }
            else if (e.getSource() == jrbAmbiance){
                data.put(KEY_DJ_TYPE,2);
            }
        }
    }
    
    /**
     * 
     * Action type (new or alter)
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class ActionSelectionPanel extends JPanel implements ActionListener{
        /**Unique ID for this panel*/
        protected static final String ID = "ACTION_SELECTION"; 
        /**NEW code*/
        private static final int ACTION_CREATION = 0;
        /**CHANGE code*/
        private static final int ACTION_CHANGE = 1;
        /**DELETE code*/
        private static final int ACTION_DELETE = 2;
        
        private final WizardController controller;
        private final Map data;

        ButtonGroup bgActions;
        JRadioButton jrbNew;
        JRadioButton jrbChange;
        JRadioButton jrbDelete;
        
        public ActionSelectionPanel(WizardController controller,Map data){
            initUI();
            this.controller = controller;
            this.data = data;
            data.put(KEY_ACTION,0); //default value: create
        }
        
        /**
         * Create panel UI
         *
         */
        private void initUI(){
            double[][] size = new double[][]
                                           {{10,TableLayout.FILL,10},
                    {20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20}};
            setLayout(new TableLayout(size));
            bgActions = new ButtonGroup();
            jrbNew = new JRadioButton(Messages.getString("DigitalDJWizard.17"));
            jrbNew.setSelected(true);
            jrbNew.addActionListener(this);
            jrbChange = new JRadioButton(Messages.getString("DigitalDJWizard.18"));
            jrbChange.addActionListener(this);
            jrbDelete = new JRadioButton(Messages.getString("DigitalDJWizard.19"));
            jrbDelete.addActionListener(this);
            bgActions.add(jrbNew);
            bgActions.add(jrbChange);
            bgActions.add(jrbDelete);
            add(jrbNew,"1,1");
            add(jrbChange,"1,3");
            add(jrbDelete,"1,5");
        }
        
        

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == jrbNew){
                data.put(KEY_ACTION,0);
            }
            else if (e.getSource() == jrbChange){
                data.put(KEY_ACTION,1);
            }
            else if (e.getSource() == jrbDelete){
                data.put(KEY_ACTION,2);
            }
        }
    }
    
     /**
     * 
     * Initial steps
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class InitStep extends WizardPanelProvider{

        /**
         * @param steps
         * @param descriptions
         */
        protected InitStep() {
            super(Messages.getString("DigitalDJWizard.4"), 
                new String[]{ActionSelectionPanel.ID,TypeSelectionPanel.ID,GeneralOptionsPanel.ID,TransitionsPanel.ID}, 
                new String[]{Messages.getString("DigitalDJWizard.16"),Messages.getString("DigitalDJWizard.0")
                ,Messages.getString("DigitalDJWizard.5"),Messages.getString("DigitalDJWizard.20")});
        }

        /* (non-Javadoc)
         * @see org.netbeans.spi.wizard.WizardPanelProvider#createPanel(org.netbeans.spi.wizard.WizardController, java.lang.String, java.util.Map)
         */
        @Override
        protected JComponent createPanel(WizardController controller, 
                String id, Map settings) {
            switch(indexOfStep(id)){
            case 0:
                return new ActionSelectionPanel(controller,settings);
            case 1:
                return new TypeSelectionPanel(controller,settings);
            case 2:
                return new GeneralOptionsPanel(controller,settings);
            case 3:
                return new TransitionsPanel(controller,settings);
            default:
                throw new IllegalArgumentException(id);
            }
        }
        
    }
    
     /**
     * 
     * General options panel 
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class GeneralOptionsPanel extends JPanel 
        implements ActionListener,CaretListener,ItemListener{
        /**Unique ID for this panel*/
        protected static final String ID = "GENERAL_OPTIONS"; 
        
        private final WizardController controller;
        private final Map data;
        
        JLabel jlName;
        JTextField jtfName;
        JCheckBox jcbUseRatings;
        JLabel jlRatingLevel;
        JSlider jsRatingLevel;
        JLabel jlFadeDuration;
        JSlider jsFadeDuration;
        JCheckBox jcbUnicity;
       
        /**
         * General options (commun to all types)
         *
         */
        public GeneralOptionsPanel(WizardController controller,Map data){
            initUI();
            this.controller = controller;
            this.data = data;
        }
        
        /**
         * Create panel UI
         *
         */
        private void initUI(){
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

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == jcbUnicity){
                data.put(KEY_UNICITY,jcbUnicity.isSelected());
            }
            if (ae.getSource() == jcbUseRatings){
                data.put(KEY_USE_RATINGS,jcbUseRatings.isSelected());
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
         */
        public void caretUpdate(CaretEvent ce) {
            if (ce.getSource() == jtfName){
                data.put(KEY_DJ_NAME,jtfName.getText());
            }
        }

        /* (non-Javadoc)
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getSource() == jsFadeDuration && !jsFadeDuration.getValueIsAdjusting()){
                data.put(KEY_FADE_DURATION,jsFadeDuration.getValue());
            }
            else if (ie.getSource() == jsRatingLevel && !jsRatingLevel.getValueIsAdjusting()){
                data.put(KEY_RATINGS_LEVEL,jsRatingLevel.getValue());
            }
        }
    }
    
    
    /**
     * 
     * Transitions panel
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class TransitionsPanel extends JPanel{
        /**Unique ID for this panel*/
        protected static final String ID = "TRANSITIONS_PANEL"; 
        
        private final WizardController controller;
        private final Map data;
        
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        /**Transitions**/
        ArrayList<Transition> alTransitions;
        
        /**DJ**/
        TransitionDigitalDJ dj = null;
        
        /**Current transition being populated*/
        Transition currentTransition;
        
        /**
         * General options (commun to all types)
         *
         */
        public TransitionsPanel(WizardController controller,Map data){
            alTransitions = new ArrayList(10);
            initUI();
            this.controller = controller;
            this.data = data;
        }
        
        /**
         * Create panel UI
         *
         */
        private void initUI(){
            //Get DJ
            if (data != null){
                dj = (TransitionDigitalDJ)DigitalDJManager.getInstance().getDJ((String)data.get(KEY_DJ_NAME));
                if (dj != null){ //null if new DJ
                    alTransitions = dj.getTransitions();
                }
            }
            add(getTransitionsPanel());
        }
        
        /**
         * 
         * @return a panel containing all transitions
         */
        private JScrollPane getTransitionsPanel(){
            JPanel out = new JPanel();
            widgets = new JComponent[alTransitions.size()+2][4]; //+1 for header and +1 for new empty row
            //Create header
            JLabel jlHeader1 = new JLabel();
            JLabel jlHeader2 = new JLabel(Messages.getString("DigitalDJWizard.22"));
            jlHeader2.setFont(new Font("Dialog",Font.BOLD,12));
            JLabel jlHeader3 = new JLabel(Messages.getString("DigitalDJWizard.23"));
            jlHeader3.setFont(new Font("Dialog",Font.BOLD,12));
            JLabel jlHeader4 = new JLabel(Messages.getString("DigitalDJWizard.24"));
            jlHeader4.setFont(new Font("Dialog",Font.BOLD,12));
            widgets[0][0] = jlHeader1;
            widgets[0][1] = jlHeader2;
            widgets[0][2] = jlHeader3;
            widgets[0][3] = jlHeader4;
            //Delete|FROM list| To list|nb tracks  
            double p = TableLayout.PREFERRED;
            double[] dHoriz = {25,150,150,p};
            double[] dVert = new double[widgets.length]; 
            dVert[0] = 20;
            //now add all known transitions
            for (int i=1;i<widgets.length;i++){
                //Delete button
                JButton jbDelete = new JButton(Util.getIcon(ICON_DELETE));
                jbDelete.addActionListener(new ActionListener() {
                
                    public void actionPerformed(ActionEvent arg0) {
                        alTransitions.remove(getWidgetIndex(TransitionsPanel.this));
                        removeAll();
                        //refresh panel
                        add(getTransitionsPanel());
                        revalidate();
                        repaint();
                    }
                
                });
                jbDelete.setToolTipText(Messages.getString("DigitalDJWizard.21"));
                widgets[i][0] = jbDelete;
                //From style list
                JButton jbFrom = new JButton(Util.getIcon(ICON_LIST));
                if (alTransitions.size() >= i){
                    Transition transition = alTransitions.get(i-1);
                    jbFrom.setText(transition.getFromString());
                }
                jbFrom.addActionListener(new ActionListener() {
                
                    public void actionPerformed(ActionEvent arg0) {
                        synchronized(StyleManager.getInstance().getLock()){
                            StylesSelectionDialog dialog = new StylesSelectionDialog();
                            HashSet styles =  dialog.getSelectedStyles();
                            System.out.println(styles);
                            //check if transition already exists
                            int index = getWidgetIndex((JComponent)arg0.getSource());
                            if (alTransitions.size() > index){
                                Transition existingTransition = alTransitions.get(index);
                                existingTransition.setFrom(styles);
                            }
                            else{ //create the transition
                                Transition transition = new Transition(styles,null,0);
                                alTransitions.add(transition);
                            }
                        }
                    }
                
                });
                jbFrom.setToolTipText(Messages.getString("DigitalDJWizard.22"));
                widgets[i][1] = jbFrom;
                //To style list
                JButton jbTo = new JButton(Util.getIcon(ICON_LIST));
                if (alTransitions.size() >= i){
                    Transition transition = alTransitions.get(i-1);
                    jbTo.setText(transition.getToString());
                }
                jbTo.addActionListener(new ActionListener() {
                
                    public void actionPerformed(ActionEvent arg0) {
                        synchronized(StyleManager.getInstance().getLock()){
                            StylesSelectionDialog dialog = new StylesSelectionDialog();
                            HashSet styles =  dialog.getSelectedStyles();
                            System.out.println(styles);
                            //check if transition already exists
                            int index = getWidgetIndex((JComponent)arg0.getSource());
                            if (alTransitions.size() > index){
                                Transition existingTransition = alTransitions.get(index);
                                existingTransition.setTo(styles);
                            }
                            else{ //create the transition
                                Transition transition = new Transition(null,styles,0);
                                alTransitions.add(transition);
                            }
                        }
                    }
                });

                jbTo.setToolTipText(Messages.getString("DigitalDJWizard.23"));
                widgets[i][2] = jbTo;
                //Nb of tracks
                JSpinner jsNb = new JSpinner(new SpinnerNumberModel(2,1,10,1));
                jsNb.setToolTipText(Messages.getString("DigitalDJWizard.24"));
                widgets[i][3] = jsNb;
                //Set layout
                dVert[i] = 20;
            }
            //Create layout
            double[][] dSizeProperties = new double[][]{dHoriz,dVert};  
            TableLayout layout = new TableLayout(dSizeProperties);
            layout.setHGap(10);
            layout.setVGap(10);
            out.setLayout(layout);
            //Add widgets
            int i = 0;
            for (i=0;i<widgets.length;i++){
                out.add(widgets[i][0],"0,"+i+",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
                out.add(widgets[i][1],"1,"+i); //$NON-NLS-1$
                out.add(widgets[i][2],"2,"+i); //$NON-NLS-1$ //$NON-NLS-2$
                out.add(widgets[i][3],"3,"+i+",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return new JScrollPane(out);
        }
        
        /**
         * 
         * @param widget
         * @return index of a given widget row in the widget table
         */
        private int getWidgetIndex(JComponent widget){
            int resu = -1;
            for (int row=1;row<widgets.length;row++){
                for (int col=0;col<widgets[0].length;col++){
                    if (widget.equals(widgets[row][col])){
                        resu = row;
                        break;
                    }    
                }
                
            }
            return resu;
        }
    }
    
    /**
     * 
     *  DJ wizard controller, gives next step for current context
     *
     * @author     Bertrand Florat
     * @created    09/03/2006
     */
    public static class DJWizardController extends WizardBranchController{
        /**Cached instance for perfs*/
        InitStep typeStep = new InitStep();  
        
        
        /**
         * @param base
         */
        protected DJWizardController() {
            super(new InitStep());
        }
        
        /**
         * Return next panel provider
         */
        protected WizardPanelProvider getPanelProviderForStep(String step,Map data){
            if (step.equals(TypeSelectionPanel.ID)){
                 return typeStep;    
            }
            /*assert step == KEY_DJ_TYPE;
            int iType = (Integer)data.get(step);
            switch (iType){
            case DJ_TYPE_TRANSITION: //Transition
                break;
            case DJ_TYPE_PROPORTION: //Proportion
                break;
            case DJ_TYPE_AMBIENCE: //Ambience
                break;
            }*/
            return null;
        }
        
    }
    
    
    /**
     * Wizard constructor
     */
    public DigitalDJWizard() {
        //Set sidebar image location
        System.setProperty("wizard.sidebar.image",IMAGE_DJ);
        Wizard wizard = new DJWizardController().createWizard();
             
        //And show it onscreen
        WizardDisplayer.showWizard(wizard);
    }

}
