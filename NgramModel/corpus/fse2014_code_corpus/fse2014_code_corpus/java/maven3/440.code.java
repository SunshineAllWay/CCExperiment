package org.apache.maven.plugin.internal;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.RepositorySystemSession;
@Component( role = LegacySupport.class )
public class DefaultLegacySupport
    implements LegacySupport
{
    private static final ThreadLocal<MavenSession[]> session = new InheritableThreadLocal<MavenSession[]>();
    public void setSession( MavenSession session )
    {
        if ( session == null )
        {
            MavenSession[] oldSession = DefaultLegacySupport.session.get();
            if ( oldSession != null )
            {
                oldSession[0] = null;
                DefaultLegacySupport.session.remove();
            }
        }
        else
        {
            DefaultLegacySupport.session.set( new MavenSession[] { session } );
        }
    }
    public MavenSession getSession()
    {
        MavenSession[] currentSession = DefaultLegacySupport.session.get();
        return currentSession != null ? currentSession[0] : null;
    }
    public RepositorySystemSession getRepositorySession()
    {
        MavenSession session = getSession();
        return ( session != null ) ? session.getRepositorySession() : null;
    }
}
