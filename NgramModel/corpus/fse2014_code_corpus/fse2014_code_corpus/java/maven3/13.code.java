package org.apache.maven.repository.internal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.sonatype.aether.ConfigurationProperties;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.impl.MetadataGenerator;
import org.sonatype.aether.metadata.Metadata;
class RemoteSnapshotMetadataGenerator
    implements MetadataGenerator
{
    private final Map<Object, RemoteSnapshotMetadata> snapshots;
    private final boolean legacyFormat;
    public RemoteSnapshotMetadataGenerator( RepositorySystemSession session, DeployRequest request )
    {
        legacyFormat = ConfigurationProperties.get( session.getConfigProperties(), "maven.metadata.legacy", false );
        snapshots = new LinkedHashMap<Object, RemoteSnapshotMetadata>();
        for ( Metadata metadata : request.getMetadata() )
        {
            if ( metadata instanceof RemoteSnapshotMetadata )
            {
                RemoteSnapshotMetadata snapshotMetadata = (RemoteSnapshotMetadata) metadata;
                snapshots.put( snapshotMetadata.getKey(), snapshotMetadata );
            }
        }
    }
    public Collection<? extends Metadata> prepare( Collection<? extends Artifact> artifacts )
    {
        for ( Artifact artifact : artifacts )
        {
            if ( artifact.isSnapshot() )
            {
                Object key = RemoteSnapshotMetadata.getKey( artifact );
                RemoteSnapshotMetadata snapshotMetadata = snapshots.get( key );
                if ( snapshotMetadata == null )
                {
                    snapshotMetadata = new RemoteSnapshotMetadata( artifact, legacyFormat );
                    snapshots.put( key, snapshotMetadata );
                }
                snapshotMetadata.bind( artifact );
            }
        }
        return snapshots.values();
    }
    public Artifact transformArtifact( Artifact artifact )
    {
        if ( artifact.isSnapshot() && artifact.getVersion().equals( artifact.getBaseVersion() ) )
        {
            Object key = RemoteSnapshotMetadata.getKey( artifact );
            RemoteSnapshotMetadata snapshotMetadata = snapshots.get( key );
            if ( snapshotMetadata != null )
            {
                artifact = artifact.setVersion( snapshotMetadata.getExpandedVersion( artifact ) );
            }
        }
        return artifact;
    }
    public Collection<? extends Metadata> finish( Collection<? extends Artifact> artifacts )
    {
        return Collections.emptyList();
    }
}
