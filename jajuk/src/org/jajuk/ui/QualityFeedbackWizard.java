/*
 *  Jajuk
 *  Copyright (C) 2005 Bertrand Florat
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

package org.jajuk.ui;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jajuk.Main;
import org.jajuk.i18n.Messages;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.log.Log;
import org.jdesktop.jdic.desktop.Desktop;
import org.jdesktop.jdic.desktop.Message;


/**
 * 
 *  Quality Feedback agent
 *
 * @author     Bertrand Florat
 * @created    20 juin 2005
 */
class QualityFeedbackWizard extends JDialog implements KeyListener,ActionListener,ITechnicalStrings{ 
    JPanel jpMain;
    JLabel jlFrom;
    JTextField jtfFrom;
    JLabel jlDesc;
    JTextField jtfDesc;
    JLabel jlDetail;
    JTextArea jtaDetail;
    OKCancelPanel okp;
    
    /**
     * Constructor
     */
    public QualityFeedbackWizard() {
        super(Main.getWindow(),Messages.getString("JajukJMenuBar.19")); //$NON-NLS-1$
        getContentPane().setPreferredSize(new Dimension(600,250));
        //From
        jlFrom = new JLabel(Messages.getString("QualityFeedbackWizard.1")); //$NON-NLS-1$
        jlFrom.setToolTipText(Messages.getString("QualityFeedbackWizard.2")); //$NON-NLS-1$
        jtfFrom = new JTextField();
        jtfFrom.setToolTipText(Messages.getString("QualityFeedbackWizard.2")); //$NON-NLS-1$
       //Description
        jlDesc = new JLabel(Messages.getString("QualityFeedbackWizard.3")); //$NON-NLS-1$
        jlDesc.setToolTipText(Messages.getString("QualityFeedbackWizard.4")); //$NON-NLS-1$
        jtfDesc = new JTextField();
        jtfDesc.setToolTipText(Messages.getString("QualityFeedbackWizard.4")); //$NON-NLS-1$
        jtfDesc.addKeyListener(this);
        //Details
        jlDetail = new JLabel(Messages.getString("QualityFeedbackWizard.5")); //$NON-NLS-1$
        jlDetail.setToolTipText(Messages.getString("QualityFeedbackWizard.6")); //$NON-NLS-1$
        jtaDetail = new JTextArea();
        jtaDetail.setToolTipText(Messages.getString("QualityFeedbackWizard.6")); //$NON-NLS-1$
        okp = new OKCancelPanel(this);
        okp.getOKButton().setEnabled(false);
        jpMain = new JPanel();
        int iXSeparator = 10;
        int iYSeparator = 20;
        double[][] dSize = {
                {iXSeparator,0.3,iXSeparator,0.7,iXSeparator},
                {iYSeparator,20,iYSeparator,20,iYSeparator,60,3*iYSeparator,20,iYSeparator} };
        jpMain.setLayout(new TableLayout(dSize));
        jpMain.add(jlFrom,"1,1"); //$NON-NLS-1$
        jpMain.add(jtfFrom,"3,1"); //$NON-NLS-1$
        jpMain.add(jlDesc,"1,3"); //$NON-NLS-1$
        jpMain.add(jtfDesc,"3,3"); //$NON-NLS-1$
        jpMain.add(jlDetail,"1,5"); //$NON-NLS-1$
        jpMain.add(jtaDetail,"3,5"); //$NON-NLS-1$
        jpMain.add(okp,"3,7"); //$NON-NLS-1$
        getContentPane().add(jpMain);
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                jtfFrom.requestFocusInWindow();
            }
        });
        getRootPane().setDefaultButton(okp.getOKButton());
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
        if (ae.getSource().equals(this.okp.getOKButton())){
            try{
                Message message = new Message();
                message.setSubject(jtfDesc.getText());
                ArrayList alTo = new ArrayList();
                alTo.add(FEEDBACK_EMAIL);
                message.setToAddrs(alTo);
                String sBody = ""; //$NON-NLS-1$
                sBody += "From: "+jtfFrom.getText()+'\n'; //$NON-NLS-1$
                sBody += "Subject: "+jtfDesc.getText()+'\n'; //$NON-NLS-1$
                sBody += "Details: "+jtaDetail.getText()+'\n'; //$NON-NLS-1$
                sBody += "Version: "+JAJUK_VERSION+'\n'; //$NON-NLS-1$
                sBody += System.getProperties().toString()+'\n';
                sBody += ConfigurationManager.getProperties().toString()+'\n';
                Iterator it = Log.getSpool();
                while (it.hasNext()){
                    sBody += it.next().toString() +'\n';
                }
                message.setBody(sBody);
                Desktop.mail(message);
                dispose();
            }
            catch(Exception e){
                Messages.showErrorMessage("136"); //$NON-NLS-1$
                Log.error(e);
            }
        }
        else if (ae.getSource().equals(this.okp.getCancelButton())){
            dispose();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    public void keyTyped(KeyEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
    }

    /* (non-Javadoc)
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
           if (!jtfDesc.getText().trim().equals("")){ //$NON-NLS-1$
            okp.getOKButton().setEnabled(true);
        }
        else{
            okp.getOKButton().setEnabled(false);
        }
    }
}