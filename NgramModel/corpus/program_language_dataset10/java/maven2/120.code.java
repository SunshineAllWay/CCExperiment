package org.apache.maven;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExclusionSetFilter;
import java.util.Set;
import java.util.HashSet;
public class MavenArtifactFilterManager
{
    public static ArtifactFilter createStandardFilter()
    {
        Set artifacts = createBaseArtifactSet();
        artifacts.add( "wagon-file" );
        artifacts.add( "wagon-http-lightweight" );
        artifacts.add( "wagon-webdav" );
        artifacts.add( "wagon-ssh" );
        artifacts.add( "wagon-ssh-external" );
        artifacts.add( "wagon-ssh-common" );
        artifacts.add( "wagon-http-shared" );
        artifacts.add( "wagon-webdav-jackrabbit" );
        return new ExclusionSetFilter( artifacts );
    }
    public static ArtifactFilter createExtensionFilter()
    {
        Set artifacts = createBaseArtifactSet();
        return new ExclusionSetFilter( artifacts );
    }
    private static Set createBaseArtifactSet()
    {
        Set artifacts = new HashSet();
        artifacts.add( "classworlds" );
        artifacts.add( "jsch" );
        artifacts.add( "doxia-sink-api" );
        artifacts.add( "doxia-logging-api" );
        artifacts.add( "maven-artifact" );
        artifacts.add( "maven-artifact-manager" );
        artifacts.add( "maven-core" );
        artifacts.add( "maven-error-diagnoser" );
        artifacts.add( "maven-model" );
        artifacts.add( "maven-monitor" );
        artifacts.add( "maven-plugin-api" );
        artifacts.add( "maven-plugin-descriptor" );
        artifacts.add( "maven-plugin-parameter-documenter" );
        artifacts.add( "maven-plugin-registry" );
        artifacts.add( "maven-profile" );
        artifacts.add( "maven-project" );
        artifacts.add( "maven-reporting-api" );
        artifacts.add( "maven-repository-metadata" );
        artifacts.add( "maven-settings" );
        artifacts.add( "plexus-container-default" );
        artifacts.add( "plexus-interactivity-api" );
        artifacts.add( "maven-toolchain" );
        artifacts.add( "wagon-provider-api" );
        artifacts.add( "plexus-component-api" );
        return artifacts;
    }
}
