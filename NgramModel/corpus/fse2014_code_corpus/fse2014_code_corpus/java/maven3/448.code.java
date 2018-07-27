package org.apache.maven.plugin.internal;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.collection.DependencyCollectionContext;
import org.sonatype.aether.collection.DependencySelector;
import org.sonatype.aether.graph.Dependency;
class WagonExcluder
    implements DependencySelector
{
    private final boolean coreArtifact;
    public WagonExcluder()
    {
        this( false );
    }
    private WagonExcluder( boolean coreArtifact )
    {
        this.coreArtifact = coreArtifact;
    }
    public boolean selectDependency( Dependency dependency )
    {
        return !coreArtifact || !isWagonProvider( dependency.getArtifact() );
    }
    public DependencySelector deriveChildSelector( DependencyCollectionContext context )
    {
        if ( coreArtifact || !isLegacyCoreArtifact( context.getDependency().getArtifact() ) )
        {
            return this;
        }
        else
        {
            return new WagonExcluder( true );
        }
    }
    private boolean isLegacyCoreArtifact( Artifact artifact )
    {
        String version = artifact.getVersion();
        return version != null && version.startsWith( "2." ) && artifact.getArtifactId().startsWith( "maven-" )
            && artifact.getGroupId().equals( "org.apache.maven" );
    }
    private boolean isWagonProvider( Artifact artifact )
    {
        if ( "org.apache.maven.wagon".equals( artifact.getGroupId() ) )
        {
            return artifact.getArtifactId().startsWith( "wagon-" );
        }
        return false;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( obj == this )
        {
            return true;
        }
        else if ( obj == null || !getClass().equals( obj.getClass() ) )
        {
            return false;
        }
        WagonExcluder that = (WagonExcluder) obj;
        return coreArtifact == that.coreArtifact;
    }
    @Override
    public int hashCode()
    {
        int hash = getClass().hashCode();
        hash = hash * 31 + ( coreArtifact ? 1 : 0 );
        return hash;
    }
}
