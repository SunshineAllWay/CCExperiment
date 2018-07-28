package org.apache.maven.lifecycle.mapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class Lifecycle
{
    private String id;
    private Map phases;
    private List optionalMojos = new ArrayList();
    public String getId()
    {
        return this.id;
    } 
    public Map getPhases()
    {
        return this.phases;
    }
    public void setId( String id )
    {
        this.id = id;
    } 
    public void addOptionalMojo( String optionalMojo )
    {
        this.optionalMojos.add( optionalMojo );
    }
    public void setOptionalMojos( List optionalMojos )
    {
        this.optionalMojos = optionalMojos;
    }
    public List getOptionalMojos()
    {
        return this.optionalMojos;
    }
}
