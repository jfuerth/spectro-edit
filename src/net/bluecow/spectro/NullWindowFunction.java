/*
 * Created on Jul 25, 2008
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.spectro;

/**
 * The trivial window function: just leaves the data alone.
 */
public class NullWindowFunction implements WindowFunction {

    /**
     * Doesn't do anything.
     */
    public void applyWindow(double[] data) {
        // noop
    }

}
