package org.apache.maven.cli;
public class BatchModeDownloadMonitorTest
    extends AbstractConsoleDownloadMonitorTest
{
    protected void setUp()
        throws Exception
    {
        super.setMonitor( new BatchModeDownloadMonitor() );
        super.setUp();
    }
}
