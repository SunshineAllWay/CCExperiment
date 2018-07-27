package org.apache.maven.repository.legacy;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.component.annotations.Component;
@Component(role=Wagon.class,hint="a")
public class WagonA
    extends WagonMock
{
    public String[] getSupportedProtocols()
    {
        return new String[]{ "a" };
    }
}
