package org.apache.maven;
import org.codehaus.plexus.util.dag.CycleDetectedException;
public class ProjectCycleException
    extends BuildFailureException
{
    public ProjectCycleException( String message, CycleDetectedException cause )
    {
        super( message, cause );
    }
}
