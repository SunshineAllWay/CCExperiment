package org.apache.maven.lifecycle.internal;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
public class PhaseRecorder
{
    private String lastLifecyclePhase;
    private final MavenProject project;
    public PhaseRecorder( MavenProject project )
    {
        this.project = project;
    }
    public void observeExecution( MojoExecution mojoExecution )
    {
        String lifecyclePhase = mojoExecution.getLifecyclePhase();
        if ( lifecyclePhase != null )
        {
            if ( lastLifecyclePhase == null )
            {
                lastLifecyclePhase = lifecyclePhase;
            }
            else if ( !lifecyclePhase.equals( lastLifecyclePhase ) )
            {
                project.addLifecyclePhase( lastLifecyclePhase );
                lastLifecyclePhase = lifecyclePhase;
            }
        }
        if ( lastLifecyclePhase != null )
        {
            project.addLifecyclePhase( lastLifecyclePhase );
        }
    }
    public boolean isDifferentPhase( MojoExecution nextMojoExecution )
    {
        String lifecyclePhase = nextMojoExecution.getLifecyclePhase();
        if ( lifecyclePhase == null )
        {
            return lastLifecyclePhase != null;
        }
        return !lifecyclePhase.equals( lastLifecyclePhase );
    }
}
