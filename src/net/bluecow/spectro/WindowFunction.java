/*
 * Created on Jul 25, 2008
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.spectro;

/**
 * WindowFunction represents an algorithm for shaping an array of
 * data by scaling each of its values in a particular way.
 * <p>
 * Once a WindowFunction instance has been created, all data arrays
 * given to it must be the same length as each other.
 */
public interface WindowFunction {

    /**
     * Shapes the data in the given array in place, according to the
     * rules of this window function.
     * 
     * @param data The data to scale. The values in this array will be
     * modified.
     * @throws IllegalArgumentException if the array length differs from
     * the expected array length (length may be declared in the implementation's
     * constructor, or inferred from the first array passed to this instance.
     */
    void applyWindow(double[] data);
}
