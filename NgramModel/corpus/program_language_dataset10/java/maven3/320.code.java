package org.apache.maven.configuration;
public class BeanConfigurationException
    extends Exception
{
    public BeanConfigurationException( String message )
    {
        super( message );
    }
    public BeanConfigurationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
