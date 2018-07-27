package org.apache.maven.project;
import java.util.List;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.aether.graph.DependencyFilter;
public interface ProjectRealmCache
{
    public static class CacheRecord
    {
        public final ClassRealm realm;
        public final DependencyFilter extensionArtifactFilter;
        public CacheRecord( ClassRealm realm, DependencyFilter extensionArtifactFilter )
        {
            this.realm = realm;
            this.extensionArtifactFilter = extensionArtifactFilter;
        }
    }
    CacheRecord get( List<? extends ClassRealm> extensionRealms );
    CacheRecord put( List<? extends ClassRealm> extensionRealms, ClassRealm projectRealm,
                     DependencyFilter extensionArtifactFilter );
    void flush();
    void register( MavenProject project, CacheRecord record );
}
