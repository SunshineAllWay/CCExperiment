package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.lifecycle.internal.DependencyContext;
import org.apache.maven.lifecycle.internal.MojoExecutor;
import org.apache.maven.lifecycle.internal.PhaseRecorder;
import org.apache.maven.lifecycle.internal.ProjectIndex;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class MojoExecutorStub
    extends MojoExecutor
{ 
    public List<MojoExecution> executions = Collections.synchronizedList( new ArrayList<MojoExecution>() );
    @Override
    public void execute( MavenSession session, MojoExecution mojoExecution, ProjectIndex projectIndex,
                         DependencyContext dependencyContext, PhaseRecorder phaseRecorder )
        throws LifecycleExecutionException
    {
        executions.add( mojoExecution );
    }
    @Override
    public void execute( MavenSession session, List<MojoExecution> mojoExecutions, ProjectIndex projectIndex )
        throws LifecycleExecutionException
    {
        for ( MojoExecution mojoExecution : mojoExecutions )
        {
            executions.add( mojoExecution );
        }
    }
    public static MojoDescriptor createMojoDescriptor( String mojoDescription )
    {
        final PluginDescriptor descriptor = new PluginDescriptor();
        descriptor.setArtifactId( mojoDescription );
        final MojoDescriptor mojoDescriptor = new MojoDescriptor();
        mojoDescriptor.setDescription( mojoDescription );
        mojoDescriptor.setPluginDescriptor( descriptor );
        return mojoDescriptor;
    }
}
