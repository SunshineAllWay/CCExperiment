package org.apache.maven.plugin.descriptor;
public class InvalidParameterException
    extends InvalidPluginDescriptorException
{
    public InvalidParameterException( String element, int i )
    {
        super( "The " + element + " element in parameter # " + i + " is invalid. It cannot be null." );
    }
    public InvalidParameterException( String message, Throwable cause )
    {
        super( message, cause );
    }
}