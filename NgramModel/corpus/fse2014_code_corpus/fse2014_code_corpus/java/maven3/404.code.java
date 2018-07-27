package org.apache.maven.lifecycle.mapping;
import java.util.List;
import java.util.Map;
public class Lifecycle
{
    private String id;
    private Map<String, String> phases;
    @SuppressWarnings( "unused" )
    private List<String> optionalMojos;
    public String getId()
    {
        return this.id;
    }
    public Map<String, String> getPhases()
    {
        return this.phases;
    }
    public void setId( String id )
    {
        this.id = id;
    }
    public void setPhases( Map<String, String> phases )
    {
        this.phases = phases;
    } 
}
