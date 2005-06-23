/*
 *  Jajuk
 *  Copyright (C) 2004 Bertrand Florat
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

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.Main;
import org.jajuk.base.AlbumManager;
import org.jajuk.base.AuthorManager;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.DirectoryManager;
import org.jajuk.base.FileManager;
import org.jajuk.base.ItemManager;
import org.jajuk.base.PlaylistFileManager;
import org.jajuk.base.StyleManager;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;

/**
 *  Type description
 *
 * @author     Administrateur
 * @created    21 juin 2005
 */
public abstract class CustomPropertyWizard extends JDialog implements ActionListener,ItemListener,ITechnicalStrings{
    JPanel jpMain;
    JLabel jlItemChoice;
    JComboBox jcbItemChoice;
    OKCancelPanel okp;
   
    /**
     * Constuctor
     * @param sTitle
     */
    CustomPropertyWizard(String sTitle){
        super(Main.getWindow(),sTitle);
    }
    
    /**
     * Create common UI for property wizards
     *
     */
    void populate(){
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
        Util.setShuffleLocation(this,400,400);
        jlItemChoice = new JLabel(Messages.getString("NewPropertyWizard.1"));
        jcbItemChoice = new JComboBox();
        jcbItemChoice.addItem(Messages.getString("Item_Track"));
        jcbItemChoice.addItem(Messages.getString("Item_File"));
        jcbItemChoice.addItem(Messages.getString("Item_Style"));
        jcbItemChoice.addItem(Messages.getString("Item_Author"));
        jcbItemChoice.addItem(Messages.getString("Item_Album"));
        jcbItemChoice.addItem(Messages.getString("Item_Device"));
        jcbItemChoice.addItem(Messages.getString("Item_Directory"));
        jcbItemChoice.addItem(Messages.getString("Item_Playlist")); //playlist file actually
        okp = new OKCancelPanel(this);
        okp.getOKButton().setEnabled(false);
        jcbItemChoice.addItemListener(this);
        jpMain = new JPanel();
    }
    
    /**
     * 
     * @return ItemManager associated with selected element in combo box
     */
    ItemManager getItemManager(){
        ItemManager im = null;
        switch(jcbItemChoice.getSelectedIndex()){
        case 0:
            im = TrackManager.getInstance();
            break;
        case 1:
            im = FileManager.getInstance();
            break;
        case 2:
            im = StyleManager.getInstance();
            break;
        case 3:
            im = AuthorManager.getInstance();
            break;
        case 4:
            im = AlbumManager.getInstance();
            break;
        case 5:
            im = DeviceManager.getInstance();
            break;
        case 6:
            im = DirectoryManager.getInstance();
            break;
        case 7:
            im = PlaylistFileManager.getInstance();
            break;
        }
        return im;
    }
        
    
}
