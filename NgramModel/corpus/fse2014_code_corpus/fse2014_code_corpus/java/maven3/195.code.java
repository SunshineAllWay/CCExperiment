package org.apache.maven.artifact.resolver;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.wagon.observers.AbstractTransferListener;
public class TestTransferListener
    extends AbstractTransferListener
{
    private final List<String> transfers = new ArrayList<String>();
    public List<String> getTransfers()
    {
        return transfers;
    }
    public void addTransfer( String name )
    {
        transfers.add( name );
    }
}
