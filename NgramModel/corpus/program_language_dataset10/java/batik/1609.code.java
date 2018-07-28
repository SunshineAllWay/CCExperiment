package org.apache.batik.test.svg;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
public class SVGAlternateStyleSheetRenderingAccuracyTest
    extends ParametrizedRenderingAccuracyTest {
    public ImageTranscoder getTestImageTranscoder(){
        ImageTranscoder t = super.getTestImageTranscoder();
        t.addTranscodingHint(PNGTranscoder.KEY_ALTERNATE_STYLESHEET,
                             parameter);
        return t;
    }
}
