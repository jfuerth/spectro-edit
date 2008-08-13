/*
 * Created on Jul 17, 2008
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
