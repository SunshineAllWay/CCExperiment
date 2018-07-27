package org.apache.maven.reporting;
public class MavenReportException extends Exception
{
    public MavenReportException( String msg )
    {
        super( msg );
    }
    public MavenReportException( String msg, Exception e )
    {
        super( msg, e );
    }
}
