package org.apache.tools.ant.helper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ExtensionPoint;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.URLProvider;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JAXPUtils;
import org.apache.tools.zip.ZipFile;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
public class ProjectHelper2 extends ProjectHelper {
    public static final String REFID_TARGETS = "ant.targets";
    private static AntHandler elementHandler = new ElementHandler();
    private static AntHandler targetHandler = new TargetHandler();
    private static AntHandler mainHandler = new MainHandler();
    private static AntHandler projectHandler = new ProjectHandler();
    private static final String REFID_CONTEXT = "ant.parsing.context";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public boolean canParseAntlibDescriptor(Resource resource) {
        return true;
    }
    public UnknownElement parseAntlibDescriptor(Project containingProject,
                                                Resource resource) {
        URLProvider up = (URLProvider) resource.as(URLProvider.class);
        if (up == null) {
            throw new BuildException("Unsupported resource type: " + resource);
        }
        return parseUnknownElement(containingProject, up.getURL());
    }
    public UnknownElement parseUnknownElement(Project project, URL source)
        throws BuildException {
        Target dummyTarget = new Target();
        dummyTarget.setProject(project);
        AntXMLContext context = new AntXMLContext(project);
        context.addTarget(dummyTarget);
        context.setImplicitTarget(dummyTarget);
        parse(context.getProject(), source, new RootHandler(context, elementHandler));
        Task[] tasks = dummyTarget.getTasks();
        if (tasks.length != 1) {
            throw new BuildException("No tasks defined");
        }
        return (UnknownElement) tasks[0];
    }
    public void parse(Project project, Object source) throws BuildException {
        getImportStack().addElement(source);
        AntXMLContext context = null;
        context = (AntXMLContext) project.getReference(REFID_CONTEXT);
        if (context == null) {
            context = new AntXMLContext(project);
            project.addReference(REFID_CONTEXT, context);
            project.addReference(REFID_TARGETS, context.getTargets());
        }
        if (getImportStack().size() > 1) {
            context.setIgnoreProjectTag(true);
            Target currentTarget = context.getCurrentTarget();
            Target currentImplicit = context.getImplicitTarget();
            Map    currentTargets = context.getCurrentTargets();
            try {
                Target newCurrent = new Target();
                newCurrent.setProject(project);
                newCurrent.setName("");
                context.setCurrentTarget(newCurrent);
                context.setCurrentTargets(new HashMap());
                context.setImplicitTarget(newCurrent);
                parse(project, source, new RootHandler(context, mainHandler));
                newCurrent.execute();
            } finally {
                context.setCurrentTarget(currentTarget);
                context.setImplicitTarget(currentImplicit);
                context.setCurrentTargets(currentTargets);
            }
        } else {
            context.setCurrentTargets(new HashMap());
            parse(project, source, new RootHandler(context, mainHandler));
            context.getImplicitTarget().execute();
            for (Iterator i = getExtensionStack().iterator(); i.hasNext(); ) {
                String[] extensionInfo = (String[]) i.next();
                String tgName = extensionInfo[0];
                String name = extensionInfo[1];
                OnMissingExtensionPoint missingBehaviour = OnMissingExtensionPoint
                        .valueOf(extensionInfo[2]);
                Hashtable projectTargets = project.getTargets();
                if (!projectTargets.containsKey(tgName)) {
                    String message = "can't add target " + name
                        + " to extension-point " + tgName
                        + " because the extension-point is unknown.";
                    if (missingBehaviour == OnMissingExtensionPoint.FAIL) {
                        throw new BuildException(message);
                    } else if (missingBehaviour == OnMissingExtensionPoint.WARN) {
                        Target target = (Target) projectTargets.get(name);
                        context.getProject().log(target,
                                                 "Warning: " + message,
                                                 Project.MSG_WARN);
                    }
                } else {
                    Target t = (Target) projectTargets.get(tgName);
                    if (!(t instanceof ExtensionPoint)) {
                        throw new BuildException("referenced target "
                                                 + tgName
                                                 + " is not an extension-point");
                    }
                    t.addDependency(name);
                }
            }
        }
    }
    public void parse(Project project, Object source, RootHandler handler) throws BuildException {
        AntXMLContext context = handler.context;
        File buildFile = null;
        URL  url = null;
        String buildFileName = null;
        if (source instanceof File) {
            buildFile = (File) source;
        } else if (source instanceof URL) {
            url = (URL) source;
        } else if (source instanceof Resource) {
            FileProvider fp =
                (FileProvider) ((Resource) source).as(FileProvider.class);
            if (fp != null) {
                buildFile = fp.getFile();
            } else {
                URLProvider up =
                    (URLProvider) ((Resource) source).as(URLProvider.class);
                if (up != null) {
                    url = up.getURL();
                }
            }
        }
        if (buildFile != null) {
            buildFile = FILE_UTILS.normalize(buildFile.getAbsolutePath());
            context.setBuildFile(buildFile);
            buildFileName = buildFile.toString();
        } else if (url != null) {
            try {
                context.setBuildFile((File) null);
                context.setBuildFile(url);
            } catch (java.net.MalformedURLException ex) {
                throw new BuildException(ex);
            }
            buildFileName = url.toString();
        } else {
            throw new BuildException("Source " + source.getClass().getName()
                                     + " not supported by this plugin");
        }
        InputStream inputStream = null;
        InputSource inputSource = null;
        ZipFile zf = null;
        try {
            XMLReader parser = JAXPUtils.getNamespaceXMLReader();
            String uri = null;
            if (buildFile != null) {
                uri = FILE_UTILS.toURI(buildFile.getAbsolutePath());
                inputStream = new FileInputStream(buildFile);
            } else {
                uri = url.toString();
                int pling = -1;
                if (uri.startsWith("jar:file")
                    && (pling = uri.indexOf("!/")) > -1) {
                    zf = new ZipFile(org.apache.tools.ant.launch.Locator
                                     .fromJarURI(uri), "UTF-8");
                    inputStream =
                        zf.getInputStream(zf.getEntry(uri.substring(pling + 1)));
                } else {
                    inputStream = url.openStream();
                }
            }
            inputSource = new InputSource(inputStream);
            if (uri != null) {
                inputSource.setSystemId(uri);
            }
            project.log("parsing buildfile " + buildFileName + " with URI = "
                        + uri + (zf != null ? " from a zip file" : ""),
                        Project.MSG_VERBOSE);
            DefaultHandler hb = handler;
            parser.setContentHandler(hb);
            parser.setEntityResolver(hb);
            parser.setErrorHandler(hb);
            parser.setDTDHandler(hb);
            parser.parse(inputSource);
        } catch (SAXParseException exc) {
            Location location = new Location(exc.getSystemId(), exc.getLineNumber(), exc
                                             .getColumnNumber());
            Throwable t = exc.getException();
            if (t instanceof BuildException) {
                BuildException be = (BuildException) t;
                if (be.getLocation() == Location.UNKNOWN_LOCATION) {
                    be.setLocation(location);
                }
                throw be;
            }
            throw new BuildException(exc.getMessage(), t == null ? exc : t, location);
        } catch (SAXException exc) {
            Throwable t = exc.getException();
            if (t instanceof BuildException) {
                throw (BuildException) t;
            }
            throw new BuildException(exc.getMessage(), t == null ? exc : t);
        } catch (FileNotFoundException exc) {
            throw new BuildException(exc);
        } catch (UnsupportedEncodingException exc) {
            throw new BuildException("Encoding of project file " + buildFileName + " is invalid.",
                                     exc);
        } catch (IOException exc) {
            throw new BuildException("Error reading project file " + buildFileName + ": "
                                     + exc.getMessage(), exc);
        } finally {
            FileUtils.close(inputStream);
            ZipFile.closeQuietly(zf);
        }
    }
    protected static AntHandler getMainHandler() {
        return mainHandler;
    }
    protected static void setMainHandler(AntHandler handler) {
        mainHandler = handler;
    }
    protected static AntHandler getProjectHandler() {
        return projectHandler;
    }
    protected static void setProjectHandler(AntHandler handler) {
        projectHandler = handler;
    }
    protected static AntHandler getTargetHandler() {
        return targetHandler;
    }
    protected static void setTargetHandler(AntHandler handler) {
        targetHandler = handler;
    }
    protected static AntHandler getElementHandler() {
        return elementHandler;
    }
    protected static void setElementHandler(AntHandler handler) {
        elementHandler = handler;
    }
    public static class AntHandler  {
        public void onStartElement(String uri, String tag, String qname, Attributes attrs,
                                   AntXMLContext context) throws SAXParseException {
        }
        public AntHandler onStartChild(String uri, String tag, String qname, Attributes attrs,
                                       AntXMLContext context) throws SAXParseException {
            throw new SAXParseException("Unexpected element \"" + qname + " \"", context
                                        .getLocator());
        }
        public void onEndChild(String uri, String tag, String qname, AntXMLContext context)
            throws SAXParseException {
        }
        public void onEndElement(String uri, String tag, AntXMLContext context) {
        }
        public void characters(char[] buf, int start, int count, AntXMLContext context)
            throws SAXParseException {
            String s = new String(buf, start, count).trim();
            if (s.length() > 0) {
                throw new SAXParseException("Unexpected text \"" + s + "\"", context.getLocator());
            }
        }
        protected void checkNamespace(String uri) {
        }
    }
    public static class RootHandler extends DefaultHandler {
        private Stack antHandlers = new Stack();
        private AntHandler currentHandler = null;
        private AntXMLContext context;
        public RootHandler(AntXMLContext context, AntHandler rootHandler) {
            currentHandler = rootHandler;
            antHandlers.push(currentHandler);
            this.context = context;
        }
        public AntHandler getCurrentAntHandler() {
            return currentHandler;
        }
        public InputSource resolveEntity(String publicId, String systemId) {
            context.getProject().log("resolving systemId: " + systemId, Project.MSG_VERBOSE);
            if (systemId.startsWith("file:")) {
                String path = FILE_UTILS.fromURI(systemId);
                File file = new File(path);
                if (!file.isAbsolute()) {
                    file = FILE_UTILS.resolveFile(context.getBuildFileParent(), path);
                    context.getProject().log(
                                             "Warning: '" + systemId + "' in " + context.getBuildFile()
                                             + " should be expressed simply as '" + path.replace('\\', '/')
                                             + "' for compliance with other XML tools", Project.MSG_WARN);
                }
                context.getProject().log("file=" + file, Project.MSG_DEBUG);
                try {
                    InputSource inputSource = new InputSource(new FileInputStream(file));
                    inputSource.setSystemId(FILE_UTILS.toURI(file.getAbsolutePath()));
                    return inputSource;
                } catch (FileNotFoundException fne) {
                    context.getProject().log(file.getAbsolutePath() + " could not be found",
                                             Project.MSG_WARN);
                }
            }
            context.getProject().log("could not resolve systemId", Project.MSG_DEBUG);
            return null;
        }
        public void startElement(String uri, String tag, String qname, Attributes attrs)
            throws SAXParseException {
            AntHandler next = currentHandler.onStartChild(uri, tag, qname, attrs, context);
            antHandlers.push(currentHandler);
            currentHandler = next;
            currentHandler.onStartElement(uri, tag, qname, attrs, context);
        }
        public void setDocumentLocator(Locator locator) {
            context.setLocator(locator);
        }
        public void endElement(String uri, String name, String qName) throws SAXException {
            currentHandler.onEndElement(uri, name, context);
            AntHandler prev = (AntHandler) antHandlers.pop();
            currentHandler = prev;
            if (currentHandler != null) {
                currentHandler.onEndChild(uri, name, qName, context);
            }
        }
        public void characters(char[] buf, int start, int count) throws SAXParseException {
            currentHandler.characters(buf, start, count, context);
        }
        public void startPrefixMapping(String prefix, String uri) {
            context.startPrefixMapping(prefix, uri);
        }
        public void endPrefixMapping(String prefix) {
            context.endPrefixMapping(prefix);
        }
    }
    public static class MainHandler extends AntHandler {
        public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs,
                                       AntXMLContext context) throws SAXParseException {
            if (name.equals("project")
                && (uri.equals("") || uri.equals(ANT_CORE_URI))) {
                return ProjectHelper2.projectHandler;
            }
            if (name.equals(qname)) {
                throw new SAXParseException("Unexpected element \"{" + uri
                                            + "}" + name + "\" {" + ANT_CORE_URI + "}" + name, context.getLocator());
            }
            throw new SAXParseException("Unexpected element \"" + qname
                                        + "\" " + name, context.getLocator());
        }
    }
    public static class ProjectHandler extends AntHandler {
        public void onStartElement(String uri, String tag, String qname, Attributes attrs,
                                   AntXMLContext context) throws SAXParseException {
            String baseDir = null;
            boolean nameAttributeSet = false;
            Project project = context.getProject();
            context.getImplicitTarget().setLocation(new Location(context.getLocator()));
            for (int i = 0; i < attrs.getLength(); i++) {
                String attrUri = attrs.getURI(i);
                if (attrUri != null && !attrUri.equals("") && !attrUri.equals(uri)) {
                    continue; 
                }
                String key = attrs.getLocalName(i);
                String value = attrs.getValue(i);
                if (key.equals("default")) {
                    if (value != null && !value.equals("")) {
                        if (!context.isIgnoringProjectTag()) {
                            project.setDefault(value);
                        }
                    }
                } else if (key.equals("name")) {
                    if (value != null) {
                        context.setCurrentProjectName(value);
                        nameAttributeSet = true;
                        if (!context.isIgnoringProjectTag()) {
                            project.setName(value);
                            project.addReference(value, project);
                        } else if (isInIncludeMode()) {
                            if (!"".equals(value)
                                && (getCurrentTargetPrefix() == null
                                    || getCurrentTargetPrefix().length() == 0)
                                ) {
                                setCurrentTargetPrefix(value);
                            }
                        }
                    }
                } else if (key.equals("id")) {
                    if (value != null) {
                        if (!context.isIgnoringProjectTag()) {
                            project.addReference(value, project);
                        }
                    }
                } else if (key.equals("basedir")) {
                    if (!context.isIgnoringProjectTag()) {
                        baseDir = value;
                    }
                } else {
                    throw new SAXParseException("Unexpected attribute \"" + attrs.getQName(i)
                                                + "\"", context.getLocator());
                }
            }
            String antFileProp =
                MagicNames.ANT_FILE + "." + context.getCurrentProjectName();
            String dup = project.getProperty(antFileProp);
            String typeProp =
                MagicNames.ANT_FILE_TYPE + "." + context.getCurrentProjectName();
            String dupType = project.getProperty(typeProp);
            if (dup != null && nameAttributeSet) {
                Object dupFile = null;
                Object contextFile = null;
                if (MagicNames.ANT_FILE_TYPE_URL.equals(dupType)) {
                    try {
                        dupFile = new URL(dup);
                    } catch (java.net.MalformedURLException mue) {
                        throw new BuildException("failed to parse "
                                                 + dup + " as URL while looking"
                                                 + " at a duplicate project"
                                                 + " name.", mue);
                    }
                    contextFile = context.getBuildFileURL();
                } else {
                    dupFile = new File(dup);
                    contextFile = context.getBuildFile();
                }
                if (context.isIgnoringProjectTag() && !dupFile.equals(contextFile)) {
                    project.log("Duplicated project name in import. Project "
                                + context.getCurrentProjectName() + " defined first in " + dup
                                + " and again in " + contextFile, Project.MSG_WARN);
                }
            }
            if (nameAttributeSet) {
                if (context.getBuildFile() != null) {
                    project.setUserProperty(antFileProp,
                                            context.getBuildFile().toString());
                    project.setUserProperty(typeProp,
                                            MagicNames.ANT_FILE_TYPE_FILE);
                } else if (context.getBuildFileURL() != null) {
                    project.setUserProperty(antFileProp,
                                            context.getBuildFileURL().toString());
                    project.setUserProperty(typeProp,
                                            MagicNames.ANT_FILE_TYPE_URL);
                }
            }
            if (context.isIgnoringProjectTag()) {
                return;
            }
            if (project.getProperty("basedir") != null) {
                project.setBasedir(project.getProperty("basedir"));
            } else {
                if (baseDir == null) {
                    project.setBasedir(context.getBuildFileParent().getAbsolutePath());
                } else {
                    if ((new File(baseDir)).isAbsolute()) {
                        project.setBasedir(baseDir);
                    } else {
                        project.setBaseDir(FILE_UTILS.resolveFile(context.getBuildFileParent(),
                                                                  baseDir));
                    }
                }
            }
            project.addTarget("", context.getImplicitTarget());
            context.setCurrentTarget(context.getImplicitTarget());
        }
        public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs,
                                       AntXMLContext context) throws SAXParseException {
            return (name.equals("target") || name.equals("extension-point"))
                && (uri.equals("") || uri.equals(ANT_CORE_URI))
                ? ProjectHelper2.targetHandler : ProjectHelper2.elementHandler;
        }
    }
    public static class TargetHandler extends AntHandler {
        public void onStartElement(String uri, String tag, String qname, Attributes attrs,
                                   AntXMLContext context) throws SAXParseException {
            String name = null;
            String depends = "";
            String extensionPoint = null;
            OnMissingExtensionPoint extensionPointMissing = null;
            Project project = context.getProject();
            Target target = "target".equals(tag)
                ? new Target() : new ExtensionPoint();
            target.setProject(project);
            target.setLocation(new Location(context.getLocator()));
            context.addTarget(target);
            for (int i = 0; i < attrs.getLength(); i++) {
                String attrUri = attrs.getURI(i);
                if (attrUri != null && !attrUri.equals("") && !attrUri.equals(uri)) {
                    continue; 
                }
                String key = attrs.getLocalName(i);
                String value = attrs.getValue(i);
                if (key.equals("name")) {
                    name = value;
                    if ("".equals(name)) {
                        throw new BuildException("name attribute must " + "not be empty");
                    }
                } else if (key.equals("depends")) {
                    depends = value;
                } else if (key.equals("if")) {
                    target.setIf(value);
                } else if (key.equals("unless")) {
                    target.setUnless(value);
                } else if (key.equals("id")) {
                    if (value != null && !value.equals("")) {
                        context.getProject().addReference(value, target);
                    }
                } else if (key.equals("description")) {
                    target.setDescription(value);
                } else if (key.equals("extensionOf")) {
                    extensionPoint = value;
                } else if (key.equals("onMissingExtensionPoint")) {
                    try {
                        extensionPointMissing = OnMissingExtensionPoint.valueOf(value);
                    } catch (IllegalArgumentException e) {
                        throw new BuildException("Invalid onMissingExtensionPoint " + value);
                    }
                } else {
                    throw new SAXParseException("Unexpected attribute \"" + key + "\"", context
                                                .getLocator());
                }
            }
            if (name == null) {
                throw new SAXParseException("target element appears without a name attribute",
                                            context.getLocator());
            }
            String prefix = null;
            boolean isInIncludeMode =
                context.isIgnoringProjectTag() && isInIncludeMode();
            String sep = getCurrentPrefixSeparator();
            if (isInIncludeMode) {
                prefix = getTargetPrefix(context);
                if (prefix == null) {
                    throw new BuildException("can't include build file "
                                             + context.getBuildFileURL()
                                             + ", no as attribute has been given"
                                             + " and the project tag doesn't"
                                             + " specify a name attribute");
                }
                name = prefix + sep + name;
            }
            if (context.getCurrentTargets().get(name) != null) {
                throw new BuildException("Duplicate target '" + name + "'",
                                         target.getLocation());
            }
            Hashtable projectTargets = project.getTargets();
            boolean   usedTarget = false;
            if (projectTargets.containsKey(name)) {
                project.log("Already defined in main or a previous import, ignore " + name,
                            Project.MSG_VERBOSE);
            } else {
                target.setName(name);
                context.getCurrentTargets().put(name, target);
                project.addOrReplaceTarget(name, target);
                usedTarget = true;
            }
            if (depends.length() > 0) {
                if (!isInIncludeMode) {
                    target.setDepends(depends);
                } else {
                    for (Iterator iter =
                             Target.parseDepends(depends, name, "depends")
                             .iterator();
                         iter.hasNext(); ) {
                        target.addDependency(prefix + sep + iter.next());
                    }
                }
            }
            if (!isInIncludeMode && context.isIgnoringProjectTag()
                && (prefix = getTargetPrefix(context)) != null) {
                String newName = prefix + sep + name;
                Target newTarget = usedTarget ? new Target(target) : target;
                newTarget.setName(newName);
                context.getCurrentTargets().put(newName, newTarget);
                project.addOrReplaceTarget(newName, newTarget);
            }
            if (extensionPointMissing != null && extensionPoint == null) {
                throw new BuildException("onMissingExtensionPoint attribute cannot " +
                                         "be specified unless extensionOf is specified", 
                                         target.getLocation());
            }
            if (extensionPoint != null) {
                ProjectHelper helper =
                    (ProjectHelper) context.getProject().
                    getReference(ProjectHelper.PROJECTHELPER_REFERENCE);
                for (Iterator iter =
                         Target.parseDepends(extensionPoint, name, "extensionOf")
                         .iterator();
                     iter.hasNext(); ) {
                    String tgName = (String) iter.next();
                    if (isInIncludeMode()) {
                        tgName = prefix + sep + tgName;
                    }
                    if (extensionPointMissing == null) {
                        extensionPointMissing = OnMissingExtensionPoint.FAIL;
                    }
                    helper.getExtensionStack().add(new String[] {
                            tgName, name, extensionPointMissing.name() });
                }
            }
        }
        private String getTargetPrefix(AntXMLContext context) {
            String configuredValue = getCurrentTargetPrefix();
            if (configuredValue != null && configuredValue.length() == 0) {
                configuredValue = null;
            }
            if (configuredValue != null) {
                return configuredValue;
            }
            String projectName = context.getCurrentProjectName();
            if ("".equals(projectName)) {
                projectName = null;
            }
            return projectName;
        }
        public AntHandler onStartChild(String uri, String name, String qname, Attributes attrs,
                                       AntXMLContext context) throws SAXParseException {
            return ProjectHelper2.elementHandler;
        }
        public void onEndElement(String uri, String tag, AntXMLContext context) {
            context.setCurrentTarget(context.getImplicitTarget());
        }
    }
    public static class ElementHandler extends AntHandler {
        public ElementHandler() {
        }
        public void onStartElement(String uri, String tag, String qname, Attributes attrs,
                                   AntXMLContext context) throws SAXParseException {
            RuntimeConfigurable parentWrapper = context.currentWrapper();
            Object parent = null;
            if (parentWrapper != null) {
                parent = parentWrapper.getProxy();
            }
            UnknownElement task = new UnknownElement(tag);
            task.setProject(context.getProject());
            task.setNamespace(uri);
            task.setQName(qname);
            task.setTaskType(ProjectHelper.genComponentName(task.getNamespace(), tag));
            task.setTaskName(qname);
            Location location = new Location(context.getLocator().getSystemId(), context
                                             .getLocator().getLineNumber(), context.getLocator().getColumnNumber());
            task.setLocation(location);
            task.setOwningTarget(context.getCurrentTarget());
            if (parent != null) {
                ((UnknownElement) parent).addChild(task);
            }  else {
                context.getCurrentTarget().addTask(task);
            }
            context.configureId(task, attrs);
            RuntimeConfigurable wrapper = new RuntimeConfigurable(task, task.getTaskName());
            for (int i = 0; i < attrs.getLength(); i++) {
                String name = attrs.getLocalName(i);
                String attrUri = attrs.getURI(i);
                if (attrUri != null && !attrUri.equals("") && !attrUri.equals(uri)) {
                    name = attrUri + ":" + attrs.getQName(i);
                }
                String value = attrs.getValue(i);
                if (ANT_TYPE.equals(name)
                    || (ANT_CORE_URI.equals(attrUri)
                        && ANT_TYPE.equals(attrs.getLocalName(i)))) {
                    name = ANT_TYPE;
                    int index = value.indexOf(":");
                    if (index >= 0) {
                        String prefix = value.substring(0, index);
                        String mappedUri = context.getPrefixMapping(prefix);
                        if (mappedUri == null) {
                            throw new BuildException("Unable to find XML NS prefix \"" + prefix
                                                     + "\"");
                        }
                        value = ProjectHelper.genComponentName(mappedUri, value
                                                               .substring(index + 1));
                    }
                }
                wrapper.setAttribute(name, value);
            }
            if (parentWrapper != null) {
                parentWrapper.addChild(wrapper);
            }
            context.pushWrapper(wrapper);
        }
        public void characters(char[] buf, int start, int count,
                               AntXMLContext context) throws SAXParseException {
            RuntimeConfigurable wrapper = context.currentWrapper();
            wrapper.addText(buf, start, count);
        }
        public AntHandler onStartChild(String uri, String tag, String qname, Attributes attrs,
                                       AntXMLContext context) throws SAXParseException {
            return ProjectHelper2.elementHandler;
        }
        public void onEndElement(String uri, String tag, AntXMLContext context) {
            context.popWrapper();
        }
    }
}
