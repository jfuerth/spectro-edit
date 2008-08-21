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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.Scrollable;

public class ClipPanel extends JPanel implements Scrollable {

    private static final Logger logger = Logger.getLogger(ClipPanel.class.getName());
    
    /**
     * The clip this panel visualizes.
     */
    private final Clip clip;

    private final BufferedImage img;
    
    private double spectralToScreenMultiplier = 6000.0;
    
    /**
     * A rectangular frame that some tools use as a bounding box for
     * the changes they make. The region feature can be turned on and off.
     */
    private Rectangle region;
    
    /**
     * The place where the region was last time it was repainted. Starts off
     * as null, and gets reset to null whenever region selection is turned off.
     */
    private Rectangle oldRegion;

    /**
     * Flag to indicate whether or not region mode is active.
     */
    private boolean regionMode;
    
    private final RegionMouseHandler mouseHandler = new RegionMouseHandler();

    private ClipDataChangeListener clipDataChangeHandler = new ClipDataChangeListener() {

        public void clipDataChanged(ClipDataChangeEvent e) {
            Rectangle r = toScreenCoords(e.getRegion());
            updateImage(r);
            repaint(r);
        }
        
    };
    
    public static ClipPanel newInstance(Clip clip) {
        ClipPanel cp = new ClipPanel(clip);
        clip.addClipDataChangeListener(cp.clipDataChangeHandler);
        return cp;
    }
    
    private ClipPanel(Clip clip) {
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

    public Rectangle toScreenCoords(Rectangle r) {
        // These operations are actually the same (the operation is its own inverse)
        return toClipCoords(r);
    }

    /**
     * Updates the image based on the existing Clip data and the settings in
     * this panel (such as the multiplier).
     * 
     * @param region
     *            The region to update, in screen co-ordinates. Null means to
     *            update the whole image.
     */
    private void updateImage(Rectangle region) {
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
     * Produces a repaint request that covers the old region as it existed last
     * time this method was called, and the new region as of now.  It shouldn't
     * be necessary to call this method directly; use {@link #setRegion(Rectangle)}.
     */
    private void repaintRegion() {
        Rectangle newRegion = (region == null) ? null : new Rectangle(region);
        if (oldRegion != null && newRegion == null) {
            repaint(oldRegion.x, oldRegion.y, oldRegion.width + 1, oldRegion.height + 1);
        } else if (oldRegion == null && newRegion != null) {
            repaint(newRegion.x, newRegion.y, newRegion.width + 1, newRegion.height + 1);
        } else if (oldRegion != null && newRegion != null) {
            oldRegion.add(newRegion);
            repaint(oldRegion.x, oldRegion.y, oldRegion.width + 1, oldRegion.height + 1);
        }
        oldRegion = newRegion;
    }

    /**
     * Turns region selection mode on or off. Attempting to set the region mode
     * to its current setting is not an error, but has no effect.
     * 
     * @param on
     *            The new setting for region mode (true for on; false for off).
     */
    public void setRegionMode(boolean on) {
        if (on != regionMode) {
            if (on) {
                regionMode = true;
                addMouseListener(mouseHandler);
                addMouseMotionListener(mouseHandler);
            } else {
                regionMode = false;
                setRegion(null);
                oldRegion = null;
                removeMouseListener(mouseHandler);
                removeMouseMotionListener(mouseHandler);
            }
        }
    }

    /**
     * Returns a copy of the currently-selected region of this clip panel. If
     * there is no region selected, returns null.
     * 
     * @return A new rectangle of this clip's selected region. The copy is
     *         independent of this clip panel--you can modify it if you wish.
     */
    public Rectangle getRegion() {
        if (region == null) {
            return null;
        } else {
            return new Rectangle(region);
        }
    }
    
    /**
     * Updates the geometry of the selected region.
     * 
     * @param r The new location and size for the selected region.
     */
    private void setRegion(Rectangle r) {
        Rectangle oldRegion = region;
        region = normalized(r);
        repaintRegion();
        firePropertyChange("region", oldRegion, region);
    }
    
    /**
     * Enumeration of states for the {@link RegionMouseHandler}.
     */
    enum MouseMode { IDLE, SIZING, MOVING }

    /**
     * Handles mouse events on this panel for purposes of creating, moving, and resizing
     * the region.
     */
    private class RegionMouseHandler implements MouseMotionListener, MouseListener {

        MouseMode mode = MouseMode.IDLE;
        
        /**
         * The offset from the region's (x,y) origin that the mouse should
         * stay at when the region is being moved.
         */
        Point moveHandle;
        
        Rectangle tempRegion;
        
//        /**
//         * The place where the user clicked to start resizing the region.
//         */
//        Point regionOrigin;
        
        public void mouseDragged(MouseEvent e) {
            switch (mode) {
            case IDLE:
                startRect(e.getPoint());
                break;
            case SIZING:
                resizeRect(e.getPoint());
                break;
            case MOVING:
                moveRect(e.getPoint());
                break;
            }
            setRegion(tempRegion);
        }

        public void mousePressed(MouseEvent e) {
            tempRegion = normalized(region);
            Point p = e.getPoint();
            if (tempRegion != null && tempRegion.contains(p)) {
                mode = MouseMode.MOVING;
                moveHandle = new Point(p.x - tempRegion.x, p.y - tempRegion.y);
            } else {
                startRect(p);
                mode = MouseMode.SIZING;
            }
            setRegion(tempRegion);
        }

        public void mouseReleased(MouseEvent e) {
            mode = MouseMode.IDLE;
            
            setRegion(tempRegion);
            tempRegion = null;
        }
        
        public void mouseMoved(MouseEvent e) {
            // don't care
        }

        public void mouseClicked(MouseEvent e) {
            // don't care
        }

        public void mouseEntered(MouseEvent e) {
            // don't care
        }

        public void mouseExited(MouseEvent e) {
            // don't care
        }
        
        private void startRect(Point p) {
            tempRegion = new Rectangle(p.x, p.y, 0, 0);
        }

        private void resizeRect(Point p) {
            tempRegion.width = p.x - tempRegion.x;
            tempRegion.height = p.y - tempRegion.y;
            logger.finer("Resizing region to: " + tempRegion);
        }

        private void moveRect(Point p) {
            tempRegion.x = p.x - moveHandle.x;
            tempRegion.y = p.y - moveHandle.y;
        }

    }

    /**
     * Creates a copy of the given rectangle with nonnegative width and
     * height. The new rectangle's actual geometry is the same as the given
     * rectangle's.
     * 
     * @param rect
     *            The source rectangle. This rectangle will not be changed
     *            as a result of this call. You can pass in null, which
     *            results in a null return value.
     * @return A rectangle with the same position and size as rect, but with
     *         nonnegative width and height. Returns null if rect is null.
     */
    private Rectangle normalized(Rectangle rect) {
        if (rect == null) return null;
        rect = new Rectangle(rect);
        if (rect.width < 0) {
            rect.x += rect.width;
            rect.width *= -1;
        }
        if (rect.height < 0) {
            rect.y += rect.height;
            rect.height *= -1;
        }
        return rect;
    }

    
    // --------------------- Scrollable interface ------------------------
    
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (int) (visibleRect.width * 0.9);
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 50;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

}
