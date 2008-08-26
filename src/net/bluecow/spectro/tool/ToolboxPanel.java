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
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
    
    private final JPanel toolButtonPanel;
    private final JPanel toolSettingsPanel;
    private final JPanel viewSettingsPanel;
    
    private final ButtonGroup toolButtonGroup = new ButtonGroup();
    
    
    /**
     * The current tool.
     */
    private Tool currentTool;

    public ToolboxPanel(SpectroEditSession session) {
        this.session = session;
        this.clipPanel = session.getClipPanel();
        
        viewSettingsPanel = new JPanel();
        viewSettingsPanel.add(makeBrightnessSlider());
        
        toolSettingsPanel = new JPanel(new BorderLayout());

        toolButtonPanel = new JPanel(new FlowLayout());
        JRadioButton paintbrushToolButton = new ToolButton(new PaintbrushTool(), "paintbrush", toolButtonGroup);
        JRadioButton regionScaleToolButton = new ToolButton(new RegionScaleTool(), "page_white_put", toolButtonGroup); // TODO better icon

        toolButtonPanel.add(paintbrushToolButton);
        toolButtonPanel.add(regionScaleToolButton);
        
        ActionListener actionHandler = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentTool != null) {
                    currentTool.deactivate();
                    toolSettingsPanel.remove(currentTool.getSettingsPanel());
                }
                currentTool = ((ToolButton) e.getSource()).getTool();
                currentTool.activate(ToolboxPanel.this.session);
                toolSettingsPanel.add(currentTool.getSettingsPanel(), BorderLayout.CENTER);
                panel.revalidate();
            }
        };

        paintbrushToolButton.addActionListener(actionHandler);
        regionScaleToolButton.addActionListener(actionHandler);
        
        panel = new JPanel(new GridLayout(3, 1));
        panel.add(toolButtonPanel);
        panel.add(toolSettingsPanel);
        panel.add(viewSettingsPanel);
        
        // activates the action listener to select the default tool (must be done last)
        paintbrushToolButton.setSelected(true);
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
