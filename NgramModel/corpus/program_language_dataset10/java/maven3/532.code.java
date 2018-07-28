package org.apache.maven.toolchain;
import java.io.File;
import java.io.Reader;
import org.apache.maven.toolchain.model.PersistedToolchains;
import org.apache.maven.toolchain.model.io.xpp3.MavenToolchainsXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
@Component( role = ToolchainsBuilder.class, hint = "default" )
public class DefaultToolchainsBuilder
    implements ToolchainsBuilder
{
    @Requirement
    private Logger logger;
    public PersistedToolchains build( File userToolchainsFile )
        throws MisconfiguredToolchainException
    {
        PersistedToolchains toolchains = null;
        if ( userToolchainsFile != null && userToolchainsFile.isFile() )
        {
            Reader in = null;
            try
            {
                in = ReaderFactory.newXmlReader( userToolchainsFile );
                toolchains = new MavenToolchainsXpp3Reader().read( in );
            }
            catch ( Exception e )
            {
                throw new MisconfiguredToolchainException( "Cannot read toolchains file at "
                    + userToolchainsFile.getAbsolutePath(), e );
            }
            finally
            {
                IOUtil.close( in );
            }
        }
        else if ( userToolchainsFile != null )
        {
            logger.debug( "Toolchains configuration was not found at " + userToolchainsFile );
        }
        return toolchains;
    }
}
