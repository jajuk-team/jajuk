/*
 *  QDWizard
 *  Copyright (C) Bertrand Florat and others
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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
 * A Wizard dialog displaying one or more screens
 * <ul>
 * <li>Create a class that extends Wizard. You have to implement
 * getPreviousScreen(), getNextScreen() and finish() abstract methods</li>
 * <li> Displaying the wizard:</li>
 * 
 * <pre>
 * MyWizard wizard = new Wizard(new Wizard.Builder("wizard name", ActionSelectionPanel.class,
        window).hSize(600).vSize(500).locale(LocaleManager.getLocale())
        .icon(anIcon));
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
  private final String name;
  private Screen current;
  private final Class<? extends Screen> initial;
  /** Wizard data. */
  protected final Map<String, Object> data = new HashMap<String, Object>(10);
  /** Wizard header. */
  private Header header;
  /** Wizard action Panel. */
  private ActionsPanel actions;
  /** Wizard dialog. */
  private JDialog dialog;
  private final Image leftSideImage;
  private final Frame parentWindow;
  private final int horizontalSize;
  private final int verticalSize;
  /** Screens instance repository. */
  private final Map<Class<? extends Screen>, Screen> hmClassScreens = new HashMap<Class<? extends Screen>, Screen>(
      10);
  /** Default Wizard size. */
  protected static final int DEFAULT_H_SIZE = 700;
  /** The Constant DEFAULT_V_SIZE.   */
  protected static final int DEFAULT_V_SIZE = 500;
  /** Default horizontal padding.   */
  protected static final int DEFAULT_H_LAYOUT_PADDING = 5;
  /** Default vertical padding.   */
  protected static final int DEFAULT_V_LAYOUT_PADDING = 5;
  /** Was the Wizard Canceled?. */
  private boolean bCancelled;
  private final int layoutHPadding;
  private final int layoutVPadding;

  public static class Builder {
    // Mandatory fields
    private final String name;
    /** Initial screen class */
    private final Class<? extends Screen> initial;
    /** Parent window. */
    private final Frame parentWindow;
    // Optional fields
    private ImageIcon icon;
    private Image headerBackgroundImage;
    private Image leftSideImage;
    private int horizontalSize = -1;
    private int verticalSize = -1;
    private int layoutHPadding = -1;
    private int layoutVPadding = -1;
    private Locale locale;

    /**
     * 
     * @param name Wizard name displayed in the frame title
     * @param initial initial screen to display
     * @param parentWindow wizard parent window
     */
    public Builder(String name, Class<? extends Screen> initial, Frame parentWindow) {
      this.name = name;
      this.initial = initial;
      this.parentWindow = parentWindow;
    }

    /**
     * Set the header left-side icon
     * @param icon header left-side icon
     * @return the wizard builder
     */
    public Builder icon(ImageIcon icon) {
      this.icon = icon;
      return this;
    }

    /**
     * Set the background image
     * @param backgroundImage image displayed in the header
     * @return the wizard builder
     */
    public Builder headerBackgroundImage(Image image) {
      this.headerBackgroundImage = image;
      return this;
    }

    /**
     * Set the left-side image
     * @param left-side image displayed in the wizard body
     * @return the wizard builder
     */
    public Builder leftSideImage(Image image) {
      this.leftSideImage = image;
      return this;
    }

    /**
     * Set the locale
     * @param locale locale (language) of the wizard
     * @return the wizard builder
     */
    public Builder locale(Locale locale) {
      this.locale = locale;
      return this;
    }

    /**
     * Set the vertical size
     * @param verticalSize vertical size in pixel of the wizard
     * @return the wizard builder
     */
    public Builder vSize(int verticalSize) {
      this.verticalSize = verticalSize;
      return this;
    }

    /**
     * Set the horizontal size
     * @param horizontalSize horizontal size in pixel of the wizard
     * @return the wizard builder
     */
    public Builder hSize(int horizontalSize) {
      this.horizontalSize = horizontalSize;
      return this;
    }

    /**
     * Set the vertical padding
     * @param layoutVPadding vertical padding in pixel between header and body
     * @return the wizard builder
     */
    public Builder vPadding(int layoutVPadding) {
      this.layoutVPadding = layoutVPadding;
      return this;
    }

    /**
     * Set the horizontal padding
     * @param layoutHPadding horizontal padding in pixel between left side image and body
     * @return the wizard builder
     */
    public Builder hPadding(int layoutHPadding) {
      this.layoutHPadding = layoutHPadding;
      return this;
    }
  }

  /**
   * Wizard constructor.
   * 
   * @param sName Wizard name displayed in dialog title
   * @param initial Initial screen class
   * @param icon Wizard icon (null if no icon)
   * @param headerBackgroundImage background image
   * @param parentWindow wizard parent window
   * @param locale Wizard locale
   * @param iHSize Horizontal size
   * @param iVSize Vertical size
   * @param iLayoutHPadding Horizontal layout padding
   * @param iLayoutVPadding Vertical layout padding
   */
  public Wizard(Builder builder) {
    bCancelled = false;
    this.name = builder.name;
    this.parentWindow = builder.parentWindow;
    Langpack.setLocale((builder.locale == null) ? Locale.getDefault() : builder.locale);
    this.header = new Header();
    header.setIcon(builder.icon);
    header.setBackgroundImage(builder.headerBackgroundImage);
    this.layoutHPadding = (builder.layoutHPadding >= 0) ? builder.layoutHPadding
        : DEFAULT_H_LAYOUT_PADDING;
    this.layoutVPadding = (builder.layoutVPadding >= 0) ? builder.layoutVPadding
        : DEFAULT_V_LAYOUT_PADDING;
    this.initial = builder.initial;
    this.leftSideImage = builder.leftSideImage;
    this.horizontalSize = (builder.horizontalSize >= 0) ? builder.horizontalSize : DEFAULT_H_SIZE;
    this.verticalSize = (builder.verticalSize >= 0) ? builder.verticalSize : DEFAULT_V_SIZE;
  }

  /**
   * Show the wizard * 
   */
  public void show() {
    createDialog();
    setScreen(initial);
    current.onEnter();
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
  private void createDialog() {
    dialog = new JajukJDialog(parentWindow, true);// modal
    // Set default size
    dialog.setSize(this.horizontalSize == 0 ? DEFAULT_H_SIZE : horizontalSize,
        this.verticalSize == 0 ? DEFAULT_V_SIZE : verticalSize);
    dialog.setTitle(name);
    actions = new ActionsPanel(this);
    dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    dialog.addWindowListener(this);
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
      if ("Prev".equals(ae.getActionCommand())) {
        setScreen(getPreviousScreen(current.getClass()));
      } else if ("Next".equals(ae.getActionCommand())) {
        current.onNext();
        setScreen(getNextScreen(current.getClass()));
        current.onEnter();
      } else if ("Cancel".equals(ae.getActionCommand())) {
        current.onCancelled();
        data.clear();
        bCancelled = true;
        onCancel();
        dialog.dispose();
      } else if ("Finish".equals(ae.getActionCommand())) {
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
        resetScreens();
      }
      // Try to get a screen from buffer or create it if needed
      if (hmClassScreens.containsKey(screenClass)) {
        screen = hmClassScreens.get(screenClass);
      } else {
        screen = screenClass.newInstance();
        screen.setWizard(this);
        screen.initUI();
        hmClassScreens.put(screenClass, screen);
      }
    } catch (Exception e) {
      Log.error(e);
      throw new RuntimeException("setScreen " + screenClass + " caused " + e.toString(), e);
    }
    current = screen;
    current.setCanGoPrevious((getPreviousScreen(screenClass) != null));
    current.setCanGoNext((getNextScreen(screenClass) != null));
    String sDesc = screen.getDescription();
    if (sDesc != null) {
      header.setTitle(screen.getName());
      header.setSubtitle(sDesc);
    } else {
      header.setTitle(screen.getName());
      header.setSubtitle("");
    }
    refreshGUI();
  }

  /**
   * Called at each screen refresh.
   */
  private void refreshGUI() {
    ((JPanel) dialog.getContentPane()).removeAll();
    dialog.setLayout(new BorderLayout(layoutHPadding, layoutVPadding));
    if (leftSideImage != null) {
      final JLabel jlIcon = new JLabel(new ImageIcon(leftSideImage));
      dialog.add(jlIcon, BorderLayout.WEST);
      // Add a listener to resize left side image if wizard window is
      // resized
      jlIcon.addComponentListener(new ComponentAdapter() {
        /* (non-Javadoc)
         * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
         */
        @Override
        public void componentResized(ComponentEvent e) {
          jlIcon.setIcon(Utils.getResizedImage(leftSideImage, jlIcon.getWidth(), jlIcon.getHeight()));
          jlIcon.setVisible(true);
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
    actions.setNextAsDefaultButtonInPanel(dialog.getRootPane());
    ((JPanel) dialog.getContentPane()).revalidate();
    dialog.getContentPane().repaint();
  }

  /**
   * Set the header image.
   * 
   * @param img 
   */
  public void setHeaderImage(Image img) {
    header.setBackgroundImage(img);
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
   * Clear screens history, all screens are dropped along with their data and will be recreated in future use.
   */
  public final void resetScreens() {
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
  public void updateGUIState() {
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
   * Was canceled.
   * 
   * 
   * @return true if...
   */
  public boolean wasCancelled() {
    return bCancelled;
  }
}
