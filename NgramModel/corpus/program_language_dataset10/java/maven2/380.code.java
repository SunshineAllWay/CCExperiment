package org.apache.maven.toolchain;
import org.apache.maven.execution.MavenSession;
public interface ToolchainManagerPrivate
{
    String ROLE = ToolchainManagerPrivate.class.getName();
    ToolchainPrivate[] getToolchainsForType( String type )
        throws MisconfiguredToolchainException;
    void storeToolchainToBuildContext( ToolchainPrivate toolchain,
                                       MavenSession context );
}
