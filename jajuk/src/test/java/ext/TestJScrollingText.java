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
 *  $Revision: 3132 $
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
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
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
 * 
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
   * Test method for
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

  public void testPaintComponentOpaque() {
    JScrollingText t = new JScrollingText("teststring");

    t.setOpaque(true);
    t.paintComponent(new MyGraphics2D());
  }

  public void testPaintComponentSpeed() {
    JScrollingText t = new JScrollingText("teststring", 1000);

    t.paintComponent(new MyGraphics2D());
  }

  public void testPaintComponentSpeedZero() {
    JScrollingText t = new JScrollingText("teststring", 0);

    t.paintComponent(new MyGraphics2D());
  }

  /**
   * Test method for {@link ext.JScrollingText#start()}.
   * 
   * @throws Exception
   */
  public void testStart() throws Exception {
    JScrollingText t = new JScrollingText("teststring");
    assertNotNull(t);
    t.start();

    // have to sleep some time here to make the Timer run at least once
    Thread.sleep(1100);
  }

  /**
   * 
   */
  private final class MyGraphics2D extends Graphics2D {
    @Override
    public void setXORMode(Color c1) {

    }

    @Override
    public void setPaintMode() {

    }

    @Override
    public void setFont(Font font) {

    }

    @Override
    public void setColor(Color c) {

    }

    @Override
    public void setClip(int x, int y, int width, int height) {

    }

    @Override
    public void setClip(Shape clip) {

    }

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

    @Override
    public Font getFont() {

      return null;
    }

    @Override
    public Color getColor() {

      return null;
    }

    @Override
    public Rectangle getClipBounds() {

      return null;
    }

    @Override
    public Shape getClip() {

      return null;
    }

    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {

    }

    @Override
    public void fillRect(int x, int y, int width, int height) {

    }

    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {

    }

    @Override
    public void fillOval(int x, int y, int width, int height) {

    }

    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {

    }

    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {

    }

    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {

    }

    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {

    }

    @Override
    public void drawOval(int x, int y, int width, int height) {

    }

    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {

    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
        int sx2, int sy2, Color bgcolor, ImageObserver observer) {

      return false;
    }

    @Override
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1,
        int sx2, int sy2, ImageObserver observer) {

      return false;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
        ImageObserver observer) {

      return false;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {

      return false;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {

      return false;
    }

    @Override
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {

      return false;
    }

    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {

    }

    @Override
    public void dispose() {

    }

    @Override
    public Graphics create() {

      return null;
    }

    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {

    }

    @Override
    public void clipRect(int x, int y, int width, int height) {

    }

    @Override
    public void clearRect(int x, int y, int width, int height) {

    }

    @Override
    public void translate(double tx, double ty) {

    }

    @Override
    public void translate(int x, int y) {

    }

    @Override
    public void transform(AffineTransform Tx) {

    }

    @Override
    public void shear(double shx, double shy) {

    }

    @Override
    public void setTransform(AffineTransform Tx) {

    }

    @Override
    public void setStroke(Stroke s) {

    }

    @Override
    public void setRenderingHints(Map<?, ?> hints) {

    }

    @Override
    public void setRenderingHint(Key hintKey, Object hintValue) {

    }

    @Override
    public void setPaint(Paint paint) {

    }

    @Override
    public void setComposite(Composite comp) {

    }

    @Override
    public void setBackground(Color color) {

    }

    @Override
    public void scale(double sx, double sy) {

    }

    @Override
    public void rotate(double theta, double x, double y) {

    }

    @Override
    public void rotate(double theta) {

    }

    @Override
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {

      return false;
    }

    @Override
    public AffineTransform getTransform() {

      return null;
    }

    @Override
    public Stroke getStroke() {

      return null;
    }

    @Override
    public RenderingHints getRenderingHints() {

      return null;
    }

    @Override
    public Object getRenderingHint(Key hintKey) {

      return null;
    }

    @Override
    public Paint getPaint() {

      return null;
    }

    @Override
    public FontRenderContext getFontRenderContext() {

      return null;
    }

    @Override
    public GraphicsConfiguration getDeviceConfiguration() {

      return null;
    }

    @Override
    public Composite getComposite() {

      return null;
    }

    @Override
    public Color getBackground() {

      return null;
    }

    @Override
    public void fill(Shape s) {

    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, float x, float y) {

    }

    @Override
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {

    }

    @Override
    public void drawString(String str, float x, float y) {

    }

    @Override
    public void drawString(String str, int x, int y) {

    }

    @Override
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {

    }

    @Override
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {

    }

    @Override
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {

    }

    @Override
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {

      return false;
    }

    @Override
    public void drawGlyphVector(GlyphVector g, float x, float y) {

    }

    @Override
    public void draw(Shape s) {

    }

    @Override
    public void clip(Shape s) {

    }

    @Override
    public void addRenderingHints(Map<?, ?> hints) {

    }
  }
}
