package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JavaEnvUtils;
public class Javadoc extends Task {
    private static final boolean JAVADOC_5 = 
        !JavaEnvUtils.isJavaVersion(JavaEnvUtils.JAVA_1_4);
    public class DocletParam {
        private String name;
        private String value;
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }
    public static class ExtensionInfo extends ProjectComponent {
        private String name;
        private Path path;
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public void setPath(Path path) {
            if (this.path == null) {
                this.path = path;
            } else {
                this.path.append(path);
            }
        }
        public Path getPath() {
            return path;
        }
        public Path createPath() {
            if (path == null) {
                path = new Path(getProject());
            }
            return path.createPath();
        }
        public void setPathRef(Reference r) {
            createPath().setRefid(r);
        }
    }
    public class DocletInfo extends ExtensionInfo {
        private Vector params = new Vector();
        public DocletParam createParam() {
            DocletParam param = new DocletParam();
            params.addElement(param);
            return param;
        }
        public Enumeration getParams() {
            return params.elements();
        }
    }
    public static class PackageName {
        private String name;
        public void setName(String name) {
            this.name = name.trim();
        }
        public String getName() {
            return name;
        }
        public String toString() {
            return getName();
        }
    }
    public static class SourceFile {
        private File file;
        public SourceFile() {
        }
        public SourceFile(File file) {
            this.file = file;
        }
        public void setFile(File file) {
            this.file = file;
        }
        public File getFile() {
            return file;
        }
    }
    public static class Html {
        private StringBuffer text = new StringBuffer();
        public void addText(String t) {
            text.append(t);
        }
        public String getText() {
            return text.substring(0);
        }
    }
    public static class AccessType extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"protected", "public", "package", "private"};
        }
    }
    public class ResourceCollectionContainer {
        private ArrayList rcs = new ArrayList();
        public void add(ResourceCollection rc) {
            rcs.add(rc);
        }
        private Iterator iterator() {
            return rcs.iterator();
        }
    }
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private Commandline cmd = new Commandline();
    private void addArgIf(boolean b, String arg) {
        if (b) {
            cmd.createArgument().setValue(arg);
        }
    }
    private void addArgIfNotEmpty(String key, String value) {
        if (value != null && value.length() != 0) {
            cmd.createArgument().setValue(key);
            cmd.createArgument().setValue(value);
        } else {
            log("Warning: Leaving out empty argument '" + key + "'",
                Project.MSG_WARN);
        }
    }
    private boolean failOnError = false;
    private Path sourcePath = null;
    private File destDir = null;
    private Vector sourceFiles = new Vector();
    private Vector packageNames = new Vector();
    private Vector excludePackageNames = new Vector(1);
    private boolean author = true;
    private boolean version = true;
    private DocletInfo doclet = null;
    private Path classpath = null;
    private Path bootclasspath = null;
    private String group = null;
    private String packageList = null;
    private Vector links = new Vector();
    private Vector groups = new Vector();
    private Vector tags = new Vector();
    private boolean useDefaultExcludes = true;
    private Html doctitle = null;
    private Html header = null;
    private Html footer = null;
    private Html bottom = null;
    private boolean useExternalFile = false;
    private String source = null;
    private boolean linksource = false;
    private boolean breakiterator = false;
    private String noqualifier;
    private boolean includeNoSourcePackages = false;
    private String executable = null;
    private boolean docFilesSubDirs = false;
    private String excludeDocFilesSubDir = null;
    private ResourceCollectionContainer nestedSourceFiles
        = new ResourceCollectionContainer();
    private Vector packageSets = new Vector();
    public void setUseExternalFile(boolean b) {
        useExternalFile = b;
    }
    public void setDefaultexcludes(boolean useDefaultExcludes) {
        this.useDefaultExcludes = useDefaultExcludes;
    }
    public void setMaxmemory(String max) {
        cmd.createArgument().setValue("-J-Xmx" + max);
    }
    public void setAdditionalparam(String add) {
        cmd.createArgument().setLine(add);
    }
    public Commandline.Argument createArg() {
        return cmd.createArgument();
    }
    public void setSourcepath(Path src) {
        if (sourcePath == null) {
            sourcePath = src;
        } else {
            sourcePath.append(src);
        }
    }
    public Path createSourcepath() {
        if (sourcePath == null) {
            sourcePath = new Path(getProject());
        }
        return sourcePath.createPath();
    }
    public void setSourcepathRef(Reference r) {
        createSourcepath().setRefid(r);
    }
    public void setDestdir(File dir) {
        destDir = dir;
        cmd.createArgument().setValue("-d");
        cmd.createArgument().setFile(destDir);
    }
    public void setSourcefiles(String src) {
        StringTokenizer tok = new StringTokenizer(src, ",");
        while (tok.hasMoreTokens()) {
            String f = tok.nextToken();
            SourceFile sf = new SourceFile();
            sf.setFile(getProject().resolveFile(f.trim()));
            addSource(sf);
        }
    }
    public void addSource(SourceFile sf) {
        sourceFiles.addElement(sf);
    }
    public void setPackagenames(String packages) {
        StringTokenizer tok = new StringTokenizer(packages, ",");
        while (tok.hasMoreTokens()) {
            String p = tok.nextToken();
            PackageName pn = new PackageName();
            pn.setName(p);
            addPackage(pn);
        }
    }
    public void addPackage(PackageName pn) {
        packageNames.addElement(pn);
    }
    public void setExcludePackageNames(String packages) {
        StringTokenizer tok = new StringTokenizer(packages, ",");
        while (tok.hasMoreTokens()) {
            String p = tok.nextToken();
            PackageName pn = new PackageName();
            pn.setName(p);
            addExcludePackage(pn);
        }
    }
    public void addExcludePackage(PackageName pn) {
        excludePackageNames.addElement(pn);
    }
    public void setOverview(File f) {
        cmd.createArgument().setValue("-overview");
        cmd.createArgument().setFile(f);
    }
    public void setPublic(boolean b) {
        addArgIf(b, "-public");
    }
    public void setProtected(boolean b) {
        addArgIf(b, "-protected");
    }
    public void setPackage(boolean b) {
        addArgIf(b, "-package");
    }
    public void setPrivate(boolean b) {
        addArgIf(b, "-private");
    }
    public void setAccess(AccessType at) {
        cmd.createArgument().setValue("-" + at.getValue());
    }
    public void setDoclet(String docletName) {
        if (doclet == null) {
            doclet = new DocletInfo();
            doclet.setProject(getProject());
        }
        doclet.setName(docletName);
    }
    public void setDocletPath(Path docletPath) {
        if (doclet == null) {
            doclet = new DocletInfo();
            doclet.setProject(getProject());
        }
        doclet.setPath(docletPath);
    }
    public void setDocletPathRef(Reference r) {
        if (doclet == null) {
            doclet = new DocletInfo();
            doclet.setProject(getProject());
        }
        doclet.createPath().setRefid(r);
    }
    public DocletInfo createDoclet() {
        if (doclet == null) {
            doclet = new DocletInfo();
        }
        return doclet;
    }
    public void addTaglet(ExtensionInfo tagletInfo) {
        tags.addElement(tagletInfo);
    }
    public void setOld(boolean b) {
        log("Javadoc 1.4 doesn't support the -1.1 switch anymore",
            Project.MSG_WARN);
    }
    public void setClasspath(Path path) {
        if (classpath == null) {
            classpath = path;
        } else {
            classpath.append(path);
        }
    }
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public void setBootclasspath(Path path) {
        if (bootclasspath == null) {
            bootclasspath = path;
        } else {
            bootclasspath.append(path);
        }
    }
    public Path createBootclasspath() {
        if (bootclasspath == null) {
            bootclasspath = new Path(getProject());
        }
        return bootclasspath.createPath();
    }
    public void setBootClasspathRef(Reference r) {
        createBootclasspath().setRefid(r);
    }
    public void setExtdirs(String path) {
        cmd.createArgument().setValue("-extdirs");
        cmd.createArgument().setValue(path);
    }
    public void setExtdirs(Path path) {
        cmd.createArgument().setValue("-extdirs");
        cmd.createArgument().setPath(path);
    }
    public void setVerbose(boolean b) {
        addArgIf(b, "-verbose");
    }
    public void setLocale(String locale) {
        cmd.createArgument(true).setValue(locale);
        cmd.createArgument(true).setValue("-locale");
    }
    public void setEncoding(String enc) {
        cmd.createArgument().setValue("-encoding");
        cmd.createArgument().setValue(enc);
    }
    public void setVersion(boolean b) {
        this.version = b;
    }
    public void setUse(boolean b) {
        addArgIf(b, "-use");
    }
    public void setAuthor(boolean b) {
        author = b;
    }
    public void setSplitindex(boolean b) {
        addArgIf(b, "-splitindex");
    }
    public void setWindowtitle(String title) {
        addArgIfNotEmpty("-windowtitle", title);
    }
    public void setDoctitle(String doctitle) {
        Html h = new Html();
        h.addText(doctitle);
        addDoctitle(h);
    }
    public void addDoctitle(Html text) {
        doctitle = text;
    }
    public void setHeader(String header) {
        Html h = new Html();
        h.addText(header);
        addHeader(h);
    }
    public void addHeader(Html text) {
        header = text;
    }
    public void setFooter(String footer) {
        Html h = new Html();
        h.addText(footer);
        addFooter(h);
    }
    public void addFooter(Html text) {
        footer = text;
    }
    public void setBottom(String bottom) {
        Html h = new Html();
        h.addText(bottom);
        addBottom(h);
    }
    public void addBottom(Html text) {
        bottom = text;
    }
    public void setLinkoffline(String src) {
        LinkArgument le = createLink();
        le.setOffline(true);
        String linkOfflineError = "The linkoffline attribute must include"
            + " a URL and a package-list file location separated by a"
            + " space";
        if (src.trim().length() == 0) {
            throw new BuildException(linkOfflineError);
        }
        StringTokenizer tok = new StringTokenizer(src, " ", false);
        le.setHref(tok.nextToken());
        if (!tok.hasMoreTokens()) {
            throw new BuildException(linkOfflineError);
        }
        le.setPackagelistLoc(getProject().resolveFile(tok.nextToken()));
    }
    public void setGroup(String src) {
        group = src;
    }
    public void setLink(String src) {
        createLink().setHref(src);
    }
    public void setNodeprecated(boolean b) {
        addArgIf(b, "-nodeprecated");
    }
    public void setNodeprecatedlist(boolean b) {
        addArgIf(b, "-nodeprecatedlist");
    }
    public void setNotree(boolean b) {
        addArgIf(b, "-notree");
    }
    public void setNoindex(boolean b) {
        addArgIf(b, "-noindex");
    }
    public void setNohelp(boolean b) {
        addArgIf(b, "-nohelp");
    }
    public void setNonavbar(boolean b) {
        addArgIf(b, "-nonavbar");
    }
    public void setSerialwarn(boolean b) {
        addArgIf(b, "-serialwarn");
    }
    public void setStylesheetfile(File f) {
        cmd.createArgument().setValue("-stylesheetfile");
        cmd.createArgument().setFile(f);
    }
    public void setHelpfile(File f) {
        cmd.createArgument().setValue("-helpfile");
        cmd.createArgument().setFile(f);
    }
    public void setDocencoding(String enc) {
        cmd.createArgument().setValue("-docencoding");
        cmd.createArgument().setValue(enc);
    }
    public void setPackageList(String src) {
        packageList = src;
    }
    public LinkArgument createLink() {
        LinkArgument la = new LinkArgument();
        links.addElement(la);
        return la;
    }
    public class LinkArgument {
        private String href;
        private boolean offline = false;
        private File packagelistLoc;
        private URL packagelistURL;
        private boolean resolveLink = false;
        public LinkArgument() {
        }
        public void setHref(String hr) {
            href = hr;
        }
        public String getHref() {
            return href;
        }
        public void setPackagelistLoc(File src) {
            packagelistLoc = src;
        }
        public File getPackagelistLoc() {
            return packagelistLoc;
        }
        public void setPackagelistURL(URL src) {
            packagelistURL = src;
        }
        public URL getPackagelistURL() {
            return packagelistURL;
        }
        public void setOffline(boolean offline) {
            this.offline = offline;
        }
        public boolean isLinkOffline() {
            return offline;
        }
        public void setResolveLink(boolean resolve) {
            this.resolveLink = resolve;
        }
        public boolean shouldResolveLink() {
            return resolveLink;
        }
    }
    public TagArgument createTag() {
        TagArgument ta = new TagArgument();
        tags.addElement (ta);
        return ta;
    }
    static final String[] SCOPE_ELEMENTS = {
        "overview", "packages", "types", "constructors",
        "methods", "fields"
    };
    public class TagArgument extends FileSet {
        private String name = null;
        private boolean enabled = true;
        private String scope = "a";
        public TagArgument () {
        }
        public void setName (String name) {
            this.name = name;
        }
        public void setScope (String verboseScope) throws BuildException {
            verboseScope = verboseScope.toLowerCase(Locale.ENGLISH);
            boolean[] elements = new boolean[SCOPE_ELEMENTS.length];
            boolean gotAll = false;
            boolean gotNotAll = false;
            StringTokenizer tok = new StringTokenizer (verboseScope, ",");
            while (tok.hasMoreTokens()) {
                String next = tok.nextToken().trim();
                if (next.equals("all")) {
                    if (gotAll) {
                        getProject().log ("Repeated tag scope element: all",
                                          Project.MSG_VERBOSE);
                    }
                    gotAll = true;
                } else {
                    int i;
                    for (i = 0; i < SCOPE_ELEMENTS.length; i++) {
                        if (next.equals (SCOPE_ELEMENTS[i])) {
                            break;
                        }
                    }
                    if (i == SCOPE_ELEMENTS.length) {
                        throw new BuildException ("Unrecognised scope element: "
                                                  + next);
                    } else {
                        if (elements[i]) {
                            getProject().log ("Repeated tag scope element: "
                                              + next, Project.MSG_VERBOSE);
                        }
                        elements[i] = true;
                        gotNotAll = true;
                    }
                }
            }
            if (gotNotAll && gotAll) {
                throw new BuildException ("Mixture of \"all\" and other scope "
                                          + "elements in tag parameter.");
            }
            if (!gotNotAll && !gotAll) {
                throw new BuildException ("No scope elements specified in tag "
                                          + "parameter.");
            }
            if (gotAll) {
                this.scope = "a";
            } else {
                StringBuffer buff = new StringBuffer (elements.length);
                for (int i = 0; i < elements.length; i++) {
                    if (elements[i]) {
                        buff.append (SCOPE_ELEMENTS[i].charAt(0));
                    }
                }
                this.scope = buff.toString();
            }
        }
        public void setEnabled (boolean enabled) {
            this.enabled = enabled;
        }
        public String getParameter() throws BuildException {
            if (name == null || name.equals("")) {
                throw new BuildException ("No name specified for custom tag.");
            }
            if (getDescription() != null) {
                return name + ":" + (enabled ? "" : "X")
                    + scope + ":" + getDescription();
            } else if (!enabled || !"a".equals(scope)) {
                return name + ":" + (enabled ? "" : "X") + scope;
            } else {
                return name;
            }
        }
    }
    public GroupArgument createGroup() {
        GroupArgument ga = new GroupArgument();
        groups.addElement(ga);
        return ga;
    }
    public class GroupArgument {
        private Html title;
        private Vector packages = new Vector();
        public GroupArgument() {
        }
        public void setTitle(String src) {
            Html h = new Html();
            h.addText(src);
            addTitle(h);
        }
        public void addTitle(Html text) {
            title = text;
        }
        public String getTitle() {
            return title != null ? title.getText() : null;
        }
        public void setPackages(String src) {
            StringTokenizer tok = new StringTokenizer(src, ",");
            while (tok.hasMoreTokens()) {
                String p = tok.nextToken();
                PackageName pn = new PackageName();
                pn.setName(p);
                addPackage(pn);
            }
        }
        public void addPackage(PackageName pn) {
            packages.addElement(pn);
        }
        public String getPackages() {
            StringBuffer p = new StringBuffer();
            for (int i = 0; i < packages.size(); i++) {
                if (i > 0) {
                    p.append(":");
                }
                p.append(packages.elementAt(i).toString());
            }
            return p.toString();
        }
    }
    public void setCharset(String src) {
        this.addArgIfNotEmpty("-charset", src);
    }
    public void setFailonerror(boolean b) {
        failOnError = b;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public void setExecutable(String executable) {
        this.executable = executable;
    }
    public void addPackageset(DirSet packageSet) {
        packageSets.addElement(packageSet);
    }
    public void addFileset(FileSet fs) {
        createSourceFiles().add(fs);
    }
    public ResourceCollectionContainer createSourceFiles() {
        return nestedSourceFiles;
    }
    public void setLinksource(boolean b) {
        this.linksource = b;
    }
    public void setBreakiterator(boolean b) {
        this.breakiterator = b;
    }
    public void setNoqualifier(String noqualifier) {
        this.noqualifier = noqualifier;
    }
    public void setIncludeNoSourcePackages(boolean b) {
        this.includeNoSourcePackages = b;
    }
    public void setDocFilesSubDirs(boolean b) {
        docFilesSubDirs = b;
    }
    public void setExcludeDocFilesSubDir(String s) {
        excludeDocFilesSubDir = s;
    }
    public void execute() throws BuildException {
        checkTaskName();
        Vector packagesToDoc = new Vector();
        Path sourceDirs = new Path(getProject());
        checkPackageAndSourcePath();
        if (sourcePath != null) {
            sourceDirs.addExisting(sourcePath);
        }
        parsePackages(packagesToDoc, sourceDirs);
        checkPackages(packagesToDoc, sourceDirs);
        Vector sourceFilesToDoc = (Vector) sourceFiles.clone();
        addSourceFiles(sourceFilesToDoc);
        checkPackagesToDoc(packagesToDoc, sourceFilesToDoc);
        log("Generating Javadoc", Project.MSG_INFO);
        Commandline toExecute = (Commandline) cmd.clone();
        if (executable != null) {
            toExecute.setExecutable(executable);
        } else {
            toExecute.setExecutable(JavaEnvUtils.getJdkExecutable("javadoc"));
        }
        generalJavadocArguments(toExecute);  
        doSourcePath(toExecute, sourceDirs); 
        doDoclet(toExecute);   
        doBootPath(toExecute); 
        doLinks(toExecute);    
        doGroup(toExecute);    
        doGroups(toExecute);  
        doDocFilesSubDirs(toExecute); 
        doJava14(toExecute);
        if (breakiterator && (doclet == null || JAVADOC_5)) {
            toExecute.createArgument().setValue("-breakiterator");
        }
        if (useExternalFile) {
            writeExternalArgs(toExecute);
        }
        File tmpList = null;
        BufferedWriter srcListWriter = null;
        try {
            if (useExternalFile) {
                tmpList = FILE_UTILS.createTempFile("javadoc", "", null, true, true);
                toExecute.createArgument()
                    .setValue("@" + tmpList.getAbsolutePath());
                srcListWriter = new BufferedWriter(
                    new FileWriter(tmpList.getAbsolutePath(),
                                   true));
            }
            doSourceAndPackageNames(
                toExecute, packagesToDoc, sourceFilesToDoc,
                useExternalFile, tmpList, srcListWriter);
        } catch (IOException e) {
            tmpList.delete();
            throw new BuildException("Error creating temporary file",
                                     e, getLocation());
        } finally {
            FileUtils.close(srcListWriter);
        }
        if (packageList != null) {
            toExecute.createArgument().setValue("@" + packageList);
        }
        log(toExecute.describeCommand(), Project.MSG_VERBOSE);
        log("Javadoc execution", Project.MSG_INFO);
        JavadocOutputStream out = new JavadocOutputStream(Project.MSG_INFO);
        JavadocOutputStream err = new JavadocOutputStream(Project.MSG_WARN);
        Execute exe = new Execute(new PumpStreamHandler(out, err));
        exe.setAntRun(getProject());
        exe.setWorkingDirectory(null);
        try {
            exe.setCommandline(toExecute.getCommandline());
            int ret = exe.execute();
            if (ret != 0 && failOnError) {
                throw new BuildException("Javadoc returned " + ret,
                                         getLocation());
            }
        } catch (IOException e) {
            throw new BuildException("Javadoc failed: " + e, e, getLocation());
        } finally {
            if (tmpList != null) {
                tmpList.delete();
                tmpList = null;
            }
            out.logFlush();
            err.logFlush();
            try {
                out.close();
                err.close();
            } catch (IOException e) {
            }
        }
    }
    private void checkTaskName() {
        if ("javadoc2".equals(getTaskType())) {
            log("Warning: the task name <javadoc2> is deprecated."
                + " Use <javadoc> instead.",
                Project.MSG_WARN);
        }
    }
    private void checkPackageAndSourcePath() {
        if (packageList != null && sourcePath == null) {
            String msg = "sourcePath attribute must be set when "
                + "specifying packagelist.";
            throw new BuildException(msg);
        }
    }
    private void checkPackages(Vector packagesToDoc, Path sourceDirs) {
        if (packagesToDoc.size() != 0 && sourceDirs.size() == 0) {
            String msg = "sourcePath attribute must be set when "
                + "specifying package names.";
            throw new BuildException(msg);
        }
    }
    private void checkPackagesToDoc(
        Vector packagesToDoc, Vector sourceFilesToDoc) {
        if (packageList == null && packagesToDoc.size() == 0
            && sourceFilesToDoc.size() == 0) {
            throw new BuildException("No source files and no packages have "
                                     + "been specified.");
        }
    }
    private void doSourcePath(Commandline toExecute, Path sourceDirs) {
        if (sourceDirs.size() > 0) {
            toExecute.createArgument().setValue("-sourcepath");
            toExecute.createArgument().setPath(sourceDirs);
        }
    }
    private void generalJavadocArguments(Commandline toExecute) {
        if (doctitle != null) {
            toExecute.createArgument().setValue("-doctitle");
            toExecute.createArgument().setValue(expand(doctitle.getText()));
        }
        if (header != null) {
            toExecute.createArgument().setValue("-header");
            toExecute.createArgument().setValue(expand(header.getText()));
        }
        if (footer != null) {
            toExecute.createArgument().setValue("-footer");
            toExecute.createArgument().setValue(expand(footer.getText()));
        }
        if (bottom != null) {
            toExecute.createArgument().setValue("-bottom");
            toExecute.createArgument().setValue(expand(bottom.getText()));
        }
        if (classpath == null) {
            classpath = (new Path(getProject())).concatSystemClasspath("last");
        } else {
            classpath = classpath.concatSystemClasspath("ignore");
        }
        if (classpath.size() > 0) {
            toExecute.createArgument().setValue("-classpath");
            toExecute.createArgument().setPath(classpath);
        }
        if (version && doclet == null) {
            toExecute.createArgument().setValue("-version");
        }
        if (author && doclet == null) {
            toExecute.createArgument().setValue("-author");
        }
        if (doclet == null && destDir == null) {
            throw new BuildException("destdir attribute must be set!");
        }
    }
    private void doDoclet(Commandline toExecute) {
        if (doclet != null) {
            if (doclet.getName() == null) {
                throw new BuildException("The doclet name must be "
                                         + "specified.", getLocation());
            } else {
                toExecute.createArgument().setValue("-doclet");
                toExecute.createArgument().setValue(doclet.getName());
                if (doclet.getPath() != null) {
                    Path docletPath
                        = doclet.getPath().concatSystemClasspath("ignore");
                    if (docletPath.size() != 0) {
                        toExecute.createArgument().setValue("-docletpath");
                        toExecute.createArgument().setPath(docletPath);
                    }
                }
                for (Enumeration e = doclet.getParams();
                     e.hasMoreElements();) {
                    DocletParam param = (DocletParam) e.nextElement();
                    if (param.getName() == null) {
                        throw new BuildException("Doclet parameters must "
                                                 + "have a name");
                    }
                    toExecute.createArgument().setValue(param.getName());
                    if (param.getValue() != null) {
                        toExecute.createArgument()
                            .setValue(param.getValue());
                    }
                }
            }
        }
    }
    private void writeExternalArgs(Commandline toExecute) {
        File optionsTmpFile = null;
        BufferedWriter optionsListWriter = null;
        try {
            optionsTmpFile = FILE_UTILS.createTempFile(
                "javadocOptions", "", null, true, true);
            String[] listOpt = toExecute.getArguments();
            toExecute.clearArgs();
            toExecute.createArgument().setValue(
                "@" + optionsTmpFile.getAbsolutePath());
            optionsListWriter = new BufferedWriter(
                new FileWriter(optionsTmpFile.getAbsolutePath(), true));
            for (int i = 0; i < listOpt.length; i++) {
                String string = listOpt[i];
                if (string.startsWith("-J-")) {
                    toExecute.createArgument().setValue(string);
                } else  {
                    if (string.startsWith("-")) {
                        optionsListWriter.write(string);
                        optionsListWriter.write(" ");
                    } else {
                        optionsListWriter.write(quoteString(string));
                        optionsListWriter.newLine();
                    }
                }
            }
            optionsListWriter.close();
        } catch (IOException ex) {
            if (optionsTmpFile != null) {
                optionsTmpFile.delete();
            }
            throw new BuildException(
                "Error creating or writing temporary file for javadoc options",
                ex, getLocation());
        } finally {
            FileUtils.close(optionsListWriter);
        }
    }
    private void doBootPath(Commandline toExecute) {
        Path bcp = new Path(getProject());
        if (bootclasspath != null) {
            bcp.append(bootclasspath);
        }
        bcp = bcp.concatSystemBootClasspath("ignore");
        if (bcp.size() > 0) {
            toExecute.createArgument().setValue("-bootclasspath");
            toExecute.createArgument().setPath(bcp);
        }
    }
    private void doLinks(Commandline toExecute) {
        if (links.size() != 0) {
            for (Enumeration e = links.elements(); e.hasMoreElements();) {
                LinkArgument la = (LinkArgument) e.nextElement();
                if (la.getHref() == null || la.getHref().length() == 0) {
                    log("No href was given for the link - skipping",
                        Project.MSG_VERBOSE);
                    continue;
                }
                String link = null;
                if (la.shouldResolveLink()) {
                    File hrefAsFile =
                        getProject().resolveFile(la.getHref());
                    if (hrefAsFile.exists()) {
                        try {
                            link = FILE_UTILS.getFileURL(hrefAsFile)
                                .toExternalForm();
                        } catch (MalformedURLException ex) {
                            log("Warning: link location was invalid "
                                + hrefAsFile, Project.MSG_WARN);
                        }
                    }
                }
                if (link == null) {
                    try {
                        URL base = new URL("file://.");
                        new URL(base, la.getHref());
                        link = la.getHref();
                    } catch (MalformedURLException mue) {
                        log("Link href \"" + la.getHref()
                            + "\" is not a valid url - skipping link",
                            Project.MSG_WARN);
                        continue;
                    }
                }
                if (la.isLinkOffline()) {
                    File packageListLocation = la.getPackagelistLoc();
                    URL packageListURL = la.getPackagelistURL();
                    if (packageListLocation == null
                        && packageListURL == null) {
                        throw new BuildException("The package list"
                                                 + " location for link "
                                                 + la.getHref()
                                                 + " must be provided "
                                                 + "because the link is "
                                                 + "offline");
                    }
                    if (packageListLocation != null) {
                        File packageListFile =
                            new File(packageListLocation, "package-list");
                        if (packageListFile.exists()) {
                            try {
                                packageListURL =
                                    FILE_UTILS.getFileURL(packageListLocation);
                            } catch (MalformedURLException ex) {
                                log("Warning: Package list location was "
                                    + "invalid " + packageListLocation,
                                    Project.MSG_WARN);
                            }
                        } else {
                            log("Warning: No package list was found at "
                                + packageListLocation, Project.MSG_VERBOSE);
                        }
                    }
                    if (packageListURL != null) {
                        toExecute.createArgument().setValue("-linkoffline");
                        toExecute.createArgument().setValue(link);
                        toExecute.createArgument()
                            .setValue(packageListURL.toExternalForm());
                    }
                } else {
                    toExecute.createArgument().setValue("-link");
                    toExecute.createArgument().setValue(link);
                }
            }
        }
    }
    private void doGroup(Commandline toExecute) {
        if (group != null) {
            StringTokenizer tok = new StringTokenizer(group, ",", false);
            while (tok.hasMoreTokens()) {
                String grp = tok.nextToken().trim();
                int space = grp.indexOf(" ");
                if (space > 0) {
                    String name = grp.substring(0, space);
                    String pkgList = grp.substring(space + 1);
                    toExecute.createArgument().setValue("-group");
                    toExecute.createArgument().setValue(name);
                    toExecute.createArgument().setValue(pkgList);
                }
            }
        }
    }
    private void doGroups(Commandline toExecute) {
        if (groups.size() != 0) {
            for (Enumeration e = groups.elements(); e.hasMoreElements();) {
                GroupArgument ga = (GroupArgument) e.nextElement();
                String title = ga.getTitle();
                String packages = ga.getPackages();
                if (title == null || packages == null) {
                    throw new BuildException("The title and packages must "
                                             + "be specified for group "
                                             + "elements.");
                }
                toExecute.createArgument().setValue("-group");
                toExecute.createArgument().setValue(expand(title));
                toExecute.createArgument().setValue(packages);
            }
        }
    }
    private void doJava14(Commandline toExecute) {
        for (Enumeration e = tags.elements(); e.hasMoreElements();) {
            Object element = e.nextElement();
            if (element instanceof TagArgument) {
                TagArgument ta = (TagArgument) element;
                File tagDir = ta.getDir(getProject());
                if (tagDir == null) {
                    toExecute.createArgument().setValue ("-tag");
                    toExecute.createArgument()
                        .setValue (ta.getParameter());
                } else {
                    DirectoryScanner tagDefScanner =
                        ta.getDirectoryScanner(getProject());
                    String[] files = tagDefScanner.getIncludedFiles();
                    for (int i = 0; i < files.length; i++) {
                        File tagDefFile = new File(tagDir, files[i]);
                        try {
                            BufferedReader in
                                = new BufferedReader(
                                    new FileReader(tagDefFile)
                                                     );
                            String line = null;
                            while ((line = in.readLine()) != null) {
                                toExecute.createArgument()
                                    .setValue("-tag");
                                toExecute.createArgument()
                                    .setValue(line);
                            }
                            in.close();
                        } catch (IOException ioe) {
                            throw new BuildException(
                                "Couldn't read "
                                + " tag file from "
                                + tagDefFile.getAbsolutePath(), ioe);
                        }
                    }
                }
            } else {
                ExtensionInfo tagletInfo = (ExtensionInfo) element;
                toExecute.createArgument().setValue("-taglet");
                toExecute.createArgument().setValue(tagletInfo
                                                    .getName());
                if (tagletInfo.getPath() != null) {
                    Path tagletPath = tagletInfo.getPath()
                        .concatSystemClasspath("ignore");
                    if (tagletPath.size() != 0) {
                        toExecute.createArgument()
                            .setValue("-tagletpath");
                        toExecute.createArgument().setPath(tagletPath);
                    }
                }
            }
        }
        String sourceArg = source != null ? source
            : getProject().getProperty(MagicNames.BUILD_JAVAC_SOURCE);
        if (sourceArg != null) {
            toExecute.createArgument().setValue("-source");
            toExecute.createArgument().setValue(sourceArg);
        }
        if (linksource && doclet == null) {
            toExecute.createArgument().setValue("-linksource");
        }
        if (noqualifier != null && doclet == null) {
            toExecute.createArgument().setValue("-noqualifier");
            toExecute.createArgument().setValue(noqualifier);
        }
    }
    private void doDocFilesSubDirs(Commandline toExecute) {
        if (docFilesSubDirs) {
            toExecute.createArgument().setValue("-docfilessubdirs");
            if (excludeDocFilesSubDir != null
                && excludeDocFilesSubDir.trim().length() > 0) {
                toExecute.createArgument().setValue("-excludedocfilessubdir");
                toExecute.createArgument().setValue(excludeDocFilesSubDir);
            }
        }
    }
    private void doSourceAndPackageNames(
        Commandline toExecute,
        Vector packagesToDoc,
        Vector sourceFilesToDoc,
        boolean useExternalFile,
        File    tmpList,
        BufferedWriter srcListWriter)
        throws IOException {
        Enumeration e = packagesToDoc.elements();
        while (e.hasMoreElements()) {
            String packageName = (String) e.nextElement();
            if (useExternalFile) {
                srcListWriter.write(packageName);
                srcListWriter.newLine();
            } else {
                toExecute.createArgument().setValue(packageName);
            }
        }
        e = sourceFilesToDoc.elements();
        while (e.hasMoreElements()) {
            SourceFile sf = (SourceFile) e.nextElement();
            String sourceFileName = sf.getFile().getAbsolutePath();
            if (useExternalFile) {
                if (sourceFileName.indexOf(" ") > -1) {
                    String name = sourceFileName;
                    if (File.separatorChar == '\\') {
                        name = sourceFileName.replace(File.separatorChar, '/');
                    }
                    srcListWriter.write("\"" + name + "\"");
                } else {
                    srcListWriter.write(sourceFileName);
                }
                srcListWriter.newLine();
            } else {
                toExecute.createArgument().setValue(sourceFileName);
            }
        }
    }
    private String quoteString(final String str) {
        if (!containsWhitespace(str)
            && str.indexOf('\'') == -1
            && str.indexOf('"') == -1) {
            return str;
        }
        if (str.indexOf('\'') == -1) {
            return quoteString(str, '\'');
        } else {
            return quoteString(str, '"');
        }
    }
    private boolean containsWhitespace(final String s) {
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            if (Character.isWhitespace(s.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    private String quoteString(final String str, final char delim) {
        StringBuffer buf = new StringBuffer(str.length() * 2);
        buf.append(delim);
        final int len = str.length();
        boolean lastCharWasCR = false;
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c == delim) { 
                buf.append('\\').append(c);
                lastCharWasCR = false;
            } else {
                switch (c) {
                case '\\':
                    buf.append("\\\\");
                    lastCharWasCR = false;
                    break;
                case '\r':
                    buf.append("\\\r");
                    lastCharWasCR = true;
                    break;
                case '\n':
                    if (!lastCharWasCR) {
                        buf.append("\\\n");
                    } else {
                        buf.append("\n");
                    }
                    lastCharWasCR = false;
                    break;
                default:
                    buf.append(c);
                    lastCharWasCR = false;
                    break;
                }
            }
        }
        buf.append(delim);
        return buf.toString();
    }
    private void addSourceFiles(Vector sf) {
        Iterator e = nestedSourceFiles.iterator();
        while (e.hasNext()) {
            ResourceCollection rc = (ResourceCollection) e.next();
            if (!rc.isFilesystemOnly()) {
                throw new BuildException("only file system based resources are"
                                         + " supported by javadoc");
            }
            if (rc instanceof FileSet) {
                FileSet fs = (FileSet) rc;
                if (!fs.hasPatterns() && !fs.hasSelectors()) {
                    FileSet fs2 = (FileSet) fs.clone();
                    fs2.createInclude().setName("**/*.java");
                    if (includeNoSourcePackages) {
                        fs2.createInclude().setName("**/package.html");
                    }
                    rc = fs2;
                }
            }
            Iterator iter = rc.iterator();
            while (iter.hasNext()) {
                Resource r = (Resource) iter.next();
                sf.addElement(new SourceFile(((FileProvider) r.as(FileProvider.class))
                                             .getFile()));
            }
        }
    }
    private void parsePackages(Vector pn, Path sp) {
        HashSet addedPackages = new HashSet();
        Vector dirSets = (Vector) packageSets.clone();
        if (sourcePath != null) {
            PatternSet ps = new PatternSet();
            ps.setProject(getProject());
            if (packageNames.size() > 0) {
                Enumeration e = packageNames.elements();
                while (e.hasMoreElements()) {
                    PackageName p = (PackageName) e.nextElement();
                    String pkg = p.getName().replace('.', '/');
                    if (pkg.endsWith("*")) {
                        pkg += "*";
                    }
                    ps.createInclude().setName(pkg);
                }
            } else {
                ps.createInclude().setName("**");
            }
            Enumeration e = excludePackageNames.elements();
            while (e.hasMoreElements()) {
                PackageName p = (PackageName) e.nextElement();
                String pkg = p.getName().replace('.', '/');
                if (pkg.endsWith("*")) {
                    pkg += "*";
                }
                ps.createExclude().setName(pkg);
            }
            String[] pathElements = sourcePath.list();
            for (int i = 0; i < pathElements.length; i++) {
                File dir = new File(pathElements[i]);
                if (dir.isDirectory()) {
                    DirSet ds = new DirSet();
                    ds.setProject(getProject());
                    ds.setDefaultexcludes(useDefaultExcludes);
                    ds.setDir(dir);
                    ds.createPatternSet().addConfiguredPatternset(ps);
                    dirSets.addElement(ds);
                } else {
                    log("Skipping " + pathElements[i]
                        + " since it is no directory.", Project.MSG_WARN);
                }
            }
        }
        Enumeration e = dirSets.elements();
        while (e.hasMoreElements()) {
            DirSet ds = (DirSet) e.nextElement();
            File baseDir = ds.getDir(getProject());
            log("scanning " + baseDir + " for packages.", Project.MSG_DEBUG);
            DirectoryScanner dsc = ds.getDirectoryScanner(getProject());
            String[] dirs = dsc.getIncludedDirectories();
            boolean containsPackages = false;
            for (int i = 0; i < dirs.length; i++) {
                File pd = new File(baseDir, dirs[i]);
                String[] files = pd.list(new FilenameFilter () {
                        public boolean accept(File dir1, String name) {
                            return name.endsWith(".java")
                                || (includeNoSourcePackages
                                    && name.equals("package.html"));
                        }
                    });
                if (files.length > 0) {
                    if ("".equals(dirs[i])) {
                        log(baseDir
                            + " contains source files in the default package,"
                            + " you must specify them as source files"
                            + " not packages.",
                            Project.MSG_WARN);
                    } else {
                        containsPackages = true;
                        String packageName =
                            dirs[i].replace(File.separatorChar, '.');
                        if (!addedPackages.contains(packageName)) {
                            addedPackages.add(packageName);
                            pn.addElement(packageName);
                        }
                    }
                }
            }
            if (containsPackages) {
                sp.createPathElement().setLocation(baseDir);
            } else {
                log(baseDir + " doesn\'t contain any packages, dropping it.",
                    Project.MSG_VERBOSE);
            }
        }
    }
    private class JavadocOutputStream extends LogOutputStream {
        JavadocOutputStream(int level) {
            super(Javadoc.this, level);
        }
        private String queuedLine = null;
        protected void processLine(String line, int messageLevel) {
            if (messageLevel == Project.MSG_INFO
                && line.startsWith("Generating ")) {
                if (queuedLine != null) {
                    super.processLine(queuedLine, Project.MSG_VERBOSE);
                }
                queuedLine = line;
            } else {
                if (queuedLine != null) {
                    if (line.startsWith("Building ")) {
                        super.processLine(queuedLine, Project.MSG_VERBOSE);
                    } else {
                        super.processLine(queuedLine, Project.MSG_INFO);
                    }
                    queuedLine = null;
                }
                super.processLine(line, messageLevel);
            }
        }
        protected void logFlush() {
            if (queuedLine != null) {
                super.processLine(queuedLine, Project.MSG_VERBOSE);
                queuedLine = null;
            }
        }
    }
    protected String expand(String content) {
        return getProject().replaceProperties(content);
    }
}
