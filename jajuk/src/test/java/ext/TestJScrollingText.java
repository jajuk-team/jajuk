/*
 *  Jajuk
 *  Copyright (C) 2003-2011 The Jajuk Team
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
 *  $Revision$
 */
package ext;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

import org.jajuk.JajukTestCase;

/**
 * DOCUMENT_ME.
 */
public class TestJScrollingText extends JajukTestCase {

  /**
   * Test method for {@link ext.JScrollingText#JScrollingText(String)}.
   */
  public void testJScrollingTextString() {
    new JScrollingText("teststring");
  }

  /**
   * Test method for {@link ext.JScrollingText#JScrollingText(String, int)}.
   */
  public void testJScrollingTextStringInt() {
    new JScrollingText("teststring", 1000);
  }

  /**
   * Test method for {@link ext.JScrollingText#JScrollingText(String, int, int)}
   * .
   */
  public void testJScrollingTextStringIntInt() {
    new JScrollingText("teststring", 500, 1000);
  }

  /**
   * Test method for.
   *
   * {@link ext.JScrollingText#JScrollingText(String, int, int, int)}.
   */
  public void testJScrollingTextStringIntIntInt() {
    new JScrollingText("teststring", 700, 100, 10);
  }

  /**
   * Test method for {@link ext.JScrollingText#paintComponent(Graphics)}.
   */
  public void testPaintComponent() {
    JScrollingText t = new JScrollingText("teststring");
    assertNotNull(t);

    t.paintComponent(new MyGraphics2D());
  }

  /**
   * Test paint component opaque.
   * DOCUMENT_ME
   */
  public void testPaintComponentOpaque() {
    JScrollingText t = new JScrollingText("teststring");

    t.setOpaque(true);
    t.paintComponent(new MyGraphics2D());
  }

  /**
   * Test paint component speed.
   * DOCUMENT_ME
   */
  public void testPaintComponentSpeed() {
    JScrollingText t = new JScrollingText("teststring", 1000);

    t.paintComponent(new MyGraphics2D());
  }

  /**
   * Test paint component speed zero.
   * DOCUMENT_ME
   */
  public void testPaintComponentSpeedZero() {
    JScrollingText t = new JScrollingText("teststring", 0);

    t.paintComponent(new MyGraphics2D());
  }

  /**
   * Test method for {@link ext.JScrollingText#start()}.
   *
   * @throws Exception the exception
   */
  public void testStart() throws Exception {
    JScrollingText t = new JScrollingText("teststring");
    assertNotNull(t);
    t.start();

    // have to sleep some time here to make the Timer run at least once
    Thread.sleep(1100);
  }

  /**
   * DOCUMENT_ME.
   */
  private final class MyGraphics2D extends Graphics2D {

    /* (non-Javadoc)
     * @see java.awt.Graphics#setXORMode(java.awt.Color)
     */
    @Override
    public void setXORMode(Color c1) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setPaintMode()
     */
    @Override
    public void setPaintMode() {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setFont(java.awt.Font)
     */
    @Override
    public void setFont(Font font) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setColor(java.awt.Color)
     */
    @Override
    public void setColor(Color c) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setClip(int, int, int, int)
     */
    @Override
    public void setClip(int x, int y, int width, int height) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#setClip(java.awt.Shape)
     */
    @Override
    public void setClip(Shape clip) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getFontMetrics(java.awt.Font)
     */
    @Override
    public FontMetrics getFontMetrics(Font f) {

      return new FontMetrics(f) {
        private static final long serialVersionUID = 9139781111511738969L;

        @Override
        public int stringWidth(String str) {
          return str.length();
        }

        @Override
        public int getHeight() {
          return 10;
        }

        @Override
        public int getAscent() {
          return 10;
        }

      };
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getFont()
     */
    @Override
    public Font getFont() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getColor()
     */
    @Override
    public Color getColor() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getClipBounds()
     */
    @Override
    public Rectangle getClipBounds() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#getClip()
     */
    @Override
    public Shape getClip() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillRoundRect(int, int, int, int, int, int)
     */
    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillRect(int, int, int, int)
     */
    @Override
    public void fillRect(int x, int y, int width, int height) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillPolygon(int[], int[], int)
     */
    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillOval(int, int, int, int)
     */
    @Override
    public void fillOval(int x, int y, int width, int height) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#fillArc(int, int, int, int, int, int)
     */
    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawRoundRect(int, int, int, int, int, int)
     */
    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawPolyline(int[], int[], int)
     */
    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawPolygon(int[], int[], int)
     */
    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawOval(int, int, int, int)
     */
    @Override
    public void drawOval(int x, int y, int width, int height) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawLine(int, int, int, int)
     */
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
        int sx2, int sy2, Color bgcolor, ImageObserver observer) {

      return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, int, int, int, int, java.awt.image.ImageObserver)
     */
    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
        int sx2, int sy2, ImageObserver observer) {

      return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
        ImageObserver observer) {

      return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, int, int, java.awt.image.ImageObserver)
     */
    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {

      return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.Color, java.awt.image.ImageObserver)
     */
    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {

      return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
     */
    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {

      return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#drawArc(int, int, int, int, int, int)
     */
    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#dispose()
     */
    @Override
    public void dispose() {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#create()
     */
    @Override
    public Graphics create() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#copyArea(int, int, int, int, int, int)
     */
    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#clipRect(int, int, int, int)
     */
    @Override
    public void clipRect(int x, int y, int width, int height) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics#clearRect(int, int, int, int)
     */
    @Override
    public void clearRect(int x, int y, int width, int height) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#translate(double, double)
     */
    @Override
    public void translate(double tx, double ty) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#translate(int, int)
     */
    @Override
    public void translate(int x, int y) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#transform(java.awt.geom.AffineTransform)
     */
    @Override
    public void transform(AffineTransform Tx) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#shear(double, double)
     */
    @Override
    public void shear(double shx, double shy) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setTransform(java.awt.geom.AffineTransform)
     */
    @Override
    public void setTransform(AffineTransform Tx) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setStroke(java.awt.Stroke)
     */
    @Override
    public void setStroke(Stroke s) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setRenderingHints(java.util.Map)
     */
    @Override
    public void setRenderingHints(Map<?, ?> hints) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setRenderingHint(java.awt.RenderingHints.Key, java.lang.Object)
     */
    @Override
    public void setRenderingHint(Key hintKey, Object hintValue) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setPaint(java.awt.Paint)
     */
    @Override
    public void setPaint(Paint paint) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
     */
    @Override
    public void setComposite(Composite comp) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#setBackground(java.awt.Color)
     */
    @Override
    public void setBackground(Color color) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#scale(double, double)
     */
    @Override
    public void scale(double sx, double sy) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#rotate(double, double, double)
     */
    @Override
    public void rotate(double theta, double x, double y) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#rotate(double)
     */
    @Override
    public void rotate(double theta) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#hit(java.awt.Rectangle, java.awt.Shape, boolean)
     */
    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {

      return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getTransform()
     */
    @Override
    public AffineTransform getTransform() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getStroke()
     */
    @Override
    public Stroke getStroke() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getRenderingHints()
     */
    @Override
    public RenderingHints getRenderingHints() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getRenderingHint(java.awt.RenderingHints.Key)
     */
    @Override
    public Object getRenderingHint(Key hintKey) {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getPaint()
     */
    @Override
    public Paint getPaint() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getFontRenderContext()
     */
    @Override
    public FontRenderContext getFontRenderContext() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getDeviceConfiguration()
     */
    @Override
    public GraphicsConfiguration getDeviceConfiguration() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getComposite()
     */
    @Override
    public Composite getComposite() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#getBackground()
     */
    @Override
    public Color getBackground() {

      return null;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#fill(java.awt.Shape)
     */
    @Override
    public void fill(Shape s) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, float, float)
     */
    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.text.AttributedCharacterIterator, int, int)
     */
    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
     */
    @Override
    public void drawString(String str, float x, float y) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawString(java.lang.String, int, int)
     */
    @Override
    public void drawString(String str, int x, int y) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawRenderedImage(java.awt.image.RenderedImage, java.awt.geom.AffineTransform)
     */
    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawRenderableImage(java.awt.image.renderable.RenderableImage, java.awt.geom.AffineTransform)
     */
    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawImage(java.awt.image.BufferedImage, java.awt.image.BufferedImageOp, int, int)
     */
    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
     */
    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {

      return false;
    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#drawGlyphVector(java.awt.font.GlyphVector, float, float)
     */
    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#draw(java.awt.Shape)
     */
    @Override
    public void draw(Shape s) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#clip(java.awt.Shape)
     */
    @Override
    public void clip(Shape s) {

    }

    /* (non-Javadoc)
     * @see java.awt.Graphics2D#addRenderingHints(java.util.Map)
     */
    @Override
    public void addRenderingHints(Map<?, ?> hints) {

    }
  }
}
