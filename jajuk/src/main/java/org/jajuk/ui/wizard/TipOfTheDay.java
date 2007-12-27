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
package org.jajuk.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jajuk.util.ConfigurationManager;
import org.jajuk.util.ITechnicalStrings;
import org.jajuk.util.IconLoader;
import org.jajuk.util.Messages;

public class TipOfTheDay extends JFrame implements ITechnicalStrings {

  private static final long serialVersionUID = 1L;

  private static final String[] tips = Messages.getAll("TipOfTheDay");

  private int iLastTip;

  private JCheckBox cbShow;

  private JTextArea tipArea;

  private JLabel lCounter;

  public TipOfTheDay() {
    super(Messages.getString("TipOfTheDayView.0"));
    setAlwaysOnTop(true);
    setIconImage(IconLoader.ICON_LOGO.getImage());
    this.iLastTip = ConfigurationManager.getInt(CONF_TIP_OF_DAY_INDEX);

    cbShow = new JCheckBox(Messages.getString("TipOfTheDayView.2"));
    cbShow.setSelected(ConfigurationManager.getBoolean(CONF_SHOW_TIP_ON_STARTUP));

    tipArea = new JTextArea();
    tipArea.setWrapStyleWord(true);
    tipArea.setLineWrap(true);
    tipArea.setEditable(false);

    lCounter = new JLabel("999/999");
    JButton bNext = new JButton(IconLoader.ICON_NEXT);
    bNext.setMargin(new Insets(1, 1, 1, 1));
    bNext.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        iLastTip = (iLastTip + 1) % TipOfTheDay.tips.length;
        setTip(iLastTip);
      }
    });

    JButton bPrevious = new JButton(IconLoader.ICON_PREVIOUS);
    bPrevious.setMargin(new Insets(1, 1, 1, 1));
    bPrevious.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        iLastTip = iLastTip - 1;
        if (iLastTip == -1) {
          iLastTip = TipOfTheDay.tips.length - 1;
        }
        setTip(iLastTip);
      }
    });

    JButton bClose = new JButton(IconLoader.ICON_OK);
    bClose.setMaximumSize(bClose.getPreferredSize());
    bClose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent actionevent) {
        setVisible(false);
      }
    });

    JScrollPane scroll = new JScrollPane(tipArea);
    scroll.setPreferredSize(new Dimension(200, 100));

    JLabel lTitle = new JLabel(Messages.getString("TipOfTheDayView.1"), JLabel.LEFT);
    Font fTitle = lTitle.getFont();
    lTitle.setFont(new Font(fTitle.getName(), fTitle.getStyle(), (int) (fTitle.getSize() * 1.3)));
    JLabel lIcon = new JLabel(IconLoader.ICON_TIP, JLabel.LEFT);

    JPanel pTop = new JPanel(new BorderLayout());
    pTop.add(lIcon, BorderLayout.WEST);
    pTop.add(lTitle, BorderLayout.CENTER);

    JPanel pCenter = new JPanel(new BorderLayout());
    pCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 3, 10));
    pCenter.add(scroll, BorderLayout.CENTER);

    JPanel pPrevNext = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    pPrevNext.add(bPrevious);
    pPrevNext.add(lCounter);
    pPrevNext.add(bNext);

    JPanel pControls = new JPanel(new BorderLayout());
    pControls.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
    pControls.add(cbShow, BorderLayout.WEST);
    pControls.add(pPrevNext);

    JPanel pButton = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    pButton.add(bClose);

    JPanel pBottom = new JPanel(new BorderLayout());
    pBottom.add(pControls, BorderLayout.NORTH);
    pBottom.add(pButton);

    add(pTop, BorderLayout.NORTH);
    add(pCenter, BorderLayout.CENTER);
    add(pBottom, BorderLayout.SOUTH);

    setTip(this.iLastTip);
    pack();
    if (getWidth() < 400) {
      setSize(400, getHeight());
    }
  }

  public void setTip(int p) {
    int i = p;
    iLastTip = i;
    if (i >= tips.length) {
      i = 0;
    }
    tipArea.setText(tips[i]);
    lCounter.setText((new StringBuilder()).append("").append(i + 1).append("/").append(tips.length)
        .toString());
    tipArea.setCaretPosition(0);
  }

  public void setVisible(boolean flag) {
    super.setVisible(flag);
    if (flag) {
      toFront();
    } else {
      ConfigurationManager.setProperty(CONF_TIP_OF_DAY_INDEX, String.valueOf((iLastTip + 1)
          % TipOfTheDay.tips.length));
      ConfigurationManager.setProperty(CONF_SHOW_TIP_ON_STARTUP, String
          .valueOf(cbShow.isSelected()));
    }
  }
}