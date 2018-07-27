package org.apache.maven.plugin;
public abstract class AbstractMojoExecutionException
    extends Exception
{
    protected Object source;
    protected String longMessage;
    public AbstractMojoExecutionException( String message )
    {
        super( message );
    }
    public AbstractMojoExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }
    public String getLongMessage()
    {
        return longMessage;
    }
    public Object getSource()
    {
        return source;
    }
}
