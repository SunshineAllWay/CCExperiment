package org.apache.maven.toolchain;
import java.io.File;
import org.apache.maven.toolchain.model.PersistedToolchains;
public interface ToolchainsBuilder
{
    PersistedToolchains build( File userToolchainsFile )
        throws MisconfiguredToolchainException;
}
