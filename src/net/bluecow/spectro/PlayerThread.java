/*
 * Created on Jul 22, 2008
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

import java.io.IOException;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class PlayerThread extends Thread {
    
    private static final Logger logger = Logger.getLogger(PlayerThread.class.getName());

    /**
     * Whether this thread should currently be playing audio. When true,
     * playback proceeds; when false, playback is paused. Use the
     * {@link #startPlaying(boolean)} and {@link #stopPlaying()} methods to
     * manipulate this variable, because they properly handle
     * synchronization between threads.
     */
    private boolean playing = false;
    
    /**
     * When true, this thread will terminate at its earliest opportunity.
     * Once terminated, it cannot be restarted. Use the {@link #terminate()}
     * method to set this flag, because it properly handles synchronization
     * between threads.
     */
    private boolean terminated = false;

    private SourceDataLine outputLine;

    private final Clip clip;

    private AudioInputStream in;
    
    public PlayerThread(Clip clip) throws LineUnavailableException {
        this.clip = clip;
    }
    
    @Override
    public void run() {
        if (in == null) {
            setPlaybackPosition(0);
        }
        try {
            AudioFormat outputFormat = in.getFormat();
            outputLine = AudioSystem.getSourceDataLine(outputFormat);
            logger.finer("Output line buffer: "+outputLine.getBufferSize());
            outputLine.open();
            outputLine.start();

            byte[] buf = new byte[outputLine.getBufferSize()];

            while (!terminated) {
                while (playing) {
                    int readSize = outputLine.available();
                    int len = in.read(buf, 0, readSize);
                    if (len != readSize) {
                        logger.fine(String.format("Didn't read full %d bytes (got %d)\n", readSize, len));
                    }
                    if (len == -1) {
                        setPlaybackPosition(0);
                        // TODO need to notify client code that playback has completed
                        playing = false;
                    } else {
                        outputLine.write(buf, 0, len);
                    }
                }

                outputLine.drain();

                for (;;) {
                    synchronized (this) {
                        if (playing || terminated) break;
                        // if not playing and not terminated, sleep again!
                    }
                    try {
                        Object[] args = { playing };
                        logger.finest(String.format("Player thread sleeping for 10 seconds. playing=%b\n", args));
                        sleep(10000);
                    } catch (InterruptedException ex) {
                        Object[] args = {};
                        logger.finest(String.format("Player thread interrupted in sleep\n", args));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputLine != null) {
                outputLine.close();
                outputLine = null;
            }
        }
        Object[] args = {};
        
        logger.fine(String.format("Player thread terminated\n", args));
    }
    
    public synchronized void stopPlaying() {
        playing = false;
        // no need to interrupt in this case
    }
    
    public synchronized void startPlaying() {
        playing = true;
        interrupt();
    }
    
    /**
     * Halts playback and permanently stops this thread.
     */
    public synchronized void terminate() {
        stopPlaying();
        terminated = true;
        interrupt();
    }
    
    /**
     * Sets the current playback position of this thread. Position 0 is
     * the beginning of the clip.
     *  
     * @param sample The sample number to jump to.  The time offset this represents
     * depends on the audio format (specifically, the sampling rate) of the clip.
     */
    public synchronized void setPlaybackPosition(int sample) {
        if (sample != 0) {
            throw new UnsupportedOperationException("Currently, only rewind to beginning is supported.");
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        in = clip.getAudio();
    }
}
