/*
 * Created on Jul 18, 2008
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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bluecow.spectro.ClipPanel;
import net.bluecow.spectro.SpectroEditSession;

public class ToolboxPanel {

    /**
     * The session this toolbox panel lives in.
     */
    private final SpectroEditSession session;
    
    /**
     * The clip panel we're modifying settings for. This is meant to
     * be the same clipPanel that belongs to {@link #session}.
     */
    private final ClipPanel clipPanel;
    
    /**
     * The panel with the user interface for changing the settings.
     */
    private final JPanel panel;
    
    private final JPanel toolSettingsPanel;
    
    private final JComboBox toolChooser;
    
    /**
     * The current tool.
     */
    private Tool currentTool;

    public ToolboxPanel(SpectroEditSession session) {
        this.session = session;
        this.clipPanel = session.getClipPanel();
        
        JPanel topPanel = new JPanel();
        topPanel.add(makeBrightnessSlider());
        
        toolSettingsPanel = new JPanel(new BorderLayout());

        toolChooser = new JComboBox();
        toolChooser.addItem(new PaintbrushTool());
        toolChooser.addItem(new RegionScaleTool());
        toolSettingsPanel.add(toolChooser, BorderLayout.WEST);

        toolChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentTool != null) {
                    currentTool.deactivate();
                    toolSettingsPanel.remove(currentTool.getSettingsPanel());
                }
                currentTool = (Tool) toolChooser.getSelectedItem();
                currentTool.activate(ToolboxPanel.this.session);
                toolSettingsPanel.add(currentTool.getSettingsPanel(), BorderLayout.CENTER);
                panel.revalidate();
            }
        });
        
        panel = new JPanel(new GridLayout(2, 1));
        panel.add(topPanel);
        panel.add(toolSettingsPanel);
        
        // activates the action listener to select the default tool (must be done last)
        toolChooser.setSelectedIndex(0);
    }

    public JComponent makeBrightnessSlider() {
        final JSlider brightness = new JSlider(0, 5000000, (int) (clipPanel.getSpectralToScreenMultiplier() * 100.0));
        brightness.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                clipPanel.setSpectralToScreenMultiplier( ((double) brightness.getValue()) / 100.0 );
            }
        });
        Box box = Box.createVerticalBox();
        box.add(new JLabel("Brightness"));
        box.add(brightness);
        return box;
    }
    
    public JPanel getPanel() {
        return panel;
    }
}
