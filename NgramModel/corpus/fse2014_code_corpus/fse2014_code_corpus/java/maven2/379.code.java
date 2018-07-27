package org.apache.maven.toolchain;
import org.apache.maven.execution.MavenSession;
public interface ToolchainManager
{
    String ROLE = ToolchainManager.class.getName();
    Toolchain getToolchainFromBuildContext( String type,
                                            MavenSession context );
}