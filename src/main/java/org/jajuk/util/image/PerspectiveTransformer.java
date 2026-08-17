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
package org.jajuk.util.image;

import java.awt.image.BufferedImage;

public class PerspectiveTransformer {

  private PerspectiveTransformer() {
  }

  /**
   * Applies a perspective (four-corner pin) transform to src.
   * The eight parameters are the destination corners, matching JHLabs PerspectiveFilter:
   * (x0,y0) = top-left,  (x1,y1) = top-right,
   * (x2,y2) = bottom-right, (x3,y3) = bottom-left
   * Source rectangle is always (0,0) -> (width, height).
   */
  public static BufferedImage applyPerspective(BufferedImage src,
                                               float x0, float y0,   // top-left  dst
                                               float x1, float y1,   // top-right dst
                                               float x2, float y2,   // bottom-right dst
                                               float x3, float y3) { // bottom-left dst

    int w = src.getWidth();
    int h = src.getHeight();

    // Bounding box of the destination quad
    int minX = (int) Math.floor(Math.min(Math.min(x0, x1), Math.min(x2, x3)));
    int minY = (int) Math.floor(Math.min(Math.min(y0, y1), Math.min(y2, y3)));
    int maxX = (int) Math.ceil(Math.max(Math.max(x0, x1), Math.max(x2, x3)));
    int maxY = (int) Math.ceil(Math.max(Math.max(y0, y1), Math.max(y2, y3)));
    int dstW = maxX - minX;
    int dstH = maxY - minY;

    // Shift corners so the output image starts at (0,0)
    x0 -= minX;
    y0 -= minY;
    x1 -= minX;
    y1 -= minY;
    x2 -= minX;
    y2 -= minY;
    x3 -= minX;
    y3 -= minY;

    // Compute the 3×3 perspective matrix that maps src unit square → dst quad,
    // then invert it so we can do inverse-mapping (dst pixel → src pixel).
    double[] fwd = getPerspectiveMatrix(
            0, 0, w, 0, w, h, 0, h,   // src corners
            x0, y0, x1, y1, x2, y2, x3, y3); // dst corners
    double[] inv = invertMatrix3x3(fwd);

    BufferedImage dst = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_ARGB);
    int[] srcPixels = src.getRGB(0, 0, w, h, null, 0, w);

    for (int dy = 0; dy < dstH; dy++) {
      for (int dx = 0; dx < dstW; dx++) {
        // Map dst → src via inverse perspective
        double denom = inv[6] * dx + inv[7] * dy + inv[8];
        double sx = (inv[0] * dx + inv[1] * dy + inv[2]) / denom;
        double sy = (inv[3] * dx + inv[4] * dy + inv[5]) / denom;

        int ix = (int) Math.round(sx);
        int iy = (int) Math.round(sy);
        if (ix >= 0 && ix < w && iy >= 0 && iy < h) {
          dst.setRGB(dx, dy, srcPixels[iy * w + ix]);
        }
      }
    }
    return dst;
  }

  /**
   * Computes the 3×3 homography (row-major, acts on homogeneous column vectors)
   * mapping the four source points to the four destination points.
   * Solves the standard 8-equation linear system via Gaussian elimination.
   */
  private static double[] getPerspectiveMatrix(
          double sx0, double sy0, double sx1, double sy1,
          double sx2, double sy2, double sx3, double sy3,
          double dx0, double dy0, double dx1, double dy1,
          double dx2, double dy2, double dx3, double dy3) {

    // Build the 8×8 system A·h = b  (h8 = 1 normalisation)
    double[][] A = {
            {sx0, sy0, 1, 0, 0, 0, -dx0 * sx0, -dx0 * sy0},
            {0, 0, 0, sx0, sy0, 1, -dy0 * sx0, -dy0 * sy0},
            {sx1, sy1, 1, 0, 0, 0, -dx1 * sx1, -dx1 * sy1},
            {0, 0, 0, sx1, sy1, 1, -dy1 * sx1, -dy1 * sy1},
            {sx2, sy2, 1, 0, 0, 0, -dx2 * sx2, -dx2 * sy2},
            {0, 0, 0, sx2, sy2, 1, -dy2 * sx2, -dy2 * sy2},
            {sx3, sy3, 1, 0, 0, 0, -dx3 * sx3, -dx3 * sy3},
            {0, 0, 0, sx3, sy3, 1, -dy3 * sx3, -dy3 * sy3},
    };
    double[] b = {dx0, dy0, dx1, dy1, dx2, dy2, dx3, dy3};

    // Gaussian elimination with partial pivoting
    int n = 8;
    for (int col = 0; col < n; col++) {
      // Pivot
      int maxRow = col;
      for (int row = col + 1; row < n; row++)
        if (Math.abs(A[row][col]) > Math.abs(A[maxRow][col]))
          maxRow = row;
      double[] tmp = A[col];
      A[col] = A[maxRow];
      A[maxRow] = tmp;
      double t = b[col];
      b[col] = b[maxRow];
      b[maxRow] = t;

      for (int row = col + 1; row < n; row++) {
        double factor = A[row][col] / A[col][col];
        for (int k = col; k < n; k++)
          A[row][k] -= factor * A[col][k];
        b[row] -= factor * b[col];
      }
    }
    // Back-substitution
    double[] h = new double[9];
    for (int row = n - 1; row >= 0; row--) {
      double sum = b[row];
      for (int k = row + 1; k < n; k++)
        sum -= A[row][k] * h[k];
      h[row] = sum / A[row][row];
    }
    h[8] = 1.0;
    return h;
  }

  /** Inverts a 3×3 matrix stored row-major. */
  private static double[] invertMatrix3x3(double[] m) {
    double a = m[0], b = m[1], c = m[2];
    double d = m[3], e = m[4], f = m[5];
    double g = m[6], h = m[7], i = m[8];
    double det = a * (e * i - f * h) - b * (d * i - f * g) + c * (d * h - e * g);
    double inv = 1.0 / det;
    return new double[]{
            (e * i - f * h) * inv, -(b * i - c * h) * inv, (b * f - c * e) * inv,
            -(d * i - f * g) * inv, (a * i - c * g) * inv, -(a * f - c * d) * inv,
            (d * h - e * g) * inv, -(a * h - b * g) * inv, (a * e - b * d) * inv
    };
  }
}