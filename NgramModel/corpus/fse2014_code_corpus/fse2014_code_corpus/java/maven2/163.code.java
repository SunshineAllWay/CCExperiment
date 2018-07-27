package org.apache.maven.plugin;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.util.StringUtils;
class PluginUtils
{
    public static String constructVersionedKey( Plugin plugin )
    {
        return constructVersionedKey( plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion() );
    }
    public static String constructVersionedKey( PluginDescriptor plugin )
    {
        return constructVersionedKey( plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion() );
    }
    private static String constructVersionedKey( String groupId, String artifactId, String version )
    {
        if ( StringUtils.isEmpty( version ) )
        {
            throw new IllegalStateException( "version for plugin " + groupId + ":" + artifactId + " is not set" );
        }
        String baseVersion = ArtifactUtils.toSnapshotVersion( version );
        StringBuffer key = new StringBuffer( 128 );
        key.append( groupId ).append( ':' ).append( artifactId ).append( ':' ).append( baseVersion );
        return key.toString();
    }
}
