package org.apache.batik.apps.rasterizer;
import java.io.File;
import java.util.Map;
import java.util.List;
import org.apache.batik.transcoder.Transcoder;
public class DefaultSVGConverterController implements SVGConverterController {
    public boolean proceedWithComputedTask(Transcoder transcoder,
                                           Map hints,
                                           List sources,
                                           List dest){
        return true;
    }
    public boolean proceedWithSourceTranscoding(SVGConverterSource source,
                                                File dest) {
        System.out.println("About to transcoder source of type: " + source.getClass().getName());
        return true;
    }
    public boolean proceedOnSourceTranscodingFailure(SVGConverterSource source,
                                                     File dest,
                                                     String errorCode){
        return true;
    }
    public void onSourceTranscodingSuccess(SVGConverterSource source,
                                           File dest){
    }
}