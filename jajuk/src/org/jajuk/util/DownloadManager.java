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

package org.jajuk.util;

import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;

/**
 *  Manages network downloads
 *
 * @author     Bertrand Florat
 * @created    29 août 2004
 */
public class DownloadManager implements ITechnicalStrings {

     	
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
			client.getState().setProxyCredentials(null,"proxy", new UsernamePasswordCredentials(sProxyUser,sProxyPassswd)  ); //$NON-NLS-1$
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
	public static ArrayList getRemoteCoversList(String search) throws JajukException{
		ArrayList alOut = new ArrayList(20); //URL list   
	    try{
	        String sSearchUrl = "http://images.google.com/images?q="+URLEncoder.encode(search, "ISO-8859-1")+"&ie=ISO-8859-1&hl=en&btnG=Google+Search"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        Log.debug("Search URL: "+sSearchUrl); //$NON-NLS-1$
	        byte[] bRes = download(new URL(sSearchUrl));
	        if (bRes == null || bRes.length==0){
	            return alOut;
	        }
	        String sRes = new String(bRes);
	        //get urls
	        String[] strings = sRes.split("imgurl="); //$NON-NLS-1$
	        for (int i=1;i<strings.length;i++){
	            String s = strings[i].substring(0,strings[i].indexOf('&')); 
	            s = s.replaceAll("%2520","%20"); //$NON-NLS-1$ //$NON-NLS-2$
	            alOut.add(new URL(s));
	        }
	        //get sizes
	        strings = sRes.split("pixels - "); //$NON-NLS-1$
	        int iNbRemove = 0; //removes number used to compute new index
	        for (int i=1;i<strings.length;i++){
	            int iKPos = strings[i].indexOf('k');
	            int iSize = Integer.parseInt(strings[i].substring(0,iKPos));
	            if ( iSize > ConfigurationManager.getInt(CONF_COVERS_MAX_SIZE) ||  iSize > MAX_COVER_SIZE || iSize < ConfigurationManager.getInt(CONF_COVERS_MIN_SIZE) ){
	                alOut.remove(i-(1+iNbRemove));
	                iNbRemove++;
	            }
	        }
	    }
	    catch(JajukException je){  //concurrent exception 
	        throw je;
	    }
	    catch(Exception e){
	        Log.debug(e.toString());
	    }
	    return alOut;
	}
	
	/**
	 * Download the resource at the given url
	 * @param url to download
	 * @throws JajukException if a concurrent connection is alive
	 * @return result as an array of bytes, null if a problem occured
	 */
	public static byte[] download(URL url) throws JajukException{
	    byte[] bOut = null;
	    GetMethod get = null;
	    try{
	        HttpClient client = null;
	        int iConTO = 1000*ConfigurationManager.getInt(CONF_NETWORK_CONNECTION_TO);
	        int iTraTO =  1000*ConfigurationManager.getInt(CONF_NETWORK_TRANSFERT_TO);
	        if (ConfigurationManager.getBoolean(CONF_NETWORK_USE_PROXY)){
	            client = getHTTPClient(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_LOGIN),DownloadManager.getProxyPwd(),iConTO,iTraTO);
	        }
	        else{
	            client = getHTTPClient(iConTO,iTraTO);
	        }
	        get = new GetMethod(url.toString());
	        get.addRequestHeader(new Header("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.03  [en]")); //$NON-NLS-1$ //$NON-NLS-2$
	        get.setDoAuthentication( true );
	        int status = client.executeMethod(getHostConfiguration(url.getHost()), get );
	        bOut = get.getResponseBody();
	     }
	    catch(Exception e){
	        e.printStackTrace();
	        Log.debug("Time out during cover lookup"); //$NON-NLS-1$
	    	throw new JajukException("129"); //mainly time outs //$NON-NLS-1$
	    }
	    finally{
	        get.releaseConnection();
	    }
	    return bOut;
	}
	
    
	/**
	 * 
	 * @return the required proxy pwd
	 */
    public static String getProxyPwd(){
        return JOptionPane.showInputDialog(Main.getWindow(),Messages.getString("DownloadManager.0"),Messages.getString("DownloadManager.1"),JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
    }
   
}
