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

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GreyscaleLogColorizer implements ValueColorizer {

    double multiplier = 6000.0;
    private final ClipPanel clipPanel;

    private final JComponent settingsPanel;

    GreyscaleLogColorizer(ClipPanel clipPanel) {
        this.clipPanel = clipPanel;
        final JSlider brightness = new JSlider(0, 5000000, (int) (multiplier * 100.0));
        brightness.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setMultiplier( ((double) brightness.getValue()) / 100.0 );
            }
        });
        settingsPanel = Box.createVerticalBox();
        settingsPanel.add(new JLabel("Brightness"));
        settingsPanel.add(brightness);
    }

    public int colorFor(double val) {
        int greyVal = (int) (multiplier * Math.abs(val));
        if (greyVal < 0) {
            greyVal = 0;
        } else if (greyVal > 255) {
            greyVal = 255;
        }
        return (greyVal << 16) | (greyVal << 8) | (greyVal);
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        clipPanel.updateImage(null);
        clipPanel.repaint();
    }
    
    public JComponent getSettingsPanel() {
        return settingsPanel;
    }
}
