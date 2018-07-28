package org.apache.tools.ant.types.optional.image;
import javax.media.jai.PlanarImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
public class Text extends ImageOperation implements DrawOperation {
    private static final int DEFAULT_POINT = 10;
    private String strText = "";
    private String font = "Arial";
    private int point = DEFAULT_POINT;
    private boolean bold = false;
    private boolean italic = false;
    private String color = "black";
    public void setString(String str) {
        strText = str;
    }
    public void setFont(String f) {
        font = f;
    }
    public void setPoint(String p) {
        point = Integer.parseInt(p);
    }
    public void setColor(String c) {
        color = c;
    }
    public void setBold(boolean state) {
        bold = state;
    }
    public void setItalic(boolean state) {
        italic = state;
    }
    public PlanarImage executeDrawOperation() {
        log("\tCreating Text \"" + strText + "\"");
        Color couloir = ColorMapper.getColorByName(color);
        int width = 1;
        int height = 1;
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        Graphics2D graphics = (Graphics2D) bi.getGraphics();
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        Font f = new Font(font, Font.PLAIN, point);
        FontMetrics fmetrics = graphics.getFontMetrics(f);
        height = fmetrics.getMaxAscent() + fmetrics.getMaxDescent();
        width = fmetrics.stringWidth(strText);
        bi = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        graphics = (Graphics2D) bi.getGraphics();
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(
            RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setFont(f);
        graphics.setColor(couloir);
        graphics.drawString(strText, 0, height - fmetrics.getMaxDescent());
        PlanarImage image = PlanarImage.wrapRenderedImage(bi);
        return image;
    }
}
