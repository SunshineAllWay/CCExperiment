package org.apache.tools.ant.taskdefs.optional.ejb;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.xml.sax.SAXException;
public class EjbJar extends MatchingTask {
    public static class DTDLocation
        extends org.apache.tools.ant.types.DTDLocation {
    }
    static class Config {
        public File srcDir;
        public File descriptorDir;
        public String baseNameTerminator = "-";
        public String baseJarName;
        public boolean flatDestDir = false;
        public Path classpath;
        public List supportFileSets = new ArrayList();
        public ArrayList dtdLocations = new ArrayList();
        public NamingScheme namingScheme;
        public File manifest;
        public String analyzer;
    }
    public static class NamingScheme extends EnumeratedAttribute {
        public static final String EJB_NAME = "ejb-name";
        public static final String DIRECTORY = "directory";
        public static final String DESCRIPTOR = "descriptor";
        public static final String BASEJARNAME = "basejarname";
        public String[] getValues() {
            return new String[] {EJB_NAME, DIRECTORY, DESCRIPTOR, BASEJARNAME};
        }
    }
    public static class CMPVersion extends EnumeratedAttribute {
        public static final String CMP1_0 = "1.0";
        public static final String CMP2_0 = "2.0";
        public String[] getValues() {
            return new String[]{
                CMP1_0,
                CMP2_0,
            };
        }
    }
    private Config config = new Config();
    private File destDir;
    private String genericJarSuffix = "-generic.jar";
    private String cmpVersion = CMPVersion.CMP1_0;
    private ArrayList deploymentTools = new ArrayList();
    protected void addDeploymentTool(EJBDeploymentTool deploymentTool) {
        deploymentTool.setTask(this);
        deploymentTools.add(deploymentTool);
    }
    public WeblogicDeploymentTool createWeblogic() {
        WeblogicDeploymentTool tool = new WeblogicDeploymentTool();
        addDeploymentTool(tool);
        return tool;
    }
    public WebsphereDeploymentTool createWebsphere() {
        WebsphereDeploymentTool tool = new WebsphereDeploymentTool();
        addDeploymentTool(tool);
        return tool;
    }
    public BorlandDeploymentTool createBorland() {
        log("Borland deployment tools",  Project.MSG_VERBOSE);
        BorlandDeploymentTool tool = new BorlandDeploymentTool();
        tool.setTask(this);
        deploymentTools.add(tool);
        return tool;
    }
    public IPlanetDeploymentTool createIplanet() {
        log("iPlanet Application Server deployment tools", Project.MSG_VERBOSE);
        IPlanetDeploymentTool tool = new IPlanetDeploymentTool();
        addDeploymentTool(tool);
        return tool;
    }
    public JbossDeploymentTool createJboss() {
        JbossDeploymentTool tool = new JbossDeploymentTool();
        addDeploymentTool(tool);
        return tool;
    }
    public JonasDeploymentTool createJonas() {
        log("JOnAS deployment tools",  Project.MSG_VERBOSE);
        JonasDeploymentTool tool = new JonasDeploymentTool();
        addDeploymentTool(tool);
        return tool;
    }
    public WeblogicTOPLinkDeploymentTool createWeblogictoplink() {
        log("The <weblogictoplink> element is no longer required. Please use "
            + "the <weblogic> element and set newCMP=\"true\"",
            Project.MSG_INFO);
        WeblogicTOPLinkDeploymentTool tool
            = new WeblogicTOPLinkDeploymentTool();
        addDeploymentTool(tool);
        return tool;
    }
    public Path createClasspath() {
        if (config.classpath == null) {
            config.classpath = new Path(getProject());
        }
        return config.classpath.createPath();
    }
    public DTDLocation createDTD() {
        DTDLocation dtdLocation = new DTDLocation();
        config.dtdLocations.add(dtdLocation);
        return dtdLocation;
    }
    public FileSet createSupport() {
        FileSet supportFileSet = new FileSet();
        config.supportFileSets.add(supportFileSet);
        return supportFileSet;
    }
     public void setManifest(File manifest) {
         config.manifest = manifest;
     }
    public void setSrcdir(File inDir) {
        config.srcDir = inDir;
    }
    public void setDescriptordir(File inDir) {
        config.descriptorDir = inDir;
    }
    public void setDependency(String analyzer) {
        config.analyzer = analyzer;
    }
    public void setBasejarname(String inValue) {
        config.baseJarName = inValue;
        if (config.namingScheme == null) {
            config.namingScheme = new NamingScheme();
            config.namingScheme.setValue(NamingScheme.BASEJARNAME);
        } else if (!config.namingScheme.getValue().equals(NamingScheme.BASEJARNAME)) {
            throw new BuildException("The basejarname attribute is not "
                + "compatible with the "
                + config.namingScheme.getValue() + " naming scheme");
        }
    }
    public void setNaming(NamingScheme namingScheme) {
        config.namingScheme = namingScheme;
        if (!config.namingScheme.getValue().equals(NamingScheme.BASEJARNAME)
            && config.baseJarName != null) {
            throw new BuildException("The basejarname attribute is not "
                + "compatible with the "
                + config.namingScheme.getValue() + " naming scheme");
        }
    }
    public File getDestdir() {
        return this.destDir;
    }
    public void setDestdir(File inDir) {
        this.destDir = inDir;
    }
    public String getCmpversion() {
        return this.cmpVersion;
    }
    public void setCmpversion(CMPVersion version) {
        this.cmpVersion = version.getValue();
    }
    public void setClasspath(Path classpath) {
        config.classpath = classpath;
    }
    public void setFlatdestdir(boolean inValue) {
        config.flatDestDir = inValue;
    }
    public void setGenericjarsuffix(String inString) {
        this.genericJarSuffix = inString;
    }
    public void setBasenameterminator(String inValue) {
        config.baseNameTerminator = inValue;
    }
    private void validateConfig() throws BuildException {
        if (config.srcDir == null) {
            throw new BuildException("The srcDir attribute must be specified");
        }
        if (config.descriptorDir == null) {
            config.descriptorDir = config.srcDir;
        }
        if (config.namingScheme == null) {
            config.namingScheme = new NamingScheme();
            config.namingScheme.setValue(NamingScheme.DESCRIPTOR);
        } else if (config.namingScheme.getValue().equals(NamingScheme.BASEJARNAME)
                    && config.baseJarName == null) {
            throw new BuildException("The basejarname attribute must "
                + "be specified with the basejarname naming scheme");
        }
    }
    public void execute() throws BuildException {
        validateConfig();
        if (deploymentTools.size() == 0) {
            GenericDeploymentTool genericTool = new GenericDeploymentTool();
            genericTool.setTask(this);
            genericTool.setDestdir(destDir);
            genericTool.setGenericJarSuffix(genericJarSuffix);
            deploymentTools.add(genericTool);
        }
        for (Iterator i = deploymentTools.iterator(); i.hasNext();) {
            EJBDeploymentTool tool = (EJBDeploymentTool) i.next();
            tool.configure(config);
            tool.validateConfigured();
        }
        try {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setValidating(true);
            SAXParser saxParser = saxParserFactory.newSAXParser();
            DirectoryScanner ds = getDirectoryScanner(config.descriptorDir);
            ds.scan();
            String[] files = ds.getIncludedFiles();
            log(files.length + " deployment descriptors located.",
                Project.MSG_VERBOSE);
            for (int index = 0; index < files.length; ++index) {
                for (Iterator i = deploymentTools.iterator(); i.hasNext();) {
                    EJBDeploymentTool tool = (EJBDeploymentTool) i.next();
                    tool.processDescriptor(files[index], saxParser);
                }
            }
        } catch (SAXException se) {
            String msg = "SAXException while creating parser."
                + "  Details: "
                + se.getMessage();
            throw new BuildException(msg, se);
        } catch (ParserConfigurationException pce) {
            String msg = "ParserConfigurationException while creating parser. "
                       + "Details: " + pce.getMessage();
            throw new BuildException(msg, pce);
        }
    } 
}
