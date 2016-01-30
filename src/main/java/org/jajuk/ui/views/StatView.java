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
package org.jajuk.ui.views;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Genre;
import org.jajuk.base.GenreManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.events.JajukEvent;
import org.jajuk.events.JajukEvents;
import org.jajuk.events.ObservationManager;
import org.jajuk.util.Conf;
import org.jajuk.util.Messages;
import org.jajuk.util.ReadOnlyIterator;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilString;
import org.jajuk.util.log.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryAxis3D;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.NumberAxis3D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.urls.StandardCategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;
import org.jfree.util.SortOrder;

/**
 * Statistics view
 * <p>
 * Help perspective.
 */
public class StatView extends ViewAdapter {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#display()
   */
  @Override
  public void initUI() {
    setLayout(new MigLayout("ins 0,gapx 4,gapy 5", "[grow][grow]", "[grow][grow]"));
    ObservationManager.register(this);
    update(new JajukEvent(JajukEvents.DEVICE_REFRESH));
  }

  /* (non-Javadoc)
   * @see org.jajuk.events.Observer#getRegistrationKeys()
   */
  @Override
  public Set<JajukEvents> getRegistrationKeys() {
    Set<JajukEvents> eventSubjectSet = new HashSet<JajukEvents>();
    eventSubjectSet.add(JajukEvents.DEVICE_DELETE);
    eventSubjectSet.add(JajukEvents.DEVICE_REFRESH);
    return eventSubjectSet;
  }

  /**
   * Genre repartition pie.
   * 
   * @return the chart
   */
  private ChartPanel createGenreRepartition() {
    try {
      DefaultPieDataset pdata = null;
      JFreeChart jfchart = null;
      // data
      pdata = new DefaultPieDataset();
      int iTotal = TrackManager.getInstance().getElementCount();
      double dOthers = 0;
      // Prepare a map genre -> nb tracks
      Map<Genre, Integer> genreNbTracks = new HashMap<Genre, Integer>(GenreManager.getInstance()
          .getElementCount());
      ReadOnlyIterator<Track> it = TrackManager.getInstance().getTracksIterator();
      while (it.hasNext()) {
        Track track = it.next();
        Genre genre = track.getGenre();
        Integer nbTracks = genreNbTracks.get(genre);
        if (nbTracks == null) {
          genreNbTracks.put(genre, 1);
        } else {
          genreNbTracks.put(genre, nbTracks + 1);
        }
      }
      // Cleanup genre with weight < 5 %
      for (Map.Entry<Genre, Integer> entry : genreNbTracks.entrySet()) {
        double d = entry.getValue();
        if (iTotal > 0 && d / iTotal < Conf.getFloat(CONF_STATS_MIN_VALUE_GENRE_DISPLAY) / 100) {
          // less than 5% -> go to others
          dOthers += d;
        } else {
          double dValue = Math.round(100 * (d / iTotal));
          pdata.setValue(entry.getKey().getName2(), dValue);
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
      plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {2}"));
      plot.setToolTipGenerator(new StandardPieToolTipGenerator("{0} = {2}"));
      return new ChartPanel(jfchart);
    } catch (RuntimeException e) {
      Log.error(e);
      return null;
    }
  }

  /**
   * Device size pie.
   * 
   * @return the chart
   */
  private ChartPanel createDeviceRepartition() {
    try {
      DefaultPieDataset pdata = null;
      JFreeChart jfchart = null;
      // data
      pdata = new DefaultPieDataset();
      // prepare devices
      long lTotalSize = 0;
      double dOthers = 0;
      List<Device> devices = DeviceManager.getInstance().getDevices();
      long[] lSizes = new long[DeviceManager.getInstance().getElementCount()];
      ReadOnlyIterator<File> it = FileManager.getInstance().getFilesIterator();
      while (it.hasNext()) {
        File file = it.next();
        lTotalSize += file.getSize();
        lSizes[devices.indexOf(file.getDirectory().getDevice())] += file.getSize();
      }
      for (Device device : devices) {
        long lSize = lSizes[devices.indexOf(device)];
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
      plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1} GB ({2})"));
      plot.setToolTipGenerator(new StandardPieToolTipGenerator("{0} = {1} GB ({2})"));
      return new ChartPanel(jfchart);
    } catch (RuntimeException e) {
      Log.error(e);
      return null;
    }
  }

  /**
   * Collection size bars.
   * 
   * @return the chart
   */
  private ChartPanel createCollectionSize() {
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
      ReadOnlyIterator<Track> tracks = TrackManager.getInstance().getTracksIterator();
      while (tracks.hasNext()) {
        Track track = tracks.next();
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
      // chart, use local copy of method to use better format string for
      // tooltips
      jfchart = createBarChart3D(Messages.getString("StatView.7"), // chart
          // title
          Messages.getString("StatView.8"), // domain axis label
          Messages.getString("StatView.9"), // range axis label
          cdata, // data
          PlotOrientation.VERTICAL, // orientation
          false, // include legend
          true, // tooltips
          false, // urls
          "{1} = {2} GB");
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
      return new ChartPanel(jfchart);
    } catch (Exception e) {
      Log.error(e);
      return null;
    }
  }

  // copied method from ChartFactory to overwrite format of tooltips which is
  // otherwise hardcoded in ChartFactory
  /**
   * Creates the bar chart3 d.
   * 
   * 
   * @param title 
   * @param categoryAxisLabel 
   * @param valueAxisLabel 
   * @param dataset 
   * @param orientation 
   * @param legend 
   * @param tooltips 
   * @param urls 
   * @param format 
   * 
   * @return the j free chart
   */
  public static JFreeChart createBarChart3D(String title, String categoryAxisLabel,
      String valueAxisLabel, CategoryDataset dataset, PlotOrientation orientation, boolean legend,
      boolean tooltips, boolean urls, String format) {
    if (orientation == null)
      throw new IllegalArgumentException("Null 'orientation' argument.");
    CategoryAxis categoryAxis = new CategoryAxis3D(categoryAxisLabel);
    ValueAxis valueAxis = new NumberAxis3D(valueAxisLabel);
    BarRenderer3D renderer = new BarRenderer3D();
    if (tooltips)
      renderer.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(format, NumberFormat
          .getInstance()));
    if (urls)
      renderer.setBaseItemURLGenerator(new StandardCategoryURLGenerator());
    CategoryPlot plot = new CategoryPlot(dataset, categoryAxis, valueAxis, renderer);
    plot.setOrientation(orientation);
    if (orientation == PlotOrientation.HORIZONTAL) {
      plot.setRowRenderingOrder(SortOrder.DESCENDING);
      plot.setColumnRenderingOrder(SortOrder.DESCENDING);
    }
    plot.setForegroundAlpha(0.75F);
    JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, legend);
    ChartFactory.getChartTheme().apply(chart);
    return chart;
  }

  /**
   * Track number bars.
   * 
   * @return the chart
   */
  private ChartPanel createTrackNumber() {
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
      ReadOnlyIterator<Track> tracks = TrackManager.getInstance().getTracksIterator();
      while (tracks.hasNext()) {
        Track track = tracks.next();
        int i = Integer.parseInt(additionFormatter.format(track.getDiscoveryDate())) / 100;
        for (int j = 0; j < iMonthsNumber + 1; j++) {
          if (i <= iMounts[j]) {
            iTracksByMonth[j]++;
          }
        }
      }
      double[][] data = new double[1][iMonthsNumber + 1];
      // cannot use System.arraycopy() here because we have different types in
      // the arrays...
      // System.arraycopy(iTracksByMonth, 0, data[0], 0, iMonthsNumber);
      for (int i = 0; i < iMonthsNumber + 1; i++) {
        data[0][i] = iTracksByMonth[i];
      }
      cdata = DatasetUtilities.createCategoryDataset(new String[] { "" },
          getMonthsLabels(iMonthsNumber), data);
      // chart, use local copy of method to use better format string for
      // tooltips
      jfchart = createBarChart3D(Messages.getString("StatView.12"), // chart
          // title
          Messages.getString("StatView.13"), // domain axis label
          Messages.getString("StatView.14"), // range axis label
          cdata, // data
          PlotOrientation.VERTICAL, // orientation
          false, // include legend
          true, // tooltips
          false, // urls
          "{1} = {2}");
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
      return new ChartPanel(jfchart);
    } catch (Exception e) {
      Log.error(e);
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.IView#getDesc()
   */
  @Override
  public String getDesc() {
    return Messages.getString("StatView.16");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.Observer#update(java.lang.String)
   */
  @Override
  public void update(JajukEvent event) {
    JajukEvents subject = event.getSubject();
    if (JajukEvents.DEVICE_REFRESH.equals(subject) || JajukEvents.DEVICE_DELETE.equals(subject)) {
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          try {
            UtilGUI.waiting();
            if (getComponentCount() > 0) {
              removeAll();
            }
            ChartPanel cp1 = createGenreRepartition();
            if (cp1 != null) {
              add(cp1);
            }
            ChartPanel cp2 = createCollectionSize();
            if (cp2 != null) {
              add(cp2, "wrap");
            }
            ChartPanel cp3 = createTrackNumber();
            if (cp3 != null) {
              add(cp3);
            }
            ChartPanel cp4 = createDeviceRepartition();
            if (cp4 != null) {
              add(cp4, "wrap");
            }
            revalidate();
            repaint();
          } finally {
            UtilGUI.stopWaiting();
          }
        }
      });
    }
  }

  /**
   * Computes mounts labels.
   * 
   * @param iMonthsNumber : number of mounts ( without 'before' ) you want
   * 
   * @return the mounts labels
   */
  private String[] getMonthsLabels(int iMonthsNumber) {
    int iNow = Integer.parseInt(new SimpleDateFormat(DATE_FILE, Locale.getDefault())
        .format(new Date())) / 100; // reference
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
   * Get months as integers.
   * 
   * @param iMonthsNumber 
   * 
   * @return the months
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
