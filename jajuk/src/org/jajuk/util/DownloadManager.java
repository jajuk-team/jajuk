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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.log.Log;

/**
 *  Manages network downloads
 *
 * @author     Bertrand Florat
 * @created    29 août 2004
 */
public class DownloadManager implements ITechnicalStrings {

    /**Proxy pwd*/
    private static String sProxyPwd = null;
     	
	/**
	 * @param sProxyUser
	 * @param sProxyPassswd
	 * @return an HTTP client
	 */
	private static HttpClient getHTTPClient(String sProxyUser,String sProxyPassswd,int iConTimeout,int iDataTimeout){
		HttpClient client = new HttpClient();
		client.getHttpConnectionManager().getParams().setConnectionTimeout(iConTimeout); //connection to
		client.getHttpConnectionManager().getParams().setSoTimeout(iDataTimeout); //data reception timeout
		if (sProxyUser!= null && sProxyPassswd!= null){
		    client.getHostConfiguration().setProxy(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_HOSTNAME),Integer.parseInt(ConfigurationManager.getProperty(CONF_NETWORK_PROXY_PORT))); 
		    client.getState().setProxyCredentials(new AuthScope(AuthScope.ANY),new UsernamePasswordCredentials(sProxyUser,sProxyPwd ));
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
    public static ArrayList getRemoteCoversList(String search) throws Exception{
        ArrayList alOut = new ArrayList(20); //URL list   
        String sSearchUrl = "http://images.google.com/images?q="+URLEncoder.encode(search, "ISO-8859-1")+"&ie=ISO-8859-1&hl=en&btnG=Google+Search"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Log.debug("Search URL: "+sSearchUrl); //$NON-NLS-1$
        byte[] bRes = download(new URL(sSearchUrl),false);
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
        return alOut;
    }
    
	/**
	 * Download the resource at the given url
	 * @param url to download
     * @param Use cache : store file in image cache
	 * @throws Exception
	 * @return result as an array of bytes, null if a problem occured
	 */
	public static byte[] download(URL url,boolean bUseCache) throws Exception{
        byte[] bOut = null;
	    //check if file is not already downloaded or being downloaded
        if (bUseCache){
            if (new File(Util.getCachePath(url)).exists()){
                return bOut;
            }
        }
	    GetMethod get = null;
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
	    get.addRequestHeader("Accept","image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*"); //$NON-NLS-1$ //$NON-NLS-2$
	    get.addRequestHeader("Accept-Language","en-us"); //$NON-NLS-1$ //$NON-NLS-2$
	    get.addRequestHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 5.01; Windows NT 5.0)"); //$NON-NLS-1$ //$NON-NLS-2$
	    get.addRequestHeader("Connection","Keep-Alive"); //$NON-NLS-1$ //$NON-NLS-2$
	    int status = client.executeMethod(get);
	    if (bUseCache){
            
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(Util.getCachePath(url)));
            BufferedInputStream bis = new BufferedInputStream(get.getResponseBodyAsStream());
            int i;
            while((i = bis.read()) != -1) {
                bos.write(i);
            }
            bos.close();
            bis.close();
        }
        else{
            bOut = get.getResponseBody();
        }
	    if (get != null && get.isRequestSent()){
	        get.releaseConnection();
	    }
	    return bOut;
    }
    
	/**
	 * @return the required proxy pwd
	 */
	public static String getProxyPwd(){
	    if (sProxyPwd == null || sProxyPwd.trim().equals("")){ //$NON-NLS-1$
	        sProxyPwd = JOptionPane.showInputDialog(Main.getWindow(),Messages.getString("DownloadManager.0"),Messages.getString("DownloadManager.1"),JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    return sProxyPwd;
	}
   
}
