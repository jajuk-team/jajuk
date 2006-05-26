/*
 *  Jajuk
 *  Copyright (C)2006  bflorat
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

import java.util.HashMap;

import javax.swing.JPanel;

/**
 *  Wizard screen
 *
 * @author     Bertrand Florat
 * @created    1 may 2006
 */
public abstract class Screen extends JPanel {
    /**Can finish*/
    boolean bCanFinish;
    /**Can Go Next*/
    boolean bCanGoNext;
    /**Can Go Previous*/
    boolean bCanGoPrevious;
    /**Wizard data*/
    public HashMap data;
    /**Problem*/
    private String sProblem;
   
    
    /**
     * Construct a screen
     * @param sName Screen name
     * @param sDesc description
     */
    public Screen() {
        data = Wizard.data;
        bCanFinish = false;
        bCanGoNext = true;
        bCanGoPrevious = true;
        initUI();
    }
        
    /**
     * Give here the step name.
     * @return screen name
     */
    abstract public String getName();
    
    /**
     * Screen description (optional)
     * @return screen description
     */
    abstract public String getDescription();
    
    boolean canFinish(){
        //Can finish only if none problem
        return bCanFinish && (sProblem == null);
    }
    
    /**
     * Set whether this screen is the last one
     * @param b
     */
    public void setCanFinish(boolean b){
        this.bCanFinish = b;
    }
    
    boolean canGoNext(){
        //if screen is last one, cannot go futher
        return bCanGoNext && !bCanFinish && (sProblem == null); 
    }
   
    boolean canGoPrevious(){
        return bCanGoPrevious; 
    }
    
    void setCanGoNext(boolean b){
        this.bCanGoNext = b;
    }
    
    void setCanGoPrevious(boolean b){
        this.bCanGoPrevious = b;
    }
    
    /**
     * Set a problem (set to null if problem is fixed)
     * @param sProblem Problem string or null if no more problem
     */
    public void setProblem(String sProblem){
        this.sProblem = sProblem;
        bCanGoNext = (sProblem==null);
    }
    
    /**
     * Get current problem
     * @return the current problem
     */
    public String getProblem(){
        return this.sProblem;
    }
           
    /**UI creation*/
    abstract public void initUI();
    
 
}
