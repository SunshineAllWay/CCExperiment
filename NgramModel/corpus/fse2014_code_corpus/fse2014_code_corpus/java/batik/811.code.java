package org.apache.batik.ext.awt.image.renderable;
import java.awt.color.ColorSpace;
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.Map;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
public abstract class AbstractColorInterpolationRable extends AbstractRable {
    protected boolean csLinear = true;
    protected AbstractColorInterpolationRable() {
        super();
    }
    protected AbstractColorInterpolationRable(Filter src) {
        super(src);
    }
    protected AbstractColorInterpolationRable(Filter src, Map props) {
        super(src, props);
    }
    protected AbstractColorInterpolationRable(List srcs) {
        super(srcs);
    }
    protected AbstractColorInterpolationRable(List srcs, Map props) {
        super(srcs, props);
    }
    public boolean isColorSpaceLinear() { return csLinear; }
    public void setColorSpaceLinear(boolean csLinear) {
        touch();
        this.csLinear = csLinear;
    }
    public ColorSpace getOperationColorSpace() {
        if (csLinear)
            return ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);
        else
            return ColorSpace.getInstance(ColorSpace.CS_sRGB);
    }
    protected CachableRed convertSourceCS(CachableRed cr) {
        if (csLinear)
            return GraphicsUtil.convertToLsRGB(cr);
        else
            return GraphicsUtil.convertTosRGB(cr);
    }
    protected CachableRed convertSourceCS(RenderedImage ri) {
        return convertSourceCS(GraphicsUtil.wrap(ri));
    }
}
