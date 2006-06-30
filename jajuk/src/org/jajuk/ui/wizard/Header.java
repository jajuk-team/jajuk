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
        jta.setFont(new Font("Dialog",Font.PLAIN,14)); //$NON-NLS-1$
        add(jta,"0,0"); //$NON-NLS-1$
    }
    
    /**
     * Set the header text
     * @param sText
     */
    public void setText(String sText){
        jta.setText(sText);
    }
    
}