/*
 *  Jajuk
 *  Copyright (C) 2005 bertrand florat
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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jajuk.Main;
import org.jajuk.base.Album;
import org.jajuk.base.Device;
import org.jajuk.base.Event;
import org.jajuk.base.FileManager;
import org.jajuk.base.IPropertyable;
import org.jajuk.base.ItemManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.PropertyMetaInformation;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.error.JajukException;
import org.jdesktop.swingx.JXDatePicker;

/**
 * Item properties wizard for any jajuk item
 * 
 * @author Bertrand Florat
 * @created 6 juin 2005
 */
public class PropertiesWizard extends JDialog implements ITechnicalStrings,ActionListener {
    
    /**Close button*/
    JButton jbClose;
    
    /**Message panel*/
    JLabel jlMessage = new JLabel();
        
    /** Layout dimensions*/
    double[][] dSize = { { 0.99 }, { 0.99,10,20,10,20}};
    
    /**Items*/
    ArrayList<IPropertyable> alItems;
    
    /**Items2*/
    ArrayList<IPropertyable> alItems2;
    
    /**Merge flag*/
    boolean bMerged = false;
    
    /**
	 * Constructor for normal wizard with only one wizard panel and n items to display 
	 * 
	 * @param alItems items to display
    */
	public PropertiesWizard(ArrayList<IPropertyable> alItems) {
        //windows title: name of the element of only one item, or "selection" word otherwise
		super(Main.getWindow(),alItems.size()==1 ? ((IPropertyable)alItems.get(0)).getDesc():Messages.getString("PropertiesWizard.6"),true); //modal
        this.alItems = alItems;
        if (alItems.size() > 1){
            bMerged = true;
        }
        setLayout(new TableLayout(dSize));
        getContentPane().add(new PropertiesPanel(alItems,alItems.size()==1 ? ((IPropertyable)alItems.get(0)).getDesc():Messages.getString("PropertiesWizard.6")),"0,0");    
        display();
    }
    
    
    /**
     * Constructor for file wizard for ie with 2 wizard panels and n items to display 
     * 
     * @param alItems1 items to display in the first wizard panel (file for ie)
     * @param alItems2 items to display in the second panel (associated track for ie )
    */
    public PropertiesWizard(ArrayList<IPropertyable> alItems1,ArrayList<IPropertyable> alItems2) {
        //windows title: name of the element of only one item, or "selection" word otherwise
        super(Main.getWindow(),alItems1.size()==1 ? ((IPropertyable)alItems1.get(0)).getDesc():Messages.getString("PropertiesWizard.6"),true); //modal
        this.alItems = alItems1;
        this.alItems2 = alItems2;
        if (alItems1.size() > 1){
            bMerged = true;
        }
        setLayout(new TableLayout(dSize));
        JPanel jpProperties = new JPanel();
        jpProperties.setLayout(new BoxLayout(jpProperties,BoxLayout.Y_AXIS));
        if (alItems1.size() == 1){
            jpProperties.add(new PropertiesPanel(alItems1,alItems1.get(0).getDesc()));    
            jpProperties.add(Box.createVerticalStrut(20));
            jpProperties.add(new PropertiesPanel(alItems2,alItems2.get(0).getDesc()));    
        }
        else{
            jpProperties.add(new PropertiesPanel(alItems1,Util.formatPropertyDesc(Messages.getString("Property_files"))));    
            jpProperties.add(Box.createVerticalStrut(20));
            jpProperties.add(new PropertiesPanel(alItems2,Util.formatPropertyDesc(Messages.getString("Property_tracks"))));
        }
        getContentPane().add(jpProperties,"0,0");
        display();
    }

    private void display(){
        //Close button
        jbClose = new JButton(Messages.getString("Close"));
        jbClose.addActionListener(this);
        //Messages
        jlMessage.setBorder(BorderFactory.createEtchedBorder());//Color.BLACK));
        jlMessage.setForeground(Color.BLUE);
        getContentPane().add(jbClose,"0,2");
        getContentPane().add(jlMessage,"0,4");
        pack();
        setLocationRelativeTo(Main.getWindow());
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == jbClose){
            dispose();
        }
    }
    
    public boolean isLinkable(PropertyMetaInformation meta){
    String sKey = meta.getName();
    return sKey.equals(XML_DEVICE) || sKey.equals(XML_TRACK)
      || sKey.equals(XML_DEVICE) || sKey.equals(XML_TRACK)
      ||     sKey.equals(XML_ALBUM) || sKey.equals(XML_AUTHOR) || sKey.equals(XML_STYLE)
      ||     sKey.equals(XML_DIRECTORY) || sKey.equals(XML_FILE)
      ||     sKey.equals(XML_PLAYLIST) || sKey.equals(XML_PLAYLIST_FILE)
      ||    sKey.equals(XML_FILES)
      ||    ( sKey.equals(XML_TYPE) && !(alItems.get(0) instanceof Device)) ;   //avoid to confuse between music types and device types
    }
    
	/**
	 * 
	 * A properties panel
	 * @author Bertrand Florat
	 *
	 */
	class PropertiesPanel extends JPanel implements ActionListener,ChangeListener{
		        
		/**Properties panel*/
        JPanel jpProperties;
		
		/**Item description*/
		JLabel jlDesc;
		
        /**All dynamic widgets*/
        JComponent[][] widgets;
        
        /**Properties to display*/
        ArrayList<PropertyMetaInformation> alToDisplay;
        
        /**Items*/
        ArrayList<IPropertyable> alItems;
        
        /**
		 * Property panel for single types elements
		 * @param alItems items to display
         * @param sDesc Description (title)
         */
		PropertiesPanel(ArrayList<IPropertyable> alItems,String sDesc) {
		    int iX_SEPARATOR = 5;
			int iY_SEPARATOR = 10;
            this.alItems = alItems;
            IPropertyable pa = alItems.get(0); //first item 
			//Process properties to display
			alToDisplay = new ArrayList(10);
			for (PropertyMetaInformation meta:ItemManager.getItemManager(pa.getClass()).getProperties()){;//add only editable and non constructor properties
			    if (meta.isVisible() && (bMerged ? meta.isMergeable() : true)){ //if more than one item to display, show only mergeable properties
			            alToDisplay.add(meta);    
                }
			}
            if (alToDisplay.size() == 0){
                jlMessage.setText("  "+Messages.getString("PropertiesWizard.9"));
            }
			widgets = new JComponent[alToDisplay.size()][5]; //contains widgets for properties
			//Varname | value | link | type | all album 
            double p = TableLayout.PREFERRED;
            double[] dHoriz = {iX_SEPARATOR,p,iX_SEPARATOR,p,iX_SEPARATOR,p,iX_SEPARATOR,p,iX_SEPARATOR,p,iX_SEPARATOR};
            double[] dVert = new double[(2*alToDisplay.size())+3];//*2n+1 rows for spaces + 2 rows for title
            dVert[0]=iY_SEPARATOR;
            dVert[1]=20; //title
            int index = 0;
            for (PropertyMetaInformation meta:alToDisplay){
                //Set layout
                dVert[2*index+2] = iY_SEPARATOR;
                dVert[(2*index)+3] = 20;
                //Set widgets
                //Property name
                String sName = Messages.getInstance().contains("Property_"+meta.getName())?
                        Messages.getString("Property_"+meta.getName()):meta.getName();
                JLabel jlName = new JLabel(sName+" :"); //check if property name is translated (for custom properties)); 
                if (meta.isCustom()){
                    jlName.setForeground(Color.BLUE);
                }
                widgets[index][0] = jlName;
                //Property value
                if (meta.isEditable()){
                    if (meta.getType().equals(Date.class)){
                        JXDatePicker jdp = new JXDatePicker(pa.getDateValue(meta.getName()).getTime()); //If several items, take first value found
                        jdp.addActionListener(this);
                        jdp.setActionCommand("date");
                        widgets[index][1] = jdp;     
                    }
                    else if(meta.getType().equals(Boolean.class)){ //for a boolean, value is a checkbox
                        JCheckBox jcb = new JCheckBox();
                        jcb.addActionListener(this);
                        jcb.setActionCommand("boolean");
                        jcb.setSelected(pa.getBooleanValue(meta.getName()));
                        widgets[index][1] = jcb;
                    }
                    else if(meta.getType().equals(Double.class)){ //for a double, value is a formatted textfield
                        JTextField jtfValue = new JFormattedTextField(NumberFormat.getInstance());
                        jtfValue.addActionListener(this);
                        jtfValue.setActionCommand("double");
                        jtfValue.setText(pa.getHumanValue(meta.getName()));//If several items, take first value found
                        widgets[index][1] = jtfValue;
                    }
                    else if(meta.getType().equals(Long.class)){ //for a double, value is a formatted textfield
                        JTextField jtfValue = new JFormattedTextField(NumberFormat.getIntegerInstance());
                        jtfValue.addActionListener(this);
                        //jtfValue.addPropertyChangeListener("value",this);
                        jtfValue.setActionCommand("long");
                        jtfValue.setText(pa.getHumanValue(meta.getName()));//If several items, take first value found
                        widgets[index][1] = jtfValue;     
                    }
                    else if (meta.getType().equals(String.class) && meta.getName().equals("style")){ //for styles
                        ArrayList alStyles = (ArrayList)Util.getStyles().clone();
                        alStyles.add(0,pa.getHumanValue(meta.getName()));//display the current genre as default
                        SpinnerModel model = new SpinnerListModel(alStyles);
                        JSpinner jspinner = new JSpinner(model);
                        jspinner.addChangeListener(this);
                        widgets[index][1] = jspinner;     
                    }
                    else { //for all others formats (string, class)
                        JTextField jtfValue = new JTextField();
                        jtfValue.addActionListener(this);
                        jtfValue.setActionCommand("text");
                        jtfValue.setText(pa.getHumanValue(meta.getName()));//If several items, take first value found
                        widgets[index][1] = jtfValue;     
                    }
                }
                else{
                    widgets[index][1] = new JLabel(pa.getHumanValue(meta.getName())); //If several items, take first value found
                }
                //Link
                if (isLinkable(meta)){
                    JButton jbLink = new JButton(Util.getIcon(ICON_PROPERTIES));
                    jbLink.addActionListener(this);
                    jbLink.setActionCommand("link");
                    widgets[index][2] = jbLink;   
                }
                //Type
                widgets[index][3] = new JLabel("("+meta.getHumanType()+")");
                //Full album checkbox
                JCheckBox jcbFull = new JCheckBox();
                widgets[index][4] = jcbFull;
                jcbFull.setVisible(pa instanceof Track && !bMerged && meta.isMergeable() && meta.isEditable()); //full album is only available for non-unique and editable properties on single tracks
          
                index ++;
            }
            if (dVert.length > 0){
                dVert[dVert.length-1] = iY_SEPARATOR;//last row is a separator    
            }
            
            double[][] dSizeProperties = new double[][]{dHoriz,dVert};  
            dSizeProperties[0]=dHoriz;
            dSizeProperties[1]=dVert;
            //construct properties panel
            jpProperties = new JPanel();
            jpProperties.setLayout(new TableLayout(dSizeProperties));
            //Add title
            JLabel jlName = new JLabel("<html><b>"+Messages.getString("PropertiesWizard.1")+"</b></html>");
            jlName.setToolTipText(Messages.getString("PropertiesWizard.1"));
            JLabel jlValue = new JLabel("<html><b>"+Messages.getString("PropertiesWizard.2")+"</b></html>");
            jlValue.setToolTipText(Messages.getString("PropertiesWizard.2"));
            JLabel jlLink = new JLabel("<html><b>"+Messages.getString("PropertiesWizard.4")+"</b></html>");
            jlLink.setToolTipText(Messages.getString("PropertiesWizard.4"));
            JLabel jlType = new JLabel("<html><b>"+Messages.getString("PropertiesWizard.7")+"</b></html>");
            jlType.setToolTipText(Messages.getString("PropertiesWizard.7"));
            JLabel jlFullAlbum = new JLabel("<html><b>"+Messages.getString("PropertiesWizard.5")+"</b></html>");
            jlFullAlbum.setToolTipText(Messages.getString("PropertiesWizard.5"));
                        
            jpProperties.add(jlName,"1,1,c,c");
            jpProperties.add(jlValue,"3,1,c,c");
            jpProperties.add(jlLink,"5,1,c,c");
            jpProperties.add(jlType,"7,1,c,c");
            jpProperties.add(jlFullAlbum,"9,1,c,c");
                           
            //Add widgets
            int i = 0;
            int j= 2;
            for (PropertyMetaInformation meta:alToDisplay){
                j = (2*i)+3;
                jpProperties.add(widgets[i][0],"1,"+j+",c,c");
                jpProperties.add(widgets[i][1],"3,"+j);
                if (widgets[i][2] != null){ //link widget can be null
                    jpProperties.add(widgets[i][2],"5,"+j+",c,c");
                }
                jpProperties.add(widgets[i][3],"7,"+j+",c,c");
                jpProperties.add(widgets[i][4],"9,"+j+",c,c");
                i++;
            }
            double[][] dSize = { { 0.99 }, { 20, iY_SEPARATOR, 0.99} };
            setLayout(new TableLayout(dSize));
            //desc
            jlDesc = new JLabel(Util.formatPropertyDesc(sDesc));
            add(jlDesc, "0,0");
        	add(new JScrollPane(jpProperties), "0,2");
        }
   	   
        public void actionPerformed(ActionEvent ae) {
            Object oValue = null;
            PropertyMetaInformation meta = alToDisplay.get(getWidgetIndex((JComponent)ae.getSource()));
            //Link
            if (ae.getActionCommand().equals("link")){
                String sProperty = meta.getName();
                if (XML_FILES.equals(sProperty)) {
                    String sValue = alItems.get(0).getStringValue(sProperty); //can be only a set a files
                    StringTokenizer st = new StringTokenizer(sValue, ",");
                    ArrayList alItems = new ArrayList(3);
                    while (st.hasMoreTokens()) {
                        String sFile = st.nextToken();
                        IPropertyable pa = FileManager.getInstance().getItem(sFile);
                        if (pa != null) {
                            alItems.add(pa);
                        }
                    }
                    new PropertiesWizard(alItems); //show properties window for this item
                } else {
                    String sValue = alItems.get(0).getStringValue(sProperty); //can be only an ID
                    IPropertyable pa = ItemManager.getItemManager(sProperty).getItem(sValue);
                    if (pa != null) {
                        ArrayList alItems = new ArrayList(1);
                        alItems.add(pa);
                        new PropertiesWizard(alItems); //show properties window for this item
                    }
                }
            }
            //Boolean value changed
            else if (ae.getActionCommand().equals("boolean")){
                JCheckBox jcb = (JCheckBox)widgets[getWidgetIndex((JComponent)ae.getSource())][1];
                oValue = jcb.isSelected();
            }
            //Date value changed
            else if (ae.getActionCommand().equals("date")){
                JXDatePicker jdp = (JXDatePicker)widgets[getWidgetIndex((JComponent)ae.getSource())][1];
                oValue = jdp.getDate();
            }
            //textfield value changed
            else if (ae.getActionCommand().equals("text") ){
                JTextField jtf = (JTextField)widgets[getWidgetIndex((JComponent)ae.getSource())][1];
                oValue = jtf.getText();
                if (((String)oValue).length() < 1){ //check that string length > 0
                    String sOldValue = alItems.get(0).getStringValue(meta.getName()); //reset old value
                    jtf.setText(sOldValue);
                    Messages.showErrorMessage("137");
                    return;
                }
            }
            else if ( ae.getActionCommand().equals("double") || 
                    ae.getActionCommand().equals("long")){
                JFormattedTextField jtf = (JFormattedTextField)widgets[getWidgetIndex((JComponent)ae.getSource())][1];
                try {
                    jtf.commitEdit();
                }
                catch (ParseException e) {
                    Messages.showErrorMessage("137");
                    return;
                }
                oValue = jtf.getValue();
             }
            //Full album
            JCheckBox jcbFull = (JCheckBox)widgets[getWidgetIndex((JComponent)ae.getSource())][4];
            try{
                applyChange(meta.getName(),oValue,jcbFull.isSelected());
            }
            catch(JajukException je){
                Messages.showErrorMessage("104");
            }
        }
	
        /**
         * 
         * @param widget
         * @return index of a given widget in the widget table
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
        
        private void applyChange(String sProperty,Object oValue, boolean bFullAlbum) throws JajukException{
            if (oValue == null){
                throw new JajukException("137");
            }
            if (bMerged){ 
                /*multiple items case, in this case, we just change a non-constructor attribute on the same item
                 because in multiple mode, we cannot change constructor methods*/      
                for (IPropertyable pa : alItems){
                    ItemManager.changeItem(pa,sProperty,oValue);
                }
            }
            else{ //single item case, in this case, we can change constructor attributes so we can overwrite current item by a new one
                //Apply to full album if selected (do it first because after, current item could be changed and checkbox reseted)
                IPropertyable pa = alItems.get(0);
                if ( pa instanceof Track && bFullAlbum){
                    Track track = (Track)pa; 
                    Album album = track.getAlbum();
                    ArrayList<Track> alTracksToChange = album.getTracks();
                    alTracksToChange.remove(pa); //we treat the current item separetly
                    //now change property for each matching item
                    for (Track trackToChange:alTracksToChange){
                        ItemManager.changeItem(trackToChange,sProperty,oValue);
                    }
                }
                //then change current item
                IPropertyable newItem = ItemManager.changeItem(pa,sProperty,oValue);
                if (!newItem.equals(pa)){ //check if item has change, if so, change current item 
                    //at this point, alItems can contain only one item
                    alItems.remove(pa);
                    alItems.add(newItem);
                    jlDesc.setText(Util.formatPropertyDesc(newItem.getDesc()));
                }
            }
            //UI refresh
            ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH)); //TBI see later for a smarter event
            //notification message
            jlMessage.setText("  "+Messages.getString("PropertiesWizard.8")+": <"+sProperty+">="+oValue.toString()+
                (bFullAlbum?" ("+Messages.getString("PropertiesWizard.5")+")":""));
        }

        /* (non-Javadoc)
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e) {
            System.out.println(e.getSource());
        }
	}
   
}
