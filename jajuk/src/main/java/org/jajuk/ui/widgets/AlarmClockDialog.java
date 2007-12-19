/*
 *  Jajuk
 *  Copyright (C) 2004 The Jajuk Team
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
 *  $Revision: 3156 $
 */

package org.jajuk.ui.widgets;

import org.jajuk.Main;
import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.Messages;
import org.jajuk.util.ITechnicalStrings;

import java.text.NumberFormat;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

/**
 * Alarm Clock Dialog window
 */
public class AlarmClockDialog extends JDialog implements ActionListener, ITechnicalStrings {
  private static final long serialVersionUID = 1L;
  
  JPanel jpAlarmClock;
  
  JPanel jpOKCancel;
  
  JButton jbOK;
  
  JButton jbCancel;
  
  JPanel jpFields;
  
  JPanel jpFields2;
  
  JLabel jlTime;
  
  JTextField jtfHour;
  
  JTextField jtfMinutes;
  
  JTextField jtfSeconds;
  
  boolean choice;
  
  public AlarmClockDialog(){ 
    jlTime = new JLabel(Messages.getString("AlarmDialog.0"));
    
    jtfHour = new JTextField(2);
    jtfMinutes = new JTextField(2);
    jtfSeconds = new JTextField(2);
    
    jpFields = new JPanel();
    jpFields.add(jlTime);
    jpFields.add(jtfHour);
    jpFields.add(jtfMinutes);
    jpFields.add(jtfSeconds);
    jpFields.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    jpOKCancel = new JPanel();
    jpOKCancel.setLayout(new FlowLayout());
    jbOK = new JButton(Messages.getString("Ok"));
    jbOK.addActionListener(this);
    jpOKCancel.add(jbOK);
    jbCancel = new JButton(Messages.getString("Cancel"));
    jbCancel.addActionListener(this);
    jpOKCancel.add(jbCancel);
    jpOKCancel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    jpAlarmClock = new JPanel(new BorderLayout());
    jpAlarmClock.add(jpFields, BorderLayout.CENTER);
    jpAlarmClock.add(jpOKCancel,BorderLayout.PAGE_END);
    jpAlarmClock.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    
    setTitle("Set Alarm Time");
    setMinimumSize(new Dimension(250, 100));
    setContentPane(jpAlarmClock);
    setModal(true);
    setAlwaysOnTop(true);
    pack();
    setLocationRelativeTo(Main.getWindow());
    setVisible(true);
    JDialog x = new JDialog();
  }
  
  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == jbOK){
      ConfigurationManager.setProperty(ALARM_TIME_HOUR, "" + jtfHour.getText());
      ConfigurationManager.setProperty(ALARM_TIME_MINUTES, "" + jtfMinutes.getText());
      ConfigurationManager.setProperty(ALARM_TIME_SECONDS, "" + jtfSeconds.getText());
      choice = true;
      dispose();
    } else if (e.getSource() == jbCancel){
      choice = false;
      dispose();
    }
  }
  
  public boolean getChoice(){
    return choice;
  }
}
  
  
  