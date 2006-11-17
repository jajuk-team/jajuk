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
package org.jajuk.i18n;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
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
import javax.swing.SwingUtilities;
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
 * 
 * @author Bertrand Florat
 * @created 5 oct. 2003
 */
public class Messages extends DefaultHandler implements ITechnicalStrings {
	/** Local ( language) to be used, default is english */
	private String sLocal = "en"; //$NON-NLS-1$

	/** Supported Locals */
	public ArrayList<String> alLocals = new ArrayList<String>(10);

	/** Locales description */
	public ArrayList<String> alDescs = new ArrayList<String>(10);

	/** self instance for singleton */
	private static Messages mesg;

	/**
	 * Messages themself extracted from an XML file to this properties class*
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
				Log.error("105", "key: " + key, new Exception()); //$NON-NLS-1$ //$NON-NLS-2$
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
				String sOut = getInstance().getProperties().getProperty(
						base + "." + i); //$NON-NLS-1$

				if (sOut == null) { // this property is unknown for this
					// local, try in english
					sOut = getInstance().getPropertiesEn().getProperty(
							base + "." + i); //$NON-NLS-1$
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
	 *            a language-independant desc like "Language_desc_en"
	 */
	public void registerLocal(String sLocal, String sDesc) {
		alLocals.add(sLocal);
		alDescs.add(sDesc);
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
		return mesg.alDescs;
	}

	/**
	 * Return Description for a given locale id
	 * 
	 * @return localized description
	 */
	public static String getHumanForLocale(String sLocale) {
		return getString(mesg.alDescs.get(mesg.alLocals.indexOf(sLocale)));
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
			// simply jajuk.properties //$NON-NLS-1$
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
				public void characters(char[] ch, int start, int length)
						throws SAXException {
					sb.append(ch, start, length);
				}

				// call when closing the tag (</body> in our case )
				public void endElement(String uri, String localName,
						String qName) throws SAXException {
					String sWhole = sb.toString();
					// ok, parse it ( comments start with #)
					StringTokenizer st = new StringTokenizer(sWhole, "\n"); //$NON-NLS-1$
					while (st.hasMoreTokens()) {
						String sLine = st.nextToken();
						if (sLine.length() > 0
								&& !sLine.startsWith("#") && sLine.indexOf('=') != -1) { //$NON-NLS-1$
							StringTokenizer stLine = new StringTokenizer(sLine,
									"="); //$NON-NLS-1$
							properties.put(stLine.nextToken().trim(), stLine
									.nextToken()); // trim to ignore space
							// at begin end end of
							// lines
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
	 * @param pCode
	 *            Error code.
	 * @return String Message corresponding to the error code.
	 */
	public static String getErrorMessage(String pCode) {
		String sOut = pCode;
		try {
			sOut = getString("Error." + pCode); //$NON-NLS-1$
		} catch (Exception e) {
			System.out
					.println("### Error getting error message for code: " + pCode); //$NON-NLS-1$
		}
		return sOut;
	}

	/**
	 * Show a dialog with specified error message
	 * 
	 * @param sCode
	 */
	public static void showErrorMessage(final String sCode) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JOptionPane
						.showMessageDialog(
								Main.getWindow(),
								"<html><b>" + Messages.getErrorMessage(sCode) + "</b></html>",//$NON-NLS-1$ //$NON-NLS-2$
								Messages.getErrorMessage("102"),
								JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	/**
	 * Show a dialog waiting for a user decision
	 * <p>
	 * CAUTION! the thread which calls this method musn't have locks on
	 * ressources : otherwise it can conduct to GUI freeze
	 * </p>
	 * 
	 * @param sText :
	 *            dialog text
	 * @param iType :
	 *            dialof type : can be JOptionPane.ERROR_MESSAGE,
	 *            WARNING_MESSAGE
	 */
	public static int getChoice(String sText, int iType) {
		ConfirmDialog confirm = new ConfirmDialog(sText,
				getTitleForType(iType), iType);
		if (SwingUtilities.isEventDispatchThread()) { // in the dispatcher
			// thread, no need to
			// use invokeLatter
			confirm.run();
		} else { // not in the awt dispatcher thread, OK, call it in an
			// invokeAndWait to block ui until we get user decision
			try {
				SwingUtilities.invokeAndWait(confirm);
			} catch (InterruptedException e) {
				Log.error(e);
			} catch (InvocationTargetException e) {
				Log.error(e);
			}
		}
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
			return Messages.getString("Error"); //$NON-NLS-1$
		case JOptionPane.WARNING_MESSAGE:
			return Messages.getString("Warning"); //$NON-NLS-1$
		case JOptionPane.INFORMATION_MESSAGE:
			return Messages.getString("Info"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Show a dialog with specified warning message
	 * 
	 * @param sMessage
	 */
	public static void showWarningMessage(String sMessage) {
		DetailsMessageDialog message = new DetailsMessageDialog(sMessage,
				getTitleForType(JOptionPane.WARNING_MESSAGE),
				JOptionPane.WARNING_MESSAGE, null, null);
		if (SwingUtilities.isEventDispatchThread()) { // in the dispatcher
			// thread, no need to
			// use invokeLatter
			message.run();
		} else { // not in the awt dispatcher thread
			try {
				// block until user reply
				SwingUtilities.invokeAndWait(message);
			} catch (InterruptedException e) {
				Log.error(e);
			} catch (InvocationTargetException e) {
				Log.error(e);
			}
		}
	}

	/**
	 * Show a dialog with specified warning message + a "not show again" button
	 * 
	 * @param sMessage
	 * @param sProperty :
	 *            property name
	 */
	public static void showHideableWarningMessage(String sMessage,
			String sProperty) {
		HideableMessageDialog message = new HideableMessageDialog(sMessage,
				getTitleForType(JOptionPane.WARNING_MESSAGE), sProperty,
				JOptionPane.WARNING_MESSAGE, null);
		if (SwingUtilities.isEventDispatchThread()) {
			// in the dispatcher thread, no need to use invokeLatter
			message.run();
		} else { // not in the awt dispatcher thread
			try {
				SwingUtilities.invokeAndWait(message);
			} catch (InterruptedException e) {
				Log.error(e);
			} catch (InvocationTargetException e) {
				Log.error(e);
			}
		}
		message.getResu();
	}

	/**
	 * Show a dialog with specified error message and an icon
	 * 
	 * @param sMessage
	 */
	public static void showInfoMessage(final String sMessage, final Icon icon) {
		DetailsMessageDialog message = new DetailsMessageDialog(sMessage,
				getTitleForType(JOptionPane.INFORMATION_MESSAGE),
				JOptionPane.INFORMATION_MESSAGE, null, icon);
		if (SwingUtilities.isEventDispatchThread()) { // in the dispatcher
			// thread, no need to
			// use invokeLatter
			message.run();
		} else { // not in the awt dispatcher thread
			SwingUtilities.invokeLater(message);
		}
	}

	/**
	 * Show a dialog with specified error message and infosup
	 * 
	 * @param sCode
	 * @param sInfoSup
	 */
	public static void showErrorMessage(final String sCode,
			final String sInfoSup) {
		DetailsMessageDialog message = new DetailsMessageDialog(
				Messages.getErrorMessage(sCode) + " : " + sInfoSup, getTitleForType(JOptionPane.ERROR_MESSAGE), JOptionPane.ERROR_MESSAGE, null, null); //$NON-NLS-1$
		if (SwingUtilities.isEventDispatchThread()) { // in the dispatcher
			// thread, no need to
			// use invokeLatter
			message.run();
		} else { // not in the awt dispatcher thread
			SwingUtilities.invokeLater(message);
		}
	}

	/**
	 * Show a dialog with specified error message and infosup and details
	 * 
	 * @param sCode
	 * @param sInfoSup
	 */
	public static void showDetailedErrorMessage(final String sCode,
			final String sInfoSup, String sDetails) {
		DetailsMessageDialog message = new DetailsMessageDialog(
				Messages.getErrorMessage(sCode) + " : " + sInfoSup, getTitleForType(JOptionPane.ERROR_MESSAGE), JOptionPane.ERROR_MESSAGE, sDetails, null); //$NON-NLS-1$
		if (SwingUtilities.isEventDispatchThread()) { // in the dispatcher
			// thread, no need to
			// use invokeLatter
			message.run();
		} else { // not in the awt dispatcher thread
			SwingUtilities.invokeLater(message);
		}
	}

	/**
	 * Show a dialog with specified error message with infos up
	 * 
	 * @param sMessage
	 * @param sInfoSup
	 */
	public static void showInfoMessage(final String sMessage,
			final String sInfoSup) {
		DetailsMessageDialog message = new DetailsMessageDialog(
				sMessage + " : " + sInfoSup, getTitleForType(JOptionPane.INFORMATION_MESSAGE), JOptionPane.INFORMATION_MESSAGE, null, null); //$NON-NLS-1$
		if (SwingUtilities.isEventDispatchThread()) { // in the dispatcher
			// thread, no need to
			// use invokeLatter
			message.run();
		} else { // not in the awt dispatcher thread
			SwingUtilities.invokeLater(message);
		}
	}

	/**
	 * Show a dialog with specified error message
	 * 
	 * @param sMessage
	 */
	public static void showInfoMessage(final String sMessage) {
		DetailsMessageDialog message = new DetailsMessageDialog(sMessage,
				getTitleForType(JOptionPane.INFORMATION_MESSAGE),
				JOptionPane.INFORMATION_MESSAGE, null, null);
		if (SwingUtilities.isEventDispatchThread()) { // in the dispatcher
			// thread, no need to
			// use invokeLatter
			message.run();
		} else { // not in the awt dispatcher thread
			SwingUtilities.invokeLater(message);
		}
	}

	/**
	 * @return Returns the sLocal.
	 */
	public String getLocal() {
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
				this.propertiesEn = parseLangpack("en"); //$NON-NLS-1$
			} catch (Exception e) {
				Log.error(e);
			}
		}
		return this.propertiesEn;
	}
}

/**
 * Confirmation Dialog
 * 
 * @author Bertrand Florat
 * @created 28 nov. 2004
 */
class ConfirmDialog implements Runnable {

	/** Dialog output */
	private int iResu = -2;

	/** Dialog text */
	private String sText;

	/** Dialog title */
	private String sTitle;

	/** dialog type */
	private int iType;

	/**
	 * Confirm dialog constructor
	 * 
	 * @param sText
	 * @param sTitle
	 * @param iType
	 */
	ConfirmDialog(String sText, String sTitle, int iType) {
		this.iType = iType;
		this.sText = sText;
		this.sTitle = sTitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		iResu = JOptionPane.showConfirmDialog(null, sText, sTitle, iType);
	}

	/**
	 * 
	 * @return the user option
	 */
	public int getResu() {
		return iResu;
	}
}

/**
 * Message Dialog
 * 
 * @author Bertrand Florat
 * @created 28 nov. 2004
 */
class DetailsMessageDialog implements Runnable {

	/** Dialog text */
	private String sText;

	/** Dialog title */
	private String sTitle;

	/** dialog type */
	private int iType;

	/** Details */
	private String sDetails;

	/** Icon */
	private Icon icon;

	/**
	 * Message dialog constructor
	 * 
	 * @param sText
	 * @param sTitle
	 * @param iType
	 */
	DetailsMessageDialog(String sText, String sTitle, int iType,
			String sDetails, Icon icon) {
		this.iType = iType;
		this.sText = sText;
		this.sTitle = sTitle;
		this.sDetails = sDetails;
		this.icon = icon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		JOptionPane optionPane = Util.getNarrowOptionPane(72);
		optionPane.setMessage(sText);
		if (sDetails != null) {
			Object[] options = {
					Messages.getString("OK"), Messages.getString("Details") }; //$NON-NLS-1$ //$NON-NLS-2$
			optionPane.setOptions(options);
		}
		optionPane.setMessageType(iType);
		if (icon != null) {
			optionPane.setIcon(icon);
		}
		JDialog dialog = optionPane.createDialog(null, sTitle);
		dialog.setVisible(true);
		if (optionPane.getValue().equals(Messages.getString("Details"))) { // details
			// //$NON-NLS-1$
			final JDialog dialogDetail = new JDialog(dialog, Messages
					.getString("Details")); //$NON-NLS-1$
			JPanel jp = new JPanel();
			jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
			JTextArea jta = new JTextArea(sDetails);
			jta.setEditable(false);
			jp.add(new JScrollPane(jta));
			JButton jbOK = new JButton(Messages.getString("OK")); //$NON-NLS-1$
			jbOK.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					dialogDetail.dispose();
				}
			});
			jp.add(Util.getCentredPanel(jbOK));
			dialogDetail.setModal(true);
			dialogDetail.setContentPane(jp);
			dialogDetail.pack();
			dialogDetail.setLocationRelativeTo(Main.getWindow());
			dialogDetail.setVisible(true);
		}
	}
}

/**
 * Hideable message dialog (has a "not show again" button)
 * 
 * @author Bertrand Florat
 * @created 29 sept 2006
 */
class HideableMessageDialog implements Runnable, ITechnicalStrings {

	/** Dialog text */
	private String sText;

	/** Dialog title */
	private String sTitle;

	/** Associated hide property */
	private String sProperty;

	/** dialog type */
	private int iType;

	/** Icon */
	private Icon icon;

	/** Dialog output */
	private int iResu = -2;

	/**
	 * Message dialog constructor
	 * 
	 * @param sText
	 * @param sTitle
	 * @param sProperty
	 * @param iType
	 * @param icon
	 */
	HideableMessageDialog(String sText, String sTitle, String sProperty,
			int iType, Icon icon) {
		this.iType = iType;
		this.sText = sText;
		this.sProperty = sProperty;
		this.sTitle = sTitle;
		this.icon = icon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		JOptionPane optionPane = Util.getNarrowOptionPane(72);
		optionPane.setMessage(sText);
		Object[] options = {
				Messages.getString("OK"), Messages.getString("Hide") }; //$NON-NLS-1$ //$NON-NLS-2$
		optionPane.setOptions(options);
		optionPane.setMessageType(iType);
		if (icon != null) {
			optionPane.setIcon(icon);
		}
		JDialog dialog = optionPane.createDialog(null, sTitle);
		// keep it modal (useful at startup)
		dialog.setModal(true);
		if (optionPane.getValue().equals(Messages.getString("Hide"))) {
			// Not show again
			ConfigurationManager.setProperty(sProperty, TRUE);
		}
		dialog.pack();
		dialog.setLocationRelativeTo(Main.getWindow());
		dialog.setVisible(true);
	}

	/**
	 * 
	 * @return the user option
	 */
	public int getResu() {
		return iResu;
	}

}
