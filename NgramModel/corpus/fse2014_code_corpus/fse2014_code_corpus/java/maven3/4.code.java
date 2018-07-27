package org.apache.maven.repository.internal;
import org.sonatype.aether.impl.ArtifactDescriptorReader;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.impl.VersionRangeResolver;
import org.sonatype.aether.impl.VersionResolver;
public class DefaultServiceLocator
    extends org.sonatype.aether.impl.internal.DefaultServiceLocator
{
    public DefaultServiceLocator()
    {
        addService( ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class );
        addService( VersionResolver.class, DefaultVersionResolver.class );
        addService( VersionRangeResolver.class, DefaultVersionRangeResolver.class );
        addService( MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class );
        addService( MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class );
    }
}
