package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.codehaus.plexus.logging.Logger;
public class WarningResolutionListener
    implements ResolutionListener
{
    private Logger logger;
    public WarningResolutionListener( Logger logger )
    {
        this.logger = logger;
    }
    public void testArtifact( Artifact node )
    {
    }
    public void startProcessChildren( Artifact artifact )
    {
    }
    public void endProcessChildren( Artifact artifact )
    {
    }
    public void includeArtifact( Artifact artifact )
    {
    }
    public void omitForNearer( Artifact omitted, Artifact kept )
    {
    }
    public void omitForCycle( Artifact omitted )
    {
    }
    public void updateScopeCurrentPom( Artifact artifact, String scope )
    {
    }
    public void updateScope( Artifact artifact, String scope )
    {
    }
    public void manageArtifact( Artifact artifact, Artifact replacement )
    {
    }
    public void selectVersionFromRange( Artifact artifact )
    {
    }
    public void restrictRange( Artifact artifact, Artifact replacement, VersionRange newRange )
    {
    }
}
