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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LogarithmicColorizer implements ValueColorizer {

    private double preMult = 0;
    private double brightness = 0;
    private double contrast = 0;
    private boolean useRed = false;
    
    private final ClipPanel clipPanel;

    private final JComponent settingsPanel;

    LogarithmicColorizer(ClipPanel clipPanel) {
        this.clipPanel = clipPanel;
        
        final JSlider preMultSlider = new JSlider(0, 7000000, 0);
        preMultSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setPreMult( Math.expm1(preMultSlider.getValue() / 1000000.0 ) );
            }
        });

        final JSlider brightnessSlider = new JSlider(0, 7000000, 0);
        brightnessSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setBrightness( Math.expm1(brightnessSlider.getValue() / 1000000.0 ) );
            }
        });

        final JSlider contrastSlider = new JSlider(0, 7000000, 0);
        contrastSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                setContrast( Math.expm1(contrastSlider.getValue() / 500000.0 ) );
            }
        });

        final JCheckBox useRedCheckbox = new JCheckBox("Use red", useRed);
        useRedCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUseRed(useRedCheckbox.isSelected());
            }
        });
        
        settingsPanel = Box.createVerticalBox();
        settingsPanel.add(new JLabel("Pre Multiplier"));
        settingsPanel.add(preMultSlider);
        settingsPanel.add(new JLabel("Brightness"));
        settingsPanel.add(brightnessSlider);
        settingsPanel.add(new JLabel("Contrast"));
        settingsPanel.add(contrastSlider);
        settingsPanel.add(useRedCheckbox);
        
        preMultSlider.setValue(3000000);
        brightnessSlider.setValue(0);
        contrastSlider.setValue(3000000);
    }

    public int colorFor(double val) {
        int greyVal = (int) (brightness + (contrast * Math.log1p(Math.abs(preMult * val))));
        
        if (useRed) {
            if (greyVal < 0) {
                return 0;
            } else if (greyVal <= 255) {
                return (greyVal << 16) | (greyVal << 8) | (greyVal);
            } else if (greyVal <= 512) {
                greyVal -= 256;
                greyVal = 256 - greyVal;
                return 0xff0000 | (greyVal << 8) | (greyVal);
            } else {
                return 0xff0000;
            }
        } else {
            greyVal = Math.min(255, Math.max(0, greyVal));
            return (greyVal << 16) | (greyVal << 8) | (greyVal);
        }
        
    }

    public void setPreMult(double multiplier) {
        this.preMult = multiplier;
        clipPanel.updateImage(null);
        clipPanel.repaint();
    }

    public void setBrightness(double multiplier) {
        this.brightness = multiplier;
        clipPanel.updateImage(null);
        clipPanel.repaint();
    }

    public void setContrast(double multiplier) {
        this.contrast = multiplier;
        clipPanel.updateImage(null);
        clipPanel.repaint();
    }

    public void setUseRed(boolean useRed) {
        this.useRed = useRed;
        clipPanel.updateImage(null);
        clipPanel.repaint();
    }

    public JComponent getSettingsPanel() {
        return settingsPanel;
    }
}
