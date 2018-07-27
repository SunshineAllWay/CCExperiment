package org.apache.batik.ext.awt.image.renderable;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.SVGComposite;
public class FilterChainRable8Bit extends AbstractRable
    implements FilterChainRable, PaintRable {
    private int filterResolutionX;
    private int filterResolutionY;
    private Filter chainSource;
    private FilterResRable filterRes;
    private PadRable crop;
    private Rectangle2D filterRegion;
    public FilterChainRable8Bit(Filter source, Rectangle2D filterRegion){
        if(source == null){
            throw new IllegalArgumentException();
        }
        if(filterRegion == null){
            throw new IllegalArgumentException();
        }
        Rectangle2D padRect = (Rectangle2D)filterRegion.clone();
        crop = new PadRable8Bit(source, padRect, 
                                    PadMode.ZERO_PAD);
        this.chainSource = source;
        this.filterRegion = filterRegion;
        init(crop); 
    }
    public int getFilterResolutionX(){
        return filterResolutionX;
    }
    public void setFilterResolutionX(int filterResolutionX){
        touch();
        this.filterResolutionX = filterResolutionX;
        setupFilterRes();
    }
    public int getFilterResolutionY(){
        return filterResolutionY;
    }
    public void setFilterResolutionY(int filterResolutionY){
        touch();
        this.filterResolutionY = filterResolutionY;
        setupFilterRes();
    }
    private void setupFilterRes(){
        if(filterResolutionX >=0){
            if(filterRes == null){
                filterRes = new FilterResRable8Bit();
                filterRes.setSource(chainSource);
            }
            filterRes.setFilterResolutionX(filterResolutionX);
            filterRes.setFilterResolutionY(filterResolutionY);
        }
        else{
            filterRes = null;
        }
        if(filterRes != null){
            crop.setSource(filterRes);
        }
        else{
            crop.setSource(chainSource);
        }
    }
    public void setFilterRegion(Rectangle2D filterRegion){
        if(filterRegion == null){
            throw new IllegalArgumentException();
        }
        touch();
        this.filterRegion = filterRegion;
     }
    public Rectangle2D getFilterRegion(){
        return filterRegion;
    }
    public Filter getSource() {
        return crop;
    }
    public void setSource(Filter chainSource) {
        if(chainSource == null){
            throw new IllegalArgumentException("Null Source for Filter Chain");
        }
        touch();
        this.chainSource = chainSource;
        if(filterRes == null){
            crop.setSource(chainSource);
        }
        else{
            filterRes.setSource(chainSource);
        }
    }
    public Rectangle2D getBounds2D(){
        return (Rectangle2D)filterRegion.clone();
    }
    public boolean paintRable(Graphics2D g2d) {
        Composite c = g2d.getComposite();
        if (!SVGComposite.OVER.equals(c))
            return false;
        GraphicsUtil.drawImage(g2d, getSource());
        return true;
    }
    public RenderedImage createRendering(RenderContext context){
        return crop.createRendering(context);
    }
}
