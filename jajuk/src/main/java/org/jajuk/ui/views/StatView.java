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
 *  $$Revision$$
 */

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.Event;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.events.Observer;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 * Statistics view
 * <p>
 * Help perspective
 */
public class StatView extends ViewAdapter implements Observer {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor
   */
  public StatView() {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  public void initUI() {
    double[][] size = new double[][] { { 0.5f, 10, 0.5f }, { 0.5f, 10, 0.5f } };
    setLayout(new TableLayout(size));
    ObservationManager.register(this);
    update(new Event(JajukEvents.EVENT_DEVICE_REFRESH, ObservationManager
        .getDetailsLastOccurence(JajukEvents.EVENT_DEVICE_REFRESH)));
  }

  public Set<JajukEvents> getRegistrationKeys() {
    HashSet<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.EVENT_DEVICE_DELETE);
    eventSubjectSet.add(JajukEvents.EVENT_DEVICE_REFRESH);
    return eventSubjectSet;
  }

  /**
   * Style repartition pie
   * 
   * @return the chart
   */
  private ChartPanel createStyleRepartition() {
    ChartPanel cpanel = null;
    try {
      DefaultPieDataset pdata = null;
      JFreeChart jfchart = null;
      // data
      pdata = new DefaultPieDataset();
      Iterator<Style> it = StyleManager.getInstance().getStyles().iterator();
      int iTotal = 0;
      double dOthers = 0;
      TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
      while (it.hasNext()) {
        Style style = it.next();
        int iCount = style.getCount();
        iTotal += iCount;
        tm.put(style.getName2(), Integer.valueOf(iCount));
      }
      Iterator<String> keys = tm.keySet().iterator();
      while (keys.hasNext()) {
        String sName = keys.next();
        Integer i = tm.get(sName);
        double d = i.doubleValue();
        if (iTotal > 0 && d / iTotal < 0.05) {
          // less than 5% -> go to others
          dOthers += d;
        } else {
          double dValue = Math.round(100 * (d / iTotal));
          pdata.setValue(sName, dValue);
        }
      }
      if (iTotal > 0 && dOthers > 0) {
        double dValue = Math.round(100 * (dOthers / iTotal));
        pdata.setValue(Messages.getString("StatView.0"), dValue);
      }
      // chart
      jfchart = ChartFactory.createPieChart3D(Messages.getString("StatView.1"), pdata, true, true,
          true);
      // set the background color for the chart...
      PiePlot plot = (PiePlot) jfchart.getPlot();
      plot.setLabelFont(PiePlot.DEFAULT_LABEL_FONT);
      plot.setNoDataMessage(Messages.getString("StatView.2"));
      plot.setForegroundAlpha(0.5f);
      plot.setBackgroundAlpha(0.5f);
      StandardPieSectionLabelGenerator labels = new StandardPieSectionLabelGenerator("{0} = {2}");
      plot.setLabelGenerator(labels);
      cpanel = new ChartPanel(jfchart);
    } catch (Exception e) {
      Log.error(e);
    }
    return cpanel;
  }

  /**
   * Device size pie
   * 
   * @return the chart
   */
  private ChartPanel createDeviceRepartition() {
    ChartPanel cpanel = null;
    try {
      DefaultPieDataset pdata = null;
      JFreeChart jfchart = null;
      // data
      pdata = new DefaultPieDataset();
      // prepare devices
      long lTotalSize = 0;
      double dOthers = 0;
      ArrayList<Device> alDevices = null;
      alDevices = new ArrayList<Device>(DeviceManager.getInstance().getDevices());
      long[] lSizes = new long[DeviceManager.getInstance().getElementCount()];
      for (File file : FileManager.getInstance().getFiles()) {
        lTotalSize += file.getSize();
        lSizes[alDevices.indexOf(file.getDirectory().getDevice())] += file.getSize();
      }
      Iterator<Device> itDevices = DeviceManager.getInstance().getDevices().iterator();
      while (itDevices.hasNext()) {
        Device device = itDevices.next();
        long lSize = lSizes[alDevices.indexOf(device)];
        if (lTotalSize > 0 && (double) lSize / lTotalSize < 0.05) {
          // less than 5% -> go to others
          dOthers += lSize;
        } else {
          double dValue = Math.round((double) lSize / 1073741824);
          pdata.setValue(device.getName(), dValue);
        }
      }
      if (dOthers > 0) {
        double dValue = Math.round((dOthers / 1073741824));
        pdata.setValue(Messages.getString("StatView.3"), dValue);
      }
      // chart
      jfchart = ChartFactory.createPieChart3D(Messages.getString("StatView.4"), pdata, true, true,
          true);
      // set the background color for the chart...
      PiePlot plot = (PiePlot) jfchart.getPlot();
      plot.setLabelFont(PiePlot.DEFAULT_LABEL_FONT);
      plot.setNoDataMessage(Messages.getString("StatView.5"));
      plot.setForegroundAlpha(0.5f);
      plot.setBackgroundAlpha(0.5f);
      plot.setLabelGenerator(new StandardPieSectionLabelGenerator());
      cpanel = new ChartPanel(jfchart);
    } catch (Exception e) {
      Log.error(e);
    }
    return cpanel;
  }

  /**
   * Collection size bars
   * 
   * @return the chart
   */
  private ChartPanel createCollectionSize() {
    ChartPanel cpanel = null;
    try {
      final DateFormat additionFormatter = UtilString.getAdditionDateFormatter();

      CategoryDataset cdata = null;
      JFreeChart jfchart = null;
      int iMonthsNumber = 5; // number of mounts we show, mounts
      // before are set together in 'before'
      long lSizeByMonth[] = new long[iMonthsNumber + 1];
      // contains size ( in Go ) for each month, first cell is before
      // data
      int[] iMonths = getMonths(iMonthsNumber);
      Iterator<Track> it = TrackManager.getInstance().getTracks().iterator();
      while (it.hasNext()) {
        Track track = it.next();
        int i = Integer.parseInt(additionFormatter.format(track.getDiscoveryDate())) / 100;
        for (int j = 0; j < iMonthsNumber + 1; j++) {
          if (i <= iMonths[j]) {
            lSizeByMonth[j] += track.getTotalSize();
          }
        }
      }
      double[][] data = new double[1][iMonthsNumber + 1];
      for (int i = 0; i < iMonthsNumber + 1; i++) {
        data[0][i] = (double) lSizeByMonth[i] / 1073741824;
      }
      cdata = DatasetUtilities.createCategoryDataset(new String[] { "" },
          getMonthsLabels(iMonthsNumber), data);
      // chart
      jfchart = ChartFactory.createBarChart3D(Messages.getString("StatView.7"), // chart
          // title
          Messages.getString("StatView.8"), // domain axis label
          Messages.getString("StatView.9"), // range axis label
          cdata, // data
          PlotOrientation.VERTICAL, // orientation
          false, // include legend
          true, // tooltips
          false // urls
          );

      CategoryPlot plot = jfchart.getCategoryPlot();
      CategoryAxis axis = plot.getDomainAxis();
      new CategoryLabelPosition(RectangleAnchor.TOP, TextBlockAnchor.TOP_RIGHT,
          TextAnchor.TOP_RIGHT, -Math.PI / 8.0, CategoryLabelWidthType.CATEGORY, 0);
      axis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

      // set the background color for the chart...
      plot.setNoDataMessage(Messages.getString("StatView.10"));
      plot.setForegroundAlpha(0.5f);
      plot.setBackgroundAlpha(0.5f);
      // plot.setBackgroundImage(IconLoader.IMAGES_STAT_PAPER).getImage());
      cpanel = new ChartPanel(jfchart);
    } catch (Exception e) {
      Log.error(e);
    }
    return cpanel;
  }

  /**
   * Track number bars
   * 
   * @return the chart
   */
  private ChartPanel createTrackNumber() {
    ChartPanel cpanel = null;
    try {
      final DateFormat additionFormatter = UtilString.getAdditionDateFormatter();

      CategoryDataset cdata = null;
      JFreeChart jfchart = null;
      // number of months we show, mounts
      // before are set together in 'before'
      int iMonthsNumber = 5;
      // contains number of tracks for each month, first cell is 'before'
      // data
      int iTracksByMonth[] = new int[iMonthsNumber + 1];
      int[] iMounts = getMonths(iMonthsNumber);
      Iterator<Track> it = TrackManager.getInstance().getTracks().iterator();
      while (it.hasNext()) {
        Track track = it.next();
        int i = Integer.parseInt(additionFormatter.format(track.getDiscoveryDate())) / 100;
        for (int j = 0; j < iMonthsNumber + 1; j++) {
          if (i <= iMounts[j]) {
            iTracksByMonth[j]++;
          }
        }
      }
      
      double[][] data = new double[1][iMonthsNumber + 1];
      // cannot use System.arraycopy() here because we have different types in the arrays...
      //      System.arraycopy(iTracksByMonth, 0, data[0], 0, iMonthsNumber);
      for (int i = 0; i < iMonthsNumber + 1; i++) {
        data[0][i] = iTracksByMonth[i];
      }
      cdata = DatasetUtilities.createCategoryDataset(new String[] { "" },
          getMonthsLabels(iMonthsNumber), data);

      // chart
      jfchart = ChartFactory.createBarChart3D(Messages.getString("StatView.12"), // chart
          // title
          Messages.getString("StatView.13"), // domain axis label
          Messages.getString("StatView.14"), // range axis label
          cdata, // data
          PlotOrientation.VERTICAL, // orientation
          false, // include legend
          true, // tooltips
          false // urls
          );
      CategoryPlot plot = jfchart.getCategoryPlot();
      CategoryAxis axis = plot.getDomainAxis();
      new CategoryLabelPosition(RectangleAnchor.TOP, TextBlockAnchor.TOP_RIGHT,
          TextAnchor.TOP_RIGHT, -Math.PI / 8.0, CategoryLabelWidthType.CATEGORY, 0);
      axis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

      // set the background color for the chart...
      plot.setNoDataMessage(Messages.getString("StatView.15"));
      plot.setForegroundAlpha(0.5f);
      plot.setBackgroundAlpha(0.5f);
      // plot.setBackgroundImage(IconLoader.IMAGES_STAT_PAPER).getImage());
      cpanel = new ChartPanel(jfchart);
    } catch (Exception e) {
      Log.error(e);
    }
    return cpanel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  public String getDesc() {
    return Messages.getString("StatView.16");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  public void update(Event event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.EVENT_DEVICE_REFRESH.equals(subject)
        || JajukEvents.EVENT_DEVICE_DELETE.equals(subject)) {
      UtilGUI.waiting();
      if (getComponentCount() > 0) {
        removeAll();
      }
      ChartPanel cp1 = createStyleRepartition();
      if (cp1 != null) {
        add(cp1, "0,0");
      }
      ChartPanel cp2 = createCollectionSize();
      if (cp2 != null) {
        add(cp2, "0,2");
      }
      ChartPanel cp3 = createTrackNumber();
      if (cp3 != null) {
        add(cp3, "2,2");
      }
      ChartPanel cp4 = createDeviceRepartition();
      if (cp4 != null) {
        add(cp4, "2,0");
      }
      revalidate();
      repaint();
      UtilGUI.stopWaiting();
    }
  }

  /**
   * Computes mounts labels
   * 
   * @param iMonthsNumber :
   *          number of mounts ( without 'before' ) you want
   * @return the mounts labels
   */
  private String[] getMonthsLabels(int iMonthsNumber) {
    int iNow = Integer.parseInt(new SimpleDateFormat(DATE_FILE).format(new Date())) / 100; // reference
    // month
    String sMonths[] = new String[iMonthsNumber + 1];
    // contains number of tracks for each month, first cell is 'before'
    int iYear = iNow / 100;
    int iMonth = Integer.parseInt(Integer.toString(iNow).substring(4, 6));
    for (int k = 0; k < iMonthsNumber; k++) {
      sMonths[iMonthsNumber - k] = new StringBuilder().append((iMonth / 10 == 0) ? "0" : "")
          .append(Integer.toString(iMonth)).append('/').append(Integer.toString(iYear)).toString();
      iMonth--;
      if (iMonth == 0) {
        iMonth = 12;
        iYear--;
      }
    }
    sMonths[0] = Messages.getString("StatView.24");
    return sMonths;
  }

  /**
   * Get months as integers
   * 
   * @param iMonthsNumber
   * @return
   */
  private int[] getMonths(int iMonthsNumber) {
    int[] iMonths = new int[iMonthsNumber + 1];
    String[] sMonths = getMonthsLabels(iMonthsNumber + 1);
    for (int i = 0; i < iMonthsNumber + 1; i++) {
      iMonths[i] = Integer
          .parseInt(sMonths[i + 1].substring(3, 7) + sMonths[i + 1].substring(0, 2));
    }
    return iMonths;
  }
}
