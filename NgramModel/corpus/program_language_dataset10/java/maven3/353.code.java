package org.apache.maven.lifecycle;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Component( role = DefaultLifecycles.class )
public class DefaultLifecycles
{
    public static final String[] STANDARD_LIFECYCLES = { "default", "clean", "site" };
    @Requirement( role = Lifecycle.class )
    private Map<String, Lifecycle> lifecycles;
    @Requirement
    private Logger logger;
    @SuppressWarnings( { "UnusedDeclaration" } )
    public DefaultLifecycles()
    {
    }
    public DefaultLifecycles( Map<String, Lifecycle> lifecycles, Logger logger )
    {
        this.lifecycles = new LinkedHashMap<String, Lifecycle>();
        this.logger = logger;
        this.lifecycles = lifecycles;
    }
    public Lifecycle get( String key )
    {
        return getPhaseToLifecycleMap().get( key );
    }
    public Map<String, Lifecycle> getPhaseToLifecycleMap()
    {
        HashMap<String, Lifecycle> phaseToLifecycleMap = new HashMap<String, Lifecycle>();
        for ( Lifecycle lifecycle : getLifeCycles() )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Lifecycle " + lifecycle );
            }
            for ( String phase : lifecycle.getPhases() )
            {
                if ( !phaseToLifecycleMap.containsKey( phase ) )
                {
                    phaseToLifecycleMap.put( phase, lifecycle );
                }
                else
                {
                    Lifecycle original = phaseToLifecycleMap.get( phase );
                    logger.warn( "Duplicated lifecycle phase " + phase + ". Defined in " + original.getId()
                        + " but also in " + lifecycle.getId() );
                }
            }
        }
        return phaseToLifecycleMap;
    }
    public List<Lifecycle> getLifeCycles()
    {
        Map<String, Lifecycle> lifecycles = new LinkedHashMap<String, Lifecycle>( this.lifecycles );
        LinkedHashSet<String> lifecycleNames = new LinkedHashSet<String>( Arrays.asList( STANDARD_LIFECYCLES ) );
        lifecycleNames.addAll( lifecycles.keySet() );
        ArrayList<Lifecycle> result = new ArrayList<Lifecycle>();
        for ( String name : lifecycleNames )
        {
            result.add( lifecycles.get( name ) );
        }
        return result;
    }
    public String getLifecyclePhaseList()
    {
        Set<String> phases = new LinkedHashSet<String>();
        for ( Lifecycle lifecycle : lifecycles.values() )
        {
            phases.addAll( lifecycle.getPhases() );
        }
        return StringUtils.join( phases.iterator(), ", " );
    }
}
