/*
 * Created on Jul 14, 2008
 *
 * Spectro-Edit is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Spectro-Edit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */
package net.bluecow.spectro;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.JPanel;

public class ClipPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(ClipPanel.class.getName());
    
    /**
     * The clip this panel visualizes.
     */
    private final Clip clip;

    private final BufferedImage img;
    
    private double spectralToScreenMultiplier = 6000.0;
    
    /**
     * A rectangular frame that's manipulated from outside this class.
     * See {@link #updateRegion(Rectangle)} for details.
     */
    private Rectangle region;
    
    public ClipPanel(Clip clip) {
        this.clip = clip;
        setPreferredSize(new Dimension(clip.getFrameCount(), clip.getFrameFreqSamples()));
        img = new BufferedImage(clip.getFrameCount(), clip.getFrameFreqSamples(), BufferedImage.TYPE_INT_RGB);
        updateImage(null);
        setBackground(Color.BLACK);
    }

    /**
     * Converts the given point (which is in screen coordinates) to co-ordinates
     * that work with clips and their frames. The x value will be the
     * corresponding frame number within the clip, and the y value will be the
     * corresponding index within the frame's spectral data.
     * <p>
     * This is the inverse operation to {@link #toScreenCoords(Point)}.
     * 
     * @param p
     *            The point to convert. This point object will be modified!
     * @return The given point, which has been modified.
     */
    public Point toClipCoords(Point p) {
        p.y = clip.getFrameFreqSamples() - p.y;
        return p;
    }

    /**
     * Converts the given rectangle (which is in screen coordinates) to co-ordinates
     * that work with clips and their frames. The x value will be the
     * corresponding frame number within the clip, and the y value will be the
     * corresponding index within the frame's spectral data.
     * <p>
     * This is the inverse operation to {@link #toScreenCoords(Point)}.
     * <pre>
     *   . (0,0)
     *   
     *   
     *       (4,3)+------------+(10,3)
     *            |            |
     *            |            |
     *       (4,6)+------------+(10,6)  width=6 height=3
     *       
     *                                       . (8,15)
     * </pre>
     * @param p
     *            The point to convert. This point object will be modified!
     * @return The given point, which has been modified.
     */
    public Rectangle toClipCoords(Rectangle r) {
        r.y = clip.getFrameFreqSamples() - (r.y + r.height);
        return r;
    }

    /**
     * Converts the given point (which is in screen coordinates) to co-ordinates
     * that work with clips and their frames. The x value will be the
     * corresponding x coordinate on screen, and the y value will be the
     * corresponding y coordinate on screen.
     * <p>
     * This is the inverse operation to {@link #toClipCoords(Point)}.
     * 
     * @param p
     *            The point to convert. This point object will be modified!
     * @return The given point, which has been modified.
     */
    public Point toScreenCoords(Point p) {
        p.y = clip.getFrameFreqSamples() - p.y;
        return p;
    }

    /**
     * Updates the image based on the existing Clip data and the settings in
     * this panel (such as the multiplier).
     * 
     * @param region
     *            The region to update, in screen co-ordinates. Null means to
     *            update the whole image.
     */
    public void updateImage(Rectangle region) {
        if (region == null) {
            region = new Rectangle(0, 0, clip.getFrameCount(), clip.getFrameFreqSamples());
        } else {
            region = new Rectangle(region);
        }
        
        toClipCoords(region);
        
        final int endCol = region.x + region.width;
        final int endRow = region.y + region.height;
        
        for (int col = region.x; col < endCol; col++) {
            Frame f = clip.getFrame(col);
            for (int row = region.y; row < endRow; row++) {
                int greyVal = (int) (spectralToScreenMultiplier * Math.abs(f.getReal(row)));
                if (greyVal < 0) {
                    greyVal = 0;
                } else if (greyVal > 255) {
                    greyVal = 255;
                }
                greyVal = (greyVal << 16) | (greyVal << 8) | (greyVal);
                img.setRGB(col, row, greyVal);
            }
            
            // this may or may not be faster...
            // img.setRGB(0, col, 1, nrows, rgbArray, 0, 1);
            
            // it would definitely be faster to update the array directly
            // (it can be retrieved from the image's WritableRaster)
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Flip upside down while rendering the spectrogram
        AffineTransform backupTransform = g2.getTransform();
        g2.translate(0, img.getHeight());
        g2.scale(1.0, -1.0);
        
        Rectangle clipBounds = g2.getClipBounds();
        logger.finer(String.format("Clip bounds: (%d, %d) %dx%d", clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height));
        if (clipBounds.x + clipBounds.width > img.getWidth()) {
            clipBounds.width = img.getWidth() - clipBounds.x;
        }
        if (clipBounds != null) {
            g2.drawImage(img,
                    clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height,
                    clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height,
                    Color.BLACK, null);
        } else {
            g2.drawImage(img, 0, 0, null);
        }
        
        // Now flip back for the region
        g2.setTransform(backupTransform);
        if (region != null) {
            g2.setColor(Color.YELLOW);
            g2.drawRect(
                    region.x, region.y,
                    region.width, region.height);
        }
    }

    public void setSpectralToScreenMultiplier(double d) {
        spectralToScreenMultiplier = d;
        updateImage(null);
        repaint();
    }
    
    public double getSpectralToScreenMultiplier() {
        return spectralToScreenMultiplier;
    }
    
    public Clip getClip() {
        return clip;
    }

    /**
     * Updates the location and size of a rectangular shape that's painted over
     * the spectral data. Tools can use this to track selections.
     * <p>
     * Future: It probably would make more sense to build the concept of region
     * selection right into this class, and let the tools read out the selected
     * region when they want it.
     * 
     * @param region
     */
    public void updateRegion(Rectangle newRegion) {
        Rectangle oldRegion = region;
        region = newRegion == null ? null : new Rectangle(newRegion);
        if (oldRegion != null && newRegion == null) {
            repaint(oldRegion.x, oldRegion.y, oldRegion.width + 1, oldRegion.height + 1);
        } else if (oldRegion == null && newRegion != null) {
            repaint(newRegion.x, newRegion.y, newRegion.width + 1, newRegion.height + 1);
        } else if (oldRegion != null && newRegion != null) {
            oldRegion.add(newRegion);
            repaint(oldRegion.x, oldRegion.y, oldRegion.width + 1, oldRegion.height + 1);
        }
    }
}
