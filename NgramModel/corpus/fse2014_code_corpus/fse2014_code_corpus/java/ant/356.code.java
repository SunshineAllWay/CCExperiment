package org.apache.tools.ant.taskdefs.optional;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.taskdefs.optional.native2ascii.Native2AsciiAdapter;
import org.apache.tools.ant.taskdefs.optional.native2ascii.Native2AsciiAdapterFactory;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.SourceFileScanner;
import org.apache.tools.ant.util.facade.FacadeTaskHelper;
import org.apache.tools.ant.util.facade.ImplementationSpecificArgument;
public class Native2Ascii extends MatchingTask {
    private boolean reverse = false;  
    private String encoding = null;   
    private File srcDir = null;       
    private File destDir = null;      
    private String extension = null;  
    private Mapper mapper;
    private FacadeTaskHelper facade = null;
    private Native2AsciiAdapter nestedAdapter = null;
    public Native2Ascii() {
        facade = new FacadeTaskHelper(Native2AsciiAdapterFactory.getDefault());
    }
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
    public boolean getReverse() {
        return reverse;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setSrc(File srcDir) {
        this.srcDir = srcDir;
    }
    public void setDest(File destDir) {
        this.destDir = destDir;
    }
    public void setExt(String ext) {
        this.extension = ext;
    }
    public void setImplementation(String impl) {
        if ("default".equals(impl)) {
            facade.setImplementation(Native2AsciiAdapterFactory.getDefault());
        } else {
            facade.setImplementation(impl);
        }
    }
    public Mapper createMapper() throws BuildException {
        if (mapper != null) {
            throw new BuildException("Cannot define more than one mapper",
                                     getLocation());
        }
        mapper = new Mapper(getProject());
        return mapper;
    }
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }
    public ImplementationSpecificArgument createArg() {
        ImplementationSpecificArgument arg =
            new ImplementationSpecificArgument();
        facade.addImplementationArgument(arg);
        return arg;
    }
    public Path createImplementationClasspath() {
        return facade.getImplementationClasspath(getProject());
    }
    public void add(Native2AsciiAdapter adapter) {
        if (nestedAdapter != null) {
            throw new BuildException("Can't have more than one native2ascii"
                                     + " adapter");
        }
        nestedAdapter = adapter;
    }
    public void execute() throws BuildException {
        DirectoryScanner scanner = null; 
        String[] files;                  
        if (srcDir == null) {
            srcDir = getProject().resolveFile(".");
        }
        if (destDir == null) {
            throw new BuildException("The dest attribute must be set.");
        }
        if (srcDir.equals(destDir) && extension == null && mapper == null) {
            throw new BuildException("The ext attribute or a mapper must be set if"
                                     + " src and dest dirs are the same.");
        }
        FileNameMapper m = null;
        if (mapper == null) {
            if (extension == null) {
                m = new IdentityMapper();
            } else {
                m = new ExtMapper();
            }
        } else {
            m = mapper.getImplementation();
        }
        scanner = getDirectoryScanner(srcDir);
        files = scanner.getIncludedFiles();
        SourceFileScanner sfs = new SourceFileScanner(this);
        files = sfs.restrict(files, srcDir, destDir, m);
        int count = files.length;
        if (count == 0) {
            return;
        }
        String message = "Converting " + count + " file"
            + (count != 1 ? "s" : "") + " from ";
        log(message + srcDir + " to " + destDir);
        for (int i = 0; i < files.length; i++) {
            convert(files[i], m.mapFileName(files[i])[0]);
        }
    }
    private void convert(String srcName, String destName)
        throws BuildException {
        File srcFile;                         
        File destFile;                        
        srcFile = new File(srcDir, srcName);
        destFile = new File(destDir, destName);
        if (srcFile.equals(destFile)) {
            throw new BuildException("file " + srcFile
                                     + " would overwrite its self");
        }
        String parentName = destFile.getParent();
        if (parentName != null) {
            File parentFile = new File(parentName);
            if ((!parentFile.exists()) && (!parentFile.mkdirs())) {
                throw new BuildException("cannot create parent directory "
                                         + parentName);
            }
        }
        log("converting " + srcName, Project.MSG_VERBOSE);
        Native2AsciiAdapter ad =
            nestedAdapter != null ? nestedAdapter :
            Native2AsciiAdapterFactory.getAdapter(facade.getImplementation(),
                                                  this,
                                                  createImplementationClasspath());
        if (!ad.convert(this, srcFile, destFile)) {
            throw new BuildException("conversion failed");
        }
    }
    public String[] getCurrentArgs() {
        return facade.getArgs();
    }
    private class ExtMapper implements FileNameMapper {
        public void setFrom(String s) {
        }
        public void setTo(String s) {
        }
        public String[] mapFileName(String fileName) {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot >= 0) {
                return new String[] {fileName.substring(0, lastDot)
                                         + extension};
            } else {
                return new String[] {fileName + extension};
            }
        }
    }
}
