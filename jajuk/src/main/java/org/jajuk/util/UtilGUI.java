/*
 *  Jajuk
 *  Copyright (C) 2003-2008 The Jajuk Team
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
 *  $Revision: 3132 $
 */
package org.jajuk.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.widgets.CommandJPanel;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJMenuBar;
import org.jajuk.ui.widgets.JajukSystray;
import org.jajuk.ui.widgets.JajukWindow;
import org.jajuk.ui.widgets.PerspectiveBarJPanel;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.graphics.GraphicsUtilities;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceColorScheme;
import org.jvnet.substance.api.SubstanceSkin;
import org.jvnet.substance.skin.SkinInfo;
import org.jvnet.substance.skin.SubstanceBusinessLookAndFeel;

/**
 * Set of GUI convenient methods
 */
public final class UtilGUI {

  /* different types of Cursors that are available */
  public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
  public static final Cursor LINK_CURSOR = new Cursor(Cursor.HAND_CURSOR);
  public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);

  // Current cursor that is displayed
  private static Cursor currentCursor = DEFAULT_CURSOR;

  private static Highlighter defaultHighlighter;

  /** Current active color scheme * */
  private static SubstanceColorScheme colorScheme;

  /** Set cursor thread, stored to avoid construction */
  private static Runnable setCursorThread = new Runnable() {
    public void run() {
      Container container = null;
      IPerspective perspective = PerspectiveManager.getCurrentPerspective();
      if (perspective != null) {
        // Log.debug("** Set cursor: " + currentCursor);
        container = perspective.getContentPane();
        container.setCursor(currentCursor);
        CommandJPanel.getInstance().setCursor(currentCursor);
        InformationJPanel.getInstance().setCursor(currentCursor);
        PerspectiveBarJPanel.getInstance().setCursor(currentCursor);
      }
    }
  };

  /**
   * Private constructor to prevent instantiation of utility class.
   */
  private UtilGUI() {
  }

  /**
   * Display a given image in a frame (for debuging purpose)
   * 
   * @param ii
   */
  public static void displayImage(final ImageIcon ii) {
    final JFrame jf = new JFrame();
    jf.add(new JLabel(ii));
    jf.pack();
    jf.setVisible(true);
  }

  /**
   * Write down a memory image to a file
   * 
   * @param src
   * @param dest
   */
  public static void extractImage(final Image src, final File dest) {
    final BufferedImage bi = UtilGUI.toBufferedImage(src, !(UtilSystem.getExtension(dest)
        .equalsIgnoreCase("jpg")));
    // Need alpha only for png and gif files);
    try {
      ImageIO.write(bi, UtilSystem.getExtension(dest), dest);
    } catch (final IOException e) {
      Log.error(e);
    }
  }

  /**
   * @param jc
   * @return an horizontaly centred panel
   */
  public static JPanel getCentredPanel(final JComponent jc) {
    return UtilGUI.getCentredPanel(jc, BoxLayout.X_AXIS);
  }

  /**
   * @param jc
   * @param iOrientation
   *          : vertical or horizontal orientation, use BoxLayout.X_AXIS or
   *          BoxLayout.Y_AXIS
   * @return a centred panel
   */
  public static JPanel getCentredPanel(final JComponent jc, final int iOrientation) {
    final JPanel jpOut = new JPanel();
    jpOut.setLayout(new BoxLayout(jpOut, iOrientation));
    if (iOrientation == BoxLayout.X_AXIS) {
      jpOut.add(Box.createHorizontalGlue());
      jpOut.add(jc);
      jpOut.add(Box.createHorizontalGlue());
    } else {
      jpOut.add(Box.createVerticalGlue());
      jpOut.add(jc);
      jpOut.add(Box.createVerticalGlue());
    }
    return jpOut;
  }

  /**
   * @param color
   *          java color
   * @return HTML RGB color ex: FF0000
   */
  public static String getHTMLColor(final Color color) {
    return Long.toString(color.getRed(), 16) + Long.toString(color.getGreen(), 16)
        + Long.toString(color.getBlue(), 16);

  }

  /**
   * Get required image with specified url
   * 
   * @param sURL
   * @return the image
   */
  public static ImageIcon getImage(final URL url) {
    ImageIcon ii = null;
    final String sURL = url.toString();
    try {
      if (UtilSystem.iconCache.containsKey(sURL)) {
        ii = UtilSystem.iconCache.get(sURL);
      } else {
        ii = new ImageIcon(url);
        UtilSystem.iconCache.put(sURL, ii);
      }

    } catch (final Exception e) {
      Log.error(e);
    }
    return ii;
  }

  /**
   * 
   * @param sText
   *          text to display, lines separated by \n characters
   * @param limit
   *          : max number of lines to be displayed without scroller
   * @return formated message: either a string, or a textarea
   */
  public static Object getLimitedMessage(final String sText, final int limit) {
    final int iNbLines = new StringTokenizer(sText, "\n").countTokens();
    Object message = null;
    if (iNbLines > limit) {
      final JTextArea area = new JTextArea(sText);
      area.setRows(10);
      area.setColumns(50);
      area.setLineWrap(true);
      message = new JScrollPane(area);
    } else {
      message = sText;
    }
    return message;
  }

  /**
   * code from
   * http://java.sun.com/developer/onlineTraining/new2java/supplements/
   * 2005/July05.html#1 Used to correctly display long messages
   * 
   * @param maxCharactersPerLineCount
   * @return
   */
  public static JOptionPane getNarrowOptionPane(final int maxCharactersPerLineCount) {
    // Our inner class definition
    class NarrowOptionPane extends JOptionPane {
      private static final long serialVersionUID = 1L;

      int lmaxCharactersPerLineCount;

      NarrowOptionPane(final int maxCharactersPerLineCount) {
        super();

        this.lmaxCharactersPerLineCount = maxCharactersPerLineCount;
      }

      @Override
      public int getMaxCharactersPerLineCount() {
        return lmaxCharactersPerLineCount;
      }
    }
    return new NarrowOptionPane(maxCharactersPerLineCount);
  }

  /**
   * Resize an image
   * 
   * @param img
   *          image to resize
   * @param iNewWidth
   * @param iNewHeight
   * @return resized image
   */
  public static ImageIcon getResizedImage(final ImageIcon img, final int iNewWidth,
      final int iNewHeight) {
    Image scaleImg = img.getImage().getScaledInstance(iNewWidth, iNewHeight,
        Image.SCALE_AREA_AVERAGING);
    // Leave image cache here as we may want to keep original image
    return new ImageIcon(scaleImg);
  }

  /**
   * @param img
   * @param iScale
   * @return a scaled image
   */
  public static ImageIcon getScaledImage(final ImageIcon img, final int iScale) {
    int iNewWidth;
    int iNewHeight;
    // Height is smaller or equal than width : try to optimize width
    iNewWidth = iScale; // take all possible width
    // we check now if height will be visible entirely with optimized width
    final float fWidthRatio = (float) iNewWidth / img.getIconWidth();
    if (img.getIconHeight() * (fWidthRatio) <= iScale) {
      iNewHeight = (int) (img.getIconHeight() * fWidthRatio);
    } else {
      // no? so we optimize width
      iNewHeight = iScale;
      iNewWidth = (int) (img.getIconWidth() * ((float) iNewHeight / img.getIconHeight()));
    }
    return UtilGUI.getResizedImage(img, iNewWidth, iNewHeight);
  }

  /**
   * Code initially written by aTunes 1.14.0
   * 
   * @param image
   * @param width
   * @param height
   * @return
   */
  public static ImageIcon scaleImageBicubic(Image image, int width, int height) {
    if (image == null) {
      return null;
    }

    double thumbRatio = (double) width / (double) height;
    int imageWidth = image.getWidth(null);
    int imageHeight = image.getHeight(null);
    double imageRatio = (double) imageWidth / (double) imageHeight;
    int calculatedWidth = width;
    int calculatedHeight = height;
    if (thumbRatio < imageRatio) {
      calculatedHeight = (int) (width / imageRatio);
    } else {
      calculatedWidth = (int) (height * imageRatio);
    }

    if (imageWidth <= calculatedWidth && imageHeight <= calculatedHeight) {
      BufferedImage thumbImage = new BufferedImage(calculatedWidth, calculatedHeight,
          BufferedImage.TYPE_INT_RGB);
      Graphics2D graphics2D = thumbImage.createGraphics();
      graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      graphics2D.drawImage(image, 0, 0, calculatedWidth, calculatedHeight, null);
      graphics2D.dispose();
      return new ImageIcon(thumbImage);
    } else {
      // If scaled image is smaller then use SwingX utilities (looks much
      // better)
      BufferedImage bi = GraphicsUtilities.createThumbnail(toBufferedImage(image), calculatedWidth,
          calculatedHeight);
      return new ImageIcon(bi);
    }
  }

  /**
   * Code initially written by aTunes 1.14.0 Gets a BufferedImage from an Image
   * object.
   * 
   * @param image
   *          the image
   * 
   * @return the buffered image
   */
  public static BufferedImage toBufferedImage(Image img) {
    BufferedImage bufferedImage;
    try {
      Image image = new ImageIcon(img).getImage();
      bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
          BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = bufferedImage.createGraphics();
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.drawImage(image, 0, 0, null);
      g.dispose();
    } catch (IllegalArgumentException e) {
      Log.debug(e.getMessage());
      return null;
    }

    return bufferedImage;
  }

  /**
   * Set a look and feel. We always use Substance Look And Feel with various
   * themes
   * 
   * @param theme
   */
  public static void setLookAndFeel(final String pTheme) {
    try {
      // Check the theme is known, if not take the default theme
      final Map<String, SkinInfo> themes = SubstanceLookAndFeel.getAllSkins();
      String theme = pTheme;
      if (themes.get(theme) == null) {
        theme = Const.LNF_DEFAULT_THEME;
        Conf.setProperty(Const.CONF_OPTIONS_LNF, Const.LNF_DEFAULT_THEME);
      }

      // Set substance theme
      UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());

      // Set substance LAF
      SubstanceLookAndFeel.setSkin(themes.get(theme).getClassName());

      // hide some useless elements such locker for not editable labels
      UIManager.put(SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS, Boolean.FALSE);

      // Store current color scheme (cannot change for the wall session)
      colorScheme = SubstanceLookAndFeel.getCurrentSkin().getMainActiveColorScheme();
    } catch (final Exception e) {
      Log.error(e);
    }
    // Set view foreground colors
    SubstanceSkin theme = SubstanceLookAndFeel.getCurrentSkin();
    SubstanceColorScheme scheme = theme.getMainActiveColorScheme();
    Color foregroundActive = null;
    Color foregroundInactive = null;
    Color backgroundActive = null;
    Color backgroundInactive = null;
    if (scheme.isDark()) {
      foregroundActive = Color.BLACK;
      foregroundInactive = Color.WHITE;
      backgroundActive = scheme.getUltraLightColor();
      backgroundInactive = scheme.getUltraDarkColor();
    } else {
      foregroundActive = Color.WHITE;
      foregroundInactive = Color.BLACK;
      backgroundActive = scheme.getDarkColor();
      backgroundInactive = scheme.getLightColor();
    }
    UIManager.put("InternalFrame.activeTitleForeground", foregroundActive);
    UIManager.put("InternalFrame.inactiveTitleForeground", foregroundInactive);
    UIManager.put("InternalFrame.activeTitleBackground", backgroundActive);
    UIManager.put("InternalFrame.inactiveTitleBackground", backgroundInactive);
    UIManager.put("DockViewTitleBar.titleFont", FontManager.getInstance().getFont(
        JajukFont.VIEW_FONT));
  }

  /**
   * Display given container at given position
   * 
   * @param container
   * @param iFromTop
   *          max number of pixels from top
   * @param iFromLeft
   *          max number of pixels from left
   */
  public static void setShuffleLocation(final Window window, final int iFromTop, final int iFromLeft) {
    window.setLocation((int) (Math.random() * iFromTop), (int) (Math.random() * iFromLeft));
  }

  /**
   * Set current cursor as waiting cursor
   */
  public static synchronized void waiting() {
    if (!currentCursor.equals(WAIT_CURSOR)) {
      currentCursor = WAIT_CURSOR;
      SwingUtilities.invokeLater(setCursorThread);
    }
  }

  /**
   * Set current cursor as default cursor
   */
  public static synchronized void stopWaiting() {
    if (!currentCursor.equals(DEFAULT_CURSOR)) {
      currentCursor = DEFAULT_CURSOR;
      SwingUtilities.invokeLater(setCursorThread);
    }
  }

  public static BufferedImage toBufferedImage(final Image image, final boolean alpha) {
    return UtilGUI.toBufferedImage(image, alpha, image.getWidth(null), image.getHeight(null));
  }

  /**
   * Transform an image to a BufferedImage
   * <p>
   * Thanks http://java.developpez.com/faq/java/?page=graphique_general_images
   * </p>
   * 
   * @param image
   * @param Do
   *          we need alpha (transparency) ?
   * @param new image width
   * @param height
   *          new image height
   * @return buffured image from an image
   */
  public static BufferedImage toBufferedImage(final Image image, final boolean alpha,
      final int width, final int height) {
    if (image instanceof BufferedImage) {
      return ((BufferedImage) image);
    } else {
      /** Create the new image */
      BufferedImage bufferedImage = null;
      if (alpha) {
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
      } else {
        // Save memory
        bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      }
      final Graphics2D graphics2D = bufferedImage.createGraphics();
      graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      graphics2D.drawImage(image, 0, 0, width, height, null);
      image.flush();
      graphics2D.dispose();
      return bufferedImage;
    }
  }

  /**
   * Method to attempt a dynamic update for any GUI accessible by this JVM. It
   * will filter through all frames and sub-components of the frames.
   */
  public static void updateAllUIs() {
    Frame frames[];
    frames = Frame.getFrames();

    for (final Frame element : frames) {
      UtilGUI.updateWindowUI(element);
    }
    // update tray
    if (JajukSystray.isLoaded() && (JajukSystray.getInstance().getMenu() != null)) {
      UtilGUI.updateComponentTreeUI(JajukSystray.getInstance().getMenu());
    }
  }

  /**
   * A simple minded look and feel change: ask each node in the tree to
   * <code>updateUI()</code> -- that is, to initialize its UI property with the
   * current look and feel. Based on the Sun
   * SwingUtilities.updateComponentTreeUI, but ensures that the update happens
   * on the components of a JToolbar before the JToolbar itself.
   */
  public static void updateComponentTreeUI(final Component c) {
    UtilGUI.updateComponentTreeUI0(c);
    c.invalidate();
    c.validate();
    c.repaint();
  }

  private static void updateComponentTreeUI0(final Component c) {

    Component[] children = null;

    if (c instanceof JToolBar) {
      children = ((JToolBar) c).getComponents();

      if (children != null) {
        for (final Component element : children) {
          UtilGUI.updateComponentTreeUI0(element);
        }
      }

      ((JComponent) c).updateUI();
    } else {
      if (c instanceof JComponent) {
        ((JComponent) c).updateUI();
      }

      if (c instanceof JMenu) {
        children = ((JMenu) c).getMenuComponents();
      } else if (c instanceof Container) {
        children = ((Container) c).getComponents();
      }

      if (children != null) {
        for (final Component element : children) {
          UtilGUI.updateComponentTreeUI0(element);
        }
      }
    }
  }

  /**
   * Method to attempt a dynamic update for all components of the given
   * <code>Window</code>.
   * 
   * @param window
   *          The <code>Window</code> for which the look and feel update has to
   *          be performed against.
   */
  public static void updateWindowUI(final Window window) {
    try {
      UtilGUI.updateComponentTreeUI(window);
    } catch (final Exception exception) {
    }

    final Window windows[] = window.getOwnedWindows();

    for (final Window element : windows) {
      UtilGUI.updateWindowUI(element);
    }
  }

  /**
   * @return a theme-dependent alternate row highlighter used in tables or trees
   */
  public static Highlighter getAlternateHighlighter() {
    if (defaultHighlighter != null) {
      return defaultHighlighter;
    }
    SubstanceSkin theme = SubstanceLookAndFeel.getCurrentSkin();
    SubstanceColorScheme scheme = theme.getMainActiveColorScheme();
    Color color1 = null;
    // Color color2 = null;
    if (scheme.isDark()) {
      color1 = scheme.getDarkColor();
    } else {
      color1 = new Color(230, 235, 240);
    }
    defaultHighlighter = HighlighterFactory.createAlternateStriping(color1, null);
    return defaultHighlighter;
  }

  /**
   * 
   * @param component
   *          location
   * @param component
   *          dimension
   * @return whether the current mouse cursor if above a given component
   */
  public static boolean isOver(Point location, Dimension dimension) {
    java.awt.Point p = MouseInfo.getPointerInfo().getLocation();

    if (p.getX() <= location.getX() || p.getY() <= location.getY()) {
      return false;
    }

    return (p.getX() < (dimension.getWidth() + location.getX()) && p.getY() < (dimension
        .getHeight() + location.getY()));
  }

  /**
   * @return ultralight color for current color scheme
   */
  static public Color getUltraLightColor() {
    return colorScheme.getUltraLightColor();
  }

  /**
   * @return foreground color for current color scheme
   */
  static public Color getForegroundColor() {
    return colorScheme.getForegroundColor();
  }

  /**
   * Display a dialog with given url picture
   * 
   * @param url
   * @throws MalformedURLException
   */
  static public void showPictureDialog(String url) throws MalformedURLException {
    JDialog jd = new JDialog(JajukWindow.getInstance());
    ImageIcon ii = new ImageIcon(new URL(url));
    JPanel jp = new JPanel();
    jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
    JLabel jl = new JLabel(ii);
    jp.add(jl);
    jd.setContentPane(jp);
    jd.pack();
    jd.setLocationRelativeTo(JajukWindow.getInstance());
    jd.setVisible(true);
  }

  /**
   * configures gui for repeat single enable/disable
   * 
   * @param enable
   */
  public static void setRepeatSingleGui(boolean enable) {
    // always disable repeat all
    Conf.setProperty(Const.CONF_STATE_REPEAT_ALL, Boolean.toString(false));
    JajukJMenuBar.getInstance().setRepeatAllSelected(false);
    CommandJPanel.getInstance().setRepeatAllSelected(false);

    Conf.setProperty(Const.CONF_STATE_REPEAT, Boolean.toString(enable));

    JajukJMenuBar.getInstance().setRepeatSelected(enable);
    CommandJPanel.getInstance().setRepeatSelected(enable);

  }

  /**
   * configures gui for repeat all enable/disable
   * 
   * @param enable
   */
  public static void setRepeatAllGui(boolean enable) {
    // always disable repeat single
    Conf.setProperty(Const.CONF_STATE_REPEAT, Boolean.toString(false));
    JajukJMenuBar.getInstance().setRepeatSelected(false);
    CommandJPanel.getInstance().setRepeatSelected(false);

    Conf.setProperty(Const.CONF_STATE_REPEAT_ALL, Boolean.toString(enable));

    JajukJMenuBar.getInstance().setRepeatAllSelected(enable);
    CommandJPanel.getInstance().setRepeatAllSelected(enable);

  }

}
