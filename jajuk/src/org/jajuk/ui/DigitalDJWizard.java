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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.base.Ambience;
import org.jajuk.base.AmbienceDigitalDJ;
import org.jajuk.base.AmbienceManager;
import org.jajuk.base.DigitalDJManager;
import org.jajuk.base.Proportion;
import org.jajuk.base.ProportionDigitalDJ;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Transition;
import org.jajuk.base.TransitionDigitalDJ;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jdesktop.swingx.autocomplete.Configurator;
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
    /**transitions*/
    private static final String KEY_TRANSITIONS = "TRANSITIONS";
    /**proportions*/
    private static final String KEY_PROPORTIONS = "PROPORTIONS";
    /**Ambience*/
    private static final String KEY_AMBIENCE = "AMBIENCE";
    
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
                                           {{20,TableLayout.FILL,10},
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
                new String[]{ActionSelectionPanel.ID,TypeSelectionPanel.ID,GeneralOptionsPanel.ID,
                    TransitionsPanel.ID,ProportionsPanel.ID,AmbiencePanel.ID}, 
                new String[]{Messages.getString("DigitalDJWizard.16"),Messages.getString("DigitalDJWizard.0")
                ,Messages.getString("DigitalDJWizard.5"),Messages.getString("DigitalDJWizard.20"),
                Messages.getString("DigitalDJWizard.29"),Messages.getString("DigitalDJWizard.31")});
        }

        /* (non-Javadoc)
         * @see org.netbeans.spi.wizard.WizardPanelProvider#createPanel(org.netbeans.spi.wizard.WizardController, java.lang.String, java.util.Map)
         */
        @Override
        protected JComponent createPanel(WizardController controller, 
                String id, Map settings) {
            switch(indexOfStep(id)){
            case 0:
                return new AmbiencePanel(controller,settings);
            case 1:
                return new TypeSelectionPanel(controller,settings);
            case 2:
                return new GeneralOptionsPanel(controller,settings);
            case 3:
                return new TransitionsPanel(controller,settings);
            case 4:
                return new ProportionsPanel(controller,settings);
            case 5:
                return new AmbiencePanel(controller,settings);
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
        
        JLabel jlStartWith;
        JComboBox jcbStartwith;
        JPanel jpStartwith;
        
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        /**Transitions**/
        ArrayList<Transition> alTransitions;
        
        /**DJ**/
        TransitionDigitalDJ dj = null;
      
        /**
         * General options (commun to all types)
         *
         */
        public TransitionsPanel(WizardController controller,Map data){
            alTransitions = new ArrayList(10);
            alTransitions.add(new Transition(1)); //add a void transition
            initUI();
            this.controller = controller;
            this.data = data;
            controller.setProblem(Messages.getString("DigitalDJWizard.26"));
        }
        
        /**
         * Create panel UI
         *
         */
        private void initUI(){
            jlStartWith = new JLabel(Messages.getString("DigitalDJWizard.25"));
            Vector<String> styles = StyleManager.getInstance().getStylesList();
            jcbStartwith = new JComboBox(styles);
            Configurator.enableAutoCompletion(jcbStartwith);
            jpStartwith = new JPanel();
            double[][] dSize = {{0.25,10,0.25},{20}};
            jpStartwith.setLayout(new TableLayout(dSize));
            jpStartwith.add(jlStartWith,"0,0");
            jpStartwith.add(jcbStartwith,"2,0");
            //Get DJ
            if (data != null){
                dj = (TransitionDigitalDJ)DigitalDJManager.getInstance().getDJ((String)data.get(KEY_DJ_NAME));
                if (dj != null){ //null if new DJ
                    alTransitions = dj.getTransitions();
                }
            }
            //set layout
            double[][] dSizeGeneral = {{10,0.99,5},
                    {10,TableLayout.PREFERRED,10,TableLayout.FILL,10}};
            setLayout(new TableLayout(dSizeGeneral));
            add(jpStartwith,"1,1");
            add(getTransitionsPanel(),"1,3");
        }
        
        /**
         * 
         * @return a panel containing all transitions
         */
        private JScrollPane getTransitionsPanel(){
            widgets = new JComponent[alTransitions.size()][4];
            JPanel out = new JPanel();
            //Delete|FROM list| To list|nb tracks  
            double[] dHoriz = {25,150,150,TableLayout.PREFERRED};
            double[] dVert = new double[widgets.length+2]; 
            dVert[0] = 20;
            //now add all known transitions
            for (int index=0;index<alTransitions.size();index++ ){
                //Delete button
                JButton jbDelete = new JButton(Util.getIcon(ICON_DELETE));
                jbDelete.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        alTransitions.remove(getWidgetIndex((JComponent)ae.getSource()));
                        refreshScreen();
                    }
                });
                //cannot delete if void selection
                if (alTransitions.size() == 1){
                    jbDelete.setEnabled(false);
                }
                jbDelete.setToolTipText(Messages.getString("DigitalDJWizard.21"));
                widgets[index][0] = jbDelete;
                //From style list
                JButton jbFrom = new JButton(Util.getIcon(ICON_LIST));
                Transition transition = alTransitions.get(index);
                if (transition.getFrom().size() > 0){
                    jbFrom.setText(transition.getFromString());
                    jbFrom.setToolTipText(transition.getFromString());
                }
                jbFrom.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        int row = getWidgetIndex((JComponent)ae.getSource()); 
                        addStyle(row,true);
                    }
                });
                jbFrom.setToolTipText(Messages.getString("DigitalDJWizard.22"));
                widgets[index][1] = jbFrom;
                //To style list
                JButton jbTo = new JButton(Util.getIcon(ICON_LIST));
                if (transition.getTo().size() > 0){
                    jbTo.setText(transition.getToString());
                    jbTo.setToolTipText(transition.getToString());
                }
                jbTo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        int row = getWidgetIndex((JComponent)ae.getSource()); 
                        addStyle(row,false);
                    }
                });
                jbTo.setToolTipText(Messages.getString("DigitalDJWizard.23"));
                widgets[index][2] = jbTo;
                //Nb of tracks
                JSpinner jsNb = new JSpinner(new SpinnerNumberModel(2,1,10,1));
                jsNb.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent ce) {
                        int row = getWidgetIndex((JComponent)ce.getSource());
                        int nb = Integer.parseInt(((JSpinner)ce.getSource()).getValue().toString());
                        Transition transition = alTransitions.get(row);
                        transition.setNb(nb);
                    }
                });
                jsNb.setToolTipText(Messages.getString("DigitalDJWizard.24"));
                widgets[index][3] = jsNb;
                //Set layout
                dVert[index+1] = 20;
            }
            dVert[widgets.length+1] = 20; //final space
            //Create layout
            double[][] dSizeProperties = new double[][]{dHoriz,dVert};  
            TableLayout layout = new TableLayout(dSizeProperties);
            layout.setHGap(10);
            layout.setVGap(10);
            out.setLayout(layout);
            //Create header
            JLabel jlHeader2 = new JLabel(Messages.getString("DigitalDJWizard.22"));
            jlHeader2.setFont(new Font("Dialog",Font.BOLD,12));
            JLabel jlHeader3 = new JLabel(Messages.getString("DigitalDJWizard.23"));
            jlHeader3.setFont(new Font("Dialog",Font.BOLD,12));
            JLabel jlHeader4 = new JLabel(Messages.getString("DigitalDJWizard.24"));
            jlHeader4.setFont(new Font("Dialog",Font.BOLD,12));
            out.add(jlHeader2,"1,0");
            out.add(jlHeader3,"2,0");
            out.add(jlHeader4,"3,0");
            //Add widgets
            for (int i=0;i<dVert.length-2;i++){
                out.add(widgets[i][0],"0,"+(i+1)+",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
                out.add(widgets[i][1],"1,"+(i+1)); //$NON-NLS-1$
                out.add(widgets[i][2],"2,"+(i+1)); //$NON-NLS-1$ //$NON-NLS-2$
                out.add(widgets[i][3],"3,"+(i+1)+",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return new JScrollPane(out);
        }
        
        /**
         * Add a style to a transition
         * @param row row
         * @param bFrom is it a from button ?
         */
        private void addStyle(int row,boolean bFrom){
            synchronized(StyleManager.getInstance().getLock()){
                Transition transition = alTransitions.get(row);
                //create list of styles used in existing transitions
                HashSet disabledStyles = new HashSet();
                for (int i=0;i<alTransitions.size();i++){
                    Transition t = alTransitions.get(i);
                    //ignore all styles expect those from current button
                    if (bFrom || i != row){   
                        disabledStyles.addAll(t.getTo());
                    }
                    if (!bFrom || i != row){
                        disabledStyles.addAll(t.getFrom());
                    }
                }
                StylesSelectionDialog dialog = new StylesSelectionDialog(disabledStyles);
                if (bFrom){
                    dialog.setSelection(transition.getFrom());
                }
                else{
                    dialog.setSelection(transition.getTo());
                }
                dialog.setVisible(true);
                HashSet<Style> styles =  dialog.getSelectedStyles();
                //check if at least one style has been selected
                if (styles.size() == 0){
                    return;
                }
                String sText = "";
                for (Style style:styles){
                    sText += style.getName2()+',';  
                }
                sText = sText.substring(0,sText.length()-1);
                int nb = Integer.parseInt(((JSpinner)widgets[row][3]).getValue().toString());
                //Set button text
                if (bFrom){
                    ((JButton)widgets[row][1]).setText(sText);
                }
                else{
                    ((JButton)widgets[row][2]).setText(sText);
                }
                //set selected style in transition object
                if (bFrom){
                    transition.setFrom(styles);
                }
                else{
                    transition.setTo(styles);
                }
                //check if the transaction is fully selected now
                if (transition.getFrom().size() > 0 
                        && transition.getTo().size() > 0){
                    //Make sure current delete button is now enabled
                    ((JButton)widgets[row][0]).setEnabled(true);
                    
                    //Reset wizard error message
                    controller.setProblem(null);
                    
                    //Fill wizard data
                    data.put(KEY_TRANSITIONS,alTransitions);
                    
                    //create a new void proportion if needed
                    if (!containsVoidItem()){
                        alTransitions.add(new Transition(nb)); //we duplicate the nb for new row
                    }
                    
                    //Refresh screen to add a new void row
                    refreshScreen();
                }
            }
        }
        
        /**
         * 
         * @return whether a void item already exist 
         * (used to avoid creating several void items)
         */
        private boolean containsVoidItem(){
            for (int i=0;i<widgets.length;i++){
                JButton jbFrom = (JButton)widgets[i][1];
                JButton jbTo = (JButton)widgets[i][2];
                if (jbFrom.getText().equals("")
                        || jbTo.getText().equals("")){
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Refresh panel
         *
         */
        private void refreshScreen(){
            removeAll();
            //refresh panel
            add(jpStartwith,"1,1");
            add(getTransitionsPanel(),"1,3");
            revalidate();
            repaint();
        }
        
        /**
         * 
         * @param widget
         * @return index of a given widget row in the widget table
         */
        private int getWidgetIndex(JComponent widget){
            int resu = -1;
            for (int row=0;row<widgets.length;row++){ 
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
     * Proportion panel
     *
     * @author     Bertrand Florat
     * @created    17 march 2006
     */
    public static class ProportionsPanel extends JPanel{
        /**Unique ID for this panel*/
        protected static final String ID = "PROPORTION_PANEL"; 
        
        private final WizardController controller;
        private final Map data;
        
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        /**Proportions**/
        ArrayList<Proportion> proportions;
        
        /**DJ**/
        ProportionDigitalDJ dj = null;
        
        /**
         * General options (commun to all types)
         */
        public ProportionsPanel(WizardController controller,Map data){
            proportions = new ArrayList(10);
            proportions.add(new Proportion()); //add a void item
            initUI();
            this.controller = controller;
            this.data = data;
            controller.setProblem(Messages.getString("DigitalDJWizard.30"));
        }
        
        /**
         * Create panel UI
         *
         */
        private void initUI(){
            //Get DJ
            if (data != null){
                dj = (ProportionDigitalDJ)DigitalDJManager.getInstance().getDJ((String)data.get(KEY_DJ_NAME));
                if (dj != null){ //null if new DJ
                    //transfer propostions from set to local list
                    HashSet<Proportion> hs = dj.getProportions();
                    for (Proportion proportion:hs){
                        proportions.add(proportion);
                    }
                }
            }
            //set layout
            double[][] dSizeGeneral = {{10,0.99,5},
                    {10,TableLayout.PREFERRED,10}};
            setLayout(new TableLayout(dSizeGeneral));
            add(getProportionsPanel(),"1,1");
        }
        
        /**
         * 
         * @return a panel containing all proportions
         */
        private JScrollPane getProportionsPanel(){
            widgets = new JComponent[proportions.size()][3];
            JPanel out = new JPanel();
            //Delete|Style list|proportion in %  
            double[] dHoriz = {25,TableLayout.FILL,250,TableLayout.FILL,TableLayout.PREFERRED};
            double[] dVert = new double[widgets.length+2]; 
            dVert[0] = 20;
            //now add all known proportions
            for (int index=0;index<proportions.size();index++ ){
                //Delete button
                JButton jbDelete = new JButton(Util.getIcon(ICON_DELETE));
                jbDelete.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        proportions.remove(getWidgetIndex((JComponent)ae.getSource()));
                        refreshScreen();
                    }
                });
                //cannot delete if void selection
                if (proportions.size() == 1){
                    jbDelete.setEnabled(false);
                }
                jbDelete.setToolTipText(Messages.getString("DigitalDJWizard.21"));
                widgets[index][0] = jbDelete;
                //style list
                JButton jbStyle = new JButton(Util.getIcon(ICON_LIST));
                Proportion proportion = proportions.get(index);
                if (proportion.getStyles() != null){
                    jbStyle.setText(proportion.getStylesDesc());
                    jbStyle.setToolTipText(proportion.getStylesDesc());
                }
                jbStyle.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        int row = getWidgetIndex((JComponent)ae.getSource()); 
                        addStyle(row);
                    }
                });
                jbStyle.setToolTipText(Messages.getString("DigitalDJWizard.27"));
                widgets[index][1] = jbStyle;
                //Proportion
                JSpinner jsNb = new JSpinner(new SpinnerNumberModel(20,1,100,1));
                jsNb.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent ce) {
                        int row = getWidgetIndex((JComponent)ce.getSource());
                        int nb = Integer.parseInt(((JSpinner)ce.getSource()).getValue().toString());
                        Proportion proportion = proportions.get(row);
                        proportion.setProportion(nb);
                    }
                });
                jsNb.setToolTipText(Messages.getString("DigitalDJWizard.28"));
                widgets[index][2] = jsNb;
                //Set layout
                dVert[index+1] = 20;
            }
            dVert[widgets.length+1] = 20; //final space
            //Create layout
            double[][] dSizeProperties = new double[][]{dHoriz,dVert};  
            TableLayout layout = new TableLayout(dSizeProperties);
            layout.setHGap(10);
            layout.setVGap(10);
            out.setLayout(layout);
            //Create header
            JLabel jlHeader1 = new JLabel(Messages.getString("DigitalDJWizard.27"));
            jlHeader1.setFont(new Font("Dialog",Font.BOLD,12));
            JLabel jlHeader2 = new JLabel(Messages.getString("DigitalDJWizard.28"));
            jlHeader2.setFont(new Font("Dialog",Font.BOLD,12));
            out.add(jlHeader1,"2,0");
            out.add(jlHeader2,"4,0");
            //Add widgets
            for (int i=0;i<dVert.length-2;i++){
                out.add(widgets[i][0],"0,"+(i+1)+",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
                out.add(widgets[i][1],"2,"+(i+1)); //$NON-NLS-1$
                out.add(widgets[i][2],"4,"+(i+1)); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return new JScrollPane(out);
        }
        
        /**
         * Add a style to a proportion
         * @param row row
         */
        private void addStyle(int row){
            synchronized(StyleManager.getInstance().getLock()){
                Proportion proportion = proportions.get(row);
                //create list of styles used in existing transitions
                HashSet disabledStyles = new HashSet();
                for (int i=0;i<proportions.size();i++){
                    if (i != row){ //do not exlude current proportion that will be selected
                        disabledStyles.addAll(proportions.get(i).getStyles());    
                    }
                }
                StylesSelectionDialog dialog = new StylesSelectionDialog(disabledStyles);
                dialog.setSelection(proportion.getStyles());
                dialog.setVisible(true);
                HashSet<Style> styles =  dialog.getSelectedStyles();
                //check if at least one style has been selected
                if (styles.size() == 0){
                    return;
                }
                //reset styles
                proportion.setStyle(new HashSet());
                String sText = "";
                for (Style style:styles){
                    proportion.addStyle(style);
                    sText += style.getName2()+',';  
                }
                sText = sText.substring(0,sText.length()-1);
                int nb = Integer.parseInt(((JSpinner)widgets[row][2]).getValue().toString());
                //Set button text
                ((JButton)widgets[row][1]).setText(sText);
                //check if the proportion is fully selected now
                if (proportion.getStyles().size() > 0 ){
                    //Make sure current delete button is now enabled
                    ((JButton)widgets[row][0]).setEnabled(true);
                    
                    //Reset wizard error message
                    controller.setProblem(null);
                    
                    //Fill wizard data
                    data.put(KEY_PROPORTIONS,proportions);
                    
                    //create a new void proportion if needed
                    if (!containsVoidItem()){
                        proportions.add(new Proportion());
                    }
                    
                    //Refresh screen to add a new void row
                    refreshScreen();
                }
            }
        }
        
        /**
         * 
         * @return whether a void item already exist 
         * (used to avoid creating several void items)
         */
        private boolean containsVoidItem(){
            for (int i=0;i<widgets.length;i++){
                JButton jb = (JButton)widgets[i][1];
                if (jb.getText().equals("")){
                    return true;
                }
            }
            return false;
        }
        
        /**
         * Refresh panel
         *
         */
        private void refreshScreen(){
            removeAll();
            //refresh panel
            add(getProportionsPanel(),"1,1");
            revalidate();
            repaint();
        }
        
        /**
         * 
         * @param widget
         * @return index of a given widget row in the widget table
         */
        private int getWidgetIndex(JComponent widget){
            int resu = -1;
            for (int row=0;row<widgets.length;row++){ 
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
     * Ambience based
     *
     * @author     Bertrand Florat
     * @created    18 march 2006
     */
    public static class AmbiencePanel extends JPanel implements ActionListener{
        /**Unique ID for this panel*/
        protected static final String ID = "AMBIENCE_PANEL"; 
        
        private final WizardController controller;
        private final Map data;
        
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        JButton jbNew;
        JButton jbDelete;
        JPanel jpButtons;
        
        /**Ambiences**/
        ArrayList<Ambience> ambiences;
        
        /**DJ**/
        AmbienceDigitalDJ dj = null;
        
        /**Selected ambience index*/
        int ambienceIndex = 0;
      
        /**
         * Generic Constructor
         */
        public AmbiencePanel(WizardController controller,Map data){
            ambiences = new ArrayList(10);
            initUI();
            this.controller = controller;
            this.data = data;
            //We need at least one ambience
            if (AmbienceManager.getInstance().getAmbiences().size() == 0){
                controller.setProblem(Messages.getString("DigitalDJWizard.38"));    
            }
        }
        
        /**
         * Create panel UI
         *
         */
        private void initUI(){
            //Get DJ
            if (data != null){
                dj = (AmbienceDigitalDJ)DigitalDJManager.getInstance().getDJ((String)data.get(KEY_DJ_NAME));
                //transfert ambiences to the list
                for (Ambience ambience:AmbienceManager.getInstance().getAmbiences()){
                    this.ambiences.add(ambience);
                }
            }
            //set layout
            double[][] dSizeGeneral = {{10,0.99,5},
                    {10,TableLayout.PREFERRED,10,20,10}};
            setLayout(new TableLayout(dSizeGeneral));
            //button layout
            double[][] dButtons = {{TableLayout.FILL,TableLayout.PREFERRED,
                TableLayout.FILL,TableLayout.PREFERRED,TableLayout.FILL},{20}};
            jpButtons = new JPanel(new TableLayout(dButtons));
            jbNew = new JButton(Messages.getString("DigitalDJWizard.32"),Util.getIcon(ICON_NEW));
            jbNew.addActionListener(this);
            jbNew.setToolTipText(Messages.getString("DigitalDJWizard.33"));
            jbDelete = new JButton(Messages.getString("DigitalDJWizard.34"),Util.getIcon(ICON_DELETE));
            jbDelete.addActionListener(this);
            jbDelete.setToolTipText(Messages.getString("DigitalDJWizard.35"));
            jpButtons.add(jbNew,"1,0");
            jpButtons.add(jbDelete,"3,0");
            add(getPanel(),"1,1");
            add(jpButtons,"1,3,c,c");
        }
        
        /**
         * 
         * @return a panel containing all items
         */
        private JScrollPane getPanel(){
            widgets = new JComponent[ambiences.size()][3];
            JPanel out = new JPanel();
            //Delete|Style list|proportion in %  
            double[] dHoriz = {25,10,0.25,10,TableLayout.FILL,5};
            double[] dVert = new double[widgets.length+2]; 
            dVert[0] = 20;
            ButtonGroup group = new ButtonGroup();
            //now add all ambiences
            for (int index=0;index<ambiences.size();index++ ){
                //Ambience name
                final JTextField jtfName = new JTextField();
                jtfName.addKeyListener(new KeyListener() {
                    public void keyReleased(KeyEvent arg0) {
                    }
                
                    public void keyPressed(KeyEvent arg0) {
                    }
                
                    public void keyTyped(KeyEvent ke) {
                        //check if each typed letter is either a letter or a digit or ignore it
                        if (!Character.isLetterOrDigit(ke.getKeyChar())){
                            String sCurrent = jtfName.getText();
                            jtfName.setText(sCurrent.substring(0,sCurrent.length()-1)); //it should exist a better way to do that
                        }
                        //if name and style are selected,no more error message
                        else if(((JButton)widgets[getWidgetIndex(jtfName)][2]).getText().length() > 0){
                            controller.setProblem(null);
                        }
                    }
                });
                jtfName.setToolTipText(Messages.getString("DigitalDJWizard.36"));
                widgets[index][1] = jtfName;
                //radio button
                final JRadioButton jrbAmbience = new JRadioButton();
                group.add(jrbAmbience);
                jrbAmbience.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        String sAmbienceName = ((JTextField)widgets[getWidgetIndex(jrbAmbience)][1]).getText();
                        ambienceIndex = getWidgetIndex(jrbAmbience);
                        Ambience ambience = AmbienceManager.getInstance().getAmbience(sAmbienceName);
                        if (!ambience.getName().equals("") && ambience.getStyles().size() > 0){
                            data.put(KEY_AMBIENCE,ambience);
                            controller.setProblem(null);
                        }
                        else{
                            data.put(KEY_AMBIENCE,null);
                            controller.setProblem(Messages.getString("DigitalDJWizard.39"));
                        }
                    }
                });
                widgets[index][0] = jrbAmbience;
                //style list
                JButton jbStyle = new JButton(Util.getIcon(ICON_LIST));
                Ambience ambience = ambiences.get(index);
                if (ambience.getStyles() != null && ambience.getStyles().size() > 0){
                    jbStyle.setText(ambience.getStylesDesc());
                    jbStyle.setToolTipText(ambience.getStylesDesc());
                }
                jbStyle.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        int row = getWidgetIndex((JComponent)ae.getSource()); 
                        addStyle(row);
                    }
                });
                jbStyle.setToolTipText(Messages.getString("DigitalDJWizard.27"));
                widgets[index][2] = jbStyle;
                //Set layout
                dVert[index+1] = 20;
            }
            dVert[widgets.length+1] = 20;
            //Create layout
            double[][] dSizeProperties = new double[][]{dHoriz,dVert};  
            TableLayout layout = new TableLayout(dSizeProperties);
            layout.setHGap(10);
            layout.setVGap(10);
            out.setLayout(layout);
            //Create header
            JLabel jlHeader1 = new JLabel(Messages.getString("DigitalDJWizard.37"));
            jlHeader1.setFont(new Font("Dialog",Font.BOLD,12));
            JLabel jlHeader2 = new JLabel(Messages.getString("DigitalDJWizard.27"));
            jlHeader2.setFont(new Font("Dialog",Font.BOLD,12));
            out.add(jlHeader1,"2,0");
            out.add(jlHeader2,"4,0");
            //Add widgets
            for (int i=0;i<dVert.length-2;i++){
                out.add(widgets[i][0],"0,"+(i+1)+",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
                out.add(widgets[i][1],"2,"+(i+1)); //$NON-NLS-1$
                out.add(widgets[i][2],"4,"+(i+1)); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return new JScrollPane(out);
        }
        
        /**
         * Add a style to a proportion
         * @param row row
         */
        private void addStyle(int row){
            synchronized(StyleManager.getInstance().getLock()){
                Ambience ambience = ambiences.get(row);
                //create list of styles used in current selection
                HashSet disabledStyles = new HashSet();
                disabledStyles.addAll(ambience.getStyles());    
                StylesSelectionDialog dialog = new StylesSelectionDialog(disabledStyles);
                dialog.setSelection(ambience.getStyles());
                dialog.setVisible(true);
                HashSet<Style> styles =  dialog.getSelectedStyles();
                //check if at least one style has been selected
                if (styles.size() == 0){
                    return;
                }
                String sText = "";
                for (Style style:styles){
                    ambience.addStyle(style);
                    sText += style.getName2()+',';  
                }
                sText = sText.substring(0,sText.length()-1);
                //Set button text
                ((JButton)widgets[row][2]).setText(sText);
                //if we have ambience name and some styles, register the ambience
                if (!ambience.getName().equals("") && 
                        ambience.getStyles().size() > 0){
                    //register this new ambience
                    AmbienceManager.getInstance().registerAmbience(ambience);
                    //no more error message if at least one ambience
                    controller.setProblem(null);
                }
            }
        }
        
        /**
         * Refresh panel
         *
         */
        private void refreshScreen(){
            removeAll();
            //refresh panel
            add(getPanel(),"1,1");
            add(jpButtons,"1,3,c,c");
            revalidate();
            repaint();
        }
        
        /**
         * 
         * @param widget
         * @return index of a given widget row in the widget table
         */
        private int getWidgetIndex(JComponent widget){
            int resu = -1;
            for (int row=0;row<widgets.length;row++){ 
                for (int col=0;col<widgets[0].length;col++){
                    if (widget.equals(widgets[row][col])){
                        resu = row;
                        break;
                    }    
                }
                
            }
            return resu; 
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == jbNew){
                ambiences.add(new Ambience("")); //create a void ambience
                controller.setProblem(null);
                //refresh screen
                refreshScreen();
                //select new row
                JRadioButton jrb = (JRadioButton)widgets[ambiences.size() - 1][0];
                jrb.setSelected(true);
                controller.setProblem(Messages.getString("DigitalDJWizard.39"));
            }
            else if (ae.getSource() == jbDelete){
                JTextField jtf = (JTextField)widgets[ambienceIndex][1];
                Ambience ambience = new Ambience(jtf.getText());
                ambiences.remove(ambience);
                AmbienceManager.getInstance().removeAmbience(ambience.getName());
                //We need at least one ambience
                if (AmbienceManager.getInstance().getAmbiences().size() == 0){
                    controller.setProblem(Messages.getString("DigitalDJWizard.38"));    
                }
                //refresh screen
                refreshScreen();
            }
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
