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
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *  Transferable table row ( for DND )
 * @author     Bertrand Florat
 * @created    13 feb. 2004
 */
public class TransferableTableRow extends DefaultMutableTreeNode implements Transferable{
	public static final DataFlavor ROW_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "Row"); //$NON-NLS-1$
	private Object oData;
	
	public TransferableTableRow(Object oData){
		this.oData = oData;
	}
	
	public Object getData(){
		return oData;
	}
	
	private DataFlavor[] flavors = { ROW_FLAVOR };
	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
	 */
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return Arrays.asList(flavors).contains(flavor);
	}

	/* (non-Javadoc)
	 * @see java.awt.datatransfer.Transferable#getTransferData(java.awt.datatransfer.DataFlavor)
	 */
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (flavor == ROW_FLAVOR) {
			return this;
		}
		else {
			throw new UnsupportedFlavorException(flavor);	
		}		
	}
}
