package org.apache.maven.plugin;
import java.util.List;
import org.apache.maven.project.ExtensionDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.aether.artifact.Artifact;
public interface ExtensionRealmCache
{
    public static class CacheRecord
    {
        public final ClassRealm realm;
        public final ExtensionDescriptor desciptor;
        public CacheRecord( ClassRealm realm, ExtensionDescriptor descriptor )
        {
            this.realm = realm;
            this.desciptor = descriptor;
        }
    }
    CacheRecord get( List<? extends Artifact> extensionArtifacts );
    CacheRecord put( List<? extends Artifact> extensionArtifacts, ClassRealm extensionRealm,
                     ExtensionDescriptor extensionDescriptor );
    void flush();
    void register( MavenProject project, CacheRecord record );
}
