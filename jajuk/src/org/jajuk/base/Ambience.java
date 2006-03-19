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

package org.jajuk.base;

import java.util.HashSet;

/**
 * an ambience is a set of styles
 * @author     Bertrand Florat
 * @created    18 march 2006
 */
public class Ambience {

    /**List of styles*/
    private HashSet<Style> styles;
    
    /**Ambience name*/
    private String sName;
    
    
    /**
     * Constructor
     * @param sName Ambience name
     * @param styles list of styles
     */
    public Ambience(String sName,HashSet<Style> styles) {
        this.sName = sName;
        this.styles = styles;
    }
    
    /**
     * Constructor
     * @param sName Ambience name
     */
    public Ambience(String sName) {
        this.sName = sName;
    }
    
    public void addStyle(Style style){
        styles.add(style);
    }
    
    public void removeStyle(Style style){
        styles.remove(style);
    }

    public String getName() {
        return this.sName;
    }

    public void setName(String name) {
        this.sName = name;
    }

    public HashSet<Style> getStyles() {
        return this.styles;
    }

    public void setStyles(HashSet<Style> styles) {
        this.styles = styles;
    }

}
