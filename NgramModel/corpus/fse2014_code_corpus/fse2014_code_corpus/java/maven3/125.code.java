package org.apache.maven.repository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.CyclicDependencyException;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
public class MetadataResolutionResult
{
    private Artifact originatingArtifact;
    private List<Artifact> missingArtifacts;
    private List<Exception> exceptions;
    private List<Exception> versionRangeViolations;
    private List<ArtifactResolutionException> metadataResolutionExceptions;
    private List<CyclicDependencyException> circularDependencyExceptions;
    private List<ArtifactResolutionException> errorArtifactExceptions;
    private List<ArtifactRepository> repositories;
    private Set<Artifact> requestedArtifacts;
    private Set<Artifact> artifacts;
    private MetadataGraph dirtyTree;
    private MetadataGraph resolvedTree;
    private MetadataGraph resolvedGraph;
    public Artifact getOriginatingArtifact()
    {
        return originatingArtifact;
    }
    public MetadataResolutionResult ListOriginatingArtifact( final Artifact originatingArtifact )
    {
        this.originatingArtifact = originatingArtifact;
        return this;
    }
    public void addArtifact( Artifact artifact )
    {
        if ( artifacts == null )
        {
            artifacts = new LinkedHashSet<Artifact>();
        }
        artifacts.add( artifact );
    }
    public Set<Artifact> getArtifacts()
    {
        return artifacts;
    }
    public void addRequestedArtifact( Artifact artifact )
    {
        if ( requestedArtifacts == null )
        {
            requestedArtifacts = new LinkedHashSet<Artifact>();
        }
        requestedArtifacts.add( artifact );
    }
    public Set<Artifact> getRequestedArtifacts()
    {
        return requestedArtifacts;
    }
    public boolean hasMissingArtifacts()
    {
        return missingArtifacts != null && !missingArtifacts.isEmpty();
    }
    public List<Artifact> getMissingArtifacts()
    {
        return missingArtifacts == null ? Collections.<Artifact> emptyList() : missingArtifacts;
    }
    public MetadataResolutionResult addMissingArtifact( Artifact artifact )
    {
        missingArtifacts = initList( missingArtifacts );
        missingArtifacts.add( artifact );
        return this;
    }
    public MetadataResolutionResult setUnresolvedArtifacts( final List<Artifact> unresolvedArtifacts )
    {
        this.missingArtifacts = unresolvedArtifacts;
        return this;
    }
    public boolean hasExceptions()
    {
        return exceptions != null && !exceptions.isEmpty();
    }
    public List<Exception> getExceptions()
    {
        return exceptions == null ? Collections.<Exception> emptyList() : exceptions;
    }
    public boolean hasVersionRangeViolations()
    {
        return versionRangeViolations != null;
    }
    public MetadataResolutionResult addVersionRangeViolation( Exception e )
    {
        versionRangeViolations = initList( versionRangeViolations );
        versionRangeViolations.add( e );
        exceptions = initList( exceptions );
        exceptions.add( e );
        return this;
    }
    public OverConstrainedVersionException getVersionRangeViolation( int i )
    {
        return (OverConstrainedVersionException) versionRangeViolations.get( i );
    }
    public List<Exception> getVersionRangeViolations()
    {
        return versionRangeViolations == null ? Collections.<Exception> emptyList() : versionRangeViolations;
    }
    public boolean hasMetadataResolutionExceptions()
    {
        return metadataResolutionExceptions != null;
    }
    public MetadataResolutionResult addMetadataResolutionException( ArtifactResolutionException e )
    {
        metadataResolutionExceptions = initList( metadataResolutionExceptions );
        metadataResolutionExceptions.add( e );
        exceptions = initList( exceptions );
        exceptions.add( e );
        return this;
    }
    public ArtifactResolutionException getMetadataResolutionException( int i )
    {
        return metadataResolutionExceptions.get( i );
    }
    public List<ArtifactResolutionException> getMetadataResolutionExceptions()
    {
        return metadataResolutionExceptions == null ? Collections.<ArtifactResolutionException> emptyList()
                        : metadataResolutionExceptions;
    }
    public boolean hasErrorArtifactExceptions()
    {
        return errorArtifactExceptions != null;
    }
    public MetadataResolutionResult addError( Exception e )
    {
        if ( exceptions == null )
        {
            initList( exceptions );
        }
        exceptions.add( e );
        return this;
    }
    public List<ArtifactResolutionException> getErrorArtifactExceptions()
    {
        if ( errorArtifactExceptions == null )
        {
            return Collections.emptyList();
        }
        return errorArtifactExceptions;
    }
    public boolean hasCircularDependencyExceptions()
    {
        return circularDependencyExceptions != null;
    }
    public MetadataResolutionResult addCircularDependencyException( CyclicDependencyException e )
    {
        circularDependencyExceptions = initList( circularDependencyExceptions );
        circularDependencyExceptions.add( e );
        exceptions = initList( exceptions );
        exceptions.add( e );
        return this;
    }
    public CyclicDependencyException getCircularDependencyException( int i )
    {
        return circularDependencyExceptions.get( i );
    }
    public List<CyclicDependencyException> getCircularDependencyExceptions()
    {
        if ( circularDependencyExceptions == null )
        {
            return Collections.emptyList();
        }
        return circularDependencyExceptions;
    }
    public List<ArtifactRepository> getRepositories()
    {
        if ( repositories == null )
        {
            return Collections.emptyList();
        }
        return repositories;
    }
    public MetadataResolutionResult setRepositories( final List<ArtifactRepository> repositories )
    {
        this.repositories = repositories;
        return this;
    }
    private <T> List<T> initList( final List<T> l )
    {
        if ( l == null )
        {
            return new ArrayList<T>();
        }
        return l;
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if ( artifacts != null )
        {
            int i = 1;
            sb.append( "---------" ).append( "\n" );
            sb.append( artifacts.size() ).append( "\n" );
            for ( Artifact a : artifacts )
            {
                sb.append( i ).append( " " ).append( a ).append( "\n" );
                i++;
            }
            sb.append( "---------" ).append( "\n" );
        }
        return sb.toString();
    }
    public MetadataGraph getResolvedTree()
    {
        return resolvedTree;
    }
    public void setResolvedTree( MetadataGraph resolvedTree )
    {
        this.resolvedTree = resolvedTree;
    }
}
