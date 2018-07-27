package org.apache.maven.cli;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.maven.wagon.WagonConstants;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.logging.Logger;
public class ConsoleDownloadMonitor
    extends AbstractConsoleDownloadMonitor
{
    private Mapdownloads;
    private int maxLength;
    public ConsoleDownloadMonitor( Logger logger )
    {
        super( logger );
        downloads = new LinkedHashMap();
    }
    public ConsoleDownloadMonitor()
    {
        downloads = new LinkedHashMap();
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
    public synchronized void transferProgress( TransferEvent transferEvent, byte[] buffer, int length )
    {
        Resource resource = transferEvent.getResource();
        if ( !downloads.containsKey( resource ) )
        {
            downloads.put( resource, new Long( length ) );
        }
        else
        {
            Long complete = (Long) downloads.get( resource );
            complete = new Long( complete.longValue() + length );
            downloads.put( resource, complete );
        }
        StringBuffer buf = new StringBuffer();
        for ( Iterator i = downloads.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) i.next();
            Long complete = (Long) entry.getValue();
            String status =
                getDownloadStatusForResource( complete.longValue(), ( (Resource) entry.getKey() ).getContentLength() );
            buf.append( status );
            if ( i.hasNext() )
            {
                buf.append( " " );
            }
        }
        if ( buf.length() > maxLength )
        {
            maxLength = buf.length();
        }
        out.print( buf.toString() + "\r" );
    }
    String getDownloadStatusForResource( long progress, long total )
    {
        if ( total >= 1024 )
        {
            return ( progress / 1024 ) + "/" + ( total == WagonConstants.UNKNOWN_LENGTH ? "?" : ( total / 1024 ) + "K" );
        }
        else
        {
            return progress + "/" + ( total == WagonConstants.UNKNOWN_LENGTH ? "?" : total + "b" );
        }
    }
    public synchronized void transferCompleted( TransferEvent transferEvent )
    {
        StringBuffer line = new StringBuffer( createCompletionLine( transferEvent ) );
        while ( line.length() < maxLength )
        {
            line.append( " " );
        }
        maxLength = 0;
        out.println( line );
        downloads.remove( transferEvent.getResource() );
    }
}
