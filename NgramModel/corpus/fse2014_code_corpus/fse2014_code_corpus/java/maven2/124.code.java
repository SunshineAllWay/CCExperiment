package org.apache.maven.cli;
import org.apache.maven.wagon.events.TransferEvent;
import org.codehaus.plexus.logging.Logger;
public class BatchModeDownloadMonitor
    extends AbstractConsoleDownloadMonitor
{
    public BatchModeDownloadMonitor( Logger logger )
    {
        super( logger );
    }
    public BatchModeDownloadMonitor()
    {
    }
    public void transferInitiated( TransferEvent transferEvent )
    {
        String message = transferEvent.getRequestType() == TransferEvent.REQUEST_PUT ? "Uploading" : "Downloading";
        String url = transferEvent.getWagon().getRepository().getUrl();
        out.println( message + ": " + url + "/" + transferEvent.getResource().getName() );
    }
}
