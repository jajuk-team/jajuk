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

package org.jajuk.util;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jajuk.Main;
import org.jajuk.base.ITechnicalStrings;
import org.jajuk.i18n.Messages;
import org.jajuk.util.log.Log;

/**
 *  Manages network downloads
 *
 * @author     bflorat
 * @created    29 août 2004
 */
public class DownloadManager implements ITechnicalStrings {

    /**Http client**/
	private static HttpClient client = null;
    
	
	/**Number of active connections ( should be 0 or 1, multiple connections are not allowed to avoid out of memory errors and dead locks*/
	private static int iCon = 0;
	
	/**
	 * @param sProxyUser
	 * @param sProxyPassswd
	 * @return an HTTP client
	 */
	private static HttpClient getHTTPClient(String sProxyUser,String sProxyPassswd,int iConTimeout,int iDataTimeout){
		HttpClient client = new HttpClient();
		client.setConnectionTimeout(iConTimeout); //connection to
		client.setTimeout(iDataTimeout); //data receptino timeout
		client.getState().setAuthenticationPreemptive(true);
		if (sProxyUser!= null && sProxyPassswd!= null){
			client.getState().setProxyCredentials(null,null, new UsernamePasswordCredentials(sProxyUser,sProxyPassswd)  );
		}
		return client;
	}
	
	/**
	 * @return Get an HTTP client
	 */
	private static HttpClient getHTTPClient(int iConTimeout,int iDataTimeout){
		return getHTTPClient(null,null,iConTimeout,iDataTimeout);
	}
	
	
	/**
	 * @param sHostname, the host name
	 * @param sProxyURL The proxy port, null if you don't use a proxy
	 * @param iProxyPort, proxy port if you use one of -1 if not
	 * @return An host configuration
	 */
	private static HostConfiguration getHostConfiguration(String sHostname,String sProxyURL,int iProxyPort){
		HostConfiguration host = new HostConfiguration();
		host.setHost(sHostname);
		if (sProxyURL != null && iProxyPort>0){
			host.setProxy(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME),ConfigurationManager.getInt(CONF_NETWORK_PROXY_PORT));
		}
		return host;
	}
	
	/**
	 * @param sHostname, the host name
	 * @return Get an host configuration without proxy
	 */
	private static HostConfiguration getHostConfiguration(String sHostname){
		return getHostConfiguration(sHostname,null,-1);
	}
	
	/**
	 * @param search
	 * @return a list of urls
	 */
	public static ArrayList getRemoteCoversList(String search){
	    ArrayList alOut = new ArrayList(20); //URL list   
	    try{
	        if ( client == null){
	            if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)){
	                client = getHTTPClient(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN),DownloadManager.getProxyPwd(),3000,7000);
	            }
	            else{
	                client = getHTTPClient(3000,7000);
	            }
	        }
	        String sSearchUrl = "http://images.google.com/images?q="+URLEncoder.encode(search, "ISO-8859-1")+"&ie=ISO-8859-1&hl=en&btnG=Google+Search";
	        Log.debug("Search URL: "+sSearchUrl);
	        GetMethod get = new GetMethod(sSearchUrl);
	        get.addRequestHeader(new Header("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.03  [en]"));
	        get.setDoAuthentication( true );
	        int status = client.executeMethod(getHostConfiguration("216.239.39.99"), get );
	        if (status != HttpStatus.SC_OK){ //should be 200
	          return alOut;  
	        }
	        String sRes = get.getResponseBodyAsString();
	        get.releaseConnection();
	        String[] strings = sRes.split("imgurl=");
	        for (int i=1;i<strings.length;i++){
	            String s = strings[i].substring(0,strings[i].indexOf('&'));
	            s = s.replaceAll("%2520","%20");
	            alOut.add(new URL(s));
	        }
	    }
	    catch(Exception e){
	        Log.error(e);
	    }
	    return alOut;
	}
	
    
	/**
	 * 
	 * @return the required proxy pwd
	 */
    public static String getProxyPwd(){
        return JOptionPane.showInputDialog(Main.getWindow(),Messages.getString("DownloadManager.0"),Messages.getString("DownloadManager.1"),JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    
}
