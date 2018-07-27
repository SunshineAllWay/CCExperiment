package org.apache.maven.toolchain;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.maven.toolchain.model.ToolchainModel;
import org.codehaus.plexus.logging.Logger;
public abstract class DefaultToolchain
    implements Toolchain, ToolchainPrivate
{
    private String type;
    private Map<String, RequirementMatcher> provides = new HashMap<String, RequirementMatcher>();
    public static final String KEY_TYPE = "type"; 
    private ToolchainModel model;
    private Logger logger;
    protected DefaultToolchain( ToolchainModel model, Logger logger )
    {
        this.model = model;
        this.logger = logger;
    }
    protected DefaultToolchain( ToolchainModel model, String type, Logger logger )
    {
        this( model, logger );
        this.type = type;
    }
    public final String getType()
    {
        return type != null ? type : model.getType();
    }
    public final ToolchainModel getModel()
    {
        return model;
    }
    public final void addProvideToken( String type,
                                       RequirementMatcher matcher )
    {
        provides.put( type, matcher );
    }
    public boolean matchesRequirements(Map requirements)
    {
        Iterator it = requirements.keySet().iterator();
        while ( it.hasNext() )
        {
            String key = (String) it.next();
            RequirementMatcher matcher = provides.get( key );
            if ( matcher == null )
            {
                getLog().debug( "Toolchain "  + this + " is missing required property: "  + key );
                return false;
            }
            if ( !matcher.matches( (String) requirements.get(key) ) )
            {
                getLog().debug( "Toolchain "  + this + " doesn't match required property: "  + key );
                return false;
            }
        }
        return true;
    }
    protected Logger getLog()
    {
        return logger;
    }
}