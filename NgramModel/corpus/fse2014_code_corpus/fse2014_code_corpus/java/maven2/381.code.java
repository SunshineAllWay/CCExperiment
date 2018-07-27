package org.apache.maven.toolchain;
import java.util.Map;
import org.apache.maven.toolchain.model.ToolchainModel;
public interface ToolchainPrivate
    extends Toolchain
{
    boolean matchesRequirements( Map requirements );
    ToolchainModel getModel();
}