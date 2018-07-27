package org.apache.maven.artifact.resolver.filter;
import org.apache.maven.artifact.Artifact;
abstract class AbstractScopeArtifactFilter
    implements ArtifactFilter
{
    private boolean compileScope;
    private boolean runtimeScope;
    private boolean testScope;
    private boolean providedScope;
    private boolean systemScope;
    void addScopeInternal( String scope )
    {
        if ( Artifact.SCOPE_COMPILE.equals( scope ) )
        {
            systemScope = true;
            providedScope = true;
            compileScope = true;
        }
        else if ( Artifact.SCOPE_RUNTIME.equals( scope ) )
        {
            compileScope = true;
            runtimeScope = true;
        }
        else if ( Artifact.SCOPE_COMPILE_PLUS_RUNTIME.equals( scope ) )
        {
            systemScope = true;
            providedScope = true;
            compileScope = true;
            runtimeScope = true;
        }
        else if ( Artifact.SCOPE_RUNTIME_PLUS_SYSTEM.equals( scope ) )
        {
            systemScope = true;
            compileScope = true;
            runtimeScope = true;
        }
        else if ( Artifact.SCOPE_TEST.equals( scope ) )
        {
            systemScope = true;
            providedScope = true;
            compileScope = true;
            runtimeScope = true;
            testScope = true;
        }
    }
    public boolean include( Artifact artifact )
    {
        if ( Artifact.SCOPE_COMPILE.equals( artifact.getScope() ) )
        {
            return compileScope;
        }
        else if ( Artifact.SCOPE_RUNTIME.equals( artifact.getScope() ) )
        {
            return runtimeScope;
        }
        else if ( Artifact.SCOPE_TEST.equals( artifact.getScope() ) )
        {
            return testScope;
        }
        else if ( Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) )
        {
            return providedScope;
        }
        else if ( Artifact.SCOPE_SYSTEM.equals( artifact.getScope() ) )
        {
            return systemScope;
        }
        else
        {
            return true;
        }
    }
}
