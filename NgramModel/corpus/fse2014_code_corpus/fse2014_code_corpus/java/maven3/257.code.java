package org.apache.maven;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExclusionSetFilter;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
@Component(role = ArtifactFilterManager.class)
public class DefaultArtifactFilterManager 
    implements ArtifactFilterManager
{
    private static final Set<String> DEFAULT_EXCLUSIONS;
    @Requirement
    private PlexusContainer plexus;
    static
    {
        Set<String> artifacts = new HashSet<String>();
        artifacts.add( "classworlds:classworlds" );
        artifacts.add( "org.codehaus.plexus:plexus-classworlds" );
        artifacts.add( "org.codehaus.plexus:plexus-component-api" );
        artifacts.add( "org.codehaus.plexus:plexus-container-default" );
        artifacts.add( "plexus:plexus-container-default" );
        artifacts.add( "org.sonatype.spice:spice-inject-plexus" );
        artifacts.add( "org.sonatype.sisu:sisu-inject-plexus" );
        artifacts.add( "org.apache.maven:maven-artifact" );
        artifacts.add( "org.apache.maven:maven-aether-provider" );
        artifacts.add( "org.apache.maven:maven-artifact-manager" );
        artifacts.add( "org.apache.maven:maven-compat" );
        artifacts.add( "org.apache.maven:maven-core" );
        artifacts.add( "org.apache.maven:maven-error-diagnostics" );
        artifacts.add( "org.apache.maven:maven-lifecycle" );
        artifacts.add( "org.apache.maven:maven-model" );
        artifacts.add( "org.apache.maven:maven-model-builder" );
        artifacts.add( "org.apache.maven:maven-monitor" );
        artifacts.add( "org.apache.maven:maven-plugin-api" );
        artifacts.add( "org.apache.maven:maven-plugin-descriptor" );
        artifacts.add( "org.apache.maven:maven-plugin-parameter-documenter" );
        artifacts.add( "org.apache.maven:maven-plugin-registry" );
        artifacts.add( "org.apache.maven:maven-profile" );
        artifacts.add( "org.apache.maven:maven-project" );
        artifacts.add( "org.apache.maven:maven-repository-metadata" );
        artifacts.add( "org.apache.maven:maven-settings" );
        artifacts.add( "org.apache.maven:maven-settings-builder" );
        artifacts.add( "org.apache.maven:maven-toolchain" );
        artifacts.add( "org.apache.maven.wagon:wagon-provider-api" );
        artifacts.add( "org.sonatype.aether:aether-api" );
        artifacts.add( "org.sonatype.aether:aether-spi" );
        artifacts.add( "org.sonatype.aether:aether-impl" );
        DEFAULT_EXCLUSIONS = Collections.unmodifiableSet( artifacts);
    }
    protected Set<String> excludedArtifacts = new HashSet<String>( DEFAULT_EXCLUSIONS );
    public static ArtifactFilter createStandardFilter()
    {
        return new ExclusionSetFilter( DEFAULT_EXCLUSIONS );
    }
    public ArtifactFilter getArtifactFilter()
    {
        Set<String> excludes = new LinkedHashSet<String>( excludedArtifacts );
        for ( ArtifactFilterManagerDelegate delegate : getDelegates() )
        {
            delegate.addExcludes( excludes );
        }
        return new ExclusionSetFilter( excludes );
    }
    public ArtifactFilter getCoreArtifactFilter()
    {
        return new ExclusionSetFilter( getCoreArtifactExcludes() );
    }
    private List<ArtifactFilterManagerDelegate> getDelegates()
    {
        try
        {
            return plexus.lookupList( ArtifactFilterManagerDelegate.class );
        }
        catch ( ComponentLookupException e )
        {
            return new ArrayList<ArtifactFilterManagerDelegate>();
        }
    }
    public void excludeArtifact( String artifactId )
    {
        excludedArtifacts.add( artifactId );
    }
    public Set<String> getCoreArtifactExcludes()
    {
        Set<String> excludes = new LinkedHashSet<String>( DEFAULT_EXCLUSIONS );
        for ( ArtifactFilterManagerDelegate delegate : getDelegates() )
        {
            delegate.addCoreExcludes( excludes );
        }
        return excludes;
    }
}
