package org.apache.maven.model.management;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.merge.MavenModelMerger;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = PluginManagementInjector.class )
public class DefaultPluginManagementInjector
    implements PluginManagementInjector
{
    private ManagementModelMerger merger = new ManagementModelMerger();
    public void injectManagement( Model model, ModelBuildingRequest request, ModelProblemCollector problems )
    {
        merger.mergeManagedBuildPlugins( model );
    }
    private static class ManagementModelMerger
        extends MavenModelMerger
    {
        public void mergeManagedBuildPlugins( Model model )
        {
            Build build = model.getBuild();
            if ( build != null )
            {
                PluginManagement pluginManagement = build.getPluginManagement();
                if ( pluginManagement != null )
                {
                    mergePluginContainer_Plugins( build, pluginManagement );
                }
            }
        }
        private void mergePluginContainer_Plugins( PluginContainer target, PluginContainer source )
        {
            List<Plugin> src = source.getPlugins();
            if ( !src.isEmpty() )
            {
                List<Plugin> tgt = target.getPlugins();
                Map<Object, Plugin> managedPlugins = new LinkedHashMap<Object, Plugin>( src.size() * 2 );
                Map<Object, Object> context = Collections.emptyMap();
                for ( Plugin element : src )
                {
                    Object key = getPluginKey( element );
                    managedPlugins.put( key, element );
                }
                for ( Plugin element : tgt )
                {
                    Object key = getPluginKey( element );
                    Plugin managedPlugin = managedPlugins.get( key );
                    if ( managedPlugin != null )
                    {
                        mergePlugin( element, managedPlugin, false, context );
                    }
                }
            }
        }
        @Override
        protected void mergePlugin_Executions( Plugin target, Plugin source, boolean sourceDominant,
                                               Map<Object, Object> context )
        {
            List<PluginExecution> src = source.getExecutions();
            if ( !src.isEmpty() )
            {
                List<PluginExecution> tgt = target.getExecutions();
                Map<Object, PluginExecution> merged =
                    new LinkedHashMap<Object, PluginExecution>( ( src.size() + tgt.size() ) * 2 );
                for ( PluginExecution element : src )
                {
                    Object key = getPluginExecutionKey( element );
                    merged.put( key, element.clone() );
                }
                for ( PluginExecution element : tgt )
                {
                    Object key = getPluginExecutionKey( element );
                    PluginExecution existing = merged.get( key );
                    if ( existing != null )
                    {
                        mergePluginExecution( element, existing, sourceDominant, context );
                    }
                    merged.put( key, element );
                }
                target.setExecutions( new ArrayList<PluginExecution>( merged.values() ) );
            }
        }
    }
}
