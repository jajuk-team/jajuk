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

package org.jajuk.ui.views;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

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
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.labels.StandardPieItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.CategoryDataset;
import org.jfree.data.DatasetUtilities;
import org.jfree.data.DefaultPieDataset;
import org.jfree.text.TextBlockAnchor;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

/**
 *  Statistics view
 * <p>Help perspective
 * <p>Singleton
 * @author     Bertrand Florat
 * @created   24 dec. 2003
 */
public class StatView extends ViewAdapter implements Observer{

	/**Self instance*/
	private static StatView sv;
	
	/**Return self instance*/
	public static synchronized StatView getInstance(){
		if (sv == null){
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
	
	
	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#display()
	 */
	public void populate(){
		double[][] size = new double[][]{
				{0.5f,0.5f},
				{0.5f,0.5f}
		};
		setLayout(new TableLayout(size));
		ObservationManager.register(EVENT_DEVICE_DELETE,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
		update(new Event(EVENT_DEVICE_REFRESH,ObservationManager.getDetailsLastOccurence(EVENT_DEVICE_REFRESH)));	
	}
	
	/** Style repartition pie
	 * @return the chart
	 * */
	private ChartPanel createStyleRepartition(){
		ChartPanel cpanel = null;
		try{
			DefaultPieDataset pdata = null;
			JFreeChart jfchart = null;
			//data
			pdata = new DefaultPieDataset();
			Iterator it = StyleManager.getStyles().iterator();
			int iTotal = 0;
			double dOthers = 0;
			TreeMap tm = new TreeMap();
			while (it.hasNext()){
				Style style = (Style)it.next();
				int iCount = style.getCount();
				iTotal += iCount;
				tm.put(style.getName2(),new Integer(iCount));
			}
			it = tm.keySet().iterator();
			while (it.hasNext()){
				String sName = (String)it.next();
				Integer i = (Integer)tm.get(sName);
				double d = i.doubleValue();
				if ( iTotal>0 && d/iTotal < 0.05){ //less than 5% -> go to others
					dOthers += d;
				}
				else{
					pdata.setValue(sName,new Double(d/iTotal));
				}
			}
			if ( iTotal>0 && dOthers > 0){
				pdata.setValue(Messages.getString("StatView.0"),new Double(dOthers/iTotal)); //$NON-NLS-1$
			}
			//chart
			jfchart = ChartFactory.createPie3DChart(Messages.getString("StatView.1"),pdata,false,true,true); //$NON-NLS-1$
			// set the background color for the chart...
			jfchart.setBackgroundPaint(Color.BLUE);
			PiePlot plot = (PiePlot) jfchart.getPlot();
			plot.setSectionLabelType(PiePlot.NAME_AND_PERCENT_LABELS);
			plot.setNoDataMessage(Messages.getString("StatView.2")); //$NON-NLS-1$
			plot.setForegroundAlpha(0.5f);
			plot.setBackgroundAlpha(0.5f);
			//plot.setBackgroundImage(Util.getIcon(IMAGES_STAT_PAPER).getImage());
			plot.setItemLabelGenerator(new StandardPieItemLabelGenerator());
			cpanel = new ChartPanel(jfchart);
		}
		catch(Exception e){
			Log.error(e);
		}
		return cpanel;
	}
	
	/** Device size pie
	 * @return the chart
	 * */
	private ChartPanel createDeviceRepartition(){
		ChartPanel cpanel = null;
		try{
			DefaultPieDataset pdata = null;
			JFreeChart jfchart = null;
			//data
			pdata = new DefaultPieDataset();
			Iterator itFiles = FileManager.getFiles().iterator();
			//prepare devices
			ArrayList alDevices = DeviceManager.getDevices();
			long lTotalSize = 0;
			double dOthers = 0;
			long[] lSizes = new long[alDevices.size()];
			while (itFiles.hasNext()){
				File file = (File)itFiles.next();
				lTotalSize += file.getSize();
				lSizes[alDevices.indexOf(file.getDirectory().getDevice())] += file.getSize();
			}
			Iterator itDevices = alDevices.iterator();
			while (itDevices.hasNext()){
				Device device = (Device)itDevices.next();
				long lSize = lSizes[alDevices.indexOf(device)];
				if ( lTotalSize >0 && (double)lSize/lTotalSize < 0.05){ //less than 5% -> go to others
					dOthers += lSize;
				}
				else{
					pdata.setValue(device.getName(),new Double((double)lSize/1073741824));
				}
			}
			if ( dOthers > 0){
				pdata.setValue(Messages.getString("StatView.3"),new Double(dOthers/1073741824)); //$NON-NLS-1$
			}
			//chart
			jfchart = ChartFactory.createPie3DChart(Messages.getString("StatView.4"),pdata,false,true,true); //$NON-NLS-1$
			// set the background color for the chart...
			jfchart.setBackgroundPaint(Color.BLUE);
			PiePlot plot = (PiePlot) jfchart.getPlot();
			plot.setSectionLabelType(PiePlot.NAME_AND_VALUE_LABELS);
			plot.setNoDataMessage(Messages.getString("StatView.5")); //$NON-NLS-1$
			plot.setForegroundAlpha(0.5f);
			plot.setBackgroundAlpha(0.5f);
			//plot.setBackgroundImage(Util.getIcon(IMAGES_STAT_PAPER).getImage());
			plot.setItemLabelGenerator(new StandardPieItemLabelGenerator());
			cpanel = new ChartPanel(jfchart);
		}
		catch(Exception e){
			Log.error(e);
		}
		return cpanel;
	}
	
	/** Collection size bars
	 * @return the chart
	 * */
	private ChartPanel createCollectionSize(){
		ChartPanel cpanel = null;
		try {
			CategoryDataset cdata = null;
			JFreeChart jfchart = null;
			int iMounthsNumber = 10; //number of mounts we show, mounts before are set together in 'before'
			long lSizeByMounth[] = new long[iMounthsNumber+1]; //contains size ( in Go ) for each mounth, first cell is before
			//data
			int[] iMounts = getMounts(iMounthsNumber);
			Iterator it = TrackManager.getTracks().iterator();
			while ( it.hasNext()){
				Track track = (Track)it.next();
				int i = Integer.parseInt(track.getAdditionDate())/100;
				for (int j=0;j<iMounthsNumber+1;j++){
					if ( i <= iMounts[j]){
						lSizeByMounth[j] += track.getTotalSize();
					}
				}
			}
			double[][] data = new double[1][iMounthsNumber+1];
			for (int i = 0;i<iMounthsNumber+1;i++){
				data[0][i] = (double)lSizeByMounth[i]/1073741824;
			}
			cdata = DatasetUtilities.createCategoryDataset(new String[]{""},getMountsLabels(iMounthsNumber), data); //$NON-NLS-1$
			//chart
			jfchart = ChartFactory.createBarChart3D(
					Messages.getString("StatView.7"),      // chart title //$NON-NLS-1$
					Messages.getString("StatView.8"),               // domain axis label //$NON-NLS-1$
					Messages.getString("StatView.9"),                  // range axis label //$NON-NLS-1$
					cdata,                  // data
					PlotOrientation.VERTICAL, // orientation
					false,                     // include legend
					true,                     // tooltips
					false                     // urls
			);
			
			CategoryPlot plot = jfchart.getCategoryPlot();
			CategoryAxis axis = plot.getDomainAxis();
			CategoryLabelPosition position = new CategoryLabelPosition(
					RectangleAnchor.TOP, TextBlockAnchor.TOP_RIGHT, TextAnchor.TOP_RIGHT, -Math.PI / 8.0
			);
			axis.setBottomCategoryLabelPosition(position);
			
			// set the background color for the chart...
			jfchart.setBackgroundPaint(Color.BLUE);
			plot.setNoDataMessage(Messages.getString("StatView.10")); //$NON-NLS-1$
			plot.setForegroundAlpha(0.5f);
			plot.setBackgroundAlpha(0.5f);
			//plot.setBackgroundImage(Util.getIcon(IMAGES_STAT_PAPER).getImage());
			cpanel = new ChartPanel(jfchart);
		}
		catch(Exception e){
			Log.error(e);
		}
		return cpanel;
	}


	/** Track number bars
	 * @return the chart
	 * */
	private ChartPanel createTrackNumber(){
		ChartPanel cpanel = null;
		try {
			CategoryDataset cdata = null;
			JFreeChart jfchart = null;
			int iMounthsNumber = 10; //number of mounts we show, mounts before are set together in 'before'
			int iTracksByMounth[] = new int[iMounthsNumber+1]; //contains number of tracks for each mounth, first cell is 'before'
			//data
			int[] iMounts = getMounts(iMounthsNumber);
			Iterator it = TrackManager.getTracks().iterator();
			while ( it.hasNext()){
				Track track = (Track)it.next();
				int i = Integer.parseInt(track.getAdditionDate())/100;
				for (int j=0;j<iMounthsNumber+1;j++){
					if ( i <= iMounts[j]){
						iTracksByMounth[j] ++;
					}
				}
			}
			double[][] data = new double[1][iMounthsNumber+1];
			for (int i = 0;i<iMounthsNumber+1;i++){
				data[0][i] = (double)iTracksByMounth[i];
			}
			cdata = DatasetUtilities.createCategoryDataset(new String[]{""},getMountsLabels(iMounthsNumber), data); //$NON-NLS-1$
			
			//chart
			jfchart = ChartFactory.createBarChart3D(
					Messages.getString("StatView.12"),      // chart title //$NON-NLS-1$
					Messages.getString("StatView.13"),               // domain axis label //$NON-NLS-1$
					Messages.getString("StatView.14"),                  // range axis label //$NON-NLS-1$
					cdata,                  // data
					PlotOrientation.VERTICAL, // orientation
					false,                     // include legend
					true,                     // tooltips
					false                     // urls
			);
			CategoryPlot plot = jfchart.getCategoryPlot();
			CategoryAxis axis = plot.getDomainAxis();
			CategoryLabelPosition position = new CategoryLabelPosition(
					RectangleAnchor.TOP, TextBlockAnchor.TOP_RIGHT, TextAnchor.TOP_RIGHT, -Math.PI / 8.0
			);
			axis.setBottomCategoryLabelPosition(position);
			
			// set the background color for the chart...
			jfchart.setBackgroundPaint(Color.BLUE);
			plot.setNoDataMessage(Messages.getString("StatView.15")); //$NON-NLS-1$
			plot.setForegroundAlpha(0.5f);
			plot.setBackgroundAlpha(0.5f);
			//plot.setBackgroundImage(Util.getIcon(IMAGES_STAT_PAPER).getImage());
			cpanel = new ChartPanel(jfchart);
		}
		catch(Exception e){
			Log.error(e);
		}
		return cpanel;
	}

	

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getDesc()
	 */
	public String getDesc() {
		return "StatView.16";	 //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getID()
	 */
	public String getID() {
	    return "org.jajuk.ui.views.StatView";   //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public synchronized  void update(Event event) {
		String subject = event.getSubject();
		if (EVENT_DEVICE_REFRESH.equals(subject) || EVENT_DEVICE_DELETE.equals(subject)){
			Util.waiting();
			if (getComponentCount() > 0){
			    removeAll();
			}
			ChartPanel cp1 = createStyleRepartition(); 
			if ( cp1!= null) add(cp1,"0,0"); //$NON-NLS-1$
			ChartPanel cp2 = createCollectionSize(); 
			if ( cp2!= null) add(cp2,"0,1"); //$NON-NLS-1$
			ChartPanel cp3 = createTrackNumber(); 
			if ( cp3!= null) add(cp3,"1,1"); //$NON-NLS-1$
			ChartPanel cp4 = createDeviceRepartition(); 
			if ( cp4!= null) add(cp4,"1,0"); //$NON-NLS-1$
			StatView.getInstance().revalidate();
			StatView.getInstance().repaint();
			Util.stopWaiting();
		}
	}

	/**
	 * Computes mounts labels
	 * @param iMounthsNumber : number of mounts ( without 'before' ) you want
	 * @return the mounts labels
	 */
	private String[] getMountsLabels(int iMounthsNumber){
			int iNow = Integer.parseInt(new SimpleDateFormat(DATE_FILE).format(new Date()))/100; //reference mounth
			String sMounths[] = new String[iMounthsNumber+1]; //contains number of tracks for each mounth, first cell is 'before'
			int iYear = iNow/100;
			int iMounth = Integer.parseInt(Integer.toString(iNow).substring(4,6));
			for (int k=0;k<iMounthsNumber;k++){
				sMounths[iMounthsNumber-k] = new StringBuffer().append((iMounth/10==0)?"0":"").append(Integer.toString(iMounth)).append('/').append(Integer.toString(iYear)).toString(); //$NON-NLS-1$ //$NON-NLS-2$
				iMounth--;
				if (iMounth == 0){
					iMounth = 12;
					iYear --;
				}
			}
			sMounths[0]=Messages.getString("StatView.24"); //$NON-NLS-1$
			return sMounths;
	}
	
	/**
	 * Get mounths as integers
	 * @param iMounthsNumber
	 * @return
	 */
	private int[] getMounts(int iMounthsNumber){
		int[] iMounths = new int[iMounthsNumber+1];	
		String[] sMounths = getMountsLabels(iMounthsNumber+1);
		for (int i=0;i<iMounthsNumber+1;i++){
			iMounths[i] = Integer.parseInt(sMounths[i+1].substring(3,7)+sMounths[i+1].substring(0,2));
		}
		return iMounths;
	}


}
