package org.apache.maven.artifact.manager;
public class WagonAWithImplementationHint
    extends WagonMock
{
    public String[] getSupportedProtocols()
    {
        return new String[]{ "a" };
    }
}
