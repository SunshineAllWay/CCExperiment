package org.apache.maven.repository.internal;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.ArtifactResolver;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.ArtifactRequest;
import org.sonatype.aether.resolution.ArtifactResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;
class DefaultModelResolver
    implements ModelResolver
{
    private final RepositorySystemSession session;
    private final String context;
    private List<RemoteRepository> repositories;
    private final ArtifactResolver resolver;
    private final RemoteRepositoryManager remoteRepositoryManager;
    private final Set<String> repositoryIds;
    public DefaultModelResolver( RepositorySystemSession session, String context, ArtifactResolver resolver,
                                 RemoteRepositoryManager remoteRepositoryManager, List<RemoteRepository> repositories )
    {
        this.session = session;
        this.context = context;
        this.resolver = resolver;
        this.remoteRepositoryManager = remoteRepositoryManager;
        this.repositories = repositories;
        this.repositoryIds = new HashSet<String>();
    }
    private DefaultModelResolver( DefaultModelResolver original )
    {
        this.session = original.session;
        this.context = original.context;
        this.resolver = original.resolver;
        this.remoteRepositoryManager = original.remoteRepositoryManager;
        this.repositories = original.repositories;
        this.repositoryIds = new HashSet<String>( original.repositoryIds );
    }
    public void addRepository( Repository repository )
        throws InvalidRepositoryException
    {
        if ( !repositoryIds.add( repository.getId() ) )
        {
            return;
        }
        List<RemoteRepository> newRepositories =
            Collections.singletonList( DefaultArtifactDescriptorReader.convert( repository ) );
        this.repositories =
            remoteRepositoryManager.aggregateRepositories( session, repositories, newRepositories, true );
    }
    public ModelResolver newCopy()
    {
        return new DefaultModelResolver( this );
    }
    public ModelSource resolveModel( String groupId, String artifactId, String version )
        throws UnresolvableModelException
    {
        Artifact pomArtifact = new DefaultArtifact( groupId, artifactId, "", "pom", version );
        try
        {
            ArtifactRequest request = new ArtifactRequest( pomArtifact, repositories, context );
            pomArtifact = resolver.resolveArtifact( session, request ).getArtifact();
        }
        catch ( ArtifactResolutionException e )
        {
            throw new UnresolvableModelException( e.getMessage(), groupId, artifactId, version, e );
        }
        File pomFile = pomArtifact.getFile();
        return new FileModelSource( pomFile );
    }
}
