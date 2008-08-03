/*
 * Created on Jul 14, 2008
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.spectro;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
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
    
    private Rectangle lastImageUpdateRegion;
    
    public ClipPanel(Clip clip) {
        this.clip = clip;
        setPreferredSize(new Dimension(clip.getFrameCount(), clip.getFrameFreqSamples()));
        img = new BufferedImage(clip.getFrameCount(), clip.getFrameFreqSamples(), BufferedImage.TYPE_INT_RGB);
        updateImage(null);
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
    void updateImage(Rectangle region) {
        if (region == null) {
            region = new Rectangle(0, 0, clip.getFrameCount(), clip.getFrameFreqSamples());
        }
        
        lastImageUpdateRegion = new Rectangle(region);
        
        // cheese-out that just repaints the whole frames in question
//        region.y = 0;
//        region.height = clip.getFrameFreqSamples();
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
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(0, img.getHeight());
        g2.scale(1.0, -1.0);
        
        Rectangle clipBounds = g2.getClipBounds();
        if (clipBounds != null) {
            g2.drawImage(img,
                    clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height,
                    clipBounds.x, clipBounds.y, clipBounds.x + clipBounds.width, clipBounds.y + clipBounds.height,
                    Color.BLACK, null);
        } else {
            g2.drawImage(img, 0, 0, null);
        }
        
        if (logger.isLoggable(Level.FINE) && lastImageUpdateRegion != null) {
            g2.setColor(Color.YELLOW);
            g2.drawRect(
                    lastImageUpdateRegion.x, lastImageUpdateRegion.y,
                    lastImageUpdateRegion.width, lastImageUpdateRegion.height);
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
}
