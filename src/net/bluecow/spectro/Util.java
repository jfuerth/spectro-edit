/*
 * Created on Jul 17, 2008
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.spectro;

public class Util {

    /**
     * Converts the given array of numbers to an array of complex numbers
     * where all the imaginary parts are 0.  The
     * array size is [real.length][2], where each sample is complex; array[n][0] is the
     * real part, array[n][1] is the imaginary part of sample n.
     */
    public static double[][] realToComplex(double[] real) {
        double[][] complex = new double[real.length][2];
        for (int i = 0; i < real.length; i++) {
            complex[i][0] = real[i];
        }
        return complex;
    }
}
