package org.apache.maven.lifecycle.mapping;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class DefaultLifecycleMapping
    implements LifecycleMapping
{
    private List lifecycles;
    private Map lifecycleMap;
    private Map phases;
    public List getOptionalMojos( String lifecycle )
    {
        if ( lifecycleMap == null )
        {
            lifecycleMap = new HashMap();
            if ( lifecycles != null )
            {
                for ( Iterator i = lifecycles.iterator(); i.hasNext(); )
                {
                    Lifecycle l = (Lifecycle) i.next();
                    lifecycleMap.put( l.getId(), l );
                }
            }
        }
        Lifecycle l = (Lifecycle) lifecycleMap.get( lifecycle );
        if ( l != null )
        {
            return l.getOptionalMojos();
        }
        else
        {
            return null;
        }
    }
    public Map getPhases( String lifecycle )
    {
        if ( lifecycleMap == null )
        {
            lifecycleMap = new HashMap();
            if ( lifecycles != null )
            {
                for ( Iterator i = lifecycles.iterator(); i.hasNext(); )
                {
                    Lifecycle l = (Lifecycle) i.next();
                    lifecycleMap.put( l.getId(), l );
                }
            }
        }
        Lifecycle l = (Lifecycle) lifecycleMap.get( lifecycle );
        Map mappings = null;
        if ( l == null )
        {
            if ( "default".equals( lifecycle ) )
            {
                mappings = phases;
            }
        }
        else
        {
            mappings = l.getPhases();
        }
        return mappings;
    }
}
