package org.apache.maven.artifact;
public enum ArtifactScopeEnum
{
    compile( 1 ), test( 2 ), runtime( 3 ), provided( 4 ), system( 5 ), runtime_plus_system( 6 );
    public static final ArtifactScopeEnum DEFAULT_SCOPE = compile;
    private int id;
    ArtifactScopeEnum( int id )
    {
        this.id = id;
    }
    int getId()
    {
        return id;
    }
    public static final ArtifactScopeEnum checkScope( ArtifactScopeEnum scope )
    {
        return scope == null ? DEFAULT_SCOPE : scope;
    }
    public String getScope()
    {
        if ( id == 1 )
        {
            return Artifact.SCOPE_COMPILE;
        }
        else if ( id == 2 )
        {
            return Artifact.SCOPE_TEST;
        }
        else if ( id == 3 )
        {
            return Artifact.SCOPE_RUNTIME;
        }
        else if ( id == 4 )
        {
            return Artifact.SCOPE_PROVIDED;
        }
        else if ( id == 5 )
        {
            return Artifact.SCOPE_SYSTEM;
        }
        else
        {
            return Artifact.SCOPE_RUNTIME_PLUS_SYSTEM;
        }
    }
    private static final ArtifactScopeEnum [][][] _compliancySets = {
          { { compile  }, { compile,                provided, system } }
        , { { test     }, { compile, test,          provided, system } }
        , { { runtime  }, { compile,       runtime,           system } }
        , { { provided }, { compile, test,          provided         } }
    };
    public boolean encloses( ArtifactScopeEnum scope )
    {
        final ArtifactScopeEnum s = checkScope( scope );
        if ( id == system.id )
        {
            return scope.id == system.id;
        }
        for ( ArtifactScopeEnum[][] set : _compliancySets )
        {
            if ( id == set[0][0].id )
            {
                for ( ArtifactScopeEnum ase : set[1] )
                {
                    if ( s.id == ase.id )
                    {
                        return true;
                    }
                }
                break;
            }
        }
        return false;
    }
}
