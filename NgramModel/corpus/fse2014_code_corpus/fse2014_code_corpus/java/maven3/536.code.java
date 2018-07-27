package org.apache.maven.toolchain;
public interface Toolchain
{
    String getType();
    String findTool( String toolName );
}