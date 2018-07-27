package org.apache.maven.model.building;
import java.util.List;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
class ModelData
{
    private Model model;
    private Model rawModel;
    private List<Profile> activeProfiles;
    private String groupId;
    private String artifactId;
    private String version;
    public ModelData( Model model )
    {
        this.model = model;
    }
    public ModelData( Model model, String groupId, String artifactId, String version )
    {
        this.model = model;
        setGroupId( groupId );
        setArtifactId( artifactId );
        setVersion( version );
    }
    public Model getModel()
    {
        return model;
    }
    public void setModel( Model model )
    {
        this.model = model;
    }
    public Model getRawModel()
    {
        return rawModel;
    }
    public void setRawModel( Model rawModel )
    {
        this.rawModel = rawModel;
    }
    public List<Profile> getActiveProfiles()
    {
        return activeProfiles;
    }
    public void setActiveProfiles( List<Profile> activeProfiles )
    {
        this.activeProfiles = activeProfiles;
    }
    public String getGroupId()
    {
        return ( groupId != null ) ? groupId : "";
    }
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }
    public String getArtifactId()
    {
        return ( artifactId != null ) ? artifactId : "";
    }
    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }
    public String getVersion()
    {
        return ( version != null ) ? version : "";
    }
    public void setVersion( String version )
    {
        this.version = version;
    }
    public String getId()
    {
        StringBuilder buffer = new StringBuilder( 96 );
        buffer.append( getGroupId() ).append( ':' ).append( getArtifactId() ).append( ':' ).append( getVersion() );
        return buffer.toString();
    }
    @Override
    public String toString()
    {
        return String.valueOf( model );
    }
}
