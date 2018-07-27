package org.apache.maven;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.WorkspaceReader;
import org.sonatype.aether.repository.WorkspaceRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
class ReactorReader
    implements WorkspaceReader
{
    private Map<String, MavenProject> projectsByGAV;
    private Map<String, List<MavenProject>> projectsByGA;
    private WorkspaceRepository repository;
    @SuppressWarnings( { "ConstantConditions" } )
    public ReactorReader( Map<String, MavenProject> reactorProjects )
    {
        projectsByGAV = reactorProjects;
        projectsByGA = new HashMap<String, List<MavenProject>>( reactorProjects.size() * 2 );
        for ( MavenProject project : reactorProjects.values() )
        {
            String key = ArtifactUtils.versionlessKey( project.getGroupId(), project.getArtifactId() );
            List<MavenProject> projects = projectsByGA.get( key );
            if ( projects == null )
            {
                projects = new ArrayList<MavenProject>( 1 );
                projectsByGA.put( key, projects );
            }
            projects.add( project );
        }
        repository = new WorkspaceRepository( "reactor", new HashSet<String>( projectsByGAV.keySet() ) );
    }
    private File find( MavenProject project, Artifact artifact )
    {
        if ( "pom".equals( artifact.getExtension() ) )
        {
            return project.getFile();
        }
        org.apache.maven.artifact.Artifact projectArtifact = findMatchingArtifact( project, artifact );
        if ( hasArtifactFileFromPackagePhase( projectArtifact ) )
        {
            return projectArtifact.getFile();
        }
        else if ( !hasBeenPackaged( project ) )
        {
            if ( isTestArtifact( artifact ) )
            {
                if ( project.hasLifecyclePhase( "test-compile" ) )
                {
                    return new File( project.getBuild().getTestOutputDirectory() );
                }
            }
            else
            {
                if ( project.hasLifecyclePhase( "compile" ) )
                {
                    return new File( project.getBuild().getOutputDirectory() );
                }
            }
        }
        return null;
    }
    private boolean hasArtifactFileFromPackagePhase( org.apache.maven.artifact.Artifact projectArtifact )
    {
        return projectArtifact != null && projectArtifact.getFile() != null && projectArtifact.getFile().exists();
    }
    private boolean hasBeenPackaged( MavenProject project )
    {
        return project.hasLifecyclePhase( "package" ) || project.hasLifecyclePhase( "install" )
            || project.hasLifecyclePhase( "deploy" );
    }
    private org.apache.maven.artifact.Artifact findMatchingArtifact( MavenProject project, Artifact requestedArtifact )
    {
        String requestedRepositoryConflictId = getConflictId( requestedArtifact );
        org.apache.maven.artifact.Artifact mainArtifact = project.getArtifact();
        if ( requestedRepositoryConflictId.equals( getConflictId( mainArtifact ) ) )
        {
            return mainArtifact;
        }
        Collection<org.apache.maven.artifact.Artifact> attachedArtifacts = project.getAttachedArtifacts();
        if ( attachedArtifacts != null && !attachedArtifacts.isEmpty() )
        {
            for ( org.apache.maven.artifact.Artifact attachedArtifact : attachedArtifacts )
            {
                if ( requestedRepositoryConflictId.equals( getConflictId( attachedArtifact ) ) )
                {
                    return attachedArtifact;
                }
            }
        }
        return null;
    }
    private String getConflictId( org.apache.maven.artifact.Artifact artifact )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( artifact.getGroupId() );
        buffer.append( ':' ).append( artifact.getArtifactId() );
        if ( artifact.getArtifactHandler() != null )
        {
            buffer.append( ':' ).append( artifact.getArtifactHandler().getExtension() );
        }
        else
        {
            buffer.append( ':' ).append( artifact.getType() );
        }
        if ( artifact.hasClassifier() )
        {
            buffer.append( ':' ).append( artifact.getClassifier() );
        }
        return buffer.toString();
    }
    private String getConflictId( Artifact artifact )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( artifact.getGroupId() );
        buffer.append( ':' ).append( artifact.getArtifactId() );
        buffer.append( ':' ).append( artifact.getExtension() );
        if ( artifact.getClassifier().length() > 0 )
        {
            buffer.append( ':' ).append( artifact.getClassifier() );
        }
        return buffer.toString();
    }
    private static boolean isTestArtifact( Artifact artifact )
    {
        if ( "test-jar".equals( artifact.getProperty( "type", "" ) ) )
        {
            return true;
        }
        else if ( "jar".equals( artifact.getExtension() ) && "tests".equals( artifact.getClassifier() ) )
        {
            return true;
        }
        return false;
    }
    public File findArtifact( Artifact artifact )
    {
        String projectKey = ArtifactUtils.key( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion() );
        MavenProject project = projectsByGAV.get( projectKey );
        if ( project != null )
        {
            return find( project, artifact );
        }
        return null;
    }
    public List<String> findVersions( Artifact artifact )
    {
        String key = ArtifactUtils.versionlessKey( artifact.getGroupId(), artifact.getArtifactId() );
        List<MavenProject> projects = projectsByGA.get( key );
        if ( projects == null || projects.isEmpty() )
        {
            return Collections.emptyList();
        }
        List<String> versions = new ArrayList<String>();
        for ( MavenProject project : projects )
        {
            if ( find( project, artifact ) != null )
            {
                versions.add( project.getVersion() );
            }
        }
        return Collections.unmodifiableList( versions );
    }
    public WorkspaceRepository getRepository()
    {
        return repository;
    }
}
