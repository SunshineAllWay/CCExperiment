package org.apache.maven.artifact.manager;
public class WagonC
    extends WagonMock
{
    public String[] getSupportedProtocols()
    {
        return new String[]{ "c" };
    }
}
