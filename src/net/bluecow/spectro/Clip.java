/*
 * Created on Jul 8, 2008
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * A Clip represents an audio clip of some length. The clip is split up
 * into a series of equal-size frames of spectral information.  The frames
 * of spectral information can be accessed in random order, and the clip
 * can also provide an AudioInputStream of the current spectral information
 * for playback or saving to a traditional PCM (WAV or AIFF) audio file.
 */
public class Clip {

    private static final Logger logger = Logger.getLogger(Clip.class.getName());

    /**
     * The audio format this class works with. Input audio will be converted to this
     * format automatically, and output data will always be created in this format.
     */
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(44100, 16, 1, true, true);

    private final List<Frame> frames = new ArrayList<Frame>();
    
    /**
     * Number of samples per frame. Currently must be a power of 2 (this is a requirement
     * of many DFT routines).
     */
    private int frameSize = 1024;
    
    /**
     * The amount of overlap: this is the number of frames that will carry information
     * about the same sample. A value of 1 means no overlap; 2 means frames will overlap
     * to cover every sample twice, and so on.  More overlap means better time resolution.
     */
    private int overlap = 2;
    
    /**
     * The amount that the time samples are divided by before sending to the transformation,
     * and the amount they're multiplied after being transformed back.
     */
    private double spectralScale = 10000.0;

    /**
     * Creates a new Clip based on the acoustical information in the given audio
     * file.
     * <p>
     * TODO: this could be time-consuming, so spectral conversion should be done
     * in a background thread.
     * 
     * @param file
     *            The audio file to read. Currently, single-channel WAV and AIFF
     *            are supported.
     * @throws UnsupportedAudioFileException
     *             If the given file can't be read because it's not of a
     *             supported type.
     * @throws IOException
     *             If the file can't be read for more basic reasons, such
     *             as nonexistence.
     */
    public Clip(File file) throws UnsupportedAudioFileException, IOException {
        WindowFunction windowFunc = new VorbisWindowFunction(frameSize);
        AudioFormat desiredFormat = AUDIO_FORMAT;
        BufferedInputStream in = new BufferedInputStream(AudioSystem.getAudioInputStream(desiredFormat, AudioSystem.getAudioInputStream(file)));
        
        // TODO: overlapping frames and window function (windowing might be better handled in Frame)
        byte[] buf = new byte[frameSize * 2]; // 16-bit mono samples
        int n;
        in.mark(buf.length * 2);
        while ( (n = in.read(buf)) != -1) {
            logger.fine("Read "+n+" bytes");
            double[] samples = new double[frameSize];
            for (int i = 0; i < frameSize; i++) {
                int hi = buf[2*i];// & 0xff; // need sign extension
                int low = buf[2*i + 1] & 0xff;
                int sampVal = ( (hi << 8) | low);
                samples[i] = (sampVal / spectralScale);
            }
            
            frames.add(new Frame(samples, windowFunc));
            in.reset();
            in.skip((frameSize * 2) / overlap);
            in.mark(buf.length * 2);
        }
        
        logger.info(String.format("Read %d frames from %s (%d bytes)\n", frames.size(), file.getAbsolutePath(), frames.size() * buf.length));
    }
    
    /**
     * Returns the number of time samples per frame.
     */
    public int getFrameTimeSamples() {
        return frameSize;
    }

    /**
     * Returns the number of frequency samples per frame.
     */
    public int getFrameFreqSamples() {
        return frameSize;
    }

    /**
     * Returns the number of frames.
     * @return
     */
    public int getFrameCount() {
        return frames.size();
    }
    
    /**
     * Returns the <i>i</i>th frame.
     * 
     * @param i The frame number--frame numbering starts with 0.
     * @return The <i>i</i>th frame. The returned frame is mutable; modifying
     * its data permanently alters the acoustic qualities of this clip.
     */
    public Frame getFrame(int i) {
        return frames.get(i);
    }
    
    /**
     * Returns the audio data in this clip as an AudioInputStream.
     * @return
     */
    public AudioInputStream getAudio() {
        InputStream audioData = new InputStream() {

            /**
             * Next frame to decode for playback.
             */
            int nextFrame = 0;
            
            /**
             * A data structure that holds all the current frames of floating point samples
             * and performs the overlap-and-combine operation for us.
             */
            OverlapBuffer overlapBuffer = new OverlapBuffer(frameSize, overlap);
            
            int currentSample;
            
            boolean currentByteHigh = true;
            
            int emptyFrameCount = 0;

            @Override
            public int available() throws IOException {
                return Integer.MAX_VALUE;
            }
            
            @Override
            public int read() throws IOException {
                if (overlapBuffer.needsNewFrame()) {
                    if (nextFrame < frames.size()) {
                        Frame f = frames.get(nextFrame++);
                        overlapBuffer.addFrame(f.asTimeData());
                    } else {
                        overlapBuffer.addEmptyFrame();
                        emptyFrameCount++;
                    }
                }
                
                if (emptyFrameCount >= overlap) {
                    return -1;
                } else if (currentByteHigh) {
                    currentSample = (int) (overlapBuffer.next() * spectralScale);
                    currentByteHigh = false;
                    return (currentSample >> 8) & 0xff;
                } else {
                    currentByteHigh = true;
                    return currentSample & 0xff;
                }
                
            }
            
        };
        int length = getFrameCount() * getFrameTimeSamples() * (AUDIO_FORMAT.getSampleSizeInBits() / 8) / overlap;
        return new AudioInputStream(audioData, AUDIO_FORMAT, length);
    }
}
