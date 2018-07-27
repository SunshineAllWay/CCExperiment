package org.apache.maven.project;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
class DefaultDependencyResolutionResult
    implements DependencyResolutionResult
{
    private DependencyNode root;
    private List<Dependency> dependencies = new ArrayList<Dependency>();
    private List<Dependency> resolvedDependencies = new ArrayList<Dependency>();
    private List<Dependency> unresolvedDependencies = new ArrayList<Dependency>();
    private List<Exception> collectionErros = new ArrayList<Exception>();
    private Map<Dependency, List<Exception>> resolutionErrors = new IdentityHashMap<Dependency, List<Exception>>();
    public DependencyNode getDependencyGraph()
    {
        return root;
    }
    public void setDependencyGraph( DependencyNode root )
    {
        this.root = root;
    }
    public List<Dependency> getDependencies()
    {
        return dependencies;
    }
    public List<Dependency> getResolvedDependencies()
    {
        return resolvedDependencies;
    }
    public void addResolvedDependency( Dependency dependency )
    {
        dependencies.add( dependency );
        resolvedDependencies.add( dependency );
    }
    public List<Dependency> getUnresolvedDependencies()
    {
        return unresolvedDependencies;
    }
    public List<Exception> getCollectionErrors()
    {
        return collectionErros;
    }
    public void setCollectionErrors( List<Exception> exceptions )
    {
        if ( exceptions != null )
        {
            this.collectionErros = exceptions;
        }
        else
        {
            this.collectionErros = new ArrayList<Exception>();
        }
    }
    public List<Exception> getResolutionErrors( Dependency dependency )
    {
        List<Exception> errors = resolutionErrors.get( dependency );
        return ( errors != null ) ? errors : Collections.<Exception> emptyList();
    }
    public void setResolutionErrors( Dependency dependency, List<Exception> errors )
    {
        dependencies.add( dependency );
        unresolvedDependencies.add( dependency );
        resolutionErrors.put( dependency, errors );
    }
}
