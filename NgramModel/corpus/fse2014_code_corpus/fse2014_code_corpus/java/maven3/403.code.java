package org.apache.maven.lifecycle.mapping;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class DefaultLifecycleMapping
    implements LifecycleMapping
{
    private List<Lifecycle> lifecycles;
    private Map<String, Lifecycle> lifecycleMap;
    private Map<String, String> phases;
    private void initLifecycleMap()
    {
        if ( lifecycleMap == null )
        {
            lifecycleMap = new HashMap<String, Lifecycle>();
            if ( lifecycles != null )
            {
                for ( Lifecycle lifecycle : lifecycles )
                {
                    lifecycleMap.put( lifecycle.getId(), lifecycle );
                }
            }
            else
            {
                String[] lifecycleIds = { "default", "clean", "site" };
                for ( String lifecycleId : lifecycleIds )
                {
                    Map<String, String> phases = getPhases( lifecycleId );
                    if ( phases != null )
                    {
                        Lifecycle lifecycle = new Lifecycle();
                        lifecycle.setId( lifecycleId );
                        lifecycle.setPhases( phases );
                        lifecycleMap.put( lifecycleId, lifecycle );
                    }
                }
            }
        }
    }
    public Map<String, Lifecycle> getLifecycles()
    {
        initLifecycleMap();
        return lifecycleMap;
    }
    public List<String> getOptionalMojos( String lifecycle )
    {
        return null;
    }
    public Map<String, String> getPhases( String lifecycle )
    {
        initLifecycleMap();
        Lifecycle lifecycleMapping = lifecycleMap.get( lifecycle );
        if ( lifecycleMapping != null )
        {
            return lifecycleMapping.getPhases();
        }
        else if ( "default".equals( lifecycle ) )
        {
            return phases;
        }
        else
        {
            return null;
        }
    }
}
