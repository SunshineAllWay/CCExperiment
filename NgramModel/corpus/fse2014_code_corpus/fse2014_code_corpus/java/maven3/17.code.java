package org.apache.maven.repository.internal;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.impl.MetadataGenerator;
import org.sonatype.aether.impl.MetadataGeneratorFactory;
import org.sonatype.aether.installation.InstallRequest;
@Component( role = MetadataGeneratorFactory.class, hint = "versions" )
public class VersionsMetadataGeneratorFactory
    implements MetadataGeneratorFactory
{
    public MetadataGenerator newInstance( RepositorySystemSession session, InstallRequest request )
    {
        return new VersionsMetadataGenerator( session, request );
    }
    public MetadataGenerator newInstance( RepositorySystemSession session, DeployRequest request )
    {
        return new VersionsMetadataGenerator( session, request );
    }
    public int getPriority()
    {
        return 5;
    }
}
