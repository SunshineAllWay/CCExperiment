package org.apache.maven.repository.metadata;
public class MetadataGraphTransformationException
    extends Exception
{
	private static final long serialVersionUID = -4029897098314019152L;
    public MetadataGraphTransformationException()
    {
    }
    public MetadataGraphTransformationException( String message )
    {
        super( message );
    }
    public MetadataGraphTransformationException( Throwable cause )
    {
        super( cause );
    }
    public MetadataGraphTransformationException( String message, Throwable cause )
    {
        super( message, cause );
    }
}
