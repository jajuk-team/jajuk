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

package org.jajuk.ui.views;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.SwingUtilities;

import layout.TableLayout;

import org.jajuk.base.Device;
import org.jajuk.base.DeviceManager;
import org.jajuk.base.File;
import org.jajuk.base.FileManager;
import org.jajuk.base.Style;
import org.jajuk.base.StyleManager;
import org.jajuk.base.Track;
import org.jajuk.base.TrackManager;
import org.jajuk.ui.ObservationManager;
import org.jajuk.ui.Observer;
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
 * @author     bflorat
 * @created   24 dec. 2003
 */
public class StatView extends ViewAdapter implements Observer{

	/**Self instance*/
	private static StatView sv;
	
	/**Return self instance*/
	public static StatView getInstance(){
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
	public void display(){
		double[][] size = new double[][]{
				{0.5f,0.5f},
				{0.5f,0.5f}
		};
		setLayout(new TableLayout(size));
		ObservationManager.register(EVENT_DEVICE_DELETE,this);
		ObservationManager.register(EVENT_DEVICE_REFRESH,this);
		update(EVENT_DEVICE_REFRESH);	
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
			if ( iTotal == 0){ //no data, leave empty
				return null;
			}
			it = tm.keySet().iterator();
			while (it.hasNext()){
				String sName = (String)it.next();
				Integer i = (Integer)tm.get(sName);
				double d = i.doubleValue();
				if ( d/iTotal < 0.05){ //less than 5% -> go to others
					dOthers += d;
				}
				else{
					pdata.setValue(sName,new Double(d/iTotal));
				}
			}
			if ( dOthers > 0){
				pdata.setValue("Others",new Double(dOthers/iTotal));
			}
			//chart
			jfchart = ChartFactory.createPie3DChart("Styles repartition",pdata,false,true,true);
			// set the background color for the chart...
			jfchart.setBackgroundPaint(Color.BLUE);
			PiePlot plot = (PiePlot) jfchart.getPlot();
			plot.setSectionLabelType(PiePlot.NAME_AND_PERCENT_LABELS);
			plot.setNoDataMessage("No data available");
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
	private ChartPanel createDeviceSize(){
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
			if ( lTotalSize == 0){ //no data, leave empty
				return null;
			}
			Iterator itDevices = alDevices.iterator();
			while (itDevices.hasNext()){
				Device device = (Device)itDevices.next();
				long lSize = lSizes[alDevices.indexOf(device)];
				if ( (double)lSize/lTotalSize < 0.05){ //less than 5% -> go to others
					dOthers += lSize;
				}
				else{
					pdata.setValue(device.getName(),new Double((double)lSize/1073741824));
				}
			}
			if ( dOthers > 0){
				pdata.setValue("Others",new Double(dOthers/1073741824));
			}
			//chart
			jfchart = ChartFactory.createPie3DChart("Size by device (Gb)",pdata,false,true,true);
			// set the background color for the chart...
			jfchart.setBackgroundPaint(Color.BLUE);
			PiePlot plot = (PiePlot) jfchart.getPlot();
			plot.setSectionLabelType(PiePlot.NAME_AND_VALUE_LABELS);
			plot.setNoDataMessage("No data available");
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
			cdata = DatasetUtilities.createCategoryDataset(new String[]{""},getMountsLabels(iMounthsNumber), data);
			//chart
			jfchart = ChartFactory.createBarChart3D(
					"Collection size by mounth",      // chart title
					"Mounths",               // domain axis label
					"Size (Gb)",                  // range axis label
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
			plot.setNoDataMessage("No data available");
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
			cdata = DatasetUtilities.createCategoryDataset(new String[]{""},getMountsLabels(iMounthsNumber), data);
			
			//chart
			jfchart = ChartFactory.createBarChart3D(
					"Total number of tracks by mounth",      // chart title
					"Mounths",               // domain axis label
					"Track number",                  // range axis label
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
			plot.setNoDataMessage("No data available");
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
		return "Statistics view";	
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.IView#getViewName()
	 */
	public String getViewName() {
		return "org.jajuk.ui.views.StatView";
	}

	/* (non-Javadoc)
	 * @see org.jajuk.ui.Observer#update(java.lang.String)
	 */
	public void update(final String subject) {
		if (EVENT_DEVICE_REFRESH.equals(subject) || EVENT_DEVICE_DELETE.equals(subject)){
			removeAll();
			ChartPanel cp1 = createStyleRepartition(); 
			if ( cp1!= null) add(cp1,"0,0");
			ChartPanel cp2 = createCollectionSize(); 
			if ( cp2!= null) add(cp2,"0,1");
			ChartPanel cp3 = createTrackNumber(); 
			if ( cp3!= null) add(cp3,"1,1");
			ChartPanel cp4 = createDeviceSize(); 
			if ( cp4!= null) add(cp4,"1,0");
			SwingUtilities.updateComponentTreeUI(StatView.getInstance());
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
				sMounths[iMounthsNumber-k] = new StringBuffer().append((iMounth/10==0)?"0":"").append(Integer.toString(iMounth)).append('/').append(Integer.toString(iYear)).toString();
				iMounth--;
				if (iMounth == 0){
					iMounth = 12;
					iYear --;
				}
			}
			sMounths[0]="before";
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
