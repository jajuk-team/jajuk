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

import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *  Wizard dialog
 *
 * @author     Bertrand Florat
 * @created    1 may 2006
 */
public abstract class Wizard extends Object implements ActionListener{
    /**Wizard name*/
    String sName;
    /**Initial screen*/
    Class<Screen> initial;
    /**Current screen*/
    Screen current;
    /**Wizard left side icon*/
    ImageIcon icon;
    /**Wizard data*/    
    public static HashMap data;
    /**Wizard header*/
    Header header;
    /**Wizard action Panel*/
    ActionsPanel actions;
    /**Wizard dialog*/
    JDialog dialog;
    /**Wizard timer used to refresh actions states*/
    Timer timer;    
    /**Screen buffer*/
    HashMap<Class<Screen>,Screen> hmClassScreens;
    /**Parent window*/
    Frame parentWindow;
    
    /**
     * Wizard constructor
     * @param sName Wizard name displayed in dialog title
     * @param initial Initial screen class
     * @param icon Wizard icon (null if no icon)
     * @param parentWindow wizard parent window
     */
     public Wizard(String sName,Class initial,ImageIcon icon,Frame parentWindow) {
        this.sName = sName;
        this.initial = initial;
        this.parentWindow = parentWindow;
        data = new HashMap(10);
        hmClassScreens = new HashMap(10);
        this.icon = icon;
        timer  = new Timer(50,new ActionListener() {
            //Refresh button states 
            public void actionPerformed(ActionEvent arg0) {
                if (current != null){
                    boolean isFirst = Wizard.this.initial.getClass().equals(Screen.class);
                    //can go previous if screen allow it and if the screen is not the first one
                    boolean bPrevious = current.canGoPrevious();
                    boolean bNext = current.canGoNext();
                    boolean bFinish = current.canFinish();
                    actions.setState(bPrevious,bNext,bFinish);
                    actions.setProblem(current.getProblem());
                }
            }
        });
        createUI();
        setScreen(initial);
        timer.start();
        dialog.setVisible(true);
     }
    
    /**
     * UI manager
     */
    private void createUI(){
        dialog = new JDialog(parentWindow,true);//modale
        dialog.setTitle(sName);
        header = new Header();
        actions = new ActionsPanel(this);
        display();
        dialog.pack();
        dialog.setLocationRelativeTo(parentWindow);
     }
    
    /**
     * Display the wizard 
     * @return wizard data
     */
    private HashMap showWizard(){
        //check initial screen is not null
        if (initial == null){
            return null;
        }
        createUI();
        return data; 
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        //Previous required. Note that the prev button is enabled only if 
        //the user can go previous
        if (ae.getActionCommand().equals("Prev")){
            setScreen(getPreviousScreen(current.getClass()));
        }
        else if (ae.getActionCommand().equals("Next")){
            setScreen(getNextScreen(current.getClass()));
        }
        else if (ae.getActionCommand().equals("Cancel")){
            dialog.dispose();
        }
        else if (ae.getActionCommand().equals("Finish")){
            finish();
            dialog.dispose();
        }
    }
    
    private void setScreen(Class screenClass) {
        Screen screen = hmClassScreens.get(screenClass);
        if (screen == null){
            try {
                screen = (Screen)screenClass.newInstance();
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
            hmClassScreens.put(screenClass,screen);
        }
        current = screen;
        current.setCanGoPrevious((getPreviousScreen(screenClass) != null));
        current.setCanGoNext((getNextScreen(screenClass) != null));
        String sDesc = screen.getDescription(); 
        if (sDesc != null){
        header.setText("<html><b>&nbsp;"+screen.getName()+
            "</b><p><p>&nbsp;"+sDesc);
        }
        else{
        header.setText("<html><b>&nbsp;"+screen.getName()+
            "</b>");
        }
        display();
    }
    
    /**
     * Called at each screen refresh
     *
     */
    private void display(){
        ((JPanel)dialog.getContentPane()).removeAll();
        //(buttons+problem)+screen+header
        double[][] dVertical = new double[][]{
                {450},
                {TableLayout.PREFERRED,250,10,TableLayout.PREFERRED}
        };
        JPanel jpVert = new JPanel(new TableLayout(dVertical));
        jpVert.add(header,"0,0");
        if (current != null){
            jpVert.add(current,"0,1");
        }
        else{ //current is null in initial state
            jpVert.add(new JPanel(),"0,1");
        }
        jpVert.add(actions,"0,3");
        //left part:icon, right part:buttons+problem+screen+header
        if (icon != null){
            double[][] dGlobal = new double[][]{
                    {200,5,TableLayout.FILL},
                    {TableLayout.FILL}
            };
            dialog.setLayout(new TableLayout(dGlobal));
            dialog.add(new JLabel(getResizedImage(icon,200,400)),"0,0");
            dialog.add(jpVert,"2,0");
        }
        else{
            dialog.add(jpVert);
        }
        dialog.getRootPane().setDefaultButton(actions.jbNext);
        ((JPanel)dialog.getContentPane()).revalidate();
        dialog.getContentPane().repaint();
    }
    
     /**
     * @return previous screen class
     */
    abstract public Class getPreviousScreen(Class screen);
    
    /**
     * 
     * @return next screen class
     */
    abstract public Class getNextScreen(Class screen);

    /**
     * Get curent screen
     * @return current screen class
     */
    public Class getCurrentScreen() {
        return this.current.getClass();
    }
    
    /**
     * Finish action. Called when user clicks on "finish"
     */
    abstract public void finish();

      
    /**
     * Icon resizing
     * @param img
     * @param iNewWidth
     * @param iNewHeight
     * @return resized icon
     */
    private static ImageIcon getResizedImage(ImageIcon img, int iNewWidth,
            int iNewHeight) {
        ImageIcon iiNew = new ImageIcon();
        Image image = img.getImage();
        Image scaleImg = image.getScaledInstance(iNewWidth, iNewHeight,
                Image.SCALE_AREA_AVERAGING);
        iiNew.setImage(scaleImg);
        return iiNew;
    }

   
}
