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

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

/**
 *  Encapsulates a label with a text and an icon, used for tables
 *
 * @author     bflorat
 * @created    12 nov. 2004
 */
public class IconLabel {
    
    /**Icon*/
    private ImageIcon icon;
    
     /**Text*/
    private String sText;
    
    /**Background color*/
    private Color cBackground;
    
    /**Foreground color*/
    private Color cForeground;
    
    /**Font*/
    private Font font;
    
    /**Tooltip*/
    private String sTooltip;
    
    /**
     * Constructor
     * @param icon
     * @param sText
     * @param cBackground
     * @param cForeground
     * @param font
     */
    public IconLabel(ImageIcon icon,String sText,Color cBackground,Color cForeground,Font font,String sTooltip){
        this.icon = icon;
        this.sText = sText;
        this.cBackground = cBackground;
        this.cForeground = cForeground;
        this.font = font;
        this.sTooltip = sTooltip;
    }
   
    public IconLabel(ImageIcon icon,String sText){
        this.icon = icon;
        this.sText = sText;
    }
    
    
    /**
     * @return Returns the sText.
     */
    public String getText() {
        return sText;
    }
    /**
     * @return Returns the icon.
     */
    public ImageIcon getIcon() {
        return icon;
    }
    
    /**
     * @return Returns the cBackground.
     */
    public Color getBackground() {
        return cBackground;
    }
    /**
     * @return Returns the cForeground.
     */
    public Color getForeground() {
        return cForeground;
    }
    /**
     * @return Returns the font.
     */
    public Font getFont() {
        return font;
    }
    
    /**
     * toString method
     */
    public String toString(){
        return sText;
    }
    /**
     * @return Returns the sTooltip.
     */
    public String getTooltip() {
        return sTooltip;
    }
}
