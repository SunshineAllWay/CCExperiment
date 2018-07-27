package org.apache.maven.project;
import java.util.Date;
import java.util.Properties;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.profiles.ProfileManager;
@Deprecated
public interface ProjectBuilderConfiguration
{
    ArtifactRepository getLocalRepository();
    ProfileManager getGlobalProfileManager();
    Properties getUserProperties();
    Properties getExecutionProperties();
    ProjectBuilderConfiguration setGlobalProfileManager( ProfileManager globalProfileManager );
    ProjectBuilderConfiguration setLocalRepository( ArtifactRepository localRepository );
    ProjectBuilderConfiguration setUserProperties( Properties userProperties );
    ProjectBuilderConfiguration setExecutionProperties( Properties executionProperties );
    Date getBuildStartTime();
    ProjectBuilderConfiguration setBuildStartTime( Date buildStartTime );
}
