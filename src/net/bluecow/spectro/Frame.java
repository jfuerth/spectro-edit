/*
 * Created on Jul 9, 2008
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.spectro;

import java.util.Arrays;

import edu.emory.mathcs.jtransforms.dct.DoubleDCT_1D;

/**
 * A frame of audio data, represented in the frequency domain. The specific
 * frequency components of this frame are modifiable.
 */
public class Frame {

    /**
     * Array of spectral data.
     */
    private double[] data;

    private static DoubleDCT_1D dct;

    private final WindowFunction windowFunc;
    
    public Frame(double[] timeData, WindowFunction windowFunc) {
        this.windowFunc = windowFunc;
        if (dct == null) {
            // XXX this only works if all frames are same size as the first one
            dct = new DoubleDCT_1D(timeData.length);
        }

        // in place window
        windowFunc.applyWindow(timeData);

        // in place transform: timeData becomes frequency data
        dct.forward(timeData, true);

        data = new double[timeData.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = timeData[i];
        }
        
    }
    
    /**
     * Returns the length of this frame, in samples.
     * @return
     */
    public int getLength() {
        return data.length;
    }
    
    /**
     * Returns the idx'th real component of this frame's spectrum.
     */
    public double getReal(int idx) {
        return data[idx];
    }

    /**
     * Returns the idx'th imaginary component of this frame's spectrum.
     */
    public double getImag(int idx) {
        return 0.0;
    }

    /**
     * Sets the real component at idx. This method sets the new actual value,
     * although it may make sense to provide another method that scales the existing
     * value.
     * 
     * @param idx The index to modify
     * @param d The new value
     */
    public void setReal(int idx, double d) {
        data[idx] = d;
    }

    /**
     * Returns the time-domain representation of this frame. Unless the spectral
     * data of this frame has been modified, the returned array will be very
     * similar to the array given in the constructor. Even if the spectral data
     * has been modified, the length of the returned array will have the same
     * length as the original array given in the constructor.
     */
    public double[] asTimeData() {
        double[] timeData = new double[data.length];
        System.arraycopy(data, 0, timeData, 0, data.length);
        dct.inverse(timeData, true);
        windowFunc.applyWindow(timeData);
        return timeData;
    }
    
    /**
     * Quick demo to show original, transformed, and inverse transformed data.
     */
    public static void main(String[] args) {
        double[] orig = new double[] { 1,2,3,4,5,0,9,8,7,6,5,4,3,2,1,7 };
        System.out.println(Arrays.toString(orig));
        Frame f = new Frame(orig, new NullWindowFunction());
        System.out.println(Arrays.toString(f.data));
        System.out.println(Arrays.toString(f.asTimeData()));
    }

}
