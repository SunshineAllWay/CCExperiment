package org.apache.maven.repository.internal;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.impl.MetadataGenerator;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.metadata.Metadata;
class VersionsMetadataGenerator
    implements MetadataGenerator
{
    private Map<Object, VersionsMetadata> versions;
    private Map<Object, VersionsMetadata> processedVersions;
    public VersionsMetadataGenerator( RepositorySystemSession session, InstallRequest request )
    {
        this( session, request.getMetadata() );
    }
    public VersionsMetadataGenerator( RepositorySystemSession session, DeployRequest request )
    {
        this( session, request.getMetadata() );
    }
    private VersionsMetadataGenerator( RepositorySystemSession session, Collection<? extends Metadata> metadatas )
    {
        versions = new LinkedHashMap<Object, VersionsMetadata>();
        processedVersions = new LinkedHashMap<Object, VersionsMetadata>();
        for ( Iterator<? extends Metadata> it = metadatas.iterator(); it.hasNext(); )
        {
            Metadata metadata = it.next();
            if ( metadata instanceof VersionsMetadata )
            {
                it.remove();
                VersionsMetadata versionsMetadata = (VersionsMetadata) metadata;
                processedVersions.put( versionsMetadata.getKey(), versionsMetadata );
            }
        }
    }
    public Collection<? extends Metadata> prepare( Collection<? extends Artifact> artifacts )
    {
        return Collections.emptyList();
    }
    public Artifact transformArtifact( Artifact artifact )
    {
        return artifact;
    }
    public Collection<? extends Metadata> finish( Collection<? extends Artifact> artifacts )
    {
        for ( Artifact artifact : artifacts )
        {
            Object key = VersionsMetadata.getKey( artifact );
            if ( processedVersions.get( key ) == null )
            {
                VersionsMetadata versionsMetadata = versions.get( key );
                if ( versionsMetadata == null )
                {
                    versionsMetadata = new VersionsMetadata( artifact );
                    versions.put( key, versionsMetadata );
                }
            }
        }
        return versions.values();
    }
}
