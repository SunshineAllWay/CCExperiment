package org.apache.maven.project.artifact;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Relocation;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.InvalidProjectModelException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.validation.ModelValidationResult;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.StringUtils;
public class MavenMetadataSource
    extends AbstractLogEnabled
    implements ArtifactMetadataSource
{
    public static final String ROLE_HINT = "maven";
    private MavenProjectBuilder mavenProjectBuilder;
    private ArtifactFactory artifactFactory;
    private RepositoryMetadataManager repositoryMetadataManager;
    private MavenProject superProject;
    private final Set<String> warnedPoms;
    private ProjectBuilderConfiguration projectBuilderConfig;
    public MavenMetadataSource()
    {
        warnedPoms = new HashSet<String>();
    }
    public MavenMetadataSource( MavenMetadataSource mms, ProjectBuilderConfiguration configuration )
    {
        mavenProjectBuilder = mms.mavenProjectBuilder;
        artifactFactory = mms.artifactFactory;
        repositoryMetadataManager = mms.repositoryMetadataManager;
        superProject = mms.superProject;
        warnedPoms = mms.warnedPoms;
        projectBuilderConfig = configuration;
        projectBuilderConfig.setMetadataSource( this );
        enableLogging( mms.getLogger() );
    }
    public Artifact retrieveRelocatedArtifact( Artifact artifact, ArtifactRepository localRepository, List remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        if ( artifact instanceof ActiveProjectArtifact )
        {
            return artifact;
        }
        ProjectRelocation rel = retrieveRelocatedProject( artifact, localRepository, remoteRepositories );
        if ( rel == null )
        {
            return artifact;
        }
        MavenProject project = rel.project;
        if ( project == null || getRelocationKey( artifact ).equals( getRelocationKey( project.getArtifact() ) ) )
        {
            return artifact;
        }
        Artifact result = null;
        if ( artifact.getClassifier() != null )
        {
            result = artifactFactory.createArtifactWithClassifier( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getType(), artifact.getClassifier() );
        }
        else
        {
            result = artifactFactory.createArtifact( artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), artifact.getScope(), artifact.getType() );
        }
        result.setResolved( artifact.isResolved() );
        result.setFile( artifact.getFile() );
        result.setScope( artifact.getScope() );
        result.setArtifactHandler( artifact.getArtifactHandler() );
        result.setDependencyFilter( artifact.getDependencyFilter() );
        result.setDependencyTrail( artifact.getDependencyTrail() );
        result.setOptional( artifact.isOptional() );
        result.setRelease( artifact.isRelease() );
        return result;
    }
    private String getRelocationKey( Artifact artifact )
    {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
    }
    private ProjectRelocation retrieveRelocatedProject( Artifact artifact, ArtifactRepository localRepository, List remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        MavenProject project = null;
        ProjectBuilderConfiguration config = projectBuilderConfig;
        if ( config == null )
        {
            config = new DefaultProjectBuilderConfiguration().setLocalRepository( localRepository );
        }
        Artifact pomArtifact;
        boolean done = false;
        do
        {
            pomArtifact = artifactFactory.createProjectArtifact( artifact.getGroupId(), artifact.getArtifactId(),
                                                                 artifact.getVersion(), artifact.getScope() );
            if ( Artifact.SCOPE_SYSTEM.equals( artifact.getScope() ) )
            {
                done = true;
            }
            else
            {
                try
                {
                    project = mavenProjectBuilder.buildFromRepository( pomArtifact, remoteRepositories, config,
                                                                       true );
                }
                catch ( InvalidProjectModelException e )
                {
                    String id = pomArtifact.getId();
                    if ( !warnedPoms.contains( id ) )
                    {
                        warnedPoms.add( pomArtifact.getId() );
                        getLogger().warn( "POM for \'"
                                              + pomArtifact
                                              + "\' is invalid.\n\nIts dependencies (if any) will NOT be available to the current build." );
                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Reason: " + e.getMessage() );
                            ModelValidationResult validationResult = e.getValidationResult();
                            if ( validationResult != null )
                            {
                                getLogger().debug( "\nValidation Errors:" );
                                for ( Iterator i = validationResult.getMessages().iterator(); i.hasNext(); )
                                {
                                    getLogger().debug( i.next().toString() );
                                }
                                getLogger().debug( "\n" );
                            }
                        }
                    }
                    project = null;
                }
                catch ( ProjectBuildingException e )
                {
                    throw new ArtifactMetadataRetrievalException( "Unable to read the metadata file for artifact '"
                        + artifact.getDependencyConflictId() + "': " + e.getMessage(), e, artifact );
                }
                if ( project != null )
                {
                    Relocation relocation = null;
                    DistributionManagement distMgmt = project.getDistributionManagement();
                    if ( distMgmt != null )
                    {
                        relocation = distMgmt.getRelocation();
                        artifact.setDownloadUrl( distMgmt.getDownloadUrl() );
                        pomArtifact.setDownloadUrl( distMgmt.getDownloadUrl() );
                    }
                    if ( relocation != null )
                    {
                        if ( relocation.getGroupId() != null )
                        {
                            artifact.setGroupId( relocation.getGroupId() );
                            project.setGroupId( relocation.getGroupId() );
                        }
                        if ( relocation.getArtifactId() != null )
                        {
                            artifact.setArtifactId( relocation.getArtifactId() );
                            project.setArtifactId( relocation.getArtifactId() );
                        }
                        if ( relocation.getVersion() != null )
                        {
                            artifact.setVersionRange( VersionRange.createFromVersion( relocation.getVersion() ) );
                            project.setVersion( relocation.getVersion() );
                        }
                        if ( artifact.getDependencyFilter() != null
                            && !artifact.getDependencyFilter().include( artifact ) )
                        {
                            return null;
                        }
                        List available = artifact.getAvailableVersions();
                        if ( available != null && !available.isEmpty() )
                        {
                            artifact.setAvailableVersions( retrieveAvailableVersions( artifact, localRepository,
                                                                                           remoteRepositories ) );
                        }
                        String message = "\n  This artifact has been relocated to " + artifact.getGroupId() + ":"
                            + artifact.getArtifactId() + ":" + artifact.getVersion() + ".\n";
                        if ( relocation.getMessage() != null )
                        {
                            message += "  " + relocation.getMessage() + "\n";
                        }
                        if ( artifact.getDependencyTrail() != null && artifact.getDependencyTrail().size() == 1 )
                        {
                            getLogger().warn( "While downloading " + pomArtifact.getGroupId() + ":"
                                + pomArtifact.getArtifactId() + ":" + pomArtifact.getVersion() + message + "\n" );
                        }
                        else
                        {
                            getLogger().debug( "While downloading " + pomArtifact.getGroupId() + ":"
                                + pomArtifact.getArtifactId() + ":" + pomArtifact.getVersion() + message + "\n" );
                        }
                    }
                    else
                    {
                        done = true;
                    }
                }
                else
                {
                    done = true;
                }
            }
        }
        while ( !done );
        ProjectRelocation rel = new ProjectRelocation();
        rel.project = project;
        rel.pomArtifact = pomArtifact;
        return rel;
    }
    public ResolutionGroup retrieve( Artifact artifact, ArtifactRepository localRepository, List remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        ProjectRelocation rel = retrieveRelocatedProject( artifact, localRepository, remoteRepositories );
        if ( rel == null )
        {
            return null;
        }
        MavenProject project = rel.project;
        Artifact pomArtifact = rel.pomArtifact;
        if ( artifact.getDownloadUrl() == null && pomArtifact != null )
        {
            artifact.setDownloadUrl( pomArtifact.getDownloadUrl() );
        }
        ResolutionGroup result;
        if ( project == null )
        {
            result = new ResolutionGroup( pomArtifact, Collections.EMPTY_SET, Collections.EMPTY_LIST );
        }
        else
        {
            Set artifacts = Collections.EMPTY_SET;
            if ( !artifact.getArtifactHandler().isIncludesDependencies() )
            {
                try
                {
                    artifacts = project.createArtifacts( artifactFactory, artifact.getScope(),
                                                         artifact.getDependencyFilter() );
                }
                catch ( InvalidDependencyVersionException e )
                {
                    throw new ArtifactMetadataRetrievalException( "Error in metadata for artifact '"
                        + artifact.getDependencyConflictId() + "': " + e.getMessage(), e );
                }
            }
            List repositories = aggregateRepositoryLists( remoteRepositories, project.getRemoteArtifactRepositories() );
            result = new ResolutionGroup( pomArtifact, artifacts, repositories );
        }
        return result;
    }
    private List aggregateRepositoryLists( List remoteRepositories, List remoteArtifactRepositories )
        throws ArtifactMetadataRetrievalException
    {
        ProjectBuilderConfiguration config = projectBuilderConfig;
        if ( config == null )
        {
            config = new DefaultProjectBuilderConfiguration();
        }
        if ( superProject == null )
        {
            try
            {
                superProject = mavenProjectBuilder.buildStandaloneSuperProject( config );
            }
            catch ( ProjectBuildingException e )
            {
                throw new ArtifactMetadataRetrievalException(
                    "Unable to parse the Maven built-in model: " + e.getMessage(), e );
            }
        }
        List repositories = new ArrayList();
        repositories.addAll( remoteRepositories );
        for ( Iterator it = superProject.getRemoteArtifactRepositories().iterator(); it.hasNext(); )
        {
            ArtifactRepository superRepo = (ArtifactRepository) it.next();
            for ( Iterator aggregatedIterator = repositories.iterator(); aggregatedIterator.hasNext(); )
            {
                ArtifactRepository repo = (ArtifactRepository) aggregatedIterator.next();
                if ( repo.getId().equals( superRepo.getId() ) && repo.getUrl().equals( superRepo.getUrl() ) )
                {
                    aggregatedIterator.remove();
                }
            }
        }
        for ( Iterator it = remoteArtifactRepositories.iterator(); it.hasNext(); )
        {
            ArtifactRepository repository = (ArtifactRepository) it.next();
            if ( !repositories.contains( repository ) )
            {
                repositories.add( repository );
            }
        }
        return repositories;
    }
    public static Set createArtifacts( ArtifactFactory artifactFactory, List dependencies, String inheritedScope,
                                       ArtifactFilter dependencyFilter, MavenProject project )
        throws InvalidDependencyVersionException
    {
        Set projectArtifacts = new LinkedHashSet( dependencies.size() );
        for ( Iterator i = dependencies.iterator(); i.hasNext(); )
        {
            Dependency d = (Dependency) i.next();
            String scope = d.getScope();
            if ( StringUtils.isEmpty( scope ) )
            {
                scope = Artifact.SCOPE_COMPILE;
                d.setScope( scope );
            }
            VersionRange versionRange;
            try
            {
                versionRange = VersionRange.createFromVersionSpec( d.getVersion() );
            }
            catch ( InvalidVersionSpecificationException e )
            {
                throw new InvalidDependencyVersionException( "Unable to parse version '" + d.getVersion()
                    + "' for dependency '" + d.getManagementKey() + "': " + e.getMessage(), e );
            }
            Artifact artifact = artifactFactory.createDependencyArtifact( d.getGroupId(), d.getArtifactId(),
                                                                          versionRange, d.getType(), d.getClassifier(),
                                                                          scope, inheritedScope, d.isOptional() );
            if ( Artifact.SCOPE_SYSTEM.equals( scope ) )
            {
                artifact.setFile( new File( d.getSystemPath() ) );
            }
            ArtifactFilter artifactFilter = dependencyFilter;
            if ( artifact != null && ( artifactFilter == null || artifactFilter.include( artifact ) ) )
            {
                if ( d.getExclusions() != null && !d.getExclusions().isEmpty() )
                {
                    List exclusions = new ArrayList();
                    for ( Iterator j = d.getExclusions().iterator(); j.hasNext(); )
                    {
                        Exclusion e = (Exclusion) j.next();
                        exclusions.add( e.getGroupId() + ":" + e.getArtifactId() );
                    }
                    ArtifactFilter newFilter = new ExcludesArtifactFilter( exclusions );
                    if ( artifactFilter != null )
                    {
                        AndArtifactFilter filter = new AndArtifactFilter();
                        filter.add( artifactFilter );
                        filter.add( newFilter );
                        artifactFilter = filter;
                    }
                    else
                    {
                        artifactFilter = newFilter;
                    }
                }
                artifact.setDependencyFilter( artifactFilter );
                if ( project != null )
                {
                    artifact = project.replaceWithActiveArtifact( artifact );
                }
                projectArtifacts.add( artifact );
            }
        }
        return projectArtifacts;
    }
    public List retrieveAvailableVersions( Artifact artifact, ArtifactRepository localRepository,
                                           List remoteRepositories )
        throws ArtifactMetadataRetrievalException
    {
        RepositoryMetadata metadata = new ArtifactRepositoryMetadata( artifact );
        try
        {
            repositoryMetadataManager.resolve( metadata, remoteRepositories, localRepository );
        }
        catch ( RepositoryMetadataResolutionException e )
        {
            throw new ArtifactMetadataRetrievalException( e.getMessage(), e );
        }
        List versions;
        Metadata repoMetadata = metadata.getMetadata();
        if ( repoMetadata != null && repoMetadata.getVersioning() != null )
        {
            List metadataVersions = repoMetadata.getVersioning().getVersions();
            versions = new ArrayList( metadataVersions.size() );
            for ( Iterator i = metadataVersions.iterator(); i.hasNext(); )
            {
                String version = (String) i.next();
                versions.add( new DefaultArtifactVersion( version ) );
            }
        }
        else
        {
            versions = Collections.EMPTY_LIST;
        }
        return versions;
    }
    private static final class ProjectRelocation
    {
        private MavenProject project;
        private Artifact pomArtifact;
    }
}
