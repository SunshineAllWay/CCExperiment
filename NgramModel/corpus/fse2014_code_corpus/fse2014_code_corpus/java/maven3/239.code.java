package org.apache.maven.repository.legacy;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.component.annotations.Component;
@Component(role=Wagon.class,hint="b")
public class WagonB
    extends WagonMock
{
    public String[] getSupportedProtocols()
    {
        return new String[]{ "b1", "b2" };
    }
}
