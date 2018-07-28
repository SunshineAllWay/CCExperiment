package org.apache.maven.project;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.model.building.ModelCache;
class ReactorModelCache
    implements ModelCache
{
    private final Map<CacheKey, Object> models = new HashMap<CacheKey, Object>( 256 );
    public Object get( String groupId, String artifactId, String version, String tag )
    {
        return models.get( new CacheKey( groupId, artifactId, version, tag ) );
    }
    public void put( String groupId, String artifactId, String version, String tag, Object data )
    {
        models.put( new CacheKey( groupId, artifactId, version, tag ), data );
    }
    private static final class CacheKey
    {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String tag;
        private final int hashCode;
        public CacheKey( String groupId, String artifactId, String version, String tag )
        {
            this.groupId = ( groupId != null ) ? groupId : "";
            this.artifactId = ( artifactId != null ) ? artifactId : "";
            this.version = ( version != null ) ? version : "";
            this.tag = ( tag != null ) ? tag : "";
            int hash = 17;
            hash = hash * 31 + this.groupId.hashCode();
            hash = hash * 31 + this.artifactId.hashCode();
            hash = hash * 31 + this.version.hashCode();
            hash = hash * 31 + this.tag.hashCode();
            hashCode = hash;
        }
        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( !( obj instanceof CacheKey ) )
            {
                return false;
            }
            CacheKey that = (CacheKey) obj;
            return artifactId.equals( that.artifactId ) && groupId.equals( that.groupId )
                && version.equals( that.version ) && tag.equals( that.tag );
        }
        @Override
        public int hashCode()
        {
            return hashCode;
        }
    }
}
