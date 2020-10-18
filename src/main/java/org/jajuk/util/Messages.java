/*
 *  Jajuk
 *  Copyright (C) The Jajuk Team
 *  http://jajuk.info
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
 *
 */
package org.jajuk.util;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.util.log.Log;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import net.miginfocom.swing.MigLayout;

/**
 * Utility class to get strings from localized property files
 * <p>
 * Singleton
 * </p>.
 */
public class Messages extends DefaultHandler {
  /** Messages initialized flag. */
  protected static boolean bInitialized = false;
  /** All choice option, completes JDialog options. */
  public static final int ALL_OPTION = 10;
  /** Specific Yes NO All Cancel option. */
  public static final int YES_NO_ALL_CANCEL_OPTION = 11;
  /** User choice. */
  private static int choice;
  /** Messages themselves extracted from an XML file to this properties class*. */
  protected static Properties properties;
  /** English messages used as default*. */
  private static Properties propertiesEn;

  /**
   * Contains.
   *
   * @return whether given key exists
   */
  public static boolean contains(final String sKey) {
    return getPropertiesEn().containsKey(sKey);
  }

  /**
   * Gets the string.
   *
   * @return the string
   */
  public static String getString(final String key) {
    String sOut = key;
    try {
      sOut = getProperties().getProperty(key);
      if (sOut == null) { // this property is unknown for this local, try
        // in English
        sOut = getPropertiesEn().getProperty(key);
      }
      // at least, returned property is the key name but we trace an
      // error to show it
      if (sOut == null) {
        Log.error(105, "key: " + key, new Exception());
        sOut = key;
      }
    } catch (final Exception e) { // system error
      Log.error(e);
    }
    return sOut;
  }

  /**
   * Fetch all messages from a given base key.
   * <P/>
   * Example:
   *
   * <pre>
   * example.0=Message 1
   * example.1=Message 2
   * example.2=Message 3
   * </pre>
   *
   * Using <tt>Messages.getAll("example");</tt> will return a size 3 String
   * array containing the messages in order.
   * <P/>
   * The keys need to have continuous numbers. So, adding
   * <tt>example.5=Message 5</tt> to the bundle, will not result in adding it to
   * the array without first adding <tt>example.3</tt> and <tt>example.4</tt>.
   *
   * @param base The base to use for generating the keys.
   *
   * @return An array of Strings containing the messages linked to the key,
   * never <tt>null</tt>. If <tt>base.0</tt> is not found, and empty
   * array is returned.
   */
  public static String[] getAll(final String base) {
    final List<String> msgs = new ArrayList<>();
    final String prefix = base + ".";
    try {
      final Properties lProperties = getProperties();
      final Properties defaultProperties = getPropertiesEn();
      for (int i = 0;; i++) {
        String sOut = lProperties.getProperty(prefix + i);
        if (sOut == null) {
          // this property is unknown for this local, try in English
          sOut = defaultProperties.getProperty(prefix + i);
          // unknown property, assume we found all properties in the set
          if (sOut == null) {
            break;
          }
        } else {
          // Remove HTML tags
          sOut = sOut.replaceAll("<.*>", "");
        }
        msgs.add(sOut);
      }
    } catch (final Exception e) { // System error
      Log.error(e);
    }
    return msgs.toArray(new String[0]);
  }

  /**
   * Gets the shuffle tip of the day.
   *
   * @return a shuffled tip of the day <br>
   */
  public static String getShuffleTipOfTheDay() {
    try {
      String[] tips = Messages.getAll("TipOfTheDay");
      // index contains the index of the last provided totd
      int index = (int) (UtilSystem.getRandom().nextFloat() * (tips.length - 1));
      // display the next one
      String totd = Messages.getString("TipOfTheDay." + index);
      // Remove <img> tags
      totd = totd.replaceAll("<.*>", "");
      // Increment and save index
      return totd;
    } catch (Exception e) {
      Log.error(e);
      // Make sure to handle every problem: this code is used in slash screen
      // and we won't propagate exception that could prevent jajuk from starting
      return "";
    }
  }

  /**
   * Return Flag icon for given description.
   *
   * @return the icon
   */
  public static Icon getIcon(final String sDesc) {
    return new ImageIcon(UtilSystem.getResource("icons/16x16/flag_"
        + LocaleManager.getLocaleForDesc(sDesc) + ".png"));
  }

  /**
   * ***************************************************************************
   * Parse a fake properties file inside an XML file as CDATA.
   *
   * @return a properties with all entries
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   */
  private static Properties parseLangpack(final Locale locale) throws SAXException, IOException,
      ParserConfigurationException {
    final Properties lProperties = new Properties();
    // Choose right jajuk_<lang>.properties file to load
    final StringBuilder sbFilename = new StringBuilder(Const.FILE_LANGPACK_PART1);
    if (!Locale.ENGLISH.equals(locale)) { // for English, properties file is
      // simply jajuk.properties
      sbFilename.append('_').append(locale);
    }
    sbFilename.append(Const.FILE_LANGPACK_PART2);
    // property file URL, either in the jajuk.jar jar
    // (normal execution) or found as regular file if in
    // development debug mode
    String resource = "org/jajuk/i18n/" + sbFilename.toString();
    URL url = UtilSystem.getResource(resource);
    if (url == null) {
      throw new IOException("Could not read resource: " + resource);
    }
    // parse it, actually it is a big properties file as CDATA in an XML
    // file
    final SAXParserFactory spf = SAXParserFactory.newInstance();
    spf.setValidating(false);
    spf.setNamespaceAware(false);
    final SAXParser saxParser = spf.newSAXParser();
    saxParser.parse(url.openStream(), new DefaultHandler() {
      // this buffer will contain the entire properties strings
      final StringBuilder sb = new StringBuilder(15000);

      // call for each element strings, actually will be called
      // several time if the element is large (our case : large CDATA)
      @Override
      public void characters(final char[] ch, final int start, final int length) {
        sb.append(ch, start, length);
      }

      // call when closing the tag (</body> in our case )
      @Override
      public void endElement(final String uri, final String localName, final String qName) {
        final String sWhole = sb.toString();
        // ok, parse it ( comments start with #)
        final StringTokenizer st = new StringTokenizer(sWhole, "\n");
        while (st.hasMoreTokens()) {
          final String sLine = st.nextToken();
          if ((sLine.length() > 0) && !sLine.startsWith("#") && (sLine.indexOf('=') != -1)) {
            final StringTokenizer stLine = new StringTokenizer(sLine, "=");
            // get full value after the '=', we don't use the
            // stringtokenizer to allow
            // using = characters in the value
            final String sValue = sLine.substring(sLine.indexOf('=') + 1);
            // trim to ignore space at begin end end of lines
            lProperties.put(stLine.nextToken().trim(), sValue);
          }
        }
      }
    });
    return lProperties;
  }

  /**
   * Return the message display to the user corresponding to the error code.
   *
   * @param code Error code.
   *
   * @return String Message corresponding to the error code.
   */
  public static String getErrorMessage(final int code) {
    String sOut = Integer.toString(code);
    try {
      sOut = getString("Error." + UtilString.padNumber(code, 3));
    } catch (final Exception e) {
      System.out.println("### Error getting error message for code: " + code);
    }
    return sOut;
  }

  /**
   * Show a dialog waiting for a user decision
   * <p>
   * CAUTION! the thread which calls this method musn't have locks on resources
   * : otherwise it can conduct to GUI freeze
   * </p>.
   *
   * @param sText : dialog text
   * @param optionsType kind of options like {@link JOptionPane}#OK_CANCEL
   *        <p>
   *        Specific option: Messages.ALL_OPTION
   *        </p>
   * @param iType message type like JOptionPane.WARNING
   * @return the choice
   */
  public static int getChoice(final String sText, final int optionsType, final int iType) {
    try {
      // Make sure to reset the choice and to return a non-existing choice if
      // the GUI fails
      choice = JOptionPane.DEFAULT_OPTION;
      Runnable t = new Thread("Get choice thread") {
        @Override
        public void run() {
          // This must be done in the EDT
          final ConfirmDialog confirm = new ConfirmDialog(sText, getTitleForType(iType),
              optionsType, iType, JajukMainWindow.getInstance());
          choice = confirm.getResu();
        }
      };
      // invokeAndWait method cannot be called from the EDT
      if (SwingUtilities.isEventDispatchThread()) {
        t.run();
      } else {
        SwingUtilities.invokeAndWait(t);
      }
    } catch (InterruptedException | InvocationTargetException e) {
      Log.error(e);
    }
    return choice;
  }

  /**
   * Gets the title for type.
   *
   * @return String for given JOptionPane message type
   */
  private static String getTitleForType(final int iType) {
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
   * Show a dialog with specified warning message.
   */
  public static void showWarningMessage(final String sMessage) {
    SwingUtilities.invokeLater(() -> new DetailsMessageDialog(sMessage, getTitleForType(JOptionPane.WARNING_MESSAGE),
        JOptionPane.WARNING_MESSAGE, null, null));
  }

  /**
   * Show a dialog with specified warning message + a "not show again" button.
   *
   * @param sMessage : the message to show
   * @param sProperty : property name
   */
  public static void showHideableWarningMessage(final String sMessage, final String sProperty) {
    // User required to hide this message
    if (Conf.getBoolean(sProperty)) {
      return;
    }
    SwingUtilities.invokeLater(() -> {
      final HideableMessageDialog message = new HideableMessageDialog(sMessage,
          getTitleForType(JOptionPane.WARNING_MESSAGE), sProperty, JOptionPane.WARNING_MESSAGE,
          null);
      message.getResu();
    });
  }

  /**
   * Show a dialog with specified error message and an icon.
   */
  public static void showInfoMessage(final String sMessage, final Icon icon) {
    SwingUtilities.invokeLater(() -> new DetailsMessageDialog(sMessage, getTitleForType(JOptionPane.INFORMATION_MESSAGE),
        JOptionPane.INFORMATION_MESSAGE, null, icon));
  }

  /**
   * Show a dialog with specified error message and infosup.
   */
  public static void showErrorMessage(final int code, final String sInfoSup) {
    SwingUtilities.invokeLater(() -> new ErrorMessageDialog(code, sInfoSup));
  }

  /**
   * Show a dialog with specified error message.
   */
  public static void showErrorMessage(final int code) {
    showErrorMessage(code, null);
  }

  /**
   * Show a dialog with specified error message and infosup and details.
   */
  public static void showDetailedErrorMessage(final int code, final String sInfoSup,
      final String sDetails) {
    SwingUtilities.invokeLater(() -> new DetailsMessageDialog(Messages.getErrorMessage(code) + " : " + sInfoSup,
        getTitleForType(JOptionPane.ERROR_MESSAGE), JOptionPane.ERROR_MESSAGE, sDetails, null));
  }

  /**
   * Show a dialog with specified error message with infos up.
   */
  public static void showInfoMessage(final String sMessage, final String sInfoSup) {
    SwingUtilities.invokeLater(() -> new DetailsMessageDialog(sMessage + " : " + sInfoSup,
        getTitleForType(JOptionPane.INFORMATION_MESSAGE), JOptionPane.INFORMATION_MESSAGE,
        null, null));
  }

  /**
   * Show a dialog with specified error message.
   */
  public static void showInfoMessage(final String sMessage) {
    SwingUtilities.invokeLater(() -> new DetailsMessageDialog(sMessage, getTitleForType(JOptionPane.INFORMATION_MESSAGE),
        JOptionPane.INFORMATION_MESSAGE, null, null));
  }

  /**
   * Return true if the messaging system is started, can be useful mainly at
   * startup by services ( like logs) using them to avoid dead locks Messages
   * service is initialized after current has been set.
   *
   * @return true, if checks if is initialized
   */
  public static boolean isInitialized() {
    return bInitialized;
  }

  /**
   * Gets localized and human property name for given key.
   *
   * @return the human property name or the property itself if not translated
   */
  public static String getHumanPropertyName(String sKey) {
    if (Messages.contains(Const.PROPERTY_SEPARATOR + sKey)) {
      return Messages.getString(Const.PROPERTY_SEPARATOR + sKey);
    }
    return sKey;
  }

  /**
   * Gets the properties.
   *
   * @return Returns the properties.
   * @throws SAXException the SAX exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParserConfigurationException the parser configuration exception
   */
  public static Properties getProperties() throws SAXException, IOException,
      ParserConfigurationException {
    if (properties == null) {
      // reuse English if possible
      if (Locale.ENGLISH.equals(LocaleManager.getLocale())) {
        properties = getPropertiesEn();
      } else {
        properties = parseLangpack(LocaleManager.getLocale());
      }
    }
    return properties;
  }

  /**
   * Gets the properties en.
   *
   * @return Returns the propertiesEn.
   */
  public static Properties getPropertiesEn() {
    if (propertiesEn == null) {
      try {
        propertiesEn = parseLangpack(Locale.ENGLISH);
      } catch (final Exception e) {
        Log.error(e);
      }
    }
    return propertiesEn;
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
   * @param optionsType : kind of options like JOptionPane.OK_CANCEL
   *        <p>
   *        Specific option: Messages.ALL_OPTION
   *        </p>
   * @param iType
   *          message type like JOptionPane.WARNING
   */
  ConfirmDialog(final String sText, final String sTitle, final int optionsType, final int iType,
      Component parent) {
    super();
    final JOptionPane optionPane = UtilGUI.getNarrowOptionPane(72);
    if (optionsType == Messages.YES_NO_ALL_CANCEL_OPTION) {
      optionPane.setOptions(new Object[] { Messages.getString("Yes"), Messages.getString("No"),
          Messages.getString("YestoAll"), Messages.getString("Cancel") });
    } else {
      optionPane.setOptionType(optionsType);
    }
    optionPane.setMessageType(iType);
    optionPane.setMessage(UtilGUI.getLimitedMessage(sText, 20));
    final JDialog dialog = optionPane.createDialog(null, sTitle);
    dialog.setModal(true);
    dialog.setAlwaysOnTop(true);
    dialog.pack();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
    final Object resu = optionPane.getValue();
    // Set Cancel as default
    iResu = JOptionPane.CANCEL_OPTION;
    if (optionPane.getValue() == null) {
      // User closed the dialog using the cross icon
      iResu = JOptionPane.CANCEL_OPTION;
    } else if (resu instanceof String) {
      // Options are string when using custom options
      if (resu.equals(Messages.getString("YestoAll"))) {
        iResu = Messages.ALL_OPTION;
      } else if (resu.equals(Messages.getString("Yes"))) {
        iResu = JOptionPane.YES_OPTION;
      } else if (resu.equals(Messages.getString("No"))) {
        iResu = JOptionPane.NO_OPTION;
      } else if (resu.equals(Messages.getString("Cancel"))) {
        iResu = JOptionPane.CANCEL_OPTION;
      } else if (resu.equals(Messages.getString("Ok"))) {
        iResu = JOptionPane.OK_OPTION;
      } else if (resu.equals(Messages.getString("Default"))) {
        iResu = JOptionPane.DEFAULT_OPTION;
      }
    } else if (resu instanceof Integer) {
      // result is an integer when using JOptionPane standard types
      iResu = (Integer) resu;
    }
    // manually dispose to free up memory, somehow this is not done automatically!
    dialog.dispose();
  }
}

/**
 * Message Dialog
 */
class DetailsMessageDialog extends JajukDialog {
  /**
   * Message dialog constructor
   */
  DetailsMessageDialog(final String sText, final String sTitle, final int iType,
      final String sDetails, final Icon icon) {
    super();
    final JOptionPane optionPane = UtilGUI.getNarrowOptionPane(72);
    optionPane.setMessage(sText);
    if (sDetails != null) {
      final Object[] options = { Messages.getString("Ok"), Messages.getString("Details") };
      optionPane.setOptions(options);
    }
    optionPane.setMessageType(iType);
    if (icon != null) {
      optionPane.setIcon(icon);
    }
    final JDialog dialog = optionPane.createDialog(null, sTitle);
    dialog.setModal(true);
    dialog.setAlwaysOnTop(true);
    dialog.setVisible(true);
    if (optionPane.getValue().equals(Messages.getString("Details"))) {
      // details
      final JDialog dialogDetail = new JDialog(dialog, Messages.getString("Details"));
      dialogDetail.setMaximumSize(new Dimension(800, 600));
      final JPanel jp = new JPanel();
      jp.setLayout(new MigLayout("ins 5", "[grow]", "[grow]"));
      final JTextArea jta = new JTextArea(sDetails);
      jta.setEditable(false);
      jp.add(new JScrollPane(jta), "wrap,grow");
      dialogDetail.setModal(true);
      dialogDetail.setAlwaysOnTop(true);
      dialogDetail.setContentPane(jp);
      dialogDetail.pack();
      dialogDetail.setLocationRelativeTo(JajukMainWindow.getInstance());
      dialogDetail.setVisible(true);
      // manually dispose to free up memory, somehow this is not done automatically!
      dialog.dispose();
    }
    // manually dispose to free up memory, somehow this is not done automatically!
    dialog.dispose();
  }
}

/**
 * Hideable message dialog (has a "not show again" button)
 */
class HideableMessageDialog extends JajukDialog {
  /**
   * Message dialog constructor
   */
  HideableMessageDialog(final String sText, final String sTitle, final String sProperty,
      final int iType, final Icon icon) {
    super();
    final JOptionPane optionPane = UtilGUI.getNarrowOptionPane(72);
    optionPane.setMessage(UtilGUI.getLimitedMessage(sText, 20));
    final Object[] options = { Messages.getString("Ok"), Messages.getString("Hide") };
    optionPane.setOptions(options);
    optionPane.setMessageType(iType);
    if (icon != null) {
      optionPane.setIcon(icon);
    }
    final JDialog dialog = optionPane.createDialog(null, sTitle);
    dialog.setAlwaysOnTop(true);
    // keep it modal (useful at startup)
    dialog.setModal(true);
    dialog.pack();
    dialog.setLocationRelativeTo(JajukMainWindow.getInstance());
    dialog.setVisible(true);
    if (Messages.getString("Hide").equals(optionPane.getValue())) {
      // Not show again
      Conf.setProperty(sProperty, Const.TRUE);
    }
    // manually dispose to free up memory, somehow this is not done automatically!
    dialog.dispose();
  }
}

/**
 * Error message dialog
 */
class ErrorMessageDialog extends JajukDialog {
  /**
   * Message dialog constructor
   */
  ErrorMessageDialog(final int code, final String sInfoSup) {
    super();
    final JOptionPane optionPane = UtilGUI.getNarrowOptionPane(72);
    optionPane.setMessage(UtilGUI.getLimitedMessage(Messages.getErrorMessage(code)
        + (sInfoSup != null ? (" : " + sInfoSup) : ""), 20));
    final Object[] options = { Messages.getString("Ok") };
    optionPane.setOptions(options);
    optionPane.setMessageType(JOptionPane.ERROR_MESSAGE);
    final JDialog dialog = optionPane.createDialog(null, Messages.getString("Error"));
    dialog.setAlwaysOnTop(true);
    // keep it modal (useful at startup)
    dialog.setModal(true);
    dialog.pack();
    dialog.setLocationRelativeTo(JajukMainWindow.getInstance());
    dialog.setVisible(true);
    // manually dispose to free up memory, somehow this is not done automatically!
    dialog.dispose();
  }
}

abstract class JajukDialog {
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
