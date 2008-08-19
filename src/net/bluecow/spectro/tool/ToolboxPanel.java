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
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.bluecow.spectro.ClipPanel;
import net.bluecow.spectro.PlayerThread;

public class ToolboxPanel {

    /**
     * The clip panel we're modifying settings for.
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
    
    public ToolboxPanel(ClipPanel cp) {
        this.clipPanel = cp;
        
        JPanel topPanel = new JPanel();
        topPanel.add(makeBrightnessSlider());
        topPanel.add(makeShuttleControls());
        topPanel.add(makeSaveButton());
        
        toolSettingsPanel = new JPanel(new BorderLayout());

        toolChooser = new JComboBox();
        toolChooser.addItem(new PaintbrushTool());
        toolChooser.addItem(new RegionScaleTool());
//        toolChooser.setSelectedItem(null);
        toolSettingsPanel.add(toolChooser, BorderLayout.WEST);

        toolChooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentTool != null) {
                    currentTool.deactivate();
                    toolSettingsPanel.remove(currentTool.getSettingsPanel());
                }
                currentTool = (Tool) toolChooser.getSelectedItem();
                currentTool.activate(clipPanel);
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
    
    private Component makeShuttleControls() {
        try {
            final PlayerThread playerThread = new PlayerThread(clipPanel.getClip()); // FIXME
            playerThread.start();
            final JButton playPause = new JButton("Play");
            final JButton rewind = new JButton("Rewind");
            playPause.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if ("Play".equals(playPause.getText())) {
                        playerThread.startPlaying();
                        playPause.setText("Pause");
                    } else if ("Pause".equals(playPause.getText())) {
                        playerThread.stopPlaying();
                        playPause.setText("Play");
                    }
                }
            });

            rewind.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    playerThread.setPlaybackPosition(0);
                }
            });
            

            JPanel p = new JPanel(new FlowLayout());
            p.add(playPause);
            p.add(rewind);
            return p;
        } catch (LineUnavailableException ex) {
            throw new RuntimeException(ex);
        }
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
    
    private JButton makeSaveButton() {
        final JButton saveButton = new JButton("Save...");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    FileDialog fd;
                    Window owner = SwingUtilities.getWindowAncestor(panel);
                    if (owner instanceof java.awt.Frame) {
                        fd = new FileDialog((java.awt.Frame) owner, "Save sample as", FileDialog.SAVE);
                    } else {
                        fd = new FileDialog((Dialog) owner, "Save sample as", FileDialog.SAVE);
                    }
                    fd.setVisible(true);
                    String dir = fd.getDirectory();
                    String file = fd.getFile();
                    if (file == null) return;
                    if (!file.toLowerCase().endsWith(".wav")) {
                        file += ".wav";
                    }
                    AudioSystem.write(
                            clipPanel.getClip().getAudio(),
                            AudioFileFormat.Type.WAVE,
                            new File(dir, file));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        return saveButton;
    }
    
    public JPanel getPanel() {
        return panel;
    }
}
