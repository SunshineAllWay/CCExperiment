package task;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
abstract public class BaseTask extends Task {
    private final static FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private File inFile;
    private File outFile;
    public void setInFile(File inFile) {
        this.inFile = inFile;
    }
    protected File getInFile() {
        return inFile;
    }
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }
    protected File getOutFile() {
        return outFile;
    }
    public void execute() {
        assertAttribute(inFile, "inFile");
        assertAttribute(outFile, "outFile");
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new BufferedInputStream(
                new FileInputStream(getInFile()));
            outputStream = new FileOutputStream(getOutFile());
            doit(inputStream, outputStream);
        } catch (Exception ex) {
            throw new BuildException(ex);
        } finally {
            FILE_UTILS.close(inputStream);
            FILE_UTILS.close(outputStream);
        }
    }
    abstract protected void doit(
        InputStream is, OutputStream os) throws Exception;
    private void assertAttribute(File file, String attributeName) {
        if (file == null) {
            throw new BuildException("Required attribute " + attributeName
                                     + " not set");
        }
    }
}
