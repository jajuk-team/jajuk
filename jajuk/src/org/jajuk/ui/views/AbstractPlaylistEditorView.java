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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;

import layout.TableLayout;

import org.jajuk.util.Util;


/**
 * Adapter for playlists editors 
 *  * <p>Singleton
 * @author     bflorat
 * @created   29 dec. 2003
 */
public abstract class AbstractPlaylistEditorView extends ViewAdapter {

	
	JPanel jpControl;
		JToolBar jtb;
			JButton jbRun;
			JButton jbSave;
			JButton jbAdd;
			JButton jbRemove;
			JButton jbUp;
			JButton jbDown;
			JButton jbCurrent;
			JButton jbClear;
	JTable jtable;
	
	/**
	 * Constructor
	 */
	public AbstractPlaylistEditorView() {
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void display(){
		//Control panel
		jpControl = new JPanel();
		jtb = new JToolBar();
		jtb.setFloatable(false);
		jpControl.setBorder(BorderFactory.createEtchedBorder());
		int iXspace = 0;
		double sizeControl[][] =
			{{iXspace,0.50,iXspace},
			{25,0.99}};
		jpControl.setLayout(new TableLayout(sizeControl));
		jbRun = new JButton(Util.getIcon(ICON_RUN));
		jbRun.setToolTipText("Run this playlist");
		jbSave = new JButton(Util.getIcon(ICON_SAVE));
		jbSave.setToolTipText("Save this playlist");
		jbAdd = new JButton(Util.getIcon(ICON_ADD));
		jbAdd.setToolTipText("Add an item to this playlist");
		jbRemove = new JButton(Util.getIcon(ICON_REMOVE));
		jbRemove.setToolTipText("Remove an item from this playlist");
		jbUp = new JButton(Util.getIcon(ICON_UP));
		jbUp.setToolTipText("Set item position higher");
		jbDown = new JButton(Util.getIcon(ICON_DOWN));
		jbDown.setToolTipText("Set item position lower");
		jbCurrent = new JButton(Util.getIcon(ICON_CURRENT_PLAYLIST));
		jbCurrent.setToolTipText("Display current played playlist");
		jbClear = new JButton(Util.getIcon(ICON_CLEAR));
		jbClear.setToolTipText("Clear this playlist");
		jtb.add(jbRun);
		jtb.add(jbSave);
		jtb.add(jbAdd);
		jtb.add(jbRemove);
		jtb.add(jbUp);
		jtb.add(jbDown);
		jtb.add(jbCurrent);
		jtb.add(jbClear);
		jpControl.add(jtb,"1,0");
		//table
		jtable = new JTable();
		double size[][] =
		{{0.99},
		{30,0.99}};
		setLayout(new TableLayout(size));
		add(jpControl,"0,0");
		add(new JScrollPane(jtable),"0,1");
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "Playlist editor view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public abstract String getViewName();

	
}
