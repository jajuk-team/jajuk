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
 * $Revision$
 */

package org.jajuk.ui;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

/**
 *  JTable with followinf features: 
 * <p>Sortable
 * <p>Tooltips on each cell
 * @author     Bertrand Florat
 * @created    21 feb. 2004
 */
public class JajukTable extends JXTable implements ITechnicalStrings{
	
	/**
	 * Constructor
	 * @param model : model to use
	 * @param bSortable : is this table sortable
	 * */
	public JajukTable(TableModel model,TableColumnModel colModel, boolean bSortable) {
		super(model,colModel);
		init(bSortable);
	}
	
    /**
     * Constructor
     * @param model : model to use
     * @param bSortable : is this table sortable
     * */
    public JajukTable(TableModel model,boolean bSortable) {
        super(model);
        init(bSortable);
    }
    
    private void init(boolean bSortable){
        super.setSortable(bSortable);
        super.setColumnControlVisible(true);
        Highlighter highlighter = new RolloverHighlighter(Color.LIGHT_GRAY,Color.WHITE);
        HighlighterPipeline pipeHighlight = new HighlighterPipeline(new Highlighter[]{highlighter});
        setHighlighters(pipeHighlight);
        setRolloverEnabled(true);
        setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        packAll();   
    }
    
	/**
	 * Constructor
	 * @param model : model to use
	 */
	public JajukTable(TableModel model) {
		this(model,true);
	}
    
    /**
     * Hide columns
     *colsToShow list of columns id to keep
     */
    public void hideColumns(ArrayList<String> colsToShow){
        Iterator it = ((DefaultTableColumnModelExt)getColumnModel()).getColumns(false).iterator();
        while (it.hasNext()){
            TableColumnExt col = (TableColumnExt)it.next();
            if (!colsToShow.contains(((JajukTableModel)getModel()).getIdentifier(col.getModelIndex()))){
                col.setVisible(false);
            }
        }
    }
    
    /**
     * 
     * @return list of visible columns names as string
     * @param Name of the configuration key giving configuration
     */
    public ArrayList getColumnsConf(String property){
        ArrayList alOut = new ArrayList(10);
        String sConf = ConfigurationManager.getProperty(property);
        StringTokenizer st = new StringTokenizer(sConf,","); //$NON-NLS-1$
        while (st.hasMoreTokens()){
            alOut.add(st.nextToken());
        }
        return alOut;
    }
   
     /**
     * 
     * @return columns configuration
     * 
     */
    public String createColumnsConf(){
        StringBuffer sb = new StringBuffer();
        Iterator it = ((DefaultTableColumnModelExt)getColumnModel()).getColumns(true).iterator();
        while (it.hasNext()){
            TableColumnExt col = (TableColumnExt)it.next();
            String sIdentifier = ((JajukTableModel)getModel()).getIdentifier(col.getModelIndex());
            if (col.isVisible()){
                sb.append(sIdentifier+",");     //$NON-NLS-1$
            }
        }
        //remove last coma
        if (sb.length()>0){
            return sb.substring(0,sb.length()-1);
        }
        else{
            return sb.toString();    
        }
    }
    
    /**
     * 
     * @return columns configuration from given list of columns identifiers
     * 
     */
    public String getColumnsConf(ArrayList alCol){
        StringBuffer sb = new StringBuffer();
        Iterator it = alCol.iterator();
        while (it.hasNext()){
            sb.append((String)it.next()+","); //$NON-NLS-1$
        }
        //remove last coma
        if (sb.length()>0){
            return sb.substring(0,sb.length()-1);
        }
        else{
            return sb.toString();    
        }
    }
    
	/**
	 * add tooltips to each cell
	*/
	public String getToolTipText(MouseEvent e) {
		String tip = null;
		java.awt.Point p = e.getPoint();
		int rowIndex = rowAtPoint(p);
		int colIndex = columnAtPoint(p);
		TableModel model = getModel();
		if (rowIndex < 0 || colIndex < 0){
			return null;
		}
		Object o = getModel().getValueAt(convertRowIndexToModel(rowIndex),convertColumnIndexToModel(colIndex));
		if (o == null){
		    return null;
		}
		else if(o instanceof IconLabel){
		    return ((IconLabel)o).getTooltip(); 
		}
		else{
		    return o.toString();
		}
	}

    
	
}


