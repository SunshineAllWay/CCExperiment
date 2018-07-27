package org.apache.tools.ant.taskdefs.optional;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.RegularExpression;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.Substitution;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.regexp.Regexp;
import org.apache.tools.ant.util.regexp.RegexpUtil;
public class ReplaceRegExp extends Task {
    private File file;
    private String flags;
    private boolean byline;
    private Union resources;
    private RegularExpression regex;
    private Substitution subs;
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private boolean preserveLastModified = false;
    private String encoding = null;
    public ReplaceRegExp() {
        super();
        this.file = null;
        this.flags = "";
        this.byline = false;
        this.regex = null;
        this.subs = null;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public void setMatch(String match) {
        if (regex != null) {
            throw new BuildException("Only one regular expression is allowed");
        }
        regex = new RegularExpression();
        regex.setPattern(match);
    }
    public void setReplace(String replace) {
        if (subs != null) {
            throw new BuildException("Only one substitution expression is "
                                     + "allowed");
        }
        subs = new Substitution();
        subs.setExpression(replace);
    }
    public void setFlags(String flags) {
        this.flags = flags;
    }
    public void setByLine(String byline) {
        Boolean res = Boolean.valueOf(byline);
        if (res == null) {
            res = Boolean.FALSE;
        }
        this.byline = res.booleanValue();
    }
    public void setByLine(boolean byline) {
        this.byline = byline;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public void addFileset(FileSet set) {
        addConfigured(set);
    }
    public void addConfigured(ResourceCollection rc) {
        if (!rc.isFilesystemOnly()) {
            throw new BuildException("only filesystem resources are supported");
        }
        if (resources == null) {
            resources = new Union();
        }
        resources.add(rc);
    }
    public RegularExpression createRegexp() {
        if (regex != null) {
            throw new BuildException("Only one regular expression is allowed.");
        }
        regex = new RegularExpression();
        return regex;
    }
    public Substitution createSubstitution() {
        if (subs != null) {
            throw new BuildException("Only one substitution expression is "
                                     + "allowed");
        }
        subs = new Substitution();
        return subs;
    }
    public void setPreserveLastModified(boolean b) {
        preserveLastModified = b;
    }
    protected String doReplace(RegularExpression r,
                               Substitution s,
                               String input,
                               int options) {
        String res = input;
        Regexp regexp = r.getRegexp(getProject());
        if (regexp.matches(input, options)) {
            log("Found match; substituting", Project.MSG_DEBUG);
            res = regexp.substitute(input, s.getExpression(getProject()),
                                    options);
        }
        return res;
    }
    protected void doReplace(File f, int options)
         throws IOException {
        File temp = FILE_UTILS.createTempFile("replace", ".txt", null, true, true);
        Reader r = null;
        Writer w = null;
        BufferedWriter bw = null;
        try {
            if (encoding == null) {
                r = new FileReader(f);
                w = new FileWriter(temp);
            } else {
                r = new InputStreamReader(new FileInputStream(f), encoding);
                w = new OutputStreamWriter(new FileOutputStream(temp),
                                           encoding);
            }
            BufferedReader br = new BufferedReader(r);
            bw = new BufferedWriter(w);
            boolean changes = false;
            log("Replacing pattern '" + regex.getPattern(getProject())
                + "' with '" + subs.getExpression(getProject())
                + "' in '" + f.getPath() + "'" + (byline ? " by line" : "")
                + (flags.length() > 0 ? " with flags: '" + flags + "'" : "")
                + ".", Project.MSG_VERBOSE);
            if (byline) {
                StringBuffer linebuf = new StringBuffer();
                String line = null;
                String res = null;
                int c;
                boolean hasCR = false;
                do {
                    c = br.read();
                    if (c == '\r') {
                        if (hasCR) {
                            line = linebuf.toString();
                            res  = doReplace(regex, subs, line, options);
                            if (!res.equals(line)) {
                                changes = true;
                            }
                            bw.write(res);
                            bw.write('\r');
                            linebuf = new StringBuffer();
                        } else {
                            hasCR = true;
                        }
                    } else if (c == '\n') {
                        line = linebuf.toString();
                        res  = doReplace(regex, subs, line, options);
                        if (!res.equals(line)) {
                            changes = true;
                        }
                        bw.write(res);
                        if (hasCR) {
                            bw.write('\r');
                            hasCR = false;
                        }
                        bw.write('\n');
                        linebuf = new StringBuffer();
                    } else { 
                        if ((hasCR) || (c < 0)) {
                            line = linebuf.toString();
                            res  = doReplace(regex, subs, line, options);
                            if (!res.equals(line)) {
                                changes = true;
                            }
                            bw.write(res);
                            if (hasCR) {
                                bw.write('\r');
                                hasCR = false;
                            }
                            linebuf = new StringBuffer();
                        }
                        if (c >= 0) {
                            linebuf.append((char) c);
                        }
                    }
                } while (c >= 0);
            } else {
                String buf = FileUtils.safeReadFully(br);
                String res = doReplace(regex, subs, buf, options);
                if (!res.equals(buf)) {
                    changes = true;
                }
                bw.write(res);
            }
            bw.flush();
            r.close();
            r = null;
            w.close();
            w = null;
            if (changes) {
                log("File has changed; saving the updated file", Project.MSG_VERBOSE);
                try {
                    long origLastModified = f.lastModified();
                    FILE_UTILS.rename(temp, f);
                    if (preserveLastModified) {
                        FILE_UTILS.setFileLastModified(f, origLastModified);
                    }
                    temp = null;
                } catch (IOException e) {
                    throw new BuildException("Couldn't rename temporary file "
                                             + temp, e, getLocation());
                }
            } else {
                log("No change made", Project.MSG_DEBUG);
            }
        } finally {
            FileUtils.close(r);
            FileUtils.close(bw);
            FileUtils.close(w);
            if (temp != null) {
                temp.delete();
            }
        }
    }
    public void execute() throws BuildException {
        if (regex == null) {
            throw new BuildException("No expression to match.");
        }
        if (subs == null) {
            throw new BuildException("Nothing to replace expression with.");
        }
        if (file != null && resources != null) {
            throw new BuildException("You cannot supply the 'file' attribute "
                                     + "and resource collections at the same "
                                     + "time.");
        }
        int options = RegexpUtil.asOptions(flags);
        if (file != null && file.exists()) {
            try {
                doReplace(file, options);
            } catch (IOException e) {
                log("An error occurred processing file: '"
                    + file.getAbsolutePath() + "': " + e.toString(),
                    Project.MSG_ERR);
            }
        } else if (file != null) {
            log("The following file is missing: '"
                + file.getAbsolutePath() + "'", Project.MSG_ERR);
        }
        if (resources != null) {
            for (Iterator i = resources.iterator(); i.hasNext(); ) {
                FileProvider fp =
                    (FileProvider) ((Resource) i.next()).as(FileProvider.class);
                File f = fp.getFile();
                if (f.exists()) {
                    try {
                        doReplace(f, options);
                    } catch (Exception e) {
                        log("An error occurred processing file: '"
                            + f.getAbsolutePath() + "': " + e.toString(),
                            Project.MSG_ERR);
                    }
                } else {
                    log("The following file is missing: '"
                        + f.getAbsolutePath() + "'", Project.MSG_ERR);
                }
            }
        }
    }
}
