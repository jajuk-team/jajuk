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
    /**data*/
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
     * @return screen name
     */
    abstract public String getName();
    
    /**
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
        return bCanGoNext && !bCanFinish; 
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
    
    public void setProblem(String sProblem){
        this.sProblem = sProblem;
        bCanGoNext = (sProblem==null);
    }
    
    public String getProblem(){
        return this.sProblem;
    }
           
    /**UI creation*/
    abstract public void initUI();
    
 
}
