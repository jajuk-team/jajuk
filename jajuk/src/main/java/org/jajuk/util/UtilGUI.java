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
package org.jajuk.util;

import com.jhlabs.image.PerspectiveFilter;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;

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
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jajuk.ui.helpers.FontManager;
import org.jajuk.ui.helpers.FontManager.JajukFont;
import org.jajuk.ui.helpers.TwoStepsDisplayable;
import org.jajuk.ui.perspectives.IPerspective;
import org.jajuk.ui.perspectives.PerspectiveManager;
import org.jajuk.ui.views.IView;
import org.jajuk.ui.widgets.CommandJPanel;
import org.jajuk.ui.widgets.InformationJPanel;
import org.jajuk.ui.widgets.JajukJMenuBar;
import org.jajuk.ui.widgets.PerspectiveBarJPanel;
import org.jajuk.ui.windows.JajukFullScreenWindow;
import org.jajuk.ui.windows.JajukMainWindow;
import org.jajuk.ui.windows.JajukSlimbar;
import org.jajuk.ui.windows.JajukSystray;
import org.jajuk.util.log.Log;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jvnet.substance.SubstanceLookAndFeel;
import org.jvnet.substance.api.SubstanceColorScheme;
import org.jvnet.substance.api.SubstanceSkin;
import org.jvnet.substance.skin.SkinInfo;
import org.jvnet.substance.skin.SubstanceBusinessLookAndFeel;

/**
 * Set of GUI convenient methods.
 */
public final class UtilGUI {
  /* different types of Cursors that are available */
  /** The Constant WAIT_CURSOR.  */
  public static final Cursor WAIT_CURSOR = new Cursor(Cursor.WAIT_CURSOR);
  /** The Constant LINK_CURSOR.  */
  public static final Cursor LINK_CURSOR = new Cursor(Cursor.HAND_CURSOR);
  /** The Constant DEFAULT_CURSOR.  */
  public static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
  // Current cursor that is displayed
  private static Cursor currentCursor = DEFAULT_CURSOR;
  /** Substance theme *. */
  private static String theme;
  /** Alternate color rows highlighter used in every table. */
  private static Highlighter alternateColorHighlighter;

  /**
   * Return whether the given highlighter is the alternateColorHighlighter.
   *
   * @param other 
   *
   * @return whether the given highlighter is the alternateColorHighlighter
   */
  public static boolean isAlternateColorHighlighter(Highlighter other) {
    return other.equals(alternateColorHighlighter);
  }

  /**
   * Reset the alternateColorHighlighter (during a theme change for eg).
   */
  public static void resetAlternateColorHighlighter() {
    alternateColorHighlighter = null;
  }

  /** Current active color scheme *. */
  private static SubstanceColorScheme colorScheme;
  /** Set cursor thread, stored to avoid construction. */
  private static Runnable setCursorThread = new Runnable() {
    @Override
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
   * Display a given image in a frame (for debuging purpose).
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
   * Write down a memory image to a file.
   *
   * @param src 
   * @param dest 
   */
  public static void extractImage(final Image src, final File dest) {
    final BufferedImage bi = UtilGUI.toBufferedImage(src);
    // Need alpha only for png and gif files);
    try {
      ImageIO.write(bi, UtilSystem.getExtension(dest), dest);
    } catch (final IOException e) {
      Log.error(e);
    }
  }

  /**
   * Gets the graphics device of main frame.
   *
   * @return the current display of the main frame
   */
  public static GraphicsDevice getGraphicsDeviceOfMainFrame() {
    GraphicsEnvironment localGraphicsEnvironment = GraphicsEnvironment
        .getLocalGraphicsEnvironment();
    for (int i = 0; i < GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length; i++) {
      GraphicsDevice graphicsDevice = localGraphicsEnvironment.getScreenDevices()[i];
      if (graphicsDevice.getDefaultConfiguration().getBounds()
          .contains(JajukMainWindow.getInstance().getLocation())) {
        return graphicsDevice;
      }
    }
    return localGraphicsEnvironment.getDefaultScreenDevice();
  }

  /**
   * Gets the centred panel.
   *
   * @param jc 
   *
   * @return an horizontaly centred panel
   */
  public static JPanel getCentredPanel(final JComponent jc) {
    return UtilGUI.getCentredPanel(jc, BoxLayout.X_AXIS);
  }

  /**
   * Gets the centred panel.
   *
   * @param jc 
   * @param iOrientation : vertical or horizontal orientation, use BoxLayout.X_AXIS or
   * BoxLayout.Y_AXIS
   *
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
   * Gets the html color.
   *
   * @param color java color
   *
   * @return HTML RGB color ex: FF0000
   */
  public static String getHTMLColor(final Color color) {
    return Long.toString(color.getRed(), 16) + Long.toString(color.getGreen(), 16)
        + Long.toString(color.getBlue(), 16);
  }

  /**
   * Get required image with specified url.
   *
   * @param url 
   *
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
   * Gets the limited message.
   *
   * @param sText text to display, lines separated by \n characters
   * @param limit : max number of lines to be displayed without scroller
   *
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
   *
   * @return the narrow option pane
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
   * Resize an image.
   *
   * @param img image to resize
   * @param iNewWidth 
   * @param iNewHeight 
   *
   * @return resized image
   */
  public static ImageIcon getResizedImage(final ImageIcon img, final int iNewWidth,
      final int iNewHeight) {
    Image scaleImg = img.getImage().getScaledInstance(iNewWidth, iNewHeight,
        Image.SCALE_AREA_AVERAGING);
    // Leave source image cache here as we may want to keep original image
    // but free the new image
    scaleImg.flush();
    return new ImageIcon(scaleImg);
  }

  /**
  * Show busy label when searching lyrics over provided panel.
  * @param panel panel to override.
  */
  public static void showBusyLabel(final JXPanel panel) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        panel.removeAll();
        Dimension dim = new Dimension(panel.getWidth() / 3, panel.getWidth() / 3);
        final JXBusyLabel busy = new JXBusyLabel(dim);
        busy.setBusy(true);
        JPanel inner = new JPanel();
        inner.setMinimumSize(new Dimension(panel.getWidth(), panel.getHeight()));
        inner.setLayout(new BoxLayout(inner, BoxLayout.X_AXIS));
        inner.add(Box.createHorizontalGlue());
        inner.add(UtilGUI.getCentredPanel(busy, BoxLayout.Y_AXIS));
        inner.add(Box.createHorizontalGlue());
        panel.add(inner);
        panel.revalidate();
        panel.repaint();
      }
    });
  }

  /**
   * Gets the scaled image.
   *
   * @param img 
   * @param iScale 
   *
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
   * Setup Substance look and feel.
   *
   * @param pTheme 
   */
  public static void setupSubstanceLookAndFeel(final String pTheme) {
    // Check the theme is known, if not take the default theme
    final Map<String, SkinInfo> themes = SubstanceLookAndFeel.getAllSkins();
    theme = pTheme;
    if (themes.get(theme) == null) {
      theme = Const.LNF_DEFAULT_THEME;
      Conf.setProperty(Const.CONF_OPTIONS_LNF, Const.LNF_DEFAULT_THEME);
    }
    // Set substance LAF
    try {
      UIManager.setLookAndFeel(new SubstanceBusinessLookAndFeel());
    } catch (UnsupportedLookAndFeelException e) {
      Log.error(e);
    }
    // Set substance LAF
    SubstanceLookAndFeel.setSkin(themes.get(theme).getClassName());
    // hide some useless elements such locker for not editable labels
    UIManager.put(SubstanceLookAndFeel.SHOW_EXTRA_WIDGETS, Boolean.FALSE);
    // Store current color scheme (cannot change for the wall session)
    colorScheme = SubstanceLookAndFeel.getCurrentSkin().getMainActiveColorScheme();
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
    UIManager.put("DockViewTitleBar.titleFont",
        FontManager.getInstance().getFont(JajukFont.VIEW_FONT));
    // Set windows decoration to look and feel
    JFrame.setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
  }

  /**
   * Display given container at given position.
   *
   * @param window 
   * @param iFromTop max number of pixels from top
   * @param iFromLeft max number of pixels from left
   */
  public static void setShuffleLocation(final Window window, final int iFromTop, final int iFromLeft) {
    window.setLocation((int) (Math.random() * iFromTop), (int) (Math.random() * iFromLeft)); //NOSONAR
  }

  /**
   * Set current cursor as waiting cursor.
   */
  public static synchronized void waiting() {
    if (!currentCursor.equals(WAIT_CURSOR)) {
      currentCursor = WAIT_CURSOR;
      SwingUtilities.invokeLater(setCursorThread);
    }
  }

  /**
   * Set current cursor as default cursor.
   */
  public static synchronized void stopWaiting() {
    if (!currentCursor.equals(DEFAULT_CURSOR)) {
      currentCursor = DEFAULT_CURSOR;
      SwingUtilities.invokeLater(setCursorThread);
    }
  }

  /**
   * To buffered image. 
   *
   * @param image the input image
   *
   * @return the buffered image
   */
  public static BufferedImage toBufferedImage(final Image image) {
    return UtilGUI.toBufferedImage(image, image.getWidth(null), image.getHeight(null));
  }

  /**
   * Create a buffered image without forced alpha channel.
   *
   * @param image the input image
   * @param targetWidth 
   * @param targetHeight 
   *
   * @return the buffered image
   */
  public static BufferedImage toBufferedImage(final Image image, final int targetWidth,
      final int targetHeight) {
    return UtilGUI.toBufferedImage(image, targetWidth, targetHeight, false);
  }

  /**
   * Transform an image to a BufferedImage
   * <p>
   * Code adapted from from http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
   * </p>
   *
   * @param image the input image
   * @param targetWidth 
   * @param targetHeight 
   * @param forcedAlpha Force using an alpha channel for target image
   *
   * @return buffered image from an image
   */
  public static BufferedImage toBufferedImage(final Image image, final int targetWidth,
      final int targetHeight, boolean forcedAlpha) {
    if (image instanceof BufferedImage) {
      return ((BufferedImage) image);
    } else {
      // This code ensures that all the pixels in the image are loaded
      Image loadedImage = new ImageIcon(image).getImage();
      // Use right target format according to need for alpha chanel or not (less memory use if no
      // alpha)
      int type = BufferedImage.TYPE_INT_RGB;
      if (forcedAlpha || hasAlpha(image)) {
        type = BufferedImage.TYPE_INT_ARGB;
      }
      BufferedImage ret = null;
      int w, h;
      // Use multi-step technique: start with original size, then
      // scale down in multiple passes with drawImage()
      // until the target size is reached
      w = image.getWidth(null);
      h = image.getHeight(null);
      // See http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html for
      // explanations about this algorithm.
      // Basically, we perform image creation dichotomy to create high quality thumb at low price
      do {
        // When w/y reaches thumb height/width, it's time to to use the target thumb size exactly
        if (w <= targetHeight || h <= targetHeight) {
          w = targetWidth;
          h = targetHeight;
        } else {
          if (w > targetWidth) {
            w /= 2;
            if (w < targetWidth) {
              w = targetWidth;
            }
          }
          if (h > targetHeight) {
            h /= 2;
            if (h < targetHeight) {
              h = targetHeight;
            }
          }
        }
        BufferedImage tmp = new BufferedImage(w, h, type);
        Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // If the input buffered image doesn't yet exist, use the input image
        if (ret != null) {
          g2.drawImage(ret, 0, 0, w, h, null);
        } else {
          g2.drawImage(loadedImage, 0, 0, w, h, null);
        }
        g2.dispose();
        ret = tmp;
      } while (w != targetWidth || h != targetHeight);
      image.flush();
      loadedImage.flush();
      return ret;
    }
  }

  /**
   * Get3d image.
   *
   * @param img 
   *
   * @return the 3d image
   */
  public static BufferedImage get3dImage(Image img) {
    int angle = 30;
    int gap = 10;
    float opacity = 0.3f;
    float fadeHeight = 0.6f;
    // cover
    BufferedImage coverImage = UtilGUI.toBufferedImage(img, Const.MIRROW_COVER_SIZE,
        Const.MIRROW_COVER_SIZE, true);
    PerspectiveFilter filter1 = new PerspectiveFilter(0, angle,
        Const.MIRROW_COVER_SIZE - angle / 2, (int) (angle * (5.0 / 3.0)), Const.MIRROW_COVER_SIZE
            - angle / 2, Const.MIRROW_COVER_SIZE, 0, Const.MIRROW_COVER_SIZE + angle);
    coverImage = filter1.filter(coverImage, null);
    // reflection
    int imageWidth = coverImage.getWidth();
    int imageHeight = coverImage.getHeight();
    BufferedImage reflection = new BufferedImage(imageWidth, imageHeight,
        BufferedImage.TYPE_INT_ARGB);
    Graphics2D rg = reflection.createGraphics();
    rg.drawRenderedImage(coverImage, null);
    rg.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
    rg.setPaint(new GradientPaint(0, imageHeight * fadeHeight, new Color(0.0f, 0.0f, 0.0f, 0.0f),
        0, imageHeight, new Color(0.0f, 0.0f, 0.0f, opacity)));
    rg.fillRect(0, 0, imageWidth, imageHeight);
    rg.dispose();
    PerspectiveFilter filter2 = new PerspectiveFilter(0, 0, coverImage.getHeight() - angle / 2,
        angle * 2, coverImage.getHeight() - angle / 2, coverImage.getHeight() + angle * 2, 0,
        coverImage.getHeight());
    BufferedImage reflectedImage = filter2.filter(reflection, null);
    // now draw everything on one bufferedImage
    BufferedImage finalImage = new BufferedImage(imageWidth, (int) (1.4 * imageHeight),
        BufferedImage.TYPE_INT_ARGB);
    Graphics g = finalImage.getGraphics();
    Graphics2D g2d = (Graphics2D) g;
    g2d.drawRenderedImage(coverImage, null);
    g2d.translate(0, 2 * imageHeight + gap);
    g2d.scale(1, -1);
    g2d.drawRenderedImage(reflectedImage, null);
    g2d.dispose();
    reflection.flush();
    coverImage.flush();
    return finalImage;
  }

  /**
   * This method returns true if the specified image has transparent pixels
   * Found at http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
   *
   * @param image 
   *
   * @return true if the specified image has transparent pixels
   */
  public static boolean hasAlpha(Image image) {
    try {
      // If buffered image, the color model is readily available
      if (image instanceof BufferedImage) {
        BufferedImage bimage = (BufferedImage) image;
        return bimage.getColorModel().hasAlpha();
      }
      // Use a pixel grabber to retrieve the image's color model;
      // grabbing a single pixel is usually sufficient
      PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
      pg.grabPixels();
      // Get the image's color model
      ColorModel cm = pg.getColorModel();
      if (cm != null) {
        return cm.hasAlpha();
      }
    } catch (Exception e) {
      Log.error(e);
    }
    return false;
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
   *
   * @param c 
   */
  public static void updateComponentTreeUI(final Component c) {
    UtilGUI.updateComponentTreeUI0(c);
    c.invalidate();
    c.validate();
    c.repaint();
  }

  /**
   * Update component tree u i0. 
   *
   * @param c 
   */
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
   * @param window The <code>Window</code> for which the look and feel update has to
   * be performed against.
   */
  public static void updateWindowUI(final Window window) {
    try {
      UtilGUI.updateComponentTreeUI(window);
    } catch (final Exception exception) {
      Log.error(exception);
    }
    final Window windows[] = window.getOwnedWindows();
    for (final Window element : windows) {
      UtilGUI.updateWindowUI(element);
    }
  }

  /**
   * Gets the alternate highlighter.
   *
   * @return a theme-dependent alternate row highlighter used in tables or trees
   */
  public static Highlighter getAlternateHighlighter() {
    if (alternateColorHighlighter != null) {
      return alternateColorHighlighter;
    }
    SubstanceSkin theme = SubstanceLookAndFeel.getCurrentSkin();
    SubstanceColorScheme scheme = theme.getMainActiveColorScheme();
    Color color1 = scheme.getWatermarkStampColor();
    Color color2 = scheme.getWatermarkDarkColor();
    Highlighter highlighter = HighlighterFactory.createAlternateStriping(color1, color2);
    alternateColorHighlighter = highlighter;
    return highlighter;
  }

  /**
   * Checks if is over.
   *
   * @param location 
   * @param dimension 
   *
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
   * Gets the ultra light color.
   *
   * @return ultralight color for current color scheme
   */
  static public Color getUltraLightColor() {
    return colorScheme.getUltraLightColor();
  }

  /**
   * Gets the foreground color.
   *
   * @return foreground color for current color scheme
   */
  static public Color getForegroundColor() {
    return colorScheme.getForegroundColor();
  }

  /**
   * Display a dialog with given url picture.
   *
   * @param url 
   *
   * @throws MalformedURLException the malformed url exception
   */
  static public void showPictureDialog(String url) throws MalformedURLException {
    JDialog jd = new JDialog(JajukMainWindow.getInstance());
    ImageIcon ii = new ImageIcon(new URL(url));
    JPanel jp = new JPanel();
    jp.setLayout(new BoxLayout(jp, BoxLayout.X_AXIS));
    JLabel jl = new JLabel(ii);
    jp.add(jl);
    jd.setContentPane(jp);
    jd.pack();
    jd.setLocationRelativeTo(JajukMainWindow.getInstance());
    jd.setVisible(true);
  }

  /**
   * configures gui for repeat single enable/disable.
   *
   * @param enable 
   */
  public static void setRepeatSingleGui(boolean enable) {
    // always disable repeat all
    Conf.setProperty(Const.CONF_STATE_REPEAT_ALL, Const.FALSE);
    JajukJMenuBar.getInstance().setRepeatAllSelected(false);
    CommandJPanel.getInstance().setRepeatAllSelected(false);
    Conf.setProperty(Const.CONF_STATE_REPEAT, Boolean.toString(enable));
    JajukJMenuBar.getInstance().setRepeatSelected(enable);
    CommandJPanel.getInstance().setRepeatSelected(enable);
  }

  /**
   * configures gui for repeat all enable/disable.
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

  /**
   * Registers the ESCAPE key on the Panel so that it closes the Dialog.
   *
   * @param window 
   * @param pane 
   */
  public static void setEscapeKeyboardAction(final Window window, JComponent pane) {
    final KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
      @Override
      public boolean dispatchKeyEvent(KeyEvent e) {
        // For some reasons (under Linux at least), pressing escape only trigger PRESSED
        // and RELEASED key events
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE && e.getID() == KeyEvent.KEY_PRESSED
            && window.isFocused()) {
          window.dispose();
          return true;
        }
        return false;
      }
    };
    // Add keystroke to close window when pressing escape
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
    // make sure the key event dispatcher is removed as soon as the Window is closing
    window.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
      }

      @Override
      public void windowClosed(WindowEvent e) {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
      }
    });
  }

  /**
   * Build GUI for a TwoStateDisplayable component.
   * <p>
   * <exception catching is preferred in the longCall() method without throwing
   * it to the fastCall() one.
   * </p>
   *
   * @param displayable 
   */
  public static void populate(final TwoStepsDisplayable displayable) {
    SwingWorker<Object, Void> sw = new SwingWorker<Object, Void>() {
      @Override
      protected Object doInBackground() {
        return displayable.longCall();
      }

      @Override
      protected void done() {
        try {
          Object in = get();
          displayable.shortCall(in);
        } catch (InterruptedException e) {
          Log.error(e);
        } catch (ExecutionException e) {
          Log.error(e);
        }
      }
    };
    sw.execute();
  }

  /**
   * Center a given window to the center of the screen.
   *
   * @param window 
   */
  public static void centerWindow(Window window) {
    Toolkit tk = Toolkit.getDefaultToolkit();
    Dimension screenSize = tk.getScreenSize();
    int screenHeight = screenSize.height;
    int screenWidth = screenSize.width;
    window.setLocation((screenWidth / 2) - (window.getWidth() / 2),
        (screenHeight / 2) - (window.getHeight() / 2));
  }

  /**
   * Return any displayed window (between main window, slimbar...)
   *
   * @return any displayed window (between main window, slimbar...)
   */
  public static Window getActiveWindow() {
    if (JajukMainWindow.getInstance().getWindowStateDecorator().isDisplayed()) {
      return JajukMainWindow.getInstance();
    } else if (JajukSlimbar.getInstance().getWindowStateDecorator().isDisplayed()) {
      return JajukSlimbar.getInstance();
    } else if (JajukFullScreenWindow.getInstance().getWindowStateDecorator().isDisplayed()) {
      return JajukFullScreenWindow.getInstance();
    } else {
      // Can happen in sys tray mode only
      return null;
    }
  }

  /**
   * Gets the given component's parent view.
   *
   * @param component the component
   *
   * @return the parent view or null if none IView is among its ancestors
   */
  public static IView getParentView(Component component) {
    try {
      Component parent = component.getParent();
      while (parent != null && !(parent instanceof IView)) {
        parent = parent.getParent();
      }
      return (IView) parent;
    } catch (RuntimeException e) {
      // Make sure to trap strange events
      return null;
    }
  }
}
