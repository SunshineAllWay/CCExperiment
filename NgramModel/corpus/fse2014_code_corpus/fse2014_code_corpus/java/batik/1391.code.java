package org.apache.batik.util;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Toolkit;
public abstract class Platform {
    public static boolean isOSX =
        System.getProperty("os.name").equals("Mac OS X");
    public static int getScreenResolution() {
        if (GraphicsEnvironment.isHeadless()) {
            return 96;
        } else {
            return Toolkit.getDefaultToolkit().getScreenResolution();
        }
    }
}
