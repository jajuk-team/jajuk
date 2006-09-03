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

package org.jajuk.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.jajuk.base.Bookmarks;
import org.jajuk.base.FIFO;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Item;
import org.jajuk.ui.views.AbstractPlaylistEditorView;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  DND handler for table
 * @author     Bertrand Florat
 * @created    13 feb. 2004
 */

public class PlaylistEditorTransferHandler extends TransferHandler implements ITechnicalStrings {
    
    private JTable jtable;
    public static int iSelectedRow = 0;
    
    /**Constructor*/
    public PlaylistEditorTransferHandler(JTable jtable) {
        this.jtable = jtable;
    }
    
    /**
     * Called when draging
     */
    protected Transferable createTransferable(JComponent c) {
        //make sure to remove others selected rows (can occur during the drag)
        jtable.getSelectionModel().setSelectionInterval(iSelectedRow,iSelectedRow);
        if (jtable instanceof JajukTable){//sorting only for jajuk table
            iSelectedRow = ((JajukTable)jtable).convertRowIndexToModel(iSelectedRow); //selected row in model
        }
        Object o = ((JajukTableModel)jtable.getModel()).getItemAt(iSelectedRow);
        if ( o  == null){ //no? try to find a file for this id
            o = FileManager.getInstance().getItem(jtable.getModel().getValueAt(iSelectedRow,0).toString());
        }
        if ( o != null){
            return new TransferableTableRow(o);
        }
        
        return null;
    }
    

    /**
     * return action type
     */
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }
    
    /**
     * Called when dropping
     */
  public boolean importData(JComponent c, Transferable t) {
        try{
            if (canImport(c, t.getTransferDataFlavors())) {
                JComponent comp = (JComponent)c.getParent().getParent().getParent();
                PlaylistFileItem plfi = ((AbstractPlaylistEditorView)comp).getCurrentPlaylistFileItem();
                Object oData = null;
                DataFlavor flavor = (DataFlavor)t.getTransferDataFlavors()[0];
                if ( flavor.getHumanPresentableName().equals(TransferableTableRow.ROW_FLAVOR.getHumanPresentableName())){ 
                    TransferableTableRow ttr = (TransferableTableRow)t.getTransferData(TransferableTableRow.ROW_FLAVOR);
                    oData = ttr.getData();  
                }
                else if ( flavor.getHumanPresentableName().equals(TransferableTreeNode.NODE_FLAVOR.getHumanPresentableName())){ 
                    TransferableTreeNode ttn = (TransferableTreeNode)t.getTransferData(TransferableTreeNode.NODE_FLAVOR);
                    oData = ttn.getData();  
                }
                ArrayList<File> alSelectedFiles = Util.getfilesFromSelection((Item)oData);
                //queue case
                if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_QUEUE){
                    FIFO.getInstance().push(Util.createStackItems(Util.applyPlayOption(alSelectedFiles),
                        ConfigurationManager.getBoolean(CONF_STATE_REPEAT),true),ConfigurationManager.getBoolean(CONF_OPTIONS_DEFAULT_ACTION_DROP));
                }
                //bookmark case
                else if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_BOOKMARK){
                    Bookmarks.getInstance().addFiles(Util.applyPlayOption(alSelectedFiles));
                }
                //normal or new playlist case
                else if ( plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NORMAL || plfi.getType() == PlaylistFileItem.PLAYLIST_TYPE_NEW){
                    plfi.getPlaylistFile().addFiles(Util.applyPlayOption(alSelectedFiles));
                }
                return true;
            }
        }
        catch(Exception e){
            Log.error(e);
        }
        return false;
        
        
    }
    
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        String sFlavor  = flavors[0].getHumanPresentableName(); 
        if ( sFlavor.equals("Node") || sFlavor.equals("Row")){ //$NON-NLS-1$ //$NON-NLS-2$
            return true;
        }
        return false;
    } 
    
}
