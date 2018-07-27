package org.apache.maven;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.classworlds.ClassWorld;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import java.io.File;
public class MavenTestUtils
{
    private MavenTestUtils()
    {
    }
    public static PlexusContainer getContainerInstance()
    {
        return new DefaultPlexusContainer();
    }
    public static void customizeContext( PlexusContainer container, File basedir, File mavenHome, File mavenHomeLocal )
        throws Exception
    {
        ClassWorld classWorld = new ClassWorld();
        ClassRealm rootClassRealm = classWorld.newRealm( "root", Thread.currentThread().getContextClassLoader() );
        container.addContextValue( "rootClassRealm", rootClassRealm );
        container.addContextValue( "maven.home", mavenHome.getAbsolutePath() );
        container.addContextValue( "maven.home.local", mavenHomeLocal.getAbsolutePath() );
    }
}
