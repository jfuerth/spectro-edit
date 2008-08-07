/*
 * Created on Aug 6, 2008
 *
 * This code belongs to SQL Power Group Inc.
 */
package net.bluecow.spectro;

import java.util.Properties;

public class Version {

    /**
     * The version number of Spectro-Edit in this classloader context.
     */
    public static final String VERSION;
    static {
        try {
            Properties props = new Properties();
            props.load(Version.class.getResourceAsStream("version.properties"));
            VERSION = props.getProperty("net.bluecow.spectro.VERSION");
        } catch (Exception e) {
            throw new RuntimeException("Failed to read version from classpath resource", e);
        }
    }
}
