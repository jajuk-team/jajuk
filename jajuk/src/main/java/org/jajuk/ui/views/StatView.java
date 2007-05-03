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

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Observer;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.i18n.Messages;
import org.jajuk.util.EventSubject;
import org.jajuk.util.Util;
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

import info.clearthought.layout.TableLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * Statistics view
 * <p>
 * Help perspective
 * <p>
 * Singleton
 */
public class StatView extends ViewAdapter implements Observer {

	private static final long serialVersionUID = 1L;

	/** Self instance */
	private static StatView sv;

	/** Return self instance */
	public static synchronized StatView getInstance() {
		if (sv == null) {
			sv = new StatView();
		}
		return sv;
	}

	/**
	 * Constructor
	 */
	public StatView() {
		sv = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.IView#display()
	 */
	public void initUI() {
		double[][] size = new double[][] { { 0.5f, 10, 0.5f },
				{ 0.5f, 10, 0.5f } };
		setLayout(new TableLayout(size));
		ObservationManager.register(this);
		update(new Event(EventSubject.EVENT_DEVICE_REFRESH, ObservationManager
				.getDetailsLastOccurence(EventSubject.EVENT_DEVICE_REFRESH)));
	}

	public Set<EventSubject> getRegistrationKeys() {
		HashSet<EventSubject> eventSubjectSet = new HashSet<EventSubject>();
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_DELETE);
		eventSubjectSet.add(EventSubject.EVENT_DEVICE_REFRESH);
		return eventSubjectSet;
	}

	/**
	 * Style repartition pie
	 * 
	 * @return the chart
	 */
	private ChartPanel createStyleRepartition() {
		synchronized (StyleManager.getInstance().getLock()) {
			ChartPanel cpanel = null;
			try {
				DefaultPieDataset pdata = null;
				JFreeChart jfchart = null;
				// data
				pdata = new DefaultPieDataset();
				Iterator<Style> it = StyleManager.getInstance().getStyles()
						.iterator();
				int iTotal = 0;
				double dOthers = 0;
				TreeMap<String, Integer> tm = new TreeMap<String, Integer>();
				while (it.hasNext()) {
					Style style = it.next();
					int iCount = style.getCount();
					iTotal += iCount;
					tm.put(style.getName2(), new Integer(iCount));
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
						double dValue = Math
								.round(100 * new Double(d / iTotal));
						pdata.setValue(sName, dValue);
					}
				}
				if (iTotal > 0 && dOthers > 0) {
					double dValue = Math.round(100 * (dOthers / iTotal));
					pdata.setValue(Messages.getString("StatView.0"), dValue); //$NON-NLS-1$
				}
				// chart
				jfchart = ChartFactory.createPieChart3D(Messages
						.getString("StatView.1"), pdata, true, true, true); //$NON-NLS-1$
				// set the background color for the chart...
				PiePlot plot = (PiePlot) jfchart.getPlot();
				plot.setLabelFont(PiePlot.DEFAULT_LABEL_FONT);
				plot.setNoDataMessage(Messages.getString("StatView.2")); //$NON-NLS-1$
				plot.setForegroundAlpha(0.5f);
				plot.setBackgroundAlpha(0.5f);
				StandardPieSectionLabelGenerator labels = new StandardPieSectionLabelGenerator(
						"{0} = {2}");
				plot.setLabelGenerator(labels);
				cpanel = new ChartPanel(jfchart);
			} catch (Exception e) {
				Log.error(e);
			}
			return cpanel;
		}
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
			Iterator itFiles = FileManager.getInstance().getFiles().iterator();
			// prepare devices
			long lTotalSize = 0;
			double dOthers = 0;
			ArrayList<Device> alDevices = null;
			alDevices = new ArrayList<Device>(DeviceManager.getInstance()
					.getDevices());
			long[] lSizes = new long[DeviceManager.getInstance()
					.getElementCount()];
			while (itFiles.hasNext()) {
				File file = (File) itFiles.next();
				lTotalSize += file.getSize();
				lSizes[alDevices.indexOf(file.getDirectory().getDevice())] += file
						.getSize();
			}
			Iterator<Device> itDevices = DeviceManager.getInstance()
					.getDevices().iterator();
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
				pdata.setValue(Messages.getString("StatView.3"), dValue); //$NON-NLS-1$
			}
			// chart
			jfchart = ChartFactory.createPieChart3D(Messages
					.getString("StatView.4"), pdata, true, true, true); //$NON-NLS-1$
			// set the background color for the chart...
			PiePlot plot = (PiePlot) jfchart.getPlot();
			plot.setLabelFont(PiePlot.DEFAULT_LABEL_FONT);
			plot.setNoDataMessage(Messages.getString("StatView.5")); //$NON-NLS-1$
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
			CategoryDataset cdata = null;
			JFreeChart jfchart = null;
			int iMounthsNumber = 5; // number of mounts we show, mounts
			// before are set together in 'before'
			long lSizeByMounth[] = new long[iMounthsNumber + 1];
			// contains size ( in Go ) for each mounth, first cell is before
			// data
			int[] iMounts = getMounts(iMounthsNumber);
			Iterator<Track> it = TrackManager.getInstance().getTracks()
					.iterator();
			while (it.hasNext()) {
				Track track = it.next();
				int i = Integer.parseInt(Util.getAdditionDateFormat().format(
						track.getAdditionDate())) / 100;
				for (int j = 0; j < iMounthsNumber + 1; j++) {
					if (i <= iMounts[j]) {
						lSizeByMounth[j] += track.getTotalSize();
					}
				}
			}
			double[][] data = new double[1][iMounthsNumber + 1];
			for (int i = 0; i < iMounthsNumber + 1; i++) {
				data[0][i] = (double) lSizeByMounth[i] / 1073741824;
			}
			cdata = DatasetUtilities.createCategoryDataset(
					new String[] { "" }, getMountsLabels(iMounthsNumber), data); //$NON-NLS-1$
			// chart
			jfchart = ChartFactory.createBarChart3D(Messages
					.getString("StatView.7"), // chart title //$NON-NLS-1$
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
			new CategoryLabelPosition(RectangleAnchor.TOP,
					TextBlockAnchor.TOP_RIGHT, TextAnchor.TOP_RIGHT,
					-Math.PI / 8.0, CategoryLabelWidthType.CATEGORY, 0);
			axis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

			// set the background color for the chart...
			plot.setNoDataMessage(Messages.getString("StatView.10")); //$NON-NLS-1$
			plot.setForegroundAlpha(0.5f);
			plot.setBackgroundAlpha(0.5f);
			// plot.setBackgroundImage(Util.getIcon(IMAGES_STAT_PAPER).getImage());
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
			CategoryDataset cdata = null;
			JFreeChart jfchart = null;
			// number of mounts we show, mounts
			// before are set together in 'before'
			int iMounthsNumber = 5;
			// contains number of tracks for each mounth, first cell is 'before'
			// data
			int iTracksByMounth[] = new int[iMounthsNumber + 1];
			int[] iMounts = getMounts(iMounthsNumber);
			Iterator<Track> it = TrackManager.getInstance().getTracks()
					.iterator();
			while (it.hasNext()) {
				Track track = it.next();
				int i = Integer.parseInt(Util.getAdditionDateFormat().format(
						track.getAdditionDate())) / 100;
				for (int j = 0; j < iMounthsNumber + 1; j++) {
					if (i <= iMounts[j]) {
						iTracksByMounth[j]++;
					}
				}
			}
			double[][] data = new double[1][iMounthsNumber + 1];
			for (int i = 0; i < iMounthsNumber + 1; i++) {
				data[0][i] = iTracksByMounth[i];
			}
			cdata = DatasetUtilities.createCategoryDataset(
					new String[] { "" }, getMountsLabels(iMounthsNumber), data); //$NON-NLS-1$

			// chart
			jfchart = ChartFactory.createBarChart3D(Messages
					.getString("StatView.12"), // chart title //$NON-NLS-1$
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
			new CategoryLabelPosition(RectangleAnchor.TOP,
					TextBlockAnchor.TOP_RIGHT, TextAnchor.TOP_RIGHT,
					-Math.PI / 8.0, CategoryLabelWidthType.CATEGORY, 0);
			axis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);

			// set the background color for the chart...
			plot.setNoDataMessage(Messages.getString("StatView.15")); //$NON-NLS-1$
			plot.setForegroundAlpha(0.5f);
			plot.setBackgroundAlpha(0.5f);
			// plot.setBackgroundImage(Util.getIcon(IMAGES_STAT_PAPER).getImage());
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
		return Messages.getString("StatView.16"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public synchronized void update(Event event) {
		EventSubject subject = event.getSubject();
		if (EventSubject.EVENT_DEVICE_REFRESH.equals(subject)
				|| EventSubject.EVENT_DEVICE_DELETE.equals(subject)) {
			Util.waiting();
			if (getComponentCount() > 0) {
				removeAll();
			}
			ChartPanel cp1 = createStyleRepartition();
			cp1.setOpaque(false);
			if (cp1 != null)
				add(cp1, "0,0"); //$NON-NLS-1$
			ChartPanel cp2 = createCollectionSize();
			cp2.setOpaque(false);
			if (cp2 != null)
				add(cp2, "0,2"); //$NON-NLS-1$
			ChartPanel cp3 = createTrackNumber();
			cp3.setOpaque(false);
			if (cp3 != null)
				add(cp3, "2,2"); //$NON-NLS-1$
			ChartPanel cp4 = createDeviceRepartition();
			cp4.setOpaque(false);
			if (cp4 != null)
				add(cp4, "2,0"); //$NON-NLS-1$
			StatView.getInstance().revalidate();
			StatView.getInstance().repaint();
			Util.stopWaiting();
		}
	}

	/**
	 * Computes mounts labels
	 * 
	 * @param iMounthsNumber :
	 *            number of mounts ( without 'before' ) you want
	 * @return the mounts labels
	 */
	private String[] getMountsLabels(int iMounthsNumber) {
		int iNow = Integer.parseInt(new SimpleDateFormat(DATE_FILE)
				.format(new Date())) / 100; // reference mounth
		String sMounths[] = new String[iMounthsNumber + 1];
		// contains number of tracks for each mounth, first cell is 'before'
		int iYear = iNow / 100;
		int iMounth = Integer.parseInt(Integer.toString(iNow).substring(4, 6));
		for (int k = 0; k < iMounthsNumber; k++) {
			sMounths[iMounthsNumber - k] = new StringBuffer()
					.append((iMounth / 10 == 0) ? "0" : "").append(Integer.toString(iMounth)).append('/').append(Integer.toString(iYear)).toString(); //$NON-NLS-1$ //$NON-NLS-2$
			iMounth--;
			if (iMounth == 0) {
				iMounth = 12;
				iYear--;
			}
		}
		sMounths[0] = Messages.getString("StatView.24"); //$NON-NLS-1$
		return sMounths;
	}

	/**
	 * Get mounths as integers
	 * 
	 * @param iMounthsNumber
	 * @return
	 */
	private int[] getMounts(int iMounthsNumber) {
		int[] iMounths = new int[iMounthsNumber + 1];
		String[] sMounths = getMountsLabels(iMounthsNumber + 1);
		for (int i = 0; i < iMounthsNumber + 1; i++) {
			iMounths[i] = Integer.parseInt(sMounths[i + 1].substring(3, 7)
					+ sMounths[i + 1].substring(0, 2));
		}
		return iMounths;
	}
}
