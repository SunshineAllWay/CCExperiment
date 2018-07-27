package org.apache.tools.ant.taskdefs.optional.image;
import com.sun.media.jai.codec.FileSeekableStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.optional.image.Draw;
import org.apache.tools.ant.types.optional.image.ImageOperation;
import org.apache.tools.ant.types.optional.image.Rotate;
import org.apache.tools.ant.types.optional.image.Scale;
import org.apache.tools.ant.types.optional.image.TransformOperation;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.IdentityMapper;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
public class Image extends MatchingTask {
    protected Vector instructions = new Vector();
    protected boolean overwrite = false;
    protected Vector filesets = new Vector();
    protected File srcDir = null;
    protected File destDir = null;
    protected String str_encoding = "JPEG";
    protected boolean garbage_collect = false;
    private boolean failonerror = true;
    private Mapper mapperElement = null;
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }
    public void setFailOnError(boolean failonerror) {
        this.failonerror = failonerror;
    }
    public void setSrcdir(File srcDir) {
        this.srcDir = srcDir;
    }
    public void setEncoding(String encoding) {
        str_encoding = encoding;
    }
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
    public void setGc(boolean gc) {
        garbage_collect = gc;
    }
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }
    public void addImageOperation(ImageOperation instr) {
        instructions.add(instr);
    }
    public void addRotate(Rotate instr) {
        instructions.add(instr);
    }
    public void addScale(Scale instr) {
        instructions.add(instr);
    }
    public void addDraw(Draw instr) {
        instructions.add(instr);
    }
    public void add(ImageOperation instr) {
        addImageOperation(instr);
    }
    public Mapper createMapper() throws BuildException {
        if (mapperElement != null) {
            throw new BuildException("Cannot define more than one mapper",
                                     getLocation());
        }
        mapperElement = new Mapper(getProject());
        return mapperElement;
    }
    public void add(FileNameMapper fileNameMapper) {
        createMapper().add(fileNameMapper);
    }
    public int processDir(final File srcDir, final String srcNames[],
                          final File dstDir, final FileNameMapper mapper) {
        int writeCount = 0;
        for (int i = 0; i < srcNames.length; ++i) {
            final String srcName = srcNames[i];
            final File srcFile = new File(srcDir, srcName).getAbsoluteFile();
            final String[] dstNames = mapper.mapFileName(srcName);
            if (dstNames == null) {
                log(srcFile + " skipped, don't know how to handle it",
                    Project.MSG_VERBOSE);
                continue;
            }
            for (int j = 0; j < dstNames.length; ++j){
                final String dstName = dstNames[j];
                final File dstFile = new File(dstDir, dstName).getAbsoluteFile();
                if (dstFile.exists()){
                    if(!overwrite
                       && srcFile.lastModified() <= dstFile.lastModified()) {
                        log(srcFile + " omitted as " + dstFile
                            + " is up to date.", Project.MSG_VERBOSE);
                        continue;
                    }
                    if (!srcFile.equals(dstFile)){
                        dstFile.delete();
                    }
                }
                processFile(srcFile, dstFile);
                ++writeCount;
            }
        }
        if (garbage_collect) {
            System.gc();
        }
        return writeCount;
    }
    public void processFile(File file) {
        processFile(file, new File(destDir == null
                                   ? srcDir : destDir, file.getName()));
    }
    public void processFile(File file, File newFile) {
        try {
            log("Processing File: " + file.getAbsolutePath());
            FileSeekableStream input = null;
            PlanarImage image = null;
            try {
                input = new FileSeekableStream(file);
                image = JAI.create("stream", input);
                for (int i = 0; i < instructions.size(); i++) {
                    Object instr = instructions.elementAt(i);
                    if (instr instanceof TransformOperation) {
                        image = ((TransformOperation) instr)
                            .executeTransformOperation(image);
                    } else {
                        log("Not a TransformOperation: " + instr);
                    }
                }
            } finally {
                FileUtils.close(input);
            }
            File dstParent = newFile.getParentFile();
            if (!dstParent.isDirectory() && !dstParent.mkdirs()){
                throw new BuildException("Failed to create parent directory "
                                         + dstParent);
            }
            if ((overwrite && newFile.exists()) && (!newFile.equals(file))) {
                newFile.delete();
            }
            FileOutputStream stream = null;
            try {
                stream = new FileOutputStream(newFile);
                JAI.create("encode", image, stream,
                           str_encoding.toUpperCase(Locale.ENGLISH),
                           null);
                stream.flush();
            } finally {
                FileUtils.close(stream);
            }
        } catch (IOException err) {
            if (!file.equals(newFile)){
                newFile.delete();
            }
            if (!failonerror) {
                log("Error processing file:  " + err);
            } else {
                throw new BuildException(err);
            }
        } catch (java.lang.RuntimeException rerr) {
            if (!file.equals(newFile)){
                newFile.delete();
            }
            if (!failonerror) {
                log("Error processing file:  " + rerr);
            } else {
                throw new BuildException(rerr);
            }
        }
    }
    public void execute() throws BuildException {
        validateAttributes();
        try {
            File dest = destDir != null ? destDir : srcDir;
            int writeCount = 0;
            final FileNameMapper mapper;
            if (mapperElement==null){
                mapper = new IdentityMapper();
            } else {
                mapper = mapperElement.getImplementation();
            }
            if (srcDir != null) {
                final DirectoryScanner ds = super.getDirectoryScanner(srcDir);
                final String[] files = ds.getIncludedFiles();
                writeCount += processDir(srcDir, files, dest, mapper);
            }
            for (int i = 0; i < filesets.size(); i++) {
                final FileSet fs = (FileSet) filesets.elementAt(i);
                final DirectoryScanner ds =
                    fs.getDirectoryScanner(getProject());
                final String[] files = ds.getIncludedFiles();
                final File fromDir = fs.getDir(getProject());
                writeCount += processDir(fromDir, files, dest, mapper);
            }
            if (writeCount>0){
                log("Processed " + writeCount +
                    (writeCount == 1 ? " image." : " images."));
            }
        } catch (Exception err) {
            err.printStackTrace();
            throw new BuildException(err.getMessage());
        }
    }
    protected void validateAttributes() throws BuildException {
        if (srcDir == null && filesets.size() == 0) {
            throw new BuildException("Specify at least one source"
                                     + "--a srcDir or a fileset.");
        }
        if (srcDir == null && destDir == null) {
            throw new BuildException("Specify the destDir, or the srcDir.");
        }
        if (str_encoding.equalsIgnoreCase("jpg")) {
            str_encoding = "JPEG";
        } else if (str_encoding.equalsIgnoreCase("tif")) {
            str_encoding = "TIFF";
        }
    }
}
