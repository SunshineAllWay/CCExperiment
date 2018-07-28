package org.apache.maven.plugin;
public class MojoExecutionException
    extends AbstractMojoExecutionException
{
    public MojoExecutionException( Object source, String shortMessage, String longMessage )
    {
        super( shortMessage );
        this.source = source;
        this.longMessage = longMessage;
    }
    public MojoExecutionException( String message, Exception cause )
    {
        super( message, cause );
    }
    public MojoExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }
    public MojoExecutionException( String message )
    {
        super( message );
    }
}
