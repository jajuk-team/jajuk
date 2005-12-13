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

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.event.ComponentListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.browser.WebBrowser;

/**
 *  Wikipedia view 
 *
 * @author     Bertrand Florat
 * @created    12/12/2005
 */
public class WikipediaView extends ViewAdapter implements ITechnicalStrings,Observer,ComponentListener {
	
	//control panel
    JPanel jpControl;
    JLabel jlLanguage;
    JComboBox jcbLanguage;
    
    WebBrowser browser;
    
    /**
     * Constructor
     *
     */
	public WikipediaView(){
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#getID()
	 */
	public String getID() {
		return getClass().getName(); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#getDesc()
	 */
	public String getDesc() {
		return "AnimationView.0"; //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.views.IView#populate()
	 */
	public void populate() {
	        //Control panel
        jpControl = new JPanel();
        jpControl.setBorder(BorderFactory.createEtchedBorder());
        int iXspace = 10;
        double sizeControl[][] =
            //        Language by                       combo lang                                       
        {{3*iXspace,TableLayout.PREFERRED,iXspace,TableLayout.PREFERRED,3*iXspace},
                {25}};
        jpControl.setLayout(new TableLayout(sizeControl));
        jlLanguage = new JLabel(Messages.getString("WikipediaView.1"));
        jcbLanguage = new JComboBox();
        for (String s:Messages.getInstance().getDescs()){
            jcbLanguage.addItem(Messages.getString(s));
        }
        jcbLanguage.setEditable(false);
        
        jpControl.add(jlLanguage,"1,0");//$NON-NLS-1$
        jpControl.add(jcbLanguage,"3,0");//$NON-NLS-1$
        
        //global layout
        double size[][] =
        {{0.99},
                {30,10,0.99}};
        setLayout(new TableLayout(size));
        try {
            browser = new WebBrowser(new URL("file:///tmp/index.html"));
        }
        catch (MalformedURLException e) {
            Log.error(e);
        }
       
        add(jpControl,"0,0"); //$NON-NLS-1$
        add(browser,"0,2");
        
        //subscriptions to events
        ObservationManager.register(EVENT_FILE_LAUNCHED,this);
    
    }
	
		
	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(Event event) {
		String subject = event.getSubject();
		if (subject.equals(EVENT_FILE_LAUNCHED)){
		}
		else if (subject.equals(EVENT_ZERO)){
		}
	}
	
}
