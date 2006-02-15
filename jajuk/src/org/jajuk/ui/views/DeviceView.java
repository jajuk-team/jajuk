/*
 *  Jajuk
 *  Copyright (C) 2003 Bertrand Florat
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

package org.jajuk.ui.views;

import static org.jajuk.ui.action.JajukAction.WIZARD;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.ui.DeviceWizard;
import org.jajuk.ui.JajukButton;
import org.jajuk.ui.action.ActionManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

import ext.FlowScrollPanel;

/**
 *  Device view used to create and modify Jajuk devices
 * <p>Configuration perspective
 * <p>Singleton
 *
 * @author     Bertrand Florat
 * @created    8 nov. 2003
 */
public class DeviceView extends ViewAdapter implements IView,ITechnicalStrings,ActionListener,Observer,MouseListener {
    
    static private DeviceView dv; //self instance
    
    JToolBar jtbButtons;
    JButton jbNew;
    JButton jbDelete;
    JButton jbProperties;
    JButton jbMount;
    JButton jbUnmount;
    JButton jbTest;
    JButton jbRefresh;
    JButton jbSynchro;
    JajukButton jbWizard;
    FlowScrollPanel jpDevices;
    
    JPopupMenu jpmenu;
    JMenuItem jmiDelete;
    JMenuItem jmiProperties;
    JMenuItem jmiMount;
    JMenuItem jmiUnmount;
    JMenuItem jmiTest;
    JMenuItem jmiRefresh;
    JMenuItem jmiSynchronize;
    
    DeviceItem diSelected;
    
    
    public DeviceView(){
        dv = this;
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#display()
     */
    public void populate(){
        //buttons
        jtbButtons = new JToolBar();
        jtbButtons.setRollover(true);
        jtbButtons.setFloatable(false);
        jtbButtons.setBorder(BorderFactory.createEtchedBorder());
        
        jbNew = new JButton(Util.getIcon(ICON_NEW));
        jbNew.setActionCommand(EVENT_DEVICE_NEW);
        jbNew.addActionListener(this);
        jbNew.setToolTipText(Messages.getString("DeviceView.0")); //$NON-NLS-1$
        
        jbDelete = new JButton(Util.getIcon(ICON_DELETE));
        jbDelete.setActionCommand(EVENT_DEVICE_DELETE);
        jbDelete.addActionListener(this);
        jbDelete.setToolTipText(Messages.getString("DeviceView.1")); //$NON-NLS-1$
        jbProperties = new JButton(Util.getIcon(ICON_PROPERTIES));
        jbProperties.setActionCommand(EVENT_DEVICE_PROPERTIES);
        jbProperties.addActionListener(this);
        jbProperties.setToolTipText(Messages.getString("DeviceView.2")); //$NON-NLS-1$
        
        jbMount = new JButton(Util.getIcon(ICON_MOUNT));
        jbMount.setActionCommand(EVENT_DEVICE_MOUNT);
        jbMount.addActionListener(this);
        jbMount.setToolTipText(Messages.getString("DeviceView.3")); //$NON-NLS-1$
        
        jbUnmount = new JButton(Util.getIcon(ICON_UNMOUNT));
        jbUnmount.setActionCommand(EVENT_DEVICE_UNMOUNT);
        jbUnmount.addActionListener(this);
        jbUnmount.setToolTipText(Messages.getString("DeviceView.4")); //$NON-NLS-1$
        
        jbTest = new JButton(Util.getIcon(ICON_TEST));
        jbTest.setActionCommand(EVENT_DEVICE_TEST);
        jbTest.addActionListener(this);
        jbTest.setToolTipText(Messages.getString("DeviceView.5")); //$NON-NLS-1$
        
        jbRefresh = new JButton(Util.getIcon(ICON_REFRESH));
        jbRefresh.setActionCommand(EVENT_DEVICE_REFRESH);
        jbRefresh.addActionListener(this);
        jbRefresh.setToolTipText(Messages.getString("DeviceView.6")); //$NON-NLS-1$
        
        jbSynchro = new JButton(Util.getIcon(ICON_SYNCHRO));
        jbSynchro.setActionCommand(EVENT_DEVICE_SYNCHRO);
        jbSynchro.addActionListener(this);
        jbSynchro.setToolTipText(Messages.getString("DeviceView.7")); //$NON-NLS-1$
        
        jbWizard = new JajukButton(ActionManager.getAction(WIZARD));
        jbWizard.setActionCommand(EVENT_WIZARD);
        jbWizard.setToolTipText(Messages.getString("JajukJMenuBar.18")); //$NON-NLS-1$
        
        jtbButtons.add(jbNew);
        jtbButtons.addSeparator();
        jtbButtons.add(jbDelete);
        jtbButtons.addSeparator();
        jtbButtons.add(jbProperties);
        jtbButtons.addSeparator();
        jtbButtons.add(jbMount);
        jtbButtons.addSeparator();
        jtbButtons.add(jbUnmount);
        jtbButtons.addSeparator();
        jtbButtons.add(jbTest);
        jtbButtons.addSeparator();
        jtbButtons.add(jbRefresh);
        jtbButtons.addSeparator();
        jtbButtons.add(jbSynchro);
        jtbButtons.addSeparator();
        jtbButtons.add(jbWizard);
        jtbButtons.addSeparator();
        
        //devices
        jpDevices = new FlowScrollPanel();
        Dimension dim = new Dimension(getWidth(),getHeight());
        jpDevices.setPreferredSize(dim);
        JScrollPane jsp = new JScrollPane(jpDevices,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jpDevices.setScroller(jsp);
        
        jpDevices.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        //Popup menus
        jpmenu =  new JPopupMenu();
        
        jmiMount = new JMenuItem(Messages.getString("DeviceView.8"),Util.getIcon(ICON_MOUNT));  //$NON-NLS-1$
        jmiMount.addActionListener(this);
        jmiMount.setActionCommand(EVENT_DEVICE_MOUNT);
        jpmenu.add(jmiMount);
        
        jmiUnmount = new JMenuItem(Messages.getString("DeviceView.9"),Util.getIcon(ICON_UNMOUNT));  //$NON-NLS-1$
        jmiUnmount.addActionListener(this);
        jmiUnmount.setActionCommand(EVENT_DEVICE_UNMOUNT);
        jpmenu.add(jmiUnmount);
        
        jmiTest = new JMenuItem(Messages.getString("DeviceView.10"),Util.getIcon(ICON_TEST)); //$NON-NLS-1$
        jmiTest.addActionListener(this);
        jmiTest.setActionCommand(EVENT_DEVICE_TEST);
        jpmenu.add(jmiTest);
        
        jmiRefresh =new JMenuItem(Messages.getString("DeviceView.11"),Util.getIcon(ICON_REFRESH));  //$NON-NLS-1$
        jmiRefresh.addActionListener(this);
        jmiRefresh.setActionCommand(EVENT_DEVICE_REFRESH);
        jpmenu.add(jmiRefresh);
        
        jmiSynchronize =new JMenuItem(Messages.getString("DeviceView.12"),Util.getIcon(ICON_SYNCHRO));  //$NON-NLS-1$
        jmiSynchronize.addActionListener(this);
        jmiSynchronize.setActionCommand(EVENT_DEVICE_SYNCHRO);
        jpmenu.add(jmiSynchronize);
        
        jmiDelete = new JMenuItem(Messages.getString("DeviceView.13"),Util.getIcon(ICON_DELETE)); //$NON-NLS-1$
        jmiDelete.addActionListener(this);
        jmiDelete.setActionCommand(EVENT_DEVICE_DELETE);
        jpmenu.add(jmiDelete);
        
        jmiProperties = new JMenuItem(Messages.getString("DeviceView.14"),Util.getIcon(ICON_CONFIGURATION)); //$NON-NLS-1$
        jmiProperties.addActionListener(this);
        jmiProperties.setActionCommand(EVENT_DEVICE_PROPERTIES);
        jpmenu.add(jmiProperties);
        
        //add devices
        refreshDevices();
        
        //add  
        double size[][] =
        {{0.99},
                {30,0.99}};
        setLayout(new TableLayout(size));
        add(jtbButtons,"0,0"); //$NON-NLS-1$
        add(jsp,"0,1"); //$NON-NLS-1$
        //Register on the list for subject we are interrested in
        ObservationManager.register(EVENT_DEVICE_MOUNT,this);
        ObservationManager.register(EVENT_DEVICE_UNMOUNT,this);
        ObservationManager.register(EVENT_DEVICE_NEW,this);
        ObservationManager.register(EVENT_DEVICE_REFRESH,this);
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getID()
     */
    public String getID() {
        return VIEW_NAME_DEVICES;
    }
    
    
    private void refreshDevices(){
        synchronized(DeviceManager.getInstance().getLock()){
            //remove all devices
            if (jpDevices.getComponentCount() > 0 ){
                jpDevices.removeAll();
            }
            //New device
            DeviceItem diNew = new DeviceItem(ICON_DEVICE_NEW,Messages.getString("DeviceView.17"),null); //$NON-NLS-1$
            diNew.setToolTipText(Messages.getString("DeviceView.18")); //$NON-NLS-1$
            jpDevices.add(diNew);
            diNew.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    DeviceWizard dw = new DeviceWizard();
                    dw.updateWidgetsDefault();
                    dw.pack();
                    dw.setVisible(true);
                }
            });
            //Add devices
            Iterator it = DeviceManager.getInstance().getItems().iterator();
            while (it.hasNext()){
                final Device device = (Device)it.next();
                String sIcon = ICON_DEVICE_DIRECTORY_MOUNTED;
                String sTooltip = ""; //$NON-NLS-1$
                switch ((int)device.getDeviceType()){
                case 0 :
                    sTooltip = Messages.getString("Device_type.directory"); //$NON-NLS-1$
                    if ( device.isMounted()){
                        sIcon = ICON_DEVICE_DIRECTORY_MOUNTED;
                    }
                    else{
                        sIcon = ICON_DEVICE_DIRECTORY_UNMOUNTED;
                    }
                    break;
                case 1 : 
                    sTooltip = Messages.getString("Device_type.file_cd"); //$NON-NLS-1$
                    if ( device.isMounted()){
                        sIcon = ICON_DEVICE_CD_MOUNTED;
                    }
                    else{
                        sIcon = ICON_DEVICE_CD_UNMOUNTED;
                    }
                    break;
                case 2 : 
                    sTooltip = Messages.getString("Device_type.network_drive"); //$NON-NLS-1$
                    if ( device.isMounted()){
                        sIcon = ICON_DEVICE_NETWORK_DRIVE_MOUNTED;
                    }
                    else{
                        sIcon = ICON_DEVICE_NETWORK_DRIVE_UNMOUNTED;
                    }
                    break;
                case 3 : 
                    sTooltip = Messages.getString("Device_type.extdd"); //$NON-NLS-1$
                    if ( device.isMounted()){
                        sIcon = ICON_DEVICE_EXT_DD_MOUNTED;
                    }
                    else{
                        sIcon = ICON_DEVICE_EXT_DD_UNMOUNTED;
                    }
                    break;
                case 4 : 
                    sTooltip = Messages.getString("Device_type.player"); //$NON-NLS-1$
                    if ( device.isMounted()){
                        sIcon = ICON_DEVICE_PLAYER_MOUNTED;
                    }
                    else{
                        sIcon = ICON_DEVICE_PLAYER_UNMOUNTED;
                    }
                    break;
                case 5 : 
                    sTooltip = Messages.getString("Device_type.remote"); //$NON-NLS-1$
                    if ( device.isMounted()){
                        sIcon = ICON_DEVICE_REMOTE_MOUNTED;
                    }
                    else{
                        sIcon = ICON_DEVICE_REMOTE_UNMOUNTED;
                    }
                    break;
                }
                DeviceItem di = new DeviceItem(sIcon,device.getName(),device);
                di.setToolTipText(sTooltip);
                di.addMouseListener(this);
                di.setToolTipText(device.getDeviceTypeS());
                jpDevices.add(di);
            }
        }
    }	
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.views.IView#setVisible(boolean)
     */
    public void setVisible(boolean pVisible) {
    }
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.views.IView#getComponent()
     */
    public Component getComponent() {
        return this;
    }
    
    /**
     * Singleton implementation
     * @return
     */
    public static synchronized DeviceView getInstance(){
        if ( dv == null){
            dv = new DeviceView();
        }
        return dv;
    }
    
    public void mouseClicked(MouseEvent e) {
        boolean bSameDevice = ((diSelected != null) && e.getSource().equals(diSelected));//be carefull: at startup, diSelected is null
        //remove old device item border if needed
        if (!bSameDevice && diSelected != null){
            diSelected.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        }
        diSelected = (DeviceItem)e.getSource();
        //Test if it is the "NEW" device
        if (((DeviceItem)e.getSource()).getDevice() == null){ //means that it is "NEW" device, just leave
            return;
        }
        //remove options for non synchronized devices
        if (diSelected.getDevice().containsProperty(XML_DEVICE_SYNCHRO_SOURCE)){
            jbSynchro.setEnabled(true);
            jmiSynchronize.setEnabled(true);
        }
        else{
            jbSynchro.setEnabled(false);
            jmiSynchronize.setEnabled(false);
        }
        if (bSameDevice && e.getButton() == 1){ //one device already selected + right click
            DeviceWizard dw = new DeviceWizard();
            dw.updateWidgets(diSelected.getDevice());
            dw.pack();
            dw.setVisible(true);
        }
        else{ //a new device is selected
            diSelected.setBorder(BorderFactory.createLineBorder(Color.BLACK,5));
            if (e.getButton() == 3){
                jpmenu.show(e.getComponent(),e.getX(),e.getY());
            }
        }
    }
    
    
    public void actionPerformed(final ActionEvent ae){
        if (ae.getActionCommand().equals(EVENT_DEVICE_NEW)){
            DeviceWizard dw = new DeviceWizard();
            dw.updateWidgetsDefault();
            dw.pack();
            dw.setVisible(true);
            return;
        }
        if (diSelected == null){  //test a device is selected
            return;
        }
        if (ae.getActionCommand().equals(EVENT_DEVICE_DELETE)){
            DeviceManager.getInstance().removeDevice(diSelected.getDevice());
            jpDevices.remove(diSelected);
            ViewManager.notify(EVENT_VIEW_REFRESH_REQUEST,DeviceView.this);
            //refresh views
            ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH));
        }
        else if (ae.getActionCommand().equals(EVENT_DEVICE_MOUNT)){
            try{
                diSelected.getDevice().mount();
            }
            catch(Exception e){
                Messages.showErrorMessage("011"); //$NON-NLS-1$
            }
        }
        else if (ae.getActionCommand().equals(EVENT_DEVICE_UNMOUNT)){
            try{
                diSelected.getDevice().unmount();
            }
            catch(Exception e){
                Messages.showErrorMessage("012"); //$NON-NLS-1$
            }
        }
        else if (ae.getActionCommand().equals(EVENT_DEVICE_PROPERTIES)){
            DeviceWizard dw = new DeviceWizard();
            dw.updateWidgets(diSelected.getDevice());
            dw.pack();
            dw.setVisible(true);
        }
        else if (ae.getActionCommand().equals(EVENT_DEVICE_REFRESH)){
            diSelected.getDevice().refresh(true);
        }
        else if (ae.getActionCommand().equals(EVENT_DEVICE_SYNCHRO)){
            diSelected.getDevice().synchronize(true);
        }
        else if (ae.getActionCommand().equals(EVENT_DEVICE_TEST)){
            new Thread() {//test asynchronously in case of delay (samba pbm for ie) 
                public void run() {
                    if (diSelected.getDevice().test()){
                        Messages.showInfoMessage(Messages.getString("DeviceView.21"),Util.getIcon(ICON_OK)); //$NON-NLS-1$
                    }
                    else{
                        Messages.showInfoMessage(Messages.getString("DeviceView.22"),Util.getIcon(ICON_KO)); //$NON-NLS-1$
                    }
                }
            }.start();
        }
        else if (ae.getActionCommand().equals(EVENT_WIZARD)){
            
        }
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.IView#getDesc()
     */
    public String getDesc() {
        return "DeviceView.23"; //$NON-NLS-1$
    }
    
    
    /* (non-Javadoc)
     * @see org.jajuk.ui.Observer#update(java.lang.String)
     */
    public void update(Event event) {
        String subject = event.getSubject();
        if ( EVENT_DEVICE_MOUNT.equals(subject) 
                || EVENT_DEVICE_UNMOUNT.equals(subject) 
                || EVENT_DEVICE_REFRESH.equals(subject)){
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Util.waiting();
                    refreshDevices();
                    jpDevices.revalidate();
                    jpDevices.repaint();
                    Util.stopWaiting();
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent arg0) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent arg0) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent arg0) {
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent arg0) {
    }
    
    
}


/**
 * A device icon + text
 *  Type description
 *
 * @author     Bertrand Florat
 * @created    8 nov. 2003
 */
class DeviceItem extends JPanel{
    
    /** Associated device */
    Device device;
    
    /**
     * Constructor
     */
    DeviceItem(String sIcon,String sName,Device device){
        this.device = device;
        setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        JLabel jlIcon = new JLabel(Util.getIcon(sIcon)); 
        add(jlIcon);
        JLabel jlName = new JLabel(sName);
        add(jlName);
    }
    
    /**
     * @return Returns the device.
     */
    public Device getDevice() {
        return device;
    }
    
    /**
     * @param device The device to set.
     */
    public void setDevice(Device device) {
        this.device = device;
    }
    
    
}
