package org.apache.maven.artifact;
public class DependencyResolutionRequiredException
    extends Exception
{
    public DependencyResolutionRequiredException( Artifact artifact )
    {
        super( "Attempted to access the artifact " + artifact + "; which has not yet been resolved" );
    }
}
