package org.apache.maven.plugin;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
public class MavenPluginValidator
{
    private final Artifact pluginArtifact;
    private List<String> errors = new ArrayList<String>();
    private boolean firstDescriptor = true;
    public MavenPluginValidator( Artifact pluginArtifact )
    {
        this.pluginArtifact = pluginArtifact;
    }
    public void validate( PluginDescriptor pluginDescriptor )
    {
        if ( !firstDescriptor )
        {
            return;
        }
        firstDescriptor = false;
        if ( !pluginArtifact.getGroupId().equals( pluginDescriptor.getGroupId() ) )
        {
            errors.add( "Plugin's descriptor contains the wrong group ID: " + pluginDescriptor.getGroupId() );
        }
        if ( !pluginArtifact.getArtifactId().equals( pluginDescriptor.getArtifactId() ) )
        {
            errors.add( "Plugin's descriptor contains the wrong artifact ID: " + pluginDescriptor.getArtifactId() );
        }
        if ( !pluginArtifact.getBaseVersion().equals( pluginDescriptor.getVersion() ) )
        {
            errors.add( "Plugin's descriptor contains the wrong version: " + pluginDescriptor.getVersion() );
        }
    }
    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }
    public List<String> getErrors()
    {
        return errors;
    }
}
