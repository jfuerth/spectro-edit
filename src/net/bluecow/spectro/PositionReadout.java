/*
 * Created on Aug 28, 2008
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

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;

/**
 * Manages a component that shows the current mouse position in terms of time and frequency.
 */
public class PositionReadout {

    private final ClipPanel cp;

    private final JLabel label = new JLabel();
    
    public PositionReadout(ClipPanel cp) {
        this.cp = cp;
        cp.addMouseMotionListener(mouseHandler);
    }
    
    private MouseMotionListener mouseHandler = new MouseMotionListener() {

        public void mouseDragged(MouseEvent e) {
            mouseMoved(e);
        }

        public void mouseMoved(MouseEvent e) {
            double rate = cp.getClip().getSamplingRate();
            double fSamples = cp.getClip().getFrameFreqSamples();
            double tSamples = cp.getClip().getFrameTimeSamples();
            Point p = cp.toClipCoords(e.getPoint());
            label.setText(String.format("0:%06.03fs %6.0fHz", p.getX() * tSamples / rate, ((rate / 2.0) / fSamples) * p.getY()));
        }
        
    };
    
    public JLabel getLabel() {
        return label;
    }
}
