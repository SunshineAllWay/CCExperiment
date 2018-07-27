package task;
import org.apache.tools.ant.util.UUEncoder;
import java.io.InputStream;
import java.io.OutputStream;
public class UUEncodeTask extends BaseTask {
    protected void doit(InputStream is, OutputStream os) throws Exception {
        new UUEncoder(getInFile().getName()).encode(is, os);
    }
}
