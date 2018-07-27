package org.apache.maven.reporting;
public class MavenReportException extends Exception
{
    public static final long serialVersionUID = -6200353563231163785L;
    public MavenReportException( String msg )
    {
        super( msg );
    }
    public MavenReportException( String msg, Exception e )
    {
        super( msg, e );
    }
}
