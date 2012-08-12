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
package org.qdwizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.jajuk.ui.widgets.JajukJDialog;
import org.jajuk.util.log.Log;

/**
 * A Wizard dialog displaying one to many screens
 * <ul>
 * <li>Create a class that extends Wizard. You have to implement
 * getPreviousScreen(), getNextScreen() and finish() abstract methods</li>
 * <li> Displaying the wizard:</li>
 * 
 * <pre>
 * MyWizard wizard = new MyWizard(String sName,Class initial,
 * ImageIcon icon,Frame parentWindow,
 * Locale locale,int iHSize,int iVSize);
 * wizard.show();
 * </pre>
 * 
 * <li>finish() method implements actions to be done at the end of the wizard</li>
 * <li>getPreviousScreen() and getNextScreen() have to return previous or next
 * screen class. Example:</li>
 * 
 * <pre>
 * public Class getNextScreen(Class screen) {
 * if (ActionSelectionPanel.class.equals(getCurrentScreen())) {
 * String sAction = (String) data.get(KEY_ACTION);
 * if (ActionSelectionPanel.ACTION_CREATION.equals(sAction)) {
 * return TypeSelectionPanel.class;
 * } else if (ActionSelectionPanel.ACTION_DELETE.equals(sAction)) {
 * return RemovePanel.class;
 * }
 * }
 * }
 * </pre>
 * 
 * </ul>
 * 
 * @author Bertrand Florat
 * @created 1 may 2006
 */
public abstract class Wizard implements ActionListener, WindowListener {
  /** Wizard name. */
  private String sName;
  /** Current screen. */
  private Screen current;
  /** Wizard left side icon. */
  private ImageIcon icon;
  /** Wizard data. */
  protected final static Map<String, Object> data = new HashMap<String, Object>(10);
  /** Wizard header. */
  private Header header;
  /** Wizard action Panel. */
  private ActionsPanel actions;
  /** Wizard dialog. */
  private JDialog dialog;
  /** Parent window. */
  private Frame parentWindow;
  /** Screens instance repository. */
  private Map<Class<? extends Screen>, Screen> hmClassScreens = new HashMap<Class<? extends Screen>, Screen>(
      10);
  /** Default Wizard size. */
  protected static final int DEFAULT_H_SIZE = 700;
  /** The Constant DEFAULT_V_SIZE.   */
  protected static final int DEFAULT_V_SIZE = 500;
  /** The Constant DEFAULT_H_LAYOUT_PADDING.   */
  protected static final int DEFAULT_H_LAYOUT_PADDING = 5;
  /** The Constant DEFAULT_V_LAYOUT_PADDING.   */
  protected static final int DEFAULT_V_LAYOUT_PADDING = 5;
  /** Was the Wizard Canceled?. */
  private boolean bCancelled;
  /** Layout Padding. */
  private int layoutHPadding = DEFAULT_H_LAYOUT_PADDING;
  private int layoutVPadding = DEFAULT_V_LAYOUT_PADDING;

  /**
   * Wizard constructor.
   * 
   * @param sName Wizard name displayed in dialog title
   * @param initial Initial screen class
   * @param icon Wizard icon (null if no icon)
   * @param backgroundImage background image
   * @param parentWindow wizard parent window
   * @param locale Wizard locale
   * @param iHSize Horizontal size
   * @param iVSize Vertical size
   * @param iLayoutHPadding Horizontal layout padding
   * @param iLayoutVPadding Vertical layout padding
   */
  public Wizard(String sName, Class<? extends Screen> initial, ImageIcon icon,
      Image backgroundImage, Frame parentWindow, Locale locale, int iHSize, int iVSize,
      int iLayoutHPadding, int iLayoutVPadding) {
    bCancelled = false;
    this.sName = sName;
    this.parentWindow = parentWindow;
    if (locale != null) {
      Langpack.setLocale(locale);
    } else {
      Langpack.setLocale(Locale.getDefault());
    }
    this.icon = icon;
    this.layoutHPadding = iLayoutHPadding;
    this.layoutVPadding = iLayoutVPadding;
    createUI();
    header.setImage(backgroundImage);
    setScreen(initial);
    current.onEnter();
    dialog.setSize(iHSize, iVSize);
  }

  /**
   * Wizard constructor.
   * 
   * @param sName Wizard name displayed in dialog title
   * @param initial Initial screen class
   * @param icon Wizard icon (null if no icon)
   * @param parentWindow wizard parent window
   * @param locale Wizard locale
   * @param iHSize Horizontal size
   * @param iVSize Vertical size
   */
  public Wizard(String sName, Class<? extends Screen> initial, ImageIcon icon, Frame parentWindow,
      Locale locale, int iHSize, int iVSize) {
    bCancelled = false;
    this.sName = sName;
    this.parentWindow = parentWindow;
    if (locale != null) {
      Langpack.setLocale(locale);
    } else {
      Langpack.setLocale(Locale.getDefault());
    }
    this.icon = icon;
    createUI();
    setScreen(initial);
    current.onEnter();
    dialog.setSize(iHSize, iVSize);
  }

  /**
   * Wizard constructor.
   * 
   * @param sName Wizard name displayed in dialog title
   * @param initial Initial screen class
   * @param icon Wizard icon (null if no icon)
   * @param backgroundImage Wizard header background (null if no image)
   * @param parentWindow wizard parent window
   * @param locale Wizard locale
   */
  public Wizard(String sName, Class<? extends Screen> initial, ImageIcon icon,
      Image backgroundImage, Frame parentWindow, Locale locale) {
    this(sName, initial, icon, backgroundImage, parentWindow, locale, DEFAULT_H_SIZE,
        DEFAULT_V_SIZE, DEFAULT_H_LAYOUT_PADDING, DEFAULT_V_LAYOUT_PADDING);
  }

  /**
   * Wizard constructor.
   * 
   * @param sName Wizard name displayed in dialog title
   * @param initial Initial screen class
   * @param icon Wizard icon (null if no icon)
   * @param parentWindow wizard parent window
   * @param locale Wizard locale
   */
  public Wizard(String sName, Class<? extends Screen> initial, ImageIcon icon, Frame parentWindow,
      Locale locale) {
    this(sName, initial, icon, null, parentWindow, locale, DEFAULT_H_SIZE, DEFAULT_V_SIZE,
        DEFAULT_H_LAYOUT_PADDING, DEFAULT_V_LAYOUT_PADDING);
  }

  /**
   * Wizard constructor (uses default locale).
   * 
   * @param sName Wizard name displayed in dialog title
   * @param initial Initial screen class
   * @param icon Wizard icon (null if no icon)
   * @param backgroundImage Wizard header background (null if no image)
   * @param parentWindow wizard parent window
   */
  public Wizard(String sName, Class<? extends Screen> initial, ImageIcon icon,
      Image backgroundImage, Frame parentWindow) {
    this(sName, initial, icon, backgroundImage, parentWindow, Locale.getDefault());
  }

  /**
   * Wizard constructor (uses default locale).
   * 
   * @param sName Wizard name displayed in dialog title
   * @param initial Initial screen class
   * @param icon Wizard icon (null if no icon)
   * @param parentWindow wizard parent window
   */
  public Wizard(String sName, Class<? extends Screen> initial, ImageIcon icon, Frame parentWindow) {
    this(sName, initial, icon, null, parentWindow, Locale.getDefault());
  }

  /**
   * Show.
   * 
   */
  public void show() {
    dialog.setVisible(true);
  }

  /**
   * access to the JDialog of the wizard, in case we need it (for instance to
   * set a glass pane when waiting).
   * 
   * @return the wizard dialog
   */
  public JDialog getDialog() {
    return dialog;
  }

  /**
   * UI manager.
   */
  private void createUI() {
    dialog = new JajukJDialog(parentWindow, true);// modal
    // Set default size
    dialog.setSize(DEFAULT_H_SIZE, DEFAULT_V_SIZE);
    dialog.setTitle(sName);
    header = new Header();
    actions = new ActionsPanel(this);
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    dialog.addWindowListener(this);
    display();
    dialog.setLocationRelativeTo(parentWindow);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent ae) {
    dialog.setCursor(new Cursor(Cursor.WAIT_CURSOR));
    try {
      // Previous required. Note that the previous button is enabled only
      // if the user can go previous
      if (ae.getActionCommand().equals("Prev")) { //$NON-NLS-1$
        setScreen(getPreviousScreen(current.getClass()));
      } else if (ae.getActionCommand().equals("Next")) { //$NON-NLS-1$
        current.onNext();
        setScreen(getNextScreen(current.getClass()));
        current.onEnter();
      } else if (ae.getActionCommand().equals("Cancel")) { //$NON-NLS-1$
        current.onCancelled();
        data.clear();
        bCancelled = true;
        onCancel();
        dialog.dispose();
      } else if (ae.getActionCommand().equals("Finish")) { //$NON-NLS-1$
        current.onFinished();
        finish();
        dialog.dispose();
      }
    } finally {
      dialog.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
  }

  /**
   * Set the screen to display a a class.
   * 
   * @param screenClass 
   */
  private void setScreen(Class<? extends Screen> screenClass) {
    Screen screen = null;
    try {
      // If the class is an clear point, we clean up all previous screens
      if (Arrays.asList(screenClass.getInterfaces()).contains(ClearPoint.class)) {
        clearScreens();
        screen = screenClass.newInstance();
      }
      // otherwise, try to get a screen from buffer or create it if needed
      else {
        if (!hmClassScreens.containsKey(screenClass)) {
          screen = screenClass.newInstance();
          hmClassScreens.put(screenClass, screen);
        }
        screen = hmClassScreens.get(screenClass);
      }
    } catch (InstantiationException e) {
      Log.error(e);
      throw new RuntimeException("setScreen " + screenClass + " caused " + e.toString(), e);
    } catch (IllegalAccessException e) {
      Log.error(e);
      throw new RuntimeException("setScreen " + screenClass + " caused " + e.toString(), e);
    }
    current = screen;
    current.setWizard(this);
    current.setCanGoPrevious((getPreviousScreen(screenClass) != null));
    current.setCanGoNext((getNextScreen(screenClass) != null));
    String sDesc = screen.getDescription();
    if (sDesc != null) {
      header.setTitleText(screen.getName());
      header.setSubtitleText(sDesc);
    } else {
      header.setTitleText(screen.getName());
      header.setSubtitleText("");
    }
    display();
  }

  /**
   * Called at each screen refresh.
   */
  private void display() {
    ((JPanel) dialog.getContentPane()).removeAll();
    dialog.setLayout(new BorderLayout(layoutHPadding, layoutVPadding));
    if (icon != null) {
      final JLabel jlIcon = new JLabel(icon);
      dialog.add(jlIcon, BorderLayout.WEST);
      // Add a listener to resize left side image if wizard window is
      // resized
      jlIcon.addComponentListener(new ComponentListener() {
        @Override
        public void componentShown(ComponentEvent e) {
          // nothing to do here
        }

        /* (non-Javadoc)
         * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
         */
        @Override
        public void componentResized(ComponentEvent e) {
          Wizard.this.icon = getResizedImage(icon, jlIcon.getWidth(), jlIcon.getHeight());
          jlIcon.setIcon(Wizard.this.icon);
          jlIcon.setVisible(true);
          // Display icon new size, useful to create an image with
          // right default size
          System.out.println("Icon new size : " + jlIcon.getIcon().getIconWidth() + "x"
              + jlIcon.getIcon().getIconHeight());
        }

        @Override
        public void componentMoved(ComponentEvent e) {
          // nothing to do here
        }

        @Override
        public void componentHidden(ComponentEvent e) {
          // nothing to do here
        }
      });
    }
    dialog.add(actions, BorderLayout.SOUTH);
    JScrollPane jsp = new JScrollPane(header);
    jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    jsp.setBorder(BorderFactory.createEmptyBorder());
    dialog.add(jsp, BorderLayout.NORTH);
    if (current != null) {
      dialog.add(current, BorderLayout.CENTER);
    }
    dialog.getRootPane().setDefaultButton(actions.jbNext);
    ((JPanel) dialog.getContentPane()).revalidate();
    dialog.getContentPane().repaint();
  }

  /**
   * Set the header image.
   * 
   * @param img 
   */
  public void setHeaderImage(Image img) {
    header.setImage(img);
  }

  /**
   * Set the header icon.
   * 
   * @param icon 
   */
  public void setHeaderIcon(ImageIcon icon) {
    header.setIcon(icon);
  }

  /**
   * Set the background color of the ActionPanel.
   * 
   * @param color 
   */
  public void setActionsBackgroundColor(Color color) {
    actions.setBackgroundColor(color);
  }

  /**
   * Set the background color of the ActionPanel's Problem notification area.
   * 
   * @param color 
   */
  public void setProblemBackgroundColor(Color color) {
    actions.setProblemBackgroundColor(color);
  }

  /**
   * Gets the previous screen.
   * 
   * @param screen 
   * 
   * @return previous screen class
   */
  abstract public Class<? extends Screen> getPreviousScreen(Class<? extends Screen> screen);

  /**
   * Clear screens history.
   */
  public final void clearScreens() {
    hmClassScreens.clear();
  }

  /**
   * Gets the next screen.
   * 
   * @param screen 
   * 
   * @return next screen class
   */
  abstract public Class<? extends Screen> getNextScreen(Class<? extends Screen> screen);

  /**
   * Get current screen.
   * 
   * @return current screen class
   */
  public Class<? extends Screen> getCurrentScreen() {
    return this.current.getClass();
  }

  /**
   * Refresh buttons and problems. Called asynchronously by the screens or by
   * the wizard itself.
   */
  public void updateGUI() {
    boolean bPrevious = current.canGoPrevious();
    boolean bNext = current.canGoNext();
    boolean bFinish = current.canFinish();
    boolean bCancel = current.canCancel();
    actions.setState(bPrevious, bNext, bFinish, bCancel);
    actions.setProblem(current.getProblem());
  }

  /**
   * Finish action. Called when user clicks on "finish"
   */
  abstract public void finish();

  /**
   * Called when user clicks on "cancel". Override it if you want to do
   * something in cancel such as display a confirmation dialog.
   * <p>
   * 
   * @return return true if the Wizard should continue to close
   * return false if the Wizard should abort the cancellation
   */
  public boolean onCancel() {
    return true;
  }

  /**
   * Icon resizing.
   * 
   * @param img 
   * @param iNewWidth 
   * @param iNewHeight 
   * 
   * @return resized icon
   */
  private static ImageIcon getResizedImage(ImageIcon img, int iNewWidth, int iNewHeight) {
    if (img == null) {
      return null;
    }
    ImageIcon iiNew = new ImageIcon();
    Image image = img.getImage();
    Image scaleImg = image.getScaledInstance(iNewWidth, iNewHeight, Image.SCALE_AREA_AVERAGING);
    iiNew.setImage(scaleImg);
    return iiNew;
  }

  /* (non-Javadoc)
   * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
   */
  @Override
  public void windowClosing(WindowEvent windowEvent) {
    // if cancel is disabled, then don't call the onCancel function and
    // don't dispose
    if (current.canCancel() && onCancel()) {
      bCancelled = true;
      dialog.dispose();
    }
  }

  /**
   * Called when the wizard dialog opens. Override it if you want notification
   * of this event.
   * 
   * @param windowEvent 
   */
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
   */
  @Override
  public void windowOpened(WindowEvent windowEvent) {
    // nothing to do here
  }

  /**
   * Called when the wizard dialog is closed. Override it if you want
   * notification of this event.
   * <p>
   * <b>caution:</b> You must always call super.windowClosed(windowEvent)
   * within the override function to ensure that the Wizard closes completely.
   * 
   * @param windowEvent 
   */
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
   */
  @Override
  public void windowClosed(WindowEvent windowEvent) {
    // nothing to do here
  }

  /**
   * Called when the wizard dialog is iconified. Override it if you want
   * notification of this event.
   * 
   * @param windowEvent 
   */
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
   */
  @Override
  public void windowIconified(WindowEvent windowEvent) {
    // nothing to do here
  }

  /**
   * Called when the wizard dialog is deiconified. Override it if you want
   * notification of this event.
   * 
   * @param windowEvent 
   */
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
   */
  @Override
  public void windowDeiconified(WindowEvent windowEvent) {
    // nothing to do here
  }

  /**
   * Called when the wizard dialog is activated. Override it if you want
   * notification of this event.
   * 
   * @param windowEvent 
   */
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
   */
  @Override
  public void windowActivated(WindowEvent windowEvent) {
    // nothing to do here
  }

  /**
   * Called when the wizard dialog is deactivated. Override it if you want
   * notification of this event.
   * 
   * @param windowEvent 
   */
  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
   */
  @Override
  public void windowDeactivated(WindowEvent windowEvent) {
    // nothing to do here
  }

  /**
   * Was cancelled.
   * 
   * 
   * @return true if...
   */
  public boolean wasCancelled() {
    return bCancelled;
  }
}
