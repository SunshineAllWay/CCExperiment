package org.apache.batik.svggen;
import java.awt.Color;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Map;
import org.apache.batik.ext.awt.g2d.GraphicContext;
public class SVGColor extends AbstractSVGConverter{
    public static final Color aqua = Color.cyan;
    public static final Color black = Color.black;
    public static final Color blue = Color.blue;
    public static final Color fuchsia = Color.magenta;
    public static final Color gray = Color.gray;
    public static final Color green = new Color(0x00, 0x80, 0x00); 
    public static final Color lime = Color.green;
    public static final Color maroon = new Color(0x80, 0x00, 0x00);
    public static final Color navy = new Color(0x00, 0x00, 0x80);
    public static final Color olive = new Color(0x80, 0x80, 0x00);
    public static final Color purple = new Color(0x80, 0x00, 0x80);
    public static final Color red = Color.red;
    public static final Color silver = new Color(0xc0, 0xc0, 0xc0);
    public static final Color teal = new Color(0x00, 0x80, 0x80);
    public static final Color white = Color.white;
    public static final Color yellow = Color.yellow;
    private static Map colorMap = new HashMap();
    static {
        colorMap.put(black, "black");
        colorMap.put(silver, "silver");
        colorMap.put(gray, "gray");
        colorMap.put(white, "white");
        colorMap.put(maroon, "maroon");
        colorMap.put(red, "red");
        colorMap.put(purple, "purple");
        colorMap.put(fuchsia, "fuchsia");
        colorMap.put(green, "green");
        colorMap.put(lime, "lime");
        colorMap.put(olive, "olive");
        colorMap.put(yellow, "yellow");
        colorMap.put(navy, "navy");
        colorMap.put(blue, "blue");
        colorMap.put(teal, "teal");
        colorMap.put(aqua, "aqua");
    }
    public SVGColor(SVGGeneratorContext generatorContext) {
        super(generatorContext);
    }
    public SVGDescriptor toSVG(GraphicContext gc) {
        Paint paint = gc.getPaint();
        return toSVG((Color)paint, generatorContext);
    }
    public static SVGPaintDescriptor toSVG(Color color, SVGGeneratorContext gc) {
        String cssColor = (String)colorMap.get(color);
        if (cssColor==null) {
            StringBuffer cssColorBuffer = new StringBuffer(RGB_PREFIX);
            cssColorBuffer.append(color.getRed());
            cssColorBuffer.append(COMMA);
            cssColorBuffer.append(color.getGreen());
            cssColorBuffer.append(COMMA);
            cssColorBuffer.append(color.getBlue());
            cssColorBuffer.append(RGB_SUFFIX);
            cssColor = cssColorBuffer.toString();
        }
        float alpha = color.getAlpha()/255f;
        String alphaString = gc.doubleString(alpha);
        return new SVGPaintDescriptor(cssColor, alphaString);
    }
}
