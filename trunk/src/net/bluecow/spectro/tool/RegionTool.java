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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.bluecow.spectro.Clip;
import net.bluecow.spectro.ClipPanel;

public class RegionTool {

    private Rectangle region;
    private final ClipPanel clipPanel;
    private final Clip clip;
    private final RegionMouseHandler mouseHandler = new RegionMouseHandler();
    
    public RegionTool(ClipPanel clipPanel) {
        this.clipPanel = clipPanel;
        clip = clipPanel.getClip();
        clipPanel.addMouseListener(mouseHandler);
        clipPanel.addMouseMotionListener(mouseHandler);
    }
    
    public void discard() {
        clipPanel.removeMouseListener(mouseHandler);
        clipPanel.removeMouseMotionListener(mouseHandler);
    }
    
    public void repaintRegion() {
        clipPanel.updateRegion(region);
    }
    
    enum MouseMode { IDLE, SIZING, MOVING }

    private class RegionMouseHandler implements MouseMotionListener, MouseListener {

        MouseMode mode = MouseMode.IDLE;
        
        /**
         * The offset from the region's (x,y) origin that the mouse should
         * stay at when the region is being moved.
         */
        Point moveHandle;
        
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

}
