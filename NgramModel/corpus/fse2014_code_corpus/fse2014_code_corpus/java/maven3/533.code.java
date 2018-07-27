package org.apache.maven.toolchain;
public class MisconfiguredToolchainException
    extends Exception
{
    public MisconfiguredToolchainException( String message )
    {
        super( message );
    }
    public MisconfiguredToolchainException( String message, Throwable orig )
    {
        super( message, orig );
    }
}