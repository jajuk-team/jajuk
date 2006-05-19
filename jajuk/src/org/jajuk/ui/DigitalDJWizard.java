package org.jajuk.ui;


import info.clearthought.layout.TableLayout;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.dj.Ambience;
import org.jajuk.dj.AmbienceDigitalDJ;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.dj.DigitalDJ;
import org.jajuk.dj.DigitalDJManager;
import org.jajuk.dj.Proportion;
import org.jajuk.dj.ProportionDigitalDJ;
import org.jajuk.dj.Transition;
import org.jajuk.dj.TransitionDigitalDJ;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.wizard.Screen;
import org.jajuk.ui.wizard.Wizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
/**
 * DJ creation wizard
 *
 * @author     Bertrand Florat
 * @created    4 mars 2006
 */
public class DigitalDJWizard extends Wizard implements ITechnicalStrings{

    /**Wizard action*/
    private static final String KEY_ACTION = "ACTION";
    /**DJ type variable name */
    private static final String KEY_DJ_TYPE = "TYPE";
    /**DJ name variable name */
    private static final String KEY_DJ_NAME = "NAME";
    /**Track unicity */
    private static final String KEY_UNICITY = "UNICITY";
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
    /**DJ to remove*/
    private static final String KEY_REMOVE = "REMOVE";
    /**DJ to change*/
    private static final String KEY_CHANGE = "CHANGE";
    /**Startup style*/
    private static final String KEY_STARTUP_STYLE = "STARTUP_STYLE";
    
    /**
     * 
     * DJ type choice 
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class TypeSelectionPanel extends Screen implements ActionListener{
        /**Transition DJ code*/
        private static final String DJ_TYPE_TRANSITION = "0";
        /**Proportions DJ code*/
        private static final String DJ_TYPE_PROPORTION = "1";
        /**Ambience DJ code*/
        private static final String DJ_TYPE_AMBIENCE = "2";
        
     
        ButtonGroup bgTypes;
        JRadioButton jrbTransitions;
        JRadioButton jrbProp;
        JRadioButton jrbAmbiance;
        
      /**
         * Create panel UI
         *
         */
        public void initUI(){
            double[][] size = new double[][]
                                           {{20,TableLayout.PREFERRED,20},
                    {20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20}};
            setLayout(new TableLayout(size));
            bgTypes = new ButtonGroup();
            jrbTransitions = new JRadioButton(Messages.getString("DigitalDJWizard.1"));
            jrbTransitions.addActionListener(this);
            jrbTransitions.doClick(); //default selection
            jrbProp = new JRadioButton(Messages.getString("DigitalDJWizard.2"));
            jrbProp.addActionListener(this);
            jrbAmbiance = new JRadioButton(Messages.getString("DigitalDJWizard.3"));
            jrbAmbiance.addActionListener(this);
            //can select ambience DJ only if at least one ambience defined
            jrbAmbiance.setEnabled(AmbienceManager.getInstance().getAmbiences().size() > 0);
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
                data.put(KEY_DJ_TYPE,DJ_TYPE_TRANSITION);
            }
            else if (e.getSource() == jrbProp){
                data.put(KEY_DJ_TYPE,DJ_TYPE_PROPORTION);
            }
            else if (e.getSource() == jrbAmbiance){
                data.put(KEY_DJ_TYPE,DJ_TYPE_AMBIENCE);
            }
        }
        
        public String getDescription() {
            return Messages.getString("DigitalDJWizard.0");
        }
        
        public String getName() {
            return Messages.getString("DigitalDJWizard.46");
        }
    }
    
    /**
     * 
     * DJ removal 
     *
     * @author     Bertrand Florat
     * @created    26 march 2006
     */
    public static class RemovePanel extends Screen implements ActionListener{
        JComponent[][] widgets;

        ButtonGroup bgDJS;
        ArrayList<DigitalDJ> djs;
        
       /**
         * Create panel UI
         *
         */
        public void initUI(){
            djs = new ArrayList(DigitalDJManager.getInstance().getDJs()); 
            Collections.sort(djs);
            widgets = new JComponent[djs.size()][1];
            double[] dVert = new double[djs.size()];
            //prepare vertical layout
            for (int i=0;i<djs.size();i++){
                dVert[i] = 20;
            }
            double[][] size = new double[][]
                                           {{0.99},dVert};
            TableLayout layout = new TableLayout(size);
            layout.setVGap(10);
            layout.setHGap(10);
            JPanel jpDjs = new JPanel(new TableLayout(size));
            bgDJS = new ButtonGroup();
            setCanFinish(true);
            int index = 0;
            for (DigitalDJ dj : djs){
                JRadioButton jrb = new JRadioButton(dj.getName()); 
                jrb.addActionListener(this);
                bgDJS.add(jrb);
                widgets[index][0] = jrb;
                jpDjs.add(jrb,"0,"+index);
                index ++;
            }
            //main panel
            double[][] main = new double[][]
                                           {{0.99},{20,TableLayout.PREFERRED}};
            setLayout(new TableLayout(main));
            add(jpDjs,"0,1");
            setProblem(Messages.getString("DigitalDJWizard.40"));
            //select first ambience found
            JRadioButton jrb = (JRadioButton)widgets[0][0];
            jrb.doClick();
          }
  
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            int row = getWidgetIndex(widgets,(JComponent)e.getSource());
            data.put(KEY_REMOVE,djs.get(row));
            setProblem(null);
        }
        
        public String getDescription() {
            return Messages.getString("DigitalDJWizard.40");
        }
        
        public String getName() {
            return Messages.getString("DigitalDJWizard.51");
        }
      }
    
     /**
     * 
     * DJ Selection for change 
     *
     * @author     Bertrand Florat
     * @created    26 march 2006
     */
    public static class ChangePanel extends Screen implements ActionListener{
        JComponent[][] widgets;

        ButtonGroup bgDJS;
        ArrayList<DigitalDJ> djs;
        
       /**
         * Create panel UI
         *
         */
        public void initUI(){
            djs = new ArrayList(DigitalDJManager.getInstance().getDJs()); 
            Collections.sort(djs);
            widgets = new JComponent[djs.size()][1];
            double[] dVert = new double[djs.size()];
            //prepare vertical layout
            for (int i=0;i<djs.size();i++){
                dVert[i] = 30;
            }
            double[][] size = new double[][]
                                           {{0.99},dVert};
            TableLayout layout = new TableLayout(size);
            layout.setVGap(10);
            layout.setHGap(10);
            JPanel jpDjs = new JPanel(new TableLayout(size));
            bgDJS = new ButtonGroup();
            int index = 0;
            for (DigitalDJ dj : djs){
                JRadioButton jrb = new JRadioButton(dj.getName()); 
                jrb.addActionListener(this);
                bgDJS.add(jrb);
                widgets[index][0] = jrb;
                jpDjs.add(jrb,"0,"+index);
                index ++;
            }
            //main panel
            double[][] main = new double[][]
                                           {{0.99},{20,TableLayout.PREFERRED}};
            setLayout(new TableLayout(main));
            add(jpDjs,"0,1");
            //If more than one DJ, select first
            if (djs.size() > 0){
                JRadioButton jrb = (JRadioButton)widgets[0][0];
                jrb.doClick();
            }
            else{
                setProblem(Messages.getString("DigitalDJWizard.40"));
            }
          }
  
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            int row = getWidgetIndex(widgets,(JComponent)e.getSource());
            //set DJ type useful for screen choice 
            DigitalDJ dj = djs.get(row);
            data.put(KEY_CHANGE,dj);
            if (dj instanceof AmbienceDigitalDJ){
                data.put(KEY_DJ_TYPE,TypeSelectionPanel.DJ_TYPE_AMBIENCE);
            }
            if (dj instanceof ProportionDigitalDJ){
                data.put(KEY_DJ_TYPE,TypeSelectionPanel.DJ_TYPE_PROPORTION);
            }
            if (dj instanceof TransitionDigitalDJ){
                data.put(KEY_DJ_TYPE,TypeSelectionPanel.DJ_TYPE_TRANSITION);
            }
            setProblem(null);
        }
        
        public String getDescription() {
            return Messages.getString("DigitalDJWizard.44");
        }
        
        public String getName() {
            return Messages.getString("DigitalDJWizard.43");
        }
      }
    
    /**
     * 
     * Action type (new or alter)
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class ActionSelectionPanel extends Screen implements ActionListener{
        /**NEW code*/
        public static final String ACTION_CREATION = "0";
        /**CHANGE code*/
        public static final String ACTION_CHANGE = "1";
        /**DELETE code*/
        public static final String ACTION_DELETE = "2";
        
        ButtonGroup bgActions;
        JRadioButton jrbNew;
        JRadioButton jrbChange;
        JRadioButton jrbDelete;
       
        /**
         * Create panel UI
         *
         */
        public void initUI(){
            double[][] size = new double[][]
                                           {{20,TableLayout.FILL,10},
                    {20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20}};
            setLayout(new TableLayout(size));
            bgActions = new ButtonGroup();
            jrbNew = new JRadioButton(Messages.getString("DigitalDJWizard.17"));
            jrbNew.addActionListener(this);
            jrbNew.doClick();
            jrbChange = new JRadioButton(Messages.getString("DigitalDJWizard.18"));
            jrbChange.addActionListener(this);
            jrbDelete = new JRadioButton(Messages.getString("DigitalDJWizard.19"));
            jrbDelete.addActionListener(this);
            //disabled change and remove if none dj
            if (DigitalDJManager.getInstance().getDJs().size() == 0){
                jrbChange.setEnabled(false);
                jrbDelete.setEnabled(false);
            }
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
                data.put(KEY_ACTION,ACTION_CREATION);
            }
            else if (e.getSource() == jrbChange){
                data.put(KEY_ACTION,ACTION_CHANGE);
            }
            else if (e.getSource() == jrbDelete){
                data.put(KEY_ACTION,ACTION_DELETE);
            }
        }
        
        public String getDescription() {
            return Messages.getString("DigitalDJWizard.16");
        }
        
        public String getName() {
            return Messages.getString("DigitalDJWizard.45");
        }
    }
    
        
       
  
     /**
     * 
     * General options panel 
     *
     * @author     Bertrand Florat
     * @created    4 march 2006
     */
    public static class GeneralOptionsPanel extends Screen 
        implements ActionListener,CaretListener,ChangeListener,MouseWheelListener{
        
        JLabel jlName;
        JTextField jtfName;
        JLabel jlRatingLevel;
        JSlider jsRatingLevel;
        JLabel jlFadeDuration;
        JSlider jsFadeDuration;
        JCheckBox jcbUnicity;
        
         public String getDescription() {
            return Messages.getString("DigitalDJWizard.49");
        }
         
         public String getName() {
            return Messages.getString("DigitalDJWizard.48");
        }

        
        /**
         * Create panel UI
         */
        public void initUI(){
            if (ActionSelectionPanel.ACTION_CREATION.equals(data.get(KEY_ACTION))){
                //default values
                data.put(KEY_FADE_DURATION,10);
                data.put(KEY_RATINGS_LEVEL,0); //all tracks by default
                data.put(KEY_UNICITY,false);
            }
            else if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))){ //keep existing DJ values
                DigitalDJ dj = (DigitalDJ)data.get(KEY_CHANGE);
                data.put(KEY_FADE_DURATION,dj.getFadingDuration());
                data.put(KEY_RATINGS_LEVEL,dj.getRatingLevel());
                data.put(KEY_UNICITY,dj.isTrackUnicity());
            }
            jlName = new JLabel(Messages.getString("DigitalDJWizard.6"));
            jtfName = new JTextField();
            jtfName.setToolTipText(Messages.getString("DigitalDJWizard.6"));
            jtfName.addCaretListener(this);
            jtfName.requestFocusInWindow();
            jlRatingLevel = new JLabel(Messages.getString("DigitalDJWizard.8"));
            jlRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
            jsRatingLevel = new JSlider(0,4,(Integer)data.get(KEY_RATINGS_LEVEL));
            jsRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.53"));
            jsRatingLevel.setMajorTickSpacing(1);
            jsRatingLevel.setMinorTickSpacing(1);
            jsRatingLevel.setPaintTicks(true);
            jsRatingLevel.setPaintLabels(true);
            jsRatingLevel.setToolTipText(Messages.getString("DigitalDJWizard.8"));
            jsRatingLevel.addMouseWheelListener(this);
            jsRatingLevel.addChangeListener(this);
            jlFadeDuration = new JLabel(Messages.getString("DigitalDJWizard.9"));
            jlFadeDuration.setToolTipText(Messages.getString("DigitalDJWizard.54"));
            jsFadeDuration = new JSlider(0,30,(Integer)data.get(KEY_FADE_DURATION));
            jsFadeDuration.setToolTipText(Messages.getString("DigitalDJWizard.54"));
            jsFadeDuration.addMouseWheelListener(this);
            jsFadeDuration.addChangeListener(this);
            jsFadeDuration.setMajorTickSpacing(10);
            jsFadeDuration.setMinorTickSpacing(1);
            jsFadeDuration.setPaintTicks(true);
            jsFadeDuration.setPaintLabels(true);
            jsFadeDuration.setToolTipText(Messages.getString("DigitalDJWizard.9"));
            jcbUnicity = new JCheckBox(Messages.getString("DigitalDJWizard.10"),(Boolean)data.get(KEY_UNICITY));
            jcbUnicity.setToolTipText(Messages.getString("DigitalDJWizard.55"));
            jcbUnicity.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    data.put(KEY_UNICITY,jcbUnicity.isSelected());
                }
            });
            //DJ change, set default values
            if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))){
                DigitalDJ dj = (DigitalDJ)data.get(KEY_CHANGE);
                jtfName.setText(dj.getName());
                jsFadeDuration.setValue((Integer)data.get(KEY_FADE_DURATION));
                jsRatingLevel.setValue((Integer)data.get(KEY_RATINGS_LEVEL));
                jcbUnicity.setSelected((Boolean)data.get(KEY_UNICITY));
            }
            else{ //new dj, dj name is required
                setProblem(Messages.getString("DigitalDJWizard.41"));
            }
            double[][] size = new double[][]
                    {{10,0.5,20,TableLayout.FILL,10},
                    {20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,
                        TableLayout.PREFERRED,20,TableLayout.PREFERRED,20,TableLayout.PREFERRED,20}};
            setLayout(new TableLayout(size));
            add(jlName,"1,1");
            add(jtfName,"3,1");
            add(jlRatingLevel,"1,3");
            add(jsRatingLevel,"3,3");
            add(jlFadeDuration,"1,5");
            add(jsFadeDuration,"3,5");
            add(jcbUnicity,"1,7");
        }

        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == jcbUnicity){
                data.put(KEY_UNICITY,jcbUnicity.isSelected());
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
         */
        public void caretUpdate(CaretEvent ce) {
            if (ce.getSource() == jtfName){
                data.put(KEY_DJ_NAME,jtfName.getText());
                String sName = jtfName.getText();
                //string length = 0
                if (sName.length() == 0){
                    setProblem(Messages.getString("DigitalDJWizard.41"));
                }
                //display an error message if the dj already exists and not in "change" mode
                else if (DigitalDJManager.getInstance().getDJNames().contains(sName)){
                    //if we are in change mode and the name is still the same, no error
                    if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))
                            &&  ((DigitalDJ)data.get(KEY_CHANGE)).getName().equals(sName) ){
                        setProblem(null);
                        return;
                    }
                    setProblem(Messages.getString("DigitalDJWizard.42"));
                }
                else{
                    setProblem(null); //no more problem
                }
            }
        }

       
        /* (non-Javadoc)
         * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
         */
        public void mouseWheelMoved(MouseWheelEvent mwe) {
            if (mwe.getSource().equals(jsFadeDuration)){
                int iOld = jsFadeDuration.getValue();
                int iNew = iOld - mwe.getUnitsToScroll();
                jsFadeDuration.setValue(iNew);
            }
            else if (mwe.getSource().equals(jsRatingLevel) && jsRatingLevel.isEnabled()){
                int iOld = jsRatingLevel.getValue();
                int iNew = iOld - mwe.getUnitsToScroll()/3;
                jsRatingLevel.setValue(iNew);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent ie) {
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
    public static class TransitionsPanel extends Screen{
            
        JLabel jlStartWith;
        JComboBox jcbStartwith;
        JPanel jpStartwith;
        
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        /**Transitions**/
        ArrayList<Transition> alTransitions;
        
          public String getDescription() {
            return Messages.getString("DigitalDJWizard.52");
        }
         public String getName() {
            return Messages.getString("DigitalDJWizard.20");
        }

         
         /**
          * 
          * @return Filled transitions only
          */
         private ArrayList<Transition> getCleanedTransitions(){
             ArrayList<Transition> out = new ArrayList(alTransitions.size());
             for (Transition transition:alTransitions){
                 if (transition.getFrom() != null && transition.getTo() != null && 
                         transition.getFrom().getStyles().size() > 0 && transition.getTo().getStyles().size() > 0){
                     out.add(transition);
                 }
             }
             return out;
         }
         
         
        /**
         * Create panel UI
         *
         */
        public void initUI(){
            if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))){
                DigitalDJ dj = (DigitalDJ)data.get(KEY_CHANGE);
                alTransitions = ((TransitionDigitalDJ)dj).getTransitions();
                data.put(KEY_TRANSITIONS,getCleanedTransitions());
                alTransitions.add(new Transition(DEFAULT_TRANSITION_TRACK_NUMBER)); //add a void transition
            }
            else{
                alTransitions = new ArrayList(10);
                alTransitions.add(new Transition(DEFAULT_TRANSITION_TRACK_NUMBER)); //add a void transition
                setProblem(Messages.getString("DigitalDJWizard.26"));
            }
            setCanFinish(true);
            jlStartWith = new JLabel(Messages.getString("DigitalDJWizard.25"));
            final Vector<String> styles = StyleManager.getInstance().getStylesList();
            jcbStartwith = new JComboBox(styles);
            //set first style by default
            data.put(KEY_STARTUP_STYLE,StyleManager.getInstance().
                        getStyleByName(styles.get(0)));
            AutoCompleteDecorator.decorate(jcbStartwith);
            jcbStartwith.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    data.put(KEY_STARTUP_STYLE,StyleManager.getInstance().
                        getStyleByName(styles.get(jcbStartwith.getSelectedIndex())));
                }
            });
            jpStartwith = new JPanel();
            double[][] dSize = {{0.25,10,0.25},{20}};
            jpStartwith.setLayout(new TableLayout(dSize));
            jpStartwith.add(jlStartWith,"0,0");
            jpStartwith.add(jcbStartwith,"2,0");
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
                        alTransitions.remove(getWidgetIndex(widgets,(JComponent)ae.getSource()));
                        refreshScreen();
                        data.put(KEY_TRANSITIONS,getCleanedTransitions()); 
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
                if (transition.getFrom().getStyles().size() > 0){
                    jbFrom.setText(transition.getFromString());
                    jbFrom.setToolTipText(transition.getFromString());
                }
                jbFrom.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        int row = getWidgetIndex(widgets,(JComponent)ae.getSource()); 
                        addStyle(row,true);
                    }
                });
                jbFrom.setToolTipText(Messages.getString("DigitalDJWizard.22"));
                widgets[index][1] = jbFrom;
                //To style list
                JButton jbTo = new JButton(Util.getIcon(ICON_LIST));
                if (transition.getTo().getStyles().size() > 0){
                    jbTo.setText(transition.getToString());
                    jbTo.setToolTipText(transition.getToString());
                }
                jbTo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        int row = getWidgetIndex(widgets,(JComponent)ae.getSource()); 
                        addStyle(row,false);
                    }
                });
                jbTo.setToolTipText(Messages.getString("DigitalDJWizard.23"));
                widgets[index][2] = jbTo;
                //Nb of tracks
                JSpinner jsNb = new JSpinner(new SpinnerNumberModel(transition.getNbTracks(),1,10,1));
                jsNb.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent ce) {
                        int row = getWidgetIndex(widgets,(JComponent)ce.getSource());
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
                        disabledStyles.addAll(t.getTo().getStyles());
                    }
                    if (!bFrom || i != row){
                        disabledStyles.addAll(t.getFrom().getStyles());
                    }
                }
                StylesSelectionDialog dialog = new StylesSelectionDialog(disabledStyles);
                if (bFrom){
                    dialog.setSelection(transition.getFrom().getStyles());
                }
                else{
                    dialog.setSelection(transition.getTo().getStyles());
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
                    transition.setFrom(new Ambience("",styles));
                }
                else{
                    transition.setTo(new Ambience("",styles));
                }
                //check if the transaction is fully selected now
                if (transition.getFrom().getStyles().size() > 0 
                        && transition.getTo().getStyles().size() > 0){
                    //Make sure current delete button is now enabled
                    ((JButton)widgets[row][0]).setEnabled(true);
                    
                    //Reset wizard error message
                    setProblem(null);
                    
                    //Fill wizard data
                    data.put(KEY_TRANSITIONS,getCleanedTransitions()); 
                    
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
        
     
    }
        
                  
         
     /**
     * 
     * Proportion panel
     *
     * @author     Bertrand Florat
     * @created    17 march 2006
     */
    public static class ProportionsPanel extends Screen{
          
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        /**Proportions**/
        ArrayList<Proportion> proportions;
        
        public String getDescription() {
            return Messages.getString("DigitalDJWizard.50");
        }
        
        public String getName() {
            return Messages.getString("DigitalDJWizard.29");
        }
        
        /**
         * Create panel UI
         */
        public void initUI(){
            if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))){
                DigitalDJ dj = (DigitalDJ)data.get(KEY_CHANGE);
                proportions = ((ProportionDigitalDJ)dj).getProportions();
                data.put(KEY_PROPORTIONS,proportions.clone());
                proportions.add(new Proportion()); //add a void item
            }
            else{
                proportions = new ArrayList(10);
                proportions.add(new Proportion()); //add a void item
                setProblem(Messages.getString("DigitalDJWizard.30"));
            }
            setCanFinish(true);
            
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
                        proportions.remove(getWidgetIndex(widgets,(JComponent)ae.getSource()));
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
                        int row = getWidgetIndex(widgets,(JComponent)ae.getSource()); 
                        addStyle(row);
                    }
                });
                jbStyle.setToolTipText(Messages.getString("DigitalDJWizard.27"));
                widgets[index][1] = jbStyle;
                //Proportion
                JSpinner jsNb = new JSpinner(new SpinnerNumberModel((int)(proportion.getProportion()*100),1,100,1));
                jsNb.addChangeListener(new ChangeListener() {
                    public void stateChanged(ChangeEvent ce) {
                        int row = getWidgetIndex(widgets,(JComponent)ce.getSource());
                        int nb = Integer.parseInt(((JSpinner)ce.getSource()).getValue().toString());
                        Proportion proportion = proportions.get(row);
                        proportion.setProportion(((float)nb)/100);
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
                proportion.setStyle(new Ambience());
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
                    setProblem(null);
                    
                    //Fill wizard data
                    data.put(KEY_PROPORTIONS,proportions.clone());//set a cloned proportion list to avoid add void items (see next line)
                    
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
    }
    
     /**
     * 
     * Ambience based
     *
     * @author     Bertrand Florat
     * @created    18 march 2006
     */
    public static class AmbiencePanel extends Screen implements ActionListener{
        
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        /**Ambiences**/
        ArrayList<Ambience> ambiences;
        
        /**DJ**/
        AmbienceDigitalDJ dj = null;
        
        /**Selected ambience index*/
        int ambienceIndex = 0;
        
        public String getDescription() {
            return Messages.getString("DigitalDJWizard.47");
        }
         
         public String getName() {
            return Messages.getString("DigitalDJWizard.31");
        }
         
        /**
         * Create panel UI
         *
         */
        public void initUI(){
            ambiences = new ArrayList(AmbienceManager.getInstance().getAmbiences());
            widgets = new JComponent[ambiences.size()][1];
            //We need at least one ambience
            if (AmbienceManager.getInstance().getAmbiences().size() == 0){
                setProblem(Messages.getString("DigitalDJWizard.38"));    
            }
            setCanFinish(true);
            //Get DJ
            dj = (AmbienceDigitalDJ)DigitalDJManager.getInstance().getDJByName((String)data.get(KEY_DJ_NAME));
            //set layout
            double[] dVert = new double[ambiences.size()];
            //prepare vertical layout
            for (int i=0;i<ambiences.size();i++){
                dVert[i] = 20;
            }
            double[][] size = new double[][]
                                           {{0.99},dVert};
            ButtonGroup bg = new ButtonGroup();
            JPanel jpAmbiences = new JPanel(new TableLayout(size));
            TableLayout layout = new TableLayout(size);
            layout.setVGap(10);
            layout.setHGap(10);
            jpAmbiences.setLayout(layout);
            int index = 0;
            for (Ambience ambience : ambiences){
                JRadioButton jrb = new JRadioButton(ambience.getName()); 
                jrb.addActionListener(this);
                bg.add(jrb);
                widgets[index][0] = jrb;
                jpAmbiences.add(jrb,"0,"+index);
                index ++;
            }
            //main panel
            double[][] main = new double[][]
                                           {{0.99},{20,TableLayout.PREFERRED}};
            setLayout(new TableLayout(main));
            add(jpAmbiences,"0,1");
            //DJ change, set right ambience
            if (ActionSelectionPanel.ACTION_CHANGE.equals(data.get(KEY_ACTION))){
                DigitalDJ dj = (DigitalDJ)data.get(KEY_CHANGE);
                Ambience ambience = ((AmbienceDigitalDJ)dj).getAmbience();
                index = 0;
                for (Ambience a:ambiences){
                    if (a.equals(ambience)){
                        JRadioButton jrb = (JRadioButton)widgets[index][0];
                        jrb.doClick();//select right ambience, it will set right value into data
                        break;
                    }
                    index ++;
                }
            }else{
                //select first ambience found
                JRadioButton jrb = (JRadioButton)widgets[0][0];
                jrb.doClick();
            }
        }
        
         /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            int row = getWidgetIndex(widgets,(JComponent)e.getSource());
            data.put(KEY_AMBIENCE,ambiences.get(row));
            setProblem(null);
        }
        
       }
    
    /**
     * 
     * @param widget
     * @return index of a given widget row in the widget table
     */
    private static int getWidgetIndex(JComponent[][] widgets,JComponent widget){
        for (int row=0;row<widgets.length;row++){ 
            for (int col=0;col<widgets[0].length;col++){
                if (widget.equals(widgets[row][col])){
                    return row;
                }    
            }
        }
        return -1; 
    }
    
   
    /* (non-Javadoc)
     * @see org.jajuk.ui.wizard.Wizard#getPreviousScreen(java.lang.Class)
     */
    @Override
    public Class getPreviousScreen(Class screen) {
        if (ActionSelectionPanel.class.equals(getCurrentScreen())){
            return null;
        }
        else if (TypeSelectionPanel.class.equals(getCurrentScreen())){
            return ActionSelectionPanel.class;
        }
        else if (GeneralOptionsPanel.class.equals(getCurrentScreen())){
            if (ActionSelectionPanel.ACTION_CREATION.equals(data.get(KEY_ACTION))){
                return TypeSelectionPanel.class;
            }
            else{
                return ChangePanel.class;
            }
        }
        else if (TransitionsPanel.class.equals(getCurrentScreen())
                || ProportionsPanel.class.equals(getCurrentScreen())
                || AmbiencePanel.class.equals(getCurrentScreen())){
            return GeneralOptionsPanel.class;
        }
        else if (RemovePanel.class.equals(getCurrentScreen())){
            return ActionSelectionPanel.class;
        }
        else if (ChangePanel.class.equals(getCurrentScreen())){
            return ActionSelectionPanel.class;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.jajuk.ui.wizard.Wizard#getNextScreen(java.lang.Class)
     */
    @Override
    public Class getNextScreen(Class screen) {
        if (ActionSelectionPanel.class.equals(getCurrentScreen())){
            String sAction = (String)data.get(KEY_ACTION);
            if (ActionSelectionPanel.ACTION_CREATION.equals(sAction)){
                return TypeSelectionPanel.class;
            }
            else if (ActionSelectionPanel.ACTION_CHANGE.equals(sAction)){
                return ChangePanel.class;
            }
            else if (ActionSelectionPanel.ACTION_DELETE.equals(sAction)){
                return RemovePanel.class;
            }
        }
        else if (TypeSelectionPanel.class.equals(getCurrentScreen())){
            return GeneralOptionsPanel.class;
        }
        else if (GeneralOptionsPanel.class.equals(getCurrentScreen())){
            String sType = (String)data.get(KEY_DJ_TYPE);
            if (TypeSelectionPanel.DJ_TYPE_AMBIENCE.equals(sType)){
                return AmbiencePanel.class;
            }
            else if (TypeSelectionPanel.DJ_TYPE_PROPORTION.equals(sType)){
                return ProportionsPanel.class;
            }
            else if (TypeSelectionPanel.DJ_TYPE_TRANSITION.equals(sType)){
                return TransitionsPanel.class;
            }
        }
        else if (RemovePanel.class.equals(getCurrentScreen())){
            return null;
        }
        else if (ChangePanel.class.equals(getCurrentScreen())){
            return GeneralOptionsPanel.class;
        }
        return null;
    }
    
    public DigitalDJWizard() {
        super(Messages.getString("DigitalDJWizard.4"),ActionSelectionPanel.class,
            Util.getIcon(IMAGE_DJ),Main.getWindow());
    }


    /* (non-Javadoc)
     * @see org.jajuk.ui.wizard.Wizard#finish()
     */
    @Override
    public void finish() {
        DigitalDJ dj = null;
        String sAction = (String)data.get(KEY_ACTION);
        if (ActionSelectionPanel.ACTION_DELETE.equals(sAction)){
            DigitalDJManager.getInstance().remove((DigitalDJ)data.get(KEY_REMOVE));
        }
        else if (ActionSelectionPanel.ACTION_CREATION.equals(sAction)){
            String sType = (String)data.get(KEY_DJ_TYPE);
            dj = null;
            String sName = (String)data.get(KEY_DJ_NAME);
            //create a unique ID for this DJ, simply use current time in ms
            String sID = Long.toString(System.currentTimeMillis());
            if (TypeSelectionPanel.DJ_TYPE_AMBIENCE.equals(sType)){
                Ambience ambience = (Ambience)data.get(KEY_AMBIENCE);
                dj = new AmbienceDigitalDJ(sID);
                ((AmbienceDigitalDJ)dj).setAmbience(ambience);
            }
            else if (TypeSelectionPanel.DJ_TYPE_PROPORTION.equals(sType)){
                dj = new ProportionDigitalDJ(sID);
                ArrayList proportions = (ArrayList)data.get(KEY_PROPORTIONS);
                ((ProportionDigitalDJ)dj).setProportions(proportions);
            }
            else if (TypeSelectionPanel.DJ_TYPE_TRANSITION.equals(sType)){
                ArrayList transitions = (ArrayList)data.get(KEY_TRANSITIONS);
                dj = new TransitionDigitalDJ(sID);
                ((TransitionDigitalDJ)dj).setTransitions(transitions);
                Style startup = (Style)data.get(KEY_STARTUP_STYLE);
                ((TransitionDigitalDJ)dj).setStartupStyle(startup);
            }
            int iFadeDuration = (Integer)data.get(KEY_FADE_DURATION);
            int iRateLevel = (Integer)data.get(KEY_RATINGS_LEVEL);
            boolean bUnicity = (Boolean)data.get(KEY_UNICITY);
            dj.setName(sName);
            dj.setFadingDuration(iFadeDuration);
            dj.setRatingLevel(iRateLevel);
            dj.setTrackUnicity(bUnicity);
            DigitalDJManager.getInstance().register(dj);
            //commit the DJ right now
            DigitalDJManager.commit(dj);
            //If first DJ, select it as default
            if (DigitalDJManager.getInstance().getDJs().size() == 1){
                ConfigurationManager.setProperty(CONF_DEFAULT_DJ,dj.getID());
            }
        }
        else if (ActionSelectionPanel.ACTION_CHANGE.equals(sAction)){
            String sType = (String)data.get(KEY_DJ_TYPE);
            dj = (DigitalDJ)data.get(KEY_CHANGE);
            if (TypeSelectionPanel.DJ_TYPE_AMBIENCE.equals(sType)){
                Ambience ambience = (Ambience)data.get(KEY_AMBIENCE);
                ((AmbienceDigitalDJ)dj).setAmbience(ambience);
            }
            else if (TypeSelectionPanel.DJ_TYPE_PROPORTION.equals(sType)){
                ArrayList proportions = (ArrayList)data.get(KEY_PROPORTIONS);
                ((ProportionDigitalDJ)dj).setProportions(proportions);
            }
            else if (TypeSelectionPanel.DJ_TYPE_TRANSITION.equals(sType)){
                ArrayList transitions = (ArrayList)data.get(KEY_TRANSITIONS);
                ((TransitionDigitalDJ)dj).setTransitions(transitions);
                Style startup = (Style)data.get(KEY_STARTUP_STYLE);
                ((TransitionDigitalDJ)dj).setStartupStyle(startup);
            }
            String sName = (String)data.get(KEY_DJ_NAME);
            int iFadeDuration = (Integer)data.get(KEY_FADE_DURATION);
            int iRateLevel = (Integer)data.get(KEY_RATINGS_LEVEL);
            boolean bUnicity = (Boolean)data.get(KEY_UNICITY);
            dj.setName(sName);
            dj.setFadingDuration(iFadeDuration);
            dj.setRatingLevel(iRateLevel);
            dj.setTrackUnicity(bUnicity);
            //commit the DJ right now
            DigitalDJManager.commit(dj);
        }
        //Refresh command panel (usefull for ie if DJ names changed)
        ObservationManager.notify(new Event(EVENT_DJ_CHANGE));
    }

}
