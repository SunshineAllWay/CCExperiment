package org.apache.maven.artifact.resolver.filter;
public class ScopeArtifactFilter
    extends AbstractScopeArtifactFilter
{
    private final String scope;
    public ScopeArtifactFilter( String scope )
    {
        this.scope = scope;
        addScopeInternal( scope );
    }
    public String getScope()
    {
        return scope;
    }
    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + ( scope != null ? scope.hashCode() : 0 );
        return hash;
    }
    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( !( obj instanceof ScopeArtifactFilter ) )
        {
            return false;
        }
        ScopeArtifactFilter other = (ScopeArtifactFilter) obj;
        return equals( scope, other.scope );
    }
    private static <T> boolean equals( T str1, T str2 )
    {
        return str1 != null ? str1.equals( str2 ) : str2 == null;
    }
}
