package org.apache.batik.apps.rasterizer;
import java.io.File;
import java.util.Map;
import java.util.List;
import org.apache.batik.transcoder.Transcoder;
public interface SVGConverterController {
    boolean proceedWithComputedTask(Transcoder transcoder,
                                           Map hints,
                                           List sources,
                                           List dest);
    boolean proceedWithSourceTranscoding(SVGConverterSource source,
                                                File dest);
    boolean proceedOnSourceTranscodingFailure(SVGConverterSource source,
                                                     File dest,
                                                     String errorCode);
    void onSourceTranscodingSuccess(SVGConverterSource source,
                                           File dest);
}
