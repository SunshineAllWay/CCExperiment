package org.apache.maven.plugin;
import org.apache.maven.execution.MavenSession;
import org.sonatype.aether.RepositorySystemSession;
public interface LegacySupport
{
    void setSession( MavenSession session );
    MavenSession getSession();
    RepositorySystemSession getRepositorySession();
}
