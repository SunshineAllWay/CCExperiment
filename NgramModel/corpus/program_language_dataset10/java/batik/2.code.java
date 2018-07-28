package org.apache.tools.ant.taskdefs.optional;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.apps.rasterizer.SVGConverterController;
import org.apache.batik.apps.rasterizer.SVGConverterSource;
import org.apache.tools.ant.Task;
import java.io.File;
import java.util.Map;
import java.util.List;
public class RasterizerTaskSVGConverterController implements SVGConverterController {
    protected Task executingTask = null;
    protected RasterizerTaskSVGConverterController() {
    }
    public RasterizerTaskSVGConverterController(Task task) {
        executingTask = task;
    }
    public boolean proceedWithComputedTask(Transcoder transcoder,
                                           Map hints,
                                           List sources,
                                           List dest){
        return true;
    }
    public boolean proceedWithSourceTranscoding(SVGConverterSource source,
                                                File dest) {
        return true;
    }
    public boolean proceedOnSourceTranscodingFailure(SVGConverterSource source,
                                                     File dest,
                                                     String errorCode){
        if(executingTask != null) {
            executingTask.log("Unable to rasterize image '"
                + source.getName() + "' to '"
                + dest.getAbsolutePath() + "': " + errorCode);
        }
        return true;
    }
    public void onSourceTranscodingSuccess(SVGConverterSource source,
                                           File dest){
    }
}
