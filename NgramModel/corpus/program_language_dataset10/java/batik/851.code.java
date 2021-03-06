package org.apache.batik.ext.awt.image.renderable;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.GraphicsUtil;
import org.apache.batik.ext.awt.image.Light;
import org.apache.batik.ext.awt.image.PadMode;
import org.apache.batik.ext.awt.image.rendered.AffineRed;
import org.apache.batik.ext.awt.image.rendered.BumpMap;
import org.apache.batik.ext.awt.image.rendered.CachableRed;
import org.apache.batik.ext.awt.image.rendered.PadRed;
import org.apache.batik.ext.awt.image.rendered.SpecularLightingRed;
public class SpecularLightingRable8Bit
    extends AbstractColorInterpolationRable
    implements SpecularLightingRable {
    private double surfaceScale;
    private double ks;
    private double specularExponent;
    private Light light;
    private Rectangle2D litRegion;
    private float [] kernelUnitLength = null;
    public SpecularLightingRable8Bit(Filter src,
                                     Rectangle2D litRegion,
                                     Light light,
                                     double ks,
                                     double specularExponent,
                                     double surfaceScale,
                                     double [] kernelUnitLength) {
        super(src, null);
        setLight(light);
        setKs(ks);
        setSpecularExponent(specularExponent);
        setSurfaceScale(surfaceScale);
        setLitRegion(litRegion);
        setKernelUnitLength(kernelUnitLength);
    }
    public Filter getSource(){
        return (Filter)getSources().get(0);
    }
    public void setSource(Filter src){
        init(src, null);
    }
    public Rectangle2D getBounds2D(){
        return (Rectangle2D)(litRegion.clone());
    }
    public Rectangle2D getLitRegion(){
        return getBounds2D();
    }
    public void setLitRegion(Rectangle2D litRegion){
        touch();
        this.litRegion = litRegion;
    }
    public Light getLight(){
        return light;
    }
    public void setLight(Light light){
        touch();
        this.light = light;
    }
    public double getSurfaceScale(){
        return surfaceScale;
    }
    public void setSurfaceScale(double surfaceScale){
        touch();
        this.surfaceScale = surfaceScale;
    }
    public double getKs(){
        return ks;
    }
    public void setKs(double ks){
        touch();
        this.ks = ks;
    }
    public double getSpecularExponent(){
        return specularExponent;
    }
    public void setSpecularExponent(double specularExponent){
        touch();
        this.specularExponent = specularExponent;
    }
    public double [] getKernelUnitLength() {
        if (kernelUnitLength == null)
            return null;
        double [] ret = new double[2];
        ret[0] = kernelUnitLength[0];
        ret[1] = kernelUnitLength[1];
        return ret;
    }
    public void setKernelUnitLength(double [] kernelUnitLength) {
        touch();
        if (kernelUnitLength == null) {
            this.kernelUnitLength = null;
            return;
        }
        if (this.kernelUnitLength == null)
            this.kernelUnitLength = new float[2];
        this.kernelUnitLength[0] = (float)kernelUnitLength[0];
        this.kernelUnitLength[1] = (float)kernelUnitLength[1];
    }
    public RenderedImage createRendering(RenderContext rc){
        Shape aoi = rc.getAreaOfInterest();
        if (aoi == null)
            aoi = getBounds2D();
        Rectangle2D aoiR = aoi.getBounds2D();
        Rectangle2D.intersect(aoiR, getBounds2D(), aoiR);
        AffineTransform at = rc.getTransform();
        Rectangle devRect = at.createTransformedShape(aoiR).getBounds();
        if(devRect.width == 0 || devRect.height == 0){
            return null;
        }
        double sx = at.getScaleX();
        double sy = at.getScaleY();
        double shx = at.getShearX();
        double shy = at.getShearY();
        double tx = at.getTranslateX();
        double ty = at.getTranslateY();
        double scaleX = Math.sqrt(sx*sx + shy*shy);
        double scaleY = Math.sqrt(sy*sy + shx*shx);
        if(scaleX == 0 || scaleY == 0){
            return null;
        }
        if (kernelUnitLength != null) {
            if (scaleX >= 1/kernelUnitLength[0])
                scaleX = 1/kernelUnitLength[0];
            if (scaleY >= 1/kernelUnitLength[1])
                scaleY = 1/kernelUnitLength[1];
        }
        AffineTransform scale =
            AffineTransform.getScaleInstance(scaleX, scaleY);
        devRect = scale.createTransformedShape(aoiR).getBounds();
        aoiR.setRect(aoiR.getX()     -(2/scaleX),
                     aoiR.getY()     -(2/scaleY),
                     aoiR.getWidth() +(4/scaleX),
                     aoiR.getHeight()+(4/scaleY));
        rc = (RenderContext)rc.clone();
        rc.setAreaOfInterest(aoiR);
        rc.setTransform(scale);
        CachableRed cr;
        cr = GraphicsUtil.wrap(getSource().createRendering(rc));
        BumpMap bumpMap = new BumpMap(cr, surfaceScale, scaleX, scaleY);
        cr = new SpecularLightingRed(ks, specularExponent, light, bumpMap,
                                     devRect, 1/scaleX, 1/scaleY,
                                     isColorSpaceLinear());
        AffineTransform shearAt =
            new AffineTransform(sx/scaleX, shy/scaleX,
                                shx/scaleY, sy/scaleY,
                                tx, ty);
        if(!shearAt.isIdentity()) {
            RenderingHints rh = rc.getRenderingHints();
            Rectangle padRect = new Rectangle(devRect.x-1, devRect.y-1,
                                              devRect.width+2,
                                              devRect.height+2);
            cr = new PadRed(cr, padRect, PadMode.REPLICATE, rh);
            cr = new AffineRed(cr, shearAt, rh);
        }
        return cr;
    }
}
