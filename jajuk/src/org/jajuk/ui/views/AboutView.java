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

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import org.jajuk.i18n.Messages;
import org.jajuk.util.Util;
import org.jfree.ui.about.AboutPanel;
import org.jfree.ui.about.Contributor;
import org.jfree.ui.about.ContributorsPanel;
import org.jfree.ui.about.Licences;
import org.jfree.ui.about.SystemPropertiesPanel;

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
	
	/**Licence panel*/
	private JPanel jpLicence;
	
	/**General informations panel*/
	private AboutPanel ap;
	
	/**Contributors panel*/
	private ContributorsPanel cp;
	
	/**JVM properties panel*/
	private SystemPropertiesPanel spp;
	
	/**Tabbed pane with previous panels*/
	private JTabbedPane jtp;
	
	/**Additional informations */
	private static final String INFOS = "<html><a href='http://jajuk.sourceforge.net'>http://jajuk.sourceforge.net</a></html>"; //$NON-NLS-1$
	
	/**Return self instance*/
	public static synchronized AboutView getInstance(){
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
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		//licence panel
		jpLicence = new JPanel(new BorderLayout());
        JTextArea jta = new JTextArea(Licences.getInstance().getGPL());
        jta.setLineWrap(true);
        jta.setWrapStyleWord(true);
        jta.setCaretPosition(0);
        jta.setEditable(false);
        jpLicence.add(new JScrollPane(jta));
		jtp = new JTabbedPane();
		ArrayList alContribs = new ArrayList(10);
		alContribs.add(new Contributor("Bertrand Florat","bertrand@florat.net")); //$NON-NLS-1$ //$NON-NLS-2$
		alContribs.add(new Contributor("Gerhard Dietrichsteiner","skyreacher@users.sourceforge.net")); //$NON-NLS-1$ //$NON-NLS-2$
		alContribs.add(new Contributor("Sébastien Gringoire","")); //$NON-NLS-1$ //$NON-NLS-2$
		cp = new ContributorsPanel(alContribs);
		JPanel jpAbout = new JPanel();
		jpAbout.setLayout(new BoxLayout(jpAbout,BoxLayout.Y_AXIS));
		ap = new AboutPanel("Jajuk",JAJUK_VERSION,"<html>Copyright 2003,2004<br>Bertrand Florat & Jajuk team</html>",INFOS,Util.getIcon(ICON_LOGO).getImage()); //$NON-NLS-1$ //$NON-NLS-2$
		jpAbout.add(ap);
		jpAbout.add(cp);
		jpAbout.add(Box.createVerticalGlue());
		spp = new SystemPropertiesPanel();
		jtp.addTab(Messages.getString("AboutView.7"),jpAbout); //$NON-NLS-1$
		jtp.addTab(Messages.getString("AboutView.8"),jpLicence); //$NON-NLS-1$
		jtp.addTab(Messages.getString("AboutView.9"),spp); //$NON-NLS-1$
		add(jtp);
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return Messages.getString("AboutView.10");	 //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.AboutView"; //$NON-NLS-1$
	}

}
