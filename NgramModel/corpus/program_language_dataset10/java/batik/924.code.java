package org.apache.batik.extension.svg;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.RenderContext;
import org.apache.batik.ext.awt.image.LinearTransfer;
import org.apache.batik.ext.awt.image.TransferFunction;
import org.apache.batik.ext.awt.image.renderable.AbstractColorInterpolationRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.rendered.ComponentTransferRed;
public class BatikHistogramNormalizationFilter8Bit
    extends      AbstractColorInterpolationRable
    implements   BatikHistogramNormalizationFilter {
    private float trim = 0.01f;
    public void setSource(Filter src){
        init(src, null);
    }
    public Filter getSource(){
        return (Filter)getSources().get(0);
    }
    public float getTrim() {
        return trim;
    }
    public void setTrim(float trim) {
        this.trim = trim;
        touch();
    }
    public BatikHistogramNormalizationFilter8Bit(Filter src, float trim) {
        setSource(src);
        setTrim(trim);
    }
    protected int [] histo = null;
    protected float slope, intercept;
    public void computeHistogram(RenderContext rc) {
        if (histo != null)
            return;
        Filter src = getSource();
        float scale  = 100.0f/src.getWidth();
        float yscale = 100.0f/src.getHeight();
        if (scale > yscale) scale=yscale;
        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        rc = new RenderContext(at, rc.getRenderingHints());
        RenderedImage histRI = getSource().createRendering(rc);
        histo = new HistogramRed(convertSourceCS(histRI)).getHistogram();
        int t = (int)(histRI.getWidth()*histRI.getHeight()*trim+0.5);
        int c, i;
        for (c=0, i=0; i<255; i++) {
            c+=histo[i];
            if (c>=t) break;
        }
        int low = i;
        for (c=0, i=255; i>0; i--) {
            c+=histo[i];
            if (c>=t) break;
        }
        int hi = i;
        slope = 255f/(hi-low);
        intercept = (slope*-low)/255f;
    }
    public RenderedImage createRendering(RenderContext rc) {
        RenderedImage srcRI = getSource().createRendering(rc);
        if(srcRI == null)
            return null;
        computeHistogram(rc);
        SampleModel sm = srcRI.getSampleModel();
        int bands = sm.getNumBands();
        TransferFunction [] tfs = new TransferFunction[bands];
        TransferFunction    tf  = new LinearTransfer(slope, intercept);
        for (int i=0; i<tfs.length; i++)
            tfs[i] = tf;
        return new ComponentTransferRed(convertSourceCS(srcRI), tfs, null);
    }
}
