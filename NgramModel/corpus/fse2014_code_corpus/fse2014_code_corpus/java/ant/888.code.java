package task;
import java.io.InputStream;
import java.io.OutputStream;
import sun.misc.UUDecoder;
public class UUDecodeTask extends BaseTask {
    protected void doit(InputStream is, OutputStream os) throws Exception {
        new UUDecoder().decodeBuffer(is, os);
    }
}
