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

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 
 *  Screen Header
 *
 * @author     Bertrand Florat
 * @created    1 mai 2006
 */
public class Header extends JPanel{
        
    JLabel jta;
    
    /**
     * 
     * @param sTextID I18N ID of header
     */
    public Header(){
        double[][] layout = new double[][]{
                {TableLayout.FILL},
                {80}
        };
        setLayout(new TableLayout(layout));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));
        jta = new JLabel(); 
        //jta.setEditable(false);
        //jta.setMargin(new Insets(10,10,10,10));
        jta.setOpaque(true);
        jta.setBackground(Color.WHITE);
        jta.setForeground(Color.BLACK);
        jta.setFont(new Font("Dialog",Font.PLAIN,14));
        add(jta,"0,0");
    }
    
    /**
     * Set the header text
     * @param sText
     */
    public void setText(String sText){
        jta.setText(sText);
    }
    
}