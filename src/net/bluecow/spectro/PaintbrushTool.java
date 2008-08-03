/*
 * Created on Jul 29, 2008
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.spectro;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class PaintbrushTool {

    int radius = 5;
    private final ClipPanel clipPanel;
    private final Clip clip;
    private final PaintbrushMouseHandler mouseHandler = new PaintbrushMouseHandler();
    
    public PaintbrushTool(ClipPanel clipPanel) {
        this.clipPanel = clipPanel;
        clip = clipPanel.getClip();
        clipPanel.addMouseListener(mouseHandler);
        clipPanel.addMouseMotionListener(mouseHandler);
    }
    
    public void discard() {
        clipPanel.removeMouseListener(mouseHandler);
        clipPanel.removeMouseMotionListener(mouseHandler);
    }
    
    private class PaintbrushMouseHandler implements MouseMotionListener, MouseListener {

        public void mouseDragged(MouseEvent e) {
            Point p = clipPanel.toClipCoords(e.getPoint());
            for (int x = p.x - radius; x < p.x + radius; x++) {
                Frame f = clip.getFrame(x);
                for (int y = p.y - radius; y < p.y + radius; y++) {
                    f.setReal(y, 0.0);
                }
            }
            Rectangle updateRegion = new Rectangle(e.getX() - radius, e.getY() - radius, radius * 2, radius * 2);
            clipPanel.updateImage(updateRegion);
            clipPanel.repaint(e.getX() - radius, e.getY() - radius, radius * 2, radius * 2);
//          clipPanel.repaint(updateRegion);
//            clipPanel.repaint(p.x, p.y, radius * 2, radius * 2);
        }

        public void mouseMoved(MouseEvent e) {
            // maybe draw an outline of the paintbrush
        }

        public void mouseClicked(MouseEvent e) {
            // don't care?
        }

        public void mouseEntered(MouseEvent e) {
            // don't care?
        }

        public void mouseExited(MouseEvent e) {
            // don't care?
        }

        public void mousePressed(MouseEvent e) {
            mouseDragged(e);
        }

        public void mouseReleased(MouseEvent e) {
            // don't care?
        }
        
    }

    public int getRadius() {
        return radius;
    }
}
