package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.DynamicConfigurator;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.ResourceUtils;
public class XSLTProcess extends MatchingTask implements XSLTLogger {
    private File destDir = null;
    private File baseDir = null;
    private String xslFile = null;
    private Resource xslResource = null;
    private String targetExtension = ".html";
    private String fileNameParameter = null;
    private String fileDirParameter = null;
    private Vector params = new Vector();
    private File inFile = null;
    private File outFile = null;
    private String processor;
    private Path classpath = null;
    private XSLTLiaison liaison;
    private boolean stylesheetLoaded = false;
    private boolean force = false;
    private Vector outputProperties = new Vector();
    private XMLCatalog xmlCatalog = new XMLCatalog();
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private boolean performDirectoryScan = true;
    private Factory factory = null;
    private boolean reuseLoadedStylesheet = true;
    private AntClassLoader loader = null;
    private Mapper mapperElement = null;
    private Union resources = new Union();
    private boolean useImplicitFileset = true;
    public static final String PROCESSOR_TRAX = "trax";
    private boolean suppressWarnings = false;
    private boolean failOnTransformationError = true;
    private boolean failOnError = true;
    private boolean failOnNoResources = true;
    private CommandlineJava.SysProperties sysProperties =
        new CommandlineJava.SysProperties();
    private TraceConfiguration traceConfiguration;
    public XSLTProcess() {
    } 
    public void setScanIncludedDirectories(boolean b) {
        performDirectoryScan = b;
    }
    public void setReloadStylesheet(boolean b) {
        reuseLoadedStylesheet = !b;
    }
    public void addMapper(Mapper mapper) {
        if (mapperElement != null) {
            handleError("Cannot define more than one mapper");
        } else {
            mapperElement = mapper;
        }
    }
    public void add(ResourceCollection rc) {
        resources.add(rc);
    }
    public void addConfiguredStyle(Resources rc) {
        if (rc.size() != 1) {
            handleError("The style element must be specified with exactly one"
                        + " nested resource.");
        } else {
            setXslResource((Resource) rc.iterator().next());
        }
    }
    public void setXslResource(Resource xslResource) {
        this.xslResource = xslResource;
    }
    public void add(FileNameMapper fileNameMapper) throws BuildException {
       Mapper mapper = new Mapper(getProject());
       mapper.add(fileNameMapper);
       addMapper(mapper);
    }
    public void execute() throws BuildException {
        if ("style".equals(getTaskType())) {
            log("Warning: the task name <style> is deprecated. Use <xslt> instead.",
                    Project.MSG_WARN);
        }
        File savedBaseDir = baseDir;
        DirectoryScanner scanner;
        String[]         list;
        String[]         dirs;
        String baseMessage =
            "specify the stylesheet either as a filename in style attribute "
            + "or as a nested resource";
        if (xslResource == null && xslFile == null) {
            handleError(baseMessage);
            return;
        }
        if (xslResource != null && xslFile != null) {
            handleError(baseMessage + " but not as both");
            return;
        }
        if (inFile != null && !inFile.exists()) {
            handleError("input file " + inFile + " does not exist");
            return;
        }
        try {
            setupLoader();
            if (sysProperties.size() > 0) {
                sysProperties.setSystem();
            }
            Resource styleResource;
            if (baseDir == null) {
                baseDir = getProject().getBaseDir();
            }
            liaison = getLiaison();
            if (liaison instanceof XSLTLoggerAware) {
                ((XSLTLoggerAware) liaison).setLogger(this);
            }
            log("Using " + liaison.getClass().toString(), Project.MSG_VERBOSE);
            if (xslFile != null) {
                File stylesheet = getProject().resolveFile(xslFile);
                if (!stylesheet.exists()) {
                    stylesheet = FILE_UTILS.resolveFile(baseDir, xslFile);
                    if (stylesheet.exists()) {
                        log("DEPRECATED - the 'style' attribute should be "
                            + "relative to the project's");
                        log("             basedir, not the tasks's basedir.");
                    }
                }
                FileResource fr = new FileResource();
                fr.setProject(getProject());
                fr.setFile(stylesheet);
                styleResource = fr;
            } else {
                styleResource = xslResource;
            }
            if (!styleResource.isExists()) {
                handleError("stylesheet " + styleResource + " doesn't exist.");
                return;
            }
            if (inFile != null && outFile != null) {
                process(inFile, outFile, styleResource);
                return;
            }
            checkDest();
            if (useImplicitFileset) {
                scanner = getDirectoryScanner(baseDir);
                log("Transforming into " + destDir, Project.MSG_INFO);
                list = scanner.getIncludedFiles();
                for (int i = 0; i < list.length; ++i) {
                    process(baseDir, list[i], destDir, styleResource);
                }
                if (performDirectoryScan) {
                    dirs = scanner.getIncludedDirectories();
                    for (int j = 0; j < dirs.length; ++j) {
                        list = new File(baseDir, dirs[j]).list();
                        for (int i = 0; i < list.length; ++i) {
                            process(baseDir, dirs[j] + File.separator + list[i], destDir,
                                    styleResource);
                        }
                    }
                }
            } else { 
                if (resources.size() == 0) {
                    if (failOnNoResources) {
                        handleError("no resources specified");
                    }
                    return;
                }
            }
            processResources(styleResource);
        } finally {
            if (loader != null) {
                loader.resetThreadContextLoader();
                loader.cleanup();
                loader = null;
            }
            if (sysProperties.size() > 0) {
                sysProperties.restoreSystem();
            }
            liaison = null;
            stylesheetLoaded = false;
            baseDir = savedBaseDir;
        }
    }
    public void setForce(boolean force) {
        this.force = force;
    }
    public void setBasedir(File dir) {
        baseDir = dir;
    }
    public void setDestdir(File dir) {
        destDir = dir;
    }
    public void setExtension(String name) {
        targetExtension = name;
    }
    public void setStyle(String xslFile) {
        this.xslFile = xslFile;
    }
    public void setClasspath(Path classpath) {
        createClasspath().append(classpath);
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
    public void setProcessor(String processor) {
        this.processor = processor;
    }
    public void setUseImplicitFileset(boolean useimplicitfileset) {
        useImplicitFileset = useimplicitfileset;
    }
    public void addConfiguredXMLCatalog(XMLCatalog xmlCatalog) {
        this.xmlCatalog.addConfiguredXMLCatalog(xmlCatalog);
    }
    public void setFileNameParameter(String fileNameParameter) {
        this.fileNameParameter = fileNameParameter;
    }
    public void setFileDirParameter(String fileDirParameter) {
        this.fileDirParameter = fileDirParameter;
    }
    public void setSuppressWarnings(boolean b) {
        suppressWarnings = b;
    }
    public boolean getSuppressWarnings() {
        return suppressWarnings;
    }    
    public void setFailOnTransformationError(boolean b) {
        failOnTransformationError = b;
    }
    public void setFailOnError(boolean b) {
        failOnError = b;
    }
    public void setFailOnNoResources(boolean b) {
        failOnNoResources = b;
    }
    public void addSysproperty(Environment.Variable sysp) {
        sysProperties.addVariable(sysp);
    }
    public void addSyspropertyset(PropertySet sysp) {
        sysProperties.addSyspropertyset(sysp);
    }
    public TraceConfiguration createTrace() {
        if (traceConfiguration != null) {
            throw new BuildException("can't have more than one trace"
                                     + " configuration");
        }
        traceConfiguration = new TraceConfiguration();
        return traceConfiguration;
    }
    public TraceConfiguration getTraceConfiguration() {
        return traceConfiguration;
    }
    private void resolveProcessor(String proc) throws Exception {
        if (proc.equals(PROCESSOR_TRAX)) {
            liaison = new org.apache.tools.ant.taskdefs.optional.TraXLiaison();
        } else {
            Class clazz = loadClass(proc);
            liaison = (XSLTLiaison) clazz.newInstance();
        }
    }
    private Class loadClass(String classname) throws Exception {
        setupLoader();
        if (loader == null) {
            return Class.forName(classname);
        }
        return Class.forName(classname, true, loader);
    }
    private void setupLoader() {
        if (classpath != null && loader == null) {
            loader = getProject().createClassLoader(classpath);
            loader.setThreadContextLoader();
        }
    }
    public void setOut(File outFile) {
        this.outFile = outFile;
    }
    public void setIn(File inFile) {
        this.inFile = inFile;
    }
    private void checkDest() {
        if (destDir == null) {
            handleError("destdir attributes must be set!");
        }
    }
    private void processResources(Resource stylesheet) {
        Iterator iter = resources.iterator();
        while (iter.hasNext()) {
            Resource r = (Resource) iter.next();
            if (!r.isExists()) {
                continue;
            }
            File base = baseDir;
            String name = r.getName();
            FileProvider fp = (FileProvider) r.as(FileProvider.class);
            if (fp != null) {
                FileResource f = ResourceUtils.asFileResource(fp);
                base = f.getBaseDir();
                if (base == null) {
                    name = f.getFile().getAbsolutePath();
                }
            }
            process(base, name, destDir, stylesheet);
        }
    }
    private void process(File baseDir, String xmlFile, File destDir, Resource stylesheet)
            throws BuildException {
        File   outF = null;
        File   inF = null;
        try {
            long styleSheetLastModified = stylesheet.getLastModified();
            inF = new File(baseDir, xmlFile);
            if (inF.isDirectory()) {
                log("Skipping " + inF + " it is a directory.", Project.MSG_VERBOSE);
                return;
            }
            FileNameMapper mapper = null;
            if (mapperElement != null) {
                mapper = mapperElement.getImplementation();
            } else {
                mapper = new StyleMapper();
            }
            String[] outFileName = mapper.mapFileName(xmlFile);
            if (outFileName == null || outFileName.length == 0) {
                log("Skipping " + inFile + " it cannot get mapped to output.", Project.MSG_VERBOSE);
                return;
            } else if (outFileName == null || outFileName.length > 1) {
                log("Skipping " + inFile + " its mapping is ambiguos.", Project.MSG_VERBOSE);
                return;
            }
            outF = new File(destDir, outFileName[0]);
            if (force || inF.lastModified() > outF.lastModified()
                    || styleSheetLastModified > outF.lastModified()) {
                ensureDirectoryFor(outF);
                log("Processing " + inF + " to " + outF);
                configureLiaison(stylesheet);
                setLiaisonDynamicFileParameters(liaison, inF);
                liaison.transform(inF, outF);
            }
        } catch (Exception ex) {
            log("Failed to process " + inFile, Project.MSG_INFO);
            if (outF != null) {
                outF.delete();
            }
            handleTransformationError(ex);
        }
    } 
    private void process(File inFile, File outFile, Resource stylesheet) throws BuildException {
        try {
            long styleSheetLastModified = stylesheet.getLastModified();
            log("In file " + inFile + " time: " + inFile.lastModified(), Project.MSG_DEBUG);
            log("Out file " + outFile + " time: " + outFile.lastModified(), Project.MSG_DEBUG);
            log("Style file " + xslFile + " time: " + styleSheetLastModified, Project.MSG_DEBUG);
            if (force || inFile.lastModified() >= outFile.lastModified()
                    || styleSheetLastModified >= outFile.lastModified()) {
                ensureDirectoryFor(outFile);
                log("Processing " + inFile + " to " + outFile, Project.MSG_INFO);
                configureLiaison(stylesheet);
                setLiaisonDynamicFileParameters(liaison, inFile);
                liaison.transform(inFile, outFile);
            } else {
                log("Skipping input file " + inFile + " because it is older than output file "
                        + outFile + " and so is the stylesheet " + stylesheet, Project.MSG_DEBUG);
            }
        } catch (Exception ex) {
            log("Failed to process " + inFile, Project.MSG_INFO);
            if (outFile != null) {
                outFile.delete();
            }
            handleTransformationError(ex);
        }
    }
    private void ensureDirectoryFor(File targetFile) throws BuildException {
        File directory = targetFile.getParentFile();
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                handleError("Unable to create directory: "
                            + directory.getAbsolutePath());
            }
        }
    }
    public Factory getFactory() {
        return factory;
    }
    public XMLCatalog getXMLCatalog() {
        xmlCatalog.setProject(getProject());
        return xmlCatalog;
    }
    public Enumeration getOutputProperties() {
        return outputProperties.elements();
    }
    protected XSLTLiaison getLiaison() {
        if (liaison == null) {
            if (processor != null) {
                try {
                    resolveProcessor(processor);
                } catch (Exception e) {
                    handleError(e);
                }
            } else {
                try {
                    resolveProcessor(PROCESSOR_TRAX);
                } catch (Throwable e1) {
                    e1.printStackTrace();
                    handleError(e1);
                }
            }
        }
        return liaison;
    }
    public Param createParam() {
        Param p = new Param();
        params.addElement(p);
        return p;
    }
    public static class Param {
        private String name = null;
        private String expression = null;
        private Object ifCond;
        private Object unlessCond;
        private Project project;
        public void setProject(Project project) {
            this.project = project;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setExpression(String expression) {
            this.expression = expression;
        }
        public String getName() throws BuildException {
            if (name == null) {
                throw new BuildException("Name attribute is missing.");
            }
            return name;
        }
        public String getExpression() throws BuildException {
            if (expression == null) {
                throw new BuildException("Expression attribute is missing.");
            }
            return expression;
        }
        public void setIf(Object ifCond) {
            this.ifCond = ifCond;
        }
        public void setIf(String ifProperty) {
            setIf((Object) ifProperty);
        }
        public void setUnless(Object unlessCond) {
            this.unlessCond = unlessCond;
        }
        public void setUnless(String unlessProperty) {
            setUnless((Object) unlessProperty);
        }
        public boolean shouldUse() {
            PropertyHelper ph = PropertyHelper.getPropertyHelper(project);
            return ph.testIfCondition(ifCond)
                && ph.testUnlessCondition(unlessCond);
        }
    } 
    public OutputProperty createOutputProperty() {
        OutputProperty p = new OutputProperty();
        outputProperties.addElement(p);
        return p;
    }
    public static class OutputProperty {
        private String name;
        private String value;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
    }
    public void init() throws BuildException {
        super.init();
        xmlCatalog.setProject(getProject());
    }
    protected void configureLiaison(File stylesheet) throws BuildException {
        FileResource fr = new FileResource();
        fr.setProject(getProject());
        fr.setFile(stylesheet);
        configureLiaison(fr);
    }
    protected void configureLiaison(Resource stylesheet) throws BuildException {
        if (stylesheetLoaded && reuseLoadedStylesheet) {
            return;
        }
        stylesheetLoaded = true;
        try {
            log("Loading stylesheet " + stylesheet, Project.MSG_INFO);
            if (liaison instanceof XSLTLiaison2) {
                ((XSLTLiaison2) liaison).configure(this);
            }
            if (liaison instanceof XSLTLiaison3) {
                ((XSLTLiaison3) liaison).setStylesheet(stylesheet);
            } else {
                FileProvider fp =
                    (FileProvider) stylesheet.as(FileProvider.class);
                if (fp != null) {
                    liaison.setStylesheet(fp.getFile());
                } else {
                    handleError(liaison.getClass().toString()
                                + " accepts the stylesheet only as a file");
                    return;
                }
            }
            for (Enumeration e = params.elements(); e.hasMoreElements();) {
                Param p = (Param) e.nextElement();
                if (p.shouldUse()) {
                    liaison.addParam(p.getName(), p.getExpression());
                }
            }
        } catch (Exception ex) {
            log("Failed to transform using stylesheet " + stylesheet, Project.MSG_INFO);
            handleTransformationError(ex);
        }
    }
    private void setLiaisonDynamicFileParameters(
        XSLTLiaison liaison, File inFile) throws Exception {
        if (fileNameParameter != null) {
            liaison.addParam(fileNameParameter, inFile.getName());
        }
        if (fileDirParameter != null) {
            String fileName = FileUtils.getRelativePath(baseDir, inFile);
            File file = new File(fileName);
            liaison.addParam(fileDirParameter, file.getParent() != null ? file.getParent().replace(
                    '\\', '/') : ".");
        }
    }
    public Factory createFactory() throws BuildException {
        if (factory != null) {
            handleError("'factory' element must be unique");
        } else {
            factory = new Factory();
        }
        return factory;
    }
    protected void handleError(String msg) {
        if (failOnError) {
            throw new BuildException(msg, getLocation());
        }
        log(msg, Project.MSG_WARN);
    }
    protected void handleError(Throwable ex) {
        if (failOnError) {
            throw new BuildException(ex);
        } else {
            log("Caught an exception: " + ex, Project.MSG_WARN);
        }
    }
    protected void handleTransformationError(Exception ex) {
        if (failOnError && failOnTransformationError) {
            throw new BuildException(ex);
        } else {
            log("Caught an error during transformation: " + ex,
                Project.MSG_WARN);
        }
    }
    public static class Factory {
        private String name;
        private Vector attributes = new Vector();
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void addAttribute(Attribute attr) {
            attributes.addElement(attr);
        }
        public Enumeration getAttributes() {
            return attributes.elements();
        }
        public static class Attribute implements DynamicConfigurator {
            private String name;
            private Object value;
            public String getName() {
                return name;
            }
            public Object getValue() {
                return value;
            }
            public Object createDynamicElement(String name) throws BuildException {
                return null;
            }
            public void setDynamicAttribute(String name, String value) throws BuildException {
                if ("name".equalsIgnoreCase(name)) {
                    this.name = value;
                } else if ("value".equalsIgnoreCase(name)) {
                    if ("true".equalsIgnoreCase(value)) {
                        this.value = Boolean.TRUE;
                    } else if ("false".equalsIgnoreCase(value)) {
                        this.value = Boolean.FALSE;
                    } else {
                        try {
                            this.value = new Integer(value);
                        } catch (NumberFormatException e) {
                            this.value = value;
                        }
                    }
                } else {
                    throw new BuildException("Unsupported attribute: " + name);
                }
            }
        } 
    } 
    private class StyleMapper implements FileNameMapper {
        public void setFrom(String from) {
        }
        public void setTo(String to) {
        }
        public String[] mapFileName(String xmlFile) {
            int dotPos = xmlFile.lastIndexOf('.');
            if (dotPos > 0) {
                xmlFile = xmlFile.substring(0, dotPos);
            }
            return new String[] {xmlFile + targetExtension};
        }
    }
    public final class TraceConfiguration {
        private boolean elements, extension, generation, selection, templates;
        public void setElements(boolean b) {
            elements = b;
        }
        public boolean getElements() {
            return elements;
        }
        public void setExtension(boolean b) {
            extension = b;
        }
        public boolean getExtension() {
            return extension;
        }
        public void setGeneration(boolean b) {
            generation = b;
        }
        public boolean getGeneration() {
            return generation;
        }
        public void setSelection(boolean b) {
            selection = b;
        }
        public boolean getSelection() {
            return selection;
        }
        public void setTemplates(boolean b) {
            templates = b;
        }
        public boolean getTemplates() {
            return templates;
        }
        public java.io.OutputStream getOutputStream() {
            return new LogOutputStream(XSLTProcess.this);
        }
    }
}
