package org.apache.maven.toolchain;
import org.apache.maven.execution.MavenSession;
public interface ToolchainManagerPrivate
{
    ToolchainPrivate[] getToolchainsForType( String type, MavenSession context )
        throws MisconfiguredToolchainException;
    void storeToolchainToBuildContext( ToolchainPrivate toolchain, MavenSession context );
}
