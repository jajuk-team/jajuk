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
 *  $$Revision: 2482 $$
 */
package org.jajuk.ui.wizard;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.jajuk.Main;
import org.jajuk.base.WebRadio;
import org.jajuk.services.events.Event;
import org.jajuk.services.events.ObservationManager;
import org.jajuk.services.webradio.WebRadioManager;
import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.util.EventSubject;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;
import org.jajuk.util.Util;
import org.jajuk.util.log.Log;
import org.qdwizard.Screen;
import org.qdwizard.Wizard;

/**
 * WebRadio management wizard
 */
public class WebRadioWizard extends Wizard implements ITechnicalStrings {

  /** web radios* */
  static ArrayList<WebRadio> radios;

  public static class RadioPanel extends Screen implements ActionListener {

    private static final long serialVersionUID = 1L;

    /** All dynamic widgets */
    JComponent[][] widgets;

    JButton jbNew;

    JButton jbDelete;

    JButton jbDefaults;

    JPanel jpButtons;

    JScrollPane jsp;

    /** Selected radio index */
    int radioIndex = 0;

    public String getDescription() {
      return Messages.getString("RadioWizard.0");
    }

    public String getName() {
      return Messages.getString("RadioWizard.1");
    }

    /**
     * Create panel UI
     * 
     */
    public void initUI() {
      radios = new ArrayList<WebRadio>(WebRadioManager.getInstance().getWebRadios());
      setCanFinish(true);
      // set layout
      double[][] dSizeGeneral = { { 10, 0.99, 5 },
          { 10, TableLayout.FILL, 10, TableLayout.PREFERRED, 10 } };
      setLayout(new TableLayout(dSizeGeneral));
      // button layout
      double[][] dButtons = { { 10, 0.33, 5, 0.33, 5, 0.33, 10 }, { 20 } };
      jpButtons = new JPanel(new TableLayout(dButtons));
      jbNew = new JButton(Messages.getString("RadioWizard.2"), IconLoader.ICON_NEW);
      jbNew.addActionListener(this);
      jbNew.setToolTipText(Messages.getString("RadioWizard.2"));
      jbDelete = new JButton(Messages.getString("RadioWizard.3"), IconLoader.ICON_DELETE);
      jbDelete.addActionListener(this);
      jbDelete.setToolTipText(Messages.getString("RadioWizard.3"));
      jbDefaults = new JButton(Messages.getString("RadioWizard.4"), IconLoader.ICON_DEFAULTS);
      jbDefaults.addActionListener(this);
      jbDefaults.setToolTipText(Messages.getString("RadioWizard.4"));
      jpButtons.add(jbNew, "1,0");
      jpButtons.add(jbDelete, "3,0");
      jpButtons.add(jbDefaults, "5,0");
      add(getPanel(), "1,1");
      add(jpButtons, "1,3,c,c");
    }

    /**
     * 
     * @return a panel containing all items
     */
    @SuppressWarnings("unchecked")
    private JScrollPane getPanel() {
      widgets = new JComponent[radios.size()][3];
      JPanel out = new JPanel();
      double[] dHoriz = { 25, 250, 150 };
      double[] dVert = new double[widgets.length + 2];
      dVert[0] = 20;
      // make sure to sort radios
      Collections.sort(radios);
      ButtonGroup group = new ButtonGroup();
      // now add all web radios
      for (int index = 0; index < radios.size(); index++) {
        // Radio name
        final JTextField jtfName = new JTextField();
        jtfName.setText(radios.get(index).getName());
        jtfName.addCaretListener(new CaretListener() {
          public void caretUpdate(CaretEvent arg0) {
            int index = getWidgetIndex(widgets, (JComponent) arg0.getSource());
            String s = jtfName.getText();
            // Check this name is not already token
            for (int i = 0; i < widgets.length; i++) {
              if (i == index) {
                continue;
              }
              JTextField jtf = (JTextField) widgets[i][1];
              if (jtf.getText().equals(s)) {
                setProblem(Messages.getString("RadioWizard.5"));
                return;
              }
            }
            // reset previous problems
            if (s.length() == 0 || ((JTextField) widgets[index][2]).getText().length() == 0) {
              setProblem(Messages.getString("RadioWizard.11"));
            } else {
              setProblem(null);
              jtfName.setToolTipText(s);
            }
            radios.get(index).setName(s);
          }
        });
        jtfName.setToolTipText(jtfName.getText());
        widgets[index][1] = jtfName;
        // radio button
        final JRadioButton jrbRadio = new JRadioButton();
        group.add(jrbRadio);
        jrbRadio.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            ((JTextField) widgets[getWidgetIndex(widgets, jrbRadio)][1]).getText();
            radioIndex = getWidgetIndex(widgets, jrbRadio);
          }
        });
        widgets[index][0] = jrbRadio;
        if (index == radioIndex) {
          jrbRadio.setSelected(true);
        }
        WebRadio radio = radios.get(index);
        // URL
        final JTextField jtfURL = new JTextField();
        if (radio.getUrl() != null) {
          jtfURL.setText(radio.getUrl().toString());
          jtfURL.setToolTipText(jtfURL.getText());
        }
        jtfURL.addCaretListener(new CaretListener() {
          public void caretUpdate(CaretEvent arg0) {
            int index = getWidgetIndex(widgets, (JComponent) arg0.getSource());
            String s = jtfURL.getText();
            // reset previous problems
            if (s.length() == 0 || ((JTextField) widgets[index][1]).getText().length() == 0) {
              setProblem(Messages.getString("RadioWizard.11"));
            } else {
              setProblem(null);
              jtfURL.setToolTipText(s);
            }
            radios.get(index).setUrl(s);
          }
        });
        widgets[index][2] = jtfURL;
        // Set layout
        dVert[index + 1] = 20;
      }
      dVert[widgets.length + 1] = 20;
      // Create layout
      double[][] dSizeProperties = new double[][] { dHoriz, dVert };
      TableLayout layout = new TableLayout(dSizeProperties);
      layout.setHGap(10);
      layout.setVGap(10);
      out.setLayout(layout);
      // Create header
      JLabel jlHeader1 = new JLabel(Messages.getString("RadioWizard.9"));
      jlHeader1.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      JLabel jlHeader2 = new JLabel(Messages.getString("RadioWizard.8"));
      jlHeader2.setFont(FontManager.getInstance().getFont(JajukFont.BOLD));
      out.add(jlHeader1, "1,0,c,c");
      out.add(jlHeader2, "2,0,c,c");
      // Add widgets
      for (int i = 0; i < dVert.length - 2; i++) {
        out.add(widgets[i][0], "0," + (i + 1) + ",c,c");
        out.add(widgets[i][1], "1," + (i + 1));
        out.add(widgets[i][2], "2," + (i + 1));
      }
      jsp = new JScrollPane(out);
      // select first ambiance found
      if (radios.size() > 0) {
        JRadioButton jrb = (JRadioButton) widgets[0][0];
        jrb.doClick();
      }
      return jsp;
    }

    /**
     * Refresh panel
     */
    private void refreshScreen() {
      removeAll();
      // refresh panel
      add(getPanel(), "1,1");
      add(jpButtons, "1,3,c,c");
      revalidate();
      repaint();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent ae) {
      if (ae.getSource() == jbNew) {
        radios.add(new WebRadio("", null));
        // refresh screen
        refreshScreen();
        // select new row
        JRadioButton jrb = (JRadioButton) widgets[0][0];
        jrb.setSelected(true);
        radioIndex = radios.size() - 1;
        setProblem(Messages.getString("RadioWizard.11"));
        jbDelete.setEnabled(true);
        JTextField jtf = (JTextField) widgets[0][1];
        jtf.requestFocusInWindow();
      } else if (ae.getSource() == jbDelete) {
        WebRadio radio = radios.get(radioIndex);
        radios.remove(radioIndex);
        WebRadioManager.getInstance().removeWebRadio(radio);
        if (WebRadioManager.getInstance().getWebRadios().size() == 0) {
          jbDelete.setEnabled(false);
        }
        if (radioIndex > 0) {
          radioIndex--;
          JRadioButton jrb = (JRadioButton) widgets[radioIndex][0];
          jrb.setSelected(true);
        }
        // refresh screen
        refreshScreen();
      } else if (ae.getSource() == jbDefaults) {
        // Ask for confirmation
        int choice = Messages.getChoice(Messages.getString("Confirmation_defaults_radios"),
            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
          return;
        }
        // OK ? Restore the list
        try {
          WebRadioManager.getInstance().restore();
          // Refresh current list
          radios = new ArrayList<WebRadio>(WebRadioManager.getInstance().getWebRadios());
        } catch (Exception e) {
          // show an "operation failed' message to users
          Messages.showErrorMessage(169);
          Log.error(e);
        }
        // refresh screen
        refreshScreen();
      }
    }
  }

  /**
   * 
   * @param widget
   * @return index of a given widget row in the widget table
   */
  private static int getWidgetIndex(JComponent[][] widgets, JComponent widget) {
    for (int row = 0; row < widgets.length; row++) {
      for (int col = 0; col < widgets[0].length; col++) {
        if (widget.equals(widgets[row][col])) {
          return row;
        }
      }
    }
    return -1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.wizard.Wizard#getPreviousScreen(java.lang.Class)
   */
  @Override
  public Class getPreviousScreen(Class screen) {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.wizard.Wizard#getNextScreen(java.lang.Class)
   */
  @Override
  public Class getNextScreen(Class screen) {
    return null;
  }

  public WebRadioWizard() {
    super(Messages.getString("RadioWizard.7"), RadioPanel.class, Util.getImage(IMAGE_WEBRADIO),
        Main.getWindow(), new Locale(Messages.getInstance().getLocale()), 700, 600);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.jajuk.ui.wizard.Wizard#finish()
   */
  @Override
  public void finish() {
    for (WebRadio radio : radios) {
      WebRadioManager.getInstance().addWebRadio(radio);
    }
    // commit it to avoid it is lost before the app close
    try {
      WebRadioManager.getInstance().commit();
    } catch (IOException e) {
      Log.error(e);
    }
    // Refresh UI
    ObservationManager.notify(new Event(EventSubject.EVENT_WEBRADIOS_CHANGE));
  }

}
