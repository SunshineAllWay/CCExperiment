package org.apache.maven.toolchain;
import org.apache.maven.toolchain.model.ToolchainModel;
public interface ToolchainFactory
{
    ToolchainPrivate createToolchain( ToolchainModel model )
        throws MisconfiguredToolchainException;
    ToolchainPrivate createDefaultToolchain();
}