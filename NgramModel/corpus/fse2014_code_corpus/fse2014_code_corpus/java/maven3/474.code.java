package org.apache.maven.project;
public class DependencyResolutionException
    extends Exception
{
    private DependencyResolutionResult result;
    public DependencyResolutionException( DependencyResolutionResult result, String message, Throwable cause )
    {
        super( message, cause );
        this.result = result;
    }
    public DependencyResolutionResult getResult()
    {
        return result;
    }
}
