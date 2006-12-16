/**
 *  WHO:   John L. Moreland modified for jajuk by Bertrand Florat
 *
 *  WHAT:  Detect.js
 *
 *  WHY:   Detect the web browser, OS, and existance of Java.
 *
 *  WHERE: San Diego Supercomputer Center
 *
 *  WHEN:  Tue Feb 24 13:26:50 PST 2004
 *
 *  HOW:   JavaScript
 */


/* ***** BEGIN LICENSE BLOCK *****
 * Licensed under Version: <a href="http://java.sun.com/products/javawebstart/1.2/docs/MPL-1.1.html">MPL 1.1</a>/GPL 2.0/LGPL 2.1
 * Full Terms at http://devedge.netscape.com/lib/js/license/mpl-tri-license.txt
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Netscape code.
 *
 * The Initial Developer of the Original Code is
 * Netscape Corporation.
 * Portions created by the Initial Developer are Copyright (C) 2001
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Bob Clary <bclary @netscape.com="">
 *
 * ***** END LICENSE BLOCK ***** */
function DetectBrowser()
{
	var oldOnError = window.onerror;
	var element = null;

	window.onerror = null;
	
	// work around bug in xpcdom Mozilla 0.9.1
	window.saveNavigator = window.navigator;

	navigator.OS		= '';
	navigator.version	= parseFloat(navigator.appVersion);
	navigator.org		= '';
	navigator.family	= '';

	var platform;
	if (typeof(window.navigator.platform) != 'undefined')
	{
		platform = window.navigator.platform.toLowerCase();
		if (platform.indexOf('win') != -1)
			navigator.OS = 'windows';
		else if (platform.indexOf('mac') != -1)
			navigator.OS = 'mac';
		else if (platform.indexOf('unix') != -1 || platform.indexOf('linux') != -1 || platform.indexOf('sun') != -1)
			navigator.OS = 'unix';
	}

	var i = 0;
	var ua = window.navigator.userAgent.toLowerCase();
	
	if (ua.indexOf('opera') != -1)
	{
		i = ua.indexOf('opera');
		navigator.family	= 'opera';
		navigator.org		= 'opera';
		navigator.version	= parseFloat('0' + ua.substr(i+6), 10);
	}
	else if ((i = ua.indexOf('msie')) != -1)
	{
		navigator.org		= 'microsoft';
		navigator.version	= parseFloat('0' + ua.substr(i+5), 10);
		
		if (navigator.version < 4)
			navigator.family = 'ie3';
		else
			navigator.family = 'ie4'
	}
	else if (ua.indexOf('gecko') != -1)
	{
		navigator.family = 'gecko';
		var rvStart = ua.indexOf('rv:');
		var rvEnd	 = ua.indexOf(')', rvStart);
		var rv			= ua.substring(rvStart+3, rvEnd);
		var rvParts = rv.split('.');
		var rvValue = 0;
		var exp		 = 1;

		for (var i = 0; i < rvParts.length; i++)
		{
			var val = parseInt(rvParts[i]);
			rvValue += val / exp;
			exp *= 100;
		}
		navigator.version = rvValue;

		if (ua.indexOf('netscape') != -1)
			navigator.org = 'netscape';
		else if (ua.indexOf('compuserve') != -1)
			navigator.org = 'compuserve';
		else
			navigator.org = 'mozilla';
	}
	else if ((ua.indexOf('mozilla') !=-1) && (ua.indexOf('spoofer')==-1) && (ua.indexOf('compatible') == -1) && (ua.indexOf('opera')==-1)&& (ua.indexOf('webtv')==-1) && (ua.indexOf('hotjava')==-1))
	{
		var is_major = parseFloat(navigator.appVersion);
		
		if (is_major < 4)
			navigator.version = is_major;
		else
		{
			i = ua.lastIndexOf('/')
			navigator.version = parseFloat('0' + ua.substr(i+1), 10);
		}
		navigator.org = 'netscape';
		navigator.family = 'nn' + parseInt(navigator.appVersion);
	}
	else if ((i = ua.indexOf('aol')) != -1 )
	{
		// aol
		navigator.family	= 'aol';
		navigator.org		= 'aol';
		navigator.version	= parseFloat('0' + ua.substr(i+4), 10);
	}
	else if ((i = ua.indexOf('hotjava')) != -1 )
	{
		// hotjava
		navigator.family	= 'hotjava';
		navigator.org		= 'sun';
		navigator.version	= parseFloat(navigator.appVersion);
	}

	window.onerror = oldOnError;
}


/**
 *  Detect the existance of Java.
 */
function DetectJava()
{
	jwsInstalled = 0;
	jws150Installed = 0;
	isIE = "false";

	if ( navigator.mimeTypes && navigator.mimeTypes.length )
	{
		x = navigator.mimeTypes['application/x-java-jnlp-file'];
		if ( x ) jwsInstalled = 1;
	}
	else
	{
		isIE = "true";

		jnlp = 'application/x-java-jnlp-file';
		result = false;
		document.write( '<SCRIPT LANGUAGE=VBScript>\n on error resume next \n result = IsObject(CreateObject("JavaWebStart.isInstalled"))</SCRIPT>\n' );
		if (result)
			jwsInstalled = 1;

		result = false;
		document.write( '<SCRIPT LANGUAGE=VBScript>\n on error resume next \n result = IsObject(CreateObject("JavaWebStart.isInstalled.1.5.0.0"))</SCRIPT>\n' );
		if (result)
			jws150Installed = 1;
	}
}


/**
 *  MAIN
 */

DetectBrowser( );
DetectJava( );

document.write( "<P>" );
document.write( "-> It looks like you are using the " + navigator.org + " " );
document.write( navigator.family + " browser version " );
document.write( navigator.version + " under " + navigator.OS + ". \n" );

if ((jws150Installed && jwsInstalled) || (navigator.userAgent.indexOf("Gecko") !=-1)) {
    document.write("<br><br><a href=\"http://82.239.70.252:8001/jajuk-notest/app\"><img style=\"width: 300px; height: 37px;\" alt=\"Web start me now !\" src=\"./images/webstartmenow.png\"></a>");
} else {
    document.write("Click ");
    document.write("<a href=http://java.sun.com/PluginBrowserCheck?pass=http://jajuk.sourceforge.net/jreinstall.html&fail=http://java.sun.com/j2se/1.5.0/download.html>here</a> ");
    document.write("to download and install JRE 5.0 and the application.");
}