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

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.jajuk.util.Util;
import org.jajuk.util.log.Log;

/**
 *  View used to show the Jajuk about and contributors. 
 * <p>Help perspective
 *  * <p>Singleton
 * @author     bflorat
 * @created   22 dec. 2003
 */
public class AboutView extends ViewAdapter {

	/**Self instance*/
	private static AboutView av;
	
	/**Text*/
	private JEditorPane jep;
	
		
	/**Return self instance*/
	public static AboutView getInstance(){
		if (av == null){
			av = new AboutView();
		}
		return av;
	}
	
	/**
	 * Constructor
	 */
	public AboutView() {
		av = this;
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		try{
			jep = new JEditorPane("text/html",Util.readFile(FILE_ABOUT).toString());
		}
		catch(Exception e){
			Log.error(e);
		}
		add(new JScrollPane(jep));
		
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "About view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.AboutView";
	}

}
