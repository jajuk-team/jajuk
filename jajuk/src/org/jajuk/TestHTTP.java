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

package org.jajuk;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 *  Type description
 *
 * @author     bflorat
 * @created    19 août 2004
 */
public class TestHTTP {
    
	private static HttpClient getHTTPClient(String sProxyUser,String sProxyPassswd){
		HttpClient client = new HttpClient();
		client.setConnectionTimeout(10000);
		client.getState().setAuthenticationPreemptive(true);
		if (sProxyUser!= null && sProxyPassswd!= null){
			client.getState().setProxyCredentials(null,null, new UsernamePasswordCredentials(sProxyUser,sProxyPassswd)  );
		}
		return client;
	}
	
	/**
	 * @return Get an HTTP 
	 */
	private static HttpClient getHTTPClient(){
		return getHTTPClient(null,null);
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
			host.setProxy(sProxyURL,iProxyPort);
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
	
	public static void main(String[] args) {
        
        try{
	        HttpClient client = getHTTPClient();
        	String sSearchUrl = "http://images.google.com/images?q="+URLEncoder.encode(args[0], "ISO-8859-1")+"&ie=ISO-8859-1&hl=en&btnG=Google+Search";
	        System.out.println("Search URL: "+sSearchUrl);
	        GetMethod get = new GetMethod(sSearchUrl);
	        get.addRequestHeader(new Header("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.03  [en]"));
	        get.setDoAuthentication( true );
	        int status = client.executeMethod(getHostConfiguration("images.google.com"), get );
	        String sRes = get.getResponseBodyAsString();
	        get.releaseConnection();
	        ArrayList alResults = new ArrayList(1000);
	        String[] strings = sRes.split("imgurl=");
	        for (int i=1;i<strings.length;i++){
	        	String s = strings[i].substring(0,strings[i].indexOf('&'));
	        	s = s.replaceAll("%2520","%20");
	        	System.out.println("Downloading : "+s);
	        	alResults.add(s);
	        	HttpURL url = new HttpURL(s);
	        	GetMethod get2 = new GetMethod(s);
	        	get2.addRequestHeader(new Header("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows 2000) Opera 6.03  [en]"));
	        	get2.setDoAuthentication( true );
	        	status = client.executeMethod(getHostConfiguration(url.getHost()), get2 );
	        	String sFile = URLDecoder.decode(url.getName(),"ISO-8859-1");
	        	String sFileName = "d:/test/"+sFile;
	        	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(sFileName,false));
	        	byte[] bytes = get2.getResponseBody();
	        	bos.write(bytes);
	        	bos.flush();
	        	bos.close();
	        	get2.releaseConnection();
	        	System.out.println(new ImageIcon(sFileName).getImageLoadStatus());
	        }
	        
	    }
	    catch(Exception e){
	        e.printStackTrace();
	    }
	    
    }
}
