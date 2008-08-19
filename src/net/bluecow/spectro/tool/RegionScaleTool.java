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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.bluecow.spectro.Clip;
import net.bluecow.spectro.ClipPanel;
import net.bluecow.spectro.Frame;

public class RegionScaleTool implements Tool {

    private ClipPanel clipPanel;
    private Clip clip;
    
    private final JPanel settingsPanel;
    
    public RegionScaleTool() {
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
        clipPanel.setRegionMode(true);
    }

    public void deactivate() {
        clip = null;
        clipPanel = null;
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
        Rectangle region = clipPanel.getRegion();
        if (region == null) return;
        Rectangle frameRegion = clipPanel.toClipCoords(new Rectangle(region));
        clip.beginEdit(frameRegion, "Scale region");
        for (int i = frameRegion.x; i < frameRegion.x + frameRegion.width; i++) {
            Frame frame = clip.getFrame(i);
            for (int j = frameRegion.y; j < frameRegion.y + frameRegion.height; j++) {
                frame.setReal(j, frame.getReal(j) * amount);
            }
        }
        clip.endEdit();
    }
}
