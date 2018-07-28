package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException;
import org.apache.maven.plugin.prefix.PluginPrefixRequest;
import org.apache.maven.plugin.prefix.PluginPrefixResolver;
import org.apache.maven.plugin.prefix.PluginPrefixResult;
import org.sonatype.aether.repository.ArtifactRepository;
public class PluginPrefixResolverStub
    implements PluginPrefixResolver
{
    public PluginPrefixResult resolve( PluginPrefixRequest request )
        throws NoPluginFoundForPrefixException
    {
        return new PluginPrefixResult()
        {
            public String getGroupId()
            {
                return "com.foobar";
            }
            public String getArtifactId()
            {
                return "bazbaz";
            }
            public ArtifactRepository getRepository()
            {
                return null;
            }
        };
    }
}
