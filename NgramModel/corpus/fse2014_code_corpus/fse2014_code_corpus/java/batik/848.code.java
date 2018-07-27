package org.apache.batik.ext.awt.image.renderable;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.color.ICCColorSpaceExt;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.ProfileRed;
public class ProfileRable extends  AbstractRable{
    private ICCColorSpaceExt colorSpace;
    public ProfileRable(Filter src, ICCColorSpaceExt colorSpace){
        super(src);
        this.colorSpace = colorSpace;
    }
    public void setSource(Filter src){
        init(src, null);
    }
    public Filter getSource(){
        return (Filter)getSources().get(0);
    }
    public void setColorSpace(ICCColorSpaceExt colorSpace){
        touch();
        this.colorSpace = colorSpace;
    }
    public ICCColorSpaceExt getColorSpace(){
        return colorSpace;
    }
    public RenderedImage createRendering(RenderContext rc) {
        RenderedImage srcRI = getSource().createRendering(rc);
        if(srcRI == null)
            return null;
        CachableRed srcCR = GraphicsUtil.wrap(srcRI);
        return new ProfileRed(srcCR, colorSpace);
    }
}
