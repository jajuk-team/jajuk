/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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

package org.jajuk.services.bookmark;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jajuk.base.Collection;
import org.jajuk.base.FileManager;
import org.jajuk.events.HighPriorityObserver;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.services.core.SessionService;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilString;
import org.jajuk.util.error.JajukException;
import org.jajuk.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Stores all files user read History is used as a model for some Swing
 * components, so any changes on the model should be done in the EDT see
 * http://java
 * .sun.com/javase/6/docs/api/javax/swing/package-summary.html#threading
 */
public final class History extends DefaultHandler implements HighPriorityObserver {

  /** Self instance. */
  private static History history = new History();

  /** History repository, last play first. */
  private static Vector<HistoryItem> vHistory = new Vector<HistoryItem>(100); // NOPMD

  /** History begin date. */
  private static long lDateStart;

  /** Cached date formatter. */
  private SimpleDateFormat formatter;

  /**
   * Instance getter.
   * 
   * @return the instance
   */
  public static History getInstance() {
    return history;
  }

  /**
   * Hidden constructor.
   */
  private History() {
    super();

    ObservationManager.register(this);
    // check if something has already started
    if (ObservationManager.getDetailLastOccurence(JajukEvents.FILE_LAUNCHED,
        Const.DETAIL_CURRENT_FILE_ID) != null
        && ObservationManager.getDetailLastOccurence(JajukEvents.FILE_LAUNCHED,
            Const.DETAIL_CURRENT_DATE) != null) {
      update(new JajukEvent(JajukEvents.FILE_LAUNCHED,
          ObservationManager.getDetailsLastOccurence(JajukEvents.FILE_LAUNCHED)));
    }
    // Fill date formatter
    formatter = new SimpleDateFormat(Messages.getString("HistoryItem.0"), Locale.getDefault());
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.FILE_LAUNCHED);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    eventSubjectSet.add(JajukEvents.CLEAR_HISTORY);
    eventSubjectSet.add(JajukEvents.FILE_NAME_CHANGED);
    eventSubjectSet.add(JajukEvents.LANGUAGE_CHANGED);

    return eventSubjectSet;
  }

  /**
   * Gets the history.
   * 
   * @return the history
   */
  public Vector<HistoryItem> getHistory() { // NOPMD
    return vHistory;
  }

  /**
   * Add an history item.
   * 
   * @param sFileId 
   * @param lDate 
   */
  public void addItem(String sFileId, long lDate) {
    // no history
    if (Conf.getString(Const.CONF_HISTORY).equals("0")) {
      return;
    }
    // check the ID maps an existing file
    if (FileManager.getInstance().getFileByID(sFileId) == null) {
      return;
    }
    // OK, begin to add the new history item
    HistoryItem hi = new HistoryItem(sFileId, lDate);
    // check if previous history item is not the same,
    // otherwise, keep last one
    if (vHistory.size() > 0) {
      HistoryItem hiPrevious = vHistory.get(0);
      if (hiPrevious.getFileId().equals(hi.getFileId())) {
        vHistory.remove(0);
      }
      vHistory.add(0, hi); // keep only most recent date
      // test maximum history size, if >, remove oldest item
      if (vHistory.size() > Const.MAX_HISTORY_SIZE) {
        vHistory.remove(vHistory.size() - 1);
      }
    } else { // first element in history
      vHistory.add(0, hi);
    }
  }

  /**
   * Clear history.
   */
  public void clear() {
    vHistory.clear();
  }

  /**
   * Cleanup history of dead items (removed files after a refresh).
   */
  public void cleanup() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Iterator<HistoryItem> it = vHistory.iterator();
        while (it.hasNext()) {
          HistoryItem hi = it.next();
          if (FileManager.getInstance().getFileByID(hi.getFileId()) == null) {
            it.remove();
          }
        }
      }
    });
  }

  /**
   * Change ID for a file.
   * 
   * @param sIDOld 
   * @param sIDNew 
   */
  public void changeID(final String sIDOld, final String sIDNew) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < vHistory.size(); i++) {
          HistoryItem hi = vHistory.get(i);
          if (hi.getFileId().equals(sIDOld)) {
            vHistory.remove(i);
            vHistory.add(i, new HistoryItem(sIDNew, hi.getDate()));
          }
        }
      }
    });
  }

  /**
   * Clear history for all history items before iDays days.
   * 
   * @param iDays 
   */
  public void clear(final int iDays) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {

        // Begins by clearing deleted files
        Iterator<HistoryItem> it = vHistory.iterator();
        while (it.hasNext()) {
          HistoryItem hi = it.next();
          if (FileManager.getInstance().getFileByID(hi.getFileId()) == null) {
            it.remove();
          }
        }
        // Follow day limits
        if (iDays == -1) { // infinite history
          return;
        }
        it = vHistory.iterator();
        while (it.hasNext()) {
          HistoryItem hi = it.next();
          if (hi.getDate() < (System.currentTimeMillis() - (((long) iDays) * Const.MILLISECONDS_IN_A_DAY))) {
            it.remove();
          }
        }
      }
    });
  }

  /**
   * Write history on disk.
   *
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static void commit() throws IOException {
    if (lDateStart == 0) {
      lDateStart = System.currentTimeMillis();
    }
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
        SessionService.getConfFileByPath(Const.FILE_HISTORY)), "UTF-8"));
    try {
      bw.write("<?xml version='1.0' encoding='UTF-8'?>\n");
      bw.write("<history JAJUK_VERSION='" + Const.JAJUK_VERSION + "' begin_date='"
          + Long.toString(lDateStart) + "'>\n");
      Iterator<HistoryItem> it = vHistory.iterator();
      while (it.hasNext()) {
        HistoryItem hi = it.next();
        bw.write("\t<play file='" + hi.getFileId() + "' date='" + hi.getDate() + "'/>\n");
      }
      bw.write("</history>");
      bw.flush();
    } finally {
      bw.close();
    }
  }

  /**
   * Read history from disk.
   *
   */
  public static void load() {
    try {
      SAXParserFactory spf = SAXParserFactory.newInstance();
      spf.setValidating(false);
      SAXParser saxParser = spf.newSAXParser();
      File frt = SessionService.getConfFileByPath(Const.FILE_HISTORY);
      saxParser.parse(frt.toURI().toURL().toString(), getInstance());
      // delete old history items
      getInstance().clear(Integer.parseInt(Conf.getString(Const.CONF_HISTORY)));
    } catch (Exception e) {
      Log.error(new JajukException(119));
      try {
        commit(); // this history looks corrupted, write a void one
      } catch (Exception e2) {
        Log.error(e2);
      }
    }
  }

  /**
   * Gets the last file.
   * 
   * @return id of last played registered track or null if history is empty
   */
  public String getLastFile() {
    HistoryItem hiLast = null;
    if (vHistory.size() == 0) {
      return null;
    }
    hiLast = vHistory.get(0);

    // we only add valid entries to hiLast, so hiLast cannot be null at this point...
    return hiLast.getFileId();
  }

  /**
   * Return the history item by index.
   * 
   * @param index 
   * 
   * @return the history item
   */
  public HistoryItem getHistoryItem(int index) {
    return (index >= 0 && index < vHistory.size() ? vHistory.get(index) : null);
  }

  /**
   * parsing warning.
   *
   * @param spe 
   * @throws SAXException the SAX exception
   */
  @Override
  public void warning(SAXParseException spe) throws SAXException {
    throw new SAXException(Messages.getErrorMessage(119) + " / " + spe.getSystemId() + "/"
        + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
  }

  /**
   * parsing error.
   *
   * @param spe 
   * @throws SAXException the SAX exception
   */
  @Override
  public void error(SAXParseException spe) throws SAXException {
    throw new SAXException(Messages.getErrorMessage(119) + " / " + spe.getSystemId() + "/"
        + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
  }

  /**
   * parsing fatal error.
   *
   * @param spe 
   * @throws SAXException the SAX exception
   */
  @Override
  public void fatalError(SAXParseException spe) throws SAXException {
    throw new SAXException(Messages.getErrorMessage(119) + " / " + spe.getSystemId() + "/"
        + spe.getLineNumber() + "/" + spe.getColumnNumber() + " : " + spe.getMessage());
  }

  /**
   * Called at parsing start.
   */
  @Override
  public void startDocument() {
    Log.debug("Starting history file parsing...");
  }

  /**
   * Called at parsing end.
   */
  @Override
  public void endDocument() {
    Log.debug("History file parsing done");
  }

  /**
   * Called when we start an element.
   * 
   * @param sUri 
   * @param sName 
   * @param sQName 
   * @param attributes 
   * 
   * @throws SAXException the SAX exception
   */
  @Override
  public void startElement(String sUri, String sName, String sQName, Attributes attributes)
      throws SAXException {
    if ("history".equals(sQName)) {
      setStartDate(UtilString
          .fastLongParser(attributes.getValue(attributes.getIndex("begin_date"))));
    } else if ("play".equals(sQName)) {
      String sID = attributes.getValue(attributes.getIndex("file"));
      // check id has not been changed
      Map<String, String> hm = Collection.getInstance().getHmWrongRightFileID();
      if (hm.size() > 0 && hm.containsKey(sID)) {
        sID = hm.get(sID);
        Log.debug("upload:" + sID);
      }
      // test if this file is still known in the collection
      if (FileManager.getInstance().getFileByID(sID) != null) {
        HistoryItem hi = new HistoryItem(sID, UtilString.fastLongParser(attributes
            .getValue(attributes.getIndex("date"))));
        vHistory.add(hi);
      }
    }
  }

  /**
   * Called when we reach the end of an element.
   * 
   * @param sUri 
   * @param sName 
   * @param sQName 
   * 
   * @throws SAXException the SAX exception
   */
  @Override
  public void endElement(String sUri, String sName, String sQName) throws SAXException {
    // nothing to do here...
  }

  /*
   * (non-Javadoc)
   *
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(final JajukEvent event) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        JajukEvents subject = event.getSubject();
        try {
          if (JajukEvents.FILE_LAUNCHED.equals(subject)) {
            String sFileID = (String) ObservationManager.getDetail(event,
                Const.DETAIL_CURRENT_FILE_ID);
            long lDate = ((Long) ObservationManager.getDetail(event, Const.DETAIL_CURRENT_DATE))
                .longValue();
            addItem(sFileID, lDate);
          } else if (JajukEvents.DEVICE_REFRESH.equals(subject)) {
            cleanup();
          } else if (JajukEvents.CLEAR_HISTORY.equals(subject)) {
            clear();
            InformationJPanel.getInstance().setMessage(Messages.getString("ParameterView.251"),
                InformationJPanel.MessageType.INFORMATIVE);
          } else if (JajukEvents.LANGUAGE_CHANGED.equals(subject)) {
            // reset formatter
            formatter = new SimpleDateFormat(Messages.getString("HistoryItem.0"), Locale
                .getDefault());
          } else if (JajukEvents.FILE_NAME_CHANGED.equals(subject)) {
            Properties properties = event.getDetails();
            org.jajuk.base.File fileOld = (org.jajuk.base.File) properties.get(Const.DETAIL_OLD);
            org.jajuk.base.File fNew = (org.jajuk.base.File) properties.get(Const.DETAIL_NEW);
            // change id in history
            changeID(fileOld.getID(), fNew.getID());
          }
        } catch (Exception e) {
          Log.error(e);
          return;
        }
      }
    });

  }

  /**
   * Gets the date formatter.
   * 
   * @return Cached date formatter
   */
  public SimpleDateFormat getDateFormatter() {
    return this.formatter;
  }

  /**
   * Sets the start date.
   * 
   * @param start the new start date
   */
  public static void setStartDate(long start) {
    lDateStart = start;
  }
}
