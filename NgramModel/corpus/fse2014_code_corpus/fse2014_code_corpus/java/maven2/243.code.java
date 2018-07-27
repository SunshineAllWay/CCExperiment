package org.apache.maven.plugin;
import org.apache.maven.plugin.logging.Log;
public interface Mojo
{
    String ROLE = Mojo.class.getName();
    void execute()
        throws MojoExecutionException, MojoFailureException;
    void setLog( Log log );
    Log getLog();
}
