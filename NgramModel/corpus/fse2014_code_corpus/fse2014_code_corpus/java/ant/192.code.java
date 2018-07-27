package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.zip.ZipOutputStream;
public class Ear extends Jar {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private File deploymentDescriptor;
    private boolean descriptorAdded;
    private static final String XML_DESCRIPTOR_PATH = "META-INF/application.xml";
    public Ear() {
        super();
        archiveType = "ear";
        emptyBehavior = "create";
    }
    public void setEarfile(File earFile) {
        setDestFile(earFile);
    }
    public void setAppxml(File descr) {
        deploymentDescriptor = descr;
        if (!deploymentDescriptor.exists()) {
            throw new BuildException("Deployment descriptor: "
                                     + deploymentDescriptor
                                     + " does not exist.");
        }
        ZipFileSet fs = new ZipFileSet();
        fs.setFile(deploymentDescriptor);
        fs.setFullpath(XML_DESCRIPTOR_PATH);
        super.addFileset(fs);
    }
    public void addArchives(ZipFileSet fs) {
        fs.setPrefix("/");
        super.addFileset(fs);
    }
    protected void initZipOutputStream(ZipOutputStream zOut)
        throws IOException, BuildException {
        if (deploymentDescriptor == null && !isInUpdateMode()) {
            throw new BuildException("appxml attribute is required", getLocation());
        }
        super.initZipOutputStream(zOut);
    }
    protected void zipFile(File file, ZipOutputStream zOut, String vPath,
                           int mode)
        throws IOException {
        if (XML_DESCRIPTOR_PATH.equalsIgnoreCase(vPath))  {
            if (deploymentDescriptor == null
                || !FILE_UTILS.fileNameEquals(deploymentDescriptor, file)
                || descriptorAdded) {
                logWhenWriting("Warning: selected " + archiveType
                               + " files include a " + XML_DESCRIPTOR_PATH
                               + " which will"
                               + " be ignored (please use appxml attribute to "
                               + archiveType + " task)",
                               Project.MSG_WARN);
            } else {
                super.zipFile(file, zOut, vPath, mode);
                descriptorAdded = true;
            }
        } else {
            super.zipFile(file, zOut, vPath, mode);
        }
    }
    protected void cleanUp() {
        descriptorAdded = false;
        super.cleanUp();
    }
}
