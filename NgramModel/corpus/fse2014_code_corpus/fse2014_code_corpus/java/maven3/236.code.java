package org.apache.maven.repository.legacy;
import org.apache.maven.wagon.Wagon;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = Wagon.class, hint = "perlookup", instantiationStrategy = "per-lookup" )
public class PerLookupWagon
    extends WagonMock
{
    public String[] getSupportedProtocols()
    {
        return new String[] { "perlookup" };
    }
}
