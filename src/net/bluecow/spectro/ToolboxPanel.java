/*
 * Created on Jul 18, 2008
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.spectro;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ToolboxPanel {

    /**
     * The clip panel we're modifying settings for.
     */
    private final ClipPanel clipPanel;
    
    /**
     * The panel with the user interface for changing the settings.
     */
    private final JPanel panel;
    
    public ToolboxPanel(ClipPanel clipPanel) {
        this.clipPanel = clipPanel;
        panel = new JPanel();
        panel.add(makeBrightnessSlider());
        panel.add(makeShuttleControls());
        panel.add(makeSaveButton());
        panel.add(makePaintControls());
    }
    
    private JComponent makePaintControls() {
        final PaintbrushTool paintbrush = new PaintbrushTool(clipPanel);
        JLabel paintLabel = new JLabel("Paintbrush size: " + paintbrush.getRadius());
        return paintLabel;
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
                    JDialog owner = (JDialog) SwingUtilities.getWindowAncestor(panel);
                    FileDialog fd = new FileDialog(owner, "Save sample as", FileDialog.SAVE);
                    fd.setVisible(true);
                    String path = fd.getFile();
                    if (path == null) return;
                    AudioSystem.write(clipPanel.getClip().getAudio(), AudioFileFormat.Type.WAVE, new File(path));
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
