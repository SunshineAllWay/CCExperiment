package org.apache.maven.plugin;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.codehaus.plexus.component.repository.exception.ComponentRepositoryException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
public class PluginManagerException
    extends Exception
{
    private final String pluginGroupId;
    private final String pluginArtifactId;
    private final String pluginVersion;
    private String goal;
    private MavenProject project;
    protected PluginManagerException( Plugin plugin, String message, MavenProject project, Throwable cause )
    {
        super( message, cause );
        this.project = project;
        pluginGroupId = plugin.getGroupId();
        pluginArtifactId = plugin.getArtifactId();
        pluginVersion = plugin.getVersion();
    }
    public PluginManagerException( Plugin plugin, String message, Throwable cause )
    {
        super( message, cause );
        pluginGroupId = plugin.getGroupId();
        pluginArtifactId = plugin.getArtifactId();
        pluginVersion = plugin.getVersion();
    }
    protected PluginManagerException( MojoDescriptor mojoDescriptor, String message, Throwable cause )
    {
        super( message, cause );
        pluginGroupId = mojoDescriptor.getPluginDescriptor().getGroupId();
        pluginArtifactId = mojoDescriptor.getPluginDescriptor().getArtifactId();
        pluginVersion = mojoDescriptor.getPluginDescriptor().getVersion();
        goal = mojoDescriptor.getGoal();
    }
    protected PluginManagerException( MojoDescriptor mojoDescriptor, MavenProject project, String message )
    {
        super( message );
        this.project = project;
        pluginGroupId = mojoDescriptor.getPluginDescriptor().getGroupId();
        pluginArtifactId = mojoDescriptor.getPluginDescriptor().getArtifactId();
        pluginVersion = mojoDescriptor.getPluginDescriptor().getVersion();
        goal = mojoDescriptor.getGoal();
    }
    protected PluginManagerException( MojoDescriptor mojoDescriptor, MavenProject project, String message,
                                      Throwable cause )
    {
        super( message, cause );
        this.project = project;
        pluginGroupId = mojoDescriptor.getPluginDescriptor().getGroupId();
        pluginArtifactId = mojoDescriptor.getPluginDescriptor().getArtifactId();
        pluginVersion = mojoDescriptor.getPluginDescriptor().getVersion();
        goal = mojoDescriptor.getGoal();
    }
    public PluginManagerException( Plugin plugin, InvalidVersionSpecificationException cause )
    {
        super( cause );
        pluginGroupId = plugin.getGroupId();
        pluginArtifactId = plugin.getArtifactId();
        pluginVersion = plugin.getVersion();
    }
    public PluginManagerException( Plugin plugin, String message, PlexusConfigurationException cause )
    {
        super( message, cause );
        pluginGroupId = plugin.getGroupId();
        pluginArtifactId = plugin.getArtifactId();
        pluginVersion = plugin.getVersion();
    }
    public PluginManagerException( Plugin plugin, String message, ComponentRepositoryException cause )
    {
        super( message, cause );
        pluginGroupId = plugin.getGroupId();
        pluginArtifactId = plugin.getArtifactId();
        pluginVersion = plugin.getVersion();
    }
    public PluginManagerException( MojoDescriptor mojoDescriptor, MavenProject project, String message,
                                   NoSuchRealmException cause )
    {
        super( message, cause );
        this.project = project;
        pluginGroupId = mojoDescriptor.getPluginDescriptor().getGroupId();
        pluginArtifactId = mojoDescriptor.getPluginDescriptor().getArtifactId();
        pluginVersion = mojoDescriptor.getPluginDescriptor().getVersion();
        goal = mojoDescriptor.getGoal();
    }
    public PluginManagerException( MojoDescriptor mojoDescriptor, String message, MavenProject project,
                                   PlexusContainerException cause )
    {
        super( message, cause );
        this.project = project;
        PluginDescriptor pd = mojoDescriptor.getPluginDescriptor();
        pluginGroupId = pd.getGroupId();
        pluginArtifactId = pd.getArtifactId();
        pluginVersion = pd.getVersion();
        goal = mojoDescriptor.getGoal();
    }
    public PluginManagerException( Plugin plugin, String message, PlexusContainerException cause )
    {
        super( message, cause );
        pluginGroupId = plugin.getGroupId();
        pluginArtifactId = plugin.getArtifactId();
        pluginVersion = plugin.getVersion();
    }
    public PluginManagerException( Plugin plugin, String message, MavenProject project )
    {
        super( message );
        pluginGroupId = plugin.getGroupId();
        pluginArtifactId = plugin.getArtifactId();
        pluginVersion = plugin.getVersion();
        this.project = project;
    }
    public String getPluginGroupId()
    {
        return pluginGroupId;
    }
    public String getPluginArtifactId()
    {
        return pluginArtifactId;
    }
    public String getPluginVersion()
    {
        return pluginVersion;
    }
    public String getGoal()
    {
        return goal;
    }
    public MavenProject getProject()
    {
        return project;
    }
}
