/*
 *  Jajuk
 *  Copyright (C) 2003 The Jajuk Team
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
package org.jajuk.i18n;

import java.util.Collections;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.Main;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Utility class to get strings from localized property files
 * <p>
 * Singleton
 * </p>
 */
public class Messages extends DefaultHandler implements ITechnicalStrings {
	/** Local ( language) to be used, default is English */
	private String sLocal = "en";

	/** Supported Locals */
	public ArrayList<String> alLocals = new ArrayList<String>(10);

	/** self instance for singleton */
	private static Messages mesg;

	/**
	 * Messages themselves extracted from an XML file to this properties class*
	 */
	private Properties properties;

	/** english messages used as default* */
	private Properties propertiesEn;

	/**
	 * Private Constructor
	 */
	private Messages() {
	}

	/**
	 * @return Singleton instance
	 */
	public static Messages getInstance() {
		if (mesg == null) {
			mesg = new Messages();
		}
		return mesg;
	}

	/**
	 * 
	 * @param sKey
	 * @return wheter given key exists
	 */
	public boolean contains(String sKey) {
		return getPropertiesEn().containsKey(sKey);
	}

	/**
	 * @param key
	 * @return
	 */
	public static String getString(String key) {
		String sOut = key;
		try {
			sOut = getInstance().getProperties().getProperty(key);
			if (sOut == null) { // this property is unknown for this local, try
				// in english
				sOut = getInstance().getPropertiesEn().getProperty(key);
			}
			// at least, returned property is the key name but we trace an
			// error to show it
			if (sOut == null) {
				Log.error(105, "key: " + key, new Exception());
				sOut = key;
			}
		} catch (Exception e) { // system error
			Log.error(e);
		}
		return sOut;
	}

	/**
	 * Fetch all messages from a given base key. <P/> Example:
	 * 
	 * <pre>
	 *     example.0=Message 1
	 *     example.1=Message 2
	 *     example.2=Message 3
	 * </pre>
	 * 
	 * Using <tt>Messages.getAll("example");</tt> will return a size 3 String
	 * array containing the messages in order. <P/> The keys need to have
	 * continuous numbers. So, adding <tt>example.5=Message 5</tt> to the
	 * bundle, will not result in adding it to the array without first adding
	 * <tt>example.3</tt> and <tt>example.4</tt>.
	 * 
	 * @param base
	 *            The base to use for generating the keys.
	 * @return An array of Strings containing the messages linked to the key,
	 *         never <tt>null</tt>. If <tt>base.0</tt> is not found, and
	 *         empty array is returned.
	 */
	public static String[] getAll(String base) {
		List<String> msgs = new ArrayList<String>();
		try {
			for (int i = 0;; i++) {
				String sOut = getInstance().getProperties().getProperty(base + "." + i);

				if (sOut == null) {
					// this property is unknown for this
					// local, try in english
					sOut = getInstance().getPropertiesEn().getProperty(base + "." + i);
				}

				// Property not found, assume we found all properties in the set
				if (sOut == null) {
					break;
				}

				msgs.add(sOut);
			}
		} catch (Exception e) { // System error
			Log.error(e);
		}
		return msgs.toArray(new String[msgs.size()]);
	}

	/**
	 * Register a local
	 * 
	 * @param sLocale :
	 *            standard local name like "en"
	 * @param sDesc :
	 *            a language-independent descriptions like "Language_desc_en"
	 */
	public void registerLocal(String sLocal) {
		alLocals.add(sLocal);
	}

	/**
	 * Return list of available locals
	 * 
	 * @return
	 */
	public static ArrayList<String> getLocales() {
		return mesg.alLocals;
	}

	/**
	 * Return list of available local descriptions
	 * 
	 * @return
	 */
	public static ArrayList<String> getDescs() {
		ArrayList<String> alDescs = new ArrayList<String>(10);
		for (String local : mesg.alLocals)
			alDescs.add(getString("Language_desc_" + local));
		Collections.sort(alDescs);
		return alDescs;
	}

	/**
	 * Return Description for a given local id
	 * 
	 * @return localized description
	 */
	public static String getDescForLocal(String sLocal) {
		return getString("Language_desc_" + sLocal);
	}

	/**
	 * Return local for a given description
	 * 
	 * @return local
	 */
	public static String getLocalForDesc(String sDesc) {
		for (int i = 0; i < getLocales().size(); i++)
			if (getDescForLocal(mesg.alLocals.get(i)).equals(sDesc))
				return getLocales().get(i);
		return null;
	}

	/**
	 * Change current local
	 * 
	 * @param sLocal
	 */
	public void setLocal(String sLocal) throws Exception {
		ConfigurationManager.setProperty(CONF_OPTIONS_LANGUAGE, sLocal);
		this.properties = null; // make sure to reinitialize cached strings
		this.sLocal = sLocal;
	}

	/***************************************************************************
	 * Parse a fake properties file inside an XML file as CDATA
	 * 
	 * @param sLocal
	 * @return a properties with all entries
	 * @throws Exception
	 */
	private Properties parseLangpack(String sLocal) throws Exception {
		final Properties properties = new Properties();
		// Choose right jajuk_<lang>.properties file to load
		StringBuffer sbFilename = new StringBuffer(FILE_LANGPACK_PART1);
		if (!sLocal.equals("en")) { // for english, properties file is
			// simply jajuk.properties
			sbFilename.append('_').append(sLocal);
		}
		sbFilename.append(FILE_LANGPACK_PART2);
		URL url;
		// property file URL, either in the jajuk.jar jar
		// (normal execution) or found as regular file if in
		// development debug mode
		url = Util.getResource("org/jajuk/i18n/" + sbFilename.toString());
		// parse it, actually it is a big properties file as CDATA in an XML
		// file
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(false);
			spf.setNamespaceAware(false);
			SAXParser saxParser = spf.newSAXParser();
			saxParser.parse(url.openStream(), new DefaultHandler() {
				// this buffer will contain the entire properties strings
				StringBuffer sb = new StringBuffer(15000);

				// call for each element strings, actually will be called
				// several time if the element is large (our case : large CDATA)
				public void characters(char[] ch, int start, int length) throws SAXException {
					sb.append(ch, start, length);
				}

				// call when closing the tag (</body> in our case )
				public void endElement(String uri, String localName, String qName)
						throws SAXException {
					String sWhole = sb.toString();
					// ok, parse it ( comments start with #)
					StringTokenizer st = new StringTokenizer(sWhole, "\n");
					while (st.hasMoreTokens()) {
						String sLine = st.nextToken();
						if (sLine.length() > 0 && !sLine.startsWith("#")
								&& sLine.indexOf('=') != -1) {
							StringTokenizer stLine = new StringTokenizer(sLine, "=");
							// get full value after the '=', we don't use the
							// stringtokenizer to allow
							// using = characters in the value
							String sValue = sLine.substring(sLine.indexOf('=') + 1);
							// trim to ignore space at begin end end of lines
							properties.put(stLine.nextToken().trim(), sValue);
						}
					}
				}
			});
			return properties;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Return the message display to the user corresponding to the error code.
	 * 
	 * @param code
	 *            Error code.
	 * @return String Message corresponding to the error code.
	 */
	public static String getErrorMessage(int code) {
		String sOut = Integer.toString(code);
		try {
			sOut = getString("Error." + Util.padNumber(code, 3));
		} catch (Exception e) {
			System.out.println("### Error getting error message for code: " + code);
		}
		return sOut;
	}

	/**
	 * Show a dialog waiting for a user decision
	 * <p>
	 * CAUTION! the thread which calls this method musn't have locks on
	 * resources : otherwise it can conduct to GUI freeze
	 * </p>
	 * 
	 * @param sText :
	 *            dialog text
	 * @param iType :
	 *            dialog type : can be JOptionPane.ERROR_MESSAGE,
	 *            WARNING_MESSAGE
	 */
	public static int getChoice(String sText, int iType) {
		ConfirmDialog confirm = new ConfirmDialog(sText, getTitleForType(iType), iType);
		return confirm.getResu();
	}

	/**
	 * 
	 * @param iType
	 * @return String for given JOptionPane message type
	 */
	static private String getTitleForType(int iType) {
		switch (iType) {
		case JOptionPane.ERROR_MESSAGE:
			return Messages.getString("Error");
		case JOptionPane.WARNING_MESSAGE:
			return Messages.getString("Warning");
		case JOptionPane.INFORMATION_MESSAGE:
			return Messages.getString("Info");
		}
		return "";
	}

	/**
	 * Show a dialog with specified warning message
	 * 
	 * @param sMessage
	 */
	public static void showWarningMessage(String sMessage) {
		new DetailsMessageDialog(sMessage, getTitleForType(JOptionPane.WARNING_MESSAGE),
				JOptionPane.WARNING_MESSAGE, null, null);
	}

	/**
	 * Show a dialog with specified warning message + a "not show again" button
	 * 
	 * @param sMessage
	 * @param sProperty :
	 *            property name
	 */
	public static void showHideableWarningMessage(String sMessage, String sProperty) {
		// User required to hide this message
		if (ConfigurationManager.getBoolean(sProperty)) {
			return;
		}
		HideableMessageDialog message = new HideableMessageDialog(sMessage,
				getTitleForType(JOptionPane.WARNING_MESSAGE), sProperty,
				JOptionPane.WARNING_MESSAGE, null);
		message.getResu();
	}

	/**
	 * Show a dialog with specified error message and an icon
	 * 
	 * @param sMessage
	 */
	public static void showInfoMessage(final String sMessage, final Icon icon) {
		new DetailsMessageDialog(sMessage, getTitleForType(JOptionPane.INFORMATION_MESSAGE),
				JOptionPane.INFORMATION_MESSAGE, null, icon);
	}

	/**
	 * 
	 * @param sText
	 *            text to display, lines separated by \n characters
	 * @param limit :
	 *            max number of lines to be displayed without scroller
	 * @return formated message: either a string, or a textarea
	 */
	protected static Object getLimitedMessage(String sText, int limit) {
		int iNbLines = new StringTokenizer(sText, "\n").countTokens();
		Object message = null;
		if (iNbLines > limit) {
			JTextArea area = new JTextArea(sText);
			area.setRows(10);
			area.setColumns(50);
			area.setLineWrap(true);
			message = new JScrollPane(area);
		} else {
			message = sText;
		}
		return message;
	}

	/**
	 * Show a dialog with specified error message and infosup
	 * 
	 * @param code
	 * @param sInfoSup
	 */
	public static void showErrorMessage(final int code, final String sInfoSup) {
		new ErrorMessageDialog(code, sInfoSup);
	}

	/**
	 * Show a dialog with specified error message
	 * 
	 * @param sCode
	 */
	public static void showErrorMessage(final int code) {
		showErrorMessage(code, null);
	}

	/**
	 * Show a dialog with specified error message and infosup and details
	 * 
	 * @param sCode
	 * @param sInfoSup
	 */
	public static void showDetailedErrorMessage(final int code, final String sInfoSup,
			String sDetails) {
		new DetailsMessageDialog(Messages.getErrorMessage(code) + " : " + sInfoSup,
				getTitleForType(JOptionPane.ERROR_MESSAGE), JOptionPane.ERROR_MESSAGE, sDetails,
				null);
	}

	/**
	 * Show a dialog with specified error message with infos up
	 * 
	 * @param sMessage
	 * @param sInfoSup
	 */
	public static void showInfoMessage(final String sMessage, final String sInfoSup) {
		new DetailsMessageDialog(sMessage + " : " + sInfoSup,
				getTitleForType(JOptionPane.INFORMATION_MESSAGE), JOptionPane.INFORMATION_MESSAGE,
				null, null);
	}

	/**
	 * Show a dialog with specified error message
	 * 
	 * @param sMessage
	 */
	public static void showInfoMessage(final String sMessage) {
		new DetailsMessageDialog(sMessage, getTitleForType(JOptionPane.INFORMATION_MESSAGE),
				JOptionPane.INFORMATION_MESSAGE, null, null);
	}

	/**
	 * @return Returns the sLocal.
	 */
	public String getLocale() {
		return this.sLocal;
	}

	/**
	 * Return true if the messaging system is started, can be usefull mainly at
	 * startup by services ( like logs) using them to avoid dead locks
	 * 
	 * @return
	 */
	public static boolean isInitialized() {
		return !(mesg == null);
	}

	/**
	 * @return Returns the properties.
	 */
	public Properties getProperties() throws Exception {
		if (this.properties == null) {
			this.properties = parseLangpack(this.sLocal);
		}
		return this.properties;
	}

	/**
	 * @return Returns the propertiesEn.
	 */
	public Properties getPropertiesEn() {
		if (this.propertiesEn == null) {
			try {
				this.propertiesEn = parseLangpack("en");
			} catch (Exception e) {
				Log.error(e);
			}
		}
		return this.propertiesEn;
	}
}

/**
 * Confirmation Dialog
 */
class ConfirmDialog extends JajukDialog {

	/**
	 * Confirm dialog constructor
	 * 
	 * @param sText
	 * @param sTitle
	 * @param iType
	 */
	ConfirmDialog(String sText, String sTitle, int iType) {
		JOptionPane optionPane = Util.getNarrowOptionPane(72);
		Object[] options = null;
		if (iType == JOptionPane.DEFAULT_OPTION) {
			options = new String[] { Messages.getString("Close") };
		} else {
			options = new String[] { Messages.getString("yes"), Messages.getString("no"),
					Messages.getString("Cancel") };
		}
		optionPane.setOptions(options);
		optionPane.setMessageType(iType);
		optionPane.setMessage(Messages.getLimitedMessage(sText, 20));
		JDialog dialog = optionPane.createDialog(null, sTitle);
		dialog.setModal(true);
		dialog.setAlwaysOnTop(true);
		dialog.pack();
		dialog.setLocationRelativeTo(Main.getWindow());
		dialog.setVisible(true);
		if (optionPane.getValue() == null) {
			// User closed the dialog using the cross icon
			iResu = JOptionPane.CANCEL_OPTION;
		} else if (optionPane.getValue().equals(Messages.getString("yes"))) {
			iResu = JOptionPane.YES_OPTION;
		} else if (optionPane.getValue().equals(Messages.getString("no"))) {
			iResu = JOptionPane.NO_OPTION;
		} else if (optionPane.getValue().equals(Messages.getString("Cancel"))) {
			iResu = JOptionPane.CANCEL_OPTION;
		}
	}

}

/**
 * Message Dialog
 */
class DetailsMessageDialog extends JajukDialog {

	/**
	 * Message dialog constructor
	 * 
	 * @param sText
	 * @param sTitle
	 * @param iType
	 */
	DetailsMessageDialog(String sText, String sTitle, int iType, String sDetails, Icon icon) {
		JOptionPane optionPane = Util.getNarrowOptionPane(72);
		optionPane.setMessage(sText);
		if (sDetails != null) {
			Object[] options = { Messages.getString("OK"), Messages.getString("Details") };
			optionPane.setOptions(options);
		}
		optionPane.setMessageType(iType);
		if (icon != null) {
			optionPane.setIcon(icon);
		}
		JDialog dialog = optionPane.createDialog(null, sTitle);
		dialog.setModal(true);
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		if (optionPane.getValue().equals(Messages.getString("Details"))) {
			// details
			final JDialog dialogDetail = new JDialog(dialog, Messages.getString("Details"));
			JPanel jp = new JPanel();
			jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
			JTextArea jta = new JTextArea(sDetails);
			jta.setEditable(false);
			jp.add(new JScrollPane(jta));
			JButton jbOK = new JButton(Messages.getString("OK"));
			jbOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dialogDetail.dispose();
				}
			});
			jp.add(Util.getCentredPanel(jbOK));
			dialogDetail.setModal(true);
			dialogDetail.setAlwaysOnTop(true);
			dialogDetail.setContentPane(jp);
			dialogDetail.pack();
			dialogDetail.setLocationRelativeTo(Main.getWindow());
			dialogDetail.setVisible(true);
		}
	}

}

/**
 * Hideable message dialog (has a "not show again" button)
 */
class HideableMessageDialog extends JajukDialog {

	/**
	 * Message dialog constructor
	 * 
	 * @param sText
	 * @param sTitle
	 * @param sProperty
	 * @param iType
	 * @param icon
	 */
	HideableMessageDialog(String sText, String sTitle, String sProperty, int iType, Icon icon) {
		JOptionPane optionPane = Util.getNarrowOptionPane(72);
		optionPane.setMessage(Messages.getLimitedMessage(sText, 20));
		Object[] options = { Messages.getString("OK"), Messages.getString("Hide") };
		optionPane.setOptions(options);
		optionPane.setMessageType(iType);
		if (icon != null) {
			optionPane.setIcon(icon);
		}
		JDialog dialog = optionPane.createDialog(null, sTitle);
		dialog.setAlwaysOnTop(true);
		// keep it modal (useful at startup)
		dialog.setModal(true);
		dialog.pack();
		dialog.setLocationRelativeTo(Main.getWindow());
		dialog.setVisible(true);
		if (optionPane.getValue().equals(Messages.getString("Hide"))) {
			// Not show again
			ConfigurationManager.setProperty(sProperty, TRUE);
		}
	}

}

/**
 * Error message dialog
 */
class ErrorMessageDialog extends JajukDialog {

	/**
	 * Message dialog constructor
	 * 
	 * @param sText
	 * @param sTitle
	 * @param sProperty
	 * @param iType
	 * @param icon
	 */
	ErrorMessageDialog(int code, String sInfoSup) {
		JOptionPane optionPane = Util.getNarrowOptionPane(72);
		optionPane.setMessage(Messages.getLimitedMessage(Messages.getErrorMessage(code)
				+ (sInfoSup != null ? (" : " + sInfoSup) : ""), 20));
		Object[] options = { Messages.getString("OK") };
		optionPane.setOptions(options);
		optionPane.setMessageType(JOptionPane.ERROR_MESSAGE);
		JDialog dialog = optionPane.createDialog(null, Messages.getString("Error"));
		dialog.setAlwaysOnTop(true);
		// keep it modal (useful at startup)
		dialog.setModal(true);
		dialog.pack();
		dialog.setLocationRelativeTo(Main.getWindow());
		dialog.setVisible(true);
	}
}

abstract class JajukDialog implements ITechnicalStrings {
	/** Dialog output */
	protected int iResu = -2;

	/**
	 * 
	 * @return the user option
	 */
	public int getResu() {
		return iResu;
	}
}
