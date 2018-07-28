package org.apache.tools.ant.types.optional.image;
import javax.media.jai.PlanarImage;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
public class Arc extends BasicShape implements DrawOperation {
    protected int width = 0;
    protected int height = 0;
    protected int start = 0;
    protected int stop = 0;
    protected int type = Arc2D.OPEN;
    public void setWidth(int width) {
        this.width = width;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public void setStop(int stop) {
        this.stop = stop;
    }
    public void setType(String strType) {
        if (strType.equalsIgnoreCase("open")) {
            type = Arc2D.OPEN;
        } else if (strType.equalsIgnoreCase("pie")) {
            type = Arc2D.PIE;
        } else if (strType.equalsIgnoreCase("chord")) {
            type = Arc2D.CHORD;
        }
    }
    public PlanarImage executeDrawOperation() {
        BufferedImage bi = new BufferedImage(width + (stroke_width * 2),
            height + (stroke_width * 2), BufferedImage.TYPE_4BYTE_ABGR_PRE);
        Graphics2D graphics = (Graphics2D) bi.getGraphics();
        if (!stroke.equals("transparent")) {
            BasicStroke bStroke = new BasicStroke(stroke_width);
            graphics.setColor(ColorMapper.getColorByName(stroke));
            graphics.setStroke(bStroke);
            graphics.draw(new Arc2D.Double(stroke_width, stroke_width, width,
                height, start, stop, type));
        }
        if (!fill.equals("transparent")) {
            graphics.setColor(ColorMapper.getColorByName(fill));
            graphics.fill(new Arc2D.Double(stroke_width, stroke_width,
                width, height, start, stop, type));
        }
        for (int i = 0; i < instructions.size(); i++) {
            ImageOperation instr = ((ImageOperation) instructions.elementAt(i));
            if (instr instanceof DrawOperation) {
                PlanarImage img = ((DrawOperation) instr).executeDrawOperation();
                graphics.drawImage(img.getAsBufferedImage(), null, 0, 0);
            } else if (instr instanceof TransformOperation) {
                graphics = (Graphics2D) bi.getGraphics();
                PlanarImage image = ((TransformOperation) instr)
                    .executeTransformOperation(PlanarImage.wrapRenderedImage(bi));
                bi = image.getAsBufferedImage();
            }
        }
        return PlanarImage.wrapRenderedImage(bi);
    }
}
