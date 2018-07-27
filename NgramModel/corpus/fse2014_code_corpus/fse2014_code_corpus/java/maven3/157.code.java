package org.apache.maven.repository.legacy.resolver.transform;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.RepositoryRequest;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
@Component( role = ArtifactTransformation.class, hint = "snapshot" )
public class SnapshotTransformation
    extends AbstractVersionTransformation
{
    private String deploymentTimestamp;
    private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone( "UTC" );
    private static final String UTC_TIMESTAMP_PATTERN = "yyyyMMdd.HHmmss";
    public void transformForResolve( Artifact artifact, RepositoryRequest request )
        throws ArtifactResolutionException
    {
        if ( artifact.isSnapshot() && artifact.getBaseVersion().equals( artifact.getVersion() ) )
        {
            try
            {
                String version = resolveVersion( artifact, request );
                artifact.updateVersion( version, request.getLocalRepository() );
            }
            catch ( RepositoryMetadataResolutionException e )
            {
                throw new ArtifactResolutionException( e.getMessage(), artifact, e );
            }
        }
    }
    public void transformForInstall( Artifact artifact, ArtifactRepository localRepository )
    {
        if ( artifact.isSnapshot() )
        {
            Snapshot snapshot = new Snapshot();
            snapshot.setLocalCopy( true );
            RepositoryMetadata metadata = new SnapshotArtifactRepositoryMetadata( artifact, snapshot );
            artifact.addMetadata( metadata );
        }
    }
    public void transformForDeployment( Artifact artifact, ArtifactRepository remoteRepository,
                                        ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        if ( artifact.isSnapshot() )
        {
            Snapshot snapshot = new Snapshot();
            snapshot.setTimestamp( getDeploymentTimestamp() );
            try
            {
                int buildNumber = resolveLatestSnapshotBuildNumber( artifact, localRepository, remoteRepository );
                snapshot.setBuildNumber( buildNumber + 1 );
            }
            catch ( RepositoryMetadataResolutionException e )
            {
                throw new ArtifactDeploymentException( "Error retrieving previous build number for artifact '"
                    + artifact.getDependencyConflictId() + "': " + e.getMessage(), e );
            }
            RepositoryMetadata metadata = new SnapshotArtifactRepositoryMetadata( artifact, snapshot );
            artifact.setResolvedVersion(
                constructVersion( metadata.getMetadata().getVersioning(), artifact.getBaseVersion() ) );
            artifact.addMetadata( metadata );
        }
    }
    public String getDeploymentTimestamp()
    {
        if ( deploymentTimestamp == null )
        {
            deploymentTimestamp = getUtcDateFormatter().format( new Date() );
        }
        return deploymentTimestamp;
    }
    protected String constructVersion( Versioning versioning, String baseVersion )
    {
        String version = null;
        Snapshot snapshot = versioning.getSnapshot();
        if ( snapshot != null )
        {
            if ( snapshot.getTimestamp() != null && snapshot.getBuildNumber() > 0 )
            {
                String newVersion = snapshot.getTimestamp() + "-" + snapshot.getBuildNumber();
                version = StringUtils.replace( baseVersion, Artifact.SNAPSHOT_VERSION, newVersion );
            }
            else
            {
                version = baseVersion;
            }
        }
        return version;
    }
    private int resolveLatestSnapshotBuildNumber( Artifact artifact, ArtifactRepository localRepository,
                                                  ArtifactRepository remoteRepository )
        throws RepositoryMetadataResolutionException
    {
        RepositoryMetadata metadata = new SnapshotArtifactRepositoryMetadata( artifact );
        getLogger().info( "Retrieving previous build number from " + remoteRepository.getId() );
        repositoryMetadataManager.resolveAlways( metadata, localRepository, remoteRepository );
        int buildNumber = 0;
        Metadata repoMetadata = metadata.getMetadata();
        if ( ( repoMetadata != null )
            && ( repoMetadata.getVersioning() != null && repoMetadata.getVersioning().getSnapshot() != null ) )
        {
            buildNumber = repoMetadata.getVersioning().getSnapshot().getBuildNumber();
        }
        return buildNumber;
    }
    public static DateFormat getUtcDateFormatter()
    {
        DateFormat utcDateFormatter = new SimpleDateFormat( UTC_TIMESTAMP_PATTERN );
        utcDateFormatter.setTimeZone( UTC_TIME_ZONE );
        return utcDateFormatter;
    }
}
