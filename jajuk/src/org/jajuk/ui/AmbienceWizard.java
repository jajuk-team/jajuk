package org.jajuk.ui;


import info.clearthought.layout.TableLayout;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.jajuk.Main;
import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.dj.Ambience;
import org.jajuk.dj.AmbienceDigitalDJ;
import org.jajuk.dj.AmbienceManager;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.wizard.Screen;
import org.jajuk.ui.wizard.Wizard;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
/**
 * Ambiences management wizard 
 *
 * @author     Bertrand Florat
 * @created    17/05/2006
 */
public class AmbienceWizard extends Wizard implements ITechnicalStrings{

     /**Ambiences**/
     static ArrayList<Ambience> ambiences;
    
     /**
     * 
     * Ambience screen
     *
     * @author     Bertrand Florat
     * @created    18 march 2006
     */
    public static class AmbiencePanel extends Screen implements ActionListener{
        
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        JButton jbNew;
        JButton jbDelete;
        JButton jbDefaults;
        
        JPanel jpButtons;
        
        /**DJ**/
        AmbienceDigitalDJ dj = null;
        
        /**Selected ambience index*/
        int ambienceIndex = 0;
      
        
         public String getDescription() {
            return Messages.getString("DigitalDJWizard.47"); //$NON-NLS-1$
        }
         
         public String getName() {
            return Messages.getString("DigitalDJWizard.57"); //$NON-NLS-1$
        }
         
        /**
         * Create panel UI
         *
         */
        public void initUI(){
            ambiences = new ArrayList(AmbienceManager.getInstance().getAmbiences());
            Collections.sort(ambiences);
            setCanFinish(true);
            //set layout
            double[][] dSizeGeneral = {{10,0.99,5},
                    {10,TableLayout.FILL,10,TableLayout.PREFERRED,10}};
            setLayout(new TableLayout(dSizeGeneral));
            //button layout
            double[][] dButtons = {{10,0.33,5,0.33,5,0.33,10},{20}};
            jpButtons = new JPanel(new TableLayout(dButtons));
            jbNew = new JButton(Messages.getString("DigitalDJWizard.32"),Util.getIcon(ICON_NEW)); //$NON-NLS-1$
            jbNew.addActionListener(this);
            jbNew.setToolTipText(Messages.getString("DigitalDJWizard.33")); //$NON-NLS-1$
            jbDelete = new JButton(Messages.getString("DigitalDJWizard.34"),Util.getIcon(ICON_DELETE)); //$NON-NLS-1$
            jbDelete.addActionListener(this);
            jbDelete.setToolTipText(Messages.getString("DigitalDJWizard.35")); //$NON-NLS-1$
            jbDefaults = new JButton(Messages.getString("DigitalDJWizard.62"),Util.getIcon(ICON_DEFAULTS)); //$NON-NLS-1$
            jbDefaults.addActionListener(this);
            jbDefaults.setToolTipText(Messages.getString("DigitalDJWizard.63")); //$NON-NLS-1$
            jpButtons.add(jbNew,"1,0"); //$NON-NLS-1$
            jpButtons.add(jbDelete,"3,0"); //$NON-NLS-1$
            jpButtons.add(jbDefaults,"5,0"); //$NON-NLS-1$
            add(getPanel(),"1,1"); //$NON-NLS-1$
            add(jpButtons,"1,3,c,c"); //$NON-NLS-1$
        }
        
        /**
         * 
         * @return a panel containing all items
         */
        private JScrollPane getPanel(){
            widgets = new JComponent[ambiences.size()][3];
            JPanel out = new JPanel();
            //Delete|Style name|styles list  
            double[] dHoriz = {25,120,200};
            double[] dVert = new double[widgets.length+2]; 
            dVert[0] = 20;
            ButtonGroup group = new ButtonGroup();
            //now add all ambiences
            for (int index=0;index<ambiences.size();index++ ){
                //Ambience name
                final JTextField jtfName = new JTextField();
                jtfName.setText(ambiences.get(index).getName());
                jtfName.addCaretListener(new CaretListener() {
                    public void caretUpdate(CaretEvent arg0) {
                        int index = getWidgetIndex(widgets,(JComponent)arg0.getSource());
                        String s = jtfName.getText();
                        //Check this name is not already token
                        for (int i=0;i<widgets.length;i++){
                            if (i == index){
                                continue;
                            }
                            JTextField jtf = (JTextField)widgets[i][1];
                            if (jtf.getText().equals(s)){
                                setProblem(Messages.getString("DigitalDJWizard.60")); //$NON-NLS-1$
                                return;
                            }
                        }
                        //reset previous problems
                        if (s.length() == 0 || ((JButton)widgets[index][2]).getText().length() == 0){
                            setProblem(Messages.getString("DigitalDJWizard.39")); //$NON-NLS-1$
                        }
                        else{
                            setProblem(null);
                        }
                        JButton jb = (JButton)widgets[index][2];
                        Ambience ambience = ambiences.get(index);
                        ambience.setName(s);
                        jb.setEnabled(s.length() > 0);
                    }
                });
                jtfName.setToolTipText(Messages.getString("DigitalDJWizard.36")); //$NON-NLS-1$
                widgets[index][1] = jtfName;
                //radio button
                final JRadioButton jrbAmbience = new JRadioButton();
                group.add(jrbAmbience);
                jrbAmbience.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        String sAmbienceName = ((JTextField)widgets[getWidgetIndex(widgets,jrbAmbience)][1]).getText();
                        ambienceIndex = getWidgetIndex(widgets,jrbAmbience);
                    }
                });
                widgets[index][0] = jrbAmbience;
                if (index == ambienceIndex){
                    jrbAmbience.setSelected(true);
                }
                Ambience ambience = ambiences.get(index);
                //style list
                JButton jbStyle = new JButton(Util.getIcon(ICON_LIST));
                if (ambience.getName().length() == 0){
                    jbStyle.setEnabled(false);
                }
                if (ambience.getStyles() != null && ambience.getStyles().size() > 0){
                    jbStyle.setText(ambience.getStylesDesc());
                    jbStyle.setToolTipText(ambience.getStylesDesc());
                }
                jbStyle.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        int row = getWidgetIndex(widgets,(JComponent)ae.getSource()); 
                        addStyle(row);
                        //refresh ambience (force an action event) 
                        JRadioButton jrb = (JRadioButton)widgets[row][0];
                        jrb.doClick();
                    }
                });
                jbStyle.setToolTipText(Messages.getString("DigitalDJWizard.27")); //$NON-NLS-1$
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
            JLabel jlHeader1 = new JLabel(Messages.getString("DigitalDJWizard.37")); //$NON-NLS-1$
            jlHeader1.setFont(new Font("Dialog",Font.BOLD,12)); //$NON-NLS-1$
            JLabel jlHeader2 = new JLabel(Messages.getString("DigitalDJWizard.27")); //$NON-NLS-1$
            jlHeader2.setFont(new Font("Dialog",Font.BOLD,12)); //$NON-NLS-1$
            out.add(jlHeader1,"1,0,c,c"); //$NON-NLS-1$
            out.add(jlHeader2,"2,0,c,c"); //$NON-NLS-1$
            //Add widgets
            for (int i=0;i<dVert.length-2;i++){
                out.add(widgets[i][0],"0,"+(i+1)+",c,c"); //$NON-NLS-1$ //$NON-NLS-2$
                out.add(widgets[i][1],"1,"+(i+1)); //$NON-NLS-1$
                out.add(widgets[i][2],"2,"+(i+1)); //$NON-NLS-1$ //$NON-NLS-2$
            }
            JScrollPane jsp = new JScrollPane(out);
            //select first ambiance found
            if (ambiences.size() > 0){ 
                JRadioButton jrb = (JRadioButton)widgets[0][0];
                jrb.doClick(); 
            }
            return jsp;
        }
        
        /**
         * Add a style to a proportion
         * @param row row
         */
        private void addStyle(int row){
            synchronized(StyleManager.getInstance().getLock()){
                Ambience ambience = ambiences.get(row);
                //create list of styles used in current selection
                StylesSelectionDialog dialog = new StylesSelectionDialog(null);
                dialog.setSelection(ambience.getStyles());
                dialog.setVisible(true);
                HashSet<Style> styles =  dialog.getSelectedStyles();
                //check if at least one style has been selected
                if (styles.size() == 0){
                    return;
                }
                String sText = ""; //$NON-NLS-1$
                //reset old styles
                ambience.setStyles(new HashSet(10));
                for (Style style:styles){
                    ambience.addStyle(style);
                    sText += style.getName2()+',';  
                }
                sText = sText.substring(0,sText.length()-1);
                //Set button text
                ((JButton)widgets[row][2]).setText(sText);
                //if we have ambience name and some styles, register the ambience
                if (ambience.getName().length() > 0 && 
                        ambience.getStyles().size() > 0){
                    //no more error message if at least one ambience
                    setProblem(null);
                    jbNew.setEnabled(true);
                }
            }
        }
        
        /**
         * Refresh panel
         */
        private void refreshScreen(){
            removeAll();
            //refresh panel
            add(getPanel(),"1,1"); //$NON-NLS-1$
            add(jpButtons,"1,3,c,c"); //$NON-NLS-1$
            revalidate();
            repaint();
        }
    
    
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent ae) {
            if (ae.getSource() == jbNew){
                ambiences.add(new Ambience(Long.toString(System.currentTimeMillis()),"")); //create a void ambience //$NON-NLS-1$
                Collections.sort(ambiences);
                //refresh screen
                refreshScreen();
                //select new row
                JRadioButton jrb = (JRadioButton)widgets[ambiences.size()-1][0];
                jrb.setSelected(true);
                ambienceIndex = ambiences.size()-1;
                setProblem(Messages.getString("DigitalDJWizard.39")); //$NON-NLS-1$
                jbNew.setEnabled(false);
                jbDelete.setEnabled(true);
                JTextField jtf = (JTextField)widgets[ambienceIndex][1];
                jtf.requestFocusInWindow();
            }
            else if (ae.getSource() == jbDelete){
                JTextField jtf = (JTextField)widgets[ambienceIndex][1];
                Ambience ambience = ambiences.get(ambienceIndex);
                ambiences.remove(ambience);
                AmbienceManager.getInstance().removeAmbience(ambience.getID());
                if (AmbienceManager.getInstance().getAmbiences().size() == 0){
                    jbDelete.setEnabled(false);
                }
                if (ambienceIndex > 0){
                    ambienceIndex --;
                    JRadioButton jrb = (JRadioButton)widgets[ambienceIndex][0];
                    jrb.setSelected(true);
                }
                //refresh screen
                refreshScreen();
            }
            else if (ae.getSource() == jbDefaults){
                AmbienceManager.getInstance().createDefaultAmbiences();
                ambiences = new ArrayList(AmbienceManager.getInstance().getAmbiences());
                Collections.sort(ambiences);
                //refresh screen
                refreshScreen();
            }
            //in all cases, notify command panel
            ObservationManager.notify(new Event(EVENT_AMBIENCES_CHANGE));
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
       return null;
    }

    /* (non-Javadoc)
     * @see org.jajuk.ui.wizard.Wizard#getNextScreen(java.lang.Class)
     */
    @Override
    public Class getNextScreen(Class screen) {
        return null;
    }
    
    public AmbienceWizard() {
        super(Messages.getString("DigitalDJWizard.56"),AmbiencePanel.class, //$NON-NLS-1$
            Util.getIcon(IMAGE_DJ),Main.getWindow(),new Locale(Messages.getInstance().getLocal()));
    }


    /* (non-Javadoc)
     * @see org.jajuk.ui.wizard.Wizard#finish()
     */
    @Override
    public void finish() {
        for (Ambience ambience:ambiences){
            AmbienceManager.getInstance().registerAmbience(ambience);
        }
         //commit it to avoid it is lost before the app close
        AmbienceManager.getInstance().commit();
        ConfigurationManager.commit();
        //Refresh UI
        ObservationManager.notify(new Event(EVENT_AMBIENCES_CHANGE));
        
    }

}
