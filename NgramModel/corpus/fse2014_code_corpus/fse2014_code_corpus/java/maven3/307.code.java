package org.apache.maven.artifact.resolver.filter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
public class CumulativeScopeArtifactFilter
    extends AbstractScopeArtifactFilter
{
    private Set<String> scopes;
    public CumulativeScopeArtifactFilter( Collection<String> scopes )
    {
        this.scopes = new HashSet<String>();
        addScopes( scopes );
    }
    public CumulativeScopeArtifactFilter( CumulativeScopeArtifactFilter... filters )
    {
        this.scopes = new HashSet<String>();
        if ( filters != null )
        {
            for ( CumulativeScopeArtifactFilter filter : filters )
            {
                addScopes( filter.getScopes() );
            }
        }
    }
    private void addScopes( Collection<String> scopes )
    {
        if ( scopes != null )
        {
            for ( String scope : scopes )
            {
                addScope( scope );
            }
        }
    }
    private void addScope( String scope )
    {
        this.scopes.add( scope );
        addScopeInternal( scope );
    }
    public Set<String> getScopes()
    {
        return scopes;
    }
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + scopes.hashCode();
        return hash;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !( obj instanceof CumulativeScopeArtifactFilter ) )
        {
            return false;
        }
        CumulativeScopeArtifactFilter that = (CumulativeScopeArtifactFilter) obj;
        return scopes.equals( that.scopes );
    }
}
