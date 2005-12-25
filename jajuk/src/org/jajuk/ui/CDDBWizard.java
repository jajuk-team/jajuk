/**
 * 
 */
package org.jajuk.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.jajuk.Main;
import org.jajuk.base.Directory;
import org.jajuk.base.Event;
import org.jajuk.base.File;
import org.jajuk.base.ObservationManager;
import org.jajuk.base.Track;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.table.DefaultTableColumnModelExt;
import org.jdesktop.swingx.table.TableColumnExt;

import entagged.freedb.Freedb;
import entagged.freedb.FreedbAlbum;
import entagged.freedb.FreedbException;
import entagged.freedb.FreedbQueryResult;
import entagged.freedb.FreedbReadResult;

/**
 * @author dhalsim
 * @created 15 december 2005
 */
public class CDDBWizard extends JDialog implements ITechnicalStrings, ActionListener,
        TableColumnModelListener, TableModelListener, MouseListener {

    /** Main panel */
    JPanel jpMain;
    NavigationPanel jpNav;
    JajukTable jtable;
    CDDBTableModel model;    
    JDialog dial;

    /** OK/Cancel panel */
    OKCancelPanel okc;
    OKCancelPanel conf;   

    /** Layout dimensions */
    double[][] dSize = { { 0, TableLayout.FILL  }, { 0, 22, TableLayout.PREFERRED, 22 } };

    /** Items */
    ArrayList<Track> alTracks;

    /** Freedb Items */
    Freedb fdb;
    FreedbAlbum fdbAlbum;
    FreedbQueryResult[] aResult;
    FreedbReadResult fdbReader;

    Vector vAlbums;

    int[] aIdxToTag;

    boolean bFinished;

    int idx;

    class NavigationPanel extends JPanel {

        public SteppedComboBox jcbAlbum;

        JLabel jlCurrent;

        JPanel jpButtons;
        
        JLabel jlGenre;
        JTextField jtGenre;
        JLabel jlAlbum;
        JButton jbPrev;

        JButton jbNext;

        NavigationPanel() {

            // Albums List
            
            jlAlbum = new JLabel (Messages.getString("CDDBWizard.5"));
            
            jcbAlbum = new SteppedComboBox();

            // add all matches
            jcbAlbum.setModel(new DefaultComboBoxModel(vAlbums));
            int iWidth = (int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2);
            jcbAlbum.setPopupWidth(iWidth);
            jcbAlbum.setSelectedIndex(idx);
            jcbAlbum.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    idx = jcbAlbum.getSelectedIndex();
                    Log.getInstance().debug("Select "+jcbAlbum.getSelectedIndex()+" index");
                    display();
                }
            });

            jlGenre = new JLabel (Messages.getString("CDDBWizard.16"));            
            jtGenre = new JTextField (fdbReader.getGenre());
            jtGenre.setEditable(false);
            
            // Current Proposition

            jlCurrent = new JLabel( (idx+1) + "/" + aResult.length);

            // Prev / Next buttons
            jbPrev = new JButton(Util.getIcon(ICON_PREVIOUS));
            jbPrev.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            jbPrev.addMouseListener(CDDBWizard.this);
            jbNext = new JButton(Util.getIcon(ICON_NEXT));
            jbNext.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            jbNext.addMouseListener(CDDBWizard.this);

            int iXspace = 5;
            double sizeControl[][] = {
                    { iXspace,TableLayout.FILL, iXspace, 350, iXspace, TableLayout.FILL, iXspace,TableLayout.FILL, iXspace, TableLayout.FILL, iXspace },
                    { 22 } };

            setLayout(new TableLayout(sizeControl));

            add(jlAlbum,"1,0");
            add(jcbAlbum, "3,0");
            add(jlGenre,"5,0");
            add(jtGenre,"7,0");
            //add(jbPrev, "3,0");
            //add(jbNext, "5,0");
            add(jlCurrent, "9   ,0");
            setMinimumSize(new Dimension(0, 0)); // allow resing with info node
            pack();
        }
    }

    public CDDBWizard(Directory dir) {
        // windows title: absolute path name of the given directory
        super(Main.getWindow(), dir.getAbsolutePath(), true); // modal //$NON-NLS-1$

        // Search all tracks in the given directory
        Set files = dir.getFiles();
        alTracks = new ArrayList(files.size());
        for (File file : dir.getFiles()) {
            Track track = file.getTrack();
            if (!alTracks.contains(track)) {
                alTracks.add(track);
            }
        }

        // Put an error message if no tracks were found
        if (alTracks.size() == 0) {
            InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.14"), 2);
            return;
            
        } 
        // Put a message that show the query is running
        else {
            InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.11"), 0);

            // Perform CDDB Query
            idx = performQuery(alTracks);

            // Put an error message if CDDB query don't found any matches
            if (idx < 0) {
                InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.12"), 2);
                return;
                
            } 
            // Put a message that show possible matches are found
            else {
                InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.13"), 0);

                // create Main panel
                jpMain = new JPanel();
                jpMain.setBorder(BorderFactory.createEtchedBorder());
                jpMain.setLayout(new TableLayout(dSize));

                // Display main panel
                display();
            }
        }
    }

    /** Fill the table */
    public JajukTable refreshTable(FreedbQueryResult fdbResult) {
        try {
            fdbReader = fdb.read(fdbResult);
        } catch (FreedbException e) {
            Log.getInstance().debug(e.getLocalizedMessage());
        }
        CDDBTableModel model = new CDDBTableModel(alTracks);
        model.populateModel(fdbReader);
        model.fireTableDataChanged();
        model.addTableModelListener(this);
        this.model = model;
        jtable = new JajukTable(model, true);
        jtable.selectAll();
        jtable.getColumnModel().addColumnModelListener(this);
        jtable.setBorder(BorderFactory.createEtchedBorder());
        new TableTransferHandler(jtable, DnDConstants.ACTION_COPY_OR_MOVE);
        jtable.packAll();
        return jtable;
    }


    public void display() {
        // Create UI
        if (jpMain.getComponentCount() > 0) {
            jpMain.removeAll();
        }
        jtable = refreshTable(aResult[idx]);
        jpNav = new NavigationPanel();
        okc = new OKCancelPanel(CDDBWizard.this, Messages.getString("Apply"), Messages
                .getString("Close"));

        jpMain.add(jpNav, "1,1");
        jpMain.add(new JScrollPane(jtable), "1,2");
        jpMain.add(okc, "1,3");

        getRootPane().setDefaultButton(okc.getOKButton());
        getContentPane().add(jpMain);
        setResizable(false);
        pack();
        setLocationRelativeTo(Main.getWindow());
        setVisible(true);
    }

    public int performQuery(ArrayList alItems) {
        fdb = new Freedb();
        Track[] alTracks = new Track[alItems.size()];
        alItems.toArray(alTracks);

        fdbAlbum = new FreedbAlbum(alTracks);

        try {
            aResult = fdb.query(fdbAlbum);

            vAlbums = new Vector(aResult.length);
            Log.getInstance().debug("CDDB Query return " + aResult.length + " match(es).");
            int idx = 0;
            for (int i = 0; i < aResult.length; i++) {
                vAlbums.add("["+aResult[i].getDiscId()+"] "+aResult[i].getAlbum());
                if (aResult[i].isExactMatch()) {
                    idx = i;
                    InformationJPanel.getInstance().setMessage(Messages.getString("CDDBWizard.17"), 0);
                }
            }
            return idx;
        } catch (FreedbException e) {
            Log.getInstance().debug(e.getLocalizedMessage());
        }
        return -1;
    }

    public void retagFiles() {
        aIdxToTag = jtable.getSelectedRows();
        if (aIdxToTag.length == 0){
            dispose();
        }
        else {            
            for (int i = 0;i<aIdxToTag.length;i++){
                int iRow = aIdxToTag[i];
                String sTrack = (String) model.oValues[iRow][3];
                String sAlbum = fdbReader.getAlbum();
                Track track = alTracks.get(iRow);
                track.setName(sTrack);     
                track.getAlbum().setName(sAlbum);       
                track.getStyle().setName(fdbReader.getGenre());
                track.getAuthor().setName(fdbReader.getArtist());                
            }
            ObservationManager.notify(new Event(EVENT_DEVICE_REFRESH));
            dispose();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == okc.getCancelButton()) {
            dispose();
        }
        if (e.getSource() == okc.getOKButton()) {      
            dial = new JDialog (this,Messages.getString("warning"));           
            JLabel lab = new JLabel (Messages.getString("CDDBWizard.15"));            
            
            
            conf = new OKCancelPanel(CDDBWizard.this,Messages.getString("yes"),Messages.getString("no"));
            double size [][]= { {TableLayout.FILL},{22,22} };
            dial.setLayout(new TableLayout(size));
            
            lab.setAlignmentY(Component.CENTER_ALIGNMENT);
            dial.add(lab,"0,0");
            dial.add(conf,"0,1");            
            dial.pack();
            dial.setLocationRelativeTo(this);
            dial.setVisible(true);
            
        }
        if (e.getSource() == conf.getOKButton()) {
            dial.dispose();
            retagFiles();
            
        }
        if (e.getSource() == conf.getCancelButton()) {
            dial.dispose();
        }        
    }

    public void columnMoved(TableColumnModelEvent arg0) {
    }

    public void columnAdded(TableColumnModelEvent e) {
    }

    public void columnMarginChanged(ChangeEvent e) {
    }

    public void columnRemoved(TableColumnModelEvent e) {
    }

    public void columnSelectionChanged(ListSelectionEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void tableChanged(TableModelEvent e) {
    }
}
