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
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
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

    private final TitleBorder toolSettingsBorder;

    public ToolboxPanel(SpectroEditSession session) {
        this.session = session;
        this.clipPanel = session.getClipPanel();
        
        viewSettingsPanel = new JPanel();
        viewSettingsPanel.setBackground(Color.WHITE);
        viewSettingsPanel.setBorder(new TitleBorder("View Settings"));
        viewSettingsPanel.add(makeBrightnessSlider());
        
        toolSettingsPanel = new JPanel(new BorderLayout());
        toolSettingsPanel.setBackground(Color.WHITE);
        toolSettingsBorder = new TitleBorder("Tool Settings");
        toolSettingsPanel.setBorder(toolSettingsBorder);

        toolButtonPanel = new JPanel(new FlowLayout());
        toolButtonPanel.setBackground(Color.WHITE);
        toolButtonPanel.setBorder(new TitleBorder("Tools"));

        ActionListener actionHandler = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentTool != null) {
                    currentTool.deactivate();
                    toolSettingsPanel.remove(currentTool.getSettingsPanel());
                }
                currentTool = ((ToolButton) e.getSource()).getTool();
                currentTool.activate(ToolboxPanel.this.session);
                toolSettingsPanel.add(currentTool.getSettingsPanel(), BorderLayout.CENTER);
                toolSettingsBorder.setTitle("Tool Settings: " + currentTool.getName());
                panel.revalidate();
                panel.repaint();
            }
        };

        JRadioButton paintbrushToolButton = new ToolButton(new PaintbrushTool(), "paintbrush", toolButtonGroup);
        toolButtonPanel.add(paintbrushToolButton);
        paintbrushToolButton.addActionListener(actionHandler);

        JRadioButton regionScaleToolButton = new ToolButton(new RegionScaleTool(), "page_white_put", toolButtonGroup); // TODO better icon
        toolButtonPanel.add(regionScaleToolButton);
        regionScaleToolButton.addActionListener(actionHandler);

        JRadioButton regionThresholdToolButton = new ToolButton(new RegionThresholdTool(), "page_white_put", toolButtonGroup); // TODO better icon
        toolButtonPanel.add(regionThresholdToolButton);
        regionThresholdToolButton.addActionListener(actionHandler);

        JRadioButton flipToolButton = new ToolButton(new RegionFlipTool(), "page_white_put", toolButtonGroup); // TODO better icon
        toolButtonPanel.add(flipToolButton);
        flipToolButton.addActionListener(actionHandler);

        panel = new JPanel(new GridLayout(3, 1));
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        panel.setBackground(Color.WHITE);
        panel.add(toolButtonPanel);
        panel.add(toolSettingsPanel);
        panel.add(viewSettingsPanel);
        
        // activates the action listener to select the default tool (must be done last)
        paintbrushToolButton.doClick();
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
