/*
 *  Jajuk
 *  Copyright (C) 2007 The Jajuk Team
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

package org.jajuk.services.lastfm;

import org.jajuk.base.Event;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.DownloadManager;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;

import java.util.HashSet;
import java.util.Set;

import com.melloware.jintellitype.Main;

import ext.services.network.Proxy;
import ext.services.network.ProxyBean;

/**
 * LastFM Manager, handle file launch events to submit informations among others
 * things
 * <p>
 * singleton
 * </p>
 */
public class LastFmManager implements Observer, ITechnicalStrings {
	/** Self instance */
	private static LastFmManager self;

	private LastFmManager() {
		// Register on the list for subject we are interested in
		ObservationManager.register(this);
		// Display an hideable message to user if audioscrobber is disable
		// Show this message only one time by jajuk session
		if (!ConfigurationManager.getBoolean(CONF_AUDIOSCROBBLER_ENABLE)
				//don't dhow this message if first jajul launch: already too many popups
				&& !org.jajuk.Main.bFirstSession) {
			Messages.showHideableWarningMessage(Messages.getString("LastFmManager.0"),
					CONF_NOT_SHOW_AGAIN_LASTFM_DISABLED);
		}
	}

	static public LastFmManager getInstance() {
		if (self == null) {
			self = new LastFmManager();
		}
		return self;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#getRegistrationKeys()
	 */
	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_FILE_LAUNCHED);
		return eventSubjectSet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.base.Observer#update(org.jajuk.base.Event)
	 */
	public void update(Event event) {
		if (EventSubject.EVENT_FILE_LAUNCHED == event.getSubject()) {
			if (ConfigurationManager.getBoolean(CONF_AUDIOSCROBBLER_ENABLE)){
			String sLogin = ConfigurationManager.getProperty(CONF_AUDIOSCROBBLER_USER);
			String sPwd = ConfigurationManager.getProperty(CONF_AUDIOSCROBBLER_PASSWORD);
			String sProxyUrl = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME);
		/*	int port = ConfigurationManager.getInt(CONF_NETWORK_PROXY_PORT);
			String sProxyLogin = ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN);
			String sProxyPwd = DownloadManager.getProxyPwd();
			Proxy proxy = new Proxy(ProxyBean.HTTP_PROXY,sProxyUrl,port,sProxyPwd);*/
			}
		}
	}

}
