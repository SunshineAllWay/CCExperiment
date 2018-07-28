package org.apache.batik.swing;
import java.awt.Image;
import java.beans.SimpleBeanInfo;
public class JSVGCanvasBeanInfo extends SimpleBeanInfo {
    protected Image iconColor16x16;
    protected Image iconMono16x16;
    protected Image iconColor32x32;
    protected Image iconMono32x32;
    public JSVGCanvasBeanInfo() {
        iconColor16x16 = loadImage("resources/batikColor16x16.gif");
        iconMono16x16 = loadImage("resources/batikMono16x16.gif");
        iconColor32x32 = loadImage("resources/batikColor32x32.gif");
        iconMono32x32 = loadImage("resources/batikMono32x32.gif");
    }
    public Image getIcon(int iconType) {
        switch(iconType) {
        case ICON_COLOR_16x16:
            return iconColor16x16;
        case ICON_MONO_16x16:
            return iconMono16x16;
        case ICON_COLOR_32x32:
            return iconColor32x32;
        case ICON_MONO_32x32:
            return iconMono32x32;
        default:
            return null;
        }
    }
}
