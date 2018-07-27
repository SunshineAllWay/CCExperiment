package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.IsSigned;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.ResourceUtils;
public class SignJar extends AbstractJarSignerTask {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    protected String sigfile;
    protected File signedjar;
    protected boolean internalsf;
    protected boolean sectionsonly;
    private boolean preserveLastModified;
    protected boolean lazy;
    protected File destDir;
    private FileNameMapper mapper;
    protected String tsaurl;
    protected String tsacert;
    private boolean force = false;
    public static final String ERROR_TODIR_AND_SIGNEDJAR
            = "'destdir' and 'signedjar' cannot both be set";
    public static final String ERROR_TOO_MANY_MAPPERS = "Too many mappers";
    public static final String ERROR_SIGNEDJAR_AND_PATHS
        = "You cannot specify the signed JAR when using paths or filesets";
    public static final String ERROR_BAD_MAP = "Cannot map source file to anything sensible: ";
    public static final String ERROR_MAPPER_WITHOUT_DEST
        = "The destDir attribute is required if a mapper is set";
    public static final String ERROR_NO_ALIAS = "alias attribute must be set";
    public static final String ERROR_NO_STOREPASS = "storepass attribute must be set";
    public void setSigfile(final String sigfile) {
        this.sigfile = sigfile;
    }
    public void setSignedjar(final File signedjar) {
        this.signedjar = signedjar;
    }
    public void setInternalsf(final boolean internalsf) {
        this.internalsf = internalsf;
    }
    public void setSectionsonly(final boolean sectionsonly) {
        this.sectionsonly = sectionsonly;
    }
    public void setLazy(final boolean lazy) {
        this.lazy = lazy;
    }
    public void setDestDir(File destDir) {
        this.destDir = destDir;
    }
    public void add(FileNameMapper newMapper) {
        if (mapper != null) {
            throw new BuildException(ERROR_TOO_MANY_MAPPERS);
        }
        mapper = newMapper;
    }
    public FileNameMapper getMapper() {
        return mapper;
    }
    public String getTsaurl() {
        return tsaurl;
    }
    public void setTsaurl(String tsaurl) {
        this.tsaurl = tsaurl;
    }
    public String getTsacert() {
        return tsacert;
    }
    public void setTsacert(String tsacert) {
        this.tsacert = tsacert;
    }
    public void setForce(boolean b) {
        force = b;
    }
    public boolean isForce() {
        return force;
    }
    public void execute() throws BuildException {
        final boolean hasJar = jar != null;
        final boolean hasSignedJar = signedjar != null;
        final boolean hasDestDir = destDir != null;
        final boolean hasMapper = mapper != null;
        if (!hasJar && !hasResources()) {
            throw new BuildException(ERROR_NO_SOURCE);
        }
        if (null == alias) {
            throw new BuildException(ERROR_NO_ALIAS);
        }
        if (null == storepass) {
            throw new BuildException(ERROR_NO_STOREPASS);
        }
        if (hasDestDir && hasSignedJar) {
            throw new BuildException(ERROR_TODIR_AND_SIGNEDJAR);
        }
        if (hasResources() && hasSignedJar) {
            throw new BuildException(ERROR_SIGNEDJAR_AND_PATHS);
        }
        if (!hasDestDir && hasMapper) {
            throw new BuildException(ERROR_MAPPER_WITHOUT_DEST);
        }
        beginExecution();
        try {
            if (hasJar && hasSignedJar) {
                signOneJar(jar, signedjar);
                return;
            }
            Path sources = createUnifiedSourcePath();
            FileNameMapper destMapper;
            if (hasMapper) {
                destMapper = mapper;
            } else {
                destMapper = new IdentityMapper();
            }
            Iterator iter = sources.iterator();
            while (iter.hasNext()) {
                Resource r = (Resource) iter.next();
                FileResource fr = ResourceUtils
                    .asFileResource((FileProvider) r.as(FileProvider.class));
                File toDir = hasDestDir ? destDir : fr.getBaseDir();
                String[] destFilenames = destMapper.mapFileName(fr.getName());
                if (destFilenames == null || destFilenames.length != 1) {
                    throw new BuildException(ERROR_BAD_MAP + fr.getFile());
                }
                File destFile = new File(toDir, destFilenames[0]);
                signOneJar(fr.getFile(), destFile);
            }
        } finally {
            endExecution();
        }
    }
    private void signOneJar(File jarSource, File jarTarget)
        throws BuildException {
        File targetFile = jarTarget;
        if (targetFile == null) {
            targetFile = jarSource;
        }
        if (isUpToDate(jarSource, targetFile)) {
            return;
        }
        long lastModified = jarSource.lastModified();
        final ExecTask cmd = createJarSigner();
        setCommonOptions(cmd);
        bindToKeystore(cmd);
        if (null != sigfile) {
            addValue(cmd, "-sigfile");
            String value = this.sigfile;
            addValue(cmd, value);
        }
        try {
            if (!FILE_UTILS.areSame(jarSource, targetFile)) {
                addValue(cmd, "-signedjar");
                addValue(cmd, targetFile.getPath());
            }
        } catch (IOException ioex) {
            throw new BuildException(ioex);
        }
        if (internalsf) {
            addValue(cmd, "-internalsf");
        }
        if (sectionsonly) {
            addValue(cmd, "-sectionsonly");
        }
        addTimestampAuthorityCommands(cmd);
        addValue(cmd, jarSource.getPath());
        addValue(cmd, alias);
        log("Signing JAR: "
            + jarSource.getAbsolutePath()
            + " to "
            + targetFile.getAbsolutePath()
            + " as " + alias);
        cmd.execute();
        if (preserveLastModified) {
            FILE_UTILS.setFileLastModified(targetFile, lastModified);
        }
    }
    private void addTimestampAuthorityCommands(final ExecTask cmd) {
        if (tsaurl != null) {
            addValue(cmd, "-tsa");
            addValue(cmd, tsaurl);
        }
        if (tsacert != null) {
            addValue(cmd, "-tsacert");
            addValue(cmd, tsacert);
        }
    }
    protected boolean isUpToDate(File jarFile, File signedjarFile) {
        if (isForce() || null == jarFile || !jarFile.exists()) {
            return false;
        }
        File destFile = signedjarFile;
        if (destFile == null) {
            destFile = jarFile;
        }
        if (jarFile.equals(destFile)) {
            if (lazy) {
                return isSigned(jarFile);
            }
            return false;
        }
        return FILE_UTILS.isUpToDate(jarFile, destFile);
    }
    protected boolean isSigned(File file) {
        try {
            return IsSigned.isSigned(file, sigfile == null ? alias : sigfile);
        } catch (IOException e) {
            log(e.toString(), Project.MSG_VERBOSE);
            return false;
        }
    }
    public void setPreserveLastModified(boolean preserveLastModified) {
        this.preserveLastModified = preserveLastModified;
    }
}
