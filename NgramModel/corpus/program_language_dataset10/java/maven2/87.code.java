package org.apache.maven.artifact.repository.metadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import java.util.Iterator;
import java.util.List;
public class GroupRepositoryMetadata
    extends AbstractRepositoryMetadata
{
    private final String groupId;
    public GroupRepositoryMetadata( String groupId )
    {
        super( new Metadata() );
        this.groupId = groupId;
    }
    public boolean storedInGroupDirectory()
    {
        return true;
    }
    public boolean storedInArtifactVersionDirectory()
    {
        return false;
    }
    public String getGroupId()
    {
        return groupId;
    }
    public String getArtifactId()
    {
        return null;
    }
    public String getBaseVersion()
    {
        return null;
    }
    public void addPluginMapping( String goalPrefix, String artifactId )
    {
        addPluginMapping( goalPrefix, artifactId, artifactId );    
    }
    public void addPluginMapping( String goalPrefix, String artifactId, String name )
    {
        List plugins = getMetadata().getPlugins();
        boolean found = false;
        for ( Iterator i = plugins.iterator(); i.hasNext() && !found; )
        {
            Plugin plugin = (Plugin) i.next();
            if ( plugin.getPrefix().equals( goalPrefix ) )
            {
                found = true;
            }
        }
        if ( !found )
        {
            Plugin plugin = new Plugin();
            plugin.setPrefix( goalPrefix );
            plugin.setArtifactId( artifactId );
            plugin.setName( name );
            getMetadata().addPlugin( plugin );
        }
    }
    public Object getKey()
    {
        return groupId;
    }
    public boolean isSnapshot()
    {
        return false;
    }
    public void setRepository( ArtifactRepository remoteRepository )
    {
    }
}
