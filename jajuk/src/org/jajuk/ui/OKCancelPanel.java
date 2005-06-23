/*
 *  Jajuk
 *  Copyright (C) 2003 Administrateur
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

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.jajuk.i18n.Messages;

/**
 *  Ok Cancel generic panel
 *
 * @author     Bertrand Florat
 * @created    20 juin 2005
 */
public class OKCancelPanel extends JPanel {
    
    JButton jbOk;
    JButton jbCancel;
    
    /**Associated action listener*/
    ActionListener al;

    public OKCancelPanel(ActionListener al){
        this.al = al;
        //buttons
        double[][] dSize = {{TableLayout.FILL,100,TableLayout.FILL,100,TableLayout.FILL},
                {0.99}};
        setLayout(new TableLayout(dSize));
        jbOk = new JButton(Messages.getString("OK")); //$NON-NLS-1$
        jbOk.addActionListener(al);
        jbCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$
        jbCancel.addActionListener(al);
        add(jbOk,"1,0");
        add(jbCancel,"3,0");
    }
    
    public JButton getOKButton(){
        return jbOk;
    }
    
    public JButton getCancelButton(){
        return jbCancel;
    }
    
}
