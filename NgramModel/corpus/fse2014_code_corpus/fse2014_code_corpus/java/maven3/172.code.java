package org.apache.maven.repository.metadata;
public class MetadataResolutionException
    extends Exception
{
    public MetadataResolutionException()
    {
    }
    public MetadataResolutionException( String message )
    {
        super( message );
    }
    public MetadataResolutionException( Throwable cause )
    {
        super( cause );
    }
    public MetadataResolutionException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
