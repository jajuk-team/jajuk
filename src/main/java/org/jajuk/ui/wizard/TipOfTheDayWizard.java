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
import javax.swing.SwingConstants;

import org.jajuk.util.Conf;
import org.jajuk.util.Const;
import org.jajuk.util.IconLoader;
import org.jajuk.util.JajukIcons;
import org.jajuk.util.Messages;
import org.jajuk.util.UtilGUI;
import org.jajuk.util.UtilSystem;

/**
 * Small dialog which displays the "Tip of the Day" from a list of useful hints.
 */
public class TipOfTheDayWizard extends JFrame {
  /** Generated serialVersionUID. */
  private static final long serialVersionUID = 1L;
  /** The Constant TIPS.  */
  private static final String[] TIPS = Messages.getAll("TipOfTheDay");
  private final JCheckBox cbShow;
  private final JTextArea tipArea;
  private final JLabel lCounter;
  private int currentIndex = 0;

  /**
   * Instantiates a new tip of the day wizard.
   */
  public TipOfTheDayWizard() {
    super(Messages.getString("TipOfTheDayView.0"));
    setAlwaysOnTop(true);
    setIconImage(IconLoader.getIcon(JajukIcons.LOGO).getImage());
    cbShow = new JCheckBox(Messages.getString("TipOfTheDayView.2"));
    cbShow.setSelected(Conf.getBoolean(Const.CONF_SHOW_TIP_ON_STARTUP));
    tipArea = new JTextArea();
    tipArea.setWrapStyleWord(true);
    tipArea.setLineWrap(true);
    tipArea.setEditable(false);
    lCounter = new JLabel("999/999");
    JButton bNext = new JButton(IconLoader.getIcon(JajukIcons.PLAYER_NEXT_SMALL));
    bNext.setMargin(new Insets(1, 1, 1, 1));
    bNext.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionevent) {
        incIndex();
        updateTip();
      }
    });
    JButton bPrevious = new JButton(IconLoader.getIcon(JajukIcons.PLAYER_PREVIOUS_SMALL));
    bPrevious.setMargin(new Insets(1, 1, 1, 1));
    bPrevious.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionevent) {
        decIndex();
        updateTip();
      }
    });
    JButton bClose = new JButton(IconLoader.getIcon(JajukIcons.OK));
    bClose.setMaximumSize(bClose.getPreferredSize());
    bClose.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionevent) {
        setVisible(false);
      }
    });
    JScrollPane scroll = new JScrollPane(tipArea);
    scroll.setPreferredSize(new Dimension(200, 100));
    JLabel lTitle = new JLabel(Messages.getString("TipOfTheDayView.1"), SwingConstants.LEFT);
    Font fTitle = lTitle.getFont();
    lTitle.setFont(new Font(fTitle.getName(), fTitle.getStyle(), (int) (fTitle.getSize() * 1.3)));
    JLabel lIcon = new JLabel(IconLoader.getIcon(JajukIcons.TIP), SwingConstants.LEFT);
    JPanel pTop = new JPanel(new BorderLayout());
    pTop.add(lIcon, BorderLayout.WEST);
    pTop.add(lTitle, BorderLayout.CENTER);
    UtilGUI.setEscapeKeyboardAction(this, pTop);
    JPanel pCenter = new JPanel(new BorderLayout());
    pCenter.setBorder(BorderFactory.createEmptyBorder(10, 10, 3, 10));
    pCenter.add(scroll, BorderLayout.CENTER);
    UtilGUI.setEscapeKeyboardAction(this, pCenter);
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
    UtilGUI.setEscapeKeyboardAction(this, pBottom);
    add(pTop, BorderLayout.NORTH);
    add(pCenter, BorderLayout.CENTER);
    add(pBottom, BorderLayout.SOUTH);
    // Display a shuffled tip of the day
    shuffleIndex();
    updateTip();
    pack();
    if (getWidth() < 400) {
      setSize(400, getHeight());
    }
  }

  /**
   * Show random TOTD.
   */
  private void shuffleIndex() {
    currentIndex = (int) (UtilSystem.getRandom().nextFloat() * (TIPS.length - 1));
  }

  /**
   * Increment the TOTD index.
   */
  private void incIndex() {
    currentIndex = (currentIndex + 1) % TIPS.length;
  }

  /**
   * Decrement the TOTD index.
   */
  private void decIndex() {
    if (currentIndex == 0) {
      currentIndex = TIPS.length - 1;
    } else {
      currentIndex--;
    }
  }

  /**
   * Update the TOTD with index from Conf.
   */
  private final void updateTip() {
    tipArea.setText(TIPS[currentIndex]);
    lCounter.setText(new StringBuilder().append("").append(currentIndex + 1).append("/")
        .append(TIPS.length).toString());
    tipArea.setCaretPosition(0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.Window#setVisible(boolean)
   */
  @Override
  public void setVisible(boolean flag) {
    super.setVisible(flag);
    if (flag) {
      toFront();
    } else {
      // Called when closing the window, inc the totd index for next display
      Conf.setProperty(Const.CONF_SHOW_TIP_ON_STARTUP, String.valueOf(cbShow.isSelected()));
    }
  }
}