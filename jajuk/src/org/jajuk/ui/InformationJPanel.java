/*
 *  Jajuk
 *  Copyright (C) 2003 bflorat
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
 * $Log$
 * Revision 1.2  2003/10/21 20:43:06  bflorat
 * TechnicalStrings to ITechnicalStrings according to coding convention
 *
 * Revision 1.1  2003/10/12 21:08:11  bflorat
 * 12/10/2003
 *
 */
package org.jajuk.ui;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EtchedBorder;

import layout.TableLayout;

import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;

/**
 *  Status / information panel ( static view )
 *
 * @author     bflorat
 * @created    11 oct. 2003
 */
public class InformationJPanel extends JPanel implements ITechnicalStrings{
	//consts
	/** Informative message type  ( displayed in blue ) **/
	public static final int INFORMATIVE = 0;
	/** Informative message type ( displayed in red )**/
	public static final int ERROR = 1;
	 
	
	//widgets declaration
	JLabel jlMessage;
	JLabel jlSelection;
	JPanel jpTotal;
		JProgressBar jpbTotal;
		JLabel jlTotal;
	JPanel jpCurrent;
		JProgressBar jpbCurrent;
		JLabel jlCurrent;
	//attributes	
	String sMessage;
	String sSelection;
	int iTotalStatus;
	String sTotalStatus;
	int iCurrentStatus;
	String sCurrentStatus;
	
	
	
	public InformationJPanel(){
		//dimensions
		//set current jpanel properties
		setBorder(BorderFactory.createEtchedBorder());
		double size[][] =
			{{0.50, 0.10,0.20,0.20},
			 {20}};
		 setLayout(new TableLayout(size));
		
		
		//message bar
		jlMessage = new JLabel();  
		jlMessage.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		//selection bar
		jlSelection = new JLabel();  
		jlSelection.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		//total progress bar
		jpTotal = new JPanel();
		jpTotal.setLayout(new BoxLayout(jpTotal,BoxLayout.X_AXIS));
		jpTotal.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		jpbTotal = new JProgressBar(0,100);
		jlTotal = new JLabel(); 
		jpTotal.add(jpbTotal);
		jpTotal.add(Box.createHorizontalStrut(3));
		jpTotal.add(jlTotal);
		
		//current progress bar
		jpCurrent = new JPanel();
		jpCurrent.setLayout(new BoxLayout(jpCurrent,BoxLayout.X_AXIS));
		jpCurrent.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		jpbCurrent = new JProgressBar(0,100);
		jlCurrent = new JLabel();
		jpCurrent.add(jpbCurrent);
		jpCurrent.add(Box.createHorizontalStrut(3));
		jpCurrent.add(jlCurrent);
		
		
		//add widgets
		add(jlMessage,"0,0"); //$NON-NLS-1$
		add(jlSelection,"1,0"); //$NON-NLS-1$
		add(jpTotal,"2,0"); //$NON-NLS-1$
		add(jpCurrent,"3,0"); //$NON-NLS-1$
		
		//tooltips
		jpTotal.setToolTipText(Messages.getString("InformationJPanel.Total_selection_progression_5")); //$NON-NLS-1$
		jpCurrent.setToolTipText(Messages.getString("InformationJPanel.Current_track_progression_6")); //$NON-NLS-1$
		
			
	}

	/**
	 * @return
	 */
	public int getCurrentStatus() {
		return iCurrentStatus;
	}

	/**
	 * @return
	 */
	public int getTotalStatus() {
		return iTotalStatus;
	}

	/**
	 * @return
	 */
	public String getMessage() {
		return sMessage;
	}

	/**
	 * @return
	 */
	public String getSelection() {
		return this.sSelection;
	}

	/**
	 * @param i
	 */
	public void setCurrentStatus(int i) {
		iCurrentStatus = i;
		jpbCurrent.setValue(i);
	}

	/**
	 * @param i
	 */
	public void setTotalStatus(int i) {
		iTotalStatus = i;
		jpbTotal.setValue(i);
	}

	/**
	 * @param label
	 */
	public void setMessage(String sMessage,int iMessageType) {
		this.sMessage = sMessage;
		switch(iMessageType){
			case INFORMATIVE:
					jlMessage.setForeground(Color.BLUE);
				break;
			case ERROR:
				jlMessage.setForeground(Color.RED);
					break;
			default:
				jlMessage.setForeground(Color.BLUE);
				break;	 
		}
		jlMessage.setText(sMessage); 
		jlMessage.setToolTipText(sMessage);
		
		
	}

	/**
	 * @param label
	 */
	public void setSelection(String sSelection) {
		this.sSelection = sSelection;
		jlSelection.setText(sSelection);
		jlSelection.setToolTipText(sSelection);
	}

	/**
	 * @return
	 */
	public String getCurrentStatusMessage() {
		return sCurrentStatus;
	}

	/**
	 * @return
	 */
	public String getTotalStatusMessage() {
		return sTotalStatus;
	}

	/**
	 * @param string
	 */
	public void setCurrentStatusMessage(String string) {
		sCurrentStatus = string;
		jlCurrent.setText(string);
	}

	/**
	 * @param string
	 */
	public void setTotalStatusMessage(String string) {
		sTotalStatus = string;
		jlTotal.setText(string);
	}

}
