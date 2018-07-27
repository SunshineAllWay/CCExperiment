package org.apache.maven.cli;
import org.apache.maven.wagon.WagonConstants;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.events.TransferListener;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import java.io.PrintStream;
public abstract class AbstractConsoleDownloadMonitor
    extends AbstractLogEnabled
    implements TransferListener
{
    private Logger logger;
    PrintStream out = System.out;
    public AbstractConsoleDownloadMonitor()
    {
    }
    public AbstractConsoleDownloadMonitor( Logger logger )
    {
        this.logger = logger;
    }
    public void transferInitiated( TransferEvent transferEvent )
    {
        String message = transferEvent.getRequestType() == TransferEvent.REQUEST_PUT ? "Uploading" : "Downloading";
        String url = transferEvent.getWagon().getRepository().getUrl();
        out.println( message + ": " + url + "/" + transferEvent.getResource().getName() );
    }
    public void transferStarted( TransferEvent transferEvent )
    {
    }
    public void transferProgress( TransferEvent transferEvent, byte[] buffer, int length )
    {
    }
    public void transferCompleted( TransferEvent transferEvent )
    {
        String line = createCompletionLine( transferEvent );
        out.println( line );
    }
    protected String createCompletionLine( TransferEvent transferEvent )
    {
        String line;
        long contentLength = transferEvent.getResource().getContentLength();
        if ( contentLength != WagonConstants.UNKNOWN_LENGTH )
        {
            StringBuffer buf = new StringBuffer();
            String type = ( transferEvent.getRequestType() == TransferEvent.REQUEST_PUT ? "uploaded" : "downloaded" );
            buf.append( contentLength >= 1024 ? ( contentLength / 1024 ) + "K" : contentLength + "b" );
            String name = transferEvent.getResource().getName();
            name = name.substring( name.lastIndexOf( '/' ) + 1, name.length() );
            buf.append( " " );
            buf.append( type );
            buf.append( "  (" );
            buf.append( name );
            buf.append( ")" );
            line = buf.toString();
        }
        else
        {
            line = "";
        }
        return line;
    }
    public void transferError( TransferEvent transferEvent )
    {
        if ( logger != null )
        {
            Exception exception = transferEvent.getException();
            logger.debug( exception.getMessage(), exception );
        }
    }
    public void debug( String message )
    {
        if ( logger != null )
        {
            logger.debug( message );
        }
    }
}
