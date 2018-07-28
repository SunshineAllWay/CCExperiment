package org.apache.maven.execution;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.plexus.util.StringUtils;
@Deprecated
@Component( role = RuntimeInformation.class )
public class DefaultRuntimeInformation
    implements RuntimeInformation, Initializable
{
    @Requirement
    private org.apache.maven.rtinfo.RuntimeInformation rtInfo;
    private ArtifactVersion applicationVersion;
    public ArtifactVersion getApplicationVersion()
    {
        return applicationVersion;
    }
    public void initialize()
        throws InitializationException
    {
        String mavenVersion = rtInfo.getMavenVersion();
        if ( StringUtils.isEmpty( mavenVersion ) )
        {
            throw new InitializationException( "Unable to read Maven version from maven-core" );
        }
        applicationVersion = new DefaultArtifactVersion( mavenVersion );
    }
}
