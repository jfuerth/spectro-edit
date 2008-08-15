/*
 * Created on Aug 6, 2008
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
package net.bluecow.spectro.tool;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.bluecow.spectro.Clip;
import net.bluecow.spectro.ClipPanel;
import net.bluecow.spectro.Frame;

public class RegionTool implements Tool {

    private Rectangle region;
    private ClipPanel clipPanel;
    private Clip clip;
    private final RegionMouseHandler mouseHandler = new RegionMouseHandler();
    
    private final JPanel settingsPanel;
    
    public RegionTool() {
        settingsPanel = new JPanel(new FlowLayout());
        settingsPanel.add(new JLabel("Region tool"));
        
        JButton scaleUpButton = new JButton("Scale Up");
        scaleUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scale(1.1);
            }
        });
        settingsPanel.add(scaleUpButton);
        
        JButton scaleDownButton = new JButton("Scale Down");
        scaleDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scale(0.91);
            }
        });
        settingsPanel.add(scaleDownButton);
    }

    public void activate(ClipPanel cp) {
        this.clipPanel = cp;
        clip = clipPanel.getClip();
        clipPanel.addMouseListener(mouseHandler);
        clipPanel.addMouseMotionListener(mouseHandler);
    }

    public void deactivate() {
        region = null; // or we could keep the region and restore it when reactivated
        repaintRegion();
        clipPanel.removeMouseListener(mouseHandler);
        clipPanel.removeMouseMotionListener(mouseHandler);
        clip = null;
        clipPanel = null;
    }

    /**
     * Just syncs the location of the rectangular region and asks for a repaint
     * on the clip panel. The spectral data is not recalculated--to do that, call
     * {@link #updateImage()}.
     */
    public void repaintRegion() {
        Rectangle normalRegion = null;
        if (region != null) {
            normalRegion = new Rectangle(region);
            normalize(normalRegion);
        }
        clipPanel.updateRegion(normalRegion);
    }

    /**
     * Asks the clip panel to update its idea of the underlying spectral data. This
     * must be called after modifying any of the clip's frame data in order for the
     * changes to be visible on screen. This method also invokes {@link #repaintRegion()}
     * for you, so there is no need to call it after calling this method.
     */
    public void updateImage() {
        clipPanel.updateImage(region);
        repaintRegion();
    }
    
    /**
     * Ensures the given rectangle has nonnegative width and height.
     * The rectangle's actual geometry is unchanged.
     * 
     * @param rect The rectangle to modify. This rectangle may be modified
     * as a side effect of this method call!
     */
    private static void normalize(Rectangle rect) {
        if (rect.width < 0) {
            rect.x += rect.width;
            rect.width *= -1;
        }
        if (rect.height < 0) {
            rect.y += rect.height;
            rect.height *= -1;
        }
    }
    
    enum MouseMode { IDLE, SIZING, MOVING }

    private class RegionMouseHandler implements MouseMotionListener, MouseListener {

        MouseMode mode = MouseMode.IDLE;
        
        /**
         * The offset from the region's (x,y) origin that the mouse should
         * stay at when the region is being moved.
         */
        Point moveHandle;
        
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
            repaintRegion();
        }

        public void mousePressed(MouseEvent e) {
            Point p = e.getPoint();
            if (region != null && region.contains(p)) {
                mode = MouseMode.MOVING;
                moveHandle = new Point(p.x - region.x, p.y - region.y);
            } else {
                startRect(p);
                mode = MouseMode.SIZING;
            }
            repaintRegion();
        }

        public void mouseReleased(MouseEvent e) {
            mode = MouseMode.IDLE;
            
            // only do this when mouse is released because region resize
            // depends on the rectangle's origin not changing
            if (region != null) {
                normalize(region);
            }
            
            repaintRegion();
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
            region = new Rectangle(p.x, p.y, 0, 0);
        }

        private void resizeRect(Point p) {
            region.width = p.x - region.x;
            region.height = p.y - region.y;
        }

        private void moveRect(Point p) {
            region.x = p.x - moveHandle.x;
            region.y = p.y - moveHandle.y;
        }
    }

    public JComponent getSettingsPanel() {
        return settingsPanel;
    }

    @Override
    public String toString() {
        return "Region";
    }
    
    /**
     * Scales the actual clip data in the given region by the amount given. 1.0 means
     * no change; between 0.0 and 1.0 means to reduce volume, and &gt;1.0 means
     * to increase volume.
     */
    public void scale(double amount) {
        if (region == null) return;
        Rectangle frameRegion = clipPanel.toClipCoords(new Rectangle(region));
        for (int i = frameRegion.x; i < frameRegion.x + frameRegion.width; i++) {
            Frame frame = clip.getFrame(i);
            for (int j = frameRegion.y; j < frameRegion.y + frameRegion.height; j++) {
                frame.setReal(j, frame.getReal(j) * amount);
            }
        }
        updateImage();
    }
}
