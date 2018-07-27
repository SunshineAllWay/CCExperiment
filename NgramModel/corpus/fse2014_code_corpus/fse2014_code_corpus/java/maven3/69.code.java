package org.apache.maven.artifact.repository.metadata;
class MetadataUtils
{
    public static Metadata cloneMetadata( Metadata src )
    {
        if ( src == null )
        {
            return null;
        }
        return src.clone();
    }
}
