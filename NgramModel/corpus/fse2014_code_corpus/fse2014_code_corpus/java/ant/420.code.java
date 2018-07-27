package org.apache.tools.ant.taskdefs.optional.ejb;
import java.io.File;
import java.util.Hashtable;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public class JbossDeploymentTool extends GenericDeploymentTool {
    protected static final String JBOSS_DD = "jboss.xml";
    protected static final String JBOSS_CMP10D = "jaws.xml";
    protected static final String JBOSS_CMP20D = "jbosscmp-jdbc.xml";
    private String jarSuffix = ".jar";
    public void setSuffix(String inString) {
        jarSuffix = inString;
    }
    protected void addVendorFiles(Hashtable ejbFiles, String ddPrefix) {
        File jbossDD = new File(getConfig().descriptorDir, ddPrefix + JBOSS_DD);
        if (jbossDD.exists()) {
            ejbFiles.put(META_DIR + JBOSS_DD, jbossDD);
        } else {
            log("Unable to locate jboss deployment descriptor. "
                + "It was expected to be in " + jbossDD.getPath(),
                Project.MSG_WARN);
            return;
        }
        String descriptorFileName = JBOSS_CMP10D;
        if (EjbJar.CMPVersion.CMP2_0.equals(getParent().getCmpversion())) {
            descriptorFileName = JBOSS_CMP20D;
        }
        File jbossCMPD
            = new File(getConfig().descriptorDir, ddPrefix + descriptorFileName);
        if (jbossCMPD.exists()) {
            ejbFiles.put(META_DIR + descriptorFileName, jbossCMPD);
        } else {
            log("Unable to locate jboss cmp descriptor. "
                + "It was expected to be in "
                + jbossCMPD.getPath(), Project.MSG_VERBOSE);
            return;
        }
    }
    File getVendorOutputJarFile(String baseName) {
        if (getDestDir() == null && getParent().getDestdir() == null) {
            throw new BuildException("DestDir not specified");
        }
        if (getDestDir() == null) {
            return new File(getParent().getDestdir(), baseName + jarSuffix);
        } else {
            return new File(getDestDir(), baseName + jarSuffix);
        }
    }
    public void validateConfigured() throws BuildException {
    }
    private EjbJar getParent() {
        return (EjbJar) this.getTask();
    }
}
