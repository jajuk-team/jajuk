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
 *  $Revision$
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
	/**Self instance*/
	static private InformationJPanel ijp = null; 	
	
	
	/**
	 * Singleton access
	 * @return
	 */
	public static InformationJPanel getInstance(){
		if (ijp == null){
			ijp = new InformationJPanel();
		}
		return ijp;
	}
	 
	
	//widgets declaration
	JLabel jlMessage;
	JLabel jlSelection;
	JLabel jlQuality;
	JPanel jpTotal;
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
	
	
	
	private InformationJPanel(){
		//dimensions
		//set current jpanel properties
		setBorder(BorderFactory.createEtchedBorder());
		double size[][] =
			{{0.42, 0.13,0.05,0.07,0.33},
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
		jpTotal.setToolTipText(Messages.getString("InformationJPanel.Total_selection_progression_5")); //$NON-NLS-1$
		jpTotal.setLayout(new BoxLayout(jpTotal,BoxLayout.X_AXIS));
		jpTotal.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		jlTotal = new JLabel(); 
		jpTotal.add(jlTotal);
		jpTotal.add(Box.createHorizontalStrut(3));
		
		//Quality
		jlQuality = new JLabel();
		jlQuality.setToolTipText("Track bitrate");
		jlQuality.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		
		//current progress bar
		jpCurrent = new JPanel();
		jpCurrent.setToolTipText(Messages.getString("InformationJPanel.Current_track_progression_6")); //$NON-NLS-1$
		jpCurrent.setLayout(new BoxLayout(jpCurrent,BoxLayout.X_AXIS));
		jpCurrent.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		jpbCurrent = new JProgressBar(0,100);
		jpbCurrent.setStringPainted(true);
		jlCurrent = new JLabel();
		jpCurrent.add(jlCurrent);
		jpCurrent.add(Box.createHorizontalStrut(3));
		jpCurrent.add(jpbCurrent);
		
		
		//add widgets
		add(jlMessage,"0,0"); //$NON-NLS-1$
		add(jlSelection,"1,0"); //$NON-NLS-1$
		add(jlQuality,"2,0"); //$NON-NLS-1$
		add(jpTotal,"3,0"); //$NON-NLS-1$
		add(jpCurrent,"4,0"); //$NON-NLS-1$
		
			
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
	 * Set the quality box info
	 * @param sQuality
	 */
	public void setQuality(String sQuality){
		jlQuality.setText(sQuality);
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
