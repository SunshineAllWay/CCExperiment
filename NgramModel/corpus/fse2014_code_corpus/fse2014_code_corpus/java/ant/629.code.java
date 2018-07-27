package org.apache.tools.ant.types.mappers;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileNameMapper;
public class CutDirsMapper implements FileNameMapper {
    private int dirs = 0;
    public void setDirs(int dirs) {
        this.dirs =  dirs;
    }
    public void setFrom(String ignore) {
    }
    public void setTo(String ignore) {
    }
    public String[] mapFileName(final String sourceFileName) {
        if (dirs <= 0) {
            throw new BuildException("dirs must be set to a positive number");
        }
        char fileSep = File.separatorChar;
        String fileSepCorrected =
            sourceFileName.replace('/', fileSep).replace('\\', fileSep);
        int nthMatch = fileSepCorrected.indexOf(fileSep);
        for (int n = 1; nthMatch > -1 && n < dirs; n++) {
            nthMatch = fileSepCorrected.indexOf(fileSep, nthMatch + 1);
        }
        if (nthMatch == -1) {
            return null;
        }
        return new String[] { sourceFileName.substring(nthMatch + 1) };
    }
}
