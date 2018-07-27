package org.apache.tools.ant.types.selectors.modifiedselector;
import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.ResourceUtils;
public class ModifiedSelector extends BaseExtendSelector
                              implements BuildListener, ResourceSelector {
    private static final String CACHE_PREFIX = "cache.";
    private static final String ALGORITHM_PREFIX = "algorithm.";
    private static final String COMPARATOR_PREFIX = "comparator.";
    private CacheName cacheName = null;
    private String cacheClass;
    private AlgorithmName algoName = null;
    private String algorithmClass;
    private ComparatorName compName = null;
    private String comparatorClass;
    private boolean update = true;
    private boolean selectDirectories = true;
    private boolean selectResourcesWithoutInputStream = true;
    private boolean delayUpdate = true;
    private Comparator comparator = null;
    private Algorithm algorithm = null;
    private Cache cache = null;
    private int modified = 0;
    private boolean isConfigured = false;
    private Vector configParameter = new Vector();
    private Vector specialParameter = new Vector();
    private ClassLoader myClassLoader = null;
    private Path classpath = null;
    public ModifiedSelector() {
    }
    public void verifySettings() {
        configure();
        if (cache == null) {
            setError("Cache must be set.");
        } else if (algorithm == null) {
            setError("Algorithm must be set.");
        } else if (!cache.isValid()) {
            setError("Cache must be proper configured.");
        } else if (!algorithm.isValid()) {
            setError("Algorithm must be proper configured.");
        }
    }
    public void configure() {
        if (isConfigured) {
            return;
        }
        isConfigured = true;
        Project p = getProject();
        String filename = "cache.properties";
        File cachefile = null;
        if (p != null) {
            cachefile = new File(p.getBaseDir(), filename);
            getProject().addBuildListener(this);
        } else {
            cachefile = new File(filename);
            setDelayUpdate(false);
        }
        Cache      defaultCache      = new PropertiesfileCache(cachefile);
        Algorithm  defaultAlgorithm  = new DigestAlgorithm();
        Comparator defaultComparator = new EqualComparator();
        for (Iterator itConfig = configParameter.iterator(); itConfig.hasNext();) {
            Parameter par = (Parameter) itConfig.next();
            if (par.getName().indexOf(".") > 0) {
                specialParameter.add(par);
            } else {
                useParameter(par);
            }
        }
        configParameter = new Vector();
        if (algoName != null) {
            if ("hashvalue".equals(algoName.getValue())) {
                algorithm = new HashvalueAlgorithm();
            } else if ("digest".equals(algoName.getValue())) {
                algorithm = new DigestAlgorithm();
            } else if ("checksum".equals(algoName.getValue())) {
                algorithm = new ChecksumAlgorithm();
            }
        } else {
            if (algorithmClass != null) {
                algorithm = (Algorithm) loadClass(
                    algorithmClass,
                    "is not an Algorithm.",
                    Algorithm.class);
            } else {
                algorithm = defaultAlgorithm;
            }
        }
        if (cacheName != null) {
            if ("propertyfile".equals(cacheName.getValue())) {
                cache = new PropertiesfileCache();
            }
        } else {
            if (cacheClass != null) {
                cache = (Cache) loadClass(cacheClass, "is not a Cache.", Cache.class);
            } else {
                cache = defaultCache;
            }
        }
        if (compName != null) {
            if ("equal".equals(compName.getValue())) {
                comparator = new EqualComparator();
             } else if ("rule".equals(compName.getValue())) {
                throw new BuildException("RuleBasedCollator not yet supported.");
            }
        } else {
            if (comparatorClass != null) {
                comparator = (Comparator) loadClass(
                    comparatorClass,
                    "is not a Comparator.",
                    Comparator.class);
            } else {
                comparator = defaultComparator;
            }
        }
        for (Iterator itSpecial = specialParameter.iterator(); itSpecial.hasNext();) {
            Parameter par = (Parameter) itSpecial.next();
            useParameter(par);
        }
        specialParameter = new Vector();
    }
    protected Object loadClass(String classname, String msg, Class type) {
        try {
            ClassLoader cl = getClassLoader();
            Class clazz = null;
            if (cl != null) {
                clazz = cl.loadClass(classname);
            } else {
                clazz = Class.forName(classname);
            }
            Object rv = clazz.newInstance();
            if (!type.isInstance(rv)) {
                throw new BuildException("Specified class (" + classname + ") " + msg);
            }
            return rv;
        } catch (ClassNotFoundException e) {
            throw new BuildException("Specified class (" + classname + ") not found.");
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
    public boolean isSelected(Resource resource) {
        if (resource.isFilesystemOnly()) {
            FileResource fileResource = (FileResource) resource;
            File file = fileResource.getFile();
            String filename = fileResource.getName();
            File basedir = fileResource.getBaseDir();
            return isSelected(basedir, filename, file);
        } else {
            try {
                FileUtils fu = FileUtils.getFileUtils();
                File tmpFile = fu.createTempFile("modified-", ".tmp", null, true, false);
                Resource tmpResource = new FileResource(tmpFile);
                ResourceUtils.copyResource(resource, tmpResource);
                boolean isSelected = isSelected(tmpFile.getParentFile(),
                                                tmpFile.getName(),
                                                resource.toLongString());
                tmpFile.delete();
                return isSelected;
            } catch (UnsupportedOperationException uoe) {
                log("The resource '"
                  + resource.getName()
                  + "' does not provide an InputStream, so it is not checked. "
                  + "Akkording to 'selres' attribute value it is "
                  + ((selectResourcesWithoutInputStream) ? "" : " not")
                  + "selected.", Project.MSG_INFO);
                return selectResourcesWithoutInputStream;
            } catch (Exception e) {
                throw new BuildException(e);
            }
        }
    }
    public boolean isSelected(File basedir, String filename, File file) {
        return isSelected(basedir, filename, file.getAbsolutePath());
    }
    private boolean isSelected(File basedir, String filename, String cacheKey) {
        validate();
        File f = new File(basedir, filename);
        if (f.isDirectory()) {
            return selectDirectories;
        }
        String cachedValue = String.valueOf(cache.get(f.getAbsolutePath()));
        String newValue = algorithm.getValue(f);
        boolean rv = (comparator.compare(cachedValue, newValue) != 0);
        if (update && rv) {
            cache.put(f.getAbsolutePath(), newValue);
            setModified(getModified() + 1);
            if (!getDelayUpdate()) {
                saveCache();
            }
        }
        return rv;
    }
    protected void saveCache() {
        if (getModified() > 0) {
            cache.save();
            setModified(0);
        }
    }
    public void setAlgorithmClass(String classname) {
        algorithmClass = classname;
    }
    public void setComparatorClass(String classname) {
        comparatorClass = classname;
    }
    public void setCacheClass(String classname) {
        cacheClass = classname;
    }
    public void setUpdate(boolean update) {
        this.update = update;
    }
    public void setSeldirs(boolean seldirs) {
        selectDirectories = seldirs;
    }
    public void setSelres(boolean newValue) {
        this.selectResourcesWithoutInputStream = newValue;
    }
    public int getModified() {
        return modified;
    }
    public void setModified(int modified) {
        this.modified = modified;
    }
    public boolean getDelayUpdate() {
        return delayUpdate;
    }
    public void setDelayUpdate(boolean delayUpdate) {
        this.delayUpdate = delayUpdate;
    }
    public void addClasspath(Path path) {
        if (classpath != null) {
            throw new BuildException("<classpath> can be set only once.");
        }
        classpath = path;
    }
    public ClassLoader getClassLoader() {
        if (myClassLoader == null) {
            myClassLoader = (classpath == null)
                ? getClass().getClassLoader()
                : getProject().createClassLoader(classpath);
        }
        return myClassLoader;
    }
    public void setClassLoader(ClassLoader loader) {
        myClassLoader = loader;
    }
    public void addParam(String key, Object value) {
        Parameter par = new Parameter();
        par.setName(key);
        par.setValue(String.valueOf(value));
        configParameter.add(par);
    }
    public void addParam(Parameter parameter) {
        configParameter.add(parameter);
    }
    public void setParameters(Parameter[] parameters) {
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                configParameter.add(parameters[i]);
            }
        }
    }
    public void useParameter(Parameter parameter) {
        String key = parameter.getName();
        String value = parameter.getValue();
        if ("cache".equals(key)) {
            CacheName cn = new CacheName();
            cn.setValue(value);
            setCache(cn);
        } else if ("algorithm".equals(key)) {
            AlgorithmName an = new AlgorithmName();
            an.setValue(value);
            setAlgorithm(an);
        } else if ("comparator".equals(key)) {
            ComparatorName cn = new ComparatorName();
            cn.setValue(value);
            setComparator(cn);
        } else if ("update".equals(key)) {
            boolean updateValue =
                ("true".equalsIgnoreCase(value))
                ? true
                : false;
            setUpdate(updateValue);
        } else if ("delayupdate".equals(key)) {
            boolean updateValue =
                ("true".equalsIgnoreCase(value))
                ? true
                : false;
            setDelayUpdate(updateValue);
        } else if ("seldirs".equals(key)) {
            boolean sdValue =
                ("true".equalsIgnoreCase(value))
                ? true
                : false;
            setSeldirs(sdValue);
        } else if (key.startsWith(CACHE_PREFIX)) {
            String name = key.substring(CACHE_PREFIX.length());
            tryToSetAParameter(cache, name, value);
        } else if (key.startsWith(ALGORITHM_PREFIX)) {
            String name = key.substring(ALGORITHM_PREFIX.length());
            tryToSetAParameter(algorithm, name, value);
        } else if (key.startsWith(COMPARATOR_PREFIX)) {
            String name = key.substring(COMPARATOR_PREFIX.length());
            tryToSetAParameter(comparator, name, value);
        } else {
            setError("Invalid parameter " + key);
        }
    }
    protected void tryToSetAParameter(Object obj, String name, String value) {
        Project prj = (getProject() != null) ? getProject() : new Project();
        IntrospectionHelper iHelper
            = IntrospectionHelper.getHelper(prj, obj.getClass());
        try {
            iHelper.setAttribute(prj, obj, name, value);
        } catch (org.apache.tools.ant.BuildException e) {
        }
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("{modifiedselector");
        buf.append(" update=").append(update);
        buf.append(" seldirs=").append(selectDirectories);
        buf.append(" cache=").append(cache);
        buf.append(" algorithm=").append(algorithm);
        buf.append(" comparator=").append(comparator);
        buf.append("}");
        return buf.toString();
    }
    public void buildFinished(BuildEvent event) {
        if (getDelayUpdate()) {
            saveCache();
        }
    }
    public void targetFinished(BuildEvent event) {
        if (getDelayUpdate()) {
            saveCache();
        }
    }
    public void taskFinished(BuildEvent event) {
        if (getDelayUpdate()) {
            saveCache();
        }
    }
    public void buildStarted(BuildEvent event) {
    }
    public void targetStarted(BuildEvent event) {
    }
    public void taskStarted(BuildEvent event) {
    }
    public void messageLogged(BuildEvent event) {
    }
    public Cache getCache() {
        return cache;
    }
    public void setCache(CacheName name) {
        cacheName = name;
    }
    public static class CacheName extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"propertyfile" };
        }
    }
    public Algorithm getAlgorithm() {
        return algorithm;
    }
    public void setAlgorithm(AlgorithmName name) {
        algoName = name;
    }
    public static class AlgorithmName extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"hashvalue", "digest", "checksum" };
        }
    }
    public Comparator getComparator() {
        return comparator;
    }
    public void setComparator(ComparatorName name) {
        compName = name;
    }
    public static class ComparatorName extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"equal", "rule" };
        }
    }
}
