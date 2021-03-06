package org.apache.maven.cli;
import org.apache.maven.wagon.resource.Resource;
import org.apache.maven.wagon.WagonConstants;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
public class ConsoleDownloadMonitorTest
    extends AbstractConsoleDownloadMonitorTest
{
    ByteArrayOutputStream bout;
    protected void setUp()
        throws Exception
    {
        super.setMonitor( new ConsoleDownloadMonitor() );
        super.setUp();
        bout = new ByteArrayOutputStream();
        monitor.out = new PrintStream( bout );
    }
    public void testTransferProgress()
        throws Exception
    {
        byte[] buffer = new byte[1024];
        monitor.transferProgress( new TransferEventMock( new Resource(), 10000 ), buffer, 1024 );
        assertEquals( "1/9K\r", new String( bout.toByteArray() ) );
    }
    public void testTransferProgressTwoFiles()
        throws Exception
    {
        byte[] buffer = new byte[2048];
        monitor.transferProgress( new TransferEventMock( new Resource( "foo" ), 10000 ), buffer, 1024 );
        assertEquals( "1/9K\r", new String( bout.toByteArray() ) );
        bout.reset();
        monitor.transferProgress( new TransferEventMock( new Resource( "bar" ), 10000 ), buffer, 2048 );
        assertEquals( "1/9K 2/9K\r", new String( bout.toByteArray() ) );
        bout.reset();
        monitor.transferProgress( new TransferEventMock( new Resource( "bar" ), 10000 ), buffer, 2048 );
        assertEquals( "1/9K 4/9K\r", new String( bout.toByteArray() ) );
        bout.reset();
        monitor.transferProgress( new TransferEventMock( new Resource( "foo" ), 10000 ), buffer, 2048 );
        assertEquals( "3/9K 4/9K\r", new String( bout.toByteArray() ) );
    }
    public void testGetDownloadStatusForResource()
    {
        ConsoleDownloadMonitor cm = (ConsoleDownloadMonitor) monitor;
        assertEquals( "200/400b", cm.getDownloadStatusForResource( 200, 400 ) );
        assertEquals( "1/2K", cm.getDownloadStatusForResource( 1024, 2048 ) );
        assertEquals( "0/2K", cm.getDownloadStatusForResource( 10, 2048 ) );
        assertEquals( "10/?", cm.getDownloadStatusForResource( 10, WagonConstants.UNKNOWN_LENGTH ) );
        assertEquals( "1024/?", cm.getDownloadStatusForResource( 1024, WagonConstants.UNKNOWN_LENGTH ) );
    }
}
