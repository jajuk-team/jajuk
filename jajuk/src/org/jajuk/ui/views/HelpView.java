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

package org.jajuk.ui.views;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.jajuk.ui.CommandJPanel;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 * Help view using java help api
 * <p>Help perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created   22 dec. 2003
 */
public class HelpView extends ViewAdapter {

	/**Self instance*/
	private static HelpView hv;
	
	/**hepl set*/
	HelpSet hs;
	
	/**Help broker*/
	HelpBroker hb;
	
	/**Return self instance*/
	public static HelpView getInstance(){
		if (hv == null){
			hv = new HelpView();
		}
		return hv;
	}
	
	/**
	 * Constructor
	 */
	public HelpView() {
		hv = this;
		try{
			ClassLoader cl = HelpView.class.getClassLoader();
			URL url = HelpSet.findHelpSet(cl,"Master.hs");
			hs= new HelpSet(null,url);
			hb = hs.createHelpBroker();
			/*JButton jbHelp = new JButton("help!");
			jbHelp.addActionListener(new CSH.DisplayHelpFromSource(hb));
			add(jbHelp);*/
			//CSH.setHelpIDString(this.getRootPane(),"main");
			//hb.showID("main");
		}
		catch(Exception e){
			Log.error(e); 
		}
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Help view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.HelpView";
	}

}
