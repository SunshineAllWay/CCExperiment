package org.apache.tools.ant.taskdefs.optional.ejb;
import javax.xml.parsers.SAXParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
public interface EJBDeploymentTool {
    void processDescriptor(String descriptorFilename, SAXParser saxParser)
        throws BuildException;
    void validateConfigured() throws BuildException;
    void setTask(Task task);
    void configure(EjbJar.Config config);
}
