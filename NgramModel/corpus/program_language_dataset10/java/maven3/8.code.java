package org.apache.maven.repository.internal;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.sonatype.aether.ConfigurationProperties;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.impl.MetadataGenerator;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.metadata.Metadata;
class LocalSnapshotMetadataGenerator
    implements MetadataGenerator
{
    private Map<Object, LocalSnapshotMetadata> snapshots;
    private final boolean legacyFormat;
    public LocalSnapshotMetadataGenerator( RepositorySystemSession session, InstallRequest request )
    {
        legacyFormat = ConfigurationProperties.get( session.getConfigProperties(), "maven.metadata.legacy", false );
        snapshots = new LinkedHashMap<Object, LocalSnapshotMetadata>();
    }
    public Collection<? extends Metadata> prepare( Collection<? extends Artifact> artifacts )
    {
        for ( Artifact artifact : artifacts )
        {
            if ( artifact.isSnapshot() )
            {
                Object key = LocalSnapshotMetadata.getKey( artifact );
                LocalSnapshotMetadata snapshotMetadata = snapshots.get( key );
                if ( snapshotMetadata == null )
                {
                    snapshotMetadata = new LocalSnapshotMetadata( artifact, legacyFormat );
                    snapshots.put( key, snapshotMetadata );
                }
                snapshotMetadata.bind( artifact );
            }
        }
        return Collections.emptyList();
    }
    public Artifact transformArtifact( Artifact artifact )
    {
        return artifact;
    }
    public Collection<? extends Metadata> finish( Collection<? extends Artifact> artifacts )
    {
        return snapshots.values();
    }
}
